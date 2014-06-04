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
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.tools.spi.MouseClickEventListener;
import org.gephi.tools.spi.Tool;
import org.gephi.tools.spi.ToolEventListener;
import org.gephi.tools.spi.ToolSelectionType;
import org.gephi.tools.spi.ToolUI;
import org.gephi.ui.tools.plugin.NodePencilPanel;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Mathieu Bastian
 */
@ServiceProvider(service = Tool.class)
public class NodePencil implements Tool {

    //Architecture
    private ToolEventListener[] listeners;
    private NodePencilPanel nodePencilPanel;
    //Settings
    private Color color;
    private float size;

    public NodePencil() {
        //Default settings
        color = new Color(153, 153, 153);//Default gray of nodes
        size = 10f;
    }

    @Override
    public void select() {
    }

    @Override
    public void unselect() {
        listeners = null;
        nodePencilPanel = null;
    }

    @Override
    public ToolEventListener[] getListeners() {
        listeners = new ToolEventListener[1];
        listeners[0] = new MouseClickEventListener() {
            @Override
            public void mouseClick(int[] positionViewport, float[] position3d) {
                color = nodePencilPanel.getColor();
                size = nodePencilPanel.getNodeSize();
                GraphController gc = Lookup.getDefault().lookup(GraphController.class);
                GraphModel gm = gc.getGraphModel();
                Graph graph = gm.getGraph();
                Node node = gm.factory().newNode();
                node.setX(position3d[0]);
                node.setY(position3d[1]);
                node.setSize(size);
                node.setColor(color);
                graph.addNode(node);
            }
        };
        return listeners;
    }

    @Override
    public ToolUI getUI() {
        return new ToolUI() {
            @Override
            public JPanel getPropertiesBar(Tool tool) {
                nodePencilPanel = new NodePencilPanel();
                nodePencilPanel.setColor(color);
                nodePencilPanel.setNodeSize(size);
                return nodePencilPanel;
            }

            @Override
            public String getName() {
                return NbBundle.getMessage(NodePencil.class, "NodePencil.name");
            }

            @Override
            public Icon getIcon() {
                return new ImageIcon(getClass().getResource("/org/gephi/tools/plugin/resources/nodepencil.png"));
            }

            @Override
            public String getDescription() {
                return NbBundle.getMessage(NodePencil.class, "NodePencil.description");
            }

            @Override
            public int getPosition() {
                return 120;
            }
        };
    }

    @Override
    public ToolSelectionType getSelectionType() {
        return ToolSelectionType.NONE;
    }
}
