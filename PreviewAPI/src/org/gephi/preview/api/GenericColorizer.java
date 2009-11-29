package org.gephi.preview.api;

/**
 * Interface of a generic colorizer, which colors its client using a given
 * color.
 *
 * @author Jérémy Subtil <jeremy.subtil@gephi.org>
 */
public interface GenericColorizer extends Colorizer {

    /**
     * Colors the given client.
     * 
     * @param client  the client to color
     */
    public void color(ColorizerClient client);

    /**
     * Returns the colorizer's reference color.
     *
     * @return the colorizer's reference color
     */
	public Color getColor();

    /**
     * Returns the colorizer's reference color as an AWT Color object.
     *
     * @return the colorizer's reference color
     */
	public java.awt.Color getAwtColor();
}
