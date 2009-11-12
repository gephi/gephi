package org.gephi.preview.api;

import org.gephi.preview.api.color.Color;
import org.gephi.preview.api.util.Holder;
import processing.core.PVector;

/**
 * Interface of a preview node.
 *
 * @author Jérémy Subtil <jeremy.subtil@gephi.org>
 */
public interface Node {

    /**
     * Returns the node's current color.
     *
     * @return the node's current color
     */
    public Color getColor();

    /**
     * Returns the node's position.
     *
     * @return the node's position
     */
    public PVector getPosition();

    /**
     * Returns the node's diameter.
     *
     * @return the node's diameter
     */
    public Float getDiameter();

    /**
     * Returns the node's label border.
     *
     * @return the node's label border
     */
    public NodeLabelBorder getLabelBorder();

    /**
     * Returns the node's label.
     *
     * @return the node's label
     */
    public NodeLabel getLabel();

    /**
     * Returns the node's border color.
     *
     * @return the node's border color
     */
    public Color getBorderColor();

    /**
     * Returns the node's border width.
     *
     * @return the node's border width
     */
    public Float getBorderWidth();

    /**
     * Returns whether or not the node's label must be displayed.
     *
     * @return true to display the node's label
     */
    public Boolean showLabel();

    /**
     * Returns whether or not the node's label borders must be displayed.
     *
     * @return true to display the node's label borders
     */
    public Boolean showLabelBorders();

    /**
     * Returns the node's color holder.
     *
     * @return the node's color holder
     */
    public Holder<Color> getColorHolder();
}
