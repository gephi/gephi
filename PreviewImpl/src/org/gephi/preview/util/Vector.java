package org.gephi.preview.util;

import org.gephi.preview.api.Point;
import processing.core.PVector;

/**
 * Vector implementation.
 *
 * @author Jérémy Subtil <jeremy.subtil@gephi.org>
 * @see PVector
 */
public class Vector extends PVector {

    /**
     * Constructor.
     *
     * @param x  the x coordinate
     * @param y  the y coordinate
     */
    public Vector(float x, float y) {
        super(x, y);
    }

    /**
     * Constructor.
     *
     * @param vector  the source vector
     */
    public Vector(Vector vector) {
        this(vector.x, vector.y);
    }

    /**
     * Constructor.
     *
     * @param point  the source point
     */
    public Vector(Point point) {
        this(point.getX(), point.getY());
    }
}
