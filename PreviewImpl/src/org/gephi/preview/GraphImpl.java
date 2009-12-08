package org.gephi.preview;

import java.util.ArrayList;
import org.gephi.preview.api.BidirectionalEdge;
import org.gephi.preview.api.Graph;
import org.gephi.preview.api.Node;
import org.gephi.preview.api.PreviewController;
import org.gephi.preview.api.SelfLoop;
import org.gephi.preview.api.UnidirectionalEdge;
import org.openide.util.Lookup;
import processing.core.PVector;

/**
 * Implementation of a preview graph.
 *
 * @author Jérémy Subtil <jeremy.subtil@gephi.org>
 */
public class GraphImpl implements Graph {

    private final ArrayList<Node> nodes = new ArrayList<Node>();
    private final ArrayList<SelfLoop> selfLoops = new ArrayList<SelfLoop>();
    private final ArrayList<UnidirectionalEdge> uniEdges = new ArrayList<UnidirectionalEdge>();
    private final ArrayList<BidirectionalEdge> biEdges = new ArrayList<BidirectionalEdge>();

    /**
     * Returns an iterable on the graph's nodes.
     *
     * @return an iterable on the graph's nodes
     */
    public Iterable<Node> getNodes() {
        return nodes;
    }

    /**
     * Returns an iterable on the graph's self-loops.
     *
     * @return an iterable on the graph's self-loops
     */
    public Iterable<SelfLoop> getSelfLoops() {
        return selfLoops;
    }

    /**
     * Returns an iterable on the graph's unidirectional edges.
     *
     * @return an iterable on the graph's unidirectional edges
     */
    public Iterable<UnidirectionalEdge> getUnidirectionalEdges() {
        return uniEdges;
    }

    /**
     * Returns an iterable on the graph's bidirectional edges.
     *
     * @return an iterable on the graph's bidirectional edges
     */
    public Iterable<BidirectionalEdge> getBidirectionalEdges() {
        return biEdges;
    }

    /**
     * Returns the number or nodes in the graph.
     *
     * @return the number or nodes in the graph
     */
    public float countNodes() {
        return nodes.size();
    }

    /**
     * Adds the given node to the graph.
     *
     * @param node  the node to add to the graph
     */
    public void addNode(NodeImpl node) {
        nodes.add(node);
    }

    /**
     * Adds the given self-loop to the graph.
     *
     * @param selfLoop  the self-loop to add to the graph
     */
    public void addSelfLoop(SelfLoop selfLoop) {
        selfLoops.add(selfLoop);
    }

    /**
     * Adds the given unidirectional edge to the graph.
     *
     * @param edge  the unidirectional edge to add to the graph
     */
    public void addUnidirectionalEdge(UnidirectionalEdge edge) {
        uniEdges.add(edge);
    }

    /**
     * Adds the given bidirectional edge to the graph.
     *
     * @param edge  the bidirectional edge to add to the graph
     */
    public void addBidirectionalEdge(BidirectionalEdge edge) {
        biEdges.add(edge);
    }

    /**
     * Returns true if the nodes must be displayed in the preview.
     *
     * @return true if the nodes must be displayed in the preview
     */
    public Boolean showNodes() {
        PreviewController controller = Lookup.getDefault().lookup(PreviewController.class);
        return controller.getNodeSupervisor().getShowNodes();
    }

    /**
     * Returns true if the edges must be displayed in the preview.
     *
     * @return true if the edges must be displayed in the preview
     */
    public Boolean showEdges() {
        PreviewController controller = Lookup.getDefault().lookup(PreviewController.class);
        return controller.getGlobalEdgeSupervisor().getShowFlag();
    }

    /**
     * Returns true if the self-loops must be displayed in the preview.
     *
     * @return true if the self-loops must be displayed in the preview
     */
    public Boolean showSelfLoops() {
        PreviewController controller = Lookup.getDefault().lookup(PreviewController.class);
        return controller.getSelfLoopSupervisor().getShowFlag();
    }
}
