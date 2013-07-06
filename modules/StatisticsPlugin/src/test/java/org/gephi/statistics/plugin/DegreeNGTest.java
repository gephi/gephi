/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.statistics.plugin;

import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.graph.api.DirectedGraph;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.UndirectedGraph;
import org.gephi.project.api.ProjectController;
import org.gephi.project.impl.ProjectControllerImpl;
import org.openide.util.Lookup;
import static org.testng.Assert.*;
import org.testng.annotations.Test;
/**
 *
 * @author Anna
 */
public class DegreeNGTest {

    @Test
    public void testOneNodeAverageDegree() {
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        pc.newProject();
        GraphModel graphModel=Lookup.getDefault().lookup(GraphController.class).getModel();
        AttributeModel attributeModel=Lookup.getDefault().lookup(AttributeController.class).getModel();
        Node n0=graphModel.factory().newNode("n0");
        DirectedGraph directedGraph=graphModel.getDirectedGraph();
        directedGraph.addNode(n0);
        
        Degree d=new Degree();
        d.execute(graphModel, attributeModel);
        
        double avDegree=d.getAverageDegree();
        assertEquals(avDegree, 0.0);
        
    }
    
    @Test
    public void testFourNodesAverageDegree() {
        ProjectController pc = Lookup.getDefault().lookup(ProjectControllerImpl.class);
        pc.newProject();
        GraphGenerator generator=Lookup.getDefault().lookup(GraphGenerator.class);      
        GraphModel graphModel=generator.generateNullUndirectedGraph(4);
        AttributeModel attributeModel=Lookup.getDefault().lookup(AttributeController.class).getModel();
        Degree d=new Degree();
        d.execute(graphModel, attributeModel);
        
        double avDegree=d.getAverageDegree();
        assertEquals(avDegree, 0.0);        
    }
    
    @Test
    public void testCompleteGraphAverageDegree() {
        ProjectController pc = Lookup.getDefault().lookup(ProjectControllerImpl.class);
        pc.newProject();
        GraphGenerator generator=Lookup.getDefault().lookup(GraphGenerator.class);      
        GraphModel graphModel=generator.generateCompleteUndirectedGraph(4);
        AttributeModel attributeModel=Lookup.getDefault().lookup(AttributeController.class).getModel();
        Degree d=new Degree();
        d.execute(graphModel, attributeModel);
        
        double avDegree=d.getAverageDegree();
        assertEquals(avDegree, 3.0);        
    }
    
     @Test
    public void testPathGraphAverageDegree() {
        ProjectController pc = Lookup.getDefault().lookup(ProjectControllerImpl.class);
        pc.newProject();
        GraphGenerator generator=Lookup.getDefault().lookup(GraphGenerator.class);      
        GraphModel graphModel=generator.generatePathUndirectedGraph(5);
        AttributeModel attributeModel=Lookup.getDefault().lookup(AttributeController.class).getModel();
        Degree d=new Degree();
        d.execute(graphModel, attributeModel);
        
        double avDegree=d.getAverageDegree();
        assertEquals(avDegree, 1.6);        
    }
     
      @Test
    public void testCyclicGraphAverageDegree() {
        ProjectController pc = Lookup.getDefault().lookup(ProjectControllerImpl.class);
        pc.newProject();
        GraphGenerator generator=Lookup.getDefault().lookup(GraphGenerator.class);      
        GraphModel graphModel=generator.generateCyclicUndirectedGraph(6);
        AttributeModel attributeModel=Lookup.getDefault().lookup(AttributeController.class).getModel();
        Degree d=new Degree();
        d.execute(graphModel, attributeModel);
        
        double avDegree=d.getAverageDegree();
        assertEquals(avDegree, 2.0);        
    }
      
      @Test
    public void testStarGraphAverageDegree() {
        ProjectController pc = Lookup.getDefault().lookup(ProjectControllerImpl.class);
        pc.newProject();
        GraphGenerator generator=Lookup.getDefault().lookup(GraphGenerator.class);      
        GraphModel graphModel=generator.generateStarUndirectedGraph(5);
        AttributeModel attributeModel=Lookup.getDefault().lookup(AttributeController.class).getModel();
        Degree d=new Degree();
        d.execute(graphModel, attributeModel);
        
        double avDegree=d.getAverageDegree();
        double answer=(double)5/3;
        assertEquals(avDegree, answer);        
    }
      
      @Test
    public void testFourNodesDirectedAverageDegree() {
        ProjectController pc = Lookup.getDefault().lookup(ProjectControllerImpl.class);
        pc.newProject();
        GraphGenerator generator=Lookup.getDefault().lookup(GraphGenerator.class);      
        GraphModel graphModel=generator.generateNullDirectedGraph(4);
        AttributeModel attributeModel=Lookup.getDefault().lookup(AttributeController.class).getModel();
        Degree d=new Degree();
        d.execute(graphModel, attributeModel);
        
        double avDegree=d.getAverageDegree();
        assertEquals(avDegree, 0.0);        
    }
    
    @Test
    public void testCompleteDirectedGraphAverageDegree() {
        ProjectController pc = Lookup.getDefault().lookup(ProjectControllerImpl.class);
        pc.newProject();
        GraphGenerator generator=Lookup.getDefault().lookup(GraphGenerator.class);      
        GraphModel graphModel=generator.generateCompleteDirectedGraph(12);
        AttributeModel attributeModel=Lookup.getDefault().lookup(AttributeController.class).getModel();
        Degree d=new Degree();
        d.execute(graphModel, attributeModel);
        
        double avDegree=d.getAverageDegree();
        assertEquals(avDegree, 22.0);        
    }
    
    @Test
    public void testPathDirectedGraphAverageDegree() {
        ProjectController pc = Lookup.getDefault().lookup(ProjectControllerImpl.class);
        pc.newProject();
        GraphGenerator generator=Lookup.getDefault().lookup(GraphGenerator.class);      
        GraphModel graphModel=generator.generatePathDirectedGraph(5);
        AttributeModel attributeModel=Lookup.getDefault().lookup(AttributeController.class).getModel();
        Degree d=new Degree();
        d.execute(graphModel, attributeModel);
        
        double avDegree=d.getAverageDegree();
        assertEquals(avDegree, 1.6);        
    }
    
     @Test
    public void testCyclicDirectedGraphAverageDegree() {
        ProjectController pc = Lookup.getDefault().lookup(ProjectControllerImpl.class);
        pc.newProject();
        GraphGenerator generator=Lookup.getDefault().lookup(GraphGenerator.class);      
        GraphModel graphModel=generator.generateCyclicDirectedGraph(6);
        AttributeModel attributeModel=Lookup.getDefault().lookup(AttributeController.class).getModel();
        Degree d=new Degree();
        d.execute(graphModel, attributeModel);
        
        double avDegree=d.getAverageDegree();
        assertEquals(avDegree, 2.0);        
    }
}