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
   
   
}