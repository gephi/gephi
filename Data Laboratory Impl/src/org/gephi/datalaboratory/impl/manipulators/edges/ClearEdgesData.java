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
package org.gephi.datalaboratory.impl.manipulators.edges;

import javax.swing.Icon;
import javax.swing.JOptionPane;
import org.gephi.datalaboratory.api.AttributeColumnsController;
import org.gephi.datalaboratory.api.DataTablesController;
import org.gephi.datalaboratory.api.GraphElementsController;
import org.gephi.datalaboratory.spi.ManipulatorUI;
import org.gephi.datalaboratory.spi.edges.EdgesManipulator;
import org.gephi.graph.api.Edge;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * Edges manipulator that clears all data of one or more edges except the id and computed attributes.
 * @author Eduardo Ramos <eduramiba@gmail.com>
 */
public class ClearEdgesData implements EdgesManipulator {

    private Edge[] edges;

    public void setup(Edge[] edges, Edge clickedEdge) {
        this.edges = edges;
    }

    public void execute() {
        if (JOptionPane.showConfirmDialog(null, NbBundle.getMessage(ClearEdgesData.class, "ClearEdgesData.confirmation.message"), getName(), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            AttributeColumnsController ac = Lookup.getDefault().lookup(AttributeColumnsController.class);
            ac.clearEdgesData(edges,null);
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
        return Lookup.getDefault().lookup(GraphElementsController.class).areEdgesInGraph(edges);
    }

    public ManipulatorUI getUI() {
        return null;
    }

    public int getType() {
        return 0;
    }

    public int getPosition() {
        return 500;
    }

    public Icon getIcon() {
        return ImageUtilities.loadImageIcon("org/gephi/datalaboratory/impl/manipulators/resources/clear-data.png", true);
    }
}
