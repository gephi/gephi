package org.gephi.preview;

import org.gephi.preview.api.Color;
import org.gephi.preview.api.EdgeArrow;
import org.gephi.preview.api.EdgeChildColorizerClient;
import org.gephi.preview.api.EdgeColorizerClient;
import org.gephi.preview.api.Point;
import org.gephi.preview.api.supervisors.DirectedEdgeSupervisor;
import org.gephi.preview.api.util.Holder;
import org.gephi.preview.util.HolderImpl;
import org.gephi.preview.util.Vector;

/**
 * Implementation of an edge arrow.
 *
 * @author Jérémy Subtil <jeremy.subtil@gephi.org>
 */
public abstract class EdgeArrowImpl implements EdgeArrow, EdgeChildColorizerClient {

    protected final DirectedEdgeImpl parent;
    private final HolderImpl<Color> colorHolder = new HolderImpl<Color>();
    protected PointImpl pt1, pt2, pt3;
    protected NodeImpl refNode;
    protected Vector direction;
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

    public final Point getPt1() {
        return pt1;
    }

    public final Point getPt2() {
        return pt2;
    }

    public final Point getPt3() {
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
        Vector v = new Vector(direction);
        v.mult(addedRadius);
        v.add(new Vector(refNode.getPosition()));

        pt1 = new PointImpl(v);
    }

    /**
     * Generates the edge arrow's second point.
     */
    protected void genPt2() {
        Vector v = new Vector(direction);
        v.mult(getDirectedEdgeSupervisor().getArrowSize());
        v.add(new Vector(pt1));

        pt2 = new PointImpl(v);
    }

    /**
     * Generates the edge arrow's third point.
     */
    protected void genPt3() {
        Vector v = new Vector(-direction.y, direction.x);
        v.mult(BASE_RATIO * getDirectedEdgeSupervisor().getArrowSize());
        v.add(new Vector(pt1));

        pt3 = new PointImpl(v);
    }
}
