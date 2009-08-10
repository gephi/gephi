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
package org.gephi.layout.force.yifanHu;

import org.gephi.graph.api.ClusteredGraph;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.NodeData;
import org.gephi.layout.AbstractLayout;
import org.gephi.layout.GraphUtils;
import org.gephi.layout.api.Layout;
import org.gephi.layout.api.LayoutBuilder;
import org.gephi.layout.api.LayoutProperty;
import org.gephi.layout.force.AbstractForce;
import org.gephi.layout.force.BarnesHut;
import org.gephi.layout.force.Displacement;
import org.gephi.layout.force.ForceVector;
import org.gephi.layout.force.StepDisplacement;
import org.gephi.layout.force.quadtree.QuadTree;
import org.openide.nodes.Node.PropertySet;
import org.openide.nodes.Sheet;

/**
 * Hu's basic algorithm
 * @author Helder Suzuki <heldersuzuki@gephi.org>
 */
public class YifanHuLayout extends AbstractLayout implements Layout {

    private float optimalDistance;
    private float relativeStrength;
    private float step;
    private int progress;
    private float stepRatio;
    private boolean converged;
    private float energy0;
    private float energy;
    private ClusteredGraph graph;

    public YifanHuLayout(LayoutBuilder layoutBuilder) {
        super(layoutBuilder);
    }

    protected void postAlgo() {
        updateStep();
        if (Math.abs((energy - energy0) / energy) < 1e-3) {
            converged = true;
        }
    }

    private Displacement getDisplacement() {
        return new StepDisplacement(step);
    }

    private AbstractForce getEdgeForce() {
        return new SpringForce(getOptimalDistance());
    }

    private AbstractForce getNodeForce() {
        return new ElectricalForce(getRelativeStrength(), getOptimalDistance());
    }

    private void updateStep() {
        System.out.println("energy: " + energy);
        if (energy < energy0) {
            progress++;
            if (progress >= 5) {
                progress = 0;
                step /= stepRatio;
            }
        } else {
            progress = 0;
            step *= stepRatio;
        }
    }

    @Override
    public void resetPropertiesValues() {
        stepRatio = (float) 0.9;
        progress = 0;
        converged = false;
        setRelativeStrength((float) 0.2);
        setOptimalDistance((float) (Math.pow(getRelativeStrength(), 1.0 / 3) * GraphUtils.getAverageEdgeLength(graph)));
        step = getOptimalDistance() / 10000;
    }


    /* Maximum level for Barnes-Hut's quadtree */
    protected int getQuadTreeMaxLevel() {
        return 10;
    }

    public PropertySet[] getPropertySets() throws NoSuchMethodException {
        //        LayoutProperty[] properties = new LayoutProperty[3];
//        properties[0] = LayoutProperty.createProperty(YifanHuLayout.class, "optimalDistance");
//        properties[1] = LayoutProperty.createProperty(YifanHuLayout.class, "relativeStrength");
//        properties[2] = LayoutProperty.createProperty(YifanHuLayout.class, "stepRatio");
//        return properties;
        Sheet.Set set = Sheet.createPropertiesSet();
        set.setDisplayName("Yifan Hu's properties");
        set.put(LayoutProperty.createProperty(
            this, Float.class, "Optimal Distance",
            "The natural length of the springs.", "getOptimalDistance",
            "setOptimalDistance"));
        set.put(LayoutProperty.createProperty(
            this, Float.class, "Relative Strength",
            "The relative electrical force strength (compared to the springs)",
            "getRelativeStrength", "setRelativeStrength"));
        return new PropertySet[]{set};
    }

    /* theta is the parameter for Barnes-Hut opening criteria */
    private float getBarnesHutTheta() {
        return (float) 1.2;
    }

    @Override
    public void setGraphController(GraphController graphController) {
        super.setGraphController(graphController);
        graph = graphController.getHierarchicalUndirectedGraph().getClusteredGraph();
    }

    public void initAlgo() {
        energy = Float.POSITIVE_INFINITY;
        for (Node n : graph.getTopNodes()) {
            NodeData data = n.getNodeData();
            data.setLayoutData(new ForceVector());
        }
    }

    public boolean canAlgo() {
        return !converged;
    }

    public void endAlgo() {
    }

    public void goAlgo() {
        // Evaluates n^2 inter node forces using BarnesHut.
        QuadTree tree = QuadTree.buildTree(graph, getQuadTreeMaxLevel());

        BarnesHut barnes = new BarnesHut(getNodeForce());
        barnes.setTheta(getBarnesHutTheta());
        for (Node node : graph.getTopNodes()) {
            NodeData data = node.getNodeData();
            ForceVector layoutData = data.getLayoutData();

            ForceVector f = barnes.calculateForce(data, tree);
            layoutData.add(f);
        }

        // Apply edge forces.
        for (Edge e : GraphUtils.getTopEdges(graph)) {
            NodeData n1 = e.getSource().getNodeData();
            NodeData n2 = e.getTarget().getNodeData();
            ForceVector f1 = n1.getLayoutData();
            ForceVector f2 = n2.getLayoutData();

            ForceVector f = getEdgeForce().calculateForce(n1, n2);
            f1.add(f);
            f2.subtract(f);
        }

        System.out.println("step = " + step);
        // Apply displacements on nodes and calculate energy.
        energy0 = energy;
        energy = 0;
        for (Node n : graph.getTopNodes()) {
            NodeData data = n.getNodeData();
            ForceVector force = data.getLayoutData();

            energy += force.getEnergy();
            getDisplacement().moveNode(data, force);
        }
        System.out.println("energy0 = " + energy0 + "   energy = " + energy);
    }

    /**
     * @return the optimalDistance
     */
    public Float getOptimalDistance() {
        return optimalDistance;
    }

    /**
     * @param optimalDistance the optimalDistance to set
     */
    public void setOptimalDistance(Float optimalDistance) {
        this.optimalDistance = optimalDistance;
    }

    /**
     * @return the relativeStrength
     */
    public Float getRelativeStrength() {
        return relativeStrength;
    }

    /**
     * @param relativeStrength the relativeStrength to set
     */
    public void setRelativeStrength(Float relativeStrength) {
        this.relativeStrength = relativeStrength;
    }
}
