package org.gephi.preview.plugin;

import java.util.Arrays;
import org.gephi.graph.GraphGenerator;
import org.gephi.graph.api.Graph;
import org.gephi.preview.api.Item;
import org.gephi.preview.plugin.builders.EdgeLabelBuilder;
import org.gephi.preview.plugin.builders.NodeLabelBuilder;
import org.gephi.preview.plugin.items.EdgeLabelItem;
import org.gephi.preview.plugin.items.NodeLabelItem;
import org.junit.Assert;
import org.junit.Test;

public class LabelBuilderTest {

    @Test
    public void testDefaultNullNodes() {
        Graph graph = GraphGenerator.build().generateTinyGraph().getGraph();

        Item[] items = new NodeLabelBuilder().getItems(graph);
        Assert.assertEquals(0, items.length);
    }

    @Test
    public void testDefaultNullEdges() {
        Graph graph = GraphGenerator.build().withWorkspace().generateTinyGraph().getGraph();

        Item[] items = new EdgeLabelBuilder().getItems(graph);
        Assert.assertEquals(0, items.length);
    }

    @Test
    public void testEmptyNodeLabel() {
        Graph graph = GraphGenerator.build().withWorkspace().generateTinyGraph().getGraph();
        graph.getNode(GraphGenerator.FIRST_NODE).setLabel("");

        Item[] items = new NodeLabelBuilder().getItems(graph);
        Assert.assertEquals(0, items.length);
    }

    @Test
    public void testNodeLabels() {
        Graph graph = GraphGenerator.build().withWorkspace().generateTinyGraph().addNodeLabels().getGraph();

        Item[] items = new NodeLabelBuilder().getItems(graph);
        Assert.assertEquals(graph.getNodeCount(), items.length);
        Arrays.stream(items).forEach(i -> Assert.assertTrue(i instanceof NodeLabelItem));
        Assert.assertEquals(graph.getNode(GraphGenerator.FIRST_NODE).getLabel(), items[0].getData(NodeLabelItem.LABEL));
    }

    @Test
    public void testEdgeLabels() {
        Graph graph = GraphGenerator.build().withWorkspace().generateTinyGraph().addEdgeLabels().getGraph();

        Item[] items = new EdgeLabelBuilder().getItems(graph);
        Assert.assertEquals(graph.getEdgeCount(), items.length);
        Arrays.stream(items).forEach(i -> Assert.assertTrue(i instanceof EdgeLabelItem));
        Assert.assertEquals(graph.getEdge(GraphGenerator.FIRST_EDGE).getLabel(), items[0].getData(EdgeLabelItem.LABEL));
    }
}
