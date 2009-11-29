package org.gephi.preview;

import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.preview.api.Graph;
import org.gephi.preview.api.PreviewController;
import org.gephi.preview.api.supervisors.EdgeSupervisor;
import org.gephi.preview.api.supervisors.GlobalEdgeSupervisor;
import org.gephi.preview.api.supervisors.NodeSupervisor;
import org.gephi.preview.api.supervisors.SelfLoopSupervisor;
import org.gephi.preview.supervisors.BidirectionalEdgeSupervisorImpl;
import org.gephi.preview.supervisors.EdgeSupervisorImpl;
import org.gephi.preview.supervisors.GlobalEdgeSupervisorImpl;
import org.gephi.preview.supervisors.NodeSupervisorImpl;
import org.gephi.preview.supervisors.SelfLoopSupervisorImpl;
import org.gephi.preview.supervisors.UnidirectionalEdgeSupervisorImpl;
import org.gephi.project.api.ProjectController;
import org.gephi.workspace.api.Workspace;
import org.gephi.workspace.api.WorkspaceListener;
import org.openide.util.Lookup;

/**
 * Implementation of the preview controller.
 *
 * @author Jérémy Subtil <jeremy.subtil@gephi.org>
 */
public class PreviewControllerImpl implements PreviewController {

    private GraphImpl previewGraph = null;
    private boolean updateFlag = false;
    private final NodeSupervisorImpl nodeSupervisor = new NodeSupervisorImpl();
    private final GlobalEdgeSupervisorImpl globalEdgeSupervisor = new GlobalEdgeSupervisorImpl();
    private final SelfLoopSupervisorImpl selfLoopSupervisor = new SelfLoopSupervisorImpl();
    private final EdgeSupervisorImpl uniEdgeSupervisor = new UnidirectionalEdgeSupervisorImpl();
    private final EdgeSupervisorImpl biEdgeSupervisor = new BidirectionalEdgeSupervisorImpl();
    private final PreviewGraphFactory factory = new PreviewGraphFactory();

    /**
     * Constructor.
     */
    public PreviewControllerImpl() {
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        pc.addWorkspaceListener(new WorkspaceListener() {

            public void initialize(Workspace workspace) {
            }

            public void select(Workspace workspace) {
                updateFlag = true;
            }

            public void unselect(Workspace workspace) {
            }

            public void close(Workspace workspace) {
            }

            public void disable() {
                previewGraph = null;
            }
        });
    }

    /**
     * Returns the current preview graph.
     *
     * The preview graph is built if needed.
     *
     * @return the current preview graph
     */
    public Graph getGraph() {
        if (updateFlag) {
            buildGraph();
        }

        return previewGraph;
    }

    /**
     * Retreives the workspace graph and builds a preview graph from it.
     *
     * For each build, the supervisors' lists of supervised elements are
     * cleared because the previous preview graph is forgotten.
     */
    public void buildGraph() {
        GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getModel();

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

    /**
     * Returns the node supervisor.
     *
     * @return the controller's node supervisor
     */
    public NodeSupervisor getNodeSupervisor() {
        return nodeSupervisor;
    }

    /**
     * Returns the global edge supervisor.
     *
     * @return the controller's global edge supervisor
     */
    public GlobalEdgeSupervisor getGlobalEdgeSupervisor() {
        return globalEdgeSupervisor;
    }

    /**
     * Returns the self-loop supervisor.
     *
     * @return the controller's self-loop supervisor
     */
    public SelfLoopSupervisor getSelfLoopSupervisor() {
        return selfLoopSupervisor;
    }

    /**
     * Returns the unidirectional edge supervisor.
     *
     * @return the controller's unidirectional edge supervisor
     */
    public EdgeSupervisor getUniEdgeSupervisor() {
        return uniEdgeSupervisor;
    }

    /**
     * Returns the bidirectional edge supervisor.
     *
     * @return the controller's bidirectional edge supervisor
     */
    public EdgeSupervisor getBiEdgeSupervisor() {
        return biEdgeSupervisor;
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
