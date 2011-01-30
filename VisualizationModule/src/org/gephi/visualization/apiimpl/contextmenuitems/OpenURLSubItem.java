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

import java.net.URL;
import javax.swing.Icon;
import org.gephi.data.attributes.api.AttributeRow;
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
public class OpenURLSubItem implements GraphContextMenuItem {

    private Node node;
    private String column, url;
    private int position;

    public OpenURLSubItem(String column, int position) {
        this.column = column;
        this.position = position;
    }

    public void setup(HierarchicalGraph graph, Node[] nodes) {
        if (nodes.length == 1) {
            node = nodes[0];
            AttributeRow row = (AttributeRow) node.getNodeData().getAttributes();
            Object value;
            if ((value = row.getValue(column)) != null) {
                url = value.toString();

                if (!url.matches("(https?|ftp):(//?|\\\\?)?.*")) {
                    //Does not look like an URL, try http:
                    url = "http://" + url;
                }
            }
        }
    }

    public void execute() {
        if (url != null) {
            try {
                URLDisplayer.getDefault().showURLExternal(new URL(url));
            } catch (Exception ex) {
            }
            LastColumnOpenedURL lc = Lookup.getDefault().lookup(ProjectController.class).getCurrentWorkspace().getLookup().lookup(LastColumnOpenedURL.class);
            if (lc == null) {
                Lookup.getDefault().lookup(ProjectController.class).getCurrentWorkspace().add(new LastColumnOpenedURL(column));
            } else {
                lc.column = column;
            }
        }
    }

    public GraphContextMenuItem[] getSubItems() {
        return null;
    }

    public String getName() {
        String shortenedUrl = url;
        if (url == null) {
            shortenedUrl = "";
        } else if (url.length() >= 60) {
            shortenedUrl = url.substring(0, 57) + "...";
        }
        return NbBundle.getMessage(OpenURLSubItem.class, "GraphContextMenu_OpenURLSubItem", column, shortenedUrl);
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
        return 100;
    }

    public int getPosition() {
        return position;
    }

    public Icon getIcon() {
        return null;
    }

    public Integer getMnemonicKey() {
        return null;
    }
}
