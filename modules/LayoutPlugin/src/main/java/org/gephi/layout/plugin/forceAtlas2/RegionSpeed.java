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

import java.util.ArrayDeque;
import java.util.Arrays;
import org.gephi.layout.plugin.forceAtlas2.ForceFactorySpeed.RepulsionForce;

/**
 * Barnes Hut optimization
 *
 * @author Mathieu Jacomy
 */
public class RegionSpeed {

    private final int[] nodesinRegion;
    private final double[] nodesInfo;
    private final ArrayDeque<RegionSpeed> subregions = new ArrayDeque<>();
    private double mass;
    private double massCenterX;
    private double massCenterY;
    private double size;
    private final int numberOfNodesInRegion;

    public RegionSpeed(double[] nodesInfo, int[] nodes, int numberOfNodesInRegion) {
        this.nodesinRegion = nodes;
        this.nodesInfo = nodesInfo;
        this.numberOfNodesInRegion = numberOfNodesInRegion;
        updateMassAndGeometry();
    }

    private void updateMassAndGeometry() {
        if (numberOfNodesInRegion > 1) {
            mass = 0;
            double massSumX = 0;
            double massSumY = 0;
            for (int n : nodesinRegion) {
                if (n == 0) {
                    continue;
                }
                double massElement = nodesInfo[n + 1];
                double xElement = nodesInfo[n + 2];
                double yElement = nodesInfo[n + 3];

                mass += massElement;
                massSumX += xElement * massElement;
                massSumY += yElement * massElement;
            }
            massCenterX = massSumX / mass;
            massCenterY = massSumY / mass;

            // Compute size
            size = Double.MIN_VALUE;
            for (int n : nodesinRegion) {
                if (n == 0) {
                    continue;
                }
                double distance = Math.sqrt(
                        (nodesInfo[n + 2] - massCenterX) * (nodesInfo[n + 2] - massCenterX) + (nodesInfo[n + 3] - massCenterY) * (nodesInfo[n + 3] - massCenterY));
                size = Math.max(size, 2 * distance);
            }
        }
    }

    public synchronized void buildSubRegions() {
        int sizeLeft = 0;
        int sizeRight = 0;
        int sizeBottomLeft = 0;
        int sizeTopLeft = 0;
        int sizeBottomRight = 0;
        int sizeTopRight = 0;
        if (numberOfNodesInRegion > 1) {
            int[] leftNodes = new int[numberOfNodesInRegion];
            int[] rightNodes = new int[numberOfNodesInRegion];
            int i = 0;
            int j = 0;
            
            for (int n : nodesinRegion) {
                if (n == 0) {
                    continue;
                }
                boolean nodeIsOnLeft = nodesInfo[n + 2] < massCenterX;
                if (nodeIsOnLeft) {
                    leftNodes[i++] = n;
                    sizeLeft++;
                } else {
                    rightNodes[j++] = n;
                    sizeRight++;
                }
            }

            i = 0;
            j = 0;
            int[] topleftNodes = new int[numberOfNodesInRegion];
            int[] bottomleftNodes = new int[numberOfNodesInRegion];
            for (int n : leftNodes) {
                if (n == 0) {
                    continue;
                }
                boolean nodeIsOnTop = nodesInfo[n + 3] < massCenterY;
                if (nodeIsOnTop) {
                    topleftNodes[i++] = n;
                    sizeTopLeft++;
                } else {
                    bottomleftNodes[j++] = n;
                    sizeBottomLeft++;
                }
            }
            topleftNodes = Arrays.copyOf(topleftNodes, i);
            bottomleftNodes = Arrays.copyOf(bottomleftNodes, j);

            i = 0;
            j = 0;
            int[] toprightNodes = new int[numberOfNodesInRegion];
            int[] bottomrightNodes = new int[numberOfNodesInRegion];
            for (int n : rightNodes) {
                if (n == 0) {
                    continue;
                }
                boolean nodeIsOnTop = nodesInfo[n + 3] < massCenterY;
                if (nodeIsOnTop) {
                    toprightNodes[i++] = n;
                    sizeTopRight++;
                } else {
                    bottomrightNodes[j++] = n;
                    sizeBottomRight++;
                }
            }
            toprightNodes = Arrays.copyOf(toprightNodes, i);
            bottomrightNodes = Arrays.copyOf(bottomrightNodes, j);

            if (sizeTopLeft > 0) {
                if (sizeTopLeft < numberOfNodesInRegion) {
                    RegionSpeed subregion = new RegionSpeed(this.nodesInfo, topleftNodes, sizeTopLeft);
                    subregions.add(subregion);
                } else {
                    for (int n : topleftNodes) {
                        if (n == 0) {
                            continue;
                        }
                        int[] oneNode = new int[]{n};
                        RegionSpeed subregion = new RegionSpeed(this.nodesInfo, oneNode, 1);
                        subregions.add(subregion);
                    }
                }
            }
            if (sizeBottomLeft > 0) {
                if (sizeBottomLeft < numberOfNodesInRegion) {
                    RegionSpeed subregion = new RegionSpeed(this.nodesInfo, bottomleftNodes, sizeBottomLeft);
                    subregions.add(subregion);
                } else {
                    for (int n : bottomleftNodes) {
                        if (n == 0) {
                            continue;
                        }
                        int[] oneNode = new int[]{n};
                        RegionSpeed subregion = new RegionSpeed(this.nodesInfo, oneNode, 1);
                        subregions.add(subregion);
                    }
                }
            }
            if (sizeBottomRight > 0) {
                if (sizeBottomRight < numberOfNodesInRegion) {
                    RegionSpeed subregion = new RegionSpeed(this.nodesInfo, bottomrightNodes, sizeBottomRight);
                    subregions.add(subregion);
                } else {
                    for (int n : bottomrightNodes) {
                        if (n == 0) {
                            continue;
                        }
                        int[] oneNode = new int[]{n};
                        RegionSpeed subregion = new RegionSpeed(this.nodesInfo, oneNode, 1);
                        subregions.add(subregion);
                    }
                }
            }
            if (sizeTopRight > 0) {
                if (sizeTopRight < numberOfNodesInRegion) {
                    RegionSpeed subregion = new RegionSpeed(this.nodesInfo, toprightNodes, sizeTopRight);
                    subregions.add(subregion);
                } else {
                    for (int n : toprightNodes) {
                        if (n == 0) {
                            continue;
                        }
                        int[] oneNode = new int[]{n};
                        RegionSpeed subregion = new RegionSpeed(this.nodesInfo, oneNode, 1);
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
        if (numberOfNodesInRegion < 2) {
            repulsionForce.applyClassicRepulsion(nodesInfo, nodeIndex, nodesinRegion[0]);
        } else {
            double distance = Math.sqrt((nodesInfo[nodeIndex + 2] - massCenterX) * (nodesInfo[nodeIndex + 2] - massCenterX) + (nodesInfo[nodeIndex + 3] - massCenterY) * (nodesInfo[nodeIndex + 3] - massCenterY));
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
