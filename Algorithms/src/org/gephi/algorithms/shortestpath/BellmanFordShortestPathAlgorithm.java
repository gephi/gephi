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
import org.gephi.data.attributes.type.TimeInterval;
import org.gephi.dynamic.DynamicUtilities;
import org.gephi.dynamic.api.DynamicController;
import org.gephi.graph.api.DirectedGraph;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Node;
import org.openide.util.Lookup;

/**
 *
 * @author Mathieu Bastian
 */
public class BellmanFordShortestPathAlgorithm extends AbstractShortestPathAlgorithm {

    protected final DirectedGraph graph;
    protected final HashMap<Node, Edge> predecessors;
    protected TimeInterval timeInterval;

    public BellmanFordShortestPathAlgorithm(DirectedGraph graph, Node sourceNode) {
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
                Node target = edge.getTarget();
                if (relax(edge)) {
                    relaxed = true;
                    predecessors.put(target, edge);
                }
            }
            if (!relaxed) {
                break;
            }
        }

        //Check for negative-weight cycles
        for (Edge edge : graph.getEdges()) {

            if (distances.get(edge.getSource()) + edgeWeight(edge) < distances.get(edge.getTarget())) {
                graph.readUnlock();
                throw new RuntimeException("The Graph contains a negative-weighted cycle");
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
