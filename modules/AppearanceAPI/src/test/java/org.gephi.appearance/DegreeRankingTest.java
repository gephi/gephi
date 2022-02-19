package org.gephi.appearance;

import org.gephi.appearance.api.Interpolator;
import org.gephi.graph.GraphGenerator;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;
import org.junit.Assert;
import org.junit.Test;

public class DegreeRankingTest {

    @Test
    public void testEmpty() {
        Graph graph = GraphGenerator.build().getGraph();
        Column col = graph.getModel().defaultColumns().degree();
        DegreeRankingImpl degreeRanking = new DegreeRankingImpl(col);

        Assert.assertNull(degreeRanking.getMinValue(graph));
        Assert.assertNull(degreeRanking.getMaxValue(graph));
    }

    @Test
    public void testInterpolator() {
        Graph graph = GraphGenerator.build().getGraph();
        Column col = graph.getModel().defaultColumns().degree();
        DegreeRankingImpl degreeRanking = new DegreeRankingImpl(col);

        Assert.assertSame(Interpolator.LINEAR, degreeRanking.getInterpolator());
        degreeRanking.setInterpolator(Interpolator.LOG2);
        Assert.assertSame(Interpolator.LOG2, degreeRanking.getInterpolator());
    }

    @Test
    public void testOneEdge() {
        Graph graph = GraphGenerator.build().generateTinyGraph().getGraph();
        Column col = graph.getModel().defaultColumns().degree();
        DegreeRankingImpl degreeRanking = new DegreeRankingImpl(col);

        Assert.assertEquals(1, degreeRanking.getMinValue(graph));
        Assert.assertEquals(1, degreeRanking.getMaxValue(graph));

        Node node = graph.getNode(GraphGenerator.FIRST_NODE);
        Assert.assertEquals(1, degreeRanking.getValue(node, graph));
        Assert.assertEquals(1f, degreeRanking.getNormalizedValue(node, graph), 0);
    }

    @Test
    public void testNormalization() {
        Graph graph = GraphGenerator.build().generateSmallRandomGraph().getGraph();
        Column col = graph.getModel().defaultColumns().degree();
        DegreeRankingImpl degreeRanking = new DegreeRankingImpl(col);

        int minDegree = degreeRanking.getMinValue(graph).intValue();
        int maxDegree = degreeRanking.getMaxValue(graph).intValue();

        for(Node node : graph.getNodes()) {
            int degree = degreeRanking.getValue(node, graph).intValue();
            float normalizedDegree = degreeRanking.getNormalizedValue(node, graph);
            if(degree == minDegree) {
                Assert.assertEquals(0f, normalizedDegree, 0);
            } else if(degree == maxDegree) {
                Assert.assertEquals(1f, normalizedDegree, 0);
            } else {
                Assert.assertNotEquals(minDegree, normalizedDegree);
                Assert.assertNotEquals(maxDegree, normalizedDegree);
            }
        }
    }

}
