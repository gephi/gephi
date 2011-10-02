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

import java.awt.Image;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import org.gephi.clustering.api.Cluster;
import org.gephi.clustering.api.ClusteringController;
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
        return new ImageIcon(getClass().getResource("/org/gephi/desktop/clustering/resources/cluster.png")).getImage();
    }

    @Override
    public Action[] getActions(boolean popup) {
        return new Action[]{new SelectAction(), new GroupAction(), new UngroupAction()};
    }

    private class SelectAction extends AbstractAction {

        public SelectAction() {
            putValue(NAME, NbBundle.getMessage(ClusterNode.class, "ClusterNode.actions.Select.name"));
        }

        public void actionPerformed(ActionEvent e) {
            ClusteringController cc = Lookup.getDefault().lookup(ClusteringController.class);
            cc.selectCluster(cluster);
        }
    }

    private class GroupAction extends AbstractAction {

        public GroupAction() {
            putValue(NAME, NbBundle.getMessage(ClusterNode.class, "ClusterNode.actions.Group.name"));
        }

        public void actionPerformed(ActionEvent e) {
            ClusteringController cc = Lookup.getDefault().lookup(ClusteringController.class);
            cc.groupCluster(cluster);
        }

        @Override
        public boolean isEnabled() {
            ClusteringController cc = Lookup.getDefault().lookup(ClusteringController.class);
            return cc.canGroup(cluster);
        }
    }

    private class UngroupAction extends AbstractAction {

        public UngroupAction() {
            putValue(NAME, NbBundle.getMessage(ClusterNode.class, "ClusterNode.actions.Ungroup.name"));
        }

        public void actionPerformed(ActionEvent e) {
            ClusteringController cc = Lookup.getDefault().lookup(ClusteringController.class);
            cc.ungroupCluster(cluster);
        }

        @Override
        public boolean isEnabled() {
            ClusteringController cc = Lookup.getDefault().lookup(ClusteringController.class);
            return cc.canUngroup(cluster);
        }
    }
}
