/*
Copyright 2008-2010 Gephi
Authors : Mathieu Jacomy
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
package org.gephi.layout.plugin.labelAdjust;

import java.util.ArrayList;
import java.util.List;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;
import org.gephi.layout.plugin.AbstractLayout;
import org.gephi.layout.spi.Layout;
import org.gephi.layout.spi.LayoutBuilder;
import org.gephi.layout.spi.LayoutProperty;
import org.openide.util.NbBundle;

/**
 *
 * @author Mathieu Jacomy
 */
public class LabelAdjust extends AbstractLayout implements Layout {

    //Graph
    protected Graph graph;
    //Settings
    private double speed = 1;
    private boolean adjustBySize = true;
    private float radiusScale = 1.1f;
    //Graph size
    private float xmin;
    private float xmax;
    private float ymin;
    private float ymax;

    public LabelAdjust(LayoutBuilder layoutBuilder) {
        super(layoutBuilder);
    }

    public void resetPropertiesValues() {
        speed = 1;
        radiusScale = 1.1f;
        adjustBySize = true;
    }

    public void initAlgo() {
        setConverged(false);
    }

    public void goAlgo() {
        this.graph = graphModel.getGraphVisible();
        graph.readLock();
        Node[] nodes = graph.getNodes().toArray();

        //Reset Layout Data
        for (Node n : nodes) {
            if (n.getNodeData().getLayoutData() == null || !(n.getNodeData().getLayoutData() instanceof LabelAdjustLayoutData)) {
                n.getNodeData().setLayoutData(new LabelAdjustLayoutData());
            }
            LabelAdjustLayoutData layoutData = n.getNodeData().getLayoutData();
            layoutData.freeze = 0;
            layoutData.dx = 0;
            layoutData.dy = 0;
        }

        // Get xmin, xmax, ymin, ymax
        xmin = Float.MAX_VALUE;
        xmax = Float.MIN_VALUE;
        ymin = Float.MAX_VALUE;
        ymax = Float.MIN_VALUE;

        List<Node> correctNodes = new ArrayList<Node>();
        for (Node n : nodes) {
            float x = n.getNodeData().x();
            float y = n.getNodeData().y();
            float w = n.getNodeData().getTextData().getWidth();
            float h = n.getNodeData().getTextData().getHeight();
            float radius = n.getNodeData().getRadius();

            if (w > 0 && h > 0) {
                // Get the rectangle occupied by the node (size + label)
                float nxmin = Math.min(x - w / 2, x - radius);
                float nxmax = Math.max(x + w / 2, x + radius);
                float nymin = Math.min(y - h / 2, y - radius);
                float nymax = Math.max(y + h / 2, y + radius);

                // Update global boundaries
                xmin = Math.min(this.xmin, nxmin);
                xmax = Math.max(this.xmax, nxmax);
                ymin = Math.min(this.ymin, nymin);
                ymax = Math.max(this.ymax, nymax);

                correctNodes.add(n);
            }
        }

        if (correctNodes.isEmpty() || xmin == xmax || ymin == ymax) {
            graph.readUnlock();
            return;
        }

        long timeStamp = 1;
        boolean someCollision = false;

        //Add all nodes in the quadtree
        QuadTree quadTree = new QuadTree(correctNodes.size(), (xmax - xmin) / (ymax - ymin));
        for (Node n : correctNodes) {
            quadTree.add(n);
        }

        //Compute repulsion - with neighbours in the 8 quadnodes around the node
        for (Node n : correctNodes) {
            timeStamp++;
            LabelAdjustLayoutData layoutData = n.getNodeData().getLayoutData();
            QuadNode quad = quadTree.getQuadNode(layoutData.labelAdjustQuadNode);

            //Repulse with adjacent quad - but only one per pair of nodes, timestamp is guaranteeing that
            for (Node neighbour : quadTree.getAdjacentNodes(quad.row, quad.col)) {
                LabelAdjustLayoutData neighborLayoutData = neighbour.getNodeData().getLayoutData();
                if (neighbour != n && neighborLayoutData.freeze < timeStamp) {
                    boolean collision = repulse(n, neighbour);
                    someCollision = someCollision || collision;
                }
                neighborLayoutData.freeze = timeStamp; //Use the existing freeze float variable to set timestamp
            }
        }

        if (!someCollision) {
            setConverged(true);
        } else {
            // apply forces
            for (Node n : correctNodes) {
                LabelAdjustLayoutData layoutData = n.getNodeData().getLayoutData();
                if (!n.getNodeData().isFixed()) {
                    layoutData.dx *= speed;
                    layoutData.dy *= speed;
                    float x = n.getNodeData().x() + layoutData.dx;
                    float y = n.getNodeData().y() + layoutData.dy;

                    n.getNodeData().setX(x);
                    n.getNodeData().setY(y);
                }
            }
        }

        graph.readUnlock();
    }

    private boolean repulse(Node n1, Node n2) {
        boolean collision = false;
        float n1x = n1.getNodeData().x();
        float n1y = n1.getNodeData().y();
        float n2x = n2.getNodeData().x();
        float n2y = n2.getNodeData().y();
        float n1w = n1.getNodeData().getTextData().getWidth();
        float n2w = n2.getNodeData().getTextData().getWidth();
        float n1h = n1.getNodeData().getTextData().getHeight();
        float n2h = n2.getNodeData().getTextData().getHeight();
        LabelAdjustLayoutData n2Data = n2.getNodeData().getLayoutData();

        double n1xmin = n1x - 0.5 * n1w;
        double n2xmin = n2x - 0.5 * n2w;
        double n1ymin = n1y - 0.5 * n1h;
        double n2ymin = n2y - 0.5 * n2h;
        double n1xmax = n1x + 0.5 * n1w;
        double n2xmax = n2x + 0.5 * n2w;
        double n1ymax = n1y + 0.5 * n1h;
        double n2ymax = n2y + 0.5 * n2h;

        //Sphere repulsion
        if (adjustBySize) {
            double xDist = n2x - n1x;
            double yDist = n2y - n1y;
            double dist = Math.sqrt(xDist * xDist + yDist * yDist);
            boolean sphereCollision = dist < radiusScale * (n1.getNodeData().getRadius() + n2.getNodeData().getRadius());
            if (sphereCollision) {
                double f = 0.1 * n1.getNodeData().getRadius() / dist;
                if (dist > 0) {
                    n2Data.dx = (float) (n2Data.dx + xDist / dist * f);
                    n2Data.dy = (float) (n2Data.dy + yDist / dist * f);
                } else {
                    n2Data.dx = (float) (n2Data.dx + 0.01 * (0.5 - Math.random()));
                    n2Data.dy = (float) (n2Data.dy + 0.01 * (0.5 - Math.random()));
                }
                collision = true;
            }
        }

        double upDifferential = n1ymax - n2ymin;
        double downDifferential = n2ymax - n1ymin;
        double labelCollisionXleft = n2xmax - n1xmin;
        double labelCollisionXright = n1xmax - n2xmin;

        if (upDifferential > 0 && downDifferential > 0) { // Potential collision
            if (labelCollisionXleft > 0 && labelCollisionXright > 0) {// Collision
                if (upDifferential > downDifferential) {
                    // N1 pushes N2 up
                    n2Data.dy = (float) (n2Data.dy - 0.02 * n1h * (0.8 + 0.4 * Math.random()));
                    collision = true;
                } else {
                    // N1 pushes N2 down
                    n2Data.dy = (float) (n2Data.dy + 0.02 * n1h * (0.8 + 0.4 * Math.random()));
                    collision = true;
                }
                if (labelCollisionXleft > labelCollisionXright) {
                    // N1 pushes N2 right
                    n2Data.dx = (float) (n2Data.dx + 0.01 * (n1h * 2) * (0.8 + 0.4 * Math.random()));
                    collision = true;
                } else {
                    // N1 pushes N2 left
                    n2Data.dx = (float) (n2Data.dx - 0.01 * (n1h * 2) * (0.8 + 0.4 * Math.random()));
                    collision = true;
                }
            }
        }

        return collision;
    }

    public void endAlgo() {
        for (Node n : graph.getNodes()) {
            n.getNodeData().setLayoutData(null);
        }
    }

    public LayoutProperty[] getProperties() {
        List<LayoutProperty> properties = new ArrayList<LayoutProperty>();
        final String LABELADJUST_CATEGORY = "LabelAdjust";
        try {
            properties.add(LayoutProperty.createProperty(
                    this, Double.class,
                    NbBundle.getMessage(getClass(), "LabelAdjust.speed.name"),
                    LABELADJUST_CATEGORY,
                    NbBundle.getMessage(getClass(), "LabelAdjust.speed.desc"),
                    "getSpeed", "setSpeed"));
            properties.add(LayoutProperty.createProperty(
                    this, Boolean.class,
                    NbBundle.getMessage(getClass(), "LabelAdjust.adjustBySize.name"),
                    LABELADJUST_CATEGORY,
                    NbBundle.getMessage(getClass(), "LabelAdjust.adjustBySize.desc"),
                    "isAdjustBySize", "setAdjustBySize"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return properties.toArray(new LayoutProperty[0]);
    }

    public Double getSpeed() {
        return speed;
    }

    public void setSpeed(Double speed) {
        this.speed = speed;
    }

    public Boolean isAdjustBySize() {
        return adjustBySize;
    }

    public void setAdjustBySize(Boolean adjustBySize) {
        this.adjustBySize = adjustBySize;
    }

    private static class QuadNode {

        private final int index;
        private final int row;
        private final int col;
        private final List<Node> nodes;

        public QuadNode(int index, int row, int col) {
            this.index = index;
            this.row = row;
            this.col = col;
            this.nodes = new ArrayList<Node>();
        }

        public List<Node> getNodes() {
            return nodes;
        }

        public void add(Node n) {
            nodes.add(n);
        }
    }

    private class QuadTree {

        private final QuadNode[] quads;
        private final int COLUMNS;
        private final int ROWS;

        public QuadTree(int numberNodes, float aspectRatio) {
            if (aspectRatio > 0) {
                COLUMNS = (int) Math.ceil(numberNodes / 50f);
                ROWS = (int) Math.ceil(COLUMNS / aspectRatio);
            } else {
                ROWS = (int) Math.ceil(numberNodes / 50f);
                COLUMNS = (int) Math.ceil(ROWS / aspectRatio);
            }
            quads = new QuadNode[COLUMNS * ROWS];
            for (int row = 0; row < ROWS; row++) {
                for (int col = 0; col < COLUMNS; col++) {
                    quads[row * COLUMNS + col] = new QuadNode(row * COLUMNS + col, row, col);
                }
            }
        }

        public void add(Node node) {
            float x = node.getNodeData().x();
            float y = node.getNodeData().y();
            float w = node.getNodeData().getTextData().getWidth();
            float h = node.getNodeData().getTextData().getHeight();
            float radius = node.getNodeData().getRadius();

            // Get the rectangle occupied by the node (size + label)
            float nxmin = Math.min(x - w / 2, x - radius);
            float nxmax = Math.max(x + w / 2, x + radius);
            float nymin = Math.min(y - h / 2, y - radius);
            float nymax = Math.max(y + h / 2, y + radius);

            // Get the rectangle as boxes
            int minXbox = (int) Math.floor((COLUMNS - 1) * (nxmin - xmin) / (xmax - xmin));
            int maxXbox = (int) Math.floor((COLUMNS - 1) * (nxmax - xmin) / (xmax - xmin));
            int minYbox = (int) Math.floor((ROWS - 1) * (((ymax - ymin) - (nymax - ymin)) / (ymax - ymin)));
            int maxYbox = (int) Math.floor((ROWS - 1) * (((ymax - ymin) - (nymin - ymin)) / (ymax - ymin)));
            for (int col = minXbox; col <= maxXbox && col < COLUMNS && col >= 0; col++) {
                for (int row = minYbox; row <= maxYbox && row < ROWS && row >= 0; row++) {
                    quads[row * COLUMNS + col].add(node);
                }
            }

            //Get the node center
            int centerX = (int) Math.floor((COLUMNS - 1) * (x - xmin) / (xmax - xmin));
            int centerY = (int) Math.floor((ROWS - 1) * (((ymax - ymin) - (y - ymin)) / (ymax - ymin)));
            LabelAdjustLayoutData layoutData = node.getNodeData().getLayoutData();
            layoutData.labelAdjustQuadNode = quads[centerY * COLUMNS + centerX].index;
        }

        public List<Node> get(int row, int col) {
            return quads[row * ROWS + col].getNodes();
        }

        public List<Node> getAdjacentNodes(int row, int col) {
            if (quads.length == 1) {
                return quads[0].getNodes();
            }

            List<Node> adjNodes = new ArrayList<Node>();
            int left = Math.max(0, col - 1);
            int top = Math.max(0, row - 1);
            int right = Math.min(COLUMNS - 1, col + 1);
            int bottom = Math.min(ROWS - 1, row + 1);
            for (int i = left; i <= right; i++) {
                for (int j = top; j <= bottom; j++) {
                    adjNodes.addAll(quads[j * COLUMNS + i].getNodes());
                }
            }
            return adjNodes;
        }

        public QuadNode getQuadNode(int index) {
            return quads[index];
        }
    }
}
