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

import javax.swing.Icon;
import org.gephi.datalaboratory.api.DataTablesController;
import org.gephi.datalaboratory.api.GraphElementsController;
import org.gephi.datalaboratory.spi.ManipulatorUI;
import org.gephi.datalaboratory.spi.nodes.NodesManipulator;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * Nodes manipulator that selects in edges table all edges that have a node.
 * @author Eduardo Ramos <eduramiba@gmail.com>
 */
public class SelectEdgesOnTable implements NodesManipulator {

    private Node node;
    private Edge[] edges;

    public void setup(Node[] nodes, Node clickedNode) {
        this.node = clickedNode;
        if (Lookup.getDefault().lookup(GraphElementsController.class).isNodeInGraph(node)) {
            this.edges = Lookup.getDefault().lookup(GraphElementsController.class).getNodeEdges(node);
        }
    }

    public void execute() {
        DataTablesController dtc = Lookup.getDefault().lookup(DataTablesController.class);
        dtc.setEdgeTableSelection(edges);
        dtc.selectEdgesTable();
    }

    public String getName() {
        return NbBundle.getMessage(SelectEdgesOnTable.class, "SelectEdgesOnTable.name");
    }

    public String getDescription() {
        return "";
    }

    public boolean canExecute() {
        return edges!=null;//Do not enable if the node has no edges.
    }

    public ManipulatorUI getUI() {
        return null;
    }

    public int getType() {
        return 0;
    }

    public int getPosition() {
        return 200;
    }

    public Icon getIcon() {
        return null;
    }
}
