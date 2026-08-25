/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
Website : http://www.gephi.org

This file is part of Gephi.

DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

Copyright 2011 Gephi Consortium. All rights reserved.

The contents of this file are subject to the terms of either the GNU
General Public License Version 3 only ("GPL") or the Common
Development and Distribution License("CDDL") (collectively, the
"License"). You may not use this file except in compliance with the
License. You can obtain a copy of the License at
http://gephi.org/about/legal/license-notice/
or /cddl-1.0.txt and /gpl-3.0.txt. See the License for the
specific language governing permissions and limitations under the
License.  When distributing the software, include this License Header
Notice in each file and include the License files at
/cddl-1.0.txt and /gpl-3.0.txt. If applicable, add the following below the
License Header, with the fields enclosed by brackets [] replaced by
your own identifying information:
"Portions Copyrighted [year] [name of copyright owner]"

If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 3, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 3] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 3 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 3 code and therefore, elected the GPL
Version 3 license, then the option applies only if the new code is
made subject to such option by the copyright holder.

Contributor(s):

Portions Copyrighted 2011 Gephi Consortium.
 */

package org.gephi.desktop.banner.workspace;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.project.api.WorkspaceInformation;
import org.gephi.project.api.WorkspaceListener;
import org.gephi.project.api.WorkspaceProvider;
import org.gephi.ui.utils.UIUtils;
import org.netbeans.swing.tabcontrol.DefaultTabDataModel;
import org.netbeans.swing.tabcontrol.TabData;
import org.netbeans.swing.tabcontrol.TabDisplayer;
import org.netbeans.swing.tabcontrol.TabbedContainer;
import org.netbeans.swing.tabcontrol.WinsysInfoForTabbedContainer;
import org.netbeans.swing.tabcontrol.event.TabActionEvent;
import org.openide.awt.Actions;
import org.openide.util.Lookup;
import org.openide.windows.WindowManager;

public class WorkspacePanel extends javax.swing.JPanel implements PropertyChangeListener {

    private transient final DefaultTabDataModel tabDataModel;
    private transient final TabDisplayer tabbedContainer;

    /**
     * True while the panel is pushing the controller's state into the tab selection. Workspace
     * opening is asynchronous, so the current workspace can't be used to tell a user-initiated
     * selection from an echo of a model-driven one. EDT-confined.
     */
    private transient boolean applyingModelSelection;

    /**
     * Creates new form WorkspacePanel
     */
    public WorkspacePanel() {
        // Make the background the same as the parent component
        if (UIUtils.isFlatLafLightLookAndFeel()) {
            UIManager.put("EditorTab.background", Color.WHITE);
        }

        initComponents();

        // Init component
        tabDataModel = new DefaultTabDataModel();

        WinsysInfoForTabbedContainer ws = new WinsysInfoForTabbedContainer() {

            @Override
            public Object getOrientation(Component cmpnt) {
                return TabDisplayer.ORIENTATION_CENTER;
            }

            @Override
            public boolean inMaximizedMode(Component cmpnt) {
                return false;
            }

            @Override
            public boolean isTopComponentMaximizationEnabled() {
                return false;
            }
        };

        tabbedContainer = new TabDisplayer(tabDataModel, TabbedContainer.TYPE_EDITOR, ws);

        // Only needed because of the popup switcher (which doesn't go through the action system)
        tabbedContainer.getSelectionModel().addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                if (applyingModelSelection) {
                    return;
                }
                if (tabbedContainer.getSelectionModel().getSelectedIndex() != -1) {
                    TabData tabData = tabDataModel.getTab(tabbedContainer.getSelectionModel().getSelectedIndex());
                    // Deliberately not filtered on the controller's current workspace: opening is
                    // asynchronous, so that read is stale and would drop a selection made while a
                    // previous one is still in flight. applyingModelSelection above is what keeps
                    // the model-driven changes from coming back here.
                    openWorkspace((Workspace) tabData.getUserObject());
                }
            }
        });

        tabbedContainer.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                TabActionEvent tabActionEvent = (TabActionEvent) e;
                if (TabbedContainer.COMMAND_CLOSE.equals(tabActionEvent.getActionCommand())) {
                    TabData tabData = tabDataModel.getTab(tabActionEvent.getTabIndex());
                    Actions.forID("Workspace", "org.gephi.desktop.project.actions.DeleteWorkspace").actionPerformed(
                        new ActionEvent(tabData.getUserObject(), 0, null));

                    tabActionEvent.consume();
                } else if (TabbedContainer.COMMAND_CLOSE_ALL.equals(tabActionEvent.getActionCommand())) {
                    Actions.forID("File", "org.gephi.desktop.project.actions.CloseProject").actionPerformed(null);
                    tabActionEvent.consume();
                } else if (TabbedContainer.COMMAND_CLOSE_ALL_BUT_THIS.equals(tabActionEvent.getActionCommand())) {
                    TabData tabData = tabDataModel.getTab(tabActionEvent.getTabIndex());

                    Actions.forID("Workspace", "org.gephi.desktop.project.actions.DeleteOtherWorkspaces").actionPerformed(
                        new ActionEvent(tabData.getUserObject(), 0, null));

                    tabActionEvent.consume();
                } else if (TabbedContainer.COMMAND_SELECT.equals(tabActionEvent.getActionCommand())) {
                    TabData tabData = tabDataModel.getTab(tabActionEvent.getTabIndex());
                    openWorkspace((Workspace) tabData.getUserObject());
                    tabActionEvent.consume();
                }
            }
        });

        // Init listener
        WindowManager.getDefault().invokeWhenUIReady(new Runnable() {

            @Override
            public void run() {
                ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
                pc.addWorkspaceListener(createWorkspaceListener());
                refreshModel();
            }
        });
    }

    /**
     * Requests that <code>workspace</code> becomes the selected one, off the Event Dispatch Thread.
     * <p>
     * Routed through the action system rather than calling ProjectControllerUIImpl directly:
     * <code>org.gephi.desktop.project</code> is not one of the DesktopProject module's public
     * packages, so the NetBeans module classloader would refuse the access at runtime. The tab bar
     * already reaches its sibling workspace actions the same way.
     */
    private static void openWorkspace(Workspace workspace) {
        Action action = Actions.forID("Workspace", "org.gephi.desktop.project.actions.OpenWorkspace");
        if (action != null) {
            action.actionPerformed(new ActionEvent(workspace, 0, null));
        }
    }

    private void refreshModel() {
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        if (pc.getCurrentProject() != null) {
            WorkspaceProvider workspaceProvider = pc.getCurrentProject().getLookup().lookup(WorkspaceProvider.class);
            Workspace[] workspaces = workspaceProvider.getWorkspaces();
            if (workspaces.length > 0) {
                for (Workspace workspace : workspaces) {
                    int index = tabDataModel.size();
                    WorkspaceInformation workspaceInformation =
                        workspace.getLookup().lookup(WorkspaceInformation.class);
                    tabDataModel.addTab(index,
                        new TabData(workspace, null, workspaceInformation.getName(),
                            workspaceInformation.getSource()));
                    if (workspaceProvider.getCurrentWorkspace() == workspace) {
                        applyingModelSelection = true;
                        try {
                            tabbedContainer.getSelectionModel().setSelectedIndex(index);
                        } finally {
                            applyingModelSelection = false;
                        }
                        workspace.getLookup().lookup(WorkspaceInformation.class).addChangeListener(this);
                    }
                }

                add(tabbedContainer, BorderLayout.CENTER);
                getParent().revalidate();
                return;
            }
        }

        clearTabs();
    }

    /**
     * Empties the tab bar and detaches it. Must run on the EDT.
     */
    private void clearTabs() {
        applyingModelSelection = true;
        try {
            tabbedContainer.getSelectionModel().clearSelection();
            if (tabDataModel.size() > 0) {
                // removeTabs(int, int) treats its end index as exclusive, unless both bounds are
                // equal, in which case it removes the single tab at that index. Hence size() and
                // not size() - 1, and hence the guard, as removeTabs(0, 0) would be out of bounds
                tabDataModel.removeTabs(0, tabDataModel.size());
            }
        } finally {
            applyingModelSelection = false;
        }
        if (tabbedContainer.getParent() == this) {
            remove(tabbedContainer);
            getParent().revalidate();
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setLayout(new java.awt.BorderLayout());
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
    /**
     * Adapts the workspace events onto this panel.
     * <p>
     * The panel deliberately does not implement {@link WorkspaceListener} itself: it extends
     * <code>JPanel</code>, and <code>WorkspaceListener.disable()</code> would collide with the
     * inherited deprecated <code>java.awt.Component.disable()</code>. Before this indirection the
     * interface method silently bound to the inherited one, so closing a project greyed the tab bar
     * out instead of clearing it; overriding it instead would make every
     * <code>setEnabled(false)</code> on this panel clear the tabs. Keeping the two apart avoids both.
     *
     * @return a listener forwarding to this panel's own callbacks
     */
    private WorkspaceListener createWorkspaceListener() {
        return new WorkspaceListener() {

            @Override
            public void initialize(Workspace workspace) {
                workspaceInitialized(workspace);
            }

            @Override
            public void select(Workspace workspace) {
                workspaceSelected(workspace);
            }

            @Override
            public void unselect(Workspace workspace) {
                workspaceUnselected(workspace);
            }

            @Override
            public void close(Workspace workspace) {
                workspaceClosed(workspace);
            }

            @Override
            public void disable() {
                workspacesDisabled();
            }
        };
    }

    private void workspaceInitialized(final Workspace workspace) {
        final WorkspaceInformation workspaceInformation = workspace.getLookup().lookup(WorkspaceInformation.class);
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                tabDataModel.addTab(tabDataModel.size(), new TabData(workspace, null,
                    workspaceInformation.getName(),
                    workspaceInformation.getSource()));
                if (tabDataModel.size() == 1) {
                    add(tabbedContainer, BorderLayout.CENTER);
                    getParent().revalidate();
                }
            }
        });
    }

    private void workspaceSelected(final Workspace workspace) {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                for (int i = 0; i < tabDataModel.size(); i++) {
                    TabData tabData = tabDataModel.getTab(i);
                    if (tabData.getUserObject() == workspace) {
                        if (tabbedContainer.getSelectionModel().getSelectedIndex() != i) {
                            applyingModelSelection = true;
                            try {
                                tabbedContainer.getSelectionModel().setSelectedIndex(i);
                            } finally {
                                applyingModelSelection = false;
                            }
                        }
                        tabDataModel.setText(i, workspace.getName());
                        workspace.getLookup().lookup(WorkspaceInformation.class).addChangeListener(WorkspacePanel.this);
                        break;
                    }
                }
            }
        });
    }

    private void workspaceUnselected(Workspace workspace) {
        workspace.getLookup().lookup(WorkspaceInformation.class).removeChangeListener(this);
    }

    private void workspaceClosed(final Workspace workspace) {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                // Removing a tab can move the selection onto a neighbour, which would ask the
                // controller to select that workspace in the middle of tearing this one down
                applyingModelSelection = true;
                try {
                    for (int i = 0; i < tabDataModel.size(); i++) {
                        TabData tabData = tabDataModel.getTab(i);
                        if (tabData.getUserObject() == workspace) {
                            tabDataModel.removeTab(i);
                            break;
                        }
                    }
                    if (tabDataModel.size() == 0) {
                        tabbedContainer.getSelectionModel().clearSelection();
                    }
                } finally {
                    applyingModelSelection = false;
                }
                if (tabDataModel.size() == 0 && tabbedContainer.getParent() == WorkspacePanel.this) {
                    remove(tabbedContainer);
                    getParent().revalidate();
                }
            }
        });
    }

    private void workspacesDisabled() {
        // Clear unconditionally instead of re-reading the controller: when a project replaces
        // another one, the replacement is already current by the time this reaches the EDT, and
        // its own initialize() callbacks are queued behind us to fill the tab bar again.
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                clearTabs();
            }
        });
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(WorkspaceInformation.EVENT_RENAME)) {

            final WorkspaceInformation workspaceInformation = (WorkspaceInformation) evt.getSource();
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    for (int i = 0; i < tabDataModel.size(); i++) {
                        if (tabDataModel.getTab(i).getUserObject() instanceof Workspace) {
                            Workspace ws = (Workspace) tabDataModel.getTab(i).getUserObject();
                            if (ws.getLookup().lookup(WorkspaceInformation.class) == workspaceInformation) {
                                tabDataModel.setText(i, workspaceInformation.getName());
                                break;
                            }
                        }
                    }
                }
            });
        } else if (evt.getPropertyName().equals(WorkspaceInformation.EVENT_SET_SOURCE)) {

            final WorkspaceInformation workspaceInformation = (WorkspaceInformation) evt.getSource();
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    for (int i = 0; i < tabDataModel.size(); i++) {
                        if (tabDataModel.getTab(i).getUserObject() instanceof Workspace) {
                            Workspace ws = (Workspace) tabDataModel.getTab(i).getUserObject();
                            if (ws.getLookup().lookup(WorkspaceInformation.class) == workspaceInformation) {
                                tabDataModel.setToolTipTextAt(i, workspaceInformation.getSource());
                                break;
                            }
                        }
                    }
                }
            });
        }
    }
}
