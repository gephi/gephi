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

import java.awt.Color;
import java.util.HashMap;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Node;

/**
 *
 * @author Mathieu Bastian
 */
public abstract class AbstractShortestPathAlgorithm {

    protected HashMap<Node, Color> colors;
    protected HashMap<Node, Double> distances;
    protected Node sourceNode;
    protected double maxDistance = 0;

    public AbstractShortestPathAlgorithm(Node sourceNode) {
        this.sourceNode = sourceNode;
        colors = new HashMap<Node, Color>();
        distances = new HashMap<Node, Double>();
    }

    protected boolean relax(Edge edge) {
        Node source = edge.getSource();
        Node target = edge.getTarget();
        double distSource = distances.get(source);
        double distTarget = distances.get(target);
        double weight = edge.getWeight();

        double sourceWeight = distSource + weight;
        if (sourceWeight < distTarget) {
            distances.put(target, sourceWeight);
            maxDistance = Math.max(maxDistance, sourceWeight);
            return true;
        } else {
            return false;
        }
    }

    public abstract void compute();

    public abstract Node getPredecessor(Node node);

    public abstract Edge getPredecessorIncoming(Node node);

    public HashMap<Node, Double> getDistances() {
        return distances;
    }

    public double getMaxDistance() {
        return maxDistance;
    }
}
