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

import java.util.Iterator;
import org.gephi.graph.api.ClusteredDirectedGraph;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.EdgeIterable;
import org.gephi.graph.api.MetaEdge;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.NodeIterable;
import org.gephi.graph.dhns.core.Dhns;
import org.gephi.graph.dhns.edge.AbstractEdge;
import org.gephi.graph.dhns.edge.MetaEdgeImpl;
import org.gephi.graph.dhns.edge.iterators.MetaEdgeContentIterator;
import org.gephi.graph.dhns.edge.iterators.EdgeIterator;
import org.gephi.graph.dhns.edge.iterators.EdgeNodeIterator;
import org.gephi.graph.dhns.edge.iterators.MetaEdgeIterator;
import org.gephi.graph.dhns.edge.iterators.MetaEdgeNodeIterator;
import org.gephi.graph.dhns.edge.iterators.RangeEdgeIterator;
import org.gephi.graph.dhns.node.AbstractNode;
import org.gephi.graph.dhns.node.iterators.NeighborIterator;
import org.gephi.graph.dhns.node.iterators.TreeIterator;
import org.gephi.graph.dhns.node.iterators.TreeViewIterator;

/**
 * Implementation of clustered directed graph.
 *
 * @author Mathieu Bastian
 */
public class ClusteredDirectedGraphImpl extends ClusteredGraphImpl implements ClusteredDirectedGraph {

    public ClusteredDirectedGraphImpl(Dhns dhns, boolean visible, boolean clustered) {
        super(dhns, visible, clustered);
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
            absEdge.setAttributes(dhns.getGraphFactory().newEdgeAttributes());
        }
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
        AbstractEdge edge = dhns.getGraphFactory().newEdge(source, target);
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
        AbstractNode absNode = checkNode(node);
        readLock();
        return dhns.newNodeIterable(new NeighborIterator(new EdgeNodeIterator(absNode, EdgeNodeIterator.EdgeNodeIteratorMode.OUT, false, edgeProposition), absNode, nodeProposition));
    }

    //Directed
    public NodeIterable getPredecessors(Node node) {
        AbstractNode absNode = checkNode(node);
        readLock();
        return dhns.newNodeIterable(new NeighborIterator(new EdgeNodeIterator(absNode, EdgeNodeIterator.EdgeNodeIteratorMode.IN, false, edgeProposition), absNode, nodeProposition));
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
        readLock();
        int count = 0;
        if (!edgeProposition.isTautology() && !absNode.getEdgesInTree().isEmpty()) {
            for (Iterator<AbstractEdge> itr = absNode.getEdgesInTree().iterator(); itr.hasNext();) {
                if (edgeProposition.evaluate(itr.next())) {
                    count++;
                }
            }
        } else {
            count = absNode.getEdgesInTree().getCount();
        }
        readUnlock();
        return count;
    }

    //Directed
    public int getOutDegree(Node node) {
        AbstractNode absNode = checkNode(node);
        readLock();
        int count = 0;
        if (!edgeProposition.isTautology() && !absNode.getEdgesInTree().isEmpty()) {
            for (Iterator<AbstractEdge> itr = absNode.getEdgesOutTree().iterator(); itr.hasNext();) {
                if (edgeProposition.evaluate(itr.next())) {
                    count++;
                }
            }
        } else {
            count = absNode.getEdgesOutTree().getCount();
        }
        readUnlock();
        return count;
    }

    //Graph
    public boolean contains(Edge edge) {
        return getEdge(edge.getSource(), edge.getTarget()) != null;
    }

    //Graph
    public EdgeIterable getEdges() {
        readLock();
        return dhns.newEdgeIterable(new EdgeIterator(dhns.getTreeStructure(), new TreeIterator(dhns.getTreeStructure(), nodeProposition), false, edgeProposition));
    }

    //Clustered
    public EdgeIterable getEdgesInView() {
        readLock();
        return dhns.newEdgeIterable(new EdgeIterator(dhns.getTreeStructure(), new TreeViewIterator(dhns.getTreeStructure(), nodeEnabledProposition), false, edgeEnabledProposition));
    }

    //Directed
    public EdgeIterable getInEdges(Node node) {
        AbstractNode absNode = checkNode(node);
        readLock();
        return dhns.newEdgeIterable(new EdgeNodeIterator(absNode, EdgeNodeIterator.EdgeNodeIteratorMode.IN, false, edgeProposition));
    }

    //Directed
    public EdgeIterable getOutEdges(Node node) {
        AbstractNode absNode = checkNode(node);
        readLock();
        return dhns.newEdgeIterable(new EdgeNodeIterator(absNode, EdgeNodeIterator.EdgeNodeIteratorMode.OUT, false, edgeProposition));
    }

    //Graph
    public EdgeIterable getEdges(Node node) {
        AbstractNode absNode = checkNode(node);
        readLock();
        return dhns.newEdgeIterable(new EdgeNodeIterator(absNode, EdgeNodeIterator.EdgeNodeIteratorMode.BOTH, false, edgeProposition));
    }

    //Graph
    public NodeIterable getNeighbors(Node node) {
        AbstractNode absNode = checkNode(node);
        readLock();
        return dhns.newNodeIterable(new NeighborIterator(new EdgeNodeIterator(absNode, EdgeNodeIterator.EdgeNodeIteratorMode.BOTH, true, edgeProposition), absNode, nodeProposition));
    }

    //Directed
    public Edge getEdge(Node source, Node target) {
        AbstractNode sourceNode = checkNode(source);
        AbstractNode targetNode = checkNode(target);
        readLock();
        AbstractEdge res = null;
        AbstractEdge edge = sourceNode.getEdgesOutTree().getItem(targetNode.getNumber());
        if (edge != null && edgeProposition.evaluate(edge)) {
            res = edge;
        }
        readUnlock();
        return res;
    }

    //Graph
    public int getEdgeCount() {
        readLock();
        int count = 0;
        for (EdgeIterator itr = new EdgeIterator(dhns.getTreeStructure(), new TreeIterator(dhns.getTreeStructure(), nodeProposition), false, edgeProposition); itr.hasNext();) {
            itr.next();
            count++;
        }
        readUnlock();
        return count;
    }

    //Graph
    public int getDegree(Node node) {
        return getInDegree(node) + getOutDegree(node);
    }

    //Graph
    public boolean isAdjacent(Node node1, Node node2) {
        if (node1 == node2) {
            throw new IllegalArgumentException("Nodes can't be the same");
        }
        return isSuccessor(node1, node2) || isPredecessor(node1, node2);
    }

    //ClusteredGraph
    public EdgeIterable getInnerEdges(Node nodeGroup) {
        AbstractNode absNode = checkNode(nodeGroup);
        readLock();
        return dhns.newEdgeIterable(new RangeEdgeIterator(dhns.getTreeStructure(), absNode, absNode, true, false, nodeProposition, edgeProposition));
    }

    //ClusteredGraph
    public EdgeIterable getOuterEdges(Node nodeGroup) {
        AbstractNode absNode = checkNode(nodeGroup);
        readLock();
        return dhns.newEdgeIterable(new RangeEdgeIterator(dhns.getTreeStructure(), absNode, absNode, false, false, nodeProposition, edgeProposition));
    }

    //ClusteredGraph
    public EdgeIterable getMetaEdges() {
        readLock();
        return dhns.newEdgeIterable(new MetaEdgeIterator(view, dhns.getTreeStructure(), new TreeViewIterator(dhns.getTreeStructure(), nodeProposition), false, edgeProposition));
    }

    //ClusteredGraph
    public EdgeIterable getMetaEdges(Node node) {
        AbstractNode absNode = checkNode(node);
        readLock();
        return dhns.newEdgeIterable(new MetaEdgeNodeIterator(view, absNode, MetaEdgeNodeIterator.EdgeNodeIteratorMode.BOTH, false, edgeProposition));
    }

    //DirectedClusteredGraph
    public EdgeIterable getMetaInEdges(Node node) {
        AbstractNode absNode = checkNode(node);
        readLock();
        return dhns.newEdgeIterable(new MetaEdgeNodeIterator(view, absNode, MetaEdgeNodeIterator.EdgeNodeIteratorMode.IN, false, edgeProposition));
    }

    //DirectedClusteredGraph
    public EdgeIterable getMetaOutEdges(Node node) {
        AbstractNode absNode = checkNode(node);
        readLock();
        return dhns.newEdgeIterable(new MetaEdgeNodeIterator(view, absNode, MetaEdgeNodeIterator.EdgeNodeIteratorMode.OUT, false, edgeProposition));
    }

    //DirectedClusteredGraph
    public int getMetaInDegree(Node node) {
        AbstractNode absNode = checkNode(node);
        readLock();
        int count = 0;
        if (!edgeProposition.isTautology() && !absNode.getMetaEdgesInTree(view).isEmpty()) {
            for (Iterator<MetaEdgeImpl> itr = absNode.getMetaEdgesInTree(view).iterator(); itr.hasNext();) {
                if (edgeProposition.evaluate(itr.next())) {
                    count++;
                }
            }
        } else {
            count = absNode.getMetaEdgesInTree(view).getCount();
        }
        readUnlock();
        return count;
    }

    //DirectedClusteredGraph
    public int getMetaOutDegree(Node node) {
        AbstractNode absNode = checkNode(node);
        readLock();
        int count = 0;
        if (!edgeProposition.isTautology() && !absNode.getMetaEdgesOutTree(view).isEmpty()) {
            for (Iterator<MetaEdgeImpl> itr = absNode.getMetaEdgesOutTree(view).iterator(); itr.hasNext();) {
                if (edgeProposition.evaluate(itr.next())) {
                    count++;
                }
            }
        } else {
            count = absNode.getMetaEdgesOutTree(view).getCount();
        }
        readUnlock();
        return count;
    }

    //ClusteredGraph
    public int getMetaDegree(Node node) {
        return getMetaInDegree(node) + getMetaOutDegree(node);
    }

    public EdgeIterable getMetaEdgeContent(Edge metaEdge) {
        MetaEdgeImpl metaEdgeImpl = checkMetaEdge(metaEdge);
        readLock();
        return dhns.newEdgeIterable(new MetaEdgeContentIterator(view, metaEdgeImpl, false, edgeProposition));
    }

    //ClusteredDirected
    public MetaEdge getMetaEdge(Node source, Node target) {
        AbstractNode AbstractNodeSource = checkNode(source);
        AbstractNode AbstractNodeTarget = checkNode(target);
        return AbstractNodeSource.getMetaEdgesOutTree(view).getItem(AbstractNodeTarget.getNumber());
    }
}
