/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
Website : http://www.gephi.org

This file is part of Gephi.

DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

Copyright 2011 Gephi Consortium. All rights reserved.

The contents of this file are subject to the terms of either the GNU
General Public License Version 3 only ("GPL") or the Common
Development and Distribution License("CDDL") (collectively, the
"License"). You may not use this file except in compliance with the
License. You can obtain a copy of the License at
http://gephi.org/about/legal/license-notice/
or /cddl-1.0.txt and /gpl-3.0.txt. See the License for the
specific language governing permissions and limitations under the
License.  When distributing the software, include this License Header
Notice in each file and include the License files at
/cddl-1.0.txt and /gpl-3.0.txt. If applicable, add the following below the
License Header, with the fields enclosed by brackets [] replaced by
your own identifying information:
"Portions Copyrighted [year] [name of copyright owner]"

If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 3, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 3] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 3 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 3 code and therefore, elected the GPL
Version 3 license, then the option applies only if the new code is
made subject to such option by the copyright holder.

Contributor(s):

Portions Copyrighted 2011 Gephi Consortium.
*/
package org.gephi.tools.plugin;

import java.awt.Color;
import java.text.DecimalFormat;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import org.gephi.algorithms.shortestpath.AbstractShortestPathAlgorithm;
import org.gephi.algorithms.shortestpath.BellmanFordShortestPathAlgorithm;
import org.gephi.algorithms.shortestpath.DijkstraShortestPathAlgorithm;
import org.gephi.graph.api.DirectedGraph;
import org.gephi.graph.api.Graph;
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
@ServiceProvider(service = Tool.class)
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

                    AbstractShortestPathAlgorithm algorithm;
                    if (gc.getModel().getGraphVisible() instanceof DirectedGraph) {
                        DirectedGraph graph = (DirectedGraph) gc.getModel().getGraphVisible();
                        algorithm = new BellmanFordShortestPathAlgorithm(graph, n);
                        algorithm.compute();
                    } else {
                        Graph graph = gc.getModel().getGraphVisible();
                        algorithm = new DijkstraShortestPathAlgorithm(graph, n);
                        algorithm.compute();
                    }

                    //Color
                    LinearGradient linearGradient = new LinearGradient(colors, positions);

                    //Algorithm


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
                    Logger.getLogger("").log(Level.SEVERE, "", e);
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
