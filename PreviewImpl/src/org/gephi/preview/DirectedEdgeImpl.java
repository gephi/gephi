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

import java.awt.Font;
import java.util.ArrayList;
import org.gephi.preview.api.DirectedEdge;
import org.gephi.preview.api.EdgeArrow;
import org.gephi.preview.api.EdgeMiniLabel;
import org.gephi.preview.api.supervisors.DirectedEdgeSupervisor;
import org.gephi.preview.supervisors.DirectedEdgeSupervisorImpl;
import org.gephi.preview.supervisors.EdgeSupervisorImpl;

/**
 * Implementation of a preview directed edge.
 *
 * @author Jérémy Subtil <jeremy.subtil@gephi.org>
 */
public abstract class DirectedEdgeImpl extends EdgeImpl implements DirectedEdge {

    protected final ArrayList<EdgeArrow> arrows = new ArrayList<EdgeArrow>();
    protected final ArrayList<EdgeMiniLabel> miniLabels = new ArrayList<EdgeMiniLabel>();

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
    protected DirectedEdgeImpl(GraphImpl parent, float thickness, NodeImpl node1, NodeImpl node2, String label, float labelSize) {
        super(parent, thickness, node1, node2, label, labelSize);

        // generate arrows
        arrows.add(new EdgeArrowB1Out(this));
        arrows.add(new EdgeArrowB2In(this));

        // generate mini-labels
        if (node1.hasLabel()) {
            miniLabels.add(new EdgeMiniLabelB1(this));
        }
        if (node2.hasLabel()) {
            miniLabels.add(new EdgeMiniLabelB2(this));
        }
    }

    /**
     * Returns the directed edge supervisor.
     *
     * This method is overridden by child classes to return the right
     * supervisor.
     *
     * @return the directed edge supervisor
     */
    public abstract DirectedEdgeSupervisorImpl getDirectedEdgeSupervisor();

    public Iterable<EdgeArrow> getArrows() {
        return arrows;
    }

    public Iterable<EdgeMiniLabel> getMiniLabels() {
        return miniLabels;
    }

    /**
     * Returns the edge mini-label font.
     *
     * @return the edge mini-label font
     */
    public Font getMiniLabelFont() {
        return getDirectedEdgeSupervisor().getMiniLabelFont();
    }

    public Boolean showArrows() {
        DirectedEdgeSupervisor supervisor = getDirectedEdgeSupervisor();
        float minlength = node1.getRadius() + node2.getRadius() + 2 * supervisor.getArrowAddedRadius() + 2 * supervisor.getArrowSize() + 30;
        return supervisor.getShowArrowsFlag() && length >= minlength;
    }

    public Boolean showMiniLabels() {
        DirectedEdgeSupervisor supervisor = getDirectedEdgeSupervisor();
        int labelSize = supervisor.getShortenMiniLabelsFlag() ? supervisor.getMiniLabelMaxChar() : 10;
        float minlength = node1.getRadius() + node2.getRadius() + 2 * 0.65f * labelSize * supervisor.getMiniLabelFont().getSize() + 30;
        return supervisor.getShowMiniLabelsFlag() && length >= minlength;
    }

    @Override
    protected EdgeSupervisorImpl getEdgeSupervisor() {
        return getDirectedEdgeSupervisor();
    }
}
