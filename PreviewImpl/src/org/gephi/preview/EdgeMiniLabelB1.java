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

import org.gephi.preview.util.HAlignImpl;
import org.gephi.preview.util.Vector;

/**
 * An edge mini-label related to the parent edge's first boundary.
 *
 * @author Jérémy Subtil <jeremy.subtil@gephi.org>
 */
class EdgeMiniLabelB1 extends EdgeMiniLabelImpl {

    /**
     * Constructor.
     *
     * @param parent  the parent edge of the edge label
     */
    public EdgeMiniLabelB1(DirectedEdgeImpl parent) {
        super(parent, parent.getNode2().getLabel().getOriginalValue());
        hAlign = HAlignImpl.LEFT;
    }

    /**
     * Generates the edge mini-label's position.
     */
    public void genPosition() {
        NodeImpl n1 = parent.getNode1();

        // relative position from the first boundary
        Vector positionVector = new Vector(n1.getPosition());

        // adds the added radius
        Vector move = new Vector(parent.getDirection());
        move.mult(getDirectedEdgeSupervisor().getMiniLabelAddedRadius() + n1.getRadius());
        positionVector.add(move);

        position = new PointImpl(positionVector);

        // sets label position above the parent edge
        putPositionAboveEdge(parent.getDirection(), parent.getThickness());
    }
}
