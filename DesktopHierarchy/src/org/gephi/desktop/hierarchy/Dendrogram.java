/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
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
package org.gephi.desktop.hierarchy;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import javax.swing.JPanel;
import org.gephi.graph.api.HierarchicalGraph;
import org.gephi.graph.api.Node;

public class Dendrogram extends JPanel {

    //Graph
    private HierarchicalGraph graph;

    //Internal
    private static final int MARGIN = 10;
    private int numObjects;
    private double maxDistance;
    private double minDistance;
    private int maxX;
    private int maxY;
    private int count;
    private Color color = Color.BLACK;
    private DendrogramNode root;

    //Settings
    private int maxHeight = 0;

    public Dendrogram() {
    }

    public void refresh(HierarchicalGraph graph) {
        this.graph = graph;
        if (graph != null) {
            numObjects = graph.getNodeCount();
            maxHeight = Math.min(graph.getHeight() + 1, maxHeight);
            root = buildTree();

            //MinMaxDistance
            minDistance = Double.POSITIVE_INFINITY;
            maxDistance = Double.NEGATIVE_INFINITY;
            findMinMaxDistance(root);
        } else {
            root = null;
        }
    }

    public int getMaxHeight() {
        return maxHeight;
    }

    public void setMaxHeight(int maxHeight) {
        this.maxHeight = maxHeight;
    }

    private DendrogramNode buildTree() {
        DendrogramNode rootNode = new DendrogramNode(new DendrogramNode[0]);
        Node[] topNodes = graph.getTopNodes().toArray();
        if (topNodes.length == 1) {
            rootNode = traverseTree(topNodes[0]);
        } else if (topNodes.length > 1) {
            DendrogramNode[] children = new DendrogramNode[topNodes.length];
            for (int i = 0; i < topNodes.length; i++) {
                children[i] = traverseTree(topNodes[i]);
            }
            rootNode = new DendrogramNode(children);
        }
        return rootNode;
    }

    private DendrogramNode traverseTree(Node node) {
        Node[] nodeChildren = graph.getChildren(node).toArray();
        DendrogramNode[] children = new DendrogramNode[nodeChildren.length];
        int height = 0;
        for (int i = 0; i < nodeChildren.length; i++) {
            children[i] = traverseTree(nodeChildren[i]);
            height = Math.max(height, children[i].getHeight());
        }
        DendrogramNode dendrogramNode = new DendrogramNode(children);
        if (children.length == 0) {
            dendrogramNode.setHeight(0);
        } else {
            dendrogramNode.setHeight(height + 1);
        }
        if (graph.isInView(node)) {
            dendrogramNode.setRed(true);
        }
        return dendrogramNode;
    }

    private void findMinMaxDistance(DendrogramNode node) {
        double distance = node.getDistance();
        maxDistance = Math.max(maxDistance, distance);
        minDistance = Math.min(minDistance, distance);

        for (DendrogramNode subNode : node.getChildren()) {
            if (subNode != null) {
                findMinMaxDistance(subNode);
            }
        }
    }

    private void drawLine(int x1, int y1, int x2, int y2, Graphics g) {
        g.drawLine(x1, y1, x2, y2);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        /*if ((minDistance == maxDistance) || (Double.isNaN(minDistance)) || (Double.isInfinite(minDistance)) ||
        (Double.isNaN(maxDistance)) || (Double.isInfinite(maxDistance))) {
        g.drawString("Dendrogram not available for this cluster model. Use an agglomerative clusterer.", MARGIN, MARGIN + 15);
        return;
        }*/

        this.maxX = getWidth() - 2 * MARGIN;
        this.maxY = getHeight() - 2 * MARGIN;

        g.setColor(Color.WHITE);
        g.fillRect(0, 0, getWidth(), getHeight());

        Graphics translated = g.create();
        translated.translate(MARGIN, MARGIN);

        count = 0;

        if (root != null) {
            paintRecursively(root, root.getDistance(), translated);
        }
    }

    private int weightToYPos(double weight) {
        return (int) Math.round(maxY * (((maxDistance - weight) - minDistance) / ((maxDistance - minDistance))));
    }

    private int countToXPos(int count) {
        return (int) Math.round((((double) count) / ((double) numObjects)) * ((double) maxX));
    }

    private int paintRecursively(DendrogramNode node, double baseDistance, Graphics g) {
        int leftPos = -1;
        int rightPos = -1;
        // doing recursive descent
        for (DendrogramNode subNode : node.getChildren()) {
            if (subNode != null) {
                if (subNode.getChildren().length > 0) {
                    int currentPos = paintRecursively(subNode, node.getDistance(), g);
                    if (leftPos == -1) {
                        leftPos = currentPos;
                    }
                    rightPos = currentPos;
                }
            }

        }
        g.setColor(color);
        // drawing vertical cluster lines of one elemental clusters
        for (DendrogramNode subNode : node.getChildren()) {
            if (subNode != null) {
                if (subNode.getChildren().length == 0) {
                    int currentPos = countToXPos(count);
                    if (subNode.isRed()) {
                        g.setColor(Color.RED);
                    }
                    drawLine(currentPos, weightToYPos(node.getDistance()), currentPos, weightToYPos(minDistance), g);
                    g.setColor(color);
                    if (leftPos == -1) {
                        leftPos = currentPos;
                    }
                    rightPos = currentPos;
                    count++;
                }
            }
        }

        int middlePos = (rightPos + leftPos) / 2;
        if (node.isRed()) {
            g.setColor(Color.RED);
        }
        // painting vertical connections of merged clusters to next cluster
        drawLine(middlePos, weightToYPos(baseDistance), middlePos, weightToYPos(node.getDistance()), g);
        // painting horizontal connections of merged clusters

        drawLine(leftPos, weightToYPos(node.getDistance()), rightPos, weightToYPos(node.getDistance()), g);
        return middlePos;
    }

    public void prepareRendering() {
    }

    public void finishRendering() {
    }

    public int getRenderHeight(int preferredHeight) {
        int height = getHeight();
        if (height < 1) {
            height = preferredHeight;
        }
        return height;
    }

    public int getRenderWidth(int preferredWidth) {
        int width = getWidth();
        if (width < 1) {
            width = preferredWidth;
        }
        return width;
    }

    public void render(Graphics graphics, int width, int height) {
        setSize(width, height);
        paint(graphics);
    }

    private class DendrogramNode {

        private DendrogramNode[] children;
        private double distance;
        private boolean red = false;
        private int height;

        public DendrogramNode(DendrogramNode[] children) {
            this.children = children;
            distance = children.length;
            for (int i = 0; i < children.length; i++) {
                DendrogramNode child = children[i];
                if (child.height < maxHeight) {
                    distance -= child.distance;
                    this.children[i] = null;
                    numObjects--;
                    distance--;
                } else {
                    distance += child.distance;
                }
            }
            if (distance == 0 && children.length > 0) {
                this.children = new DendrogramNode[0];
            }
        }

        public void removeChildren(int index) {
            distance -= children[index].distance;
            children[index] = null;
        }

        public void setHeight(int height) {
            this.height = height;
        }

        public int getHeight() {
            return height;
        }

        public DendrogramNode[] getChildren() {
            return children;
        }

        public double getDistance() {
            return distance;
        }

        public boolean isRed() {
            return red;
        }

        public void setRed(boolean red) {
            this.red = red;
        }
    }
}
