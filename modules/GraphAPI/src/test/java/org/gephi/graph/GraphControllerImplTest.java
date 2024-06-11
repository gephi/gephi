package org.gephi.graph;

import org.gephi.graph.api.Configuration;
import org.gephi.graph.api.GraphModel;
import org.gephi.project.api.Project;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.junit.Assert;
import org.junit.Test;
import org.openide.util.Lookup;

public class GraphControllerImplTest {

    @Test
    public void testDefaultConfiguration() {
        GraphControllerImpl graphController = new GraphControllerImpl();
        Configuration defaultConfiguration = graphController.getDefaultConfigurationBuilder().build();

        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        Project project = pc.newProject();
        Workspace workspace = pc.newWorkspace(project);

        GraphModel gm = graphController.getGraphModel(workspace);
        Assert.assertEquals(gm.getConfiguration(), defaultConfiguration);
    }

    @Test
    public void testWithoutConfiguration() {
        Configuration configuration = Configuration.builder().nodeIdType(Long.class).build();

        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        Project project = pc.newProject();
        Workspace workspace = pc.newWorkspace(project);

        GraphControllerImpl graphController = new GraphControllerImpl();
        GraphModel gm = graphController.getGraphModel(workspace);
        Assert.assertNotEquals(gm.getConfiguration(), configuration);
    }

    @Test
    public void testWithConfiguration() {
        GraphControllerImpl graphController = new GraphControllerImpl();
        Configuration configuration = graphController.getDefaultConfigurationBuilder().nodeIdType(Long.class).build();

        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        Project project = pc.newProject();
        Workspace workspace = pc.newWorkspace(project, configuration);

        GraphModel gm = graphController.getGraphModel(workspace);
        Assert.assertEquals(gm.getConfiguration(), configuration);
    }
}
