/*
 Copyright 2008-2024 Gephi
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

 Portions Copyrighted 2024 Gephi Consortium.
 */

package org.gephi.tools.plugin;

import org.gephi.graph.api.Node;
import org.gephi.tools.spi.*;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

import javax.swing.*;

/**
 * @author Eduardo Ramos
 */
@ServiceProvider(service = Tool.class)
public class NodesDragger implements Tool {

    private ToolEventListener[] listeners;
    //Vars
    private Node[] nodes;
    private float[] initialX;
    private float[] initialY;

    @Override
    public void select() {
    }

    @Override
    public void unselect() {
        listeners = null;
        nodes = null;
    }

    @Override
    public ToolEventListener[] getListeners() {
        listeners = new ToolEventListener[1];
        listeners[0] = new NodePressAndDraggingEventListener() {
            @Override
            public boolean pressNodes(Node[] nodes) {
                NodesDragger.this.nodes = nodes;

                initialX = new float[nodes.length];
                initialY = new float[nodes.length];
                for (int i = 0; i < nodes.length; i++) {
                    Node n = nodes[i];
                    initialX[i] = n.x();
                    initialY[i] = n.y();
                }

                return true;
            }

            @Override
            public void released() {
                nodes = null;
            }

            @Override
            public boolean drag(float displacementXScreen, float displacementYScreen,
                                float displacementXWorld, float displacementYWorld) {
                if (nodes != null && nodes.length > 0) {
                    for (int i = 0; i < nodes.length; i++) {
                        Node n = nodes[i];
                        n.setX(initialX[i] + displacementXWorld);
                        n.setY(initialY[i] + displacementYWorld);
                    }

                    return true;
                }

                return false;
            }
        };
        return listeners;
    }

    @Override
    public ToolUI getUI() {
        return new ToolUI() {
            @Override
            public JPanel getPropertiesBar(Tool tool) {
                return null;
            }

            @Override
            public String getName() {
                return NbBundle.getMessage(NodesDragger.class, "NodesDragger.name");
            }

            @Override
            public Icon getIcon() {
                return ImageUtilities.loadImageIcon("ToolsPlugin/hand.png", false);
            }

            @Override
            public String getDescription() {
                return NbBundle.getMessage(NodesDragger.class, "NodesDragger.description");
            }

            @Override
            public int getPosition() {
                return 0;
            }
        };

    }

    @Override
    public ToolSelectionType getSelectionType() {
        return ToolSelectionType.SELECTION_AND_DRAGGING;
    }
}
