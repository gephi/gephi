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
import org.gephi.datalab.api.GraphElementsController;
import org.gephi.graph.api.HierarchicalGraph;
import org.gephi.graph.api.Node;
import org.gephi.visualization.spi.GraphContextMenuItem;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 */
@ServiceProvider(service=GraphContextMenuItem.class)
public class Group implements GraphContextMenuItem{
    private Node[] nodes;

    public void setup(HierarchicalGraph graph,Node[] nodes) {
        this.nodes=nodes;
    }

    public void execute() {
        GraphElementsController gec=Lookup.getDefault().lookup(GraphElementsController.class);
        gec.groupNodes(nodes);
    }

    public GraphContextMenuItem[] getSubItems() {
        return null;
    }

    public String getName() {
        return NbBundle.getMessage(Group.class, "GraphContextMenu_Group");
    }

    public String getDescription() {
        return null;
    }

    public boolean isAvailable() {
        return true;
    }

    public boolean canExecute() {
        return nodes.length>0;
    }

    public int getType() {
        return 0;
    }

    public int getPosition() {
        return 0;
    }

    public Icon getIcon() {
        return ImageUtilities.loadImageIcon("org/gephi/visualization/api/resources/group.png", false);
    }

    public Integer getMnemonicKey() {
        return KeyEvent.VK_G;
    }
}
