/*
Copyright 2008 WebAtlas
Authors : Mathieu Bastian, Mathieu Jacomy, Julian Bilcke
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
package org.gephi.layout.forceAtlas;

import javax.swing.Icon;
import javax.swing.JPanel;
import org.gephi.data.network.api.LayoutReader;
import org.gephi.layout.AbstractForceVector;
import org.gephi.layout.EdgeLayout;
import org.gephi.layout.ForceVectorUtils;
import org.gephi.layout.NodeLayout;
import org.gephi.layout.api.LayoutProperty;
import org.openide.util.NbBundle;

/**
 *
 * @author Mathieu Jacomy
 */
public class ForceAtlas extends AbstractForceVector {

    public double inertia;
    public double repulsionStrength;
    public double attractionStrength;
    public double maxDisplacement;
    public boolean freezeBalance;
    public double freezeStrength;
    public double freezeInertia;
    public double gravity;
    public double speed;
    public double cooling;
    public boolean outboundAttractionDistribution;
    public boolean adjustSizes;

    public String getName() {
        return NbBundle.getMessage(ForceAtlas.class, "name");
    }

    public String getDescription() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Icon getIcon() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void resetPropertiesValues() {
        inertia = 0.1;
        repulsionStrength = 10;
        attractionStrength = 10;
        maxDisplacement = 10;
        freezeBalance = true;
        freezeStrength = 30;
        freezeInertia = 0.2;
        gravity = 30;
        outboundAttractionDistribution = false;
        adjustSizes = false;
    }

    public boolean testAlgo() {
        return true;
    }

    public void initAlgo() {
    }
    

    public void goAlgo(LayoutReader<NodeLayout, EdgeLayout> reader) {
        for (NodeLayout n : reader.getNodes()) {
            n.old_dx = n.dx;
            n.old_dy = n.dy;
            n.dx *= inertia;
            n.dy *= inertia;
        }
        // repulsion
        if (adjustSizes) {
            for (NodeLayout n1 : reader.getNodes()) {
                for (NodeLayout n2 : reader.getNodes()) {
                    if (n1 != n2) {
                        ForceVectorUtils.fcBiRepulsor_noCollide(n1, n2, repulsionStrength * (1 + n1.getNeighboursCount()) * (1 + n2.getNeighboursCount()));
                    }
                }
            }
        } else {
            for (NodeLayout n1 : reader.getNodes()) {
                for (NodeLayout n2 : reader.getNodes()) {
                    if (n1 != n2) {
                        ForceVectorUtils.fcBiRepulsor(n1, n2, repulsionStrength * (1 + n1.getNeighboursCount()) * (1 + n2.getNeighboursCount()));
                    }
                }
            }
        }
        // attraction
        if (adjustSizes) {
            if (outboundAttractionDistribution) {
                for (EdgeLayout e : reader.getEdges()) {
                    NodeLayout nf = e.getSource();
                    NodeLayout nt = e.getTarget();
                    double bonus = (nf.fixed || nt.fixed) ? (100) : (1);
                    ForceVectorUtils.fcBiAttractor_noCollide(nf, nt, bonus * attractionStrength / (1 + nf.getNeighboursCount()));
                }
            } else {
                for (EdgeLayout e : reader.getEdges()) {
                    NodeLayout nf = e.getSource();
                    NodeLayout nt = e.getTarget();
                    double bonus = (nf.fixed || nt.fixed) ? (100) : (1);
                    ForceVectorUtils.fcBiAttractor_noCollide(nf, nt, bonus * attractionStrength);
                }
            }
        } else {
            if (outboundAttractionDistribution) {
                for (EdgeLayout e : reader.getEdges()) {
                    NodeLayout nf = e.getSource();
                    NodeLayout nt = e.getTarget();
                    double bonus = (nf.fixed || nt.fixed) ? (100) : (1);
                    ForceVectorUtils.fcBiAttractor(nf, nt, bonus * attractionStrength / (1 + nf.getNeighboursCount()));
                }
            } else {
                for (EdgeLayout e : reader.getEdges()) {
                    NodeLayout nf = e.getSource();
                    NodeLayout nt = e.getTarget();
                    double bonus = (nf.fixed || nt.fixed) ? (100) : (1);
                    ForceVectorUtils.fcBiAttractor(nf, nt, bonus * attractionStrength);
                }
            }
        }
        // gravity
        for (NodeLayout n : reader.getNodes()) {

            float nx = n.x();
            float ny = n.y();
            double d = 0.0001 + Math.sqrt(nx * nx + ny * ny);
            double gf = 0.0001 * gravity * d;
            n.dx -= gf * nx / d;
            n.dy -= gf * ny / d;
        }
        // speed
        if (freezeBalance) {
            for (NodeLayout n : reader.getNodes()) {
                n.dx *= speed * 10f;
                n.dy *= speed * 10f;
            }
        } else {
            for (NodeLayout n : reader.getNodes()) {
                n.dx *= speed;
                n.dy *= speed;
            }
        }
        // apply forces
        for (NodeLayout n : reader.getNodes()) {
            if (!n.fixed) {
                double d = 0.0001 + Math.sqrt(n.dx * n.dx + n.dy * n.dy);
                float ratio;
                if (freezeBalance) {
                    n.freeze = (float) (freezeInertia * n.freeze + (1 - freezeInertia) * 0.1 * freezeStrength * (Math.sqrt(Math.sqrt((n.old_dx - n.dx) * (n.old_dx - n.dx) + (n.old_dy - n.dy) * (n.old_dy - n.dy)))));
                    ratio = (float) Math.min((d / (d * (1f + n.freeze))), maxDisplacement / d);
                } else {
                    ratio = (float) Math.min(1, maxDisplacement / d);
                }
                n.dx *= ratio / cooling;
                n.dy *= ratio / cooling;
                float x = n.x() + n.dx;
                float y = n.y() + n.dy;

                n.setX(x);
                n.setY(y);
            }
        }
    }

    public void endAlgo() {
    }

    public LayoutProperty[] getProperties() {
        LayoutProperty[] layoutProperties = new LayoutProperty[11];
        layoutProperties[0] = LayoutProperty.createProperty(ForceAtlas.class, "inertia");
        layoutProperties[1] = LayoutProperty.createProperty(ForceAtlas.class, "repulsionStrength");
        layoutProperties[2] = LayoutProperty.createProperty(ForceAtlas.class, "attractionStrength");
        layoutProperties[3] = LayoutProperty.createProperty(ForceAtlas.class, "maxDisplacement");
        layoutProperties[4] = LayoutProperty.createProperty(ForceAtlas.class, "freezeBalance");
        layoutProperties[5] = LayoutProperty.createProperty(ForceAtlas.class, "freezeStrength");
        layoutProperties[6] = LayoutProperty.createProperty(ForceAtlas.class, "freezeInertia");
        layoutProperties[7] = LayoutProperty.createProperty(ForceAtlas.class, "gravity");
        layoutProperties[8] = LayoutProperty.createProperty(ForceAtlas.class, "outboundAttractionDistribution");
        layoutProperties[9] = LayoutProperty.createProperty(ForceAtlas.class, "adjustSizes");
        layoutProperties[10] = LayoutProperty.createProperty(ForceAtlas.class, "speed");
        return layoutProperties;
    }

    public JPanel getPanel() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
