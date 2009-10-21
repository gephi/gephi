package org.gephi.preview.color.colormode;

import org.gephi.preview.api.color.colorizer.EdgeChildColorizer;
import org.gephi.preview.api.color.colorizer.EdgeColorizer;
import org.gephi.preview.api.color.colorizer.EdgeColorizerClient;
import org.gephi.preview.color.InheritedColor;

/**
 *
 * @author jeremy
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

	@Override
    public String toString() {
        return IDENTIFIER;
    }

    public static String getIdentifier() {
        return IDENTIFIER;
    }
}
