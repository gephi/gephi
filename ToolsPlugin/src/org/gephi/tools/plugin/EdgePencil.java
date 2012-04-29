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
import org.gephi.graph.api.DirectedGraph;
import org.gephi.graph.api.MixedGraph;
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
                GraphController gc = Lookup.getDefault().lookup(GraphController.class);
                Graph graph = gc.getModel().getGraph();
                if ( graph instanceof MixedGraph ) {
                    edgePencilPanel.enableTypeCombo(true);
                    edgePencilPanel.setType(true);
                } else {
                    edgePencilPanel.enableTypeCombo(false);
                    edgePencilPanel.setType(graph instanceof DirectedGraph);
                }
                    
                if (sourceNode == null) {
                    sourceNode = n;
                    edgePencilPanel.setStatus(NbBundle.getMessage(EdgePencil.class, "EdgePencil.status2"));
                } else {
                    color = edgePencilPanel.getColor();
                    weight = edgePencilPanel.getWeight();
                    boolean directed = edgePencilPanel.isDirected;
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
                GraphController gc = Lookup.getDefault().lookup(GraphController.class);
                Graph graph = gc.getModel().getGraph();
                if ( graph instanceof MixedGraph ) {
                    edgePencilPanel.enableTypeCombo(true);
                } else {
                    edgePencilPanel.enableTypeCombo(false);
                    edgePencilPanel.setType(graph instanceof DirectedGraph);
                }
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
