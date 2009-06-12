/*
 *  Copyright 2009 Helder Suzuki <heldersuzuki@gmail.com>.
 */
package org.gephi.layout.force;

import org.gephi.graph.api.Spatial;

/**
 * Spring actrattive force: (n2 - n1) * ||n2 - n1|| / K
 * @author Helder Suzuki <heldersuzuki@gmail.com>
 */
public class SpringForce extends Force {

    private float optimalDistance;

    public SpringForce(float optimalDistance) {
        this.optimalDistance = optimalDistance;
    }

    @Override
    public ForceVector calculateForce(Spatial node1, Spatial node2,
                                      float distance) {
        ForceVector f = new ForceVector(node2.x() - node1.x(),
                                        node2.y() - node1.y());
        f.multiply(distance / optimalDistance);
        return f;
    }
}
