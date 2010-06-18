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

import java.util.ArrayList;
import org.gephi.datalaboratory.api.DataTablesController;
import org.gephi.datalaboratory.api.DataTablesEventListener;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Node;
import org.openide.util.lookup.ServiceProvider;

/**
 * Implementation of the DataTablesController interface
 * declared in the Data Laboratory API.
 * @author Eduardo Ramos <eduramiba@gmail.com>
 * @see DataTablesController
 */
@ServiceProvider(service=DataTablesController.class)
public class DataTablesControllerImpl implements DataTablesController{
    ArrayList<DataTablesEventListener> listeners=new ArrayList<DataTablesEventListener>();

    public void addDataTablesEventListener(DataTablesEventListener listener) {
        listeners.add(listener);
    }

    public void removeDataTablesEventListener(DataTablesEventListener listener) {
        listeners.remove(listener);
    }

    public void selectNodesTable() {
        for(DataTablesEventListener l:listeners){
            l.selectNodesTable();
        }
    }

    public void selectEdgesTable() {
        for(DataTablesEventListener l:listeners){
            l.selectEdgesTable();
        }
    }

    public void refreshCurrentTable() {
        for(DataTablesEventListener l:listeners){
            l.refreshCurrentTable();
        }
    }

    public void setNodeTableSelection(Node[] nodes) {
        for(DataTablesEventListener l:listeners){
            l.setNodeTableSelection(nodes);
        }
    }

    public void setEdgeTableSelection(Edge[] edges) {
        for(DataTablesEventListener l:listeners){
            l.setEdgeTableSelection(edges);
        }
    }
}
