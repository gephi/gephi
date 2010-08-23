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
package org.gephi.datalaboratory.api;

import org.gephi.datalaboratory.api.DataTablesController.ExportMode;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Node;

/**
 * <p>This is the interface for a listener of <code>DataTablesController</code> requests.</p>
 * <p><b>Only data table UI should be an implementation of this listener</b></p>
 * @see DataTablesController
 * @author Eduardo Ramos <eduramiba@gmail.com>
 */
public interface DataTablesEventListener {
    void selectNodesTable();

    void selectEdgesTable();

    void refreshCurrentTable();

    void setNodeTableSelection(Node[] nodes);

    void setEdgeTableSelection(Edge[] edges);

    Node[] getNodeTableSelection();

    Edge[] getEdgeTableSelection();

    boolean isNodeTableMode();

    boolean isEdgeTableMode();

    boolean isShowingOnlyVisible();

    void exportCurrentTable(ExportMode exportMode);
}
