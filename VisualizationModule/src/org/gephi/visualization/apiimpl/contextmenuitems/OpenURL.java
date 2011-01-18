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

import java.awt.HeadlessException;
import java.awt.event.KeyEvent;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.MissingResourceException;
import javax.swing.Icon;
import javax.swing.JOptionPane;
import org.gephi.data.attributes.api.AttributeRow;
import org.gephi.data.attributes.api.AttributeType;
import org.gephi.data.properties.PropertiesColumn;
import org.gephi.graph.api.HierarchicalGraph;
import org.gephi.graph.api.Node;
import org.gephi.visualization.spi.GraphContextMenuItem;
import org.openide.awt.HtmlBrowser.URLDisplayer;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 */
@ServiceProvider(service = GraphContextMenuItem.class)
public class OpenURL implements GraphContextMenuItem {

    private Node node;
    private ArrayList<String> urls;
    private String labelURL;

    /**
     * It checks what valid urls are contained in the node attributes (of string type).
     * If an attribute is not valid as an url, it will try to put "http://" before to make it valid.
     */
    public void setup(HierarchicalGraph graph, Node[] nodes) {
        if (nodes.length == 1) {
            node = nodes[0];
            urls = new ArrayList<String>();
            AttributeRow row = (AttributeRow) node.getNodeData().getAttributes();
            Object value;
            String str;
            for (int i = 0; i < row.countValues(); i++) {
                if ((row.getColumnAt(i).getType() == AttributeType.STRING || row.getColumnAt(i).getType() == AttributeType.DYNAMIC_STRING) && (value = row.getValue(i)) != null) {
                    str = value.toString();

                    if(!str.matches("(https?|ftp):(//?|\\\\?)?.*")){
                        //Does not look like an URL, try http:
                        str="http://"+str;
                    }
                    try {
                        new URI(str);//URI only validates, URL tries to connect and can be slow
                        urls.add(str);
                        if (i == PropertiesColumn.NODE_LABEL.getIndex()) {
                            labelURL = str;//Keep label url for preselection
                        }
                    } catch (Exception ex) {
                    }
                }
            }
        }
    }

    public void execute() {
        try {
            if (urls.size() > 0) {
                if (urls.size() > 1) {
                    //More than one valid url, let the user choose:
                    String selection = (String) JOptionPane.showInputDialog(null, NbBundle.getMessage(OpenURL.class, "GraphContextMenu_OpenURL.select"), getName(), JOptionPane.QUESTION_MESSAGE, null, urls.toArray(), labelURL);
                    if (selection != null) {
                        URLDisplayer.getDefault().showURLExternal(new URL(selection));
                    }
                } else {
                    //Only 1 URL, show it:
                    URLDisplayer.getDefault().showURLExternal(new URL(urls.get(0)));
                }
            }
        } catch (Exception ex) {
        }
    }

    public GraphContextMenuItem[] getSubItems() {
        return null;
    }

    public String getName() {
        return NbBundle.getMessage(OpenURL.class, "GraphContextMenu_OpenURL");
    }

    public String getDescription() {
        return NbBundle.getMessage(OpenURL.class, "GraphContextMenu_OpenURL.description");
    }

    public boolean isAvailable() {
        return node != null;
    }

    public boolean canExecute() {
        return urls.size() > 0;
    }

    public int getType() {
        return 400;
    }

    public int getPosition() {
        return 300;
    }

    public Icon getIcon() {
        return ImageUtilities.loadImageIcon("org/gephi/visualization/api/resources/globe-network.png", false);
    }

    public Integer getMnemonicKey() {
        return KeyEvent.VK_P;
    }
}
