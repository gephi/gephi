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
package org.gephi.layout;

import java.util.Collection;
import java.util.HashSet;
import org.gephi.graph.api.ClusteredGraph;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Node;

/**
 *
 * @author Helder Suzuki <heldersuzuki@gmail.com>
 */
public class GraphUtils {

    public static Node getTopmostParent(ClusteredGraph graph, Node n) {
        Node parent = graph.getParent(n);
        while (parent != null) {
            n = parent;
            parent = graph.getParent(n);
        }
        return n;
    }

    public static float getAverageEdge(ClusteredGraph graph) {
        float edgeLength = 0;
        int count = 1;
        for (Edge e : getTopEdges(graph)) {
            edgeLength += ForceVectorUtils.distance(
                e.getSource().getNodeData(), e.getTarget().getNodeData());
            count++;
        }

        return edgeLength / count;
    }

    public static Collection<EdgeImpl> getTopEdges(ClusteredGraph graph) {
        HashSet<EdgeImpl> edges = new HashSet<EdgeImpl>();

        for (Edge e : graph.getEdges()) {
            Node n1 = getTopmostParent(graph, e.getSource());
            Node n2 = getTopmostParent(graph, e.getTarget());
            if (n1 != n2) {
                edges.add(new EdgeImpl(n1, n2));
            }
        }

        return edges;
    }
}
