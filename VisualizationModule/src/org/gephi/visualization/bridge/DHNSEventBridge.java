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


import org.gephi.graph.api.ClusteredGraph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.Model;
import org.gephi.visualization.VizArchitecture;
import org.gephi.visualization.VizController;
import org.gephi.visualization.api.objects.ModelClass;
import org.gephi.visualization.opengl.AbstractEngine;
import org.openide.util.Lookup;

/**
 *
 * @author Mathieu Bastian
 */
public class DHNSEventBridge implements EventBridge, VizArchitecture {

    //Architecture
    private AbstractEngine engine;
    private ClusteredGraph graph;

    @Override
    public void initArchitecture() {
        GraphController graphController = Lookup.getDefault().lookup(GraphController.class);
        this.graph = graphController.getClusteredDirectedGraph();
        this.engine = VizController.getInstance().getEngine();
        initEvents();
    }

    @Override
    public void initEvents() {
    }

    public void mouseClick(ModelClass objClass, Model[] clickedObjects) {

       /* switch (objClass.getClassId()) {
            case AbstractEngine.CLASS_NODE:
                for (int i = 0; i < clickedObjects.length; i++) {
                    Model obj = clickedObjects[i];
                    Node node = (Node) obj.getObj();
                    DhnsController dhnsController = Lookup.getDefault().lookup(DhnsController.class);
                    freeModifier.expand(node);
                }
                break;
            case AbstractEngine.CLASS_POTATO:
                for (int i = 0; i < clickedObjects.length; i++) {
                    Model obj = clickedObjects[i];
                    Potato potato = (Potato) obj.getObj();
                    DhnsController dhnsController = Lookup.getDefault().lookup(DhnsController.class);
                    freeModifier.retract(potato.getNode());
                }
                break;
        }*/



    }
}
