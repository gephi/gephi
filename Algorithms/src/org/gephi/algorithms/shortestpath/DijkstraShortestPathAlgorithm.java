/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gephi.algorithms.shortestpath;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import org.gephi.data.attributes.type.TimeInterval;
import org.gephi.dynamic.DynamicUtilities;
import org.gephi.dynamic.api.DynamicController;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;
import org.openide.util.Lookup;

/**
 *
 * @author Mathieu Bastian
 */
public class DijkstraShortestPathAlgorithm extends AbstractShortestPathAlgorithm {

    protected final Graph graph;
    protected final HashMap<Node, Edge> predecessors;
    protected TimeInterval timeInterval;

    public DijkstraShortestPathAlgorithm(Graph graph, Node sourceNode) {
        super(sourceNode);
        this.graph = graph;
        predecessors = new HashMap<Node, Edge>();
        DynamicController dynamicController = Lookup.getDefault().lookup(DynamicController.class);
        if (dynamicController != null) {
            timeInterval = DynamicUtilities.getVisibleInterval(dynamicController.getModel(graph.getGraphModel().getWorkspace()));
        }
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
                double dist = edgeWeight(edge) + distances.get(currentNode);
                if (distances.get(neighbor).equals(Double.POSITIVE_INFINITY)) {
                    distances.put(neighbor, dist);
                    maxDistance = Math.max(maxDistance, dist);
                    predecessors.put(neighbor, edge);
                } else {
                    if (dist < distances.get(neighbor)) {
                        distances.put(neighbor, dist);
                        maxDistance = Math.max(maxDistance, dist);
                        predecessors.put(neighbor, edge);
                    }
                }
            }
        }

        graph.readUnlock();
    }

    @Override
    protected double edgeWeight(Edge edge) {
        if (timeInterval != null) {
            return edge.getWeight(timeInterval.getLow(), timeInterval.getHigh());
        }
        return edge.getWeight();
    }

    public Node getPredecessor(Node node) {
        Edge edge = predecessors.get(node);
        if (edge != null) {
            if (edge.getSource() != node) {
                return edge.getSource();
            } else {
                return edge.getTarget();
            }
        }
        return null;
    }

    public Edge getPredecessorIncoming(Node node) {
        return predecessors.get(node);
    }
}
