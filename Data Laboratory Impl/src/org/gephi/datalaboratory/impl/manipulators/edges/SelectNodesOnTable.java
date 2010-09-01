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

import javax.swing.Icon;
import org.gephi.datalaboratory.api.DataTablesController;
import org.gephi.datalaboratory.spi.ManipulatorUI;
import org.gephi.datalaboratory.spi.edges.EdgesManipulator;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * Edges manipulator that selects source and target node of an edge and selects them in nodes table.
 * @author Eduardo Ramos <eduramiba@gmail.com>
 */
public class SelectNodesOnTable implements EdgesManipulator{
    private Edge clickedEdge;

    public void setup(Edge[] edges, Edge clickedEdge) {
        this.clickedEdge=clickedEdge;
    }

    public void execute() {
        Node[] nodes=new Node[]{clickedEdge.getSource(),clickedEdge.getTarget()};
        DataTablesController dtc=Lookup.getDefault().lookup(DataTablesController.class);
        dtc.setNodeTableSelection(nodes);
        dtc.selectNodesTable();
    }

    public String getName() {
        return NbBundle.getMessage(SelectNodesOnTable.class, "SelectNodesOnTable.name");
    }

    public String getDescription() {
        return "";
    }

    public boolean canExecute() {
        return true;
    }

    public ManipulatorUI getUI() {
        return null;
    }

    public int getType() {
        return 100;
    }

    public int getPosition() {
        return 200;
    }

    public Icon getIcon() {
        return ImageUtilities.loadImageIcon("org/gephi/datalaboratory/impl/manipulators/resources/table-select-row.png", true);
    }

}
