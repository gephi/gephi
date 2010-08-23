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
package org.gephi.datalaboratory.impl.manipulators.edges;

import java.util.ArrayList;
import javax.swing.Icon;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.datalaboratory.api.AttributeColumnsController;
import org.gephi.datalaboratory.api.DataTablesController;
import org.gephi.datalaboratory.impl.manipulators.GeneralColumnsChooser;
import org.gephi.datalaboratory.impl.manipulators.ui.GeneralChooseColumnsUI;
import org.gephi.datalaboratory.spi.ManipulatorUI;
import org.gephi.datalaboratory.spi.edges.EdgesManipulator;
import org.gephi.graph.api.Edge;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * Edges manipulator that copies the given columns data of one edge to the other selected edges.
 * @author Eduardo Ramos <eduramiba@gmail.com>
 */
public class CopyEdgeDataToOtherEdges implements EdgesManipulator, GeneralColumnsChooser {

    private Edge clickedEdge;
    private Edge[] edges;
    private AttributeColumn[] columnsToCopyData;

    public void setup(Edge[] edges, Edge clickedEdge) {
        this.clickedEdge = clickedEdge;
        this.edges = edges;
        AttributeColumnsController ac = Lookup.getDefault().lookup(AttributeColumnsController.class);
        ArrayList<AttributeColumn> columnsToClearDataList = new ArrayList<AttributeColumn>();
        for (AttributeColumn column : Lookup.getDefault().lookup(AttributeController.class).getModel().getEdgeTable().getColumns()) {
            if (ac.canChangeColumnData(column)) {
                columnsToClearDataList.add(column);
            }
        }
        columnsToCopyData = columnsToClearDataList.toArray(new AttributeColumn[0]);
    }

    public void execute() {
        if (columnsToCopyData.length >= 0) {
            AttributeColumnsController ac = Lookup.getDefault().lookup(AttributeColumnsController.class);
            ac.copyEdgeDataToOtherEdges(clickedEdge, edges, columnsToCopyData);
            Lookup.getDefault().lookup(DataTablesController.class).refreshCurrentTable();
        }
    }

    public String getName() {
        return NbBundle.getMessage(CopyEdgeDataToOtherEdges.class, "CopyEdgeDataToOtherEdges.name");
    }

    public String getDescription() {
        return NbBundle.getMessage(CopyEdgeDataToOtherEdges.class, "CopyEdgeDataToOtherEdges.description");
    }

    public boolean canExecute() {
        return edges.length>1;//At least 2 edges to copy data from one to the other.
    }

    public ManipulatorUI getUI() {
        return new GeneralChooseColumnsUI(NbBundle.getMessage(CopyEdgeDataToOtherEdges.class, "CopyEdgeDataToOtherEdges.ui.description"));
    }

    public int getType() {
        return 100;
    }

    public int getPosition() {
        return 100;
    }

    public Icon getIcon() {
        return ImageUtilities.loadImageIcon("org/gephi/datalaboratory/impl/manipulators/resources/broom--arrow.png", true);
    }

    public AttributeColumn[] getColumns() {
        return columnsToCopyData;
    }

    public void setColumns(AttributeColumn[] columnsToClearData) {
        this.columnsToCopyData = columnsToClearData;
    }
}
