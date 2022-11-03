package org.gephi.project.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
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
import org.openide.util.Lookup;

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
