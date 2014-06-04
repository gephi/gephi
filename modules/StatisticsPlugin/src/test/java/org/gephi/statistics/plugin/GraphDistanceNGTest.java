/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.statistics.plugin;

import java.util.HashMap;
import org.gephi.graph.api.DirectedGraph;
import org.gephi.graph.api.UndirectedGraph;
import org.gephi.graph.api.Edge;
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
public class GraphDistanceNGTest {

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
    public void testOneNodeAvPathLength() {
        GraphModel graphModel = GraphGenerator.generateNullUndirectedGraph(1);

        GraphDistance d = new GraphDistance();
        d.initializeStartValues();
        UndirectedGraph undirectedGraph = graphModel.getUndirectedGraph();
        HashMap<Node, Integer> indicies = d.createIndiciesMap(undirectedGraph);

        d.calculateDistanceMetrics(graphModel.getGraph(), indicies, false, false);

        double averageDegree = d.getPathLength();
        assertEquals(averageDegree, Double.NaN);
    }

    @Test
    public void testTwoConnectedNodesAvPathLength() {
        GraphModel graphModel = GraphGenerator.generatePathUndirectedGraph(2);

        GraphDistance d = new GraphDistance();
        d.initializeStartValues();
        UndirectedGraph undirectedGraph = graphModel.getUndirectedGraph();
        HashMap<Node, Integer> indicies = d.createIndiciesMap(undirectedGraph);

        d.calculateDistanceMetrics(graphModel.getGraph(), indicies, false, false);

        double averageDegree = d.getPathLength();
        assertEquals(averageDegree, 1.0);
    }

    @Test
    public void testNullGraphAvPathLength() {
        GraphModel graphModel = GraphGenerator.generateNullUndirectedGraph(5);

        GraphDistance d = new GraphDistance();
        d.initializeStartValues();
        UndirectedGraph undirectedGraph = graphModel.getUndirectedGraph();
        HashMap<Node, Integer> indicies = d.createIndiciesMap(undirectedGraph);

        d.calculateDistanceMetrics(graphModel.getGraph(), indicies, false, false);

        double averageDegree = d.getPathLength();
        assertEquals(averageDegree, 0.0);
    }

    @Test
    public void testCompleteGraphAvPathLength() {
        GraphModel graphModel = GraphGenerator.generateCompleteUndirectedGraph(5);

        GraphDistance d = new GraphDistance();
        d.initializeStartValues();
        UndirectedGraph undirectedGraph = graphModel.getUndirectedGraph();
        HashMap<Node, Integer> indicies = d.createIndiciesMap(undirectedGraph);

        d.calculateDistanceMetrics(graphModel.getGraph(), indicies, false, false);

        double averageDegree = d.getPathLength();
        assertEquals(averageDegree, 1.0);
    }

    @Test
    public void testStarGraphAvPathLength() {
        GraphModel graphModel = GraphGenerator.generateStarUndirectedGraph(5);

        GraphDistance d = new GraphDistance();
        d.initializeStartValues();
        UndirectedGraph undirectedGraph = graphModel.getUndirectedGraph();
        HashMap<Node, Integer> indicies = d.createIndiciesMap(undirectedGraph);

        d.calculateDistanceMetrics(graphModel.getGraph(), indicies, false, false);

        double averageDegree = d.getPathLength();
        double res = 1.6667;
        double diff = Math.abs(averageDegree - res);
        assertTrue(diff < 0.01);
    }

    @Test
    public void testCyclicGraphAvPathLength() {
        GraphModel graphModel = GraphGenerator.generateCyclicUndirectedGraph(5);

        GraphDistance d = new GraphDistance();
        d.initializeStartValues();
        UndirectedGraph undirectedGraph = graphModel.getUndirectedGraph();
        HashMap<Node, Integer> indicies = d.createIndiciesMap(undirectedGraph);

        d.calculateDistanceMetrics(graphModel.getGraph(), indicies, false, false);

        double averageDegree = d.getPathLength();
        assertEquals(averageDegree, 1.5);
    }

    @Test
    public void testDirectedPathGraphAvPathLength() {

        GraphModel graphModel = GraphGenerator.generatePathDirectedGraph(4);

        GraphDistance d = new GraphDistance();
        d.initializeStartValues();
        UndirectedGraph undirectedGraph = graphModel.getUndirectedGraph();
        HashMap<Node, Integer> indicies = d.createIndiciesMap(undirectedGraph);

        d.calculateDistanceMetrics(graphModel.getGraph(), indicies, true, false);

        double averageDegree = d.getPathLength();
        double res = 1.6667;
        double diff = Math.abs(averageDegree - res);
        assertTrue(diff < 0.01);
    }

    @Test
    public void testDirectedCyclicGraphAvPathLength() {
        GraphModel graphModel = GraphGenerator.generateCyclicDirectedGraph(5);

        GraphDistance d = new GraphDistance();
        d.initializeStartValues();
        UndirectedGraph undirectedGraph = graphModel.getUndirectedGraph();
        HashMap<Node, Integer> indicies = d.createIndiciesMap(undirectedGraph);

        d.calculateDistanceMetrics(graphModel.getGraph(), indicies, true, false);

        double averageDegree = d.getPathLength();
        assertEquals(averageDegree, 2.5);
    }

    @Test
    public void testDirectedStarOutGraphAvPathLength() {
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

        GraphDistance d = new GraphDistance();
        d.initializeStartValues();
        UndirectedGraph undirectedGraph = graphModel.getUndirectedGraph();
        HashMap<Node, Integer> indicies = d.createIndiciesMap(undirectedGraph);

        d.calculateDistanceMetrics(graphModel.getGraph(), indicies, true, false);

        double averageDegree = d.getPathLength();
        assertEquals(averageDegree, 1.0);
    }

    @Test
    public void testDirectedSpecial1GraphAvPathLength() {
        GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getGraphModel();

        DirectedGraph directedGraph = graphModel.getDirectedGraph();
        Node node1 = graphModel.factory().newNode("0");
        Node node2 = graphModel.factory().newNode("1");
        Node node3 = graphModel.factory().newNode("2");
        Node node4 = graphModel.factory().newNode("3");
        Node node5 = graphModel.factory().newNode("4");

        directedGraph.addNode(node1);
        directedGraph.addNode(node2);
        directedGraph.addNode(node3);
        directedGraph.addNode(node4);
        directedGraph.addNode(node5);

        Edge edge15 = graphModel.factory().newEdge(node1, node5);
        Edge edge52 = graphModel.factory().newEdge(node5, node2);
        Edge edge53 = graphModel.factory().newEdge(node5, node3);
        Edge edge45 = graphModel.factory().newEdge(node4, node5);

        directedGraph.addEdge(edge15);
        directedGraph.addEdge(edge52);
        directedGraph.addEdge(edge53);
        directedGraph.addEdge(edge45);

        GraphDistance d = new GraphDistance();
        d.initializeStartValues();
        UndirectedGraph undirectedGraph = graphModel.getUndirectedGraph();
        HashMap<Node, Integer> indicies = d.createIndiciesMap(undirectedGraph);

        d.calculateDistanceMetrics(graphModel.getGraph(), indicies, true, false);

        double averageDegree = d.getPathLength();
        assertEquals(averageDegree, 1.5);
    }

    @Test
    public void testOneNodeDiameter() {
        GraphModel graphModel = GraphGenerator.generateNullUndirectedGraph(1);

        GraphDistance d = new GraphDistance();
        d.initializeStartValues();
        UndirectedGraph undirectedGraph = graphModel.getUndirectedGraph();
        HashMap<Node, Integer> indicies = d.createIndiciesMap(undirectedGraph);

        d.calculateDistanceMetrics(graphModel.getGraph(), indicies, false, false);

        double diameter = d.getDiameter();
        assertEquals(diameter, 0.0);
    }

    @Test
    public void testTwoConnectrdNodesDiameter() {
        GraphModel graphModel = GraphGenerator.generatePathUndirectedGraph(2);

        GraphDistance d = new GraphDistance();
        d.initializeStartValues();
        UndirectedGraph undirectedGraph = graphModel.getUndirectedGraph();
        HashMap<Node, Integer> indicies = d.createIndiciesMap(undirectedGraph);

        d.calculateDistanceMetrics(graphModel.getGraph(), indicies, false, false);

        double diameter = d.getDiameter();
        assertEquals(diameter, 1.0);
    }

    @Test
    public void testNullGraphDiameter() {
        GraphModel graphModel = GraphGenerator.generateNullUndirectedGraph(5);

        GraphDistance d = new GraphDistance();
        d.initializeStartValues();
        UndirectedGraph undirectedGraph = graphModel.getUndirectedGraph();
        HashMap<Node, Integer> indicies = d.createIndiciesMap(undirectedGraph);

        d.calculateDistanceMetrics(graphModel.getGraph(), indicies, false, false);

        double diameter = d.getDiameter();
        assertEquals(diameter, 0.0);
    }

    @Test
    public void testCompleteGraphDiameter() {
        GraphModel graphModel = GraphGenerator.generateCompleteUndirectedGraph(5);

        GraphDistance d = new GraphDistance();
        d.initializeStartValues();
        UndirectedGraph undirectedGraph = graphModel.getUndirectedGraph();
        HashMap<Node, Integer> indicies = d.createIndiciesMap(undirectedGraph);

        d.calculateDistanceMetrics(graphModel.getGraph(), indicies, false, false);

        double diameter = d.getDiameter();
        assertEquals(diameter, 1.0);
    }

    @Test
    public void testCyclicGraphDiameter() {
        GraphModel graphModel = GraphGenerator.generateCyclicUndirectedGraph(5);

        GraphDistance d = new GraphDistance();
        d.initializeStartValues();
        UndirectedGraph undirectedGraph = graphModel.getUndirectedGraph();
        HashMap<Node, Integer> indicies = d.createIndiciesMap(undirectedGraph);

        d.calculateDistanceMetrics(graphModel.getGraph(), indicies, false, false);

        double diameter = d.getDiameter();
        assertEquals(diameter, 2.0);
    }

    @Test
    public void testSpecial1UndirectedGraphDiameter() {
        GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getGraphModel();
        UndirectedGraph undirectedGraph = graphModel.getUndirectedGraph();
        Node node1 = graphModel.factory().newNode("0");
        Node node2 = graphModel.factory().newNode("1");
        Node node3 = graphModel.factory().newNode("2");
        Node node4 = graphModel.factory().newNode("3");
        Node node5 = graphModel.factory().newNode("4");
        Node node6 = graphModel.factory().newNode("5");
        Node node7 = graphModel.factory().newNode("6");
        Node node8 = graphModel.factory().newNode("7");
        undirectedGraph.addNode(node1);
        undirectedGraph.addNode(node2);
        undirectedGraph.addNode(node3);
        undirectedGraph.addNode(node4);
        undirectedGraph.addNode(node5);
        undirectedGraph.addNode(node6);
        undirectedGraph.addNode(node7);
        undirectedGraph.addNode(node8);
        Edge edge12 = graphModel.factory().newEdge(node1, node2, false);
        Edge edge13 = graphModel.factory().newEdge(node1, node3, false);
        Edge edge34 = graphModel.factory().newEdge(node3, node4, false);
        Edge edge24 = graphModel.factory().newEdge(node2, node4, false);
        Edge edge56 = graphModel.factory().newEdge(node5, node6, false);
        Edge edge67 = graphModel.factory().newEdge(node6, node7, false);
        Edge edge78 = graphModel.factory().newEdge(node7, node8, false);
        Edge edge85 = graphModel.factory().newEdge(node8, node5, false);
        Edge edge45 = graphModel.factory().newEdge(node4, node5, false);
        undirectedGraph.addEdge(edge12);
        undirectedGraph.addEdge(edge13);
        undirectedGraph.addEdge(edge34);
        undirectedGraph.addEdge(edge24);
        undirectedGraph.addEdge(edge56);
        undirectedGraph.addEdge(edge67);
        undirectedGraph.addEdge(edge78);
        undirectedGraph.addEdge(edge85);
        undirectedGraph.addEdge(edge45);

        GraphDistance d = new GraphDistance();
        d.initializeStartValues();
        UndirectedGraph hierarchicalUndirectedGraph = graphModel.getUndirectedGraph();
        HashMap<Node, Integer> indicies = d.createIndiciesMap(hierarchicalUndirectedGraph);

        d.calculateDistanceMetrics(graphModel.getGraph(), indicies, false, false);

        double diameter = d.getDiameter();
        assertEquals(diameter, 5.0);
    }

    @Test
    public void testSpecial2UndirectedGraphDiameter() {
        GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getGraphModel();
        UndirectedGraph undirectedGraph = graphModel.getUndirectedGraph();
        Node node1 = graphModel.factory().newNode("0");
        Node node2 = graphModel.factory().newNode("1");
        Node node3 = graphModel.factory().newNode("2");
        Node node4 = graphModel.factory().newNode("3");
        Node node5 = graphModel.factory().newNode("4");
        Node node6 = graphModel.factory().newNode("5");
        Node node7 = graphModel.factory().newNode("6");
        Node node8 = graphModel.factory().newNode("7");
        undirectedGraph.addNode(node1);
        undirectedGraph.addNode(node2);
        undirectedGraph.addNode(node3);
        undirectedGraph.addNode(node4);
        undirectedGraph.addNode(node5);
        undirectedGraph.addNode(node6);
        undirectedGraph.addNode(node7);
        undirectedGraph.addNode(node8);
        Edge edge12 = graphModel.factory().newEdge(node1, node2, false);
        Edge edge23 = graphModel.factory().newEdge(node2, node3, false);
        Edge edge34 = graphModel.factory().newEdge(node3, node4, false);
        Edge edge45 = graphModel.factory().newEdge(node4, node5, false);
        Edge edge56 = graphModel.factory().newEdge(node5, node6, false);
        Edge edge67 = graphModel.factory().newEdge(node6, node7, false);
        Edge edge78 = graphModel.factory().newEdge(node7, node8, false);
        Edge edge81 = graphModel.factory().newEdge(node8, node1, false);
        Edge edge14 = graphModel.factory().newEdge(node1, node4, false);
        Edge edge85 = graphModel.factory().newEdge(node8, node5, false);
        undirectedGraph.addEdge(edge12);
        undirectedGraph.addEdge(edge23);
        undirectedGraph.addEdge(edge34);
        undirectedGraph.addEdge(edge45);
        undirectedGraph.addEdge(edge56);
        undirectedGraph.addEdge(edge67);
        undirectedGraph.addEdge(edge78);
        undirectedGraph.addEdge(edge81);
        undirectedGraph.addEdge(edge14);
        undirectedGraph.addEdge(edge85);

        GraphDistance d = new GraphDistance();
        d.initializeStartValues();
        UndirectedGraph hierarchicalUndirectedGraph = graphModel.getUndirectedGraph();
        HashMap<Node, Integer> indicies = d.createIndiciesMap(hierarchicalUndirectedGraph);

        d.calculateDistanceMetrics(graphModel.getGraph(), indicies, false, false);

        double diameter = d.getDiameter();
        assertEquals(diameter, 4.0);
    }

    @Test
    public void testDirectedPathGraphDiameter() {
        GraphModel graphModel = GraphGenerator.generatePathDirectedGraph(4);

        GraphDistance d = new GraphDistance();
        d.initializeStartValues();
        UndirectedGraph undirectedGraph = graphModel.getUndirectedGraph();
        HashMap<Node, Integer> indicies = d.createIndiciesMap(undirectedGraph);

        d.calculateDistanceMetrics(graphModel.getGraph(), indicies, true, false);

        double diameter = d.getDiameter();
        assertEquals(diameter, 3.0);
    }

    @Test
    public void testDirectedCyclicDiameter() {
        GraphModel graphModel = GraphGenerator.generateCyclicDirectedGraph(5);

        GraphDistance d = new GraphDistance();
        d.initializeStartValues();
        UndirectedGraph undirectedGraph = graphModel.getUndirectedGraph();
        HashMap<Node, Integer> indicies = d.createIndiciesMap(undirectedGraph);

        d.calculateDistanceMetrics(graphModel.getGraph(), indicies, true, false);

        double diameter = d.getDiameter();
        assertEquals(diameter, 4.0);
    }

    @Test
    public void testSpecial1DirectedGraphDiameter() {
        GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getGraphModel();
        DirectedGraph directedGraph = graphModel.getDirectedGraph();
        Node node1 = graphModel.factory().newNode("0");
        Node node2 = graphModel.factory().newNode("1");
        Node node3 = graphModel.factory().newNode("2");
        Node node4 = graphModel.factory().newNode("3");
        Node node5 = graphModel.factory().newNode("4");
        Node node6 = graphModel.factory().newNode("5");
        Node node7 = graphModel.factory().newNode("6");
        Node node8 = graphModel.factory().newNode("7");
        directedGraph.addNode(node1);
        directedGraph.addNode(node2);
        directedGraph.addNode(node3);
        directedGraph.addNode(node4);
        directedGraph.addNode(node5);
        directedGraph.addNode(node6);
        directedGraph.addNode(node7);
        directedGraph.addNode(node8);
        Edge edge12 = graphModel.factory().newEdge(node1, node2);
        Edge edge23 = graphModel.factory().newEdge(node2, node3);
        Edge edge34 = graphModel.factory().newEdge(node3, node4);
        Edge edge41 = graphModel.factory().newEdge(node4, node1);
        Edge edge56 = graphModel.factory().newEdge(node5, node6);
        Edge edge67 = graphModel.factory().newEdge(node6, node7);
        Edge edge78 = graphModel.factory().newEdge(node7, node8);
        Edge edge85 = graphModel.factory().newEdge(node8, node5);
        Edge edge45 = graphModel.factory().newEdge(node4, node5);
        directedGraph.addEdge(edge12);
        directedGraph.addEdge(edge23);
        directedGraph.addEdge(edge34);
        directedGraph.addEdge(edge41);
        directedGraph.addEdge(edge56);
        directedGraph.addEdge(edge67);
        directedGraph.addEdge(edge78);
        directedGraph.addEdge(edge85);
        directedGraph.addEdge(edge45);

        GraphDistance d = new GraphDistance();
        d.initializeStartValues();
        UndirectedGraph undirectedGraph = graphModel.getUndirectedGraph();
        HashMap<Node, Integer> indicies = d.createIndiciesMap(undirectedGraph);

        d.calculateDistanceMetrics(graphModel.getGraph(), indicies, true, false);

        double diameter = d.getDiameter();
        assertEquals(diameter, 7.0);
    }

    @Test
    public void testSpecial2DirectedGraphDiameter() {
        GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getGraphModel();
        DirectedGraph directedGraph = graphModel.getDirectedGraph();
        Node node1 = graphModel.factory().newNode("0");
        Node node2 = graphModel.factory().newNode("1");
        Node node3 = graphModel.factory().newNode("2");
        Node node4 = graphModel.factory().newNode("3");
        Node node5 = graphModel.factory().newNode("4");
        directedGraph.addNode(node1);
        directedGraph.addNode(node2);
        directedGraph.addNode(node3);
        directedGraph.addNode(node4);
        directedGraph.addNode(node5);
        Edge edge12 = graphModel.factory().newEdge(node1, node2);
        Edge edge14 = graphModel.factory().newEdge(node1, node4);
        Edge edge23 = graphModel.factory().newEdge(node2, node3);
        Edge edge25 = graphModel.factory().newEdge(node2, node5);
        Edge edge35 = graphModel.factory().newEdge(node3, node5);
        Edge edge43 = graphModel.factory().newEdge(node4, node3);
        Edge edge51 = graphModel.factory().newEdge(node5, node1);
        Edge edge54 = graphModel.factory().newEdge(node5, node4);
        directedGraph.addEdge(edge12);
        directedGraph.addEdge(edge14);
        directedGraph.addEdge(edge23);
        directedGraph.addEdge(edge25);
        directedGraph.addEdge(edge35);
        directedGraph.addEdge(edge43);
        directedGraph.addEdge(edge51);
        directedGraph.addEdge(edge54);

        GraphDistance d = new GraphDistance();
        d.initializeStartValues();
        UndirectedGraph undirectedGraph = graphModel.getUndirectedGraph();
        HashMap<Node, Integer> indicies = d.createIndiciesMap(undirectedGraph);

        d.calculateDistanceMetrics(graphModel.getGraph(), indicies, true, false);

        double diameter = d.getDiameter();
        assertEquals(diameter, 4.0);
    }

    @Test
    public void testOneNodeRadius() {
        GraphModel graphModel = GraphGenerator.generateNullUndirectedGraph(1);

        GraphDistance d = new GraphDistance();
        d.initializeStartValues();
        UndirectedGraph undirectedGraph = graphModel.getUndirectedGraph();
        HashMap<Node, Integer> indicies = d.createIndiciesMap(undirectedGraph);

        d.calculateDistanceMetrics(graphModel.getGraph(), indicies, false, false);

        double radius = d.getRadius();
        assertEquals(radius, 0.0);
    }

    @Test
    public void testTwoConnectrdNodesRadius() {
        GraphModel graphModel = GraphGenerator.generatePathUndirectedGraph(2);

        GraphDistance d = new GraphDistance();
        d.initializeStartValues();
        UndirectedGraph undirectedGraph = graphModel.getUndirectedGraph();
        HashMap<Node, Integer> indicies = d.createIndiciesMap(undirectedGraph);

        d.calculateDistanceMetrics(graphModel.getGraph(), indicies, false, false);

        double radius = d.getRadius();
        assertEquals(radius, 1.0);
    }

    @Test
    public void testNullGraphRadius() {
        GraphModel graphModel = GraphGenerator.generateNullUndirectedGraph(5);

        GraphDistance d = new GraphDistance();
        d.initializeStartValues();
        UndirectedGraph undirectedGraph = graphModel.getUndirectedGraph();
        HashMap<Node, Integer> indicies = d.createIndiciesMap(undirectedGraph);

        d.calculateDistanceMetrics(graphModel.getGraph(), indicies, false, false);

        double radius = d.getRadius();
        assertEquals(radius, 0.0);
    }

    @Test
    public void testCompleteGraphRadius() {
        GraphModel graphModel = GraphGenerator.generateCompleteUndirectedGraph(5);

        GraphDistance d = new GraphDistance();
        d.initializeStartValues();
        UndirectedGraph undirectedGraph = graphModel.getUndirectedGraph();
        HashMap<Node, Integer> indicies = d.createIndiciesMap(undirectedGraph);

        d.calculateDistanceMetrics(graphModel.getGraph(), indicies, false, false);

        double radius = d.getRadius();
        assertEquals(radius, 1.0);
    }

    @Test
    public void testStarGraphRadius() {
        GraphModel graphModel = GraphGenerator.generateStarUndirectedGraph(5);

        GraphDistance d = new GraphDistance();
        d.initializeStartValues();
        UndirectedGraph undirectedGraph = graphModel.getUndirectedGraph();
        HashMap<Node, Integer> indicies = d.createIndiciesMap(undirectedGraph);

        d.calculateDistanceMetrics(graphModel.getGraph(), indicies, false, false);

        double radius = d.getRadius();
        assertEquals(radius, 1.0);
    }

    @Test
    public void testCyclicGraphRadius() {
        GraphModel graphModel = GraphGenerator.generateCyclicUndirectedGraph(5);

        GraphDistance d = new GraphDistance();
        d.initializeStartValues();
        UndirectedGraph undirectedGraph = graphModel.getUndirectedGraph();
        HashMap<Node, Integer> indicies = d.createIndiciesMap(undirectedGraph);

        d.calculateDistanceMetrics(graphModel.getGraph(), indicies, false, false);

        double radius = d.getRadius();
        assertEquals(radius, 2.0);
    }

    @Test
    public void testPathGraphRadius() {
        GraphModel graphModel = GraphGenerator.generatePathUndirectedGraph(6);

        GraphDistance d = new GraphDistance();
        d.initializeStartValues();
        UndirectedGraph undirectedGraph = graphModel.getUndirectedGraph();
        HashMap<Node, Integer> indicies = d.createIndiciesMap(undirectedGraph);

        d.calculateDistanceMetrics(graphModel.getGraph(), indicies, false, false);

        double radius = d.getRadius();
        assertEquals(radius, 3.0);
    }

    @Test
    public void testSpecial1UndirectedGraphRadius() {
        GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getGraphModel();
        UndirectedGraph undirectedGraph = graphModel.getUndirectedGraph();
        Node node1 = graphModel.factory().newNode("0");
        Node node2 = graphModel.factory().newNode("1");
        Node node3 = graphModel.factory().newNode("2");
        Node node4 = graphModel.factory().newNode("3");
        Node node5 = graphModel.factory().newNode("4");
        Node node6 = graphModel.factory().newNode("5");
        Node node7 = graphModel.factory().newNode("6");
        Node node8 = graphModel.factory().newNode("7");
        undirectedGraph.addNode(node1);
        undirectedGraph.addNode(node2);
        undirectedGraph.addNode(node3);
        undirectedGraph.addNode(node4);
        undirectedGraph.addNode(node5);
        undirectedGraph.addNode(node6);
        undirectedGraph.addNode(node7);
        undirectedGraph.addNode(node8);
        Edge edge12 = graphModel.factory().newEdge(node1, node2, false);
        Edge edge13 = graphModel.factory().newEdge(node1, node3, false);
        Edge edge34 = graphModel.factory().newEdge(node3, node4, false);
        Edge edge24 = graphModel.factory().newEdge(node2, node4, false);
        Edge edge56 = graphModel.factory().newEdge(node5, node6, false);
        Edge edge67 = graphModel.factory().newEdge(node6, node7, false);
        Edge edge78 = graphModel.factory().newEdge(node7, node8, false);
        Edge edge85 = graphModel.factory().newEdge(node8, node5, false);
        Edge edge45 = graphModel.factory().newEdge(node4, node5, false);
        undirectedGraph.addEdge(edge12);
        undirectedGraph.addEdge(edge13);
        undirectedGraph.addEdge(edge34);
        undirectedGraph.addEdge(edge24);
        undirectedGraph.addEdge(edge56);
        undirectedGraph.addEdge(edge67);
        undirectedGraph.addEdge(edge78);
        undirectedGraph.addEdge(edge85);
        undirectedGraph.addEdge(edge45);

        GraphDistance d = new GraphDistance();
        d.initializeStartValues();
        UndirectedGraph hierarchicalUndirectedGraph = graphModel.getUndirectedGraph();
        HashMap<Node, Integer> indicies = d.createIndiciesMap(hierarchicalUndirectedGraph);

        d.calculateDistanceMetrics(graphModel.getGraph(), indicies, false, false);

        double radius = d.getRadius();
        assertEquals(radius, 3.0);
    }

    @Test
    public void testSpecial2UndirectedGraphRadius() {
        GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getGraphModel();
        UndirectedGraph undirectedGraph = graphModel.getUndirectedGraph();
        Node node1 = graphModel.factory().newNode("0");
        Node node2 = graphModel.factory().newNode("1");
        Node node3 = graphModel.factory().newNode("2");
        Node node4 = graphModel.factory().newNode("3");
        Node node5 = graphModel.factory().newNode("4");
        Node node6 = graphModel.factory().newNode("5");
        Node node7 = graphModel.factory().newNode("6");
        Node node8 = graphModel.factory().newNode("7");
        undirectedGraph.addNode(node1);
        undirectedGraph.addNode(node2);
        undirectedGraph.addNode(node3);
        undirectedGraph.addNode(node4);
        undirectedGraph.addNode(node5);
        undirectedGraph.addNode(node6);
        undirectedGraph.addNode(node7);
        undirectedGraph.addNode(node8);
        Edge edge12 = graphModel.factory().newEdge(node1, node2, false);
        Edge edge23 = graphModel.factory().newEdge(node2, node3, false);
        Edge edge34 = graphModel.factory().newEdge(node3, node4, false);
        Edge edge45 = graphModel.factory().newEdge(node4, node5, false);
        Edge edge56 = graphModel.factory().newEdge(node5, node6, false);
        Edge edge67 = graphModel.factory().newEdge(node6, node7, false);
        Edge edge78 = graphModel.factory().newEdge(node7, node8, false);
        Edge edge81 = graphModel.factory().newEdge(node8, node1, false);
        Edge edge14 = graphModel.factory().newEdge(node1, node4, false);
        Edge edge85 = graphModel.factory().newEdge(node8, node5, false);
        undirectedGraph.addEdge(edge12);
        undirectedGraph.addEdge(edge23);
        undirectedGraph.addEdge(edge34);
        undirectedGraph.addEdge(edge45);
        undirectedGraph.addEdge(edge56);
        undirectedGraph.addEdge(edge67);
        undirectedGraph.addEdge(edge78);
        undirectedGraph.addEdge(edge81);
        undirectedGraph.addEdge(edge14);
        undirectedGraph.addEdge(edge85);

        GraphDistance d = new GraphDistance();
        d.initializeStartValues();
        UndirectedGraph hierarchicalUndirectedGraph = graphModel.getUndirectedGraph();
        HashMap<Node, Integer> indicies = d.createIndiciesMap(hierarchicalUndirectedGraph);

        d.calculateDistanceMetrics(graphModel.getGraph(), indicies, false, false);

        double radius = d.getRadius();
        assertEquals(radius, 3.0);
    }

    @Test
    public void testDirectedCyclicRadius() {
        GraphModel graphModel = GraphGenerator.generateCyclicDirectedGraph(5);

        GraphDistance d = new GraphDistance();
        d.initializeStartValues();
        UndirectedGraph undirectedGraph = graphModel.getUndirectedGraph();
        HashMap<Node, Integer> indicies = d.createIndiciesMap(undirectedGraph);

        d.calculateDistanceMetrics(graphModel.getGraph(), indicies, true, false);

        double radius = d.getRadius();
        assertEquals(radius, 4.0);
    }

    @Test
    public void testDirectedPathGraphRadius() {
        GraphModel graphModel = GraphGenerator.generatePathDirectedGraph(4);

        GraphDistance d = new GraphDistance();
        d.initializeStartValues();
        UndirectedGraph undirectedGraph = graphModel.getUndirectedGraph();
        HashMap<Node, Integer> indicies = d.createIndiciesMap(undirectedGraph);

        d.calculateDistanceMetrics(graphModel.getGraph(), indicies, true, false);

        double radius = d.getRadius();
        assertEquals(radius, 0.0);
    }

    @Test
    public void testSpecial2DirectedGraphRadius() {
        GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getGraphModel();
        DirectedGraph directedGraph = graphModel.getDirectedGraph();
        Node node1 = graphModel.factory().newNode("0");
        Node node2 = graphModel.factory().newNode("1");
        Node node3 = graphModel.factory().newNode("2");
        Node node4 = graphModel.factory().newNode("3");
        Node node5 = graphModel.factory().newNode("4");
        directedGraph.addNode(node1);
        directedGraph.addNode(node2);
        directedGraph.addNode(node3);
        directedGraph.addNode(node4);
        directedGraph.addNode(node5);
        Edge edge12 = graphModel.factory().newEdge(node1, node2);
        Edge edge14 = graphModel.factory().newEdge(node1, node4);
        Edge edge23 = graphModel.factory().newEdge(node2, node3);
        Edge edge25 = graphModel.factory().newEdge(node2, node5);
        Edge edge35 = graphModel.factory().newEdge(node3, node5);
        Edge edge43 = graphModel.factory().newEdge(node4, node3);
        Edge edge51 = graphModel.factory().newEdge(node5, node1);
        Edge edge54 = graphModel.factory().newEdge(node5, node4);
        directedGraph.addEdge(edge12);
        directedGraph.addEdge(edge14);
        directedGraph.addEdge(edge23);
        directedGraph.addEdge(edge25);
        directedGraph.addEdge(edge35);
        directedGraph.addEdge(edge43);
        directedGraph.addEdge(edge51);
        directedGraph.addEdge(edge54);

        GraphDistance d = new GraphDistance();
        d.initializeStartValues();
        UndirectedGraph undirectedGraph = graphModel.getUndirectedGraph();
        HashMap<Node, Integer> indicies = d.createIndiciesMap(undirectedGraph);

        d.calculateDistanceMetrics(graphModel.getGraph(), indicies, true, false);

        double radius = d.getRadius();
        assertEquals(radius, 2.0);
    }

    @Test
    public void testTwoConnectedNodesBetweenness() {
        GraphModel graphModel = GraphGenerator.generatePathUndirectedGraph(2);

        GraphDistance d = new GraphDistance();
        d.initializeStartValues();
        UndirectedGraph undirectedGraph = graphModel.getUndirectedGraph();
        HashMap<Node, Integer> indicies = d.createIndiciesMap(undirectedGraph);

        HashMap<String, double[]> metricsMap = (HashMap) d.calculateDistanceMetrics(graphModel.getGraph(), indicies, false, false);
        double[] betweenness = metricsMap.get(GraphDistance.BETWEENNESS);

        Node n1 = undirectedGraph.getNode("0");
        int index1 = indicies.get(n1);

        assertEquals(betweenness[index1], 0.0);
    }

    @Test
    public void testTwoConnectedNodesCloseness() {
        GraphModel graphModel = GraphGenerator.generatePathUndirectedGraph(2);

        GraphDistance d = new GraphDistance();
        d.initializeStartValues();
        UndirectedGraph undirectedGraph = graphModel.getUndirectedGraph();
        HashMap<Node, Integer> indicies = d.createIndiciesMap(undirectedGraph);

        HashMap<String, double[]> metricsMap = (HashMap) d.calculateDistanceMetrics(graphModel.getGraph(), indicies, false, false);
        double[] closeness = metricsMap.get(GraphDistance.CLOSENESS);

        Node n1 = undirectedGraph.getNode("0");
        int index1 = indicies.get(n1);

        assertEquals(closeness[index1], 1.0);
    }

    @Test
    public void testNullGraphBetweenness() {
        GraphModel graphModel = GraphGenerator.generateNullUndirectedGraph(5);

        GraphDistance d = new GraphDistance();
        d.initializeStartValues();
        UndirectedGraph undirectedGraph = graphModel.getUndirectedGraph();
        HashMap<Node, Integer> indicies = d.createIndiciesMap(undirectedGraph);

        HashMap<String, double[]> metricsMap = (HashMap) d.calculateDistanceMetrics(graphModel.getGraph(), indicies, false, false);
        double[] betweenness = metricsMap.get(GraphDistance.BETWEENNESS);

        Node n1 = undirectedGraph.getNode("0");
        int index1 = indicies.get(n1);

        assertEquals(betweenness[index1], 0.0);
    }

    @Test
    public void testNullGraphCloseness() {
        GraphModel graphModel = GraphGenerator.generateNullUndirectedGraph(5);

        GraphDistance d = new GraphDistance();
        d.initializeStartValues();
        UndirectedGraph undirectedGraph = graphModel.getUndirectedGraph();
        HashMap<Node, Integer> indicies = d.createIndiciesMap(undirectedGraph);

        HashMap<String, double[]> metricsMap = (HashMap) d.calculateDistanceMetrics(graphModel.getGraph(), indicies, false, false);
        double[] closeness = metricsMap.get(GraphDistance.CLOSENESS);

        Node n1 = undirectedGraph.getNode("0");
        int index1 = indicies.get(n1);

        assertEquals(closeness[index1], 0.0);
    }

    @Test
    public void testCompleteGraphBetweenness() {
        GraphModel graphModel = GraphGenerator.generateCompleteUndirectedGraph(5);

        GraphDistance d = new GraphDistance();
        d.initializeStartValues();
        UndirectedGraph undirectedGraph = graphModel.getUndirectedGraph();
        HashMap<Node, Integer> indicies = d.createIndiciesMap(undirectedGraph);

        HashMap<String, double[]> metricsMap = (HashMap) d.calculateDistanceMetrics(graphModel.getGraph(), indicies, false, false);
        double[] betweenness = metricsMap.get(GraphDistance.BETWEENNESS);

        Node n1 = undirectedGraph.getNode("0");
        int index1 = indicies.get(n1);

        assertEquals(betweenness[index1], 0.0);
    }

    @Test
    public void testCompleteGraphCloseness() {
        GraphModel graphModel = GraphGenerator.generateCompleteUndirectedGraph(5);

        GraphDistance d = new GraphDistance();
        d.initializeStartValues();
        UndirectedGraph undirectedGraph = graphModel.getUndirectedGraph();
        HashMap<Node, Integer> indicies = d.createIndiciesMap(undirectedGraph);

        HashMap<String, double[]> metricsMap = (HashMap) d.calculateDistanceMetrics(graphModel.getGraph(), indicies, false, false);
        double[] closeness = metricsMap.get(GraphDistance.CLOSENESS);

        Node n1 = undirectedGraph.getNode("0");
        int index1 = indicies.get(n1);

        assertEquals(closeness[index1], 1.0);
    }

    @Test
    public void testStarGraphBetweenness() {
        GraphModel graphModel = GraphGenerator.generateStarUndirectedGraph(5);

        GraphDistance d = new GraphDistance();
        d.initializeStartValues();
        UndirectedGraph undirectedGraph = graphModel.getUndirectedGraph();
        HashMap<Node, Integer> indicies = d.createIndiciesMap(undirectedGraph);

        HashMap<String, double[]> metricsMap = (HashMap) d.calculateDistanceMetrics(graphModel.getGraph(), indicies, false, false);
        double[] betweenness = metricsMap.get(GraphDistance.BETWEENNESS);

        Node n1 = undirectedGraph.getNode("0");
        Node n2 = undirectedGraph.getNode("1");
        int index1 = indicies.get(n1);
        int index2 = indicies.get(n2);

        assertEquals(betweenness[index1], 10.0);
        assertEquals(betweenness[index2], 0.0);
    }

    @Test
    public void testStarGraphCloseness() {
        GraphModel graphModel = GraphGenerator.generateStarUndirectedGraph(5);

        GraphDistance d = new GraphDistance();
        d.initializeStartValues();
        UndirectedGraph undirectedGraph = graphModel.getUndirectedGraph();
        HashMap<Node, Integer> indicies = d.createIndiciesMap(undirectedGraph);

        HashMap<String, double[]> metricsMap = (HashMap) d.calculateDistanceMetrics(graphModel.getGraph(), indicies, false, false);
        double[] closeness = metricsMap.get(GraphDistance.CLOSENESS);

        Node n1 = undirectedGraph.getNode("0");
        Node n2 = undirectedGraph.getNode("1");
        int index1 = indicies.get(n1);
        int index2 = indicies.get(n2);

        assertEquals(closeness[index1], 1.0);
        assertEquals(closeness[index2], 1.8);
    }

    @Test
    public void testCyclic5GraphBetweenness() {
        GraphModel graphModel = GraphGenerator.generateCyclicUndirectedGraph(5);

        GraphDistance d = new GraphDistance();
        d.initializeStartValues();
        UndirectedGraph undirectedGraph = graphModel.getUndirectedGraph();
        HashMap<Node, Integer> indicies = d.createIndiciesMap(undirectedGraph);

        HashMap<String, double[]> metricsMap = (HashMap) d.calculateDistanceMetrics(graphModel.getGraph(), indicies, false, false);
        double[] betweenness = metricsMap.get(GraphDistance.BETWEENNESS);

        Node n4 = undirectedGraph.getNode("3");
        int index4 = indicies.get(n4);

        assertEquals(betweenness[index4], 1.0);
    }

    @Test
    public void testCyclic6GraphBetweenness() {
        GraphModel graphModel = GraphGenerator.generateCyclicUndirectedGraph(6);

        GraphDistance d = new GraphDistance();
        d.initializeStartValues();
        UndirectedGraph undirectedGraph = graphModel.getUndirectedGraph();
        HashMap<Node, Integer> indicies = d.createIndiciesMap(undirectedGraph);

        HashMap<String, double[]> metricsMap = (HashMap) d.calculateDistanceMetrics(graphModel.getGraph(), indicies, false, false);
        double[] betweenness = metricsMap.get(GraphDistance.BETWEENNESS);

        Node n2 = undirectedGraph.getNode("1");
        int index2 = indicies.get(n2);

        assertEquals(betweenness[index2], 2.0);
    }

    @Test
    public void testCyclic5GraphCloseness() {
        GraphModel graphModel = GraphGenerator.generateCyclicUndirectedGraph(5);

        GraphDistance d = new GraphDistance();
        d.initializeStartValues();
        UndirectedGraph undirectedGraph = graphModel.getUndirectedGraph();
        HashMap<Node, Integer> indicies = d.createIndiciesMap(undirectedGraph);

        HashMap<String, double[]> metricsMap = (HashMap) d.calculateDistanceMetrics(graphModel.getGraph(), indicies, false, false);
        double[] closeness = metricsMap.get(GraphDistance.CLOSENESS);

        Node n3 = undirectedGraph.getNode("2");
        int index3 = indicies.get(n3);

        assertEquals(closeness[index3], 1.5);
    }

    @Test
    public void testSpecial1UndirectedGraphBetweenness() {
        GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getGraphModel();
        UndirectedGraph undirectedGraph = graphModel.getUndirectedGraph();
        Node node1 = graphModel.factory().newNode("0");
        Node node2 = graphModel.factory().newNode("1");
        Node node3 = graphModel.factory().newNode("2");
        Node node4 = graphModel.factory().newNode("3");
        Node node5 = graphModel.factory().newNode("4");
        Node node6 = graphModel.factory().newNode("5");
        Node node7 = graphModel.factory().newNode("6");
        undirectedGraph.addNode(node1);
        undirectedGraph.addNode(node2);
        undirectedGraph.addNode(node3);
        undirectedGraph.addNode(node4);
        undirectedGraph.addNode(node5);
        undirectedGraph.addNode(node6);
        undirectedGraph.addNode(node7);
        Edge edge12 = graphModel.factory().newEdge(node1, node2, false);
        Edge edge13 = graphModel.factory().newEdge(node1, node3, false);
        Edge edge14 = graphModel.factory().newEdge(node1, node4, false);
        Edge edge15 = graphModel.factory().newEdge(node1, node5, false);
        Edge edge16 = graphModel.factory().newEdge(node1, node6, false);
        Edge edge27 = graphModel.factory().newEdge(node2, node7, false);
        Edge edge37 = graphModel.factory().newEdge(node3, node7, false);
        Edge edge47 = graphModel.factory().newEdge(node4, node7, false);
        Edge edge57 = graphModel.factory().newEdge(node5, node7, false);
        Edge edge67 = graphModel.factory().newEdge(node6, node7, false);
        undirectedGraph.addEdge(edge12);
        undirectedGraph.addEdge(edge13);
        undirectedGraph.addEdge(edge14);
        undirectedGraph.addEdge(edge15);
        undirectedGraph.addEdge(edge16);
        undirectedGraph.addEdge(edge27);
        undirectedGraph.addEdge(edge37);
        undirectedGraph.addEdge(edge47);
        undirectedGraph.addEdge(edge57);
        undirectedGraph.addEdge(edge67);

        GraphDistance d = new GraphDistance();
        d.initializeStartValues();
        UndirectedGraph hierarchicalUndirectedGraph = graphModel.getUndirectedGraph();
        HashMap<Node, Integer> indicies = d.createIndiciesMap(hierarchicalUndirectedGraph);

        HashMap<String, double[]> metricsMap = (HashMap) d.calculateDistanceMetrics(graphModel.getGraph(), indicies, false, false);
        double[] betweenness = metricsMap.get(GraphDistance.BETWEENNESS);

        int index1 = indicies.get(node1);
        int index3 = indicies.get(node3);

        assertEquals(betweenness[index1], 5.);
        assertEquals(betweenness[index3], 0.2);
    }

    @Test
    public void testSpecial1UndirectedGraphCloseness() {
        GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getGraphModel();
        UndirectedGraph undirectedGraph = graphModel.getUndirectedGraph();
        Node node1 = graphModel.factory().newNode("0");
        Node node2 = graphModel.factory().newNode("1");
        Node node3 = graphModel.factory().newNode("2");
        Node node4 = graphModel.factory().newNode("3");
        Node node5 = graphModel.factory().newNode("4");
        Node node6 = graphModel.factory().newNode("5");
        Node node7 = graphModel.factory().newNode("6");
        undirectedGraph.addNode(node1);
        undirectedGraph.addNode(node2);
        undirectedGraph.addNode(node3);
        undirectedGraph.addNode(node4);
        undirectedGraph.addNode(node5);
        undirectedGraph.addNode(node6);
        undirectedGraph.addNode(node7);
        Edge edge12 = graphModel.factory().newEdge(node1, node2, false);
        Edge edge13 = graphModel.factory().newEdge(node1, node3, false);
        Edge edge14 = graphModel.factory().newEdge(node1, node4, false);
        Edge edge15 = graphModel.factory().newEdge(node1, node5, false);
        Edge edge16 = graphModel.factory().newEdge(node1, node6, false);
        Edge edge27 = graphModel.factory().newEdge(node2, node7, false);
        Edge edge37 = graphModel.factory().newEdge(node3, node7, false);
        Edge edge47 = graphModel.factory().newEdge(node4, node7, false);
        Edge edge57 = graphModel.factory().newEdge(node5, node7, false);
        Edge edge67 = graphModel.factory().newEdge(node6, node7, false);
        undirectedGraph.addEdge(edge12);
        undirectedGraph.addEdge(edge13);
        undirectedGraph.addEdge(edge14);
        undirectedGraph.addEdge(edge15);
        undirectedGraph.addEdge(edge16);
        undirectedGraph.addEdge(edge27);
        undirectedGraph.addEdge(edge37);
        undirectedGraph.addEdge(edge47);
        undirectedGraph.addEdge(edge57);
        undirectedGraph.addEdge(edge67);

        GraphDistance d = new GraphDistance();
        d.initializeStartValues();
        UndirectedGraph hierarchicalUndirectedGraph = graphModel.getUndirectedGraph();
        HashMap<Node, Integer> indicies = d.createIndiciesMap(hierarchicalUndirectedGraph);

        HashMap<String, double[]> metricsMap = (HashMap) d.calculateDistanceMetrics(graphModel.getGraph(), indicies, false, false);
        double[] closeness = metricsMap.get(GraphDistance.CLOSENESS);

        int index7 = indicies.get(node7);

        double res = 1.1667;
        double diff = 0.01;

        assertTrue(Math.abs(closeness[index7] - res) < diff);
    }

    @Test
    public void testSpecial2UndirectedGraphBetweenness() {
        GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getGraphModel();
        UndirectedGraph undirectedGraph = graphModel.getUndirectedGraph();
        Node node1 = graphModel.factory().newNode("0");
        Node node2 = graphModel.factory().newNode("1");
        Node node3 = graphModel.factory().newNode("2");
        Node node4 = graphModel.factory().newNode("3");
        Node node5 = graphModel.factory().newNode("4");
        Node node6 = graphModel.factory().newNode("5");
        Node node7 = graphModel.factory().newNode("6");
        Node node8 = graphModel.factory().newNode("7");
        undirectedGraph.addNode(node1);
        undirectedGraph.addNode(node2);
        undirectedGraph.addNode(node3);
        undirectedGraph.addNode(node4);
        undirectedGraph.addNode(node5);
        undirectedGraph.addNode(node6);
        undirectedGraph.addNode(node7);
        undirectedGraph.addNode(node8);
        Edge edge12 = graphModel.factory().newEdge(node1, node2, false);
        Edge edge13 = graphModel.factory().newEdge(node1, node3, false);
        Edge edge34 = graphModel.factory().newEdge(node3, node4, false);
        Edge edge24 = graphModel.factory().newEdge(node2, node4, false);
        Edge edge56 = graphModel.factory().newEdge(node5, node6, false);
        Edge edge67 = graphModel.factory().newEdge(node6, node7, false);
        Edge edge78 = graphModel.factory().newEdge(node7, node8, false);
        Edge edge85 = graphModel.factory().newEdge(node8, node5, false);
        Edge edge45 = graphModel.factory().newEdge(node4, node5, false);
        undirectedGraph.addEdge(edge12);
        undirectedGraph.addEdge(edge13);
        undirectedGraph.addEdge(edge34);
        undirectedGraph.addEdge(edge24);
        undirectedGraph.addEdge(edge56);
        undirectedGraph.addEdge(edge67);
        undirectedGraph.addEdge(edge78);
        undirectedGraph.addEdge(edge85);
        undirectedGraph.addEdge(edge45);

        GraphDistance d = new GraphDistance();
        d.initializeStartValues();
        UndirectedGraph hierarchicalUndirectedGraph = graphModel.getUndirectedGraph();
        HashMap<Node, Integer> indicies = d.createIndiciesMap(hierarchicalUndirectedGraph);

        HashMap<String, double[]> metricsMap = (HashMap) d.calculateDistanceMetrics(graphModel.getGraph(), indicies, false, false);
        double[] betweenness = metricsMap.get(GraphDistance.BETWEENNESS);

        Node n4 = undirectedGraph.getNode("3");
        Node n6 = undirectedGraph.getNode("5");
        Node n7 = undirectedGraph.getNode("6");

        int index4 = indicies.get(n4);
        int index6 = indicies.get(n6);
        int index7 = indicies.get(n7);

        assertEquals(betweenness[index4], 12.5);
        assertEquals(betweenness[index6], 2.5);
        assertEquals(betweenness[index7], 0.5);
    }

    @Test
    public void testSpecial2UndirectedGraphCloseness() {
        GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getGraphModel();
        UndirectedGraph undirectedGraph = graphModel.getUndirectedGraph();
        Node node1 = graphModel.factory().newNode("0");
        Node node2 = graphModel.factory().newNode("1");
        Node node3 = graphModel.factory().newNode("2");
        Node node4 = graphModel.factory().newNode("3");
        Node node5 = graphModel.factory().newNode("4");
        Node node6 = graphModel.factory().newNode("5");
        Node node7 = graphModel.factory().newNode("6");
        Node node8 = graphModel.factory().newNode("7");
        undirectedGraph.addNode(node1);
        undirectedGraph.addNode(node2);
        undirectedGraph.addNode(node3);
        undirectedGraph.addNode(node4);
        undirectedGraph.addNode(node5);
        undirectedGraph.addNode(node6);
        undirectedGraph.addNode(node7);
        undirectedGraph.addNode(node8);
        Edge edge12 = graphModel.factory().newEdge(node1, node2, false);
        Edge edge13 = graphModel.factory().newEdge(node1, node3, false);
        Edge edge34 = graphModel.factory().newEdge(node3, node4, false);
        Edge edge24 = graphModel.factory().newEdge(node2, node4, false);
        Edge edge56 = graphModel.factory().newEdge(node5, node6, false);
        Edge edge67 = graphModel.factory().newEdge(node6, node7, false);
        Edge edge78 = graphModel.factory().newEdge(node7, node8, false);
        Edge edge85 = graphModel.factory().newEdge(node8, node5, false);
        Edge edge45 = graphModel.factory().newEdge(node4, node5, false);
        undirectedGraph.addEdge(edge12);
        undirectedGraph.addEdge(edge13);
        undirectedGraph.addEdge(edge34);
        undirectedGraph.addEdge(edge24);
        undirectedGraph.addEdge(edge56);
        undirectedGraph.addEdge(edge67);
        undirectedGraph.addEdge(edge78);
        undirectedGraph.addEdge(edge85);
        undirectedGraph.addEdge(edge45);

        GraphDistance d = new GraphDistance();
        d.initializeStartValues();
        UndirectedGraph hierarchicalUndirectedGraph = graphModel.getUndirectedGraph();
        HashMap<Node, Integer> indicies = d.createIndiciesMap(hierarchicalUndirectedGraph);

        HashMap<String, double[]> metricsMap = (HashMap) d.calculateDistanceMetrics(graphModel.getGraph(), indicies, false, false);
        double[] closeness = metricsMap.get(GraphDistance.CLOSENESS);

        int index2 = indicies.get(node2);

        double res = 2.2857;
        double diff = 0.01;

        assertTrue(Math.abs(closeness[index2] - res) < diff);
    }

    @Test
    public void testSpecial3UndirectedGraphBetweenness() {
        GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getGraphModel();
        UndirectedGraph undirectedGraph = graphModel.getUndirectedGraph();
        Node node1 = graphModel.factory().newNode("0");
        Node node2 = graphModel.factory().newNode("1");
        Node node3 = graphModel.factory().newNode("2");
        Node node4 = graphModel.factory().newNode("3");
        Node node5 = graphModel.factory().newNode("4");
        Node node6 = graphModel.factory().newNode("5");
        Node node7 = graphModel.factory().newNode("6");
        undirectedGraph.addNode(node1);
        undirectedGraph.addNode(node2);
        undirectedGraph.addNode(node3);
        undirectedGraph.addNode(node4);
        undirectedGraph.addNode(node5);
        undirectedGraph.addNode(node6);
        undirectedGraph.addNode(node7);
        Edge edge12 = graphModel.factory().newEdge(node1, node2, false);
        Edge edge13 = graphModel.factory().newEdge(node1, node3, false);
        Edge edge14 = graphModel.factory().newEdge(node1, node4, false);
        Edge edge15 = graphModel.factory().newEdge(node1, node5, false);
        Edge edge16 = graphModel.factory().newEdge(node1, node6, false);
        Edge edge17 = graphModel.factory().newEdge(node1, node7, false);
        Edge edge23 = graphModel.factory().newEdge(node2, node3, false);
        Edge edge34 = graphModel.factory().newEdge(node3, node4, false);
        Edge edge45 = graphModel.factory().newEdge(node4, node5, false);
        Edge edge56 = graphModel.factory().newEdge(node5, node6, false);
        Edge edge67 = graphModel.factory().newEdge(node6, node7, false);
        Edge edge72 = graphModel.factory().newEdge(node7, node2, false);
        undirectedGraph.addEdge(edge12);
        undirectedGraph.addEdge(edge13);
        undirectedGraph.addEdge(edge14);
        undirectedGraph.addEdge(edge15);
        undirectedGraph.addEdge(edge16);
        undirectedGraph.addEdge(edge17);
        undirectedGraph.addEdge(edge23);
        undirectedGraph.addEdge(edge34);
        undirectedGraph.addEdge(edge45);
        undirectedGraph.addEdge(edge56);
        undirectedGraph.addEdge(edge67);
        undirectedGraph.addEdge(edge72);

        GraphDistance d = new GraphDistance();
        d.initializeStartValues();
        UndirectedGraph hierarchicalUndirectedGraph = graphModel.getUndirectedGraph();
        HashMap<Node, Integer> indicies = d.createIndiciesMap(hierarchicalUndirectedGraph);

        HashMap<String, double[]> metricsMap = (HashMap) d.calculateDistanceMetrics(graphModel.getGraph(), indicies, false, false);
        double[] betweenness = metricsMap.get(GraphDistance.BETWEENNESS);

        int index3 = indicies.get(node3);

        assertEquals(betweenness[index3], 0.5);
    }

    @Test
    public void testSpecial3UndirectedGraphCloseness() {
        GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getGraphModel();
        UndirectedGraph undirectedGraph = graphModel.getUndirectedGraph();
        Node node1 = graphModel.factory().newNode("0");
        Node node2 = graphModel.factory().newNode("1");
        Node node3 = graphModel.factory().newNode("2");
        Node node4 = graphModel.factory().newNode("3");
        Node node5 = graphModel.factory().newNode("4");
        Node node6 = graphModel.factory().newNode("5");
        Node node7 = graphModel.factory().newNode("6");
        undirectedGraph.addNode(node1);
        undirectedGraph.addNode(node2);
        undirectedGraph.addNode(node3);
        undirectedGraph.addNode(node4);
        undirectedGraph.addNode(node5);
        undirectedGraph.addNode(node6);
        undirectedGraph.addNode(node7);
        Edge edge12 = graphModel.factory().newEdge(node1, node2, false);
        Edge edge13 = graphModel.factory().newEdge(node1, node3, false);
        Edge edge14 = graphModel.factory().newEdge(node1, node4, false);
        Edge edge15 = graphModel.factory().newEdge(node1, node5, false);
        Edge edge16 = graphModel.factory().newEdge(node1, node6, false);
        Edge edge17 = graphModel.factory().newEdge(node1, node7, false);
        Edge edge23 = graphModel.factory().newEdge(node2, node3, false);
        Edge edge34 = graphModel.factory().newEdge(node3, node4, false);
        Edge edge45 = graphModel.factory().newEdge(node4, node5, false);
        Edge edge56 = graphModel.factory().newEdge(node5, node6, false);
        Edge edge67 = graphModel.factory().newEdge(node6, node7, false);
        Edge edge72 = graphModel.factory().newEdge(node7, node2, false);
        undirectedGraph.addEdge(edge12);
        undirectedGraph.addEdge(edge13);
        undirectedGraph.addEdge(edge14);
        undirectedGraph.addEdge(edge15);
        undirectedGraph.addEdge(edge16);
        undirectedGraph.addEdge(edge17);
        undirectedGraph.addEdge(edge23);
        undirectedGraph.addEdge(edge34);
        undirectedGraph.addEdge(edge45);
        undirectedGraph.addEdge(edge56);
        undirectedGraph.addEdge(edge67);
        undirectedGraph.addEdge(edge72);

        GraphDistance d = new GraphDistance();
        d.initializeStartValues();
        UndirectedGraph hierarchicalUndirectedGraph = graphModel.getUndirectedGraph();
        HashMap<Node, Integer> indicies = d.createIndiciesMap(hierarchicalUndirectedGraph);

        HashMap<String, double[]> metricsMap = (HashMap) d.calculateDistanceMetrics(graphModel.getGraph(), indicies, false, false);
        double[] closeness = metricsMap.get(GraphDistance.CLOSENESS);

        int index1 = indicies.get(node1);
        int index3 = indicies.get(node3);

        assertEquals(closeness[index1], 1.0);
        assertEquals(closeness[index3], 1.5);
    }

    @Test
    public void testDirectedPathGraphBetweenness() {
        GraphModel graphModel = GraphGenerator.generatePathDirectedGraph(4);

        GraphDistance d = new GraphDistance();
        d.initializeStartValues();
        DirectedGraph directedGraph = graphModel.getDirectedGraph();
        HashMap<Node, Integer> indicies = d.createIndiciesMap(graphModel.getGraph());

        HashMap<String, double[]> metricsMap = (HashMap) d.calculateDistanceMetrics(graphModel.getGraph(), indicies, true, false);
        double[] betweenness = metricsMap.get(GraphDistance.BETWEENNESS);

        Node n2 = directedGraph.getNode("1");
        int index2 = indicies.get(n2);

        assertEquals(betweenness[index2], 2.0);
    }

    @Test
    public void testDirectedPathGraphCloseness() {
        GraphModel graphModel = GraphGenerator.generatePathDirectedGraph(4);

        GraphDistance d = new GraphDistance();
        d.initializeStartValues();
        DirectedGraph directedGraph = graphModel.getDirectedGraph();
        HashMap<Node, Integer> indicies = d.createIndiciesMap(graphModel.getGraph());

        HashMap<String, double[]> metricsMap = (HashMap) d.calculateDistanceMetrics(graphModel.getGraph(), indicies, true, false);
        double[] closeness = metricsMap.get(GraphDistance.CLOSENESS);

        Node n1 = directedGraph.getNode("0");
        Node n3 = directedGraph.getNode("2");
        int index1 = indicies.get(n1);
        int index3 = indicies.get(n3);

        assertEquals(closeness[index1], 2.0);
        assertEquals(closeness[index3], 1.0);
    }

    @Test
    public void testDirectedCyclicGraphBetweenness() {
        GraphModel graphModel = GraphGenerator.generateCyclicDirectedGraph(5);

        GraphDistance d = new GraphDistance();
        d.initializeStartValues();
        DirectedGraph directedGraph = graphModel.getDirectedGraph();
        HashMap<Node, Integer> indicies = d.createIndiciesMap(graphModel.getGraph());

        HashMap<String, double[]> metricsMap = (HashMap) d.calculateDistanceMetrics(graphModel.getGraph(), indicies, true, false);
        double[] betweenness = metricsMap.get(GraphDistance.BETWEENNESS);

        Node n1 = directedGraph.getNode("0");
        Node n3 = directedGraph.getNode("2");
        int index1 = indicies.get(n1);
        int index3 = indicies.get(n3);

        assertEquals(betweenness[index1], 6.0);
        assertEquals(betweenness[index3], 6.0);
    }

    @Test
    public void testDirectedCyclicGraphCloseness() {
        GraphModel graphModel = GraphGenerator.generateCyclicDirectedGraph(5);

        GraphDistance d = new GraphDistance();
        d.initializeStartValues();
        DirectedGraph directedGraph = graphModel.getDirectedGraph();
        HashMap<Node, Integer> indicies = d.createIndiciesMap(graphModel.getGraph());

        HashMap<String, double[]> metricsMap = (HashMap) d.calculateDistanceMetrics(graphModel.getGraph(), indicies, true, false);
        double[] closeness = metricsMap.get(GraphDistance.CLOSENESS);

        Node n2 = directedGraph.getNode("1");
        int index2 = indicies.get(n2);

        assertEquals(closeness[index2], 2.5);
    }

    @Test
    public void testDirectedStarOutGraphBetweenness() {
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

        GraphDistance d = new GraphDistance();
        d.initializeStartValues();

        HashMap<Node, Integer> indicies = d.createIndiciesMap(graphModel.getGraph());

        HashMap<String, double[]> metricsMap = (HashMap) d.calculateDistanceMetrics(graphModel.getGraph(), indicies, true, false);
        double[] betweenness = metricsMap.get(GraphDistance.BETWEENNESS);

        Node n1 = directedGraph.getNode("0");
        Node n5 = directedGraph.getNode("4");
        int index1 = indicies.get(n1);
        int index5 = indicies.get(n5);

        assertEquals(betweenness[index1], 0.0);
        assertEquals(betweenness[index5], 0.0);
    }

    @Test
    public void testDirectedStarOutGraphCloseness() {
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

        GraphDistance d = new GraphDistance();
        d.initializeStartValues();

        HashMap<Node, Integer> indicies = d.createIndiciesMap(graphModel.getGraph());

        HashMap<String, double[]> metricsMap = (HashMap) d.calculateDistanceMetrics(graphModel.getGraph(), indicies, true, false);
        double[] closeness = metricsMap.get(GraphDistance.CLOSENESS);

        Node n1 = directedGraph.getNode("0");
        Node n6 = directedGraph.getNode("5");
        int index1 = indicies.get(n1);
        int index6 = indicies.get(n6);

        assertEquals(closeness[index1], 1.0);
        assertEquals(closeness[index6], 0.0);
    }

    @Test
    public void testSpecial1DirectedGraphBetweenness() {
        GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getGraphModel();
        DirectedGraph directedGraph = graphModel.getDirectedGraph();
        Node node1 = graphModel.factory().newNode("0");
        Node node2 = graphModel.factory().newNode("1");
        Node node3 = graphModel.factory().newNode("2");
        Node node4 = graphModel.factory().newNode("3");
        Node node5 = graphModel.factory().newNode("4");
        directedGraph.addNode(node1);
        directedGraph.addNode(node2);
        directedGraph.addNode(node3);
        directedGraph.addNode(node4);
        directedGraph.addNode(node5);
        Edge edge13 = graphModel.factory().newEdge(node1, node3);
        Edge edge32 = graphModel.factory().newEdge(node3, node2);
        Edge edge21 = graphModel.factory().newEdge(node2, node1);
        Edge edge34 = graphModel.factory().newEdge(node3, node4);
        Edge edge45 = graphModel.factory().newEdge(node4, node5);
        Edge edge53 = graphModel.factory().newEdge(node5, node3);
        directedGraph.addEdge(edge13);
        directedGraph.addEdge(edge32);
        directedGraph.addEdge(edge21);
        directedGraph.addEdge(edge34);
        directedGraph.addEdge(edge45);
        directedGraph.addEdge(edge53);

        GraphDistance d = new GraphDistance();
        d.initializeStartValues();

        HashMap<Node, Integer> indicies = d.createIndiciesMap(graphModel.getGraph());

        HashMap<String, double[]> metricsMap = (HashMap) d.calculateDistanceMetrics(graphModel.getGraph(), indicies, true, false);
        double[] betweenness = metricsMap.get(GraphDistance.BETWEENNESS);

        int index1 = indicies.get(node1);
        int index3 = indicies.get(node3);
        int index4 = indicies.get(node4);

        assertEquals(betweenness[index1], 3.0);
        assertEquals(betweenness[index3], 10.0);
        assertEquals(betweenness[index4], 3.0);
    }

    @Test
    public void testSpecial1DirectedGraphCloseness() {
        GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getGraphModel();
        DirectedGraph directedGraph = graphModel.getDirectedGraph();
        Node node1 = graphModel.factory().newNode("0");
        Node node2 = graphModel.factory().newNode("1");
        Node node3 = graphModel.factory().newNode("2");
        Node node4 = graphModel.factory().newNode("3");
        Node node5 = graphModel.factory().newNode("4");
        directedGraph.addNode(node1);
        directedGraph.addNode(node2);
        directedGraph.addNode(node3);
        directedGraph.addNode(node4);
        directedGraph.addNode(node5);
        Edge edge13 = graphModel.factory().newEdge(node1, node3);
        Edge edge32 = graphModel.factory().newEdge(node3, node2);
        Edge edge21 = graphModel.factory().newEdge(node2, node1);
        Edge edge34 = graphModel.factory().newEdge(node3, node4);
        Edge edge45 = graphModel.factory().newEdge(node4, node5);
        Edge edge53 = graphModel.factory().newEdge(node5, node3);
        directedGraph.addEdge(edge13);
        directedGraph.addEdge(edge32);
        directedGraph.addEdge(edge21);
        directedGraph.addEdge(edge34);
        directedGraph.addEdge(edge45);
        directedGraph.addEdge(edge53);

        GraphDistance d = new GraphDistance();
        d.initializeStartValues();

        HashMap<Node, Integer> indicies = d.createIndiciesMap(graphModel.getGraph());

        HashMap<String, double[]> metricsMap = (HashMap) d.calculateDistanceMetrics(graphModel.getGraph(), indicies, true, false);
        double[] closeness = metricsMap.get(GraphDistance.CLOSENESS);

        int index2 = indicies.get(node2);
        int index3 = indicies.get(node3);

        assertEquals(closeness[index2], 2.5);
        assertEquals(closeness[index3], 1.5);
    }

    @Test
    public void testConnectedComponentsUndirectedGraphCloseness() {
        //expected that values are computed separatly for every connected component
        GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getGraphModel();
        UndirectedGraph undirectedGraph = graphModel.getUndirectedGraph();
        Node node1 = graphModel.factory().newNode("0");
        Node node2 = graphModel.factory().newNode("1");
        Node node3 = graphModel.factory().newNode("2");
        Node node4 = graphModel.factory().newNode("3");
        Node node5 = graphModel.factory().newNode("4");
        undirectedGraph.addNode(node1);
        undirectedGraph.addNode(node2);
        undirectedGraph.addNode(node3);
        undirectedGraph.addNode(node4);
        undirectedGraph.addNode(node5);
        Edge edge12 = graphModel.factory().newEdge(node1, node2, false);
        Edge edge23 = graphModel.factory().newEdge(node2, node3, false);
        Edge edge45 = graphModel.factory().newEdge(node4, node5, false);
        undirectedGraph.addEdge(edge12);
        undirectedGraph.addEdge(edge23);
        undirectedGraph.addEdge(edge45);

        GraphDistance d = new GraphDistance();
        d.initializeStartValues();
        UndirectedGraph hierarchicalUndirectedGraph = graphModel.getUndirectedGraph();
        HashMap<Node, Integer> indicies = d.createIndiciesMap(hierarchicalUndirectedGraph);

        HashMap<String, double[]> metricsMap = (HashMap) d.calculateDistanceMetrics(graphModel.getGraph(), indicies, false, false);
        double[] closeness = metricsMap.get(GraphDistance.CLOSENESS);

        int index1 = indicies.get(node1);
        int index4 = indicies.get(node4);

        assertEquals(closeness[index1], 1.5);
        assertEquals(closeness[index4], 1.);
    }
}
