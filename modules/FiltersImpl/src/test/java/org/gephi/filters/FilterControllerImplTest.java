package org.gephi.filters;

import static org.awaitility.Awaitility.await;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.gephi.filters.api.FilterController;
import org.gephi.filters.api.Query;
import org.gephi.filters.plugin.graph.KCoreBuilder;
import org.gephi.filters.spi.FilterProperty;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.GraphView;
import org.gephi.project.api.ProjectController;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

public class FilterControllerImplTest {

    private final ProjectController projectController = Lookup.getDefault().lookup(ProjectController.class);
    private final FilterController filterController = Lookup.getDefault().lookup(FilterController.class);
    private final List<BlockingFilter> filters = new ArrayList<>();
    private final AtomicBoolean idleEvent = new AtomicBoolean();
    private FilterModelImpl filterModel;

    @Before
    public void setUp() {
        projectController.newProject();
        filterModel = (FilterModelImpl) filterController.getModel();
        filterModel.addChangeListener(e -> {
            if (!filterModel.isRunning()) {
                idleEvent.set(true);
            }
        });
    }

    @After
    public void tearDown() {
        // A blocked filter holds the graph's write lock, which clearing the filter below waits for, so a failed
        // assertion would otherwise hang the build instead of failing it
        filters.forEach(filter -> filter.permits.release(1_000));
        filterController.filterVisible(null);
        projectController.closeCurrentProject();
    }

    @Test
    public void testEventWhenClearedWhileRunning() {
        BlockingFilter filter = blockingFilter();
        filterController.filterVisible(new FilterQueryImpl(null, filter));
        await().atMost(10, TimeUnit.SECONDS).until(() -> filter.inits.get() == 1);
        Assert.assertTrue(filterModel.isRunning());
        idleEvent.set(false);

        // Clearing resets the visible view under the graph's write lock, which the stopped computation holds, so
        // the filter is released once this thread blocks there
        Thread testThread = Thread.currentThread();
        Thread releaser = new Thread(() -> {
            try {
                await().atMost(10, TimeUnit.SECONDS).until(() -> testThread.getState() == Thread.State.WAITING);
            } finally {
                filter.permits.release();
            }
        });
        releaser.start();
        filterController.filterVisible(null);

        Assert.assertFalse(filterModel.isRunning());
        Assert.assertTrue("A change event is fired once not running", idleEvent.get());
    }

    @Test
    public void testNoIdleEventWhenReplacedWhileRunning() {
        BlockingFilter first = blockingFilter();
        filterController.filterVisible(new FilterQueryImpl(null, first));
        await().atMost(10, TimeUnit.SECONDS).until(() -> first.inits.get() == 1);
        idleEvent.set(false);

        BlockingFilter second = blockingFilter();
        filterController.filterVisible(new FilterQueryImpl(null, second));

        Assert.assertTrue(filterModel.isRunning());
        Assert.assertFalse("Not idle while the new query is computed", idleEvent.get());
        first.permits.release();
        second.permits.release();
        // The thread stops running before it fires the event, so the event is awaited first
        await().atMost(10, TimeUnit.SECONDS).until(idleEvent::get);
        Assert.assertFalse(filterModel.isRunning());
    }

    /**
     * Clearing the filter as soon as a refreshed result is visible destroys each result once.
     */
    @Test
    public void testClearWhenRefreshedResultVisible() throws Exception {
        Query query = filterController.createQuery(Lookup.getDefault().lookup(KCoreBuilder.class));
        filterController.add(query);
        FilterProperty property = query.getFilter().getProperties()[0];
        GraphModel graphModel = filterModel.getGraphModel();
        AtomicReference<Throwable> failure = new AtomicReference<>();
        // The filter thread logs the exceptions of a query through Exceptions.printStackTrace and carries on
        Logger logger = Logger.getLogger(Exceptions.class.getName());
        Handler handler = new Handler() {
            @Override
            public void publish(LogRecord record) {
                if (record.getThrown() != null) {
                    failure.compareAndSet(null, record.getThrown());
                }
            }

            @Override
            public void flush() {
            }

            @Override
            public void close() {
            }
        };
        logger.addHandler(handler);
        try {
            for (int i = 0; i < 50; i++) {
                GraphView view = graphModel.getVisibleView();
                filterController.filterVisible(query);
                Thread filterThread = filterModel.getFilterThread();
                filterThread.setUncaughtExceptionHandler((thread, ex) -> failure.compareAndSet(null, ex));
                view = awaitVisibleViewReplaced(graphModel, view);
                property.setValue(1);
                awaitVisibleViewReplaced(graphModel, view);
                filterController.filterVisible(null);
                filterThread.join(10_000);
                if (failure.get() != null) {
                    throw new AssertionError("The filter thread threw", failure.get());
                }
            }
        } finally {
            logger.removeHandler(handler);
        }
    }

    /**
     * Refreshing a selection destroys the result it replaces.
     */
    @Test
    public void testSelectRefreshDestroysReplacedResult() {
        Query query = filterController.createQuery(Lookup.getDefault().lookup(KCoreBuilder.class));
        filterController.add(query);
        filterController.selectVisible(query);
        await().atMost(10, TimeUnit.SECONDS).until(() -> !filterModel.isRunning());
        GraphView result = filterModel.getCurrentResult();

        query.getFilter().getProperties()[0].setValue(1);
        await().atMost(10, TimeUnit.SECONDS).until(() -> !filterModel.isRunning());

        Assert.assertNotSame(result, filterModel.getCurrentResult());
        Assert.assertTrue("Replaced result destroyed", result.isDestroyed());
    }

    /**
     * Clearing the filter doesn't wait for a reader of the graph, such as a statistic, before returning.
     */
    @Test
    public void testClearDoesNotWaitForReader() throws Exception {
        Query query = filterController.createQuery(Lookup.getDefault().lookup(KCoreBuilder.class));
        filterController.add(query);
        filterController.filterVisible(query);
        await().atMost(10, TimeUnit.SECONDS)
            .until(() -> filterModel.getCurrentResult() != null && !filterModel.isRunning());
        GraphView result = filterModel.getCurrentResult();
        GraphModel graphModel = filterModel.getGraphModel();

        CountDownLatch locked = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        AtomicBoolean unlocked = new AtomicBoolean();
        Thread reader = new Thread(() -> {
            Graph graph = graphModel.getGraph(result);
            graph.readLock();
            try {
                locked.countDown();
                release.await(10, TimeUnit.SECONDS);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            } finally {
                unlocked.set(true);
                graph.readUnlock();
            }
        });
        reader.start();
        locked.await();
        try {
            filterController.filterVisible(null);

            Assert.assertFalse("Returned while the reader holds the lock", unlocked.get());
            Assert.assertTrue(graphModel.getVisibleView().isMainView());
        } finally {
            release.countDown();
        }
        await().atMost(10, TimeUnit.SECONDS).until(result::isDestroyed);
    }

    private static GraphView awaitVisibleViewReplaced(GraphModel graphModel, GraphView view) {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
        GraphView visibleView;
        while ((visibleView = graphModel.getVisibleView()) == view) {
            if (System.nanoTime() > deadline) {
                throw new AssertionError("The visible view wasn't replaced");
            }
            Thread.onSpinWait();
        }
        return visibleView;
    }

    private BlockingFilter blockingFilter() {
        BlockingFilter filter = new BlockingFilter();
        filters.add(filter);
        return filter;
    }
}
