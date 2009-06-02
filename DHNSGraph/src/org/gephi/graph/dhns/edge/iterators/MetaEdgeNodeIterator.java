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
package org.gephi.graph.dhns.edge.iterators;

import java.util.Iterator;
import org.gephi.datastructure.avl.param.ParamAVLIterator;
import org.gephi.graph.api.Edge;
import org.gephi.graph.dhns.edge.EdgeImpl;
import org.gephi.graph.dhns.node.PreNode;

/**
 * Meta Edge Iterator for edges linked to the given node. It gives IN, OUT or IN+OUT edges
 *
 * @author Mathieu Bastian
 */
public class MetaEdgeNodeIterator extends AbstractEdgeIterator implements Iterator<Edge> {

    public enum EdgeNodeIteratorMode {

        OUT, IN, BOTH
    };
    protected PreNode node;
    protected ParamAVLIterator<EdgeImpl> edgeIterator;
    protected EdgeNodeIteratorMode mode;

    public MetaEdgeNodeIterator(PreNode node, EdgeNodeIteratorMode mode) {
        this.node = node;
        this.mode = mode;
        this.edgeIterator = new ParamAVLIterator<EdgeImpl>();
        if (mode.equals(EdgeNodeIteratorMode.OUT) || mode.equals(EdgeNodeIteratorMode.BOTH)) {
            this.edgeIterator.setNode(node.getMetaEdgesOutTree());
        } else {
            this.edgeIterator.setNode(node.getMetaEdgesInTree());
        }
    }

    public boolean hasNext() {
        if (mode.equals(EdgeNodeIteratorMode.BOTH)) {
            boolean res = edgeIterator.hasNext();
            if (!res) {
                this.edgeIterator.setNode(node.getMetaEdgesInTree());
                this.mode = EdgeNodeIteratorMode.IN;
                return edgeIterator.hasNext();
            }
        } else {
            return edgeIterator.hasNext();
        }
        return false;
    }

    public EdgeImpl next() {
        return edgeIterator.next();
    }

    public void remove() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
