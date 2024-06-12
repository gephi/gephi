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

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.gephi.graph.api.Column;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Node;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.visualization.api.VisualizationController;
import org.gephi.visualization.api.selection.SelectionManager;
import org.gephi.visualization.apiimpl.VizConfig;
import org.gephi.visualization.apiimpl.VizEventManager;
import org.gephi.visualization.events.StandardVizEventManager;
import org.gephi.visualization.screenshot.ScreenshotMaker;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 * @author Mathieu Bastian
 */
@ServiceProvider(service = VisualizationController.class)
public class VizController implements VisualizationController {

    //Singleton
    private static VizController instance;
    private CurrentWorkspaceVizEngine currentWorkspaceVizEngine;
    //Architecture
    private VizEventManager vizEventManager;
    private VizConfig vizConfig;
    private ScreenshotMaker screenshotMaker;
    private SelectionManager selectionManager;

    private VizModel currentModel;

    public VizController() {
    }

    public synchronized static VizController getInstance() {
        if (instance == null) {
            instance = (VizController) Lookup.getDefault().lookup(VisualizationController.class);
            instance.initInstances();
        }
        return instance;
    }

    public VizModel getModel() {
        final ProjectController projectController = Lookup.getDefault().lookup(ProjectController.class);
        final Workspace workspace = projectController.getCurrentWorkspace();

        if (workspace != null) {
            return workspace.getLookup().lookup(VizModel.class);
        }

        return null;
    }

    public void initInstances() {
        currentWorkspaceVizEngine = Lookup.getDefault().lookup(CurrentWorkspaceVizEngine.class);

        vizConfig = new VizConfig();
        vizEventManager = new StandardVizEventManager();
        screenshotMaker = new ScreenshotMaker();
//        limits = new GraphLimits();
//        textManager = new TextManager();
        currentModel = null;
        selectionManager = new SelectionManager();

        //TODO
//        textManager.initArchitecture();
        screenshotMaker.initArchitecture();
        vizEventManager.initArchitecture();
        selectionManager.initArchitecture();
    }

    public void refreshWorkspace() {
        final ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        final Workspace currentWorkspace = pc.getCurrentWorkspace();

        VizModel model = null;
        if (currentWorkspace != null) {
            model = currentWorkspace.getLookup().lookup(VizModel.class);
            if (model == null) {
                model = new VizModel(currentWorkspace);
                currentWorkspace.add(model);
            }
        }

        currentModel = model;
        if (currentModel != null) {
            currentModel.init();
        }
    }

    public void destroy() {
        vizEventManager = null;
//        textManager = null;
        //TODO
    }

    public Optional<VizModel> getVizModel() {
        return Optional.ofNullable(currentModel)
                .filter(VizModel::isReady);
    }

    public Optional<VizModel> getVizModelEvenIfNotReady() {
        return Optional.ofNullable(currentModel);
    }

    public boolean vizModelReady() {
        return currentModel != null && currentModel.isReady();
    }

    @Override
    public void resetSelection() {
        if (selectionManager != null && vizModelReady()) {
            selectionManager.resetSelection(currentModel);
        }
    }

    @Override
    public void centerOnGraph() {
        if (selectionManager != null) {
            selectionManager.centerOnGraph();
        }
    }

    @Override
    public void centerOnNode(Node node) {
        if (selectionManager != null) {
            selectionManager.centerOnNode(node);
        }
    }

    @Override
    public void centerOnEdge(Edge edge) {
        if (selectionManager != null) {
            selectionManager.centerOnEdge(edge);
        }
    }

    public void selectNode(Node node) {
        if (node == null) {
            selectNodes(null);
        } else {
            selectNodes(new Node[] {node});
        }
    }

    public void selectEdge(Edge edge) {
        if (edge == null) {
            selectEdges(null);
        } else {
            selectEdges(new Edge[] {edge});
        }
    }

    @Override
    public void selectNodes(Node[] nodes) {
        if (selectionManager != null && vizModelReady()) {
            selectionManager.selectNodes(nodes, currentModel);
        }
    }

    @Override
    public void selectEdges(Edge[] edges) {
        if (selectionManager != null && vizModelReady()) {
            selectionManager.selectEdges(edges, currentModel);
        }
    }

    @Override
    public List<Node> getSelectedNodes() {
        if (selectionManager != null) {
            return selectionManager.getSelectedNodes();
        }
        return Collections.emptyList();
    }

    @Override
    public List<Edge> getSelectedEdges() {
        if (selectionManager != null) {
            return selectionManager.getSelectedEdges();
        }
        return Collections.emptyList();
    }

    @Override
    public Column[] getEdgeTextColumns() {
        if (vizModelReady()) {
            //TODO
            // return currentModel.textModel.getEdgeTextColumns();
        }
        return new Column[0];
    }

    @Override
    public Column[] getEdgeTextColumns(Workspace workspace) {
        VizModel vizModel = workspace.getLookup().lookup(VizModel.class);
        if (vizModel != null && vizModel.isReady()) {
            //TODO
            //return vizModel.textModel.getEdgeTextColumns();
        }
        return new Column[0];
    }

    @Override
    public Column[] getNodeTextColumns() {
        if (vizModelReady()) {
            //TODO
            //return currentModel.textModel.getNodeTextColumns();
        }
        return new Column[0];
    }

    @Override
    public Column[] getNodeTextColumns(Workspace workspace) {
        VizModel vizModel = workspace.getLookup().lookup(VizModel.class);
        if (vizModel != null && vizModel.isReady()) {
            //TODO
            //return vizModel.textModel.getNodeTextColumns();
        }
        return new Column[0];
    }

    public VizConfig getVizConfig() {
        return vizConfig;
    }

    public VizEventManager getVizEventManager() {
        return vizEventManager;
    }

    public ScreenshotMaker getScreenshotMaker() {
        return screenshotMaker;
    }

    public SelectionManager getSelectionManager() {
        return selectionManager;
    }
}
