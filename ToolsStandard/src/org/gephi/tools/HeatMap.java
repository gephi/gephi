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
import java.awt.LinearGradientPaint;
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
import org.gephi.ui.utils.GradientUtils.LinearGradient;
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
    private Color[] gradientColors;
    private float[] gradientPositions;  //All between 0 and 1

    public HeatMap() {
        //Default settings
        gradientColors = new Color[]{new Color(254, 232, 200), new Color(253, 187, 132), new Color(227, 74, 51)};
        gradientPositions = new float[]{0f, 0.5f, 1f};
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
                try {
                    Node n = nodes[0];
                    gradientColors = heatMapPanel.getGradientColors();
                    gradientPositions = heatMapPanel.getGradientPositions();
                    GraphController gc = Lookup.getDefault().lookup(GraphController.class);
                    DirectedGraph graph = gc.getVisibleDirectedGraph();

                    //Color
                    LinearGradient linearGradient = new LinearGradient(gradientColors, gradientPositions);

                    //Algorithm
                    BellmanFordShortestPathAlgorithm algorithm = new BellmanFordShortestPathAlgorithm(graph, n);
                    algorithm.compute();

                    double maxDistance = algorithm.getMaxDistance() + 1;  //+1 to have the maxdistance nodes a ratio<1
                    if (maxDistance > 0) {
                        for (Entry<Node, Double> entry : algorithm.getDistances().entrySet()) {
                            NodeData node = entry.getKey().getNodeData();
                            if (!Double.isInfinite(entry.getValue())) {
                                float ratio = (float) (entry.getValue() / maxDistance);
                                Color c = linearGradient.getValue(ratio);
                                node.setColor(c.getRed() / 255f, c.getGreen() / 255f, c.getBlue() / 255f);
                            } else {
                                Color c = gradientColors[gradientColors.length - 1];
                                node.setColor(c.getRed() / 255f, c.getGreen() / 255f, c.getBlue() / 255f);
                            }
                        }
                    }
                    Color c = gradientColors[0];
                    n.getNodeData().setColor(c.getRed() / 255f, c.getGreen() / 255f, c.getBlue() / 255f);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        };
        return listeners;
    }

    public ToolUI getUI() {
        return new ToolUI() {

            public JPanel getPropertiesBar(Tool tool) {
                heatMapPanel = new HeatMapPanel(gradientColors, gradientPositions);
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
