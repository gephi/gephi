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
import org.gephi.graph.api.UndirectedGraph;
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
public class GraphDensityNGTest {

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
    public void testOneNodeDensity() {
        GraphModel graphModel = GraphGenerator.generateNullUndirectedGraph(1);
        Graph graph = graphModel.getGraph();
        GraphDensity d = new GraphDensity();
        double density = d.calculateDensity(graph, false);
        assertEquals(density, Double.NaN);
    }

    @Test
    public void testTwoConnectedNodesDensity() {
        GraphModel graphModel = GraphGenerator.generateCompleteUndirectedGraph(2);
        Graph graph = graphModel.getGraph();
        GraphDensity d = new GraphDensity();
        double density = d.calculateDensity(graph, false);
        assertEquals(density, 1.0);
    }

    @Test
    public void testNullGraphDensity() {
        GraphModel graphModel = GraphGenerator.generateNullUndirectedGraph(5);
        Graph graph = graphModel.getGraph();
        GraphDensity d = new GraphDensity();
        double density = d.calculateDensity(graph, false);
        assertEquals(density, 0.0);
    }

    @Test
    public void testCompleteGraphDensity() {
        GraphModel graphModel = GraphGenerator.generateCompleteUndirectedGraph(5);
        Graph graph = graphModel.getGraph();
        GraphDensity d = new GraphDensity();
        double density = d.calculateDensity(graph, false);
        assertEquals(density, 1.0);
    }

    @Test
    public void testCyclicGraphDensity() {
        GraphModel graphModel = GraphGenerator.generateCyclicUndirectedGraph(6);
        Graph graph = graphModel.getGraph();
        GraphDensity d = new GraphDensity();
        double density = d.calculateDensity(graph, false);
        assertEquals(density, 0.4);
    }

    @Test
    public void testSelfLoopNodeDensity() {
        GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getGraphModel();
        UndirectedGraph undirectedGraph = graphModel.getUndirectedGraph();
        Node currentNode = graphModel.factory().newNode("0");
        undirectedGraph.addNode(currentNode);
        Edge currentEdge = graphModel.factory().newEdge(currentNode, currentNode, false);
        undirectedGraph.addEdge(currentEdge);
        Graph graph = graphModel.getGraph();
        GraphDensity d = new GraphDensity();
        double density = d.calculateDensity(graph, false);
        assertEquals(density, Double.POSITIVE_INFINITY);
    }

    @Test
    public void testCompleteGraphWithSelfLoopsDensity() {
        GraphModel graphModel = GraphGenerator.generateCompleteUndirectedGraph(3);
        UndirectedGraph undirectedGraph = graphModel.getUndirectedGraph();
        Node n1 = undirectedGraph.getNode("0");
        Node n2 = undirectedGraph.getNode("1");
        Node n3 = undirectedGraph.getNode("2");
        Edge currentEdge = graphModel.factory().newEdge(n1, n1, false);
        undirectedGraph.addEdge(currentEdge);
        currentEdge = graphModel.factory().newEdge(n2, n2, false);
        undirectedGraph.addEdge(currentEdge);
        currentEdge = graphModel.factory().newEdge(n3, n3, false);
        undirectedGraph.addEdge(currentEdge);
        Graph graph = graphModel.getGraph();
        GraphDensity d = new GraphDensity();
        double density = d.calculateDensity(graph, false);
        assertEquals(density, 2.0);
    }

    @Test
    public void testTwoCompleteGraphsDensity() {
        GraphModel graphModel = GraphGenerator.generateCompleteUndirectedGraph(4);
        UndirectedGraph undirectedGraph = graphModel.getUndirectedGraph();
        Node[] nodes = new Node[4];
        for (int i = 0; i < 4; i++) {
            Node currentNode = graphModel.factory().newNode(((Integer) (i + 4)).toString());
            nodes[i] = currentNode;
            undirectedGraph.addNode(currentNode);
        }
        for (int i = 0; i < 3; i++) {
            for (int j = i + 1; j < 4; j++) {
                Edge currentEdge = graphModel.factory().newEdge(nodes[i], nodes[j], false);
                undirectedGraph.addEdge(currentEdge);
            }
        }
        Graph graph = graphModel.getGraph();
        GraphDensity d = new GraphDensity();
        double density = d.calculateDensity(graph, false);
        double expectedAvDegree = 0.4286;
        double diff = Math.abs(density - expectedAvDegree);
        assertTrue(diff < 0.01);
    }

    @Test
    public void testDirectedPathGraphDensity() {
        GraphModel graphModel = GraphGenerator.generatePathDirectedGraph(2);
        DirectedGraph graph = graphModel.getDirectedGraph();
        GraphDensity d = new GraphDensity();
        double density = d.calculateDensity(graph, true);
        assertEquals(density, 0.5);
    }

    @Test
    public void testDirectedCyclicGraphDensity() {
        GraphModel graphModel = GraphGenerator.generateCyclicDirectedGraph(5);
        DirectedGraph graph = graphModel.getDirectedGraph();
        GraphDensity d = new GraphDensity();
        double density = d.calculateDensity(graph, true);
        assertEquals(density, 0.25);
    }

    @Test
    public void testDirectedCompleteGraphDensity() {
        GraphModel graphModel = GraphGenerator.generateCompleteDirectedGraph(5);
        DirectedGraph graph = graphModel.getDirectedGraph();
        GraphDensity d = new GraphDensity();
        double density = d.calculateDensity(graph, true);
        assertEquals(density, 1.0);
    }

    @Test
    public void testDirectedCompleteGraphWithSelfLoopsDensity() {
        GraphModel graphModel = GraphGenerator.generateCompleteDirectedGraph(3);
        DirectedGraph directedGraph = graphModel.getDirectedGraph();
        Node n1 = directedGraph.getNode("0");
        Node n2 = directedGraph.getNode("1");
        Node n3 = directedGraph.getNode("2");
        Edge currentEdge = graphModel.factory().newEdge(n1, n1);
        directedGraph.addEdge(currentEdge);
        currentEdge = graphModel.factory().newEdge(n2, n2);
        directedGraph.addEdge(currentEdge);
        currentEdge = graphModel.factory().newEdge(n3, n3);
        directedGraph.addEdge(currentEdge);

        DirectedGraph graph = graphModel.getDirectedGraph();

        GraphDensity d = new GraphDensity();
        double density = d.calculateDensity(graph, true);
        assertEquals(density, 1.5);
    }
}
