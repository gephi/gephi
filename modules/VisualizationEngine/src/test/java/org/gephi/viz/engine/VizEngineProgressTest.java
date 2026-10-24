package org.gephi.viz.engine;

import java.util.EnumSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
import org.gephi.graph.api.Configuration;
import org.gephi.graph.api.DirectedGraph;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.viz.engine.pipeline.RenderingLayer;
import org.gephi.viz.engine.spi.ElementsCallback;
import org.gephi.viz.engine.spi.Renderer;
import org.gephi.viz.engine.spi.RenderingTarget;
import org.gephi.viz.engine.spi.WorldData;
import org.gephi.viz.engine.spi.WorldUpdater;
import org.gephi.viz.engine.util.structure.EdgesCallback;
import org.gephi.viz.engine.util.structure.NodesCallback;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class VizEngineProgressTest {

    private static final long TIMEOUT_MS = 10_000;

    private GraphModel graphModel;
    private Node lastNode;
    private VizEngine<RenderingTarget, Object> engine;

    @Before
    public void setUp() {
        graphModel = GraphModel.Factory.newInstance(Configuration.builder().enableSpatialIndex(true).build());
        DirectedGraph graph = graphModel.getDirectedGraph();
        Node n1 = newNode("1", 0f, 0f);
        Node n2 = newNode("2", 100f, 0f);
        lastNode = newNode("3", 0f, 100f);
        graph.addNode(n1);
        graph.addNode(n2);
        graph.addNode(lastNode);
        graph.addEdge(graphModel.factory().newEdge(n1, n2));
        graph.addEdge(graphModel.factory().newEdge(n1, lastNode));

        engine = new VizEngine<>(new TestTarget());
        engine.addWorldUpdater(new TestUpdater<>("nodes", new NodesCallback()));
        engine.addWorldUpdater(new TestUpdater<>("edges", new EdgesCallback()));
        engine.addRenderer(new TestRenderer());
        engine.setGraphModel(graphModel, null, null);
        engine.reshape(800, 600);
        engine.initPipeline();
        engine.centerOnGraph();
    }

    @After
    public void tearDown() {
        if (engine != null) {
            engine.destroy();
        }
    }

    @Test
    public void testFrameCountAdvancesWithEachDisplay() {
        Assert.assertEquals(0L, engine.getFrameCount());
        Assert.assertEquals(WorldUpdateInfo.NONE, engine.getLastWorldUpdate());

        engine.display();
        engine.display();
        engine.display();

        Assert.assertEquals(3L, engine.getFrameCount());
    }

    @Test
    public void testWorldUpdateCountsFollowTheGraph() throws InterruptedException {
        displayUntil(() -> engine.getLastWorldUpdate().sequence() > 0);
        Assert.assertEquals(3, engine.getLastWorldUpdate().nodeCount());
        Assert.assertEquals(2, engine.getLastWorldUpdate().edgeCount());

        graphModel.getDirectedGraph().removeNode(lastNode);

        // Nodes and edges are gathered separately, so an update may see the removal in only one of them
        displayUntil(() -> engine.getLastWorldUpdate().nodeCount() == 2
            && engine.getLastWorldUpdate().edgeCount() == 1);
    }

    @Test
    public void testWorldUpdatesStallWhileGraphIsWriteLocked() throws InterruptedException {
        displayUntil(() -> engine.getLastWorldUpdate().sequence() > 0);

        long sequenceWhileLocked;
        try (WriteLockHolder ignored = new WriteLockHolder(graphModel.getGraph())) {
            long sequenceBefore = engine.getLastWorldUpdate().sequence();
            long framesBefore = engine.getFrameCount();
            displayTimes(50);
            sequenceWhileLocked = engine.getLastWorldUpdate().sequence();

            // display() returned each time, it never waits for the graph lock
            Assert.assertEquals(framesBefore + 50, engine.getFrameCount());
            // An update that gathered its elements before the lock was taken may still be committed
            Assert.assertTrue("World updates stall", sequenceWhileLocked - sequenceBefore <= 1);
        }

        displayUntil(() -> engine.getLastWorldUpdate().sequence() > sequenceWhileLocked);
    }

    @Test
    public void testUpdateCompletedWhilePausedIsNotCommitted() throws InterruptedException {
        displayUntil(() -> engine.getLastWorldUpdate().sequence() > 0);

        long sequenceBefore;
        try (WriteLockHolder ignored = new WriteLockHolder(graphModel.getGraph())) {
            // Leaves an update in flight, waiting for the lock
            displayTimes(20);
            sequenceBefore = engine.getLastWorldUpdate().sequence();
            engine.pauseUpdating();
        }
        Assert.assertFalse(engine.isUpdating());

        // The update in flight completes now and is consumed while paused
        displayTimes(20);
        Assert.assertEquals(sequenceBefore, engine.getLastWorldUpdate().sequence());

        engine.resumeUpdating();
        Assert.assertTrue(engine.isUpdating());
        displayUntil(() -> engine.getLastWorldUpdate().sequence() > sequenceBefore);
    }

    private Node newNode(String id, float x, float y) {
        Node node = graphModel.factory().newNode(id);
        node.setX(x);
        node.setY(y);
        return node;
    }

    private void displayUntil(BooleanSupplier condition) throws InterruptedException {
        long deadline = System.currentTimeMillis() + TIMEOUT_MS;
        while (!condition.getAsBoolean()) {
            Assert.assertTrue("Timed out, last world update: " + engine.getLastWorldUpdate(),
                System.currentTimeMillis() < deadline);
            engine.display();
            Thread.sleep(5);
        }
    }

    private void displayTimes(int frames) throws InterruptedException {
        for (int i = 0; i < frames; i++) {
            engine.display();
            Thread.sleep(5);
        }
    }

    /**
     * Holds the graph write lock from another thread, as a writer in the application would.
     */
    private static class WriteLockHolder implements AutoCloseable {

        private final CountDownLatch locked = new CountDownLatch(1);
        private final CountDownLatch release = new CountDownLatch(1);
        private final Thread thread;

        WriteLockHolder(Graph graph) throws InterruptedException {
            thread = new Thread(() -> {
                graph.writeLock();
                try {
                    locked.countDown();
                    release.await();
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } finally {
                    graph.writeUnlock();
                }
            }, "Test Writer");
            thread.start();
            Assert.assertTrue("Write lock acquired", locked.await(TIMEOUT_MS, TimeUnit.MILLISECONDS));
        }

        @Override
        public void close() throws InterruptedException {
            release.countDown();
            thread.join(TIMEOUT_MS);
        }
    }

    private static class TestTarget implements RenderingTarget {

        @Override
        public void setup(VizEngine engine) {
        }

        @Override
        public int getFps() {
            return 0;
        }
    }

    private static class TestUpdater<T> implements WorldUpdater<RenderingTarget, T> {

        private final String category;
        private final ElementsCallback<T> callback;

        TestUpdater(String category, ElementsCallback<T> callback) {
            this.category = category;
            this.callback = callback;
        }

        @Override
        public void updateWorld(VizEngineModel model) {
        }

        @Override
        public ElementsCallback<T> getElementsCallback() {
            return callback;
        }

        @Override
        public String getCategory() {
            return category;
        }

        @Override
        public int getPreferenceInCategory() {
            return 0;
        }

        @Override
        public String getName() {
            return category;
        }

        @Override
        public void init(RenderingTarget target) {
        }

        @Override
        public int getOrder() {
            return 0;
        }
    }

    private static class TestRenderer implements Renderer<RenderingTarget, WorldData> {

        @Override
        public WorldData worldUpdated(VizEngineModel model, RenderingTarget target, float[] mvpFloats) {
            return new WorldData() {
            };
        }

        @Override
        public void render(WorldData data, RenderingTarget target, RenderingLayer layer, float[] mvpFloats) {
        }

        @Override
        public EnumSet<RenderingLayer> getLayers() {
            return EnumSet.noneOf(RenderingLayer.class);
        }

        @Override
        public String getCategory() {
            return "test";
        }

        @Override
        public int getPreferenceInCategory() {
            return 0;
        }

        @Override
        public String getName() {
            return "test";
        }

        @Override
        public void init(RenderingTarget target) {
        }

        @Override
        public int getOrder() {
            return 0;
        }
    }
}
