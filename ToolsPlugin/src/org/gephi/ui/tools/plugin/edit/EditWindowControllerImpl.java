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
package org.gephi.ui.tools.plugin.edit;

import javax.swing.SwingUtilities;
import org.gephi.graph.api.Node;
import org.gephi.tools.api.EditWindowController;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.WindowManager;

/**
 * Implementation of EditWindowController interface of Tools API.
 * @author Eduardo Ramos <eduramiba@gmail.com>
 */
@ServiceProvider(service = EditWindowController.class)
public class EditWindowControllerImpl implements EditWindowController {

    public void openEditWindow() {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                EditToolTopComponent topComponent = EditToolTopComponent.findInstance();
                topComponent.open();
                topComponent.requestActive();
            }
        });

    }

    public void closeEditWindow() {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                EditToolTopComponent topComponent = EditToolTopComponent.findInstance();
                topComponent.disableEdit();
                topComponent.close();
            }
        });
    }

    public boolean isOpen(){
        EditToolTopComponent topComponent = EditToolTopComponent.findInstance();
        return topComponent.isOpened();
    }

    public void editNode(final Node node) {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                EditToolTopComponent topComponent = EditToolTopComponent.findInstance();
                topComponent.editNode(node);
            }
        });
    }

    public void disableEdit() {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                EditToolTopComponent topComponent = EditToolTopComponent.findInstance();
                topComponent.disableEdit();
            }
        });
    }
}
