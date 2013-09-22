/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.statistics.plugin;

import java.util.HashMap;
import java.util.LinkedList;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.HierarchicalGraph;
import org.gephi.graph.api.HierarchicalUndirectedGraph;
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
public class ModularityNGTest {
    
    private ProjectController pc;
    private GraphGenerator generator;
    
    @BeforeClass
    public void setUp() {
      pc = Lookup.getDefault().lookup(ProjectControllerImpl.class);
      generator=Lookup.getDefault().lookup(GraphGenerator.class);
    }
    
    @Test
    public void testTwoConnectedNodesModularity() {
        pc.newProject();
        GraphModel graphModel=generator.generateCompleteUndirectedGraph(2);
        HierarchicalUndirectedGraph hgraph = graphModel.getHierarchicalUndirectedGraph();

        Modularity mod = new Modularity();
        
        Modularity.CommunityStructure theStructure  = mod.new CommunityStructure(hgraph);
        int[] comStructure = new int[hgraph.getNodeCount()];
        
        HashMap<String, Double> modularityValues = mod.computeModularity(hgraph, theStructure, comStructure, 
            1., true, false);
        
        double modValue = modularityValues.get("modularity");
        int class1 = comStructure[0];
        int class2 = comStructure[1];      
        
        assertEquals(modValue, 0.0);
        assertEquals(class1, class2);
    }
    
     @Test
    public void testGraphWithouLinksModularity() {
        pc.newProject();
        GraphModel graphModel=generator.generateNullUndirectedGraph(5);
        HierarchicalUndirectedGraph hgraph = graphModel.getHierarchicalUndirectedGraph();

        Modularity mod = new Modularity();
        
        Modularity.CommunityStructure theStructure  = mod.new CommunityStructure(hgraph);
        int[] comStructure = new int[hgraph.getNodeCount()];
        
        HashMap<String, Double> modularityValues = mod.computeModularity(hgraph, theStructure, comStructure, 
            1., true, false);
        
        double modValue = modularityValues.get("modularity");     
        
        assertEquals(modValue, Double.NaN);
    }
    
     @Test
    public void testComputeBarbellGraphModularityNormalResolution() {
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
        Edge currentEdge=graphModel.factory().newEdge(undirectedGraph.getNode("0"), undirectedGraph.getNode("5"));
        undirectedGraph.addEdge(currentEdge);
        HierarchicalUndirectedGraph graph = graphModel.getHierarchicalUndirectedGraph();

        Modularity mod = new Modularity();
        
        Modularity.CommunityStructure theStructure  = mod.new CommunityStructure(graph);
        int[] comStructure = new int[graph.getNodeCount()];
        
        HashMap<String, Double> modularityValues = mod.computeModularity(graph, theStructure, comStructure, 
            1., true, false);
        
        double modValue = modularityValues.get("modularity");
        
        int class4 = comStructure[0];
        int class5 = comStructure[5];
        
        boolean correctResult = (class4!=class5 || modValue==0.);
        
        assertTrue(correctResult);
    }
     
      @Test
    public void testComputeBarbellGraphHighResolution() {
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
        Edge currentEdge=graphModel.factory().newEdge(undirectedGraph.getNode("0"), undirectedGraph.getNode("5"));
        undirectedGraph.addEdge(currentEdge);
        HierarchicalUndirectedGraph graph = graphModel.getHierarchicalUndirectedGraph();

        Modularity mod = new Modularity();
        
        Modularity.CommunityStructure theStructure  = mod.new CommunityStructure(graph);
        int[] comStructure = new int[graph.getNodeCount()];
        
        HashMap<String, Double> modularityValues = mod.computeModularity(graph, theStructure, comStructure, 
            100., true, false);
        
        double modValue = modularityValues.get("modularity");
        
        int class4 = comStructure[0];
        int class5 = comStructure[5];
        
        assertEquals(modValue, 0.0);
        assertEquals(class4, class5);
    }
      
      @Test
    public void testComputeBarbellGraphModularityHasHighWeight() {
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
        Edge currentEdge=graphModel.factory().newEdge(undirectedGraph.getNode("0"), undirectedGraph.getNode("5"), 100.f, false);
        undirectedGraph.addEdge(currentEdge);
        HierarchicalUndirectedGraph graph = graphModel.getHierarchicalUndirectedGraph();

        Modularity mod = new Modularity();
        
        Modularity.CommunityStructure theStructure  = mod.new CommunityStructure(graph);
        int[] comStructure = new int[graph.getNodeCount()];
        
        HashMap<String, Double> modularityValues = mod.computeModularity(graph, theStructure, comStructure, 
            1., true, true);

        
        int class4 = comStructure[0];
        int class5 = comStructure[5];
        
        assertEquals(class4, class5);
    }
      
      @Test
    public void testCyclicWithWeightsGraphModularity() {
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
        
        Edge edge12=graphModel.factory().newEdge(node1, node2, 10.f, false);
        Edge edge23=graphModel.factory().newEdge(node2, node3);
        Edge edge34=graphModel.factory().newEdge(node3, node4, 10.f, false);       
        Edge edge45=graphModel.factory().newEdge(node4, node5);
        Edge edge56=graphModel.factory().newEdge(node5, node6, 10.f, false);
        Edge edge67=graphModel.factory().newEdge(node6, node7);
        Edge edge78=graphModel.factory().newEdge(node7, node8, 10.f, false);       
        Edge edge81=graphModel.factory().newEdge(node8, node1);
  
        undirectedGraph.addEdge(edge12);
        undirectedGraph.addEdge(edge23);
        undirectedGraph.addEdge(edge34);
        undirectedGraph.addEdge(edge45);
        undirectedGraph.addEdge(edge56);
        undirectedGraph.addEdge(edge67);
        undirectedGraph.addEdge(edge78);
        undirectedGraph.addEdge(edge81);
        
        HierarchicalUndirectedGraph hgraph = graphModel.getHierarchicalUndirectedGraphVisible();
        Modularity mod = new Modularity();
        
        Modularity.CommunityStructure theStructure  = mod.new CommunityStructure(hgraph);
        int[] comStructure = new int[hgraph.getNodeCount()];
        
        HashMap<String, Double> modularityValues = mod.computeModularity(hgraph, theStructure, comStructure, 
            1., true, true);

        int class1 = comStructure[0];
        int class2 = comStructure[1];
        int class4 = comStructure[3];
        int class5 = comStructure[4];
        int class7 = comStructure[6];
        int class8 = comStructure[7];
        
        assertEquals(class1, class2);
        assertEquals(class7, class8);
        assertNotEquals(class4, class5);
     }
}