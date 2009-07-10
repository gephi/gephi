/*
Copyright 2008 WebAtlas
Authors : Mathieu Bastian, Mathieu Jacomy, Julian Bilcke
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
package org.gephi.visualization.api;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import org.gephi.visualization.VizController;
import org.gephi.visualization.bridge.DHNSEventBridge;
import org.openide.util.NbBundle;

/**
 *
 * @author Mathieu Bastian
 */
public class GraphContextMenu {

    private VizConfig config;
    private DHNSEventBridge eventBridge;

    public GraphContextMenu() {
        config = VizController.getInstance().getVizConfig();
        eventBridge = (DHNSEventBridge) VizController.getInstance().getEventBridge();
    }

    public JPopupMenu getMenu() {
        //Group
        GraphContextMenuAction groupAction = new GraphContextMenuImpl("GraphContextMenu_Group") {

            @Override
            public void actionPerformed(ActionEvent e) {
            }
        };
        groupAction.setEnabled(eventBridge.canGroup());

        //Ungroup
        GraphContextMenuAction ungroupAction = new GraphContextMenuImpl("GraphContextMenu_Ungroup") {

            @Override
            public void actionPerformed(ActionEvent e) {
            }
        };
        ungroupAction.setEnabled(eventBridge.canUngroup());

        //Expand
        GraphContextMenuAction expandAction = new GraphContextMenuImpl("GraphContextMenu_Expand") {

            @Override
            public void actionPerformed(ActionEvent e) {
                eventBridge.expand();
            }
        };
        expandAction.setEnabled(eventBridge.canExpand());

        //Contract
        GraphContextMenuAction contractAction = new GraphContextMenuImpl("GraphContextMenu_Contract") {

            @Override
            public void actionPerformed(ActionEvent e) {
                eventBridge.contract();
            }
        };
        contractAction.setEnabled(eventBridge.canContract());

        //Popup
        JPopupMenu popupMenu = new JPopupMenu();
        popupMenu.add(groupAction);
        popupMenu.add(ungroupAction);
        popupMenu.addSeparator();
        popupMenu.add(expandAction);
        popupMenu.add(contractAction);
        return popupMenu;
    }

    public static interface GraphContextMenuAction extends Action {

        public boolean isVisible();
    }

    private static class GraphContextMenuImpl extends AbstractAction implements GraphContextMenuAction {

        public GraphContextMenuImpl(String key) {
            putValue(Action.NAME, NbBundle.getMessage(GraphContextMenu.class, key));
        }

        public void actionPerformed(ActionEvent e) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public boolean isVisible() {
            return true;
        }
    }
}
