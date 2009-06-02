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
import org.gephi.graph.api.Object3d;
import org.gephi.visualization.VizArchitecture;
import org.gephi.visualization.VizController;
import org.gephi.visualization.api.Object3dImpl;
import org.gephi.visualization.api.VizConfig;
import org.gephi.visualization.api.initializer.Object3dInitializer;
import org.gephi.visualization.api.objects.Object3dClass;
import org.gephi.visualization.opengl.AbstractEngine;
import org.gephi.visualization.opengl.compatibility.objects.Edge3dObject;
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

        Object3dClass[] object3dClasses = engine.getObject3dClasses();

        graph.readLock();

        Object3dClass nodeClass = object3dClasses[AbstractEngine.CLASS_NODE];
        if (nodeClass.isEnabled() && graph.getNodeVersion() > nodeVersion) {
            updateNodes();
            nodeClass.setCacheMarker(cacheMarker);
        }

        Object3dClass edgeClass = object3dClasses[AbstractEngine.CLASS_EDGE];
        if (edgeClass.isEnabled() && graph.getEdgeVersion() > edgeVersion) {
            updateEdges();
            edgeClass.setCacheMarker(cacheMarker);
            if (vizConfig.isDirectedEdges()) {
                object3dClasses[AbstractEngine.CLASS_ARROW].setCacheMarker(cacheMarker);
            }
        }

        /*Object3dClass potatoClass = object3dClasses[AbstractEngine.CLASS_POTATO];
        if (potatoClass.isEnabled() && reader.requirePotatoUpdate()) {
            updatePotatoes();
            potatoClass.setCacheMarker(cacheMarker);
        }*/

        graph.readUnlock();

        engine.worldUpdated(cacheMarker);
    }

    private void updateNodes() {
        Object3dInitializer nodeInit = engine.getObject3dClasses()[AbstractEngine.CLASS_NODE].getCurrentObject3dInitializer();

        for (Node node : graph.getNodes()) {
            Object3d obj = node.getNodeData().getObject3d();
            if (obj == null) {
                //Object3d is null, ADD
                obj = nodeInit.initObject(node.getNodeData());
                engine.addObject(AbstractEngine.CLASS_NODE, (Object3dImpl) obj);
            } else if (!obj.isValid()) {
                engine.addObject(AbstractEngine.CLASS_NODE, (Object3dImpl) obj);
            }
            obj.setCacheMarker(cacheMarker);
        }

        nodeVersion = graph.getNodeVersion();
    }

    private void updateEdges() {
        Object3dInitializer edgeInit = engine.getObject3dClasses()[AbstractEngine.CLASS_EDGE].getCurrentObject3dInitializer();
        Object3dInitializer arrowInit = engine.getObject3dClasses()[AbstractEngine.CLASS_ARROW].getCurrentObject3dInitializer();

        for (Edge edge : graph.getEdges()) {

            Object3d obj = edge.getEdgeData().getObject3d();
            if (obj == null) {
                //Object3d is null, ADD
                obj = edgeInit.initObject(edge.getEdgeData());

                engine.addObject(AbstractEngine.CLASS_EDGE, (Object3dImpl) obj);
                if (vizConfig.isDirectedEdges()) {
                    Object3dImpl arrowObj = arrowInit.initObject(edge.getEdgeData());
                    engine.addObject(AbstractEngine.CLASS_ARROW, arrowObj);
                    arrowObj.setCacheMarker(cacheMarker);
                    ((Edge3dObject) obj).setArrow(arrowObj);
                }
            } else if (!obj.isValid()) {
                engine.addObject(AbstractEngine.CLASS_EDGE, (Object3dImpl) obj);
                if (vizConfig.isDirectedEdges()) {
                    Object3dImpl arrowObj = ((Edge3dObject) obj).getArrow();
                    engine.addObject(AbstractEngine.CLASS_ARROW, arrowObj);
                    arrowObj.setCacheMarker(cacheMarker);
                }
            }
            obj.setCacheMarker(cacheMarker);
        }

        edgeVersion = graph.getEdgeVersion();
    }

    public void updatePotatoes() {
        /*Object3dInitializer potatoInit = engine.getObject3dClasses()[AbstractEngine.CLASS_POTATO].getCurrentObject3dInitializer();

        Iterator<? extends Potato> itr = reader.getPotatoes();
        for (; itr.hasNext();) {
            Potato potato = itr.next();

            Object3d obj = potato.getObject3d();
            if (obj == null) {
                //Object3d is null, ADD
                obj = potatoInit.initObject(potato);
                engine.addObject(AbstractEngine.CLASS_POTATO, (Object3dImpl) obj);
            } else if (!obj.isValid()) {
                engine.addObject(AbstractEngine.CLASS_POTATO, (Object3dImpl) obj);
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
        for (Object3dClass objClass : engine.getObject3dClasses()) {
            if (objClass.isEnabled()) {
                engine.resetObjectClass(objClass);
            }
        }
    }
}
