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
package org.gephi.datalaboratory.impl.manipulators.nodes;

import java.util.ArrayList;
import javax.swing.Icon;
import javax.swing.JOptionPane;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.datalaboratory.api.AttributeColumnsController;
import org.gephi.datalaboratory.api.DataTablesController;
import org.gephi.datalaboratory.api.GraphElementsController;
import org.gephi.datalaboratory.impl.manipulators.GeneralClearRowData;
import org.gephi.datalaboratory.impl.manipulators.ui.GeneralClearRowDataUI;
import org.gephi.datalaboratory.spi.ManipulatorUI;
import org.gephi.datalaboratory.spi.nodes.NodesManipulator;
import org.gephi.graph.api.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * Nodes manipulator that clears the given columns data of one or more nodes except the id and computed attributes.
 * @author Eduardo Ramos <eduramiba@gmail.com>
 */
public class ClearNodesData implements NodesManipulator, GeneralClearRowData {

    private Node[] nodes;
    private AttributeColumn[] columnsToClearData;

    public void setup(Node[] nodes, Node clickedNode) {
        this.nodes = nodes;
        AttributeColumnsController ac = Lookup.getDefault().lookup(AttributeColumnsController.class);
        ArrayList<AttributeColumn> columnsToClearDataList = new ArrayList<AttributeColumn>();
        for (AttributeColumn column : Lookup.getDefault().lookup(AttributeController.class).getModel().getNodeTable().getColumns()) {
            if (ac.canClearColumnData(column)) {
                columnsToClearDataList.add(column);
            }
        }
        columnsToClearData = columnsToClearDataList.toArray(new AttributeColumn[0]);
    }

    public void execute() {
        if (columnsToClearData.length >= 0) {
            AttributeColumnsController ac = Lookup.getDefault().lookup(AttributeColumnsController.class);
            ac.clearNodesData(nodes, columnsToClearData);
            Lookup.getDefault().lookup(DataTablesController.class).refreshCurrentTable();
        }
    }

    public String getName() {
        if (nodes.length > 1) {
            return NbBundle.getMessage(ClearNodesData.class, "ClearNodesData.name.multiple");
        } else {
            return NbBundle.getMessage(ClearNodesData.class, "ClearNodesData.name.single");
        }
    }

    public String getDescription() {
        return NbBundle.getMessage(ClearNodesData.class, "ClearNodesData.description");
    }

    public boolean canExecute() {
        return Lookup.getDefault().lookup(GraphElementsController.class).areNodesInGraph(nodes);
    }

    public ManipulatorUI getUI() {
        return new GeneralClearRowDataUI();
    }

    public int getType() {
        return 0;
    }

    public int getPosition() {
        return 400;
    }

    public Icon getIcon() {
        return ImageUtilities.loadImageIcon("org/gephi/datalaboratory/impl/manipulators/resources/clear-data.png", true);
    }

    public AttributeColumn[] getColumnsToClearData() {
        return columnsToClearData;
    }

    public void setColumnsToClearData(AttributeColumn[] columnsToClearData) {
        this.columnsToClearData = columnsToClearData;
    }
}
