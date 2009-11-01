package org.gephi.preview;

import org.gephi.preview.api.Holder;
import org.gephi.preview.api.color.Color;
import org.gephi.preview.api.color.colorizer.NodeChildColorizerClient;
import org.gephi.preview.util.HolderImpl;
import processing.core.PVector;

/**
 *
 * @author jeremy
 */
public class AbstractNodeChild implements NodeChildColorizerClient {

    protected final NodeImpl parent;
    private final HolderImpl<Color> colorHolder = new HolderImpl<Color>();

    public AbstractNodeChild(NodeImpl parent) {
        this.parent = parent;
    }

    public Color getColor() {
        return colorHolder.getComponent();
    }

    public PVector getPosition() {
        return parent.getPosition();
    }

    public void setColor(Color color) {
        colorHolder.setComponent(color);
    }

    public Holder<Color> getParentColorHolder() {
        return parent.getColorHolder();
    }
}
