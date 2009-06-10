/*
 *  Copyright 2009 Helder Suzuki <heldersuzuki@gmail.com>.
 */
package org.gephi.layout.force;

import org.gephi.graph.api.Spatial;

/**
 *
 * @author Helder Suzuki <heldersuzuki@gmail.com>
 */
public class ForceVector implements Spatial {

    private float x;
    private float y;

    public ForceVector(float x, float y) {
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

    public void add(ForceVector f) {
        if (f != null) {
            x += f.x();
            y += f.y();
        }
    }

    public void multiply(float s) {
        x *= s;
        y *= s;
    }
}
