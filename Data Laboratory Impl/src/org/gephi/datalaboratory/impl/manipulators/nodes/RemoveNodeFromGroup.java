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
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * Nodes manipulator that removes a node from its group if it has one. If the last node of the group is removed, breaks the group.
 * @author Eduardo Ramos <eduramiba@gmail.com>
 */
public class RemoveNodeFromGroup implements NodesManipulator {

    private Node[] nodes;

    public void setup(Node[] nodes, Node clickedNode) {
        this.nodes = nodes;
    }

    public void execute() {
        GraphElementsController gec = Lookup.getDefault().lookup(GraphElementsController.class);
        gec.removeNodesFromGroup(nodes);//At least 1 node is in a group. And we don't have to check now every node because the removeNodesFromGroup method does it for us.
    }

    public String getName() {
        if(nodes.length>1){
            return NbBundle.getMessage(RemoveNodeFromGroup.class, "RemoveNodeFromGroup.name.multiple");
        }else{
            return NbBundle.getMessage(RemoveNodeFromGroup.class, "RemoveNodeFromGroup.name.single");
        }
    }

    public String getDescription() {
        return "";
    }

    public boolean canExecute() {
        GraphElementsController gec = Lookup.getDefault().lookup(GraphElementsController.class);
        for (Node n : nodes) {
            if (gec.isNodeInGroup(n)) {
                return true;//If any of the nodes can be removed from its group, then allow to execute this action.
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
        return 300;
    }

    public Icon getIcon() {
        return null;
    }
}
