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

import org.gephi.graph.api.ClusteredUndirectedGraph;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.EdgeIterable;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.NodeIterable;
import org.gephi.graph.dhns.core.Dhns;
import org.gephi.graph.dhns.edge.AbstractEdge;
import org.gephi.graph.dhns.edge.iterators.EdgeIterator;
import org.gephi.graph.dhns.edge.iterators.EdgeNodeIterator;
import org.gephi.graph.dhns.edge.iterators.VisibleEdgeIterator;
import org.gephi.graph.dhns.edge.iterators.VisibleEdgeNodeIterator;
import org.gephi.graph.dhns.node.PreNode;
import org.gephi.graph.dhns.node.iterators.CompleteTreeIterator;
import org.gephi.graph.dhns.node.iterators.NeighborIterator;
import org.gephi.graph.dhns.node.iterators.VisibleTreeIterator;

/**
 * Implementation of clustered undirected graph.
 *
 * @author Mathieu Bastian
 */
public class ClusteredUndirectedGraphImpl extends ClusteredGraphImpl implements ClusteredUndirectedGraph {

    public ClusteredUndirectedGraphImpl(Dhns dhns, boolean visible) {
        super(dhns, visible);
    }

    public boolean addEdge(Node node1, Node node2) {
        PreNode preNode1 = checkNode(node1);
        PreNode preNode2 = checkNode(node2);
        if (checkEdgeExist(preNode1, preNode2) || checkEdgeExist(preNode2, preNode1)) {
            //Edge already exist
            return false;
        }
        AbstractEdge edge = dhns.getGraphFactory().newEdge(node1, node2);
        dhns.getStructureModifier().addEdge(edge);
        return true;
    }

    public void removeEdge(Edge edge) {
        checkEdge(edge);
        AbstractEdge absEdge = (AbstractEdge) edge;
        if (!absEdge.isSelfLoop()) {
            //Remove also mutual edge if present
            AbstractEdge symmetricEdge = getSymmetricEdge(absEdge);
            if (symmetricEdge != null) {
                dhns.getStructureModifier().deleteEdge(symmetricEdge);
            }
        }
        dhns.getStructureModifier().deleteEdge(edge);
    }

    public boolean contains(Edge edge) {
        return getEdge(edge.getSource(), edge.getTarget()) != null;
    }

    public EdgeIterable getEdges() {
        readLock();
        if (visible) {
            return dhns.newEdgeIterable(new VisibleEdgeIterator(dhns.getTreeStructure(), new VisibleTreeIterator(dhns.getTreeStructure()), true));
        } else {
            return dhns.newEdgeIterable(new EdgeIterator(dhns.getTreeStructure(), new CompleteTreeIterator(dhns.getTreeStructure()), true));
        }
    }

    public EdgeIterable getEdges(Node node) {
        PreNode preNode = checkNode(node);
        readLock();
        if (visible) {
            return dhns.newEdgeIterable(new VisibleEdgeNodeIterator(preNode, VisibleEdgeNodeIterator.EdgeNodeIteratorMode.BOTH, true));
        } else {
            return dhns.newEdgeIterable(new EdgeNodeIterator(preNode, EdgeNodeIterator.EdgeNodeIteratorMode.BOTH, true));
        }
    }

    public NodeIterable getNeighbors(Node node) {
        PreNode preNode = checkNode(node);
        readLock();
        if (visible) {
            return dhns.newNodeIterable(new NeighborIterator(new VisibleEdgeNodeIterator(preNode, VisibleEdgeNodeIterator.EdgeNodeIteratorMode.BOTH, true), preNode));
        } else {
            return dhns.newNodeIterable(new NeighborIterator(new EdgeNodeIterator(preNode, EdgeNodeIterator.EdgeNodeIteratorMode.BOTH, true), preNode));
        }
    }

    public int getEdgeCount() {
        readLock();
        int count = 0;
        if (visible) {
            for (VisibleEdgeIterator itr = new VisibleEdgeIterator(dhns.getTreeStructure(), new VisibleTreeIterator(dhns.getTreeStructure()), true); itr.hasNext();) {
                itr.next();
                count++;
            }
        } else {
            for (EdgeIterator itr = new EdgeIterator(dhns.getTreeStructure(), new VisibleTreeIterator(dhns.getTreeStructure()), true); itr.hasNext();) {
                itr.next();
                count++;
            }
        }
        readUnlock();
        return count;
    }

    public int getDegree(Node node) {
        PreNode preNode = checkNode(node);
        readLock();
        int count = 0;
        if (visible) {
            VisibleEdgeNodeIterator itr = new VisibleEdgeNodeIterator(preNode, VisibleEdgeNodeIterator.EdgeNodeIteratorMode.BOTH, true);
            for (; itr.hasNext();) {
                AbstractEdge edge = itr.next();
                if (edge.isSelfLoop()) {
                    count++;
                }
                count++;
            }
        } else {
            EdgeNodeIterator itr = new EdgeNodeIterator(preNode, EdgeNodeIterator.EdgeNodeIteratorMode.BOTH, true);
            for (; itr.hasNext();) {
                AbstractEdge edge = itr.next();
                if (edge.isSelfLoop()) {
                    count++;
                }
                count++;
            }
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

    public EdgeIterable getInnerEdges(Node nodeGroup) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public EdgeIterable getOuterEdges(Node nodeGroup) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int getMetaDegree(Node node) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public EdgeIterable getMetaEdges() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public EdgeIterable getMetaEdges(Node node) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Edge getEdge(Node node1, Node node2) {
        PreNode sourceNode = checkNode(node1);
        PreNode targetNode = checkNode(node2);
        readLock();
        AbstractEdge res = null;
        if (visible) {
            AbstractEdge edge = sourceNode.getEdgesOutTree().getItem(targetNode.getNumber());
            if (edge == null) {
                edge = sourceNode.getEdgesInTree().getItem(targetNode.getNumber());
            } else if (edge.isVisible()) {
                res = edge;
            }
        } else {
            res = sourceNode.getEdgesOutTree().getItem(targetNode.getNumber());
            if (res == null) {
                res = sourceNode.getEdgesInTree().getItem(targetNode.getNumber());
            }
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
        if(mutualEdge!=null) {
            mutualEdge.setVisible(visible);
        }
        writeUnlock();

    }
}
