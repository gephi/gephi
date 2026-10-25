package org.gephi.integration;

import java.awt.Color;
import org.gephi.appearance.api.AppearanceModel;
import org.gephi.appearance.api.Function;
import org.gephi.appearance.api.Partition;
import org.gephi.appearance.api.PartitionFunction;
import org.gephi.appearance.api.RankingFunction;
import org.gephi.appearance.plugin.PartitionElementColorTransformer;
import org.gephi.appearance.plugin.RankingElementColorTransformer;
import org.gephi.appearance.plugin.palette.Palette;
import org.gephi.appearance.plugin.palette.PaletteManager;
import org.gephi.graph.GraphGenerator;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;
import org.gephi.project.api.Project;
import org.junit.Assert;
import org.junit.Test;

public class AppearanceTest extends AbstractIntegrationTest {

    @Test
    public void testPartition() {
        Project project = projectController.newProject();
        AppearanceModel appearanceModel = appearanceController.getModel(project.getCurrentWorkspace());
        Graph graph =
            GraphGenerator.build(project.getCurrentWorkspace()).generateTinyGraph().addStringNodeColumn().getGraph();

        Column column = graph.getModel().getNodeTable().getColumn(GraphGenerator.STRING_COLUMN);
        Function func = appearanceModel.getNodeFunction(column, PartitionElementColorTransformer.class);
        Partition partition = ((PartitionFunction) func).getPartition();
        Palette palette = PaletteManager.getInstance().randomPalette(2);
        partition.setColors(graph, palette.getColors());
        appearanceController.transform(func);

        Assert.assertEquals(palette.getColors()[0], graph.getNode(GraphGenerator.FIRST_NODE).getColor());
        Assert.assertEquals(palette.getColors()[1], graph.getNode(GraphGenerator.SECOND_NODE).getColor());
    }

    @Test
    public void testRanking() {
        Project project = projectController.newProject();
        AppearanceModel appearanceModel = appearanceController.getModel(project.getCurrentWorkspace());
        Graph graph = GraphGenerator.build(project.getCurrentWorkspace()).generateSmallRandomGraph().getGraph();

        Function degreeRanking = appearanceModel.getNodeFunction(graph.getModel().defaultColumns()
            .degree(), RankingElementColorTransformer.class);
        RankingElementColorTransformer degreeTransformer = degreeRanking.getTransformer();
        Color[] colors = new Color[] {new Color(0xFEF0D9), new Color(0xB30000)};
        degreeTransformer.setColors(colors);
        degreeTransformer.setColorPositions(new float[] {0f, 1f});
        appearanceController.transform(degreeRanking);

        Number minValue = ((RankingFunction) degreeRanking).getRanking().getMinValue(graph);
        Number maxValue = ((RankingFunction) degreeRanking).getRanking().getMaxValue(graph);
        for (Node node : graph.getNodes()) {
            int degree = graph.getDegree(node);
            if (degree == minValue.intValue()) {
                Assert.assertEquals(colors[0], node.getColor());
            } else if (degree == maxValue.intValue()) {
                Assert.assertEquals(colors[1], node.getColor());
            } else {
                Assert.assertNotEquals(colors[0], node.getColor());
                Assert.assertNotEquals(colors[1], node.getColor());
            }
        }
    }
}
