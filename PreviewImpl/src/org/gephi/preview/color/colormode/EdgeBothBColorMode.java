package org.gephi.preview.color.colormode;

import org.gephi.preview.api.color.colorizer.EdgeChildColorizer;
import org.gephi.preview.api.color.colorizer.EdgeColorizer;
import org.gephi.preview.api.color.colorizer.EdgeColorizerClient;
import org.gephi.preview.color.MixedColor;

/**
 *
 * @author jeremy
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

	@Override
    public String toString() {
        return IDENTIFIER;
    }

    public static String getIdentifier() {
        return IDENTIFIER;
    }
}
