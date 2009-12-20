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

import org.gephi.tools.spi.ToolSelectionType;
import org.gephi.ui.tools.BrushPanel;
import java.awt.Color;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.tools.spi.NodePressingEventListener;
import org.gephi.tools.spi.Tool;
import org.gephi.tools.spi.ToolEventListener;
import org.gephi.tools.spi.ToolUI;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Mathieu Bastian
 */
@ServiceProvider(service=Tool.class)
public class Brush implements Tool {

    //Architecture
    private ToolEventListener[] listeners;
    private BrushPanel brushPanel;
    //Settings
    private float[] color = {1f, 0f, 0f};
    private float intensity = 0.1f;
    private DiffusionMethods.DiffusionMethod diffusionMethod = DiffusionMethods.DiffusionMethod.NEIGHBORS;

    public void select() {
    }

    public void unselect() {
        listeners = null;
        brushPanel = null;
    }

    private void brush(Node[] nodes) {

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

        for (Node node : getDiffusedNodes(nodes)) {
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

    private Node[] getDiffusedNodes(Node[] input) {
        GraphModel model = Lookup.getDefault().lookup(GraphController.class).getModel();
        switch (diffusionMethod) {
            case NEIGHBORS:
                return DiffusionMethods.getNeighbors(model.getGraphVisible(), input);
            case NEIGHBORS_OF_NEIGHBORS:
                return DiffusionMethods.getNeighborsOfNeighbors(model.getGraphVisible(), input);
            case PREDECESSORS:
                if (model.isDirected()) {
                    return DiffusionMethods.getPredecessors(model.getDirectedGraphVisible(), input);
                } else {
                    return DiffusionMethods.getNeighbors(model.getGraphVisible(), input);
                }
            case SUCCESSORS:
                if (model.isDirected()) {
                    return DiffusionMethods.getSuccessors(model.getDirectedGraphVisible(), input);
                } else {
                    return DiffusionMethods.getNeighbors(model.getGraphVisible(), input);
                }
        }
        return new Node[0];
    }

    public ToolEventListener[] getListeners() {
        listeners = new ToolEventListener[1];
        listeners[0] = new NodePressingEventListener() {

            public void pressingNodes(Node[] nodes) {
                System.out.println("-------Nodes pressed");
                diffusionMethod = brushPanel.getDiffusionMethod();
                color = brushPanel.getColor().getColorComponents(color);
                intensity = brushPanel.getIntensity();
                brush(nodes);
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
                brushPanel = new BrushPanel();
                brushPanel.setDiffusionMethod(diffusionMethod);
                brushPanel.setColor(new Color(color[0], color[1], color[2]));
                brushPanel.setIntensity(intensity);
                return brushPanel;
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

    public ToolSelectionType getSelectionType() {
        return ToolSelectionType.SELECTION;
    }
}
