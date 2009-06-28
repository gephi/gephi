/*
Copyright 2008-2009 Gephi
Authors : Helder Suzuki <heldersuzuki@gmail.com>
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
package org.gephi.layout.force;

import org.gephi.graph.api.ClusteredGraph;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.NodeData;
import org.gephi.layout.GraphUtils;
import org.gephi.layout.api.Layout;
import org.gephi.layout.force.quadtree.QuadTree;

/**
 *
 * @author Helder Suzuki <heldersuzuki@gmail.com>
 */
public abstract class AbstractForceLayout implements Layout {

    protected ClusteredGraph graph;
    protected float energy0;
    protected float energy;
    protected AbstractForce edgeForce;
    protected AbstractForce nodeForce;

    public void initAlgo(ClusteredGraph graph) {
        this.graph = graph;

        for (Node n : graph.getTopNodes()) {
            n.getNodeData().setLayoutData(new ForceVector());
        }
        energy = Float.POSITIVE_INFINITY;
    }

    public void initAlgo(GraphController graphController) {
        initAlgo(graphController.getClusteredDirectedGraph());
    }

    /* Maximum level for Barnes-Hut's quadtree */
    protected int getQuadTreeMaxLevel() {
        return Integer.MAX_VALUE;
    }

    /* theta is the parameter for Barnes-Hut opening criteria */
    protected float getBarnesHutTheta() {
        return (float) 1.2;
    }

    public void goAlgo() {
        // Evaluates n^2 inter node forces using BarnesHut.
        BarnesHut barnes = new BarnesHut(getNodeForce());
        barnes.theta = getBarnesHutTheta();
        QuadTree tree = QuadTree.buildTree(graph,
                                           getQuadTreeMaxLevel());
        for (Node node : graph.getTopNodes()) {
            NodeData data = node.getNodeData();
            ForceVector layoutData = data.getLayoutData();

            ForceVector f = barnes.calculateForce(data, tree);
            layoutData.add(f);
        }

        // Apply edge forces.
        int count = 0;
        for (Edge e : GraphUtils.getTopEdges(graph)) {
            NodeData n1 = e.getSource().getNodeData();
            NodeData n2 = e.getTarget().getNodeData();
            ForceVector f1 = n1.getLayoutData();
            ForceVector f2 = n2.getLayoutData();

            ForceVector f = getEdgeForce().calculateForce(n1, n2);
            f1.add(f);
            f2.subtract(f);
        }

        // Apply displacements on nodes and calculate energy.
        energy0 = energy;
        energy = 0;
        for (Node n : graph.getTopNodes()) {
            NodeData data = n.getNodeData();
            ForceVector force = data.getLayoutData();

            energy += force.getEnergy();
            getDisplacement().moveNode(data, force);
        }

        postAlgo();
    }

    public boolean canAlgo() {
        return !hasConverged();
    }

    protected AbstractForce getEdgeForce() {
        return edgeForce;
    }

    protected AbstractForce getNodeForce() {
        return nodeForce;
    }

    protected abstract Displacement getDisplacement();

    protected abstract void postAlgo();

    protected abstract boolean hasConverged();
}
