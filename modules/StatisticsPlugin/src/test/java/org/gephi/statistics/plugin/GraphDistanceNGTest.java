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
import org.gephi.graph.api.HierarchicalGraph;
import org.gephi.graph.api.HierarchicalUndirectedGraph;
import org.gephi.graph.api.Node;
import org.gephi.io.generator.plugin.GraphGenerator;
import org.gephi.project.api.ProjectController;
import org.gephi.project.impl.ProjectControllerImpl;
import org.openide.util.Lookup;
import static org.testng.Assert.*;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 * @author Anna
 */
public class GraphDistanceNGTest {
    
   private ProjectController pc;
    private GraphGenerator generator;
    
    @BeforeClass
    public void setUp() {
      pc = Lookup.getDefault().lookup(ProjectControllerImpl.class);
      generator=Lookup.getDefault().lookup(GraphGenerator.class);
    }
    
    @Test
    public void testOneNodeAvPathLength() {
        pc.newProject();
        GraphModel graphModel=generator.generateNullUndirectedGraph(1);

        GraphDistance d = new GraphDistance();
        d.initializeStartValues();
        HierarchicalUndirectedGraph undirectedGraph = graphModel.getHierarchicalUndirectedGraph();
        HashMap<Node, Integer> indicies = d.createIndiciesMap(undirectedGraph);
        
        d.calculateDistanceMetrics(graphModel.getHierarchicalGraph(), indicies, false, false);

        double averageDegree = d.getPathLength();
        assertEquals(averageDegree, 0.0);
    }
    
    @Test
    public void testTwoConnectrdNodesAvPathLength() {
        pc.newProject();
        GraphModel graphModel=generator.generatePathUndirectedGraph(2);

        GraphDistance d = new GraphDistance();
        d.initializeStartValues();
        HierarchicalUndirectedGraph undirectedGraph = graphModel.getHierarchicalUndirectedGraph();
        HashMap<Node, Integer> indicies = d.createIndiciesMap(undirectedGraph);
        
        d.calculateDistanceMetrics(graphModel.getHierarchicalGraph(), indicies, false, false);

        double averageDegree = d.getPathLength();
        assertEquals(averageDegree, 1.0);
    }
    
     @Test
    public void testNullGraphAvPathLength() {
        pc.newProject();
        GraphModel graphModel=generator.generateNullUndirectedGraph(5);

        GraphDistance d = new GraphDistance();
        d.initializeStartValues();
        HierarchicalUndirectedGraph undirectedGraph = graphModel.getHierarchicalUndirectedGraph();
        HashMap<Node, Integer> indicies = d.createIndiciesMap(undirectedGraph);
        
        d.calculateDistanceMetrics(graphModel.getHierarchicalGraph(), indicies, false, false);

        double averageDegree = d.getPathLength();
        assertEquals(averageDegree, 0.0);
    }
     
     @Test
    public void testCompleteGraphAvPathLength() {
        pc.newProject();
        GraphModel graphModel=generator.generateCompleteUndirectedGraph(5);

        GraphDistance d = new GraphDistance();
        d.initializeStartValues();
        HierarchicalUndirectedGraph undirectedGraph = graphModel.getHierarchicalUndirectedGraph();
        HashMap<Node, Integer> indicies = d.createIndiciesMap(undirectedGraph);
        
        d.calculateDistanceMetrics(graphModel.getHierarchicalGraph(), indicies, false, false);

        double averageDegree = d.getPathLength();
        assertEquals(averageDegree, 1.0);
    }
     
     @Test
    public void testStarGraphAvPathLength() {
        pc.newProject();
        GraphModel graphModel=generator.generateStarUndirectedGraph(5);;

        GraphDistance d = new GraphDistance();
        d.initializeStartValues();
        HierarchicalUndirectedGraph undirectedGraph = graphModel.getHierarchicalUndirectedGraph();
        HashMap<Node, Integer> indicies = d.createIndiciesMap(undirectedGraph);
        
        d.calculateDistanceMetrics(graphModel.getHierarchicalGraph(), indicies, false, false);

        double averageDegree = d.getPathLength();
        double res = 1.6667;
        double diff = Math.abs(averageDegree - res);
        assertTrue(diff<0.01);
    }
     
     @Test
    public void testCyclicGraphAvPathLength() {
        pc.newProject();
        GraphModel graphModel=generator.generateCyclicUndirectedGraph(5);

        GraphDistance d = new GraphDistance();
        d.initializeStartValues();
        HierarchicalUndirectedGraph undirectedGraph = graphModel.getHierarchicalUndirectedGraph();
        HashMap<Node, Integer> indicies = d.createIndiciesMap(undirectedGraph);
        
        d.calculateDistanceMetrics(graphModel.getHierarchicalGraph(), indicies, false, false);

        double averageDegree = d.getPathLength();
        assertEquals(averageDegree, 1.5);
    }
     
     @Test
    public void testDirectedPathGraphAvPathLength() {
        pc.newProject();
        GraphModel graphModel=generator.generatePathDirectedGraph(4);

        GraphDistance d = new GraphDistance();
        d.initializeStartValues();
        HierarchicalUndirectedGraph undirectedGraph = graphModel.getHierarchicalUndirectedGraph();
        HashMap<Node, Integer> indicies = d.createIndiciesMap(undirectedGraph);
        
        d.calculateDistanceMetrics(graphModel.getHierarchicalGraph(), indicies, true, false);

        double averageDegree = d.getPathLength();
        double res = 1.6667;
        double diff = Math.abs(averageDegree - res);
        assertTrue(diff<0.01);
    }
     
      @Test
    public void testDirectedCyclicGraphAvPathLength() {
        pc.newProject();
        GraphModel graphModel=generator.generateCyclicDirectedGraph(5);

        GraphDistance d = new GraphDistance();
        d.initializeStartValues();
        HierarchicalUndirectedGraph undirectedGraph = graphModel.getHierarchicalUndirectedGraph();
        HashMap<Node, Integer> indicies = d.createIndiciesMap(undirectedGraph);
        
        d.calculateDistanceMetrics(graphModel.getHierarchicalGraph(), indicies, true, false);

        double averageDegree = d.getPathLength();
        assertEquals(averageDegree, 2.5);
    }
      
      @Test 
    public void testDirectedStarOutGraphAvPathLength(){
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
        
        GraphDistance d = new GraphDistance();
        d.initializeStartValues();
        HierarchicalUndirectedGraph undirectedGraph = graphModel.getHierarchicalUndirectedGraph();
        HashMap<Node, Integer> indicies = d.createIndiciesMap(undirectedGraph);
        
        d.calculateDistanceMetrics(graphModel.getHierarchicalGraph(), indicies, true, false);

        double averageDegree = d.getPathLength();
        assertEquals(averageDegree, 1.0);
    }
      
      @Test 
    public void testDirectedSpecial1GraphAvPathLength(){
        pc.newProject();
        GraphModel graphModel=Lookup.getDefault().lookup(GraphController.class).getModel();
        
        DirectedGraph directedGraph=graphModel.getDirectedGraph();
        Node node1=graphModel.factory().newNode("0");
        Node node2=graphModel.factory().newNode("1");
        Node node3=graphModel.factory().newNode("2");
        Node node4=graphModel.factory().newNode("3");
        Node node5=graphModel.factory().newNode("4");
        
        directedGraph.addNode(node1);
        directedGraph.addNode(node2);
        directedGraph.addNode(node3);
        directedGraph.addNode(node4);
        directedGraph.addNode(node5);
        
        Edge edge15=graphModel.factory().newEdge(node1, node5);
        Edge edge52=graphModel.factory().newEdge(node5, node2);
        Edge edge53=graphModel.factory().newEdge(node5, node3);
        Edge edge45=graphModel.factory().newEdge(node4, node5);
        
        directedGraph.addEdge(edge15);
        directedGraph.addEdge(edge52);
        directedGraph.addEdge(edge53);
        directedGraph.addEdge(edge45);
        
        GraphDistance d = new GraphDistance();
        d.initializeStartValues();
        HierarchicalUndirectedGraph undirectedGraph = graphModel.getHierarchicalUndirectedGraph();
        HashMap<Node, Integer> indicies = d.createIndiciesMap(undirectedGraph);
        
        d.calculateDistanceMetrics(graphModel.getHierarchicalGraph(), indicies, true, false);

        double averageDegree = d.getPathLength();
        assertEquals(averageDegree, 1.5);
    }
      
      @Test
    public void testOneNodeDiameter() {
        pc.newProject();
        GraphModel graphModel=generator.generateNullUndirectedGraph(1);

        GraphDistance d = new GraphDistance();
        d.initializeStartValues();
        HierarchicalUndirectedGraph undirectedGraph = graphModel.getHierarchicalUndirectedGraph();
        HashMap<Node, Integer> indicies = d.createIndiciesMap(undirectedGraph);
        
        d.calculateDistanceMetrics(graphModel.getHierarchicalGraph(), indicies, false, false);

        double diameter = d.getDiameter();
        assertEquals(diameter, 0.0);
    }
      
       @Test
    public void testTwoConnectrdNodesDiameter() {
        pc.newProject();
        GraphModel graphModel=generator.generatePathUndirectedGraph(2);

        GraphDistance d = new GraphDistance();
        d.initializeStartValues();
        HierarchicalUndirectedGraph undirectedGraph = graphModel.getHierarchicalUndirectedGraph();
        HashMap<Node, Integer> indicies = d.createIndiciesMap(undirectedGraph);
        
        d.calculateDistanceMetrics(graphModel.getHierarchicalGraph(), indicies, false, false);

        double diameter = d.getDiameter();
        assertEquals(diameter, 1.0);
    }
    
     @Test
    public void testNullGraphDiameter() {
        pc.newProject();
        GraphModel graphModel=generator.generateNullUndirectedGraph(5);

        GraphDistance d = new GraphDistance();
        d.initializeStartValues();
        HierarchicalUndirectedGraph undirectedGraph = graphModel.getHierarchicalUndirectedGraph();
        HashMap<Node, Integer> indicies = d.createIndiciesMap(undirectedGraph);
        
        d.calculateDistanceMetrics(graphModel.getHierarchicalGraph(), indicies, false, false);

        double diameter = d.getDiameter();
        assertEquals(diameter, 0.0);
    }
     
     @Test
    public void testCompleteGraphDiameter() {
        pc.newProject();
        GraphModel graphModel=generator.generateCompleteUndirectedGraph(5);

        GraphDistance d = new GraphDistance();
        d.initializeStartValues();
        HierarchicalUndirectedGraph undirectedGraph = graphModel.getHierarchicalUndirectedGraph();
        HashMap<Node, Integer> indicies = d.createIndiciesMap(undirectedGraph);
        
        d.calculateDistanceMetrics(graphModel.getHierarchicalGraph(), indicies, false, false);

        double diameter = d.getDiameter();
        assertEquals(diameter, 1.0);
    }
     
     @Test
    public void testCyclicGraphDiameter() {
        pc.newProject();
        GraphModel graphModel=generator.generateCyclicUndirectedGraph(5);

        GraphDistance d = new GraphDistance();
        d.initializeStartValues();
        HierarchicalUndirectedGraph undirectedGraph = graphModel.getHierarchicalUndirectedGraph();
        HashMap<Node, Integer> indicies = d.createIndiciesMap(undirectedGraph);
        
        d.calculateDistanceMetrics(graphModel.getHierarchicalGraph(), indicies, false, false);

        double diameter = d.getDiameter();
        assertEquals(diameter, 2.0);
    }
     
     
     @Test
    public void testSpecial4DirectedGraphConnectedComponents() {
        pc.newProject();
        GraphModel graphModel=Lookup.getDefault().lookup(GraphController.class).getModel();
        UndirectedGraph undirectedGraph=graphModel.getUndirectedGraph();
        Node node1=graphModel.factory().newNode("0");
        Node node2=graphModel.factory().newNode("1");
        Node node3=graphModel.factory().newNode("2");
        Node node4=graphModel.factory().newNode("3");
        Node node5=graphModel.factory().newNode("4");
        Node node6=graphModel.factory().newNode("5");
        Node node7=graphModel.factory().newNode("6");
        Node node8=graphModel.factory().newNode("7");
        undirectedGraph.addNode(node1);
        undirectedGraph.addNode(node2);
        undirectedGraph.addNode(node3);
        undirectedGraph.addNode(node4);
        undirectedGraph.addNode(node5);
        undirectedGraph.addNode(node6);
        undirectedGraph.addNode(node7);
        undirectedGraph.addNode(node8);
        Edge edge12=graphModel.factory().newEdge(node1, node2);
        Edge edge23=graphModel.factory().newEdge(node2, node3);
        Edge edge34=graphModel.factory().newEdge(node3, node4);
        Edge edge41=graphModel.factory().newEdge(node4, node1);
        Edge edge56=graphModel.factory().newEdge(node5, node6);
        Edge edge67=graphModel.factory().newEdge(node6, node7);
        Edge edge78=graphModel.factory().newEdge(node7, node8);
        Edge edge85=graphModel.factory().newEdge(node8, node5);
        Edge edge45=graphModel.factory().newEdge(node4, node5);
        undirectedGraph.addEdge(edge12);
        undirectedGraph.addEdge(edge23);
        undirectedGraph.addEdge(edge34);
        undirectedGraph.addEdge(edge41);
        undirectedGraph.addEdge(edge56);
        undirectedGraph.addEdge(edge67);
        undirectedGraph.addEdge(edge78);
        undirectedGraph.addEdge(edge85);
        undirectedGraph.addEdge(edge45);
        

        GraphDistance d = new GraphDistance();
        d.initializeStartValues();
        HierarchicalUndirectedGraph hierarchicalUndirectedGraph = graphModel.getHierarchicalUndirectedGraph();
        HashMap<Node, Integer> indicies = d.createIndiciesMap(hierarchicalUndirectedGraph);
        
        d.calculateDistanceMetrics(graphModel.getHierarchicalGraph(), indicies, false, false);

        double diameter = d.getDiameter();
        assertEquals(diameter, 5.0);
    }
     
    
    
}