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
package org.gephi.layout.rotate;

import javax.swing.Icon;
import javax.swing.JPanel;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.Node;
import org.gephi.layout.api.Layout;
import org.gephi.layout.api.LayoutProperty;
import org.openide.util.NbBundle;

/**
 * Sample layout that simply rotates the graph.
 * @author Helder Suzuki <heldersuzuki@gmail.com>
 */
public class RotateLayout implements Layout {

    private boolean converged;
    public double angle;
    private Graph graph;

    public RotateLayout(double angle) {
        this.angle = angle;
    }

    public String getName() {
        return NbBundle.getMessage(RotateLayout.class, "name");
    }

    public String getDescription() {
        return NbBundle.getMessage(RotateLayout.class, "description");
    }

    public Icon getIcon() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void initAlgo(GraphController graphController) {
        this.graph = graphController.getUndirectedGraph();
        converged = false;
    }

    public void goAlgo() {
        double sin = Math.sin(angle);
        double cos = Math.cos(angle);
        double px = 0f;
        double py = 0f;

        for (Node n : graph.getNodes()) {
            double dx = n.getNodeData().x() - px;
            double dy = n.getNodeData().y() - py;

            n.getNodeData().setX((float) (px + dx * cos - dy * sin));
            n.getNodeData().setY((float) (py + dy * cos + dx * sin));
        }
        converged = true;
    }

    public boolean canAlgo() {
        return !converged;
    }

    public void endAlgo() {
    }

    public LayoutProperty[] getProperties() {
        LayoutProperty[] layoutProperties = new LayoutProperty[1];
        layoutProperties[0] = LayoutProperty.createProperty(RotateLayout.class, "angle");
        return layoutProperties;
    }

    public JPanel getPanel() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void resetPropertiesValues() {
    }
}
