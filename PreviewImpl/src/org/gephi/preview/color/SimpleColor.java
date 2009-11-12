package org.gephi.preview.color;

import org.gephi.preview.api.color.Color;

/**
 * Implementation of an sRGB color.
 *
 * @author Jérémy Subtil <jeremy.subtil@gephi.org>
 */
public class SimpleColor implements Color {

    private final java.awt.Color color;

    /**
     * Creates an opaque sRGB color with the specified red, green, and blue
     * values in the range (0 - 255).
     *
     * @param r  the red component
     * @param g  the green component
     * @param b  the blue component
     */
    public SimpleColor(int r, int g, int b) {
        color = new java.awt.Color(r, g, b);
    }

    /**
     * Creates an opaque sRGB color with the specified red, green, and blue
     * values in the range (0.0 - 1.0).
     *
     * @param r  the red component
     * @param g  the green component
     * @param b  the blue component
     */
	public SimpleColor(float r, float g, float b) {
        color = new java.awt.Color(r, g, b);
    }

    /**
     * Creates an sRGB color with the specified red, green, blue, and alpha
     * values in the range (0 - 255).
     *
     * @param r  the red component
     * @param g  the green component
     * @param b  the blue component
     * @param a  the alpha component
     */
    public SimpleColor(int r, int g, int b, int a) {
        color = new java.awt.Color(r, g, b, a);
    }

    /**
     * Creates an sRGB color with the specified red, green, blue, and alpha
     * values in the range (0.0 - 1.0).
     *
     * @param r  the red component
     * @param g  the green component
     * @param b  the blue component
     * @param a  the alpha component
     */
	public SimpleColor(float r, float g, float b, float a) {
        color = new java.awt.Color(r, g, b, a);
    }

    /**
     * Returns the red component.
     *
     * @return the red component
     */
    public Integer getRed() {
        return color.getRed();
    }

    /**
     * Returns the green component.
     *
     * @return the green component
     */
    public Integer getGreen() {
        return color.getGreen();
    }

    /**
     * Returns the blue component.
     *
     * @return the blue component
     */
    public Integer getBlue() {
        return color.getBlue();
    }

    /**
     * Formats the color as an hex string.
     *
     * @return  the color formatted as an hex string
     */
    public String toHexString() {
        String str = Integer.toHexString(color.getRGB());

        for (int i = str.length(); i > 6; i--)
            str = str.substring(1);

        for (int i = str.length(); i < 6; i++)
            str = "0" + str;

        return "#" + str;
    }
}
