/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.statistics.plugin;

import java.util.HashMap;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.graph.api.DirectedGraph;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.UndirectedGraph;
import org.gephi.graph.api.HierarchicalGraph;
import org.gephi.graph.api.HierarchicalUndirectedGraph;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.HierarchicalDirectedGraph;
import org.gephi.io.generator.plugin.GraphGenerator;
import org.gephi.project.api.ProjectController;
import org.gephi.project.impl.ProjectControllerImpl;
import org.openide.util.Lookup;
import static org.testng.Assert.*;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 * @author akharitonova
 */
public class ClusteringCoefficientNGTest {
    
    private ProjectController pc;
    private GraphGenerator generator;
    
    @BeforeClass
    public void setUp() {
      pc = Lookup.getDefault().lookup(ProjectControllerImpl.class);
      generator=Lookup.getDefault().lookup(GraphGenerator.class);
    }
    
    @Test
    public void testOneNodeClusteringCoefficient() {
        pc.newProject();
        GraphModel graphModel=generator.generateCompleteUndirectedGraph(1);
        HierarchicalGraph hgraph = graphModel.getHierarchicalUndirectedGraph();

        ClusteringCoefficient cc = new ClusteringCoefficient();
        ArrayWrapper[] network = new ArrayWrapper[1];
        int[] triangles = new int[1];
        double[] nodeClustering = new double[1];

        HashMap<String, Double> results = cc.computeClusteringCoefficient(hgraph, network, triangles, nodeClustering, false);
        double avClusteringCoefficient = results.get("clusteringCoefficient");
        
        
        assertEquals(avClusteringCoefficient, Double.NaN);
    }
    
    @Test
    public void testTwoConectedNodesClusteringCoefficient() {
        pc.newProject();
        GraphModel graphModel=generator.generateCompleteUndirectedGraph(2);
        HierarchicalGraph hgraph = graphModel.getHierarchicalUndirectedGraph();

        ClusteringCoefficient cc = new ClusteringCoefficient();
        ArrayWrapper[] network = new ArrayWrapper[2];
        int[] triangles = new int[2];
        double[] nodeClustering = new double[2];

        HashMap<String, Double> results = cc.computeClusteringCoefficient(hgraph, network, triangles, nodeClustering, false);
        double avClusteringCoefficient = results.get("clusteringCoefficient");
        
        
        assertEquals(avClusteringCoefficient, Double.NaN);
    }
    
    @Test
    public void testNullGraphClusteringCoefficient() {
        pc.newProject();
        GraphModel graphModel=generator.generateNullUndirectedGraph(5);
        HierarchicalGraph hgraph = graphModel.getHierarchicalUndirectedGraph();

        ClusteringCoefficient cc = new ClusteringCoefficient();
        ArrayWrapper[] network = new ArrayWrapper[5];
        int[] triangles = new int[5];
        double[] nodeClustering = new double[5];

        HashMap<String, Double> results = cc.computeClusteringCoefficient(hgraph, network, triangles, nodeClustering, false);
        double avClusteringCoefficient = results.get("clusteringCoefficient");
        
        
        assertEquals(avClusteringCoefficient, Double.NaN);
    }
    
   @Test
    public void testCompleteGraphClusteringCoefficient() {
        pc.newProject();
        GraphModel graphModel=generator.generateCompleteUndirectedGraph(5);
        HierarchicalGraph hgraph = graphModel.getHierarchicalUndirectedGraph();

        ClusteringCoefficient cc = new ClusteringCoefficient();
        ArrayWrapper[] network = new ArrayWrapper[5];
        int[] triangles = new int[5];
        double[] nodeClustering = new double[5];

        HashMap<String, Double> results = cc.computeClusteringCoefficient(hgraph, network, triangles, nodeClustering, false);
        double avClusteringCoefficient = results.get("clusteringCoefficient");
        
        assertEquals(avClusteringCoefficient, 1.0);
    }
   
   @Test
    public void testStarGraphClusteringCoefficient() {
        pc.newProject();
        GraphModel graphModel=generator.generateStarUndirectedGraph(5);
        HierarchicalGraph hgraph = graphModel.getHierarchicalUndirectedGraph();

        ClusteringCoefficient cc = new ClusteringCoefficient();
        ArrayWrapper[] network = new ArrayWrapper[6];
        int[] triangles = new int[6];
        double[] nodeClustering = new double[6];

        HashMap<String, Double> results = cc.computeClusteringCoefficient(hgraph, network, triangles, nodeClustering, false);

        double avClusteringCoefficient = results.get("clusteringCoefficient");
        
        assertEquals(avClusteringCoefficient, 0.0);
    }
  
   
   @Test
    public void testSpecial1UndirectedGraphClusteringCoefficient() {
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
        undirectedGraph.addNode(node1);
        undirectedGraph.addNode(node2);
        undirectedGraph.addNode(node3);
        undirectedGraph.addNode(node4);
        undirectedGraph.addNode(node5);
        undirectedGraph.addNode(node6);
        undirectedGraph.addNode(node7);
        Edge edge12=graphModel.factory().newEdge(node1, node2);
        Edge edge13=graphModel.factory().newEdge(node1, node3);
        Edge edge14=graphModel.factory().newEdge(node1, node4);
        Edge edge15=graphModel.factory().newEdge(node1, node5);
        Edge edge16=graphModel.factory().newEdge(node1, node6);
        Edge edge17=graphModel.factory().newEdge(node1, node7);
        Edge edge23=graphModel.factory().newEdge(node2, node3);
        Edge edge34=graphModel.factory().newEdge(node3, node4);
        Edge edge45=graphModel.factory().newEdge(node4, node5);
        Edge edge56=graphModel.factory().newEdge(node5, node6);
        Edge edge67=graphModel.factory().newEdge(node6, node7);
        Edge edge72=graphModel.factory().newEdge(node7, node2);
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
        
        HierarchicalGraph hgraph = graphModel.getHierarchicalGraph();
        ClusteringCoefficient cc = new ClusteringCoefficient();

        ArrayWrapper[] network = new ArrayWrapper[7];
        int[] triangles = new int[7];
        double[] nodeClustering = new double[7];

        HashMap<String, Double> results = cc.computeClusteringCoefficient(hgraph, network, triangles, nodeClustering, false);
        
        double cl1 = nodeClustering[0];
        double cl3 = nodeClustering[2];
        
        double res3=0.667;
        double diff = 0.01;
        
        assertEquals(cl1, 0.4);
        assertTrue(Math.abs(cl3-res3)<diff);
        
    }
   
   @Test
    public void testSpecial2UndirectedGraphClusteringCoefficient() {
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
        undirectedGraph.addNode(node1);
        undirectedGraph.addNode(node2);
        undirectedGraph.addNode(node3);
        undirectedGraph.addNode(node4);
        undirectedGraph.addNode(node5);
        undirectedGraph.addNode(node6);
        undirectedGraph.addNode(node7);
        Edge edge12=graphModel.factory().newEdge(node1, node2);
        Edge edge23=graphModel.factory().newEdge(node2, node3);
        Edge edge31=graphModel.factory().newEdge(node3, node1);
        Edge edge14=graphModel.factory().newEdge(node1, node4);
        Edge edge45=graphModel.factory().newEdge(node4, node5);
        Edge edge51=graphModel.factory().newEdge(node5, node1);
        Edge edge16=graphModel.factory().newEdge(node1, node6);
        Edge edge67=graphModel.factory().newEdge(node6, node7);
        Edge edge71=graphModel.factory().newEdge(node7, node1);
        undirectedGraph.addEdge(edge12);
        undirectedGraph.addEdge(edge23);
        undirectedGraph.addEdge(edge31);
        undirectedGraph.addEdge(edge14);
        undirectedGraph.addEdge(edge45);
        undirectedGraph.addEdge(edge51);
        undirectedGraph.addEdge(edge16);
        undirectedGraph.addEdge(edge67);
        undirectedGraph.addEdge(edge71);
        
        HierarchicalGraph hgraph = graphModel.getHierarchicalGraph();
        ClusteringCoefficient cc = new ClusteringCoefficient();

        ArrayWrapper[] network = new ArrayWrapper[7];
        int[] triangles = new int[7];
        double[] nodeClustering = new double[7];

        HashMap<String, Double> results = cc.computeClusteringCoefficient(hgraph, network, triangles, nodeClustering, false);
        
        double cl2 = nodeClustering[1];
        double avClusteringCoefficient = results.get("clusteringCoefficient");
        
        double resAv=0.8857;
        double diff = 0.01;
        
        assertEquals(cl2, 1.0);
        assertTrue(Math.abs(avClusteringCoefficient-resAv)<diff);
        
    }
   
   @Test
    public void testSpecial3UndirectedGraphClusteringCoefficient() {
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
        Edge edge23=graphModel.factory().newEdge(node2, node3);
        Edge edge31=graphModel.factory().newEdge(node3, node1);
        Edge edge14=graphModel.factory().newEdge(node1, node4);
        Edge edge25=graphModel.factory().newEdge(node2, node5);
        Edge edge36=graphModel.factory().newEdge(node3, node6);
        undirectedGraph.addEdge(edge12);
        undirectedGraph.addEdge(edge23);
        undirectedGraph.addEdge(edge31);
        undirectedGraph.addEdge(edge14);
        undirectedGraph.addEdge(edge25);
        undirectedGraph.addEdge(edge36);;
        
        HierarchicalGraph hgraph = graphModel.getHierarchicalGraph();
        ClusteringCoefficient cc = new ClusteringCoefficient();

        ArrayWrapper[] network = new ArrayWrapper[6];
        int[] triangles = new int[6];
        double[] nodeClustering = new double[6];

        HashMap<String, Double> results = cc.computeClusteringCoefficient(hgraph, network, triangles, nodeClustering, false);
        
        double cl1 = nodeClustering[0];
        
        double res1=0.333;
        double diff = 0.01;
        
        assertTrue(Math.abs(cl1-res1)<diff);
        
    }
   
   @Test
    public void testTriangleGraphClusteringCoefficient() {
        pc.newProject();
        GraphModel graphModel=generator.generateCompleteUndirectedGraph(3);
        HierarchicalGraph hgraph = graphModel.getHierarchicalUndirectedGraph();

        ClusteringCoefficient cc = new ClusteringCoefficient();
        ArrayWrapper[] network = new ArrayWrapper[3];

        int[] triangles = new int[3];
        double[] nodeClustering = new double[3];

        HashMap<String, Double> results = cc.computeClusteringCoefficient(hgraph, network, triangles, nodeClustering, false);
        
        double avClusteringCoefficient = results.get("clusteringCoefficient");
        
        assertEquals(avClusteringCoefficient, 1.0);
    }

   
   @Test 
    public void testSpecial1DirectedGraphClusteringCoefficient() {
        pc.newProject();
        GraphModel graphModel=Lookup.getDefault().lookup(GraphController.class).getModel();
        
        DirectedGraph directedGraph=graphModel.getDirectedGraph();
        
        Node node1=graphModel.factory().newNode("0");
        Node node2=graphModel.factory().newNode("1");
        Node node3=graphModel.factory().newNode("2");
        Node node4=graphModel.factory().newNode("3");
        
        directedGraph.addNode(node1);
        directedGraph.addNode(node2);
        directedGraph.addNode(node3);
        directedGraph.addNode(node4);
        
        Edge edge12=graphModel.factory().newEdge(node1, node2);
        Edge edge23=graphModel.factory().newEdge(node2, node3);
        Edge edge24=graphModel.factory().newEdge(node2, node4);
        Edge edge31=graphModel.factory().newEdge(node3, node1);
        Edge edge34=graphModel.factory().newEdge(node3, node4);
        Edge edge41=graphModel.factory().newEdge(node4, node1);
         
        directedGraph.addEdge(edge12);
        directedGraph.addEdge(edge23);
        directedGraph.addEdge(edge24);
        directedGraph.addEdge(edge31);
        directedGraph.addEdge(edge34);
        directedGraph.addEdge(edge41);
        
        
        HierarchicalDirectedGraph hgraph = graphModel.getHierarchicalDirectedGraphVisible();
        
        ClusteringCoefficient cc = new ClusteringCoefficient();
        ArrayWrapper[] network = new ArrayWrapper[4];
        int[] triangles = new int[4];
        double[] nodeClustering = new double[4];

        HashMap<String, Double> results = cc.computeClusteringCoefficient(hgraph, network, triangles, nodeClustering, true);
        double avClusteringCoefficient = results.get("clusteringCoefficient");
        
        
        assertEquals(avClusteringCoefficient, 0.5);
    }
   
   @Test 
    public void testTriangleDirectedGraphClusteringCoefficient() {
        pc.newProject();
        GraphModel graphModel=Lookup.getDefault().lookup(GraphController.class).getModel();
        
        DirectedGraph directedGraph=graphModel.getDirectedGraph();
        
        Node node1=graphModel.factory().newNode("0");
        Node node2=graphModel.factory().newNode("1");
        Node node3=graphModel.factory().newNode("2");
        
        directedGraph.addNode(node1);
        directedGraph.addNode(node2);
        directedGraph.addNode(node3);
        
        Edge edge12=graphModel.factory().newEdge(node1, node2);
        Edge edge21=graphModel.factory().newEdge(node2, node1);
        Edge edge23=graphModel.factory().newEdge(node2, node3);
        Edge edge32=graphModel.factory().newEdge(node3, node2);
        Edge edge31=graphModel.factory().newEdge(node3, node1);
        Edge edge13=graphModel.factory().newEdge(node1, node3);
         
        directedGraph.addEdge(edge12);
        directedGraph.addEdge(edge21);
        directedGraph.addEdge(edge23);
        directedGraph.addEdge(edge32);
        directedGraph.addEdge(edge31);
        directedGraph.addEdge(edge13);
        
        
        HierarchicalDirectedGraph hgraph = graphModel.getHierarchicalDirectedGraphVisible();
        
        ClusteringCoefficient cc = new ClusteringCoefficient();
        ArrayWrapper[] network = new ArrayWrapper[3];
        int[] triangles = new int[3];
        double[] nodeClustering = new double[3];

        HashMap<String, Double> results = cc.computeClusteringCoefficient(hgraph, network, triangles, nodeClustering, true);
        double avClusteringCoefficient = results.get("clusteringCoefficient");
        
        
        assertEquals(avClusteringCoefficient, 1.);
    }
   
   @Test 
    public void testSpecial2DirectedGraphClusteringCoefficient() {
        pc.newProject();
        GraphModel graphModel=Lookup.getDefault().lookup(GraphController.class).getModel();
        
        DirectedGraph directedGraph=graphModel.getDirectedGraph();
        
        Node node1=graphModel.factory().newNode("0");
        Node node2=graphModel.factory().newNode("1");
        Node node3=graphModel.factory().newNode("2");
        Node node4=graphModel.factory().newNode("3");
        
        directedGraph.addNode(node1);
        directedGraph.addNode(node2);
        directedGraph.addNode(node3);
        directedGraph.addNode(node4);
        
        Edge edge21=graphModel.factory().newEdge(node2, node1);
        Edge edge24=graphModel.factory().newEdge(node2, node4);
        Edge edge31=graphModel.factory().newEdge(node3, node1);
        Edge edge32=graphModel.factory().newEdge(node3, node2);
        Edge edge43=graphModel.factory().newEdge(node4, node3);
         
        directedGraph.addEdge(edge21);
        directedGraph.addEdge(edge24);
        directedGraph.addEdge(edge31);
        directedGraph.addEdge(edge32);
        directedGraph.addEdge(edge43);
        
        
        HierarchicalDirectedGraph hgraph = graphModel.getHierarchicalDirectedGraphVisible();
        
        ClusteringCoefficient cc = new ClusteringCoefficient();
        ArrayWrapper[] network = new ArrayWrapper[4];
        int[] triangles = new int[4];
        double[] nodeClustering = new double[4];

        HashMap<String, Double> results = cc.computeClusteringCoefficient(hgraph, network, triangles, nodeClustering, true);
        double avClusteringCoefficient = results.get("clusteringCoefficient");
        double res = 0.4167;
        double diff = 0.01;
        
        assertTrue(Math.abs(avClusteringCoefficient-res)<diff);
    }
   
    @Test 
    public void testTriangleNonCompleteDirectedGraphClusteringCoefficient() {
        pc.newProject();
        GraphModel graphModel=Lookup.getDefault().lookup(GraphController.class).getModel();
        
        DirectedGraph directedGraph=graphModel.getDirectedGraph();
        
        Node node1=graphModel.factory().newNode("0");
        Node node2=graphModel.factory().newNode("1");
        Node node3=graphModel.factory().newNode("2");
        
        directedGraph.addNode(node1);
        directedGraph.addNode(node2);
        directedGraph.addNode(node3);
        
        Edge edge12=graphModel.factory().newEdge(node1, node2);
        Edge edge21=graphModel.factory().newEdge(node2, node1);
        Edge edge23=graphModel.factory().newEdge(node2, node3);
        Edge edge32=graphModel.factory().newEdge(node3, node2);
        Edge edge13=graphModel.factory().newEdge(node1, node3);
         
        directedGraph.addEdge(edge12);
        directedGraph.addEdge(edge21);
        directedGraph.addEdge(edge23);
        directedGraph.addEdge(edge32);
        directedGraph.addEdge(edge13);
        
        
        HierarchicalDirectedGraph hgraph = graphModel.getHierarchicalDirectedGraphVisible();
        
        ClusteringCoefficient cc = new ClusteringCoefficient();
        ArrayWrapper[] network = new ArrayWrapper[3];
        int[] triangles = new int[3];
        double[] nodeClustering = new double[3];

        HashMap<String, Double> results = cc.computeClusteringCoefficient(hgraph, network, triangles, nodeClustering, true);
        double avClusteringCoefficient = results.get("clusteringCoefficient");
        double res = 0.833;
        double diff = 0.01;
        
        assertTrue(Math.abs(avClusteringCoefficient-res)<diff);
    }
}