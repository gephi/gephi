/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.statistics.plugin;

import org.gephi.graph.api.DirectedGraph;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.HierarchicalDirectedGraph;
import org.gephi.graph.api.HierarchicalGraph;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.UndirectedGraph;
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
public class GraphDensityNGTest {
    private ProjectController pc;
    private GraphGenerator generator;
    
    @BeforeClass
    public void setUp() {
      pc = Lookup.getDefault().lookup(ProjectControllerImpl.class);
      generator=Lookup.getDefault().lookup(GraphGenerator.class);
    }

    
//    @Test
//    public void testOneNodeDensity() {
//        pc.newProject();
//        GraphModel graphModel=generator.generateNullUndirectedGraph(1);
//        HierarchicalGraph graph = graphModel.getHierarchicalGraphVisible();
//        GraphDensity d=new GraphDensity();
//        double density=d.calculateDensity(graph, false);
//        assertEquals(density, 0.0);
//    }
    
    @Test
    public void testTwoConnectedNodesDensity() {
        pc.newProject();
        GraphModel graphModel=generator.generateCompleteUndirectedGraph(2);
        HierarchicalGraph graph = graphModel.getHierarchicalGraphVisible();
        GraphDensity d=new GraphDensity();
        double density=d.calculateDensity(graph, false);
        assertEquals(density, 1.0);
    }
    
    @Test
    public void testNullGraphDensity() {
        pc.newProject();
        GraphModel graphModel=generator.generateNullUndirectedGraph(5);
        HierarchicalGraph graph = graphModel.getHierarchicalGraphVisible();
        GraphDensity d=new GraphDensity();
        double density=d.calculateDensity(graph, false);
        assertEquals(density, 0.0);
    }
    
    @Test
    public void testCompleteGraphDensity() {
        pc.newProject();
        GraphModel graphModel=generator.generateCompleteUndirectedGraph(5);
        HierarchicalGraph graph = graphModel.getHierarchicalGraphVisible();
        GraphDensity d=new GraphDensity();
        double density=d.calculateDensity(graph, false);
        assertEquals(density, 1.0);
    }
     
    @Test
    public void testCyclicGraphDensity() {
        pc.newProject();
        GraphModel graphModel=generator.generateCyclicUndirectedGraph(6);
        HierarchicalGraph graph = graphModel.getHierarchicalGraphVisible();
        GraphDensity d=new GraphDensity();
        double density=d.calculateDensity(graph, false);
        assertEquals(density, 0.4);
    }
    
//    @Test
//    public void testSelfLoopNodeDensity() {
//        pc.newProject();
//        GraphModel graphModel=Lookup.getDefault().lookup(GraphController.class).getModel();
//        UndirectedGraph undirectedGraph=graphModel.getUndirectedGraph();
//        Node currentNode=graphModel.factory().newNode("0");
//        undirectedGraph.addNode(currentNode);
//        Edge currentEdge=graphModel.factory().newEdge(currentNode, currentNode);
//        undirectedGraph.addEdge(currentEdge);
//        HierarchicalGraph graph = graphModel.getHierarchicalGraphVisible();
//        GraphDensity d=new GraphDensity();
//        double density=d.calculateDensity(graph, false);
//        assertEquals(density, 0.0);
//    }
    
    @Test
    public void testCompleteGraphWithSelfLoopsDensity() {
        pc.newProject();
        GraphModel graphModel=generator.generateCompleteUndirectedGraph(3);
        UndirectedGraph undirectedGraph=graphModel.getUndirectedGraph();
        Node n1=undirectedGraph.getNode("0");
        Node n2=undirectedGraph.getNode("1");
        Node n3=undirectedGraph.getNode("2");
        Edge currentEdge=graphModel.factory().newEdge(n1, n1);
        undirectedGraph.addEdge(currentEdge);
        currentEdge=graphModel.factory().newEdge(n2, n2);
        undirectedGraph.addEdge(currentEdge);
        currentEdge=graphModel.factory().newEdge(n3, n3);
        undirectedGraph.addEdge(currentEdge);
        HierarchicalGraph graph = graphModel.getHierarchicalGraphVisible();
        GraphDensity d=new GraphDensity();
        double density=d.calculateDensity(graph, false);
        assertEquals(density, 2.0);
    }
    
     @Test
    public void testTwoCompleteGraphsDensity() {
        pc.newProject();
        GraphModel graphModel=generator.generateCompleteUndirectedGraph(4);
        UndirectedGraph undirectedGraph=graphModel.getUndirectedGraph();
        Node[] nodes=new Node[4];
        for (int i=0; i<4; i++) {
             Node currentNode=graphModel.factory().newNode(((Integer)(i+4)).toString());
             nodes[i]=currentNode;
             undirectedGraph.addNode(currentNode);
        }
        for (int i=0; i<3; i++) {
            for (int j=i+1; j<4; j++) {
                Edge currentEdge=graphModel.factory().newEdge(nodes[i], nodes[j]);
                undirectedGraph.addEdge(currentEdge);
            }
        }
        HierarchicalGraph graph = graphModel.getHierarchicalGraphVisible();
        GraphDensity d=new GraphDensity();
        double density=d.calculateDensity(graph, false);
        double expectedAvDegree=0.4286;
        double diff=Math.abs(density-expectedAvDegree);
        assertTrue(diff<0.01);
    }
    
    @Test 
    public void testDirectedPathGraphDensity() {
        pc.newProject();
        GraphModel graphModel=generator.generatePathDirectedGraph(2);
        HierarchicalDirectedGraph graph = graphModel.getHierarchicalDirectedGraphVisible();
        GraphDensity d=new GraphDensity();
        double density=d.calculateDensity(graph, true);
        assertEquals(density, 0.5);
    }
    
    @Test 
    public void testDirectedCyclicGraphDensity() {
        pc.newProject();
        GraphModel graphModel=generator.generateCyclicDirectedGraph(5);
        HierarchicalDirectedGraph graph = graphModel.getHierarchicalDirectedGraphVisible();
        GraphDensity d=new GraphDensity();
        double density=d.calculateDensity(graph, true);
        assertEquals(density, 0.25);
    }
    
    @Test 
    public void testDirectedCompleteGraphDensity() {
        pc.newProject();
        GraphModel graphModel=generator.generateCompleteDirectedGraph(5);
        HierarchicalDirectedGraph graph = graphModel.getHierarchicalDirectedGraphVisible();
        GraphDensity d=new GraphDensity();
        double density=d.calculateDensity(graph, true);
        assertEquals(density, 1.0);
    }
    
    @Test 
    public void testDirectedCompleteGraphWithSelfLoopsDensity() {
        pc.newProject();
        GraphModel graphModel=generator.generateCompleteDirectedGraph(3);
        DirectedGraph directedGraph=graphModel.getDirectedGraph();
        Node n1=directedGraph.getNode("0");
        Node n2=directedGraph.getNode("1");
        Node n3=directedGraph.getNode("2");
        Edge currentEdge=graphModel.factory().newEdge(n1, n1);
        directedGraph.addEdge(currentEdge);
        currentEdge=graphModel.factory().newEdge(n2, n2);
        directedGraph.addEdge(currentEdge);
        currentEdge=graphModel.factory().newEdge(n3, n3);
        directedGraph.addEdge(currentEdge);
        
        HierarchicalDirectedGraph graph = graphModel.getHierarchicalDirectedGraphVisible();
        
        GraphDensity d=new GraphDensity();
        double density=d.calculateDensity(graph, true);
        assertEquals(density, 1.5);
    }
}