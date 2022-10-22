package org.gephi.project.impl;

import org.gephi.project.api.Project;
import org.junit.Assert;
import org.junit.Test;

public class ProjectsImplTest {

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
}
