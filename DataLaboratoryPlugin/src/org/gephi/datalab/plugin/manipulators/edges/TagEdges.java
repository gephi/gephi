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
import org.gephi.data.attributes.api.AttributeTable;
import org.gephi.datalab.api.AttributeColumnsController;
import org.gephi.datalab.api.DataTablesController;
import org.gephi.datalab.plugin.manipulators.GeneralColumnAndValueChooser;
import org.gephi.datalab.plugin.manipulators.ui.GeneralColumnAndValueChooserUI;
import org.gephi.datalab.spi.ManipulatorUI;
import org.gephi.datalab.spi.edges.EdgesManipulator;
import org.gephi.graph.api.Edge;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * Edges manipulator that fills the given column of multiple edges with a value.
 * @author Eduardo Ramos <eduramiba@gmail.com>
 */
public class TagEdges implements EdgesManipulator, GeneralColumnAndValueChooser {

    private Edge[] edges;
    private AttributeColumn column;
    private AttributeTable table;
    private AttributeColumn[] availableColumns;
    private String value;

    public void setup(Edge[] edges, Edge clickedEdge) {
        this.edges = edges;
        table = Lookup.getDefault().lookup(AttributeController.class).getModel().getEdgeTable();
        AttributeColumnsController ac = Lookup.getDefault().lookup(AttributeColumnsController.class);
        ArrayList<AttributeColumn> availableColumnsList = new ArrayList<AttributeColumn>();
        for (AttributeColumn c : table.getColumns()) {
            if (ac.canChangeColumnData(c)) {
                availableColumnsList.add(c);
            }
        }
        availableColumns = availableColumnsList.toArray(new AttributeColumn[0]);
    }

    public void execute() {
        if (column != null) {
            AttributeColumnsController ac = Lookup.getDefault().lookup(AttributeColumnsController.class);
            ac.fillEdgesColumnWithValue(edges, column, value);
            Lookup.getDefault().lookup(DataTablesController.class).refreshCurrentTable();
        }
    }

    public String getName() {
        if (edges.length > 1) {
            return NbBundle.getMessage(TagEdges.class, "TagEdges.name.multiple");
        } else {
            return NbBundle.getMessage(TagEdges.class, "TagEdges.name.single");
        }
    }

    public String getDescription() {
        return "";
    }

    public boolean canExecute() {
        return edges.length > 0;
    }

    public ManipulatorUI getUI() {
        return new GeneralColumnAndValueChooserUI();
    }

    public int getType() {
        return 200;
    }

    public int getPosition() {
        return 0;
    }

    public Icon getIcon() {
        return ImageUtilities.loadImageIcon("org/gephi/datalab/plugin/manipulators/resources/tag-label.png", true);
    }

    public AttributeColumn[] getColumns() {
        return availableColumns;
    }

    public void setColumn(AttributeColumn column) {
        this.column = column;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public AttributeTable getTable() {
        return table;
    }
}
