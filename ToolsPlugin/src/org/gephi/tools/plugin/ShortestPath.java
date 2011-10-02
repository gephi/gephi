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
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import org.gephi.algorithms.shortestpath.AbstractShortestPathAlgorithm;
import org.gephi.algorithms.shortestpath.BellmanFordShortestPathAlgorithm;
import org.gephi.algorithms.shortestpath.DijkstraShortestPathAlgorithm;
import org.gephi.graph.api.DirectedGraph;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.Node;
import org.gephi.tools.spi.MouseClickEventListener;
import org.gephi.tools.spi.NodeClickEventListener;
import org.gephi.tools.spi.Tool;
import org.gephi.tools.spi.ToolEventListener;
import org.gephi.tools.spi.ToolSelectionType;
import org.gephi.ui.tools.plugin.ShortestPathPanel;
import org.gephi.tools.spi.ToolUI;
import org.gephi.visualization.VizController;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Mathieu Bastian
 */
@ServiceProvider(service = Tool.class)
public class ShortestPath implements Tool {

    //Architecture
    private ToolEventListener[] listeners;
    private ShortestPathPanel shortestPathPanel;
    //Settings
    private Color color;
    private boolean settingEdgeSourceColor;
    //State
    private Node sourceNode;

    public ShortestPath() {
        //Default settings
        color = Color.RED;
    }

    public void select() {
        settingEdgeSourceColor = !VizController.getInstance().getVizModel().isEdgeHasUniColor();
        VizController.getInstance().getVizModel().setEdgeHasUniColor(true);
        VizController.getInstance().getVizConfig().setEnableAutoSelect(false);
    }

    public void unselect() {
        listeners = null;
        sourceNode = null;
        shortestPathPanel = null;
        VizController.getInstance().getVizModel().setEdgeHasUniColor(settingEdgeSourceColor);
        VizController.getInstance().getVizConfig().setEnableAutoSelect(true);
    }

    public ToolEventListener[] getListeners() {
        listeners = new ToolEventListener[2];
        listeners[0] = new NodeClickEventListener() {

            public void clickNodes(Node[] nodes) {
                Node n = nodes[0];
                if (sourceNode == null) {
                    sourceNode = n;
                    shortestPathPanel.setResult("");
                    shortestPathPanel.setStatus(NbBundle.getMessage(ShortestPath.class, "ShortestPath.status2"));
                } else if (n != sourceNode) {
                    color = shortestPathPanel.getColor();
                    float[] colorArray = new float[]{color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f};
                    Node targetNode = n;
                    GraphController gc = Lookup.getDefault().lookup(GraphController.class);

                    AbstractShortestPathAlgorithm algorithm;
                    if (gc.getModel().getGraphVisible() instanceof DirectedGraph) {
                        algorithm = new BellmanFordShortestPathAlgorithm((DirectedGraph) gc.getModel().getGraphVisible(), sourceNode);
                    } else {
                        algorithm = new DijkstraShortestPathAlgorithm(gc.getModel().getGraphVisible(), sourceNode);
                    }
                    algorithm.compute();

                    double distance;
                    if ((distance = algorithm.getDistances().get(targetNode)) != Double.POSITIVE_INFINITY) {
                        targetNode.getNodeData().setColor(colorArray[0], colorArray[1], colorArray[2]);
                        VizController.getInstance().selectNode(targetNode);
                        Edge predecessorEdge = algorithm.getPredecessorIncoming(targetNode);
                        Node predecessor = algorithm.getPredecessor(targetNode);
                        while (predecessorEdge != null && predecessor != sourceNode) {
                            predecessorEdge.getEdgeData().setColor(colorArray[0], colorArray[1], colorArray[2]);
                            VizController.getInstance().selectEdge(predecessorEdge);
                            predecessor.getNodeData().setColor(colorArray[0], colorArray[1], colorArray[2]);
                            VizController.getInstance().selectNode(predecessor);
                            predecessorEdge = algorithm.getPredecessorIncoming(predecessor);
                            predecessor = algorithm.getPredecessor(predecessor);
                        }
                        predecessorEdge.getEdgeData().setColor(colorArray[0], colorArray[1], colorArray[2]);
                        VizController.getInstance().selectEdge(predecessorEdge);
                        sourceNode.getNodeData().setColor(colorArray[0], colorArray[1], colorArray[2]);
                        VizController.getInstance().selectNode(sourceNode);
                        shortestPathPanel.setResult(NbBundle.getMessage(ShortestPath.class, "ShortestPath.result", distance));
                    } else {
                        //No path
                        shortestPathPanel.setResult(NbBundle.getMessage(ShortestPath.class, "ShortestPath.noresult"));
                    }

                    sourceNode = null;
                    shortestPathPanel.setStatus(NbBundle.getMessage(ShortestPath.class, "ShortestPath.status1"));
                }
            }
        };
        listeners[1] = new MouseClickEventListener() {

            public void mouseClick(int[] positionViewport, float[] position3d) {
                if (sourceNode != null) {
                    //Cancel
                    shortestPathPanel.setStatus(NbBundle.getMessage(ShortestPath.class, "ShortestPath.status1"));
                    sourceNode = null;
                } else {
                    VizController.getInstance().resetSelection();
                }
            }
        };
        return listeners;
    }

    public ToolUI getUI() {
        return new ToolUI() {

            public JPanel getPropertiesBar(Tool tool) {
                shortestPathPanel = new ShortestPathPanel();
                shortestPathPanel.setColor(color);
                shortestPathPanel.setStatus(NbBundle.getMessage(ShortestPath.class, "ShortestPath.status1"));
                return shortestPathPanel;
            }

            public String getName() {
                return NbBundle.getMessage(ShortestPath.class, "ShortestPath.name");
            }

            public Icon getIcon() {
                return new ImageIcon(getClass().getResource("/org/gephi/tools/plugin/resources/shortestpath.png"));
            }

            public String getDescription() {
                return NbBundle.getMessage(ShortestPath.class, "ShortestPath.description");
            }

            public int getPosition() {
                return 140;
            }
        };
    }

    public ToolSelectionType getSelectionType() {
        return ToolSelectionType.SELECTION;
    }
}
