/*
 *  Copyright 2009 Helder Suzuki <heldersuzuki@gmail.com>.
 */
package org.gephi.layout.multilevel;

import java.util.Vector;
import org.gephi.graph.api.ClusteredGraph;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Node;
import org.gephi.layout.force.EdgeImpl;

/**
 *
 * @author Helder Suzuki <heldersuzuki@gmail.com>
 */
public class MaximalMatchingCoarsening implements CoarseningStrategy {

    private Node getTopmostParent(ClusteredGraph graph, Node n) {
        Node parent = graph.getParent(n);
        while (parent != null) {
            n = parent;
            parent = graph.getParent(n);
        }
        return n;
    }

    private Vector<EdgeImpl> getTopEdges(ClusteredGraph graph){
        Vector<EdgeImpl> edges = new Vector<EdgeImpl>();
        for (Edge e : graph.getEdges()) {
            Node n1 = getTopmostParent(graph, e.getSource());
            Node n2 = getTopmostParent(graph, e.getTarget());
            if (n1 != n2) {
                edges.add(new EdgeImpl(n1, n2));
            }
        }
        return edges;
    }

    public void coarsen(ClusteredGraph graph) {
        for (EdgeImpl e : getTopEdges(graph)) {
            if (graph.getLevel(e.N1()) == 0 && graph.getLevel(e.N2()) == 0) {
                Node[] nodes = new Node[2];
                nodes[0] = e.N1();
                nodes[1] = e.N2();

                float x = (e.N1().getNodeData().x() + e.N2().getNodeData().x()) / 2;
                float y = (e.N1().getNodeData().y() + e.N2().getNodeData().y()) / 2;

                Node parent = graph.groupNodes(nodes);
                parent.getNodeData().setX(x);
                parent.getNodeData().setY(y);
                graph.retract(parent);
            }
        }

        graph.resetView();
    }

    public void refine(ClusteredGraph graph) {
        for (Node node : graph.getTopNodes().toArray()) {

            if (graph.getChildrenCount(node) == 2) {
                float x = node.getNodeData().x();
                float y = node.getNodeData().y();

                // System.out.println("(" + x + ", " + y + ")");

                for (Node child : graph.getChildren(node)) {
                    child.getNodeData().setX(x);
                    child.getNodeData().setY(y);
                }
                graph.ungroupNodes(node);

            }
        }
    }
}
