package org.gephi.preview.color.colormode;

import org.gephi.preview.api.Holder;
import org.gephi.preview.api.color.Color;
import org.gephi.preview.api.color.colorizer.ColorizerType;
import org.gephi.preview.api.color.colorizer.NodeChildColorizer;
import org.gephi.preview.api.color.colorizer.NodeChildColorizerClient;
import org.gephi.preview.color.InheritedColor;

/**
 *
 * @author jeremy
 */
public class ParentNodeColorMode implements NodeChildColorizer {

    public ColorizerType getColorizerType() {
        return ColorizerType.PARENT_NODE;
    }

    public void color(NodeChildColorizerClient client) {
        Holder<Color> parentColorHolder = client.getParentNode().getColorHolder();
        client.setColor(new InheritedColor(parentColorHolder));
    }
}
