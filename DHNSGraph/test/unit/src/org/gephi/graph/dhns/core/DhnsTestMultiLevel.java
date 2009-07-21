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

import java.util.HashMap;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.MetaEdge;
import org.gephi.graph.api.Node;
import org.gephi.graph.dhns.DhnsGraphController;
import org.gephi.graph.dhns.graph.ClusteredDirectedGraphImpl;
import org.gephi.graph.dhns.node.AbstractNode;
import org.gephi.graph.dhns.node.CloneNode;
import org.gephi.graph.dhns.node.PreNode;
import org.gephi.graph.dhns.view.View;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Mathieu Bastian
 */
public class DhnsTestMultiLevel {

    private Dhns dhns1;
    private ClusteredDirectedGraphImpl graph1;
    private HashMap<String, AbstractNode> nodeMap;

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
        nodeMap = new HashMap<String, AbstractNode>();
        DhnsGraphController controller = new DhnsGraphController();
        dhns1 = controller.getMainDhns();
        graph1 = new ClusteredDirectedGraphImpl(dhns1, false, true);
        GraphFactoryImpl factory = controller.factory();

        AbstractNode nodeA = factory.newNode();
        AbstractNode nodeB = factory.newNode();
        AbstractNode nodeC = factory.newNode();
        AbstractNode nodeD = factory.newNode();
        AbstractNode nodeE = factory.newNode();
        graph1.addNode(nodeA);
        graph1.addNode(nodeB);
        graph1.addNode(nodeC, nodeA);
        graph1.addNode(nodeE, nodeB);
        graph1.addNode(nodeD, nodeA);
        graph1.addNode(nodeD, nodeB);
        nodeMap.put("nodeA", nodeA);
        nodeMap.put("nodeB", nodeB);
        nodeMap.put("nodeC", nodeC);
        nodeMap.put("nodeD", nodeD);
        nodeMap.put("nodeE", nodeE);
    }

    @After
    public void tearDown() {
        dhns1 = null;
        graph1 = null;
    }

    @Test
    public void testDurableList() {

        TreeStructure treeStructure = new TreeStructure(new Dhns(new DhnsGraphController()));
        PreNode preNodeA = new PreNode(0, 0, 0, 0, null);
        PreNode preNodeB = new PreNode(1, 0, 0, 0, null);
        treeStructure.insertAsChild(preNodeA, treeStructure.getRoot());
        treeStructure.insertAsChild(preNodeB, treeStructure.getRoot());
        PreNode preNodeC = new PreNode(2, 0, 0, 0, preNodeA);
        PreNode preNodeD = new PreNode(3, 0, 0, 0, preNodeA);
        PreNode preNodeE = new PreNode(4, 0, 0, 0, preNodeB);
        treeStructure.insertAsChild(preNodeC, preNodeA);
        treeStructure.insertAsChild(preNodeD, preNodeA);
        treeStructure.insertAsChild(preNodeE, preNodeB);
        CloneNode cloneNodeD = new CloneNode(preNodeD);
        treeStructure.insertAsChild(cloneNodeD, preNodeB);

    //treeStructure.showTreeAsTable();
    }

    @Test
    public void testCloneAdd() {
        //Test that adding the node to a descendant fails
        Exception ex = null;
        try {
            graph1.addNode(nodeMap.get("nodeB"), nodeMap.get("nodeD"));        //Error, add descendant
        } catch (IllegalArgumentException e) {
            ex = e;
        }
        assertNotNull(ex);

        //Test descendant & ancestor
        assertTrue(graph1.isDescendant(nodeMap.get("nodeA"), nodeMap.get("nodeC")));
        assertTrue(graph1.isDescendant(nodeMap.get("nodeA"), nodeMap.get("nodeD")));
        assertTrue(graph1.isDescendant(nodeMap.get("nodeB"), nodeMap.get("nodeD")));
        assertTrue(graph1.isAncestor(nodeMap.get("nodeC"), nodeMap.get("nodeA")));
        assertTrue(graph1.isAncestor(nodeMap.get("nodeD"), nodeMap.get("nodeA")));
        assertTrue(graph1.isAncestor(nodeMap.get("nodeD"), nodeMap.get("nodeB")));
    }

    @Test
    public void testAddWithDescendants() {

        //Add A as child of E
        graph1.addNode(nodeMap.get("nodeA"), nodeMap.get("nodeE"));

        assertTrue(graph1.isDescendant(nodeMap.get("nodeE"), nodeMap.get("nodeC")));
        assertTrue(graph1.isDescendant(nodeMap.get("nodeE"), nodeMap.get("nodeA")));
        assertTrue(graph1.isDescendant(nodeMap.get("nodeE"), nodeMap.get("nodeD")));
        assertNotNull(nodeMap.get("nodeC").getOriginalNode().getClones());
        assertEquals(2, nodeMap.get("nodeD").getOriginalNode().countClones());
        assertEquals(9, graph1.getNodeCount());

        //Add the cloned node A to B
        AbstractNode cloneAafterE = nodeMap.get("nodeA").getOriginalNode().getClones();
        graph1.addNode(cloneAafterE, nodeMap.get("nodeB"));

        assertEquals(12, graph1.getNodeCount());
        assertEquals(3, nodeMap.get("nodeD").getOriginalNode().countClones());
        assertEquals(2, nodeMap.get("nodeA").getOriginalNode().countClones());

    // dhns1.getTreeStructure().showTreeAsTable();
    }

    @Test
    public void testCloneRemove() {
        //Test remove a clone
        graph1.removeNode(nodeMap.get("nodeB"));
        assertFalse(graph1.contains(nodeMap.get("nodeD")));
        assertEquals(1, ((PreNode) nodeMap.get("nodeA")).size);
        assertEquals(2, ((PreNode) dhns1.getTreeStructure().root).size);
        assertEquals(2, graph1.getNodeCount());

        setUp();

        //Test remove an original
        graph1.removeNode(nodeMap.get("nodeA"));
        assertFalse(graph1.contains(nodeMap.get("nodeD")));
        assertEquals(1, ((PreNode) nodeMap.get("nodeB")).size);
        assertEquals(2, ((PreNode) dhns1.getTreeStructure().root).size);
        assertEquals(2, graph1.getNodeCount());

        //dhns1.getTreeStructure().showTreeAsTable();

        //Test diamond
        DhnsGraphController controller = new DhnsGraphController();
        Dhns dhns = controller.getMainDhns();
        ClusteredDirectedGraphImpl graph = new ClusteredDirectedGraphImpl(dhns, false, true);
        GraphFactoryImpl factory = controller.factory();

        AbstractNode nodeA = factory.newNode();
        AbstractNode nodeB = factory.newNode();
        AbstractNode nodeC = factory.newNode();
        AbstractNode nodeD = factory.newNode();
        graph.addNode(nodeA);
        graph.addNode(nodeB, nodeA);
        graph.addNode(nodeC, nodeA);
        graph.addNode(nodeD, nodeB);
        graph.addNode(nodeD, nodeC);
        graph.addNode(nodeD, nodeA);

        graph.removeNode(nodeD);
        assertFalse(graph.contains(nodeD));
        assertEquals(2, ((PreNode) nodeA).size);

        //Test remove complex
        setUp();
        graph1.addNode(nodeMap.get("nodeA"), nodeMap.get("nodeE"));
        AbstractNode cloneAafterE = nodeMap.get("nodeA").getOriginalNode().getClones();
        graph1.addNode(cloneAafterE, nodeMap.get("nodeB"));
        graph1.removeNode(nodeMap.get("nodeA"));
        assertEquals(2, graph1.getNodeCount());
        assertTrue(graph1.contains(nodeMap.get("nodeB")));
        assertTrue(graph1.contains(nodeMap.get("nodeE")));

    //dhns.getTreeStructure().showTreeAsTable();
    }

    @Test
    public void testEnabled() {
        graph1.expand(nodeMap.get("nodeB"));
        View mainView = dhns1.getViewManager().getMainView();
        assertTrue(dhns1.getTreeStructure().hasEnabledDescendant(mainView, nodeMap.get("nodeB")));

    //dhns1.getTreeStructure().showTreeAsTable();
    }

    @Test
    public void testCloneLinkedList() {
        PreNode preNode = new PreNode(0, 0, 0, 0, null);
        CloneNode cn1 = new CloneNode(preNode);
        assertEquals(cn1, preNode.getClones());
        assertEquals(preNode, cn1.getPreNode());
        CloneNode cn2 = new CloneNode(preNode);
        assertEquals(cn2, preNode.getClones());
        assertEquals(cn1, cn2.getNext());
        assertNull(cn1.getNext());
        preNode.removeClone(cn2);
        assertEquals(cn1, preNode.getClones());
        CloneNode cn3 = new CloneNode(preNode);
        assertEquals(cn1, cn3.getNext());
        preNode.removeClone(cn1);
        assertNull(cn3.getNext());
        CloneNode cn4 = new CloneNode(preNode);
        CloneNode cn5 = new CloneNode(preNode);
        assertEquals(3, preNode.countClones());
        preNode.removeClone(cn4);
        assertEquals(cn3, cn5.getNext());
    }

    @Test
    public void testGetEnabledAncestors() {
        dhns1.getTreeStructure().showTreeAsTable();
        View mainView = dhns1.getViewManager().getMainView();
        AbstractNode[] enabled = dhns1.getTreeStructure().getEnabledAncestorsOrSelf(mainView, nodeMap.get("nodeD"));
        assertArrayEquals(new AbstractNode[]{nodeMap.get("nodeA"), nodeMap.get("nodeB")}, enabled);
    }

    @Test
    public void testComputeMetaEdges() {

        GraphFactoryImpl factory = dhns1.getGraphFactory();
        Node nodeF = factory.newNode();
        graph1.addNode(nodeF);
        graph1.addNode(nodeMap.get("nodeE"), nodeF);
        graph1.addEdge(nodeMap.get("nodeD"), nodeMap.get("nodeE"));

        Edge[] metaEdges = graph1.getMetaEdges().toArray();
        MetaEdge[] expectedArray = new MetaEdge[3];
        expectedArray[0] = graph1.getMetaEdge(nodeMap.get("nodeA"), nodeMap.get("nodeB"));
        expectedArray[1] = graph1.getMetaEdge(nodeMap.get("nodeA"), nodeF);
        expectedArray[2] = graph1.getMetaEdge(nodeMap.get("nodeB"), nodeF);
        assertArrayEquals(expectedArray, metaEdges);
    }
}
