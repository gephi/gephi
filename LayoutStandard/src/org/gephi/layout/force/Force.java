/*
 *  Copyright 2009 Helder Suzuki <heldersuzuki@gmail.com>.
 */
package org.gephi.layout.force;

import org.gephi.graph.api.Spatial;
import org.gephi.layout.ForceVectorUtils;

/**
 *
 * @author Helder Suzuki <heldersuzuki@gmail.com>
 */
public abstract class Force {

    public ForceVector calculateForce(Spatial node1, Spatial node2) {
        return calculateForce(node1, node2,
                              ForceVectorUtils.distance(node1, node2));
    }

    public abstract ForceVector calculateForce(Spatial node1, Spatial node2,
                                               float distance);
}
