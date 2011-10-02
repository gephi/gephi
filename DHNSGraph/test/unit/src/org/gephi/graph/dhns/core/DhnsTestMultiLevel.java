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
package org.gephi.graph.dhns.core;

import java.util.HashMap;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.MetaEdge;
import org.gephi.graph.api.Node;
import org.gephi.graph.dhns.DhnsGraphController;
import org.gephi.graph.dhns.graph.HierarchicalDirectedGraphImpl;
import org.gephi.graph.dhns.node.AbstractNode;
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
    private HierarchicalDirectedGraphImpl graph1;
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
        dhns1 = new Dhns(controller, null);
        graph1 = new HierarchicalDirectedGraphImpl(dhns1, dhns1.getGraphStructure().getMainView());
        GraphFactoryImpl factory = dhns1.factory();

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
/*
    @Test
    public void testDurableList() {

        TreeStructure treeStructure = new GraphStructure().getStructure();
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

        // dhns1.getGraphStructure().getStructure().showTreeAsTable();
    }

    @Test
    public void testCloneRemove() {
        //Test remove a clone
        graph1.removeNode(nodeMap.get("nodeB"));
        assertFalse(graph1.contains(nodeMap.get("nodeD")));
        assertEquals(1, ((PreNode) nodeMap.get("nodeA")).size);
        assertEquals(2, ((PreNode) dhns1.getGraphStructure().getStructure().root).size);
        assertEquals(2, graph1.getNodeCount());

        setUp();

        //Test remove an original
        graph1.removeNode(nodeMap.get("nodeA"));
        assertFalse(graph1.contains(nodeMap.get("nodeD")));
        assertEquals(1, ((PreNode) nodeMap.get("nodeB")).size);
        assertEquals(2, ((PreNode) dhns1.getGraphStructure().getStructure().root).size);
        assertEquals(2, graph1.getNodeCount());

        //dhns1.getGraphStructure().getStructure().showTreeAsTable();

        //Test diamond
        DhnsGraphController controller = new DhnsGraphController();
        Dhns dhns = new Dhns(controller, null);
        HierarchicalDirectedGraphImpl graph = new HierarchicalDirectedGraphImpl(dhns, dhns.getGraphStructure());
        GraphFactoryImpl factory = dhns.factory();

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

        //dhns.getGraphStructure().getStructure().showTreeAsTable();
    }

    @Test
    public void testEnabled() {
        graph1.expand(nodeMap.get("nodeB"));
        assertTrue(dhns1.getGraphStructure().getStructure().hasEnabledDescendant(nodeMap.get("nodeB")));

        //dhns1.getGraphStructure().getStructure().showTreeAsTable();
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
        dhns1.getGraphStructure().getStructure().showTreeAsTable();
        AbstractNode[] enabled = dhns1.getGraphStructure().getStructure().getEnabledAncestorsOrSelf(nodeMap.get("nodeD"));
        assertArrayEquals(new AbstractNode[]{nodeMap.get("nodeA"), nodeMap.get("nodeB")}, enabled);
    }

    @Test
    public void testComputeMetaEdges() {

        GraphFactoryImpl factory = dhns1.factory();
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
    }*/
}
