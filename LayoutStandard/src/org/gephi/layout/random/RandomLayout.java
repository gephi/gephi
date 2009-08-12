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
package org.gephi.layout.random;

import java.util.Random;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.UndirectedGraph;
import org.gephi.layout.AbstractLayout;
import org.gephi.layout.api.Layout;
import org.gephi.layout.api.LayoutBuilder;
import org.gephi.layout.api.LayoutProperty;
import org.openide.nodes.Node.PropertySet;
import org.openide.nodes.Sheet;

/**
 *
 * @author Helder Suzuki <heldersuzuki@gephi.org>
 */
public class RandomLayout extends AbstractLayout implements Layout {

    private Random random;
    private UndirectedGraph graph;
    private boolean converged;
    private double size;

    public RandomLayout(LayoutBuilder layoutBuilder, double size) {
        super(layoutBuilder);
        this.size = size;
        random = new Random();
    }

    @Override
    public void setGraphController(GraphController graphController) {
        super.setGraphController(graphController);
        graph = graphController.getUndirectedGraph();
    }

    public void initAlgo() {
        converged = false;
    }

    public void goAlgo() {
        for (Node n : graph.getNodes()) {
            n.getNodeData().setX((float) (-size / 2 + size * random.nextDouble()));
            n.getNodeData().setY((float) (-size / 2 + size * random.nextDouble()));
        }
        converged = true;
    }

    public boolean canAlgo() {
        return !converged;
    }

    public void endAlgo() {
    }

    public PropertySet[] getPropertySets() throws NoSuchMethodException {
        Sheet.Set set = Sheet.createPropertiesSet();
        set.put(LayoutProperty.createProperty(
            this, Double.class, "Space size",
            "The size of the space to randomly distribute the nodes.",
            "getSize", "setSize"));
        return new PropertySet[]{set};
    }

    public void resetPropertiesValues() {
    }

    public void setSize(Double size) {
        this.size = size;
    }

    public Double getSize() {
        return size;
    }
}
