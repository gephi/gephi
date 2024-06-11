package org.gephi.appearance;

import org.gephi.graph.GraphGenerator;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Configuration;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.TimeRepresentation;
import org.junit.Assert;
import org.junit.Test;

public class TimesetRankingTest {

    private final Configuration intervalConfiguration = Configuration.builder().timeRepresentation(
        TimeRepresentation.INTERVAL).build();
    private final Configuration timestampConfiguration = Configuration.builder().timeRepresentation(
        TimeRepresentation.TIMESTAMP).build();

    @Test
    public void testEmpty() {
        Graph graph = GraphGenerator.build(timestampConfiguration).getGraph();
        Column column = graph.getModel().defaultColumns().nodeTimeSet();
        TimesetRankingImpl timesetRanking = new TimesetRankingImpl(column);

        Assert.assertEquals(Double.NEGATIVE_INFINITY, timesetRanking.getMinValue(graph));
        Assert.assertEquals(Double.POSITIVE_INFINITY, timesetRanking.getMaxValue(graph));
    }

    @Test
    public void testMinMax() {
        Graph graph = GraphGenerator.build(timestampConfiguration).generateTinyGraph().setTimestampSet().getGraph();
        Column column = graph.getModel().defaultColumns().nodeTimeSet();
        TimesetRankingImpl timesetRanking = new TimesetRankingImpl(column);

        Assert.assertEquals(GraphGenerator.TIMESTAMP_SET_VALUES[0], timesetRanking.getMinValue(graph));
        Assert.assertEquals(GraphGenerator.TIMESTAMP_SET_VALUES[1], timesetRanking.getMaxValue(graph));
    }

    @Test
    public void testGetValue() {
        Graph graph = GraphGenerator.build(timestampConfiguration).generateTinyGraph().setTimestampSet().getGraph();
        Column column = graph.getModel().defaultColumns().nodeTimeSet();
        TimesetRankingImpl timesetRanking = new TimesetRankingImpl(column);

        Assert.assertEquals(GraphGenerator.TIMESTAMP_SET_VALUES[0],
            timesetRanking.getValue(graph.getNode(GraphGenerator.FIRST_NODE), graph));
    }

    @Test
    public void testMinMaxInterval() {
        Graph graph = GraphGenerator.build(intervalConfiguration).generateTinyGraph().setIntervalSet().getGraph();
        Column column = graph.getModel().defaultColumns().nodeTimeSet();
        TimesetRankingImpl timesetRanking = new TimesetRankingImpl(column);

        Assert.assertEquals(GraphGenerator.INTERVAL_SET_VALUES[0][0], timesetRanking.getMinValue(graph));
        Assert.assertEquals(GraphGenerator.INTERVAL_SET_VALUES[1][1], timesetRanking.getMaxValue(graph));
    }

    @Test
    public void testMinMaxOtherColumn() {
        Graph graph = GraphGenerator.build(timestampConfiguration).generateTinyGraph().addTimestampSetColumn().getGraph();
        Column column = graph.getModel().getNodeTable().getColumn(GraphGenerator.TIMESTAMP_SET_COLUMN);
        TimesetRankingImpl timesetRanking = new TimesetRankingImpl(column);

        Assert.assertEquals(GraphGenerator.TIMESTAMP_SET_VALUES[0], timesetRanking.getMinValue(graph));
        Assert.assertEquals(GraphGenerator.TIMESTAMP_SET_VALUES[1], timesetRanking.getMaxValue(graph));
    }
}
