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

import org.gephi.preview.api.supervisors.DirectedEdgeSupervisor;
import org.gephi.preview.util.Vector;

/**
 * An edge arrow outgoing from the parent edge's first boundary.
 *
 * @author Jérémy Subtil <jeremy.subtil@gephi.org>
 */
class EdgeArrowB1Out extends EdgeArrowImpl {

    /**
     * Constructor.
     *
     * @param parent  the parent edge of the edge arrow
     */
    public EdgeArrowB1Out(DirectedEdgeImpl parent) {
        super(parent);
        refNode = parent.getNode1();
        direction = new Vector(parent.getDirection().x, parent.getDirection().y);
    }

    /**
     * Generates the edge arrow's added radius.
     */
    protected void genAddedRadius() {
        DirectedEdgeSupervisor supervisor = getDirectedEdgeSupervisor();
        addedRadius = supervisor.getArrowAddedRadius() + supervisor.getArrowSize() + refNode.getRadius();
    }
}
