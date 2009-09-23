/*
Copyright 2008 WebAtlas
Authors : Mathieu Bastian, Mathieu Jacomy, Julian Bilcke
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
package org.gephi.algorithms.shortestpath;

import java.util.HashMap;
import org.gephi.graph.api.DirectedGraph;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Node;

/**
 *
 * @author Mathieu Bastian
 */
public class BellmanFordShortestPathAlgorithm extends AbstractShortestPathAlgorithm {

    protected DirectedGraph graph;
    protected HashMap<Node, Node> predecessors;

    public BellmanFordShortestPathAlgorithm(DirectedGraph graph, Node sourceNode) {
        super(sourceNode);
        this.graph = graph;
        predecessors = new HashMap<Node, Node>();
    }

    protected void compute() {

        graph.readLock();

        //Initialize
        int nodeCount = 0;
        for (Node node : graph.getNodes()) {
            distances.put(node, Double.POSITIVE_INFINITY);
            nodeCount++;
        }
        distances.put(sourceNode, 0d);


        //Relax edges repeatedly
        for (int i = 0; i < nodeCount; i++) {

            boolean relaxed = false;
            for (Edge edge : graph.getEdges()) {
                Node source = edge.getSource();
                Node target = edge.getTarget();
                if (relax(edge)) {
                    relaxed = true;
                    predecessors.put(target, source);
                }
            }
            if (!relaxed) {
                break;
            }
        }

        //Check for negative-weight cycles
        for (Edge edge : graph.getEdges()) {

            if (distances.get(edge.getSource()) + edge.getWeight() < distances.get(edge.getTarget())) {
                graph.readUnlock();
                throw new RuntimeException("The Graph contains a negative-weighted cycle");
            }
        }

        graph.readUnlock();
    }
}
