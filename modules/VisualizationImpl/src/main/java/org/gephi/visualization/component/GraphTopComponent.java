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

package org.gephi.visualization.component;

import java.awt.*;
import java.awt.event.AWTEventListener;
import java.awt.event.KeyEvent;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.swing.*;

import org.gephi.desktop.visualization.collapse.CollapseGroup;
import org.gephi.desktop.visualization.collapse.CollapsePanel;
import org.gephi.desktop.visualization.collapse.EdgeGroup;
import org.gephi.desktop.visualization.collapse.GlobalGroup;
import org.gephi.desktop.visualization.collapse.LabelGroup;
import org.gephi.desktop.visualization.collapse.NodeGroup;
import org.gephi.desktop.visualization.collapse.VizExtendedBar;
import org.gephi.desktop.visualization.collapse.VizToolbar;
import org.gephi.desktop.visualization.tools.DesktopToolController;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.project.api.WorkspaceListener;
 
import org.gephi.visualization.VizController;
import org.gephi.visualization.VizModel;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.modules.OnStop;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

@ConvertAsProperties(dtd = "-//org.gephi.visualization.component//Graph//EN",
    autostore = false)
@TopComponent.Description(preferredID = "GraphTopComponent",
    iconBase = "VisualizationImpl/graph.svg",
    persistenceType = TopComponent.PERSISTENCE_ALWAYS)
@TopComponent.Registration(mode = "editor", openAtStartup = true, roles = {"overview"})
@ActionID(category = "Window", id = "org.gephi.visualization.component.GraphTopComponent")
@ActionReference(path = "Menu/Window", position = 500)
@TopComponent.OpenActionRegistration(displayName = "#CTL_GraphTopComponent",
    preferredID = "GraphTopComponent")
public class GraphTopComponent extends TopComponent implements AWTEventListener {

    private final VizController controller;
    private final SelectionToolbar selectionToolbar;
    private final ActionsToolbar actionsToolbar;
    private final JComponent toolbar;
    private final PropertiesBar propertiesBar;
    private final CollapseGroup[] groups;
    // Variables declaration - do not modify
    private CollapsePanel collapsePanel;
    private javax.swing.JLabel waitingLabel;
    // End of variables declaration//GEN-END:variables

    private final ScheduledExecutorService vizExecutor;

    public GraphTopComponent() {
        controller = Lookup.getDefault().lookup(VizController.class);
        setName(NbBundle.getMessage(GraphTopComponent.class, "CTL_GraphTopComponent"));
        initComponents();
        initKeyEventContextMenuActionMappings();

        vizExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "GraphTopComponent-VizExecutor");
            t.setDaemon(true);
            return t;
        });

        // Create groups
        groups = createCollapseGroups();

        // Create toolbars
        selectionToolbar = new SelectionToolbar();
        actionsToolbar = new ActionsToolbar();
        propertiesBar = new PropertiesBar();

        // Create the toolbar
        final DesktopToolController tc = Lookup.getDefault().lookup(DesktopToolController.class);
        toolbar = tc.getToolbar();
        JComponent toolsPropertiesBar = tc.getPropertiesBar();
        propertiesBar.addToolsPropertiesBar(toolsPropertiesBar);

        listenToWorkspaceEvents();

        SwingUtilities.invokeLater(() -> {
            // Create the collapse panel
            collapsePanel.init(new VizToolbar(groups), new VizExtendedBar(groups), false);

            // Create the toolbar
            initToolPanels();
        });
    }

    private CollapseGroup[] createCollapseGroups() {
        CollapseGroup[] groups = new CollapseGroup[4];
        groups[0] = new GlobalGroup();
        groups[1] = new NodeGroup();
        groups[2] = new EdgeGroup();
        groups[3] = new LabelGroup();

        // Disable all groups
        for (CollapseGroup group : groups) {
            group.disable();
        }
        return groups;
    }

    private void listenToWorkspaceEvents() {
        remove(waitingLabel);

        //Workspace events
        ProjectController projectController = Lookup.getDefault().lookup(ProjectController.class);
        projectController.addWorkspaceListener(new WorkspaceListener() {
            @Override
            public void initialize(Workspace workspace) {
            }

            @Override
            public void select(Workspace workspace) {
                activateWorkspaceVizEngine(workspace);
            }

            @Override
            public void unselect(Workspace workspace) {
                deactivateWorkspaceVizEngine(workspace);
            }

            @Override
            public void close(Workspace workspace) {
                deactivateWorkspaceVizEngine(workspace);
            }

            @Override
            public void disable() {
                SwingUtilities.invokeLater(() -> {
                    toolbar.setEnabled(false);
                    propertiesBar.setEnabled(false);
                    actionsToolbar.setEnabled(false);
                    selectionToolbar.setEnabled(false);
                });
            }
        });

        final boolean hasWorkspace = projectController.getCurrentWorkspace() != null;
        if (hasWorkspace) {
            activateWorkspaceVizEngine(projectController.getCurrentWorkspace());
        }
    }

    private void deactivateWorkspaceVizEngine(final Workspace workspace) {
        vizExecutor.schedule(() -> doDeactivateWorkspaceVizEngine(workspace), 0, TimeUnit.MILLISECONDS);
    }

    private void doDeactivateWorkspaceVizEngine(final Workspace workspace) {
        if (workspace == null) {
            return;
        }
        VizModel vizModel = controller.getModel(workspace);
        SwingUtilities.invokeLater(() -> {
            for (CollapseGroup group : groups) {
                group.unsetup(vizModel);
            }
            selectionToolbar.unsetup(vizModel);
            propertiesBar.unsetup();
        });
        vizModel.destroy(this);
    }

    private void activateWorkspaceVizEngine(final Workspace workspace) {
        vizExecutor.schedule(() -> doActivateWorkspaceVizEngine(workspace), 0, TimeUnit.MILLISECONDS);
    }

    private void doActivateWorkspaceVizEngine(final Workspace workspace) {
        if (workspace == null) {
            return;
        }
        VizModel vizModel = controller.getModel(workspace);
        vizModel.init(this);
        SwingUtilities.invokeLater(() -> {
            toolbar.setEnabled(true);
            propertiesBar.setEnabled(true);
            actionsToolbar.setEnabled(true);
            selectionToolbar.setEnabled(true);

            for (CollapseGroup group : groups) {
                group.setup(vizModel);
            }
            selectionToolbar.setup(vizModel);
            propertiesBar.setup(vizModel);
        });
    }

    private void initKeyEventContextMenuActionMappings() {
//        mapItems(Lookup.getDefault().lookupAll(GraphContextMenuItem.class).toArray(new GraphContextMenuItem[0]));
    }

    private void initToolPanels() {
        JPanel westPanel = new JPanel(new BorderLayout(0, 0));
        westPanel.add(toolbar, BorderLayout.CENTER);
        westPanel.add(selectionToolbar, BorderLayout.NORTH);
        westPanel.add(actionsToolbar, BorderLayout.SOUTH);
        add(propertiesBar, BorderLayout.NORTH);

        add(westPanel, BorderLayout.WEST);

//        final ProjectController projectController = Lookup.getDefault().lookup(ProjectController.class);
//        projectController.addWorkspaceListener(new WorkspaceListener() {
//            @Override
//            public void initialize(Workspace workspace) {
//            }
//
//            @Override
//            public void select(Workspace workspace) {
//            }
//
//            @Override
//            public void unselect(Workspace workspace) {
//
//            }
//
//            @Override
//            public void close(Workspace workspace) {
//
//            }
//
//            @Override
//            public void disable() {
//                if (tc != null) {
//                    tc.select(null);//Unselect any selected tool
//                }
//            }
//        });
    }

    /**
     * For attending Ctrl+Key events in graph window to launch context menu actions
     */
    @Override
    public void eventDispatched(AWTEvent event) {
        KeyEvent evt = (KeyEvent) event;

        if (evt.getID() == KeyEvent.KEY_RELEASED
            && (evt.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) == KeyEvent.CTRL_DOWN_MASK) {
//            final ContextMenuItemManipulator item = keyActionMappings.get(evt.getKeyCode());
//            if (item != null) {
//                ((GraphContextMenuItem) item).setup(eventBridge.getGraph(), eventBridge.getSelectedNodes());
//                if (item.isAvailable() && item.canExecute()) {
//                    DataLaboratoryHelper.getDefault().executeManipulator(item);
//                }
//                evt.consume();
//            }
            //TODO
        }
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        waitingLabel = new javax.swing.JLabel();
        collapsePanel = new CollapsePanel();

        setLayout(new java.awt.BorderLayout());

        waitingLabel.setBackground(new java.awt.Color(255, 255, 255));
        org.openide.awt.Mnemonics.setLocalizedText(waitingLabel, org.openide.util.NbBundle
            .getMessage(GraphTopComponent.class, "GraphTopComponent.waitingLabel.text")); // NOI18N
        waitingLabel.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        add(waitingLabel, java.awt.BorderLayout.CENTER);
        add(collapsePanel, java.awt.BorderLayout.PAGE_END);
    }// </editor-fold>//GEN-END:initComponents

    @Override
    protected void componentActivated() {
        super.componentActivated();
        java.awt.Toolkit.getDefaultToolkit().addAWTEventListener(this, AWTEvent.KEY_EVENT_MASK);
    }

    @Override
    protected void componentClosed() {
        super.componentClosed();
        deactivateWorkspaceVizEngine(
            Lookup.getDefault().lookup(ProjectController.class)
                .getCurrentWorkspace()
        );

        // Note: we cannot shutdown the vizExecutor here because the TopComponent
        // can be later reused/reactivated by the netbeans platform because persistenceType = PERSISTENCE_ALWAYS
    }

    public void shutdown() {
        vizExecutor.shutdown();
    }

    @Override
    protected void componentHidden() {
        super.componentHidden();
        deactivateWorkspaceVizEngine(
            Lookup.getDefault().lookup(ProjectController.class)
                .getCurrentWorkspace()
        );
    }

    @Override
    protected void componentShowing() {
        super.componentShowing();
        activateWorkspaceVizEngine(
            Lookup.getDefault().lookup(ProjectController.class)
                .getCurrentWorkspace()
        );
    }

    @Override
    protected void componentOpened() {
        super.componentOpened();
        activateWorkspaceVizEngine(
            Lookup.getDefault().lookup(ProjectController.class)
                .getCurrentWorkspace()
        );
    }

    @Override
    protected void componentDeactivated() {
        super.componentDeactivated();
        java.awt.Toolkit.getDefaultToolkit().removeAWTEventListener(this);
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // TODO store your settings
    }

    void readProperties(java.util.Properties p) {
        String version = p.getProperty("version");
        // TODO read your settings according to their version
    }
}
