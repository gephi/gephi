package org.gephi.project.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.gephi.project.api.Project;
import org.gephi.project.api.ProjectListener;
import org.gephi.project.api.Workspace;
import org.gephi.project.api.WorkspaceListener;
import org.gephi.project.spi.Controller;
import org.gephi.project.spi.Model;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.netbeans.junit.MockServices;

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
    public void testSave() throws IOException, InterruptedException {
        ProjectControllerImpl pc = new ProjectControllerImpl();
        pc.addProjectListener(projectListener);
        Project project = pc.newProject();
        File file = tempFolder.newFile("save.gephi");

        // Use a latch to wait for async save to complete
        CountDownLatch latch = new CountDownLatch(1);

        pc.addProjectListener(new ProjectListener() {
            @Override
            public void saved(Project p) {
                latch.countDown();
            }

            @Override public void opened(Project project) {}
            @Override public void closed(Project project) {}
            @Override public void changed(Project project) {}
            @Override public void lock() {}
            @Override public void unlock() {}
            @Override public void error(Project project, Throwable throwable) {}
        });

        pc.saveProject(project, file);
        Assert.assertTrue("Timed out waiting for save", latch.await(5, TimeUnit.SECONDS));

        Assert.assertTrue(file.exists());
        Assert.assertTrue(project.hasFile());
        Assert.assertSame(file, project.getFile());
        Mockito.verify(projectListener).saved(project);
    }

    @Test
    public void testLoad() throws IOException, InterruptedException {
        MockServices.setServices(MockController.class);

        ProjectControllerImpl pc = new ProjectControllerImpl();
        pc.addProjectListener(projectListener);
        Project project = pc.newProject();
        File file = tempFolder.newFile("save.gephi");
        pc.saveProject(project, file);

        // Wait for save to complete
        Thread.sleep(100);

        // Use a latch to wait for async open to complete
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Project> openedProject = new AtomicReference<>();

        pc.addProjectListener(new ProjectListener() {
            @Override
            public void opened(Project p) {
                openedProject.set(p);
                latch.countDown();
            }

            @Override public void closed(Project project) {}
            @Override public void saved(Project project) {}
            @Override public void changed(Project project) {}
            @Override public void lock() {}
            @Override public void unlock() {}
            @Override public void error(Project project, Throwable throwable) {}
        });

        // openProject now returns null (async) - wait for opened event
        pc.openProject(file);
        Assert.assertTrue("Timed out waiting for project to open", latch.await(5, TimeUnit.SECONDS));

        Project loadedProject = openedProject.get();
        Assert.assertNotNull(loadedProject);
        Assert.assertTrue(loadedProject.isOpen());
        Mockito.verify(projectListener, Mockito.times(2)).opened(Mockito.any(Project.class));
        Assert.assertNotNull(loadedProject.getCurrentWorkspace().getLookup().lookup(MockModel.class));
    }

    @Test
    public void testOpenFileNotFound() throws IOException, InterruptedException {
        ProjectControllerImpl pc = new ProjectControllerImpl();
        pc.addProjectListener(projectListener);
        File file = tempFolder.newFile("foo.gephi");
        file.delete();

        // Use a latch to wait for async error
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Throwable> errorRef = new AtomicReference<>();

        pc.addProjectListener(new ProjectListener() {
            @Override
            public void error(Project project, Throwable throwable) {
                errorRef.set(throwable);
                latch.countDown();
            }

            @Override public void opened(Project project) {}
            @Override public void closed(Project project) {}
            @Override public void saved(Project project) {}
            @Override public void changed(Project project) {}
            @Override public void lock() {}
            @Override public void unlock() {}
        });

        pc.openProject(file);
        Assert.assertTrue("Timed out waiting for error", latch.await(5, TimeUnit.SECONDS));

        Throwable error = errorRef.get();
        Assert.assertNotNull("Expected error to be thrown", error);
        Assert.assertTrue("Expected FileNotFoundException cause",
            error instanceof RuntimeException && error.getCause() instanceof FileNotFoundException);
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
    public void testOpenAnotherProject() throws IOException, InterruptedException {
        ProjectControllerImpl pc = new ProjectControllerImpl();
        pc.addProjectListener(projectListener);
        pc.addWorkspaceListener(workspaceListener);
        Project project = pc.newProject();
        File file = tempFolder.newFile("project.gephi");
        pc.saveProject(project, file);

        // Wait for save to complete
        Thread.sleep(100);

        pc.closeCurrentProject();

        // Use a latch to wait for async open to complete
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Project> openedProject = new AtomicReference<>();

        pc.addProjectListener(new ProjectListener() {
            @Override
            public void opened(Project p) {
                openedProject.set(p);
                latch.countDown();
            }

            @Override public void closed(Project project) {}
            @Override public void saved(Project project) {}
            @Override public void changed(Project project) {}
            @Override public void lock() {}
            @Override public void unlock() {}
            @Override public void error(Project project, Throwable throwable) {}
        });

        pc.openProject(project);
        Assert.assertTrue("Timed out waiting for project to open", latch.await(5, TimeUnit.SECONDS));

        Project loadedProject = openedProject.get();
        Assert.assertTrue(loadedProject.isOpen());
        Assert.assertSame(loadedProject, pc.getCurrentProject());
        Mockito.verify(projectListener, Mockito.times(2)).opened(Mockito.any(Project.class));
        Mockito.verify(workspaceListener, Mockito.atLeast(1)).initialize(Mockito.any(Workspace.class));
    }

    @Test
    public void testDuplicateWorkspace() throws InterruptedException {
        MockServices.setServices(MockController.class);

        ProjectControllerImpl pc = new ProjectControllerImpl();
        pc.addWorkspaceListener(workspaceListener);
        pc.newProject();

        // duplicateWorkspace now returns null (async) - wait for workspace to be selected
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Workspace> selectedWorkspace = new AtomicReference<>();

        pc.addWorkspaceListener(new WorkspaceListener() {
            int selectCount = 0;

            @Override
            public void select(Workspace workspace) {
                selectCount++;
                // Second select is the duplicated workspace
                if (selectCount >= 2) {
                    selectedWorkspace.set(workspace);
                    latch.countDown();
                }
            }

            @Override public void initialize(Workspace workspace) {}
            @Override public void unselect(Workspace workspace) {}
            @Override public void close(Workspace workspace) {}
            @Override public void disable() {}
        });

        Workspace original = pc.getCurrentWorkspace();
        pc.duplicateWorkspace(original);
        Assert.assertTrue("Timed out waiting for workspace duplication", latch.await(5, TimeUnit.SECONDS));

        Workspace duplicate = selectedWorkspace.get();
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
}
