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
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * Nodes manipulator that breaks one or more selected groups.
 * @author Eduardo Ramos <eduramiba@gmail.com>
 */
public class Ungroup implements NodesManipulator {

    private Node[] nodes;

    public void setup(Node[] nodes, Node clickedNode) {
        this.nodes = nodes;
    }

    public void execute() {
        GraphElementsController gec = Lookup.getDefault().lookup(GraphElementsController.class);
        gec.ungroupNodes(nodes);//At least 1 node is a group. And we don't have to check now every node because the ungroupNodes method does it for us.
    }

    public String getName() {
        if (nodes.length > 1) {
            return NbBundle.getMessage(Ungroup.class, "Ungroup.name.multiple");
        } else {
            return NbBundle.getMessage(Ungroup.class, "Ungroup.name.single");
        }
    }

    public String getDescription() {
        return "";
    }

    public boolean canExecute() {
        GraphElementsController gec = Lookup.getDefault().lookup(GraphElementsController.class);
        for (Node n : nodes) {
            if (gec.canUngroupNode(n)) {
                return true;//If any of the nodes can be ungrouped, then allow to execute this action.
            }
        }
        return false;
    }

    public ManipulatorUI getUI() {
        return null;
    }

    public int getType() {
        return 100;
    }

    public int getPosition() {
        return 100;
    }

    public Icon getIcon() {
        return ImageUtilities.loadImageIcon("org/gephi/datalaboratory/impl/manipulators/resources/ungroup.png", true);
    }
}
