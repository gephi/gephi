package org.gephi.viz.engine;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
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
    private VizEngine<RenderingTarget, Object> engine;
    private TestRenderer renderer;

    @Before
    public void setUp() {
        graphModel = GraphModel.Factory.newInstance(Configuration.builder().enableSpatialIndex(true).build());
        DirectedGraph graph = graphModel.getDirectedGraph();
        Node n1 = graphModel.factory().newNode("1");
        Node n2 = graphModel.factory().newNode("2");
        n2.setX(100f);
        graph.addNode(n1);
        graph.addNode(n2);
        graph.addEdge(graphModel.factory().newEdge(n1, n2));

        // The real callbacks gather elements under the graph read lock
        engine = new VizEngine<>(new TestTarget());
        engine.addWorldUpdater(new TestUpdater<>("nodes", new NodesCallback()));
        engine.addWorldUpdater(new TestUpdater<>("edges", new EdgesCallback()));
        renderer = new TestRenderer();
        engine.addRenderer(renderer);
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
        Assert.assertEquals(0L, engine.getWorldUpdateCount());

        engine.display();
        engine.display();
        engine.display();

        Assert.assertEquals(3L, engine.getFrameCount());
    }

    @Test
    public void testWorldUpdatesStallWhileGraphIsWriteLocked() throws InterruptedException {
        displayUntil(() -> engine.getWorldUpdateCount() > 0);

        long updatesWhileLocked;
        try (WriteLockHolder ignored = new WriteLockHolder(graphModel.getGraph())) {
            long updatesBefore = engine.getWorldUpdateCount();
            long framesBefore = engine.getFrameCount();
            displayTimes(50);
            updatesWhileLocked = engine.getWorldUpdateCount();

            // display() returned each time, it never waits for the graph lock
            Assert.assertEquals(framesBefore + 50, engine.getFrameCount());
            // An update that gathered its elements before the lock was taken may still be committed
            Assert.assertTrue("World updates stall", updatesWhileLocked - updatesBefore <= 1);
        }

        displayUntil(() -> engine.getWorldUpdateCount() > updatesWhileLocked);
    }

    @Test
    public void testUpdateCompletedWhilePausedIsNotCounted() throws InterruptedException {
        displayUntil(() -> engine.getWorldUpdateCount() > 0);

        long updatesBefore;
        try (WriteLockHolder ignored = new WriteLockHolder(graphModel.getGraph())) {
            // Leaves an update in flight, waiting for the lock
            displayTimes(20);
            updatesBefore = engine.getWorldUpdateCount();
            engine.pauseUpdating();
        }

        // The update in flight completes now and is consumed while paused
        displayTimes(20);
        Assert.assertEquals(updatesBefore, engine.getWorldUpdateCount());

        engine.resumeUpdating();
        displayUntil(() -> engine.getWorldUpdateCount() > updatesBefore);
    }

    @Test
    public void testSwitchedGraphIsDrawnWhileOldGraphIsWriteLocked() throws InterruptedException {
        displayUntil(() -> renderer.lastDrawn == graphModel);

        GraphModel otherGraphModel =
            GraphModel.Factory.newInstance(Configuration.builder().enableSpatialIndex(true).build());
        otherGraphModel.getGraph().addNode(otherGraphModel.factory().newNode("other"));

        try (WriteLockHolder ignored = new WriteLockHolder(graphModel.getGraph())) {
            // Leaves an update in flight, waiting for the lock
            displayTimes(20);

            engine.unsetGraphModel(graphModel);
            engine.setGraphModel(otherGraphModel, null, null);
            renderer.drawnSinceReset.clear();

            displayUntil(() -> renderer.lastDrawn == otherGraphModel);
            Assert.assertFalse("Old graph drawn after the switch", renderer.drawnSinceReset.contains(graphModel));
        }
    }

    private void displayUntil(BooleanSupplier condition) throws InterruptedException {
        long deadline = System.currentTimeMillis() + TIMEOUT_MS;
        while (!condition.getAsBoolean()) {
            Assert.assertTrue("Timed out, world updates: " + engine.getWorldUpdateCount(),
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

    private static class TestWorldData implements WorldData {

        private final GraphModel graphModel;

        TestWorldData(GraphModel graphModel) {
            this.graphModel = graphModel;
        }
    }

    /**
     * Records the graph model each frame was drawn from. Frames are drawn on the test thread, in display().
     */
    private static class TestRenderer implements Renderer<RenderingTarget, TestWorldData> {

        private final Set<GraphModel> drawnSinceReset = new HashSet<>();
        private GraphModel lastDrawn;

        @Override
        public TestWorldData worldUpdated(VizEngineModel model, RenderingTarget target, float[] mvpFloats) {
            return new TestWorldData(model.getGraphModel());
        }

        @Override
        public void render(TestWorldData data, RenderingTarget target, RenderingLayer layer, float[] mvpFloats) {
            drawnSinceReset.add(data.graphModel);
            lastDrawn = data.graphModel;
        }

        @Override
        public EnumSet<RenderingLayer> getLayers() {
            return EnumSet.of(RenderingLayer.MIDDLE1);
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
