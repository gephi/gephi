package org.gephi.timeline;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.gephi.filters.api.FilterController;
import org.gephi.filters.api.FilterModel;
import org.gephi.filters.api.Query;
import org.gephi.filters.plugin.dynamic.DynamicRangeBuilder;
import org.gephi.filters.plugin.dynamic.DynamicRangeBuilder.DynamicRangeFilter;
import org.gephi.filters.plugin.graph.DegreeRangeBuilder;
import org.gephi.filters.plugin.graph.DegreeRangeBuilder.DegreeRangeFilter;
import org.gephi.filters.spi.FilterBuilder;
import org.gephi.graph.GraphGenerator;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.GraphView;
import org.gephi.graph.api.Interval;
import org.gephi.graph.api.Node;
import org.gephi.project.api.Project;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.timeline.api.TimelineController;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openide.util.Lookup;

public class TimelineControllerImplTest {

    private Project project;
    private Workspace workspace;

    @Before
    public void setUp() {
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        project = pc.newProject();
        workspace = project.getCurrentWorkspace();
    }

    @After
    public void cleanUp() {
        Lookup.getDefault().lookup(ProjectController.class).closeCurrentProject();
        project = null;
    }

    /**
     * Regression test for #3025: playing the timeline while the "Time Interval" (Dynamic Range)
     * filter is a subquery of another filter (here Degree Range) used to corrupt the parent
     * filter's range property instead of the Dynamic Range filter's own property, crashing
     * FilterProcessor with "Lower and upper must be the same class".
     */
    @Test
    public void testSetIntervalWithDynamicRangeAsSubQuery() {
        GraphGenerator generator = GraphGenerator.build(workspace);
        GraphModel graphModel = generator.getGraphModel();

        Node n1 = graphModel.factory().newNode("1");
        Node n2 = graphModel.factory().newNode("2");
        n1.addInterval(new Interval(0.0, 10.0));
        n2.addInterval(new Interval(0.0, 10.0));
        Edge e1 = graphModel.factory().newEdge("1", n1, n2, 0, 1.0, true);
        e1.addInterval(new Interval(0.0, 10.0));

        Graph graph = graphModel.getGraph();
        graph.addNode(n1);
        graph.addNode(n2);
        graph.addEdge(e1);

        FilterController filterController = Lookup.getDefault().lookup(FilterController.class);

        DegreeRangeBuilder degreeRangeBuilder = Lookup.getDefault().lookup(DegreeRangeBuilder.class);
        Query degreeQuery = filterController.createQuery(degreeRangeBuilder);
        filterController.add(degreeQuery);

        DynamicRangeBuilder dynamicRangeBuilder = Lookup.getDefault().lookup(DynamicRangeBuilder.class);
        FilterBuilder[] dynamicBuilders = dynamicRangeBuilder.getBuilders(workspace);
        Assert.assertTrue(dynamicBuilders.length > 0);
        Query dynamicQuery = filterController.createQuery(dynamicBuilders[0]);
        filterController.setSubQuery(degreeQuery, dynamicQuery);

        FilterModel filterModel = filterController.getModel(workspace);
        Assert.assertSame(degreeQuery, filterModel.getCurrentQuery());

        TimelineController timelineController = Lookup.getDefault().lookup(TimelineController.class);
        timelineController.setCustomBounds(0.0, 10.0);

        //Simulate the timeline playing through the interval
        timelineController.setInterval(0.0, 4.0);

        //The Dynamic Range filter's own range must have been updated...
        DynamicRangeFilter dynamicRangeFilter = (DynamicRangeFilter) dynamicQuery.getFilter();
        Assert.assertEquals(0.0, dynamicRangeFilter.getRange().getLowerDouble(), 0.0);
        Assert.assertEquals(4.0, dynamicRangeFilter.getRange().getUpperDouble(), 0.0);

        //...and NOT the Degree Range filter's range, which must stay Integer-typed
        DegreeRangeFilter degreeRangeFilter = (DegreeRangeFilter) degreeQuery.getFilter();
        Assert.assertEquals(Integer.class, degreeRangeFilter.getRange().getRangeType());

        //Must not throw IllegalArgumentException: Lower and upper must be the same class
        GraphView view1 = filterController.filter(degreeQuery);
        Assert.assertNotNull(view1);

        //Advance the timeline further, still without crashing
        timelineController.setInterval(4.0, 8.0);
        Assert.assertEquals(Integer.class, degreeRangeFilter.getRange().getRangeType());
        GraphView view2 = filterController.filter(degreeQuery);
        Assert.assertNotNull(view2);

        //Stopping the timeline should only remove the Dynamic Range subquery, not the whole query
        timelineController.setInterval(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
        Assert.assertSame(degreeQuery, filterModel.getCurrentQuery());
        Assert.assertEquals(0, degreeQuery.getChildren().length);

        filterController.filterVisible(null);
    }

    /**
     * The EDT-facing accessors must not wait for the graph lock: a reader holding the lock and a queued writer
     * block every new reader (the old getGraphModel().getTimeBounds() calls would have blocked here).
     */
    @Test
    public void testAccessorsDoNotBlockOnContendedGraphLock() throws Exception {
        GraphModel graphModel = GraphGenerator.build(workspace).getGraphModel();
        Graph graph = graphModel.getGraph();
        Node n1 = graphModel.factory().newNode("1");
        n1.addInterval(new Interval(0.0, 10.0));
        graph.addNode(n1);

        TimelineControllerImpl controller =
            (TimelineControllerImpl) Lookup.getDefault().lookup(TimelineController.class);
        TimelineModelImpl model = controller.getModel();
        model.refreshTimeBounds();

        ExecutorService executor = Executors.newSingleThreadExecutor(r -> daemon(r, "edt-like"));
        try (ContendedLock ignored = new ContendedLock(graph)) {
            Future<Void> future = executor.submit(() -> {
                Assert.assertEquals(0.0, model.getMin(), 0.0);
                Assert.assertEquals(10.0, model.getMax(), 0.0);
                Assert.assertTrue(model.hasValidBounds());
                controller.setCustomBounds(2.0, 8.0);
                Assert.assertTrue(model.hasCustomBounds());
                controller.setEnabled(true);
                Assert.assertTrue(model.isEnabled());
                return null;
            });
            future.get(5, TimeUnit.SECONDS);
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    public void testPollPublishesNewBounds() {
        GraphModel graphModel = GraphGenerator.build(workspace).getGraphModel();
        TimelineControllerImpl controller =
            (TimelineControllerImpl) Lookup.getDefault().lookup(TimelineController.class);
        TimelineModelImpl model = new TimelineModelImpl(workspace);
        GraphObserverThread observer = new GraphObserverThread(controller, model);
        Assert.assertFalse(model.hasValidBounds());

        Graph graph = graphModel.getGraph();
        Node n1 = graphModel.factory().newNode("1");
        n1.addInterval(new Interval(0.0, 10.0));
        graph.addNode(n1);
        observer.poll();
        Assert.assertTrue(model.hasValidBounds());
        Assert.assertEquals(0.0, model.getMin(), 0.0);
        Assert.assertEquals(10.0, model.getMax(), 0.0);

        Node n2 = graphModel.factory().newNode("2");
        n2.addInterval(new Interval(-5.0, 20.0));
        graph.addNode(n2);
        observer.poll();
        Assert.assertEquals(-5.0, model.getMin(), 0.0);
        Assert.assertEquals(20.0, model.getMax(), 0.0);

        //A poll over unchanged bounds keeps them
        observer.poll();
        Assert.assertEquals(-5.0, model.getMin(), 0.0);
        Assert.assertEquals(20.0, model.getMax(), 0.0);
    }

    /**
     * A stopped observer that was waiting for the graph lock must not publish its result afterwards.
     */
    @Test
    public void testStoppedObserverDoesNotPublishBounds() throws Exception {
        GraphModel graphModel = GraphGenerator.build(workspace).getGraphModel();
        TimelineControllerImpl controller =
            (TimelineControllerImpl) Lookup.getDefault().lookup(TimelineController.class);
        TimelineModelImpl model = new TimelineModelImpl(workspace);
        GraphObserverThread observer = new GraphObserverThread(controller, model);

        Graph graph = graphModel.getGraph();
        Node n1 = graphModel.factory().newNode("1");
        n1.addInterval(new Interval(0.0, 10.0));
        graph.addNode(n1);

        ExecutorService executor = Executors.newSingleThreadExecutor(r -> daemon(r, "observer-poll"));
        try (ContendedLock ignored = new ContendedLock(graph)) {
            Future<?> future = executor.submit(observer::poll);
            try {
                future.get(200, TimeUnit.MILLISECONDS);
                Assert.fail("poll should wait for the graph lock");
            } catch (TimeoutException expected) {
                //Blocked behind the queued writer
            }
            observer.stopThread();
            ignored.release();
            future.get(5, TimeUnit.SECONDS);
        } finally {
            executor.shutdownNow();
        }
        Assert.assertFalse(model.hasValidBounds());
    }

    private static Thread daemon(Runnable runnable, String name) {
        Thread thread = new Thread(runnable, name);
        thread.setDaemon(true);
        return thread;
    }

    /**
     * Holds the graph read lock on one thread and queues a writer behind it on another, so that any new reader
     * blocks until {@link #release()} or {@link #close()}.
     */
    private static class ContendedLock implements AutoCloseable {

        private final CountDownLatch readLocked = new CountDownLatch(1);
        private final CountDownLatch releaseReader = new CountDownLatch(1);
        private final Thread reader;
        private final Thread writer;

        ContendedLock(Graph graph) throws InterruptedException {
            reader = daemon(() -> {
                graph.readLock();
                try {
                    readLocked.countDown();
                    releaseReader.await();
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                } finally {
                    graph.readUnlock();
                }
            }, "lock-reader");
            writer = daemon(() -> {
                graph.writeLock();
                graph.writeUnlock();
            }, "lock-writer");
            reader.start();
            Assert.assertTrue(readLocked.await(5, TimeUnit.SECONDS));
            writer.start();
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
            while (writer.getState() != Thread.State.WAITING) {
                Assert.assertTrue("writer never queued", System.nanoTime() < deadline);
                Thread.yield();
            }
        }

        void release() {
            releaseReader.countDown();
        }

        @Override
        public void close() throws InterruptedException {
            release();
            reader.join(5000);
            writer.join(5000);
        }
    }
}
