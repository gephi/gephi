/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>, Eduardo Ramos <eduramiba@gmail.com>
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
package org.gephi.visualization.apiimpl.contextmenuitems;

import java.awt.event.KeyEvent;
import java.net.URL;
import javax.swing.Icon;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeRow;
import org.gephi.data.attributes.api.AttributeTable;
import org.gephi.graph.api.HierarchicalGraph;
import org.gephi.graph.api.Node;
import org.gephi.project.api.ProjectController;
import org.gephi.visualization.spi.GraphContextMenuItem;
import org.openide.awt.HtmlBrowser.URLDisplayer;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 */
public class OpenURLLastItem implements GraphContextMenuItem {

    private Node node;
    private String column, url;

    public void setup(HierarchicalGraph graph, Node[] nodes) {
        url = null;
        column = null;
        if (nodes.length == 1) {
            node = nodes[0];
            LastColumnOpenedURL lc = Lookup.getDefault().lookup(ProjectController.class).getCurrentWorkspace().getLookup().lookup(LastColumnOpenedURL.class);
            if (lc != null) {
                column = lc.column;
                AttributeTable table = Lookup.getDefault().lookup(AttributeController.class).getModel().getNodeTable();
                if (table.hasColumn(column)) {
                    AttributeRow row = (AttributeRow) node.getNodeData().getAttributes();
                    Object value;
                    if ((value = row.getValue(column)) != null) {
                        url = value.toString();

                        if (!url.matches("(https?|ftp):(//?|\\\\?)?.*")) {
                            //Does not look like an URL, try http:
                            url = "http://" + url;
                        }
                    }
                }else{
                    column=null;
                     Lookup.getDefault().lookup(ProjectController.class).getCurrentWorkspace().remove(lc);
                }
            }
        } else {
            node = null;
        }
    }

    public void execute() {
        if (url != null) {
            try {
                URLDisplayer.getDefault().showURLExternal(new URL(url));
            } catch (Exception ex) {
            }
        }
    }

    public GraphContextMenuItem[] getSubItems() {
        return null;
    }

    public String getName() {
        return NbBundle.getMessage(OpenURLLastItem.class, "GraphContextMenu_OpenURLLastItem", column != null ? column : "--");
    }

    public String getDescription() {
        return null;
    }

    public boolean isAvailable() {
        return true;
    }

    public boolean canExecute() {
        return url != null;
    }

    public int getType() {
        return 0;
    }

    public int getPosition() {
        return 0;
    }

    public Icon getIcon() {
        return null;
    }

    public Integer getMnemonicKey() {
        return KeyEvent.VK_P;
    }
}
