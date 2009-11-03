/*
Copyright 2008 WebAtlas
Authors : Mathieu Bastian, Mathieu Jacomy, Julian Bilcke
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gephi.desktop.algorithms.cluster;

import java.awt.Image;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import org.gephi.algorithms.cluster.api.Cluster;
import org.gephi.algorithms.cluster.api.ClusteringController;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Mathieu Bastian
 */
public class ClusterNode extends AbstractNode {

    private Cluster cluster;

    public ClusterNode(Cluster cluster) {
        super(Children.LEAF);
        /*if (project.isOpen()) {
        setChildren(new ProjectChildren(project));
        }*/
        this.cluster = cluster;
        setShortDescription("<html><b>" + cluster.getName() + "</b><br>Elements: " + cluster.getNodesCount() + "</html>");
    }

    @Override
    public String getHtmlDisplayName() {
        String msg = "<html>" + cluster.getName() + " <font color='AAAAAA'><i>- ";
        if (cluster.getNodesCount() > 1) {
            msg += NbBundle.getMessage(ClusterNode.class, "ClusterNode.displayName.nodesCount.plural", cluster.getNodesCount());
        } else {
            msg += NbBundle.getMessage(ClusterNode.class, "ClusterNode.displayName.nodesCount.singular", cluster.getNodesCount());
        }
        msg += "</i></font></html>";
        return msg;
    }

    @Override
    public Image getIcon(int type) {
        return new ImageIcon(getClass().getResource("/org/gephi/desktop/algorithms/cluster/cluster.png")).getImage();
    }

    @Override
    public Action[] getActions(boolean popup) {
        return new Action[]{new MyAction()};
    }

    private class MyAction extends AbstractAction {

        public MyAction() {
            putValue(NAME, NbBundle.getMessage(ClusterNode.class, "ClusterNode.actions.Select.name"));
        }

        public void actionPerformed(ActionEvent e) {
            ClusteringController cc = Lookup.getDefault().lookup(ClusteringController.class);
            cc.selectCluster(cluster);
        }
    }
}
