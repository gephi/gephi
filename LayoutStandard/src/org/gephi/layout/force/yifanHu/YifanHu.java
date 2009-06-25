/*
 *  Copyright 2009 Helder Suzuki <heldersuzuki@gmail.com>.
 */
package org.gephi.layout.force.yifanHu;

import javax.swing.Icon;
import javax.swing.JPanel;
import org.gephi.graph.api.ClusteredGraph;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Spatial;
import org.gephi.layout.ForceVectorUtils;
import org.gephi.layout.api.LayoutProperty;
import org.gephi.layout.force.Displacement;
import org.gephi.layout.force.AbstractForce;
import org.gephi.layout.force.AbstractForceLayout;
import org.gephi.layout.force.ForceVector;
import org.gephi.layout.force.StepDisplacement;
import org.openide.util.NbBundle;

/**
 * Hu's basic algorithm
 * @author Helder Suzuki <heldersuzuki@gmail.com>
 */
public class YifanHu extends AbstractForceLayout {

    public float optimalDistance;
    public float relativeStrength;
    private float step;
    private int progress;
    public float stepRatio;
    private boolean converged;

    private float getAverageEdge(ClusteredGraph graph) {
        float edgeLength = 0;
        int count = 1;
        for (Edge e : graph.getEdges()) {
            edgeLength += ForceVectorUtils.distance(
                e.getSource().getNodeData(), e.getTarget().getNodeData());
            count++;
        }

        return edgeLength / count;
    }

    /* Fa = (n2 - n1) * ||n2 - n1|| / K
     */
    class AttractionForce extends AbstractForce {

        @Override
        public ForceVector calculateForce(Spatial node1, Spatial node2,
                                          float distance) {
            ForceVector f = new ForceVector(node2.x() - node1.x(),
                                            node2.y() - node1.y());
            f.multiply(distance / optimalDistance);
            return f;
        }
    }

    /* Fr = -C*K*K*(n2-n1)/||n2-n1||
     */
    class RepulsionForce extends AbstractForce {

        @Override
        public ForceVector calculateForce(Spatial node1, Spatial node2,
                                          float distance) {
            ForceVector f = new ForceVector(node2.x() - node1.x(),
                                            node2.y() - node1.y());
            f.multiply(-relativeStrength * optimalDistance * optimalDistance / (distance * distance));
            if (Float.isNaN(f.x()) || Float.isNaN(f.y())) {
                f = new ForceVector(100, 100);
            }
            return f;
        }
    }

    @Override
    protected void postAlgo() {
        updateStep();
    }

    private void updateStep() {
        if (energy < energy0) {
            progress++;
            if (progress >= 5) {
                progress = 0;
                step /= stepRatio;
            }
        } else {
            progress = 0;
            step *= stepRatio;
        }

        if (Math.abs((energy - energy0) / energy) < 1e-3) {
            converged = true;
        }
    }

    @Override
    protected AbstractForce getNodeForce() {
        return new RepulsionForce();
    }

    @Override
    protected AbstractForce getEdgeForce() {
        return new AttractionForce();
    }

    @Override
    protected boolean hasConverged() {
        return converged;
    }

    @Override
    protected Displacement getDisplacement() {
        return new StepDisplacement(step);
    }

    public String getName() {
        return NbBundle.getMessage(YifanHu.class, "name");
    }

    public String getDescription() {
        return NbBundle.getMessage(YifanHu.class, "description");
    }

    public Icon getIcon() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean testAlgo() {
        return graph.isClustered();
    }

    public void endAlgo() {
    }

    public LayoutProperty[] getProperties() {
        LayoutProperty[] properties = new LayoutProperty[3];
        properties[0] = LayoutProperty.createProperty(YifanHu.class, "optimalDistance");
        properties[1] = LayoutProperty.createProperty(YifanHu.class, "relativeStrength");
        properties[2] = LayoutProperty.createProperty(YifanHu.class, "stepRatio");
        return properties;
    }

    public void resetPropertiesValues() {
        stepRatio = (float) 0.9;
        progress = 0;
        converged = false;
        optimalDistance = getAverageEdge(graph);
        System.out.println("distance = " + optimalDistance);
        step = (float) optimalDistance / 10000;
        relativeStrength = (float) 0.2;
    }

    public JPanel getPanel() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected int getBarnesHutMaxLevel() {
        return 10;
    }
}
