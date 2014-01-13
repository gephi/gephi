/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.statistics.plugin;

import java.util.HashMap;
import java.util.LinkedList;
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
public class HitsNGTest {

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
    public void testOneNodeHits() {
        GraphModel graphModel = GraphGenerator.generateNullUndirectedGraph(1);
        Graph hgraph = graphModel.getUndirectedGraph();

        Hits hit = new Hits();

        double[] authority = new double[1];
        double[] hubs = new double[1];

        HashMap<Node, Integer> indicies = hit.createIndiciesMap(hgraph);

        hit.calculateHits(hgraph, hubs, authority, indicies, false, 0.01);

        Node n1 = hgraph.getNode("0");
        int index = indicies.get(n1);
        double hub1 = hubs[index];
        double auth1 = authority[index];

        assertEquals(hub1, 1.0);
        assertEquals(auth1, 1.0);
    }

    @Test
    public void testTwoConnectedNodesHits() {
        GraphModel graphModel = GraphGenerator.generateCompleteUndirectedGraph(2);
        Graph hgraph = graphModel.getUndirectedGraph();

        Hits hit = new Hits();

        double[] authority = new double[2];
        double[] hubs = new double[2];

        HashMap<Node, Integer> indicies = hit.createIndiciesMap(hgraph);

        hit.calculateHits(hgraph, hubs, authority, indicies, false, 0.01);

        Node n1 = hgraph.getNode("0");
        Node n2 = hgraph.getNode("1");
        int index1 = indicies.get(n1);
        int index2 = indicies.get(n2);
        double hub1 = hubs[index1];
        double auth2 = authority[index2];

        assertEquals(hub1, 0.5);
        assertEquals(auth2, 0.5);
    }

    @Test
    public void testNullGraphHits() {
        GraphModel graphModel = GraphGenerator.generateNullUndirectedGraph(5);
        Graph hgraph = graphModel.getUndirectedGraph();

        Hits hit = new Hits();

        double[] authority = new double[5];
        double[] hubs = new double[5];

        HashMap<Node, Integer> indicies = hit.createIndiciesMap(hgraph);

        hit.calculateHits(hgraph, hubs, authority,indicies, false, 0.01);

        Node n2 = hgraph.getNode("1");
        Node n3 = hgraph.getNode("2");
        int index2 = indicies.get(n2);
        int index3 = indicies.get(n3);
        double hub2 = hubs[index2];
        double auth3 = authority[index3];

        assertEquals(hub2, 0.2);
        assertEquals(auth3, 0.2);
    }

    @Test
    public void testCompleteGraphHits() {
        GraphModel graphModel = GraphGenerator.generateCompleteUndirectedGraph(5);
        Graph hgraph = graphModel.getUndirectedGraph();

        Hits hit = new Hits();

        double[] authority = new double[5];
        double[] hubs = new double[5];

        HashMap<Node, Integer> indicies = hit.createIndiciesMap(hgraph);

        hit.calculateHits(hgraph, hubs, authority, indicies, false, 0.01);

        Node n1 = hgraph.getNode("0");
        Node n5 = hgraph.getNode("4");
        int index1 = indicies.get(n1);
        int index5 = indicies.get(n5);
        double hub1 = hubs[index1];
        double auth5 = authority[index5];

        assertEquals(hub1, 0.2);
        assertEquals(auth5, 0.2);
    }

    @Test
    public void testStarGraphHits() {
        GraphModel graphModel = GraphGenerator.generateStarUndirectedGraph(5);
        Graph hgraph = graphModel.getUndirectedGraph();

        Hits hit = new Hits();

        double[] authority = new double[6];
        double[] hubs = new double[6];

        HashMap<Node, Integer> indicies = hit.createIndiciesMap(hgraph);

        hit.calculateHits(hgraph, hubs, authority, indicies, false, 0.01);

        Node n1 = hgraph.getNode("0");
        Node n3 = hgraph.getNode("2");
        Node n4 = hgraph.getNode("3");
        int index1 = indicies.get(n1);
        int index3 = indicies.get(n3);
        int index4 = indicies.get(n4);

        double hub1 = hubs[index1];
        double hub3 = hubs[index3];
        double auth1 = authority[index1];
        double auth4 = authority[index4];

        boolean b1 = hub1 > hub3;
        boolean b2 = auth1 > auth4;

        assertTrue(b1);
        assertTrue(b2);
    }

    @Test
    public void testGraphWithSelfLoopsHits() {
        GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getGraphModel();

        UndirectedGraph undirectedGraph = graphModel.getUndirectedGraph();
        Node node1 = graphModel.factory().newNode("0");
        Node node2 = graphModel.factory().newNode("1");
        Node node3 = graphModel.factory().newNode("2");

        undirectedGraph.addNode(node1);
        undirectedGraph.addNode(node2);
        undirectedGraph.addNode(node3);

        Edge edge12 = graphModel.factory().newEdge(node1, node2, false);
        Edge edge23 = graphModel.factory().newEdge(node2, node3, false);
        Edge edge11 = graphModel.factory().newEdge(node1, node1, false);
        Edge edge33 = graphModel.factory().newEdge(node3, node3, false);

        undirectedGraph.addEdge(edge12);
        undirectedGraph.addEdge(edge23);
        undirectedGraph.addEdge(edge11);
        undirectedGraph.addEdge(edge33);

        Graph hgraph = graphModel.getUndirectedGraph();

        Hits hit = new Hits();

        HashMap<Node, Integer> indicies = hit.createIndiciesMap(hgraph);

        double[] authority = new double[3];
        double[] hubs = new double[3];

        hit.calculateHits(hgraph, hubs, authority, indicies, false, 0.01);

        Node n1 = hgraph.getNode("0");
        Node n2 = hgraph.getNode("1");
        int index1 = indicies.get(n1);
        int index2 = indicies.get(n2);

        double hub1 = hubs[index1];
        double hub2 = hubs[index2];

        boolean b1 = hub2 > hub1;

        assertTrue(b1);

    }

    @Test
    public void testDirectedSpecial1GraphHits() {
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

        Edge edge14 = graphModel.factory().newEdge(node1, node4);
        Edge edge15 = graphModel.factory().newEdge(node1, node5);
        Edge edge24 = graphModel.factory().newEdge(node2, node4);
        Edge edge25 = graphModel.factory().newEdge(node2, node5);
        Edge edge34 = graphModel.factory().newEdge(node3, node4);
        Edge edge35 = graphModel.factory().newEdge(node3, node5);

        directedGraph.addEdge(edge14);
        directedGraph.addEdge(edge15);
        directedGraph.addEdge(edge24);
        directedGraph.addEdge(edge25);
        directedGraph.addEdge(edge34);
        directedGraph.addEdge(edge35);

        DirectedGraph hgraph = graphModel.getDirectedGraph();
        Hits hit = new Hits();

        double[] authority = new double[5];
        double[] hubs = new double[5];

        HashMap<Node, Integer> indicies = hit.createIndiciesMap(hgraph);

        hit.calculateHits(hgraph, hubs, authority, indicies, true, 0.01);

        Node n1 = hgraph.getNode("0");
        Node n2 = hgraph.getNode("1");
        Node n4 = hgraph.getNode("3");
        Node n5 = hgraph.getNode("4");

        int index1 = indicies.get(n1);
        int index2 = indicies.get(n2);
        int index4 = indicies.get(n4);
        int index5 = indicies.get(n5);

        double hub1 = hubs[index1];
        double hub4 = hubs[index4];
        double auth2 = authority[index2];
        double auth5 = authority[index5];

        double res = 0.333;
        double diff = 0.01;

        assertTrue(Math.abs(hub1 - res) < diff);
        assertEquals(hub4, 0.);
        assertEquals(auth2, 0.);
        assertEquals(auth5, 0.5);
    }

    @Test
    public void testDirectedStarOutGraphHits() {
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

        DirectedGraph hgraph = graphModel.getDirectedGraph();

        Hits hit = new Hits();

        double[] authority = new double[6];
        double[] hubs = new double[6];

        HashMap<Node, Integer> indicies = hit.createIndiciesMap(hgraph);

        hit.calculateHits(hgraph, hubs, authority, indicies, true, 0.01);

        Node n1 = hgraph.getNode("0");
        Node n3 = hgraph.getNode("2");

        int index1 = indicies.get(n1);
        int index3 = indicies.get(n3);

        double hub1 = hubs[index1];
        double hub3 = hubs[index3];
        double auth1 = authority[index1];
        double auth3 = authority[index3];

        double res = 0.146;
        double diff = 0.01;

        assertEquals(hub1, 1.);
        assertEquals(auth1, 0.);
        assertEquals(hub3, 0.);
        assertEquals(auth3, 0.2);
    }

    @Test
    public void testDirectedSpecial2GraphHits() {
        GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getGraphModel();

        DirectedGraph directedGraph = graphModel.getDirectedGraph();
        Node node1 = graphModel.factory().newNode("0");
        Node node2 = graphModel.factory().newNode("1");
        Node node3 = graphModel.factory().newNode("2");
        Node node4 = graphModel.factory().newNode("3");
        Node node5 = graphModel.factory().newNode("4");
        Node node6 = graphModel.factory().newNode("5");

        directedGraph.addNode(node1);
        directedGraph.addNode(node2);
        directedGraph.addNode(node3);
        directedGraph.addNode(node4);
        directedGraph.addNode(node5);
        directedGraph.addNode(node6);

        Edge edge21 = graphModel.factory().newEdge(node2, node1);
        Edge edge31 = graphModel.factory().newEdge(node3, node1);
        Edge edge41 = graphModel.factory().newEdge(node4, node1);
        Edge edge51 = graphModel.factory().newEdge(node5, node1);
        Edge edge36 = graphModel.factory().newEdge(node3, node6);
        Edge edge46 = graphModel.factory().newEdge(node4, node6);
        Edge edge56 = graphModel.factory().newEdge(node5, node6);

        directedGraph.addEdge(edge21);
        directedGraph.addEdge(edge31);
        directedGraph.addEdge(edge41);
        directedGraph.addEdge(edge51);
        directedGraph.addEdge(edge36);
        directedGraph.addEdge(edge46);
        directedGraph.addEdge(edge56);

        DirectedGraph hgraph = graphModel.getDirectedGraph();
        Hits hit = new Hits();

        double[] authority = new double[6];
        double[] hubs = new double[6];

        HashMap<Node, Integer> indicies = hit.createIndiciesMap(hgraph);

        hit.calculateHits(hgraph, hubs, authority, indicies, true, 0.01);

        int index1 = indicies.get(node1);
        int index2 = indicies.get(node2);
        int index3 = indicies.get(node3);
        int index5 = indicies.get(node5);
        int index6 = indicies.get(node6);

        double hub2 = hubs[index2];
        double hub3 = hubs[index3];
        double hub5 = hubs[index5];
        double hub6 = hubs[index6];
        double auth1 = authority[index1];
        double auth3 = authority[index3];
        double auth6 = authority[index6];

        assertEquals(hub3, hub5);
        assertTrue(hub3 > hub2);
        assertTrue(auth1 > auth6);
        assertEquals(hub6, 0.);
        assertEquals(auth3, 0.);
    }

    @Test
    public void testDirectedSpecial3GraphHits() {
        GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getGraphModel();

        DirectedGraph directedGraph = graphModel.getDirectedGraph();
        Node node1 = graphModel.factory().newNode("0");
        Node node2 = graphModel.factory().newNode("1");
        Node node3 = graphModel.factory().newNode("2");
        Node node4 = graphModel.factory().newNode("3");
        Node node5 = graphModel.factory().newNode("4");
        Node node6 = graphModel.factory().newNode("5");

        directedGraph.addNode(node1);
        directedGraph.addNode(node2);
        directedGraph.addNode(node3);
        directedGraph.addNode(node4);
        directedGraph.addNode(node5);
        directedGraph.addNode(node6);

        Edge edge15 = graphModel.factory().newEdge(node1, node5);
        Edge edge25 = graphModel.factory().newEdge(node2, node5);
        Edge edge35 = graphModel.factory().newEdge(node3, node5);
        Edge edge45 = graphModel.factory().newEdge(node4, node5);
        Edge edge56 = graphModel.factory().newEdge(node5, node6);

        directedGraph.addEdge(edge15);
        directedGraph.addEdge(edge25);
        directedGraph.addEdge(edge35);
        directedGraph.addEdge(edge45);
        directedGraph.addEdge(edge56);

        DirectedGraph hgraph = graphModel.getDirectedGraph();
        Hits hit = new Hits();

        double[] authority = new double[6];
        double[] hubs = new double[6];

        HashMap<Node, Integer> indicies = hit.createIndiciesMap(hgraph);

        hit.calculateHits(hgraph, hubs, authority, indicies, true, 0.01);

        int index1 = indicies.get(node1);
        int index3 = indicies.get(node3);
        int index5 = indicies.get(node5);
        int index6 = indicies.get(node6);

        double hub1 = hubs[index1];
        double hub3 = hubs[index3];
        double hub5 = hubs[index5];
        double auth5 = authority[index5];
        double auth6 = authority[index6];

        assertEquals(hub1, hub3);
        assertTrue(hub1 > hub5);
        assertTrue(auth5 > auth6);
    }

}
