/*
Copyright 2008-2010 Gephi
Authors : Jeremy Subtil <jeremy.subtil@gephi.org>
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
*/
package org.gephi.preview;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.gephi.preview.api.BidirectionalEdge;
import org.gephi.preview.api.Graph;
import org.gephi.preview.api.Node;
import org.gephi.preview.api.SelfLoop;
import org.gephi.preview.api.UndirectedEdge;
import org.gephi.preview.api.UnidirectionalEdge;

/**
 * Implementation of a partial preview graph.
 *
 * @author Jérémy Subtil <jeremy.subtil@gephi.org>
 */
public class PartialGraphImpl implements Graph {

    private final float visibilityRatio;
    private final Set<Node> visibleNodes = new HashSet<Node>();
    private final Set<SelfLoop> visibleSelfLoops = new HashSet<SelfLoop>();
    private final Set<UnidirectionalEdge> visibleUnidirectionalEdges = new HashSet<UnidirectionalEdge>();
    private final Set<BidirectionalEdge> visibleBidirectionalEdges = new HashSet<BidirectionalEdge>();
    private final Set<UndirectedEdge> visibleUndirectedEdges = new HashSet<UndirectedEdge>();
    private final Graph originalGraph;

    /**
     * Constructor.
     *
     * @param originalGraph    the original preview graph
     * @param visibilityRatio  the graph's visibility ratio
     */
    public PartialGraphImpl(Graph originalGraph, float visibilityRatio) {
        this.originalGraph = originalGraph;
        this.visibilityRatio = visibilityRatio;
        updateVisibleGraphParts();
    }

    /**
     * Returns the graph visibility ratio.
     *
     * @return the graph visibility ratio
     */
    public float getVisibilityRatio() {
        return visibilityRatio;
    }

    /**
     * Returns the original preview graph.
     *
     * @return the original preview graph
     */
    public Graph getOriginalGraph() {
        return originalGraph;
    }

    /**
     * Returns an iterable on the graph's visible nodes.
     *
     * @return an iterable on the graph's visible nodes
     * @see Graph#getNodes()
     */
    public Iterable<Node> getNodes() {
        return visibleNodes;
    }

    /**
     * Returns an iterable on the graph's visible self-loops.
     *
     * @return an iterable on the graph's visible self-loops
     * @see Graph#getSelfLoops()
     */
    public Iterable<SelfLoop> getSelfLoops() {
        return visibleSelfLoops;
    }

    /**
     * Returns an iterable on the graph's visible unidirectional edges.
     *
     * @return an iterable on the graph's visible unidirectional edges
     * @see Graph#getUnidirectionalEdges()
     */
    public Iterable<UnidirectionalEdge> getUnidirectionalEdges() {
        return visibleUnidirectionalEdges;
    }

    /**
     * Returns an iterable on the graph's visible bidirectional edges.
     *
     * @return an iterable on the graph's visible bidirectional edges
     * @see Graph#getBidirectionalEdges()
     */
    public Iterable<BidirectionalEdge> getBidirectionalEdges() {
        return visibleBidirectionalEdges;
    }

    /**
     * Returns an iterable on the graph's visible undirected edges.
     *
     * @return an iterable on the graph's visible undirected edges
     * @see Graph#getUndirectedEdges()
     */
    public Iterable<UndirectedEdge> getUndirectedEdges() {
        return visibleUndirectedEdges;
    }

    /**
     * Returns the number of visible nodes in the graph.
     *
     * @return the number of visible nodes in the graph
     * @see Graph#countNodes()
     */
    public int countNodes() {
        return visibleNodes.size();
    }

    /**
     * Returns the number of visible self-loops in the graph.
     *
     * @return the number of visible self-loops in the graph
     * @see Graph#countSelfLoops()
     */
    public int countSelfLoops() {
        return visibleSelfLoops.size();
    }

    /**
     * Returns the number of visible unidirectional edges in the graph.
     *
     * @return the number of visible unidirectional edges in the graph
     * @see Graph#countUnidirectionalEdges()
     */
    public int countUnidirectionalEdges() {
        return visibleUnidirectionalEdges.size();
    }

    /**
     * Returns the number of visible bidirectional edges in the graph.
     *
     * @return the number of visible bidirectional edges in the graph
     * @see Graph#countBidirectionalEdges()
     */
    public int countBidirectionalEdges() {
        return visibleBidirectionalEdges.size();
    }

    /**
     * Returns the number of visible undirected edges in the graph.
     *
     * @return the number of visible undirected edges in the graph
     * @see Graph#countUndirectedEdges()
     */
    public int countUndirectedEdges() {
        return visibleUndirectedEdges.size();
    }

    public Boolean showNodes() {
        return originalGraph.showNodes();
    }

    public Boolean showEdges() {
        return originalGraph.showEdges();
    }

    public Boolean showSelfLoops() {
        return originalGraph.showSelfLoops();
    }

    /**
     * Updates lists of visible graph parts.
     */
    private void updateVisibleGraphParts() {
        updateVisibleNodes();
        updateVisibleSelfLoops();
        updateVisibleUnidirectionalEdges();
        updateVisibleBidirectionalEdges();
        updateVisibleUndirectedEdges();
    }

    /**
     * Updates the list of visible nodes.
     */
    private void updateVisibleNodes() {
        int visibleNodesCount = (int) (originalGraph.countNodes() * visibilityRatio);
        if (visibleNodesCount == 0) {
            visibleNodesCount = Math.min(10, originalGraph.countNodes());
        }
        Iterator<Node> nodeIt = originalGraph.getNodes().iterator();

        visibleNodes.clear();

        for (int i = 0; i < visibleNodesCount; i++) {
            if (!nodeIt.hasNext()) {
                break;
            }

            visibleNodes.add(nodeIt.next());
        }
    }

    /**
     * Updates the list of visible self-loops.
     */
    private void updateVisibleSelfLoops() {
        visibleSelfLoops.clear();

        for (SelfLoop sl : originalGraph.getSelfLoops()) {
            if (visibleNodes.contains(sl.getNode())) {
                visibleSelfLoops.add(sl);
            }
        }
    }

    /**
     * Updates the list of visible unidirectional edges.
     */
    private void updateVisibleUnidirectionalEdges() {
        visibleUnidirectionalEdges.clear();

        for (UnidirectionalEdge ue : originalGraph.getUnidirectionalEdges()) {
            if (visibleNodes.contains(ue.getNode1()) && visibleNodes.contains(ue.getNode2())) {
                visibleUnidirectionalEdges.add(ue);
            }
        }
    }

    /**
     * Updates the list of visible bidirectional edges.
     */
    private void updateVisibleBidirectionalEdges() {
        visibleBidirectionalEdges.clear();

        for (BidirectionalEdge be : originalGraph.getBidirectionalEdges()) {
            if (visibleNodes.contains(be.getNode1()) && visibleNodes.contains(be.getNode2())) {
                visibleBidirectionalEdges.add(be);
            }
        }
    }

    /**
     * Updates the list of visible undirected edges.
     */
    private void updateVisibleUndirectedEdges() {
        visibleUndirectedEdges.clear();

        for (UndirectedEdge e : originalGraph.getUndirectedEdges()) {
            if (visibleNodes.contains(e.getNode1()) && visibleNodes.contains(e.getNode2())) {
                visibleUndirectedEdges.add(e);
            }
        }
    }
}
