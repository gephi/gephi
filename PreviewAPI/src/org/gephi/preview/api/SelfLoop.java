package org.gephi.preview.api;

/**
 * Interface of a preview self-loop.
 *
 * @author Jérémy Subtil <jeremy.subtil@gephi.org>
 */
public interface SelfLoop {

    /**
     * Returns the self-loop's related node.
     *
     * @return the self-loop's related node
     */
    public Node getNode();

    /**
     * Returns the self-loop's thickness.
     *
     * @return the self-loop's thickness
     */
    public Float getThickness();

    /**
     * Returns the self-loop's color.
     *
     * @return the self-loop's color
     */
    public Color getColor();

    /**
     * Returns the self-loop's curve.
     *
     * @return the self-loop's curve
     */
    public CubicBezierCurve getCurve();
}
