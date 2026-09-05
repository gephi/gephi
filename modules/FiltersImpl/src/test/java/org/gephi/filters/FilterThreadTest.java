package org.gephi.filters;

import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertFalse;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.gephi.filters.plugin.graph.GiantComponentBuilder;
import org.junit.Test;

public class FilterThreadTest {

    /**
     * A {@link FilterThread} must keep making progress when setRootQuery() is called
     * concurrently from many threads - a notify() landing at the wrong moment must
     * never leave it parked in wait() forever.
     */
    @Test
    public void testConcurrentSetRootQueryDoesNotHang() throws Exception {
        FilterModelImpl filterModel = Utils.newFilterModelWithGraph();
        AtomicInteger completions = new AtomicInteger();
        filterModel.addChangeListener(e -> {
            if (filterModel.getCurrentResult() != null) {
                completions.incrementAndGet();
            }
        });

        FilterQueryImpl query = new FilterQueryImpl(null, new GiantComponentBuilder.GiantComponentFilter());
        filterModel.setFilterState(true, false, query);

        FilterThread filterThread = new FilterThread(filterModel, query);
        filterThread.setRootQuery(query);
        filterThread.start();

        //Hammer setRootQuery() from many threads at once, as happens when filter
        //properties are edited in rapid succession while a query is mid-flight
        int before = completions.get();
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
                        filterThread.setRootQuery(query);
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

        if (failure.get() != null) {
            throw new AssertionError("Concurrent setRootQuery() calls threw", failure.get());
        }

        //No further setRootQuery() call is made here: the last one issued by the
        //hammering above must, on its own, eventually be consumed and processed.
        //Bounded so a hang fails the test instead of the build.
        await().atMost(10, TimeUnit.SECONDS)
            .until(() -> filterThread.getRootQuery() == null && completions.get() > before);

        filterThread.setRunning(false);
        filterThread.join(10_000);
        assertFalse(filterThread.isAlive());
    }
}
