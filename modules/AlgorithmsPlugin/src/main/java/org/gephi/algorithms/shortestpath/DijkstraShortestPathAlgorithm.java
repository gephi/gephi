/*
 Copyright 2008-2010 Gephi
 Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
 Website : http://www.gephi.org

 This file is part of Gephi.

 DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

 Copyright 2011 Gephi Consortium. All rights reserved.

 The contents of this file are subject to the terms of either the GNU
 General Public License Version 3 only ("GPL") or the Common
 Development and Distribution License("CDDL") (collectively, the
 "License"). You may not use this file except in compliance with the
 License. You can obtain a copy of the License at
 http://gephi.org/about/legal/license-notice/
 or /cddl-1.0.txt and /gpl-3.0.txt. See the License for the
 specific language governing permissions and limitations under the
 License.  When distributing the software, include this License Header
 Notice in each file and include the License files at
 /cddl-1.0.txt and /gpl-3.0.txt. If applicable, add the following below the
 License Header, with the fields enclosed by brackets [] replaced by
 your own identifying information:
 "Portions Copyrighted [year] [name of copyright owner]"

 If you wish your version of this file to be governed by only the CDDL
 or only the GPL Version 3, indicate your decision by adding
 "[Contributor] elects to include this software in this distribution
 under the [CDDL or GPL Version 3] license." If you do not indicate a
 single choice of license, a recipient has the option to distribute
 your version of this file under either the CDDL, the GPL Version 3 or
 to extend the choice of license to its licensees as provided above.
 However, if you add GPL Version 3 code and therefore, elected the GPL
 Version 3 license, then the option applies only if the new code is
 made subject to such option by the copyright holder.

 Contributor(s):

 Portions Copyrighted 2011 Gephi Consortium.
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

    protected final Graph graph;
    protected final HashMap<Node, Edge> predecessors;

    public DijkstraShortestPathAlgorithm(Graph graph, Node sourceNode) {
        super(sourceNode);
        this.graph = graph;
        predecessors = new HashMap<Node, Edge>();
    }

    @Override
    public void compute() {

        graph.readLock();
        Set<Node> unsettledNodes = new HashSet<Node>();
        Set<Node> settledNodes = new HashSet<Node>();

        //Initialize
        for (Node node : graph.getNodes()) {
            distances.put(node, Double.POSITIVE_INFINITY);
        }
        distances.put(sourceNode, 0d);
        unsettledNodes.add(sourceNode);

        while (!unsettledNodes.isEmpty()) {

            // find node with smallest distance value
            Double minDistance = Double.POSITIVE_INFINITY;
            Node minDistanceNode = null;
            for (Node k : unsettledNodes) {
                Double dist = distances.get(k);
                if (minDistanceNode == null) {
                    minDistanceNode = k;
                }

                if (dist.compareTo(minDistance) < 0) {
                    minDistance = dist;
                    minDistanceNode = k;
                }
            }
            unsettledNodes.remove(minDistanceNode);
            settledNodes.add(minDistanceNode);

            for (Edge edge : graph.getEdges(minDistanceNode)) {
                Node neighbor = graph.getOpposite(minDistanceNode, edge);
                if (!settledNodes.contains(neighbor)) {
                    double dist = getShortestDistance(minDistanceNode) + edgeWeight(edge);
                    if (getShortestDistance(neighbor) > dist) {

                        distances.put(neighbor, dist);
                        predecessors.put(neighbor, edge);
                        unsettledNodes.add(neighbor);
                        maxDistance = Math.max(maxDistance, dist);
                    }
                }
            }
        }

        graph.readUnlock();
    }

    private double getShortestDistance(Node destination) {
        Double d = distances.get(destination);
        if (d == null) {
            return Double.POSITIVE_INFINITY;
        } else {
            return d;
        }
    }

    @Override
    protected double edgeWeight(Edge edge) {
        return edge.getWeight();
    }

    @Override
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

    @Override
    public Edge getPredecessorIncoming(Node node) {
        return predecessors.get(node);
    }
}
