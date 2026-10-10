package org.gephi.branding.desktop.reporter;

import io.sentry.Attachment;
import io.sentry.Hint;
import io.sentry.Sentry;
import io.sentry.SentryLevel;
import io.sentry.exception.ExceptionMechanismException;
import io.sentry.protocol.Mechanism;
import io.sentry.protocol.User;
import java.lang.management.LockInfo;
import java.lang.management.ManagementFactory;
import java.lang.management.MonitorInfo;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.nio.charset.StandardCharsets;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.LongSupplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.gephi.branding.desktop.SentryIdentity;
import org.openide.util.NbPreferences;

/**
 * Detects a frozen Swing event dispatch thread and reports it to Sentry. A hang or deadlock never
 * throws, so without this nothing reaches Sentry and the session counts as crash-free.
 * <p>
 * A daemon thread posts a no-op to the EDT every {@link #INTERVAL_MS}. If one is still pending
 * after {@link #THRESHOLD_MS}, one event is captured with the EDT stack, a thread dump and any
 * deadlock. Nothing more is reported until the EDT runs a probe again. Time during which the
 * watchdog itself did not run (machine asleep, process suspended) is not counted.
 * <p>
 * Sent automatically only when the user opted in to automatic crash reports, as
 * {@link ReporterHandler} does. The report dialog is never shown, since the EDT is stuck.
 */
public final class EdtWatchdog {

    static final long INTERVAL_MS = 2_000;
    static final long THRESHOLD_MS = 10_000;
    /** A gap between two ticks larger than this means the watchdog was not running. */
    static final long MAX_TICK_GAP_MS = 3 * INTERVAL_MS;
    static final int MAX_REPORTS_PER_SESSION = 3;

    private static final Logger LOGGER = Logger.getLogger(EdtWatchdog.class.getName());
    private static EdtWatchdog instance;

    interface HangReporter {

        void report(Thread edt, long hungForMs);
    }

    private final Consumer<Runnable> poster;
    private final LongSupplier monotonicClock;
    private final LongSupplier wallClock;
    private final BooleanSupplier consent;
    private final HangReporter reporter;

    private volatile Thread edt;
    private volatile Thread thread;

    // Owned by the watchdog thread
    private Probe probe;
    private long probeAge;
    private long lastMonotonic = Long.MIN_VALUE;
    private long lastWall;
    private boolean episodeReported;
    private int reportCount;

    EdtWatchdog(Consumer<Runnable> poster, LongSupplier monotonicClock, LongSupplier wallClock,
                BooleanSupplier consent, HangReporter reporter) {
        this.poster = poster;
        this.monotonicClock = monotonicClock;
        this.wallClock = wallClock;
        this.consent = consent;
        this.reporter = reporter;
    }

    /**
     * Starts the watchdog. Call once the main window is shown, preferably from the EDT.
     */
    public static synchronized void install() {
        if (instance != null
            || !"true".equals(System.getProperty("org.gephi.crashReporter.enabled", "true"))
            || !"true".equals(System.getProperty("org.gephi.edtWatchdog.enabled", "true"))
            || isDebuggerAttached()) {
            return;
        }
        instance = new EdtWatchdog(SwingUtilities::invokeLater,
            () -> System.nanoTime() / 1_000_000L,
            System::currentTimeMillis,
            EdtWatchdog::hasConsent,
            EdtWatchdog::captureHang);
        instance.start();
    }

    public static synchronized void uninstall() {
        if (instance != null) {
            instance.stop();
            instance = null;
        }
    }

    static boolean hasConsent() {
        java.util.prefs.Preferences prefs = NbPreferences.forModule(ReportController.class);
        return !prefs.getBoolean(ReportController.DISABLE_ALL_TRACKING, false)
            && prefs.getBoolean(ReportController.SEND_CRASH_REPORTS, ReportController.DEFAULT_SEND_CRASH_REPORTS);
    }

    private static boolean isDebuggerAttached() {
        try {
            return ManagementFactory.getRuntimeMXBean().getInputArguments().stream()
                .anyMatch(arg -> arg.startsWith("-agentlib:jdwp") || arg.startsWith("-Xrunjdwp"));
        } catch (Exception e) {
            return false;
        }
    }

    void start() {
        if (SwingUtilities.isEventDispatchThread()) {
            edt = Thread.currentThread();
        }
        Thread t = new Thread(this::run, "EDT Watchdog");
        t.setDaemon(true);
        thread = t;
        t.start();
    }

    void stop() {
        Thread t = thread;
        thread = null;
        if (t != null) {
            t.interrupt();
        }
    }

    private void run() {
        while (thread == Thread.currentThread()) {
            try {
                Thread.sleep(INTERVAL_MS);
            } catch (InterruptedException e) {
                return;
            }
            try {
                tick();
            } catch (Throwable t) {
                // Logged without the throwable so that ReporterHandler doesn't turn it into a crash report
                LOGGER.log(Level.INFO, "EDT watchdog failure: {0}", t.toString());
            }
        }
    }

    /**
     * One watchdog step. Package-private for tests.
     */
    void tick() {
        long monotonic = monotonicClock.getAsLong();
        long wall = wallClock.getAsLong();
        boolean firstTick = lastMonotonic == Long.MIN_VALUE;
        long elapsed = firstTick ? 0 : monotonic - lastMonotonic;
        long wallElapsed = firstTick ? 0 : wall - lastWall;
        lastMonotonic = monotonic;
        lastWall = wall;

        // Depending on the OS, the monotonic clock may or may not advance during sleep, but the wall
        // clock does. Either way, a large gap means the watchdog didn't run and can't tell how long the
        // EDT was really busy: start measuring again.
        boolean gap = elapsed > MAX_TICK_GAP_MS || wallElapsed > MAX_TICK_GAP_MS || elapsed < 0;

        if (probe != null) {
            if (probe.done) {
                if (episodeReported) {
                    LOGGER.log(Level.INFO, "EDT responsive again after about {0} ms", probeAge);
                }
                probe = null;
                episodeReported = false;
            } else {
                probeAge = gap ? 0 : probeAge + elapsed;
                if (!episodeReported && probeAge >= THRESHOLD_MS) {
                    episodeReported = true;
                    report();
                }
                return;
            }
        }

        probe = new Probe();
        probeAge = 0;
        poster.accept(probe);
    }

    private void report() {
        if (reportCount >= MAX_REPORTS_PER_SESSION || !consent.getAsBoolean()) {
            return;
        }
        reportCount++;
        reporter.report(edt, probeAge);
    }

    private static Thread findEdt() {
        for (Thread t : Thread.getAllStackTraces().keySet()) {
            if (t.getName().startsWith("AWT-EventQueue")) {
                return t;
            }
        }
        return null;
    }

    private final class Probe implements Runnable {

        private volatile boolean done;

        @Override
        public void run() {
            // The EDT can be replaced after an uncaught exception, so refresh it
            edt = Thread.currentThread();
            done = true;
        }
    }

    /**
     * Synthetic exception carrying the EDT stack at the time of the hang.
     */
    static final class EdtHangException extends RuntimeException {

        EdtHangException(String message, StackTraceElement[] stack) {
            super(message, null, false, true);
            setStackTrace(stack);
        }
    }

    /**
     * @param edt the event dispatch thread, or {@code null} if no probe has run on it yet
     */
    static void captureHang(Thread probedEdt, long hungForMs) {
        Thread edt = probedEdt != null ? probedEdt : findEdt();
        if (edt == null || !Sentry.isEnabled()) {
            return;
        }
        long[] deadlocked = findDeadlockedThreads();
        int deadlockedCount = deadlocked != null ? deadlocked.length : 0;
        boolean edtDeadlocked = deadlocked != null
            && java.util.Arrays.stream(deadlocked).anyMatch(id -> id == edt.getId());

        EdtHangException hang = new EdtHangException(
            edtDeadlocked ? "Event dispatch thread deadlocked" : "Event dispatch thread unresponsive",
            edt.getStackTrace());
        Mechanism mechanism = new Mechanism();
        mechanism.setType("edt_hang");
        mechanism.setHandled(true);
        // Group by the EDT stack, not by the synthetic exception type and message
        mechanism.setSynthetic(true);
        Throwable throwable = new ExceptionMechanismException(mechanism, hang, edt, true);

        String dump = threadDump(deadlocked);
        Hint hint = Hint.withAttachment(
            new Attachment(dump.getBytes(StandardCharsets.UTF_8), "threads.txt", "text/plain"));

        User user = new User();
        user.setId(SentryIdentity.getOrCreateDistinctId());

        Sentry.withIsolationScope(scope -> {
            scope.setUser(user);
            scope.setLevel(edtDeadlocked ? SentryLevel.FATAL : SentryLevel.ERROR);
            scope.setTag("hang", "edt");
            scope.setTag("deadlock", Boolean.toString(deadlockedCount > 0));
            scope.setTag("OS", System.getProperty("os.name", "unknown") + ", "
                + System.getProperty("os.version", "unknown") + ", " + System.getProperty("os.arch", "unknown"));
            scope.setExtra("hang_duration_ms", Long.toString(hungForMs));
            scope.setExtra("edt_state", String.valueOf(edt.getState()));
            scope.setExtra("deadlocked_threads", Integer.toString(deadlockedCount));
            Sentry.captureException(throwable, hint);
        });
        LOGGER.log(Level.INFO, "EDT unresponsive for {0} ms, reported", hungForMs);
    }

    private static long[] findDeadlockedThreads() {
        ThreadMXBean mx = ManagementFactory.getThreadMXBean();
        try {
            return mx.isSynchronizerUsageSupported() ? mx.findDeadlockedThreads() : mx.findMonitorDeadlockedThreads();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Full thread dump, deadlocked threads first, with paths removed from thread and lock names.
     * Unlike {@link ThreadInfo#toString()}, stacks are not truncated.
     */
    static String threadDump(long[] deadlocked) {
        ThreadMXBean mx = ManagementFactory.getThreadMXBean();
        StringBuilder sb = new StringBuilder();
        if (deadlocked != null && deadlocked.length > 0) {
            sb.append("Deadlocked threads:\n");
            for (ThreadInfo info : mx.getThreadInfo(deadlocked, mx.isObjectMonitorUsageSupported(),
                mx.isSynchronizerUsageSupported())) {
                appendThread(sb, info);
            }
            sb.append("\nAll threads:\n");
        }
        for (ThreadInfo info : mx.dumpAllThreads(mx.isObjectMonitorUsageSupported(),
            mx.isSynchronizerUsageSupported())) {
            appendThread(sb, info);
        }
        return SentryScrubber.scrub(sb.toString());
    }

    private static void appendThread(StringBuilder sb, ThreadInfo info) {
        if (info == null) {
            return;
        }
        sb.append('"').append(info.getThreadName()).append("\" #").append(info.getThreadId());
        if (info.isDaemon()) {
            sb.append(" daemon");
        }
        sb.append(' ').append(info.getThreadState());
        if (info.getLockName() != null) {
            sb.append(" on ").append(info.getLockName());
        }
        if (info.getLockOwnerName() != null) {
            sb.append(" owned by \"").append(info.getLockOwnerName()).append("\" #").append(info.getLockOwnerId());
        }
        if (info.isSuspended()) {
            sb.append(" (suspended)");
        }
        if (info.isInNative()) {
            sb.append(" (in native)");
        }
        sb.append('\n');
        StackTraceElement[] stack = info.getStackTrace();
        MonitorInfo[] monitors = info.getLockedMonitors();
        for (int i = 0; i < stack.length; i++) {
            sb.append("\tat ").append(stack[i]).append('\n');
            if (i == 0 && info.getLockInfo() != null) {
                sb.append("\t-  waiting on ").append(info.getLockInfo()).append('\n');
            }
            for (MonitorInfo mi : monitors) {
                if (mi.getLockedStackDepth() == i) {
                    sb.append("\t-  locked ").append(mi).append('\n');
                }
            }
        }
        LockInfo[] synchronizers = info.getLockedSynchronizers();
        if (synchronizers.length > 0) {
            sb.append("\tLocked synchronizers:\n");
            for (LockInfo li : synchronizers) {
                sb.append("\t- ").append(li).append('\n');
            }
        }
        sb.append('\n');
    }
}
