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

package gephi.visualization.bridge;

import gephi.data.network.Dhns;
import gephi.data.network.DhnsController;
import gephi.data.network.Node;
import gephi.visualization.VizArchitecture;
import gephi.visualization.VizController;
import gephi.visualization.events.VizEvent;
import gephi.visualization.events.VizEventListener;
import gephi.visualization.events.VizEventManager;
import gephi.visualization.opengl.AbstractEngine;
import gephi.visualization.opengl.Object3d;

/**
 *
 * @author Mathieu
 */
public class DHNSEventBridge implements EventBridge, VizArchitecture {

    //Architecture
    private Dhns dhns;
    private AbstractEngine engine;

    @Override
    public void initArchitecture() {
        this.dhns = DhnsController.getInstance().getDhns();
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
            dhns.expand(node.getPreNode(), DhnsController.getInstance().getMainSight());
        }

    }
}
