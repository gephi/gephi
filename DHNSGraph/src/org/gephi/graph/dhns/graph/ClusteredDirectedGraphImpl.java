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
import org.gephi.graph.api.Predicate;
import org.gephi.graph.dhns.core.Dhns;
import org.gephi.graph.dhns.edge.AbstractEdge;
import org.gephi.graph.dhns.edge.MetaEdgeImpl;
import org.gephi.graph.dhns.edge.iterators.EdgeAndMetaEdgeIterator;
import org.gephi.graph.dhns.edge.iterators.MetaEdgeContentIterator;
import org.gephi.graph.dhns.edge.iterators.EdgeIterator;
import org.gephi.graph.dhns.edge.iterators.EdgeNodeIterator;
import org.gephi.graph.dhns.edge.iterators.MetaEdgeIterator;
import org.gephi.graph.dhns.edge.iterators.MetaEdgeNodeIterator;
import org.gephi.graph.dhns.edge.iterators.RangeEdgeIterator;
import org.gephi.graph.dhns.filter.ClusteredViewPredicate;
import org.gephi.graph.dhns.node.AbstractNode;
import org.gephi.graph.dhns.node.iterators.NeighborIterator;
import org.gephi.graph.dhns.node.iterators.TreeIterator;
import org.gephi.graph.dhns.proposition.Tautology;

/**
 * Implementation of clustered directed graph.
 *
 * @author Mathieu Bastian
 */
public class ClusteredDirectedGraphImpl extends ClusteredGraphImpl implements ClusteredDirectedGraph {

    protected ClusteredDirectedGraphImpl clusteredCopy;

    public ClusteredDirectedGraphImpl(Dhns dhns, boolean visible, boolean clustered) {
        super(dhns, visible, clustered);

        if (!clustered) {
            clusteredCopy = new ClusteredDirectedGraphImpl(dhns, visible, true);
        } else {
            filterControl.addPredicate(new ClusteredViewPredicate(dhns.getViewManager().getMainView()));
        }
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
        AbstractEdge edge = dhns.getGraphFactory().newEdge(source, target);
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
        AbstractNode absNode = checkNode(node);
        readLock();
        return dhns.newNodeIterable(new NeighborIterator(new EdgeNodeIterator(absNode, EdgeNodeIterator.EdgeNodeIteratorMode.OUT, false, filterControl.getEdgePredicate(), filterControl.getNodePredicate()), absNode));
    }

    //Directed
    public NodeIterable getPredecessors(Node node) {
        AbstractNode absNode = checkNode(node);
        readLock();
        return dhns.newNodeIterable(new NeighborIterator(new EdgeNodeIterator(absNode, EdgeNodeIterator.EdgeNodeIteratorMode.IN, false, filterControl.getEdgePredicate(), filterControl.getNodePredicate()), absNode));

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
        if (Tautology.isTautology(filterControl.getEdgePredicate()) && Tautology.isTautology(filterControl.getNodePredicate())) {
            count = absNode.getEdgesInTree().getCount();
        } else {
            if (!absNode.getEdgesInTree().isEmpty()) {
                Predicate<AbstractNode> nodePredicate = filterControl.getNodePredicate();
                Predicate<AbstractEdge> edgePredicate = filterControl.getEdgePredicate();
                for (Iterator<AbstractEdge> itr = absNode.getEdgesInTree().iterator(); itr.hasNext();) {
                    AbstractEdge edge = itr.next();
                    if (edgePredicate.evaluate(edge) && nodePredicate.evaluate(edge.getSource())) {
                        count++;
                    }
                }
            }
        }
        readUnlock();
        return count;
    }

    //Directed
    public int getOutDegree(Node node) {
        AbstractNode absNode = checkNode(node);
        readLock();
        int count = 0;
        if (Tautology.isTautology(filterControl.getEdgePredicate()) && Tautology.isTautology(filterControl.getNodePredicate())) {
            count = absNode.getEdgesOutTree().getCount();
        } else {
            if (!absNode.getEdgesOutTree().isEmpty()) {
                Predicate<AbstractNode> nodePredicate = filterControl.getNodePredicate();
                Predicate<AbstractEdge> edgePredicate = filterControl.getEdgePredicate();
                for (Iterator<AbstractEdge> itr = absNode.getEdgesOutTree().iterator(); itr.hasNext();) {
                    AbstractEdge edge = itr.next();
                    if (edgePredicate.evaluate(edge) && nodePredicate.evaluate(edge.getTarget())) {
                        count++;
                    }
                }
            }
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
        if (filterControl.isFiltered()) {
            return dhns.newEdgeIterable(filterControl.edgeIterator());
        }
        return dhns.newEdgeIterable(new EdgeIterator(dhns.getTreeStructure(), new TreeIterator(dhns.getTreeStructure(), filterControl.getNodePredicate()), false, filterControl.getEdgePredicate(), filterControl.getNodePredicate()));
    }

    //Directed
    public EdgeIterable getInEdges(Node node) {
        AbstractNode absNode = checkNode(node);
        readLock();
        return dhns.newEdgeIterable(new EdgeNodeIterator(absNode, EdgeNodeIterator.EdgeNodeIteratorMode.IN, false, filterControl.getEdgePredicate(), filterControl.getNodePredicate()));
    }

    //Directed
    public EdgeIterable getOutEdges(Node node) {
        AbstractNode absNode = checkNode(node);
        readLock();
        return dhns.newEdgeIterable(new EdgeNodeIterator(absNode, EdgeNodeIterator.EdgeNodeIteratorMode.OUT, false, filterControl.getEdgePredicate(), filterControl.getNodePredicate()));
    }

    //Graph
    public EdgeIterable getEdges(Node node) {
        AbstractNode absNode = checkNode(node);
        readLock();
        return dhns.newEdgeIterable(new EdgeNodeIterator(absNode, EdgeNodeIterator.EdgeNodeIteratorMode.BOTH, false, filterControl.getEdgePredicate(), filterControl.getNodePredicate()));
    }

    //Graph
    public NodeIterable getNeighbors(Node node) {
        AbstractNode absNode = checkNode(node);
        readLock();
        return dhns.newNodeIterable(new NeighborIterator(new EdgeNodeIterator(absNode, EdgeNodeIterator.EdgeNodeIteratorMode.BOTH, false, filterControl.getEdgePredicate(), filterControl.getNodePredicate()), absNode));
    }

    //Directed
    public Edge getEdge(Node source, Node target) {
        AbstractNode sourceNode = checkNode(source);
        AbstractNode targetNode = checkNode(target);
        readLock();
        AbstractEdge res = null;
        AbstractEdge edge = sourceNode.getEdgesOutTree().getItem(targetNode.getNumber());
        if (edge != null) {
            if (filterControl.evaluateEdge(edge)) {
                res = edge;
            }
        }
        readUnlock();
        return res;
    }

    //Graph
    public int getEdgeCount() {
        readLock();
        int count = 0;
        if (filterControl.isFiltered()) {
            count = filterControl.getEdgeCount();
        } else {
            for (EdgeIterator itr = new EdgeIterator(dhns.getTreeStructure(), new TreeIterator(dhns.getTreeStructure(), filterControl.getNodePredicate()), false, filterControl.getEdgePredicate(), filterControl.getNodePredicate()); itr.hasNext();) {
                itr.next();
                count++;
            }
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

    //Graph
    public boolean isDirected(Edge edge) {
        checkEdge(edge);
        return true;
    }

    //ClusteredGraph
    public EdgeIterable getInnerEdges(Node nodeGroup) {
        AbstractNode absNode = checkNode(nodeGroup);
        readLock();
        return dhns.newEdgeIterable(new RangeEdgeIterator(dhns.getTreeStructure(), absNode, absNode, true, false, filterControl.getNodePredicate(), filterControl.getEdgePredicate()));
    }

    //ClusteredGraph
    public EdgeIterable getOuterEdges(Node nodeGroup) {
        AbstractNode absNode = checkNode(nodeGroup);
        readLock();
        return dhns.newEdgeIterable(new RangeEdgeIterator(dhns.getTreeStructure(), absNode, absNode, false, false, filterControl.getNodePredicate(), filterControl.getEdgePredicate()));
    }

    //ClusteredGraph
    public EdgeIterable getMetaEdges() {
        readLock();
        if (filterControl.isFiltered()) {
            return dhns.newEdgeIterable(filterControl.metaEdgeIterator());
        }
        return dhns.newEdgeIterable(new MetaEdgeIterator(view, dhns.getTreeStructure(), new TreeIterator(dhns.getTreeStructure(), filterControl.getNodePredicate()), false, filterControl.getMetaEdgePredicate(), filterControl.getNodePredicate()));
    }

    public EdgeIterable getEdgesAndMetaEdges() {
        readLock();
        return dhns.newEdgeIterable(new EdgeAndMetaEdgeIterator(view, dhns.getTreeStructure(), new TreeIterator(dhns.getTreeStructure(), filterControl.getNodePredicate()), false, filterControl.getEdgePredicate()));
    }

    //ClusteredGraph
    public EdgeIterable getMetaEdges(Node node) {
        AbstractNode absNode = checkNode(node);
        readLock();
        return dhns.newEdgeIterable(new MetaEdgeNodeIterator(view, absNode, MetaEdgeNodeIterator.EdgeNodeIteratorMode.BOTH, false, filterControl.getMetaEdgePredicate(), filterControl.getNodePredicate()));
    }

    //DirectedClusteredGraph
    public EdgeIterable getMetaInEdges(Node node) {
        AbstractNode absNode = checkNode(node);
        readLock();
        return dhns.newEdgeIterable(new MetaEdgeNodeIterator(view, absNode, MetaEdgeNodeIterator.EdgeNodeIteratorMode.IN, false, filterControl.getMetaEdgePredicate(), filterControl.getNodePredicate()));
    }

    //DirectedClusteredGraph
    public EdgeIterable getMetaOutEdges(Node node) {
        AbstractNode absNode = checkNode(node);
        readLock();
        return dhns.newEdgeIterable(new MetaEdgeNodeIterator(view, absNode, MetaEdgeNodeIterator.EdgeNodeIteratorMode.OUT, false, filterControl.getMetaEdgePredicate(), filterControl.getNodePredicate()));
    }

    //DirectedClusteredGraph
    public int getMetaInDegree(Node node) {
        AbstractNode absNode = checkNode(node);
        readLock();
        int count = 0;
        if (Tautology.isTautology(filterControl.getMetaEdgePredicate()) && Tautology.isTautology(filterControl.getNodePredicate())) {
            count = absNode.getMetaEdgesInTree(view).getCount();
        } else {
            if (!absNode.getMetaEdgesInTree(view).isEmpty()) {
                Predicate<AbstractNode> nodePredicate = filterControl.getNodePredicate();
                Predicate<AbstractEdge> edgePredicate = filterControl.getMetaEdgePredicate();
                for (Iterator<MetaEdgeImpl> itr = absNode.getMetaEdgesInTree(view).iterator(); itr.hasNext();) {
                    AbstractEdge edge = itr.next();
                    if (edgePredicate.evaluate(edge) && nodePredicate.evaluate(edge.getSource())) {
                        count++;
                    }
                }
            }
        }
        readUnlock();
        return count;
    }

    //DirectedClusteredGraph
    public int getMetaOutDegree(Node node) {
        AbstractNode absNode = checkNode(node);
        readLock();
        int count = 0;
        if (Tautology.isTautology(filterControl.getMetaEdgePredicate()) && Tautology.isTautology(filterControl.getNodePredicate())) {
            count = absNode.getMetaEdgesOutTree(view).getCount();
        } else {
            if (!absNode.getMetaEdgesOutTree(view).isEmpty()) {
                Predicate<AbstractNode> nodePredicate = filterControl.getNodePredicate();
                Predicate<AbstractEdge> edgePredicate = filterControl.getMetaEdgePredicate();
                for (Iterator<MetaEdgeImpl> itr = absNode.getMetaEdgesOutTree(view).iterator(); itr.hasNext();) {
                    AbstractEdge edge = itr.next();
                    if (edgePredicate.evaluate(edge) && nodePredicate.evaluate(edge.getTarget())) {
                        count++;
                    }
                }
            }
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
        return dhns.newEdgeIterable(new MetaEdgeContentIterator(view, metaEdgeImpl, false, Tautology.instance));
    }

    //ClusteredDirected
    public MetaEdge getMetaEdge(Node source, Node target) {
        AbstractNode AbstractNodeSource = checkNode(source);
        AbstractNode AbstractNodeTarget = checkNode(target);
        return AbstractNodeSource.getMetaEdgesOutTree(view).getItem(AbstractNodeTarget.getNumber());
    }

    @Override
    public ClusteredDirectedGraphImpl copy(ClusteredGraphImpl graph) {
        return new ClusteredDirectedGraphImpl(dhns, false, false);
    }

    public ClusteredDirectedGraph getClusteredGraph() {
        return clusteredCopy;
    }
}
