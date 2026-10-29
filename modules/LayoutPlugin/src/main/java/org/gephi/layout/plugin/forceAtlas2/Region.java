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
import org.gephi.layout.plugin.forceAtlas2.ForceFactory.RepulsionForce;

/**
 * Barnes Hut optimization
 *
 * @author Mathieu Jacomy
 */
public class Region {

    private final List<Node> nodes;
    private final List<Region> subregions = new ArrayList<>();
    private double mass;
    private double massCenterX;
    private double massCenterY;
    private double size;

    public Region(Node[] nodes) {
        this.nodes = new ArrayList<>(Arrays.asList(nodes));
        updateMassAndGeometry();
    }

    public Region(List<Node> nodes) {
        this.nodes = new ArrayList<>(nodes);
        updateMassAndGeometry();
    }

    private void updateMassAndGeometry() {
        if (nodes.size() > 1) {
            // Compute Mass
            mass = 0;
            double massSumX = 0;
            double massSumY = 0;
            for (Node n : nodes) {
                ForceAtlas2LayoutData nLayout = n.getLayoutData();
                mass += nLayout.mass;
                massSumX += n.x() * nLayout.mass;
                massSumY += n.y() * nLayout.mass;
            }
            massCenterX = massSumX / mass;
            massCenterY = massSumY / mass;

            // Compute size
            size = 0d;
            for (Node n : nodes) {
                double distance = Math.hypot(n.x() - massCenterX, n.y() - massCenterY);
                size = Math.max(size, 2 * distance);
            }
        }
    }

    public synchronized void buildSubRegions() {
        if (nodes.size() > 1) {
            List<Node> leftNodes = new ArrayList<>();
            List<Node> rightNodes = new ArrayList<>();
            for (Node n : nodes) {
                List<Node> nodesColumn = n.x() < massCenterX ? leftNodes : rightNodes;
                nodesColumn.add(n);
            }

            List<Node> topLeftNodes = new ArrayList<>();
            List<Node> bottomLeftNodes = new ArrayList<>();
            for (Node n : leftNodes) {
                List<Node> nodesLine = n.y() < massCenterY ? topLeftNodes : bottomLeftNodes;
                nodesLine.add(n);
            }

            List<Node> bottomRightNodes = new ArrayList<>();
            List<Node> topRightNodes = new ArrayList<>();
            for (Node n : rightNodes) {
                List<Node> nodesLine = n.y() < massCenterY ? topRightNodes : bottomRightNodes;
                nodesLine.add(n);
            }

            if (!topLeftNodes.isEmpty()) {
                if (topLeftNodes.size() < nodes.size()) {
                    Region subregion = new Region(topLeftNodes);
                    subregions.add(subregion);
                } else {
                    for (Node n : topLeftNodes) {
                        List<Node> oneNodeList = new ArrayList<>();
                        oneNodeList.add(n);
                        Region subregion = new Region(oneNodeList);
                        subregions.add(subregion);
                    }
                }
            }
            if (!bottomLeftNodes.isEmpty()) {
                if (bottomLeftNodes.size() < nodes.size()) {
                    Region subregion = new Region(bottomLeftNodes);
                    subregions.add(subregion);
                } else {
                    for (Node n : bottomLeftNodes) {
                        List<Node> oneNodeList = new ArrayList<>();
                        oneNodeList.add(n);
                        Region subregion = new Region(oneNodeList);
                        subregions.add(subregion);
                    }
                }
            }
            if (!bottomRightNodes.isEmpty()) {
                if (bottomRightNodes.size() < nodes.size()) {
                    Region subregion = new Region(bottomRightNodes);
                    subregions.add(subregion);
                } else {
                    for (Node n : bottomRightNodes) {
                        List<Node> oneNodeList = new ArrayList<>();
                        oneNodeList.add(n);
                        Region subregion = new Region(oneNodeList);
                        subregions.add(subregion);
                    }
                }
            }
            if (!topRightNodes.isEmpty()) {
                if (topRightNodes.size() < nodes.size()) {
                    Region subregion = new Region(topRightNodes);
                    subregions.add(subregion);
                } else {
                    for (Node n : topRightNodes) {
                        List<Node> oneNodeList = new ArrayList<>();
                        oneNodeList.add(n);
                        Region subregion = new Region(oneNodeList);
                        subregions.add(subregion);
                    }
                }
            }

            for (Region subregion : subregions) {
                subregion.buildSubRegions();
            }
        }
    }

    public void applyForce(Node node, RepulsionForce repulsionForce, double theta) {
        if (nodes.size() < 2) {
            Node regionNode = nodes.get(0);
            repulsionForce.apply(node, regionNode);
        } else {
            double distance = Math.hypot(node.x() - massCenterX, node.y() - massCenterY);
            if (distance * theta > size) {
                repulsionForce.apply(node, this);
            } else {
                for (Region subregion : subregions) {
                    subregion.applyForce(node, repulsionForce, theta);
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
