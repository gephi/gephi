package org.gephi.desktop.attributes;

import java.util.concurrent.CountDownLatch;
import javax.swing.SwingUtilities;
import org.gephi.graph.api.Node;
import org.gephi.project.api.Project;
import org.gephi.project.api.ProjectController;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.openide.util.Lookup;

/**
 * Regression test for the race between {@link AttributesUIControllerImpl#selectNodes(Node[])}
 * and edit mode. VizController's MOUSE_MOVE handler calls selectNodes with an empty array from
 * outside the EDT whenever nothing is under the cursor; the call is deferred with
 * SwingUtilities.invokeLater. If the user enters edit mode with a real selection while that
 * deferred call is still queued, it must not overwrite the real selection with the stale empty
 * array once it finally runs.
 */
public class AttributesUIControllerImplTest {

    private AttributesUIControllerImpl controller;
    private AttributesUIModelImpl model;

    @Before
    public void setUp() {
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        Project project = pc.newProject();
        model = project.getCurrentWorkspace().getLookup().lookup(AttributesUIModelImpl.class);
        controller = new AttributesUIControllerImpl();
    }

    @After
    public void tearDown() {
        Lookup.getDefault().lookup(ProjectController.class).closeCurrentProject();
    }

    @Test
    public void testDeferredEmptySelectionDoesNotOverwriteLaterEditModeSelection() throws Exception {
        Node node = Mockito.mock(Node.class);
        CountDownLatch blockEdt = new CountDownLatch(1);

        // Hold the EDT so the callback queued by selectNodes() below cannot run until we release it.
        SwingUtilities.invokeLater(() -> {
            try {
                blockEdt.await();
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        });

        // Like VizController's MOUSE_MOVE handler: call selectNodes with an empty array from
        // outside the EDT while not in edit mode. The guard passes and a deferred callback is queued.
        Thread mouseMove = new Thread(() -> controller.selectNodes(new Node[0]));
        mouseMove.start();
        mouseMove.join();

        // The user enters edit mode with a real selection before that deferred callback runs.
        model.setEditMode(true);
        model.setSelectedNodes(new Node[] {node});

        // Release the EDT so the queued callback finally runs, then flush the queue.
        blockEdt.countDown();
        SwingUtilities.invokeAndWait(() -> {
        });

        Assert.assertArrayEquals(new Node[] {node}, model.getSelectedNodes());
    }
}
