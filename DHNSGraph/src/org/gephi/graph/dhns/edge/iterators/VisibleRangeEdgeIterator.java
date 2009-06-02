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
import org.gephi.graph.dhns.edge.EdgeImpl;
import org.gephi.graph.dhns.node.PreNode;
import org.gephi.graph.dhns.node.iterators.VisibleDescendantAndSelfIterator;

/**
 * See {@link RangeEdgeIterator}
 *
 * @author Mathieu Bastian
 */
public class VisibleRangeEdgeIterator extends RangeEdgeIterator implements Iterator<Edge> {

    public VisibleRangeEdgeIterator(TreeStructure treeStructure, PreNode nodeGroup, PreNode target, boolean inner) {
        nodeIterator = new VisibleDescendantAndSelfIterator(treeStructure, nodeGroup);
        this.inner = inner;
        this.nodeGroup = nodeGroup;
        this.rangeStart = target.getPre();
        this.rangeLimit = rangeStart + target.size;
    }

    @Override
    protected boolean testTarget(EdgeImpl edgeImpl) {
        if (edgeImpl.isVisible()) {
            if (IN) {
                PreNode source = edgeImpl.getSource();
                int pre = source.getPre();
                if (pre < nodeGroup.getPre() && pre > nodeGroup.pre + nodeGroup.size) {
                    boolean isInner = pre >= rangeStart && pre <= rangeLimit;
                    return (inner && isInner) || (!inner && !isInner);
                }
            } else {
                PreNode target = edgeImpl.getTarget();
                int pre = target.getPre();
                boolean isInner = pre >= rangeStart && pre <= rangeLimit;
                return (inner && isInner) || (!inner && !isInner);
            }
        }
        return false;
    }
}
