package org.gephi.viz.engine.structure;

import org.gephi.graph.GraphGenerator;
import org.gephi.graph.api.Configuration;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.GraphView;
import org.gephi.graph.api.Subgraph;
import org.gephi.graph.api.TimeRepresentation;
import org.gephi.graph.api.types.TimestampDoubleMap;
import org.gephi.viz.engine.VizEngine;
import org.gephi.viz.engine.spi.RenderingTarget;
import org.junit.Assert;
import org.junit.Test;

public class GraphIndexTest {

    @Test
    public void testMinMaxEdgeWeight() {
        GraphGenerator generator = GraphGenerator.build().generateTinyGraph();
        GraphModel graph = generator.getGraphModel();
        Edge edge = graph.getGraph().getEdge(GraphGenerator.FIRST_EDGE);
        edge.setWeight(2.0);

        VizEngine engine = new VizEngine(graph, new MockRenderingTarget());
        final GraphIndexImpl index = new GraphIndexImpl(engine);
        index.indexEdges();
        Assert.assertEquals(2.0, index.getEdgesMinWeight(), 0.01);
        Assert.assertEquals(2.0, index.getEdgesMaxWeight(), 0.01);
    }

    @Test
    public void testMinMaxDynamicEdgeWeight() {
        Configuration configuration = Configuration.builder().timeRepresentation(TimeRepresentation.TIMESTAMP).edgeWeightType(TimestampDoubleMap.class).build();
        GraphGenerator generator = GraphGenerator.build(configuration).generateTinyDynamicTimestampGraph();
        GraphModel graph = generator.getGraphModel();
        Edge edge = graph.getGraph().getEdge(GraphGenerator.FIRST_EDGE);
        edge.setWeight(2.0, 1.0);

        VizEngine engine = new VizEngine(graph, new MockRenderingTarget());
        final GraphIndexImpl index = new GraphIndexImpl(engine);
        index.indexEdges();
        Assert.assertEquals(2.0, index.getEdgesMinWeight(), 0.01);
        Assert.assertEquals(2.0, index.getEdgesMaxWeight(), 0.01);
    }

    @Test
    public void testMinMaxEdgeWeightInView() {
        Configuration configuration = Configuration.builder().timeRepresentation(TimeRepresentation.TIMESTAMP).edgeWeightType(TimestampDoubleMap.class).build();
        GraphGenerator generator = GraphGenerator.build(configuration).generateTinyDynamicTimestampGraph();
        GraphModel graphModel = generator.getGraphModel();
        GraphView view = graphModel.createView();
        Subgraph graph = graphModel.getGraph(view);
        graph.fill();
        graphModel.setVisibleView(view);

        Edge edge = graph.getEdge(GraphGenerator.FIRST_EDGE);
        edge.setWeight(2.0, 1.0);

        VizEngine engine = new VizEngine(graphModel, new MockRenderingTarget());
        final GraphIndexImpl index = new GraphIndexImpl(engine);
        index.indexEdges();
        Assert.assertEquals(2.0, index.getEdgesMinWeight(), 0.01);
        Assert.assertEquals(2.0, index.getEdgesMaxWeight(), 0.01);
    }

    private static class MockRenderingTarget implements RenderingTarget {

        @Override
        public void setup(VizEngine engine) {

        }

        @Override
        public void start() {

        }

        @Override
        public void stop() {

        }
    }
}
