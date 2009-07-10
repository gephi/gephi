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
package org.gephi.graph.dhns.tree;

import org.gephi.graph.api.EdgeIterable;
import org.gephi.graph.api.NodeIterable;
import org.gephi.graph.api.Tree;
import org.gephi.graph.dhns.core.Dhns;
import org.gephi.graph.dhns.edge.iterators.HierarchyEdgeIterator;
import org.gephi.graph.dhns.graph.AbstractGraphImpl;
import org.gephi.graph.dhns.node.iterators.PreNodeTreeListIterator;

/**
 *
 * @author Mathieu Bastian
 */
public class HierarchyTreeImpl extends AbstractGraphImpl implements Tree {

    public HierarchyTreeImpl(Dhns dhns) {
        this.dhns = dhns;
    }

    public NodeIterable getNodes() {
        readLock();
        return dhns.newNodeIterable(new PreNodeTreeListIterator(dhns.getTreeStructure().getTree(), 1));
    }

    public EdgeIterable getEdges() {
        readLock();
        return dhns.newEdgeIterable(new HierarchyEdgeIterator(dhns.getTreeStructure(), new PreNodeTreeListIterator(dhns.getTreeStructure().getTree(), 1)));
    }
}
