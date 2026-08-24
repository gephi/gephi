package org.gephi.desktop.appearance;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.SwingUtilities;
import org.gephi.project.api.Project;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.openide.util.Lookup;

/**
 * Regression tests for the listener-registration race described in issue #3246 (finding 3):
 * {@link AppearanceUIController} used to register its {@link org.gephi.project.api.WorkspaceListener}
 * before its own {@code listeners}/{@code transformers} fields were assigned, and delivered
 * workspace events synchronously on whatever thread fired them.
 */
public class AppearanceUIControllerTest {

    private ProjectController pc;

    @After
    public void cleanup() {
        if (pc != null && pc.hasCurrentProject()) {
            pc.closeCurrentProject();
        }
    }

    @Test
    public void testUsableImmediatelyAfterConstruction() {
        pc = Lookup.getDefault().lookup(ProjectController.class);
        pc.newProject();

        AppearanceUIController controller = new AppearanceUIController();

        // A workspace event delivered right after construction must find the listener set and
        // the transformer registry already initialized, not null.
        Workspace workspace = pc.openNewWorkspace();

        Assert.assertNotNull(controller.getModel());
        Assert.assertNotNull(controller.getCategories(AppearanceUIController.NODE_ELEMENT));
    }

    @Test
    public void testSelectFiredFromBackgroundThreadDefersListenerToEdt() throws InterruptedException {
        pc = Lookup.getDefault().lookup(ProjectController.class);
        Project project = pc.newProject();

        AppearanceUIController controller = new AppearanceUIController();

        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean firedOnEdt = new AtomicBoolean(false);
        controller.addPropertyChangeListener(evt -> {
            firedOnEdt.set(SwingUtilities.isEventDispatchThread());
            latch.countDown();
        });

        Workspace workspace = pc.newWorkspace(project);
        Thread backgroundThread = new Thread(() -> pc.openWorkspace(workspace), "test-background-select");
        backgroundThread.start();
        backgroundThread.join();

        Assert.assertTrue("Listener should have fired", latch.await(5, TimeUnit.SECONDS));
        Assert.assertTrue("propertyChange must be delivered on the EDT, not the firing thread", firedOnEdt.get());
    }
}
