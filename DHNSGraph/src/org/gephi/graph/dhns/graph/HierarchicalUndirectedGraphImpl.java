/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gephi.graph.dhns.graph;

import org.gephi.graph.api.Edge;
import org.gephi.graph.api.EdgeIterable;
import org.gephi.graph.api.HierarchicalUndirectedGraph;
import org.gephi.graph.api.MetaEdge;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.NodeIterable;
import org.gephi.graph.dhns.core.Dhns;
import org.gephi.graph.dhns.core.GraphViewImpl;
import org.gephi.graph.dhns.edge.AbstractEdge;
import org.gephi.graph.dhns.edge.MixedEdgeImpl;
import org.gephi.graph.dhns.edge.iterators.EdgeAndMetaEdgeIterator;
import org.gephi.graph.dhns.edge.iterators.EdgeIterator;
import org.gephi.graph.dhns.edge.iterators.EdgeNodeIterator;
import org.gephi.graph.dhns.edge.iterators.MetaEdgeIterator;
import org.gephi.graph.dhns.edge.iterators.MetaEdgeNodeIterator;
import org.gephi.graph.dhns.edge.iterators.RangeEdgeIterator;
import org.gephi.graph.dhns.node.AbstractNode;
import org.gephi.graph.dhns.node.iterators.NeighborIterator;
import org.gephi.graph.dhns.node.iterators.TreeIterator;
import org.gephi.graph.dhns.predicate.Tautology;

/**
 *
 * @author Mathieu Bastian
 */
public class HierarchicalUndirectedGraphImpl extends HierarchicalGraphImpl implements HierarchicalUndirectedGraph {

    public HierarchicalUndirectedGraphImpl(Dhns dhns, GraphViewImpl view) {
        super(dhns, view);
    }

    public boolean addEdge(Edge edge) {
        AbstractEdge absEdge = checkEdge(edge);
        if (absEdge instanceof MixedEdgeImpl && edge.isDirected() && !absEdge.isSelfLoop()) {
            throw new IllegalArgumentException("Can't add a directed egde");
        }
        AbstractNode source = checkNode(edge.getSource());
        AbstractNode target = checkNode(edge.getTarget());
        if (checkEdgeExist(source, target) || checkEdgeExist(target, source)) {
            //Edge already exist
            return false;
        }
        if (!absEdge.hasAttributes()) {
            absEdge.setAttributes(dhns.factory().newEdgeAttributes(edge.getEdgeData()));
        }
        view.getStructureModifier().addEdge(absEdge);
        dhns.touchUndirected();
        return true;
    }

    public boolean addEdge(Node node1, Node node2) {
        AbstractNode absNode1 = checkNode(node1);
        AbstractNode absNode2 = checkNode(node2);
        if (checkEdgeExist(absNode1, absNode2) || checkEdgeExist(absNode2, absNode1)) {
            //Edge already exist
            return false;
        }
        AbstractEdge edge = dhns.factory().newEdge(null, absNode1, absNode2, 1.0f, false);
        view.getStructureModifier().addEdge(edge);
        dhns.touchUndirected();
        return true;
    }

    public boolean removeEdge(Edge edge) {
        AbstractEdge absEdge = checkEdge(edge);
        boolean res = false;
        if (!absEdge.isSelfLoop()) {
            //Remove also mutual edge if present
            AbstractEdge symmetricEdge = getSymmetricEdge(absEdge);
            if (symmetricEdge != null) {
                res = view.getStructureModifier().deleteEdge(symmetricEdge);
            }
        }
        res = view.getStructureModifier().deleteEdge(absEdge) || res;
        return res;
    }

    public boolean contains(Edge edge) {
        if (edge == null) {
            throw new NullPointerException();
        }
        AbstractEdge absEdge = (AbstractEdge) edge;
        return getEdge(absEdge.getSource(view.getViewId()), absEdge.getTarget(view.getViewId())) != null;
    }

    public EdgeIterable getEdges() {
        readLock();
        return dhns.newEdgeIterable(new EdgeIterator(structure, new TreeIterator(structure, true, Tautology.instance), true, enabledNodePredicate, Tautology.instance));
    }

    public EdgeIterable getEdgesTree() {
        readLock();
        return dhns.newEdgeIterable(new EdgeIterator(structure, new TreeIterator(structure, true, Tautology.instance), true, Tautology.instance, Tautology.instance));
    }

    public EdgeIterable getEdges(Node node) {
        readLock();
        AbstractNode absNode = checkNode(node);
        return dhns.newEdgeIterable(new EdgeNodeIterator(absNode, EdgeNodeIterator.EdgeNodeIteratorMode.BOTH, true, enabledNodePredicate, Tautology.instance));
    }

    public NodeIterable getNeighbors(Node node) {
        readLock();
        AbstractNode absNode = checkNode(node);
        return dhns.newNodeIterable(new NeighborIterator(new EdgeNodeIterator(absNode, EdgeNodeIterator.EdgeNodeIteratorMode.BOTH, true, enabledNodePredicate, Tautology.instance), absNode, Tautology.instance));
    }

    public int getEdgeCount() {
        return view.getEdgesCountEnabled() - view.getMutualEdgesEnabled();
    }

    public int getDegree(Node node) {
        AbstractNode absNode = checkNode(node);
        int count = absNode.getEnabledInDegree() + absNode.getEnabledOutDegree() - absNode.getEnabledMutualDegree();
        return count;
    }

    public boolean isAdjacent(Node node1, Node node2) {
        if (node1 == node2) {
            throw new IllegalArgumentException("Nodes can't be the same");
        }
        return getEdge(node1, node2) != null;
    }

    //Graph
    public boolean isDirected(Edge edge) {
        checkEdgeOrMetaEdge(edge);
        return false;
    }

    public Edge getEdge(Node node1, Node node2) {
        if (node1 == null || node2 == null) {
            return null;
        }
        readLock();
        AbstractNode sourceNode = checkNode(node1);
        AbstractNode targetNode = checkNode(node2);
        AbstractEdge res = null;
        AbstractEdge edge1 = sourceNode.getEdgesOutTree().getItem(targetNode.getNumber());
        AbstractEdge edge2 = sourceNode.getEdgesInTree().getItem(targetNode.getNumber());
        if (edge1 != null && edge2 != null) {
            if (edge1.getId() < edge2.getId()) {
                res = edge1;
            } else {
                res = edge2;
            }
        } else if (edge1 != null) {
            res = edge1;
        } else if (edge2 != null) {
            res = edge2;
        }
        readUnlock();
        return res;
    }

    public EdgeIterable getInnerEdges(Node nodeGroup) {
        readLock();
        AbstractNode absNode = checkNode(nodeGroup);
        return dhns.newEdgeIterable(new RangeEdgeIterator(structure, view.getViewId(), absNode, absNode, true, true, Tautology.instance, Tautology.instance));
    }

    public EdgeIterable getOuterEdges(Node nodeGroup) {
        readLock();
        AbstractNode absNode = checkNode(nodeGroup);
        return dhns.newEdgeIterable(new RangeEdgeIterator(structure, view.getViewId(), absNode, absNode, false, true, Tautology.instance, Tautology.instance));
    }

    public int getMetaDegree(Node node) {
        readLock();
        AbstractNode absNode = checkNode(node);
        int count = 0;
        MetaEdgeNodeIterator itr = new MetaEdgeNodeIterator(absNode.getMetaEdgesOutTree(), absNode.getMetaEdgesInTree(), MetaEdgeNodeIterator.EdgeNodeIteratorMode.BOTH, true);
        for (; itr.hasNext();) {
            AbstractEdge edge = itr.next();
            if (edge.isSelfLoop()) {
                count++;
            }
            count++;
        }
        readUnlock();
        return count;
    }

    public EdgeIterable getMetaEdges() {
        readLock();
        return dhns.newEdgeIterable(new MetaEdgeIterator(structure, new TreeIterator(structure, true, Tautology.instance), true));
    }

    public EdgeIterable getEdgesAndMetaEdges() {
        readLock();
        return dhns.newEdgeIterable(new EdgeAndMetaEdgeIterator(structure, new TreeIterator(structure, true, Tautology.instance), true, enabledNodePredicate, Tautology.instance));
    }

    public EdgeIterable getMetaEdges(Node node) {
        readLock();
        AbstractNode absNode = checkNode(node);
        return dhns.newEdgeIterable(new MetaEdgeNodeIterator(absNode.getMetaEdgesOutTree(), absNode.getMetaEdgesInTree(), MetaEdgeNodeIterator.EdgeNodeIteratorMode.BOTH, true));
    }

    public MetaEdge getMetaEdge(Node node1, Node node2) {
        if (node1 == null || node2 == null) {
            return null;
        }
        readLock();
        AbstractNode sourceNode = checkNode(node1);
        AbstractNode targetNode = checkNode(node2);
        AbstractEdge res = null;
        AbstractEdge edge1 = sourceNode.getMetaEdgesOutTree().getItem(targetNode.getNumber());
        AbstractEdge edge2 = sourceNode.getMetaEdgesInTree().getItem(targetNode.getNumber());
        if (edge1 != null && edge2 != null) {
            if (edge1.getId() < edge2.getId()) {
                res = edge1;
            } else {
                res = edge2;
            }
        } else if (edge1 != null) {
            res = edge1;
        } else if (edge2 != null) {
            res = edge2;
        }
        readUnlock();
        return (MetaEdge) res;
    }

    @Override
    public HierarchicalUndirectedGraphImpl copy(Dhns dhns, GraphViewImpl view) {
        return new HierarchicalUndirectedGraphImpl(dhns, view);
    }

    public EdgeIterable getHierarchyEdges() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
