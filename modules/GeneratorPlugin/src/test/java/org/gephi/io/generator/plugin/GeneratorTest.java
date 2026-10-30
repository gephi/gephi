package org.gephi.io.generator.plugin;

import org.gephi.graph.api.TimeRepresentation;
import org.gephi.io.importer.api.Container;
import org.gephi.io.importer.api.ContainerUnloader;
import org.gephi.io.importer.api.EdgeDraft;
import org.gephi.io.importer.api.NodeDraft;
import org.gephi.io.importer.impl.ImportContainerImpl;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class GeneratorTest {

    @Test
    public void testRandomGraph() {
        RandomGraph randomGraph = new RandomGraph();
        randomGraph.setNumberOfNodes(500);
        randomGraph.setWiringProbability(0.005);
        Container container = new ImportContainerImpl();
        randomGraph.generate(container.getLoader());

        Assert.assertEquals(500, container.getUnloader().getNodeCount());
    }

    @Test
    public void testDynamicGraph() {
        Container container = new ImportContainerImpl();
        new DynamicGraph().generate(container.getLoader());

        ContainerUnloader unloader = container.getUnloader();
        Assert.assertEquals(TimeRepresentation.TIMESTAMP, unloader.getTimeRepresentation());
        Assert.assertTrue(unloader.getNodeColumn("score").isDynamic());
        Assert.assertTrue(unloader.getEdgeColumn("weight").isDynamic());
    }

    @Test
    public void testScaleFreeGraphStructure() {
        int n = 500;
        int m = 3;
        ContainerUnloader unloader = generateScaleFree(n, m, 42L);

        Assert.assertEquals(n, unloader.getNodeCount());
        Assert.assertEquals(m + (n - m - 1) * m, unloader.getEdgeCount());

        Map<Object, Integer> degrees = new HashMap<>();
        Set<String> pairs = new HashSet<>();
        for (EdgeDraft edge : unloader.getEdges()) {
            Object source = edge.getSource().getId();
            Object target = edge.getTarget().getId();
            Assert.assertNotEquals("No self-loops", source, target);
            Assert.assertTrue("No parallel edges", pairs.add(source + "-" + target) && !pairs.contains(target + "-" + source));
            degrees.merge(source, 1, Integer::sum);
            degrees.merge(target, 1, Integer::sum);
        }
        Assert.assertEquals("No isolated nodes", n, degrees.size());

        // Every node past the initial star attaches to m nodes, so it has degree m at least
        int belowM = 0;
        for (int degree : degrees.values()) {
            if (degree < m) {
                belowM++;
            }
        }
        Assert.assertTrue("Only leaves of the initial star can stay below degree m", belowM <= m);
    }

    @Test
    public void testScaleFreeGraphDegreeDistribution() {
        int n = 20000;
        int m = 3;
        ContainerUnloader unloader = generateScaleFree(n, m, 7L);

        Map<Object, Integer> degrees = new HashMap<>();
        for (EdgeDraft edge : unloader.getEdges()) {
            degrees.merge(edge.getSource().getId(), 1, Integer::sum);
            degrees.merge(edge.getTarget().getId(), 1, Integer::sum);
        }
        int[] histogram = new int[n];
        int max = 0;
        for (int degree : degrees.values()) {
            histogram[degree]++;
            max = Math.max(max, degree);
        }

        // P(k) = 2m(m+1) / (k(k+1)(k+2)) for k >= m
        for (int k = m; k <= m + 3; k++) {
            double expected = 2.0 * m * (m + 1) / ((double) k * (k + 1) * (k + 2));
            Assert.assertEquals("P(k=" + k + ")", expected, histogram[k] / (double) n, 0.01);
        }
        // Largest hub grows as m * sqrt(n), far above what a random graph of the same density gives
        Assert.assertTrue("Max degree " + max, max > 5 * m * Math.log(n));
    }

    @Test
    public void testScaleFreeGraphSeed() {
        ContainerUnloader first = generateScaleFree(200, 2, 5L);
        ContainerUnloader second = generateScaleFree(200, 2, 5L);
        ContainerUnloader other = generateScaleFree(200, 2, 6L);

        Assert.assertEquals(edgeSet(first), edgeSet(second));
        Assert.assertNotEquals(edgeSet(first), edgeSet(other));
    }

    @Test(expected = IllegalStateException.class)
    public void testScaleFreeGraphEdgesPerNodeTooLarge() {
        generateScaleFree(5, 5, null);
    }

    private ContainerUnloader generateScaleFree(int nodes, int edgesPerNode, Long seed) {
        ScaleFreeGraph graph = new ScaleFreeGraph();
        graph.setNumberOfNodes(nodes);
        graph.setEdgesPerNode(edgesPerNode);
        graph.setSeed(seed);
        Container container = new ImportContainerImpl();
        graph.generate(container.getLoader());
        return container.getUnloader();
    }

    private Set<String> edgeSet(ContainerUnloader unloader) {
        // Node ids come from a global counter, so edges are compared by node position
        Map<Object, Integer> positions = new HashMap<>();
        for (NodeDraft node : unloader.getNodes()) {
            positions.put(node.getId(), positions.size());
        }
        Set<String> edges = new HashSet<>();
        for (EdgeDraft edge : unloader.getEdges()) {
            edges.add(positions.get(edge.getSource().getId()) + "-" + positions.get(edge.getTarget().getId()));
        }
        return edges;
    }
}
