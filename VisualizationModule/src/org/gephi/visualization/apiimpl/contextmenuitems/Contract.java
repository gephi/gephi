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
import java.util.HashSet;
import java.util.Set;
import javax.swing.Icon;
import org.gephi.graph.api.GroupData;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.NodeData;
import org.gephi.visualization.hull.ConvexHull;
import org.gephi.visualization.opengl.compatibility.objects.ConvexHullModel;
import org.gephi.visualization.spi.GraphContextMenuItem;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 */
@ServiceProvider(service = GraphContextMenuItem.class)
public class Contract extends BasicItem{

    public void execute() {
        try {
            Set<Node> parents = new HashSet<Node>();
            for (Node node : nodes) {
                Node nodeParent = graph.getParent(node);
                if (nodeParent != null) {
                    parents.add(nodeParent);
                }
            }

            for (Node parent : parents) {
                GroupData gd = (GroupData) parent.getNodeData();
                if (gd.getHullModel() != null) {
                    ConvexHull hull = ((ConvexHullModel) gd.getHullModel()).getObj();
                    contractPositioning(hull);
                }
                graph.retract(parent);
            }
        } catch (Exception e) {
            e.printStackTrace();
            graph.readUnlockAll();
        }
    }

    private void contractPositioning(ConvexHull hull) {
        NodeData metaNode = hull.getMetaNode().getNodeData();
        metaNode.setX(hull.x());
        metaNode.setY(hull.y());

        ConvexHullModel model = (ConvexHullModel) hull.getModel();
        model.setScale(0.9f);
        model.setScaleQuantum(-0.1f);
    }

    public String getName() {
        return NbBundle.getMessage(Contract.class, "GraphContextMenu_Contract");
    }

    public boolean canExecute() {
        for (Node n : nodes) {
            if (graph.getParent(n)!=null) {
                return true;
            }
        }
        return false;
    }

    public int getType() {
        return 100;
    }

    public int getPosition() {
        return 100;
    }

    public Icon getIcon() {
        return ImageUtilities.loadImageIcon("org/gephi/visualization/api/resources/contract.png", false);
    }

    @Override
    public Integer getMnemonicKey() {
        return KeyEvent.VK_C;
    }
}
