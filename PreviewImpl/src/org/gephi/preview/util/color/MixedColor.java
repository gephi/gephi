package org.gephi.preview.util.color;

import org.gephi.preview.api.Color;
import org.gephi.preview.api.util.Holder;

/**
 * Implementation of a mix of two colors.
 *
 * @author Jérémy Subtil <jeremy.subtil@gephi.org>
 */
public class MixedColor implements Color {

    private final Holder<Color> colorHolder1;
    private final Holder<Color> colorHolder2;

    /**
     * Constructor.
     *
     * @param colorHolder1  the first color's holder used in the mix
     * @param colorHolder2  the second color's holder used in the mix
     */
    public MixedColor(Holder<Color> colorHolder1, Holder<Color> colorHolder2) {
        this.colorHolder1 = colorHolder1;
        this.colorHolder2 = colorHolder2;
    }

    /**
     * Returns the red component.
     *
     * @return the red component
     */
    public Integer getRed() {
        Color c1 = colorHolder1.getComponent();
        Color c2 = colorHolder2.getComponent();
        return (c1.getRed() + c2.getRed()) / 2;
    }

    /**
     * Returns the green component.
     *
     * @return the green component
     */
    public Integer getGreen() {
        Color c1 = colorHolder1.getComponent();
        Color c2 = colorHolder2.getComponent();
        return (c1.getGreen() + c2.getGreen()) / 2;
    }

    /**
     * Returns the blue component.
     *
     * @return the blue component
     */
    public Integer getBlue() {
        Color c1 = colorHolder1.getComponent();
        Color c2 = colorHolder2.getComponent();
        return (c1.getBlue() + c2.getBlue()) / 2;
    }

    /**
     * Formats the color as an hex string.
     *
     * @return  the color formatted as an hex string
     */
    public String toHexString() {
        Color c = new SimpleColor(getRed(), getGreen(), getBlue());
        return c.toHexString();
    }
}
