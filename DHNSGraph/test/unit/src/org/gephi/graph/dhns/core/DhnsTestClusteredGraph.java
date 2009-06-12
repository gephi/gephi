/*
Copyright 2008 WebAtlas
Authors : Mathieu Bastian, Mathieu Jacomy, Julian Bilcke
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gephi.graph.dhns.core;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.gephi.graph.api.ClusteredGraph;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Node;
import org.gephi.graph.dhns.edge.MetaEdgeImpl;
import org.gephi.graph.dhns.graph.ClusteredDirectedGraphImpl;
import org.gephi.graph.dhns.graph.ClusteredUndirectedGraphImpl;
import org.gephi.graph.dhns.node.PreNode;
import org.gephi.graph.dhns.node.iterators.TreeListIterator;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.openide.util.Exceptions;

/**
 *
 * @author Mathieu
 */
public class DhnsTestClusteredGraph {

    private Dhns dhnsGlobal;
    private ClusteredDirectedGraphImpl graphGlobal;
    private Dhns dhnsGlobal2;
    private ClusteredDirectedGraphImpl graphGlobal2Directed;
    private ClusteredUndirectedGraphImpl graphGlobal2Undirected;
    private Map<String, Node> nodeMap;
    private Map<String, Edge> edgeMap;

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
        dhnsGlobal = new Dhns();
        graphGlobal = new ClusteredDirectedGraphImpl(dhnsGlobal, false);
        nodeMap = new HashMap<String, Node>();
        edgeMap = new HashMap<String, Edge>();

        TreeStructure treeStructure = dhnsGlobal.getTreeStructure();
        GraphFactoryImpl factory = dhnsGlobal.getGraphFactory();

        //Nodes
        for (int i = 0; i < 15; i++) {
            Node node = factory.newNode();
            node.getNodeData().setLabel("Node " + i);
            graphGlobal.addNode(node);
            nodeMap.put(node.getNodeData().getLabel(), node);
        }

        //2
        dhnsGlobal2 = new Dhns();
        graphGlobal2Directed = new ClusteredDirectedGraphImpl(dhnsGlobal2, false);
        graphGlobal2Undirected = new ClusteredUndirectedGraphImpl(dhnsGlobal2, false);
        treeStructure = dhnsGlobal2.getTreeStructure();
        factory = dhnsGlobal2.getGraphFactory();

        //Nodes
        for (int i = 0; i < 3; i++) {
            Node node = factory.newNode();
            graphGlobal2Directed.addNode(node);
        }

        int i = 0;
        for (Node n : graphGlobal2Directed.getTopNodes().toArray()) {
            Node newC = factory.newNode();
            graphGlobal2Directed.addNode(newC, n);
            nodeMap.put("Leaf " + (i++), newC);
            newC = factory.newNode();
            graphGlobal2Directed.addNode(newC, n);
            nodeMap.put("Leaf " + (i++), newC);
        }

        Node leaf2 = nodeMap.get("Leaf 0");
        Node leaf3 = nodeMap.get("Leaf 1");
        Node leaf5 = nodeMap.get("Leaf 2");
        Node leaf6 = nodeMap.get("Leaf 3");
        Node leaf8 = nodeMap.get("Leaf 4");
        Node leaf9 = nodeMap.get("Leaf 5");

        graphGlobal2Directed.addEdge(leaf2, leaf5);
        graphGlobal2Directed.addEdge(leaf2, leaf6);
        graphGlobal2Directed.addEdge(leaf3, leaf2);
        graphGlobal2Directed.addEdge(leaf3, leaf5);
        graphGlobal2Directed.addEdge(leaf6, leaf8);
        graphGlobal2Directed.addEdge(leaf9, leaf8);
        graphGlobal2Directed.addEdge(leaf8, leaf9);
        graphGlobal2Directed.addEdge(leaf9, leaf5);
        graphGlobal2Directed.addEdge(leaf5, leaf9);
        graphGlobal2Directed.addEdge(leaf9, leaf9);

    }

    @After
    public void tearDown() {
        nodeMap.clear();
        dhnsGlobal = null;
        graphGlobal = null;
        dhnsGlobal2 = null;
        graphGlobal2Directed = null;
        graphGlobal2Undirected = null;
    }

    @Test
    public void testMoveDurableTreeList() {

        TreeStructure treeStructure = new TreeStructure();

        PreNode p0 = treeStructure.getRoot();
        PreNode p1 = new PreNode(1, 0, 0, 0, null);
        PreNode p2 = new PreNode(2, 0, 0, 0, null);
        PreNode p3 = new PreNode(3, 0, 0, 0, null);
        PreNode p4 = new PreNode(4, 0, 0, 0, null);
        PreNode p5 = new PreNode(5, 0, 0, 0, null);
        PreNode p6 = new PreNode(6, 0, 0, 0, null);
        PreNode p7 = new PreNode(7, 0, 0, 0, null);

        treeStructure.insertAsChild(p1, p0);
        treeStructure.insertAsChild(p2, p1);
        treeStructure.insertAsChild(p4, p0);
        treeStructure.insertAsChild(p5, p4);
        treeStructure.insertAsChild(p6, p4);
        treeStructure.insertAsChild(p3, p1);
        treeStructure.insertAsChild(p7, p0);

        //Test if ID = pre
        for (Iterator<PreNode> itr = treeStructure.getTree().iterator(1); itr.hasNext();) {
            PreNode n = itr.next();
            assertEquals(n.getId(), n.getPre());
        }

        //Move p1 -> p4
        treeStructure.move(p1, p4);

        //Write expected array
        int[] expected = new int[treeStructure.getTreeSize() - 1];
        int index = 0;
        for (Iterator<PreNode> itr = treeStructure.getTree().iterator(1); itr.hasNext();) {
            PreNode n = itr.next();
            expected[index] = n.getId();
        }

        treeStructure.move(p1, p0);
        treeStructure.move(p1, p4);

        int[] actual = new int[treeStructure.getTreeSize() - 1];
        for (Iterator<PreNode> itr = treeStructure.getTree().iterator(1); itr.hasNext();) {
            PreNode n = itr.next();
            actual[index] = n.getId();
        }
        assertArrayEquals(expected, actual);

        treeStructure.move(p3, p4);
        treeStructure.move(p3, p5);

    //treeStructure.showTreeAsTable();
    }

    @Test
    public void testMoveToGroup() {

        TreeStructure treeStructure = dhnsGlobal.getTreeStructure();

        PreNode target = (PreNode) nodeMap.get("Node 10");
        for (int i = 1; i < 5; i++) {
            PreNode ch = (PreNode) nodeMap.get("Node " + i);
            int oldSize = target.size;

            graphGlobal.moveToGroup(ch, target);

            assertEquals(oldSize + 1, target.size);
            assertEquals(ch.parent, target);
            assertEquals(target.getPre() + target.size, ch.getPre());
        }

        for (int i = 1; i < 5; i++) {
            PreNode ch = (PreNode) nodeMap.get("Node " + i);
            int oldSize = target.size;
            graphGlobal.removeFromGroup(ch);

            assertEquals(oldSize - 1, target.size);
            assertEquals(ch.parent, target.parent);
        }

        //Hierarchy consistency
        try {
            checkHierarchy(treeStructure);
        } catch (Exception e) {
            fail(e.getMessage());
        }

    //treeStructure.showTreeAsTable();
    }

    @Test
    public void testGroup() {

        TreeStructure treeStructure = dhnsGlobal.getTreeStructure();
        int oldSize = graphGlobal.getNodeCount();

        Node[] groupArray = new Node[5];
        for (int i = 1; i < 6; i++) {
            groupArray[i - 1] = nodeMap.get("Node " + i);
        }

        PreNode group = (PreNode) graphGlobal.groupNodes(groupArray);

        assertEquals(oldSize + 1, graphGlobal.getNodeCount());
        assertEquals(groupArray.length, group.size);
        assertEquals(treeStructure.treeHeight, group.level);

        int i = 0;
        for (TreeListIterator itr = new TreeListIterator(treeStructure.getTree(), group.getPre()); itr.hasNext();) {
            PreNode node = itr.next();
            assertEquals(group.pre + i, node.getPre());
            i++;
        }
        try {
            checkHierarchy(treeStructure);
        } catch (Exception ex) {
            fail(ex.getMessage());
        }

    //treeStructure.showTreeAsTable();
    }

    @Test
    public void testUnGroup() {
        TreeStructure treeStructure = dhnsGlobal.getTreeStructure();
        int oldSize = graphGlobal.getNodeCount();

        Node[] groupArray = new Node[5];
        for (int i = 1; i < 6; i++) {
            groupArray[i - 1] = nodeMap.get("Node " + i);
        }

        PreNode group = (PreNode) graphGlobal.groupNodes(groupArray);

        graphGlobal.ungroupNodes(group);

        assertEquals(0, group.size);
        for (Node n : groupArray) {
            PreNode pn = (PreNode) n;
            assertEquals(1, pn.level);
            assertSame(treeStructure.getRoot(), pn.parent);
        }

        assertEquals(oldSize, graphGlobal.getNodeCount());

        try {
            checkHierarchy(treeStructure);
        } catch (Exception ex) {
            fail(ex.getMessage());
        }

    //treeStructure.showTreeAsTable();
    }

    @Test
    public void testView() {
        Dhns dhns = new Dhns();
        ClusteredGraph graph = new ClusteredDirectedGraphImpl(dhns, false);

        TreeStructure treeStructure = dhns.getTreeStructure();
        GraphFactoryImpl factory = dhns.getGraphFactory();

        //Nodes
        for (int i = 0; i < 5; i++) {
            Node node = factory.newNode();
            node.getNodeData().setLabel("Node " + i);
            graph.addNode(node);
        }

        for (Node n : graph.getTopNodes().toArray()) {
            Node newC = factory.newNode();
            graph.addNode(newC, n);
        }

        //Test getNodes()
        for (Node n : graph.getNodes()) {
            assertEquals(1, graph.getChildrenCount(n));
        }

        //Test isInView
        for (Node n : graph.getNodes()) {
            assertTrue(graph.isInView(n));
            assertFalse(graph.isInView(graph.getChildren(n).toArray()[0]));
        }

        //Test resetView
        graph.resetView();
        for (Node n : graph.getNodes()) {
            assertEquals(2, graph.getLevel(n));
            assertFalse(graph.isInView(graph.getParent(n)));
        }

        //treeStructure.showTreeAsTable();
        try {
            checkHierarchy(treeStructure);
        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    public void testMetaEdgesDirected() {
        TreeStructure treeStructure = dhnsGlobal2.getTreeStructure();

        Node metaNode0 = graphGlobal2Directed.getTopNodes().toArray()[0];
        Node metaNode1 = graphGlobal2Directed.getTopNodes().toArray()[1];
        Node metaNode2 = graphGlobal2Directed.getTopNodes().toArray()[2];

        Node leaf2 = graphGlobal2Directed.getChildren(metaNode0).toArray()[0];
        Node leaf3 = graphGlobal2Directed.getChildren(metaNode0).toArray()[1];
        Node leaf5 = graphGlobal2Directed.getChildren(metaNode1).toArray()[0];
        Node leaf6 = graphGlobal2Directed.getChildren(metaNode1).toArray()[1];
        Node leaf8 = graphGlobal2Directed.getChildren(metaNode2).toArray()[0];
        Node leaf9 = graphGlobal2Directed.getChildren(metaNode2).toArray()[1];

        //Get meta edges
        Edge[] metaEdges = graphGlobal2Directed.getMetaEdges().toArray();
        assertEquals(3, metaEdges.length);
        MetaEdgeImpl metaEdge01 = (MetaEdgeImpl) metaEdges[0];
        MetaEdgeImpl metaEdge12 = (MetaEdgeImpl) metaEdges[1];
        MetaEdgeImpl metaEdge21 = (MetaEdgeImpl) metaEdges[2];

        assertSame(metaNode0, metaEdge01.getSource());
        assertSame(metaNode1, metaEdge01.getTarget());
        assertSame(metaNode1, metaEdge12.getSource());
        assertSame(metaNode2, metaEdge12.getTarget());
        assertSame(metaNode2, metaEdge21.getSource());
        assertSame(metaNode1, metaEdge21.getTarget());

        //Meta edge content
        Edge[] metaEdge01content = graphGlobal2Directed.getMetaEdgeContent(metaEdge01).toArray();
        assertEquals(3, metaEdge01content.length);
        assertSame(metaEdge01content[0], graphGlobal2Directed.getEdge(leaf2, leaf5));
        assertSame(metaEdge01content[1], graphGlobal2Directed.getEdge(leaf2, leaf6));
        assertSame(metaEdge01content[2], graphGlobal2Directed.getEdge(leaf3, leaf5));

        Edge[] metaEdge12content = graphGlobal2Directed.getMetaEdgeContent(metaEdge12).toArray();
        assertEquals(2, metaEdge12content.length);
        assertSame(metaEdge12content[0], graphGlobal2Directed.getEdge(leaf6, leaf8));
        assertSame(metaEdge12content[1], graphGlobal2Directed.getEdge(leaf5, leaf9));

        Edge[] metaEdge21content = graphGlobal2Directed.getMetaEdgeContent(metaEdge21).toArray();
        assertEquals(1, metaEdge21content.length);
        assertSame(metaEdge21content[0], graphGlobal2Directed.getEdge(leaf9, leaf5));

        //Degree
        assertEquals(2, graphGlobal2Directed.getMetaInDegree(metaNode1));
        assertEquals(1, graphGlobal2Directed.getMetaOutDegree(metaNode1));
        assertEquals(1, graphGlobal2Directed.getMetaOutDegree(metaNode0));
        assertEquals(3, graphGlobal2Directed.getMetaDegree(metaNode1));

        Edge[] metaInEdges1 = graphGlobal2Directed.getMetaInEdges(metaNode1).toArray();
        assertEquals(2, metaInEdges1.length);
        assertSame(metaInEdges1[0], metaEdge01);
        assertSame(metaInEdges1[1], metaEdge21);

        Edge[] metaOutEdges1 = graphGlobal2Directed.getMetaOutEdges(metaNode1).toArray();
        assertEquals(1, metaOutEdges1.length);
        assertSame(metaOutEdges1[0], metaEdge12);

        Edge[] metaEdges1 = graphGlobal2Directed.getMetaEdges(metaNode1).toArray();
        assertEquals(3, metaEdges1.length);
        assertSame(metaEdges1[0], metaEdge12);
        assertSame(metaEdges1[1], metaEdge01);
        assertSame(metaEdges1[2], metaEdge21);

        //Remove edge
        graphGlobal2Directed.removeEdge(graphGlobal2Directed.getEdge(leaf2, leaf5));
        assertEquals(2, graphGlobal2Directed.getMetaEdgeContent(metaEdge01).toArray().length);

        graphGlobal2Directed.removeEdge(graphGlobal2Directed.getEdge(leaf3, leaf2));
        assertEquals(2, graphGlobal2Directed.getMetaEdgeContent(metaEdge01).toArray().length);

        graphGlobal2Directed.removeEdge(graphGlobal2Directed.getEdge(leaf3, leaf5));
        graphGlobal2Directed.removeEdge(graphGlobal2Directed.getEdge(leaf2, leaf6));
        assertEquals(0, graphGlobal2Directed.getMetaEdges(metaNode0).toArray().length);

        graphGlobal2Directed.clearMetaEdges(metaNode1);
        graphGlobal2Directed.clearMetaEdges(metaNode2);

        assertEquals(0, graphGlobal2Directed.getMetaEdges().toArray().length);
        assertEquals(0, graphGlobal2Directed.getMetaEdges(metaNode1).toArray().length);
        assertEquals(0, graphGlobal2Directed.getMetaEdges(metaNode2).toArray().length);

        treeStructure.showTreeAsTable();
        try {
            checkHierarchy(treeStructure);
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Test
    public void testMetaEdgesUndirected() {
        TreeStructure treeStructure = dhnsGlobal2.getTreeStructure();

        Node metaNode0 = graphGlobal2Undirected.getTopNodes().toArray()[0];
        Node metaNode1 = graphGlobal2Undirected.getTopNodes().toArray()[1];
        Node metaNode2 = graphGlobal2Undirected.getTopNodes().toArray()[2];

        Node leaf2 = graphGlobal2Undirected.getChildren(metaNode0).toArray()[0];
        Node leaf3 = graphGlobal2Undirected.getChildren(metaNode0).toArray()[1];
        Node leaf5 = graphGlobal2Undirected.getChildren(metaNode1).toArray()[0];
        Node leaf6 = graphGlobal2Undirected.getChildren(metaNode1).toArray()[1];
        Node leaf8 = graphGlobal2Undirected.getChildren(metaNode2).toArray()[0];
        Node leaf9 = graphGlobal2Undirected.getChildren(metaNode2).toArray()[1];

        //Get meta edges
        Edge[] metaEdges = graphGlobal2Undirected.getMetaEdges().toArray();
        assertEquals(2, metaEdges.length);
        MetaEdgeImpl metaEdge01 = (MetaEdgeImpl) metaEdges[0];
        MetaEdgeImpl metaEdge12 = (MetaEdgeImpl) metaEdges[1];

        assertSame(metaNode0, metaEdge01.getSource());
        assertSame(metaNode1, metaEdge01.getTarget());
        assertSame(metaNode1, metaEdge12.getSource());
        assertSame(metaNode2, metaEdge12.getTarget());

        //Degree
        assertEquals(2, graphGlobal2Undirected.getMetaDegree(metaNode1));
        assertEquals(1, graphGlobal2Undirected.getMetaDegree(metaNode0));
        assertEquals(1, graphGlobal2Undirected.getMetaDegree(metaNode2));

        Edge[] metaEdges1 = graphGlobal2Undirected.getMetaEdges(metaNode1).toArray();
        assertEquals(2, metaEdges1.length);
        assertSame(metaEdges1[0], metaEdge12);
        assertSame(metaEdges1[1], metaEdge01);

        //Meta edge content
        Edge[] metaEdge01content = graphGlobal2Undirected.getMetaEdgeContent(metaEdge01).toArray();
        assertEquals(3, metaEdge01content.length);
        assertSame(metaEdge01content[0], graphGlobal2Undirected.getEdge(leaf2, leaf5));
        assertSame(metaEdge01content[1], graphGlobal2Undirected.getEdge(leaf2, leaf6));
        assertSame(metaEdge01content[2], graphGlobal2Undirected.getEdge(leaf3, leaf5));

        Edge[] metaEdge12content = graphGlobal2Undirected.getMetaEdgeContent(metaEdge12).toArray();
        assertEquals(2, metaEdge12content.length);
        assertSame(metaEdge12content[0], graphGlobal2Undirected.getEdge(leaf6, leaf8));
        assertSame(metaEdge12content[1], graphGlobal2Undirected.getEdge(leaf5, leaf9));

        //Remove edge
        graphGlobal2Undirected.removeEdge(graphGlobal2Undirected.getEdge(leaf2, leaf5));
        assertEquals(2, graphGlobal2Undirected.getMetaEdgeContent(metaEdge01).toArray().length);

        graphGlobal2Undirected.removeEdge(graphGlobal2Undirected.getEdge(leaf3, leaf2));
        assertEquals(2, graphGlobal2Undirected.getMetaEdgeContent(metaEdge01).toArray().length);

        graphGlobal2Undirected.removeEdge(graphGlobal2Undirected.getEdge(leaf3, leaf5));
        graphGlobal2Undirected.removeEdge(graphGlobal2Undirected.getEdge(leaf2, leaf6));
        assertEquals(0, graphGlobal2Undirected.getMetaEdges(metaNode0).toArray().length);

        graphGlobal2Undirected.removeEdge(graphGlobal2Undirected.getEdge(leaf5, leaf9));
        assertEquals(1, graphGlobal2Undirected.getMetaEdgeContent(metaEdge12).toArray().length);

        graphGlobal2Undirected.clearMetaEdges(metaNode1);
        graphGlobal2Undirected.clearMetaEdges(metaNode2);

        assertEquals(0, graphGlobal2Undirected.getMetaEdges().toArray().length);
        assertEquals(0, graphGlobal2Undirected.getMetaEdges(metaNode1).toArray().length);
        assertEquals(0, graphGlobal2Undirected.getMetaEdges(metaNode2).toArray().length);

        treeStructure.showTreeAsTable();
        try {
            checkHierarchy(treeStructure);
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Test
    public void testInnerOuterEdgesDirected() {

        Node metaNode0 = graphGlobal2Directed.getTopNodes().toArray()[0];
        Node metaNode1 = graphGlobal2Directed.getTopNodes().toArray()[1];
        Node metaNode2 = graphGlobal2Directed.getTopNodes().toArray()[2];

        Node leaf2 = graphGlobal2Directed.getChildren(metaNode0).toArray()[0];
        Node leaf3 = graphGlobal2Directed.getChildren(metaNode0).toArray()[1];
        Node leaf5 = graphGlobal2Directed.getChildren(metaNode1).toArray()[0];
        Node leaf6 = graphGlobal2Directed.getChildren(metaNode1).toArray()[1];
        Node leaf8 = graphGlobal2Directed.getChildren(metaNode2).toArray()[0];
        Node leaf9 = graphGlobal2Directed.getChildren(metaNode2).toArray()[1];

        //Inner
        Edge[] innerEdges = graphGlobal2Directed.getInnerEdges(metaNode0).toArray();
        assertEquals(1, innerEdges.length);
        assertSame(graphGlobal2Directed.getEdge(leaf3, leaf2), innerEdges[0]);

        //Outer
        Edge[] outerEdges = graphGlobal2Directed.getOuterEdges(metaNode0).toArray();
        assertEquals(3, outerEdges.length);
        assertSame(graphGlobal2Directed.getEdge(leaf2, leaf5), outerEdges[0]);
        assertSame(graphGlobal2Directed.getEdge(leaf2, leaf6), outerEdges[1]);
        assertSame(graphGlobal2Directed.getEdge(leaf3, leaf5), outerEdges[2]);

        //Inner with self loop
        innerEdges = graphGlobal2Directed.getInnerEdges(metaNode2).toArray();
        assertEquals(3, innerEdges.length);
        assertSame(graphGlobal2Directed.getEdge(leaf8, leaf9), innerEdges[0]);
        assertSame(graphGlobal2Directed.getEdge(leaf9, leaf8), innerEdges[1]);
        assertSame(graphGlobal2Directed.getEdge(leaf9, leaf9), innerEdges[2]);

        //Inner from self loop node
        innerEdges = graphGlobal2Directed.getInnerEdges(leaf9).toArray();
        assertEquals(1, innerEdges.length);
        assertSame(graphGlobal2Directed.getEdge(leaf9, leaf9), innerEdges[0]);

        //Outer with mutual
        System.out.println();
        outerEdges = graphGlobal2Directed.getOuterEdges(metaNode2).toArray();
        assertEquals(3, outerEdges.length);
        assertSame(graphGlobal2Directed.getEdge(leaf6, leaf8), outerEdges[0]);
        assertSame(graphGlobal2Directed.getEdge(leaf9, leaf5), outerEdges[1]);
        assertSame(graphGlobal2Directed.getEdge(leaf5, leaf9), outerEdges[2]);
    }

    @Test
    public void testInnerOuterEdgesUndirected() {

        Node metaNode0 = graphGlobal2Undirected.getTopNodes().toArray()[0];
        Node metaNode1 = graphGlobal2Undirected.getTopNodes().toArray()[1];
        Node metaNode2 = graphGlobal2Undirected.getTopNodes().toArray()[2];

        Node leaf2 = graphGlobal2Undirected.getChildren(metaNode0).toArray()[0];
        Node leaf3 = graphGlobal2Undirected.getChildren(metaNode0).toArray()[1];
        Node leaf5 = graphGlobal2Undirected.getChildren(metaNode1).toArray()[0];
        Node leaf6 = graphGlobal2Undirected.getChildren(metaNode1).toArray()[1];
        Node leaf8 = graphGlobal2Undirected.getChildren(metaNode2).toArray()[0];
        Node leaf9 = graphGlobal2Undirected.getChildren(metaNode2).toArray()[1];

        //Inner
        Edge[] innerEdges = graphGlobal2Undirected.getInnerEdges(metaNode0).toArray();
        assertEquals(1, innerEdges.length);
        assertSame(graphGlobal2Directed.getEdge(leaf3, leaf2), innerEdges[0]);

        //Outer
        Edge[] outerEdges = graphGlobal2Undirected.getOuterEdges(metaNode0).toArray();
        assertEquals(3, outerEdges.length);
        assertSame(graphGlobal2Undirected.getEdge(leaf2, leaf5), outerEdges[0]);
        assertSame(graphGlobal2Undirected.getEdge(leaf2, leaf6), outerEdges[1]);
        assertSame(graphGlobal2Undirected.getEdge(leaf3, leaf5), outerEdges[2]);

        //Inner with self loop
        innerEdges = graphGlobal2Undirected.getInnerEdges(metaNode2).toArray();
        assertEquals(2, innerEdges.length);
        assertSame(graphGlobal2Undirected.getEdge(leaf8, leaf9), innerEdges[0]);
        assertSame(graphGlobal2Undirected.getEdge(leaf9, leaf9), innerEdges[1]);

        //Inner from self loop node
        innerEdges = graphGlobal2Undirected.getInnerEdges(leaf9).toArray();
        assertEquals(1, innerEdges.length);
        assertSame(graphGlobal2Undirected.getEdge(leaf9, leaf9), innerEdges[0]);

        //Outer with mutual
        outerEdges = graphGlobal2Undirected.getOuterEdges(metaNode2).toArray();
        assertEquals(2, outerEdges.length);
        assertSame(graphGlobal2Undirected.getEdge(leaf6, leaf8), outerEdges[0]);
        assertSame(graphGlobal2Undirected.getEdge(leaf5, leaf9), outerEdges[1]);
    }

    public void checkHierarchy(TreeStructure treeStructure) throws Exception {

        int count = 0;
        PreNode[] array = new PreNode[treeStructure.getTreeSize()];

        //Pre test
        for (TreeListIterator itr = new TreeListIterator(treeStructure.getTree()); itr.hasNext();) {
            PreNode node = itr.next();

            assertEquals("node pre test", node.pre, count);
            array[count] = node;
            count++;
        }

        //Post test
        Arrays.sort(array, new Comparator<PreNode>() {

            public int compare(PreNode o1, PreNode o2) {
                if (o1.post > o2.post) {
                    return 1;
                } else if (o1.post < o2.post) {
                    return -1;
                }
                throw new IllegalArgumentException("duplicated post numbers");
            }
        });
        for (int i = 0; i < array.length; i++) {
            assertEquals(i, array[i].post);
        }
    }
}
