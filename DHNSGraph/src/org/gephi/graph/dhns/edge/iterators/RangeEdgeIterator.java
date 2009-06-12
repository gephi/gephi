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
import org.gephi.graph.api.ClusteredGraph;
import org.gephi.graph.api.Edge;
import org.gephi.graph.dhns.core.TreeStructure;
import org.gephi.graph.dhns.edge.AbstractEdge;
import org.gephi.graph.dhns.edge.ProperEdgeImpl;
import org.gephi.graph.dhns.node.PreNode;
import org.gephi.graph.dhns.node.iterators.AbstractNodeIterator;
import org.gephi.graph.dhns.node.iterators.DescendantAndSelfIterator;

/**
 * Main edge iterator for egdes from a specific node cluster to another cluster. 
 * <p>
 * The iterator returns edges within the range between targets <b>pre</b> number and target 
 * <b>pre + size</b> number. The <code>inner</code> parameter specify if either the iterator
 * returns edges <b>in</b> the range or <b>out</b> the range.
 * <p>
 * Used for <code>getInnerEdges</code> and <code>getOuterEdges</code> methods. 
 *
 * @author Mathieu Bastian
 * @see ClusteredGraph
 */
public class RangeEdgeIterator extends AbstractEdgeIterator implements Iterator<Edge> {

    protected AbstractNodeIterator nodeIterator;
    protected ParamAVLIterator<AbstractEdge> edgeIterator;
    protected PreNode currentNode;
    protected AbstractEdge pointer;
    protected boolean IN = false;
    protected boolean inner;
    protected int rangeStart;
    protected int rangeLimit;
    protected PreNode nodeGroup;
    protected boolean undirected;

    public RangeEdgeIterator(TreeStructure treeStructure, PreNode nodeGroup, PreNode target, boolean inner, boolean undirected) {
        nodeIterator = new DescendantAndSelfIterator(treeStructure, nodeGroup);
        this.inner = inner;
        this.nodeGroup = nodeGroup;
        this.rangeStart = target.getPre();
        this.rangeLimit = rangeStart + target.size;
        this.edgeIterator = new ParamAVLIterator<AbstractEdge>();
        this.undirected = undirected;
    }

    public RangeEdgeIterator() {
    }

    @Override
    public boolean hasNext() {
        while (true) {
            while (!edgeIterator.hasNext()) {
                if (currentNode == null) {
                    if (nodeIterator.hasNext()) {
                        currentNode = nodeIterator.next();
                        edgeIterator.setNode(currentNode.getEdgesOutTree());
                        IN = false;
                    } else {
                        return false;
                    }
                } else {
                    edgeIterator.setNode(currentNode.getEdgesInTree());
                    currentNode = null;
                    IN = true;
                }
            }

            pointer = edgeIterator.next();
            if (testTarget(pointer)) {
                return true;
            }
        }
    }

    protected boolean testTarget(AbstractEdge edgeImpl) {
        if (!undirected || edgeImpl.getUndirected() == edgeImpl) {
            if (IN) {
                PreNode source = edgeImpl.getSource();
                int pre = source.getPre();
                if (!inner) {
                    return pre < rangeStart || pre > rangeLimit;
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

    @Override
    public AbstractEdge next() {
        return pointer;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
