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

import java.util.Arrays;
import java.util.concurrent.ConcurrentLinkedDeque;
import org.gephi.layout.plugin.forceAtlas2.ForceFactorySpeed.RepulsionForce;

/**
 * Barnes Hut optimization
 *
 * @author Mathieu Jacomy
 */
public class RegionSpeed {

    private final int[] nodesinRegion;
    private final float[] nodesInfo;
    private final ConcurrentLinkedDeque<RegionSpeed> subregions = new ConcurrentLinkedDeque<>();
    private float mass;
    private float massCenterX;
    private float massCenterY;
    private float size;
    private float sizeSquared;
    private final int numberOfNodesInRegion;

    public RegionSpeed(float[] nodesInfo, int[] nodes, int numberOfNodesInRegion) {
        this.nodesinRegion = nodes;
        this.nodesInfo = nodesInfo;
        this.numberOfNodesInRegion = numberOfNodesInRegion;
        updateMassAndGeometry();
    }

    private void updateMassAndGeometry() {
        if (numberOfNodesInRegion > 1) {
            mass = 0;
            float massSumX = 0;
            float massSumY = 0;
            for (int n : nodesinRegion) {
                if (n == 0) {
                    continue;
                }
                float massElement = nodesInfo[n + 1];
                float xElement = nodesInfo[n + 2];
                float yElement = nodesInfo[n + 3];

                mass += massElement;
                massSumX += xElement * massElement;
                massSumY += yElement * massElement;
            }
            massCenterX = massSumX / mass;
            massCenterY = massSumY / mass;

            // Compute size
            size = Float.MIN_VALUE;
            for (int n : nodesinRegion) {
                if (n == 0) {
                    continue;
                }
                float deltaX = nodesInfo[n + 2] - massCenterX;
                float deltaY = nodesInfo[n + 3] - massCenterY;
                size = Math.max(size, deltaX * deltaX + deltaY * deltaY);
            }
            size = 2 * (float) Math.sqrt(size);
            sizeSquared = size * size;
        }
    }

    public void buildSubRegions() {
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

    public void applyForce(float[] nodesInfo, int nodeIndex, RepulsionForce repulsionForce, float thetaSquared) {
        if (numberOfNodesInRegion < 2) {
            repulsionForce.applyClassicRepulsion(nodesInfo, nodeIndex, nodesinRegion[0]);
        } else {
            float xDist = nodesInfo[nodeIndex + 2] - massCenterX;
            float yDist = nodesInfo[nodeIndex + 3] - massCenterY;
            float distSquared = xDist * xDist + yDist * yDist;
            if (distSquared * thetaSquared > sizeSquared) {
                repulsionForce.applyBarnesHutRepulsion(nodesInfo, nodeIndex, this, distSquared, xDist, yDist);
            } else {
                for (RegionSpeed subregion : subregions) {
                    subregion.applyForce(nodesInfo, nodeIndex, repulsionForce, thetaSquared);
                }
            }
        }
    }

    public float getMass() {
        return mass;
    }

    public void setMass(float mass) {
        this.mass = mass;
    }

    public float getMassCenterX() {
        return massCenterX;
    }

    public void setMassCenterX(float massCenterX) {
        this.massCenterX = massCenterX;
    }

    public float getMassCenterY() {
        return massCenterY;
    }

    public void setMassCenterY(float massCenterY) {
        this.massCenterY = massCenterY;
    }
}
