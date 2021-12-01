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

import org.gephi.graph.api.GraphModel;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.project.api.WorkspaceListener;
import org.gephi.tools.api.ToolController;
import org.gephi.ui.utils.UIUtils;
import org.gephi.visualization.VizController;
import org.gephi.viz.engine.VizEngine;
import org.gephi.viz.engine.VizEngineFactory;
import org.gephi.viz.engine.lwjgl.LWJGLRenderingTarget;
import org.gephi.viz.engine.lwjgl.LWJGLRenderingTargetAWT;
import org.gephi.viz.engine.lwjgl.VizEngineLWJGLConfigurator;
import org.gephi.viz.engine.lwjgl.pipeline.events.AWTEventsListener;
import org.gephi.viz.engine.lwjgl.pipeline.events.LWJGLInputEvent;
import org.gephi.viz.engine.spi.WorldUpdaterExecutionMode;
import org.gephi.viz.engine.util.gl.OpenGLOptions;
import org.lwjgl.opengl.awt.AWTGLCanvas;
import org.lwjgl.opengl.awt.GLData;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

import javax.swing.*;
import java.awt.*;
import java.awt.event.AWTEventListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@ConvertAsProperties(dtd = "-//org.gephi.visualization.component//Graph//EN",
        autostore = false)
@TopComponent.Description(preferredID = "GraphTopComponent",
        persistenceType = TopComponent.PERSISTENCE_ALWAYS)
@TopComponent.Registration(mode = "editor", openAtStartup = true, roles = {"overview"})
@ActionID(category = "Window", id = "org.gephi.visualization.component.GraphTopComponent")
@ActionReference(path = "Menu/Window", position = 500)
@TopComponent.OpenActionRegistration(displayName = "#CTL_GraphTopComponent",
        preferredID = "GraphTopComponent")
public class GraphTopComponent extends TopComponent implements AWTEventListener {

    private static final boolean DISABLE_INDIRECT_RENDERING = false;
    private static final boolean DISABLE_INSTANCED_RENDERING = false;
    private static final boolean DISABLE_VAOS = false;

    private static final boolean DEBUG = false;
    private static final boolean USE_OPENGL_ES = false;

    private static final WorldUpdaterExecutionMode UPDATE_DATA_MODE = WorldUpdaterExecutionMode.CONCURRENT_ASYNCHRONOUS;

    private transient final ExecutorService LAYOUT_THREAD_POOL = Executors.newSingleThreadExecutor();

    private transient VizBarController vizBarController;
    private SelectionToolbar selectionToolbar;
    private ActionsToolbar actionsToolbar;
    private JComponent toolbar;
    private JComponent propertiesBar;
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private org.gephi.visualization.component.CollapsePanel collapsePanel;
    private javax.swing.JLabel waitingLabel;

    public GraphTopComponent() {
        initComponents();

        setName(NbBundle.getMessage(GraphTopComponent.class, "CTL_GraphTopComponent"));

        initKeyEventContextMenuActionMappings();

        listenToWorkspaceEvents();
        initTools();
    }

    private void stopRendering(VizEngine engine) {
        if (engine != null) {
            engine.stop();
        }

        //TODO When you stop the program:
//        eventsListener.destroy();
//        stopEventListeners();
    }

    private void listenToWorkspaceEvents() {
        //Workspace events
        ProjectController projectController = Lookup.getDefault().lookup(ProjectController.class);
        projectController.addWorkspaceListener(new WorkspaceListener() {
            @Override
            public void initialize(Workspace workspace) {
            }

            @Override
            public void select(Workspace workspace) {
                if (toolbar != null) {
                    toolbar.setEnabled(true);
                }
                if (propertiesBar != null) {
                    propertiesBar.setEnabled(true);
                }
                if (actionsToolbar != null) {
                    actionsToolbar.setEnabled(true);
                }
                if (selectionToolbar != null) {
                    selectionToolbar.setEnabled(true);
                }

                activateWorkspaceVizEngine(workspace);
            }

            @Override
            public void unselect(Workspace workspace) {
            }

            @Override
            public void close(Workspace workspace) {
            }

            @Override
            public void disable() {
                if (toolbar != null) {
                    toolbar.setEnabled(false);
                }
                if (propertiesBar != null) {
                    propertiesBar.setEnabled(false);
                }
                if (actionsToolbar != null) {
                    actionsToolbar.setEnabled(false);
                }
                if (selectionToolbar != null) {
                    selectionToolbar.setEnabled(false);
                }
            }
        });

        boolean hasWorkspace = projectController.getCurrentWorkspace() != null;
        if (toolbar != null) {
            toolbar.setEnabled(hasWorkspace);
        }
        if (propertiesBar != null) {
            propertiesBar.setEnabled(hasWorkspace);
        }
        if (actionsToolbar != null) {
            actionsToolbar.setEnabled(hasWorkspace);
        }
        if (selectionToolbar != null) {
            selectionToolbar.setEnabled(hasWorkspace);
        }

        if (hasWorkspace) {
            activateWorkspaceVizEngine(projectController.getCurrentWorkspace());
        }
    }

    private void stopEventListeners() {
        LAYOUT_THREAD_POOL.shutdown();
    }

    private void initCollapsePanel() {
        vizBarController = new VizBarController();
        if (VizController.getInstance().getVizConfig().isShowVizVar()) {
            collapsePanel.init(vizBarController.getToolbar(), vizBarController.getExtendedBar(), false);
        } else {
            collapsePanel.setVisible(false);
        }
    }

    private void activateWorkspaceVizEngine(final Workspace workspace) {
        final GraphModel graphModel = workspace.getLookup().lookup(GraphModel.class);

        final GLData data = new GLData();

        if (USE_OPENGL_ES) {
            data.api = GLData.API.GLES;
        }

        data.samples = 4;//4 samples anti-aliasing
        data.swapInterval = 0;

        LWJGLRenderingTargetAWT renderingTarget = new LWJGLRenderingTargetAWT(null);


        final AWTGLCanvas glCanvas = new AWTGLCanvas(data) {
            @Override
            public void initGL() {
                renderingTarget.initializeContext();
            }

            @Override
            public void paintGL() {
                renderingTarget.display();
                swapBuffers();
            }
        };

        remove(waitingLabel);
        add(glCanvas, BorderLayout.CENTER);

        final VizEngine<LWJGLRenderingTarget, LWJGLInputEvent> engine = VizEngineFactory.newEngine(
                renderingTarget,
                graphModel,
                Collections.singletonList(
                        new VizEngineLWJGLConfigurator()
                )
        );

        final OpenGLOptions glOptions = engine.getLookup().lookup(OpenGLOptions.class);
        glOptions.setDisableIndirectDrawing(DISABLE_INDIRECT_RENDERING);
        glOptions.setDisableInstancedDrawing(DISABLE_INSTANCED_RENDERING);
        glOptions.setDisableVAOS(DISABLE_VAOS);
        glOptions.setDebug(DEBUG);

        engine.setWorldUpdatersExecutionMode(UPDATE_DATA_MODE);

//        if (eventsListener != null) {
//            eventsListener.destroy();
        //Destroy last workspace event listener
//        }

        AWTEventsListener eventsListener = new AWTEventsListener(glCanvas, engine);
        eventsListener.register();

        renderingTarget.setup(engine);

        engine.start();

        //                        this.addWindowListener(new WindowAdapter() {
//                            @Override
//                            public void windowClosed(WindowEvent e) {
//                                stopAll();
//                            }
//
//                            @Override
//                            public void windowActivated(WindowEvent e) {
//                                renderingTarget.reshape(glCanvas.getWidth(), glCanvas.getHeight());
//                            }
//
//                            @Override
//                            public void windowOpened(WindowEvent e) {
//                                renderingTarget.reshape(glCanvas.getWidth(), glCanvas.getHeight());
//                            }
//                        });
        glCanvas.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent event) {
                if (renderingTarget.getEngine() != null) {
                    renderingTarget.reshape(event.getComponent().getWidth(), event.getComponent().getHeight());
                }
            }
        });

        final Runnable renderLoop = new Runnable() {
            @Override
            public void run() {
                if (!renderingTarget.isRunning()) {
                    stopRendering(engine);
                    return;
                }

                if (glCanvas.isValid()) {
                    glCanvas.render();
                }
                SwingUtilities.invokeLater(this);
            }
        };
        SwingUtilities.invokeLater(renderLoop);
    }

    private void initKeyEventContextMenuActionMappings() {
//        mapItems(Lookup.getDefault().lookupAll(GraphContextMenuItem.class).toArray(new GraphContextMenuItem[0]));
    }

    private void initTools() {
        final ToolController tc = Lookup.getDefault().lookup(ToolController.class);
        if (tc != null) {
//            if (VizController.getInstance().getVizConfig().isToolbar()) {
//                JPanel westPanel = new JPanel(new BorderLayout(0, 0));
//                if (UIUtils.isAquaLookAndFeel()) {
//                    westPanel.setBackground(UIManager.getColor("NbExplorerView.background"));
//                }
//
//                toolbar = tc.getToolbar();
//                if (toolbar != null) {
//                    westPanel.add(toolbar, BorderLayout.CENTER);
//                }
//                selectionToolbar = new SelectionToolbar();
//                actionsToolbar = new ActionsToolbar();
//
//                westPanel.add(selectionToolbar, BorderLayout.NORTH);
//                westPanel.add(actionsToolbar, BorderLayout.SOUTH);
//                add(westPanel, BorderLayout.WEST);
//            }
//
//            if (VizController.getInstance().getVizConfig().isPropertiesbar()) {
//                propertiesBar = tc.getPropertiesBar();
//                if (propertiesBar != null) {
//                    add(propertiesBar, BorderLayout.NORTH);
//                }
//            }
            //TODO
        }

        final ProjectController projectController = Lookup.getDefault().lookup(ProjectController.class);
        projectController.addWorkspaceListener(new WorkspaceListener() {
            @Override
            public void initialize(Workspace workspace) {
            }

            @Override
            public void select(Workspace workspace) {
            }

            @Override
            public void unselect(Workspace workspace) {

            }

            @Override
            public void close(Workspace workspace) {

            }

            @Override
            public void disable() {
                if (tc != null) {
                    tc.select(null);//Unselect any selected tool
                }
            }
        });
    }

    /**
     * For attending Ctrl+Key events in graph window to launch context menu
     * actions
     */
    @Override
    public void eventDispatched(AWTEvent event) {
        KeyEvent evt = (KeyEvent) event;

        if (evt.getID() == KeyEvent.KEY_RELEASED &&
                (evt.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) == KeyEvent.CTRL_DOWN_MASK) {
//            final ContextMenuItemManipulator item = keyActionMappings.get(evt.getKeyCode());
//            if (item != null) {
//                ((GraphContextMenuItem) item).setup(eventBridge.getGraph(), eventBridge.getSelectedNodes());
//                if (item.isAvailable() && item.canExecute()) {
//                    DataLaboratoryHelper.getDefault().executeManipulator(item);
//                }
//                evt.consume();
//            }
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        waitingLabel = new javax.swing.JLabel();
        collapsePanel = new org.gephi.visualization.component.CollapsePanel();

        setLayout(new java.awt.BorderLayout());

        waitingLabel.setBackground(new java.awt.Color(255, 255, 255));
        org.openide.awt.Mnemonics.setLocalizedText(waitingLabel, org.openide.util.NbBundle
                .getMessage(GraphTopComponent.class, "GraphTopComponent.waitingLabel.text")); // NOI18N
        waitingLabel.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        add(waitingLabel, java.awt.BorderLayout.CENTER);
        add(collapsePanel, java.awt.BorderLayout.PAGE_END);
    }// </editor-fold>//GEN-END:initComponents
    // End of variables declaration//GEN-END:variables

    @Override
    protected void componentActivated() {
        java.awt.Toolkit.getDefaultToolkit().addAWTEventListener(this, AWTEvent.KEY_EVENT_MASK);
    }

    @Override
    protected void componentDeactivated() {
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
