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
import org.gephi.data.network.node.PreNode;
import org.gephi.data.network.node.treelist.SightTreeIterator;
import org.gephi.data.network.sight.SightImpl;
import org.gephi.data.network.tree.TreeStructure;
import org.gephi.datastructure.avl.param.ParamAVLIterator;

/**
 *
 * @author Mathieu Bastian
 */
public class HierarchyEdgesIterator implements Iterator<DhnsEdge> {

    protected SightTreeIterator treeIterator;
    protected ParamAVLIterator<DhnsEdge> edgeIterator;
    protected PreNode currentNode;
    protected DhnsEdge pointer;
    protected SightImpl sight;

    public HierarchyEdgesIterator(TreeStructure treeStructure, SightImpl sight) {
        treeIterator = new SightTreeIterator(treeStructure, sight);
        edgeIterator = new ParamAVLIterator<DhnsEdge>();
        this.sight = sight;
    }

    @Override
    public boolean hasNext() {
        while (!edgeIterator.hasNext()) {
            if (treeIterator.hasNext()) {
                currentNode = treeIterator.next();
                edgeIterator.setNode(currentNode.getVirtualEdgesOUT(sight));
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
