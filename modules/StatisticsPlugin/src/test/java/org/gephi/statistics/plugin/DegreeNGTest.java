/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.statistics.plugin;

import org.gephi.data.attributes.AttributeControllerImpl;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.graph.api.DirectedGraph;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.UndirectedGraph;
import org.gephi.graph.dhns.DhnsGraphController;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.project.impl.ProjectControllerImpl;
import org.netbeans.junit.MockServices;
import org.openide.util.Lookup;
import static org.testng.Assert.*;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
/**
 *
 * @author Anna
 */
public class DegreeNGTest {
    
    public DegreeNGTest() {
    }
    
    @BeforeClass
    private void setUp() {
         MockServices.setServices(DhnsGraphController.class, AttributeControllerImpl.class);
    }

    @Test
    public void testOneNodeAverageDegree() {
        ProjectController pc = Lookup.getDefault().lookup(ProjectControllerImpl.class);
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
    public void testTwoNodesAverageDegree() {
        ProjectController pc = Lookup.getDefault().lookup(ProjectControllerImpl.class);
        pc.newProject();
        GraphModel graphModel=Lookup.getDefault().lookup(GraphController.class).getModel();
        AttributeModel attributeModel=Lookup.getDefault().lookup(AttributeController.class).getModel();
        Node n0=graphModel.factory().newNode("n0");
        Node n1=graphModel.factory().newNode("n1");
        Edge e1=graphModel.factory().newEdge(n1, n0);
        UndirectedGraph undirectedGraph=graphModel.getUndirectedGraph();
        undirectedGraph.addNode(n0);
        undirectedGraph.addNode(n1);
        undirectedGraph.addEdge(e1);
        
        Degree d=new Degree();
        d.execute(graphModel, attributeModel);
        
        double avDegree=d.getAverageDegree();
        assertEquals(avDegree, 1.0);        
    }
}