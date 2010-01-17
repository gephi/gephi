package org.gephi.preview;

import org.gephi.preview.api.Color;
import org.gephi.preview.api.EdgeColorizerClient;
import org.gephi.preview.api.util.Holder;
import org.gephi.preview.util.HolderImpl;

/**
 * Generic implementation of a preview edge.
 *
 * @author Jérémy Subtil <jeremy.subtil@gephi.org>
 */
public abstract class AbstractEdge implements EdgeColorizerClient {

    protected final GraphImpl parent;
    private final Float thickness;
    protected final HolderImpl<Color> colorHolder = new HolderImpl<Color>();

	/**
	 * Constructor.
	 *
	 * @param parent     the parent graph of the edge
	 * @param thickness  the edge's thickness
	 */
    public AbstractEdge(GraphImpl parent, float thickness) {
        this.parent = parent;
        this.thickness = thickness;
    }

	/**
	 * Returns the edge's color.
	 *
	 * @return the edge's color
	 */
    public Color getColor() {
        return colorHolder.getComponent();
    }

    /**
     * Returns the edge's color holder.
     * 
     * @return the edge's color holder
     */
    public Holder<Color> getColorHolder() {
        return colorHolder;
    }

	/**
	 * Returns the edge's thickness.
	 *
	 * @return the edge's thickness
	 */
    public Float getThickness() {
        return thickness;
    }

	/**
	 * Sets the edge's color.
	 *
	 * @return the color to set to the edge
	 */
    public void setColor(Color color) {
        colorHolder.setComponent(color);
    }
}
