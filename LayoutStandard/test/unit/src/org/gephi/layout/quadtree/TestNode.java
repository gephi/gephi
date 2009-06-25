/*
 *  Copyright 2009 Helder Suzuki <heldersuzuki@gmail.com>.
 */
package org.gephi.layout.quadtree;

import org.gephi.graph.api.Spatial;

/**
 *
 * @author Helder Suzuki <heldersuzuki@gmail.com>
 */
class TestNode implements Spatial {

    public float x,  y;

    public TestNode(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public float x() {
        return x;
    }

    public float y() {
        return y;
    }

    public float z() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
