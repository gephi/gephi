package org.gephi.preview.updaters;

import org.gephi.preview.api.EdgeChildColorizer;
import org.gephi.preview.api.EdgeChildColorizerClient;
import org.gephi.preview.api.EdgeColorizer;
import org.gephi.preview.api.EdgeColorizerClient;
import org.gephi.preview.util.color.InheritedColor;

/**
 * This color mode colors its clients using the color of an edge's first
 * boundary.
 *
 * @author Jérémy Subtil <jeremy.subtil@gephi.org>
 */
public class EdgeB1ColorMode
        implements EdgeColorizer, EdgeChildColorizer {

    private static final String IDENTIFIER = "source";

    /**
     * Colors the given client.
     *
     * @param client  the client to color
     */
    public void color(EdgeColorizerClient client) {
        client.setColor(new InheritedColor(client.getNode1().getColorHolder()));
    }

    /**
     * Colors the given client.
     *
     * @param client  the client to color
     */
    public void color(EdgeChildColorizerClient client) {
        client.setColor(new InheritedColor(client.getParentEdge().getNode1().getColorHolder()));
    }

    /**
     * Formats the colorizer as a string.
     *
     * @return the colorizer formatted as a string
     */
    @Override
    public String toString() {
        return IDENTIFIER;
    }

    /**
     * Returns the colorizer's identifier.
     *
     * @return the colorizer's identifier
     */
    public static String getIdentifier() {
        return IDENTIFIER;
    }
}
