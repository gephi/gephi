package org.gephi.preview;

import org.gephi.preview.api.Color;
import org.gephi.preview.api.NodeChildColorizerClient;
import org.gephi.preview.api.util.Holder;
import org.gephi.preview.util.HolderImpl;
import processing.core.PVector;

/**
 * Generic implementation of a preview node child.
 *
 * @author Jérémy Subtil <jeremy.subtil@gephi.org>
 */
public class AbstractNodeChild implements NodeChildColorizerClient {

    protected final NodeImpl parent;
    private final HolderImpl<Color> colorHolder = new HolderImpl<Color>();

    /**
     * Constructor.
     *
     * @param parent  the parent node
     */
    public AbstractNodeChild(NodeImpl parent) {
        this.parent = parent;
    }

    /**
     * Returns the node child's color.
     *
     * @return the node child's color
     */
    public Color getColor() {
        return colorHolder.getComponent();
    }

    /**
     * Returns the node child's position.
     *
     * @return the node child's position
     */
    public PVector getPosition() {
        return parent.getPosition();
    }

    /**
     * Sets the color of the node child.
     *
     * @param color  the color to set to the node child
     */
    public void setColor(Color color) {
        colorHolder.setComponent(color);
    }

    /**
     * Returns the color holder of the parent node.
     *
     * @return the color holder of the parent node
     */
    public Holder<Color> getParentColorHolder() {
        return parent.getColorHolder();
    }
}
