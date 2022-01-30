package org.gephi.appearance;

import org.gephi.appearance.api.Interpolator;
import org.gephi.graph.GraphGenerator;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;
import org.junit.Assert;
import org.junit.Test;

public class DegreeRankingTest {

    @Test
    public void testEmpty() {
        Graph graph = GraphGenerator.build().getGraph();
        DegreeRankingImpl degreeRanking = new DegreeRankingImpl();

        Assert.assertEquals(0, degreeRanking.getMinValue(graph));
        Assert.assertEquals(0, degreeRanking.getMaxValue(graph));
    }

    @Test
    public void testInterpolator() {
        DegreeRankingImpl degreeRanking = new DegreeRankingImpl();
        Assert.assertSame(Interpolator.LINEAR, degreeRanking.getInterpolator());
        degreeRanking.setInterpolator(Interpolator.LOG2);
        Assert.assertSame(Interpolator.LOG2, degreeRanking.getInterpolator());
    }

    @Test
    public void testOneEdge() {
        Graph graph = GraphGenerator.build().generateTinyGraph().getGraph();
        DegreeRankingImpl degreeRanking = new DegreeRankingImpl();

        Assert.assertEquals(1, degreeRanking.getMinValue(graph));
        Assert.assertEquals(1, degreeRanking.getMaxValue(graph));

        Node node = graph.getNode(GraphGenerator.FIRST_NODE);
        Assert.assertEquals(1, degreeRanking.getValue(node, graph));
        Assert.assertEquals(1f, degreeRanking.getNormalizedValue(node, graph), 0);
    }

    @Test
    public void testNormalization() {
        Graph graph = GraphGenerator.build().generateSmallRandomGraph().getGraph();
        DegreeRankingImpl degreeRanking = new DegreeRankingImpl();

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
