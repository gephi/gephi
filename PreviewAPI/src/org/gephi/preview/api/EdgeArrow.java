package org.gephi.preview.api;

import org.gephi.preview.api.color.Color;
import processing.core.PVector;

/**
 * Interface of an edge arrow.
 *
 * @author Jérémy Subtil <jeremy.subtil@gephi.org>
 */
public interface EdgeArrow {

    /**
     * Returns the edge arrow's color.
     *
     * @return the edge arrow's color
     */
    public Color getColor();

    /**
     * Returns the edge arrow's first point.
     *
     * @return the edge arrow's first point
     */
    public PVector getPt1();

    /**
     * Returns the edge arrow's second point.
     *
     * @return the edge arrow's second point
     */
    public PVector getPt2();

    /**
     * Returns the edge arrow's third point.
     *
     * @return the edge arrow's third point
     */
    public PVector getPt3();
}
