/*
Copyright 2008-2010 Gephi
Authors : Helder Suzuki <heldersuzuki@gephi.org>
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
package org.gephi.layout.plugin.force.yifanHu;

import java.util.ArrayList;
import java.util.List;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.HierarchicalGraph;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.NodeData;
import org.gephi.graph.api.Spatial;
import org.gephi.layout.plugin.AbstractLayout;
import org.gephi.layout.plugin.GraphUtils;
import org.gephi.layout.plugin.force.AbstractForce;
import org.gephi.layout.plugin.force.Displacement;
import org.gephi.layout.plugin.force.ForceVector;
import org.gephi.layout.plugin.force.quadtree.BarnesHut;
import org.gephi.layout.spi.Layout;
import org.gephi.layout.spi.LayoutBuilder;
import org.gephi.layout.spi.LayoutProperty;
import org.gephi.layout.plugin.force.quadtree.QuadTree;
import org.openide.util.NbBundle;

/**
 * Hu's basic algorithm
 * @author Helder Suzuki <heldersuzuki@gephi.org>
 */
public class YifanHuLayout extends AbstractLayout implements Layout {

    private float optimalDistance;
    private float relativeStrength;
    private float step;
    private float initialStep;
    private int progress;
    private float stepRatio;
    private int quadTreeMaxLevel;
    private float barnesHutTheta;
    private float convergenceThreshold;
    private boolean adaptiveCooling;
    private Displacement displacement;
    private double energy0;
    private double energy;
    private HierarchicalGraph graph;

    public YifanHuLayout(LayoutBuilder layoutBuilder, Displacement displacement) {
        super(layoutBuilder);
        this.displacement = displacement;
    }

    protected void postAlgo() {
        updateStep();
        if (Math.abs((energy - energy0) / energy) < getConvergenceThreshold()) {
            setConverged(true);
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
                    setStep(step / getStepRatio());
                }
            } else {
                progress = 0;
                setStep(step * getStepRatio());
            }
        } else {
            setStep(step * getStepRatio());
        }
    }

    @Override
    public void resetPropertiesValues() {
        setStepRatio((float) 0.95);
        setRelativeStrength((float) 0.2);
        if (graph != null) {
            setOptimalDistance((float) (Math.pow(getRelativeStrength(), 1.0 / 3) * GraphUtils.getAverageEdgeLength(graph)));
        } else {
            setOptimalDistance(100.0f);
        }

        setInitialStep(optimalDistance / 5);
        setStep(initialStep);
        setQuadTreeMaxLevel(10);
        setBarnesHutTheta(1.2f);
        setAdaptiveCooling(true);
        setConvergenceThreshold(1e-4f);
    }

    public LayoutProperty[] getProperties() {
        List<LayoutProperty> properties = new ArrayList<LayoutProperty>();
        final String YIFANHU_CATEGORY = "Yifan Hu's properties";
        final String BARNESHUT_CATEGORY = "Barnes-Hut's properties";

        try {
            properties.add(LayoutProperty.createProperty(
                    this, Float.class, 
                    NbBundle.getMessage(getClass(), "YifanHu.optimalDistance.name"),
                    YIFANHU_CATEGORY,
                    "YifanHu.optimalDistance.name",
                    NbBundle.getMessage(getClass(), "YifanHu.optimalDistance.desc"),
                    "getOptimalDistance", "setOptimalDistance"));
            properties.add(LayoutProperty.createProperty(
                    this, Float.class, 
                    NbBundle.getMessage(getClass(), "YifanHu.relativeStrength.name"),
                    YIFANHU_CATEGORY,
                    "YifanHu.relativeStrength.name",
                    NbBundle.getMessage(getClass(), "YifanHu.relativeStrength.desc"),
                    "getRelativeStrength", "setRelativeStrength"));

            properties.add(LayoutProperty.createProperty(
                    this, Float.class, 
                    NbBundle.getMessage(getClass(), "YifanHu.initialStepSize.name"),
                    YIFANHU_CATEGORY,
                    "YifanHu.initialStepSize.name",
                    NbBundle.getMessage(getClass(), "YifanHu.initialStepSize.desc"),
                    "getInitialStep", "setInitialStep"));
            properties.add(LayoutProperty.createProperty(
                    this, Float.class, 
                    NbBundle.getMessage(getClass(), "YifanHu.stepRatio.name"),
                    YIFANHU_CATEGORY,
                    "YifanHu.stepRatio.name",
                    NbBundle.getMessage(getClass(), "YifanHu.stepRatio.desc"),
                    "getStepRatio", "setStepRatio"));
            properties.add(LayoutProperty.createProperty(
                    this, Boolean.class, 
                    NbBundle.getMessage(getClass(), "YifanHu.adaptativeCooling.name"),
                    YIFANHU_CATEGORY,
                    "YifanHu.adaptativeCooling.name",
                    NbBundle.getMessage(getClass(), "YifanHu.adaptativeCooling.desc"),
                    "isAdaptiveCooling", "setAdaptiveCooling"));
            properties.add(LayoutProperty.createProperty(
                    this, Float.class, 
                    NbBundle.getMessage(getClass(), "YifanHu.convergenceThreshold.name"),
                    YIFANHU_CATEGORY,
                    "YifanHu.convergenceThreshold.name",
                    NbBundle.getMessage(getClass(), "YifanHu.convergenceThreshold.desc"),
                    "getConvergenceThreshold", "setConvergenceThreshold"));
            properties.add(LayoutProperty.createProperty(
                    this, Integer.class, 
                    NbBundle.getMessage(getClass(), "YifanHu.quadTreeMaxLevel.name"),
                    BARNESHUT_CATEGORY,
                    "YifanHu.quadTreeMaxLevel.name",
                    NbBundle.getMessage(getClass(), "YifanHu.quadTreeMaxLevel.desc"),
                    "getQuadTreeMaxLevel", "setQuadTreeMaxLevel"));
            properties.add(LayoutProperty.createProperty(
                    this, Float.class, 
                    NbBundle.getMessage(getClass(), "YifanHu.theta.name"),
                    BARNESHUT_CATEGORY,
                    "YifanHu.theta.name",
                    NbBundle.getMessage(getClass(), "YifanHu.theta.desc"),
                    "getBarnesHutTheta", "setBarnesHutTheta"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return properties.toArray(new LayoutProperty[0]);
    }

    public void initAlgo() {
        if (graphModel == null) {
            return;
        }
        graph = graphModel.getHierarchicalGraphVisible();
        energy = Float.POSITIVE_INFINITY;
        for (Node n : graph.getNodes()) {
            NodeData data = n.getNodeData();
            data.setLayoutData(new ForceVector());
        }
        progress = 0;
        setConverged(false);
        setStep(initialStep);
    }

    public void endAlgo() {
        for (Node node : graph.getNodes()) {
            NodeData data = node.getNodeData();
            data.setLayoutData(null);
        }
    }

    public void goAlgo() {
        graph = graphModel.getHierarchicalGraphVisible();
        graph.readLock();
        Node[] nodes = graph.getNodes().toArray();
        for (Node n : nodes) {
            if (n.getNodeData().getLayoutData() == null || !(n.getNodeData().getLayoutData() instanceof ForceVector)) {
                n.getNodeData().setLayoutData(new ForceVector());
            }
        }

        // Evaluates n^2 inter node forces using BarnesHut.
        QuadTree tree = QuadTree.buildTree(graph, getQuadTreeMaxLevel());

//        double electricEnergy = 0; ///////////////////////
//        double springEnergy = 0; ///////////////////////
        BarnesHut barnes = new BarnesHut(getNodeForce());
        barnes.setTheta(getBarnesHutTheta());
        for (Node node : nodes) {
            NodeData data = node.getNodeData();
            ForceVector layoutData = data.getLayoutData();

            ForceVector f = barnes.calculateForce(data, tree);
            layoutData.add(f);
//            electricEnergy += f.getEnergy();
        }

        // Apply edge forces.

        for (Edge e : graph.getEdgesAndMetaEdges()) {
            if (!e.getSource().equals(e.getTarget())) {
                NodeData n1 = e.getSource().getNodeData();
                NodeData n2 = e.getTarget().getNodeData();
                ForceVector f1 = n1.getLayoutData();
                ForceVector f2 = n2.getLayoutData();

                ForceVector f = getEdgeForce().calculateForce(n1, n2);
                f1.add(f);
                f2.subtract(f);
            }
        }

        // Calculate energy and max force.
        energy0 = energy;
        energy = 0;
        double maxForce = 1;
        for (Node n : nodes) {
            NodeData data = n.getNodeData();
            ForceVector force = data.getLayoutData();

            energy += force.getNorm();
            maxForce = Math.max(maxForce, force.getNorm());
        }

        // Apply displacements on nodes.
        for (Node n : nodes) {
            NodeData data = n.getNodeData();
            if (!data.isFixed()) {
                ForceVector force = data.getLayoutData();

                force.multiply((float) (1.0 / maxForce));
                getDisplacement().moveNode(data, force);
            }
        }
        postAlgo();
//        springEnergy = energy - electricEnergy;
//        System.out.println("electric: " + electricEnergy + "    spring: " + springEnergy);
//        System.out.println("energy0 = " + energy0 + "   energy = " + energy);
        graph.readUnlock();
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
     * @return the stepRatio
     */
    public Float getStepRatio() {
        return stepRatio;
    }

    /**
     * @param stepRatio the stepRatio to set
     */
    public void setStepRatio(Float stepRatio) {
        this.stepRatio = stepRatio;
    }

    /**
     * @return the convergenceThreshold
     */
    public Float getConvergenceThreshold() {
        return convergenceThreshold;
    }

    /**
     * @param convergenceThreshold the convergenceThreshold to set
     */
    public void setConvergenceThreshold(Float convergenceThreshold) {
        this.convergenceThreshold = convergenceThreshold;
    }

    /**
     * @return the initialStep
     */
    public Float getInitialStep() {
        return initialStep;
    }

    /**
     * @param initialStep the initialStep to set
     */
    public void setInitialStep(Float initialStep) {
        this.initialStep = initialStep;
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
