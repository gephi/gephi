/*
 *  Copyright 2009 Helder Suzuki <heldersuzuki@gmail.com>.
 */
package org.gephi.layout.quadtree;

import org.gephi.graph.api.Spatial;
import org.gephi.layout.force.AbstractForce;
import org.gephi.layout.force.BarnesHut;
import org.gephi.layout.force.ForceVector;
import org.gephi.layout.force.quadtree.QuadTree;
import static org.junit.Assert.*;

/**
 *
 * @author Helder Suzuki <heldersuzuki@gmail.com>
 */
public class BarnesHutTest {

    public static final double eps = 1e-6;

    class TestForce extends AbstractForce {

        @Override
        public ForceVector calculateForce(Spatial node1, Spatial node2, float distance) {
            return new ForceVector(1, 1);
        }
    }

    @org.junit.Test
    public void testLeafTree() {
        QuadTree tree = new QuadTree(0, 0, 10, 10);
        tree.addNode(new TestNode(1, 1));
        BarnesHut barnesHut = new BarnesHut(new TestForce());

        ForceVector f = barnesHut.calculateForce(tree, tree);
        assertNotNull(f);
        assertEquals(f.x(), 1, eps);
        assertEquals(f.y(), 1, eps);
    }
}
