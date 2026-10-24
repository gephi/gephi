package org.gephi.viz.engine;

import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.gephi.viz.engine.pipeline.RenderingLayer;
import org.gephi.viz.engine.spi.InputListener;
import org.gephi.viz.engine.spi.Renderer;
import org.gephi.viz.engine.spi.RenderingTarget;
import org.gephi.viz.engine.spi.WorldData;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class VizEngineInputFailureTest {

    private static final long TIMEOUT_MS = 10_000;

    private final AtomicInteger worldUpdates = new AtomicInteger();
    private VizEngine<RenderingTarget, Object> engine;

    @Before
    public void setUp() {
        engine = new VizEngine<>(new TestTarget());
        engine.addRenderer(new CountingRenderer(worldUpdates));
        engine.addInputListener(new FailingInputListener());
        engine.initPipeline();
    }

    @After
    public void tearDown() {
        if (engine != null) {
            engine.destroy();
        }
    }

    @Test
    public void testFailingInputListenerDoesNotStopWorldUpdates() throws InterruptedException {
        long deadline = System.currentTimeMillis() + TIMEOUT_MS;
        while (worldUpdates.get() < 3) {
            Assert.assertTrue("Timed out, world updates: " + worldUpdates.get(),
                System.currentTimeMillis() < deadline);
            engine.queueEvent(new Object());
            engine.display();
            Thread.sleep(5);
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

    private static class FailingInputListener implements InputListener<RenderingTarget, Object> {

        @Override
        public List<Object> processEvents(List<Object> events) {
            throw new IllegalStateException("Input listener failure");
        }

        @Override
        public String getCategory() {
            return "input";
        }

        @Override
        public int getPreferenceInCategory() {
            return 0;
        }

        @Override
        public String getName() {
            return "input";
        }

        @Override
        public void init(RenderingTarget target) {
        }

        @Override
        public int getOrder() {
            return 0;
        }
    }

    private static class CountingRenderer implements Renderer<RenderingTarget, WorldData> {

        private final AtomicInteger worldUpdates;

        CountingRenderer(AtomicInteger worldUpdates) {
            this.worldUpdates = worldUpdates;
        }

        @Override
        public WorldData worldUpdated(VizEngineModel model, RenderingTarget target, float[] mvpFloats) {
            worldUpdates.incrementAndGet();
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
