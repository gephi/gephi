package org.gephi.ui.preview;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.gephi.preview.api.BidirectionalEdge;
import org.gephi.preview.api.Graph;
import org.gephi.preview.api.Node;
import org.gephi.preview.api.SelfLoop;
import org.gephi.preview.api.UnidirectionalEdge;
import processing.core.PVector;

/**
 * Implementation of a partial preview graph.
 *
 * @author Jérémy Subtil <jeremy.subtil@gephi.org>
 */
public class PartialGraphImpl implements Graph {

    private float visibilityRatio;
    private final Set<Node> visibleNodes = new HashSet<Node>();
    private final Set<SelfLoop> visibleSelfLoops = new HashSet<SelfLoop>();
    private final Set<UnidirectionalEdge> visibleUnidirectionalEdges = new HashSet<UnidirectionalEdge>();
    private final Set<BidirectionalEdge> visibleBidirectionalEdges = new HashSet<BidirectionalEdge>();
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
     * Defines the graph visibility ratio.
     *
     * If it is updated, the list of visible parts is updated.
     *
     * @param ratio  the graph visibility ratio
     */
    public void setVisibilityRatio(float ratio) {
        if (ratio != visibilityRatio) {
            visibilityRatio = ratio;
            updateVisibleGraphParts();
        }
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
     * @see Graph#getMinPos()
     */
    public PVector getMinPos() {
        return originalGraph.getMinPos();
    }

    /**
     * @see Graph#getMaxPos()
     */
    public PVector getMaxPos() {
        return originalGraph.getMaxPos();
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
     * Returns the number of visible nodes in the graph.
     *
     * @return the number of visible nodes in the graph
     * @see Graph#countNodes()
     */
    public float countNodes() {
        return visibleNodes.size();
    }

    /**
     * @see Graph#showNodes()
     */
    public Boolean showNodes() {
        return originalGraph.showNodes();
    }

    /**
     * @see Graph#showEdges()
     */
    public Boolean showEdges() {
        return originalGraph.showEdges();
    }

    /**
     * @see Graph#showSelfLoops()
     */
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
    }

    /**
     * Updates the list of visible nodes.
     */
    private void updateVisibleNodes() {
        int visibleNodesCount = (int) (originalGraph.countNodes() * visibilityRatio);
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
}
