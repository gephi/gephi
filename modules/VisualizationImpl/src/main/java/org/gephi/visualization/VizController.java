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

import java.util.Iterator;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Node;
import org.gephi.project.api.ProjectController;
import org.gephi.visualization.apiimpl.GraphIO;
import org.gephi.visualization.apiimpl.VizEventManager;
import org.gephi.visualization.config.VizCommander;
import org.gephi.visualization.apiimpl.VizConfig;
import org.gephi.visualization.events.StandardVizEventManager;
import org.gephi.visualization.api.objects.ModelClassLibrary;
import org.gephi.visualization.objects.StandardModelClassLibrary;
import org.gephi.visualization.opengl.AbstractEngine;
import org.gephi.visualization.opengl.compatibility.CompatibilityEngine;
import org.gephi.visualization.opengl.compatibility.CompatibilityScheduler;
import org.gephi.visualization.apiimpl.Scheduler;
import org.gephi.visualization.api.selection.SelectionManager;
import org.gephi.visualization.bridge.DHNSDataBridge;
import org.gephi.visualization.bridge.DHNSEventBridge;
import org.gephi.visualization.bridge.DataBridge;
import org.gephi.visualization.bridge.EventBridge;
import org.gephi.visualization.mode.ModeManager;
import org.gephi.visualization.screenshot.ScreenshotMaker;
import org.gephi.visualization.opengl.text.TextManager;
import org.gephi.visualization.swing.GraphDrawableImpl;
import org.gephi.visualization.swing.StandardGraphIO;
import org.gephi.project.api.Workspace;
import org.gephi.project.api.WorkspaceListener;
import org.gephi.visualization.api.VisualizationController;
import org.gephi.visualization.apiimpl.ModelImpl;
import org.gephi.visualization.opengl.text.TextModel;
import org.openide.util.Lookup;
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
    private GraphDrawableImpl drawable;
    private AbstractEngine engine;
    private Scheduler scheduler;
    private VizConfig vizConfig;
    private GraphIO graphIO;
    private VizEventManager vizEventManager;
    private ModelClassLibrary modelClassLibrary;
    private GraphLimits limits;
    private DataBridge dataBridge;
    private EventBridge eventBridge;
    private ModeManager modeManager;
    private TextManager textManager;
    private ScreenshotMaker screenshotMaker;
    private SelectionManager selectionManager;
    //Variable
    private VizModel currentModel;

    public void initInstances() {
        VizCommander commander = new VizCommander();

        vizConfig = new VizConfig();
        graphIO = new StandardGraphIO();
        engine = new CompatibilityEngine();
        vizEventManager = new StandardVizEventManager();
        scheduler = new CompatibilityScheduler();
        modelClassLibrary = new StandardModelClassLibrary();
        limits = new GraphLimits();
        dataBridge = new DHNSDataBridge();
        eventBridge = new DHNSEventBridge();
        //dataBridge = new TestDataBridge();
        modeManager = new ModeManager();
        textManager = new TextManager();
        screenshotMaker = new ScreenshotMaker();
        currentModel = new VizModel(true);
        selectionManager = new SelectionManager();

        if (vizConfig.isUseGLJPanel()) {
            drawable = commander.createPanel();
        } else {
            drawable = commander.createCanvas();
        }
        drawable.initArchitecture();
        engine.initArchitecture();
        ((CompatibilityScheduler) scheduler).initArchitecture();
        ((StandardGraphIO) graphIO).initArchitecture();
        dataBridge.initArchitecture();
        eventBridge.initArchitecture();
        modeManager.initArchitecture();
        textManager.initArchitecture();
        screenshotMaker.initArchitecture();
        vizEventManager.initArchitecture();
        selectionManager.initArchitecture();

        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        pc.addWorkspaceListener(new WorkspaceListener() {

            public void initialize(Workspace workspace) {
                workspace.add(new VizModel());
            }

            public void select(Workspace workspace) {
                engine.reinit();
                dataBridge.resetGraph();
            }

            public void unselect(Workspace workspace) {
            }

            public void close(Workspace workspace) {
            }

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
                model = new VizModel();
                pc.getCurrentWorkspace().add(model);
            }
        }
        if (model != currentModel) {
            model.setListeners(currentModel.getListeners());
            model.getTextModel().setListeners(currentModel.getTextModel().getListeners());
            currentModel.setListeners(null);
            currentModel.getTextModel().setListeners(null);
            currentModel = model;
            VizController.getInstance().getModelClassLibrary().getNodeClass().setCurrentModeler(currentModel.getNodeModeler());
            currentModel.init();
        }
    }

    public void destroy() {
        engine.stopDisplay();
        drawable.destroy();
        for (Iterator<ModelImpl> itr = engine.getOctree().getObjectIterator(AbstractEngine.CLASS_NODE); itr.hasNext();) {
            ModelImpl m = itr.next();
            m.cleanModel();
        }
        for (Iterator<ModelImpl> itr = engine.getOctree().getObjectIterator(AbstractEngine.CLASS_EDGE); itr.hasNext();) {
            ModelImpl m = itr.next();
            m.cleanModel();
        }
        engine = null;
        scheduler = null;
        graphIO = null;
        vizEventManager = null;
        modelClassLibrary = null;
        dataBridge = null;
        eventBridge = null;
        modeManager = null;
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

    public void selectNodes(Node[] nodes) {
        if (selectionManager != null) {
            selectionManager.selectNodes(nodes);
        }
    }

    public void selectEdges(Edge[] edges) {
        if (selectionManager != null) {
            selectionManager.selectEdges(edges);
        }
    }

    public VizModel getVizModel() {
        return currentModel;
    }

    public GraphDrawableImpl getDrawable() {
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

    public ModelClassLibrary getModelClassLibrary() {
        return modelClassLibrary;
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

    public EventBridge getEventBridge() {
        return eventBridge;
    }

    public ModeManager getModeManager() {
        return modeManager;
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

    public float getMetaEdgeScale() {
        if (currentModel != null) {
            return currentModel.getMetaEdgeScale();
        }
        return 1f;
    }

    public AttributeColumn[] getNodeTextColumns() {
        if (currentModel != null && currentModel.getTextModel() != null) {
            TextModel textModel = currentModel.getTextModel();
            return textModel.getNodeTextColumns();
        }
        return new AttributeColumn[0];
    }

    public AttributeColumn[] getEdgeTextColumns() {
        if (currentModel != null && currentModel.getTextModel() != null) {
            TextModel textModel = currentModel.getTextModel();
            return textModel.getEdgeTextColumns();
        }
        return new AttributeColumn[0];
    }
}
