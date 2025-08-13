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

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.swing.event.ChangeListener;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Node;
import org.gephi.project.api.Workspace;
import org.gephi.project.spi.Controller;
import org.gephi.visualization.api.LabelColorMode;
import org.gephi.visualization.api.LabelSizeMode;
import org.gephi.visualization.api.ScreenshotController;
import org.gephi.visualization.api.VisualizationController;
import org.gephi.visualization.api.VisualizationEventListener;
import org.gephi.visualization.api.VisualizationPropertyChangeListener;
import org.gephi.visualization.events.StandardVizEventManager;
import org.gephi.visualization.screenshot.ScreenshotControllerImpl;
import org.gephi.viz.engine.VizEngine;
import org.gephi.viz.engine.status.GraphSelection;
import org.joml.Vector2f;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;

/**
 * @author Mathieu Bastian
 */
@ServiceProviders({
    @ServiceProvider(service = VisualizationController.class),
    @ServiceProvider(service = Controller.class)})
public class VizController implements VisualizationController, Controller<VizModel> {

    //Architecture
    protected final List<VisualizationPropertyChangeListener> listeners = new ArrayList<>();
    private final StandardVizEventManager vizEventManager;
    private final ScreenshotControllerImpl screenshotMaker;
    private VizModel currentModel;

    public VizController() {
        vizEventManager = new StandardVizEventManager();
        screenshotMaker = new ScreenshotControllerImpl();
//        limits = new GraphLimits();
//        textManager = new TextManager();
        currentModel = null;

        //TODO
//        textManager.initArchitecture();
//        screenshotMaker.initArchitecture();
    }

    @Override
    public VizModel newModel(Workspace workspace) {
        return new VizModel(this, workspace);
    }

    @Override
    public VizModel getModel(Workspace workspace) {
        return Controller.super.getModel(workspace);
    }

    @Override
    public Class<VizModel> getModelClass() {
        return VizModel.class;
    }

    @Override
    public VizModel getModel() {
        return Controller.super.getModel();
    }

    @Override
    public ScreenshotController getScreenshotController() {
        return null;
    }

    @Override
    public void addPropertyChangeListener(VisualizationPropertyChangeListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removePropertyChangeListener(VisualizationPropertyChangeListener listener) {
        listeners.remove(listener);
    }

    @Override
    public void addListener(VisualizationEventListener listener) {
        vizEventManager.addListener(listener);
    }

    @Override
    public void removeListener(VisualizationEventListener listener) {
        vizEventManager.removeListener(listener);
    }

    @Override
    public void setZoom(float zoom) {
        final VizModel model = getModel();
        model.setZoom(zoom);
    }

    @Override
    public void setAutoSelectNeighbors(boolean autoSelectNeighbors) {
        final VizModel model = getModel();
        model.setAutoSelectNeighbors(autoSelectNeighbors);
    }

    @Override
    public void setBackgroundColor(Color color) {
        final VizModel model = getModel();
        model.setBackgroundColor(color);
    }

    @Override
    public void setShowEdges(boolean showEdges) {
        final VizModel model = getModel();
        model.setShowEdges(showEdges);
    }

    @Override
    public void setEdgeHasUniColor(boolean edgeHasUniColor) {
        final VizModel model = getModel();
        model.setEdgeHasUniColor(edgeHasUniColor);
    }

    @Override
    public void setEdgeUniColor(Color edgeUniColor) {
        final VizModel model = getModel();
        model.setEdgeUniColor(VizController.toColorArray(edgeUniColor));
    }

    private static float[] toColorArray(Color color) {
        return new float[] {color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f};
    }

    @Override
    public void setHideNonSelectedEdges(boolean hideNonSelectedEdges) {
        final VizModel model = getModel();
        model.setHideNonSelectedEdges(hideNonSelectedEdges);
    }

    @Override
    public void setLightenNonSelectedAuto(boolean lightenNonSelectedAuto) {
        final VizModel model = getModel();
        model.setLightenNonSelectedAuto(lightenNonSelectedAuto);
    }

    @Override
    public void setEdgeSelectionColor(boolean edgeSelectionColor) {
        final VizModel model = getModel();
        model.setEdgeSelectionColor(edgeSelectionColor);
    }

    @Override
    public void setEdgeInSelectionColor(Color edgeInSelectionColor) {
        final VizModel model = getModel();
        model.setEdgeInSelectionColor(VizController.toColorArray(edgeInSelectionColor));
    }

    @Override
    public void setEdgeOutSelectionColor(Color edgeOutSelectionColor) {
        final VizModel model = getModel();
        model.setEdgeOutSelectionColor(VizController.toColorArray(edgeOutSelectionColor));
    }

    @Override
    public void setEdgeBothSelectionColor(Color edgeBothSelectionColor) {
        final VizModel model = getModel();
        model.setEdgeBothSelectionColor(VizController.toColorArray(edgeBothSelectionColor));
    }

    @Override
    public void setEdgeScale(float edgeScale) {
        final VizModel model = getModel();
        model.setEdgeScale(edgeScale);
    }

    // TEXT

    @Override
    public void setShowNodeLabels(boolean showNodeLabels) {
        final VizModel model = getModel();
        model.setShowNodeLabels(showNodeLabels);
    }

    @Override
    public void setShowEdgeLabels(boolean showEdgeLabels) {
        final VizModel model = getModel();
        model.setShowEdgeLabels(showEdgeLabels);
    }

    @Override
    public void setNodeLabelFont(Font font) {
        final VizModel model = getModel();
        model.setNodeLabelFont(font);
    }

    @Override
    public void setEdgeLabelFont(Font font) {
        final VizModel model = getModel();
        model.setEdgeLabelFont(font);
    }

    @Override
    public void setNodeLabelColor(Color color) {
        final VizModel model = getModel();
        model.setNodeLabelColor(color);
    }

    @Override
    public void setEdgeLabelColor(Color color) {
        final VizModel model = getModel();
        model.setEdgeLabelColor(color);
    }

    @Override
    public void setNodeLabelSize(float size) {
        final VizModel model = getModel();
        model.setNodeLabelSize(size);
    }

    @Override
    public void setEdgeLabelSize(float size) {
        final VizModel model = getModel();
        model.setEdgeLabelSize(size);
    }

    @Override
    public void setNodeLabelColorMode(LabelColorMode mode) {
        final VizModel model = getModel();
        model.setNodeLabelColorMode(mode);
    }

    @Override
    public void setNodeLabelSizeMode(LabelSizeMode mode) {
        final VizModel model = getModel();
        model.setNodeLabelSizeMode(mode);
    }

    @Override
    public void setHideNonSelectedLabels(boolean hideNonSelected) {
        final VizModel model = getModel();
        model.setHideNonSelectedLabels(hideNonSelected);
    }

    @Override
    public void setNodeLabelColumns(Column[] columns) {
        final VizModel model = getModel();
        model.setNodeLabelColumns(columns);
    }

    @Override
    public void setEdgeLabelColumns(Column[] columns) {
        final VizModel model = getModel();
        model.setEdgeLabelColumns(columns);
    }

    //    public void refreshWorkspace() {
//        final ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
//        final Workspace currentWorkspace = pc.getCurrentWorkspace();
//
//        VizModel model = null;
//        if (currentWorkspace != null) {
//            model = currentWorkspace.getLookup().lookup(VizModel.class);
//            if (model == null) {
//                model = new VizModel(currentWorkspace);
//                currentWorkspace.add(model);
//            }
//        }
//
//        currentModel = model;
//        if (currentModel != null) {
//            currentModel.init();
//        }
//    }

    public void destroy() {
//        vizEventManager = null;
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


//    @Override
//    public void selectNodes(Node[] nodes) {
//        if (selectionManager != null && vizModelReady()) {
//            selectionManager.selectNodes(nodes, currentModel);
//        }
//    }
//
//    @Override
//    public void selectEdges(Edge[] edges) {
//        if (selectionManager != null && vizModelReady()) {
//            selectionManager.selectEdges(edges, currentModel);
//        }
//    }
//
//    @Override
//    public List<Node> getSelectedNodes() {
//        if (selectionManager != null) {
//            return selectionManager.getSelectedNodes();
//        }
//        return Collections.emptyList();
//    }
//
//    @Override
//    public List<Edge> getSelectedEdges() {
//        if (selectionManager != null) {
//            return selectionManager.getSelectedEdges();
//        }
//        return Collections.emptyList();
//    }
//
//    public void blockSelection(boolean block) {
//        if (vizConfig.isRectangleSelection()) {
//            this.blocked = block;
//            vizConfig.setSelectionEnable(!block);
//            fireChangeEvent();
//        } else {
//            setDirectMouseSelection();
//        }
//    }
//
//    public void disableSelection() {
//        vizConfig.setSelectionEnable(false);
//        this.blocked = false;
//        fireChangeEvent();
//    }
//
//    public void setRectangleSelection() {
//        vizConfig.setCustomSelection(false);
//        vizConfig.setSelectionEnable(true);
//        setEngineSelectionMode(GraphSelection.GraphSelectionMode.RECTANGLE_SELECTION);
//        this.blocked = false;
//        fireChangeEvent();
//    }
//
//    public void setDirectMouseSelection() {
//        vizConfig.setSelectionEnable(true);
//        vizConfig.setCustomSelection(false);
//        setEngineSelectionMode(GraphSelection.GraphSelectionMode.SIMPLE_MOUSE_SELECTION);
//        this.blocked = false;
//        fireChangeEvent();
//    }
//
//    public void setDraggingMouseSelection() {
//        vizConfig.setSelectionEnable(true);
//        vizConfig.setCustomSelection(false);
//        setEngineSelectionMode(GraphSelection.GraphSelectionMode.SIMPLE_MOUSE_SELECTION);
//        this.blocked = false;
//        fireChangeEvent();
//    }
//
//    public void setCustomSelection() {
//        vizConfig.setSelectionEnable(false);
//        vizConfig.setCustomSelection(true);
//        setEngineSelectionMode(GraphSelection.GraphSelectionMode.SIMPLE_MOUSE_SELECTION);
//        this.blocked = true;
//        fireChangeEvent();
//    }
//
//    public void resetSelection(VizModel vizModel) {
//        if (isCustomSelection()) {
//            currentEngineSelectionModel()
//                .ifPresent(GraphSelection::clearSelection);
//
//            vizModel.setAutoSelectNeighbors(wasAutoSelectNeighbors);
//            if (wasRectangleSelection) {
//                setRectangleSelection();
//            } else if (wasDirectSelection) {
//                setDirectMouseSelection();
//            }
//        }
//    }

    public StandardVizEventManager getVizEventManager() {
        return vizEventManager;
    }

    public ScreenshotControllerImpl getScreenshotMaker() {
        return screenshotMaker;
    }

    @Override
    public void centerOnGraph() {
        getModel().getEngine().ifPresent(
            VizEngine::centerOnGraph
        );
    }

    @Override
    public void centerOnZero() {
        centerOn(0, 0, 1000, 1000);
    }

    @Override
    public void centerOn(float x, float y, float width, float height) {
        getModel().getEngine().ifPresent(
            engine -> engine.centerOn(new Vector2f(x,y), width, height)
        );
    }

    @Override
    public void centerOnNode(Node node) {
        if (node == null) {
            return;
        }
        getModel().getEngine().ifPresent(
            engine -> {
                final Vector2f position = new Vector2f(node.x(), node.y());
                final float size = node.size() * 10f;
                engine.centerOn(position, size, size);
            }
        );
    }

    @Override
    public void centerOnEdge(Edge edge) {
        if (edge == null) {
            return;
        }
        getModel().getEngine().ifPresent(
            engine -> {
                Node source = edge.getSource();
                Node target = edge.getTarget();
                float len = (float) Math.hypot(source.x() - target.x(), source.y() - target.y());
                final Vector2f position = new Vector2f((source.x() + target.x()) / 2f, (source.y() + target.y()) / 2f);
                engine.centerOn(position, len, len);
            }
        );
    }

    public void selectNodes(Node[] nodes, VizModel vizModel) {
        // TODO fix
//        if (!isCustomSelection()) {
//            wasAutoSelectNeighbors = vizModel.isAutoSelectNeighbors();
//            wasDirectSelection = isDirectMouseSelection();
//            wasRectangleSelection = isRectangleSelection();
//            vizModel.setAutoSelectNeighbors(false);
//            setCustomSelection();
//        }
//
//        currentEngineSelectionModel()
//            .ifPresent(selection -> {
//                if (nodes == null) {
//                    selection.clearSelectedNodes();
//                } else {
//                    selection.setSelectedNodes(Arrays.asList(nodes));
//                }
//            });
    }

    public void selectEdges(Edge[] edges, VizModel vizModel) {
        // TODO fix
//        if (!isCustomSelection()) {
//            wasAutoSelectNeighbors = vizModel.isAutoSelectNeighbors();
//            wasDirectSelection = isDirectMouseSelection();
//            wasRectangleSelection = isRectangleSelection();
//            vizModel.setAutoSelectNeighbors(false);
//            setCustomSelection();
//        }
//
//        currentEngineSelectionModel()
//            .ifPresent(selection -> {
//                if (edges == null) {
//                    selection.clearSelectedEdges();
//                } else {
//                    selection.setSelectedEdges(Arrays.asList(edges));
//                }
//            });
    }

    @Override
    public synchronized void blockSelection(boolean block) {
        VizModel model = getModel();
        if (model == null) {
            return;
        }
        if (model.isRectangleSelection()) {
            model.getSelectionModel().setBlocked(block);
            model.getSelectionModel().setSelectionEnable(!block);
        } else {
            setDirectMouseSelection();
        }
        model.fireSelectionChange();
    }

    @Override
    public synchronized void disableSelection() {
        VizModel model = getModel();
        if (model == null) {
            return;
        }
        model.getSelectionModel().setSelectionEnable(false);
        model.getSelectionModel().setRectangleSelection(false);
        model.getSelectionModel().setCustomSelection(false);
        setEngineSelectionMode(GraphSelection.GraphSelectionMode.NO_SELECTION);
        model.getSelectionModel().setBlocked(false);
        model.fireSelectionChange();
    }

    @Override
    public void setMouseSelectionDiameter(int diameter) {
        VizModel model = getModel();
        if (model == null) {
            return;
        }
        model.getSelectionModel().setMouseSelectionDiameter(diameter);
        model.fireSelectionChange();
    }

    @Override
    public void setMouseSelectionZoomProportional(boolean proportional) {
        VizModel model = getModel();
        if (model == null) {
            return;
        }
        model.getSelectionModel().setMouseSelectionZoomProportional(proportional);
        model.fireSelectionChange();
    }

    @Override
    public synchronized void setRectangleSelection() {
        VizModel model = getModel();
        if (model == null) {
            return;
        }
        model.getSelectionModel().setSelectionEnable(true);
        model.getSelectionModel().setRectangleSelection(true);
        model.getSelectionModel().setCustomSelection(false);
        setEngineSelectionMode(GraphSelection.GraphSelectionMode.RECTANGLE_SELECTION);
        model.getSelectionModel().setBlocked(false);
        model.fireSelectionChange();
    }

    @Override
    public synchronized void setDirectMouseSelection() {
        VizModel model = getModel();
        if (model == null) {
            return;
        }
        model.getSelectionModel().setSelectionEnable(true);
        model.getSelectionModel().setRectangleSelection(false);
        model.getSelectionModel().setCustomSelection(false);
        setEngineSelectionMode(GraphSelection.GraphSelectionMode.SIMPLE_MOUSE_SELECTION);
        model.getSelectionModel().setBlocked(false);
        model.fireSelectionChange();
    }

    @Override
    public synchronized void setCustomSelection() {
        VizModel model = getModel();
        if (model == null) {
            return;
        }
        model.getSelectionModel().setSelectionEnable(false);
        model.getSelectionModel().setRectangleSelection(false);
        model.getSelectionModel().setCustomSelection(true);
        setEngineSelectionMode(GraphSelection.GraphSelectionMode.SIMPLE_MOUSE_SELECTION);
        model.getSelectionModel().setBlocked(true);
        model.fireSelectionChange();
    }

    @Override
    public synchronized void resetSelection() {
        VizModel model = getModel();
        if (model == null) {
            return;
        }
        if (model.isCustomSelection()) {
            model.getSelectionModel().currentEngineSelectionModel()
                .ifPresent(GraphSelection::clearSelection);

            // TODO, it was enabled
//            vizModel.setAutoSelectNeighbors(wasAutoSelectNeighbors);
            if (model.getSelectionModel().wasRectangleSelection) {
                setRectangleSelection();
            } else if (model.getSelectionModel().wasDirectSelection) {
                setDirectMouseSelection();
            }
        }
    }

    @Override
    public void selectNodes(Node[] nodes) {
        VizModel model = getModel();
        if (model == null) {
            return;
        }
        if (!model.isCustomSelection()) {
//            wasAutoSelectNeighbors = vizModel.isAutoSelectNeighbors();
//            wasDirectSelection = isDirectMouseSelection();
//            wasRectangleSelection = isRectangleSelection();
//            vizModel.setAutoSelectNeighbors(false);
            setCustomSelection();
        }

        model.getSelectionModel().currentEngineSelectionModel()
            .ifPresent(selection -> {
                if (nodes == null) {
                    selection.clearSelectedNodes();
                } else {
                    selection.setSelectedNodes(Arrays.asList(nodes));
                }
            });
    }

    @Override
    public void selectEdges(Edge[] edges) {
        VizModel model = getModel();
        if (model == null) {
            return;
        }
        if (!model.isCustomSelection()) {
//            wasAutoSelectNeighbors = vizModel.isAutoSelectNeighbors();
//            wasDirectSelection = isDirectMouseSelection();
//            wasRectangleSelection = isRectangleSelection();
//            vizModel.setAutoSelectNeighbors(false);
            setCustomSelection();
        }

        model.getSelectionModel().currentEngineSelectionModel()
            .ifPresent(selection -> {
                if (edges == null) {
                    selection.clearSelectedEdges();
                } else {
                    selection.setSelectedEdges(Arrays.asList(edges));
                }
            });
    }

    private void setEngineSelectionMode(GraphSelection.GraphSelectionMode mode) {
        VizModel model = getModel();
        if (model == null) {
            return;
        }
        model.getSelectionModel().currentEngineSelectionModel().ifPresent(graphSelection -> {
            graphSelection.setMode(mode);
        });
    }
}
