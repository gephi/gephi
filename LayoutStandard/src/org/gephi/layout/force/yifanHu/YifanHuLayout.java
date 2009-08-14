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
import org.gephi.graph.api.Spatial;
import org.gephi.layout.AbstractLayout;
import org.gephi.layout.GraphUtils;
import org.gephi.layout.api.Layout;
import org.gephi.layout.api.LayoutBuilder;
import org.gephi.layout.api.LayoutProperty;
import org.gephi.layout.force.AbstractForce;
import org.gephi.layout.force.quadtree.BarnesHut;
import org.gephi.layout.force.Displacement;
import org.gephi.layout.force.ForceVector;
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
    private int quadTreeMaxLevel;
    private float barnesHutTheta;
    private boolean converged;
    private boolean adaptiveCooling;
    private Displacement displacement;
    private double energy0;
    private double energy;
    private ClusteredGraph graph;

    public YifanHuLayout(LayoutBuilder layoutBuilder, Displacement displacement) {
        super(layoutBuilder);
        this.displacement = displacement;
    }

    protected void postAlgo() {
        updateStep();
        if (Math.abs((energy - energy0) / energy) < 1e-3) {
            converged = true;
        }
    }

    private Displacement getDisplacement() {
        displacement.setStep(step);
        return displacement;
    }

    private AbstractForce getEdgeForce() {
        return new SpringForce(getOptimalDistance());
    }

    private AbstractForce getNodeForce() {
        return new ElectricalForce(getRelativeStrength(), getOptimalDistance());
    }

    private void updateStep() {
        if (isAdaptiveCooling()) {
            if (energy < energy0) {
                progress++;
                if (progress >= 5) {
                    progress = 0;
                    setStep(getStep() / stepRatio);
                }
            } else {
                progress = 0;
                setStep(getStep() * stepRatio);
            }
        } else {
            setStep(step * stepRatio);
        }
    }

    @Override
    public void resetPropertiesValues() {
        stepRatio = (float) 0.9;
        setRelativeStrength((float) 0.2);
        setOptimalDistance((float) (Math.pow(getRelativeStrength(), 1.0 / 3) * GraphUtils.getAverageEdgeLength(graph)));
        setStep(100f);
        setQuadTreeMaxLevel(10);
        setBarnesHutTheta(1.2f);
    }

    public PropertySet[] getPropertySets() throws NoSuchMethodException {
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
        set.put(LayoutProperty.createProperty(
            this, Float.class, "Step size",
            "The step size used in the integration phase",
            "getStep", "setStep"));
        set.put(LayoutProperty.createProperty(
            this, Boolean.class, "Adaptive Cooling",
            "Controls the use of adaptive cooling",
            "isAdaptiveCooling", "setAdaptiveCooling"));

        Sheet.Set barnesSet = Sheet.createPropertiesSet();
        barnesSet.setDisplayName("Barnes-Hut's properties");
        barnesSet.put(LayoutProperty.createProperty(
            this, Integer.class, "Quadtree Max Level",
            "The max level to be used in the quadtree representation (smaller is less accurate, but faster)",
            "getQuadTreeMaxLevel", "setQuadTreeMaxLevel"));
        barnesSet.put(LayoutProperty.createProperty(
            this, Float.class, "Theta",
            "The theta parameter for Barnes-Hut opening criteria (smaller is more accurate, but more expensive)",
            "getBarnesHutTheta", "setBarnesHutTheta"));
        return new PropertySet[]{set, barnesSet};
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
        progress = 0;
        converged = false;
    }

    public boolean canAlgo() {
        return !converged;
    }

    public void endAlgo() {
    }

    public void goAlgo() {
        // Evaluates n^2 inter node forces using BarnesHut.
        QuadTree tree = QuadTree.buildTree(graph, getQuadTreeMaxLevel());

        double electricEnergy = 0; ///////////////////////
        double springEnergy = 0; ///////////////////////
        BarnesHut barnes = new BarnesHut(getNodeForce());
        barnes.setTheta(getBarnesHutTheta());
        for (Node node : graph.getTopNodes()) {
            NodeData data = node.getNodeData();
            ForceVector layoutData = data.getLayoutData();

            ForceVector f = barnes.calculateForce(data, tree);
            layoutData.add(f);
            electricEnergy += f.getEnergy();
        }

        // Apply edge forces.

        for (Edge e : graph.getEdgesAndMetaEdges()) {
            NodeData n1 = e.getSource().getNodeData();
            NodeData n2 = e.getTarget().getNodeData();
            ForceVector f1 = n1.getLayoutData();
            ForceVector f2 = n2.getLayoutData();

            ForceVector f = getEdgeForce().calculateForce(n1, n2);
            f1.add(f);
            f2.subtract(f);
        }

        System.out.println("step = " + getStep());
        // Calculate energy and max force.
        energy0 = energy;
        energy = 0;
        double maxForce = 1;
        for (Node n : graph.getTopNodes()) {
            NodeData data = n.getNodeData();
            ForceVector force = data.getLayoutData();

            energy += force.getNorm();
            maxForce = Math.max(maxForce, force.getNorm());
        }

        // Apply displacements on nodes.
        for (Node n : graph.getTopNodes()) {
            NodeData data = n.getNodeData();
            ForceVector force = data.getLayoutData();

            force.multiply((float) (1.0 / maxForce));
            getDisplacement().moveNode(data, force);
        }
        postAlgo();
        springEnergy = energy - electricEnergy;
        System.out.println("electric: " + electricEnergy + "    spring: " + springEnergy);
        System.out.println("energy0 = " + energy0 + "   energy = " + energy);
    }


    /* Maximum level for Barnes-Hut's quadtree */
    public Integer getQuadTreeMaxLevel() {
        return quadTreeMaxLevel;
    }

    public void setQuadTreeMaxLevel(Integer quadTreeMaxLevel) {
        this.quadTreeMaxLevel = quadTreeMaxLevel;
    }

    /* theta is the parameter for Barnes-Hut opening criteria */
    public Float getBarnesHutTheta() {
        return barnesHutTheta;
    }

    public void setBarnesHutTheta(Float barnesHutTheta) {
        this.barnesHutTheta = barnesHutTheta;
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

    /**
     * @return the step
     */
    public Float getStep() {
        return step;
    }

    /**
     * @param step the step to set
     */
    public void setStep(Float step) {
        this.step = step;
    }

    /**
     * @return the adaptiveCooling
     */
    public Boolean isAdaptiveCooling() {
        return adaptiveCooling;
    }

    /**
     * @param adaptiveCooling the adaptiveCooling to set
     */
    public void setAdaptiveCooling(Boolean adaptiveCooling) {
        this.adaptiveCooling = adaptiveCooling;
    }

    /**
     * Fa = (n2 - n1) * ||n2 - n1|| / K
     * @author Helder Suzuki <heldersuzuki@gephi.org>
     */
    public class SpringForce extends AbstractForce {

        private float optimalDistance;

        public SpringForce(float optimalDistance) {
            this.optimalDistance = optimalDistance;
        }

        @Override
        public ForceVector calculateForce(Spatial node1, Spatial node2,
                                          float distance) {
            ForceVector f = new ForceVector(node2.x() - node1.x(),
                                            node2.y() - node1.y());
            f.multiply(distance / optimalDistance);
            return f;
        }

        public void setOptimalDistance(Float optimalDistance) {
            this.optimalDistance = optimalDistance;
        }

        public Float getOptimalDistance() {
            return optimalDistance;
        }
    }

    /**
     * Fr = -C*K*K*(n2-n1)/||n2-n1||
     * @author Helder Suzuki <heldersuzuki@gephi.org>
     */
    public class ElectricalForce extends AbstractForce {

        private float relativeStrength;
        private float optimalDistance;

        public ElectricalForce(float relativeStrength, float optimalDistance) {
            this.relativeStrength = relativeStrength;
            this.optimalDistance = optimalDistance;
        }

        @Override
        public ForceVector calculateForce(Spatial node1, Spatial node2,
                                          float distance) {
            ForceVector f = new ForceVector(node2.x() - node1.x(),
                                            node2.y() - node1.y());
            float scale = -relativeStrength * optimalDistance * optimalDistance / (distance * distance);
            if (Float.isNaN(scale) || Float.isInfinite(scale)) {
                scale = -1;
            }

            f.multiply(scale);
            return f;
        }
    }
}