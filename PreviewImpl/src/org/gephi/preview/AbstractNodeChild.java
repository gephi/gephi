package org.gephi.preview;

import org.gephi.preview.api.color.Color;
import org.gephi.preview.api.color.colorizer.NodeChildColorizerClient;
import org.gephi.preview.util.HolderImpl;
import processing.core.PVector;

/**
 *
 * @author jeremy
 */
public class AbstractNodeChild implements NodeChildColorizerClient {

    private final NodeImpl parent;
    private final HolderImpl<Color> colorHolder = new HolderImpl<Color>();

    public AbstractNodeChild(NodeImpl parent) {
        this.parent = parent;
    }

    public final Color getColor() {
        return colorHolder.getComponent();
    }

    public final NodeImpl getParentNode() {
        return parent;
    }

    public final PVector getPosition() {
        return parent.getPosition();
    }

    @Override
    public final void setColor(Color color) {
        colorHolder.setComponent(color);
    }
}
