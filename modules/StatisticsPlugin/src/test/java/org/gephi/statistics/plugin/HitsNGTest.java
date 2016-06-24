/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
Website : http://www.gephi.org

This file is part of Gephi.

DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

Copyright 2011 Gephi Consortium. All rights reserved.

The contents of this file are subject to the terms of either the GNU
General Public License Version 3 only ("GPL") or the Common
Development and Distribution License("CDDL") (collectively, the
"License"). You may not use this file except in compliance with the
License. You can obtain a copy of the License at
http://gephi.org/about/legal/license-notice/
or /cddl-1.0.txt and /gpl-3.0.txt. See the License for the
specific language governing permissions and limitations under the
License.  When distributing the software, include this License Header
Notice in each file and include the License files at
/cddl-1.0.txt and /gpl-3.0.txt. If applicable, add the following below the
License Header, with the fields enclosed by brackets [] replaced by
your own identifying information:
"Portions Copyrighted [year] [name of copyright owner]"

If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 3, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 3] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 3 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 3 code and therefore, elected the GPL
Version 3 license, then the option applies only if the new code is
made subject to such option by the copyright holder.

Contributor(s):

Portions Copyrighted 2011 Gephi Consortium.
 */
package org.gephi.statistics.plugin;

import java.util.Arrays;
import java.util.HashMap;
import org.gephi.graph.api.DirectedGraph;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.UndirectedGraph;
import org.gephi.project.api.ProjectController;
import org.gephi.project.impl.ProjectControllerImpl;
import org.openide.util.Lookup;
import org.testng.Assert;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author Anna
 */
public class HitsNGTest {

    private ProjectController pc;
    private static final double EPSILON = 1e-4;

    @BeforeClass
    public void setUp() {
        pc = Lookup.getDefault().lookup(ProjectControllerImpl.class);
    }

    @BeforeMethod
    public void initialize() {
        pc.newProject();
    }

    @AfterMethod
    public void clean() {
        pc.closeCurrentProject();
    }

    @Test
    public void testOneNodeHits() {
        GraphModel graphModel = GraphGenerator.generateNullUndirectedGraph(1);
        Graph graph = graphModel.getUndirectedGraph();

        Hits hit = new Hits();

        double[] authority = new double[1];
        double[] hubs = new double[1];

        HashMap<Node, Integer> indices = hit.createIndicesMap(graph);

        hit.calculateHits(graph, hubs, authority, indices, false, EPSILON);

        Node n1 = graph.getNode("0");
        int index = indices.get(n1);
        double hub1 = hubs[index];
        double auth1 = authority[index];

        assertEquals(hub1, 0.0);
        assertEquals(auth1, 0.0);
    }

    @Test
    public void testTwoConnectedNodesHits() {
        GraphModel graphModel = GraphGenerator.generateCompleteUndirectedGraph(2);
        Graph graph = graphModel.getUndirectedGraph();

        Hits hit = new Hits();

        double[] authority = new double[2];
        double[] hubs = new double[2];

        HashMap<Node, Integer> indices = hit.createIndicesMap(graph);

        hit.calculateHits(graph, hubs, authority, indices, false, EPSILON);

        Node n1 = graph.getNode("0");
        Node n2 = graph.getNode("1");
        int index1 = indices.get(n1);
        int index2 = indices.get(n2);
        double hub1 = hubs[index1];
        double auth2 = authority[index2];

        assertEquals(hub1, 0.7071);
        assertEquals(auth2, 0.7071);
    }

    @Test
    public void testNullGraphHits() {
        GraphModel graphModel = GraphGenerator.generateNullUndirectedGraph(5);
        Graph graph = graphModel.getUndirectedGraph();

        Hits hit = new Hits();

        double[] authority = new double[5];
        double[] hubs = new double[5];

        HashMap<Node, Integer> indices = hit.createIndicesMap(graph);

        hit.calculateHits(graph, hubs, authority, indices, false, EPSILON);

        Node n2 = graph.getNode("1");
        Node n3 = graph.getNode("2");
        int index2 = indices.get(n2);
        int index3 = indices.get(n3);
        double hub2 = hubs[index2];
        double auth3 = authority[index3];

        assertEquals(hub2, 0.0);
        assertEquals(auth3, 0.0);
    }

    @Test
    public void testCompleteGraphHits() {
        GraphModel graphModel = GraphGenerator.generateCompleteUndirectedGraph(5);
        Graph graph = graphModel.getUndirectedGraph();

        Hits hit = new Hits();

        double[] authority = new double[5];
        double[] hubs = new double[5];

        HashMap<Node, Integer> indices = hit.createIndicesMap(graph);

        hit.calculateHits(graph, hubs, authority, indices, false, EPSILON);

        Node n1 = graph.getNode("0");
        Node n5 = graph.getNode("4");
        int index1 = indices.get(n1);
        int index5 = indices.get(n5);
        double hub1 = hubs[index1];
        double auth5 = authority[index5];

        assertEquals(hub1, 0.4472);
        assertEquals(auth5, 0.4472);
    }

    @Test
    public void testStarGraphHits() {
        GraphModel graphModel = GraphGenerator.generateStarUndirectedGraph(5);
        Graph graph = graphModel.getUndirectedGraph();

        Hits hit = new Hits();

        double[] authority = new double[6];
        double[] hubs = new double[6];

        HashMap<Node, Integer> indices = hit.createIndicesMap(graph);

        hit.calculateHits(graph, hubs, authority, indices, false, EPSILON);

        Node n1 = graph.getNode("0");
        Node n3 = graph.getNode("2");
        Node n4 = graph.getNode("3");
        int index1 = indices.get(n1);
        int index3 = indices.get(n3);
        int index4 = indices.get(n4);

        double hub1 = hubs[index1];
        double hub3 = hubs[index3];
        double auth1 = authority[index1];
        double auth4 = authority[index4];

        assertEquals(hub1, 0.4082);
        assertEquals(auth1, 0.9128);
        assertEquals(auth4, 0.1825);
        
        assertEquals(hub1, hub3);
    }

    @Test
    public void testGraphWithSelfLoopsHits() {
        GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getGraphModel();

        UndirectedGraph undirectedGraph = graphModel.getUndirectedGraph();
        Node node1 = graphModel.factory().newNode("0");
        Node node2 = graphModel.factory().newNode("1");
        Node node3 = graphModel.factory().newNode("2");

        undirectedGraph.addNode(node1);
        undirectedGraph.addNode(node2);
        undirectedGraph.addNode(node3);

        Edge edge12 = graphModel.factory().newEdge(node1, node2, false);
        Edge edge23 = graphModel.factory().newEdge(node2, node3, false);
        Edge edge11 = graphModel.factory().newEdge(node1, node1, false);
        Edge edge33 = graphModel.factory().newEdge(node3, node3, false);

        undirectedGraph.addEdge(edge12);
        undirectedGraph.addEdge(edge23);
        undirectedGraph.addEdge(edge11);
        undirectedGraph.addEdge(edge33);

        Graph graph = graphModel.getUndirectedGraph();

        Hits hit = new Hits();

        HashMap<Node, Integer> indices = hit.createIndicesMap(graph);

        double[] authority = new double[3];
        double[] hubs = new double[3];

        hit.calculateHits(graph, hubs, authority, indices, false, EPSILON);

        Node n1 = graph.getNode("0");
        Node n2 = graph.getNode("1");
        int index1 = indices.get(n1);
        int index2 = indices.get(n2);
        
        double auth1 = authority[index1];
        double auth2 = authority[index2];
        
        assertTrue(auth2 > auth1);
    }

    @Test
    public void testDirectedSpecial1GraphHits() {
        GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getGraphModel();

        DirectedGraph directedGraph = graphModel.getDirectedGraph();
        Node node1 = graphModel.factory().newNode("0");
        Node node2 = graphModel.factory().newNode("1");
        Node node3 = graphModel.factory().newNode("2");
        Node node4 = graphModel.factory().newNode("3");
        Node node5 = graphModel.factory().newNode("4");

        directedGraph.addNode(node1);
        directedGraph.addNode(node2);
        directedGraph.addNode(node3);
        directedGraph.addNode(node4);
        directedGraph.addNode(node5);

        Edge edge14 = graphModel.factory().newEdge(node1, node4);
        Edge edge15 = graphModel.factory().newEdge(node1, node5);
        Edge edge24 = graphModel.factory().newEdge(node2, node4);
        Edge edge25 = graphModel.factory().newEdge(node2, node5);
        Edge edge34 = graphModel.factory().newEdge(node3, node4);
        Edge edge35 = graphModel.factory().newEdge(node3, node5);

        directedGraph.addEdge(edge14);
        directedGraph.addEdge(edge15);
        directedGraph.addEdge(edge24);
        directedGraph.addEdge(edge25);
        directedGraph.addEdge(edge34);
        directedGraph.addEdge(edge35);

        DirectedGraph graph = graphModel.getDirectedGraph();
        Hits hit = new Hits();

        double[] authority = new double[5];
        double[] hubs = new double[5];

        HashMap<Node, Integer> indices = hit.createIndicesMap(graph);

        hit.calculateHits(graph, hubs, authority, indices, true, EPSILON);

        Node n1 = graph.getNode("0");
        Node n2 = graph.getNode("1");
        Node n4 = graph.getNode("3");
        Node n5 = graph.getNode("4");

        int index1 = indices.get(n1);
        int index2 = indices.get(n2);
        int index4 = indices.get(n4);
        int index5 = indices.get(n5);

        double hub1 = hubs[index1];
        double hub4 = hubs[index4];
        double auth2 = authority[index2];
        double auth5 = authority[index5];

        assertEquals(hub1, 0.5773);
        assertEquals(hub4, 0.0);
        assertEquals(auth2, 0.0);
        assertEquals(auth5, 0.7071);
    }

    @Test
    public void testDirectedStarOutGraphHits() {
        GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getGraphModel();

        DirectedGraph directedGraph = graphModel.getDirectedGraph();
        Node firstNode = graphModel.factory().newNode("0");
        directedGraph.addNode(firstNode);
        for (int i = 1; i <= 5; i++) {
            Node currentNode = graphModel.factory().newNode(((Integer) i).toString());
            directedGraph.addNode(currentNode);
            Edge currentEdge = graphModel.factory().newEdge(firstNode, currentNode);
            directedGraph.addEdge(currentEdge);
        }

        DirectedGraph graph = graphModel.getDirectedGraph();

        Hits hit = new Hits();

        double[] authority = new double[6];
        double[] hubs = new double[6];

        HashMap<Node, Integer> indices = hit.createIndicesMap(graph);

        hit.calculateHits(graph, hubs, authority, indices, true, EPSILON);

        Node n1 = graph.getNode("0");
        Node n3 = graph.getNode("2");

        int index1 = indices.get(n1);
        int index3 = indices.get(n3);

        double hub1 = hubs[index1];
        double hub3 = hubs[index3];
        double auth1 = authority[index1];
        double auth3 = authority[index3];

        assertEquals(hub1, 1.0);
        assertEquals(auth1, 0.0);
        assertEquals(hub3, 0.0);
        assertEquals(auth3, 0.4472);
    }

    @Test
    public void testDirectedSpecial2GraphHits() {
        GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getGraphModel();

        DirectedGraph directedGraph = graphModel.getDirectedGraph();
        Node node1 = graphModel.factory().newNode("0");
        Node node2 = graphModel.factory().newNode("1");
        Node node3 = graphModel.factory().newNode("2");
        Node node4 = graphModel.factory().newNode("3");
        Node node5 = graphModel.factory().newNode("4");
        Node node6 = graphModel.factory().newNode("5");

        directedGraph.addNode(node1);
        directedGraph.addNode(node2);
        directedGraph.addNode(node3);
        directedGraph.addNode(node4);
        directedGraph.addNode(node5);
        directedGraph.addNode(node6);

        Edge edge21 = graphModel.factory().newEdge(node2, node1);
        Edge edge31 = graphModel.factory().newEdge(node3, node1);
        Edge edge41 = graphModel.factory().newEdge(node4, node1);
        Edge edge51 = graphModel.factory().newEdge(node5, node1);
        Edge edge36 = graphModel.factory().newEdge(node3, node6);
        Edge edge46 = graphModel.factory().newEdge(node4, node6);
        Edge edge56 = graphModel.factory().newEdge(node5, node6);

        directedGraph.addEdge(edge21);
        directedGraph.addEdge(edge31);
        directedGraph.addEdge(edge41);
        directedGraph.addEdge(edge51);
        directedGraph.addEdge(edge36);
        directedGraph.addEdge(edge46);
        directedGraph.addEdge(edge56);

        DirectedGraph graph = graphModel.getDirectedGraph();
        Hits hit = new Hits();

        double[] authority = new double[6];
        double[] hubs = new double[6];

        HashMap<Node, Integer> indices = hit.createIndicesMap(graph);

        hit.calculateHits(graph, hubs, authority, indices, true, EPSILON);

        int index1 = indices.get(node1);
        int index2 = indices.get(node2);
        int index3 = indices.get(node3);
        int index5 = indices.get(node5);
        int index6 = indices.get(node6);

        double hub2 = hubs[index2];
        double hub3 = hubs[index3];
        double hub5 = hubs[index5];
        double hub6 = hubs[index6];
        double auth1 = authority[index1];
        double auth3 = authority[index3];
        double auth6 = authority[index6];

        assertEquals(hub3, hub5);
        assertTrue(hub3 > hub2);
        assertTrue(auth1 > auth6);
        assertEquals(hub6, 0.0);
        assertEquals(auth3, 0.0);
    }

    @Test
    public void testDirectedSpecial3GraphHits() {
        GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getGraphModel();

        DirectedGraph directedGraph = graphModel.getDirectedGraph();
        Node node1 = graphModel.factory().newNode("0");
        Node node2 = graphModel.factory().newNode("1");
        Node node3 = graphModel.factory().newNode("2");
        Node node4 = graphModel.factory().newNode("3");
        Node node5 = graphModel.factory().newNode("4");
        Node node6 = graphModel.factory().newNode("5");

        directedGraph.addNode(node1);
        directedGraph.addNode(node2);
        directedGraph.addNode(node3);
        directedGraph.addNode(node4);
        directedGraph.addNode(node5);
        directedGraph.addNode(node6);

        Edge edge15 = graphModel.factory().newEdge(node1, node5);
        Edge edge25 = graphModel.factory().newEdge(node2, node5);
        Edge edge35 = graphModel.factory().newEdge(node3, node5);
        Edge edge45 = graphModel.factory().newEdge(node4, node5);
        Edge edge56 = graphModel.factory().newEdge(node5, node6);

        directedGraph.addEdge(edge15);
        directedGraph.addEdge(edge25);
        directedGraph.addEdge(edge35);
        directedGraph.addEdge(edge45);
        directedGraph.addEdge(edge56);

        DirectedGraph graph = graphModel.getDirectedGraph();
        Hits hit = new Hits();

        double[] authority = new double[6];
        double[] hubs = new double[6];

        HashMap<Node, Integer> indices = hit.createIndicesMap(graph);

        hit.calculateHits(graph, hubs, authority, indices, true, EPSILON);

        int index1 = indices.get(node1);
        int index3 = indices.get(node3);
        int index5 = indices.get(node5);
        int index6 = indices.get(node6);

        double hub1 = hubs[index1];
        double hub3 = hubs[index3];
        double hub5 = hubs[index5];
        double auth5 = authority[index5];
        double auth6 = authority[index6];

        assertEquals(hub1, hub3);
        assertTrue(hub1 > hub5);
        assertTrue(auth5 > auth6);
    }

    @Test
    public void testExampleDirectedGraph() {
        GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getGraphModel();

        DirectedGraph directedGraph = graphModel.getDirectedGraph();
        Node node1 = graphModel.factory().newNode("0");
        Node node2 = graphModel.factory().newNode("1");
        Node node3 = graphModel.factory().newNode("2");
        Node node4 = graphModel.factory().newNode("3");

        directedGraph.addNode(node1);
        directedGraph.addNode(node2);
        directedGraph.addNode(node3);
        directedGraph.addNode(node4);

        Edge edge12 = graphModel.factory().newEdge(node1, node2);
        Edge edge13 = graphModel.factory().newEdge(node1, node3);
        Edge edge14 = graphModel.factory().newEdge(node1, node4);
        
        Edge edge23 = graphModel.factory().newEdge(node2, node3);
        Edge edge24 = graphModel.factory().newEdge(node2, node4);
        
        Edge edge32 = graphModel.factory().newEdge(node3, node2);

        directedGraph.addEdge(edge12);
        directedGraph.addEdge(edge13);
        directedGraph.addEdge(edge14);
        directedGraph.addEdge(edge23);
        directedGraph.addEdge(edge24);
        directedGraph.addEdge(edge32);

        DirectedGraph graph = graphModel.getDirectedGraph();
        Hits hit = new Hits();

        double[] authority = new double[4];
        double[] hubs = new double[4];

        HashMap<Node, Integer> indices = hit.createIndicesMap(graph);

        hit.calculateHits(graph, hubs, authority, indices, true, EPSILON);

        int index1 = indices.get(node1);
        int index2 = indices.get(node2);
        int index3 = indices.get(node3);
        int index4 = indices.get(node4);
        
        assertEquals(hubs[index1], 0.7887);
        assertEquals(hubs[index2], 0.5774);
        assertEquals(hubs[index3], 0.2113);
        assertEquals(hubs[index4], 0);
        
        assertEquals(authority[index1], 0);
        assertEquals(authority[index2], 0.4597);
        assertEquals(authority[index3], 0.6280);
        assertEquals(authority[index4], 0.6280);
    }
    
    private void assertEquals(double a, double b){
        Assert.assertEquals(a, b, EPSILON);
    }
}
