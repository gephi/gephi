/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.graph.dhns.core;

import org.gephi.data.attributes.api.AttributeController;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphFactory;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.project.api.ProjectController;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openide.util.Lookup;
import static org.junit.Assert.*;

/**
 *
 * @author Mathieu Bastian
 */
public class DhnsTestId {

    private GraphModel graphModel;
    private Graph rootGraph;

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
        ProjectController pj = Lookup.getDefault().lookup(ProjectController.class);
        pj.newProject();
        AttributeController ac = Lookup.getDefault().lookup(AttributeController.class);
        ac.getModel();
        GraphController gc = Lookup.getDefault().lookup(GraphController.class);
        graphModel = gc.getModel();
        Graph graph = gc.getModel().getUndirectedGraph();
        rootGraph = graph;
    }

    @After
    public void tearDown() {
        rootGraph = null;
        graphModel = null;
    }

    @Test
    public void testNodeDefaultId() {
        GraphFactory factory = graphModel.factory();

        Node n1 = factory.newNode();
        rootGraph.addNode(n1);

        String n1Id = "" + n1.getId();
        assertEquals(n1Id, n1.getNodeData().getId());
        assertEquals(n1, rootGraph.getNode(n1Id));
    }

    @Test
    public void testEdgeDefaultId() {
        GraphFactory factory = graphModel.factory();

        Node n1 = factory.newNode();
        Node n2 = factory.newNode();
        rootGraph.addNode(n1);
        rootGraph.addNode(n2);

        Edge e1 = factory.newEdge(n1, n2);
        rootGraph.addEdge(e1);

        String e1Id = "" + e1.getId();
        assertEquals(e1Id, e1.getEdgeData().getId());
        assertEquals(e1, rootGraph.getEdge(e1Id));
    }

    @Test
    public void testSetId() {
        GraphFactory factory = graphModel.factory();

        Node n1 = factory.newNode();
        Node n2 = factory.newNode();
        rootGraph.addNode(n1);
        rootGraph.addNode(n2);

        Edge e1 = factory.newEdge(n1, n2);
        rootGraph.addEdge(e1);

        rootGraph.setId(n1, "test");
        assertEquals(n1, rootGraph.getNode("test"));

        rootGraph.setId(e1, "edge");
        assertEquals(e1, rootGraph.getEdge("edge"));
    }

    @Test
    public void testNodeUserId() {
        GraphFactory factory = graphModel.factory();

        Node n1 = factory.newNode("test");
        rootGraph.addNode(n1);

        assertEquals(n1, rootGraph.getNode("test"));

        rootGraph.setId(n1, "test2");

        assertEquals(n1, rootGraph.getNode("test2"));
    }

    @Test
    public void testEdgeUserId() {
        GraphFactory factory = graphModel.factory();

        Node n1 = factory.newNode();
        Node n2 = factory.newNode();
        rootGraph.addNode(n1);
        rootGraph.addNode(n2);

        Edge e1 = factory.newEdge("test", n1, n2, 1f, false);
        rootGraph.addEdge(e1);

        assertEquals(e1, rootGraph.getEdge("test"));

        rootGraph.setId(e1, "test2");

        assertEquals(e1, rootGraph.getEdge("test2"));
    }

    @Test
    public void testMetaEdgeId() {
        //TODO
    }
}
