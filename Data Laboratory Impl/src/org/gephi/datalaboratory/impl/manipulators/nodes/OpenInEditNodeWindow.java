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
import javax.swing.SwingUtilities;
import org.gephi.datalaboratory.api.GraphElementsController;
import org.gephi.datalaboratory.spi.ManipulatorUI;
import org.gephi.datalaboratory.spi.nodes.NodesManipulator;
import org.gephi.graph.api.Node;
import org.gephi.tools.api.EditWindowController;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Eduardo Ramos <eduramiba@gmail.com>
 */
public class OpenInEditNodeWindow implements NodesManipulator{
    private Node node;

    public void setup(Node[] nodes, Node clickedNode) {
        this.node=clickedNode;
    }

    public void execute() {
        EditWindowController edc=Lookup.getDefault().lookup(EditWindowController.class);
        edc.openEditWindow();
        edc.editNode(node);
    }

    public String getName() {
        return NbBundle.getMessage(OpenInEditNodeWindow.class, "OpenInEditNodeWindow.name");
    }

    public String getDescription() {
        return NbBundle.getMessage(OpenInEditNodeWindow.class, "OpenInEditNodeWindow.description");
    }

    public boolean canExecute() {
        return Lookup.getDefault().lookup(GraphElementsController.class).isNodeInGraph(node);
    }

    public ManipulatorUI getUI() {
        return null;
    }

    public int getType() {
        return -100;
    }

    public int getPosition() {
        return 0;
    }

    public Icon getIcon() {
        return ImageUtilities.loadImageIcon("org/gephi/datalaboratory/impl/manipulators/resources/edit.png", true);
    }

}
