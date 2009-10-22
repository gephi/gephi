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
package org.gephi.algorithms.cluster;

import java.awt.Color;
import java.awt.Graphics;
import javax.swing.JPanel;
import org.gephi.graph.api.HierarchicalGraph;
import org.gephi.graph.api.Node;

public class Dendrogram extends JPanel {

    private static final long serialVersionUID = 2892192060246909733L;
    private static final int MARGIN = 10;
    private HierarchicalGraph graph;
    private int numObjects;
    private double maxDistance;
    private double minDistance;
    private int maxX;
    private int maxY;
    private int count;
    private Color color = Color.BLACK;
    private DendrogramNode root;

    public Dendrogram(HierarchicalGraph graph) {

        this.graph = graph;
        numObjects = graph.getNodeCount();
        root = buildTree();

        //MinMaxDistance
        minDistance = Double.POSITIVE_INFINITY;
        maxDistance = Double.NEGATIVE_INFINITY;
        findMinMaxDistance(root);

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
        for (int i = 0; i < nodeChildren.length; i++) {
            children[i] = traverseTree(nodeChildren[i]);
        }
        return new DendrogramNode(children);
    }

    private void findMinMaxDistance(DendrogramNode node) {
        double distance = node.getDistance();
        maxDistance = Math.max(maxDistance, distance);
        minDistance = Math.min(minDistance, distance);

        for (DendrogramNode subNode : node.getChildren()) {
            findMinMaxDistance(subNode);
        }
    }

    private void drawLine(int x1, int y1, int x2, int y2, Graphics g) {
        g.setColor(color);
        g.drawLine(x1, y1, x2, y2);
    }

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

        paintRecursively(root, root.getDistance(), translated);
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
            if (subNode.getChildren().length > 0) {
                int currentPos = paintRecursively(subNode, node.getDistance(), g);
                if (leftPos == -1) {
                    leftPos = currentPos;
                }
                rightPos = currentPos;
            }
        }

        // drawing vertical cluster lines of one elemental clusters
        for (DendrogramNode subNode : node.getChildren()) {
            if (subNode.getChildren().length == 0) {
                int currentPos = countToXPos(count);
                drawLine(currentPos, weightToYPos(node.getDistance()), currentPos, weightToYPos(minDistance), g);
                if (leftPos == -1) {
                    leftPos = currentPos;
                }
                rightPos = currentPos;
                count++;
            }
        }

        int middlePos = (rightPos + leftPos) / 2;

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

        public DendrogramNode(DendrogramNode[] children) {
            this.children = children;
            distance = children.length;
            for(DendrogramNode d : children) {
                distance+=d.distance;
            }
        }

        public DendrogramNode[] getChildren() {
            return children;
        }

        public double getDistance() {
            return distance;
        }
    }
}
