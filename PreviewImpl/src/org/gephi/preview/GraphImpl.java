package org.gephi.preview;

import java.util.ArrayList;
import org.gephi.preview.api.BidirectionalEdge;
import org.gephi.preview.api.Graph;
import org.gephi.preview.api.Node;
import org.gephi.preview.api.PreviewController;
import org.gephi.preview.api.SelfLoop;
import org.gephi.preview.api.UnidirectionalEdge;
import org.openide.util.Lookup;

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
     * @see Graph#getNodes()
     */
    public Iterable<Node> getNodes() {
        return nodes;
    }

    /**
     * @see Graph#getSelfLoops()
     */
    public Iterable<SelfLoop> getSelfLoops() {
        return selfLoops;
    }

    /**
     * @see Graph#getUnidirectionalEdges()
     */
    public Iterable<UnidirectionalEdge> getUnidirectionalEdges() {
        return uniEdges;
    }

    /**
     * @see Graph#getBidirectionalEdges()
     */
    public Iterable<BidirectionalEdge> getBidirectionalEdges() {
        return biEdges;
    }

    /**
     * @see Graph#countNodes()
     */
    public float countNodes() {
        return nodes.size();
    }

    /**
     * @see Graph#countUnidirectionalEdges()
     */
    public float countUnidirectionalEdges() {
        return uniEdges.size();
    }

    /**
     * @see Graph#countBidirectionalEdges()
     */
    public float countBidirectionalEdges() {
        return biEdges.size();
    }

    /**
     * @see Graph#countSelfLoops()
     */
    public float countSelfLoops() {
        return selfLoops.size();
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
     * @see Graph#showNodes()
     */
    public Boolean showNodes() {
        PreviewController controller = Lookup.getDefault().lookup(PreviewController.class);
        return controller.getNodeSupervisor().getShowNodes();
    }

    /**
     * @see Graph#showEdges()
     */
    public Boolean showEdges() {
        PreviewController controller = Lookup.getDefault().lookup(PreviewController.class);
        return controller.getGlobalEdgeSupervisor().getShowFlag();
    }

    /**
     * @see Graph#showSelfLoops()
     */
    public Boolean showSelfLoops() {
        PreviewController controller = Lookup.getDefault().lookup(PreviewController.class);
        return controller.getSelfLoopSupervisor().getShowFlag();
    }
}
