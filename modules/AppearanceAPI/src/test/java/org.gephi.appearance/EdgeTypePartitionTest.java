package org.gephi.appearance;

import org.gephi.graph.GraphGenerator;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;
import org.gephi.graph.impl.GraphStoreConfiguration;
import org.junit.Assert;
import org.junit.Test;

public class EdgeTypePartitionTest {

    @Test
    public void testEmpty() {
        Graph graph = GraphGenerator.build().getGraph();
        Column col = graph.getModel().defaultColumns().edgeType();
        EdgeTypePartitionImpl typePartition = new EdgeTypePartitionImpl(col, GraphStoreConfiguration.DEFAULT_EDGE_LABEL_TYPE);

        Assert.assertEquals(0, typePartition.getElementCount(graph));
        Assert.assertEquals(0, typePartition.getValues(graph).size());
        Assert.assertEquals(0, typePartition.getSortedValues(graph).size());
        Assert.assertEquals(0, typePartition.size(graph));
    }

    @Test
    public void testSimpleGraph() {
        Graph graph = GraphGenerator.build().generateTinyGraph().getGraph();
        Column col = graph.getModel().defaultColumns().edgeType();
        EdgeTypePartitionImpl typePartition = new EdgeTypePartitionImpl(col, GraphStoreConfiguration.DEFAULT_EDGE_LABEL_TYPE);

        Assert.assertEquals(1, typePartition.getElementCount(graph));
        Assert.assertEquals(1, typePartition.getValues(graph).size());
        Assert.assertEquals(1, typePartition.getSortedValues(graph).size());
        Assert.assertEquals(1, typePartition.size(graph));
    }

    @Test
    public void testMultiGraph() {
        Graph graph = GraphGenerator.build().generateTinyMultiGraph().getGraph();
        Column col = graph.getModel().defaultColumns().edgeType();
        EdgeTypePartitionImpl typePartition = new EdgeTypePartitionImpl(col, GraphStoreConfiguration.DEFAULT_EDGE_LABEL_TYPE);

        Assert.assertEquals(2, typePartition.getElementCount(graph));
        Assert.assertEquals(2, typePartition.getValues(graph).size());
        Assert.assertEquals(2, typePartition.getSortedValues(graph).size());
        Assert.assertEquals(2, typePartition.size(graph));
    }

    @Test
    public void testIsValid() {
        Graph graph = GraphGenerator.build().generateTinyMultiGraph().getGraph();
        Column col = graph.getModel().defaultColumns().edgeType();
        EdgeTypePartitionImpl typePartition = new EdgeTypePartitionImpl(col, GraphStoreConfiguration.DEFAULT_EDGE_LABEL_TYPE);
        Assert.assertTrue(typePartition.isValid(graph));
    }

    @Test
    public void testIsNotValid() {
        Graph graph = GraphGenerator.build().generateTinyGraph().getGraph();
        Column col = graph.getModel().defaultColumns().edgeType();
        EdgeTypePartitionImpl typePartition = new EdgeTypePartitionImpl(col, GraphStoreConfiguration.DEFAULT_EDGE_LABEL_TYPE);
        Assert.assertFalse(typePartition.isValid(graph));
    }

    @Test
    public void testVersion() {
        Graph graph = GraphGenerator.build().generateTinyMultiGraph().getGraph();
        Column col = graph.getModel().defaultColumns().edgeType();
        EdgeTypePartitionImpl p = new EdgeTypePartitionImpl(col, GraphStoreConfiguration.DEFAULT_EDGE_LABEL_TYPE);
        Edge e2 = graph.getEdge(GraphGenerator.SECOND_EDGE);

        int version = p.getVersion(graph);
        graph.removeEdge(e2);
        Assert.assertNotEquals(version, version = p.getVersion(graph));
        Assert.assertEquals(version, p.getVersion(graph));
    }
}
