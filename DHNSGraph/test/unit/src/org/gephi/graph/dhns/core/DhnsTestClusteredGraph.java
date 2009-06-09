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
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Node;
import org.gephi.graph.dhns.graph.ClusteredDirectedGraphImpl;
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
    }

    @After
    public void tearDown() {
        nodeMap.clear();
        dhnsGlobal = null;
        graphGlobal = null;
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
            groupArray[i-1] = nodeMap.get("Node " + i);
        }

        PreNode group = (PreNode)graphGlobal.groupNodes(groupArray);

        assertEquals(oldSize+1, graphGlobal.getNodeCount());
        assertEquals(groupArray.length, group.size);
        assertEquals(treeStructure.treeHeight, group.level);

        int i=0;
        for(TreeListIterator itr = new TreeListIterator(treeStructure.getTree(), group.getPre());itr.hasNext();) {
            PreNode node = itr.next();
            assertEquals(group.pre + i, node.getPre());
            i++;
        }
        try {
            checkHierarchy(treeStructure);
        } catch (Exception ex) {
            fail(ex.getMessage());
        }

        treeStructure.showTreeAsTable();
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
