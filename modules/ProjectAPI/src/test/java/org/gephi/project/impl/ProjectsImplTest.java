package org.gephi.project.impl;

import java.io.File;
import java.io.IOException;
import org.gephi.project.api.Project;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class ProjectsImplTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Test
    public void testProjectsSort() {
        ProjectsImpl projects = new ProjectsImpl();
        ProjectImpl p1 = new ProjectImpl("p1", "p1");
        p1.open();
        ProjectImpl p2 = new ProjectImpl("p2", "p2");
        p2.open();
        ProjectImpl p3 = new ProjectImpl("p3", "p3");

        projects.addProject(p3);
        projects.addProject(p1);
        projects.addProject(p2);

        Project[] res = projects.getProjects();
        Project[] expected = new Project[] {p2, p1, p3};
        Assert.assertArrayEquals(expected, res);
    }

    @Test
    public void testEmptyPersistence() throws IOException {
        ProjectsImpl projects = new ProjectsImpl();
        File file = tempFolder.newFile("projects.xml");
        projects.saveProjects(file);

        projects = new ProjectsImpl();
        projects.loadProjects(file);
        Assert.assertEquals(0, projects.getProjects().length);
    }

    @Test
    public void testPersistenceNoFile() throws IOException {
        ProjectsImpl projects = new ProjectsImpl();
        ProjectImpl p1 = new ProjectImpl("i1", "p1");
        projects.addProject(p1);

        File file = tempFolder.newFile("projects.xml");
        projects.saveProjects(file);

        projects = new ProjectsImpl();
        projects.loadProjects(file);
        Assert.assertEquals(1, projects.getProjects().length);
    }

    @Test
    public void testPersistenceFileNotExist() throws IOException {
        ProjectsImpl projects = new ProjectsImpl();
        ProjectImpl p1 = new ProjectImpl("i1", "p1");
        p1.setFile(new File("notexist.gephi"));
        projects.addProject(p1);

        File file = tempFolder.newFile("projects.xml");
        projects.saveProjects(file);

        projects = new ProjectsImpl();
        projects.loadProjects(file);
        Assert.assertEquals(0, projects.getProjects().length);
    }

    @Test
    public void testPersistence() throws IOException {
        ProjectsImpl projects = new ProjectsImpl();
        ProjectImpl p1 = new ProjectImpl("i1", "p1");
        p1.setFile(tempFolder.newFile("p1.gephi"));
        ProjectImpl p2 = new ProjectImpl("i2", "p2");
        p2.setFile(tempFolder.newFile("p2.gephi"));

        projects.addProject(p1);
        projects.addProject(p2);

        File file = tempFolder.newFile("projects.xml");
        projects.saveProjects(file);

        projects = new ProjectsImpl();
        projects.loadProjects(file);
        Assert.assertEquals(2, projects.getProjects().length);
        Assert.assertEquals("p1", projects.getProjectByIdentifier("i1").getName());
        Assert.assertEquals("p2", projects.getProjectByIdentifier("i2").getName());
    }
}
