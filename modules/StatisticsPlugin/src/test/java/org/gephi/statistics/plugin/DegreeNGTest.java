/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.statistics.plugin;

import org.gephi.graph.api.DirectedGraph;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.project.api.ProjectController;
import org.gephi.project.impl.ProjectControllerImpl;
import org.openide.util.Lookup;
import static org.testng.Assert.*;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author Anna
 */
public class DegreeNGTest {

    private ProjectController pc;

    @BeforeClass
    public void setUp() {
        pc = Lookup.getDefault().lookup(ProjectControllerImpl.class);
    }

    @BeforeMethod
    public void initialize() {
        pc.newProject();
    }

    @AfterMethod
    public void clean() {
        pc.closeCurrentProject();
    }

    @Test
    public void testOneNodeDegree() {
        GraphModel graphModel = GraphGenerator.generateNullUndirectedGraph(1);
        Graph graph = graphModel.getGraph();
        Node n = graph.getNode("0");

        Degree d = new Degree();
        int degree = d.calculateDegree(graph, n);
        assertEquals(degree, 0);
    }

    @Test
    public void testNullGraphDegree() {
        GraphModel graphModel = GraphGenerator.generateNullUndirectedGraph(5);
        Graph graph = graphModel.getGraph();
        Node n = graph.getNode("1");
        Degree d = new Degree();
        int degree = d.calculateDegree(graph, n);
        double avDegree = d.calculateAverageDegree(graph, false, false);
        assertEquals(degree, 0);
        assertEquals(avDegree, 0.0);
    }

    @Test
    public void testCompleteGraphDegree() {
        GraphModel graphModel = GraphGenerator.generateCompleteUndirectedGraph(5);
        Graph graph = graphModel.getGraph();
        Node n = graph.getNode("2");
        Degree d = new Degree();
        int degree = d.calculateDegree(graph, n);
        assertEquals(degree, 4);
    }

    @Test
    public void testStarGraphDegree() {
        GraphModel graphModel = GraphGenerator.generateStarUndirectedGraph(5);
        Graph graph = graphModel.getGraph();
        Node n1 = graph.getNode("0");
        Node n2 = graph.getNode("1");
        Degree d = new Degree();
        int degree1 = d.calculateDegree(graph, n1);
        int degree2 = d.calculateDegree(graph, n2);
        double avDegree = d.calculateAverageDegree(graph, false, false);
        double expectedAvDegree = 1.6667;
        double diff = Math.abs(avDegree - expectedAvDegree);
        assertEquals(degree1, 5);
        assertEquals(degree2, 1);
        assertTrue(diff < 0.001);
    }

    @Test
    public void testCyclicGraphDegree() {
        GraphModel graphModel = GraphGenerator.generateCyclicUndirectedGraph(5);
        Graph graph = graphModel.getGraph();
        Node n = graph.getNode("3");
        Degree d = new Degree();
        int degree = d.calculateDegree(graph, n);
        double avDegree = d.calculateAverageDegree(graph, false, false);
        assertEquals(degree, 2);
        assertEquals(avDegree, 2.0);
    }

    @Test
    public void testDirectedPathGraphDegree() {
        GraphModel graphModel = GraphGenerator.generatePathDirectedGraph(2);
        DirectedGraph graph = graphModel.getDirectedGraph();
        Node n1 = graph.getNode("0");
        Node n2 = graph.getNode("1");
        Degree d = new Degree();
        int inDegree1 = d.calculateInDegree(graph, n1);
        int inDegree2 = d.calculateInDegree(graph, n2);
        int outDegree1 = d.calculateOutDegree(graph, n1);
        double avDegree = d.calculateAverageDegree(graph, true, false);
        assertEquals(inDegree1, 0);
        assertEquals(inDegree2, 1);
        assertEquals(outDegree1, 1);
        assertEquals(avDegree, 1.0);
    }

    @Test
    public void testDirectedCyclicGraphDegree() {
        GraphModel graphModel = GraphGenerator.generateCyclicDirectedGraph(5);
        DirectedGraph graph = graphModel.getDirectedGraph();
        Node n1 = graph.getNode("0");
        Node n3 = graph.getNode("2");
        Node n5 = graph.getNode("4");
        Degree d = new Degree();
        int inDegree3 = d.calculateInDegree(graph, n3);
        int degree1 = d.calculateDegree(graph, n1);
        int outDegree5 = d.calculateOutDegree(graph, n5);
        double avDegree = d.calculateAverageDegree(graph, true, false);
        assertEquals(inDegree3, 1);
        assertEquals(degree1, 2);
        assertEquals(outDegree5, 1);
        assertEquals(avDegree, 2.0);
    }

    @Test
    public void testDirectedStarOutGraphDegree() {
        GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getGraphModel();

        DirectedGraph directedGraph = graphModel.getDirectedGraph();
        Node firstNode = graphModel.factory().newNode("0");
        directedGraph.addNode(firstNode);
        for (int i = 1; i <= 5; i++) {
            Node currentNode = graphModel.factory().newNode(((Integer) i).toString());
            directedGraph.addNode(currentNode);
            Edge currentEdge = graphModel.factory().newEdge(firstNode, currentNode);
            directedGraph.addEdge(currentEdge);
        }

        DirectedGraph graph = graphModel.getDirectedGraph();
        Node n1 = graph.getNode("0");
        Node n3 = graph.getNode("2");
        Degree d = new Degree();
        int inDegree1 = d.calculateInDegree(graph, n1);
        int outDegree1 = d.calculateOutDegree(graph, n1);
        int degree3 = d.calculateDegree(graph, n3);

        assertEquals(inDegree1, 0);
        assertEquals(outDegree1, 5);
        assertEquals(degree3, 1);
    }
}
