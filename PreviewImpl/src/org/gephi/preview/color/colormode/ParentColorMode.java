package org.gephi.preview.color.colormode;

import org.gephi.preview.api.Holder;
import org.gephi.preview.api.color.Color;
import org.gephi.preview.api.color.colorizer.EdgeColorizerClient;
import org.gephi.preview.api.color.colorizer.NodeChildColorizer;
import org.gephi.preview.api.color.colorizer.EdgeChildColorizerClient;
import org.gephi.preview.api.color.colorizer.EdgeChildColorizer;
import org.gephi.preview.api.color.colorizer.NodeChildColorizerClient;
import org.gephi.preview.color.InheritedColor;

/**
 *
 * @author jeremy
 */
public class ParentColorMode implements NodeChildColorizer, EdgeChildColorizer {

    public static final String IDENTIFIER = "parent";

    public void color(NodeChildColorizerClient client) {
        Holder<Color> parentColorHolder = client.getParentColorHolder();
        client.setColor(new InheritedColor(parentColorHolder));
    }

    public void color(EdgeChildColorizerClient client) {
        Holder<Color> parentColorHolder = client.getParentColorHolder();
        client.setColor(new InheritedColor(parentColorHolder));
    }

    @Override
    public String toString() {
        return IDENTIFIER;
    }

    public static String getIdentifier() {
        return IDENTIFIER;
    }

    public void color(EdgeColorizerClient client) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
