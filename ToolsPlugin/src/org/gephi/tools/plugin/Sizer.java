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

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import org.gephi.graph.api.Node;
import org.gephi.tools.spi.NodePressAndDraggingEventListener;
import org.gephi.tools.spi.Tool;
import org.gephi.tools.spi.ToolEventListener;
import org.gephi.tools.spi.ToolSelectionType;
import org.gephi.tools.spi.ToolUI;
import org.gephi.ui.tools.plugin.SizerPanel;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Mathieu Bastian
 */
@ServiceProvider(service = Tool.class)
public class Sizer implements Tool {

    private SizerPanel sizerPanel;
    private ToolEventListener[] listeners;
    private final float INTENSITY = 0.4f;
    private final float LIMIT = 0.1f;
    //Vars
    private Node[] nodes;
    private float[] sizes;

    public void select() {
    }

    public void unselect() {
        listeners = null;
        sizerPanel = null;
        nodes = null;
        sizes = null;
    }

    public ToolEventListener[] getListeners() {
        listeners = new ToolEventListener[1];
        listeners[0] = new NodePressAndDraggingEventListener() {

            public void pressNodes(Node[] nodes) {
                Sizer.this.nodes = nodes;
                sizes = new float[nodes.length];
                for (int i = 0; i < nodes.length; i++) {
                    Node n = nodes[i];
                    sizes[i] = n.getNodeData().getSize();
                }
            }

            public void released() {
                nodes = null;
                sizerPanel.setAvgSize(-1);
            }

            public void drag(float displacementX, float displacementY) {
                if (nodes != null) {
                    float averageSize = 0f;
                    for (int i = 0; i < nodes.length; i++) {
                        Node n = nodes[i];
                        float size = sizes[i];
                        size += displacementY * INTENSITY;
                        if (size < LIMIT) {
                            size = LIMIT;
                        }
                        averageSize += size;
                        n.getNodeData().setSize(size);
                    }
                    averageSize /= nodes.length;
                    sizerPanel.setAvgSize(averageSize);
                }
            }
        };
        return listeners;
    }

    public ToolUI getUI() {
        return new ToolUI() {

            public JPanel getPropertiesBar(Tool tool) {
                sizerPanel = new SizerPanel();
                return sizerPanel;
            }

            public String getName() {
                return NbBundle.getMessage(Sizer.class, "Sizer.name");
            }

            public Icon getIcon() {
                return new ImageIcon(getClass().getResource("/org/gephi/tools/plugin/resources/sizer.png"));
            }

            public String getDescription() {
                return NbBundle.getMessage(Sizer.class, "Sizer.description");
            }

            public int getPosition() {
                return 105;
            }
        };
    }

    public ToolSelectionType getSelectionType() {
        return ToolSelectionType.SELECTION_AND_DRAGGING;
    }
}

