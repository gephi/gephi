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
package org.gephi.layout.force;

import org.gephi.graph.api.Spatial;
import org.gephi.layout.ForceVectorUtils;
import org.gephi.layout.force.quadtree.QuadTree;

/**
 * Barnes-Hut's O(n log n) force calculation algorithm.
 * @author Helder Suzuki <heldersuzuki@gephi.org>
 */
public class BarnesHut {

    /* theta is the parameter for Barnes-Hut opening criteria
     */
    private float theta = (float) 1.2;
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

        float distance = ForceVectorUtils.distance(node, tree);

        if (tree.isIsLeaf() || tree.mass() == 1) {
            // this is probably the case where tree has only the node.
            if (distance < 1e-8) {
                return null;
            }
            return force.calculateForce(node, tree);
        }

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

    public void setTheta(float theta) {
        this.theta = theta;
    }

    public float getTheta() {
        return theta;
    }
}
