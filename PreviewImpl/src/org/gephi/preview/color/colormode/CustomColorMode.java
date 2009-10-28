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
 *
 * @author jeremy
 */
public class CustomColorMode
        implements GenericColorizer, NodeColorizer, NodeChildColorizer, EdgeColorizer, EdgeChildColorizer {

    private static final String IDENTIFIER = "custom";
    private final Color color;

    public CustomColorMode(int r, int g, int b) {
        color = new SimpleColor(r, g, b);
    }

    private void doColor(ColorizerClient client) {
        client.setColor(color);
    }

    public void color(ColorizerClient client) {
        doColor(client);
    }

    public void color(NodeColorizerClient client) {
        doColor(client);
    }

    public void color(NodeChildColorizerClient client) {
        doColor(client);
    }

    public void color(EdgeChildColorizerClient client) {
        doColor(client);
    }

	public void color(EdgeColorizerClient client) {
		doColor(client);
	}

    @Override
    public String toString() {
        return String.format(
                "%s [%d,%d,%d]",
                IDENTIFIER,
                color.getRed(),
                color.getGreen(),
                color.getBlue());
    }

    public static String getIdentifier() {
        return IDENTIFIER;
    }

	public Color getColor() {
		return color;
	}

	public java.awt.Color getAwtColor() {
		return new java.awt.Color(color.getRed(), color.getBlue(), color.getGreen());
	}
}
