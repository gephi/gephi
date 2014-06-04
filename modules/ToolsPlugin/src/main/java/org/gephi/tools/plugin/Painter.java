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
import org.gephi.graph.api.Node;
import org.gephi.tools.spi.NodePressingEventListener;
import org.gephi.tools.spi.Tool;
import org.gephi.tools.spi.ToolEventListener;
import org.gephi.tools.spi.ToolSelectionType;
import org.gephi.tools.spi.ToolUI;
import org.gephi.ui.tools.plugin.PainterPanel;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Mathieu Bastian
 */
@ServiceProvider(service = Tool.class)
public class Painter implements Tool {

    private ToolEventListener[] listeners;
    private PainterPanel painterPanel;
    //Settings
    private float[] color = {1f, 0f, 0f};
    private float intensity = 0.3f;

    @Override
    public void select() {
    }

    @Override
    public void unselect() {
        listeners = null;
        painterPanel = null;
    }

    @Override
    public ToolEventListener[] getListeners() {
        listeners = new ToolEventListener[1];
        listeners[0] = new NodePressingEventListener() {
            @Override
            public void pressingNodes(Node[] nodes) {
                color = painterPanel.getColor().getColorComponents(color);
                for (Node node : nodes) {
                    float r = node.r();
                    float g = node.g();
                    float b = node.b();
                    r = intensity * color[0] + (1 - intensity) * r;
                    g = intensity * color[1] + (1 - intensity) * g;
                    b = intensity * color[2] + (1 - intensity) * b;
                    node.setR(r);
                    node.setG(g);
                    node.setB(b);
                }
            }

            @Override
            public void released() {
            }
        };
        return listeners;
    }

    @Override
    public ToolUI getUI() {
        return new ToolUI() {
            @Override
            public JPanel getPropertiesBar(Tool tool) {
                painterPanel = new PainterPanel();
                painterPanel.setColor(new Color(color[0], color[1], color[2]));
                return painterPanel;
            }

            @Override
            public String getName() {
                return NbBundle.getMessage(Painter.class, "Painter.name");
            }

            @Override
            public Icon getIcon() {
                return new ImageIcon(getClass().getResource("/org/gephi/tools/plugin/resources/painter.png"));
            }

            @Override
            public String getDescription() {
                return NbBundle.getMessage(Painter.class, "Painter.description");
            }

            @Override
            public int getPosition() {
                return 100;
            }
        };
    }

    @Override
    public ToolSelectionType getSelectionType() {
        return ToolSelectionType.SELECTION;
    }
}
