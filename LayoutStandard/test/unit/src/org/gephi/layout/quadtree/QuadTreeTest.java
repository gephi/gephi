/*
Copyright 2008-2009 Gephi
Authors : Helder Suzuki <heldersuzuki@gephi.org>
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
package org.gephi.layout.quadtree;

import org.gephi.layout.force.quadtree.QuadTree;
import static org.junit.Assert.*;

/**
 *
 * @author Helder Suzuki <heldersuzuki@gephi.org>
 */
public class QuadTreeTest {

    public static final double eps = 1e-6;

    public void checkMassCenterConservation(QuadTree tree) {
        if (tree.isIsLeaf()) {
            return;
        }

        float x = 0, y = 0;
        int totalMass = 0;
        for (QuadTree child : tree.getChildren()) {
            x += child.x() * child.mass();
            y += child.y() * child.mass();
            totalMass += child.mass();
        }

        assertEquals(totalMass, tree.mass());
        assertEquals(x / totalMass, tree.x(), eps);
        assertEquals(y / totalMass, tree.y(), eps);
    }

    @org.junit.Test
    public void testMaxLevel0() {
        QuadTree tree = new QuadTree(0, 0, 10, 0);
        assertEquals(tree.mass(), 0);

        assertTrue(tree.addNode(new TestNode(1, 1)));
        assertEquals(1, tree.mass());
        assertEquals(1, tree.x(), eps);
        assertEquals(1, tree.y(), eps);

        assertTrue(tree.addNode(new TestNode(9, 9)));
        assertEquals(tree.mass(), 2);
        assertEquals(5, tree.x(), eps);
        assertEquals(5, tree.y(), eps);
    }

    @org.junit.Test
    public void testMaxLevel1() {
        QuadTree tree = new QuadTree(0, 0, 10, 1);
        assertEquals(0, tree.mass());

        assertTrue(tree.addNode(new TestNode(1, 1)));
        assertEquals(1, tree.mass());
        assertEquals(1, tree.x(), eps);
        assertEquals(1, tree.y(), eps);

        assertTrue(tree.addNode(new TestNode(9, 9)));
        assertEquals(2, tree.mass());
        assertEquals(5, tree.y(), eps);

        checkMassCenterConservation(tree);
    }

    @org.junit.Test
    public void testLevel1Leaf() {
        QuadTree tree = new QuadTree(0, 0, 10, 1);
        assertEquals(0, tree.mass());

        assertTrue(tree.addNode(new TestNode(7, 7)));
        assertTrue(tree.addNode(new TestNode(7, 8)));
        assertTrue(tree.addNode(new TestNode(8, 7)));
        assertTrue(tree.addNode(new TestNode(8, 8)));

        assertTrue(tree.addNode(new TestNode(1, 1)));
        checkMassCenterConservation(tree);
    }
}

