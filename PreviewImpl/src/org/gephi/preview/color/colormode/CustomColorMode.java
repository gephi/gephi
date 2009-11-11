package org.gephi.preview.color.colormode;

import org.gephi.preview.api.color.colorizer.ColorizerClient;
import org.gephi.preview.api.color.colorizer.EdgeChildColorizerClient;
import org.gephi.preview.api.color.colorizer.NodeChildColorizerClient;
import org.gephi.preview.api.color.colorizer.NodeColorizerClient;
import org.gephi.preview.api.color.Color;
import org.gephi.preview.api.color.colorizer.EdgeChildColorizer;
import org.gephi.preview.api.color.colorizer.EdgeColorizerClient;
import org.gephi.preview.api.color.colorizer.EdgeColorizer;
import org.gephi.preview.api.color.colorizer.GenericColorizer;
import org.gephi.preview.api.color.colorizer.NodeChildColorizer;
import org.gephi.preview.api.color.colorizer.NodeColorizer;
import org.gephi.preview.color.SimpleColor;

/**
 * This color mode colors its clients using a custom color.
 *
 * @author Jérémy Subtil <jeremy.subtil@gephi.org>
 */
public class CustomColorMode
        implements GenericColorizer, NodeColorizer, NodeChildColorizer, EdgeColorizer, EdgeChildColorizer {

    private static final String IDENTIFIER = "custom";
    private final Color color;

    /**
     * Constructor.
     *
     * @param r  the red color component
     * @param g  the green color component
     * @param b  the blue color component
     */
    public CustomColorMode(int r, int g, int b) {
        color = new SimpleColor(r, g, b);
    }

    /**
     * Colors the given client.
     *
     * @param client  the client to color
     */
    private void doColor(ColorizerClient client) {
        client.setColor(color);
    }

    /**
     * Colors the given client.
     *
     * @param client  the client to color
     */
    public void color(ColorizerClient client) {
        doColor(client);
    }

    /**
     * Colors the given client.
     *
     * @param client  the client to color
     */
    public void color(NodeColorizerClient client) {
        doColor(client);
    }

    /**
     * Colors the given client.
     *
     * @param client  the client to color
     */
    public void color(NodeChildColorizerClient client) {
        doColor(client);
    }

    /**
     * Colors the given client.
     *
     * @param client  the client to color
     */
    public void color(EdgeChildColorizerClient client) {
        doColor(client);
    }

    /**
     * Colors the given client.
     *
     * @param client  the client to color
     */
	public void color(EdgeColorizerClient client) {
		doColor(client);
	}

    /**
     * Formats the colorizer as a string.
     *
     * @return the colorizer formatted as a string
     */
    @Override
    public String toString() {
        return String.format(
                "%s [%d,%d,%d]",
                IDENTIFIER,
                color.getRed(),
                color.getGreen(),
                color.getBlue());
    }

    /**
     * Returns the colorizer's identifier.
     *
     * @return the colorizer's identifier
     */
    public static String getIdentifier() {
        return IDENTIFIER;
    }

    /**
     * Returns the colorizer's reference color.
     *
     * @return the colorizer's reference color
     */
	public Color getColor() {
		return color;
	}

    /**
     * Returns the colorizer's reference color as an AWT Color object.
     *
     * @return the colorizer's reference color
     */
	public java.awt.Color getAwtColor() {
		return new java.awt.Color(color.getRed(), color.getBlue(), color.getGreen());
	}
}
