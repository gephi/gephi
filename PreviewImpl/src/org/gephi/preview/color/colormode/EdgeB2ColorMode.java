package org.gephi.preview.color.colormode;

import org.gephi.preview.api.color.colorizer.EdgeChildColorizer;
import org.gephi.preview.api.color.colorizer.EdgeChildColorizerClient;
import org.gephi.preview.api.color.colorizer.EdgeColorizer;
import org.gephi.preview.api.color.colorizer.EdgeColorizerClient;
import org.gephi.preview.color.InheritedColor;

/**
 * This color mode colors its clients using the color of an edge's second
 * boundary.
 *
 * @author Jérémy Subtil <jeremy.subtil@gephi.org>
 */
public class EdgeB2ColorMode
		implements EdgeColorizer, EdgeChildColorizer {

	private static final String IDENTIFIER = "boundary2";

	/**
	 * Colors the given client.
	 *
	 * @param client  the client to color
	 */
	public void color(EdgeColorizerClient client) {
		client.setColor(new InheritedColor(client.getNode2().getColorHolder()));
	}

    /**
	 * Colors the given client.
	 *
	 * @param client  the client to color
	 */
	public void color(EdgeChildColorizerClient client) {
		client.setColor(new InheritedColor(client.getParentEdge().getNode2().getColorHolder()));
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
