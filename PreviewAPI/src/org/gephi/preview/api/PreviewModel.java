/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.preview.api;

import org.gephi.preview.api.supervisors.DirectedEdgeSupervisor;
import org.gephi.preview.api.supervisors.GlobalEdgeSupervisor;
import org.gephi.preview.api.supervisors.NodeSupervisor;
import org.gephi.preview.api.supervisors.SelfLoopSupervisor;
import org.gephi.preview.api.supervisors.UndirectedEdgeSupervisor;

/**
 *
 * @author Mathieu Bastian
 */
public interface PreviewModel {

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
    public DirectedEdgeSupervisor getUniEdgeSupervisor();

    /**
     * Returns the bidirectional edge supervisor.
     *
     * @return the controller's bidirectional edge supervisor
     */
    public DirectedEdgeSupervisor getBiEdgeSupervisor();

    /**
     * Returns the undirected edge supervisor.
     *
     * @return the controller's undirected edge supervisor
     */
    public UndirectedEdgeSupervisor getUndirectedEdgeSupervisor();

    public float getVisibilityRatio();

    public PreviewPreset getCurrentPreset();

    public java.awt.Color getBackgroundColor();
}
