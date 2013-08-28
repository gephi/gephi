/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.statistics.plugin;

import java.util.HashMap;
import org.apache.bcel.generic.CALOAD;
import org.gephi.graph.api.DirectedGraph;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.HierarchicalDirectedGraph;
import org.gephi.graph.api.HierarchicalGraph;
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
public class EigenvectorCentralityNGTest {
    
    private ProjectController pc;
    private GraphGenerator generator;
    
    @BeforeClass
    public void setUp() {
      pc = Lookup.getDefault().lookup(ProjectControllerImpl.class);
      generator=Lookup.getDefault().lookup(GraphGenerator.class);
    }
    
    @Test
    public void testOneNodeEigenvectorCentrality() {
        pc.newProject();
        GraphModel graphModel=generator.generateNullUndirectedGraph(1);
        HierarchicalGraph hgraph = graphModel.getHierarchicalUndirectedGraph();

        EigenvectorCentrality ec = new EigenvectorCentrality();
        
        double[] centralities = new double[1];
        
        HashMap<Integer, Node> indicies = new HashMap();
        HashMap<Node, Integer> invIndicies = new HashMap();
                
        ec.fillIndiciesMaps(hgraph, centralities, indicies, invIndicies);
        
        ec.calculateEigenvectorCentrality(hgraph, centralities, indicies, invIndicies, false, 100);
        
        Node n1 = hgraph.getNode("0");
        int index = invIndicies.get(n1);
        double ec1 = centralities[index];
        
        assertEquals(ec1, 0.0);
    }
    
    @Test
    public void testTwoConnectedNodesEigenvectorCentrality() {
        pc.newProject();
        GraphModel graphModel=generator.generateCompleteUndirectedGraph(2);
        HierarchicalGraph hgraph = graphModel.getHierarchicalUndirectedGraph();

        EigenvectorCentrality ec = new EigenvectorCentrality();
        
        double[] centralities = new double[2];
        
        HashMap<Integer, Node> indicies = new HashMap();
        HashMap<Node, Integer> invIndicies = new HashMap();
                
        ec.fillIndiciesMaps(hgraph, centralities, indicies, invIndicies);
        
        ec.calculateEigenvectorCentrality(hgraph, centralities, indicies, invIndicies, false, 100);
        
        Node n1 = hgraph.getNode("0");
        int index = invIndicies.get(n1);
        double ec1 = centralities[index];
        
        assertEquals(ec1, 1.0);
    }
    
    @Test
    public void testNullGraphEigenvectorCentrality() {
        pc.newProject();
        GraphModel graphModel=generator.generateNullUndirectedGraph(5);
        HierarchicalGraph hgraph = graphModel.getHierarchicalUndirectedGraph();

        EigenvectorCentrality ec = new EigenvectorCentrality();
        
        double[] centralities = new double[5];
        
        HashMap<Integer, Node> indicies = new HashMap();
        HashMap<Node, Integer> invIndicies = new HashMap();
                
        ec.fillIndiciesMaps(hgraph, centralities, indicies, invIndicies);
        
        ec.calculateEigenvectorCentrality(hgraph, centralities, indicies, invIndicies, false, 100);
        
        Node n2 = hgraph.getNode("1");
        int index = invIndicies.get(n2);
        double ec2 = centralities[index];
        
        assertEquals(ec2, 0.0);
    }
    
    @Test
    public void testCompleteUndirectedGraphEigenvectorCentrality() {
        pc.newProject();
        GraphModel graphModel=generator.generateCompleteUndirectedGraph(5);
        HierarchicalGraph hgraph = graphModel.getHierarchicalUndirectedGraph();

        EigenvectorCentrality ec = new EigenvectorCentrality();
        
        double[] centralities = new double[5];
        
        HashMap<Integer, Node> indicies = new HashMap();
        HashMap<Node, Integer> invIndicies = new HashMap();
                
        ec.fillIndiciesMaps(hgraph, centralities, indicies, invIndicies);
        
        ec.calculateEigenvectorCentrality(hgraph, centralities, indicies, invIndicies, false, 100);
        
        Node n1 = hgraph.getNode("0");
        Node n3 = hgraph.getNode("2");
        int index1 = invIndicies.get(n1);
        int index3 = invIndicies.get(n3);
        double ec1 = centralities[index1];
        double ec3 = centralities[index3];
        
        assertEquals(ec1, 1.0);
        assertEquals(ec3, 1.0);
    }
    
    @Test
    public void testCyclicDirectedGraphEigenvectorCentrality() {
        pc.newProject();
        GraphModel graphModel=generator.generateCyclicDirectedGraph(3);
        HierarchicalGraph hgraph = graphModel.getHierarchicalDirectedGraph();

        EigenvectorCentrality ec = new EigenvectorCentrality();
        
        double[] centralities = new double[3];
        
        HashMap<Integer, Node> indicies = new HashMap();
        HashMap<Node, Integer> invIndicies = new HashMap();
                
        ec.fillIndiciesMaps(hgraph, centralities, indicies, invIndicies);
        
        ec.calculateEigenvectorCentrality(hgraph, centralities, indicies, invIndicies, true, 100);
        
        Node n1 = hgraph.getNode("0");
        int index1 = invIndicies.get(n1);
        double ec1 = centralities[index1];
        
        assertEquals(ec1, 1.0);
    }
    
    @Test
    public void testPathDirectedGraphEigenvectorCentrality() {
        pc.newProject();
        GraphModel graphModel=generator.generatePathDirectedGraph(4);
        HierarchicalGraph hgraph = graphModel.getHierarchicalDirectedGraph();

        EigenvectorCentrality ec = new EigenvectorCentrality();
        
        double[] centralities = new double[4];
        
        HashMap<Integer, Node> indicies = new HashMap();
        HashMap<Node, Integer> invIndicies = new HashMap();
                
        ec.fillIndiciesMaps(hgraph, centralities, indicies, invIndicies);
        
        ec.calculateEigenvectorCentrality(hgraph, centralities, indicies, invIndicies, true, 100);
        
        Node n1 = hgraph.getNode("0");
        Node n4 = hgraph.getNode("3");
        int index1 = invIndicies.get(n1);
        int index4 = invIndicies.get(n4);
        double ec1 = centralities[index1];
        double ec4 = centralities[index4];
        
        assertEquals(ec1, 0.0);
        assertEquals(ec4, 1.0);
    }
    
    @Test 
    public void testDirectedStarOutEigenvectorCentrality() {
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
        EigenvectorCentrality ec = new EigenvectorCentrality();
        
        double[] centralities = new double[6];
        
        HashMap<Integer, Node> indicies = new HashMap();
        HashMap<Node, Integer> invIndicies = new HashMap();
                
        ec.fillIndiciesMaps(hgraph, centralities, indicies, invIndicies);
        
        ec.calculateEigenvectorCentrality(hgraph, centralities, indicies, invIndicies, true, 100);
        
        Node n1 = hgraph.getNode("0");
        Node n2 = hgraph.getNode("1");
        int index1 = invIndicies.get(n1);
        int index2 = invIndicies.get(n2);
        double ec1 = centralities[index1];
        double ec2 = centralities[index2];
        
        assertEquals(ec1, 0.0);
        assertEquals(ec2, 1.0);
    }
}