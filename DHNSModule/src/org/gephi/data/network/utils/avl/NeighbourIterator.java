/*
Copyright 2008 WebAtlas
Authors : Mathieu Bastian, Mathieu Jacomy, Julian Bilcke
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gephi.data.network.utils.avl;

import java.util.Iterator;
import org.gephi.data.network.edge.DhnsEdge;
import org.gephi.data.network.node.PreNode;

/**
 * Iterator on neighbour of a node. Based on the <code>DhnsEdgeTree</code> data, one can
 * retrieve edges connected to the node owner of the tree and then the {@link PreNode} neighbour.
 * 
 * @author Mathieu Bastian
 */
public class NeighbourIterator implements Iterator<PreNode> {

    private Iterator<DhnsEdge> edgeIterator;
    private PreNode owner;

    public NeighbourIterator(Iterator<DhnsEdge> edgeIterator, PreNode owner) {
        this.edgeIterator = edgeIterator;
        this.owner = owner;
    }

    public boolean hasNext() {
        return edgeIterator.hasNext();
    }

    public PreNode next() {
        DhnsEdge edge = edgeIterator.next();
        if (edge.getPreNodeFrom() == owner) {
            return edge.getPreNodeTo();
        } else {
            return edge.getPreNodeFrom();
        }
    }

    public void remove() {
        edgeIterator.remove();
    }
}
