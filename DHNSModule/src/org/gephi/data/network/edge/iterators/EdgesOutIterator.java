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
package org.gephi.data.network.edge.iterators;

import java.util.Iterator;
import org.gephi.data.network.edge.DhnsEdge;
import org.gephi.data.network.tree.TreeStructure;
import org.gephi.data.network.node.PreNode;
import org.gephi.data.network.node.treelist.SingleTreeIterator;
import org.gephi.datastructure.avl.param.ParamAVLIterator;

/**
 * Edge Iterator for <b>OUT</b> edges of the tree. Use a {@link SingleViewSpaceTreeIterator} for getting edges
 * from the tree nodes.
 * 
 * @author Mathieu Bastian
 */
public class EdgesOutIterator implements Iterator<DhnsEdge> {

    protected SingleTreeIterator treeIterator;
    protected ParamAVLIterator<DhnsEdge> edgeIterator;
    protected PreNode currentNode;
    protected DhnsEdge pointer;

    public EdgesOutIterator(TreeStructure treeStructure) {
        treeIterator = new SingleTreeIterator(treeStructure);
        edgeIterator = new ParamAVLIterator<DhnsEdge>();
    }

    @Override
    public boolean hasNext() {
        while (!edgeIterator.hasNext()) {
            if (treeIterator.hasNext()) {
                currentNode = treeIterator.next();
                edgeIterator.setNode(currentNode.getDhnsEdgesOUT());
            } else {
                return false;
            }
        }

        pointer = edgeIterator.next();
        return true;
    }

    @Override
    public DhnsEdge next() {
        return pointer;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}
