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
import java.util.ArrayList;
import javax.swing.Icon;
import javax.swing.JOptionPane;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeRow;
import org.gephi.data.attributes.api.AttributeType;
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
    private String columnTitle, url;

    public void setup(HierarchicalGraph graph, Node[] nodes) {
        if (nodes.length == 1) {
            node = nodes[0];
        } else {
            node = null;
        }
    }

    public void execute() {
        if (node != null) {
            LastColumnOpenedURL lc = Lookup.getDefault().lookup(ProjectController.class).getCurrentWorkspace().getLookup().lookup(LastColumnOpenedURL.class);
            ArrayList<ColumnURL> columnUrls = new ArrayList<ColumnURL>();
            ColumnURL initialSelection = null;
            Object value;
            String str;
            AttributeColumn column;
            AttributeRow row = (AttributeRow) node.getNodeData().getAttributes();
            for (int i = 0; i < row.countValues(); i++) {
                column = row.getColumnAt(i);
                if ((column.getType() == AttributeType.STRING || column.getType() == AttributeType.DYNAMIC_STRING) && (value = row.getValue(i)) != null) {
                    str = value.toString();
                    if (!str.matches("(https?|ftp):(//?|\\\\?)?.*")) {
                        //Does not look like an URL, try http:
                        str = "http://" + str;
                    }
                    if (lc != null && lc.column.equals(column.getTitle())) {
                        columnUrls.add(initialSelection=new ColumnURL(column.getTitle(), str));
                    }else{
                        columnUrls.add(new ColumnURL(column.getTitle(), str));
                    }
                }
            }

            ColumnURL selection = (ColumnURL) JOptionPane.showInputDialog(null, NbBundle.getMessage(OpenURLSubItem.class, "GraphContextMenu_OpenURLSubItem.message"), getName(), JOptionPane.QUESTION_MESSAGE, null, columnUrls.toArray(), initialSelection);
            if (selection != null) {
                columnTitle = selection.column;
                url = selection.url;
                showUrl();
            }
        }
    }

    private void showUrl() {
        try {
            URLDisplayer.getDefault().showURLExternal(new URL(url));
        } catch (Exception ex) {
        }
        LastColumnOpenedURL lc = Lookup.getDefault().lookup(ProjectController.class).getCurrentWorkspace().getLookup().lookup(LastColumnOpenedURL.class);
        if (lc == null) {
            Lookup.getDefault().lookup(ProjectController.class).getCurrentWorkspace().add(new LastColumnOpenedURL(columnTitle));
        } else {
            lc.column = columnTitle;
        }
    }

    public GraphContextMenuItem[] getSubItems() {
        return null;
    }

    public String getName() {
        return NbBundle.getMessage(OpenURLSubItem.class, "GraphContextMenu_OpenURLSubItem");
    }

    public String getDescription() {
        return null;
    }

    public boolean isAvailable() {
        return true;
    }

    public boolean canExecute() {
        return true;
    }

    public int getType() {
        return 100;
    }

    public int getPosition() {
        return 0;
    }

    public Icon getIcon() {
        return null;
    }

    public Integer getMnemonicKey() {
        return KeyEvent.VK_N;
    }

    class ColumnURL {

        String column, url;

        public ColumnURL(String column, String url) {
            this.column = column;
            this.url = url;
        }

        @Override
        public String toString() {
            return String.format("%s - %s", column, url);
        }
    }
}
