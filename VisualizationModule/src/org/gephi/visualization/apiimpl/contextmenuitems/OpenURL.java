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
import java.net.MalformedURLException;
import java.net.URL;
import javax.swing.Icon;
import org.gephi.graph.api.HierarchicalGraph;
import org.gephi.graph.api.Node;
import org.gephi.visualization.spi.GraphContextMenuItem;
import org.openide.awt.HtmlBrowser.URLDisplayer;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 */
@ServiceProvider(service = GraphContextMenuItem.class)
public class OpenURL implements GraphContextMenuItem {

    private Node[] nodes;
    private URL url = null;

    public void setup(HierarchicalGraph graph, Node[] nodes) {
        this.nodes = nodes;
        if (nodes.length > 0) {
            try {
                this.url = new URL(nodes[0].getNodeData().getLabel());
            } catch (MalformedURLException ex) {
                url = null;
            }
        }
    }

    public void execute() {
        URLDisplayer.getDefault().showURLExternal(url);
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
        return nodes.length == 1;
    }

    public boolean canExecute() {
        return url != null;
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
        return KeyEvent.VK_O;
    }
}
