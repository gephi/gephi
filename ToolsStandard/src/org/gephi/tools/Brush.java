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
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.gephi.graph.api.Node;
import org.gephi.tools.api.NodePressingEventListener;
import org.gephi.tools.api.Tool;
import org.gephi.tools.api.ToolEventListener;
import org.gephi.ui.tools.ToolUI;
import org.openide.util.NbBundle;

/**
 *
 * @author Mathieu Bastian
 */
public class Brush implements Tool {

    private ToolEventListener[] listeners;

    public ToolEventListener[] getListeners() {
        listeners = new ToolEventListener[1];
        listeners[0] = new NodePressingEventListener() {

            public void pressNodes(Node[] nodes) {
                System.out.println("-------Nodes pressed");
                for (int i = 0; i < nodes.length; i++) {
                    System.out.println(nodes[i].getNodeData().getLabel());
                }
            }

            public void pressing() {
                System.out.println("--pressing");
            }

            public void released() {
                System.out.println("-----released");
            }
        };
        return listeners;
    }

    public ToolUI getUI() {
        return new ToolUI() {

            public JPanel getPropertiesBar(Tool tool) {
                JLabel lbl = new JLabel("brusher");
                JPanel pnl = new JPanel();
                pnl.add(lbl);
                return pnl;
            }

            public String getName() {
                return NbBundle.getMessage(Brush.class, "Brush.name");
            }

            public Icon getIcon() {
                return new ImageIcon(getClass().getResource("/org/gephi/tools/resources/brush.png"));
            }

            public String getDescription() {
                return NbBundle.getMessage(Painter.class, "Brush.description");
            }

            public int getPosition() {
                return 110;
            }
        };
    }
}
