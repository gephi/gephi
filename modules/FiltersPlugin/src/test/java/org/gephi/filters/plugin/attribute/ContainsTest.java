package org.gephi.filters.plugin.attribute;

import org.gephi.filters.spi.FilterBuilder;
import org.gephi.graph.GraphGenerator;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;
import org.junit.Assert;
import org.junit.Test;

public class ContainsTest {

    @Test
    public void testStringArray() {
        GraphGenerator graphGenerator = GraphGenerator.build().generateTinyGraph().addStringArrayNodeColumn();
        Graph graph = graphGenerator.getGraph();

        ListAttributeContainsBuilder builder = new ListAttributeContainsBuilder();
        FilterBuilder[] builders = builder.getBuilders(graphGenerator.getWorkspace());
        Assert.assertEquals(1, builders.length);

        ListAttributeContainsBuilder.AttributeContainsFilter<Node> filter =
            (ListAttributeContainsBuilder.AttributeContainsFilter<Node>) builders[0].getFilter(graphGenerator.getWorkspace());
        Assert.assertTrue(filter.getColumn().isArray());
        Assert.assertFalse(filter.evaluate(graphGenerator.getGraph(), graph.getNode(GraphGenerator.FIRST_NODE)));
        filter.setMatch(GraphGenerator.STRING_ARRAY_COLUMN_VALUES[0][0]);
        Assert.assertTrue(filter.evaluate(graphGenerator.getGraph(), graph.getNode(GraphGenerator.FIRST_NODE)));
        filter.setMatch("none");
        Assert.assertFalse(filter.evaluate(graphGenerator.getGraph(), graph.getNode(GraphGenerator.FIRST_NODE)));
    }

    @Test
    public void testFloatArrayStringMatch() {
        GraphGenerator graphGenerator = GraphGenerator.build().generateTinyGraph().addFloatArrayNodeColumn();
        Graph graph = graphGenerator.getGraph();

        ListAttributeContainsBuilder builder = new ListAttributeContainsBuilder();
        FilterBuilder[] builders = builder.getBuilders(graphGenerator.getWorkspace());

        ListAttributeContainsBuilder.AttributeContainsFilter<Node> filter =
            (ListAttributeContainsBuilder.AttributeContainsFilter<Node>) builders[0].getFilter(graphGenerator.getWorkspace());
        filter.setMatch("1.0");
        Assert.assertTrue(filter.evaluate(graphGenerator.getGraph(), graph.getNode(GraphGenerator.FIRST_NODE)));
    }

}
