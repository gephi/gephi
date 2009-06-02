/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.graph.dhns.core;

import org.gephi.graph.api.Node;
import org.gephi.graph.dhns.graph.ClusteredDirectedGraphImpl;
import org.gephi.graph.dhns.node.PreNode;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Mathieu
 */
public class DhnsTestDirectedGraph {

    private Dhns dhnsGlobal;
    private ClusteredDirectedGraphImpl graphGlobal;

    public DhnsTestDirectedGraph() {
    }

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
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testAddNode() {
        System.out.println("testAddNode");
        Dhns dhns = new Dhns();
        ClusteredDirectedGraphImpl graph = new ClusteredDirectedGraphImpl(dhns, false);
        TreeStructure treeStructure = dhns.getTreeStructure();
        GraphFactoryImpl factory = dhns.getGraphFactory();

        for (int i = 0; i < 10; i++) {
            Node node = factory.newNode();
            node.getNodeData().setLabel("Node " + i);
            graph.addNode(node);
            System.out.println("Node " + i + " added. Id = " + node.getId());
        }

        graph.readLock();

        //Test
        assertEquals("root size", 11, treeStructure.getTreeSize());
        assertEquals("graph size", 10, graph.getNodeCount());

        for (int i = 0; i < 10; i++) {
            PreNode n = treeStructure.getNodeAt(i);
            assertEquals("prenode pre", i, n.getPre());
            assertEquals("prenode id", i - 1, n.getId());
            assertEquals("prenode enabled", i > 0, n.isEnabled());
            assertEquals("prenode avl node", i, n.avlNode.getIndex());
            if (n.avlNode.next() != null) {
                assertEquals("prenode next", treeStructure.getNodeAt(i + 1).avlNode, n.avlNode.next());
            }
            if (n.avlNode.previous() != null) {
                assertEquals("prenode previous", treeStructure.getNodeAt(i - 1).avlNode, n.avlNode.previous());
            }
        }

        int i = 0;
        for (Node node : graph.getNodes()) {
            assertEquals("node iterator", i, node.getId());
            i++;
        }

        graph.readUnlock();
    }

    @Test
    public void testRemoveNode() {
        Dhns dhns = new Dhns();
        ClusteredDirectedGraphImpl graph = new ClusteredDirectedGraphImpl(dhns, false);
        TreeStructure treeStructure = dhns.getTreeStructure();
        GraphFactoryImpl factory = dhns.getGraphFactory();

        Node first=null;
        Node middle=null;
        Node end=null;
        for (int i = 0; i < 10; i++) {
            Node node = factory.newNode();
            node.getNodeData().setLabel("Node " + i);
            graph.addNode(node);
            System.out.println("Node " + i + " added. Id = " + node.getId());

            if(i==0) {
                first = node;
            } else if(i==4) {
                middle = node;
            } else if(i==9) {
                end = node;
            }
        }

        graph.removeNode(first);

        //Test1
        System.out.print("Test1 nodes: ");
        for (int i = 0; i < treeStructure.getTreeSize(); i++) {
            PreNode n = treeStructure.getNodeAt(i);
            System.out.print(n.getId()+" ");
            assertEquals("prenode pre", i, n.getPre());
            assertEquals("prenode avl node", i, n.avlNode.getIndex());
            if (n.avlNode.next() != null) {
                assertEquals("prenode next", treeStructure.getNodeAt(i + 1).avlNode, n.avlNode.next());
            }
            if (n.avlNode.previous() != null) {
                assertEquals("prenode previous", treeStructure.getNodeAt(i - 1).avlNode, n.avlNode.previous());
            }
        }
        System.out.println();
        //End Test1

        graph.removeNode(middle);

        //Test2
        System.out.print("Test2 nodes: ");
        for (int i = 0; i < treeStructure.getTreeSize(); i++) {
            PreNode n = treeStructure.getNodeAt(i);
            System.out.print(n.getId()+" ");
            assertEquals("prenode pre", i, n.getPre());
            assertEquals("prenode avl node", i, n.avlNode.getIndex());
            if (n.avlNode.next() != null) {
                assertEquals("prenode next", treeStructure.getNodeAt(i + 1).avlNode, n.avlNode.next());
            }
            if (n.avlNode.previous() != null) {
                assertEquals("prenode previous", treeStructure.getNodeAt(i - 1).avlNode, n.avlNode.previous());
            }
        }
        System.out.println();
        //End Test2


        graph.removeNode(end);

        //Test3
        System.out.print("Test3 nodes: ");
        for (int i = 0; i < treeStructure.getTreeSize(); i++) {
            PreNode n = treeStructure.getNodeAt(i);
            System.out.print(n.getId()+" ");
            assertEquals("prenode pre", i, n.getPre());
            assertEquals("prenode avl node", i, n.avlNode.getIndex());
            if (n.avlNode.next() != null) {
                assertEquals("prenode next", treeStructure.getNodeAt(i + 1).avlNode, n.avlNode.next());
            }
            if (n.avlNode.previous() != null) {
                assertEquals("prenode previous", treeStructure.getNodeAt(i - 1).avlNode, n.avlNode.previous());
            }
        }
        System.out.println();
        //End Test3

        //Test
        assertEquals("tree size", 8, treeStructure.getTreeSize());
    }
}