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

    public Iterable<Node> getNodes() {
        return nodes;
    }

    public Iterable<SelfLoop> getSelfLoops() {
        return selfLoops;
    }

    public Iterable<UnidirectionalEdge> getUnidirectionalEdges() {
        return uniEdges;
    }

    public Iterable<BidirectionalEdge> getBidirectionalEdges() {
        return biEdges;
    }

    public int countNodes() {
        return nodes.size();
    }

    public int countSelfLoops() {
        return selfLoops.size();
    }

    public int countUnidirectionalEdges() {
        return uniEdges.size();
    }

    public int countBidirectionalEdges() {
        return biEdges.size();
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

    public Boolean showNodes() {
        PreviewController controller = Lookup.getDefault().lookup(PreviewController.class);
        return controller.getNodeSupervisor().getShowNodes();
    }

    public Boolean showEdges() {
        PreviewController controller = Lookup.getDefault().lookup(PreviewController.class);
        return controller.getGlobalEdgeSupervisor().getShowFlag();
    }

    public Boolean showSelfLoops() {
        PreviewController controller = Lookup.getDefault().lookup(PreviewController.class);
        return controller.getSelfLoopSupervisor().getShowFlag();
    }
}
