package org.gephi.preview.api;

import org.gephi.preview.api.color.Color;
import processing.core.PVector;

/**
 * Interface of a preview node label.
 *
 * @author Jérémy Subtil <jeremy.subtil@gephi.org>
 */
public interface NodeLabel {

    /**
     * Returns the node label's current color.
     *
     * @return the node label's current color
     */
    public Color getColor();

    /**
     * Returns the node label's current value.
     *
     * @return the node label's current value
     */
    public String getValue();

    /**
     * Returns the node label's position.
     *
     * @return the node label's position
     */
    public PVector getPosition();

}
