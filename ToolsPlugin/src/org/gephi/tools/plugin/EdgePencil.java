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
package org.gephi.tools.plugin;

import java.awt.Color;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import org.gephi.graph.api.DirectedGraph;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.Node;
import org.gephi.tools.spi.MouseClickEventListener;
import org.gephi.tools.spi.NodeClickEventListener;
import org.gephi.tools.spi.Tool;
import org.gephi.tools.spi.ToolEventListener;
import org.gephi.tools.spi.ToolSelectionType;
import org.gephi.tools.spi.ToolUI;
import org.gephi.ui.tools.plugin.EdgePencilPanel;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Mathieu Bastian
 */
@ServiceProvider(service = Tool.class)
public class EdgePencil implements Tool {

    //Architecture
    private ToolEventListener[] listeners;
    private EdgePencilPanel edgePencilPanel;
    //Settings
    private Color color;
    private float weight;
    //State
    private Node sourceNode;

    public EdgePencil() {
        //Default settings
        color = Color.BLACK;
        weight = 1f;
    }

    public void select() {
    }

    public void unselect() {
        listeners = null;
        sourceNode = null;
        edgePencilPanel = null;
    }

    public ToolEventListener[] getListeners() {
        listeners = new ToolEventListener[2];
        listeners[0] = new NodeClickEventListener() {

            public void clickNodes(Node[] nodes) {
                Node n = nodes[0];
                if (sourceNode == null) {
                    sourceNode = n;
                    edgePencilPanel.setStatus(NbBundle.getMessage(EdgePencil.class, "EdgePencil.status2"));
                } else {
                    color = edgePencilPanel.getColor();
                    weight = edgePencilPanel.getWeight();
                    GraphController gc = Lookup.getDefault().lookup(GraphController.class);
                    Graph graph = gc.getModel().getGraph();
                    boolean directed = graph instanceof DirectedGraph;
                    Edge edge = gc.getModel().factory().newEdge(sourceNode, n, weight, directed);
                    edge.getEdgeData().setR(color.getRed() / 255f);
                    edge.getEdgeData().setG(color.getGreen() / 255f);
                    edge.getEdgeData().setB(color.getBlue() / 255f);
                    graph.addEdge(edge);
                    sourceNode = null;
                    edgePencilPanel.setStatus(NbBundle.getMessage(EdgePencil.class, "EdgePencil.status1"));
                }
            }
        };
        listeners[1] = new MouseClickEventListener() {

            public void mouseClick(int[] positionViewport, float[] position3d) {
                if (sourceNode != null) {
                    //Cancel
                    edgePencilPanel.setStatus(NbBundle.getMessage(EdgePencil.class, "EdgePencil.status1"));
                    sourceNode = null;
                }
            }
        };
        return listeners;
    }

    public ToolUI getUI() {
        return new ToolUI() {

            public JPanel getPropertiesBar(Tool tool) {
                edgePencilPanel = new EdgePencilPanel();
                edgePencilPanel.setColor(color);
                edgePencilPanel.setWeight(weight);
                edgePencilPanel.setStatus(NbBundle.getMessage(EdgePencil.class, "EdgePencil.status1"));
                return edgePencilPanel;
            }

            public String getName() {
                return NbBundle.getMessage(EdgePencil.class, "EdgePencil.name");
            }

            public Icon getIcon() {
                return new ImageIcon(getClass().getResource("/org/gephi/tools/plugin/resources/edgepencil.png"));
            }

            public String getDescription() {
                return NbBundle.getMessage(EdgePencil.class, "EdgePencil.description");
            }

            public int getPosition() {
                return 130;
            }
        };
    }

    public ToolSelectionType getSelectionType() {
        return ToolSelectionType.SELECTION;
    }
}
