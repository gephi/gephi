/*
Copyright 2008-2010 Gephi
Authors : Mathieu Jacomy
Website : http://www.gephi.org

This file is part of Gephi.

DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

Copyright 2011 Gephi Consortium. All rights reserved.

The contents of this file are subject to the terms of either the GNU
General Public License Version 3 only ("GPL") or the Common
Development and Distribution License("CDDL") (collectively, the
"License"). You may not use this file except in compliance with the
License. You can obtain a copy of the License at
http://gephi.org/about/legal/license-notice/
or /cddl-1.0.txt and /gpl-3.0.txt. See the License for the
specific language governing permissions and limitations under the
License.  When distributing the software, include this License Header
Notice in each file and include the License files at
/cddl-1.0.txt and /gpl-3.0.txt. If applicable, add the following below the
License Header, with the fields enclosed by brackets [] replaced by
your own identifying information:
"Portions Copyrighted [year] [name of copyright owner]"

If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 3, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 3] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 3 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 3 code and therefore, elected the GPL
Version 3 license, then the option applies only if the new code is
made subject to such option by the copyright holder.

Contributor(s):

Portions Copyrighted 2011 Gephi Consortium.
*/
package org.gephi.layout.plugin.forceAtlas;

import java.util.ArrayList;
import java.util.List;
import org.gephi.data.attributes.type.TimeInterval;
import org.gephi.dynamic.DynamicUtilities;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.HierarchicalGraph;
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
    protected HierarchicalGraph graph;
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
    //Dynamic Weight
    private TimeInterval timeInterval;

    public ForceAtlasLayout(LayoutBuilder layoutBuilder) {
        super(layoutBuilder);
    }

    public void resetPropertiesValues() {
        inertia = 0.1;
        setRepulsionStrength(200d);
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
        this.graph = graphModel.getHierarchicalGraphVisible();
    }

    public void goAlgo() {
        this.graph = graphModel.getHierarchicalGraphVisible();
        this.timeInterval = DynamicUtilities.getVisibleInterval(dynamicModel);
        graph.readLock();
        Node[] nodes = graph.getNodes().toArray();
        Edge[] edges = graph.getEdgesAndMetaEdges().toArray();

        for (Node n : nodes) {
            if (n.getNodeData().getLayoutData() == null || !(n.getNodeData().getLayoutData() instanceof ForceVectorNodeLayoutData)) {
                n.getNodeData().setLayoutData(new ForceVectorNodeLayoutData());
            }
        }

        for (Node n : nodes) {
            ForceVectorNodeLayoutData layoutData = n.getNodeData().getLayoutData();
            layoutData.old_dx = layoutData.dx;
            layoutData.old_dy = layoutData.dy;
            layoutData.dx *= inertia;
            layoutData.dy *= inertia;
        }
        // repulsion
        if (isAdjustSizes()) {
            for (Node n1 : nodes) {
                for (Node n2 : nodes) {
                    if (n1 != n2) {
                        ForceVectorUtils.fcBiRepulsor_noCollide(n1.getNodeData(), n2.getNodeData(), getRepulsionStrength() * (1 + graph.getDegree(n1)) * (1 + graph.getDegree(n2)));
                    }
                }
            }
        } else {
            for (Node n1 : nodes) {
                for (Node n2 : nodes) {
                    if (n1 != n2) {
                        ForceVectorUtils.fcBiRepulsor(n1.getNodeData(), n2.getNodeData(), getRepulsionStrength() * (1 + graph.getDegree(n1)) * (1 + graph.getDegree(n2)));
                    }
                }
            }
        }
        // attraction
        if (isAdjustSizes()) {
            if (isOutboundAttractionDistribution()) {
                for (Edge e : edges) {
                    Node nf = e.getSource();
                    Node nt = e.getTarget();
                    double bonus = (nf.getNodeData().isFixed() || nt.getNodeData().isFixed()) ? (100) : (1);
                    bonus *= getWeight(e);
                    ForceVectorUtils.fcBiAttractor_noCollide(nf.getNodeData(), nt.getNodeData(), bonus * getAttractionStrength() / (1 + graph.getDegree(nf)));
                }
            } else {
                for (Edge e : edges) {
                    Node nf = e.getSource();
                    Node nt = e.getTarget();
                    double bonus = (nf.getNodeData().isFixed() || nt.getNodeData().isFixed()) ? (100) : (1);
                    bonus *= getWeight(e);
                    ForceVectorUtils.fcBiAttractor_noCollide(nf.getNodeData(), nt.getNodeData(), bonus * getAttractionStrength());
                }
            }
        } else {
            if (isOutboundAttractionDistribution()) {
                for (Edge e : edges) {
                    Node nf = e.getSource();
                    Node nt = e.getTarget();
                    double bonus = (nf.getNodeData().isFixed() || nt.getNodeData().isFixed()) ? (100) : (1);
                    bonus *= getWeight(e);
                    ForceVectorUtils.fcBiAttractor(nf.getNodeData(), nt.getNodeData(), bonus * getAttractionStrength() / (1 + graph.getDegree(nf)));
                }
            } else {
                for (Edge e : edges) {
                    Node nf = e.getSource();
                    Node nt = e.getTarget();
                    double bonus = (nf.getNodeData().isFixed() || nt.getNodeData().isFixed()) ? (100) : (1);
                    bonus *= getWeight(e);
                    ForceVectorUtils.fcBiAttractor(nf.getNodeData(), nt.getNodeData(), bonus * getAttractionStrength());
                }
            }
        }
        // gravity
        for (Node n : nodes) {

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
            for (Node n : nodes) {
                ForceVectorNodeLayoutData layoutData = n.getNodeData().getLayoutData();
                layoutData.dx *= getSpeed() * 10f;
                layoutData.dy *= getSpeed() * 10f;
            }
        } else {
            for (Node n : nodes) {
                ForceVectorNodeLayoutData layoutData = n.getNodeData().getLayoutData();
                layoutData.dx *= getSpeed();
                layoutData.dy *= getSpeed();
            }
        }
        // apply forces
        for (Node n : nodes) {
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
        graph.readUnlock();
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

    private float getWeight(Edge edge) {
        if(timeInterval!=null) {
            return edge.getWeight(timeInterval.getLow(), timeInterval.getHigh());
        } else {
            return edge.getWeight();
        }
    }

    public LayoutProperty[] getProperties() {
        List<LayoutProperty> properties = new ArrayList<LayoutProperty>();
        final String FORCE_ATLAS = "Force Atlas";

        try {
            properties.add(LayoutProperty.createProperty(
                    this, Double.class,
                    NbBundle.getMessage(ForceAtlasLayout.class, "forceAtlas.inertia.name"),
                    FORCE_ATLAS,
                    "forceAtlas.inertia.name",
                    NbBundle.getMessage(ForceAtlasLayout.class, "forceAtlas.inertia.desc"),
                    "getInertia", "setInertia"));
            properties.add(LayoutProperty.createProperty(
                    this, Double.class,
                    NbBundle.getMessage(ForceAtlasLayout.class, "forceAtlas.repulsionStrength.name"),
                    FORCE_ATLAS,
                    "forceAtlas.repulsionStrength.name",
                    NbBundle.getMessage(ForceAtlasLayout.class, "forceAtlas.repulsionStrength.desc"),
                    "getRepulsionStrength", "setRepulsionStrength"));
            properties.add(LayoutProperty.createProperty(
                    this, Double.class,
                    NbBundle.getMessage(ForceAtlasLayout.class, "forceAtlas.attractionStrength.name"),
                    FORCE_ATLAS,
                    "forceAtlas.attractionStrength.name",
                    NbBundle.getMessage(ForceAtlasLayout.class, "forceAtlas.attractionStrength.desc"),
                    "getAttractionStrength", "setAttractionStrength"));
            properties.add(LayoutProperty.createProperty(
                    this, Double.class,
                    NbBundle.getMessage(ForceAtlasLayout.class, "forceAtlas.maxDisplacement.name"),
                    FORCE_ATLAS,
                    "forceAtlas.maxDisplacement.name",
                    NbBundle.getMessage(ForceAtlasLayout.class, "forceAtlas.maxDisplacement.desc"),
                    "getMaxDisplacement", "setMaxDisplacement"));
            properties.add(LayoutProperty.createProperty(
                    this, Boolean.class,
                    NbBundle.getMessage(ForceAtlasLayout.class, "forceAtlas.freezeBalance.name"),
                    FORCE_ATLAS,
                    "forceAtlas.freezeBalance.name",
                    NbBundle.getMessage(ForceAtlasLayout.class, "forceAtlas.freezeBalance.desc"),
                    "isFreezeBalance", "setFreezeBalance"));
            properties.add(LayoutProperty.createProperty(
                    this, Double.class,
                    NbBundle.getMessage(ForceAtlasLayout.class, "forceAtlas.freezeStrength.name"),
                    FORCE_ATLAS,
                    "forceAtlas.freezeStrength.name",
                    NbBundle.getMessage(ForceAtlasLayout.class, "forceAtlas.freezeStrength.desc"),
                    "getFreezeStrength", "setFreezeStrength"));
            properties.add(LayoutProperty.createProperty(
                    this, Double.class,
                    NbBundle.getMessage(ForceAtlasLayout.class, "forceAtlas.freezeInertia.name"),
                    FORCE_ATLAS,
                    "forceAtlas.freezeInertia.name",
                    NbBundle.getMessage(ForceAtlasLayout.class, "forceAtlas.freezeInertia.desc"),
                    "getFreezeInertia", "setFreezeInertia"));
            properties.add(LayoutProperty.createProperty(
                    this, Double.class,
                    NbBundle.getMessage(ForceAtlasLayout.class, "forceAtlas.gravity.name"),
                    FORCE_ATLAS,
                    "forceAtlas.gravity.name",
                    NbBundle.getMessage(ForceAtlasLayout.class, "forceAtlas.gravity.desc"),
                    "getGravity", "setGravity"));
            properties.add(LayoutProperty.createProperty(
                    this, Boolean.class,
                    NbBundle.getMessage(ForceAtlasLayout.class, "forceAtlas.outboundAttractionDistribution.name"),
                    FORCE_ATLAS,
                    "forceAtlas.outboundAttractionDistribution.name",
                    NbBundle.getMessage(ForceAtlasLayout.class, "forceAtlas.outboundAttractionDistribution.desc"),
                    "isOutboundAttractionDistribution", "setOutboundAttractionDistribution"));
            properties.add(LayoutProperty.createProperty(
                    this, Boolean.class,
                    NbBundle.getMessage(ForceAtlasLayout.class, "forceAtlas.adjustSizes.name"),
                    FORCE_ATLAS,
                    "forceAtlas.adjustSizes.name",
                    NbBundle.getMessage(ForceAtlasLayout.class, "forceAtlas.adjustSizes.desc"),
                    "isAdjustSizes", "setAdjustSizes"));
            properties.add(LayoutProperty.createProperty(
                    this, Double.class,
                    NbBundle.getMessage(ForceAtlasLayout.class, "forceAtlas.speed.name"),
                    FORCE_ATLAS,
                    "forceAtlas.speed.name",
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
