/*
 *  Copyright 2009 Helder Suzuki <heldersuzuki@gephi.org>.
 */
package org.gephi.layout.force.yifanHu;

import org.gephi.graph.api.Spatial;
import org.gephi.layout.force.AbstractForce;
import org.gephi.layout.force.ForceVector;

/**
 * Fa = (n2 - n1) * ||n2 - n1|| / K
 * @author Helder Suzuki <heldersuzuki@gephi.org>
 */
class SpringForce extends AbstractForce {

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

    public void setOptimalDistance(Float optimalDistance) {
        this.optimalDistance = optimalDistance;
    }

    public Float getOptimalDistance() {
        return optimalDistance;
    }
}