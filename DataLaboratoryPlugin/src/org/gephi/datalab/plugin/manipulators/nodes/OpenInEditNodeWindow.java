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
package org.gephi.datalab.plugin.manipulators.nodes;

import javax.swing.Icon;
import org.gephi.datalab.spi.ManipulatorUI;
import org.gephi.datalab.spi.nodes.NodesManipulator;
import org.gephi.graph.api.Node;
import org.gephi.tools.api.EditWindowController;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * Opens the selected node(s) one or various in Edit window.
 * @author Eduardo Ramos <eduramiba@gmail.com>
 */
public class OpenInEditNodeWindow implements NodesManipulator {

    private Node[] nodes;

    public void setup(Node[] nodes, Node clickedNode) {
        this.nodes = nodes;
    }

    public void execute() {
        EditWindowController edc = Lookup.getDefault().lookup(EditWindowController.class);
        edc.openEditWindow();
        edc.editNodes(nodes);
    }

    public String getName() {
        if (nodes.length > 1) {
            return NbBundle.getMessage(OpenInEditNodeWindow.class, "OpenInEditNodeWindow.name.multiple");
        } else {
            return NbBundle.getMessage(OpenInEditNodeWindow.class, "OpenInEditNodeWindow.name");
        }
    }

    public String getDescription() {
        if (nodes.length > 1) {
            return NbBundle.getMessage(OpenInEditNodeWindow.class, "OpenInEditNodeWindow.description.multiple");
        } else {
            return NbBundle.getMessage(OpenInEditNodeWindow.class, "OpenInEditNodeWindow.description");
        }
    }

    public boolean canExecute() {
        return true;
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
        return ImageUtilities.loadImageIcon("org/gephi/datalab/plugin/manipulators/resources/edit.png", true);
    }
}
