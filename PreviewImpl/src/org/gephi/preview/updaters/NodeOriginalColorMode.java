package org.gephi.preview.updaters;

import org.gephi.preview.api.Color;
import org.gephi.preview.api.NodeColorizer;
import org.gephi.preview.api.NodeColorizerClient;

/**
 * This color mode colors its node clients using their original color.
 *
 * @author Jérémy Subtil <jeremy.subtil@gephi.org>
 */
public class NodeOriginalColorMode implements NodeColorizer {

    private static final String IDENTIFIER = "original";

    /**
     * Colors the given client.
     *
     * @param client  the client to color
     */
    public void color(NodeColorizerClient client) {
        Color color = client.getOriginalColor();
        client.setColor(color);
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
