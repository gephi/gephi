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
package org.gephi.layout.plugin.scale;

import java.util.ArrayList;
import java.util.List;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.Node;
import org.gephi.layout.plugin.AbstractLayout;
import org.gephi.layout.spi.Layout;
import org.gephi.layout.spi.LayoutBuilder;
import org.gephi.layout.spi.LayoutProperty;

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

    public void initAlgo() {
        graph = graphModel.getGraphVisible();
        setConverged(false);
    }

    public void goAlgo() {
        graph = graphModel.getGraphVisible();
        double xMean = 0, yMean = 0;
        for (Node n : graph.getNodes()) {
            xMean += n.getNodeData().x();
            yMean += n.getNodeData().y();
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

    public LayoutProperty[] getProperties() {
        List<LayoutProperty> properties = new ArrayList<LayoutProperty>();
        try {
            properties.add(LayoutProperty.createProperty(
                    this, Double.class, "Scale factor", null,
                    "Scale factor", "getScale", "setScale"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return properties.toArray(new LayoutProperty[0]);
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
