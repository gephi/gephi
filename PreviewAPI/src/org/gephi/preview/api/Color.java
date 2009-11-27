package org.gephi.preview.api;

/**
 * Interface of a color.
 *
 * @author Jérémy Subtil <jeremy.subtil@gephi.org>
 */
public interface Color {

    /**
     * Returns the red component.
     *
     * @return the red component
     */
    public Integer getRed();

    /**
     * Returns the green component.
     *
     * @return the green component
     */
    public Integer getGreen();

    /**
     * Returns the blue component.
     *
     * @return the blue component
     */
    public Integer getBlue();

    /**
     * Formats the color as an hex string.
     *
     * @return  the color formatted as an hex string
     */
    public String toHexString();
}
