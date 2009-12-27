package org.gephi.preview.api;

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
    public Point getPt1();

    /**
     * Returns the edge arrow's second point.
     *
     * @return the edge arrow's second point
     */
    public Point getPt2();

    /**
     * Returns the edge arrow's third point.
     *
     * @return the edge arrow's third point
     */
    public Point getPt3();
}
