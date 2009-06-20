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

import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.Model;
import org.gephi.visualization.VizArchitecture;
import org.gephi.visualization.VizController;
import org.gephi.visualization.api.ModelImpl;
import org.gephi.visualization.api.VizConfig;
import org.gephi.visualization.api.initializer.Modeler;
import org.gephi.visualization.api.objects.ModelClass;
import org.gephi.visualization.opengl.AbstractEngine;
import org.gephi.visualization.opengl.compatibility.objects.Edge3dModel;
import org.openide.util.Lookup;



/**
 *
 * @author Mathieu Bastian
 */
public class DHNSDataBridge implements DataBridge, VizArchitecture {

    //Architecture
    protected AbstractEngine engine;
    protected GraphController controller;
    protected Graph graph;
    private VizConfig vizConfig;

    //Version
    protected int nodeVersion=-1;
    protected int edgeVersion=-1;

    //Attributes
    private int cacheMarker = 0;

    @Override
    public void initArchitecture() {
        this.engine = VizController.getInstance().getEngine();
        controller = Lookup.getDefault().lookup(GraphController.class);
        graph = controller.getVisibleDirectedGraph();
        this.vizConfig = VizController.getInstance().getVizConfig();
    }

    public void updateWorld() {
        System.out.println("update world");
        cacheMarker++;

        ModelClass[] object3dClasses = engine.getObject3dClasses();

        graph.readLock();

        ModelClass nodeClass = object3dClasses[AbstractEngine.CLASS_NODE];
        if (nodeClass.isEnabled() && graph.getNodeVersion() > nodeVersion) {
            updateNodes();
            nodeClass.setCacheMarker(cacheMarker);
        }

        ModelClass edgeClass = object3dClasses[AbstractEngine.CLASS_EDGE];
        if (edgeClass.isEnabled() && graph.getEdgeVersion() > edgeVersion) {
            updateEdges();
            edgeClass.setCacheMarker(cacheMarker);
            if (vizConfig.isDirectedEdges()) {
                object3dClasses[AbstractEngine.CLASS_ARROW].setCacheMarker(cacheMarker);
            }
        }

        /*ModelClass potatoClass = object3dClasses[AbstractEngine.CLASS_POTATO];
        if (potatoClass.isEnabled() && reader.requirePotatoUpdate()) {
            updatePotatoes();
            potatoClass.setCacheMarker(cacheMarker);
        }*/

        graph.readUnlock();

        engine.worldUpdated(cacheMarker);
    }

    private void updateNodes() {
        Modeler nodeInit = engine.getObject3dClasses()[AbstractEngine.CLASS_NODE].getCurrentModeler();

        for (Node node : graph.getNodes()) {
            Model obj = node.getNodeData().getObject3d();
            if (obj == null) {
                //Model is null, ADD
                obj = nodeInit.initModel(node.getNodeData());
                engine.addObject(AbstractEngine.CLASS_NODE, (ModelImpl) obj);
            } else if (!obj.isValid()) {
                engine.addObject(AbstractEngine.CLASS_NODE, (ModelImpl) obj);
            }
            obj.setCacheMarker(cacheMarker);
        }

        nodeVersion = graph.getNodeVersion();
    }

    private void updateEdges() {
        Modeler edgeInit = engine.getObject3dClasses()[AbstractEngine.CLASS_EDGE].getCurrentModeler();
        Modeler arrowInit = engine.getObject3dClasses()[AbstractEngine.CLASS_ARROW].getCurrentModeler();

        for (Edge edge : graph.getEdges()) {

            Model obj = edge.getEdgeData().getObject3d();
            if (obj == null) {
                //Model is null, ADD
                obj = edgeInit.initModel(edge.getEdgeData());

                engine.addObject(AbstractEngine.CLASS_EDGE, (ModelImpl) obj);
                if (vizConfig.isDirectedEdges()) {
                    ModelImpl arrowObj = arrowInit.initModel(edge.getEdgeData());
                    engine.addObject(AbstractEngine.CLASS_ARROW, arrowObj);
                    arrowObj.setCacheMarker(cacheMarker);
                    ((Edge3dModel) obj).setArrow(arrowObj);
                }
            } else if (!obj.isValid()) {
                engine.addObject(AbstractEngine.CLASS_EDGE, (ModelImpl) obj);
                if (vizConfig.isDirectedEdges()) {
                    ModelImpl arrowObj = ((Edge3dModel) obj).getArrow();
                    engine.addObject(AbstractEngine.CLASS_ARROW, arrowObj);
                    arrowObj.setCacheMarker(cacheMarker);
                }
            }
            obj.setCacheMarker(cacheMarker);
        }

        edgeVersion = graph.getEdgeVersion();
    }

    public void updatePotatoes() {
        /*Modeler potatoInit = engine.getObject3dClasses()[AbstractEngine.CLASS_POTATO].getCurrentModeler();

        Iterator<? extends Potato> itr = reader.getPotatoes();
        for (; itr.hasNext();) {
            Potato potato = itr.next();

            Model obj = potato.getObject3d();
            if (obj == null) {
                //Model is null, ADD
                obj = potatoInit.initModel(potato);
                engine.addObject(AbstractEngine.CLASS_POTATO, (ModelImpl) obj);
            } else if (!obj.isValid()) {
                engine.addObject(AbstractEngine.CLASS_POTATO, (ModelImpl) obj);
            }
            obj.setCacheMarker(cacheMarker);
        }*/
    }

    public boolean requireUpdate() {
        //Refresh reader if sight changed
        if (graph != null) {
            return graph.getNodeVersion() > nodeVersion || graph.getEdgeVersion() > edgeVersion;
        }
        return false;
    }


    private void resetClasses() {
        for (ModelClass objClass : engine.getObject3dClasses()) {
            if (objClass.isEnabled()) {
                engine.resetObjectClass(objClass);
            }
        }
    }
}
