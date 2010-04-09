/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.algorithms.shortestpath;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;

/**
 *
 * @author Mathieu Bastian
 */
public class DijkstraShortestPathAlgorithm extends AbstractShortestPathAlgorithm {

    protected Graph graph;

    public DijkstraShortestPathAlgorithm(Graph graph, Node sourceNode) {
        super(sourceNode);
        this.graph = graph;
        // predecessors = new HashMap<Node, Edge>();
    }

    public void compute() {

        graph.readLock();
        Set<Node> nodes = new HashSet<Node>();

        //Initialize
        int nodeCount = 0;
        for (Node node : graph.getNodes()) {
            distances.put(node, Double.POSITIVE_INFINITY);
            nodes.add(node);
            nodeCount++;
        }
        distances.put(sourceNode, 0d);

        while (!nodes.isEmpty()) {

            // find node with smallest distance value
            Double minDistance = Double.POSITIVE_INFINITY;
            Node minDistanceNode = null;
            for (Node k : nodes) {
                Double dist = distances.get(k);
                if (dist.compareTo(minDistance) < 0) {
                    minDistance = dist;
                    minDistanceNode = k;
                }
            }
            Node currentNode = minDistanceNode;
            nodes.remove(currentNode);

            for (Edge edge : graph.getEdges(currentNode)) {
                Node neighbor = graph.getOpposite(currentNode, edge);
                if (distances.get(neighbor).equals(Double.POSITIVE_INFINITY)) {
                    double dist = edgeWeight(edge) + distances.get(currentNode);
                    distances.put(neighbor, dist);
                    maxDistance = Math.max(maxDistance, dist);
                } else {
                    double testDistance = distances.get(currentNode) + edgeWeight(edge);
                    if (testDistance < distances.get(neighbor)) {
                        distances.put(neighbor, testDistance);
                        maxDistance = Math.max(maxDistance, testDistance);
                    }
                }
            }
        }

        graph.readUnlock();
    }

    private double edgeWeight(Edge edge) {
        return edge.getWeight();
    }
}
