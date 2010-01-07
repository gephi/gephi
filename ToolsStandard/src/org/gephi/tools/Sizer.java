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
import org.gephi.graph.api.Node;
import org.gephi.tools.spi.NodePressAndDraggingEventListener;
import org.gephi.tools.spi.Tool;
import org.gephi.tools.spi.ToolEventListener;
import org.gephi.tools.spi.ToolSelectionType;
import org.gephi.tools.spi.ToolUI;
import org.gephi.ui.tools.SizerPanel;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Mathieu Bastian
 */
@ServiceProvider(service = Tool.class)
public class Sizer implements Tool {

    private SizerPanel sizerPanel;
    private ToolEventListener[] listeners;
    private final float INTENSITY = 0.4f;
    private final float LIMIT = 0.1f;
    //Vars
    private Node[] nodes;
    private float[] sizes;

    public void select() {
    }

    public void unselect() {
        listeners = null;
        sizerPanel = null;
        nodes = null;
        sizes = null;
    }

    public ToolEventListener[] getListeners() {
        listeners = new ToolEventListener[1];
        listeners[0] = new NodePressAndDraggingEventListener() {

            public void pressNodes(Node[] nodes) {
                Sizer.this.nodes = nodes;
                sizes = new float[nodes.length];
                for (int i = 0; i < nodes.length; i++) {
                    Node n = nodes[i];
                    sizes[i] = n.getNodeData().getSize();
                }
            }

            public void released() {
                nodes = null;
            }

            public void drag(float displacementX, float displacementY) {
                if (nodes != null) {
                    for (int i = 0; i < nodes.length; i++) {
                        Node n = nodes[i];
                        float size = sizes[i];
                        size += displacementY * INTENSITY;
                        if (size < LIMIT) {
                            size = LIMIT;
                        }
                        n.getNodeData().setSize(size);
                    }
                }
            }
        };
        return listeners;
    }

    public ToolUI getUI() {
        return new ToolUI() {

            public JPanel getPropertiesBar(Tool tool) {
                sizerPanel = new SizerPanel();
                return sizerPanel;
            }

            public String getName() {
                return NbBundle.getMessage(Sizer.class, "Sizer.name");
            }

            public Icon getIcon() {
                return new ImageIcon(getClass().getResource("/org/gephi/tools/resources/painter.png"));
            }

            public String getDescription() {
                return NbBundle.getMessage(Sizer.class, "Sizer.description");
            }

            public int getPosition() {
                return 105;
            }
        };
    }

    public ToolSelectionType getSelectionType() {
        return ToolSelectionType.SELECTION_AND_DRAGGING;
    }
}

