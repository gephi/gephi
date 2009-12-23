package org.gephi.preview.api;

import java.awt.Font;
import org.gephi.preview.api.util.HAlign;
import processing.core.PVector;

/**
 * Interface of a preview edge mini-label.
 *
 * @author Jérémy Subtil <jeremy.subtil@gephi.org>
 */
public interface EdgeMiniLabel {

    /**
     * Returns the edge mini-label's color.
     *
     * @return the edge mini-label's color
     */
    public Color getColor();

    /**
     * Returns the edge mini-label's horizontal alignment.
     *
     * @return the edge mini-label's horizontal alignment
     */
    public HAlign getHAlign();

    /**
     * Returns the edge mini-label's position.
     *
     * @return the edge mini-label's position
     */
    public PVector getPosition();

    /**
     * Returns the edge mini-label's angle.
     *
     * @return the edge mini-label's angl
     */
    public Float getAngle();

    /**
     * Returns the edge mini-label's current value.
     *
     * @return the edge mini-label's current value
     */
    public String getValue();

    /**
     * Returns the edge mini-label font.
     *
     * @return the edge mini-label font
     */
    public Font getFont();
}
