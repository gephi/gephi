package org.gephi.preview.updaters;

import org.gephi.preview.api.EdgeChildColorizer;
import org.gephi.preview.api.EdgeChildColorizerClient;
import org.gephi.preview.api.EdgeColorizer;
import org.gephi.preview.api.EdgeColorizerClient;
import org.gephi.preview.util.color.MixedColor;

/**
 * This color mode colors its clients using a mix of the colors of an edge's
 * boundaries.
 *
 * @author Jérémy Subtil <jeremy.subtil@gephi.org>
 */
public class EdgeBothBColorMode
        implements EdgeColorizer, EdgeChildColorizer {

    private static final String IDENTIFIER = "both";

    /**
     * Colors the given client.
     *
     * @param client  the client to color
     */
    public void color(EdgeColorizerClient client) {
        client.setColor(new MixedColor(
                client.getNode1().getColorHolder(),
                client.getNode2().getColorHolder()));
    }

    /**
     * Colors the given client.
     *
     * @param client  the client to color
     */
    public void color(EdgeChildColorizerClient client) {
        client.setColor(new MixedColor(
                client.getParentEdge().getNode1().getColorHolder(),
                client.getParentEdge().getNode2().getColorHolder()));
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
