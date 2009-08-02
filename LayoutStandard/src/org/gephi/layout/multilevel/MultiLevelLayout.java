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
package org.gephi.layout.multilevel;

import javax.swing.Icon;
import javax.swing.JPanel;
import org.gephi.graph.api.ClusteredGraph;
import org.gephi.graph.api.GraphController;
import org.gephi.layout.api.Layout;
import org.gephi.layout.api.LayoutBuilder;
import org.gephi.layout.api.LayoutProperty;

/**
 *
 * @author Helder Suzuki <heldersuzuki@gmail.com>
 */
public class MultiLevelLayout implements Layout {

    private ClusteredGraph graph;
    private boolean acabou;
    private int level;
    private GraphController graphController;
    private Layout layout;
    private LayoutBuilder layoutBuilder;
    private CoarseningStrategy coarseningStrategy;
    private String name;
    private String description;

    public MultiLevelLayout(CoarseningStrategy coarseningStrategy,
                            LayoutBuilder layoutBuilder,
                            String name,
                            String description) {
        this.coarseningStrategy = coarseningStrategy;
        this.layoutBuilder = layoutBuilder;
        this.name = name;
        this.description = description;
    }

    public void initAlgo(GraphController graphController) {
        this.graphController = graphController;
        graph = graphController.getHierarchicalUndirectedGraph().getClusteredGraph();
        level = 0;
        while (level < 2) {
            coarseningStrategy.coarsen(graph);
            level++;
        }
        System.out.println("Level = " + level);
        layout = layoutBuilder.buildLayout();
        layout.initAlgo(graphController);
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
                layout = layoutBuilder.buildLayout();
                layout.initAlgo(graphController);
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

    public LayoutProperty[] getProperties() {
        return null;
    }

    public void resetPropertiesValues() {
        acabou = false;
    }

    public JPanel getPanel() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Icon getIcon() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
