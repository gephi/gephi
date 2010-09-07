/*
Copyright 2008-2010 Gephi
Authors : Eduardo Ramos <eduramiba@gmail.com>
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
package org.gephi.datalab.api;

import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Node;

/**
 * <p>This is the interface for a listener of <code>DataTablesController</code> requests.</p>
 * <p><b>Only data table UI should be an implementation of this listener</b></p>
 * @see DataTablesController
 * @author Eduardo Ramos <eduramiba@gmail.com>
 */
public interface DataTablesEventListener {
    /**
     * Requests the tables implementation to show nodes table.
     */
    void selectNodesTable();

    /**
     * Requests the tables implementation to show edges table.
     */
    void selectEdgesTable();

    /**
     * Requests the tables implementation to refresh the data of the table being shown.
     */
    void refreshCurrentTable();

    /**
     * Requests the tables implementation to adapt the nodes table row selection to the specified nodes.
     * @param nodes Nodes to select
     */
    void setNodeTableSelection(Node[] nodes);

    /**
     * Requests the tables implementation to adapt the edges table row selection to the specified edges.
     * @param edges Edges to select
     */
    void setEdgeTableSelection(Edge[] edges);

    /**
     * Request the tables implementation to provide the selected nodes in nodes table.
     * @return Array of selected nodes
     */
    Node[] getNodeTableSelection();

    /**
     * Request the tables implementation to provide the selected edges in edges table.
     * @return Array of selected edges
     */
    Edge[] getEdgeTableSelection();

    /**
     * Checks if the data tables implementation is showing nodes table
     * @return True if nodes table is being shown, false otherwise
     */
    boolean isNodeTableMode();

    /**
     * Checks if the data tables implementation is showing edges table
     * @return True if edges table is being shown, false otherwise
     */
    boolean isEdgeTableMode();

    /**
     * Checks if the data tables implementation is showing only visible elements (nodes or edges)
     * in the graph at the moment.
     * @return True if only visible elements are being shown, false otherwise
     */
    boolean isShowOnlyVisible();

    /**
     * Requests the tables implementation to show only visible elements or not.
     * @param showOnlyVisible Indicates if only visible elements have to be shown in table
     */
    void setShowOnlyVisible(boolean showOnlyVisible);

    /**
     * Checks if the data tables implementation is showing number lists and dynamic numbers as sparklines at the moment.
     * @return True if sparklines are on, false otherwise
     */
    boolean isUseSparklines();

    /**
     * Requests the tables implementation to show number lists and dynamic numbers as sparklines.
     * @param showOnlyVisible Indicates if sparklines should be used
     */
    void setUseSparklines(boolean useSparklines);

    /**
     * Checks if the data tables implementation is showing edges nodes (source and target) labels at the moment.
     * @return True if edges nodes lables are shown, false otherwise
     */
    boolean isShowEdgesNodesLabels();

    /**
     * Requests the tables implementation to show edges nodes (source and target).
     * @param showOnlyVisible Indicates if edges nodes labels should be shown
     */
    void setShowEdgesNodesLabels(boolean showEdgesNodesLabels);

    public enum ExportMode{
        CSV
    }

    /**
     * Requests to exports current table being shown as a file.
     * @param exportMode <code>ExportMode</code> - CSV only for now
     */
    void exportCurrentTable(ExportMode exportMode);
}
