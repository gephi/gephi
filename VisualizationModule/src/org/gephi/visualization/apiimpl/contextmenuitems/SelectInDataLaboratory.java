/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>, Eduardo Ramos <eduramiba@gmail.com>
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
package org.gephi.visualization.apiimpl.contextmenuitems;

import java.awt.event.KeyEvent;
import javax.swing.Icon;
import org.gephi.datalab.api.datatables.DataTablesController;
import org.gephi.graph.api.HierarchicalGraph;
import org.gephi.graph.api.Node;
import org.gephi.visualization.spi.GraphContextMenuItem;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 */
@ServiceProvider(service = GraphContextMenuItem.class)
public class SelectInDataLaboratory extends BasicItem {

    private DataTablesController dtc;

    @Override
    public void setup(HierarchicalGraph graph, Node[] nodes) {
        this.nodes = nodes;
        dtc = Lookup.getDefault().lookup(DataTablesController.class);
        if(!dtc.isDataTablesReady()){
            dtc.prepareDataTables();
        }
    }

    public void execute() {
        dtc.setNodeTableSelection(nodes);
        dtc.selectNodesTable();
    }

    public String getName() {
        return NbBundle.getMessage(SelectInDataLaboratory.class, "GraphContextMenu_SelectInDataLaboratory");
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    public boolean canExecute() {
        return nodes.length >= 1 && dtc.isDataTablesReady();
    }

    public int getType() {
        return 400;
    }

    public int getPosition() {
        return 100;
    }

    public Icon getIcon() {
        return ImageUtilities.loadImageIcon("org/gephi/visualization/api/resources/table-select.png", false);
    }

    @Override
    public Integer getMnemonicKey() {
        return KeyEvent.VK_L;
    }
}
