/*
Copyright 2008-2010 Gephi
Authors : Jeremy Subtil <jeremy.subtil@gephi.org>
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gephi.preview;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.MetaEdge;
import org.gephi.preview.api.CubicBezierCurve;
import org.gephi.preview.api.Point;
import org.gephi.preview.api.supervisors.EdgeSupervisor;
import org.gephi.preview.supervisors.EdgeSupervisorImpl;
import org.gephi.preview.util.Vector;

/**
 * Implementation of a preview edge.
 *
 * @author Jérémy Subtil <jeremy.subtil@gephi.org>
 */
public abstract class EdgeImpl extends AbstractEdge implements org.gephi.preview.api.Edge {

    protected final NodeImpl node1;
    protected final NodeImpl node2;
    protected final Vector direction;
    protected final float length;
    protected final ArrayList<CubicBezierCurve> curves = new ArrayList<CubicBezierCurve>();
    private final EdgeLabelImpl label;
    protected static final float BEZIER_CURVE_FACTOR = 0.2f;
    protected final Color originalColor;
    protected Boolean metaEdge;

    /**
     * Constructor.
     *
     * @param parent     the parent graph of the edge
     * @param thickness  the edge's thickness
     * @param node1      the edge's node 1
     * @param node2      the edge's node 2
     * @param label      the edge's label
     * @param labelSize  the edge's label size
     */
    protected EdgeImpl(GraphImpl parent, Edge edge, float thickness, NodeImpl node1, NodeImpl node2, String label, float labelSize) {
        super(parent, thickness);
        this.node1 = node1;
        this.node2 = node2;
        this.metaEdge = edge instanceof MetaEdge;

        // edge direction vector + edge length
        direction = new Vector(this.node2.getPosition());
        direction.sub(new Vector(this.node1.getPosition()));
        length = direction.mag();
        direction.normalize();

        //Color
        if (edge.getEdgeData().r() != -1) {
            originalColor = new Color(edge.getEdgeData().r(), edge.getEdgeData().g(), edge.getEdgeData().b(), edge.getEdgeData().alpha());
        } else {
            originalColor = null;
        }

        // curved edge (cubic Bézier curve)
        genCurves();

        // generate label
        if (null != label) {
            this.label = new EdgeLabelImpl(this, label, labelSize);
        } else {
            this.label = null;
        }
    }

    /**
     * Generates a curve from node 1 to node 2 and adds it to the curve list.
     */
    protected void genCurves() {
        float factor = BEZIER_CURVE_FACTOR * length;

        // normal vector to the edge
        Vector n = new Vector(direction.y, -direction.x);
        n.mult(factor);

        // first control point
        Vector v1 = new Vector(direction);
        v1.mult(factor);
        v1.add(new Vector(node1.getPosition()));
        v1.add(n);

        // second control point
        Vector v2 = new Vector(direction);
        v2.mult(-factor);
        v2.add(new Vector(node2.getPosition()));
        v2.add(n);

        curves.add(new CubicBezierCurveImpl(
                node1.getPosition(),
                new PointImpl(v1),
                new PointImpl(v2),
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

    public Color getOriginalColor() {
        return originalColor;
    }

    public EdgeLabelImpl getLabel() {
        return label;
    }

    public Float getScale() {
        return getEdgeSupervisor().getEdgeScale();
    }

    /**
     * Returns the edge's base label font.
     *
     * @return the edge's base label font
     */
    public Font getBaseLabelFont() {
        return getEdgeSupervisor().getBaseLabelFont();
    }

    /**
     * Returns the edge's direction.
     *
     * @return the edge's direction
     */
    public Vector getDirection() {
        return direction;
    }

    public Boolean getMetaEdge() {
        return metaEdge;
    }

    public void setMetaEdge(Boolean metaEdge) {
        this.metaEdge = metaEdge;
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
        Point p1 = node1.getPosition();
        Point p2 = node2.getPosition();

        return (float) Math.atan2(p2.getY() - p1.getY(), p2.getX() - p1.getX());
    }

    public Boolean isCurved() {
        return getEdgeSupervisor().getCurvedFlag();
    }

    public Boolean showLabel() {
        if (!hasLabel() || getLabel().getFont() == null) {
            return false;
        }

        EdgeSupervisor supervisor = getEdgeSupervisor();
        int labelCharCount = supervisor.getShortenLabelsFlag() ? supervisor.getLabelMaxChar() : 10;
        float minlength = node1.getRadius() + node2.getRadius() + 0.65f * labelCharCount * label.getFont().getSize();
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
