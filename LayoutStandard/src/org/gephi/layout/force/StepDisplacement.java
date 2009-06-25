/*
 *  Copyright 2009 Helder Suzuki <heldersuzuki@gmail.com>.
 */
package org.gephi.layout.force;

import org.gephi.graph.api.NodeData;

/**
 *
 * @author Helder Suzuki <heldersuzuki@gmail.com>
 */
public class StepDisplacement implements Displacement {

    protected float step;

    public StepDisplacement(float step) {
        this.step = step;
    }

    public void moveNode(NodeData node, ForceVector forceData) {
        //ForceVector displacement = forceData.normalize();
        ForceVector displacement = forceData;
        displacement.multiply(step);

        float x = node.x();
        float y = node.y();
        node.setX(node.x() + displacement.x());
        node.setY(node.y() + displacement.y());
    }
}