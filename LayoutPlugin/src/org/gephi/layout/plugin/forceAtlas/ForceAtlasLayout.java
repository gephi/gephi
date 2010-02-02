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
package org.gephi.layout.plugin.forceAtlas;

import java.util.ArrayList;
import java.util.List;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.NodeData;

import org.gephi.layout.plugin.AbstractLayout;
import org.gephi.layout.plugin.ForceVectorUtils;
import org.gephi.layout.plugin.ForceVectorNodeLayoutData;
import org.gephi.layout.spi.Layout;
import org.gephi.layout.spi.LayoutBuilder;
import org.gephi.layout.spi.LayoutProperty;
import org.openide.util.NbBundle;

/**
 *
 * @author Mathieu Jacomy
 */
public class ForceAtlasLayout extends AbstractLayout implements Layout {

    //Graph
    protected Graph graph;
    //Properties
    public double inertia;
    private double repulsionStrength;
    private double attractionStrength;
    private double maxDisplacement;
    private boolean freezeBalance;
    private double freezeStrength;
    private double freezeInertia;
    private double gravity;
    private double speed;
    private double cooling;
    private boolean outboundAttractionDistribution;
    private boolean adjustSizes;

    public ForceAtlasLayout(LayoutBuilder layoutBuilder) {
        super(layoutBuilder);
    }

    public void resetPropertiesValues() {
        inertia = 0.1;
        setRepulsionStrength(5000d);
        setAttractionStrength(10d);
        setMaxDisplacement(10d);
        setFreezeBalance(true);
        setFreezeStrength(80d);
        setFreezeInertia(0.2);
        setGravity(30d);
        setOutboundAttractionDistribution(false);
        setAdjustSizes(false);
        setSpeed(1d);
        setCooling(1d);
    }

    public void initAlgo() {
        this.graph = graphModel.getGraphVisible();
        for (Node n : graph.getNodes()) {
            n.getNodeData().setLayoutData(new ForceVectorNodeLayoutData());
        }
    }

    public void goAlgo() {
        this.graph = graphModel.getGraphVisible();
        for (Node n : graph.getNodes()) {
            ForceVectorNodeLayoutData layoutData = n.getNodeData().getLayoutData();
            layoutData.old_dx = layoutData.dx;
            layoutData.old_dy = layoutData.dy;
            layoutData.dx *= inertia;
            layoutData.dy *= inertia;
        }
        // repulsion
        if (isAdjustSizes()) {
            for (Node n1 : graph.getNodes()) {
                for (Node n2 : graph.getNodes()) {
                    if (n1 != n2) {
                        ForceVectorUtils.fcBiRepulsor_noCollide(n1.getNodeData(), n2.getNodeData(), getRepulsionStrength() * (1 + graph.getDegree(n1)) * (1 + graph.getDegree(n2)));
                    }
                }
            }
        } else {
            for (Node n1 : graph.getNodes()) {
                for (Node n2 : graph.getNodes()) {
                    if (n1 != n2) {
                        ForceVectorUtils.fcBiRepulsor(n1.getNodeData(), n2.getNodeData(), getRepulsionStrength() * (1 + graph.getDegree(n1)) * (1 + graph.getDegree(n2)));
                    }
                }
            }
        }
        // attraction
        if (isAdjustSizes()) {
            if (isOutboundAttractionDistribution()) {
                for (Edge e : graph.getEdges()) {
                    Node nf = e.getSource();
                    Node nt = e.getTarget();
                    double bonus = (nf.getNodeData().isFixed() || nt.getNodeData().isFixed()) ? (100) : (1);
                    bonus *= e.getWeight();
                    ForceVectorUtils.fcBiAttractor_noCollide(nf.getNodeData(), nt.getNodeData(), bonus * getAttractionStrength() / (1 + graph.getDegree(nf)));
                }
            } else {
                for (Edge e : graph.getEdges()) {
                    Node nf = e.getSource();
                    Node nt = e.getTarget();
                    double bonus = (nf.getNodeData().isFixed() || nt.getNodeData().isFixed()) ? (100) : (1);
                    bonus *= e.getWeight();
                    ForceVectorUtils.fcBiAttractor_noCollide(nf.getNodeData(), nt.getNodeData(), bonus * getAttractionStrength());
                }
            }
        } else {
            if (isOutboundAttractionDistribution()) {
                for (Edge e : graph.getEdges()) {
                    Node nf = e.getSource();
                    Node nt = e.getTarget();
                    double bonus = (nf.getNodeData().isFixed() || nt.getNodeData().isFixed()) ? (100) : (1);
                    bonus *= e.getWeight();
                    ForceVectorUtils.fcBiAttractor(nf.getNodeData(), nt.getNodeData(), bonus * getAttractionStrength() / (1 + graph.getDegree(nf)));
                }
            } else {
                for (Edge e : graph.getEdges()) {
                    Node nf = e.getSource();
                    Node nt = e.getTarget();
                    double bonus = (nf.getNodeData().isFixed() || nt.getNodeData().isFixed()) ? (100) : (1);
                    bonus *= e.getWeight();
                    ForceVectorUtils.fcBiAttractor(nf.getNodeData(), nt.getNodeData(), bonus * getAttractionStrength());
                }
            }
        }
        // gravity
        for (Node n : graph.getNodes()) {

            float nx = n.getNodeData().x();
            float ny = n.getNodeData().y();
            double d = 0.0001 + Math.sqrt(nx * nx + ny * ny);
            double gf = 0.0001 * getGravity() * d;
            ForceVectorNodeLayoutData layoutData = n.getNodeData().getLayoutData();
            layoutData.dx -= gf * nx / d;
            layoutData.dy -= gf * ny / d;
        }
        // speed
        if (isFreezeBalance()) {
            for (Node n : graph.getNodes()) {
                ForceVectorNodeLayoutData layoutData = n.getNodeData().getLayoutData();
                layoutData.dx *= getSpeed() * 10f;
                layoutData.dy *= getSpeed() * 10f;
            }
        } else {
            for (Node n : graph.getNodes()) {
                ForceVectorNodeLayoutData layoutData = n.getNodeData().getLayoutData();
                layoutData.dx *= getSpeed();
                layoutData.dy *= getSpeed();
            }
        }
        // apply forces
        for (Node n : graph.getNodes()) {
            NodeData nData = n.getNodeData();
            ForceVectorNodeLayoutData nLayout = nData.getLayoutData();
            if (!nData.isFixed()) {
                double d = 0.0001 + Math.sqrt(nLayout.dx * nLayout.dx + nLayout.dy * nLayout.dy);
                float ratio;
                if (isFreezeBalance()) {
                    nLayout.freeze = (float) (getFreezeInertia() * nLayout.freeze + (1 - getFreezeInertia()) * 0.1 * getFreezeStrength() * (Math.sqrt(Math.sqrt((nLayout.old_dx - nLayout.dx) * (nLayout.old_dx - nLayout.dx) + (nLayout.old_dy - nLayout.dy) * (nLayout.old_dy - nLayout.dy)))));
                    ratio = (float) Math.min((d / (d * (1f + nLayout.freeze))), getMaxDisplacement() / d);
                } else {
                    ratio = (float) Math.min(1, getMaxDisplacement() / d);
                }
                nLayout.dx *= ratio / getCooling();
                nLayout.dy *= ratio / getCooling();
                float x = nData.x() + nLayout.dx;
                float y = nData.y() + nLayout.dy;

                nData.setX(x);
                nData.setY(y);
            }
        }
    }

    public void endAlgo() {
        for (Node n : graph.getNodes()) {
            n.getNodeData().setLayoutData(null);
        }
    }

    @Override
    public boolean canAlgo() {
        return true;
    }

    public LayoutProperty[] getProperties() {
        List<LayoutProperty> properties = new ArrayList<LayoutProperty>();
        final String FORCE_ATLAS = "Force Atlas";

        try {
            properties.add(LayoutProperty.createProperty(
                    this, Double.class,
                    NbBundle.getMessage(ForceAtlasLayout.class, "forceAtlas.inertia.name"),
                    FORCE_ATLAS,
                    NbBundle.getMessage(ForceAtlasLayout.class, "forceAtlas.inertia.desc"),
                    "getInertia", "setInertia"));
            properties.add(LayoutProperty.createProperty(
                    this, Double.class,
                    NbBundle.getMessage(ForceAtlasLayout.class, "forceAtlas.repulsionStrength.name"),
                    FORCE_ATLAS,
                    NbBundle.getMessage(ForceAtlasLayout.class, "forceAtlas.repulsionStrength.desc"),
                    "getRepulsionStrength", "setRepulsionStrength"));
            properties.add(LayoutProperty.createProperty(
                    this, Double.class,
                    NbBundle.getMessage(ForceAtlasLayout.class, "forceAtlas.attractionStrength.name"),
                    FORCE_ATLAS,
                    NbBundle.getMessage(ForceAtlasLayout.class, "forceAtlas.attractionStrength.desc"),
                    "getAttractionStrength", "setAttractionStrength"));
            properties.add(LayoutProperty.createProperty(
                    this, Double.class,
                    NbBundle.getMessage(ForceAtlasLayout.class, "forceAtlas.maxDisplacement.name"),
                    FORCE_ATLAS,
                    NbBundle.getMessage(ForceAtlasLayout.class, "forceAtlas.maxDisplacement.desc"),
                    "getMaxDisplacement", "setMaxDisplacement"));
            properties.add(LayoutProperty.createProperty(
                    this, Boolean.class,
                    NbBundle.getMessage(ForceAtlasLayout.class, "forceAtlas.freezeBalance.name"),
                    FORCE_ATLAS,
                    NbBundle.getMessage(ForceAtlasLayout.class, "forceAtlas.freezeBalance.desc"),
                    "isFreezeBalance", "setFreezeBalance"));
            properties.add(LayoutProperty.createProperty(
                    this, Double.class,
                    NbBundle.getMessage(ForceAtlasLayout.class, "forceAtlas.freezeStrength.name"),
                    FORCE_ATLAS,
                    NbBundle.getMessage(ForceAtlasLayout.class, "forceAtlas.freezeStrength.desc"),
                    "getFreezeStrength", "setFreezeStrength"));
            properties.add(LayoutProperty.createProperty(
                    this, Double.class,
                    NbBundle.getMessage(ForceAtlasLayout.class, "forceAtlas.freezeInertia.name"),
                    FORCE_ATLAS,
                    NbBundle.getMessage(ForceAtlasLayout.class, "forceAtlas.freezeInertia.desc"),
                    "getFreezeInertia", "setFreezeInertia"));
            properties.add(LayoutProperty.createProperty(
                    this, Double.class,
                    NbBundle.getMessage(ForceAtlasLayout.class, "forceAtlas.gravity.name"),
                    FORCE_ATLAS,
                    NbBundle.getMessage(ForceAtlasLayout.class, "forceAtlas.gravity.desc"),
                    "getGravity", "setGravity"));
            properties.add(LayoutProperty.createProperty(
                    this, Boolean.class,
                    NbBundle.getMessage(ForceAtlasLayout.class, "forceAtlas.outboundAttractionDistribution.name"),
                    FORCE_ATLAS,
                    NbBundle.getMessage(ForceAtlasLayout.class, "forceAtlas.outboundAttractionDistribution.desc"),
                    "isOutboundAttractionDistribution", "setOutboundAttractionDistribution"));
            properties.add(LayoutProperty.createProperty(
                    this, Boolean.class,
                    NbBundle.getMessage(ForceAtlasLayout.class, "forceAtlas.adjustSizes.name"),
                    FORCE_ATLAS,
                    NbBundle.getMessage(ForceAtlasLayout.class, "forceAtlas.adjustSizes.desc"),
                    "isAdjustSizes", "setAdjustSizes"));
            properties.add(LayoutProperty.createProperty(
                    this, Double.class,
                    NbBundle.getMessage(ForceAtlasLayout.class, "forceAtlas.speed.name"),
                    FORCE_ATLAS,
                    NbBundle.getMessage(ForceAtlasLayout.class, "forceAtlas.speed.desc"),
                    "getSpeed", "setSpeed"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return properties.toArray(new LayoutProperty[0]);
    }

    public void setInertia(Double inertia) {
        this.inertia = inertia;
    }

    public Double getInertia() {
        return inertia;
    }

    /**
     * @return the repulsionStrength
     */
    public Double getRepulsionStrength() {
        return repulsionStrength;
    }

    /**
     * @param repulsionStrength the repulsionStrength to set
     */
    public void setRepulsionStrength(Double repulsionStrength) {
        this.repulsionStrength = repulsionStrength;
    }

    /**
     * @return the attractionStrength
     */
    public Double getAttractionStrength() {
        return attractionStrength;
    }

    /**
     * @param attractionStrength the attractionStrength to set
     */
    public void setAttractionStrength(Double attractionStrength) {
        this.attractionStrength = attractionStrength;
    }

    /**
     * @return the maxDisplacement
     */
    public Double getMaxDisplacement() {
        return maxDisplacement;
    }

    /**
     * @param maxDisplacement the maxDisplacement to set
     */
    public void setMaxDisplacement(Double maxDisplacement) {
        this.maxDisplacement = maxDisplacement;
    }

    /**
     * @return the freezeBalance
     */
    public Boolean isFreezeBalance() {
        return freezeBalance;
    }

    /**
     * @param freezeBalance the freezeBalance to set
     */
    public void setFreezeBalance(Boolean freezeBalance) {
        this.freezeBalance = freezeBalance;
    }

    /**
     * @return the freezeStrength
     */
    public Double getFreezeStrength() {
        return freezeStrength;
    }

    /**
     * @param freezeStrength the freezeStrength to set
     */
    public void setFreezeStrength(Double freezeStrength) {
        this.freezeStrength = freezeStrength;
    }

    /**
     * @return the freezeInertia
     */
    public Double getFreezeInertia() {
        return freezeInertia;
    }

    /**
     * @param freezeInertia the freezeInertia to set
     */
    public void setFreezeInertia(Double freezeInertia) {
        this.freezeInertia = freezeInertia;
    }

    /**
     * @return the gravity
     */
    public Double getGravity() {
        return gravity;
    }

    /**
     * @param gravity the gravity to set
     */
    public void setGravity(Double gravity) {
        this.gravity = gravity;
    }

    /**
     * @return the speed
     */
    public Double getSpeed() {
        return speed;
    }

    /**
     * @param speed the speed to set
     */
    public void setSpeed(Double speed) {
        this.speed = speed;
    }

    /**
     * @return the cooling
     */
    public Double getCooling() {
        return cooling;
    }

    /**
     * @param cooling the cooling to set
     */
    public void setCooling(Double cooling) {
        this.cooling = cooling;
    }

    /**
     * @return the outboundAttractionDistribution
     */
    public Boolean isOutboundAttractionDistribution() {
        return outboundAttractionDistribution;
    }

    /**
     * @param outboundAttractionDistribution the outboundAttractionDistribution to set
     */
    public void setOutboundAttractionDistribution(Boolean outboundAttractionDistribution) {
        this.outboundAttractionDistribution = outboundAttractionDistribution;
    }

    /**
     * @return the adjustSizes
     */
    public Boolean isAdjustSizes() {
        return adjustSizes;
    }

    /**
     * @param adjustSizes the adjustSizes to set
     */
    public void setAdjustSizes(Boolean adjustSizes) {
        this.adjustSizes = adjustSizes;
    }
}
