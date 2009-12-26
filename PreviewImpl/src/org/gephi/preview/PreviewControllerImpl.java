package org.gephi.preview;

import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphEvent;
import org.gephi.graph.api.GraphListener;
import org.gephi.graph.api.GraphModel;
import org.gephi.preview.api.Graph;
import org.gephi.preview.api.GraphSheet;
import org.gephi.preview.api.PreviewController;
import org.gephi.preview.api.supervisors.DirectedEdgeSupervisor;
import org.gephi.preview.api.supervisors.GlobalEdgeSupervisor;
import org.gephi.preview.api.supervisors.NodeSupervisor;
import org.gephi.preview.api.supervisors.SelfLoopSupervisor;
import org.gephi.preview.api.supervisors.UndirectedEdgeSupervisor;
import org.gephi.preview.supervisors.BidirectionalEdgeSupervisorImpl;
import org.gephi.preview.supervisors.DirectedEdgeSupervisorImpl;
import org.gephi.preview.supervisors.GlobalEdgeSupervisorImpl;
import org.gephi.preview.supervisors.NodeSupervisorImpl;
import org.gephi.preview.supervisors.SelfLoopSupervisorImpl;
import org.gephi.preview.supervisors.UndirectedEdgeSupervisorImpl;
import org.gephi.preview.supervisors.UnidirectionalEdgeSupervisorImpl;
import org.gephi.project.api.ProjectController;
import org.gephi.workspace.api.Workspace;
import org.gephi.workspace.api.WorkspaceListener;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 * Implementation of the preview controller.
 *
 * @author Jérémy Subtil <jeremy.subtil@gephi.org>
 */
@ServiceProvider(service = PreviewController.class)
public class PreviewControllerImpl implements PreviewController, GraphListener {

    private GraphModel graphModel = null;
    private GraphImpl previewGraph = null;
    private PartialGraphImpl partialPreviewGraph = null;
    private GraphSheetImpl graphSheet = null;
    private GraphSheetImpl partialGraphSheet = null;
    private boolean updateFlag = false;
    private final NodeSupervisorImpl nodeSupervisor = new NodeSupervisorImpl();
    private final GlobalEdgeSupervisorImpl globalEdgeSupervisor = new GlobalEdgeSupervisorImpl();
    private final SelfLoopSupervisorImpl selfLoopSupervisor = new SelfLoopSupervisorImpl();
    private final DirectedEdgeSupervisorImpl uniEdgeSupervisor = new UnidirectionalEdgeSupervisorImpl();
    private final DirectedEdgeSupervisorImpl biEdgeSupervisor = new BidirectionalEdgeSupervisorImpl();
    private final UndirectedEdgeSupervisorImpl undirectedEdgeSupervisor = new UndirectedEdgeSupervisorImpl();
    private final PreviewGraphFactory factory = new PreviewGraphFactory();

    /**
     * Constructor.
     */
    public PreviewControllerImpl() {
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        final GraphController gc = Lookup.getDefault().lookup(GraphController.class);

        // checks the current workspace state before listening to the related events
        if (pc.getCurrentWorkspace() != null) {
            updateFlag = true;
            graphModel = gc.getModel();
            graphModel.addGraphListener(this);
        }

        pc.addWorkspaceListener(new WorkspaceListener() {

            public void initialize(Workspace workspace) {
            }

            public void select(Workspace workspace) {
                updateFlag = true;
                graphModel = gc.getModel();
                graphModel.addGraphListener(PreviewControllerImpl.this);
            }

            public void unselect(Workspace workspace) {
                graphModel.removeGraphListener(PreviewControllerImpl.this);
                graphModel = null;
            }

            public void close(Workspace workspace) {
            }

            public void disable() {
                graphSheet = null;
            }
        });
    }

    /**
     * Sets the update flag when the structure of the workspace graph has
     * changed.
     *
     * @param event
     * @see GraphListener#graphChanged(org.gephi.graph.api.GraphEvent)
     */
    public void graphChanged(GraphEvent event) {
        updateFlag = true;
    }

    /**
     * Returns the current preview graph.
     *
     * @return the current preview graph
     */
    public Graph getGraph() {
        // the preview graph is built if needed
        if (updateFlag) {
            buildGraph();
        }

        return previewGraph;
    }

    /**
     * Returns a subgraph of the current preview graph.
     *
     * @param visibilityRatio  the ratio of the preview graph to display
     * @return                 a subgraph of the current preview graph
     */
    public Graph getPartialGraph(float visibilityRatio) {
        if (updateFlag || null == partialPreviewGraph || partialPreviewGraph.getVisibilityRatio() != visibilityRatio) {
            partialPreviewGraph = new PartialGraphImpl(getGraph(), visibilityRatio);
        }

        return partialPreviewGraph;
    }

    public GraphSheet getGraphSheet() {
        if (updateFlag || null == graphSheet || graphSheet.getGraph() != previewGraph) {
            graphSheet = new GraphSheetImpl(getGraph());
        }

        return graphSheet;
    }

    public GraphSheet getPartialGraphSheet(float visibilityRatio) {
        if (updateFlag || null == partialGraphSheet
                || ((PartialGraphImpl) partialGraphSheet.getGraph()).getVisibilityRatio() != visibilityRatio) {
            partialGraphSheet = new GraphSheetImpl(getPartialGraph(visibilityRatio));
        }

        return partialGraphSheet;
    }

    /**
     * Retreives the workspace graph and builds a preview graph from it.
     *
     * For each build, the supervisors' lists of supervised elements are
     * cleared because the previous preview graph is forgotten.
     *
     * @see PreviewController#buildGraph()
     */
    public void buildGraph() {
        clearSupervisors();

        if (graphModel.isUndirected()) {
            previewGraph = factory.createPreviewGraph(graphModel.getUndirectedGraph());
        } else if (graphModel.isDirected()) {
            previewGraph = factory.createPreviewGraph(graphModel.getDirectedGraph());
        } else if (graphModel.isMixed()) {
            previewGraph = factory.createPreviewGraph(graphModel.getMixedGraph());
        }

        updateFlag = false;
    }

    public NodeSupervisor getNodeSupervisor() {
        return nodeSupervisor;
    }

    public GlobalEdgeSupervisor getGlobalEdgeSupervisor() {
        return globalEdgeSupervisor;
    }

    public SelfLoopSupervisor getSelfLoopSupervisor() {
        return selfLoopSupervisor;
    }

    public DirectedEdgeSupervisor getUniEdgeSupervisor() {
        return uniEdgeSupervisor;
    }

    public DirectedEdgeSupervisor getBiEdgeSupervisor() {
        return biEdgeSupervisor;
    }

    public UndirectedEdgeSupervisor getUndirectedEdgeSupervisor() {
        return undirectedEdgeSupervisor;
    }

    /**
     * Clears the supervisors' lists of supervised elements.
     */
    private void clearSupervisors() {
        nodeSupervisor.clearSupervised();
        globalEdgeSupervisor.clearSupervised();
        selfLoopSupervisor.clearSupervised();
        uniEdgeSupervisor.clearSupervised();
        biEdgeSupervisor.clearSupervised();
    }
}
