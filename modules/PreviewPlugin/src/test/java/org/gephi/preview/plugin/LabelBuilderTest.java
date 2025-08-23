package org.gephi.preview.plugin;

import java.util.Arrays;
import org.gephi.graph.GraphGenerator;
import org.gephi.graph.api.Graph;
import org.gephi.preview.api.Item;
import org.gephi.preview.plugin.builders.EdgeLabelBuilder;
import org.gephi.preview.plugin.builders.NodeLabelBuilder;
import org.gephi.preview.plugin.items.EdgeLabelItem;
import org.gephi.preview.plugin.items.NodeLabelItem;
import org.gephi.project.api.Project;
import org.gephi.project.api.ProjectController;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openide.util.Lookup;

public class LabelBuilderTest {

    private Project project;

    @Before
    public void setUp() {
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        project = pc.newProject();
    }

    @After
    public void cleanUp() {
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        pc.closeCurrentProject();
        project = null;
    }

    @Test
    public void testDefaultNullNodes() {
        Graph graph = GraphGenerator.build(project.getCurrentWorkspace()).generateTinyGraph().getGraph();

        Item[] items = new NodeLabelBuilder().getItems(graph);
        Assert.assertEquals(0, items.length);
    }

    @Test
    public void testDefaultNullEdges() {
        Graph graph = GraphGenerator.build(project.getCurrentWorkspace()).generateTinyGraph().getGraph();

        Item[] items = new EdgeLabelBuilder().getItems(graph);
        Assert.assertEquals(0, items.length);
    }

    @Test
    public void testEmptyNodeLabel() {
        Graph graph = GraphGenerator.build(project.getCurrentWorkspace()).generateTinyGraph().getGraph();
        graph.getNode(GraphGenerator.FIRST_NODE).setLabel("");

        Item[] items = new NodeLabelBuilder().getItems(graph);
        Assert.assertEquals(0, items.length);
    }

    @Test
    public void testNodeLabels() {
        Graph graph = GraphGenerator.build(project.getCurrentWorkspace()).generateTinyGraph().addNodeLabels().getGraph();

        Item[] items = new NodeLabelBuilder().getItems(graph);
        Assert.assertEquals(graph.getNodeCount(), items.length);
        Arrays.stream(items).forEach(i -> Assert.assertTrue(i instanceof NodeLabelItem));
        Assert.assertEquals(graph.getNode(GraphGenerator.FIRST_NODE).getLabel(), items[0].getData(NodeLabelItem.LABEL));
    }

    @Test
    public void testEdgeLabels() {
        Graph graph = GraphGenerator.build(project.getCurrentWorkspace()).generateTinyGraph().addEdgeLabels().getGraph();

        Item[] items = new EdgeLabelBuilder().getItems(graph);
        Assert.assertEquals(graph.getEdgeCount(), items.length);
        Arrays.stream(items).forEach(i -> Assert.assertTrue(i instanceof EdgeLabelItem));
        Assert.assertEquals(graph.getEdge(GraphGenerator.FIRST_EDGE).getLabel(), items[0].getData(EdgeLabelItem.LABEL));
    }
}
