/*
 *  Copyright 2009 Helder Suzuki <heldersuzuki@gmail.com>.
 */
package org.gephi.layout.force;

import org.gephi.graph.api.Spatial;

/**
 *
 * Repulsive Electrical Force: -C*K*K*(n2-n1)/||n2-n1||
 * @author Helder Suzuki <heldersuzuki@gmail.com>
 */
public class ElectricalForce extends Force {

    private float optimalDistance;
    private float relativeStrength;
    private float forceConstant;

    public ElectricalForce(float optimalDistance, float relativeStrength) {
        this.optimalDistance = optimalDistance;
        this.relativeStrength = relativeStrength;
        forceConstant = (-1) * relativeStrength * optimalDistance * optimalDistance;
    }

    @Override
    public ForceVector calculateForce(Spatial node1, Spatial node2,
                                      float distance) {
        ForceVector f = new ForceVector(node2.x() - node1.x(),
                                        node2.y() - node1.y());

        f.multiply(forceConstant / distance);
        return f;
    }
}
