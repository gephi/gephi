package org.gephi.preview.updaters;

import org.gephi.preview.api.Color;
import org.gephi.preview.api.EdgeChildColorizer;
import org.gephi.preview.api.EdgeChildColorizerClient;
import org.gephi.preview.api.NodeChildColorizer;
import org.gephi.preview.api.NodeChildColorizerClient;
import org.gephi.preview.api.util.Holder;
import org.gephi.preview.util.color.InheritedColor;

/**
 * This color mode colors its clients using their parent's color.
 *
 * @author Jérémy Subtil <jeremy.subtil@gephi.org>
 */
public class ParentColorMode implements NodeChildColorizer, EdgeChildColorizer {

    public static final String IDENTIFIER = "parent";

    /**
     * Colors the given client.
     *
     * @param client  the client to color
     */
    public void color(NodeChildColorizerClient client) {
        Holder<Color> parentColorHolder = client.getParentColorHolder();
        client.setColor(new InheritedColor(parentColorHolder));
    }

    /**
     * Colors the given client.
     *
     * @param client  the client to color
     */
    public void color(EdgeChildColorizerClient client) {
        Holder<Color> parentColorHolder = client.getParentColorHolder();
        client.setColor(new InheritedColor(parentColorHolder));
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
