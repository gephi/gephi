/*
 *  Copyright 2009 Helder Suzuki <heldersuzuki@gmail.com>.
 */
package org.gephi.layout.force;

import org.gephi.graph.api.Spatial;
import org.gephi.layout.ForceVectorUtils;
import org.gephi.layout.quadtree.QuadTree;

/**
 * Barnes-Hut's O(n log n) force calculation algorithm.
 * @author Helder Suzuki <heldersuzuki@gmail.com>
 */
public class BarnesHut {

    /* theta is the parameter for Barnes-Hut opening criteria
     */
    public float theta = (float) 1.2;
    private Force force;

    public BarnesHut(Force force) {
        this.force = force;
    }

    /* Calculates the ForceVector on node against everyother node represented
     * in the tree with respect to force.
     */
    public ForceVector calculateForce(Spatial node, QuadTree tree) {
        if (tree.isIsLeaf()) {
            return force.calculateForce(node, tree);
        }

        float distance = ForceVectorUtils.distance(node, tree);
        if (distance * theta > tree.size()) {
            ForceVector f = force.calculateForce(node, tree, distance);
            f.multiply(tree.mass());
            return f;
        }

        ForceVector f = new ForceVector(0, 0);
        for (QuadTree child : tree.getChildren()) {
            f.add(calculateForce(node, child));
        }
        return f;
    }
}
