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
package org.gephi.graph.dhns.node.iterators;

import java.util.Iterator;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.Predicate;
import org.gephi.graph.dhns.edge.AbstractEdge;
import org.gephi.graph.dhns.edge.iterators.AbstractEdgeIterator;
import org.gephi.graph.dhns.node.AbstractNode;

/**
 * Iterator on neighbour of a node. The edge iterator is given to the constructor.
 *
 * @author Mathieu Bastian
 */
public class NeighborIterator extends AbstractNodeIterator implements Iterator<Node> {

    private AbstractEdgeIterator edgeIterator;
    private AbstractNode owner;
    private AbstractNode pointer;
    //Propostion
    private Predicate<AbstractNode> predicate;

    public NeighborIterator(AbstractEdgeIterator edgeIterator, AbstractNode owner, Predicate<AbstractNode> predicate) {
        this.edgeIterator = edgeIterator;
        this.owner = owner;
        this.predicate = predicate;
    }

    public boolean hasNext() {
        while (edgeIterator.hasNext()) {
            AbstractEdge edge = edgeIterator.next();
            if (!edge.isSelfLoop()) {
                if (edge.getSource() == owner) {
                    pointer = edge.getTarget();
                } else {
                    pointer = edge.getSource();
                }
                if (predicate.evaluate(pointer)) {
                    return true;
                }
            }
        }
        return false;
    }

    public AbstractNode next() {
        return pointer;
    }

    public void remove() {
        edgeIterator.remove();
    }
}
