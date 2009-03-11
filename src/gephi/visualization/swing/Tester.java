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

import gephi.data.network.Node;
import gephi.visualization.NodeInitializer;
import gephi.visualization.config.VizCommander;
import gephi.visualization.events.StandardVizEventManager;
import gephi.visualization.events.VizEvent;
import gephi.visualization.events.VizEventListener;
import gephi.visualization.opengl.compatibility.CompatibilityEngine;
import gephi.visualization.opengl.octree.Octree;
import gephi.visualization.scheduler.Scheduler;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Label;
import java.util.ArrayList;
import javax.swing.JFrame;
import javax.swing.WindowConstants;

/**
 *
 * @author Mathieu
 */
public class Tester extends JFrame {

    private VizEventListener listener;

    public Tester() {
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        Container container = getContentPane();
        container.setLayout(new BorderLayout());
        Label label = new Label("Waiting");
        container.add(label, BorderLayout.CENTER);

        setSize(new Dimension(600,600));
        setVisible(true);

        VizCommander commander = new VizCommander();
        GraphDrawable drawable = commander.createPanel();
        drawable.graphComponent.setPreferredSize( new Dimension(600, 600));
        
        StandardGraphIO graphIO = new StandardGraphIO(drawable);
        CompatibilityEngine engine = new CompatibilityEngine(drawable, graphIO);
        graphIO.setEngine(engine);
        drawable.setEngine(engine);
        StandardVizEventManager vizEventManager = new StandardVizEventManager();
        graphIO.setVizEventManager(vizEventManager);
        engine.setVizEventManager(vizEventManager);
        VizEvent.Type[] types = {VizEvent.Type.DRAG,VizEvent.Type.MOUSE_LEFT_PRESS,VizEvent.Type.MOUSE_MOVE,VizEvent.Type.MOUSE_RIGHT_CLICK};
        listener = new VizEventListener() {

            public void vizEvent(VizEvent event) {
               System.out.println(event.getType());
            }
        };
        vizEventManager.addListener(listener,types );

        //Engine
        ArrayList<Node> nodeList = new ArrayList<Node>();
        for(int i=0;i<100;i++)
        {
            Node n = new Node();
            n.size=3;
            nodeList.add(n);
        }
        NodeInitializer nodeInit = engine.getCurrentNodeInitializer();
        for(Node n : nodeList)
        {
            engine.octree.addObject(0, nodeInit.initNodeObject(n));
        }
        container.add(drawable.graphComponent, BorderLayout.CENTER);
        container.addNotify();
        container.validate();
        container.remove(label);
        drawable.display();
        Scheduler scheduler = new Scheduler(drawable,engine);
        scheduler.start();
        
    }

    public static void main(String[] args) {
        Tester tester = new Tester();
    }
}
