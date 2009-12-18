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
import org.gephi.graph.api.HierarchicalUndirectedGraph;
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
public class HierarchicalUndirectedGraphImplFiltered extends HierarchicalGraphImplFiltered implements HierarchicalUndirectedGraph {

    private HierarchicalUndirectedGraphImpl delegate;

    public HierarchicalUndirectedGraphImplFiltered(Dhns dhns, GraphStructure graphStructure, View view) {
        super(dhns, graphStructure, view);
        this.delegate = new HierarchicalUndirectedGraphImpl(dhns, structure);
    }

    public boolean addEdge(Edge edge) {
        return delegate.addEdge(edge);
    }

    public boolean addEdge(Node node1, Node node2) {
        return delegate.addEdge(node1, node2);
    }

    public boolean removeEdge(Edge edge) {
        return delegate.removeEdge(edge);
    }

    public boolean contains(Edge edge) {
        return getEdge(edge.getSource(), edge.getTarget()) != null;
    }

    public EdgeIterable getEdges() {
        readLock();
        view.checkUpdate();
        return dhns.newEdgeIterable(view.getClusteredLayerEdgeIterator());
    }

    public EdgeIterable getEdges(Node node) {
        readLock();
        view.checkUpdate();
        AbstractNode absNode = checkNode(node);
        return dhns.newEdgeIterable(new EdgeNodeIterator(absNode, EdgeNodeIterator.EdgeNodeIteratorMode.BOTH, true, view.getClusteredLayerNodePredicate(), view.getClusteredLayerEdgePredicate()));
    }

    public NodeIterable getNeighbors(Node node) {
        readLock();
        view.checkUpdate();
        AbstractNode absNode = checkNode(node);
        Predicate<AbstractNode> nodePredicate = view.getClusteredLayerNodePredicate();
        Predicate<AbstractEdge> edgePredicate = view.getClusteredLayerEdgePredicate();
        return dhns.newNodeIterable(new NeighborIterator(new EdgeNodeIterator(absNode, EdgeNodeIterator.EdgeNodeIteratorMode.BOTH, true, nodePredicate, edgePredicate), absNode, nodePredicate));
    }

    public int getEdgeCount() {
        view.checkUpdate();
        int count = view.getClusteredEdgesCount();
        return count;
    }

    public int getDegree(Node node) {
        view.checkUpdate();
        AbstractNode absNode = checkNode(node);
        int count = view.getClusteredLayerDegree(absNode);
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
        return delegate.isDirected(edge);
    }

    public Edge getEdge(Node node1, Node node2) {
        view.checkUpdate();
        AbstractNode sourceNode = checkNode(node1);
        AbstractNode targetNode = checkNode(node2);
        Predicate<AbstractEdge> edgePredicate = view.getClusteredLayerEdgePredicate();
        AbstractEdge res = sourceNode.getEdgesOutTree().getItem(targetNode.getNumber());
        if (res != null && edgePredicate.evaluate(res)) {
            return res;
        }
        res = sourceNode.getEdgesInTree().getItem(targetNode.getNumber());
        if (res != null && edgePredicate.evaluate(res)) {
            return res;
        }
        return null;
    }

    @Override
    public EdgeIterable getInnerEdges(Node nodeGroup) {
        readLock();
        view.checkUpdate();
        AbstractNode absNode = checkNode(nodeGroup);
        return dhns.newEdgeIterable(new RangeEdgeIterator(structure.getStructure(), absNode, absNode, true, true, view.getHierarchyLayerNodePredicate(), view.getHierarchyLayerEdgePredicate()));
    }

    @Override
    public EdgeIterable getOuterEdges(Node nodeGroup) {
        readLock();
        view.checkUpdate();
        AbstractNode absNode = checkNode(nodeGroup);
        return dhns.newEdgeIterable(new RangeEdgeIterator(structure.getStructure(), absNode, absNode, false, true, view.getHierarchyLayerNodePredicate(), view.getHierarchyLayerEdgePredicate()));
    }

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
        return dhns.newEdgeIterable(new MetaEdgeNodeIterator(outTree, inTree, MetaEdgeNodeIterator.EdgeNodeIteratorMode.BOTH, true));
    }

    @Override
    public HierarchicalUndirectedGraphImpl copy(Dhns dhns, GraphStructure structure, View view) {
        return new HierarchicalUndirectedGraphImpl(dhns, structure);
    }
}
