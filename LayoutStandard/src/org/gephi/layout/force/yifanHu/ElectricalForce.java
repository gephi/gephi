/*
 *  Copyright 2009 Helder Suzuki <heldersuzuki@gephi.org>.
 */
package org.gephi.layout.force.yifanHu;

import org.gephi.graph.api.Spatial;
import org.gephi.layout.force.AbstractForce;
import org.gephi.layout.force.ForceVector;

/**
 * Fr = -C*K*K*(n2-n1)/||n2-n1||
 * @author Helder Suzuki <heldersuzuki@gephi.org>
 */
class ElectricalForce extends AbstractForce {

    private float relativeStrength;
    private float optimalDistance;

    public ElectricalForce(float relativeStrength, float optimalDistance) {
        this.relativeStrength = relativeStrength;
        this.optimalDistance = optimalDistance;
    }

    @Override
    public ForceVector calculateForce(Spatial node1, Spatial node2,
                                      float distance) {
        ForceVector f = new ForceVector(node2.x() - node1.x(),
                                        node2.y() - node1.y());
        float scale = -relativeStrength * optimalDistance * optimalDistance / (distance * distance);
        if (Float.isNaN(scale) || Float.isInfinite(scale)) {
            System.out.println("infinite: nodes = (" + node1 + ", " + node2 + ")");
            scale = -1;
        }

        f.multiply(scale);
//        if (f.getNorm() > 100) {
//            f.multiply(10 / f.getNorm());
//        }

        if (Float.isNaN(f.x()) || Float.isNaN(f.y())) {
            System.out.println("NaN!!!");
            System.out.println("relativeStrength = " + relativeStrength);
            System.out.println("optimalDistance = " + optimalDistance);
            System.out.println("distance = " + distance);
        }
        return f;
    }
}