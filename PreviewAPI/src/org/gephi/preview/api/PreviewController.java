package org.gephi.preview.api;

import org.gephi.preview.api.supervisors.EdgeSupervisor;
import org.gephi.preview.api.supervisors.GlobalEdgeSupervisor;
import org.gephi.preview.api.supervisors.NodeSupervisor;
import org.gephi.preview.api.supervisors.SelfLoopSupervisor;

/**
 * Interface of the preview controller.
 *
 * @author Jérémy Subtil <jeremy.subtil@gephi.org>
 */
public interface PreviewController {

    /**
     * Returns the current preview graph sheet.
     * 
     * @return the current preview graph sheet
     */
    public GraphSheet getGraphSheet();

    /**
     * Returns a portion of the current preview graph sheet.
     *
     * @param visibilityRatio  the ratio of the preview graph to display
     * @return                 a portion of the current preview graph sheet
     */
    public GraphSheet getPartialGraphSheet(float visibilityRatio);

    /**
     * Retreives the workspace graph and builds a preview graph from it.
     */
    public void buildGraph();

    /**
     * Returns the node supervisor.
     *
     * @return the controller's node supervisor
     */
    public NodeSupervisor getNodeSupervisor();

    /**
     * Returns the global edge supervisor.
     *
     * @return the controller's global edge supervisor
     */
    public GlobalEdgeSupervisor getGlobalEdgeSupervisor();

    /**
     * Returns the self-loop supervisor.
     *
     * @return the controller's self-loop supervisor
     */
    public SelfLoopSupervisor getSelfLoopSupervisor();

    /**
     * Returns the unidirectional edge supervisor.
     *
     * @return the controller's unidirectional edge supervisor
     */
    public EdgeSupervisor getUniEdgeSupervisor();

    /**
     * Returns the bidirectional edge supervisor.
     *
     * @return the controller's bidirectional edge supervisor
     */
    public EdgeSupervisor getBiEdgeSupervisor();
}
