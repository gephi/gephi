package org.gephi.transformation.operation.rotation;

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
public class RotationTest extends TestCase {
    private static final float EPSILON = 0.00001f;



    Graph getGraphCentered() {

        GraphModel graphModel = GraphModel.Factory.newInstance();
        UndirectedGraph undirectedGraph = graphModel.getUndirectedGraph();
        Node node1 = graphModel.factory().newNode("0");
        node1.setX(-1);
        node1.setY(0);
        Node node2 = graphModel.factory().newNode("1");
        node2.setX(1);
        node2.setY(0);
        undirectedGraph.addNode(node1);
        undirectedGraph.addNode(node2);
        return undirectedGraph;
    }
    Graph getGraphNotCentered() {

        GraphModel graphModel = GraphModel.Factory.newInstance();
        UndirectedGraph undirectedGraph = graphModel.getUndirectedGraph();
        Node node1 = graphModel.factory().newNode("0");
        node1.setX(2);
        node1.setY(0);
        Node node2 = graphModel.factory().newNode("1");
        node2.setX(0);
        node2.setY(0);
        undirectedGraph.addNode(node1);
        undirectedGraph.addNode(node2);
        return undirectedGraph;
    }
    @Test
    public void testRotateCenterGraph() {
        Graph graph = getGraphCentered();
        Rotation rotation = new Rotation();
        rotation.setAngle(90);

        TransformationController transformationController = new TransformationControllerImpl();
        transformationController.apply(rotation,graph);

        Node node1 = graph.getNode("0");

        assertEquals(0f, node1.x(),EPSILON);
        assertEquals(1.0f, node1.y(),EPSILON);
        Node node2 = graph.getNode("1");
        assertEquals(0f,node2.x(),EPSILON);
        assertEquals(-1.0f,node2.y(),EPSILON);

    }
    @Test
    public void testRotateNotCenterGraph() {
        Graph graph = getGraphNotCentered();
        Rotation rotation = new Rotation();
        rotation.setAngle(90);

        TransformationController transformationController = new TransformationControllerImpl();
        transformationController.apply(rotation,graph);

        Node node1 = graph.getNode("0");

        assertEquals(1.0f, node1.x(),EPSILON);
        assertEquals(-1.0f, node1.y(),EPSILON);
        Node node2 = graph.getNode("1");
        assertEquals(1f,node2.x(),EPSILON);
        assertEquals(1.0f,node2.y(),EPSILON);

    }
}
