/*
 Copyright 2008-2011 Gephi
 Authors : Mathieu Jacomy <mathieu.jacomy@gmail.com>
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

package org.gephi.layout.plugin.forceAtlas2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.gephi.graph.api.Node;
import org.gephi.layout.plugin.forceAtlas2.ForceFactorySpeed.RepulsionForce;

/**
 * Barnes Hut optimization
 *
 * @author Mathieu Jacomy
 */
public class RegionSpeed {

    private final int[] nodesinRegion;
    private final double[] nodesInfo;
    private final List<RegionSpeed> subregions = new ArrayList<>();
    private double mass;
    private double massCenterX;
    private double massCenterY;
    private double size;

    public RegionSpeed(double[] nodesInfo, int[] nodes) {
        this.nodesinRegion = nodes;
        this.nodesInfo = nodesInfo;
        updateMassAndGeometry();
    }

    public RegionSpeed(double[] nodesInfo, ArrayList<Integer> nodes) {
        this.nodesInfo = nodesInfo;
        this.nodesinRegion = nodes.stream().mapToInt(Integer::intValue).toArray();
        updateMassAndGeometry();
    }


    private void updateMassAndGeometry() {
        if (nodesinRegion.length > 1) {
            // Compute Mass
            mass = 0;
            double massSumX = 0;
            double massSumY = 0;
            for (int n : nodesinRegion) {
                mass += nodesInfo[n+1];
                massSumX += nodesInfo[n+2] * nodesInfo[n+1];
                massSumY += nodesInfo[n+3] * nodesInfo[n+1];
            }
            massCenterX = massSumX / mass;
            massCenterY = massSumY / mass;

            // Compute size
            size = Double.MIN_VALUE;
            for (int n : nodesinRegion) {
                double distance = Math.sqrt(
                    (nodesInfo[n+2] - massCenterX) * (nodesInfo[n+2] - massCenterX) + (nodesInfo[n+3] - massCenterY) * (nodesInfo[n+3] - massCenterY));
                size = Math.max(size, 2 * distance);
            }
        }
    }

    public synchronized void buildSubRegions() {
        if (nodesinRegion.length > 1) {
            ArrayList<Integer> leftNodes = new ArrayList<>();
            ArrayList<Integer> rightNodes = new ArrayList<>();
            for (int n : nodesinRegion) {
                ArrayList<Integer> nodesColumn = (nodesInfo[n+2] < massCenterX) ? (leftNodes) : (rightNodes);
                nodesColumn.add(n);
            }

            ArrayList<Integer> topleftNodes = new ArrayList<>();
            ArrayList<Integer> bottomleftNodes = new ArrayList<>();
            for (Integer n : leftNodes) {
                ArrayList<Integer> nodesLine = (nodesInfo[n+3] < massCenterY) ? (topleftNodes) : (bottomleftNodes);
                nodesLine.add(n);
            }

            ArrayList<Integer> bottomrightNodes = new ArrayList<>();
            ArrayList<Integer> toprightNodes = new ArrayList<>();
            for (Integer n : rightNodes) {
                ArrayList<Integer> nodesLine = (nodesInfo[n+3] < massCenterY) ? (toprightNodes) : (bottomrightNodes);
                nodesLine.add(n);
            }

            if (!topleftNodes.isEmpty()) {
                if (topleftNodes.size() < nodesinRegion.length) {
                    RegionSpeed subregion = new RegionSpeed( this.nodesInfo, topleftNodes);
                    subregions.add(subregion);
                } else {
                    for (Integer n : topleftNodes) {
                        ArrayList<Integer> oneNodeList = new ArrayList<>();
                        oneNodeList.add(n);
                        RegionSpeed subregion = new RegionSpeed( this.nodesInfo, oneNodeList);
                        subregions.add(subregion);
                    }
                }
            }
            if (!bottomleftNodes.isEmpty()) {
                if (bottomleftNodes.size() < nodesinRegion.length) {
                    RegionSpeed subregion = new RegionSpeed( this.nodesInfo, bottomleftNodes);
                    subregions.add(subregion);
                } else {
                    for (Integer n : bottomleftNodes) {
                        ArrayList<Integer> oneNodeList = new ArrayList<>();
                        oneNodeList.add(n);
                        RegionSpeed subregion = new RegionSpeed( this.nodesInfo, oneNodeList);
                        subregions.add(subregion);
                    }
                }
            }
            if (!bottomrightNodes.isEmpty()) {
                if (bottomrightNodes.size() < nodesinRegion.length) {
                    RegionSpeed subregion = new RegionSpeed( this.nodesInfo, bottomrightNodes);
                    subregions.add(subregion);
                } else {
                    for (Integer n : bottomrightNodes) {
                        ArrayList<Integer> oneNodeList = new ArrayList<>();
                        oneNodeList.add(n);
                        RegionSpeed subregion = new RegionSpeed( this.nodesInfo, oneNodeList);
                        subregions.add(subregion);
                    }
                }
            }
            if (!toprightNodes.isEmpty()) {
                if (toprightNodes.size() < nodesinRegion.length) {
                    RegionSpeed subregion = new RegionSpeed( this.nodesInfo, toprightNodes);
                    subregions.add(subregion);
                } else {
                    for (Integer n : toprightNodes) {
                        ArrayList<Integer> oneNodeList = new ArrayList<>();
                        oneNodeList.add(n);
                        RegionSpeed subregion = new RegionSpeed( this.nodesInfo, oneNodeList);
                        subregions.add(subregion);
                    }
                }
            }

            for (RegionSpeed subregion : subregions) {
                subregion.buildSubRegions();
            }
        }
    }

    public void applyForce(double[] nodesInfo, int nodeIndex, RepulsionForce repulsionForce, double theta) {
        if (nodesinRegion.length < 2) {
            repulsionForce.applyClassicRepulsion(nodesInfo, nodeIndex, nodesinRegion[0]);
        } else {
            double distance = Math.sqrt(
                (nodesInfo[nodeIndex+2] - massCenterX) * (nodesInfo[nodeIndex+2] - massCenterX) + (nodesInfo[nodeIndex+3] - massCenterY) * (nodesInfo[nodeIndex+3] - massCenterY));
            if (distance * theta > size) {
                repulsionForce.applyBarnesHutRepulsion(nodesInfo, nodeIndex, this);
            } else {
                for (RegionSpeed subregion : subregions) {
                    subregion.applyForce(nodesInfo, nodeIndex, repulsionForce, theta);
                }
            }
        }
    }

    public double getMass() {
        return mass;
    }

    public void setMass(double mass) {
        this.mass = mass;
    }

    public double getMassCenterX() {
        return massCenterX;
    }

    public void setMassCenterX(double massCenterX) {
        this.massCenterX = massCenterX;
    }

    public double getMassCenterY() {
        return massCenterY;
    }

    public void setMassCenterY(double massCenterY) {
        this.massCenterY = massCenterY;
    }
}
