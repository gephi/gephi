package org.gephi.preview;

import org.gephi.preview.api.Color;
import org.gephi.preview.api.EdgeChildColorizerClient;
import org.gephi.preview.api.Point;
import org.gephi.preview.updaters.LabelShortenerClient;
import org.gephi.preview.util.HolderImpl;
import org.gephi.preview.util.Vector;

/**
 * Generic implementation of an edge label.
 *
 * @author Jérémy Subtil <jeremy.subtil@gephi.org>
 */
public abstract class AbstractEdgeLabel implements LabelShortenerClient, EdgeChildColorizerClient {

    protected final String originalValue;
    private final HolderImpl<Color> colorHolder = new HolderImpl<Color>();
    protected String value;
    protected PointImpl position;

    /**
     * Constructor.
     *
     * @param value   the value of the edge label
     */
    public AbstractEdgeLabel(String value) {
        originalValue = value;
    }

    /**
     * Returns the edge label's position.
     *
     * @return the edge label's position
     */
    public Point getPosition() {
        return position;
    }

    /**
     * Returns the edge label's original value.
     *
     * @return the edge label's original value
     */
    public String getOriginalValue() {
        return originalValue;
    }

    /**
     * Returns the edge label's current value.
     *
     * @return the edge label's current value
     */
    public String getValue() {
        return value;
    }

    /**
     * Returns the edge label's color.
     *
     * @return the edge label's color
     */
    public Color getColor() {
        return colorHolder.getComponent();
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setColor(Color color) {
        colorHolder.setComponent(color);
    }

    /**
     * Sets the edge label's position above its parent edge's one.
     */
    protected void putPositionAboveEdge(Vector edgeDirection, float edgeThickness) {
        // normal vector for vertical align
        Vector n = new Vector(edgeDirection.y, -edgeDirection.x);

        // the mini-label mustn't be on the edge but over/under it
        n.mult(edgeThickness / 2);
        Vector positionVector = new Vector(position);
        positionVector.add(n);

        position = new PointImpl(positionVector);
    }
}
