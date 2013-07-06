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
}