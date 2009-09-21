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
package org.gephi.tools;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import org.gephi.graph.api.DirectedGraph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.Node;
import org.gephi.tools.api.MouseClickEventListener;
import org.gephi.tools.api.Tool;
import org.gephi.tools.api.ToolEventListener;
import org.gephi.ui.tools.ToolUI;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Mathieu Bastian
 */
public class NodePencil implements Tool {

    private ToolEventListener[] listeners;

    public ToolEventListener[] getListeners() {
        listeners = new ToolEventListener[1];
        listeners[0] = new MouseClickEventListener() {

            public void mouseClick(int[] positionViewport, float[] position3d) {
                GraphController gc = Lookup.getDefault().lookup(GraphController.class);
                DirectedGraph graph = gc.getVisibleDirectedGraph();
                Node node = gc.factory().newNode();
                node.getNodeData().setX(position3d[0]);
                node.getNodeData().setY(position3d[1]);
                graph.addNode(node);
                System.out.println("addNode "+position3d[0]+" - "+position3d[1]);
            }
        };
        return listeners;
    }

    public ToolUI getUI() {
        return new ToolUI() {

            public JPanel getPropertiesBar(Tool tool) {
                JPanel panel = new JPanel();
                return panel;
            }

            public String getName() {
                return NbBundle.getMessage(NodePencil.class, "NodePencil.name");
            }

            public Icon getIcon() {
                return new ImageIcon(getClass().getResource("/org/gephi/tools/resources/nodepencil.png"));
            }

            public String getDescription() {
                return NbBundle.getMessage(NodePencil.class, "NodePencil.description");
            }

            public int getPosition() {
                return 120;
            }
        };
    }
}
