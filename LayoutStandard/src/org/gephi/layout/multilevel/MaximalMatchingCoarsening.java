/*
Copyright 2008-2009 Gephi
Authors : Helder Suzuki <heldersuzuki@gmail.com>
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
package org.gephi.layout.multilevel;

import org.gephi.graph.api.ClusteredGraph;
import org.gephi.graph.api.Node;
import org.gephi.layout.EdgeImpl;
import org.gephi.layout.GraphUtils;

/**
 *
 * @author Helder Suzuki <heldersuzuki@gmail.com>
 */
public class MaximalMatchingCoarsening implements CoarseningStrategy {

    public void coarsen(ClusteredGraph graph) {
        for (EdgeImpl e : GraphUtils.getTopEdges(graph)) {
            if (graph.getLevel(e.N1()) == 0 && graph.getLevel(e.N2()) == 0) {
                float x = (e.N1().getNodeData().x() + e.N2().getNodeData().x()) / 2;
                float y = (e.N1().getNodeData().y() + e.N2().getNodeData().y()) / 2;

                Node parent = graph.groupNodes(new Node[]{e.N1(), e.N2()});
                parent.getNodeData().setX(x);
                parent.getNodeData().setY(y);
                graph.retract(parent);
            }
        }
    }

    public void refine(ClusteredGraph graph) {
        for (Node node : graph.getTopNodes().toArray()) {
            if (graph.getChildrenCount(node) == 2) {
                float x = node.getNodeData().x();
                float y = node.getNodeData().y();

                for (Node child : graph.getChildren(node)) {
                    child.getNodeData().setX(x);
                    child.getNodeData().setY(y);
                }
                graph.ungroupNodes(node);
            }
        }
    }
}
