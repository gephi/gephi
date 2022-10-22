package org.gephi.project.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.gephi.project.api.Project;
import org.gephi.project.api.Workspace;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

public class ProjectControllerImplTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

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
        ProjectControllerImpl pc = new ProjectControllerImpl();
        Project project = pc.newProject();
        Assert.assertTrue(pc.hasCurrentProject());
        Assert.assertFalse(pc.getAllProjects().isEmpty());
        Assert.assertSame(project, pc.getCurrentProject());
        Assert.assertTrue(project.isOpen());
    }

    @Test
    public void testCloseProject() {
        ProjectControllerImpl pc = new ProjectControllerImpl();
        Project project = pc.newProject();
        pc.closeCurrentProject();
        Assert.assertFalse(pc.hasCurrentProject());
        Assert.assertFalse(pc.getAllProjects().isEmpty());
        Assert.assertFalse(project.isOpen());
        Assert.assertTrue(project.isClosed());
    }

    @Test
    public void testRemoveProject() {
        ProjectControllerImpl pc = new ProjectControllerImpl();
        Project project = pc.newProject();
        pc.removeProject(project);
        Assert.assertFalse(pc.hasCurrentProject());
        Assert.assertTrue(pc.getAllProjects().isEmpty());
        Assert.assertTrue(project.isClosed());
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
        Project project = pc.newProject();
        File file = tempFolder.newFile("save.gephi");
        pc.saveProject(project, file);
        Assert.assertTrue(file.exists());
        Assert.assertTrue(project.hasFile());
        Assert.assertSame(file, project.getFile());
    }

    @Test
    public void testLoad() throws IOException {
        ProjectControllerImpl pc = new ProjectControllerImpl();
        Project project = pc.newProject();
        File file = tempFolder.newFile("save.gephi");
        pc.saveProject(project, file);
        project = pc.openProject(file);
        Assert.assertNotNull(project);
        Assert.assertTrue(project.isOpen());
    }

    @Test
    public void testOpenFileNotFound() throws IOException {
        expectedException.expect(RuntimeException.class);
        expectedException.expectCause(new org.hamcrest.core.IsInstanceOf(FileNotFoundException.class));

        ProjectControllerImpl pc = new ProjectControllerImpl();
        File file = tempFolder.newFile("foo.gephi");
        file.delete();
        pc.openProject(file);
    }

    @Test
    public void testDefaultWorkspace() {
        ProjectControllerImpl pc = new ProjectControllerImpl();
        Project project = pc.newProject();

        Assert.assertNotNull(pc.getCurrentWorkspace());
        Assert.assertSame(project, pc.getCurrentWorkspace().getProject());
        Assert.assertTrue(project.hasCurrentWorkspace());
        Assert.assertSame(pc.getCurrentWorkspace(), project.getCurrentWorkspace());
        Assert.assertTrue(project.getWorkspaces().contains(pc.getCurrentWorkspace()));
    }

    @Test
    public void testAddWorkspace() {
        ProjectControllerImpl pc = new ProjectControllerImpl();
        Project project = pc.newProject();
        Workspace workspace = pc.newWorkspace(project);

        Assert.assertNotSame(workspace, pc.getCurrentWorkspace());
        Assert.assertTrue(workspace.isClosed());
        Assert.assertTrue(project.hasCurrentWorkspace());
        Assert.assertSame(workspace.getProject(), project);
        Assert.assertEquals(2, project.getWorkspaces().size());
    }

    @Test
    public void testDeleteWorkspace() {
        ProjectControllerImpl pc = new ProjectControllerImpl();
        Project project = pc.newProject();
        Workspace originalWorkspace = pc.getCurrentWorkspace();
        Workspace workspace = pc.newWorkspace(project);
        pc.deleteWorkspace(workspace);

        Assert.assertTrue(workspace.isClosed());
        Assert.assertSame(originalWorkspace, pc.getCurrentWorkspace());
        Assert.assertTrue(project.getWorkspaces().contains(originalWorkspace));
    }

    @Test
    public void testDeleteLastWorkspace() {
        ProjectControllerImpl pc = new ProjectControllerImpl();
        Project project = pc.newProject();
        Workspace workspace = pc.getCurrentWorkspace();
        pc.deleteWorkspace(workspace);

        Assert.assertTrue(project.isClosed());
        Assert.assertNull(pc.getCurrentProject());
    }

    @Test
    public void testOpenWorkspace() {
        ProjectControllerImpl pc = new ProjectControllerImpl();
        Project project = pc.newProject();
        Workspace originalWorkspace = pc.getCurrentWorkspace();
        Workspace workspace = pc.newWorkspace(project);
        pc.openWorkspace(workspace);

        Assert.assertSame(workspace, pc.getCurrentWorkspace());
        Assert.assertTrue(originalWorkspace.isClosed());
        Assert.assertTrue(workspace.isOpen());
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
        ProjectControllerImpl pc = new ProjectControllerImpl();
        pc.newProject();
        Workspace workspace = pc.openNewWorkspace();
        Assert.assertNotNull(workspace);
        Assert.assertTrue(workspace.isOpen());
        Assert.assertSame(workspace, pc.getCurrentWorkspace());
        Assert.assertEquals(2, pc.getCurrentProject().getWorkspaces().size());
    }

    @Test
    public void testRenameProject() {
        ProjectControllerImpl pc = new ProjectControllerImpl();
        Project project = pc.newProject();
        pc.renameProject(project, "foo");
        Assert.assertEquals("foo", project.getName());
    }
}
