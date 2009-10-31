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
import org.gephi.graph.api.MetaEdge;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.NodeIterable;
import org.gephi.graph.api.Predicate;
import org.gephi.graph.api.View;
import org.gephi.graph.dhns.core.Dhns;
import org.gephi.graph.dhns.core.GraphStructure;
import org.gephi.graph.dhns.edge.AbstractEdge;
import org.gephi.graph.dhns.edge.iterators.EdgeAndMetaEdgeIterator;
import org.gephi.graph.dhns.edge.iterators.EdgeNodeIterator;
import org.gephi.graph.dhns.edge.iterators.MetaEdgeIterator;
import org.gephi.graph.dhns.edge.iterators.MetaEdgeNodeIterator;
import org.gephi.graph.dhns.edge.iterators.RangeEdgeIterator;
import org.gephi.graph.dhns.filter.Tautology;
import org.gephi.graph.dhns.node.AbstractNode;
import org.gephi.graph.dhns.node.iterators.NeighborIterator;
import org.gephi.graph.dhns.node.iterators.TreeIterator;
import org.gephi.graph.dhns.views.ViewImpl;

/**
 *
 * @author Mathieu Bastian
 */
public class HierarchicalDirectedGraphImplFiltered extends HierarchicalDirectedGraphImpl {

    public HierarchicalDirectedGraphImplFiltered(Dhns dhns, GraphStructure graphStructure, View view) {
        super(dhns, graphStructure);
        this.view = (ViewImpl) view;
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
        readLock();
        AbstractNode absNode = checkNode(node);
        int count = absNode.getEdgesInTree().getCount();
        readUnlock();
        return count;
    }

    @Override
    public int getOutDegree(Node node) {
        readLock();
        AbstractNode absNode = checkNode(node);
        int count = absNode.getEdgesOutTree().getCount();
        readUnlock();
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
        readLock();
        view.checkUpdate();
        AbstractNode sourceNode = checkNode(source);
        AbstractNode targetNode = checkNode(target);
        Predicate<AbstractEdge> edgePredicate = view.getClusteredLayerEdgePredicate();
        AbstractEdge res = sourceNode.getEdgesOutTree().getItem(targetNode.getNumber());
        if (!edgePredicate.evaluate(res)) {
            res = null;
        }
        readUnlock();
        return res;
    }

    @Override
    public int getEdgeCount() {
        readLock();
        view.checkUpdate();
        int count = view.getClusteredEdgesCount();
        readUnlock();
        return count;
    }

    //Graph
    public int getDegree(Node node) {
        readLock();
        AbstractNode absNode = checkNode(node);
        int count = absNode.getEdgesInTree().getCount() + absNode.getEdgesOutTree().getCount();
        readUnlock();
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

    //ClusteredGraph
    public EdgeIterable getMetaEdges() {
        readLock();
        return dhns.newEdgeIterable(new MetaEdgeIterator(structure.getStructure(), new TreeIterator(structure.getStructure(), true, Tautology.instance), false));
    }

    public EdgeIterable getEdgesAndMetaEdges() {
        readLock();
        return dhns.newEdgeIterable(new EdgeAndMetaEdgeIterator(structure.getStructure(), new TreeIterator(structure.getStructure(), false, Tautology.instance), false, Tautology.instance, Tautology.instance));
    }

    //ClusteredGraph
    public EdgeIterable getMetaEdges(Node node) {
        readLock();
        AbstractNode absNode = checkNode(node);
        return dhns.newEdgeIterable(new MetaEdgeNodeIterator(absNode, MetaEdgeNodeIterator.EdgeNodeIteratorMode.BOTH, false));
    }

    //DirectedClusteredGraph
    public EdgeIterable getMetaInEdges(Node node) {
        readLock();
        AbstractNode absNode = checkNode(node);
        return dhns.newEdgeIterable(new MetaEdgeNodeIterator(absNode, MetaEdgeNodeIterator.EdgeNodeIteratorMode.IN, false));
    }

    //DirectedClusteredGraph
    public EdgeIterable getMetaOutEdges(Node node) {
        readLock();
        AbstractNode absNode = checkNode(node);
        return dhns.newEdgeIterable(new MetaEdgeNodeIterator(absNode, MetaEdgeNodeIterator.EdgeNodeIteratorMode.OUT, false));
    }

    //DirectedClusteredGraph
    public int getMetaInDegree(Node node) {
        readLock();
        AbstractNode absNode = checkNode(node);
        int count = absNode.getMetaEdgesInTree().getCount();
        readUnlock();
        return count;
    }

    //DirectedClusteredGraph
    public int getMetaOutDegree(Node node) {
        readLock();
        AbstractNode absNode = checkNode(node);
        int count = absNode.getMetaEdgesOutTree().getCount();
        readUnlock();
        return count;
    }

    //ClusteredGraph
    public int getMetaDegree(Node node) {
        readLock();
        AbstractNode absNode = checkNode(node);
        int count = absNode.getMetaEdgesInTree().getCount() + absNode.getMetaEdgesOutTree().getCount();
        readUnlock();
        return count;
    }

    //ClusteredDirected
    public MetaEdge getMetaEdge(Node source, Node target) {
        AbstractNode AbstractNodeSource = checkNode(source);
        AbstractNode AbstractNodeTarget = checkNode(target);
        return AbstractNodeSource.getMetaEdgesOutTree().getItem(AbstractNodeTarget.getNumber());
    }

    @Override
    public HierarchicalDirectedGraphImpl copy(Dhns dhns, GraphStructure structure, View view) {
        return new HierarchicalDirectedGraphImpl(dhns, structure);
    }
}
