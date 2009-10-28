package org.gephi.preview;

import org.gephi.preview.util.LabelShortenerClient;
import processing.core.PVector;

/**
 * An abstract edge label.
 *
 * @author Jérémy Subtil <jeremy.subtil@gephi.org>
 */
public abstract class AbstractEdgeLabel extends AbstractEdgeChild
        implements LabelShortenerClient {

    protected final String originalValue;
    protected String value;
    protected PVector position;

    /**
     * Constructor.
     *
     * @param parent  the parent edge of the edge label
     * @param value   the value of the edge label
     */
    public AbstractEdgeLabel(EdgeImpl parent, String value) {
        super(parent);
        originalValue = value;
    }

    /**
     * Returns the edge label's angle.
     *
     * @return the edge label's angle
     */
    public float getAngle() {
        return parent.getAngle();
    }

    /**
     * Returns the edge label's position.
     *
     * @return the edge label's position
     */
    public PVector getPosition() {
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
     * Defines the edge label's current value
     *
     * @param value  the edge label's current value to set
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Sets the edge label's position above its parent edge's one.
     */
    protected void putPositionAboveEdge() {
        // normal vector for vertical align
        PVector edgeDir = parent.getDirection();
        PVector n = new PVector(edgeDir.y, -edgeDir.x);

        // the mini-label mustn't be on the edge but over/under it
        n.mult(parent.getThickness() / 2);
        position.add(n);
    }
}
