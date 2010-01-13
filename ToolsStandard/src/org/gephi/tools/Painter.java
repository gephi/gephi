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

import java.awt.Color;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import org.gephi.graph.api.Node;
import org.gephi.tools.spi.NodePressingEventListener;
import org.gephi.tools.spi.Tool;
import org.gephi.tools.spi.ToolEventListener;
import org.gephi.tools.spi.ToolSelectionType;
import org.gephi.tools.spi.ToolUI;
import org.gephi.ui.tools.PainterPanel;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Mathieu Bastian
 */
@ServiceProvider(service = Tool.class)
public class Painter implements Tool {

    private ToolEventListener[] listeners;
    private PainterPanel painterPanel;
    //Settings
    private float[] color = {1f, 0f, 0f};
    private float intensity = 0.3f;

    public void select() {
    }

    public void unselect() {
        listeners = null;
        painterPanel = null;
    }

    public ToolEventListener[] getListeners() {
        listeners = new ToolEventListener[1];
        listeners[0] = new NodePressingEventListener() {

            public void pressingNodes(Node[] nodes) {
                color = painterPanel.getColor().getColorComponents(color);
                for (Node node : nodes) {
                    float r = node.getNodeData().r();
                    float g = node.getNodeData().g();
                    float b = node.getNodeData().b();
                    r = intensity * color[0] + (1 - intensity) * r;
                    g = intensity * color[1] + (1 - intensity) * g;
                    b = intensity * color[2] + (1 - intensity) * b;
                    node.getNodeData().setR(r);
                    node.getNodeData().setG(g);
                    node.getNodeData().setB(b);
                }
            }

            public void released() {
            }
        };
        return listeners;
    }

    public ToolUI getUI() {
        return new ToolUI() {

            public JPanel getPropertiesBar(Tool tool) {
                painterPanel = new PainterPanel();
                painterPanel.setColor(new Color(color[0], color[1], color[2]));
                return painterPanel;
            }

            public String getName() {
                return NbBundle.getMessage(Painter.class, "Painter.name");
            }

            public Icon getIcon() {
                return new ImageIcon(getClass().getResource("/org/gephi/tools/resources/painter.png"));
            }

            public String getDescription() {
                return NbBundle.getMessage(Painter.class, "Painter.description");
            }

            public int getPosition() {
                return 100;
            }
        };
    }

    public ToolSelectionType getSelectionType() {
        return ToolSelectionType.SELECTION;
    }
}
