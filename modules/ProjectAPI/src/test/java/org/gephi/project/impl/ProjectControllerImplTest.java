package org.gephi.project.impl;

import java.io.File;
import java.io.IOException;
import org.gephi.project.api.Project;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class ProjectControllerImplTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

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
}
