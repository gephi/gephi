/*
 *  Copyright 2009 Helder Suzuki <heldersuzuki@gmail.com>.
 */
package org.gephi.layout.force;

import org.gephi.graph.api.Spatial;
import org.gephi.layout.ForceVectorUtils;
import org.gephi.layout.force.quadtree.QuadTree;

/**
 * Barnes-Hut's O(n log n) force calculation algorithm.
 * @author Helder Suzuki <heldersuzuki@gmail.com>
 */
public class BarnesHut {

    /* theta is the parameter for Barnes-Hut opening criteria
     */
    public float theta = (float) 1.2;
    private AbstractForce force;

    public BarnesHut(AbstractForce force) {
        this.force = force;
    }

    /* Calculates the ForceVector on node against every other node represented
     * in the tree with respect to force.
     */
    public ForceVector calculateForce(Spatial node, QuadTree tree) {
        if (tree.mass() <= 0) {
            return null;
        }

        if (tree.isIsLeaf() || tree.mass() == 1) {
            return force.calculateForce(node, tree);
        }

        float distance = ForceVectorUtils.distance(node, tree);
        if (distance * theta > tree.size()) {
            ForceVector f = force.calculateForce(node, tree, distance);
            f.multiply(tree.mass());
            return f;
        }

        ForceVector f = new ForceVector();
        for (QuadTree child : tree.getChildren()) {
            f.add(calculateForce(node, child));
        }
        return f;
    }
}
