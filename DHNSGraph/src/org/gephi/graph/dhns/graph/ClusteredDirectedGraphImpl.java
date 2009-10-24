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

import org.gephi.graph.api.ClusteredDirectedGraph;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.EdgeIterable;
import org.gephi.graph.api.MetaEdge;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.NodeIterable;
import org.gephi.graph.api.View;
import org.gephi.graph.dhns.core.Dhns;
import org.gephi.graph.dhns.core.GraphStructure;
import org.gephi.graph.dhns.edge.AbstractEdge;
import org.gephi.graph.dhns.edge.MetaEdgeImpl;
import org.gephi.graph.dhns.edge.iterators.EdgeAndMetaEdgeIterator;
import org.gephi.graph.dhns.edge.iterators.MetaEdgeContentIterator;
import org.gephi.graph.dhns.edge.iterators.EdgeIterator;
import org.gephi.graph.dhns.edge.iterators.EdgeNodeIterator;
import org.gephi.graph.dhns.edge.iterators.MetaEdgeIterator;
import org.gephi.graph.dhns.edge.iterators.MetaEdgeNodeIterator;
import org.gephi.graph.dhns.edge.iterators.RangeEdgeIterator;
import org.gephi.graph.dhns.node.AbstractNode;
import org.gephi.graph.dhns.node.iterators.NeighborIterator;
import org.gephi.graph.dhns.node.iterators.TreeIterator;

/**
 * Implementation of clustered directed graph.
 *
 * @author Mathieu Bastian
 */
public class ClusteredDirectedGraphImpl extends ClusteredGraphImpl implements ClusteredDirectedGraph {

    public ClusteredDirectedGraphImpl(Dhns dhns, GraphStructure structure, View view) {
        super(dhns, structure, view);
    }

    public boolean addEdge(Edge edge) {
        AbstractEdge absEdge = checkEdge(edge);
        if (!edge.isDirected()) {
            throw new IllegalArgumentException("Can't add an undirected egde");
        }
        if (checkEdgeExist(absEdge.getSource(), absEdge.getTarget())) {
            //Edge already exist
            return false;
        }
        if (!absEdge.hasAttributes()) {
            absEdge.setAttributes(dhns.factory().newEdgeAttributes());
        }
        dhns.getDynamicManager().pushEdge(absEdge.getEdgeData());
        dhns.getStructureModifier().addEdge(edge);
        dhns.touchDirected();
        return true;
    }

    //Directed
    public boolean addEdge(Node source, Node target) {
        AbstractNode preSource = checkNode(source);
        AbstractNode preTarget = checkNode(target);
        if (checkEdgeExist(preSource, preTarget)) {
            //Edge already exist
            return false;
        }
        AbstractEdge edge = dhns.factory().newEdge(source, target);
        dhns.getDynamicManager().pushEdge(edge.getEdgeData());
        dhns.getStructureModifier().addEdge(edge);
        dhns.touchDirected();
        return true;
    }

    //Directed
    public boolean removeEdge(Edge edge) {
        AbstractEdge absEdge = checkEdge(edge);
        AbstractEdge undirected = absEdge.getUndirected();      //Ensure that the edge with the min id is removed before his mutual with a greater id
        return dhns.getStructureModifier().deleteEdge(undirected);
    }

    //Directed
    public NodeIterable getSuccessors(Node node) {
        readLock();
        view.checkUpdate();
        AbstractNode absNode = checkNode(node);
        return dhns.newNodeIterable(new NeighborIterator(new EdgeNodeIterator(absNode, EdgeNodeIterator.EdgeNodeIteratorMode.OUT, false), absNode));
    }

    //Directed
    public NodeIterable getPredecessors(Node node) {
        readLock();
        view.checkUpdate();
        AbstractNode absNode = checkNode(node);
        return dhns.newNodeIterable(new NeighborIterator(new EdgeNodeIterator(absNode, EdgeNodeIterator.EdgeNodeIteratorMode.IN, false), absNode));

    }

    //Directed
    public boolean isSuccessor(Node node, Node successor) {
        return getEdge(node, successor) != null;
    }

    //Directed
    public boolean isPredecessor(Node node, Node predecessor) {
        return getEdge(predecessor, node) != null;
    }

    //Directed
    public int getInDegree(Node node) {
        readLock();
        view.checkUpdate();
        AbstractNode absNode = checkNode(node);
        int count = absNode.getEdgesInTree().getCount();
        readUnlock();
        return count;
    }

    //Directed
    public int getOutDegree(Node node) {
        readLock();
        view.checkUpdate();
        AbstractNode absNode = checkNode(node);
        int count = absNode.getEdgesOutTree().getCount();
        readUnlock();
        return count;
    }

    //Graph
    public boolean contains(Edge edge) {
        return getEdge(edge.getSource(), edge.getTarget()) != null;
    }

    //Graph
    public EdgeIterable getEdges() {
        view.checkUpdate();
        readLock();
        return dhns.newEdgeIterable(new EdgeIterator(structure.getStructure(), new TreeIterator(structure.getStructure(), false), false));
    }

    //Directed
    public EdgeIterable getInEdges(Node node) {
        readLock();
        view.checkUpdate();
        AbstractNode absNode = checkNode(node);
        return dhns.newEdgeIterable(new EdgeNodeIterator(absNode, EdgeNodeIterator.EdgeNodeIteratorMode.IN, false));
    }

    //Directed
    public EdgeIterable getOutEdges(Node node) {
        readLock();
        view.checkUpdate();
        AbstractNode absNode = checkNode(node);
        return dhns.newEdgeIterable(new EdgeNodeIterator(absNode, EdgeNodeIterator.EdgeNodeIteratorMode.OUT, false));
    }

    //Graph
    public EdgeIterable getEdges(Node node) {
        readLock();
        view.checkUpdate();
        AbstractNode absNode = checkNode(node);
        return dhns.newEdgeIterable(new EdgeNodeIterator(absNode, EdgeNodeIterator.EdgeNodeIteratorMode.BOTH, false));
    }

    //Graph
    public NodeIterable getNeighbors(Node node) {
        readLock();
        view.checkUpdate();
        AbstractNode absNode = checkNode(node);
        return dhns.newNodeIterable(new NeighborIterator(new EdgeNodeIterator(absNode, EdgeNodeIterator.EdgeNodeIteratorMode.BOTH, true), absNode));
    }

    //Directed
    public Edge getEdge(Node source, Node target) {
        readLock();
        view.checkUpdate();
        AbstractNode sourceNode = checkNode(source);
        AbstractNode targetNode = checkNode(target);
        AbstractEdge res = null;
        AbstractEdge edge = sourceNode.getEdgesOutTree().getItem(targetNode.getNumber());
        if (edge != null) {
            res = edge;
        }
        readUnlock();
        return res;
    }

    //Graph
    public int getEdgeCount() {
        readLock();
        view.checkUpdate();
        int count = 0;
        for (EdgeIterator itr = new EdgeIterator(structure.getStructure(), new TreeIterator(structure.getStructure(), false), false); itr.hasNext();) {
            itr.next();
            count++;
        }
        readUnlock();
        return count;
    }

    //Graph
    public int getDegree(Node node) {
        readLock();
        view.checkUpdate();
        AbstractNode absNode = checkNode(node);
        int count = absNode.getEdgesInTree().getCount() + absNode.getEdgesOutTree().getCount();
        readUnlock();
        return count;
    }

    //Graph
    public boolean isAdjacent(Node node1, Node node2) {
        if (node1 == node2) {
            throw new IllegalArgumentException("Nodes can't be the same");
        }
        return isSuccessor(node1, node2) || isPredecessor(node1, node2);
    }

    //Graph
    public boolean isDirected(Edge edge) {
        checkEdge(edge);
        return true;
    }

    //ClusteredGraph
    public EdgeIterable getInnerEdges(Node nodeGroup) {
        readLock();
        view.checkUpdate();
        AbstractNode absNode = checkNode(nodeGroup);
        return dhns.newEdgeIterable(new RangeEdgeIterator(structure.getStructure(), absNode, absNode, true, false));
    }

    //ClusteredGraph
    public EdgeIterable getOuterEdges(Node nodeGroup) {
        readLock();
        view.checkUpdate();
        AbstractNode absNode = checkNode(nodeGroup);
        return dhns.newEdgeIterable(new RangeEdgeIterator(structure.getStructure(), absNode, absNode, false, false));
    }

    //ClusteredGraph
    public EdgeIterable getMetaEdges() {
        readLock();
        view.checkUpdate();
        return dhns.newEdgeIterable(new MetaEdgeIterator(structure.getStructure(), new TreeIterator(structure.getStructure(), true), false));
    }

    public EdgeIterable getEdgesAndMetaEdges() {
        readLock();
        view.checkUpdate();
        return dhns.newEdgeIterable(new EdgeAndMetaEdgeIterator(structure.getStructure(), new TreeIterator(structure.getStructure(), false), false));
    }

    //ClusteredGraph
    public EdgeIterable getMetaEdges(Node node) {
        readLock();
        view.checkUpdate();
        AbstractNode absNode = checkNode(node);
        return dhns.newEdgeIterable(new MetaEdgeNodeIterator(absNode, MetaEdgeNodeIterator.EdgeNodeIteratorMode.BOTH, false));
    }

    //DirectedClusteredGraph
    public EdgeIterable getMetaInEdges(Node node) {
        readLock();
        view.checkUpdate();
        AbstractNode absNode = checkNode(node);
        return dhns.newEdgeIterable(new MetaEdgeNodeIterator(absNode, MetaEdgeNodeIterator.EdgeNodeIteratorMode.IN, false));
    }

    //DirectedClusteredGraph
    public EdgeIterable getMetaOutEdges(Node node) {
        readLock();
        view.checkUpdate();
        AbstractNode absNode = checkNode(node);
        return dhns.newEdgeIterable(new MetaEdgeNodeIterator(absNode, MetaEdgeNodeIterator.EdgeNodeIteratorMode.OUT, false));
    }

    //DirectedClusteredGraph
    public int getMetaInDegree(Node node) {
        readLock();
        view.checkUpdate();
        AbstractNode absNode = checkNode(node);
        int count = absNode.getMetaEdgesInTree().getCount();
        readUnlock();
        return count;
    }

    //DirectedClusteredGraph
    public int getMetaOutDegree(Node node) {
        readLock();
        view.checkUpdate();
        AbstractNode absNode = checkNode(node);
        int count = absNode.getMetaEdgesOutTree().getCount();
        readUnlock();
        return count;
    }

    //ClusteredGraph
    public int getMetaDegree(Node node) {
        readLock();
        view.checkUpdate();
        AbstractNode absNode = checkNode(node);
        int count = absNode.getMetaEdgesInTree().getCount() + absNode.getMetaEdgesOutTree().getCount();
        readUnlock();
        return count;
    }

    public EdgeIterable getMetaEdgeContent(Edge metaEdge) {
        readLock();
        view.checkUpdate();
        MetaEdgeImpl metaEdgeImpl = checkMetaEdge(metaEdge);
        return dhns.newEdgeIterable(new MetaEdgeContentIterator(metaEdgeImpl, false));
    }

    //ClusteredDirected
    public MetaEdge getMetaEdge(Node source, Node target) {
        AbstractNode AbstractNodeSource = checkNode(source);
        AbstractNode AbstractNodeTarget = checkNode(target);
        return AbstractNodeSource.getMetaEdgesOutTree().getItem(AbstractNodeTarget.getNumber());
    }

    @Override
    public ClusteredDirectedGraphImpl copy(ClusteredGraphImpl graph) {
        return new ClusteredDirectedGraphImpl(dhns, structure, view);
    }

    public ClusteredDirectedGraph getClusteredGraph() {
        return this;
    }
}
