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

    public DataTablesEventListener getDataTablesEventListener() {
        return listener;
    }

    public boolean isDataTablesReady() {
        return listener!=null;
    }

    public void selectNodesTable() {
        if (listener != null) {
            listener.selectNodesTable();
        }
    }

    public void selectEdgesTable() {
        if (listener != null) {
            listener.selectEdgesTable();
        }
    }

    public void selectTable(AttributeTable table) {
        if (listener != null) {
            AttributeController ac = Lookup.getDefault().lookup(AttributeController.class);
            if (ac.getModel().getNodeTable() == table) {
                selectNodesTable();
            } else {
                selectEdgesTable();
            }
        }
    }

    public void refreshCurrentTable() {
        if (listener != null) {
            listener.refreshCurrentTable();
        }
    }

    public void setNodeTableSelection(Node[] nodes) {
        if (listener != null) {
            listener.setNodeTableSelection(nodes);
        }
    }

    public void setEdgeTableSelection(Edge[] edges) {
        if (listener != null) {
            listener.setEdgeTableSelection(edges);
        }
    }

    public Node[] getNodeTableSelection() {
        if (listener != null) {
            return listener.getNodeTableSelection();
        } else {
            return null;
        }
    }

    public Edge[] getEdgeTableSelection() {
        if (listener != null) {
            return listener.getEdgeTableSelection();
        } else {
            return null;
        }
    }

    public boolean isNodeTableMode() {
        if (listener != null) {
            return listener.isNodeTableMode();
        } else {
            return false;
        }
    }

    public boolean isEdgeTableMode() {
        if (listener != null) {
            return listener.isEdgeTableMode();
        } else {
            return false;
        }
    }

    public boolean isShowOnlyVisible() {
        if (listener != null) {
            return listener.isShowOnlyVisible();
        } else {
            return false;
        }
    }

    public void setShowOnlyVisible(boolean showOnlyVisible){
        if (listener != null) {
            listener.setShowOnlyVisible(showOnlyVisible);
        }
    }

    public void exportCurrentTable(ExportMode exportMode) {
        if (listener != null) {
            listener.exportCurrentTable(exportMode);
        }
    }

    public boolean isUseSparklines() {
        if (listener != null) {
            return listener.isUseSparklines();
        } else {
            return false;
        }
    }

    public void setUseSparklines(boolean useSparklines) {
        if (listener != null) {
            listener.setUseSparklines(useSparklines);
        }
    }

    public boolean isShowEdgesNodesLabels() {
        if (listener != null) {
            return listener.isShowEdgesNodesLabels();
        } else {
            return false;
        }
    }

    public void setShowEdgesNodesLabels(boolean showEdgesNodesLabels) {
        if (listener != null) {
            listener.setShowEdgesNodesLabels(showEdgesNodesLabels);
        }
    }
}
