/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.statistics.plugin;

import java.util.HashMap;
import java.util.LinkedList;
import org.gephi.graph.api.DirectedGraph;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.HierarchicalGraph;
import org.gephi.graph.api.HierarchicalUndirectedGraph;
import org.gephi.graph.api.HierarchicalDirectedGraph;
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
public class ConnectedComponentsNGTest {
    private ProjectController pc;
    private GraphGenerator generator;
    
    @BeforeClass
    public void setUp() {
      pc = Lookup.getDefault().lookup(ProjectControllerImpl.class);
      generator=Lookup.getDefault().lookup(GraphGenerator.class);
    }

     @Test
    public void testComputeOneNodeWeeklyConnectedComponents() {
        pc.newProject();
        GraphModel graphModel=generator.generateNullUndirectedGraph(1);
        HierarchicalUndirectedGraph graph = graphModel.getHierarchicalUndirectedGraph();
        Node n=graph.getNode("0");

        ConnectedComponents c=new ConnectedComponents();
        HashMap<Node, Integer> indicies = new HashMap<Node, Integer>();
        indicies.put(n,0);
        LinkedList<LinkedList<Node>> components = c.computeWeeklyConnectedComponents(graph, indicies);
        assertEquals(components.size(), 1);
    }
     
     @Test
    public void testNullGraphWeeklyConnectedComponents() {
        pc.newProject();
        GraphModel graphModel=generator.generateNullUndirectedGraph(5);
        HierarchicalUndirectedGraph graph = graphModel.getHierarchicalUndirectedGraph();
        Node n0=graph.getNode("0");
        Node n1=graph.getNode("1");
        Node n2=graph.getNode("2");
        Node n3=graph.getNode("3");
        Node n4=graph.getNode("4");

        ConnectedComponents c=new ConnectedComponents();
        HashMap<Node, Integer> indicies = new HashMap<Node, Integer>();
        indicies.put(n0,0);
        indicies.put(n1,1);
        indicies.put(n2,2);
        indicies.put(n3,3);
        indicies.put(n4,4);
        LinkedList<LinkedList<Node>> components = c.computeWeeklyConnectedComponents(graph, indicies);
        assertEquals(components.size(), 5);
    }
     
     @Test
    public void testComputeBarbellGraphWeeklyConnectedComponents() {
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

        ConnectedComponents c=new ConnectedComponents();
        HashMap<Node, Integer> indicies = c.createIndiciesMap(graph);
        LinkedList<LinkedList<Node>> components = c.computeWeeklyConnectedComponents(graph, indicies);
        assertEquals(components.size(), 1);
    }
     
     @Test
    public void testSpecial1UndirectedGraphConnectedComponents() {
        pc.newProject();
        GraphModel graphModel=Lookup.getDefault().lookup(GraphController.class).getModel();
        UndirectedGraph undirectedGraph=graphModel.getUndirectedGraph();
        Node node1=graphModel.factory().newNode("0");
        Node node2=graphModel.factory().newNode("1");
        Node node3=graphModel.factory().newNode("2");
        Node node4=graphModel.factory().newNode("3");
        Node node5=graphModel.factory().newNode("4");
        undirectedGraph.addNode(node1);
        undirectedGraph.addNode(node2);
        undirectedGraph.addNode(node3);
        undirectedGraph.addNode(node4);
        undirectedGraph.addNode(node5);
        Edge edge12=graphModel.factory().newEdge(node1, node2);
        Edge edge14=graphModel.factory().newEdge(node1, node4);
        Edge edge23=graphModel.factory().newEdge(node2, node3);
        Edge edge25=graphModel.factory().newEdge(node2, node5);
        Edge edge35=graphModel.factory().newEdge(node3, node5);
        Edge edge43=graphModel.factory().newEdge(node4, node3);
        Edge edge51=graphModel.factory().newEdge(node5, node1);
        Edge edge54=graphModel.factory().newEdge(node5, node4);
        undirectedGraph.addEdge(edge12);
        undirectedGraph.addEdge(edge14);
        undirectedGraph.addEdge(edge23);
        undirectedGraph.addEdge(edge25);
        undirectedGraph.addEdge(edge35);
        undirectedGraph.addEdge(edge43);
        undirectedGraph.addEdge(edge51);
        undirectedGraph.addEdge(edge54);
        
        HierarchicalUndirectedGraph graph=graphModel.getHierarchicalUndirectedGraph();

        ConnectedComponents c=new ConnectedComponents();
        HashMap<Node, Integer> indicies = c.createIndiciesMap(graphModel.getHierarchicalUndirectedGraph());
        LinkedList<LinkedList<Node>> components = c.computeWeeklyConnectedComponents(graph, indicies);
        assertEquals(components.size(), 1);
    }
     
     @Test
    public void testSpecial2UndirectedGraphConnectedComponents() {
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
        Node node9=graphModel.factory().newNode("8");
        undirectedGraph.addNode(node1);
        undirectedGraph.addNode(node2);
        undirectedGraph.addNode(node3);
        undirectedGraph.addNode(node4);
        undirectedGraph.addNode(node5);
        undirectedGraph.addNode(node6);
        undirectedGraph.addNode(node7);
        undirectedGraph.addNode(node8);
        undirectedGraph.addNode(node9);
        Edge edge12=graphModel.factory().newEdge(node1, node2);
        Edge edge23=graphModel.factory().newEdge(node2, node3);
        Edge edge45=graphModel.factory().newEdge(node4, node5);
        Edge edge56=graphModel.factory().newEdge(node5, node6);
        Edge edge64=graphModel.factory().newEdge(node6, node4);
        Edge edge75=graphModel.factory().newEdge(node7, node5);
        undirectedGraph.addEdge(edge12);
        undirectedGraph.addEdge(edge23);
        undirectedGraph.addEdge(edge45);
        undirectedGraph.addEdge(edge56);
        undirectedGraph.addEdge(edge64);
        undirectedGraph.addEdge(edge75);
        
        HierarchicalUndirectedGraph graph=graphModel.getHierarchicalUndirectedGraph();

        ConnectedComponents c=new ConnectedComponents();
        HashMap<Node, Integer> indicies = c.createIndiciesMap(graphModel.getHierarchicalUndirectedGraph());
        LinkedList<LinkedList<Node>> components = c.computeWeeklyConnectedComponents(graph, indicies);
        
        int componentNumber3=c.getComponentNumber(components, node3);
        int componentNumber4=c.getComponentNumber(components, node4);
        int componentNumber7=c.getComponentNumber(components, node7);
        int componentNumber8=c.getComponentNumber(components, node8);
        
        assertEquals(components.size(), 4);
        assertEquals(componentNumber4, componentNumber7);
        assertNotEquals(componentNumber3, componentNumber8);
    }
     
     
     @Test
    public void testDirectedPathGraphConnectedComponents() {
        pc.newProject();
        GraphModel graphModel=generator.generatePathDirectedGraph(4);
        HierarchicalDirectedGraph graph = graphModel.getHierarchicalDirectedGraph();

        ConnectedComponents c=new ConnectedComponents();
        HashMap<Node, Integer> indicies = c.createIndiciesMap(graphModel.getHierarchicalUndirectedGraph());
        LinkedList<LinkedList<Node>> components = c.top_tarjans(graph, indicies);
        assertEquals(components.size(), 4);
    }
     
     @Test
    public void testDirectedCyclicGraphConnectedComponents() {
        pc.newProject();
        GraphModel graphModel=generator.generateCyclicDirectedGraph(5);
        HierarchicalDirectedGraph graph = graphModel.getHierarchicalDirectedGraph();

        ConnectedComponents c=new ConnectedComponents();
        HashMap<Node, Integer> indicies = c.createIndiciesMap(graphModel.getHierarchicalUndirectedGraph());
        LinkedList<LinkedList<Node>> components = c.top_tarjans(graph, indicies);
        assertEquals(components.size(), 1);
    }
     
     @Test
    public void testSpecial1DirectedGraphConnectedComponents() {
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
        Edge edge12=graphModel.factory().newEdge(node1, node2);
        Edge edge14=graphModel.factory().newEdge(node1, node4);
        Edge edge23=graphModel.factory().newEdge(node2, node3);
        Edge edge25=graphModel.factory().newEdge(node2, node5);
        Edge edge35=graphModel.factory().newEdge(node3, node5);
        Edge edge43=graphModel.factory().newEdge(node4, node3);
        Edge edge51=graphModel.factory().newEdge(node5, node1);
        Edge edge54=graphModel.factory().newEdge(node5, node4);
        directedGraph.addEdge(edge12);
        directedGraph.addEdge(edge14);
        directedGraph.addEdge(edge23);
        directedGraph.addEdge(edge25);
        directedGraph.addEdge(edge35);
        directedGraph.addEdge(edge43);
        directedGraph.addEdge(edge51);
        directedGraph.addEdge(edge54);
        
        HierarchicalDirectedGraph graph=graphModel.getHierarchicalDirectedGraph();

        ConnectedComponents c=new ConnectedComponents();
        HashMap<Node, Integer> indicies = c.createIndiciesMap(graphModel.getHierarchicalUndirectedGraph());
        LinkedList<LinkedList<Node>> components = c.top_tarjans(graph, indicies);
        assertEquals(components.size(), 1);
    }
     
     @Test
    public void testSpecial2DirectedGraphConnectedComponents() {
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
        Edge edge12=graphModel.factory().newEdge(node1, node2);
        Edge edge14=graphModel.factory().newEdge(node1, node4);
        Edge edge23=graphModel.factory().newEdge(node2, node3);
        Edge edge25=graphModel.factory().newEdge(node2, node5);
        Edge edge35=graphModel.factory().newEdge(node3, node5);
        Edge edge43=graphModel.factory().newEdge(node4, node3);
        Edge edge54=graphModel.factory().newEdge(node5, node4);
        directedGraph.addEdge(edge12);
        directedGraph.addEdge(edge14);
        directedGraph.addEdge(edge23);
        directedGraph.addEdge(edge25);
        directedGraph.addEdge(edge35);
        directedGraph.addEdge(edge43);
        directedGraph.addEdge(edge54);
        
        HierarchicalDirectedGraph graph=graphModel.getHierarchicalDirectedGraph();

        ConnectedComponents c=new ConnectedComponents();
        HashMap<Node, Integer> indicies = c.createIndiciesMap(graphModel.getHierarchicalUndirectedGraph());
        LinkedList<LinkedList<Node>> weeklyConnectedComponents = c.computeWeeklyConnectedComponents(graphModel.getHierarchicalUndirectedGraph(), indicies);
        LinkedList<LinkedList<Node>> stronglyConnectedComponents = c.top_tarjans(graph, indicies);
        int componentNumber1=c.getComponentNumber(stronglyConnectedComponents, node1);
        int componentNumber3=c.getComponentNumber(stronglyConnectedComponents, node3);
        int componentNumber4=c.getComponentNumber(stronglyConnectedComponents, node4);
        int componentNumber5=c.getComponentNumber(stronglyConnectedComponents, node5);
        
        assertEquals(stronglyConnectedComponents.size(), 3);
        assertEquals(weeklyConnectedComponents.size(), 1);
        assertEquals(componentNumber3, componentNumber5);
        assertNotEquals(componentNumber1, componentNumber4);
    }
     
     @Test
    public void testSpecial3DirectedGraphConnectedComponents() {
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
        Edge edge45=graphModel.factory().newEdge(node4, node5);
        Edge edge56=graphModel.factory().newEdge(node5, node6);
        Edge edge64=graphModel.factory().newEdge(node6, node4);
        Edge edge75=graphModel.factory().newEdge(node7, node5);
        Edge edge89=graphModel.factory().newEdge(node8, node9);
        Edge edge98=graphModel.factory().newEdge(node9, node8);
        directedGraph.addEdge(edge12);
        directedGraph.addEdge(edge23);
        directedGraph.addEdge(edge45);
        directedGraph.addEdge(edge56);
        directedGraph.addEdge(edge64);
        directedGraph.addEdge(edge75);
        directedGraph.addEdge(edge89);
        directedGraph.addEdge(edge98);
        
        HierarchicalDirectedGraph graph=graphModel.getHierarchicalDirectedGraph();

        ConnectedComponents c=new ConnectedComponents();
        HashMap<Node, Integer> indicies = c.createIndiciesMap(graphModel.getHierarchicalUndirectedGraph());
        LinkedList<LinkedList<Node>> stronglyConnectedComponents = c.top_tarjans(graph, indicies);
        
        assertEquals(stronglyConnectedComponents.size(), 6);
    }
     
     @Test
    public void testSpecial4DirectedGraphConnectedComponents() {
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
        directedGraph.addNode(node1);
        directedGraph.addNode(node2);
        directedGraph.addNode(node3);
        directedGraph.addNode(node4);
        directedGraph.addNode(node5);
        directedGraph.addNode(node6);
        directedGraph.addNode(node7);
        directedGraph.addNode(node8);
        Edge edge12=graphModel.factory().newEdge(node1, node2);
        Edge edge23=graphModel.factory().newEdge(node2, node3);
        Edge edge34=graphModel.factory().newEdge(node3, node4);
        Edge edge41=graphModel.factory().newEdge(node4, node1);
        Edge edge56=graphModel.factory().newEdge(node5, node6);
        Edge edge67=graphModel.factory().newEdge(node6, node7);
        Edge edge78=graphModel.factory().newEdge(node7, node8);
        Edge edge85=graphModel.factory().newEdge(node8, node5);
        Edge edge45=graphModel.factory().newEdge(node4, node5);
        directedGraph.addEdge(edge12);
        directedGraph.addEdge(edge23);
        directedGraph.addEdge(edge34);
        directedGraph.addEdge(edge41);
        directedGraph.addEdge(edge56);
        directedGraph.addEdge(edge67);
        directedGraph.addEdge(edge78);
        directedGraph.addEdge(edge85);
        directedGraph.addEdge(edge45);
        
        HierarchicalDirectedGraph graph=graphModel.getHierarchicalDirectedGraph();

        ConnectedComponents c=new ConnectedComponents();
        HashMap<Node, Integer> indicies = c.createIndiciesMap(graphModel.getHierarchicalUndirectedGraph());
        LinkedList<LinkedList<Node>> stronglyConnectedComponents = c.top_tarjans(graph, indicies);
        
        int componentNumber1=c.getComponentNumber(stronglyConnectedComponents, node1);
        int componentNumber5=c.getComponentNumber(stronglyConnectedComponents, node5);
        
        assertEquals(stronglyConnectedComponents.size(), 2);
        assertNotEquals(componentNumber1, componentNumber5);
    }
    
     
    @Test
    public void testSpecial2UndirectedGraphGiantComponent() {
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
        Node node9=graphModel.factory().newNode("8");
        undirectedGraph.addNode(node1);
        undirectedGraph.addNode(node2);
        undirectedGraph.addNode(node3);
        undirectedGraph.addNode(node4);
        undirectedGraph.addNode(node5);
        undirectedGraph.addNode(node6);
        undirectedGraph.addNode(node7);
        undirectedGraph.addNode(node8);
        undirectedGraph.addNode(node9);
        Edge edge12=graphModel.factory().newEdge(node1, node2);
        Edge edge23=graphModel.factory().newEdge(node2, node3);
        Edge edge45=graphModel.factory().newEdge(node4, node5);
        Edge edge56=graphModel.factory().newEdge(node5, node6);
        Edge edge64=graphModel.factory().newEdge(node6, node4);
        Edge edge75=graphModel.factory().newEdge(node7, node5);
        undirectedGraph.addEdge(edge12);
        undirectedGraph.addEdge(edge23);
        undirectedGraph.addEdge(edge45);
        undirectedGraph.addEdge(edge56);
        undirectedGraph.addEdge(edge64);
        undirectedGraph.addEdge(edge75);
        
        HierarchicalUndirectedGraph graph=graphModel.getHierarchicalUndirectedGraph();

        ConnectedComponents c=new ConnectedComponents();
        HashMap<Node, Integer> indicies = c.createIndiciesMap(graphModel.getHierarchicalUndirectedGraph());
        LinkedList<LinkedList<Node>> components = c.computeWeeklyConnectedComponents(graph, indicies);
        c.fillComponentSizeList(components);
        
        int giantComponent = c.getGiantComponent();
        int componentNumber5=c.getComponentNumber(components, node5);
        
        assertEquals(giantComponent, componentNumber5);
    }
}