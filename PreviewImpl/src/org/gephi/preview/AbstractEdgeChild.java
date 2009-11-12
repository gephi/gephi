package org.gephi.preview;

import org.gephi.preview.api.color.Color;
import org.gephi.preview.api.color.colorizer.EdgeChildColorizerClient;
import org.gephi.preview.api.color.colorizer.EdgeColorizerClient;
import org.gephi.preview.api.supervisor.EdgeSupervisor;
import org.gephi.preview.api.util.Holder;
import org.gephi.preview.util.HolderImpl;

/**
 * Generic implementation of a preview edge child.
 *
 * @author Jérémy Subtil <jeremy.subtil@gephi.org>
 */
public abstract class AbstractEdgeChild implements EdgeChildColorizerClient {

    protected final EdgeImpl parent;
    private final HolderImpl<Color> colorHolder = new HolderImpl<Color>();

    /**
     * Constructor.
     *
     * @param parent  the parent edge of the edge child
     */
    public AbstractEdgeChild(EdgeImpl parent) {
        this.parent = parent;
    }

    /**
     * Returns the edge supervisor.
     *
     * @return the parent edge's edge supervisor
     */
    public EdgeSupervisor getEdgeSupervisor() {
        return parent.getEdgeSupervisor();
    }

    /**
     * Returns the parent edge.
     *
     * @return the parent edge
     */
    public EdgeColorizerClient getParentEdge() {
        return parent;
    }

    /**
	 * Returns the edge child's color.
	 *
	 * @return the edge child's color
	 */
    public Color getColor() {
        return colorHolder.getComponent();
    }

    /**
     * Returns the color holder of the edge child's parent.
     * 
     * @return the color holder of the edge child's parent
     */
    public Holder<Color> getParentColorHolder() {
        return parent.getColorHolder();
    }

    /**
	 * Sets the edge child's color.
	 *
	 * @return the color to set to the edge child
	 */
    public void setColor(Color color) {
        colorHolder.setComponent(color);
    }
}
