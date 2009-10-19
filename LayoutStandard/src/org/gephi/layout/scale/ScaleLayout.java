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
package org.gephi.layout.scale;

import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.Node;
import org.gephi.layout.AbstractLayout;
import org.gephi.layout.api.Layout;
import org.gephi.layout.api.LayoutBuilder;
import org.gephi.layout.api.LayoutProperty;
import org.openide.nodes.Node.PropertySet;
import org.openide.nodes.Sheet;

/**
 * Sample layout that scales the graph.
 * @author Helder Suzuki <heldersuzuki@gephi.org>
 */
public class ScaleLayout extends AbstractLayout implements Layout {

    private double scale;
    private Graph graph;

    public ScaleLayout(LayoutBuilder layoutBuilder, double scale) {
        super(layoutBuilder);
        this.scale = scale;
    }

    @Override
    public void setGraphController(GraphController graphController) {
        super.setGraphController(graphController);
        graph = graphController.getModel().getGraphVisible();
    }

    public void initAlgo() {
        setConverged(false);
    }

    public void goAlgo() {
        double xMean = 0, yMean = 0;
        for (Node n : graph.getNodes()) {
            xMean += n.getNodeData().x();
            yMean += n.getNodeData().x();
        }
        xMean /= graph.getNodeCount();
        yMean /= graph.getNodeCount();

        for (Node n : graph.getNodes()) {
            double dx = (n.getNodeData().x() - xMean) * getScale();
            double dy = (n.getNodeData().y() - yMean) * getScale();

            n.getNodeData().setX((float) (xMean + dx));
            n.getNodeData().setY((float) (yMean + dy));
        }
        setConverged(true);
    }

    public void endAlgo() {
    }

    public PropertySet[] getPropertySets() throws NoSuchMethodException {
        Sheet.Set set = Sheet.createPropertiesSet();
        set.put(LayoutProperty.createProperty(
            this, Double.class, "Scale factor",
            "Scale factor", "getScale", "setScale"));
        return new PropertySet[]{set};
    }

    public void resetPropertiesValues() {
    }

    /**
     * @return the scale
     */
    public Double getScale() {
        return scale;
    }

    /**
     * @param scale the scale to set
     */
    public void setScale(Double scale) {
        this.scale = scale;
    }
}
