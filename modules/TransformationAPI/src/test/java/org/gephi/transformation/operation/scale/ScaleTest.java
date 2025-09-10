package org.gephi.transformation.operation.scale;

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
public class ScaleTest extends TestCase {
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
    public void scaleExtendCenteredTest() {
        Graph graph = getGraphCentered();
        Scale scale = new Scale(2);
        TransformationController transformationController = new TransformationControllerImpl();
        transformationController.apply(scale,graph);

        Node node1 = graph.getNode("0");
        assertEquals(node1.x(),-2.0f);

        Node node2 = graph.getNode("1");
        assertEquals(node2.x(),2.0f);

    }

    @Test
    public void scaleExtendNotCenteredTest() {
        // Scaling is based on graph center
        Graph graph = getGraphNotCentered();
        Scale scale = new Scale(2);
        TransformationController transformationController = new TransformationControllerImpl();
        transformationController.apply(scale,graph);

        Node node1 = graph.getNode("0");
        assertEquals(node1.x(),3.0f);

        Node node2 = graph.getNode("1");
        assertEquals(node2.x(),-1.0f);

    }
    @Test
    public void scaleReduceCenteredTest() {
        Graph graph = getGraphCentered();
        Scale scale = new Scale(0.5f);
        TransformationController transformationController = new TransformationControllerImpl();
        transformationController.apply(scale,graph);

        Node node1 = graph.getNode("0");
        assertEquals(node1.x(),-0.5f);

        Node node2 = graph.getNode("1");
        assertEquals(node2.x(),0.5f);

    }

    @Test
    public void scaleReduceNotCenteredTest() {
        // Scaling is based on graph center
        Graph graph = getGraphNotCentered();
        Scale scale = new Scale(0.5f);
        TransformationController transformationController = new TransformationControllerImpl();
        transformationController.apply(scale,graph);

        Node node1 = graph.getNode("0");
        assertEquals(node1.x(),1.5f);

        Node node2 = graph.getNode("1");
        assertEquals(node2.x(),.5f);

    }
}
