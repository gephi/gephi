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
import org.gephi.layout.api.Layout;
import org.gephi.layout.api.LayoutBuilder;
import org.gephi.layout.api.LayoutProperty;
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
    private double optimalDistance;
    private int minSize;
    private double minCoarseningRate;

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
                System.out.printf("Size: BEFORE = %d   AFTER = %d\n", graphSize, newGraphSize);
                break;
            }
        }

        Layout random = new RandomLayout(null, 1000);
        random.setGraphController(graphController);
        random.initAlgo();
        random.goAlgo();

        layout = yifanHu.buildLayout();
        layout.setGraphController(graphController);
        layout.resetPropertiesValues();
        layout.setAdaptiveCooling(false);
        layout.setStepRatio(0.95f);
        layout.setOptimalDistance(100f);
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
                layout = (YifanHuLayout) yifanHu.buildLayout();
                layout.setGraphController(graphController);
                layout.resetPropertiesValues();
                layout.setAdaptiveCooling(false);
                layout.setStepRatio(0.95f);
                layout.setOptimalDistance(100f);
                layout.initAlgo();
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
    }

    public LayoutProperty[] getProperties() {
        List<LayoutProperty> properties = new ArrayList<LayoutProperty>();
        final String MULTILEVEL_CATEGORY = "Multi-level";
        try {
            properties.add(LayoutProperty.createProperty(
                    this, Integer.class, "Minimum level size", MULTILEVEL_CATEGORY,
                    "The minimum amount of nodes every level must have (bigger values mean less levels)",
                    "getMinSize", "setMinSize"));
            properties.add(LayoutProperty.createProperty(
                    this, Double.class, "Minimum coarsening rate", MULTILEVEL_CATEGORY,
                    "The minimum relative size between two levels (smaller values mean less levels)",
                    "getMinCoarseningRate", "setMinCoarseningRate"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return properties.toArray(new LayoutProperty[0]);
    }

    /**
     * @return the optimalDistance
     */
    public Double getOptimalDistance() {
        return optimalDistance;
    }

    /**
     * @param optimalDistance the optimalDistance to set
     */
    public void setOptimalDistance(Double optimalDistance) {
        this.optimalDistance = optimalDistance;
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

    public interface CoarseningStrategy {

        public void coarsen(HierarchicalGraph graph);

        public void refine(HierarchicalGraph graph);
    }
}
