package org.gephi.preview;

import org.gephi.preview.api.Color;
import org.gephi.preview.api.EdgeArrow;
import org.gephi.preview.api.EdgeChildColorizerClient;
import org.gephi.preview.api.EdgeColorizerClient;
import org.gephi.preview.api.supervisors.DirectedEdgeSupervisor;
import org.gephi.preview.api.util.Holder;
import org.gephi.preview.util.HolderImpl;
import processing.core.PVector;

/**
 * Implementation of an edge arrow.
 *
 * @author Jérémy Subtil <jeremy.subtil@gephi.org>
 */
public abstract class EdgeArrowImpl implements EdgeArrow, EdgeChildColorizerClient {

    protected final DirectedEdgeImpl parent;
    private final HolderImpl<Color> colorHolder = new HolderImpl<Color>();
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
    public EdgeArrowImpl(DirectedEdgeImpl parent) {
        this.parent = parent;
    }

    /**
     * Returns the directed edge supervisor.
     *
     * @return the directed edge supervisor
     */
    public DirectedEdgeSupervisor getDirectedEdgeSupervisor() {
        return ((DirectedEdgeImpl) parent).getDirectedEdgeSupervisor();
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

    public final PVector getPt1() {
        return pt1;
    }

    public final PVector getPt2() {
        return pt2;
    }

    public final PVector getPt3() {
        return pt3;
    }

    public Color getColor() {
        return colorHolder.getComponent();
    }

    public EdgeColorizerClient getParentEdge() {
        return parent;
    }

    public Holder<Color> getParentColorHolder() {
        return parent.getColorHolder();
    }

    public void setColor(Color color) {
        colorHolder.setComponent(color);
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
        float arrowSize = getDirectedEdgeSupervisor().getArrowSize();
        pt2 = PVector.mult(direction, arrowSize);
        pt2.add(pt1);
    }

    /**
     * Generates the edge arrow's third point.
     */
    protected void genPt3() {
        float arrowSize = getDirectedEdgeSupervisor().getArrowSize();
        pt3 = new PVector(-direction.y, direction.x);
        pt3.mult(BASE_RATIO * arrowSize); // base size
        pt3.add(pt1);
    }
}
