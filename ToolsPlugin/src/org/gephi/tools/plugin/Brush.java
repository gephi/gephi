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

import org.gephi.tools.spi.ToolSelectionType;
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
import org.gephi.ui.tools.plugin.BrushPanel;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Mathieu Bastian
 */
@ServiceProvider(service = Tool.class)
public class Brush implements Tool {

    //Architecture
    private ToolEventListener[] listeners;
    private BrushPanel brushPanel;
    //Settings
    private float[] color = {1f, 0f, 0f};
    private float intensity = 0.3f;
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
                diffusionMethod = brushPanel.getDiffusionMethod();
                color = brushPanel.getColor().getColorComponents(color);
                intensity = brushPanel.getIntensity();
                brush(nodes);
            }

            public void released() {
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
                return new ImageIcon(getClass().getResource("/org/gephi/tools/plugin/resources/brush.png"));
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
