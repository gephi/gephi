package org.gephi.integration;

import java.io.File;
import java.io.IOException;
import org.gephi.graph.GraphGenerator;
import org.gephi.preview.api.PreviewModel;
import org.gephi.preview.api.PreviewProperty;
import org.gephi.project.api.Project;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class ProjectIOTest extends AbstractIntegrationTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Test
    public void testSave() throws IOException {
        Project project = projectController.newProject();
        GraphGenerator.build(project.getCurrentWorkspace()).generateTinyGraph();

        File fileCreatedEmpty = tempFolder.newFile("test.gephi");
        File fileNotExisting = new File(tempFolder.getRoot(), "test2.gephi");
        projectController.saveProject(project, fileCreatedEmpty);
        projectController.saveProject(project, fileNotExisting);
        projectController.closeCurrentProject();

        Assert.assertTrue(fileCreatedEmpty.length() > 0);
        Assert.assertTrue(fileNotExisting.length() > 0);
        assertTinyGraph(fileCreatedEmpty);
        assertTinyGraph(fileNotExisting);
    }

    @Test
    public void testSavePreviewModel() throws IOException {
        Project project = projectController.newProject();
        GraphGenerator.build(project.getCurrentWorkspace()).generateTinyGraph();
        PreviewModel previewModel = previewController.getModel();
        Assert.assertFalse(previewModel.getProperties().getBooleanValue(PreviewProperty.SHOW_NODE_LABELS));
        previewModel.getProperties().putValue(PreviewProperty.SHOW_NODE_LABELS, Boolean.TRUE);

        File file = new File(tempFolder.getRoot(), "testpreview.gephi");
        projectController.saveProject(project, file);
        projectController.closeCurrentProject();
        projectController.openProject(file);

        PreviewModel readPreviewModel = previewController.getModel();
        Assert.assertNotSame(previewModel, readPreviewModel);
        Assert.assertTrue(readPreviewModel.getProperties().getBooleanValue(PreviewProperty.SHOW_NODE_LABELS));
    }

    private void assertTinyGraph(File file) {
        projectController.closeCurrentProject();
        Project project = projectController.openProject(file);
        Assert.assertEquals(2, graphController.getGraphModel(project.getCurrentWorkspace()).getGraph().getNodeCount());
    }
}
