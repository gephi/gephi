package org.gephi.preview;

import org.gephi.preview.api.Holder;
import org.gephi.preview.api.color.Color;
import org.gephi.preview.api.color.colorizer.EdgeColorizerClient;
import org.gephi.preview.util.HolderImpl;

/**
 *
 * @author jeremy
 */
public abstract class AbstractEdge implements EdgeColorizerClient {

    private final GraphImpl parent;
    private final Float thickness;
    private final Integer alpha; // TODO keep Edge.alpha?
    protected final HolderImpl<Color> colorHolder = new HolderImpl<Color>();

	/**
	 * Constructor.
	 *
	 * @param parent     the parent graph of the edge
	 * @param thickness  the edge's thickness
	 * @param alpha      the edge's alpha color
	 */
    public AbstractEdge(GraphImpl parent, float thickness, int alpha) {
        this.parent = parent;
        this.thickness = thickness;
        this.alpha = alpha;
    }

	/**
	 * Returns the edge's color.
	 *
	 * @return the edge's color
	 */
    public Color getColor() {
        return colorHolder.getComponent();
    }

    public Holder<Color> getColorHolder() {
        return colorHolder;
    }

	/**
	 * Returns the edge's alpha color.
	 *
	 * @return the edge's alpha color
	 */
    @Override
    public int getAlpha() {
        return alpha;
    }

	/**
	 * Returns the edge's thickness.
	 *
	 * @return the edge's thickness
	 */
    public float getThickness() {
        return thickness;
    }

	/**
	 * Sets the edge's color.
	 *
	 * @return the color to set to the edge
	 */
    @Override
    public void setColor(Color color) {
        colorHolder.setComponent(color);
    }
}
