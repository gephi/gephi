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
import java.util.Map.Entry;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import org.gephi.algorithms.shortestpath.BellmanFordShortestPathAlgorithm;
import org.gephi.graph.api.DirectedGraph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.NodeData;
import org.gephi.tools.api.NodeClickEventListener;
import org.gephi.tools.api.Tool;
import org.gephi.tools.api.ToolEventListener;
import org.gephi.tools.api.ToolSelectionType;
import org.gephi.ui.tools.HeatMapPanel;
import org.gephi.ui.tools.ToolUI;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Mathieu Bastian
 */
public class HeatMap implements Tool {
    //Architecture

    private ToolEventListener[] listeners;
    private HeatMapPanel heatMapPanel;

    //Settings
    private Color startColor;
    private Color stopColor;

    public HeatMap() {
        //Default settings
        startColor = Color.RED;
        stopColor = new Color(0.95f, 0.95f, 0.95f);
    }

    public void select() {
    }

    public void unselect() {
        listeners = null;
        heatMapPanel = null;
    }

    public ToolEventListener[] getListeners() {
        listeners = new ToolEventListener[1];
        listeners[0] = new NodeClickEventListener() {

            public void clickNodes(Node[] nodes) {
                Node n = nodes[0];
                startColor = heatMapPanel.getColor1();
                stopColor = heatMapPanel.getColor2();
                float[] color1 = new float[]{startColor.getRed() / 255f, startColor.getGreen() / 255f, startColor.getBlue() / 255f};
                float[] color2 = new float[]{stopColor.getRed() / 255f, stopColor.getGreen() / 255f, stopColor.getBlue() / 255f};
                GraphController gc = Lookup.getDefault().lookup(GraphController.class);
                DirectedGraph graph = gc.getVisibleDirectedGraph();

                BellmanFordShortestPathAlgorithm algorithm = new BellmanFordShortestPathAlgorithm(graph, n);
                algorithm.compute();
                double maxDistance = algorithm.getMaxDistance() + 1;  //+1 to have the maxdistance nodes a ratio<1
                if (maxDistance > 0) {
                    for (Entry<Node, Double> entry : algorithm.getDistances().entrySet()) {
                        NodeData node = entry.getKey().getNodeData();
                        if (entry.getValue() != Double.POSITIVE_INFINITY) {
                            float ratio = (float) (entry.getValue() / maxDistance);
                            node.setR(color1[0] + (color2[0] - color1[0]) * ratio);
                            node.setG(color1[1] + (color2[1] - color1[1]) * ratio);
                            node.setB(color1[2] + (color2[2] - color1[2]) * ratio);
                        } else {
                            node.setColor(color2[0], color2[1], color2[2]);
                        }
                    }
                }
                n.getNodeData().setColor(color1[0], color1[1], color1[2]);
            }
        };
        return listeners;
    }

    public ToolUI getUI() {
        return new ToolUI() {

            public JPanel getPropertiesBar(Tool tool) {
                heatMapPanel = new HeatMapPanel(startColor, stopColor);
                return heatMapPanel;
            }

            public String getName() {
                return NbBundle.getMessage(ShortestPath.class, "HeatMap.name");
            }

            public Icon getIcon() {
                return new ImageIcon(getClass().getResource("/org/gephi/tools/resources/heatmap.png"));
            }

            public String getDescription() {
                return NbBundle.getMessage(ShortestPath.class, "HeatMap.description");
            }

            public int getPosition() {
                return 150;
            }
        };
    }

    public ToolSelectionType getSelectionType() {
        return ToolSelectionType.SELECTION;
    }
}
