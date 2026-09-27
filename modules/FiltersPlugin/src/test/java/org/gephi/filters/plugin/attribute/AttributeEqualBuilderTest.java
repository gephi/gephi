package org.gephi.filters.plugin.attribute;

import org.gephi.filters.spi.FilterBuilder;
import org.gephi.graph.GraphGenerator;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;
import org.junit.Assert;
import org.junit.Test;

public class AttributeEqualBuilderTest {

    @Test
    public void testEqualStringFilterDefaultPatternIsNotNull() {
        GraphGenerator graphGenerator = GraphGenerator.build().generateTinyGraph().addStringNodeColumn();

        AttributeEqualBuilder builder = new AttributeEqualBuilder();
        FilterBuilder[] builders = builder.getBuilders(graphGenerator.getWorkspace());
        Assert.assertEquals(1, builders.length);

        AttributeEqualBuilder.EqualStringFilter filter =
            (AttributeEqualBuilder.EqualStringFilter) builders[0].getFilter(graphGenerator.getWorkspace());
        Assert.assertNotNull(filter.getPattern());
        Assert.assertEquals("", filter.getPattern());
    }

    @Test
    public void testEqualStringFilterUseRegexWithoutSetPatternDoesNotThrow() {
        GraphGenerator graphGenerator = GraphGenerator.build().generateTinyGraph().addStringNodeColumn();
        Graph graph = graphGenerator.getGraph();

        AttributeEqualBuilder builder = new AttributeEqualBuilder();
        FilterBuilder[] builders = builder.getBuilders(graphGenerator.getWorkspace());

        AttributeEqualBuilder.EqualStringFilter<Node> filter =
            (AttributeEqualBuilder.EqualStringFilter<Node>) builders[0].getFilter(graphGenerator.getWorkspace());
        filter.setUseRegex(true);
        Node firstNode = graph.getNode(GraphGenerator.FIRST_NODE);
        Assert.assertFalse(filter.evaluate(graph, firstNode));
    }

    @Test
    public void testEqualStringFilterSetEmptyPatternThenUseRegexDoesNotThrow() {
        GraphGenerator graphGenerator = GraphGenerator.build().generateTinyGraph().addStringNodeColumn();
        Graph graph = graphGenerator.getGraph();

        AttributeEqualBuilder builder = new AttributeEqualBuilder();
        FilterBuilder[] builders = builder.getBuilders(graphGenerator.getWorkspace());

        AttributeEqualBuilder.EqualStringFilter<Node> filter =
            (AttributeEqualBuilder.EqualStringFilter<Node>) builders[0].getFilter(graphGenerator.getWorkspace());
        filter.setPattern("");
        filter.setUseRegex(true);
        Node firstNode = graph.getNode(GraphGenerator.FIRST_NODE);
        Assert.assertFalse(filter.evaluate(graph, firstNode));
    }
}
