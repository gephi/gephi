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

import org.gephi.graph.api.Edge;
import org.gephi.preview.api.UnidirectionalEdge;
import org.gephi.preview.supervisors.DirectedEdgeSupervisorImpl;

/**
 * Implementation of an unidirectional preview edge.
 *
 * @author Jérémy Subtil <jeremy.subtil@gephi.org>
 */
public class UnidirectionalEdgeImpl extends DirectedEdgeImpl
        implements UnidirectionalEdge {

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
    public UnidirectionalEdgeImpl(GraphImpl parent, Edge edge, float thickness, NodeImpl node1, NodeImpl node2, String label, float labelSize) {
        super(parent, edge, thickness, node1, node2, label, labelSize);

        getDirectedEdgeSupervisor().addEdge(this);
    }

    @Override
    public DirectedEdgeSupervisorImpl getDirectedEdgeSupervisor() {
        return (DirectedEdgeSupervisorImpl) parent.getModel().getUniEdgeSupervisor();
    }
}
