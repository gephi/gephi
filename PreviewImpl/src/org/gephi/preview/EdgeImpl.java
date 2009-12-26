package org.gephi.preview;

import java.awt.Font;
import java.util.ArrayList;
import org.gephi.preview.api.CubicBezierCurve;
import org.gephi.preview.api.Edge;
import org.gephi.preview.api.supervisors.EdgeSupervisor;
import org.gephi.preview.supervisors.EdgeSupervisorImpl;
import processing.core.PVector;

/**
 * Implementation of a preview edge.
 *
 * @author Jérémy Subtil <jeremy.subtil@gephi.org>
 */
public abstract class EdgeImpl extends AbstractEdge implements Edge {

    protected final NodeImpl node1;
    protected final NodeImpl node2;
    protected final PVector direction;
    protected final float length;
    protected final ArrayList<CubicBezierCurve> curves = new ArrayList<CubicBezierCurve>();
    private EdgeLabelImpl label = null;
    protected static final float BEZIER_CURVE_FACTOR = 0.2f;

    /**
     * Constructor.
     *
     * @param parent     the parent graph of the edge
     * @param thickness  the edge's thickness
     * @param node1      the edge's node 1
     * @param node2      the edge's node 2
     * @param label      the edge's label
     */
    protected EdgeImpl(GraphImpl parent, float thickness, NodeImpl node1, NodeImpl node2, String label) {
        super(parent, thickness);
        this.node1 = node1;
        this.node2 = node2;

        // edge direction vector + edge length
        PVector v = PVector.sub(this.node2.getPosition(), this.node1.getPosition());
        length = v.mag();
        direction = v.get();
        direction.normalize();

        // curved edge (cubic Bézier curve)
        genCurves();

        // generate label
        if (null != label) {
            this.label = new EdgeLabelImpl(this, label);
        }
    }

    /**
     * Generates a curve from node 1 to node 2 and adds it to the curve list.
     */
    protected void genCurves() {
        float factor = BEZIER_CURVE_FACTOR * length;
        PVector v, n;

        // normal vector to the edge
        n = new PVector(direction.y, -direction.x);
        n.mult(factor);

        // first control point
        v = PVector.mult(direction, factor);
        v.add(node1.getPosition());
        PVector cp1 = PVector.add(v, n);

        // second control point
        v = PVector.mult(direction, -factor);
        v.add(node2.getPosition());
        PVector cp2 = PVector.add(v, n);

        curves.add(new CubicBezierCurveImpl(
                node1.getPosition(),
                cp1,
                cp2,
                node2.getPosition()));
    }

    public boolean hasLabel() {
        return null != label;
    }

    public Iterable<CubicBezierCurve> getCurves() {
        return curves;
    }

    public NodeImpl getNode1() {
        return node1;
    }

    public NodeImpl getNode2() {
        return node2;
    }

    public EdgeLabelImpl getLabel() {
        return label;
    }

    /**
     * Returns the edge label font.
     *
     * @return the edge label font
     */
    public Font getLabelFont() {
        return getEdgeSupervisor().getLabelFont();
    }

    /**
     * Returns the edge's direction.
     *
     * @return the edge's direction
     */
    public PVector getDirection() {
        return direction;
    }

    /**
     * Returns the edge's length.
     *
     * @return the edge's length
     */
    public Float getLength() {
        return length;
    }

    /**
     * Returns the edge's angle.
     *
     * @return the edge's angle
     */
    public Float getAngle() {
        PVector p1 = node1.getPosition();
        PVector p2 = node2.getPosition();

        return (float) Math.atan2(p2.y - p1.y, p2.x - p1.x);
    }

    public Boolean isCurved() {
        return getEdgeSupervisor().getCurvedFlag();
    }

    public Boolean showLabel() {
        EdgeSupervisor supervisor = getEdgeSupervisor();
        float minlength = node1.getRadius() + node2.getRadius() + 0.65f * supervisor.getLabelMaxChar() * supervisor.getLabelFont().getSize();
        return supervisor.getShowLabelsFlag() && length >= minlength;
    }

    /**
     * Returns the edge supervisor.
     *
     * This method is overridden by child classes to return the right
     * supervisor.
     *
     * @return an edge supervisor
     */
    protected abstract EdgeSupervisorImpl getEdgeSupervisor();
}
