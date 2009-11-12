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
import org.gephi.graph.api.HierarchicalMixedGraph;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.NodeIterable;
import org.gephi.graph.api.Predicate;
import org.gephi.graph.api.View;
import org.gephi.graph.dhns.core.Dhns;
import org.gephi.graph.dhns.core.GraphStructure;
import org.gephi.graph.dhns.edge.AbstractEdge;
import org.gephi.graph.dhns.edge.iterators.EdgeIterator;
import org.gephi.graph.dhns.edge.iterators.EdgeNodeIterator;
import org.gephi.graph.dhns.filter.Tautology;
import org.gephi.graph.dhns.node.AbstractNode;
import org.gephi.graph.dhns.node.iterators.NeighborIterator;
import org.gephi.graph.dhns.node.iterators.TreeIterator;

public class HierarchicalMixedGraphImpl extends HierarchicalGraphImpl implements HierarchicalMixedGraph {

    private Predicate<AbstractEdge> undirectedPredicate;
    private Predicate<AbstractEdge> directedPredicate;

    public HierarchicalMixedGraphImpl(Dhns dhns, GraphStructure structure) {
        super(dhns, structure);
        directedPredicate = new Predicate<AbstractEdge>() {

            public boolean evaluate(AbstractEdge t) {
                return t.isDirected();
            }
        };
        undirectedPredicate = new Predicate<AbstractEdge>() {

            public boolean evaluate(AbstractEdge t) {
                return !t.isDirected();
            }
        };
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
        AbstractEdge symmetricEdge = getSymmetricEdge(absEdge);
        if (symmetricEdge != null && (!symmetricEdge.isDirected() || !absEdge.isDirected())) {
            //The symmetric edge exist and is undirected
            return false;
        }
        if (!absEdge.hasAttributes()) {
            absEdge.setAttributes(dhns.factory().newEdgeAttributes());
        }
        dhns.getDynamicManager().pushEdge(edge.getEdgeData());
        dhns.getStructureModifier().addEdge(edge);
        if (absEdge.isDirected()) {
            dhns.touchDirected();
        } else {
            dhns.touchUndirected();
        }
        return true;
    }

    public boolean addEdge(Node source, Node target, boolean directed) {
        AbstractNode preSource = checkNode(source);
        AbstractNode preTarget = checkNode(target);
        if (checkEdgeExist(preSource, preTarget)) {
            //Edge already exist
            return false;
        }
        AbstractEdge symmetricEdge = preSource.getEdgesInTree().getItem(preTarget.getNumber());
        if (symmetricEdge != null && (!symmetricEdge.isDirected() || !directed)) {
            //The symmetric edge exist and is undirected
            return false;
        }

        AbstractEdge edge = dhns.factory().newEdge(source, target, 1.0f, directed);
        dhns.getDynamicManager().pushEdge(edge.getEdgeData());
        dhns.getStructureModifier().addEdge(edge);
        if (directed) {
            dhns.touchDirected();
        } else {
            dhns.touchUndirected();
        }
        return true;
    }

    public boolean removeEdge(Edge edge) {
        AbstractEdge absEdge = checkEdge(edge);
        AbstractEdge undirected = absEdge.getUndirected();      //Ensure that the edge with the min id is removed before his mutual with a greater id
        return dhns.getStructureModifier().deleteEdge(undirected);
    }

    public EdgeIterable getDirectedEdges() {
        readLock();
        return dhns.newEdgeIterable(new EdgeIterator(structure.getStructure(), new TreeIterator(structure.getStructure(), false, Tautology.instance), false, Tautology.instance, Tautology.instance), directedPredicate);
    }

    public EdgeIterable getUndirectedEdges() {
        readLock();
        return dhns.newEdgeIterable(new EdgeIterator(structure.getStructure(), new TreeIterator(structure.getStructure(), false, Tautology.instance), false, Tautology.instance, Tautology.instance), undirectedPredicate);
    }

    public boolean isDirected(Edge edge) {
        AbstractEdge absEdge = checkEdge(edge);
        return absEdge.isDirected();
    }

    public boolean contains(Edge edge) {
        return getEdge(edge.getSource(), edge.getTarget()) != null;
    }

    public Edge getEdge(Node node1, Node node2) {
        readLock();
        AbstractNode sourceNode = checkNode(node1);
        AbstractNode targetNode = checkNode(node2);
        AbstractEdge res = sourceNode.getEdgesOutTree().getItem(targetNode.getNumber());
        if (res == null) {
            res = sourceNode.getEdgesInTree().getItem(targetNode.getNumber());
        }
        readUnlock();
        return res;
    }

    public EdgeIterable getEdges() {
        readLock();
        return dhns.newEdgeIterable(new EdgeIterator(structure.getStructure(), new TreeIterator(structure.getStructure(), false, Tautology.instance), false, Tautology.instance, Tautology.instance));
    }

    public NodeIterable getNeighbors(Node node) {
        readLock();
        AbstractNode absNode = checkNode(node);
        return dhns.newNodeIterable(new NeighborIterator(new EdgeNodeIterator(absNode, EdgeNodeIterator.EdgeNodeIteratorMode.BOTH, true, Tautology.instance, Tautology.instance), absNode, Tautology.instance));
    }

    public EdgeIterable getEdges(Node node) {
        readLock();
        AbstractNode absNode = checkNode(node);
        return dhns.newEdgeIterable(new EdgeNodeIterator(absNode, EdgeNodeIterator.EdgeNodeIteratorMode.BOTH, false, Tautology.instance, Tautology.instance));
    }

    public int getEdgeCount() {
        readLock();
        int count = 0;
        for (EdgeIterator itr = new EdgeIterator(structure.getStructure(), new TreeIterator(structure.getStructure(), false, Tautology.instance), false, Tautology.instance, Tautology.instance); itr.hasNext();) {
            itr.next();
            count++;
        }
        readUnlock();
        return count;
    }

    public int getDegree(Node node) {
        readLock();
        AbstractNode absNode = checkNode(node);
        int count = absNode.getEdgesInTree().getCount() + absNode.getEdgesOutTree().getCount();
        readUnlock();
        return count;
    }

    //Directed
    public int getInDegree(Node node) {
        readLock();
        AbstractNode absNode = checkNode(node);
        int count = absNode.getEdgesInTree().getCount();
        readUnlock();
        return count;
    }

    //Directed
    public int getOutDegree(Node node) {
        readLock();
        AbstractNode absNode = checkNode(node);
        int count = 0;
        count = absNode.getEdgesOutTree().getCount();
        readUnlock();
        return count;
    }

    public boolean isAdjacent(Node node1, Node node2) {
        if (node1 == node2) {
            throw new IllegalArgumentException("Nodes can't be the same");
        }
        return isSuccessor(node1, node2) || isPredecessor(node1, node2);
    }

    //Directed
    public boolean isSuccessor(Node node, Node successor) {
        return getEdge(node, successor) != null;
    }

    //Directed
    public boolean isPredecessor(Node node, Node predecessor) {
        return getEdge(predecessor, node) != null;
    }

    public EdgeIterable getInnerEdges(Node nodeGroup) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public EdgeIterable getOuterEdges(Node nodeGroup) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public EdgeIterable getMetaEdges() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public EdgeIterable getEdgesAndMetaEdges() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public EdgeIterable getMetaEdges(Node node) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int getMetaDegree(Node node) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public HierarchicalMixedGraphImpl copy(Dhns dhns, GraphStructure structure, View view) {
        return new HierarchicalMixedGraphImpl(dhns, structure);

    }
}
