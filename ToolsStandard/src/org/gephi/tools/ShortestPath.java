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
import org.gephi.algorithms.shortestpath.BellmanFordShortestPathAlgorithm;
import org.gephi.graph.api.DirectedGraph;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.Node;
import org.gephi.tools.api.MouseClickEventListener;
import org.gephi.tools.api.NodeClickEventListener;
import org.gephi.tools.api.Tool;
import org.gephi.tools.api.ToolEventListener;
import org.gephi.tools.api.ToolSelectionType;
import org.gephi.ui.tools.ShortestPathPanel;
import org.gephi.ui.tools.ToolUI;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Mathieu Bastian
 */
public class ShortestPath implements Tool {

    //Architecture
    private ToolEventListener[] listeners;
    private ShortestPathPanel shortestPathPanel;

    //Settings
    private Color color;

    //State
    private Node sourceNode;

    public ShortestPath() {
        //Default settings
        color = Color.RED;
    }

    public void select() {
    }

    public void unselect() {
        listeners = null;
        sourceNode = null;
        shortestPathPanel = null;
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
                } else {
                    float[] colorArray = new float[]{color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f};
                    Node targetNode = n;
                    GraphController gc = Lookup.getDefault().lookup(GraphController.class);
                    DirectedGraph graph = gc.getVisibleDirectedGraph();

                    BellmanFordShortestPathAlgorithm algorithm = new BellmanFordShortestPathAlgorithm(graph, sourceNode);
                    algorithm.compute();
                    double distance;
                    if ((distance = algorithm.getDistances().get(targetNode)) != Double.POSITIVE_INFINITY) {
                        targetNode.getNodeData().setColor(colorArray[0], colorArray[1], colorArray[2]);
                        Edge predecessorEdge = algorithm.getPredecessorIncoming(targetNode);
                        while (predecessorEdge != null && predecessorEdge.getSource() != sourceNode) {
                            predecessorEdge.getEdgeData().setColor(colorArray[0], colorArray[1], colorArray[2]);
                            predecessorEdge.getSource().getNodeData().setColor(colorArray[0], colorArray[1], colorArray[2]);
                            predecessorEdge = algorithm.getPredecessorIncoming(predecessorEdge.getSource());
                        }
                        predecessorEdge.getEdgeData().setColor(colorArray[0], colorArray[1], colorArray[2]);
                        sourceNode.getNodeData().setColor(colorArray[0], colorArray[1], colorArray[2]);
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
                }
            }
        };
        return listeners;
    }

    public ToolUI getUI() {
        return new ToolUI() {

            public JPanel getPropertiesBar(Tool tool) {
                shortestPathPanel = new ShortestPathPanel();
                shortestPathPanel.setStatus(NbBundle.getMessage(ShortestPath.class, "ShortestPath.status1"));
                return shortestPathPanel;
            }

            public String getName() {
                return NbBundle.getMessage(ShortestPath.class, "ShortestPath.name");
            }

            public Icon getIcon() {
                return new ImageIcon(getClass().getResource("/org/gephi/tools/resources/shortestpath.png"));
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
