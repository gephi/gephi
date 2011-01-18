/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>, Eduardo Ramos <eduramiba@gmail.com>
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
package org.gephi.visualization.apiimpl.contextmenuitems;

import java.awt.event.KeyEvent;
import javax.swing.Icon;
import org.gephi.datalab.api.DataLaboratoryHelper;
import org.gephi.datalab.spi.nodes.NodesManipulator;
import org.gephi.graph.api.HierarchicalGraph;
import org.gephi.graph.api.Node;
import org.gephi.visualization.spi.GraphContextMenuItem;
import org.openide.util.lookup.ServiceProvider;

/**
 * Tag Nodes context menu action, uses existing same Data Laboratory manipulator when available.
 */
@ServiceProvider(service = GraphContextMenuItem.class)
public class TagNodes implements GraphContextMenuItem {
    private NodesManipulator tagNodes;

    public TagNodes() {
        tagNodes = DataLaboratoryHelper.getDefault().getNodesManipulatorByName("TagNodes");
    }

    public void setup(HierarchicalGraph graph, Node[] nodes) {
        tagNodes.setup(nodes, null);
    }

    public void execute() {
        DataLaboratoryHelper.getDefault().executeManipulator(tagNodes);
    }

    public GraphContextMenuItem[] getSubItems() {
        return null;
    }

    public String getName() {
         return tagNodes != null ? tagNodes.getName() : null;
    }

    public String getDescription() {
        return null;
    }

    public boolean isAvailable() {
        return tagNodes != null;//Do not show tag nodes action if the TagNodes nodes manipulator does not exist
    }

    public boolean canExecute() {
        return tagNodes != null ? tagNodes.canExecute() : false;
    }

    public int getType() {
        return 400;
    }

    public int getPosition() {
        return 0;
    }

    public Icon getIcon() {
        return tagNodes != null ? tagNodes.getIcon() : null;
    }

    public Integer getMnemonicKey() {
        return KeyEvent.VK_T;
    }
}
