package org.gephi.releasetests;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.swing.SwingUtilities;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphModel;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.visualization.VizController;
import org.gephi.visualization.api.VisualizationController;
import org.gephi.viz.engine.VizEngine;
import org.openide.util.Lookup;

/**
 * Checks once a second, while a suite runs, that Gephi keeps responding:
 * <ul>
 *     <li>the Event Dispatch Thread runs a posted no-op;</li>
 *     <li>a new thread gets the graph read lock, as the Overview's world updaters must;</li>
 *     <li>the Overview draws frames and receives world updates, while the suite expects it to render;</li>
 *     <li>no threads are deadlocked.</li>
 * </ul>
 * A wait past the soft limit is a slowdown, past the hard limit a violation, both reported with a thread dump while
 * the wait lasts. A wait that crosses a limit and ends between two checks is reported when it ends, without a dump.
 */
final class Monitor {

    static final long SOFT_LIMIT_MS = 2_000;
    static final long HARD_LIMIT_MS = 10_000;
    private static final long TICK_MS = 1_000;

    private final Results results;
    private final ThreadMXBean threads = ManagementFactory.getThreadMXBean();
    private final Watch edt = new Watch("edt", "The Event Dispatch Thread hasn't run a posted no-op");
    private final Watch readLock = new Watch("read-lock", "A new reader has waited for the graph read lock");
    private final Watch frames = new Watch("render", "The Overview hasn't drawn a frame");
    private final Watch worldUpdates =
        new Watch("world-update", "The Overview has drawn frames without receiving a world update");
    private volatile boolean expectRendering;
    private volatile Thread thread;

    // Owned by the monitor thread
    private VizEngine<?, ?> engine;
    private long lastFrameCount;
    private long lastWorldUpdateCount;
    private long framesDrawn;
    private long worldUpdatesReceived;
    private boolean deadlockReported;

    Monitor(Results results) {
        this.results = results;
    }

    private static long nowMs() {
        return TimeUnit.NANOSECONDS.toMillis(System.nanoTime());
    }

    /**
     * Sets whether the Overview must be drawing the current workspace, from when a graph is shown until the project
     * is closed.
     */
    void setExpectRendering(boolean expectRendering) {
        this.expectRendering = expectRendering;
    }

    void start() {
        Thread t = new Thread(this::run, "Release Tests Monitor");
        t.setDaemon(true);
        thread = t;
        t.start();
    }

    /**
     * Stops after the current check, without interrupting it so that a thread dump being written isn't lost.
     */
    void stop() throws InterruptedException {
        Thread t = thread;
        thread = null;
        if (t != null) {
            t.join(TICK_MS * 5);
        }
        long now = nowMs();
        results.metric("edtMaxMs", edt.maxMs(now));
        results.metric("readLockMaxMs", readLock.maxMs(now));
        results.metric("frameGapMaxMs", frames.maxMs(now));
        results.metric("worldUpdateGapMaxMs", worldUpdates.maxMs(now));
        results.metric("framesDrawn", framesDrawn);
        results.metric("worldUpdatesReceived", worldUpdatesReceived);
    }

    private void run() {
        while (thread == Thread.currentThread()) {
            try {
                Thread.sleep(TICK_MS);
            } catch (InterruptedException ex) {
                return;
            }
            try {
                // Each check reads the time again, as a thread dump written by the previous one takes a while
                probeEdt();
                probeReadLock();
                checkRendering();
                checkDeadlocks();
            } catch (RuntimeException ex) {
                results.violation("monitor", "The monitor failed: " + Results.stackTrace(ex), null);
            }
        }
    }

    private void probeEdt() {
        if (edt.isWaiting()) {
            edt.check(nowMs());
            return;
        }
        edt.start(nowMs());
        SwingUtilities.invokeLater(() -> edt.end(nowMs()));
    }

    private void probeReadLock() {
        if (readLock.isWaiting()) {
            readLock.check(nowMs());
            return;
        }
        Graph graph = currentGraph();
        if (graph == null) {
            return;
        }
        readLock.start(nowMs());
        Thread probe = new Thread(() -> {
            try {
                graph.readLock();
                graph.readUnlock();
            } finally {
                readLock.end(nowMs());
            }
        }, "Release Tests Read Lock Probe");
        probe.setDaemon(true);
        probe.start();
    }

    private void checkRendering() {
        long now = nowMs();
        if (!expectRendering) {
            engine = null;
            frames.end(now);
            worldUpdates.end(now);
            return;
        }
        VizEngine<?, ?> current = currentEngine();
        long frameCount = current != null ? current.getFrameCount() : 0;
        long worldUpdateCount = current != null ? current.getWorldUpdateCount() : 0;
        if (current != engine || !frames.isWaiting()) {
            // Rendering just became expected, or the Overview has a new engine
            engine = current;
            lastFrameCount = frameCount;
            lastWorldUpdateCount = worldUpdateCount;
            frames.start(now);
            worldUpdates.restart(now);
            return;
        }
        boolean drawing = frameCount != lastFrameCount;
        if (drawing) {
            framesDrawn += frameCount - lastFrameCount;
            lastFrameCount = frameCount;
            frames.start(now);
        }
        if (worldUpdateCount != lastWorldUpdateCount) {
            worldUpdatesReceived += worldUpdateCount - lastWorldUpdateCount;
            lastWorldUpdateCount = worldUpdateCount;
            worldUpdates.start(now);
        } else if (!drawing) {
            // Without frames, missing world updates are the frames' failure, so only time spent drawing counts
            worldUpdates.restart(now);
        }
        frames.check(now);
        worldUpdates.check(now);
    }

    private void checkDeadlocks() {
        long[] ids = threads.findDeadlockedThreads();
        if (ids == null || deadlockReported) {
            return;
        }
        deadlockReported = true;
        String names = Arrays.stream(threads.getThreadInfo(ids))
            .filter(info -> info != null)
            .map(ThreadInfo::getThreadName)
            .collect(Collectors.joining("\", \"", "\"", "\""));
        results.violation("deadlock", "Deadlocked threads: " + names, ThreadDumps.write(results, "deadlock"));
    }

    private static Graph currentGraph() {
        Workspace workspace = Lookup.getDefault().lookup(ProjectController.class).getCurrentWorkspace();
        if (workspace == null) {
            return null;
        }
        // From the lookup, as GraphController would create a model the workspace doesn't have yet
        GraphModel graphModel = workspace.getLookup().lookup(GraphModel.class);
        return graphModel != null ? graphModel.getGraph() : null;
    }

    private static VizEngine<?, ?> currentEngine() {
        VisualizationController controller = Lookup.getDefault().lookup(VisualizationController.class);
        if (controller instanceof VizController vizController) {
            return vizController.getEngine().orElse(null);
        }
        return null;
    }

    /**
     * How long something has been waiting: a probe to complete, or progress to be made. A wait is reported once
     * past the soft limit and once past the hard limit.
     */
    private final class Watch {

        private final String kind;
        private final String description;
        private long since = -1;
        private boolean slow;
        private boolean failed;
        private long maxMs;

        Watch(String kind, String description) {
            this.kind = kind;
            this.description = description;
        }

        /**
         * Starts a new wait, ending the current one.
         */
        void start(long now) {
            end(now);
            synchronized (this) {
                since = now;
            }
        }

        /**
         * Starts a new wait, dropping the current one without measuring or reporting it.
         */
        synchronized void restart(long now) {
            since = now;
            slow = false;
            failed = false;
        }

        void end(long now) {
            long waitedMs;
            boolean unreported;
            synchronized (this) {
                if (since < 0) {
                    return;
                }
                waitedMs = now - since;
                maxMs = Math.max(maxMs, waitedMs);
                unreported = waitedMs >= HARD_LIMIT_MS ? !failed : waitedMs >= SOFT_LIMIT_MS && !slow;
                since = -1;
                slow = false;
                failed = false;
            }
            if (unreported) {
                report(waitedMs, false);
            }
        }

        synchronized boolean isWaiting() {
            return since >= 0;
        }

        synchronized long maxMs(long now) {
            return since >= 0 ? Math.max(maxMs, now - since) : maxMs;
        }

        void check(long now) {
            long waitedMs;
            synchronized (this) {
                if (since < 0) {
                    return;
                }
                waitedMs = now - since;
                if (waitedMs >= HARD_LIMIT_MS ? failed : waitedMs < SOFT_LIMIT_MS || slow) {
                    return;
                }
                slow = true;
                failed = waitedMs >= HARD_LIMIT_MS;
            }
            report(waitedMs, true);
        }

        private void report(long waitedMs, boolean dump) {
            String message = description + " for " + waitedMs + " ms";
            String dumpFile = dump ? ThreadDumps.write(results, kind) : null;
            if (waitedMs >= HARD_LIMIT_MS) {
                results.violation(kind, message, dumpFile);
            } else {
                results.slowdown(kind, message, dumpFile);
            }
        }
    }
}
