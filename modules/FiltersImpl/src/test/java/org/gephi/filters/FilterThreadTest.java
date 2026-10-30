package org.gephi.filters;

import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertFalse;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.gephi.filters.plugin.graph.GiantComponentBuilder;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;
import org.junit.Assert;
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

    @Test
    public void testRunningUntilResultApplied() throws Exception {
        FilterModelImpl filterModel = Utils.newFilterModelWithGraph();
        AtomicBoolean idleEvent = new AtomicBoolean();
        filterModel.addChangeListener(e -> {
            if (!filterModel.isRunning()) {
                idleEvent.set(true);
            }
        });
        BlockingFilter filter = new BlockingFilter();
        FilterThread filterThread = startFilterThread(filterModel, filter);

        Assert.assertTrue("Running as soon as the query is set", filterModel.isRunning());
        await().atMost(10, TimeUnit.SECONDS).until(() -> filter.inits.get() == 1);
        Assert.assertTrue("Running while the filter computes", filterModel.isRunning());
        Assert.assertNull(filterModel.getCurrentResult());

        filter.permits.release();
        // The thread stops running before it fires the event, so the event is awaited first
        await().atMost(10, TimeUnit.SECONDS).until(idleEvent::get);
        Assert.assertFalse(filterModel.isRunning());
        Assert.assertNotNull("The result is applied once not running", filterModel.getCurrentResult());

        stop(filterThread);
    }

    @Test
    public void testRunningUntilQuerySetDuringComputationIsApplied() throws Exception {
        FilterModelImpl filterModel = Utils.newFilterModelWithGraph();
        BlockingFilter filter = new BlockingFilter();
        FilterThread filterThread = startFilterThread(filterModel, filter);
        await().atMost(10, TimeUnit.SECONDS).until(() -> filter.inits.get() == 1);

        //A parameter change while the first computation is in progress
        filterThread.setRootQuery((AbstractQueryImpl) filterModel.getCurrentQuery());
        filter.permits.release();
        await().atMost(10, TimeUnit.SECONDS).until(() -> filter.inits.get() == 2);
        Assert.assertTrue("Running until the second query is applied", filterModel.isRunning());

        filter.permits.release();
        await().atMost(10, TimeUnit.SECONDS).until(() -> !filterModel.isRunning());

        stop(filterThread);
        Assert.assertFalse("Not running once stopped", filterModel.isRunning());
    }

    @Test
    public void testFailingQueryDoesNotStopThread() throws Exception {
        FilterModelImpl filterModel = Utils.newFilterModelWithGraph();
        //The tiny graph has nodes, so the filter fails while iterating, holding a read lock
        AtomicBoolean fail = new AtomicBoolean(true);
        AtomicReference<Graph> failedGraph = new AtomicReference<>();
        BlockingFilter filter = new BlockingFilter() {
            @Override
            public boolean evaluate(Graph graph, Node node) {
                if (fail.get()) {
                    failedGraph.set(graph);
                    throw new IllegalStateException("Failing filter");
                }
                return true;
            }
        };
        filter.permits.release(2);
        FilterThread filterThread = startFilterThread(filterModel, filter);

        await().atMost(10, TimeUnit.SECONDS).until(() -> !filterModel.isRunning());
        Assert.assertNull(filterModel.getCurrentResult());
        Assert.assertTrue("The view of the failed pass is destroyed", failedGraph.get().getView().isDestroyed());

        fail.set(false);
        filterThread.setRootQuery((AbstractQueryImpl) filterModel.getCurrentQuery());
        await().atMost(10, TimeUnit.SECONDS).until(() -> filterModel.getCurrentResult() != null);

        stop(filterThread);
    }

    private static FilterThread startFilterThread(FilterModelImpl filterModel, BlockingFilter filter) {
        FilterQueryImpl query = new FilterQueryImpl(null, filter);
        filterModel.setFilterState(true, false, query);
        FilterThread filterThread = new FilterThread(filterModel, query);
        filterModel.setFilterThread(filterThread);
        filterThread.start();
        filterThread.setRootQuery(query);
        return filterThread;
    }

    private static void stop(FilterThread filterThread) throws InterruptedException {
        filterThread.setRunning(false);
        filterThread.join(10_000);
        assertFalse(filterThread.isAlive());
    }
}
