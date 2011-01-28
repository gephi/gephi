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
package org.gephi.visualization.component;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.io.Serializable;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import org.gephi.project.api.ProjectController;
import org.gephi.tools.api.ToolController;
import org.gephi.ui.utils.UIUtils;
import org.gephi.visualization.VizController;
import org.gephi.visualization.apiimpl.PropertiesBarAddon;
import org.gephi.visualization.opengl.AbstractEngine;
import org.gephi.visualization.swing.GraphDrawableImpl;
import org.gephi.project.api.Workspace;
import org.gephi.project.api.WorkspaceListener;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

final class GraphTopComponent extends TopComponent {

    private static GraphTopComponent instance;
    /** path to the icon used by the component and its open action */
//    static final String ICON_PATH = "SET/PATH/TO/ICON/HERE";
    private static final String PREFERRED_ID = "GraphTopComponent";
    private AbstractEngine engine;
    private VizBarController vizBarController;

    private GraphTopComponent() {
        initComponents();

        setName(NbBundle.getMessage(GraphTopComponent.class, "CTL_GraphTopComponent"));
//        setToolTipText(NbBundle.getMessage(GraphTopComponent.class, "HINT_GraphTopComponent"));
//        setIcon(Utilities.loadImage(ICON_PATH, true));

        engine = VizController.getInstance().getEngine();

        //Init
        initCollapsePanel();
        initToolPanels();
        final GraphDrawableImpl drawable = VizController.getInstance().getDrawable();

        //Request component activation and therefore initialize JOGL component
        WindowManager.getDefault().invokeWhenUIReady(new Runnable() {

            public void run() {
                open();
                SwingUtilities.invokeLater(new Runnable() {

                    public void run() {
                        requestActive();
                        add(drawable.getGraphComponent(), BorderLayout.CENTER);
                        remove(waitingLabel);
                    }
                });
            }
        });
        //remove(waitingLabel);
        //add(drawable.getGraphComponent(), BorderLayout.CENTER);
    }

    private void initCollapsePanel() {
        vizBarController = new VizBarController();
        if (VizController.getInstance().getVizConfig().isShowVizVar()) {
            collapsePanel.init(vizBarController.getToolbar(), vizBarController.getExtendedBar(), false);
        } else {
            collapsePanel.setVisible(false);
        }
    }
    private SelectionToolbar selectionToolbar;
    private ActionsToolbar actionsToolbar;
    private JComponent toolbar;
    private JComponent propertiesBar;
    private AddonsBar addonsBar;

    private void initToolPanels() {
        ToolController tc = Lookup.getDefault().lookup(ToolController.class);
        if (tc != null) {
            if (VizController.getInstance().getVizConfig().isToolbar()) {
                JPanel westPanel = new JPanel(new BorderLayout(0, 0));
                if (UIUtils.isAquaLookAndFeel()) {
                    westPanel.setBackground(UIManager.getColor("NbExplorerView.background"));
                }

                toolbar = tc.getToolbar();
                if (toolbar != null) {
                    westPanel.add(toolbar, BorderLayout.CENTER);
                }
                selectionToolbar = new SelectionToolbar();
                actionsToolbar = new ActionsToolbar();

                westPanel.add(selectionToolbar, BorderLayout.NORTH);
                westPanel.add(actionsToolbar, BorderLayout.SOUTH);
                add(westPanel, BorderLayout.WEST);
            }

            if (VizController.getInstance().getVizConfig().isPropertiesbar()) {
                JPanel northBar = new JPanel(new BorderLayout());
                if (UIUtils.isAquaLookAndFeel()) {
                    northBar.setBackground(UIManager.getColor("NbExplorerView.background"));
                }
                propertiesBar = tc.getPropertiesBar();
                if (propertiesBar != null) {
                    northBar.add(propertiesBar, BorderLayout.CENTER);
                }
                addonsBar = new AddonsBar();
                for (PropertiesBarAddon addon : Lookup.getDefault().lookupAll(PropertiesBarAddon.class)) {
                    addonsBar.add(addon.getComponent());
                }
                northBar.add(addonsBar, BorderLayout.EAST);
                add(northBar, BorderLayout.NORTH);
            }
        }

        //Workspace events
        ProjectController projectController = Lookup.getDefault().lookup(ProjectController.class);
        projectController.addWorkspaceListener(new WorkspaceListener() {

            public void initialize(Workspace workspace) {
            }

            public void select(Workspace workspace) {
                toolbar.setEnabled(true);
                propertiesBar.setEnabled(true);
                actionsToolbar.setEnabled(true);
                selectionToolbar.setEnabled(true);
                addonsBar.setEnabled(true);
            }

            public void unselect(Workspace workspace) {
            }

            public void close(Workspace workspace) {
            }

            public void disable() {
                toolbar.setEnabled(false);
                propertiesBar.setEnabled(false);
                actionsToolbar.setEnabled(false);
                selectionToolbar.setEnabled(false);
                addonsBar.setEnabled(false);
            }
        });

        boolean hasWorkspace = projectController.getCurrentWorkspace() != null;
        toolbar.setEnabled(hasWorkspace);
        propertiesBar.setEnabled(hasWorkspace);
        actionsToolbar.setEnabled(hasWorkspace);
        selectionToolbar.setEnabled(hasWorkspace);
        addonsBar.setEnabled(hasWorkspace);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        waitingLabel = new javax.swing.JLabel();
        collapsePanel = new org.gephi.visualization.component.CollapsePanel();

        setLayout(new java.awt.BorderLayout());

        waitingLabel.setBackground(new java.awt.Color(255, 255, 255));
        org.openide.awt.Mnemonics.setLocalizedText(waitingLabel, org.openide.util.NbBundle.getMessage(GraphTopComponent.class, "GraphTopComponent.waitingLabel.text")); // NOI18N
        waitingLabel.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        add(waitingLabel, java.awt.BorderLayout.CENTER);
        add(collapsePanel, java.awt.BorderLayout.PAGE_END);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private org.gephi.visualization.component.CollapsePanel collapsePanel;
    private javax.swing.JLabel waitingLabel;
    // End of variables declaration//GEN-END:variables

    /**
     * Gets default instance. Do not use directly: reserved for *.settings files only,
     * i.e. deserialization routines; otherwise you could get a non-deserialized instance.
     * To obtain the singleton instance, use {@link #findInstance}.
     */
    public static synchronized GraphTopComponent getDefault() {
        if (instance == null) {
            instance = new GraphTopComponent();
        }
        return instance;
    }

    /**
     * Obtain the GraphTopComponent instance. Never call {@link #getDefault} directly!
     */
    public static synchronized GraphTopComponent findInstance() {
        TopComponent win = WindowManager.getDefault().findTopComponent(PREFERRED_ID);
        if (win == null) {
            Logger.getLogger(GraphTopComponent.class.getName()).warning(
                    "Cannot find " + PREFERRED_ID + " component. It will not be located properly in the window system.");
            return getDefault();
        }
        if (win instanceof GraphTopComponent) {
            return (GraphTopComponent) win;
        }
        Logger.getLogger(GraphTopComponent.class.getName()).warning(
                "There seem to be multiple components with the '" + PREFERRED_ID
                + "' ID. That is a potential source of errors and unexpected behavior.");
        return getDefault();
    }

    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_ALWAYS;
    }

    @Override
    protected void componentShowing() {
        super.componentShowing();
        engine.startDisplay();
    }

    @Override
    protected void componentHidden() {
        super.componentHidden();
        engine.stopDisplay();
    }

    @Override
    public void componentOpened() {
    }

    @Override
    public void componentClosed() {
        engine.stopDisplay();
    }

    /** replaces this in object stream */
    @Override
    public Object writeReplace() {
        return new ResolvableHelper();
    }

    @Override
    protected String preferredID() {
        return PREFERRED_ID;
    }

    final static class ResolvableHelper implements Serializable {

        private static final long serialVersionUID = 1L;

        public Object readResolve() {
            return GraphTopComponent.getDefault();
        }
    }

    private static class AddonsBar extends JPanel {

        public AddonsBar() {
            super(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        }

        @Override
        public void setEnabled(final boolean enabled) {
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    for (Component c : getComponents()) {
                        c.setEnabled(enabled);
                    }
                }
            });
        }
    }
}
