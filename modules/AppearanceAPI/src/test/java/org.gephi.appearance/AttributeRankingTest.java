package org.gephi.appearance;

import org.gephi.graph.GraphGenerator;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Estimator;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;
import org.junit.Assert;
import org.junit.Test;

public class AttributeRankingTest {

    @Test
    public void testEmpty() {
        Graph graph = GraphGenerator.build().addIntNodeColumn().getGraph();
        Column column = graph.getModel().getNodeTable().getColumn(GraphGenerator.INT_COLUMN);
        AttributeRankingImpl attributeRanking = new AttributeRankingImpl(column);

        Assert.assertNull(attributeRanking.getMinValue(graph));
        Assert.assertNull(attributeRanking.getMaxValue(graph));
    }

    @Test
    public void testTwoNodes() {
        Graph graph = GraphGenerator.build().generateTinyGraph().addIntNodeColumn().getGraph();
        Column column = graph.getModel().getNodeTable().getColumn(GraphGenerator.INT_COLUMN);
        AttributeRankingImpl attributeRanking = new AttributeRankingImpl(column);

        Assert.assertEquals(GraphGenerator.INT_COLUMN_MIN_VALUE, attributeRanking.getMinValue(graph));
        Assert.assertEquals(GraphGenerator.INT_COLUMN_MIN_VALUE + 1, attributeRanking.getMaxValue(graph));

        Node n1 = graph.getNode(GraphGenerator.FIRST_NODE);
        Node n2 = graph.getNode(GraphGenerator.SECOND_NODE);

        Assert.assertEquals(GraphGenerator.INT_COLUMN_MIN_VALUE, attributeRanking.getValue(n1, graph));
        Assert.assertEquals(0f, attributeRanking.getNormalizedValue(n1, graph), 0);
        Assert.assertEquals(1f, attributeRanking.getNormalizedValue(n2, graph), 0);
    }

    @Test
    public void testIsValidStringColumn() {
        Graph graph = GraphGenerator.build().generateTinyGraph().addStringNodeColumn().getGraph();
        Column column = graph.getModel().getNodeTable().getColumn(GraphGenerator.STRING_COLUMN);

        AttributeRankingImpl p = new AttributeRankingImpl(column);
        Assert.assertFalse(p.isValid(graph));
    }

    @Test
    public void testIsValidIntColumn() {
        Graph graph = GraphGenerator.build().generateTinyGraph().addIntNodeColumn().getGraph();
        Column column = graph.getModel().getNodeTable().getColumn(GraphGenerator.INT_COLUMN);

        AttributeRankingImpl p = new AttributeRankingImpl(column);
        Assert.assertTrue(p.isValid(graph));
    }

    @Test
    public void testArrayColumnNotValid() {
        Graph graph = GraphGenerator.build().generateTinyGraph().addFloatArrayNodeColumn().getGraph();
        Column column = graph.getModel().getNodeTable().getColumn(GraphGenerator.FLOAT_ARRAY_COLUMN);

        AttributeRankingImpl p = new AttributeRankingImpl(column);
        Assert.assertFalse(p.isValid(graph));
    }

    @Test
    public void testDynamicTimestampColumn() {
        Graph graph = GraphGenerator.build().generateTinyGraph().addTimestampDoubleColumn().getGraph();
        Column column = graph.getModel().getNodeTable().getColumn(GraphGenerator.TIMESTAMP_DOUBLE_COLUMN);

        AttributeRankingImpl p = new AttributeRankingImpl(column);
        Assert.assertTrue(p.isValid(graph));


        Assert.assertEquals(GraphGenerator.TIMESTAMP_DOUBLE_COLUMN_VALUES[0][0], p.getMinValue(graph));
        Assert.assertEquals(GraphGenerator.TIMESTAMP_DOUBLE_COLUMN_VALUES[1][0], p.getMaxValue(graph));

        Node n1 = graph.getNode(GraphGenerator.FIRST_NODE);

        Assert.assertEquals(GraphGenerator.TIMESTAMP_DOUBLE_COLUMN_VALUES[0][0], p.getValue(n1, graph));
    }
}
