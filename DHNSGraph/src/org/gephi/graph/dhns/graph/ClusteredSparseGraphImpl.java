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

import org.gephi.graph.api.ClusteredSparseGraph;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.EdgeIterable;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.NodeIterable;
import org.gephi.graph.dhns.core.Dhns;
import org.gephi.graph.dhns.edge.AbstractEdge;
import org.gephi.graph.dhns.node.PreNode;

/**
 * Implementation of clustered sparse graph.
 *
 * @author Mathieu Bastian
 */
public class ClusteredSparseGraphImpl extends ClusteredGraphImpl implements ClusteredSparseGraph {

    public ClusteredSparseGraphImpl(Dhns dhns, boolean visible) {
        super(dhns, visible);
    }

    public void addEdge(Node source, Node target, boolean directed) {
        if (source == null || target == null) {
            throw new NullPointerException();
        }
        AbstractEdge edge = dhns.getGraphFactory().newEdge(source, target, 1f, directed);
        dhns.getStructureModifier().addEdge(edge);
    }

    public Iterable<Edge> getDirectedEdges() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Iterable<Edge> getUndirectedEdges() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean isDirected(Edge edge) {
        return ((AbstractEdge) edge).isDirected();
    }

    public boolean contains(Edge edge) {
        return getEdge(edge.getSource(), edge.getTarget()) != null;
    }

    public Edge getEdge(Node node1, Node node2) {
        if (node1 == null || node2 == null) {
            throw new NullPointerException();
        }
        readLock();
        PreNode sourceNode = (PreNode) node1;
        PreNode targetNode = (PreNode) node2;
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

    public EdgeIterable getEdges() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public NodeIterable getNeigbors(Node node) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public EdgeIterable getEdges(Node node) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int getEdgeCount() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int getDegree(Node node) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean isNeighbor(Node node, Node neighbor) {
        throw new UnsupportedOperationException("Not supported yet.");
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

    public EdgeIterable getMetaEdges(Node node) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int getMetaDegree(Node node) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
