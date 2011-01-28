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
package org.gephi.desktop.clustering;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.gephi.clustering.api.Cluster;
import org.gephi.clustering.api.ClusteringController;
import org.openide.nodes.AbstractNode;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Mathieu Bastian
 */
public class ClustersNode extends AbstractNode {

    private Cluster[] clusters;

    public ClustersNode(Cluster[] clusters) {
        super(new ClustersChildren(clusters));
        this.clusters = clusters;
        setIconBaseWithExtension("org/gephi/desktop/clustering/resources/cluster.png");
    }

    @Override
    public String getHtmlDisplayName() {
        String msg = "<html>" + NbBundle.getMessage(ClusterNode.class, "ClustersNode.displayName") + " - <font color='AAAAAA'><i> " + clusters.length + "</i></font></html>";
        return msg;
    }

    @Override
    public Action[] getActions(boolean popup) {
        return new Action[]{new SelectAllAction(), new GroupAllAction(), new UngroupAllAction()};
    }

    private class SelectAllAction extends AbstractAction {

        public SelectAllAction() {
            putValue(NAME, NbBundle.getMessage(ClusterNode.class, "ClustersNode.actions.SelectAll.name"));
        }

        public void actionPerformed(ActionEvent e) {
            ClusteringController cc = Lookup.getDefault().lookup(ClusteringController.class);
            for (Cluster cluster : clusters) {
                cc.selectCluster(cluster);
            }
        }
    }

    private class GroupAllAction extends AbstractAction {

        public GroupAllAction() {
            putValue(NAME, NbBundle.getMessage(ClusterNode.class, "ClustersNode.actions.GroupAll.name"));
        }

        public void actionPerformed(ActionEvent e) {
            ClusteringController cc = Lookup.getDefault().lookup(ClusteringController.class);
            for (Cluster cluster : clusters) {
                if (cc.canGroup(cluster)) {
                    cc.groupCluster(cluster);
                }
            }
        }

        @Override
        public boolean isEnabled() {
            ClusteringController cc = Lookup.getDefault().lookup(ClusteringController.class);
            for (Cluster cluster : clusters) {
                if (cc.canGroup(cluster)) {
                    return true;
                }
            }
            return false;
        }
    }

    private class UngroupAllAction extends AbstractAction {

        public UngroupAllAction() {
            putValue(NAME, NbBundle.getMessage(ClusterNode.class, "ClustersNode.actions.UngroupAll.name"));
        }

        public void actionPerformed(ActionEvent e) {
            ClusteringController cc = Lookup.getDefault().lookup(ClusteringController.class);
            for (Cluster cluster : clusters) {
                if (cc.canUngroup(cluster)) {
                    cc.ungroupCluster(cluster);
                }
            }
        }

        @Override
        public boolean isEnabled() {
            ClusteringController cc = Lookup.getDefault().lookup(ClusteringController.class);
            for (Cluster cluster : clusters) {
                if (cc.canUngroup(cluster)) {
                    return true;
                }
            }
            return false;
        }
    }
}
