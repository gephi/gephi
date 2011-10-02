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

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.AWTEventListener;
import java.awt.event.KeyEvent;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import org.gephi.datalab.api.DataLaboratoryHelper;
import org.gephi.datalab.spi.ContextMenuItemManipulator;
import org.gephi.project.api.ProjectController;
import org.gephi.tools.api.ToolController;
import org.gephi.ui.utils.UIUtils;
import org.gephi.visualization.VizController;
import org.gephi.visualization.apiimpl.PropertiesBarAddon;
import org.gephi.visualization.opengl.AbstractEngine;
import org.gephi.visualization.swing.GraphDrawableImpl;
import org.gephi.project.api.Workspace;
import org.gephi.project.api.WorkspaceListener;
import org.gephi.visualization.bridge.DHNSEventBridge;
import org.gephi.visualization.spi.GraphContextMenuItem;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

final class GraphTopComponent extends TopComponent implements AWTEventListener {

    private static GraphTopComponent instance;
    /** path to the icon used by the component and its open action */
//    static final String ICON_PATH = "SET/PATH/TO/ICON/HERE";
    private static final String PREFERRED_ID = "GraphTopComponent";
    private AbstractEngine engine;
    private VizBarController vizBarController;
    private final DHNSEventBridge eventBridge;
    private Map<Integer, ContextMenuItemManipulator> keyActionMappings = new HashMap<Integer, ContextMenuItemManipulator>();

    private GraphTopComponent() {
        initComponents();

        setName(NbBundle.getMessage(GraphTopComponent.class, "CTL_GraphTopComponent"));
//        setToolTipText(NbBundle.getMessage(GraphTopComponent.class, "HINT_GraphTopComponent"));
//        setIcon(Utilities.loadImage(ICON_PATH, true));

        engine = VizController.getInstance().getEngine();
        eventBridge = (DHNSEventBridge) VizController.getInstance().getEventBridge();

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
        initKeyEventContextMenuActionMappings();
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

    private void initKeyEventContextMenuActionMappings() {
        mapItems(Lookup.getDefault().lookupAll(GraphContextMenuItem.class).toArray(new GraphContextMenuItem[0]));
    }

    private void mapItems(ContextMenuItemManipulator[] items) {
        Integer key;
        ContextMenuItemManipulator[] subItems;
        for (ContextMenuItemManipulator item : items) {
            key = item.getMnemonicKey();
            if (key != null) {
                if (!keyActionMappings.containsKey(key)) {
                    keyActionMappings.put(key, item);
                }
            }
            subItems = item.getSubItems();
            if (subItems != null) {
                mapItems(subItems);
            }
        }
    }

    /**
     * For attending Ctrl+Key events in graph window to launch context menu actions
     */
    public void eventDispatched(AWTEvent event) {
        KeyEvent evt = (KeyEvent) event;

        if (evt.getID() == KeyEvent.KEY_RELEASED && (evt.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) == KeyEvent.CTRL_DOWN_MASK) {
            final ContextMenuItemManipulator item = keyActionMappings.get(evt.getKeyCode());
            if (item != null) {
                ((GraphContextMenuItem) item).setup(eventBridge.getGraph(), eventBridge.getSelectedNodes());
                if (item.isAvailable() && item.canExecute()) {
                    DataLaboratoryHelper.getDefault().executeManipulator(item);
                }
                evt.consume();
            }
        }
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
    protected void componentActivated() {
        java.awt.Toolkit.getDefaultToolkit().addAWTEventListener(this, AWTEvent.KEY_EVENT_MASK);
    }

    @Override
    protected void componentDeactivated() {
        java.awt.Toolkit.getDefaultToolkit().removeAWTEventListener(this);
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
