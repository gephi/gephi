package org.gephi.project.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.gephi.project.api.GephiFormatException;
import org.gephi.project.api.Project;
import org.gephi.project.api.ProjectListener;
import org.gephi.project.api.Workspace;
import org.gephi.project.api.WorkspaceListener;
import org.gephi.project.io.utils.MockBytesPersistenceProviderFailWrite;
import org.gephi.project.spi.Controller;
import org.gephi.project.spi.Model;
import org.gephi.utils.progress.ProgressTicket;
import org.gephi.utils.progress.ProgressTicketProvider;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.netbeans.junit.MockServices;
import org.openide.util.Cancellable;

@RunWith(MockitoJUnitRunner.class)
public class ProjectControllerImplTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Mock
    private ProjectListener projectListener;

    @Mock
    private WorkspaceListener workspaceListener;

    @After
    public void resetServices() {
        // Services are registered globally, don't leak them into the next test
        MockServices.setServices();
    }

    @Test
    public void testInit() {
        ProjectControllerImpl pc = new ProjectControllerImpl();
        Assert.assertFalse(pc.hasCurrentProject());
        Assert.assertTrue(pc.getAllProjects().isEmpty());
        Assert.assertNull(pc.getCurrentProject());
        Assert.assertNull(pc.getCurrentWorkspace());
    }

    @Test
    public void testNewProject() {
        MockServices.setServices(MockController.class);

        ProjectControllerImpl pc = new ProjectControllerImpl();
        pc.addProjectListener(projectListener);
        Project project = pc.newProject();
        Assert.assertTrue(pc.hasCurrentProject());
        Assert.assertFalse(pc.getAllProjects().isEmpty());
        Assert.assertSame(project, pc.getCurrentProject());
        Assert.assertTrue(project.isOpen());
        Mockito.verify(projectListener).opened(project);
        Assert.assertNotNull(project.getCurrentWorkspace().getLookup().lookup(MockModel.class));
    }

    @Test
    public void testCloseProject() {
        ProjectControllerImpl pc = new ProjectControllerImpl();
        pc.addProjectListener(projectListener);
        Project project = pc.newProject();
        pc.closeCurrentProject();
        Assert.assertFalse(pc.hasCurrentProject());
        Assert.assertFalse(pc.getAllProjects().isEmpty());
        Assert.assertFalse(project.isOpen());
        Assert.assertTrue(project.isClosed());
        Mockito.verify(projectListener).closed(project);
    }

    @Test
    public void testRemoveProject() {
        ProjectControllerImpl pc = new ProjectControllerImpl();
        pc.addProjectListener(projectListener);
        Project project = pc.newProject();
        pc.removeProject(project);
        Assert.assertFalse(pc.hasCurrentProject());
        Assert.assertTrue(pc.getAllProjects().isEmpty());
        Assert.assertTrue(project.isClosed());
        Mockito.verify(projectListener).closed(project);
    }

    @Test
    public void testMultipleProjects() {
        ProjectControllerImpl pc = new ProjectControllerImpl();
        Project project1 = pc.newProject();
        Project project2 = pc.newProject();
        Assert.assertSame(project2, pc.getCurrentProject());
        Assert.assertTrue(project1.isClosed());
        Assert.assertEquals(2, pc.getAllProjects().size());
    }

    @Test
    public void testSave() throws IOException {
        ProjectControllerImpl pc = new ProjectControllerImpl();
        pc.addProjectListener(projectListener);
        Project project = pc.newProject();
        File file = tempFolder.newFile("save.gephi");
        pc.saveProject(project, file);
        Assert.assertTrue(file.exists());
        Assert.assertTrue(project.hasFile());
        Assert.assertSame(file, project.getFile());
        Mockito.verify(projectListener).saved(project);
    }

    /**
     * Regression test: a save that fails must leave the project file unchanged, as the project isn't associated with
     * a file that was never written.
     */
    @Test
    public void testFailedSaveDoesNotSetFile() {
        MockServices.setServices(MockBytesPersistenceProviderFailWrite.class);

        ProjectControllerImpl pc = new ProjectControllerImpl();
        pc.addProjectListener(projectListener);
        Project project = pc.newProject();
        Assert.assertFalse(project.hasFile());

        File file = new File(tempFolder.getRoot(), "failed.gephi");
        try {
            pc.saveProject(project, file);
            Assert.fail("Expected the save to fail");
        } catch (GephiFormatException expected) {
        }

        Assert.assertFalse("A failed save must not associate the project with the file", project.hasFile());
        Assert.assertNull(project.getFile());
        Assert.assertFalse(file.exists());
        Mockito.verify(projectListener, Mockito.never()).saved(project);
    }

    @Test
    public void testLoad() throws IOException {
        MockServices.setServices(MockController.class);

        ProjectControllerImpl pc = new ProjectControllerImpl();
        pc.addProjectListener(projectListener);
        Project project = pc.newProject();
        File file = tempFolder.newFile("save.gephi");
        pc.saveProject(project, file);
        project = pc.openProject(file);
        Assert.assertNotNull(project);
        Assert.assertTrue(project.isOpen());
        Mockito.verify(projectListener, Mockito.times(2)).opened(project);
        Assert.assertNotNull(project.getCurrentWorkspace().getLookup().lookup(MockModel.class));
    }

    @Test
    public void testOpenFileNotFound() throws IOException {
        expectedException.expect(RuntimeException.class);
        expectedException.expectCause(new org.hamcrest.core.IsInstanceOf(FileNotFoundException.class));

        ProjectControllerImpl pc = new ProjectControllerImpl();
        pc.addProjectListener(projectListener);
        File file = tempFolder.newFile("foo.gephi");
        file.delete();
        pc.openProject(file);
        Mockito.verify(projectListener).error(Mockito.isNull(), Mockito.any(RuntimeException.class));
    }

    @Test
    public void testDefaultWorkspace() {
        ProjectControllerImpl pc = new ProjectControllerImpl();
        pc.addWorkspaceListener(workspaceListener);
        Project project = pc.newProject();

        Assert.assertNotNull(pc.getCurrentWorkspace());
        Assert.assertSame(project, pc.getCurrentWorkspace().getProject());
        Assert.assertTrue(project.hasCurrentWorkspace());
        Assert.assertSame(pc.getCurrentWorkspace(), project.getCurrentWorkspace());
        Assert.assertTrue(project.getWorkspaces().contains(pc.getCurrentWorkspace()));
        Mockito.verify(workspaceListener).initialize(pc.getCurrentWorkspace());
        Mockito.verify(workspaceListener).select(pc.getCurrentWorkspace());
    }

    @Test
    public void testAddWorkspace() {
        MockServices.setServices(MockController.class);

        ProjectControllerImpl pc = new ProjectControllerImpl();
        pc.addWorkspaceListener(workspaceListener);
        Project project = pc.newProject();
        Workspace workspace = pc.newWorkspace(project);

        Assert.assertNotSame(workspace, pc.getCurrentWorkspace());
        Assert.assertTrue(workspace.isClosed());
        Assert.assertTrue(project.hasCurrentWorkspace());
        Assert.assertSame(workspace.getProject(), project);
        Assert.assertEquals(2, project.getWorkspaces().size());
        Mockito.verify(workspaceListener).initialize(workspace);
        Assert.assertNotNull(workspace.getLookup().lookup(MockModel.class));
    }

    @Test
    public void testDeleteWorkspace() {
        ProjectControllerImpl pc = new ProjectControllerImpl();
        pc.addWorkspaceListener(workspaceListener);
        Project project = pc.newProject();
        Workspace originalWorkspace = pc.getCurrentWorkspace();
        Workspace workspace = pc.newWorkspace(project);
        pc.deleteWorkspace(workspace);

        Assert.assertTrue(workspace.isClosed());
        Assert.assertSame(originalWorkspace, pc.getCurrentWorkspace());
        Assert.assertTrue(project.getWorkspaces().contains(originalWorkspace));
        Mockito.verify(workspaceListener).close(workspace);
        Mockito.verify(workspaceListener, Mockito.never()).unselect(workspace);
    }

    @Test
    public void testDeleteSelectedWorkspace() {
        ProjectControllerImpl pc = new ProjectControllerImpl();
        pc.addWorkspaceListener(workspaceListener);
        Project project = pc.newProject();
        Workspace originalWorkspace = pc.getCurrentWorkspace();
        Workspace workspace = pc.newWorkspace(project);
        pc.deleteWorkspace(originalWorkspace);

        Assert.assertSame(workspace, pc.getCurrentWorkspace());
        Mockito.verify(workspaceListener).close(originalWorkspace);
        Mockito.verify(workspaceListener).select(workspace);
        Mockito.verify(workspaceListener).unselect(originalWorkspace);
    }

    @Test
    public void testDeleteLastWorkspace() {
        ProjectControllerImpl pc = new ProjectControllerImpl();
        pc.addWorkspaceListener(workspaceListener);
        pc.addProjectListener(projectListener);
        Project project = pc.newProject();
        Workspace workspace = pc.getCurrentWorkspace();
        pc.deleteWorkspace(workspace);

        Assert.assertTrue(project.isClosed());
        Assert.assertNull(pc.getCurrentProject());
        Mockito.verify(workspaceListener).unselect(workspace);
        Mockito.verify(workspaceListener).close(workspace);
        Mockito.verify(projectListener).closed(project);
    }

    @Test
    public void testOpenWorkspace() {
        ProjectControllerImpl pc = new ProjectControllerImpl();
        pc.addWorkspaceListener(workspaceListener);
        Project project = pc.newProject();
        Workspace originalWorkspace = pc.getCurrentWorkspace();
        Workspace workspace = pc.newWorkspace(project);
        pc.openWorkspace(workspace);

        Assert.assertSame(workspace, pc.getCurrentWorkspace());
        Assert.assertTrue(originalWorkspace.isClosed());
        Assert.assertTrue(workspace.isOpen());
        Mockito.verify(workspaceListener).unselect(originalWorkspace);
        Mockito.verify(workspaceListener).select(workspace);
    }

    @Test
    public void testCloseWorkspace() {
        ProjectControllerImpl pc = new ProjectControllerImpl();
        pc.newProject();
        Workspace workspace = pc.getCurrentWorkspace();
        pc.closeCurrentWorkspace();

        Assert.assertTrue(workspace.isClosed());
        // TODO: Should we make it null?
//        Assert.assertNull(pc.getCurrentWorkspace());
    }

    @Test
    public void testOpenNewWorkspace() {
        MockServices.setServices(MockController.class);

        ProjectControllerImpl pc = new ProjectControllerImpl();
        pc.addWorkspaceListener(workspaceListener);
        pc.newProject();
        Workspace workspace = pc.openNewWorkspace();
        Assert.assertNotNull(workspace);
        Assert.assertTrue(workspace.isOpen());
        Assert.assertSame(workspace, pc.getCurrentWorkspace());
        Assert.assertEquals(2, pc.getCurrentProject().getWorkspaces().size());
        Mockito.verify(workspaceListener).initialize(workspace);
        Mockito.verify(workspaceListener).select(workspace);
        Assert.assertNotNull(workspace.getLookup().lookup(MockModel.class));
    }

    @Test
    public void testOpenNewWorkspaceNoProject() {
        MockServices.setServices(MockController.class);

        ProjectControllerImpl pc = new ProjectControllerImpl();
        pc.addWorkspaceListener(workspaceListener);
        Workspace workspace = pc.openNewWorkspace();
        Assert.assertNotNull(workspace);
        Assert.assertTrue(workspace.isOpen());
        Assert.assertSame(workspace, pc.getCurrentWorkspace());
        Assert.assertEquals(1, pc.getCurrentProject().getWorkspaces().size());
        Mockito.verify(workspaceListener).initialize(workspace);
        Mockito.verify(workspaceListener).select(workspace);
        Assert.assertNotNull(workspace.getLookup().lookup(MockModel.class));
    }

    @Test
    public void testRenameProject() {
        ProjectControllerImpl pc = new ProjectControllerImpl();
        pc.addProjectListener(projectListener);
        Project project = pc.newProject();
        pc.renameProject(project, "foo");
        Assert.assertEquals("foo", project.getName());
        Mockito.verify(projectListener).changed(project);
    }

    @Test
    public void testOpenAnotherProject() throws IOException {
        ProjectControllerImpl pc = new ProjectControllerImpl();
        pc.addProjectListener(projectListener);
        pc.addWorkspaceListener(workspaceListener);
        Project project = pc.newProject();
        File file = tempFolder.newFile("project.gephi");
        pc.saveProject(project, file);
        pc.closeCurrentProject();
        pc.openProject(project);
        Assert.assertTrue(project.isOpen());
        Assert.assertSame(project, pc.getCurrentProject());
        Mockito.verify(projectListener, Mockito.times(2)).opened(project);
        Mockito.verify(workspaceListener).initialize(pc.getCurrentWorkspace());
    }

    @Test
    public void testDuplicateWorkspace() {
        MockServices.setServices(MockController.class);

        ProjectControllerImpl pc = new ProjectControllerImpl();
        pc.addWorkspaceListener(workspaceListener);
        pc.newProject();
        Workspace duplicate = pc.duplicateWorkspace(pc.getCurrentWorkspace());
        Assert.assertNotNull(duplicate);
        Assert.assertTrue(duplicate.isOpen());
        Assert.assertSame(duplicate, pc.getCurrentWorkspace());
        Assert.assertEquals(2, pc.getCurrentProject().getWorkspaces().size());
        Mockito.verify(workspaceListener).initialize(duplicate);
        Mockito.verify(workspaceListener).select(duplicate);
        Assert.assertNotNull(duplicate.getLookup().lookup(MockModel.class));
    }

    @Test
    public void testNewWorkspaceWithLookupObjects() {
        MockServices.setServices(MockController.class);

        ProjectControllerImpl pc = new ProjectControllerImpl();
        pc.addWorkspaceListener(workspaceListener);
        Project project = pc.newProject();
        String foo = "foo";
        Workspace workspace = pc.newWorkspace(project, foo);
        Assert.assertEquals(foo, workspace.getLookup().lookup(String.class));
    }

    @Test
    public void testOpenNewWorkspaceWithLookupObjects() {
        MockServices.setServices(MockController.class);

        ProjectControllerImpl pc = new ProjectControllerImpl();
        pc.addWorkspaceListener(workspaceListener);
        String foo = "foo";
        Workspace workspace = pc.openNewWorkspace(foo);
        Assert.assertEquals(foo, workspace.getLookup().lookup(String.class));
    }

    /**
     * Mutating methods notify their listeners synchronously, so they must never run on the Event Dispatch Thread. The
     * controller warns when they do, and stays silent otherwise.
     */
    @Test
    public void testWarnsWhenMutatingFromEventDispatchThread() throws Exception {
        String eventQueueThreadName = eventQueueThreadName();

        // The controller detects the EDT by thread name so that it doesn't have to touch AWT, hence there is nothing
        // to verify in an environment that names its event queue thread differently
        Assume.assumeTrue("Unexpected event queue thread name: " + eventQueueThreadName,
            eventQueueThreadName != null && eventQueueThreadName.startsWith("AWT-EventQueue"));

        ProjectControllerImpl pc = new ProjectControllerImpl();
        Project project = pc.newProject();

        List<LogRecord> records = Collections.synchronizedList(new ArrayList<>());
        Handler handler = new Handler() {
            @Override
            public void publish(LogRecord record) {
                records.add(record);
            }

            @Override
            public void flush() {
            }

            @Override
            public void close() {
            }
        };
        Logger logger = Logger.getLogger(ProjectControllerImpl.class.getName());
        boolean useParentHandlers = logger.getUseParentHandlers();
        logger.addHandler(handler);
        // The expected warning would otherwise be printed with its stack trace and read as a build failure
        logger.setUseParentHandlers(false);
        try {
            pc.renameProject(project, "background");
            Assert.assertTrue("A call from outside the event queue must not warn", records.isEmpty());

            SwingUtilities.invokeAndWait(() -> pc.renameProject(project, "edt"));
            Assert.assertEquals("A call from the event queue must warn once", 1, records.size());
            LogRecord record = records.get(0);
            Assert.assertEquals(Level.WARNING, record.getLevel());
            Assert.assertTrue("The warning must name the offending method: " + record.getMessage(),
                record.getMessage().contains("renameProject"));
            Assert.assertTrue("The warning must include the call site", record.getMessage().contains("\tat "));
            // A logged throwable would be turned into a crash report by the desktop application
            Assert.assertNull("The warning must not carry a throwable", record.getThrown());
        } finally {
            logger.setUseParentHandlers(useParentHandlers);
            logger.removeHandler(handler);
        }
    }

    /**
     * Returns the name of the event queue thread, or skips the test if this environment has no usable event queue.
     */
    private String eventQueueThreadName() {
        String[] name = new String[1];
        try {
            SwingUtilities.invokeAndWait(() -> name[0] = Thread.currentThread().getName());
        } catch (Throwable t) {
            Assume.assumeNoException("No usable event queue in this environment", t);
        }
        return name[0];
    }

    /**
     * Regression test for the save-then-close flow in the desktop UI: it saves the project and only
     * then closes it, so it has to be able to tell a cancelled save from a successful one. A cancel
     * writes nothing and reports no failure, so the absence of <code>saved()</code> is the only
     * signal available. Should this contract change, the UI would close the project and throw away
     * whatever wasn't written.
     */
    @Test
    public void testCancelledSaveWritesNothingAndDoesNotReportSaved() {
        MockServices.setServices(CancellingProgressTicketProvider.class);

        ProjectControllerImpl pc = new ProjectControllerImpl();
        pc.addProjectListener(projectListener);
        Project project = pc.newProject();

        File file = new File(tempFolder.getRoot(), "cancelled.gephi");
        pc.saveProject(project, file);

        Assert.assertFalse("A cancelled save must not leave a file behind", file.exists());
        Mockito.verify(projectListener, Mockito.never()).saved(project);
    }

    public static class MockModel implements Model {

        private final Workspace workspace;

        public MockModel(Workspace workspace) {
            this.workspace = workspace;
        }

        @Override
        public Workspace getWorkspace() {
            return workspace;
        }
    }

    public static class MockController implements Controller {


        @Override
        public Model newModel(Workspace workspace) {
            return new MockModel(workspace);
        }

        @Override
        public Class getModelClass() {
            return MockModel.class;
        }
    }

    /**
     * Hands out a ticket that cancels the running task the first time it reports progress, which is
     * what the progress bar's Cancel button does.
     */
    public static class CancellingProgressTicketProvider implements ProgressTicketProvider {

        @Override
        public ProgressTicket createTicket(String taskName, Cancellable cancellable) {
            return new CancellingProgressTicket(cancellable);
        }
    }

    private static class CancellingProgressTicket implements ProgressTicket {

        private final Cancellable cancellable;
        private boolean cancelled;

        CancellingProgressTicket(Cancellable cancellable) {
            this.cancellable = cancellable;
        }

        private void cancelOnce() {
            if (!cancelled && cancellable != null) {
                cancelled = true;
                cancellable.cancel();
            }
        }

        @Override
        public void finish() {
        }

        @Override
        public void finish(String finishMessage) {
        }

        @Override
        public void progress() {
            cancelOnce();
        }

        @Override
        public void progress(int workunit) {
            cancelOnce();
        }

        @Override
        public void progress(String message) {
            cancelOnce();
        }

        @Override
        public void progress(String message, int workunit) {
            cancelOnce();
        }

        @Override
        public String getDisplayName() {
            return "";
        }

        @Override
        public void setDisplayName(String newDisplayName) {
        }

        @Override
        public void start() {
        }

        @Override
        public void start(int workunits) {
        }

        @Override
        public void switchToDeterminate(int workunits) {
        }

        @Override
        public void switchToIndeterminate() {
        }
    }
}
