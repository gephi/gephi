/*
Copyright 2008 WebAtlas
Authors : Mathieu Bastian, Mathieu Jacomy, Julian Bilcke
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gephi.visualization.bridge;

import java.util.ArrayList;
import java.util.List;
import org.gephi.graph.api.ClusteredDirectedGraph;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.EdgeIterable;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.Group;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.Model;
import org.gephi.graph.api.NodeIterable;
import org.gephi.visualization.VizArchitecture;
import org.gephi.visualization.VizController;
import org.gephi.visualization.api.ColorLayer;
import org.gephi.visualization.api.ModelImpl;
import org.gephi.visualization.api.VizConfig;
import org.gephi.visualization.api.initializer.Modeler;
import org.gephi.visualization.api.objects.ModelClass;
import org.gephi.visualization.hull.ConvexHull;
import org.gephi.visualization.mode.ModeManager;
import org.gephi.visualization.opengl.AbstractEngine;
import org.gephi.visualization.opengl.compatibility.objects.ConvexHullModel;
import org.gephi.visualization.opengl.compatibility.objects.Edge2dModel;
import org.openide.util.Lookup;

/**
 *
 * @author Mathieu Bastian
 */
public class DHNSDataBridge implements DataBridge, VizArchitecture {

    //Architecture
    protected AbstractEngine engine;
    protected GraphController controller;
    protected ClusteredDirectedGraph graph;
    private VizConfig vizConfig;
    protected ModeManager modeManager;

    //Version
    protected int nodeVersion = -1;
    protected int edgeVersion = -1;

    //Attributes
    private int cacheMarker = 0;

    @Override
    public void initArchitecture() {
        this.engine = VizController.getInstance().getEngine();
        controller = Lookup.getDefault().lookup(GraphController.class);
        this.vizConfig = VizController.getInstance().getVizConfig();
        this.modeManager = VizController.getInstance().getModeManager();
        graph = controller.getClusteredDirectedGraph();
    }

    public void updateWorld() {
        System.out.println("update world");
        cacheMarker++;

        switch (modeManager.getMode()) {
            case FULL:
                graph = controller.getClusteredDirectedGraph();
                break;
            case VISIBLE:
                //graph = controller.getVisibleDirectedGraph();
                break;
            case HIGHLIGHT:
                //graph = controller.getDirectedGraph();
                break;
        }

        ModelClass[] object3dClasses = engine.getModelClasses();

        graph.readLock();

        ModelClass nodeClass = object3dClasses[AbstractEngine.CLASS_NODE];
        if (nodeClass.isEnabled() && (graph.getNodeVersion() > nodeVersion || modeManager.requireModeChange())) {
            updateNodes();
            nodeClass.setCacheMarker(cacheMarker);
        }

        ModelClass edgeClass = object3dClasses[AbstractEngine.CLASS_EDGE];
        if (edgeClass.isEnabled() && (graph.getEdgeVersion() > edgeVersion || modeManager.requireModeChange() || vizConfig.isVisualizeTree())) {
            updateEdges();
            edgeClass.setCacheMarker(cacheMarker);
            if (vizConfig.isShowArrows()) {
                object3dClasses[AbstractEngine.CLASS_ARROW].setCacheMarker(cacheMarker);
            }
        }

        ModelClass potatoClass = object3dClasses[AbstractEngine.CLASS_POTATO];
        if (potatoClass.isEnabled() && (graph.getNodeVersion() > nodeVersion || modeManager.requireModeChange())) {
            updatePotatoes();
            potatoClass.setCacheMarker(cacheMarker);
        }

        nodeVersion = graph.getNodeVersion();
        edgeVersion = graph.getEdgeVersion();

        graph.readUnlock();

        engine.worldUpdated(cacheMarker);
    }

    private void updateNodes() {
        Modeler nodeInit = engine.getModelClasses()[AbstractEngine.CLASS_NODE].getCurrentModeler();

        NodeIterable nodeIterable;
        if (vizConfig.isVisualizeTree()) {
            nodeIterable = graph.getHierarchyTree().getNodes();
        } else {
            nodeIterable = graph.getNodes();
        }

        for (Node node : nodeIterable) {
            Model obj = node.getNodeData().getModel();
            if (obj == null) {
                //Model is null, ADD
                obj = nodeInit.initModel(node.getNodeData());
                engine.addObject(AbstractEngine.CLASS_NODE, (ModelImpl) obj);
            } else if (!obj.isValid()) {
                engine.addObject(AbstractEngine.CLASS_NODE, (ModelImpl) obj);
            }
            obj.setCacheMarker(cacheMarker);

            //Modeaction
            if (modeManager.getMode().equals(ModeManager.AVAILABLE_MODES.HIGHLIGHT)) {
                ModelImpl impl = (ModelImpl) obj;
                if (!node.isVisible()) {
                    ColorLayer.layerColor(impl, 0.8f, 0.8f, 0.8f);
                }
            }
            //Tree position
            if (vizConfig.isVisualizeTree()) {
                node.getNodeData().setX(node.getPre() * 25);
                node.getNodeData().setY(node.getPost() * 25);
                if (graph.isInView(node)) {
                    node.getNodeData().setR(1f);
                    node.getNodeData().setG(0f);
                    node.getNodeData().setB(0f);
                } else {
                    node.getNodeData().setR(0.2f);
                    node.getNodeData().setG(0.2f);
                    node.getNodeData().setB(0.2f);
                }
            }
        }
    }

    private void updateEdges() {
        Modeler edgeInit = engine.getModelClasses()[AbstractEngine.CLASS_EDGE].getCurrentModeler();
        Modeler arrowInit = engine.getModelClasses()[AbstractEngine.CLASS_ARROW].getCurrentModeler();

        EdgeIterable edgeIterable;
        if (vizConfig.isVisualizeTree()) {
            edgeIterable = graph.getHierarchyTree().getEdges();
        } else {
            edgeIterable = graph.getEdges();
        }

        for (Edge edge : edgeIterable) {

            Model obj = edge.getEdgeData().getModel();
            if (obj == null) {
                //Model is null, ADD
                obj = edgeInit.initModel(edge.getEdgeData());

                engine.addObject(AbstractEngine.CLASS_EDGE, (ModelImpl) obj);
                if (vizConfig.isShowArrows() && !edge.isSelfLoop()) {
                    ModelImpl arrowObj = arrowInit.initModel(edge.getEdgeData());
                    engine.addObject(AbstractEngine.CLASS_ARROW, arrowObj);
                    arrowObj.setCacheMarker(cacheMarker);
                    ((Edge2dModel) obj).setArrow(arrowObj);
                }
            } else if (!obj.isValid()) {
                engine.addObject(AbstractEngine.CLASS_EDGE, (ModelImpl) obj);
                if (vizConfig.isShowArrows() && !edge.isSelfLoop()) {
                    ModelImpl arrowObj = ((Edge2dModel) obj).getArrow();
                    engine.addObject(AbstractEngine.CLASS_ARROW, arrowObj);
                    arrowObj.setCacheMarker(cacheMarker);
                }
            }
            obj.setCacheMarker(cacheMarker);
        }
    }

    public void updatePotatoes() {
        ModelClass potatoClass = engine.getModelClasses()[AbstractEngine.CLASS_POTATO];
        if (potatoClass.isEnabled()) {
            Modeler potInit = engine.getModelClasses()[AbstractEngine.CLASS_POTATO].getCurrentModeler();

            List<ModelImpl> hulls = new ArrayList<ModelImpl>();
            Node[] nodes = graph.getNodes().toArray();
            for (Node n : nodes) {
                Node parent = graph.getParent(n);
                if (parent != null) {
                    Group group = (Group) parent;
                    Model hullModel = group.getGroupData().getHullModel();
                    if (hullModel != null && hullModel.isCacheMatching(cacheMarker)) {
                        ConvexHull hull = (ConvexHull) hullModel.getObj();
                        hull.addNode(n);
                    } else {
                        ConvexHull ch = new ConvexHull();
                        ch.setMetaNode(parent);
                        ch.addNode(n);
                        ModelImpl obj = potInit.initModel(ch);
                        group.getGroupData().setHullModel(obj);
                        obj.setCacheMarker(cacheMarker);
                        hulls.add(obj);
                        if (hullModel != null) {
                            //Its not the first time the hull exist
                            ConvexHullModel model = (ConvexHullModel) obj;
                            model.setScale(1f);
                        }
                    }
                }
            }
            for (ModelImpl im : hulls) {
                ConvexHull hull = (ConvexHull) im.getObj();
                hull.recompute();
                for (Node n : hull.getGroupNodes()) {
                    ModelImpl model = (ModelImpl) n.getNodeData().getModel();
                    model.addUpdatePositionChainItem(im);
                }
                engine.addObject(AbstractEngine.CLASS_POTATO, im);
            }
        }
    }

    public boolean requireUpdate() {
        //Refresh reader if sight changed
        if (graph != null) {
            if (vizConfig.isVisualizeTree()) {
                return graph.getNodeVersion() > nodeVersion;
            } else {
                return graph.getNodeVersion() > nodeVersion || graph.getEdgeVersion() > edgeVersion;
            }

        }
        return false;
    }

    private void resetClasses() {
        for (ModelClass objClass : engine.getModelClasses()) {
            if (objClass.isEnabled()) {
                engine.resetObjectClass(objClass);
            }
        }
    }
}
