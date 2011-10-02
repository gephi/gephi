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
import org.gephi.graph.api.NodeData;
import org.gephi.layout.plugin.forceAtlas2.ForceFactory.RepulsionForce;

/**
 * Barnes Hut optimization
 * @author Mathieu Jacomy
 */
public class Region {

    private double mass;
    private double massCenterX;
    private double massCenterY;
    private double size;
    private final List<Node> nodes;
    private final List<Region> subregions = new ArrayList<Region>();

    public Region(Node[] nodes) {
        this.nodes = new ArrayList<Node>();
        this.nodes.addAll(Arrays.asList(nodes));
        updateMassAndGeometry();
    }

    public Region(ArrayList<Node> nodes) {
        this.nodes = new ArrayList<Node>(nodes);
        updateMassAndGeometry();
    }

    private void updateMassAndGeometry() {
        if (nodes.size() > 1) {
            // Compute Mass
            mass = 0;
            double massSumX = 0;
            double massSumY = 0;
            for (Node n : nodes) {
                NodeData nData = n.getNodeData();
                ForceAtlas2LayoutData nLayout = nData.getLayoutData();
                mass += nLayout.mass;
                massSumX += nData.x() * nLayout.mass;
                massSumY += nData.y() * nLayout.mass;
            }
            massCenterX = massSumX / mass;
            massCenterY = massSumY / mass;

            // Compute size
            size = Double.MIN_VALUE;
            for (Node n : nodes) {
                NodeData nData = n.getNodeData();
                double distance = Math.sqrt((nData.x() - massCenterX) * (nData.x() - massCenterX) + (nData.y() - massCenterY) * (nData.y() - massCenterY));
                size = Math.max(size, 2 * distance);
            }
        }
    }

    public synchronized void buildSubRegions() {
        if (nodes.size() > 1) {
            ArrayList<Node> leftNodes = new ArrayList<Node>();
            ArrayList<Node> rightNodes = new ArrayList<Node>();
            for (Node n : nodes) {
                NodeData nData = n.getNodeData();
                ArrayList<Node> nodesColumn = (nData.x() < massCenterX) ? (leftNodes) : (rightNodes);
                nodesColumn.add(n);
            }

            ArrayList<Node> topleftNodes = new ArrayList<Node>();
            ArrayList<Node> bottomleftNodes = new ArrayList<Node>();
            for (Node n : leftNodes) {
                NodeData nData = n.getNodeData();
                ArrayList<Node> nodesLine = (nData.y() < massCenterY) ? (topleftNodes) : (bottomleftNodes);
                nodesLine.add(n);
            }

            ArrayList<Node> bottomrightNodes = new ArrayList<Node>();
            ArrayList<Node> toprightNodes = new ArrayList<Node>();
            for (Node n : rightNodes) {
                NodeData nData = n.getNodeData();
                ArrayList<Node> nodesLine = (nData.y() < massCenterY) ? (toprightNodes) : (bottomrightNodes);
                nodesLine.add(n);
            }

            if (topleftNodes.size() > 0) {
                if (topleftNodes.size() < nodes.size()) {
                    Region subregion = new Region(topleftNodes);
                    subregions.add(subregion);
                } else {
                    for (Node n : topleftNodes) {
                        ArrayList<Node> oneNodeList = new ArrayList<Node>();
                        oneNodeList.add(n);
                        Region subregion = new Region(oneNodeList);
                        subregions.add(subregion);
                    }
                }
            }
            if (bottomleftNodes.size() > 0) {
                if (bottomleftNodes.size() < nodes.size()) {
                    Region subregion = new Region(bottomleftNodes);
                    subregions.add(subregion);
                } else {
                    for (Node n : bottomleftNodes) {
                        ArrayList<Node> oneNodeList = new ArrayList<Node>();
                        oneNodeList.add(n);
                        Region subregion = new Region(oneNodeList);
                        subregions.add(subregion);
                    }
                }
            }
            if (bottomrightNodes.size() > 0) {
                if (bottomrightNodes.size() < nodes.size()) {
                    Region subregion = new Region(bottomrightNodes);
                    subregions.add(subregion);
                } else {
                    for (Node n : bottomrightNodes) {
                        ArrayList<Node> oneNodeList = new ArrayList<Node>();
                        oneNodeList.add(n);
                        Region subregion = new Region(oneNodeList);
                        subregions.add(subregion);
                    }
                }
            }
            if (toprightNodes.size() > 0) {
                if (toprightNodes.size() < nodes.size()) {
                    Region subregion = new Region(toprightNodes);
                    subregions.add(subregion);
                } else {
                    for (Node n : toprightNodes) {
                        ArrayList<Node> oneNodeList = new ArrayList<Node>();
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

    public void applyForce(Node n, RepulsionForce Force, double theta) {
        NodeData nData = n.getNodeData();
        if (nodes.size() < 2) {
            Node regionNode = nodes.get(0);
            Force.apply(n, regionNode);
        } else {
            double distance = Math.sqrt((nData.x() - massCenterX) * (nData.x() - massCenterX) + (nData.y() - massCenterY) * (nData.y() - massCenterY));
            if (distance * theta > size) {
                Force.apply(n, this);
            } else {
                for (Region subregion : subregions) {
                    subregion.applyForce(n, Force, theta);
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
