/*
 *  Copyright 2009 Helder Suzuki <heldersuzuki@gmail.com>.
 */
package org.gephi.layout.multilevel;

import java.util.Vector;
import org.gephi.graph.api.ClusteredGraph;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Node;

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

    class EdgeAbstraction {

        public Node n1,  n2;

        public EdgeAbstraction(Node n1, Node n2) {
            this.n1 = n1;
            this.n2 = n2;
        }
    }

    public void coarsen(ClusteredGraph graph) {
        Vector<EdgeAbstraction> edges = new Vector<EdgeAbstraction>();
        for (Edge e : graph.getEdges()) {
            Node n1 = getTopmostParent(graph, e.getSource());
            Node n2 = getTopmostParent(graph, e.getTarget());
            if (n1 != n2) {
                edges.add(new EdgeAbstraction(n1, n2));
            }
        }

        for (EdgeAbstraction e : edges) {
            if (graph.getLevel(e.n1) == 0 && graph.getLevel(e.n2) == 0) {
                Node[] nodes = new Node[2];
                nodes[0] = e.n1;
                nodes[1] = e.n2;

                float x = (e.n1.getNodeData().x() + e.n2.getNodeData().x()) / 2;
                float y = (e.n1.getNodeData().y() + e.n2.getNodeData().y()) / 2;

                Node parent = graph.groupNodes(nodes);
                parent.getNodeData().setX(x);
                parent.getNodeData().setY(y);
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
