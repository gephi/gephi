package org.gephi.preview.controller;

import org.gephi.preview.GraphImpl;
import org.gephi.preview.NodeImpl;
import org.gephi.preview.api.Graph;

/**
 *
 * @author jeremy
 */
public class TestGraph extends PreviewControllerImpl {

    @Override
    public Graph getGraph() {

        GraphImpl graph = new GraphImpl(gs);

        NodeImpl n1 = new NodeImpl(graph, "Node 1", 90, 360, 20, 32, 189, 95);
        NodeImpl n2 = new NodeImpl(graph, "Node 2", 300, 100, 20, 95, 189, 32);
        NodeImpl n3 = new NodeImpl(graph, "Node 3", 480, 510, 20, 189, 32, 32);
        NodeImpl n4 = new NodeImpl(graph, "Bonus Nope", 270, 300, 20, 30, 100, 150);

        graph.addNode(n1);
        graph.addNode(n2);
        graph.addNode(n3);
        graph.addNode(n4);

        return graph;
    }
}
