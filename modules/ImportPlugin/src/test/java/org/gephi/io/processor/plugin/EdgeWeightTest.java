package org.gephi.io.processor.plugin;

import org.gephi.graph.GraphGenerator;
import org.gephi.graph.api.Edge;
import org.gephi.io.importer.api.EdgeMergeStrategy;
import org.gephi.io.importer.impl.EdgeDraftImpl;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class EdgeWeightTest {

    private Utils.TestProcessor processor;
    private EdgeDraftImpl edgeDraft;
    private Edge edge;

    @Before
    public void setUp() {
        processor = new Utils.TestProcessor(
            GraphGenerator.build().generateTinyGraph().getGraphModel()
        );
        edgeDraft = new EdgeDraftImpl(processor.getContainer(), GraphGenerator.FIRST_EDGE);
        edge = processor.graphModel.getGraph().getEdge(GraphGenerator.FIRST_EDGE);
    }

    @Test
    public void testSumMergeStrategy() {
        edgeDraft.setWeight(42.0);

        processor.flushEdgeWeight(processor.getContainer(), edgeDraft, edge, false);

        Assert.assertEquals(edgeDraft.getWeight() + 1, edge.getWeight(), 0.0);
    }

    @Test
    public void testFirstMergeStrategy() {
        edgeDraft.setWeight(42.0);
        processor.getContainer().setEdgesMergeStrategy(EdgeMergeStrategy.FIRST);

        processor.flushEdgeWeight(processor.getContainer(), edgeDraft, edge, false);

        Assert.assertEquals(1, edge.getWeight(), 0.0);
    }

    @Test
    public void testLastMergeStrategy() {
        edgeDraft.setWeight(42.0);
        processor.getContainer().setEdgesMergeStrategy(EdgeMergeStrategy.LAST);

        processor.flushEdgeWeight(processor.getContainer(), edgeDraft, edge, false);

        Assert.assertEquals(edgeDraft.getWeight(), edge.getWeight(), 0.0);
    }

    @Test
    public void testNoMergeStrategy() {
        edgeDraft.setWeight(42.0);
        processor.getContainer().setEdgesMergeStrategy(EdgeMergeStrategy.NO_MERGE);

        processor.flushEdgeWeight(processor.getContainer(), edgeDraft, edge, false);

        Assert.assertEquals(1, edge.getWeight(), 0.0);
    }

    @Test
    public void testAvgMergeStrategy() {
        edgeDraft.setWeight(42.0);
        processor.getContainer().setEdgesMergeStrategy(EdgeMergeStrategy.AVG);

        processor.flushEdgeWeight(processor.getContainer(), edgeDraft, edge, false);

        Assert.assertEquals((edgeDraft.getWeight() + 1) / 2, edge.getWeight(), 0.0);
    }

    @Test
    public void testAvgMergeStrategyWithThree() {
        processor.getContainer().setEdgesMergeStrategy(EdgeMergeStrategy.AVG);
        edge.setWeight(4);

        edgeDraft = new EdgeDraftImpl(processor.getContainer(), GraphGenerator.FIRST_EDGE);
        edgeDraft.setWeight(10.0);
        processor.flushEdgeWeight(processor.getContainer(), edgeDraft, edge, false);
        processor.flushEdgeWeight(processor.getContainer(), edgeDraft, edge, false);

        Assert.assertEquals((24.0) / 3, edge.getWeight(), 0.0);
    }

    @Test
    public void testMinMergeStrategy() {
        edgeDraft.setWeight(42.0);
        processor.getContainer().setEdgesMergeStrategy(EdgeMergeStrategy.MIN);

        processor.flushEdgeWeight(processor.getContainer(), edgeDraft, edge, false);

        Assert.assertEquals(1, edge.getWeight(), 0.0);
    }

    @Test
    public void testMaxMergeStrategy() {
        edgeDraft.setWeight(42.0);
        processor.getContainer().setEdgesMergeStrategy(EdgeMergeStrategy.MAX);

        processor.flushEdgeWeight(processor.getContainer(), edgeDraft, edge, false);

        Assert.assertEquals(edgeDraft.getWeight(), edge.getWeight(), 0.0);
    }
}
