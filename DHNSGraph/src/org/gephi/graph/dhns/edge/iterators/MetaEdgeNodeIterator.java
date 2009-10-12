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
import org.gephi.graph.dhns.edge.MetaEdgeImpl;
import org.gephi.graph.dhns.node.AbstractNode;
import org.gephi.graph.dhns.proposition.Proposition;
import org.gephi.graph.dhns.proposition.Tautology;
import org.gephi.graph.dhns.view.View;

/**
 * Edge Iterator for edges linked to the given node. It gives IN, OUT or IN+OUT edges
 *
 * @author Mathieu Bastian
 * @see EdgeNodeIterator
 */
public class MetaEdgeNodeIterator extends AbstractEdgeIterator implements Iterator<Edge> {

    public enum EdgeNodeIteratorMode {

        OUT, IN, BOTH
    };
    protected AbstractNode node;
    protected ParamAVLIterator<MetaEdgeImpl> edgeIterator;
    protected EdgeNodeIteratorMode mode;
    protected MetaEdgeImpl pointer;
    protected boolean undirected;
    protected View view;

    //Proposition
    protected Predicate<AbstractEdge> proposition;

    public MetaEdgeNodeIterator(View view, AbstractNode node, EdgeNodeIteratorMode mode, boolean undirected, Predicate<AbstractEdge> proposition) {
        this.node = node;
        this.mode = mode;
        this.view = view;
        this.edgeIterator = new ParamAVLIterator<MetaEdgeImpl>();
        if (mode.equals(EdgeNodeIteratorMode.OUT) || mode.equals(EdgeNodeIteratorMode.BOTH)) {
            this.edgeIterator.setNode(node.getMetaEdgesOutTree(view));
        } else {
            this.edgeIterator.setNode(node.getMetaEdgesInTree(view));
        }
        this.undirected = undirected;
        if (proposition == null) {
            this.proposition = Tautology.instance;
        } else {
            this.proposition = proposition;
        }
    }

    public boolean hasNext() {
        while (pointer == null || (undirected && pointer.getUndirected(view) != pointer) || !proposition.evaluate(pointer)) {
            if (mode.equals(EdgeNodeIteratorMode.BOTH)) {
                boolean res = edgeIterator.hasNext();
                if (res) {
                    pointer = edgeIterator.next();
                    if (pointer.isSelfLoop()) {  //Ignore self loop here to avoid double iteration
                        pointer = null;
                    }
                } else {
                    this.edgeIterator.setNode(node.getMetaEdgesInTree(view));
                    this.mode = EdgeNodeIteratorMode.IN;
                }
            } else {
                if (edgeIterator.hasNext()) {
                    pointer = edgeIterator.next();
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
