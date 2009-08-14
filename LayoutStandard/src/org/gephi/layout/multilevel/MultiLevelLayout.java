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

import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.HierarchicalDirectedGraph;
import org.gephi.layout.AbstractLayout;
import org.gephi.layout.api.Layout;
import org.gephi.layout.api.LayoutBuilder;
import org.gephi.layout.force.yifanHu.YifanHu;
import org.gephi.layout.force.yifanHu.YifanHuLayout;
import org.gephi.layout.random.RandomLayout;
import org.openide.nodes.Node.PropertySet;

/**
 *
 * @author Helder Suzuki <heldersuzuki@gephi.org>
 */
public class MultiLevelLayout extends AbstractLayout implements Layout {

    private HierarchicalDirectedGraph graph;
    private boolean converged;
    private int level;
    private YifanHuLayout layout;
    private YifanHu yifanHu;
    private CoarseningStrategy coarseningStrategy;

    public MultiLevelLayout(LayoutBuilder layoutBuilder,
                            CoarseningStrategy coarseningStrategy) {
        super(layoutBuilder);
        this.coarseningStrategy = coarseningStrategy;
        this.yifanHu = new YifanHu();
    }

    @Override
    public void setGraphController(GraphController graphController) {
        super.setGraphController(graphController);
        graph = graphController.getHierarchicalDirectedGraph();
    }

    public void initAlgo() {
        converged = false;
        level = 0;
        while (graph.getClusteredGraph().getTopNodes().toArray().length > 20) {
            coarseningStrategy.coarsen(graph);
            System.out.println("COARSEN!!");
            level++;
        }

        Layout random = new RandomLayout(null, 1000);
        random.setGraphController(graphController);
        random.initAlgo();
        random.goAlgo();

        System.out.println("Level = " + level);
        layout = yifanHu.buildLayout();
        layout.setGraphController(graphController);
        layout.resetPropertiesValues();
        layout.initAlgo();

    }

    public void goAlgo() {
        if (layout.canAlgo()) {
            layout.goAlgo();
        } else {
            System.out.println("Level = " + level);
            layout.endAlgo();
            if (level > 0) {
                coarseningStrategy.refine(graph);
                level--;
                layout = (YifanHuLayout) yifanHu.buildLayout();
                layout.setGraphController(graphController);
                layout.resetPropertiesValues();
                layout.initAlgo();

            } else {
                converged = true;
                layout = null;
            }
        }
    }

    public boolean canAlgo() {
        return !converged;
    }

    public void endAlgo() {
    }

    public void resetPropertiesValues() {
    }

    public PropertySet[] getPropertySets() {
        return new PropertySet[]{};
    }

    public interface CoarseningStrategy {

        public void coarsen(HierarchicalDirectedGraph graph);

        public void refine(HierarchicalDirectedGraph graph);
    }
}
