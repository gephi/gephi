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

import javax.swing.JPanel;
import org.gephi.algorithms.cluster.api.Cluster;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.BeanTreeView;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;

/**
 *
 * @author Mathieu Bastian
 */
public class ClusterExplorer extends JPanel implements ExplorerManager.Provider {

    private BeanTreeView tree;
    private final ExplorerManager manager = new ExplorerManager();

    public ClusterExplorer() {
        initComponents();
    }

    public void initExplorer(Cluster[] clusters) {
        if (clusters != null) {
            manager.setRootContext(new ClustersNode(clusters));
            ((BeanTreeView) tree).setRootVisible(true);
        } else {
            resetExplorer();
        }
    }

    public void resetExplorer() {
        manager.setRootContext(new AbstractNode(Children.LEAF));
        ((BeanTreeView) tree).setRootVisible(false);
    }

    private void initComponents() {
        tree = new BeanTreeView();

        setLayout(new java.awt.BorderLayout());

        //((BeanTreeView) tree).setRootVisible(false);
        add(tree, java.awt.BorderLayout.CENTER);
    }

    public ExplorerManager getExplorerManager() {
        return manager;
    }
}
