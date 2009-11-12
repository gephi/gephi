/*
Copyright 2008-2009 Gephi
Authors : Helder Suzuki <heldersuzuki@gephi.org>
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
package org.gephi.layout;

import org.gephi.graph.api.Edge;
import org.gephi.graph.api.HierarchicalGraph;
import org.gephi.graph.api.Node;

/**
 *
 * @author Helder Suzuki <heldersuzuki@gephi.org>
 */
public class GraphUtils {

    public static Node getTopmostParent(HierarchicalGraph graph, Node n) {
        Node parent = graph.getParent(n);
        while (parent != null) {
            n = parent;
            parent = graph.getParent(n);
        }
        return n;
    }

    public static float getAverageEdgeLength(HierarchicalGraph graph) {
        float edgeLength = 0;
        int count = 1;
        for (Edge e : graph.getEdgesAndMetaEdges()) {
            edgeLength += ForceVectorUtils.distance(
                e.getSource().getNodeData(), e.getTarget().getNodeData());
            count++;
        }

        return edgeLength / count;
    }
}
