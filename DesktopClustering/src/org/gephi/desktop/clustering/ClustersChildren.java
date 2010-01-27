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
package org.gephi.desktop.clustering;

import org.gephi.clustering.api.Cluster;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 *
 * @author Mathieu Bastian
 */
public class ClustersChildren extends Children.Keys<Cluster> {

    private Cluster[] clusters;

    public ClustersChildren(Cluster[] clusters) {
        this.clusters = clusters;

        setKeys(clusters);
    }

    @Override
    protected Node[] createNodes(Cluster project) {
        return new Node[]{new ClusterNode(project)};
    }

    @Override
    protected void addNotify() {
    }
}
