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
import org.gephi.graph.dhns.core.TreeStructure;
import org.gephi.graph.dhns.edge.AbstractEdge;
import org.gephi.graph.dhns.edge.HierarchyEdgeImpl;
import org.gephi.graph.dhns.node.AbstractNode;
import org.gephi.graph.dhns.node.iterators.AbstractNodeIterator;

/**
 *
 * @author Mathieu Bastian
 */
public class HierarchyEdgeIterator extends AbstractEdgeIterator implements Iterator<Edge> {

    private TreeStructure treeStructure;
    private AbstractNodeIterator nodeIterator;
    private HierarchyEdgeImpl hierarchyEdge;
    private int count = 0;

    public HierarchyEdgeIterator(TreeStructure treeStructure, AbstractNodeIterator nodeIterator) {
        this.treeStructure = treeStructure;
        this.nodeIterator = nodeIterator;
    }

    @Override
    public boolean hasNext() {
        while (nodeIterator.hasNext()) {
            AbstractNode children = nodeIterator.next();
            AbstractNode parent = children.parent.getOriginalNode();

            if (parent != treeStructure.getRoot()) {
                hierarchyEdge = new HierarchyEdgeImpl(count, children);
                return true;
            }
        }
        return false;
    }

    @Override
    public AbstractEdge next() {
        return hierarchyEdge;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
