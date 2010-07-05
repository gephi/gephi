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
package org.gephi.datalaboratory.impl;

import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeTable;
import org.gephi.datalaboratory.api.DataTablesController;
import org.gephi.datalaboratory.api.DataTablesEventListener;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Node;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 * Implementation of the DataTablesController interface
 * declared in the Data Laboratory API.
 * @author Eduardo Ramos <eduramiba@gmail.com>
 * @see DataTablesController
 */
@ServiceProvider(service = DataTablesController.class)
public class DataTablesControllerImpl implements DataTablesController {

    DataTablesEventListener listener;

    public void setDataTablesEventListener(DataTablesEventListener listener) {
        this.listener = listener;
    }

    public void selectNodesTable() {
        listener.selectNodesTable();
    }

    public void selectEdgesTable() {
        listener.selectEdgesTable();
    }

    public void selectTable(AttributeTable table) {
        AttributeController ac = Lookup.getDefault().lookup(AttributeController.class);
        if (ac.getModel().getNodeTable() == table) {
            selectNodesTable();
        } else {
            selectEdgesTable();
        }
    }

    public void refreshCurrentTable() {
        listener.refreshCurrentTable();
    }

    public void setNodeTableSelection(Node[] nodes) {
        listener.setNodeTableSelection(nodes);
    }

    public void setEdgeTableSelection(Edge[] edges) {
        listener.setEdgeTableSelection(edges);
    }

    public Node[] getNodeTableSelection() {
        return listener.getNodeTableSelection();
    }

    public Edge[] getEdgeTableSelection() {
        return listener.getEdgeTableSelection();
    }

    public boolean isNodeTableMode() {
        return listener.isNodeTableMode();
    }

    public boolean isEdgeTableMode() {
        return listener.isEdgeTableMode();
    }
}
