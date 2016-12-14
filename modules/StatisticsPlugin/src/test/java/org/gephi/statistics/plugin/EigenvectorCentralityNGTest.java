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

import java.util.HashMap;
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
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author Anna
 */
public class EigenvectorCentralityNGTest {

    private ProjectController pc;

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
    public void testOneNodeEigenvectorCentrality() {
        GraphModel graphModel = GraphGenerator.generateNullUndirectedGraph(1);
        UndirectedGraph graph = graphModel.getUndirectedGraph();

        EigenvectorCentrality ec = new EigenvectorCentrality();

        double[] centralities = new double[1];

        HashMap<Integer, Node> indicies = new HashMap();
        HashMap<Node, Integer> invIndicies = new HashMap();

        ec.fillIndiciesMaps(graph, centralities, indicies, invIndicies);

        ec.calculateEigenvectorCentrality(graph, centralities, indicies, invIndicies, false, 100);

        Node n1 = graph.getNode("0");
        int index = invIndicies.get(n1);
        double ec1 = centralities[index];

        assertEquals(ec1, 0.0);
    }

    @Test
    public void testTwoConnectedNodesEigenvectorCentrality() {
        GraphModel graphModel = GraphGenerator.generateCompleteUndirectedGraph(2);
        UndirectedGraph graph = graphModel.getUndirectedGraph();

        EigenvectorCentrality ec = new EigenvectorCentrality();

        double[] centralities = new double[2];

        HashMap<Integer, Node> indicies = new HashMap();
        HashMap<Node, Integer> invIndicies = new HashMap();

        ec.fillIndiciesMaps(graph, centralities, indicies, invIndicies);

        ec.calculateEigenvectorCentrality(graph, centralities, indicies, invIndicies, false, 100);

        Node n1 = graph.getNode("0");
        int index = invIndicies.get(n1);
        double ec1 = centralities[index];

        assertEquals(ec1, 1.0);
    }

    @Test
    public void testNullGraphEigenvectorCentrality() {
        GraphModel graphModel = GraphGenerator.generateNullUndirectedGraph(5);
        UndirectedGraph graph = graphModel.getUndirectedGraph();

        EigenvectorCentrality ec = new EigenvectorCentrality();
        ec.setDirected(false);

        double[] centralities = new double[5];

        HashMap<Integer, Node> indicies = new HashMap();
        HashMap<Node, Integer> invIndicies = new HashMap();

        ec.fillIndiciesMaps(graph, centralities, indicies, invIndicies);

        ec.calculateEigenvectorCentrality(graph, centralities, indicies, invIndicies, false, 100);

        Node n2 = graph.getNode("1");
        int index = invIndicies.get(n2);
        double ec2 = centralities[index];

        assertEquals(ec2, 0.0);
    }

    @Test
    public void testCompleteUndirectedGraphEigenvectorCentrality() {
        GraphModel graphModel = GraphGenerator.generateCompleteUndirectedGraph(5);
        UndirectedGraph graph = graphModel.getUndirectedGraph();

        EigenvectorCentrality ec = new EigenvectorCentrality();
        ec.setDirected(false);

        double[] centralities = new double[5];

        HashMap<Integer, Node> indicies = new HashMap();
        HashMap<Node, Integer> invIndicies = new HashMap();

        ec.fillIndiciesMaps(graph, centralities, indicies, invIndicies);

        ec.calculateEigenvectorCentrality(graph, centralities, indicies, invIndicies, false, 100);

        Node n1 = graph.getNode("0");
        Node n3 = graph.getNode("2");
        int index1 = invIndicies.get(n1);
        int index3 = invIndicies.get(n3);
        double ec1 = centralities[index1];
        double ec3 = centralities[index3];

        assertEquals(ec1, 1.0);
        assertEquals(ec3, 1.0);
    }

    @Test
    public void testSpecial1UndirectedGraphEigenvectorCentrlity() {
        GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getGraphModel();
        UndirectedGraph undirectedGraph = graphModel.getUndirectedGraph();

        Node node1 = graphModel.factory().newNode("0");
        Node node2 = graphModel.factory().newNode("1");
        Node node3 = graphModel.factory().newNode("2");
        Node node4 = graphModel.factory().newNode("3");

        undirectedGraph.addNode(node1);
        undirectedGraph.addNode(node2);
        undirectedGraph.addNode(node3);
        undirectedGraph.addNode(node4);

        Edge edge12 = graphModel.factory().newEdge(node1, node2, false);
        Edge edge23 = graphModel.factory().newEdge(node2, node3, false);
        Edge edge34 = graphModel.factory().newEdge(node3, node4, false);
        Edge edge13 = graphModel.factory().newEdge(node1, node3, false);
        Edge edge24 = graphModel.factory().newEdge(node2, node4, false);

        undirectedGraph.addEdge(edge12);
        undirectedGraph.addEdge(edge23);
        undirectedGraph.addEdge(edge34);
        undirectedGraph.addEdge(edge13);
        undirectedGraph.addEdge(edge24);

        UndirectedGraph graph = graphModel.getUndirectedGraph();
        EigenvectorCentrality ec = new EigenvectorCentrality();

        double[] centralities = new double[4];

        HashMap<Integer, Node> indicies = new HashMap();
        HashMap<Node, Integer> invIndicies = new HashMap();

        ec.fillIndiciesMaps(graph, centralities, indicies, invIndicies);

        ec.calculateEigenvectorCentrality(graph, centralities, indicies, invIndicies, false, 100);

        int index1 = invIndicies.get(node1);
        int index2 = invIndicies.get(node2);
        int index3 = invIndicies.get(node3);
        double ec1 = centralities[index1];
        double ec2 = centralities[index2];
        double ec3 = centralities[index3];

        assertEquals(ec2, ec3);
        assertNotEquals(ec1, ec2);
        assertEquals(ec3, 1.0);
    }

    @Test
    public void testSpecial2UndirectedGraphEigenvectorCentrlity() {
        GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getGraphModel();
        UndirectedGraph undirectedGraph = graphModel.getUndirectedGraph();

        Node node1 = graphModel.factory().newNode("0");
        Node node2 = graphModel.factory().newNode("1");
        Node node3 = graphModel.factory().newNode("2");
        Node node4 = graphModel.factory().newNode("3");
        Node node5 = graphModel.factory().newNode("4");

        undirectedGraph.addNode(node1);
        undirectedGraph.addNode(node2);
        undirectedGraph.addNode(node3);
        undirectedGraph.addNode(node4);
        undirectedGraph.addNode(node5);

        Edge edge13 = graphModel.factory().newEdge(node1, node3, false);
        Edge edge23 = graphModel.factory().newEdge(node2, node3, false);
        Edge edge34 = graphModel.factory().newEdge(node3, node4, false);
        Edge edge45 = graphModel.factory().newEdge(node4, node5, false);

        undirectedGraph.addEdge(edge13);
        undirectedGraph.addEdge(edge23);
        undirectedGraph.addEdge(edge34);
        undirectedGraph.addEdge(edge45);

        UndirectedGraph graph = graphModel.getUndirectedGraph();
        EigenvectorCentrality ec = new EigenvectorCentrality();

        double[] centralities = new double[5];

        HashMap<Integer, Node> indicies = new HashMap();
        HashMap<Node, Integer> invIndicies = new HashMap();

        ec.fillIndiciesMaps(graph, centralities, indicies, invIndicies);

        ec.calculateEigenvectorCentrality(graph, centralities, indicies, invIndicies, false, 100);

        int index2 = invIndicies.get(node2);
        int index3 = invIndicies.get(node3);
        int index4 = invIndicies.get(node4);
        double ec2 = centralities[index2];
        double ec3 = centralities[index3];
        double ec4 = centralities[index4];

        double res = 0.765;
        double diff = 0.01;

        assertTrue(ec2 < ec3);
        assertTrue(Math.abs(ec4 - res) < diff);
    }

    @Test
    public void testSpecial3UndirectedGraphEigenvectorCentrlity() {
        GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getGraphModel();
        UndirectedGraph undirectedGraph = graphModel.getUndirectedGraph();

        Node node1 = graphModel.factory().newNode("0");
        Node node2 = graphModel.factory().newNode("1");
        Node node3 = graphModel.factory().newNode("2");

        undirectedGraph.addNode(node1);
        undirectedGraph.addNode(node2);
        undirectedGraph.addNode(node3);

        Edge edge11 = graphModel.factory().newEdge(node1, node1, false);
        Edge edge12 = graphModel.factory().newEdge(node1, node2, false);
        Edge edge23 = graphModel.factory().newEdge(node2, node3, false);
        Edge edge33 = graphModel.factory().newEdge(node3, node3, false);

        undirectedGraph.addEdge(edge11);
        undirectedGraph.addEdge(edge12);
        undirectedGraph.addEdge(edge23);
        undirectedGraph.addEdge(edge33);

        UndirectedGraph graph = graphModel.getUndirectedGraph();
        EigenvectorCentrality ec = new EigenvectorCentrality();

        double[] centralities = new double[3];

        HashMap<Integer, Node> indicies = new HashMap();
        HashMap<Node, Integer> invIndicies = new HashMap();

        ec.fillIndiciesMaps(graph, centralities, indicies, invIndicies);

        ec.calculateEigenvectorCentrality(graph, centralities, indicies, invIndicies, false, 100);

        int index1 = invIndicies.get(node1);
        int index2 = invIndicies.get(node2);
        double ec1 = centralities[index1];
        double ec2 = centralities[index2];

        assertEquals(ec1, ec2);
    }

    @Test
    public void testCyclicDirectedGraphEigenvectorCentrality() {
        GraphModel graphModel = GraphGenerator.generateCyclicDirectedGraph(3);
        DirectedGraph graph = graphModel.getDirectedGraph();

        EigenvectorCentrality ec = new EigenvectorCentrality();

        double[] centralities = new double[3];

        HashMap<Integer, Node> indicies = new HashMap();
        HashMap<Node, Integer> invIndicies = new HashMap();

        ec.fillIndiciesMaps(graph, centralities, indicies, invIndicies);

        ec.calculateEigenvectorCentrality(graph, centralities, indicies, invIndicies, true, 100);

        Node n1 = graph.getNode("0");
        int index1 = invIndicies.get(n1);
        double ec1 = centralities[index1];

        assertEquals(ec1, 1.0);
    }

    @Test
    public void testSpecial1DirectedEigenvectorCentrality() {
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

        Edge edge12 = graphModel.factory().newEdge(node1, node2);
        Edge edge23 = graphModel.factory().newEdge(node2, node3);
        Edge edge31 = graphModel.factory().newEdge(node3, node1);
        Edge edge42 = graphModel.factory().newEdge(node4, node2);
        Edge edge54 = graphModel.factory().newEdge(node5, node4);

        directedGraph.addEdge(edge12);
        directedGraph.addEdge(edge23);
        directedGraph.addEdge(edge31);
        directedGraph.addEdge(edge42);
        directedGraph.addEdge(edge54);

        DirectedGraph graph = graphModel.getDirectedGraph();
        EigenvectorCentrality ec = new EigenvectorCentrality();

        double[] centralities = new double[5];

        HashMap<Integer, Node> indicies = new HashMap();
        HashMap<Node, Integer> invIndicies = new HashMap();

        ec.fillIndiciesMaps(graph, centralities, indicies, invIndicies);

        ec.calculateEigenvectorCentrality(graph, centralities, indicies, invIndicies, true, 1000);

        int index1 = invIndicies.get(node1);
        int index2 = invIndicies.get(node2);
        int index4 = invIndicies.get(node4);
        int index5 = invIndicies.get(node5);

        double ec1 = centralities[index1];
        double ec2 = centralities[index2];
        double ec4 = centralities[index4];
        double ec5 = centralities[index5];

        double diff = 0.01;
        double res0 = 0.;
        double res1 = 1.;

        assertEquals(ec5, 0.0);
        assertTrue(Math.abs(ec4 - res0) < diff);
        assertTrue(Math.abs(ec1 - res1) < diff);
        assertTrue(Math.abs(ec1 - ec2) < diff);
    }

    @Test
    public void testDirectedStarOutEigenvectorCentrality() {
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
        EigenvectorCentrality ec = new EigenvectorCentrality();

        double[] centralities = new double[6];

        HashMap<Integer, Node> indicies = new HashMap();
        HashMap<Node, Integer> invIndicies = new HashMap();

        ec.fillIndiciesMaps(graph, centralities, indicies, invIndicies);

        ec.calculateEigenvectorCentrality(graph, centralities, indicies, invIndicies, true, 100);

        Node n1 = graph.getNode("0");
        Node n2 = graph.getNode("1");
        int index1 = invIndicies.get(n1);
        int index2 = invIndicies.get(n2);
        double ec1 = centralities[index1];
        double ec2 = centralities[index2];

        assertEquals(ec1, 0.0);
        assertEquals(ec2, 1.0);
    }

    @Test
    public void testPathDirectedGraphEigenvectorCentrality() {
        GraphModel graphModel = GraphGenerator.generatePathDirectedGraph(4);
        DirectedGraph graph = graphModel.getDirectedGraph();

        EigenvectorCentrality ec = new EigenvectorCentrality();

        double[] centralities = new double[4];

        HashMap<Integer, Node> indicies = new HashMap();
        HashMap<Node, Integer> invIndicies = new HashMap();

        ec.fillIndiciesMaps(graph, centralities, indicies, invIndicies);

        ec.calculateEigenvectorCentrality(graph, centralities, indicies, invIndicies, true, 100);

        Node n1 = graph.getNode("0");
        Node n4 = graph.getNode("3");
        int index1 = invIndicies.get(n1);
        int index4 = invIndicies.get(n4);
        double ec1 = centralities[index1];
        double ec4 = centralities[index4];

        assertEquals(ec1, 0.0);
        assertEquals(ec4, 1.0);
    }
}
