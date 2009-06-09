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

import java.util.Iterator;
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
public class DhnsTestClusteredGraph {

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {

    }

    @After
    public void tearDown() {

    }

    @Test
    public void testDurableTreeList() {

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
        for(Iterator<PreNode> itr = treeStructure.getTree().iterator(1);itr.hasNext();) {
            PreNode n = itr.next();
            assertEquals(n.getId(), n.getPre());
        }

        //Move p1 -> p4
        treeStructure.move(p1, p4);

         //Write expected array
        int[] expected = new int[treeStructure.getTreeSize()-1];
        int index=0;
        for(Iterator<PreNode> itr = treeStructure.getTree().iterator(1);itr.hasNext();) {
            PreNode n = itr.next();
            expected[index] = n.getId();
        }

        treeStructure.move(p1, p0);
        treeStructure.move(p1, p4);

        int[] actual = new int[treeStructure.getTreeSize()-1];
        for(Iterator<PreNode> itr = treeStructure.getTree().iterator(1);itr.hasNext();) {
            PreNode n = itr.next();
            actual[index] = n.getId();
        }
        assertArrayEquals(expected, actual);

        treeStructure.showTreeAsTable();

        for(Iterator<PreNode> itr = treeStructure.getTree().iterator(1);itr.hasNext();) {
            PreNode n = itr.next();
        }
    }
}
