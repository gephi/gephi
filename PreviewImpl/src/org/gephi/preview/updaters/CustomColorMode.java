package org.gephi.preview.updaters;

import org.gephi.preview.api.Color;
import org.gephi.preview.api.ColorizerClient;
import org.gephi.preview.api.EdgeChildColorizer;
import org.gephi.preview.api.EdgeChildColorizerClient;
import org.gephi.preview.api.EdgeColorizer;
import org.gephi.preview.api.EdgeColorizerClient;
import org.gephi.preview.api.GenericColorizer;
import org.gephi.preview.api.NodeChildColorizer;
import org.gephi.preview.api.NodeChildColorizerClient;
import org.gephi.preview.api.NodeColorizer;
import org.gephi.preview.api.NodeColorizerClient;
import org.gephi.preview.util.color.SimpleColor;

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
        return new java.awt.Color(color.getRed(), color.getGreen(), color.getBlue());
    }
}
