/*
Copyright 2008-2010 Gephi
Authors : Eduardo Ramos <eduramiba@gmail.com>
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gephi.datalaboratory.api;

import org.gephi.data.attributes.api.AttributeTable;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Node;


/**
 * This interface defines part of the Data Laboratory API.
 * It provides methods to control the Data Table UI that shows a table for nodes and other for edges.
 * This is done by registering the data table ui as a listener of these events that can be requested with this controller.
 * @author Eduardo Ramos <eduramiba@gmail.com>
 */
public interface DataTablesController{

    /**
     * Requests the tables implementation to show nodes table.
     */
    void selectNodesTable();

     /**
     * Requests the tables implementation to show edges table.
     */
    void selectEdgesTable();

    /**
     * Request the tables implementation to show the given table.
     * Normally nodes or edges table.
     * @param table Table to show
     */
    void selectTable(AttributeTable table);

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
     * Register a listener for these requests.
     * @param listener Instance of DataTablesEventListener
     */
    void addDataTablesEventListener(DataTablesEventListener listener);

    /**
     * Remove a registered listener for these requests.
     * @param listener Instance of DataTablesEventListener
     */
    void removeDataTablesEventListener(DataTablesEventListener listener);
}
