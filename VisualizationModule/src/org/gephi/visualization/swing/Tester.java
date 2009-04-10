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
package org.gephi.visualization.swing;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Label;
import javax.swing.JFrame;
import javax.swing.WindowConstants;
import org.gephi.visualization.VizController;
import org.gephi.visualization.api.VizEvent;
import org.gephi.visualization.api.VizEventListener;
import org.gephi.visualization.api.VizEventManager;
import org.gephi.visualization.opengl.compatibility.CompatibilityEngine;

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

        setSize(new Dimension(600, 600));
        setVisible(true);

        //DhnsController.getInstance().initInstances();
        VizController.getInstance().initInstances();
        VizEventManager vizEventManager = VizController.getInstance().getVizEventManager();
        CompatibilityEngine engine = (CompatibilityEngine) VizController.getInstance().getEngine();
        GraphDrawableImpl drawable = VizController.getInstance().getDrawable();

        VizEvent.Type[] types = {VizEvent.Type.DRAG, VizEvent.Type.MOUSE_LEFT_PRESS, VizEvent.Type.MOUSE_MOVE, VizEvent.Type.MOUSE_RIGHT_CLICK};
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
