package org.gephi.preview.color.colormode;

import org.gephi.preview.api.color.Color;
import org.gephi.preview.api.color.colorizer.ColorizerType;
import org.gephi.preview.api.color.colorizer.NodeColorizer;
import org.gephi.preview.api.color.colorizer.NodeColorizerClient;

/**
 *
 * @author jeremy
 */
public class NodeOriginalColorMode implements NodeColorizer {

    private static final String IDENTIFIER = "original";

    public ColorizerType getColorizerType() {
        return ColorizerType.NODE_ORIGINAL;
    }

    public void color(NodeColorizerClient client) {
        Color color = client.getOriginalColor();
        client.setColor(color);
    }

    @Override
    public String toString() {
        return IDENTIFIER;
    }

    public static String getIdentifier() {
        return IDENTIFIER;
    }
}
