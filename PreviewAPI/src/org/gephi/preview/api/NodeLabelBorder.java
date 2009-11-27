package org.gephi.preview.api;

import processing.core.PVector;

/**
 * Implementation of a node label border.
 *
 * @author Jérémy Subtil <jeremy.subtil@gephi.org>
 */
public interface NodeLabelBorder {

    /**
     * Returns the node label border's color.
     *
     * @return the node label border's color
     */
    public Color getColor();

    /**
     * Returns the node label border's position.
     *
     * @return the node label border's position
     */
    public PVector getPosition();

    /**
     * Returns the node label border's related label.
     *
     * @return the node label border's related label
     */
    public NodeLabel getLabel();
}
