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
package org.gephi.visualization.apiimpl;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JPopupMenu;
import org.gephi.visualization.VizController;
import org.gephi.visualization.bridge.DHNSEventBridge;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.ImageUtilities;
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
        GraphContextMenuAction groupAction = new GraphContextMenuImpl("GraphContextMenu_Group", "org/gephi/visualization/api/resources/group.png") {

            @Override
            public void actionPerformed(ActionEvent e) {
                eventBridge.group();
            }
        };
        groupAction.setEnabled(eventBridge.canGroup());

        //Ungroup
        GraphContextMenuAction ungroupAction = new GraphContextMenuImpl("GraphContextMenu_Ungroup", "org/gephi/visualization/api/resources/ungroup.png") {

            @Override
            public void actionPerformed(ActionEvent e) {
                eventBridge.ungroup();
            }
        };
        ungroupAction.setEnabled(eventBridge.canUngroup());

        //Expand
        GraphContextMenuAction expandAction = new GraphContextMenuImpl("GraphContextMenu_Expand", "org/gephi/visualization/api/resources/expand.png") {

            @Override
            public void actionPerformed(ActionEvent e) {
                eventBridge.expand();
            }
        };
        expandAction.setEnabled(eventBridge.canExpand());

        //Contract
        GraphContextMenuAction contractAction = new GraphContextMenuImpl("GraphContextMenu_Contract", "org/gephi/visualization/api/resources/contract.png") {

            @Override
            public void actionPerformed(ActionEvent e) {
                eventBridge.contract();
            }
        };
        contractAction.setEnabled(eventBridge.canContract());

        //Settle
        GraphContextMenuAction settleAction = new GraphContextMenuImpl("GraphContextMenu_Settle", "org/gephi/visualization/api/resources/settle.png") {

            @Override
            public void actionPerformed(ActionEvent e) {
                eventBridge.settle();
            }
        };
        settleAction.setEnabled(eventBridge.canSettle());

        //Free
        GraphContextMenuAction freeAction = new GraphContextMenuImpl("GraphContextMenu_Free") {

            @Override
            public void actionPerformed(ActionEvent e) {
                eventBridge.free();
            }
        };
        freeAction.setEnabled(eventBridge.canFree());

        //Free
        GraphContextMenuAction deleteAction = new GraphContextMenuImpl("GraphContextMenu_Delete") {

            @Override
            public void actionPerformed(ActionEvent e) {
                NotifyDescriptor.Confirmation notifyDescriptor = new NotifyDescriptor.Confirmation("Nodes will be deleted, do you want to proceed?", "Delete nodes", NotifyDescriptor.YES_NO_OPTION);
                if (DialogDisplayer.getDefault().notify(notifyDescriptor).equals(NotifyDescriptor.YES_OPTION)) {
                    eventBridge.delete();
                }
            }
        };
        deleteAction.setEnabled(eventBridge.canDelete());

        //Popup
        JPopupMenu popupMenu = new JPopupMenu();
        popupMenu.add(groupAction);
        popupMenu.add(ungroupAction);
        popupMenu.addSeparator();
        popupMenu.add(expandAction);
        popupMenu.add(contractAction);
        popupMenu.addSeparator();
        popupMenu.add(deleteAction);
        popupMenu.addSeparator();
        popupMenu.add(settleAction);
        popupMenu.add(freeAction);
        return popupMenu;
    }

    public static interface GraphContextMenuAction extends Action {

        public boolean isVisible();
    }

    private static class GraphContextMenuImpl extends AbstractAction implements GraphContextMenuAction {

        public GraphContextMenuImpl(String key) {
            putValue(Action.NAME, NbBundle.getMessage(GraphContextMenu.class, key));
        }

        public GraphContextMenuImpl(String key, String icon) {
            putValue(Action.NAME, NbBundle.getMessage(GraphContextMenu.class, key));
            putValue(Action.SMALL_ICON, ImageUtilities.loadImageIcon(icon, false));
        }

        public void actionPerformed(ActionEvent e) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public boolean isVisible() {
            return true;
        }
    }
}
