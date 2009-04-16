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

import org.gephi.data.network.api.DhnsController;
import org.gephi.data.network.api.FreeModifier;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.Object3d;
import org.gephi.visualization.VizArchitecture;
import org.gephi.visualization.VizController;
import org.gephi.visualization.opengl.AbstractEngine;
import org.openide.util.Lookup;


/**
 *
 * @author Mathieu Bastian
 */
public class DHNSEventBridge implements EventBridge, VizArchitecture {

    //Architecture
    private FreeModifier freeModifier;
    private AbstractEngine engine;

    @Override
    public void initArchitecture() {
        DhnsController dhnsController = Lookup.getDefault().lookup(DhnsController.class);
        this.freeModifier = dhnsController.getFreeModifier();
        this.engine = VizController.getInstance().getEngine();
        initEvents();
    }

    @Override
    public void initEvents()
    {
        
    }

    public void mouseClick(Object3d[] clickedObjects) {

        for(int i=0;i<clickedObjects.length;i++)
        {
            Object3d obj = clickedObjects[i];
            Node node = (Node)obj.getObj();
            DhnsController dhnsController = Lookup.getDefault().lookup(DhnsController.class);
            freeModifier.expand(node, dhnsController.getMainSight());
        }

    }
}
