package org.gephi.appearance;

import org.gephi.graph.GraphGenerator;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.junit.Assert;
import org.junit.Test;

public class EdgeWeightRankingTest {

    @Test
    public void testEmpty() {
        Graph graph = GraphGenerator.build().getGraph();
        EdgeWeightRankingImpl weightRanking = new EdgeWeightRankingImpl();

        Assert.assertEquals(0, weightRanking.getMinValue(graph));
        Assert.assertEquals(0, weightRanking.getMaxValue(graph));
    }

    @Test
    public void testOneEdge() {
        Graph graph = GraphGenerator.build().generateTinyGraph().getGraph();
        EdgeWeightRankingImpl weightRanking = new EdgeWeightRankingImpl();

        Assert.assertEquals(1.0, weightRanking.getMinValue(graph).doubleValue(), 0);
        Assert.assertEquals(1.0, weightRanking.getMaxValue(graph).doubleValue(), 0);

        Edge edge = graph.getEdge(GraphGenerator.FIRST_EDGE);
        Assert.assertEquals(1.0, weightRanking.getValue(edge, graph).doubleValue(), 0);
    }
}
