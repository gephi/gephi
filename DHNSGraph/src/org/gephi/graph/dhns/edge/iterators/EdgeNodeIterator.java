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
import org.gephi.graph.api.Predicate;
import org.gephi.graph.dhns.edge.AbstractEdge;
import org.gephi.graph.dhns.edge.ProperEdgeImpl;
import org.gephi.graph.dhns.node.AbstractNode;

/**
 * Edge Iterator for edges linked to the given node. It gives IN, OUT or IN+OUT edges
 *
 * @author Mathieu Bastian
 * @see EdgeNodeIterator
 */
public class EdgeNodeIterator extends AbstractEdgeIterator implements Iterator<Edge> {

    public enum EdgeNodeIteratorMode {

        OUT, IN, BOTH
    };
    protected AbstractNode node;
    protected int viewId;
    protected ParamAVLIterator<ProperEdgeImpl> edgeIterator;
    protected EdgeNodeIteratorMode mode;
    protected AbstractEdge pointer;
    protected boolean undirected;
    protected Predicate<AbstractNode> nodePredicate;
    protected Predicate<AbstractEdge> edgePredicate;

    public EdgeNodeIterator(AbstractNode node, EdgeNodeIteratorMode mode, boolean undirected, Predicate<AbstractNode> nodePredicate, Predicate<AbstractEdge> edgePredicate) {
        this.node = node;
        this.mode = mode;
        this.viewId = node.getViewId();
        this.undirected = undirected;
        this.edgeIterator = new ParamAVLIterator<ProperEdgeImpl>();
        if (mode.equals(EdgeNodeIteratorMode.OUT) || mode.equals(EdgeNodeIteratorMode.BOTH)) {
            this.edgeIterator.setNode(node.getEdgesOutTree());
        } else {
            this.edgeIterator.setNode(node.getEdgesInTree());
        }
        this.nodePredicate = nodePredicate;
        this.edgePredicate = edgePredicate;
    }

    public boolean hasNext() {
        while (pointer == null || (undirected && pointer.getUndirected(viewId) != pointer)) {
            if (mode.equals(EdgeNodeIteratorMode.BOTH)) {
                boolean res = edgeIterator.hasNext();
                if (res) {
                    pointer = edgeIterator.next();
                    if (pointer.isSelfLoop()) {  //Ignore self loop here to avoid double iteration
                        pointer = null;
                    }
                } else {
                    this.edgeIterator.setNode(node.getEdgesInTree());
                    this.mode = EdgeNodeIteratorMode.IN;
                }
            } else {
                if (edgeIterator.hasNext()) {
                    pointer = edgeIterator.next();
                    if (!nodePredicate.evaluate(mode.equals(EdgeNodeIteratorMode.IN) ? pointer.getSource() : pointer.getTarget())) {
                        pointer = null;
                    }
                } else {
                    return false;
                }
            }
        }
        return true;
    }

    public AbstractEdge next() {
        AbstractEdge e = pointer;
        pointer = null;
        return e;
    }

    public void remove() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
