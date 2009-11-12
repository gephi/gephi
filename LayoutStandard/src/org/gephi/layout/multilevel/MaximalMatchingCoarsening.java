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
package org.gephi.layout.multilevel;

import org.gephi.graph.api.Edge;
import org.gephi.graph.api.HierarchicalGraph;
import org.gephi.graph.api.Node;

/**
 *
 * @author Helder Suzuki <heldersuzuki@gephi.org>
 */
public class MaximalMatchingCoarsening implements MultiLevelLayout.CoarseningStrategy {

//    public void print(ClusteredGraph graph) {
//        System.out.println("------ print ------");
//        for (int i = 0; i <= graph.getHeight(); i++) {
//            System.out.printf("Level %d: %d nodes\n", i, graph.getNodes(i).toArray().length);
//        }
//        System.out.println("Topnodes: " + graph.getTopNodes().toArray().length);
//    }
    public void coarsen(HierarchicalGraph g) {
        HierarchicalGraph graph = g;
        int retract = 0;
        int count = 0;
        //print(graph);
        for (Edge e : graph.getEdgesAndMetaEdges().toArray()) {
            Node a = e.getSource();
            Node b = e.getTarget();
            count++;
            if (graph.getParent(a) == graph.getParent(b) && graph.getLevel(a) == 0) {
                float x = (a.getNodeData().x() + b.getNodeData().x()) / 2;
                float y = (a.getNodeData().y() + b.getNodeData().y()) / 2;

                Node parent = graph.groupNodes(new Node[]{a, b});
                parent.getNodeData().setX(x);
                parent.getNodeData().setY(y);
                graph.retract(parent);
                retract++;
            }
        }
        System.out.println("count = " + count);
        System.out.println("Retract: " + retract);
    // print(graph);
    }

    public void refine(HierarchicalGraph graph) {
        double r = 10;
        int count = 0;
        int refined = 0;
        for (Node node : graph.getTopNodes().toArray()) {
            count++;
            if (graph.getChildrenCount(node) == 2) {
                refined++;
                float x = node.getNodeData().x();
                float y = node.getNodeData().y();

                for (Node child : graph.getChildren(node)) {
                    double t = Math.random();
                    child.getNodeData().setX((float) (x + r * Math.cos(t)));
                    child.getNodeData().setY((float) (y + r * Math.sin(t)));
                }
                graph.ungroupNodes(node);
            }
//                    System.out.println("graph.getChildrenCount(node): " + graph.getChildrenCount(node));
        }
        System.out.println("COUNT = " + count);
        System.out.println("REFINED = " + refined);
    }
}
