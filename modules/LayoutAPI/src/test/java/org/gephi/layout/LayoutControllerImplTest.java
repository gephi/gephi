package org.gephi.layout;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.gephi.graph.api.GraphModel;
import org.gephi.layout.api.LayoutController;
import org.gephi.layout.api.LayoutModel;
import org.gephi.layout.spi.Layout;
import org.gephi.layout.spi.LayoutBuilder;
import org.gephi.layout.spi.LayoutProperty;
import org.gephi.layout.utils.MockLayoutBuilder;
import org.gephi.project.api.ProjectController;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openide.util.Lookup;

public class LayoutControllerImplTest {

    private final ProjectController projectController = Lookup.getDefault().lookup(ProjectController.class);
    private final LayoutController layoutController = Lookup.getDefault().lookup(LayoutController.class);

    @Before
    public void setUp() {
        projectController.newProject();
    }

    @After
    public void tearDown() {
        projectController.closeCurrentProject();
    }

    @Test
    public void testRunningFromExecuteUntilFinished() throws InterruptedException {
        BlockingLayout layout = new BlockingLayout();
        layoutController.setLayout(layout);
        LayoutModel model = layoutController.getModel();
        List<Object> runningEvents = new CopyOnWriteArrayList<>();
        CountDownLatch finished = new CountDownLatch(1);
        model.addPropertyChangeListener(evt -> {
            if (LayoutModel.RUNNING.equals(evt.getPropertyName())) {
                runningEvents.add(evt.getNewValue());
                if (Boolean.FALSE.equals(evt.getNewValue())) {
                    finished.countDown();
                }
            }
        });

        layoutController.executeLayout(1);
        // Whether or not the layout thread has started yet
        Assert.assertTrue(model.isRunning());
        Assert.assertTrue(layoutController.canStop());
        Assert.assertFalse(layoutController.canExecute());

        layout.release.countDown();
        Assert.assertTrue(finished.await(30, TimeUnit.SECONDS));
        Assert.assertFalse(model.isRunning());
        Assert.assertEquals(List.of(true, false), runningEvents);
    }

    /**
     * Each iteration waits until the test releases it.
     */
    private static class BlockingLayout implements Layout {

        final CountDownLatch release = new CountDownLatch(1);
        private final LayoutBuilder builder = new MockLayoutBuilder();

        @Override
        public void initAlgo() {
        }

        @Override
        public void setGraphModel(GraphModel graphModel) {
        }

        @Override
        public void goAlgo() {
            try {
                release.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        @Override
        public boolean canAlgo() {
            return true;
        }

        @Override
        public void endAlgo() {
        }

        @Override
        public LayoutProperty[] getProperties() {
            return new LayoutProperty[0];
        }

        @Override
        public void resetPropertiesValues() {
        }

        @Override
        public LayoutBuilder getBuilder() {
            return builder;
        }
    }
}
