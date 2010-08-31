/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
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
