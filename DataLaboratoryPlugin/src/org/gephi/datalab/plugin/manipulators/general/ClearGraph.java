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
package org.gephi.datalab.plugin.manipulators.general;

import javax.swing.Icon;
import javax.swing.JOptionPane;
import org.gephi.datalab.api.GraphElementsController;
import org.gephi.datalab.spi.ManipulatorUI;
import org.gephi.datalab.spi.general.GeneralActionsManipulator;
import org.gephi.graph.api.GraphController;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 * GeneralActionsManipulator that clears the entire graph, asking for confirmation.
 * @author Eduardo Ramos <eduramiba@gmail.com>
 */
@ServiceProvider(service=GeneralActionsManipulator.class)
public class ClearGraph implements GeneralActionsManipulator {

    public void execute() {
        if (JOptionPane.showConfirmDialog(null, NbBundle.getMessage(ClearGraph.class, "ClearGraph.dialog.text"), NbBundle.getMessage(ClearGraph.class, "ClearGraph.name"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION) {
            Lookup.getDefault().lookup(GraphController.class).getModel().getGraph().clear();
        }
    }

    public String getName() {
        return NbBundle.getMessage(ClearGraph.class, "ClearGraph.name");
    }

    public String getDescription() {
        return "";
    }

    public boolean canExecute() {
        return Lookup.getDefault().lookup(GraphElementsController.class).getNodesCount()>0;
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
        return ImageUtilities.loadImageIcon("org/gephi/datalab/plugin/manipulators/resources/eraser--minus.png", true);
    }
}
