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
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.gephi.graph.api.EdgeWrap;
import org.gephi.graph.api.NodeWrap;
import org.gephi.data.network.api.AsyncReader;
import org.gephi.data.network.api.DhnsController;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.Object3d;
import org.gephi.data.network.api.Potato;
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
public class DHNSDataBridge implements DataBridge, VizArchitecture, ChangeListener {

    //Architecture
    protected AbstractEngine engine;
    protected DhnsController controller;
    protected AsyncReader reader;
    private VizConfig vizConfig;

    //States
    private AtomicBoolean sightChange = new AtomicBoolean(true);

    //Attributes
    private int cacheMarker = 0;

    @Override
    public void initArchitecture() {
        this.engine = VizController.getInstance().getEngine();
        controller = Lookup.getDefault().lookup(DhnsController.class);
        //controller.getSightManager().addChangeListener(this);
        reader = controller.getAsyncReader();
        //resetClasses();
        this.vizConfig = VizController.getInstance().getVizConfig();
    }

    public void updateWorld() {
        System.out.println("update world");
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

        Object3dClass potatoClass = object3dClasses[AbstractEngine.CLASS_POTATO];
        if (potatoClass.isEnabled() && reader.requirePotatoUpdate()) {
            updatePotatoes();
            potatoClass.setCacheMarker(cacheMarker);
        }

        reader.setUpdated();
        engine.worldUpdated(cacheMarker);
    }

    private void updateNodes() {
        Object3dInitializer nodeInit = engine.getObject3dClasses()[AbstractEngine.CLASS_NODE].getCurrentObject3dInitializer();

        Iterator<? extends NodeWrap> itr = reader.getNodes();
        for (; itr.hasNext();) {
            NodeWrap preNode = itr.next();
            Node node = preNode.getNode();
            
            Object3d obj = node.getObject3d();
            if (obj == null) {
                //Object3d is null, ADD
                obj = nodeInit.initObject(node);
                engine.addObject(AbstractEngine.CLASS_NODE, (Object3dImpl) obj);
            } else if (!obj.isValid()) {
                engine.addObject(AbstractEngine.CLASS_NODE, (Object3dImpl) obj);
            }
            obj.setCacheMarker(cacheMarker);
        }
    }

    private void updateEdges() {
        Object3dInitializer edgeInit = engine.getObject3dClasses()[AbstractEngine.CLASS_EDGE].getCurrentObject3dInitializer();
        Object3dInitializer arrowInit = engine.getObject3dClasses()[AbstractEngine.CLASS_ARROW].getCurrentObject3dInitializer();

        Iterator<? extends EdgeWrap> itr = reader.getEdges();
        for (; itr.hasNext();) {
            EdgeWrap virtualEdge = itr.next();
            Edge edge = virtualEdge.getEdge();

            Object3d obj = edge.getObject3d();
            if (obj == null) {
                //Object3d is null, ADD
                obj = edgeInit.initObject(edge);

                engine.addObject(AbstractEngine.CLASS_EDGE, (Object3dImpl) obj);
                if (vizConfig.isDirectedEdges()) {
                    Object3dImpl arrowObj = arrowInit.initObject(edge);
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
    }

    public void updatePotatoes() {
        Object3dInitializer potatoInit = engine.getObject3dClasses()[AbstractEngine.CLASS_POTATO].getCurrentObject3dInitializer();

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
        }
    }

    public boolean requireUpdate() {
        //Refresh reader if sight changed
        if (reader != null) {
            return reader.requireUpdate();
        }
        return false;
    }

    public void stateChanged(ChangeEvent e) {
        sightChange.set(true);
    }

    private void resetClasses() {
        for (Object3dClass objClass : engine.getObject3dClasses()) {
            if (objClass.isEnabled()) {
                engine.resetObjectClass(objClass);
            }
        }
    }
}
