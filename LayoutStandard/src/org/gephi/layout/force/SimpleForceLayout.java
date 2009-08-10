/*
Copyright 2008-2009 Gephi
Authors : Helder Suzuki <heldersuzuki@gephi.org>
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

import org.gephi.graph.api.ClusteredUndirectedGraph;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.NodeData;
import org.gephi.layout.AbstractLayout;
import org.gephi.layout.GraphUtils;
import org.gephi.layout.api.Layout;
import org.gephi.layout.api.LayoutBuilder;
import org.gephi.layout.force.quadtree.QuadTree;
import org.openide.nodes.Node.PropertySet;

/**
 *
 * @author Helder Suzuki <heldersuzuki@gephi.org>
 */
public class SimpleForceLayout extends AbstractLayout implements Layout {

    public ClusteredUndirectedGraph graph;
    public float energy0;
    public float energy;
    private AbstractForce edgeForce;
    private AbstractForce nodeForce;
    private Displacement displacement;
    private ConvergenceCriterium converenceCriterium;

    public SimpleForceLayout(LayoutBuilder layoutBuilder) {
        super(layoutBuilder);
    }

    public void initAlgo() {
        energy = Float.POSITIVE_INFINITY;
    }

    @Override
    public void setGraphController(GraphController graphController) {
        super.setGraphController(graphController);
        graph = graphController.getHierarchicalUndirectedGraph().getClusteredGraph();
    }

    public void setGraph(ClusteredUndirectedGraph graph) {
        this.graph = graph;
    }

    /* Maximum level for Barnes-Hut's quadtree */
    private int getQuadTreeMaxLevel() {
        return Integer.MAX_VALUE;
    }

    /* theta is the parameter for Barnes-Hut opening criteria */
    private float getBarnesHutTheta() {
        return (float) 1.2;
    }

    public void goAlgo() {
        // Evaluates n^2 inter node forces using BarnesHut.
        BarnesHut barnes = new BarnesHut(getNodeForce());
        barnes.setTheta(getBarnesHutTheta());
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
    }

    public boolean canAlgo() {
        return getConverenceCriterium().hasConverged();
    }

    public void endAlgo() {
    }

    /**
     * @return the edgeForce
     */
    public AbstractForce getEdgeForce() {
        return edgeForce;
    }

    /**
     * @param edgeForce the edgeForce to set
     */
    public void setEdgeForce(AbstractForce edgeForce) {
        this.edgeForce = edgeForce;
    }

    /**
     * @return the nodeForce
     */
    public AbstractForce getNodeForce() {
        return nodeForce;
    }

    /**
     * @param nodeForce the nodeForce to set
     */
    public void setNodeForce(AbstractForce nodeForce) {
        this.nodeForce = nodeForce;
    }

    /**
     * @return the displacement
     */
    public Displacement getDisplacement() {
        return displacement;
    }

    /**
     * @param displacement the displacement to set
     */
    public void setDisplacement(Displacement displacement) {
        this.displacement = displacement;
    }

    /**
     * @return the converenceCriterium
     */
    public ConvergenceCriterium getConverenceCriterium() {
        return converenceCriterium;
    }

    public void resetPropertiesValues() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public PropertySet[] getPropertySets() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
