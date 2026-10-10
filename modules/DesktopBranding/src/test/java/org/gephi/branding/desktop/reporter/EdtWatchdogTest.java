package org.gephi.branding.desktop.reporter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class EdtWatchdogTest {

    private final List<Runnable> posted = new ArrayList<>();
    private final List<Long> reports = new ArrayList<>();
    private long monotonic;
    private long wall;
    private boolean consent;
    private EdtWatchdog watchdog;

    @Before
    public void setUp() {
        monotonic = 1_000_000;
        wall = 1_700_000_000_000L;
        consent = true;
        watchdog = new EdtWatchdog(posted::add, () -> monotonic, () -> wall, () -> consent,
            (edt, hungForMs) -> reports.add(hungForMs));
    }

    private void tick(long ms) {
        monotonic += ms;
        wall += ms;
        watchdog.tick();
    }

    private void runPosted() {
        posted.forEach(Runnable::run);
        posted.clear();
    }

    private void hang(long ms) {
        for (long t = 0; t < ms; t += EdtWatchdog.INTERVAL_MS) {
            tick(EdtWatchdog.INTERVAL_MS);
        }
    }

    @Test
    public void testResponsiveEdtNeverReported() {
        for (int i = 0; i < 50; i++) {
            tick(EdtWatchdog.INTERVAL_MS);
            runPosted();
        }
        Assert.assertTrue(reports.isEmpty());
    }

    @Test
    public void testOnlyOneProbeInFlight() {
        tick(0);
        hang(30_000);
        Assert.assertEquals(1, posted.size());
    }

    @Test
    public void testHangReportedOncePerEpisode() {
        tick(0);
        hang(EdtWatchdog.THRESHOLD_MS - EdtWatchdog.INTERVAL_MS);
        Assert.assertTrue(reports.isEmpty());
        hang(EdtWatchdog.INTERVAL_MS);
        Assert.assertEquals(List.of(EdtWatchdog.THRESHOLD_MS), reports);
        hang(60_000);
        Assert.assertEquals(1, reports.size());

        // Recovery re-arms
        runPosted();
        tick(EdtWatchdog.INTERVAL_MS);
        tick(EdtWatchdog.INTERVAL_MS);
        runPosted();
        tick(EdtWatchdog.INTERVAL_MS);
        Assert.assertEquals(1, reports.size());
        hang(EdtWatchdog.THRESHOLD_MS);
        Assert.assertEquals(2, reports.size());
    }

    @Test
    public void testReportsCappedPerSession() {
        tick(0);
        for (int i = 0; i < EdtWatchdog.MAX_REPORTS_PER_SESSION + 2; i++) {
            hang(EdtWatchdog.THRESHOLD_MS);
            runPosted();
            tick(EdtWatchdog.INTERVAL_MS);
        }
        Assert.assertEquals(EdtWatchdog.MAX_REPORTS_PER_SESSION, reports.size());
    }

    @Test
    public void testSleepGapNotCounted() {
        tick(0);
        hang(EdtWatchdog.THRESHOLD_MS - EdtWatchdog.INTERVAL_MS);
        // Machine asleep for an hour, monotonic clock running (Windows)
        tick(3_600_000);
        hang(EdtWatchdog.THRESHOLD_MS - EdtWatchdog.INTERVAL_MS);
        Assert.assertTrue(reports.isEmpty());
        hang(EdtWatchdog.INTERVAL_MS);
        Assert.assertEquals(1, reports.size());
    }

    @Test
    public void testSleepGapOnWallClockOnly() {
        tick(0);
        hang(EdtWatchdog.THRESHOLD_MS - EdtWatchdog.INTERVAL_MS);
        // Monotonic clock stopped during sleep (macOS, Linux), wall clock jumped
        wall += 3_600_000;
        tick(EdtWatchdog.INTERVAL_MS);
        Assert.assertTrue(reports.isEmpty());
    }

    @Test
    public void testNoConsentNoReport() {
        consent = false;
        tick(0);
        hang(60_000);
        Assert.assertTrue(reports.isEmpty());
    }

    @Test
    public void testConsentCheckedAtReportTime() {
        consent = false;
        tick(0);
        hang(EdtWatchdog.THRESHOLD_MS);
        runPosted();
        tick(EdtWatchdog.INTERVAL_MS);
        consent = true;
        hang(EdtWatchdog.THRESHOLD_MS);
        Assert.assertEquals(1, reports.size());
    }

    @Test
    public void testThreadDumpScrubsPathsAndKeepsFullStacks() throws Exception {
        CountDownLatch started = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        Thread t = new Thread(() -> deep(30, started, release), "Loading /Users/jane/secret/graph.gexf");
        t.setDaemon(true);
        t.start();
        try {
            Assert.assertTrue(started.await(5, TimeUnit.SECONDS));
            String dump = EdtWatchdog.threadDump(null);
            Assert.assertTrue(dump.contains("\"Loading <path>.gexf\""));
            Assert.assertFalse(dump.contains("jane"));
            Assert.assertTrue(dump.contains("java.util.concurrent.CountDownLatch.await("));
            int depth = dump.split("EdtWatchdogTest.deep", -1).length - 1;
            Assert.assertTrue("stack truncated: " + depth, depth >= 30);
        } finally {
            release.countDown();
            t.join(5_000);
        }
    }

    private static void deep(int n, CountDownLatch started, CountDownLatch release) {
        if (n > 0) {
            deep(n - 1, started, release);
            return;
        }
        started.countDown();
        try {
            release.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Test
    public void testThreadDumpListsDeadlock() throws Exception {
        ReentrantLock a = new ReentrantLock();
        ReentrantLock b = new ReentrantLock();
        CountDownLatch bothLocked = new CountDownLatch(2);
        Thread t1 = new Thread(() -> lockBoth(a, b, bothLocked), "deadlock-1");
        Thread t2 = new Thread(() -> lockBoth(b, a, bothLocked), "deadlock-2");
        t1.setDaemon(true);
        t2.setDaemon(true);
        t1.start();
        t2.start();
        try {
            long[] deadlocked = null;
            for (int i = 0; i < 100 && (deadlocked == null || deadlocked.length < 2); i++) {
                Thread.sleep(50);
                deadlocked = java.lang.management.ManagementFactory.getThreadMXBean().findDeadlockedThreads();
            }
            Assert.assertNotNull(deadlocked);
            String dump = EdtWatchdog.threadDump(deadlocked);
            Assert.assertTrue(dump.startsWith("Deadlocked threads:\n"));
            String head = dump.substring(0, dump.indexOf("All threads:"));
            Assert.assertTrue(head.contains("\"deadlock-1\""));
            Assert.assertTrue(head.contains("\"deadlock-2\""));
            Assert.assertTrue(head.contains("Locked synchronizers"));
        } finally {
            t1.interrupt();
            t2.interrupt();
            t1.join(5_000);
            t2.join(5_000);
        }
    }

    private static void lockBoth(ReentrantLock first, ReentrantLock second, CountDownLatch bothLocked) {
        first.lock();
        try {
            bothLocked.countDown();
            bothLocked.await();
            second.lockInterruptibly();
            second.unlock();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            first.unlock();
        }
    }
}
