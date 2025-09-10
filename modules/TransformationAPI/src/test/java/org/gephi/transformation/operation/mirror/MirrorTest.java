package org.gephi.transformation.operation.mirror;

import junit.framework.TestCase;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.UndirectedGraph;
import org.gephi.transformation.TransformationControllerImpl;
import org.gephi.transformation.api.TransformationController;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class MirrorTest extends TestCase {
    Graph getGraphCentered() {

        GraphModel graphModel = GraphModel.Factory.newInstance();
        UndirectedGraph undirectedGraph = graphModel.getUndirectedGraph();
        Node node1 = graphModel.factory().newNode("0");
        node1.setX(-1);
        node1.setY(-1);
        Node node2 = graphModel.factory().newNode("1");
        node2.setX(1);
        node2.setY(1);
        undirectedGraph.addNode(node1);
        undirectedGraph.addNode(node2);
        return undirectedGraph;
    }

    Graph getGraphNotCentered() {

        GraphModel graphModel = GraphModel.Factory.newInstance();
        UndirectedGraph undirectedGraph = graphModel.getUndirectedGraph();
        Node node1 = graphModel.factory().newNode("0");
        node1.setX(2);
        node1.setY(2);
        Node node2 = graphModel.factory().newNode("1");
        node2.setX(0);
        node2.setY(0);
        undirectedGraph.addNode(node1);
        undirectedGraph.addNode(node2);
        return undirectedGraph;
    }

    @Test
    public void testMirrorYCenteredGraph() {
        Graph graph = getGraphCentered();
        MirrorYAxis mirrorYAxis = new MirrorYAxis();

        TransformationController transformationController = new TransformationControllerImpl();
        transformationController.apply(mirrorYAxis, graph);

        Node node1 = graph.getNode("0");
        assertEquals(1.0f, node1.x());
        assertEquals(-1.0f, node1.y());
        Node node2 = graph.getNode("1");
        assertEquals(-1.0f, node2.x());
        assertEquals(1.0f, node2.y());

    }

    @Test
    public void testMirrorXCenteredGraph() {
        Graph graph = getGraphCentered();
        MirrorXAxis mirrorXAxis = new MirrorXAxis();

        TransformationController transformationController = new TransformationControllerImpl();
        transformationController.apply(mirrorXAxis, graph);

        Node node1 = graph.getNode("0");
        assertEquals(-1.0f, node1.x());
        assertEquals(1.0f, node1.y());

        Node node2 = graph.getNode("1");
        assertEquals(1.0f, node2.x());
        assertEquals(-1.0f, node2.y());

    }

    @Test
    public void testMirrorYNotCenteredGraph() {
        Graph graph = getGraphNotCentered();
        MirrorYAxis mirrorYAxis = new MirrorYAxis();

        TransformationController transformationController = new TransformationControllerImpl();
        transformationController.apply(mirrorYAxis, graph);

        Node node1 = graph.getNode("0");
        assertEquals(0.0f, node1.x());
        assertEquals(2.0f, node1.y());
        Node node2 = graph.getNode("1");
        assertEquals(2.0f, node2.x());
        assertEquals(0.0f, node2.y());

    }

    @Test
    public void testMirrorXNotCenteredGraph() {
        Graph graph = getGraphNotCentered();
        MirrorXAxis mirrorXAxis = new MirrorXAxis();

        TransformationController transformationController = new TransformationControllerImpl();
        transformationController.apply(mirrorXAxis, graph);

        Node node1 = graph.getNode("0");
        assertEquals(2.0f, node1.x());
        assertEquals(0.0f, node1.y());

        Node node2 = graph.getNode("1");
        assertEquals(0.0f, node2.x());
        assertEquals(2.0f, node2.y());

    }
}
