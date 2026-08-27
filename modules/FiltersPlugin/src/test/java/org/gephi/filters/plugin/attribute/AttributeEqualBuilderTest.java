package org.gephi.filters.plugin.attribute;

import org.gephi.filters.spi.FilterBuilder;
import org.gephi.graph.GraphGenerator;
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
}
