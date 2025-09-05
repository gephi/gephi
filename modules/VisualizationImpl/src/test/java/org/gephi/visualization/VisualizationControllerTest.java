package org.gephi.visualization;

import org.gephi.graph.GraphGenerator;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class VisualizationControllerTest {

    @Spy
    private VizController vizController = new VizController();

    @Test
    public void testController() {
        GraphGenerator generator = GraphGenerator.build().generateTinyGraph();

        VizModel vizModel = vizController.getModel(generator.getWorkspace());
        Assert.assertSame(vizModel.getWorkspace(), generator.getWorkspace());
    }
}
