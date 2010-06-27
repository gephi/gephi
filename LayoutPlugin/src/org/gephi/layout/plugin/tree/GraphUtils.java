package org.gephi.layout.plugin.tree;


import org.gephi.algorithms.shortestpath.AbstractShortestPathAlgorithm;
import org.gephi.algorithms.shortestpath.BellmanFordShortestPathAlgorithm;
import org.gephi.algorithms.shortestpath.DijkstraShortestPathAlgorithm;
import org.gephi.graph.api.DirectedGraph;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.Node;
import org.openide.util.Lookup;


public class GraphUtils {
    private GraphUtils() {}

    public static Node determineRootNode(DirectedGraph graph) {
        for (Node node : graph.getNodes()) {
            int inEdges = 0;

            for (Edge edge : graph.getInEdges(node))
                inEdges++;

            if (inEdges == 0)
                return node;
        }

        return null;
    }

    public static int calculateShortestPath(Node source, Node target) {
        GraphController gc = Lookup.getDefault().lookup(GraphController.class);

        AbstractShortestPathAlgorithm algorithm;
        if (gc.getModel().getGraphVisible() instanceof DirectedGraph)
            algorithm = new BellmanFordShortestPathAlgorithm((DirectedGraph) gc.getModel().getGraphVisible(), source);
        else
            algorithm = new DijkstraShortestPathAlgorithm(gc.getModel().getGraphVisible(), source);

        algorithm.compute();

        return (int) (double) algorithm.getDistances().get(target);
    }

    public static boolean isLeaf(Node node) {
        return outEdgesCount(node) == 0;
    }

    public static int outEdgesCount(Node node) {
        DirectedGraph graph = (DirectedGraph) Lookup.getDefault().lookup(GraphController.class).getModel().getGraph();

        int outEdges = 0;
        for (Edge edge : graph.getOutEdges(node))
            outEdges++;

        return outEdges;
    }
}
