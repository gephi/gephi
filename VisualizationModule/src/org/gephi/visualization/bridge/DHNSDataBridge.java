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
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.EdgeIterable;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Group;
import org.gephi.graph.api.HierarchicalGraph;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.Model;
import org.gephi.graph.api.NodeIterable;
import org.gephi.project.api.ProjectController;
import org.gephi.visualization.VizArchitecture;
import org.gephi.visualization.VizController;
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
    protected HierarchicalGraph graph;
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
    }

    public void updateWorld() {
        System.out.println("update world");
        cacheMarker++;

        GraphModel graphModel = controller.getModel();
        if (graphModel.isDirected()) {
            graph = graphModel.getHierarchicalDirectedGraphVisible();
        } else if (graphModel.isUndirected()) {
            graph = graphModel.getHierarchicalUndirectedGraphVisible();
        } else if (graphModel.isMixed()) {
            graph = graphModel.getHierarchicalMixedGraphVisible();
        } else {
            //No visualized graph inited yet
            return;
        }

        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);

        /*if (graph.isDynamic()) {
        System.out.println("dynamic graph");
        graph = (ClusteredDirectedGraph) controller.getVisualizedGraph();
        }*/

        ModelClass[] object3dClasses = engine.getModelClasses();

        graph.readLock();



        ModelClass nodeClass = object3dClasses[AbstractEngine.CLASS_NODE];
        if (nodeClass.isEnabled() && (graph.getNodeVersion() > nodeVersion || modeManager.requireModeChange())) {
            updateNodes();
            nodeClass.setCacheMarker(cacheMarker);
        }

        ModelClass edgeClass = object3dClasses[AbstractEngine.CLASS_EDGE];
        if (edgeClass.isEnabled() && (graph.getEdgeVersion() > edgeVersion || modeManager.requireModeChange())) {
            updateEdges();
            updateMetaEdges();
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
        nodeIterable = graph.getNodes();


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
//                if (!node.isVisible()) {
//                    ColorLayer.layerColor(impl, 0.8f, 0.8f, 0.8f);
//                }
            }
        }
    }

    private void updateEdges() {
        Modeler edgeInit = engine.getModelClasses()[AbstractEngine.CLASS_EDGE].getCurrentModeler();
        Modeler arrowInit = engine.getModelClasses()[AbstractEngine.CLASS_ARROW].getCurrentModeler();

        EdgeIterable edgeIterable;
        edgeIterable = graph.getEdges();

        for (Edge edge : edgeIterable) {
            assert edge.getSource().getNodeData().getModel() != null && edge.getTarget().getNodeData().getModel() != null;
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
            } else {
                if (vizConfig.isShowArrows() && !edge.isSelfLoop()) {
                    ModelImpl arrowObj = ((Edge2dModel) obj).getArrow();
                    arrowObj.setCacheMarker(cacheMarker);
                }
            }
            obj.setCacheMarker(cacheMarker);
        }
    }

    public void updateMetaEdges() {
        Modeler edgeInit = engine.getModelClasses()[AbstractEngine.CLASS_EDGE].getCurrentModeler();

        for (Edge edge : graph.getMetaEdges()) {
            assert edge.getSource().getNodeData().getModel() != null && edge.getTarget().getNodeData().getModel() != null;
            Model obj = edge.getEdgeData().getModel();
            if (obj == null) {
                //Model is null, ADD
                obj = edgeInit.initModel(edge.getEdgeData());

                engine.addObject(AbstractEngine.CLASS_EDGE, (ModelImpl) obj);
            } else if (!obj.isValid()) {
                engine.addObject(AbstractEngine.CLASS_EDGE, (ModelImpl) obj);
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
                engine.addObject(AbstractEngine.CLASS_POTATO, im);
            }
        }
    }

    public boolean requireUpdate() {
        if (graph == null) {
            //Try to get a graph
            if (controller.getModel() != null) {
                GraphModel graphModel = controller.getModel();
                if (!graphModel.isDirected() && !graphModel.isUndirected() && !graphModel.isMixed()) {
                    return false;
                } else {
                    graph = graphModel.getHierarchicalGraphVisible();
                }
            }
        }
        //Refresh reader if sight changed
        if (graph != null) {
            return graph.getNodeVersion() > nodeVersion || graph.getEdgeVersion() > edgeVersion;

        }
        return false;
    }

    public void reset() {
        nodeVersion = -1;
        edgeVersion = -1;
    }

    private void resetClasses() {
        for (ModelClass objClass : engine.getModelClasses()) {
            if (objClass.isEnabled()) {
                engine.resetObjectClass(objClass);
            }
        }
    }
}
