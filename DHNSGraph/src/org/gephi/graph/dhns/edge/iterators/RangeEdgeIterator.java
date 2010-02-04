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
import org.gephi.graph.dhns.core.TreeStructure;
import org.gephi.graph.dhns.edge.AbstractEdge;
import org.gephi.graph.dhns.node.AbstractNode;
import org.gephi.graph.dhns.node.iterators.AbstractNodeIterator;
import org.gephi.graph.dhns.node.iterators.DescendantAndSelfIterator;
import org.gephi.graph.dhns.predicate.Predicate;

/**
 * See {@link RangeEdgeIterator}
 *
 * @author Mathieu Bastian
 */
public class RangeEdgeIterator extends AbstractEdgeIterator implements Iterator<Edge> {

    protected AbstractNodeIterator nodeIterator;
    protected ParamAVLIterator<AbstractEdge> edgeIterator;
    protected AbstractNode currentNode;
    protected AbstractEdge pointer;
    protected boolean IN = false;
    protected boolean inner;
    protected int rangeStart;
    protected int rangeLimit;
    protected AbstractNode nodeGroup;
    protected boolean undirected;
    protected Predicate<AbstractEdge> edgePredicate;
    protected Predicate<AbstractNode> nodePredicate;
    protected int viewId;

    public RangeEdgeIterator(TreeStructure treeStructure, int viewId, AbstractNode nodeGroup, AbstractNode target, boolean inner, boolean undirected, Predicate<AbstractNode> nodePredicate, Predicate<AbstractEdge> edgePredicate) {
        nodeIterator = new DescendantAndSelfIterator(treeStructure, nodeGroup, nodePredicate);
        this.inner = inner;
        this.nodeGroup = nodeGroup;
        this.rangeStart = target.getPre();
        this.rangeLimit = rangeStart + target.size;
        this.undirected = undirected;
        this.edgeIterator = new ParamAVLIterator<AbstractEdge>();
        this.nodePredicate = nodePredicate;
        this.edgePredicate = edgePredicate;
        this.viewId = viewId;
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
        if (!undirected || edgeImpl.getUndirected(viewId) == edgeImpl) {
            if (edgePredicate.evaluate(edgeImpl)) {
                if (IN) {
                    AbstractNode source = edgeImpl.getSource(viewId);
                    if (!nodePredicate.evaluate(source)) {
                        return false;
                    }
                    int pre = source.getPre();
                    if (!inner) {
                        return pre < rangeStart || pre > rangeLimit;
                    }
                } else {
                    AbstractNode target = edgeImpl.getTarget(viewId);
                    if (!nodePredicate.evaluate(target)) {
                        return false;
                    }
                    int pre = target.getPre();
                    boolean isInner = pre >= rangeStart && pre <= rangeLimit;
                    return (inner && isInner) || (!inner && !isInner);
                }
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
