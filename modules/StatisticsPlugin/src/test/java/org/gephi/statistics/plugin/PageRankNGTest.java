/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.statistics.plugin;

import java.util.HashMap;
import java.util.LinkedList;
import org.gephi.graph.api.GraphModel;
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
        
        double[] pageRank = new double[1];
        
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
        
        double[] pageRank = new double[2];
        
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
        
        double[] pageRank = new double[5];
        
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
        
        double[] pageRank = new double[5];
        
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
        
        double[] pageRank = new double[6];
        
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
        
        double[] pageRank = new double[6];
        
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
    public void testPathUndirectedGraphPageRank() {
        pc.newProject();
        GraphModel graphModel=generator.generatePathDirectedGraph(4);
        HierarchicalGraph hgraph = graphModel.getHierarchicalDirectedGraph();

        PageRank pr = new PageRank();
        
        double[] pageRank = new double[4];
        
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
   
   
}