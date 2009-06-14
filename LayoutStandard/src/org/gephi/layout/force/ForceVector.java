/*
 *  Copyright 2009 Helder Suzuki <heldersuzuki@gmail.com>.
 */
package org.gephi.layout.force;

import org.gephi.graph.api.LayoutData;
import org.gephi.graph.api.Spatial;

/**
 *
 * @author Helder Suzuki <heldersuzuki@gmail.com>
 */
public class ForceVector implements Spatial, LayoutData {

    protected float x;
    protected float y;

    public ForceVector(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public ForceVector() {
        this.x = 0;
        this.y = 0;
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

    void subtract(ForceVector f) {
        if (f != null) {
            x -= f.x();
            y -= f.y();
        }
    }

    public float getEnergy() {
        return x * x + y * y;
    }

    public float getNorm() {
        return (float) Math.sqrt(getEnergy());
    }

    public ForceVector normalize() {
        float norm = getNorm();
        return new ForceVector(x / norm, y / norm);
    }

    public String toString() {
        return "(" + x + ", " + y + ")";
    }
}
