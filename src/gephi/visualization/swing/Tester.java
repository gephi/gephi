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
package gephi.visualization.swing;

import gephi.data.network.DhnsController;
import gephi.data.network.Edge;
import gephi.data.network.Node;
import gephi.data.network.avl.param.AVLItemAccessor;
import gephi.data.network.avl.param.ParamAVLTree;
import gephi.data.network.avl.simple.AVLItem;
import gephi.data.network.avl.simple.SimpleAVLTree;
import gephi.visualization.VizController;
import gephi.visualization.initializer.NodeInitializer;
import gephi.visualization.config.VizCommander;
import gephi.visualization.events.StandardVizEventManager;
import gephi.visualization.events.VizEvent;
import gephi.visualization.events.VizEventListener;
import gephi.visualization.events.VizEventManager;
import gephi.visualization.initializer.Object3dInitializer;
import gephi.visualization.opengl.AbstractEngine;
import gephi.visualization.opengl.compatibility.CompatibilityEngine;
import gephi.visualization.opengl.octree.Octree;
import gephi.visualization.scheduler.Scheduler;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Label;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.WindowConstants;

/**
 *
 * @author Mathieu
 */
public class Tester extends JFrame {

    private VizEventListener listener;

    public Tester() 
    {
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        Container container = getContentPane();
        container.setLayout(new BorderLayout());
        Label label = new Label("Waiting");
        container.add(label, BorderLayout.CENTER);

        setSize(new Dimension(600,600));
        setVisible(true);

       DhnsController.getInstance().initInstances();
       VizController.getInstance().initInstances();
       VizEventManager vizEventManager = VizController.getInstance().getVizEventManager();
       CompatibilityEngine engine = (CompatibilityEngine)VizController.getInstance().getEngine();
       GraphDrawable drawable = VizController.getInstance().getDrawable();
       
        VizEvent.Type[] types = {VizEvent.Type.DRAG,VizEvent.Type.MOUSE_LEFT_PRESS,VizEvent.Type.MOUSE_MOVE,VizEvent.Type.MOUSE_RIGHT_CLICK};
        listener = new VizEventListener() {

            public void vizEvent(VizEvent event) {
               System.out.println(event.getType());
            }
        };
        //vizEventManager.addListener(listener,types );

        //Engine
        container.add(drawable.graphComponent, BorderLayout.CENTER);
        container.addNotify();
        container.validate();
        container.remove(label);
        drawable.display();
        engine.getScheduler().start();
    }

    public static void main(String[] args) {
        Tester tester = new Tester();
    }
}
