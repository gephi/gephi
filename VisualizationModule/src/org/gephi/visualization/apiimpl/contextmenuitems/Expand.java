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
import javax.swing.Icon;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.NodeData;
import org.gephi.visualization.spi.GraphContextMenuItem;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 */
@ServiceProvider(service = GraphContextMenuItem.class)
public class Expand extends BasicItem {

    public void execute() {
        try {
            for (Node node : nodes) {
                if (graph.getDescendantCount(node) > 0) {
                    expandPositioning(node);
                    graph.expand(node);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            graph.readUnlockAll();
        }
    }

    private void expandPositioning(Node node) {
        NodeData nodeData = node.getNodeData();
        float centroidX = 0;
        float centroidY = 0;
        int len = 0;
        Node[] children = graph.getChildren(node).toArray();
        for (Node child : children) {
            centroidX += child.getNodeData().x();
            centroidY += child.getNodeData().y();
            len++;
        }
        centroidX /= len;
        centroidY /= len;

        float diffX = nodeData.x() - centroidX;
        float diffY = nodeData.y() - centroidY;
        for (Node child : children) {
            NodeData nd = child.getNodeData();
            nd.setX(nd.x() + diffX);
            nd.setY(nd.y() + diffY);
        }
    }

    public String getName() {
        return NbBundle.getMessage(Expand.class, "GraphContextMenu_Expand");
    }
    public boolean canExecute() {
        for (Node n : nodes) {
            if (graph.getDescendantCount(n)>0) {
                return true;
            }
        }
        return false;
    }

    public int getType() {
        return 100;
    }

    public int getPosition() {
        return 0;
    }

    public Icon getIcon() {
        return ImageUtilities.loadImageIcon("org/gephi/visualization/api/resources/expand.png", false);
    }

    @Override
    public Integer getMnemonicKey() {
        return KeyEvent.VK_E;
    }
}
