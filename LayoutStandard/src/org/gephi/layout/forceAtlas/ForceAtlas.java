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
import org.gephi.graph.api.DirectedGraph;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.NodeData;
import org.gephi.layout.AbstractForceVector;

import org.gephi.layout.ForceVectorUtils;
import org.gephi.layout.ForceVectorNodeLayoutData;
import org.gephi.layout.api.LayoutProperty;
import org.openide.util.NbBundle;

/**
 *
 * @author Mathieu Jacomy
 */
public class ForceAtlas extends AbstractForceVector {

    //Graph
    protected DirectedGraph graph;

    //Properties
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
        repulsionStrength = 5000;
        attractionStrength = 10;
        maxDisplacement = 10;
        freezeBalance = true;
        freezeStrength = 80;
        freezeInertia = 0.2;
        gravity = 30;
        outboundAttractionDistribution = false;
        adjustSizes = false;
        speed = 1f;
        cooling=1f;
    }

    public boolean testAlgo() {
        return true;
    }

    public void initAlgo(DirectedGraph graph) {
        this.graph = graph;
        for (Node n : graph.getNodes()) {
            n.getNodeData().setLayoutData(new ForceVectorNodeLayoutData());
        }
    }
    

    public void goAlgo() {
        for (Node n : graph.getNodes()) {
            ForceVectorNodeLayoutData layoutData = n.getNodeData().getLayoutData();
            layoutData.old_dx = layoutData.dx;
            layoutData.old_dy = layoutData.dy;
            layoutData.dx *= inertia;
            layoutData.dy *= inertia;
        }
        // repulsion
        if (adjustSizes) {
            for (Node n1 : graph.getNodes()) {
                for (Node n2 : graph.getNodes()) {
                    if (n1 != n2) {
                        ForceVectorUtils.fcBiRepulsor_noCollide(n1.getNodeData(), n2.getNodeData(), repulsionStrength * (1 + graph.getDegree(n1)) * (1 + graph.getDegree(n2)));
                    }
                }
            }
        } else {
            for (Node n1 : graph.getNodes()) {
                for (Node n2 : graph.getNodes()) {
                    if (n1 != n2) {
                        ForceVectorUtils.fcBiRepulsor(n1.getNodeData(), n2.getNodeData(), repulsionStrength * (1 + graph.getDegree(n1)) * (1 + graph.getDegree(n2)));
                    }
                }
            }
        }
        // attraction
        if (adjustSizes) {
            if (outboundAttractionDistribution) {
                for (Edge e : graph.getEdges()) {
                    Node nf = e.getSource();
                    Node nt = e.getTarget();
                    double bonus = (nf.getNodeData().isFixed() || nt.getNodeData().isFixed()) ? (100) : (1);
                    ForceVectorUtils.fcBiAttractor_noCollide(nf.getNodeData(), nt.getNodeData(), bonus * attractionStrength / (1 + graph.getDegree(nf)));
                }
            } else {
                for (Edge e : graph.getEdges()) {
                    Node nf = e.getSource();
                    Node nt = e.getTarget();
                    double bonus = (nf.getNodeData().isFixed() || nt.getNodeData().isFixed()) ? (100) : (1);
                    ForceVectorUtils.fcBiAttractor_noCollide(nf.getNodeData(), nt.getNodeData(), bonus * attractionStrength);
                }
            }
        } else {
            if (outboundAttractionDistribution) {
                for (Edge e : graph.getEdges()) {
                    Node nf = e.getSource();
                    Node nt = e.getTarget();
                    double bonus = (nf.getNodeData().isFixed() || nt.getNodeData().isFixed()) ? (100) : (1);
                    ForceVectorUtils.fcBiAttractor(nf.getNodeData(), nt.getNodeData(), bonus * attractionStrength / (1 + graph.getDegree(nf)));
                }
            } else {
                for (Edge e : graph.getEdges()) {
                    Node nf = e.getSource();
                    Node nt = e.getTarget();
                    double bonus = (nf.getNodeData().isFixed() || nt.getNodeData().isFixed()) ? (100) : (1);
                    ForceVectorUtils.fcBiAttractor(nf.getNodeData(), nt.getNodeData(), bonus * attractionStrength);
                }
            }
        }
        // gravity
        for (Node n : graph.getNodes()) {

            float nx = n.getNodeData().x();
            float ny = n.getNodeData().y();
            double d = 0.0001 + Math.sqrt(nx * nx + ny * ny);
            double gf = 0.0001 * gravity * d;
            ForceVectorNodeLayoutData layoutData = n.getNodeData().getLayoutData();
            layoutData.dx -= gf * nx / d;
            layoutData.dy -= gf * ny / d;
        }
        // speed
        if (freezeBalance) {
            for (Node n : graph.getNodes()) {
                ForceVectorNodeLayoutData layoutData = n.getNodeData().getLayoutData();
                layoutData.dx *= speed * 10f;
                layoutData.dy *= speed * 10f;
            }
        } else {
            for (Node n : graph.getNodes()) {
                ForceVectorNodeLayoutData layoutData = n.getNodeData().getLayoutData();
                layoutData.dx *= speed;
                layoutData.dy *= speed;
            }
        }
        // apply forces
        for (Node n : graph.getNodes()) {
            NodeData nData = n.getNodeData();
            ForceVectorNodeLayoutData nLayout = nData.getLayoutData();
            if (!nData.isFixed()) {
                double d = 0.0001 + Math.sqrt(nLayout.dx * nLayout.dx + nLayout.dy * nLayout.dy);
                float ratio;
                if (freezeBalance) {
                    nLayout.freeze = (float) (freezeInertia * nLayout.freeze + (1 - freezeInertia) * 0.1 * freezeStrength * (Math.sqrt(Math.sqrt((nLayout.old_dx - nLayout.dx) * (nLayout.old_dx - nLayout.dx) + (nLayout.old_dy - nLayout.dy) * (nLayout.old_dy - nLayout.dy)))));
                    ratio = (float) Math.min((d / (d * (1f + nLayout.freeze))), maxDisplacement / d);
                } else {
                    ratio = (float) Math.min(1, maxDisplacement / d);
                }
                nLayout.dx *= ratio / cooling;
                nLayout.dy *= ratio / cooling;
                float x = nData.x() + nLayout.dx;
                float y = nData.y() + nLayout.dy;

                nData.setX(x);
                nData.setY(y);
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
