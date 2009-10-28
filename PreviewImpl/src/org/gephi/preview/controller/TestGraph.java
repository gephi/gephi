package org.gephi.preview.controller;

import org.gephi.preview.BidirectionalEdgeImpl;
import org.gephi.preview.GraphImpl;
import org.gephi.preview.NodeImpl;
import org.gephi.preview.SelfLoopImpl;
import org.gephi.preview.UnidirectionalEdgeImpl;
import org.gephi.preview.api.Graph;

/**
 *
 * @author jeremy
 */
public class TestGraph extends PreviewControllerImpl {

    @Override
    public Graph getGraph() {
        GraphImpl graph = new GraphImpl(gs);

        NodeImpl n1 = new NodeImpl(graph, "Node 1", 90, 360, 20, 0.13f, 0.74f, 0.37f);
        NodeImpl n2 = new NodeImpl(graph, "Node 2", 300, 100, 20, 0.37f, 0.74f, 0.13f);
        NodeImpl n3 = new NodeImpl(graph, "Node 3", 480, 510, 20, 0.74f, 0.13f, 0.13f);
        NodeImpl n4 = new NodeImpl(graph, "Bonus Nope", 270, 300, 20, 0.12f, 0.39f, 0.59f);

        graph.addNode(n1);
        graph.addNode(n2);
        graph.addNode(n3);
        graph.addNode(n4);

		graph.addSelfLoop(new SelfLoopImpl(graph, 0.5f, 0, n2));

        graph.addUnidirectionalEdge(new UnidirectionalEdgeImpl(graph, 0.5f, 0, n1, n2, "Edge 1"));
        graph.addUnidirectionalEdge(new UnidirectionalEdgeImpl(graph, 0.5f, 0, n1, n3, "Edge 2"));
        graph.addUnidirectionalEdge(new UnidirectionalEdgeImpl(graph, 0.5f, 0, n2, n3, "Edge 3"));
        graph.addBidirectionalEdge(new BidirectionalEdgeImpl(graph, 2, 0, n3, n4, "Edge 4"));

        return graph;
    }
}
