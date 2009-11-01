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
import org.gephi.graph.api.View;
import org.gephi.graph.dhns.core.Dhns;
import org.gephi.graph.dhns.core.GraphStructure;
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

/**
 *
 * @author Mathieu Bastian
 */
public class HierarchicalUndirectedGraphImpl extends HierarchicalGraphImpl implements HierarchicalUndirectedGraph {

    public HierarchicalUndirectedGraphImpl(Dhns dhns, GraphStructure structure) {
        super(dhns, structure);
    }

    public boolean addEdge(Edge edge) {
        AbstractEdge absEdge = checkEdge(edge);
        if (edge.isDirected() && !absEdge.isSelfLoop()) {
            throw new IllegalArgumentException("Can't add a directed egde");
        }
        if (checkEdgeExist(absEdge.getSource(), absEdge.getTarget()) || checkEdgeExist(absEdge.getTarget(), absEdge.getSource())) {
            //Edge already exist
            return false;
        }
        if (!absEdge.hasAttributes()) {
            absEdge.setAttributes(dhns.factory().newEdgeAttributes());
        }
        dhns.getDynamicManager().pushEdge(edge.getEdgeData());
        dhns.getStructureModifier().addEdge(edge);
        dhns.touchUndirected();
        return true;
    }

    public boolean addEdge(Node node1, Node node2) {
        AbstractNode AbstractNode1 = checkNode(node1);
        AbstractNode AbstractNode2 = checkNode(node2);
        if (checkEdgeExist(AbstractNode1, AbstractNode2) || checkEdgeExist(AbstractNode2, AbstractNode1)) {
            //Edge already exist
            return false;
        }
        AbstractEdge edge = dhns.factory().newEdge(node1, node2, 1.0f, false);
        dhns.getDynamicManager().pushEdge(edge.getEdgeData());
        dhns.getStructureModifier().addEdge(edge);
        dhns.touchUndirected();
        return true;
    }

    public boolean removeEdge(Edge edge) {
        checkEdge(edge);
        AbstractEdge absEdge = (AbstractEdge) edge;
        boolean res = false;
        if (!absEdge.isSelfLoop()) {
            //Remove also mutual edge if present
            AbstractEdge symmetricEdge = getSymmetricEdge(absEdge);
            if (symmetricEdge != null) {
                res = dhns.getStructureModifier().deleteEdge(symmetricEdge);
            }
        }
        res = dhns.getStructureModifier().deleteEdge(edge) || res;
        return res;
    }

    public boolean contains(Edge edge) {
        return getEdge(edge.getSource(), edge.getTarget()) != null;
    }

    public EdgeIterable getEdges() {
        readLock();
        return dhns.newEdgeIterable(new EdgeIterator(structure.getStructure(), new TreeIterator(structure.getStructure(), false, Tautology.instance), true, Tautology.instance, Tautology.instance));
    }

    public EdgeIterable getEdges(Node node) {
        readLock();
        AbstractNode absNode = checkNode(node);
        return dhns.newEdgeIterable(new EdgeNodeIterator(absNode, EdgeNodeIterator.EdgeNodeIteratorMode.BOTH, true, Tautology.instance, Tautology.instance));
    }

    public NodeIterable getNeighbors(Node node) {
        readLock();
        AbstractNode absNode = checkNode(node);
        return dhns.newNodeIterable(new NeighborIterator(new EdgeNodeIterator(absNode, EdgeNodeIterator.EdgeNodeIteratorMode.BOTH, true, Tautology.instance, Tautology.instance), absNode, Tautology.instance));
    }

    public int getEdgeCount() {
        readLock();
        int count = 0;
        for (EdgeIterator itr = new EdgeIterator(structure.getStructure(), new TreeIterator(structure.getStructure(), false, Tautology.instance), true, Tautology.instance, Tautology.instance); itr.hasNext();) {
            itr.next();
            count++;
        }
        readUnlock();
        return count;
    }

    public int getDegree(Node node) {
        readLock();
        AbstractNode absNode = checkNode(node);
        int count = 0;
        EdgeNodeIterator itr = new EdgeNodeIterator(absNode, EdgeNodeIterator.EdgeNodeIteratorMode.BOTH, true, Tautology.instance, Tautology.instance);
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

    public boolean isAdjacent(Node node1, Node node2) {
        if (node1 == node2) {
            throw new IllegalArgumentException("Nodes can't be the same");
        }
        return getEdge(node1, node2) != null;
    }

    //Graph
    public boolean isDirected(Edge edge) {
        checkEdge(edge);
        return false;
    }

    public Edge getEdge(Node node1, Node node2) {
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

    @Override
    public void setVisible(Edge edge, boolean visible) {
        AbstractEdge absEdge = checkEdge(edge);
        writeLock();
        absEdge.setVisible(visible);
        AbstractEdge mutualEdge = getSymmetricEdge(absEdge);
        if (mutualEdge != null) {
            mutualEdge.setVisible(visible);
        }
        writeUnlock();

    }

    public EdgeIterable getInnerEdges(Node nodeGroup) {
        readLock();
        AbstractNode absNode = checkNode(nodeGroup);
        return dhns.newEdgeIterable(new RangeEdgeIterator(structure.getStructure(), absNode, absNode, true, true, Tautology.instance, Tautology.instance));
    }

    public EdgeIterable getOuterEdges(Node nodeGroup) {
        readLock();
        AbstractNode absNode = checkNode(nodeGroup);
        return dhns.newEdgeIterable(new RangeEdgeIterator(structure.getStructure(), absNode, absNode, false, true, Tautology.instance, Tautology.instance));
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
        return dhns.newEdgeIterable(new MetaEdgeIterator(structure.getStructure(), new TreeIterator(structure.getStructure(), true, Tautology.instance), true));
    }

    public EdgeIterable getEdgesAndMetaEdges() {
        readLock();
        return dhns.newEdgeIterable(new EdgeAndMetaEdgeIterator(structure.getStructure(), new TreeIterator(structure.getStructure(), false, Tautology.instance), true, Tautology.instance, Tautology.instance));
    }

    public EdgeIterable getMetaEdges(Node node) {
        readLock();
        AbstractNode absNode = checkNode(node);
        return dhns.newEdgeIterable(new MetaEdgeNodeIterator(absNode.getMetaEdgesOutTree(), absNode.getMetaEdgesInTree(), MetaEdgeNodeIterator.EdgeNodeIteratorMode.BOTH, true));
    }

    @Override
    public HierarchicalUndirectedGraphImpl copy(Dhns dhns, GraphStructure structure, View view) {
        return new HierarchicalUndirectedGraphImpl(dhns, structure);
    }
}
