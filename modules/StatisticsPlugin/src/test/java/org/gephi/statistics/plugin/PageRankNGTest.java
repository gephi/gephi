/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.statistics.plugin;

import java.util.HashMap;
import java.util.LinkedList;
import org.gephi.graph.api.DirectedGraph;
import org.gephi.graph.api.UndirectedGraph;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.HierarchicalDirectedGraph;
import org.gephi.graph.api.HierarchicalUndirectedGraph;
import org.gephi.graph.api.HierarchicalGraph;
import org.gephi.graph.api.Node;
import org.gephi.io.generator.plugin.GraphGenerator;
import org.gephi.project.api.ProjectController;
import org.gephi.project.impl.ProjectControllerImpl;
import org.gephi.io.generator.plugin.GraphGenerator;
import org.openide.util.Lookup;
import static org.testng.Assert.*;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 * @author Anna
 */
public class PageRankNGTest {
    
    private ProjectController pc;
    private GraphGenerator generator;
    
    @BeforeClass
    public void setUp() {
      pc = Lookup.getDefault().lookup(ProjectControllerImpl.class);
      generator=Lookup.getDefault().lookup(GraphGenerator.class);
    }
    
    @Test
    public void testOneNodePageRank() {
        pc.newProject();
        GraphModel graphModel=generator.generateNullUndirectedGraph(1);
        HierarchicalGraph hgraph = graphModel.getHierarchicalUndirectedGraph();

        PageRank pr = new PageRank();
        
        double[] pageRank;
        
        HashMap<Node, Integer> indicies = pr.createIndiciesMap(hgraph);
        
        pageRank = pr.calculatePagerank(hgraph, indicies, false, false, 0.001, 0.85);
        
        Node n1 = hgraph.getNode("0");
        int index = indicies.get(n1);
        double pr1 = pageRank[index];
        
        assertEquals(pr1, 1.0);
    }
    
    @Test
    public void testTwoConnectedNodesPageRank() {
        pc.newProject();
        GraphModel graphModel=generator.generatePathUndirectedGraph(2);
        HierarchicalGraph hgraph = graphModel.getHierarchicalUndirectedGraph();

        PageRank pr = new PageRank();
        
        double[] pageRank;
        
        HashMap<Node, Integer> indicies = pr.createIndiciesMap(hgraph);
        
        pageRank = pr.calculatePagerank(hgraph, indicies, false, false, 0.001, 0.85);
        
        Node n2 = hgraph.getNode("1");
        int index = indicies.get(n2);
        double pr2 = pageRank[index];
        
        assertEquals(pr2, 0.5);
    }
    
     @Test
    public void testNullGraphPageRank() {
        pc.newProject();
        GraphModel graphModel=generator.generateNullUndirectedGraph(5);
        HierarchicalGraph hgraph = graphModel.getHierarchicalUndirectedGraph();

        PageRank pr = new PageRank();
        
        double[] pageRank;
        
        HashMap<Node, Integer> indicies = pr.createIndiciesMap(hgraph);
        
        pageRank = pr.calculatePagerank(hgraph, indicies, false, false, 0.001, 0.85);
        
        Node n1 = hgraph.getNode("0");
        Node n4 = hgraph.getNode("3");
        int index1 = indicies.get(n1);
        int index4 = indicies.get(n4);
        double pr1 = pageRank[index1];
        double pr4 = pageRank[index4];
        double res=0.2d;
        
        double diff1 = Math.abs(pr1 - res);
        double diff4 = Math.abs(pr4 - res);
        assertTrue(diff1<0.01);
        assertTrue(diff4<0.01);
        assertEquals(pr1, pr4);
    }
     
      @Test
    public void testCompleteGraphPageRank() {
        pc.newProject();
        GraphModel graphModel=generator.generateCompleteUndirectedGraph(5);
        HierarchicalGraph hgraph = graphModel.getHierarchicalUndirectedGraph();

        PageRank pr = new PageRank();
        
        double[] pageRank;
        
        HashMap<Node, Integer> indicies = pr.createIndiciesMap(hgraph);
        
        pageRank = pr.calculatePagerank(hgraph, indicies, false, false, 0.001, 0.85);
        
        Node n2 = hgraph.getNode("2");
        int index2 = indicies.get(n2);
        double pr2 = pageRank[index2];
        double res=0.2d;
        
        double diff2 = Math.abs(pr2 - res);
        assertTrue(diff2<0.01);
    }
   
    @Test
    public void testCyclicGraphPageRank() {
        pc.newProject();
        GraphModel graphModel=generator.generateCyclicUndirectedGraph(6);
        HierarchicalGraph hgraph = graphModel.getHierarchicalUndirectedGraph();

        PageRank pr = new PageRank();
        
        double[] pageRank;
        
        HashMap<Node, Integer> indicies = pr.createIndiciesMap(hgraph);
        
        pageRank = pr.calculatePagerank(hgraph, indicies, false, false, 0.001, 0.6);
        
        Node n4 = hgraph.getNode("3");
        int index4 = indicies.get(n4);
        double pr4 = pageRank[index4];
        double res=0.1667;
        
        double diff4 = Math.abs(pr4 - res);
        assertTrue(diff4<0.01);
    }
      
      @Test
    public void testStarGraphPageRank() {
        pc.newProject();
        GraphModel graphModel=generator.generateStarUndirectedGraph(5);
        HierarchicalGraph hgraph = graphModel.getHierarchicalUndirectedGraph();

        PageRank pr = new PageRank();
        
        double[] pageRank;
        
        HashMap<Node, Integer> indicies = pr.createIndiciesMap(hgraph);
        
        pageRank = pr.calculatePagerank(hgraph, indicies, false, false, 0.001, 0.6);
        
        Node n1 = hgraph.getNode("0");
        Node n2 = hgraph.getNode("1");
        Node n3 = hgraph.getNode("2");
        Node n4 = hgraph.getNode("3");
        Node n5 = hgraph.getNode("4");
        Node n6 = hgraph.getNode("5");
        
        int index1 = indicies.get(n1);
        int index2 = indicies.get(n2);
        int index3 = indicies.get(n3);
        int index4 = indicies.get(n4);
        int index5 = indicies.get(n5);
        int index6 = indicies.get(n6);
        
        
        double pr1 = pageRank[index1];
        double pr2 = pageRank[index2];
        double pr3 = pageRank[index3];
        double pr4 = pageRank[index4];
        double pr5 = pageRank[index5];
        double pr6 = pageRank[index6];
        
        boolean oneMoreThree = pr1>pr3;
        double res=1.;
        double diff=0.01;
        double sum = pr1+pr2+pr3+pr4+pr5+pr6;
        
        assertTrue(oneMoreThree);
        assertEquals(pr2, pr4);
        assertTrue(Math.abs(sum-res)<diff);
    }
   
   @Test
    public void testPathDirectedGraphPageRank() {
        pc.newProject();
        GraphModel graphModel=generator.generatePathDirectedGraph(4);
        HierarchicalGraph hgraph = graphModel.getHierarchicalDirectedGraph();

        PageRank pr = new PageRank();
        
        double[] pageRank;
        
        HashMap<Node, Integer> indicies = pr.createIndiciesMap(hgraph);
        
        pageRank = pr.calculatePagerank(hgraph, indicies, true, false, 0.001, 0.85);
        
        Node n1 = hgraph.getNode("0");
        Node n2 = hgraph.getNode("1");
        Node n3 = hgraph.getNode("2");
        Node n4 = hgraph.getNode("3");
        
        int index1 = indicies.get(n1);
        int index2 = indicies.get(n2);
        int index3 = indicies.get(n3);
        int index4 = indicies.get(n4);
        
        double pr1 = pageRank[index1];
        double pr2 = pageRank[index2];
        double pr3 = pageRank[index3];
        double pr4 = pageRank[index4];
        
        double res=1.;
        double diff = 0.01;
        double sum = pr1+pr2+pr3+pr4;
        
        assertTrue(pr1<pr2);
        assertTrue(pr2<pr4);
        assertTrue(Math.abs(sum-res)<diff);
    }
   
   @Test
    public void testCyclicDirectedGraphPageRank() {
        pc.newProject();
        GraphModel graphModel=generator.generateCyclicDirectedGraph(5);
        HierarchicalGraph hgraph = graphModel.getHierarchicalDirectedGraph();

        PageRank pr = new PageRank();
        
        double[] pageRank;
        
        HashMap<Node, Integer> indicies = pr.createIndiciesMap(hgraph);
        
        pageRank = pr.calculatePagerank(hgraph, indicies, true, false, 0.001, 0.85);
        
        Node n3 = hgraph.getNode("2");
        
        int index3 = indicies.get(n3);
        
        double pr3 = pageRank[index3];
        double res=0.2d;
        
        double diff3 = Math.abs(pr3 - res);
        assertTrue(diff3<0.01);
    }
   
   @Test 
    public void testDirectedSpecial1GraphPageRank() {
        pc.newProject();
        GraphModel graphModel=Lookup.getDefault().lookup(GraphController.class).getModel();
        
        DirectedGraph directedGraph=graphModel.getDirectedGraph();
        Node node1=graphModel.factory().newNode("0");
        Node node2=graphModel.factory().newNode("1");
        Node node3=graphModel.factory().newNode("2");
        Node node4=graphModel.factory().newNode("3");
        Node node5=graphModel.factory().newNode("4");
        Node node6=graphModel.factory().newNode("5");
        Node node7=graphModel.factory().newNode("6");
        Node node8=graphModel.factory().newNode("7");
        Node node9=graphModel.factory().newNode("8");
        
        directedGraph.addNode(node1);
        directedGraph.addNode(node2);
        directedGraph.addNode(node3);
        directedGraph.addNode(node4);
        directedGraph.addNode(node5);
        directedGraph.addNode(node6);
        directedGraph.addNode(node7);
        directedGraph.addNode(node8);
        directedGraph.addNode(node9);
        
        Edge edge12=graphModel.factory().newEdge(node1, node2);
        Edge edge23=graphModel.factory().newEdge(node2, node3);
        Edge edge31=graphModel.factory().newEdge(node3, node1);
        Edge edge14=graphModel.factory().newEdge(node1, node4);
        Edge edge45=graphModel.factory().newEdge(node4, node5);
        Edge edge51=graphModel.factory().newEdge(node5, node1);
        Edge edge16=graphModel.factory().newEdge(node1, node6);
        Edge edge67=graphModel.factory().newEdge(node6, node7);
        Edge edge71=graphModel.factory().newEdge(node7, node1);
        Edge edge18=graphModel.factory().newEdge(node1, node8);
        Edge edge89=graphModel.factory().newEdge(node8, node9);
        Edge edge91=graphModel.factory().newEdge(node9, node1);
        
        
        directedGraph.addEdge(edge12);
        directedGraph.addEdge(edge23);
        directedGraph.addEdge(edge31);
        directedGraph.addEdge(edge14);
        directedGraph.addEdge(edge45);
        directedGraph.addEdge(edge51);
        directedGraph.addEdge(edge16);
        directedGraph.addEdge(edge67);
        directedGraph.addEdge(edge71);
        directedGraph.addEdge(edge18);
        directedGraph.addEdge(edge89);
        directedGraph.addEdge(edge91);
        
        HierarchicalDirectedGraph hgraph = graphModel.getHierarchicalDirectedGraphVisible();
        PageRank pr = new PageRank();
        
        double[] pageRank;
        
        HashMap<Node, Integer> indicies = pr.createIndiciesMap(hgraph);
        
        pageRank = pr.calculatePagerank(hgraph, indicies, true, false, 0.001, 0.85);
        
        int index1 = indicies.get(node1);
        int index2 = indicies.get(node2);
        int index3 = indicies.get(node3);
        
        double pr1 = pageRank[index1];
        double pr2 = pageRank[index2];
        double pr3 = pageRank[index3];

        assertTrue(pr1>pr2);
        assertTrue(pr2<pr3);
    }
   
    @Test 
    public void testDirectedStarOutGraphPageRank() {
        pc.newProject();
        GraphModel graphModel=Lookup.getDefault().lookup(GraphController.class).getModel();
        
        DirectedGraph directedGraph=graphModel.getDirectedGraph();
        Node firstNode=graphModel.factory().newNode("0");
        directedGraph.addNode(firstNode);
        for (int i=1; i<=5; i++) {
             Node currentNode=graphModel.factory().newNode(((Integer)i).toString());
             directedGraph.addNode(currentNode);
             Edge currentEdge=graphModel.factory().newEdge(firstNode, currentNode);
             directedGraph.addEdge(currentEdge);
        }
        
        HierarchicalDirectedGraph hgraph = graphModel.getHierarchicalDirectedGraphVisible();
       PageRank pr = new PageRank();
        
        double[] pageRank;
        
        HashMap<Node, Integer> indicies = pr.createIndiciesMap(hgraph);
        
        pageRank = pr.calculatePagerank(hgraph, indicies, true, false, 0.001, 0.85);
        
        Node n1 = hgraph.getNode("0");
        Node n2 = hgraph.getNode("1");
        Node n3 = hgraph.getNode("2");
        Node n5 = hgraph.getNode("4");
        
        int index1 = indicies.get(n1);
        int index2 = indicies.get(n2);
        int index3 = indicies.get(n3);
        int index5 = indicies.get(n5);
        
        double pr1 = pageRank[index1];
        double pr2 = pageRank[index2];
        double pr3 = pageRank[index3];
        double pr5 = pageRank[index5];
        
        double res=0.146;
        double diff = 0.01;
        
        assertTrue(pr1<pr3);
        assertEquals(pr2, pr5);
        assertTrue(Math.abs(pr1-res)<diff);
    }
    
     @Test 
    public void testUndirectedWeightedGraphPageRank() {
        pc.newProject();
        GraphModel graphModel=Lookup.getDefault().lookup(GraphController.class).getModel();
        
        UndirectedGraph undirectedGraph=graphModel.getUndirectedGraph();
        Node node1=graphModel.factory().newNode("0");
        Node node2=graphModel.factory().newNode("1");
        Node node3=graphModel.factory().newNode("2");
        Node node4=graphModel.factory().newNode("3");
        Node node5=graphModel.factory().newNode("4");
        Node node6=graphModel.factory().newNode("5");
        
        undirectedGraph.addNode(node1);
        undirectedGraph.addNode(node2);
        undirectedGraph.addNode(node3);
        undirectedGraph.addNode(node4);
        undirectedGraph.addNode(node5);
        undirectedGraph.addNode(node6);
        
        Edge edge12=graphModel.factory().newEdge(node1, node2);
        Edge edge23=graphModel.factory().newEdge(node2, node3, 10, false);
        Edge edge34=graphModel.factory().newEdge(node3, node4);
        Edge edge45=graphModel.factory().newEdge(node4, node5);
        Edge edge56=graphModel.factory().newEdge(node5, node6);
        Edge edge61=graphModel.factory().newEdge(node6, node1);
        
        
        undirectedGraph.addEdge(edge12);
        undirectedGraph.addEdge(edge23);
        undirectedGraph.addEdge(edge34);
        undirectedGraph.addEdge(edge45);
        undirectedGraph.addEdge(edge56);
        undirectedGraph.addEdge(edge61);
        
        HierarchicalUndirectedGraph hgraph = graphModel.getHierarchicalUndirectedGraphVisible();
        PageRank pr = new PageRank();
        
        double[] pageRank;
        
        HashMap<Node, Integer> indicies = pr.createIndiciesMap(hgraph);
        
        pageRank = pr.calculatePagerank(hgraph, indicies, false, true, 0.001, 0.85);
        
        int index1 = indicies.get(node1);
        int index2 = indicies.get(node2);
        int index3 = indicies.get(node3);
        int index6 = indicies.get(node6);
        
        double diff = 0.01;
        
        double pr1 = pageRank[index1];
        double pr2 = pageRank[index2];
        double pr3 = pageRank[index3];
        double pr6 = pageRank[index6];

        assertTrue(Math.abs(pr2-pr3)<diff);
        assertTrue(pr1<pr2);
        assertTrue(pr1<pr6);
    }
}