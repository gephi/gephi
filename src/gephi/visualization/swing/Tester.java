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

import gephi.visualization.config.VizCommander;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Label;
import javax.swing.JFrame;

/**
 *
 * @author Mathieu
 */
public class Tester extends JFrame {

    public Tester() {
        Container container = getContentPane();
        container.setLayout(new BorderLayout());
        Label label = new Label("Waiting");
        container.add(label, BorderLayout.CENTER);

        setSize(new Dimension(400,400));
        setVisible(true);

        VizCommander commander = new VizCommander();
        GraphDrawable drawable = commander.createPanel();
        drawable.graphComponent.setPreferredSize( new Dimension(400, 400));
        container.add(drawable.graphComponent, BorderLayout.CENTER);
        container.addNotify();
        container.validate();
        container.remove(label);
        drawable.display();
    }

    public static void main(String[] args) {
        Tester tester = new Tester();
    }
}
