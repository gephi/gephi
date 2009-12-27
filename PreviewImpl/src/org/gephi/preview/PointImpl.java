package org.gephi.preview;

import org.gephi.preview.api.Point;
import org.gephi.preview.util.Vector;

/**
 * Point implementation.
 *
 * @author Jérémy Subtil <jeremy.subtil@gephi.org>
 */
public class PointImpl implements Point {

    private final Float x;
    private final Float y;

    /**
     * Constructor.
     *
     * @param x  the x coordinate
     * @param y  the y coordinate
     */
    public PointImpl(Float x, Float y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Constructor.
     *
     * @param vector  the source vector
     */
    public PointImpl(Vector vector) {
        this.x = vector.x;
        this.y = vector.y;
    }

    public Float getX() {
        return x;
    }

    public Float getY() {
        return y;
    }
}
