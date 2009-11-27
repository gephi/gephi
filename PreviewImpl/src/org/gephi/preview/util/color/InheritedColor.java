package org.gephi.preview.util.color;

import org.gephi.preview.api.Color;
import org.gephi.preview.api.util.Holder;

/**
 * Implementation of an inherited color.
 *
 * @author Jérémy Subtil <jeremy.subtil@gephi.org>
 */
public class InheritedColor implements Color {

    private final Holder<Color> colorHolder;

    /**
     * Constructor.
     *
     * @param colorHolder  the inherited color's holder
     */
    public InheritedColor(Holder<Color> colorHolder) {
        this.colorHolder = colorHolder;
    }

    /**
     * Returns the red component.
     *
     * @return the red component
     */
    public Integer getRed() {
        return colorHolder.getComponent().getRed();
    }

    /**
     * Returns the green component.
     *
     * @return the green component
     */
    public Integer getGreen() {
        return colorHolder.getComponent().getGreen();
    }

    /**
     * Returns the blue component.
     *
     * @return the blue component
     */
    public Integer getBlue() {
        return colorHolder.getComponent().getBlue();
    }

    /**
     * Formats the color as an hex string.
     *
     * @return  the color formatted as an hex string
     */
    public String toHexString() {
        return colorHolder.getComponent().toHexString();
    }
}
