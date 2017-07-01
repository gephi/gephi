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
import java.util.LinkedList;
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
public class ConnectedComponentsNGTest {

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
    public void testComputeOneNodeWeaklyConnectedComponents() {
        GraphModel graphModel = GraphGenerator.generateNullUndirectedGraph(1);
        UndirectedGraph graph = graphModel.getUndirectedGraph();
        Node n = graph.getNode("0");

        ConnectedComponents c = new ConnectedComponents();
        HashMap<Node, Integer> indices = new HashMap<>();
        indices.put(n, 0);
        LinkedList<LinkedList<Node>> components = c.computeWeaklyConnectedComponents(graph, indices);
        assertEquals(components.size(), 1);
    }
    
    @Test
    public void testComputeSelfLoopNodeAndIsolatedNodeWeaklyConnectedComponents() {
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
        undirectedGraph.addEdge(edge11);
        undirectedGraph.addEdge(edge12);

        ConnectedComponents c = new ConnectedComponents();
        HashMap<Node, Integer> indices = new HashMap<>();
        
        indices.put(node1, 0);
        indices.put(node2, 1);
        indices.put(node3, 2);
        
        LinkedList<LinkedList<Node>> components = c.computeWeaklyConnectedComponents(undirectedGraph, indices);
        assertEquals(components.size(), 2);
    }

    @Test
    public void testNullGraphWeaklyConnectedComponents() {
        GraphModel graphModel = GraphGenerator.generateNullUndirectedGraph(5);
        UndirectedGraph graph = graphModel.getUndirectedGraph();
        Node n0 = graph.getNode("0");
        Node n1 = graph.getNode("1");
        Node n2 = graph.getNode("2");
        Node n3 = graph.getNode("3");
        Node n4 = graph.getNode("4");

        ConnectedComponents c = new ConnectedComponents();
        HashMap<Node, Integer> indices = new HashMap<>();
        indices.put(n0, 0);
        indices.put(n1, 1);
        indices.put(n2, 2);
        indices.put(n3, 3);
        indices.put(n4, 4);
        LinkedList<LinkedList<Node>> components = c.computeWeaklyConnectedComponents(graph, indices);
        assertEquals(components.size(), 5);
    }

    @Test
    public void testComputeBarbellGraphWeaklyConnectedComponents() {
        GraphModel graphModel = GraphGenerator.generateCompleteUndirectedGraph(4);
        UndirectedGraph undirectedGraph = graphModel.getUndirectedGraph();
        Node[] nodes = new Node[4];
        for (int i = 0; i < 4; i++) {
            Node currentNode = graphModel.factory().newNode(((Integer) (i + 4)).toString());
            nodes[i] = currentNode;
            undirectedGraph.addNode(currentNode);
        }
        for (int i = 0; i < 3; i++) {
            for (int j = i + 1; j < 4; j++) {
                Edge currentEdge = graphModel.factory().newEdge(nodes[i], nodes[j], false);
                undirectedGraph.addEdge(currentEdge);
            }
        }
        Edge currentEdge = graphModel.factory().newEdge(undirectedGraph.getNode("0"), undirectedGraph.getNode("5"), false);
        undirectedGraph.addEdge(currentEdge);
        UndirectedGraph graph = graphModel.getUndirectedGraph();

        ConnectedComponents c = new ConnectedComponents();
        HashMap<Node, Integer> indices = c.createIndicesMap(graph);
        LinkedList<LinkedList<Node>> components = c.computeWeaklyConnectedComponents(graph, indices);
        assertEquals(components.size(), 1);
    }

    @Test
    public void testSpecial1UndirectedGraphConnectedComponents() {
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
        Edge edge12 = graphModel.factory().newEdge(node1, node2, false);
        Edge edge14 = graphModel.factory().newEdge(node1, node4, false);
        Edge edge23 = graphModel.factory().newEdge(node2, node3, false);
        Edge edge25 = graphModel.factory().newEdge(node2, node5, false);
        Edge edge35 = graphModel.factory().newEdge(node3, node5, false);
        Edge edge43 = graphModel.factory().newEdge(node4, node3, false);
        Edge edge51 = graphModel.factory().newEdge(node5, node1, false);
        Edge edge54 = graphModel.factory().newEdge(node5, node4, false);
        undirectedGraph.addEdge(edge12);
        undirectedGraph.addEdge(edge14);
        undirectedGraph.addEdge(edge23);
        undirectedGraph.addEdge(edge25);
        undirectedGraph.addEdge(edge35);
        undirectedGraph.addEdge(edge43);
        undirectedGraph.addEdge(edge51);
        undirectedGraph.addEdge(edge54);

        UndirectedGraph graph = graphModel.getUndirectedGraph();

        ConnectedComponents c = new ConnectedComponents();
        HashMap<Node, Integer> indices = c.createIndicesMap(graph);
        LinkedList<LinkedList<Node>> components = c.computeWeaklyConnectedComponents(graph, indices);
        assertEquals(components.size(), 1);
    }

    @Test
    public void testSpecial2UndirectedGraphConnectedComponents() {
        GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getGraphModel();
        UndirectedGraph undirectedGraph = graphModel.getUndirectedGraph();
        Node node1 = graphModel.factory().newNode("0");
        Node node2 = graphModel.factory().newNode("1");
        Node node3 = graphModel.factory().newNode("2");
        Node node4 = graphModel.factory().newNode("3");
        Node node5 = graphModel.factory().newNode("4");
        Node node6 = graphModel.factory().newNode("5");
        Node node7 = graphModel.factory().newNode("6");
        Node node8 = graphModel.factory().newNode("7");
        Node node9 = graphModel.factory().newNode("8");
        undirectedGraph.addNode(node1);
        undirectedGraph.addNode(node2);
        undirectedGraph.addNode(node3);
        undirectedGraph.addNode(node4);
        undirectedGraph.addNode(node5);
        undirectedGraph.addNode(node6);
        undirectedGraph.addNode(node7);
        undirectedGraph.addNode(node8);
        undirectedGraph.addNode(node9);
        Edge edge12 = graphModel.factory().newEdge(node1, node2, false);
        Edge edge23 = graphModel.factory().newEdge(node2, node3, false);
        Edge edge45 = graphModel.factory().newEdge(node4, node5, false);
        Edge edge56 = graphModel.factory().newEdge(node5, node6, false);
        Edge edge64 = graphModel.factory().newEdge(node6, node4, false);
        Edge edge75 = graphModel.factory().newEdge(node7, node5, false);
        undirectedGraph.addEdge(edge12);
        undirectedGraph.addEdge(edge23);
        undirectedGraph.addEdge(edge45);
        undirectedGraph.addEdge(edge56);
        undirectedGraph.addEdge(edge64);
        undirectedGraph.addEdge(edge75);

        UndirectedGraph graph = graphModel.getUndirectedGraph();

        ConnectedComponents c = new ConnectedComponents();
        HashMap<Node, Integer> indices = c.createIndicesMap(graph);
        LinkedList<LinkedList<Node>> components = c.computeWeaklyConnectedComponents(graph, indices);

        int componentNumber3 = c.getComponentNumber(components, node3);
        int componentNumber4 = c.getComponentNumber(components, node4);
        int componentNumber7 = c.getComponentNumber(components, node7);
        int componentNumber8 = c.getComponentNumber(components, node8);

        assertEquals(components.size(), 4);
        assertEquals(componentNumber4, componentNumber7);
        assertNotEquals(componentNumber3, componentNumber8);
    }

    @Test
    public void testDirectedPathGraphConnectedComponents() {
        GraphModel graphModel = GraphGenerator.generatePathDirectedGraph(4);
        DirectedGraph graph = graphModel.getDirectedGraph();

        ConnectedComponents c = new ConnectedComponents();
        HashMap<Node, Integer> indices = c.createIndicesMap(graph);
        LinkedList<LinkedList<Node>> components = c.top_tarjans(graph, indices);
        assertEquals(components.size(), 4);
    }

    @Test
    public void testDirectedCyclicGraphConnectedComponents() {
        GraphModel graphModel = GraphGenerator.generateCyclicDirectedGraph(5);
        DirectedGraph graph = graphModel.getDirectedGraph();

        ConnectedComponents c = new ConnectedComponents();
        HashMap<Node, Integer> indices = c.createIndicesMap(graph);
        LinkedList<LinkedList<Node>> components = c.top_tarjans(graph, indices);
        assertEquals(components.size(), 1);
    }

    @Test
    public void testSpecial1DirectedGraphConnectedComponents() {
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
        Edge edge14 = graphModel.factory().newEdge(node1, node4);
        Edge edge23 = graphModel.factory().newEdge(node2, node3);
        Edge edge25 = graphModel.factory().newEdge(node2, node5);
        Edge edge35 = graphModel.factory().newEdge(node3, node5);
        Edge edge43 = graphModel.factory().newEdge(node4, node3);
        Edge edge51 = graphModel.factory().newEdge(node5, node1);
        Edge edge54 = graphModel.factory().newEdge(node5, node4);
        directedGraph.addEdge(edge12);
        directedGraph.addEdge(edge14);
        directedGraph.addEdge(edge23);
        directedGraph.addEdge(edge25);
        directedGraph.addEdge(edge35);
        directedGraph.addEdge(edge43);
        directedGraph.addEdge(edge51);
        directedGraph.addEdge(edge54);

        DirectedGraph graph = graphModel.getDirectedGraph();

        ConnectedComponents c = new ConnectedComponents();
        HashMap<Node, Integer> indices = c.createIndicesMap(graph);
        LinkedList<LinkedList<Node>> components = c.top_tarjans(graph, indices);
        assertEquals(components.size(), 1);
    }

    @Test
    public void testSpecial2DirectedGraphConnectedComponents() {
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
        Edge edge14 = graphModel.factory().newEdge(node1, node4);
        Edge edge23 = graphModel.factory().newEdge(node2, node3);
        Edge edge25 = graphModel.factory().newEdge(node2, node5);
        Edge edge35 = graphModel.factory().newEdge(node3, node5);
        Edge edge43 = graphModel.factory().newEdge(node4, node3);
        Edge edge54 = graphModel.factory().newEdge(node5, node4);
        directedGraph.addEdge(edge12);
        directedGraph.addEdge(edge14);
        directedGraph.addEdge(edge23);
        directedGraph.addEdge(edge25);
        directedGraph.addEdge(edge35);
        directedGraph.addEdge(edge43);
        directedGraph.addEdge(edge54);

        DirectedGraph graph = graphModel.getDirectedGraph();

        ConnectedComponents c = new ConnectedComponents();
        HashMap<Node, Integer> indices = c.createIndicesMap(graph);
        LinkedList<LinkedList<Node>> weeklyConnectedComponents = c.computeWeaklyConnectedComponents(graph, indices);
        LinkedList<LinkedList<Node>> stronglyConnectedComponents = c.top_tarjans(graph, indices);
        int componentNumber1 = c.getComponentNumber(stronglyConnectedComponents, node1);
        int componentNumber3 = c.getComponentNumber(stronglyConnectedComponents, node3);
        int componentNumber4 = c.getComponentNumber(stronglyConnectedComponents, node4);
        int componentNumber5 = c.getComponentNumber(stronglyConnectedComponents, node5);

        assertEquals(stronglyConnectedComponents.size(), 3);
        assertEquals(weeklyConnectedComponents.size(), 1);
        assertEquals(componentNumber3, componentNumber5);
        assertNotEquals(componentNumber1, componentNumber4);
    }

    @Test
    public void testSpecial3DirectedGraphConnectedComponents() {
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
        Edge edge45 = graphModel.factory().newEdge(node4, node5);
        Edge edge56 = graphModel.factory().newEdge(node5, node6);
        Edge edge64 = graphModel.factory().newEdge(node6, node4);
        Edge edge75 = graphModel.factory().newEdge(node7, node5);
        Edge edge89 = graphModel.factory().newEdge(node8, node9);
        Edge edge98 = graphModel.factory().newEdge(node9, node8);
        directedGraph.addEdge(edge12);
        directedGraph.addEdge(edge23);
        directedGraph.addEdge(edge45);
        directedGraph.addEdge(edge56);
        directedGraph.addEdge(edge64);
        directedGraph.addEdge(edge75);
        directedGraph.addEdge(edge89);
        directedGraph.addEdge(edge98);

        DirectedGraph graph = graphModel.getDirectedGraph();

        ConnectedComponents c = new ConnectedComponents();
        HashMap<Node, Integer> indices = c.createIndicesMap(graph);
        LinkedList<LinkedList<Node>> stronglyConnectedComponents = c.top_tarjans(graph, indices);

        assertEquals(stronglyConnectedComponents.size(), 6);
    }

    @Test
    public void testSpecial4DirectedGraphConnectedComponents() {
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
        directedGraph.addNode(node1);
        directedGraph.addNode(node2);
        directedGraph.addNode(node3);
        directedGraph.addNode(node4);
        directedGraph.addNode(node5);
        directedGraph.addNode(node6);
        directedGraph.addNode(node7);
        directedGraph.addNode(node8);
        Edge edge12 = graphModel.factory().newEdge(node1, node2);
        Edge edge23 = graphModel.factory().newEdge(node2, node3);
        Edge edge34 = graphModel.factory().newEdge(node3, node4);
        Edge edge41 = graphModel.factory().newEdge(node4, node1);
        Edge edge56 = graphModel.factory().newEdge(node5, node6);
        Edge edge67 = graphModel.factory().newEdge(node6, node7);
        Edge edge78 = graphModel.factory().newEdge(node7, node8);
        Edge edge85 = graphModel.factory().newEdge(node8, node5);
        Edge edge45 = graphModel.factory().newEdge(node4, node5);
        directedGraph.addEdge(edge12);
        directedGraph.addEdge(edge23);
        directedGraph.addEdge(edge34);
        directedGraph.addEdge(edge41);
        directedGraph.addEdge(edge56);
        directedGraph.addEdge(edge67);
        directedGraph.addEdge(edge78);
        directedGraph.addEdge(edge85);
        directedGraph.addEdge(edge45);

        DirectedGraph graph = graphModel.getDirectedGraph();

        ConnectedComponents c = new ConnectedComponents();
        HashMap<Node, Integer> indices = c.createIndicesMap(graph);
        LinkedList<LinkedList<Node>> stronglyConnectedComponents = c.top_tarjans(graph, indices);

        int componentNumber1 = c.getComponentNumber(stronglyConnectedComponents, node1);
        int componentNumber5 = c.getComponentNumber(stronglyConnectedComponents, node5);

        assertEquals(stronglyConnectedComponents.size(), 2);
        assertNotEquals(componentNumber1, componentNumber5);
    }

    @Test
    public void testSpecial2UndirectedGraphGiantComponent() {
        GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getGraphModel();
        UndirectedGraph undirectedGraph = graphModel.getUndirectedGraph();
        Node node1 = graphModel.factory().newNode("0");
        Node node2 = graphModel.factory().newNode("1");
        Node node3 = graphModel.factory().newNode("2");
        Node node4 = graphModel.factory().newNode("3");
        Node node5 = graphModel.factory().newNode("4");
        Node node6 = graphModel.factory().newNode("5");
        Node node7 = graphModel.factory().newNode("6");
        Node node8 = graphModel.factory().newNode("7");
        Node node9 = graphModel.factory().newNode("8");
        undirectedGraph.addNode(node1);
        undirectedGraph.addNode(node2);
        undirectedGraph.addNode(node3);
        undirectedGraph.addNode(node4);
        undirectedGraph.addNode(node5);
        undirectedGraph.addNode(node6);
        undirectedGraph.addNode(node7);
        undirectedGraph.addNode(node8);
        undirectedGraph.addNode(node9);
        Edge edge12 = graphModel.factory().newEdge(node1, node2, false);
        Edge edge23 = graphModel.factory().newEdge(node2, node3, false);
        Edge edge45 = graphModel.factory().newEdge(node4, node5, false);
        Edge edge56 = graphModel.factory().newEdge(node5, node6, false);
        Edge edge64 = graphModel.factory().newEdge(node6, node4, false);
        Edge edge75 = graphModel.factory().newEdge(node7, node5, false);
        undirectedGraph.addEdge(edge12);
        undirectedGraph.addEdge(edge23);
        undirectedGraph.addEdge(edge45);
        undirectedGraph.addEdge(edge56);
        undirectedGraph.addEdge(edge64);
        undirectedGraph.addEdge(edge75);

        UndirectedGraph graph = graphModel.getUndirectedGraph();

        ConnectedComponents c = new ConnectedComponents();
        HashMap<Node, Integer> indices = c.createIndicesMap(graph);
        LinkedList<LinkedList<Node>> components = c.computeWeaklyConnectedComponents(graph, indices);
        c.fillComponentSizeList(components);

        int giantComponent = c.getGiantComponent();
        int componentNumber5 = c.getComponentNumber(components, node5);

        assertEquals(giantComponent, componentNumber5);
    }
}
