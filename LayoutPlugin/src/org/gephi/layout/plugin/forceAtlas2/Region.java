/*
Copyright 2008-2011 Gephi
Authors : Mathieu Jacomy <mathieu.jacomy@gmail.com>
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
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
