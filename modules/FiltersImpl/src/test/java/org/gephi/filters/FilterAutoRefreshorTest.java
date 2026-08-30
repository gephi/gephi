package org.gephi.filters;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.Test;

public class FilterAutoRefreshorTest {

    @Test
    public void testConcurrentSetEnableDoesNotThrow() throws Exception {
        FilterModelImpl filterModel = Utils.newFilterModelWithGraph();
        FilterAutoRefreshor autoRefreshor = filterModel.getAutoRefreshor();

        //Simulate the caller thread and a background FilterThread both toggling
        //auto-refresh around the same time, as happens when a filter/select operation
        //completes right as the model's current result is set/cleared elsewhere
        int threadCount = 16;
        int iterationsPerThread = 200;
        CountDownLatch startLatch = new CountDownLatch(1);
        AtomicReference<Throwable> failure = new AtomicReference<>();
        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < threadCount; i++) {
            Thread t = new Thread(() -> {
                try {
                    startLatch.await();
                    for (int j = 0; j < iterationsPerThread; j++) {
                        autoRefreshor.setEnable(true);
                        autoRefreshor.setEnable(false);
                    }
                } catch (Throwable ex) {
                    failure.compareAndSet(null, ex);
                }
            });
            threads.add(t);
            t.start();
        }
        startLatch.countDown();
        for (Thread t : threads) {
            t.join();
        }
        autoRefreshor.setRunning(false);

        if (failure.get() != null) {
            throw new AssertionError("Concurrent setEnable() calls threw", failure.get());
        }
    }
}
