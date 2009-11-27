package org.gephi.ui.preview;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.gephi.preview.api.BidirectionalEdge;
import org.gephi.preview.api.Graph;
import org.gephi.preview.api.Node;
import org.gephi.preview.api.PreviewController;
import org.gephi.preview.api.SelfLoop;
import org.gephi.preview.api.UnidirectionalEdge;
import org.openide.util.Lookup;

/**
 * Controller implementation of the preview UI.
 *
 * @author Jérémy Subtil <jeremy.subtil@gephi.org>
 */
public class PreviewUIController {

    private static PreviewUIController instance;
    private Graph graph = null;
    private float visibilityRatio;
    private boolean visibilityRatioChangedFlag = false;
    private final Set<Node> visibleNodes = new HashSet<Node>();
    private final Set<SelfLoop> visibleSelfLoops = new HashSet<SelfLoop>();
    private final Set<UnidirectionalEdge> visibleUnidirectionalEdges = new HashSet<UnidirectionalEdge>();
    private final Set<BidirectionalEdge> visibleBidirectionalEdges = new HashSet<BidirectionalEdge>();

    /**
     * Private constructor.
     */
    private PreviewUIController() {

    }

    /**
     * Returns the PreviewUIController singleton instance.
     *
     * @return the PreviewUIController singleton instance
     */
    public static synchronized PreviewUIController findInstance() {
        if (null == instance) {
            instance = new PreviewUIController();
        }
        return instance;
    }

    /**
     * Returns the current preview graph.
     *
     * @return the current preview graph
     */
    public Graph getGraph() {
        return graph;
    }

    /**
     * Returns the visible nodes of the graph.
     *
     * @return an iterable on visible nodes
     */
    public Iterable<Node> getVisibleNodes() {
        return visibleNodes;
    }

    /**
     * Returns the visible self-loops of the graph.
     *
     * @return an iterable on visible self-loops
     */
    public Iterable<SelfLoop> getVisibleSelfLoops() {
        return visibleSelfLoops;
    }

    /**
     * Returns the visible unidirectional edges of the graph.
     *
     * @return an iterable on visible unidirectional edges
     */
    public Iterable<UnidirectionalEdge> getVisibleUnidirectionalEdges() {
        return visibleUnidirectionalEdges;
    }

    /**
     * Returns the visible bidirectional edges of the graph.
     *
     * @return an iterable on visible bidirectional edges
     */
    public Iterable<BidirectionalEdge> getVisibleBidirectionalEdges() {
        return visibleBidirectionalEdges;
    }

    /**
     * Refreshes the preview applet.
     */
    public void refreshPreview() {
        PreviewController previewController = Lookup.getDefault().lookup(PreviewController.class);
        PreviewTopComponent previewTopComponent = PreviewTopComponent.findInstance();
        PreviewSettingsTopComponent previewSettingsTopComponent = PreviewSettingsTopComponent.findInstance();

        previewTopComponent.hideBannerPanel();

        previewController.buildGraph();
        setVisibilityRatio(previewSettingsTopComponent.getVisibilityRatio());

        // updates graph if needed
        if (hasGraphChanged()) {
            updateGraph();
        }

        // updates visible graph parts if needed
        if (visibilityRatioChangedFlag) {
            updateVisibleGraphParts();
        }

        // actually updates the preview
        previewTopComponent.refreshPreview();
    }

    /**
     * Defines the graph visibility ratio.
     *
     * @param ratio  the graph visibility ratio
     */
    private void setVisibilityRatio(float ratio) {
        if (ratio != visibilityRatio) {
            visibilityRatioChangedFlag = true;
        }
        visibilityRatio = ratio;
    }

    /**
     * Returns whether or not the preview graph has changed.
     *
     * @return true if the preview graph has changed
     */
    private boolean hasGraphChanged() {
        PreviewController controller = Lookup.getDefault().lookup(PreviewController.class);
        return controller.getGraph() != graph;
    }

    /**
     * Updates the preview graph from the controller's one.
     */
    private void updateGraph() {
        PreviewController controller = Lookup.getDefault().lookup(PreviewController.class);
        PreviewTopComponent previewTopComponent = PreviewTopComponent.findInstance();

        // fetches the current graph
        graph = controller.getGraph();

        // visible graph parts must be updated
        updateVisibleGraphParts();

        // updates preview's graph
        previewTopComponent.setGraph(graph);
    }

    /**
     * Updates lists of visible graph parts.
     */
    private void updateVisibleGraphParts() {
        updateVisibleNodes();
        updateVisibleSelfLoops();
        updateVisibleUnidirectionalEdges();
        updateVisibleBidirectionalEdges();

        visibilityRatioChangedFlag = false;
    }

    /**
     * Updates the list of visible nodes.
     */
    private void updateVisibleNodes() {
        int visibleNodesCount = (int) (graph.countNodes() * visibilityRatio);
        Iterator<Node> nodeIt = graph.getNodes().iterator();

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

        for (SelfLoop sl : graph.getSelfLoops()) {
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

        for (UnidirectionalEdge ue : graph.getUnidirectionalEdges()) {
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

        for (BidirectionalEdge be : graph.getBidirectionalEdges()) {
            if (visibleNodes.contains(be.getNode1()) && visibleNodes.contains(be.getNode2())) {
                visibleBidirectionalEdges.add(be);
            }
        }
    }
}
