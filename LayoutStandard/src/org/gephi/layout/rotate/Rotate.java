/*
 *  Copyright 2009 Helder Suzuki <heldersuzuki@gmail.com>.
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
public class Rotate implements Layout {

    private boolean converged;
    public double angle;
    private Graph graph;

    public String getName() {
        return NbBundle.getMessage(Rotate.class, "name");
    }

    public String getDescription() {
        return NbBundle.getMessage(Rotate.class, "description");
    }

    public Icon getIcon() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean testAlgo() {
        return true;
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
        layoutProperties[0] = LayoutProperty.createProperty(Rotate.class, "angle");
        return layoutProperties;
    }

    public void resetPropertiesValues() {
        angle = Math.PI / 2;
    }

    public JPanel getPanel() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
