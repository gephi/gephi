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
import org.gephi.graph.api.Edge;
import org.gephi.graph.dhns.edge.AbstractEdge;
import org.gephi.graph.dhns.edge.MetaEdgeImpl;
import org.gephi.graph.dhns.proposition.Proposition;
import org.gephi.graph.dhns.proposition.Tautology;

/**
 * Iterates over the edges contained in a meta edge. Support either directed ot undirected request.
 *
 * @author Mathieu Bastian
 */
public class MetaEdgeContentIterator extends AbstractEdgeIterator implements Iterator<Edge> {

    protected MetaEdgeImpl metaEdge;
    protected Iterator<AbstractEdge> iterator;
    protected boolean undirected;
    protected AbstractEdge pointer;
    protected boolean undirectedSecond = false;   //Flag to know we are iterating already the mutual edge

    //Proposition
    protected Proposition<AbstractEdge> proposition;

    public MetaEdgeContentIterator(MetaEdgeImpl metaEdge, boolean undirected, Proposition<AbstractEdge> proposition) {
        iterator = metaEdge.getEdges().iterator();
        this.metaEdge = metaEdge;
        this.undirected = undirected;
        if (proposition == null) {
            this.proposition = Tautology.instance;
        } else {
            this.proposition = proposition;
        }
    }

    @Override
    public boolean hasNext() {
        while (pointer == null || !proposition.evaluate(pointer) || (undirected && pointer.getUndirected() != pointer)) {
            boolean res = iterator.hasNext();
            if (res) {
                pointer = iterator.next();
            } else {
                if (undirected && !undirectedSecond) {
                    MetaEdgeImpl symmetric = getSymmetricEdge(metaEdge);
                    if (symmetric != null) {
                        iterator = symmetric.getEdges().iterator();
                        pointer = null;
                    }
                    undirectedSecond = true;
                } else {
                    return false;
                }
            }
        }
        return true;
    }

    private MetaEdgeImpl getSymmetricEdge(MetaEdgeImpl edge) {
        return edge.getTarget().getMetaEdgesOutTree().getItem(edge.getSource().getNumber());
    }

    @Override
    public AbstractEdge next() {
        AbstractEdge e = pointer;
        pointer = null;
        return e;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
