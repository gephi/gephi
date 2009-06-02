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
import org.gephi.graph.dhns.core.TreeStructure;
import org.gephi.graph.dhns.node.PreNode;
import org.gephi.graph.dhns.node.iterators.CompleteTreeIterator;
import org.gephi.datastructure.avl.param.ParamAVLIterator;

/**
 * Meta Edge Iterator for edges in the activates view.
 * 
 * @author Mathieu Bastian
 */
public class MetaEdgeIterator implements Iterator<EdgeImpl> {

    protected CompleteTreeIterator treeIterator;
    protected ParamAVLIterator<EdgeImpl> edgeIterator;
    protected PreNode currentNode;
    protected EdgeImpl pointer;

    public MetaEdgeIterator(TreeStructure treeStructure) {
        treeIterator = new CompleteTreeIterator(treeStructure);
        edgeIterator = new ParamAVLIterator<EdgeImpl>();
    }

    @Override
    public boolean hasNext() {
        while (!edgeIterator.hasNext()) {
            if (treeIterator.hasNext()) {
                currentNode = treeIterator.next();
                edgeIterator.setNode(currentNode.getMetaEdgesOutTree());
            } else {
                return false;
            }
        }

        pointer = edgeIterator.next();
        return true;
    }

    @Override
    public EdgeImpl next() {
        return pointer;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}
