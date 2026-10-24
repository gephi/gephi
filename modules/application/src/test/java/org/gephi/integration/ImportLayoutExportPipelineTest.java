package org.gephi.integration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;
import org.gephi.layout.plugin.forceAtlas2.ForceAtlas2;
import org.gephi.layout.plugin.forceAtlas2.ForceAtlas2Builder;
import org.gephi.project.api.Workspace;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Drives a GEXF import, a ForceAtlas 2 run and a GEXF export through the real controllers.
 */
public class ImportLayoutExportPipelineTest extends AbstractIntegrationTest {

    private static final float POSITION_EPS = 0.01f;

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Test
    public void testImportLayoutExportRoundTrip() throws Exception {
        Workspace workspace = importFile(fixture("two-cliques.gexf"));
        Graph graph = graphController.getGraphModel(workspace).getGraph();
        Assert.assertEquals(12, graph.getNodeCount());
        Assert.assertEquals(31, graph.getEdgeCount());
        Assert.assertTrue("Cliques should start interleaved", separation(graph) < 1.0);

        ForceAtlas2 layout = new ForceAtlas2Builder().buildLayout();
        layout.setGraphModel(graphController.getGraphModel(workspace));
        layout.setThreadsCount(1);
        layoutController.setLayout(layout);
        runLayout(500);

        for (Node node : graph.getNodes()) {
            Assert.assertTrue("Non finite position for " + node.getId(),
                Float.isFinite(node.x()) && Float.isFinite(node.y()));
        }
        Assert.assertTrue("Cliques should be pulled apart, separation was " + separation(graph),
            separation(graph) > 2.0);

        File exported = tempFolder.newFile("two-cliques-laid-out.gexf");
        exportController.exportFile(exported);

        Workspace reimported = importFile(exported);
        Graph reimportedGraph = graphController.getGraphModel(reimported).getGraph();
        Assert.assertEquals(graph.getNodeCount(), reimportedGraph.getNodeCount());
        Assert.assertEquals(graph.getEdgeCount(), reimportedGraph.getEdgeCount());
        for (Node node : graph.getNodes()) {
            Node copy = reimportedGraph.getNode(node.getId());
            Assert.assertNotNull("Missing node " + node.getId(), copy);
            Assert.assertEquals(node.x(), copy.x(), POSITION_EPS);
            Assert.assertEquals(node.y(), copy.y(), POSITION_EPS);
            Assert.assertEquals(node.getAttribute("group"), copy.getAttribute("group"));
        }
    }

    /**
     * Distance between the two group centroids divided by the mean distance of nodes to their own centroid.
     */
    private static double separation(Graph graph) {
        Map<Object, double[]> centroids = new HashMap<>();
        for (Node node : graph.getNodes()) {
            double[] c = centroids.computeIfAbsent(node.getAttribute("group"), g -> new double[3]);
            c[0] += node.x();
            c[1] += node.y();
            c[2]++;
        }
        centroids.values().forEach(c -> {
            c[0] /= c[2];
            c[1] /= c[2];
        });
        double spread = 0;
        for (Node node : graph.getNodes()) {
            double[] c = centroids.get(node.getAttribute("group"));
            spread += Math.hypot(node.x() - c[0], node.y() - c[1]);
        }
        spread /= graph.getNodeCount();
        double[] a = centroids.get("a");
        double[] b = centroids.get("b");
        return Math.hypot(a[0] - b[0], a[1] - b[1]) / spread;
    }
}
