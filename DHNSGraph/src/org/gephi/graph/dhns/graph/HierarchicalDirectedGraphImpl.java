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

import org.gephi.datastructure.avl.param.ParamAVLIterator;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.EdgeIterable;
import org.gephi.graph.api.HierarchicalDirectedGraph;
import org.gephi.graph.api.MetaEdge;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.NodeIterable;
import org.gephi.graph.dhns.core.Dhns;
import org.gephi.graph.dhns.core.GraphViewImpl;
import org.gephi.graph.dhns.edge.AbstractEdge;
import org.gephi.graph.dhns.edge.iterators.EdgeAndMetaEdgeIterator;
import org.gephi.graph.dhns.edge.iterators.EdgeIterator;
import org.gephi.graph.dhns.edge.iterators.EdgeNodeIterator;

import org.gephi.graph.dhns.edge.iterators.MetaEdgeIterator;
import org.gephi.graph.dhns.edge.iterators.MetaEdgeNodeIterator;
import org.gephi.graph.dhns.edge.iterators.RangeEdgeIterator;
import org.gephi.graph.dhns.filter.Tautology;
import org.gephi.graph.dhns.node.AbstractNode;
import org.gephi.graph.dhns.node.iterators.NeighborIterator;
import org.gephi.graph.dhns.node.iterators.TreeIterator;
import org.gephi.graph.dhns.utils.avl.EdgeOppositeTree;

/**
 *
 * @author Mathieu Bastian
 */
public class HierarchicalDirectedGraphImpl extends HierarchicalGraphImpl implements HierarchicalDirectedGraph {

    public HierarchicalDirectedGraphImpl(Dhns dhns, GraphViewImpl view) {
        super(dhns, view);
    }

    //Graph
    public boolean addEdge(Edge edge) {
        AbstractEdge absEdge = checkEdge(edge);
        if (!edge.isDirected()) {
            throw new IllegalArgumentException("Can't add an undirected egde");
        }
        if (checkEdgeExist(absEdge.getSource(view.getViewId()), absEdge.getTarget(view.getViewId()))) {
            //Edge already exist
            return false;
        }
        if (!absEdge.hasAttributes()) {
            absEdge.setAttributes(dhns.factory().newEdgeAttributes());
        }
        dhns.getDynamicManager().pushEdge(absEdge.getEdgeData());
        view.getStructureModifier().addEdge(absEdge);
        dhns.touchDirected();
        return true;
    }

    //Directed
    public boolean addEdge(Node source, Node target) {
        AbstractNode absSource = checkNode(source);
        AbstractNode absTarget = checkNode(target);
        if (checkEdgeExist(absSource, absTarget)) {
            //Edge already exist
            return false;
        }
        AbstractEdge edge = dhns.factory().newEdge(source, target);
        dhns.getDynamicManager().pushEdge(edge.getEdgeData());
        view.getStructureModifier().addEdge(edge);
        dhns.touchDirected();
        return true;
    }

    //Directed
    public boolean removeEdge(Edge edge) {
        AbstractEdge absEdge = checkEdge(edge);
        AbstractEdge undirected = absEdge.getUndirected(view.getViewId());      //Ensure that the edge with the min id is removed before his mutual with a greater id
        return view.getStructureModifier().deleteEdge(undirected);
    }

    //Directed
    public NodeIterable getSuccessors(Node node) {
        readLock();
        AbstractNode absNode = checkNode(node);
        return dhns.newNodeIterable(new NeighborIterator(new EdgeNodeIterator(absNode, EdgeNodeIterator.EdgeNodeIteratorMode.OUT, false, Tautology.instance, Tautology.instance), absNode, Tautology.instance));
    }

    //Directed
    public NodeIterable getPredecessors(Node node) {
        readLock();
        AbstractNode absNode = checkNode(node);
        return dhns.newNodeIterable(new NeighborIterator(new EdgeNodeIterator(absNode, EdgeNodeIterator.EdgeNodeIteratorMode.IN, false, Tautology.instance, Tautology.instance), absNode, Tautology.instance));

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
        AbstractNode absNode = checkNode(node);
        int count = absNode.getEdgesInTree().getCount();
        return count;
    }

    //Directed
    public int getOutDegree(Node node) {
        AbstractNode absNode = checkNode(node);
        int count = absNode.getEdgesOutTree().getCount();
        return count;
    }

    public int getMutualDegree(Node node) {
        AbstractNode absNode = checkNode(node);
        int count = 0;
        if (absNode.getEdgesOutTree().getCount() > 0 && absNode.getEdgesInTree().getCount() > 0) {
            EdgeOppositeTree inTree = absNode.getEdgesInTree();
            for (ParamAVLIterator<AbstractEdge> itr = new ParamAVLIterator<AbstractEdge>(); itr.hasNext();) {
                AbstractEdge edge = itr.next();
                if (!edge.isSelfLoop()) {
                    if (inTree.hasNeighbour(edge.getTarget())) {
                        count++;
                    }
                }
            }
        }

        return count;
    }

    //Graph
    public boolean contains(Edge edge) {
        if (edge == null) {
            throw new NullPointerException();
        }
        AbstractEdge absEdge = (AbstractEdge) edge;
        return getEdge(absEdge.getSource(view.getViewId()), absEdge.getTarget(view.getViewId())) != null;
    }

    //Graph
    public EdgeIterable getEdges() {
        readLock();
        return dhns.newEdgeIterable(new EdgeIterator(structure, new TreeIterator(structure, false, Tautology.instance), false, Tautology.instance, Tautology.instance));
    }

    //Directed
    public EdgeIterable getInEdges(Node node) {
        readLock();
        AbstractNode absNode = checkNode(node);
        return dhns.newEdgeIterable(new EdgeNodeIterator(absNode, EdgeNodeIterator.EdgeNodeIteratorMode.IN, false, Tautology.instance, Tautology.instance));
    }

    //Directed
    public EdgeIterable getOutEdges(Node node) {
        readLock();
        AbstractNode absNode = checkNode(node);
        return dhns.newEdgeIterable(new EdgeNodeIterator(absNode, EdgeNodeIterator.EdgeNodeIteratorMode.OUT, false, Tautology.instance, Tautology.instance));
    }

    //Graph
    public EdgeIterable getEdges(Node node) {
        readLock();
        AbstractNode absNode = checkNode(node);
        return dhns.newEdgeIterable(new EdgeNodeIterator(absNode, EdgeNodeIterator.EdgeNodeIteratorMode.BOTH, false, Tautology.instance, Tautology.instance));
    }

    //Graph
    public NodeIterable getNeighbors(Node node) {
        readLock();
        AbstractNode absNode = checkNode(node);
        return dhns.newNodeIterable(new NeighborIterator(new EdgeNodeIterator(absNode, EdgeNodeIterator.EdgeNodeIteratorMode.BOTH, true, Tautology.instance, Tautology.instance), absNode, Tautology.instance));
    }

    //Directed
    public Edge getEdge(Node source, Node target) {
        if (source == null || target == null) {
            return null;
        }
        AbstractNode sourceNode = checkNode(source);
        AbstractNode targetNode = checkNode(target);
        AbstractEdge res = sourceNode.getEdgesOutTree().getItem(targetNode.getNumber());
        return res;
    }

    //Graph
    public int getEdgeCount() {
        int count = 0;
        for (EdgeIterator itr = new EdgeIterator(structure, new TreeIterator(structure, false, Tautology.instance), false, Tautology.instance, Tautology.instance); itr.hasNext();) {
            itr.next();
            count++;
        }
        return count;
    }

    //Graph
    public int getDegree(Node node) {
        AbstractNode absNode = checkNode(node);
        int count = absNode.getEdgesInTree().getCount() + absNode.getEdgesOutTree().getCount();
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
        checkEdgeOrMetaEdge(edge);
        return true;
    }

    //ClusteredGraph
    public EdgeIterable getInnerEdges(Node nodeGroup) {
        readLock();
        AbstractNode absNode = checkNode(nodeGroup);
        return dhns.newEdgeIterable(new RangeEdgeIterator(structure, absNode, absNode, true, false, Tautology.instance, Tautology.instance));
    }

    //ClusteredGraph
    public EdgeIterable getOuterEdges(Node nodeGroup) {
        readLock();
        AbstractNode absNode = checkNode(nodeGroup);
        return dhns.newEdgeIterable(new RangeEdgeIterator(structure, absNode, absNode, false, false, Tautology.instance, Tautology.instance));
    }

    //ClusteredGraph
    public EdgeIterable getMetaEdges() {
        readLock();
        return dhns.newEdgeIterable(new MetaEdgeIterator(structure, new TreeIterator(structure, true, Tautology.instance), false));
    }

    public EdgeIterable getEdgesAndMetaEdges() {
        readLock();
        return dhns.newEdgeIterable(new EdgeAndMetaEdgeIterator(structure, new TreeIterator(structure, false, Tautology.instance), false, Tautology.instance, Tautology.instance));
    }

    //ClusteredGraph
    public EdgeIterable getMetaEdges(Node node) {
        readLock();
        AbstractNode absNode = checkNode(node);
        return dhns.newEdgeIterable(new MetaEdgeNodeIterator(absNode.getMetaEdgesOutTree(), absNode.getMetaEdgesInTree(), MetaEdgeNodeIterator.EdgeNodeIteratorMode.BOTH, false));
    }

    //DirectedClusteredGraph
    public EdgeIterable getMetaInEdges(Node node) {
        readLock();
        AbstractNode absNode = checkNode(node);
        return dhns.newEdgeIterable(new MetaEdgeNodeIterator(null, absNode.getMetaEdgesInTree(), MetaEdgeNodeIterator.EdgeNodeIteratorMode.IN, false));
    }

    //DirectedClusteredGraph
    public EdgeIterable getMetaOutEdges(Node node) {
        readLock();
        AbstractNode absNode = checkNode(node);
        return dhns.newEdgeIterable(new MetaEdgeNodeIterator(absNode.getMetaEdgesOutTree(), null, MetaEdgeNodeIterator.EdgeNodeIteratorMode.OUT, false));
    }

    //DirectedClusteredGraph
    public int getMetaInDegree(Node node) {
        AbstractNode absNode = checkNode(node);
        int count = absNode.getMetaEdgesInTree().getCount();
        return count;
    }

    //DirectedClusteredGraph
    public int getMetaOutDegree(Node node) {
        AbstractNode absNode = checkNode(node);
        int count = absNode.getMetaEdgesOutTree().getCount();
        return count;
    }

    //ClusteredGraph
    public int getMetaDegree(Node node) {
        AbstractNode absNode = checkNode(node);
        int count = absNode.getMetaEdgesInTree().getCount() + absNode.getMetaEdgesOutTree().getCount();
        return count;
    }

    //ClusteredDirected
    public MetaEdge getMetaEdge(Node source, Node target) {
        AbstractNode AbstractNodeSource = checkNode(source);
        AbstractNode AbstractNodeTarget = checkNode(target);
        return AbstractNodeSource.getMetaEdgesOutTree().getItem(AbstractNodeTarget.getNumber());
    }

    @Override
    public HierarchicalDirectedGraphImpl copy(Dhns dhns, GraphViewImpl view) {
        return new HierarchicalDirectedGraphImpl(dhns, view);
    }

    public EdgeIterable getHierarchyEdges() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
