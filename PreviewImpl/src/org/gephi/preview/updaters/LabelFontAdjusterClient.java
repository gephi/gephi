package org.gephi.preview.updaters;

import java.awt.Font;

/**
 * Classes implementing this interface are able to have their label font
 * adjusted.
 *
 * @author Jérémy Subtil <jeremy.subtil@gephi.org>
 */
public interface LabelFontAdjusterClient {

    /**
     * Returns the label's base font.
     *
     * @return the base font
     */
    public Font getBaseFont();

    /**
     * Returns the label's size factor.
     *
     * @return the size factor
     */
    public float getSizeFactor();

    /**
     * Defines the label's font.
     *
     * @param font  the label's font to set
     */
    public void setFont(Font font);
}
