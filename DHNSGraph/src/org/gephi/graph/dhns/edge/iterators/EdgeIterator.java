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
import org.gephi.graph.dhns.edge.EdgeImpl;
import org.gephi.graph.dhns.node.PreNode;
import org.gephi.graph.dhns.core.TreeStructure;
import org.gephi.datastructure.avl.param.ParamAVLIterator;
import org.gephi.graph.api.Edge;
import org.gephi.graph.dhns.node.iterators.AbstractNodeIterator;

/**
 * Iterator for main edges for the whole graph. The node iterator is given to the constructor.
 *
 * @author Mathieu Bastian
 */
public class EdgeIterator extends AbstractEdgeIterator implements Iterator<Edge> {

    protected AbstractNodeIterator nodeIterator;
    protected ParamAVLIterator<EdgeImpl> edgeIterator;
    protected PreNode currentNode;
    protected EdgeImpl pointer;
    protected boolean undirected;

    public EdgeIterator(TreeStructure treeStructure, AbstractNodeIterator nodeIterator, boolean undirected) {
        this.nodeIterator = nodeIterator;
        edgeIterator = new ParamAVLIterator<EdgeImpl>();
        this.undirected = undirected;
    }

    @Override
    public boolean hasNext() {
        while (pointer == null || (undirected && pointer.isSecondMutual())) {
            while (!edgeIterator.hasNext()) {
                if (nodeIterator.hasNext()) {
                    currentNode = nodeIterator.next();
                    if (!currentNode.getEdgesOutTree().isEmpty()) {
                        edgeIterator.setNode(currentNode.getEdgesOutTree());
                    }
                } else {
                    return false;
                }
            }

            pointer = edgeIterator.next();
        }

        return true;
    }

    @Override
    public EdgeImpl next() {
        EdgeImpl e = pointer;
        pointer = null;
        return e;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}
