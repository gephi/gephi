package org.gephi.preview;

import org.gephi.preview.api.EdgeArrow;
import processing.core.PVector;

/**
 * Implementation of an edge arrow.
 *
 * @author Jérémy Subtil <jeremy.subtil@gephi.org>
 */
public abstract class EdgeArrowImpl extends AbstractEdgeChild implements EdgeArrow {

    protected PVector pt1;
    protected PVector pt2;
    protected PVector pt3;
    protected NodeImpl refNode;
    protected PVector direction;
    protected float addedRadius;
    protected static final float BASE_RATIO = 0.5f;

    /**
     * Constructor.
     *
     * @param parent  the parent edge of the edge arrow
     */
    public EdgeArrowImpl(EdgeImpl parent) {
        super(parent);
    }

    /**
     * Generates the edge arrow's position.
     */
    public void genPosition() {
        genAddedRadius();
        genPt1();           // right angle point
        genPt2();           // direction point on the edge
        genPt3();           // last point (not on the edge)
    }

    /**
     * Returns the edge arrow's first point.
     *
     * @return the edge arrow's first point
     */
    public final PVector getPt1() {
        return pt1;
    }

    /**
     * Returns the edge arrow's second point.
     *
     * @return the edge arrow's second point
     */
    public final PVector getPt2() {
        return pt2;
    }

    /**
     * Returns the edge arrow's third point.
     *
     * @return the edge arrow's third point
     */
    public final PVector getPt3() {
        return pt3;
    }

    /**
     * Generates the edge arrow's added radius.
     */
    protected abstract void genAddedRadius();

    /**
     * Generates the edge arrow's first point.
     */
    protected void genPt1() {
        pt1 = PVector.mult(direction, addedRadius);
        pt1.add(refNode.getPosition());
    }

    /**
     * Generates the edge arrow's second point.
     */
    protected void genPt2() {
        float arrowSize = getEdgeSupervisor().getArrowSize();
        pt2 = PVector.mult(direction, arrowSize);
        pt2.add(pt1);
    }

    /**
     * Generates the edge arrow's third point.
     */
    protected void genPt3() {
        float arrowSize = getEdgeSupervisor().getArrowSize();
        pt3 = new PVector(-direction.y, direction.x);
        pt3.mult(BASE_RATIO * arrowSize); // base size
        pt3.add(pt1);
    }
}
