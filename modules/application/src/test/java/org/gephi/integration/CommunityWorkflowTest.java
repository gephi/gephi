package org.gephi.integration;

import java.awt.Color;
import java.io.File;
import org.gephi.appearance.api.AppearanceModel;
import org.gephi.appearance.api.Function;
import org.gephi.appearance.api.Partition;
import org.gephi.appearance.api.PartitionFunction;
import org.gephi.appearance.plugin.PartitionElementColorTransformer;
import org.gephi.appearance.plugin.palette.PaletteManager;
import org.gephi.filters.plugin.partition.PartitionBuilder;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.io.exporter.spi.GraphExporter;
import org.gephi.project.api.Workspace;
import org.gephi.statistics.plugin.Modularity;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Detects communities, colors and filters by them, then exports the visible graph: each step consumes what the
 * previous module produced.
 */
public class CommunityWorkflowTest extends AbstractIntegrationTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Test
    public void testModularityDrivesAppearanceFilterAndExport() throws Exception {
        // Two 6-cliques joined by a single edge, the node's "group" attribute is the expected community
        Workspace workspace = importFile(fixture("two-cliques.gexf"));
        GraphModel graphModel = graphController.getGraphModel(workspace);
        Graph graph = graphModel.getGraph();

        Modularity modularity = new Modularity();
        modularity.setRandom(false);
        runStatistics(modularity);
        Assert.assertNotNull(statisticsController.getModel(workspace).getReport(Modularity.class));
        Column communities = graphModel.getNodeTable().getColumn(Modularity.MODULARITY_CLASS);
        Assert.assertNotNull(communities);

        AppearanceModel appearanceModel = appearanceController.getModel(workspace);
        Function colorByCommunity =
            appearanceModel.getNodeFunction(communities, PartitionElementColorTransformer.class);
        Partition partition = ((PartitionFunction) colorByCommunity).getPartition();
        Assert.assertEquals(2, partition.size(graph));
        partition.setColors(graph, PaletteManager.getInstance().randomPalette(2).getColors());
        appearanceController.transform(colorByCommunity);

        Color colorA = graph.getNode("a0").getColor();
        Color colorB = graph.getNode("b0").getColor();
        Assert.assertNotEquals(colorA, colorB);
        for (Node node : graph.getNodes()) {
            Color expected = "a".equals(node.getAttribute("group")) ? colorA : colorB;
            Assert.assertEquals("Color of " + node.getId(), expected, node.getColor());
        }

        PartitionBuilder.NodePartitionFilter communityFilter =
            new PartitionBuilder.NodePartitionFilter(appearanceModel, appearanceModel.getNodePartition(communities));
        communityFilter.unselectAll();
        communityFilter.addPart(graph.getNode("a0").getAttribute(Modularity.MODULARITY_CLASS));
        filterVisible(filterController.createQuery(communityFilter));

        Graph visible = graphModel.getGraphVisible();
        Assert.assertEquals(6, visible.getNodeCount());
        Assert.assertEquals(15, visible.getEdgeCount());
        for (Node node : visible.getNodes()) {
            Assert.assertEquals("a", node.getAttribute("group"));
        }

        GraphExporter exporter = (GraphExporter) exportController.getExporter("gexf");
        exporter.setExportVisible(true);
        File file = tempFolder.newFile("community.gexf");
        exportController.exportFile(file, exporter);

        Graph exported = graphController.getGraphModel(importFile(file)).getGraph();
        Assert.assertEquals(6, exported.getNodeCount());
        Assert.assertEquals(15, exported.getEdgeCount());
        for (Node node : exported.getNodes()) {
            Assert.assertEquals("a", node.getAttribute("group"));
            Assert.assertEquals("Color of " + node.getId(), colorA, node.getColor());
        }
    }
}
