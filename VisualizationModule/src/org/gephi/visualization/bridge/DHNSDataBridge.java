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

import java.util.Iterator;
import org.gephi.data.network.api.EdgeWrap;
import org.gephi.data.network.api.NodeWrap;
import org.gephi.data.network.api.Reader;
import org.gephi.data.network.controller.DhnsController;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.Object3d;
import org.gephi.visualization.VizArchitecture;
import org.gephi.visualization.VizController;
import org.gephi.visualization.api.Object3dImpl;
import org.gephi.visualization.api.VizConfig;
import org.gephi.visualization.api.initializer.Object3dInitializer;
import org.gephi.visualization.api.objects.Object3dClass;
import org.gephi.visualization.opengl.AbstractEngine;

/**
 *
 * @author Mathieu Bastian
 */
public class DHNSDataBridge implements DataBridge, VizArchitecture {

    //Architecture
    protected AbstractEngine engine;
    protected Reader reader;
    private VizConfig vizConfig;

    //Attributes
    private int cacheMarker = 0;

    @Override
    public void initArchitecture() {
        this.engine = VizController.getInstance().getEngine();
        this.reader = DhnsController.getInstance().getReader();
        this.vizConfig = VizController.getInstance().getVizConfig();
    }

    public void updateWorld() {
        cacheMarker++;

        Object3dClass[] object3dClasses = engine.getObject3dClasses();

        Object3dClass nodeClass = object3dClasses[AbstractEngine.CLASS_NODE];
        if (nodeClass.isEnabled() && reader.requireNodeUpdate()) {
            updateNodes();
            nodeClass.setCacheMarker(cacheMarker);
        }

        Object3dClass edgeClass = object3dClasses[AbstractEngine.CLASS_EDGE];
        if (edgeClass.isEnabled() && reader.requireEdgeUpdate()) {
            updateEdges();
            edgeClass.setCacheMarker(cacheMarker);
            if (vizConfig.isDirectedEdges()) {
                object3dClasses[AbstractEngine.CLASS_ARROW].setCacheMarker(cacheMarker);
            }
        }

        engine.worldUpdated(cacheMarker);
    }

    private void updateNodes() {
        Object3dInitializer nodeInit = engine.getObject3dClasses()[AbstractEngine.CLASS_NODE].getCurrentObject3dInitializer();

        Iterator<? extends NodeWrap> itr = reader.getNodes();
        System.out.println("Bridge update nodes");
        for (; itr.hasNext();) {
            NodeWrap preNode = itr.next();
            Node node = preNode.getNode();

            Object3d obj = node.getObject3d();
            if (obj == null) {
                //Object3d is null, ADD
                obj = nodeInit.initObject(node);
                engine.addObject(AbstractEngine.CLASS_NODE, (Object3dImpl) obj);
            }
            obj.setCacheMarker(cacheMarker);

            node.setSize(10f);
        }
    }

    private void updateEdges() {
        Object3dInitializer edgeInit = engine.getObject3dClasses()[AbstractEngine.CLASS_EDGE].getCurrentObject3dInitializer();
        Object3dInitializer arrowInit = engine.getObject3dClasses()[AbstractEngine.CLASS_ARROW].getCurrentObject3dInitializer();

        Iterator<? extends EdgeWrap> itr = reader.getEdges();
        System.out.println("Bridge update edges");
        for (; itr.hasNext();) {
            EdgeWrap virtualEdge = itr.next();
            Edge edge = virtualEdge.getEdge();

            Object3d obj = edge.getObject3d();
            if (obj == null) {
                //Object3d is null, ADD
                obj = edgeInit.initObject(edge);

                engine.addObject(AbstractEngine.CLASS_EDGE, (Object3dImpl) obj);
                if (vizConfig.isDirectedEdges()) {
                    Object3d arrowObj = arrowInit.initObject(edge);
                    engine.addObject(AbstractEngine.CLASS_ARROW, (Object3dImpl) arrowObj);
                    arrowObj.setCacheMarker(cacheMarker);
                }
            }
            obj.setCacheMarker(cacheMarker);
        }
    }

    public boolean requireUpdate() {
        return reader.requireUpdate();
    }
}
