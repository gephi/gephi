package org.gephi.io.processor.plugin;

import java.util.Collections;
import org.gephi.graph.GraphGenerator;
import org.gephi.graph.api.Configuration;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.TimeRepresentation;
import org.gephi.graph.api.types.TimestampDoubleMap;
import org.gephi.io.importer.impl.EdgeDraftImpl;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class DynamicEdgeWeightTest {

    private Utils.TestProcessor processor;
    private EdgeDraftImpl edgeDraft;
    private Edge edge;

    @Before
    public void setUp() {
        Configuration configuration = Configuration.builder()
                .edgeWeightType(TimestampDoubleMap.class)
                    .timeRepresentation(TimeRepresentation.TIMESTAMP).build();
        processor = new Utils.TestProcessor(
            GraphGenerator.build(configuration).generateTinyGraph().getGraphModel()
        );
        edgeDraft = new EdgeDraftImpl(processor.getContainer(), GraphGenerator.FIRST_EDGE);
        processor.getContainer().setTimeRepresentation(TimeRepresentation.TIMESTAMP);
        edge = processor.graphModel.getGraph().getEdge(GraphGenerator.FIRST_EDGE);

    }

    @Test
    public void testNewEdge() {
        processor.getContainer().addEdgeColumn("weight", Double.class, true);

        edgeDraft.setValue("weight", new TimestampDoubleMap(new double[] {2.0}, new double[] {4.0}));

        processor.flushEdgeWeight(processor.getContainer(), edgeDraft, edge, true);

        Assert.assertEquals(Collections.EMPTY_LIST, processor.getReport().getIssuesList(100));
        Assert.assertEquals(4.0, edge.getWeight(2.0), 0.0);
    }

    @Test
    public void testMergeWeight() {
        edge.setWeight(5.0, 1.0);
        processor.getContainer().addEdgeColumn("weight", Double.class, true);
        edgeDraft.setValue("weight", new TimestampDoubleMap(new double[] {2.0}, new double[] {4.0}));

        processor.flushEdgeWeight(processor.getContainer(), edgeDraft, edge, false);

        Assert.assertEquals(Collections.EMPTY_LIST, processor.getReport().getIssuesList(100));
        Assert.assertEquals(4.0, edge.getWeight(2.0), 0.0);
        Assert.assertEquals(5.0, edge.getWeight(1.0), 0.0);
    }

    @Test
    public void testPreserveEdgeWeight() {
        processor.getContainer().addEdgeColumn("weight", Double.class, true);
        edge.setWeight(5.0, 1.0);

        processor.flushEdgeWeight(processor.getContainer(), edgeDraft, edge, false);

        Assert.assertEquals(Collections.EMPTY_LIST, processor.getReport().getIssuesList(100));
        Assert.assertEquals(5.0, edge.getWeight(1.0), 0.0);
    }
}
