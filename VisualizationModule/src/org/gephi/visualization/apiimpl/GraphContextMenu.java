/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
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
package org.gephi.visualization.apiimpl;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.project.api.WorkspaceInformation;
import org.gephi.project.api.WorkspaceProvider;
import org.gephi.visualization.VizController;
import org.gephi.visualization.bridge.DHNSEventBridge;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
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
                NotifyDescriptor.Confirmation notifyDescriptor = new NotifyDescriptor.Confirmation(
                        NbBundle.getMessage(GraphContextMenu.class, "GraphContextMenu.Delete.message"),
                        NbBundle.getMessage(GraphContextMenu.class, "GraphContextMenu.Delete.message.title"), NotifyDescriptor.YES_NO_OPTION);
                if (DialogDisplayer.getDefault().notify(notifyDescriptor).equals(NotifyDescriptor.YES_OPTION)) {
                    eventBridge.delete();
                }
            }
        };
        deleteAction.setEnabled(eventBridge.canDelete());

        //Move workspace
        JMenu moveToWorkspaceMenu = new JMenu(NbBundle.getMessage(GraphContextMenu.class, "GraphContextMenu_MoveToWorkspace"));
        boolean moveOrCopyEnabled = eventBridge.canMoveOrCopyWorkspace();
        if (moveOrCopyEnabled) {
            moveToWorkspaceMenu.add(new GraphContextMenuImpl("GraphContextMenu_MoveToWorkspace_NewWorkspace", "org/gephi/visualization/api/resources/new-wokspace.png") {

                @Override
                public void actionPerformed(ActionEvent e) {
                    eventBridge.moveToNewWorkspace();
                }
            });
            moveToWorkspaceMenu.addSeparator();
            ProjectController projectController = Lookup.getDefault().lookup(ProjectController.class);
            for (final Workspace w : projectController.getCurrentProject().getLookup().lookup(WorkspaceProvider.class).getWorkspaces()) {
                JMenuItem item = new JMenuItem(w.getLookup().lookup(WorkspaceInformation.class).getName());
                item.addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent e) {
                        eventBridge.moveToWorkspace(w);
                    }
                });
                moveToWorkspaceMenu.add(item);
                item.setEnabled(w != projectController.getCurrentWorkspace());
            }
        }
        moveToWorkspaceMenu.setEnabled(moveOrCopyEnabled);

        //Copy workspace
        JMenu copyToWorkspaceMenu = new JMenu(NbBundle.getMessage(GraphContextMenu.class, "GraphContextMenu_CopyToWorkspace"));
        if (moveOrCopyEnabled) {
            copyToWorkspaceMenu.add(new GraphContextMenuImpl("GraphContextMenu_CopyToWorkspace_NewWorkspace", "org/gephi/visualization/api/resources/new-wokspace.png") {

                @Override
                public void actionPerformed(ActionEvent e) {
                    eventBridge.copyToNewWorkspace();
                }
            });
            copyToWorkspaceMenu.addSeparator();
            ProjectController projectController = Lookup.getDefault().lookup(ProjectController.class);
            for (final Workspace w : projectController.getCurrentProject().getLookup().lookup(WorkspaceProvider.class).getWorkspaces()) {
                JMenuItem item = new JMenuItem(w.getLookup().lookup(WorkspaceInformation.class).getName());
                item.addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent e) {
                        eventBridge.copyToWorkspace(w);
                    }
                });
                copyToWorkspaceMenu.add(item);
                item.setEnabled(w != projectController.getCurrentWorkspace());
            }
        }
        copyToWorkspaceMenu.setEnabled(moveOrCopyEnabled);

        //Popup
        JPopupMenu popupMenu = new JPopupMenu();
        popupMenu.add(groupAction);
        popupMenu.add(ungroupAction);
        popupMenu.addSeparator();
        popupMenu.add(expandAction);
        popupMenu.add(contractAction);
        popupMenu.addSeparator();
        popupMenu.add(deleteAction);
        popupMenu.add(moveToWorkspaceMenu);
        popupMenu.add(copyToWorkspaceMenu);
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
