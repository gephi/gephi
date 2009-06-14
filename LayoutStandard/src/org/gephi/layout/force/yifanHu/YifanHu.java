/*
 *  Copyright 2009 Helder Suzuki <heldersuzuki@gmail.com>.
 */
package org.gephi.layout.force.yifanHu;

import javax.swing.Icon;
import javax.swing.JPanel;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Spatial;
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

    /* Fa = (n2 - n1) * ||n2 - n1|| / K
     */
    class AttractionForce extends AbstractForce {

        @Override
        public ForceVector calculateForce(Spatial node1, Spatial node2,
                                          float distance) {
            ForceVector f = new ForceVector(node2.x() - node1.x(),
                                            node2.y() - node1.y());
            f.multiply(distance);
            return f;
        }
    }

    /* Fr = -C*K*K*(n2-n1)/||n2-n1||
     */
    class RepulsionForce extends AbstractForce {

        private float forceConstant;

        public RepulsionForce() {
            forceConstant = relativeStrength * optimalDistance * optimalDistance;
        }

        @Override
        public ForceVector calculateForce(Spatial node1, Spatial node2,
                                          float distance) {
            ForceVector f = new ForceVector(node2.x() - node1.x(),
                                            node2.y() - node1.y());
            f.multiply(forceConstant / distance);
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

        if (Math.abs(energy - energy0) < optimalDistance * 1e-6) {
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
        optimalDistance = 10;
        step = optimalDistance / 10;
        relativeStrength = (float) 0.1;
    }

    public JPanel getPanel() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
