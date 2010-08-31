/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
*/
package org.gephi.tools.plugin;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import org.gephi.graph.api.Node;
import org.gephi.tools.spi.NodeClickEventListener;
import org.gephi.tools.spi.Tool;
import org.gephi.tools.spi.ToolEventListener;
import org.gephi.tools.spi.ToolSelectionType;
import org.gephi.tools.spi.ToolUI;
import org.gephi.ui.tools.plugin.edit.EditToolTopComponent;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.WindowManager;

/**
 *
 * @author Mathieu Bastian
 */
@ServiceProvider(service = Tool.class)
public class Edit implements Tool {

    private EditToolTopComponent topComponent;

    public void select() {
        topComponent = EditToolTopComponent.findInstance();
        topComponent.open();
        topComponent.requestActive();
    }

    public void unselect() {
        topComponent.disableEdit();
        topComponent.close();
        topComponent = null;
    }

    public ToolEventListener[] getListeners() {
        return new ToolEventListener[]{new NodeClickEventListener() {

                public void clickNodes(Node[] nodes) {
                    if (nodes.length > 0) {
                        topComponent.editNode(nodes[0]);
                    } else {
                        topComponent.disableEdit();
                    }
                }
            }};
    }

    public ToolUI getUI() {
        return new ToolUI() {

            public JPanel getPropertiesBar(Tool tool) {
                return new JPanel();
            }

            public Icon getIcon() {
                return new ImageIcon(getClass().getResource("/org/gephi/ui/tools/plugin/edit/edit.png"));
            }

            public String getName() {
                return NbBundle.getMessage(Edit.class, "Edit.name");
            }

            public String getDescription() {
                return NbBundle.getMessage(Edit.class, "Edit.description");
            }

            public int getPosition() {
                return 200;
            }
        };
    }

    public ToolSelectionType getSelectionType() {
        return ToolSelectionType.SELECTION;
    }
}
