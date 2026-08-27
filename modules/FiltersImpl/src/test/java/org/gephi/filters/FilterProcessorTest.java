package org.gephi.filters;

import org.gephi.filters.api.Range;
import org.gephi.filters.plugin.attribute.AttributeRangeBuilder.AttributeRangeFilter;
import org.gephi.graph.GraphGenerator;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphModel;
import org.junit.Test;

public class FilterProcessorTest {

    @Test
    public void testInitRangeFilterWithIncompatiblePreviousRangeType() {
        GraphModel graphModel = GraphGenerator.build().generateTinyGraph().addIntNodeColumn().getGraph().getModel();
        Column column = graphModel.getNodeTable().getColumn(GraphGenerator.INT_COLUMN);
        Graph graph = graphModel.getGraph();
        AttributeRangeFilter.Node filter = new AttributeRangeFilter.Node(column);

        //Simulate a range restored from a project file whose bound type (Long) no longer
        //matches the live column type (Integer), e.g. a file saved by an older Gephi version
        filter.getRangeProperty().setValue(new Range(0L, 100L));

        new FilterProcessor().init(filter, graph);
    }
}
