/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
Website : http://www.gephi.org

This file is part of Gephi.

DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

Copyright 2011 Gephi Consortium. All rights reserved.

The contents of this file are subject to the terms of either the GNU
General Public License Version 3 only ("GPL") or the Common
Development and Distribution License("CDDL") (collectively, the
"License"). You may not use this file except in compliance with the
License. You can obtain a copy of the License at
http://gephi.org/about/legal/license-notice/
or /cddl-1.0.txt and /gpl-3.0.txt. See the License for the
specific language governing permissions and limitations under the
License.  When distributing the software, include this License Header
Notice in each file and include the License files at
/cddl-1.0.txt and /gpl-3.0.txt. If applicable, add the following below the
License Header, with the fields enclosed by brackets [] replaced by
your own identifying information:
"Portions Copyrighted [year] [name of copyright owner]"

If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 3, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 3] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 3 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 3 code and therefore, elected the GPL
Version 3 license, then the option applies only if the new code is
made subject to such option by the copyright holder.

Contributor(s):

Portions Copyrighted 2011 Gephi Consortium.
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
