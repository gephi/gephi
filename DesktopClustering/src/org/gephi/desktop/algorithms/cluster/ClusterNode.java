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

import org.gephi.algorithms.cluster.api.Cluster;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;

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
        setDisplayName(cluster.getName());
        setShortDescription("<html><b>" + cluster.getName() + "</b><br>Elements: " + cluster.getNodesCount() + "</html>");
    }
}
