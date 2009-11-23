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
package org.gephi.graph.dhns.graph;

import org.gephi.graph.api.Edge;
import org.gephi.graph.api.EdgeIterable;
import org.gephi.graph.api.HierarchicalDirectedGraph;
import org.gephi.graph.api.MetaEdge;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.NodeIterable;
import org.gephi.graph.api.Predicate;
import org.gephi.graph.api.View;
import org.gephi.graph.dhns.core.Dhns;
import org.gephi.graph.dhns.core.GraphStructure;
import org.gephi.graph.dhns.edge.AbstractEdge;
import org.gephi.graph.dhns.edge.iterators.BiEdgeIterator;
import org.gephi.graph.dhns.edge.iterators.EdgeNodeIterator;
import org.gephi.graph.dhns.edge.iterators.MetaEdgeNodeIterator;
import org.gephi.graph.dhns.edge.iterators.RangeEdgeIterator;
import org.gephi.graph.dhns.node.AbstractNode;
import org.gephi.graph.dhns.node.iterators.NeighborIterator;
import org.gephi.graph.dhns.utils.avl.MetaEdgeTree;

/**
 *
 * @author Mathieu Bastian
 */
public class HierarchicalDirectedGraphImplFiltered extends HierarchicalGraphImplFiltered implements HierarchicalDirectedGraph {

    private HierarchicalDirectedGraphImpl delegate;

    public HierarchicalDirectedGraphImplFiltered(Dhns dhns, GraphStructure graphStructure, View view) {
        super(dhns, graphStructure, view);
        this.delegate = new HierarchicalDirectedGraphImpl(dhns, structure);
    }

    @Override
    public NodeIterable getSuccessors(Node node) {
        readLock();
        view.checkUpdate();
        AbstractNode absNode = checkNode(node);
        Predicate<AbstractNode> nodePredicate = view.getClusteredLayerNodePredicate();
        Predicate<AbstractEdge> edgePredicate = view.getClusteredLayerEdgePredicate();
        return dhns.newNodeIterable(new NeighborIterator(new EdgeNodeIterator(absNode, EdgeNodeIterator.EdgeNodeIteratorMode.OUT, false, nodePredicate, edgePredicate), absNode, nodePredicate));
    }

    @Override
    public NodeIterable getPredecessors(Node node) {
        readLock();
        view.checkUpdate();
        AbstractNode absNode = checkNode(node);
        Predicate<AbstractNode> nodePredicate = view.getClusteredLayerNodePredicate();
        Predicate<AbstractEdge> edgePredicate = view.getClusteredLayerEdgePredicate();
        return dhns.newNodeIterable(new NeighborIterator(new EdgeNodeIterator(absNode, EdgeNodeIterator.EdgeNodeIteratorMode.IN, false, nodePredicate, edgePredicate), absNode, nodePredicate));

    }

    @Override
    public int getInDegree(Node node) {
        view.checkUpdate();
        AbstractNode absNode = checkNode(node);
        int count = view.getClusteredLayerInDegree(absNode);
        return count;
    }

    @Override
    public int getOutDegree(Node node) {
        view.checkUpdate();
        AbstractNode absNode = checkNode(node);
        int count = view.getClusteredLayerOutDegree(absNode);
        return count;
    }

    @Override
    public int getDegree(Node node) {
        view.checkUpdate();
        AbstractNode absNode = checkNode(node);
        int count = view.getClusteredLayerInDegree(absNode);
        return count;
    }

    @Override
    public EdgeIterable getEdges() {
        readLock();
        view.checkUpdate();
        return dhns.newEdgeIterable(view.getClusteredLayerEdgeIterator());
    }

    @Override
    public EdgeIterable getInEdges(Node node) {
        readLock();
        view.checkUpdate();
        AbstractNode absNode = checkNode(node);
        return dhns.newEdgeIterable(new EdgeNodeIterator(absNode, EdgeNodeIterator.EdgeNodeIteratorMode.IN, false, view.getClusteredLayerNodePredicate(), view.getClusteredLayerEdgePredicate()));
    }

    @Override
    public EdgeIterable getOutEdges(Node node) {
        readLock();
        view.checkUpdate();
        AbstractNode absNode = checkNode(node);
        return dhns.newEdgeIterable(new EdgeNodeIterator(absNode, EdgeNodeIterator.EdgeNodeIteratorMode.OUT, false, view.getClusteredLayerNodePredicate(), view.getClusteredLayerEdgePredicate()));
    }

    @Override
    public EdgeIterable getEdges(Node node) {
        readLock();
        view.checkUpdate();
        AbstractNode absNode = checkNode(node);
        return dhns.newEdgeIterable(new EdgeNodeIterator(absNode, EdgeNodeIterator.EdgeNodeIteratorMode.BOTH, false, view.getClusteredLayerNodePredicate(), view.getClusteredLayerEdgePredicate()));
    }

    @Override
    public NodeIterable getNeighbors(Node node) {
        readLock();
        view.checkUpdate();
        AbstractNode absNode = checkNode(node);
        Predicate<AbstractNode> nodePredicate = view.getClusteredLayerNodePredicate();
        Predicate<AbstractEdge> edgePredicate = view.getClusteredLayerEdgePredicate();
        return dhns.newNodeIterable(new NeighborIterator(new EdgeNodeIterator(absNode, EdgeNodeIterator.EdgeNodeIteratorMode.BOTH, true, nodePredicate, edgePredicate), absNode, nodePredicate));
    }

    @Override
    public Edge getEdge(Node source, Node target) {
        view.checkUpdate();
        AbstractNode sourceNode = checkNode(source);
        AbstractNode targetNode = checkNode(target);
        Predicate<AbstractEdge> edgePredicate = view.getClusteredLayerEdgePredicate();
        AbstractEdge res = sourceNode.getEdgesOutTree().getItem(targetNode.getNumber());
        if(res!=null && edgePredicate.evaluate(res)) {
            return res;
        }
        return null;
    }

    @Override
    public int getEdgeCount() {
        view.checkUpdate();
        int count = view.getClusteredEdgesCount();
        return count;
    }

    @Override
    public EdgeIterable getInnerEdges(Node nodeGroup) {
        readLock();
        view.checkUpdate();
        AbstractNode absNode = checkNode(nodeGroup);
        return dhns.newEdgeIterable(new RangeEdgeIterator(structure.getStructure(), absNode, absNode, true, false, view.getHierarchyLayerNodePredicate(), view.getHierarchyLayerEdgePredicate()));
    }

    @Override
    public EdgeIterable getOuterEdges(Node nodeGroup) {
        readLock();
        view.checkUpdate();
        AbstractNode absNode = checkNode(nodeGroup);
        return dhns.newEdgeIterable(new RangeEdgeIterator(structure.getStructure(), absNode, absNode, false, false, view.getHierarchyLayerNodePredicate(), view.getHierarchyLayerEdgePredicate()));
    }

    @Override
    public EdgeIterable getMetaEdges() {
        readLock();
        view.checkUpdate();
        return dhns.newEdgeIterable(view.getMetaEdgeIterator());
    }

    @Override
    public EdgeIterable getEdgesAndMetaEdges() {
        readLock();
        view.checkUpdate();
        return dhns.newEdgeIterable(new BiEdgeIterator(view.getClusteredLayerEdgeIterator(), view.getMetaEdgeIterator()));
    }

    @Override
    public EdgeIterable getMetaEdges(Node node) {
        readLock();
        view.checkUpdate();
        AbstractNode absNode = checkNode(node);
        MetaEdgeTree inTree = view.getInMetaEdgeTree(absNode);
        MetaEdgeTree outTree = view.getOutMetaEdgeTree(absNode);
        return dhns.newEdgeIterable(new MetaEdgeNodeIterator(outTree, inTree, MetaEdgeNodeIterator.EdgeNodeIteratorMode.BOTH, false));
    }

    @Override
    public EdgeIterable getMetaInEdges(Node node) {
        readLock();
        view.checkUpdate();
        AbstractNode absNode = checkNode(node);
        MetaEdgeTree inTree = view.getInMetaEdgeTree(absNode);
        return dhns.newEdgeIterable(new MetaEdgeNodeIterator(null, inTree, MetaEdgeNodeIterator.EdgeNodeIteratorMode.IN, false));
    }

    @Override
    public EdgeIterable getMetaOutEdges(Node node) {
        readLock();
        view.checkUpdate();
        AbstractNode absNode = checkNode(node);
        MetaEdgeTree outTree = view.getOutMetaEdgeTree(absNode);
        return dhns.newEdgeIterable(new MetaEdgeNodeIterator(outTree, null, MetaEdgeNodeIterator.EdgeNodeIteratorMode.OUT, false));
    }

    @Override
    public int getMetaInDegree(Node node) {
        view.checkUpdate();
        AbstractNode absNode = checkNode(node);
        int count = 0;
        MetaEdgeTree inTree = view.getInMetaEdgeTree(absNode);
        if (inTree != null) {
            count = inTree.getCount();
        }
        return count;
    }

    @Override
    public int getMetaOutDegree(Node node) {
        view.checkUpdate();
        AbstractNode absNode = checkNode(node);
        int count = 0;
        MetaEdgeTree outTree = view.getOutMetaEdgeTree(absNode);
        if (outTree != null) {
            count = outTree.getCount();
        }
        return count;
    }

    @Override
    public int getMetaDegree(Node node) {
        view.checkUpdate();
        AbstractNode absNode = checkNode(node);
        int count = 0;
        MetaEdgeTree inTree = view.getInMetaEdgeTree(absNode);
        MetaEdgeTree outTree = view.getOutMetaEdgeTree(absNode);
        if (outTree != null) {
            count = outTree.getCount();
        }
        if (inTree != null) {
            count += inTree.getCount();
        }
        return count;
    }

    @Override
    public MetaEdge getMetaEdge(Node source, Node target) {
        readLock();
        view.checkUpdate();
        AbstractNode abstractNodeSource = checkNode(source);
        AbstractNode abstractNodeTarget = checkNode(target);
        MetaEdgeTree outTree = view.getOutMetaEdgeTree(abstractNodeSource);
        MetaEdge res = null;
        if (outTree != null) {
            res = outTree.getItem(abstractNodeTarget.getNumber());
        }
        readUnlock();
        return res;
    }

    @Override
    public HierarchicalDirectedGraphImplFiltered copy(Dhns dhns, GraphStructure structure, View view) {
        return new HierarchicalDirectedGraphImplFiltered(dhns, structure, view);
    }

    public boolean addEdge(Edge edge) {
        return delegate.addEdge(edge);
    }

    public boolean addEdge(Node source, Node target) {
        return delegate.addEdge(source, target);
    }

    public boolean removeEdge(Edge edge) {
        return delegate.removeEdge(edge);
    }

    public boolean contains(Edge edge) {
        return getEdge(edge.getSource(), edge.getTarget()) != null;
    }

    public boolean isDirected(Edge edge) {
        return delegate.isDirected(edge);
    }

    public boolean isAdjacent(Node node1, Node node2) {
        if (node1 == node2) {
            throw new IllegalArgumentException("Nodes can't be the same");
        }
        return isSuccessor(node1, node2) || isPredecessor(node1, node2);
    }

    public boolean isSuccessor(Node node, Node successor) {
        return getEdge(node, successor) != null;
    }

    public boolean isPredecessor(Node node, Node predecessor) {
        return getEdge(predecessor, node) != null;
    }
}
