package org.gephi.preview.color.colormode;

import org.gephi.preview.api.color.Color;
import org.gephi.preview.api.color.colorizer.NodeChildColorizer;
import org.gephi.preview.api.color.colorizer.EdgeChildColorizerClient;
import org.gephi.preview.api.color.colorizer.EdgeChildColorizer;
import org.gephi.preview.api.color.colorizer.NodeChildColorizerClient;
import org.gephi.preview.api.util.Holder;
import org.gephi.preview.color.InheritedColor;

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
