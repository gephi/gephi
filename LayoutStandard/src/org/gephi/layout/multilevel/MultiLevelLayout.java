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

import org.gephi.graph.api.ClusteredGraph;
import org.gephi.graph.api.GraphController;
import org.gephi.layout.AbstractLayout;
import org.gephi.layout.api.Layout;
import org.gephi.layout.api.LayoutBuilder;
import org.openide.nodes.Node.PropertySet;

/**
 *
 * @author Helder Suzuki <heldersuzuki@gephi.org>
 */
public class MultiLevelLayout extends AbstractLayout implements Layout {

    private ClusteredGraph graph;
    private boolean acabou;
    private int level;
    private Layout layout;
    private LayoutBuilder subLayoutBuilder;
    private CoarseningStrategy coarseningStrategy;

    public MultiLevelLayout(LayoutBuilder layoutBuilder,
                            CoarseningStrategy coarseningStrategy,
                            LayoutBuilder subLayoutBuilder) {
        super(layoutBuilder);
        this.coarseningStrategy = coarseningStrategy;
        this.subLayoutBuilder = subLayoutBuilder;
    }

    @Override
    public void setGraphController(GraphController graphController) {
        super.setGraphController(graphController);
        graph = graphController.getHierarchicalUndirectedGraph().getClusteredGraph();
    }

    public void initAlgo() {
        level = 0;
        while (level < 2) {
            coarseningStrategy.coarsen(graph);
            level++;
        }
        System.out.println("Level = " + level);
        layout = subLayoutBuilder.buildLayout();
        layout.setGraphController(graphController);
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
                layout = subLayoutBuilder.buildLayout();
                layout.setGraphController(graphController);
                layout.initAlgo();
            } else {
                acabou = true;
                layout = null;
            }
        }
    }

    public boolean canAlgo() {
        return !acabou;
    }

    public void endAlgo() {
    }

    public void resetPropertiesValues() {
        acabou = false;
    }

    public PropertySet[] getPropertySets() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
