package org.gephi.viz.engine.structure;

import org.gephi.viz.engine.VizEngine;
import org.gephi.viz.engine.spi.RenderingTarget;

public class GraphIndexTest {

//    @Test
//    public void testMinMaxEdgeWeight() {
//        GraphGenerator generator = GraphGenerator.build().generateTinyGraph();
//        GraphModel graph = generator.getGraphModel();
//        Edge edge = graph.getGraph().getEdge(GraphGenerator.FIRST_EDGE);
//        edge.setWeight(2.0);
//
//        VizEngine engine = new VizEngine(graph, new MockRenderingTarget());
//        final GraphIndexImpl index = new GraphIndexImpl(engine);
//        index.indexEdges();
//        Assert.assertEquals(2.0, index.getEdgesMinWeight(), 0.01);
//        Assert.assertEquals(2.0, index.getEdgesMaxWeight(), 0.01);
//    }
//
//    @Test
//    public void testMinMaxDynamicEdgeWeight() {
//        Configuration configuration = Configuration.builder().timeRepresentation(TimeRepresentation.TIMESTAMP)
//            .edgeWeightType(TimestampDoubleMap.class).build();
//        GraphGenerator generator = GraphGenerator.build(configuration).generateTinyDynamicTimestampGraph();
//        GraphModel graph = generator.getGraphModel();
//        Edge edge = graph.getGraph().getEdge(GraphGenerator.FIRST_EDGE);
//        edge.setWeight(2.0, 1.0);
//
//        VizEngine engine = new VizEngine(graph, new MockRenderingTarget());
//        final GraphIndexImpl index = new GraphIndexImpl(engine);
//        index.indexEdges();
//        Assert.assertEquals(2.0, index.getEdgesMinWeight(), 0.01);
//        Assert.assertEquals(2.0, index.getEdgesMaxWeight(), 0.01);
//    }
//
//    @Test
//    public void testMinMaxEdgeWeightInView() {
//        Configuration configuration = Configuration.builder().timeRepresentation(TimeRepresentation.TIMESTAMP)
//            .edgeWeightType(TimestampDoubleMap.class).build();
//        GraphGenerator generator = GraphGenerator.build(configuration).generateTinyDynamicTimestampGraph();
//        GraphModel graphModel = generator.getGraphModel();
//        GraphView view = graphModel.createView();
//        Subgraph graph = graphModel.getGraph(view);
//        graph.fill();
//        graphModel.setVisibleView(view);
//
//        Edge edge = graph.getEdge(GraphGenerator.FIRST_EDGE);
//        edge.setWeight(2.0, 1.0);
//
//        VizEngine engine = new VizEngine(graphModel, new MockRenderingTarget());
//        final GraphIndexImpl index = new GraphIndexImpl(engine);
//        index.indexEdges();
//        Assert.assertEquals(2.0, index.getEdgesMinWeight(), 0.01);
//        Assert.assertEquals(2.0, index.getEdgesMaxWeight(), 0.01);
//    }

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

        @Override
        public int getFps() {
            return 0;
        }
    }
}
