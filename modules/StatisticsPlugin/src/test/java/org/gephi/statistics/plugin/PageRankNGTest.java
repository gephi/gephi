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
public class PageRankNGTest {

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
    public void testOneNodePageRank() {
        pc.newProject();
        GraphModel graphModel = GraphGenerator.generateNullUndirectedGraph(1);
        UndirectedGraph graph = graphModel.getUndirectedGraph();

        PageRank pr = new PageRank();

        double[] pageRank;

        HashMap<Node, Integer> indicies = pr.createIndiciesMap(graph);

        pageRank = pr.calculatePagerank(graph, indicies, false, false, 0.001, 0.85);

        Node n1 = graph.getNode("0");
        int index = indicies.get(n1);
        double pr1 = pageRank[index];

        assertEquals(pr1, 1.0);
    }

    @Test
    public void testTwoConnectedNodesPageRank() {
        pc.newProject();
        GraphModel graphModel = GraphGenerator.generatePathUndirectedGraph(2);
        UndirectedGraph graph = graphModel.getUndirectedGraph();

        PageRank pr = new PageRank();

        double[] pageRank;

        HashMap<Node, Integer> indicies = pr.createIndiciesMap(graph);

        pageRank = pr.calculatePagerank(graph, indicies, false, false, 0.001, 0.85);

        Node n2 = graph.getNode("1");
        int index = indicies.get(n2);
        double pr2 = pageRank[index];

        assertEquals(pr2, 0.5);
    }

    @Test
    public void testNullGraphPageRank() {
        pc.newProject();
        GraphModel graphModel = GraphGenerator.generateNullUndirectedGraph(5);
        UndirectedGraph graph = graphModel.getUndirectedGraph();

        PageRank pr = new PageRank();

        double[] pageRank;

        HashMap<Node, Integer> indicies = pr.createIndiciesMap(graph);

        pageRank = pr.calculatePagerank(graph, indicies, false, false, 0.001, 0.85);

        Node n1 = graph.getNode("0");
        Node n4 = graph.getNode("3");
        int index1 = indicies.get(n1);
        int index4 = indicies.get(n4);
        double pr1 = pageRank[index1];
        double pr4 = pageRank[index4];
        double res = 0.2d;

        double diff1 = Math.abs(pr1 - res);
        double diff4 = Math.abs(pr4 - res);
        assertTrue(diff1 < 0.01);
        assertTrue(diff4 < 0.01);
        assertEquals(pr1, pr4);
    }

    @Test
    public void testCompleteGraphPageRank() {
        pc.newProject();
        GraphModel graphModel = GraphGenerator.generateCompleteUndirectedGraph(5);
        UndirectedGraph graph = graphModel.getUndirectedGraph();

        PageRank pr = new PageRank();

        double[] pageRank;

        HashMap<Node, Integer> indicies = pr.createIndiciesMap(graph);

        pageRank = pr.calculatePagerank(graph, indicies, false, false, 0.001, 0.85);

        Node n2 = graph.getNode("2");
        int index2 = indicies.get(n2);
        double pr2 = pageRank[index2];
        double res = 0.2d;

        double diff2 = Math.abs(pr2 - res);
        assertTrue(diff2 < 0.01);
    }

    @Test
    public void testCyclicGraphPageRank() {
        pc.newProject();
        GraphModel graphModel = GraphGenerator.generateCyclicUndirectedGraph(6);
        UndirectedGraph graph = graphModel.getUndirectedGraph();

        PageRank pr = new PageRank();

        double[] pageRank;

        HashMap<Node, Integer> indicies = pr.createIndiciesMap(graph);

        pageRank = pr.calculatePagerank(graph, indicies, false, false, 0.001, 0.6);

        Node n4 = graph.getNode("3");
        int index4 = indicies.get(n4);
        double pr4 = pageRank[index4];
        double res = 0.1667;

        double diff4 = Math.abs(pr4 - res);
        assertTrue(diff4 < 0.01);
    }

    @Test
    public void testStarGraphPageRank() {
        pc.newProject();
        GraphModel graphModel = GraphGenerator.generateStarUndirectedGraph(5);
        UndirectedGraph graph = graphModel.getUndirectedGraph();

        PageRank pr = new PageRank();

        double[] pageRank;

        HashMap<Node, Integer> indicies = pr.createIndiciesMap(graph);

        pageRank = pr.calculatePagerank(graph, indicies, false, false, 0.001, 0.6);

        Node n1 = graph.getNode("0");
        Node n2 = graph.getNode("1");
        Node n3 = graph.getNode("2");
        Node n4 = graph.getNode("3");
        Node n5 = graph.getNode("4");
        Node n6 = graph.getNode("5");

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

        boolean oneMoreThree = pr1 > pr3;
        double res = 1.;
        double diff = 0.01;
        double sum = pr1 + pr2 + pr3 + pr4 + pr5 + pr6;

        assertTrue(oneMoreThree);
        assertEquals(pr2, pr4);
        assertTrue(Math.abs(sum - res) < diff);
    }

    @Test
    public void testPathDirectedGraphPageRank() {
        pc.newProject();
        GraphModel graphModel = GraphGenerator.generatePathDirectedGraph(4);
        DirectedGraph graph = graphModel.getDirectedGraph();

        PageRank pr = new PageRank();

        double[] pageRank;

        HashMap<Node, Integer> indicies = pr.createIndiciesMap(graph);

        pageRank = pr.calculatePagerank(graph, indicies, true, false, 0.001, 0.85);

        Node n1 = graph.getNode("0");
        Node n2 = graph.getNode("1");
        Node n3 = graph.getNode("2");
        Node n4 = graph.getNode("3");

        int index1 = indicies.get(n1);
        int index2 = indicies.get(n2);
        int index3 = indicies.get(n3);
        int index4 = indicies.get(n4);

        double pr1 = pageRank[index1];
        double pr2 = pageRank[index2];
        double pr3 = pageRank[index3];
        double pr4 = pageRank[index4];

        double res = 1.;
        double diff = 0.01;
        double sum = pr1 + pr2 + pr3 + pr4;

        assertTrue(pr1 < pr2);
        assertTrue(pr2 < pr4);
        assertTrue(Math.abs(sum - res) < diff);
    }

    @Test
    public void testCyclicDirectedGraphPageRank() {
        pc.newProject();
        GraphModel graphModel = GraphGenerator.generateCyclicDirectedGraph(5);
        DirectedGraph graph = graphModel.getDirectedGraph();

        PageRank pr = new PageRank();

        double[] pageRank;

        HashMap<Node, Integer> indicies = pr.createIndiciesMap(graph);

        pageRank = pr.calculatePagerank(graph, indicies, true, false, 0.001, 0.85);

        Node n3 = graph.getNode("2");

        int index3 = indicies.get(n3);

        double pr3 = pageRank[index3];
        double res = 0.2d;

        double diff3 = Math.abs(pr3 - res);
        assertTrue(diff3 < 0.01);
    }

    @Test
    public void testDirectedSpecial1GraphPageRank() {
        pc.newProject();
        GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getGraphModel();

        DirectedGraph directedGraph = graphModel.getDirectedGraph();
        Node node1 = graphModel.factory().newNode("0");
        Node node2 = graphModel.factory().newNode("1");
        Node node3 = graphModel.factory().newNode("2");
        Node node4 = graphModel.factory().newNode("3");
        Node node5 = graphModel.factory().newNode("4");
        Node node6 = graphModel.factory().newNode("5");
        Node node7 = graphModel.factory().newNode("6");
        Node node8 = graphModel.factory().newNode("7");
        Node node9 = graphModel.factory().newNode("8");

        directedGraph.addNode(node1);
        directedGraph.addNode(node2);
        directedGraph.addNode(node3);
        directedGraph.addNode(node4);
        directedGraph.addNode(node5);
        directedGraph.addNode(node6);
        directedGraph.addNode(node7);
        directedGraph.addNode(node8);
        directedGraph.addNode(node9);

        Edge edge12 = graphModel.factory().newEdge(node1, node2);
        Edge edge23 = graphModel.factory().newEdge(node2, node3);
        Edge edge31 = graphModel.factory().newEdge(node3, node1);
        Edge edge14 = graphModel.factory().newEdge(node1, node4);
        Edge edge45 = graphModel.factory().newEdge(node4, node5);
        Edge edge51 = graphModel.factory().newEdge(node5, node1);
        Edge edge16 = graphModel.factory().newEdge(node1, node6);
        Edge edge67 = graphModel.factory().newEdge(node6, node7);
        Edge edge71 = graphModel.factory().newEdge(node7, node1);
        Edge edge18 = graphModel.factory().newEdge(node1, node8);
        Edge edge89 = graphModel.factory().newEdge(node8, node9);
        Edge edge91 = graphModel.factory().newEdge(node9, node1);

        directedGraph.addEdge(edge12);
        directedGraph.addEdge(edge23);
        directedGraph.addEdge(edge31);
        directedGraph.addEdge(edge14);
        directedGraph.addEdge(edge45);
        directedGraph.addEdge(edge51);
        directedGraph.addEdge(edge16);
        directedGraph.addEdge(edge67);
        directedGraph.addEdge(edge71);
        directedGraph.addEdge(edge18);
        directedGraph.addEdge(edge89);
        directedGraph.addEdge(edge91);

        DirectedGraph graph = graphModel.getDirectedGraph();
        PageRank pr = new PageRank();

        double[] pageRank;

        HashMap<Node, Integer> indicies = pr.createIndiciesMap(graph);

        pageRank = pr.calculatePagerank(graph, indicies, true, false, 0.001, 0.85);

        int index1 = indicies.get(node1);
        int index2 = indicies.get(node2);
        int index3 = indicies.get(node3);

        double pr1 = pageRank[index1];
        double pr2 = pageRank[index2];
        double pr3 = pageRank[index3];

        assertTrue(pr1 > pr2);
        assertTrue(pr2 < pr3);
    }

    @Test
    public void testDirectedStarOutGraphPageRank() {
        pc.newProject();
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
        PageRank pr = new PageRank();

        double[] pageRank;

        HashMap<Node, Integer> indicies = pr.createIndiciesMap(graph);

        pageRank = pr.calculatePagerank(graph, indicies, true, false, 0.001, 0.85);

        Node n1 = graph.getNode("0");
        Node n2 = graph.getNode("1");
        Node n3 = graph.getNode("2");
        Node n5 = graph.getNode("4");

        int index1 = indicies.get(n1);
        int index2 = indicies.get(n2);
        int index3 = indicies.get(n3);
        int index5 = indicies.get(n5);

        double pr1 = pageRank[index1];
        double pr2 = pageRank[index2];
        double pr3 = pageRank[index3];
        double pr5 = pageRank[index5];

        double res = 0.146;
        double diff = 0.01;

        assertTrue(pr1 < pr3);
        assertEquals(pr2, pr5);
        assertTrue(Math.abs(pr1 - res) < diff);
    }

    @Test
    public void testUndirectedWeightedGraphPageRank() {
        pc.newProject();
        GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getGraphModel();

        UndirectedGraph undirectedGraph = graphModel.getUndirectedGraph();
        Node node1 = graphModel.factory().newNode("0");
        Node node2 = graphModel.factory().newNode("1");
        Node node3 = graphModel.factory().newNode("2");
        Node node4 = graphModel.factory().newNode("3");
        Node node5 = graphModel.factory().newNode("4");
        Node node6 = graphModel.factory().newNode("5");

        undirectedGraph.addNode(node1);
        undirectedGraph.addNode(node2);
        undirectedGraph.addNode(node3);
        undirectedGraph.addNode(node4);
        undirectedGraph.addNode(node5);
        undirectedGraph.addNode(node6);

        Edge edge12 = graphModel.factory().newEdge(node1, node2, false);
        Edge edge23 = graphModel.factory().newEdge(node2, node3, 0, 10, false);
        Edge edge34 = graphModel.factory().newEdge(node3, node4, false);
        Edge edge45 = graphModel.factory().newEdge(node4, node5, false);
        Edge edge56 = graphModel.factory().newEdge(node5, node6, false);
        Edge edge61 = graphModel.factory().newEdge(node6, node1, false);

        undirectedGraph.addEdge(edge12);
        undirectedGraph.addEdge(edge23);
        undirectedGraph.addEdge(edge34);
        undirectedGraph.addEdge(edge45);
        undirectedGraph.addEdge(edge56);
        undirectedGraph.addEdge(edge61);

        UndirectedGraph graph = graphModel.getUndirectedGraph();
        PageRank pr = new PageRank();

        double[] pageRank;

        HashMap<Node, Integer> indicies = pr.createIndiciesMap(graph);

        pageRank = pr.calculatePagerank(graph, indicies, false, true, 0.001, 0.85);

        int index1 = indicies.get(node1);
        int index2 = indicies.get(node2);
        int index3 = indicies.get(node3);
        int index6 = indicies.get(node6);

        double diff = 0.01;

        double pr1 = pageRank[index1];
        double pr2 = pageRank[index2];
        double pr3 = pageRank[index3];
        double pr6 = pageRank[index6];

        assertTrue(Math.abs(pr2 - pr3) < diff);
        assertTrue(pr1 < pr2);
        assertTrue(pr1 < pr6);
    }
}
