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
import java.text.DecimalFormat;
import java.util.Map.Entry;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import org.gephi.algorithms.shortestpath.BellmanFordShortestPathAlgorithm;
import org.gephi.graph.api.DirectedGraph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.NodeData;
import org.gephi.tools.spi.NodeClickEventListener;
import org.gephi.tools.spi.Tool;
import org.gephi.tools.spi.ToolEventListener;
import org.gephi.tools.spi.ToolSelectionType;
import org.gephi.tools.spi.ToolUI;
import org.gephi.ui.tools.plugin.HeatMapPanel;
import org.gephi.ui.utils.GradientUtils.LinearGradient;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Mathieu Bastian
 */
@ServiceProvider(service=Tool.class)
public class HeatMap implements Tool {
    //Architecture

    private ToolEventListener[] listeners;
    private HeatMapPanel heatMapPanel;
    //Settings
    private Color[] gradientColors;
    private float[] gradientPositions;  //All between 0 and 1
    private boolean dontPaintUnreachable = true;

    public HeatMap() {
        //Default settings
        gradientColors = new Color[]{new Color(227, 74, 51), new Color(253, 187, 132), new Color(254, 232, 200)};
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
                    Color[] colors;
                    float[] positions;
                    if (heatMapPanel.isUsePalette()) {
                        colors = heatMapPanel.getSelectedPalette().getColors();
                        positions = heatMapPanel.getSelectedPalette().getPositions();
                        dontPaintUnreachable = true;
                    } else {
                        gradientColors = colors = heatMapPanel.getGradientColors();
                        gradientPositions = positions = heatMapPanel.getGradientPositions();
                        dontPaintUnreachable = heatMapPanel.isDontPaintUnreachable();
                    }
                    GraphController gc = Lookup.getDefault().lookup(GraphController.class);
                    DirectedGraph graph = null;
                    if (gc.getModel().getGraph() instanceof DirectedGraph) {
                        graph = (DirectedGraph) gc.getModel().getGraph();
                    } else {
                        return;
                    }

                    //Color
                    LinearGradient linearGradient = new LinearGradient(colors, positions);

                    //Algorithm
                    BellmanFordShortestPathAlgorithm algorithm = new BellmanFordShortestPathAlgorithm(graph, n);
                    algorithm.compute();

                    double maxDistance = algorithm.getMaxDistance();
                    if (!dontPaintUnreachable) {
                        maxDistance++;   //+1 to have the maxdistance nodes a ratio<1
                    }
                    if (maxDistance > 0) {
                        for (Entry<Node, Double> entry : algorithm.getDistances().entrySet()) {
                            NodeData node = entry.getKey().getNodeData();
                            if (!Double.isInfinite(entry.getValue())) {
                                float ratio = (float) (entry.getValue() / maxDistance);
                                Color c = linearGradient.getValue(ratio);
                                node.setColor(c.getRed() / 255f, c.getGreen() / 255f, c.getBlue() / 255f);
                            } else if (!dontPaintUnreachable) {
                                Color c = colors[colors.length - 1];
                                node.setColor(c.getRed() / 255f, c.getGreen() / 255f, c.getBlue() / 255f);
                            }
                        }
                    }
                    Color c = colors[0];
                    n.getNodeData().setColor(c.getRed() / 255f, c.getGreen() / 255f, c.getBlue() / 255f);
                    heatMapPanel.setStatus(NbBundle.getMessage(HeatMap.class, "HeatMap.status.maxdistance") + new DecimalFormat("#.##").format(algorithm.getMaxDistance()));
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
                heatMapPanel = new HeatMapPanel(gradientColors, gradientPositions, dontPaintUnreachable);
                return heatMapPanel;
            }

            public String getName() {
                return NbBundle.getMessage(HeatMap.class, "HeatMap.name");
            }

            public Icon getIcon() {
                return new ImageIcon(getClass().getResource("/org/gephi/tools/plugin/resources/heatmap.png"));
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
