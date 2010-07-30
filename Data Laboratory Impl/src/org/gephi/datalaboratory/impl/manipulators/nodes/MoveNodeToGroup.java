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
import org.gephi.datalaboratory.impl.manipulators.nodes.ui.MoveNodeToGroupUI;
import org.gephi.datalaboratory.spi.ManipulatorUI;
import org.gephi.datalaboratory.spi.nodes.NodesManipulator;
import org.gephi.graph.api.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * Nodes manipulator that moves one or more nodes to a group. It shows an UI to select 1 of the available groups.
 * @author Eduardo Ramos <eduramiba@gmail.com>
 */
public class MoveNodeToGroup implements NodesManipulator {

    private Node[] nodes;
    private Node[] availableGroupsToMoveNodes;
    private Node group=null;

    public void setup(Node[] nodes, Node clickedNode) {
        this.nodes = nodes;
    }

    public void execute() {
        if (group != null) {
            GraphElementsController gec = Lookup.getDefault().lookup(GraphElementsController.class);
            gec.moveNodesToGroup(nodes, group);
        }
    }

    public String getName() {
        if (nodes.length > 1) {
            return NbBundle.getMessage(MoveNodeToGroup.class, "MoveNodeToGroup.name.multiple");
        } else {
            return NbBundle.getMessage(MoveNodeToGroup.class, "MoveNodeToGroup.name.single");
        }
    }

    public String getDescription() {
        return "";
    }

    /**
     * Can group nodes so it can be all moved to a group (at least 1 available)
     */
    public boolean canExecute() {
        GraphElementsController gec = Lookup.getDefault().lookup(GraphElementsController.class);
        boolean canGroup = gec.canGroupNodes(nodes);
        if (canGroup) {
            availableGroupsToMoveNodes = gec.getAvailableGroupsToMoveNodes(nodes);
            return availableGroupsToMoveNodes != null && availableGroupsToMoveNodes.length > 0;
        } else {
            return false;
        }
    }

    public ManipulatorUI getUI() {
        return new MoveNodeToGroupUI();
    }

    public int getType() {
        return 300;
    }

    public int getPosition() {
        return 300;
    }

    public Icon getIcon() {
        return null;
    }

    public Node[] getAvailableGroupsToMoveNodes() {
        return availableGroupsToMoveNodes;
    }

    public void setAvailableGroupsToMoveNodes(Node[] availableGroupsToMoveNodes) {
        this.availableGroupsToMoveNodes = availableGroupsToMoveNodes;
    }

    public Node getGroup() {
        return group;
    }

    public void setGroup(Node group) {
        this.group = group;
    }
}
