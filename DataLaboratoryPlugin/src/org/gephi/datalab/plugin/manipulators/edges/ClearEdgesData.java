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
package org.gephi.datalab.plugin.manipulators.edges;

import java.util.ArrayList;
import javax.swing.Icon;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.datalab.api.AttributeColumnsController;
import org.gephi.datalab.api.DataTablesController;
import org.gephi.datalab.plugin.manipulators.GeneralColumnsChooser;
import org.gephi.datalab.plugin.manipulators.ui.GeneralChooseColumnsUI;
import org.gephi.datalab.spi.ManipulatorUI;
import org.gephi.datalab.spi.edges.EdgesManipulator;
import org.gephi.graph.api.Edge;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * Edges manipulator that clears the given columns data of one or more edges except the id and computed attributes.
 * @author Eduardo Ramos <eduramiba@gmail.com>
 */
public class ClearEdgesData implements EdgesManipulator, GeneralColumnsChooser {

    private Edge[] edges;
    private AttributeColumn[] columnsToClearData;

    public void setup(Edge[] edges, Edge clickedEdge) {
        this.edges = edges;
        AttributeColumnsController ac = Lookup.getDefault().lookup(AttributeColumnsController.class);
        ArrayList<AttributeColumn> columnsToClearDataList = new ArrayList<AttributeColumn>();
        for (AttributeColumn column : Lookup.getDefault().lookup(AttributeController.class).getModel().getEdgeTable().getColumns()) {
            if (ac.canClearColumnData(column)) {
                columnsToClearDataList.add(column);
            }
        }
        columnsToClearData = columnsToClearDataList.toArray(new AttributeColumn[0]);
    }

    public void execute() {
        if (columnsToClearData.length >= 0) {
            AttributeColumnsController ac = Lookup.getDefault().lookup(AttributeColumnsController.class);
            ac.clearEdgesData(edges, columnsToClearData);
            Lookup.getDefault().lookup(DataTablesController.class).refreshCurrentTable();
        }
    }

    public String getName() {
        if (edges.length > 1) {
            return NbBundle.getMessage(ClearEdgesData.class, "ClearEdgesData.name.multiple");
        } else {
            return NbBundle.getMessage(ClearEdgesData.class, "ClearEdgesData.name.single");
        }
    }

    public String getDescription() {
        return NbBundle.getMessage(ClearEdgesData.class, "ClearEdgesData.description");
    }

    public boolean canExecute() {
        return true;
    }

    public ManipulatorUI getUI() {
        return new GeneralChooseColumnsUI(NbBundle.getMessage(ClearEdgesData.class, "ClearEdgesData.ui.description"));
    }

    public int getType() {
        return 200;
    }

    public int getPosition() {
        return 0;
    }

    public Icon getIcon() {
        return ImageUtilities.loadImageIcon("org/gephi/datalab/plugin/manipulators/resources/clear-data.png", true);
    }

    public AttributeColumn[] getColumns() {
        return columnsToClearData;
    }

    public void setColumns(AttributeColumn[] columnsToClearData) {
        this.columnsToClearData = columnsToClearData;
    }
}
