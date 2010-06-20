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
import org.gephi.datalaboratory.api.GraphElementsController;
import org.gephi.datalaboratory.spi.ManipulatorUI;
import org.gephi.datalaboratory.spi.nodes.NodesManipulator;
import org.gephi.graph.api.Node;
import org.gephi.visualization.VizController;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * Nodes manipulator that centers the graph view to show a node.
 * @author Eduardo Ramos <eduramiba@gmail.com>
 */
public class SelectOnGraph implements NodesManipulator {
    private Node node;

    public void setup(Node[] nodes, Node clickedNode) {
        this.node=clickedNode;
    }

    public void execute() {
        VizController.getInstance().getSelectionManager().centerOnNode(node);
    }

    public String getName() {
        return NbBundle.getMessage(SelectOnGraph.class, "SelectOnGraph.name");
    }

    public String getDescription() {
        return "";
    }

    public boolean canExecute() {
        return Lookup.getDefault().lookup(GraphElementsController.class).isNodeInGraph(node);
    }

    public ManipulatorUI getUI() {
        return null;
    }

    public int getType() {
        return 0;
    }

    public int getPosition() {
        return 0;
    }

    public Icon getIcon() {
        return null;
    }
}
