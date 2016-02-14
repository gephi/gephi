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
package org.gephi.visualization;

import org.gephi.graph.api.Column;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Node;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.project.api.WorkspaceListener;
import org.gephi.visualization.api.VisualizationController;
import org.gephi.visualization.api.selection.SelectionManager;
import org.gephi.visualization.apiimpl.GraphDrawable;
import org.gephi.visualization.apiimpl.GraphIO;
import org.gephi.visualization.apiimpl.Scheduler;
import org.gephi.visualization.apiimpl.VizConfig;
import org.gephi.visualization.apiimpl.VizEventManager;
import org.gephi.visualization.bridge.DataBridge;
import org.gephi.visualization.events.StandardVizEventManager;
import org.gephi.visualization.opengl.AbstractEngine;
import org.gephi.visualization.opengl.CompatibilityEngine;
import org.gephi.visualization.scheduler.CompatibilityScheduler;
import org.gephi.visualization.screenshot.ScreenshotMaker;
import org.gephi.visualization.swing.GLAbstractListener;
import org.gephi.visualization.swing.GraphCanvas;
import org.gephi.visualization.swing.NewtGraphCanvas;
import org.gephi.visualization.swing.StandardGraphIO;
import org.gephi.visualization.text.TextManager;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Mathieu Bastian
 */
@ServiceProvider(service = VisualizationController.class)
public class VizController implements VisualizationController {

    //Singleton
    private static VizController instance;

    public VizController() {
    }

    public synchronized static VizController getInstance() {
        if (instance == null) {
            instance = (VizController) Lookup.getDefault().lookup(VisualizationController.class);
            instance.initInstances();
        }
        return instance;
    }
    //Architecture
    private GLAbstractListener drawable;
    private AbstractEngine engine;
    private Scheduler scheduler;
    private VizConfig vizConfig;
    private GraphIO graphIO;
    private VizEventManager vizEventManager;
    private GraphLimits limits;
    private DataBridge dataBridge;
    private TextManager textManager;
    private ScreenshotMaker screenshotMaker;
    private SelectionManager selectionManager;
    //Variable
    private VizModel currentModel;

    public void initInstances() {
        vizConfig = new VizConfig();
        graphIO = new StandardGraphIO();
        engine = new CompatibilityEngine();
        vizEventManager = new StandardVizEventManager();
        scheduler = new CompatibilityScheduler();
        limits = new GraphLimits();
        dataBridge = new DataBridge();
        textManager = new TextManager();
        screenshotMaker = new ScreenshotMaker();
        currentModel = new VizModel(true);
        selectionManager = new SelectionManager();

        if (vizConfig.isUseGLJPanel()) {
            //No more supported
        } else if (Utilities.isMac()) {
            drawable = createCanvas();
        } else {
            drawable = createNewtCanvas();
        }
        drawable.initArchitecture();
        engine.initArchitecture();
        ((CompatibilityScheduler) scheduler).initArchitecture();
        ((StandardGraphIO) graphIO).initArchitecture();
        dataBridge.initArchitecture();
        textManager.initArchitecture();
        screenshotMaker.initArchitecture();
        vizEventManager.initArchitecture();
        selectionManager.initArchitecture();

        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        pc.addWorkspaceListener(new WorkspaceListener() {
            @Override
            public void initialize(Workspace workspace) {
                if (workspace.getLookup().lookup(VizModel.class) == null) {
                    workspace.add(new VizModel(workspace));
                }
            }

            @Override
            public void select(Workspace workspace) {
                engine.reinit();
            }

            @Override
            public void unselect(Workspace workspace) {
            }

            @Override
            public void close(Workspace workspace) {
            }

            @Override
            public void disable() {
                engine.reinit();
            }
        });

        if (pc.getCurrentWorkspace() != null) {
            engine.reinit();
        }
    }

    public void refreshWorkspace() {
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        VizModel model = null;
        if (pc.getCurrentWorkspace() == null) {
            model = new VizModel(true);
        } else {
            model = pc.getCurrentWorkspace().getLookup().lookup(VizModel.class);
            if (model == null) {
                model = new VizModel(pc.getCurrentWorkspace());
                pc.getCurrentWorkspace().add(model);

            }
        }
        if (model != currentModel) {
            model.setListeners(currentModel.getListeners());
            model.getTextModel().setListeners(currentModel.getTextModel().getListeners());
            currentModel.setListeners(null);
            currentModel.getTextModel().setListeners(null);
            currentModel = model;
            currentModel.init();
        }
    }

    public void destroy() {
        engine.stopDisplay();
        drawable.destroy();
        engine = null;
        scheduler = null;
        graphIO = null;
        vizEventManager = null;
        dataBridge = null;
        textManager = null;
        screenshotMaker = null;
        selectionManager = null;
    }

    public void resetSelection() {
        if (selectionManager != null) {
            selectionManager.resetSelection();
        }
    }

    public void selectNode(Node node) {
        if (selectionManager != null) {
            selectionManager.selectNode(node);
        }
    }

    public void selectEdge(Edge edge) {
        if (selectionManager != null) {
            selectionManager.selectEdge(edge);
        }
    }

    @Override
    public void selectNodes(Node[] nodes) {
        if (selectionManager != null) {
            selectionManager.selectNodes(nodes);
        }
    }

    @Override
    public void selectEdges(Edge[] edges) {
        if (selectionManager != null) {
            selectionManager.selectEdges(edges);
        }
    }

    @Override
    public Column[] getEdgeTextColumns() {
        return new Column[0];
    }

    @Override
    public Column[] getNodeTextColumns() {
        return new Column[0];
    }

    public VizModel getVizModel() {
        return currentModel;
    }

    public GraphDrawable getDrawable() {
        return drawable;
    }

    public AbstractEngine getEngine() {
        return engine;
    }

    public GraphIO getGraphIO() {
        return graphIO;
    }

    public Scheduler getScheduler() {
        return scheduler;
    }

    public VizConfig getVizConfig() {
        return vizConfig;
    }

    public VizEventManager getVizEventManager() {
        return vizEventManager;
    }

    public GraphLimits getLimits() {
        return limits;
    }

    public DataBridge getDataBridge() {
        return dataBridge;
    }

    public TextManager getTextManager() {
        return textManager;
    }

    public ScreenshotMaker getScreenshotMaker() {
        return screenshotMaker;
    }

    public SelectionManager getSelectionManager() {
        return selectionManager;
    }

    public GraphCanvas createCanvas() {
        GraphCanvas canvas = new GraphCanvas();
        return canvas;
    }

    public NewtGraphCanvas createNewtCanvas() {
        NewtGraphCanvas canvas = new NewtGraphCanvas();
        return canvas;
    }

//
//    @Override
//    public AttributeColumn[] getNodeTextColumns() {
//        if (currentModel != null && currentModel.getTextModel() != null) {
//            TextModelImpl textModel = currentModel.getTextModel();
//            return textModel.getNodeTextColumns();
//        }
//        return new AttributeColumn[0];
//    }
//
//    @Override
//    public AttributeColumn[] getEdgeTextColumns() {
//        if (currentModel != null && currentModel.getTextModel() != null) {
//            TextModelImpl textModel = currentModel.getTextModel();
//            return textModel.getEdgeTextColumns();
//        }
//        return new AttributeColumn[0];
//    }
}
