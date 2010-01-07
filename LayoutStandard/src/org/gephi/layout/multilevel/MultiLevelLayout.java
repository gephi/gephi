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
package org.gephi.layout.multilevel;

import java.util.ArrayList;
import java.util.List;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.HierarchicalGraph;
import org.gephi.layout.AbstractLayout;
import org.gephi.layout.spi.Layout;
import org.gephi.layout.spi.LayoutBuilder;
import org.gephi.layout.spi.LayoutProperty;
import org.gephi.layout.force.yifanHu.YifanHuLayout;
import org.gephi.layout.force.yifanHu.YifanHuProportional;
import org.gephi.layout.random.RandomLayout;

/**
 *
 * @author Helder Suzuki <heldersuzuki@gephi.org>
 */
public class MultiLevelLayout extends AbstractLayout implements Layout {

    private HierarchicalGraph graph;
    private int level;
    private YifanHuLayout layout;
    private YifanHuProportional yifanHu;
    private CoarseningStrategy coarseningStrategy;
    private int minSize;
    private double minCoarseningRate;
    private float stepRatio;
    private float optimalDistance;
    private int quadTreeMaxLevel;
    private float barnesHutTheta;

    public MultiLevelLayout(LayoutBuilder layoutBuilder,
                            CoarseningStrategy coarseningStrategy) {
        super(layoutBuilder);
        this.coarseningStrategy = coarseningStrategy;
        //     this.yifanHu = new YifanHu();
        this.yifanHu = new YifanHuProportional();
    }

    @Override
    public void setGraphController(GraphController graphController) {
        super.setGraphController(graphController);
        graph = graphController.getModel().getHierarchicalGraphVisible();
    }

    public void initAlgo() {
        setConverged(false);
        level = 0;

        while (true) {
            int graphSize = graph.getTopNodes().toArray().length;
            coarseningStrategy.coarsen(graph);
            level++;
            int newGraphSize = graph.getTopNodes().toArray().length;
            if (newGraphSize < getMinSize() || newGraphSize > graphSize * getMinCoarseningRate()) {
                break;
            }
        }

        Layout random = new RandomLayout(null, 1000);
        random.setGraphController(graphController);
        random.initAlgo();
        random.goAlgo();

        initYifanHu();
    }

    void initYifanHu() {
        layout = yifanHu.buildLayout();
        layout.setGraphController(graphController);
        layout.resetPropertiesValues();
        layout.setAdaptiveCooling(false);
        layout.setStepRatio(stepRatio);
        layout.setOptimalDistance(optimalDistance);
        layout.setBarnesHutTheta(barnesHutTheta);
        layout.setQuadTreeMaxLevel(quadTreeMaxLevel);
        layout.initAlgo();
    }

    public void goAlgo() {
        if (layout.canAlgo()) {
            layout.goAlgo();
        } else {
            layout.endAlgo();
            if (level > 0) {
                coarseningStrategy.refine(graph);
                level--;

                initYifanHu();
            } else {
                setConverged(true);
                layout = null;
            }
        }
    }

    public void endAlgo() {
        while (level > 0) {
            coarseningStrategy.refine(graph);
            level--;
        }
    }

    public void resetPropertiesValues() {
        setMinSize(3);
        setMinCoarseningRate(0.75d);
        setStepRatio(0.97f);
        setOptimalDistance(100f);
        setQuadTreeMaxLevel(10);
        setBarnesHutTheta(1.2f);
    }

    public LayoutProperty[] getProperties() {
        List<LayoutProperty> properties = new ArrayList<LayoutProperty>();
        final String MULTILEVEL_CATEGORY = "Multi-level";
        final String YIFANHU_CATEGORY = "Yifan Hu's properties";
        final String BARNESHUT_CATEGORY = "Barnes-Hut's properties";

        try {
            properties.add(LayoutProperty.createProperty(
                this, Integer.class, "Minimum level size", MULTILEVEL_CATEGORY,
                "The minimum amount of nodes every level must have (bigger values mean less levels)",
                "getMinSize", "setMinSize"));
            properties.add(LayoutProperty.createProperty(
                this, Double.class, "Minimum coarsening rate", MULTILEVEL_CATEGORY,
                "The minimum relative size between two levels (smaller values mean less levels)",
                "getMinCoarseningRate", "setMinCoarseningRate"));

            properties.add(LayoutProperty.createProperty(
                this, Float.class, "Step ratio", YIFANHU_CATEGORY,
                "The ratio used to update the step size across iterations.",
                "getStepRatio", "setStepRatio"));
            properties.add(LayoutProperty.createProperty(
                this, Float.class, "Optimal Distance", YIFANHU_CATEGORY,
                "The natural length of the springs. Bigger values mean nodes will be farther apart.",
                "getOptimalDistance", "setOptimalDistance"));

            properties.add(LayoutProperty.createProperty(
                this, Integer.class, "Quadtree Max Level", BARNESHUT_CATEGORY,
                "The maximun level to be used in the quadtree representation. Greater values mean more accuracy.",
                "getQuadTreeMaxLevel", "setQuadTreeMaxLevel"));
            properties.add(LayoutProperty.createProperty(
                this, Float.class, "Theta", BARNESHUT_CATEGORY,
                "The theta parameter for Barnes-Hut opening criteria. Smaller values mean more accuracy.",
                "getBarnesHutTheta", "setBarnesHutTheta"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return properties.toArray(new LayoutProperty[0]);
    }

    /**
     * @return the minSize
     */
    public Integer getMinSize() {
        return minSize;
    }

    /**
     * @param minSize the minSize to set
     */
    public void setMinSize(Integer minSize) {
        this.minSize = minSize;
    }

    /**
     * @return the minCoarseningRate
     */
    public Double getMinCoarseningRate() {
        return minCoarseningRate;
    }

    /**
     * @param minCoarseningRate the minCoarseningRate to set
     */
    public void setMinCoarseningRate(Double minCoarseningRate) {
        this.minCoarseningRate = minCoarseningRate;
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
     * @return the quadTreeMaxLevel
     */
    public Integer getQuadTreeMaxLevel() {
        return quadTreeMaxLevel;
    }

    /**
     * @param quadTreeMaxLevel the quadTreeMaxLevel to set
     */
    public void setQuadTreeMaxLevel(Integer quadTreeMaxLevel) {
        this.quadTreeMaxLevel = quadTreeMaxLevel;
    }

    /**
     * @return the barnesHutTheta
     */
    public Float getBarnesHutTheta() {
        return barnesHutTheta;
    }

    /**
     * @param barnesHutTheta the barnesHutTheta to set
     */
    public void setBarnesHutTheta(Float barnesHutTheta) {
        this.barnesHutTheta = barnesHutTheta;
    }

    public interface CoarseningStrategy {

        public void coarsen(HierarchicalGraph graph);

        public void refine(HierarchicalGraph graph);
    }
}
