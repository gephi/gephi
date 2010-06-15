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
import javax.swing.JOptionPane;
import org.gephi.datalaboratory.api.GraphElementsController;
import org.gephi.datalaboratory.spi.ManipulatorUI;
import org.gephi.datalaboratory.spi.nodes.NodesManipulator;
import org.gephi.graph.api.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Eduardo Ramos <eduramiba@gmail.com>
 */
public class DeleteNodes implements NodesManipulator {

    private Node[] nodes;

    public void setup(Node[] nodes, Node clickedNode) {
        this.nodes = nodes;
    }

    public void execute() {
        if (JOptionPane.showConfirmDialog(null, NbBundle.getMessage(DeleteNodes.class, "DeleteNodes.confirmation.message"), getName(), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            GraphElementsController gec = Lookup.getDefault().lookup(GraphElementsController.class);
            gec.deleteNodes(nodes);
        }
    }

    public String getName() {
        if (nodes.length > 1) {
            return NbBundle.getMessage(DeleteNodes.class, "DeleteNodes.name.multiple");
        } else {
            return NbBundle.getMessage(DeleteNodes.class, "DeleteNodes.name.single");
        }
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
        return 0;
    }

    public int getPosition() {
        return 100;
    }

    public Icon getIcon() {
        return ImageUtilities.loadImageIcon("org/gephi/datalaboratory/impl/manipulators/resources/cross.png", true);
    }
}
