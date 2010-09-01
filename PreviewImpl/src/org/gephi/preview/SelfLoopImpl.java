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
import org.gephi.graph.api.Edge;
import org.gephi.preview.api.CubicBezierCurve;
import org.gephi.preview.api.SelfLoop;
import org.gephi.preview.supervisors.SelfLoopSupervisorImpl;
import org.gephi.preview.util.Vector;

/**
 * Implementation of a preview self-loop.
 *
 * @author Jérémy Subtil <jeremy.subtil@gephi.org>
 */
public class SelfLoopImpl extends AbstractEdge implements SelfLoop {

    private final NodeImpl node;
    private CubicBezierCurveImpl curve;
    protected final Color originalColor;

    /**
     * Constructor.
     *
     * @param parent     the parent graph of the self-loop
     * @param thickness  the self-loop's thickness
     * @param node       the self-loop's related node
     */
    public SelfLoopImpl(GraphImpl parent, Edge edge, NodeImpl node) {
        super(parent, edge.getWeight());
        this.node = node;

        //Color
        if(edge.getEdgeData().r()!=-1) {
            originalColor = new Color(edge.getEdgeData().r(), edge.getEdgeData().g(), edge.getEdgeData().b(), edge.getEdgeData().alpha());
        } else {
            originalColor = null;
        }

        // generate the self-loop's curve
        genCurve();

        // register the self-loop to its supervisor
        getSelfLoopSupervisor().addSelfLoop(this);
    }

    /**
     * Generates the self-loop's curve.
     */
    private void genCurve() {
        Vector v1 = new Vector(node.getPosition());
        v1.add(node.getDiameter(), -node.getDiameter(), 0);

        Vector v2 = new Vector(node.getPosition());
        v2.add(node.getDiameter(), node.getDiameter(), 0);

        curve = new CubicBezierCurveImpl(
                node.getPosition(),
                new PointImpl(v1),
                new PointImpl(v2),
                node.getPosition());
    }

    /**
     * Returns the self-loop's related node.
     *
     * @return the self-loop's related node
     */
    public NodeImpl getNode() {
        return node;
    }

    /**
     * Alias of getNode().
     *
     * @return the self-loop's related node
     * @see getNode()
     */
    public NodeImpl getNode1() {
        return getNode();
    }

    /**
     * Alias of getNode().
     *
     * @return the self-loop's related node
     * @see getNode()
     */
    public NodeImpl getNode2() {
        return getNode();
    }

    /**
     * Returns the self-loop's curve.
     *
     * @return the self-loop's curve
     */
    public CubicBezierCurve getCurve() {
        return curve;
    }

    public Color getOriginalColor() {
        return originalColor;
    }

    /**
     * Returns the self-loop supervisor.
     *
     * @return the controller's self-loop supervisor
     */
    public SelfLoopSupervisorImpl getSelfLoopSupervisor() {
        return (SelfLoopSupervisorImpl) parent.getModel().getSelfLoopSupervisor();
    }

    public Float getScale() {
        return getSelfLoopSupervisor().getEdgeScale();
    }
}
