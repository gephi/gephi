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
package org.gephi.layout.plugin.labelAdjust;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;
import org.gephi.layout.plugin.AbstractLayout;
import org.gephi.layout.plugin.ForceVectorUtils;
import org.gephi.layout.spi.Layout;
import org.gephi.layout.spi.LayoutBuilder;
import org.gephi.layout.spi.LayoutProperty;

/**
 *
 * @author Mathieu Jacomy
 */
public class LabelAdjust extends AbstractLayout implements Layout {

    //Graph
    protected Graph graph;
    //Settings
    private double speed = 1;
    private final double RADIUS_SCALE = 2;
    private double xmin;
    private double xmax;
    private double ymin;
    private double ymax;

    public LabelAdjust(LayoutBuilder layoutBuilder) {
        super(layoutBuilder);
    }

    public void resetPropertiesValues() {
        speed = 1;
    }

    public void initAlgo() {
        setConverged(false);
    }

    public void goAlgo() {
        boolean somethingMoved = false;
        this.graph = graphModel.getGraphVisible();
        graph.readLock();
        Node[] nodes = graph.getNodes().toArray();

        //Reset Layout Data
        for (Node n : nodes) {
            if (n.getNodeData().getLayoutData() == null || !(n.getNodeData().getLayoutData() instanceof LabelAdjustLayoutData)) {
                n.getNodeData().setLayoutData(new LabelAdjustLayoutData());
            }
            LabelAdjustLayoutData layoutData = n.getNodeData().getLayoutData();
            layoutData.neighbours.clear();
            layoutData.dx = 0;
            layoutData.dy = 0;
        }

        // Get xmin, xmax, ymin, ymax
        this.xmin = Double.MAX_VALUE;
        this.xmax = Double.MIN_VALUE;
        this.ymin = Double.MAX_VALUE;
        this.ymax = Double.MIN_VALUE;

        List<Node> correctNodes = new ArrayList<Node>();
        for (Node n : nodes) {
            float x = n.getNodeData().x();
            float y = n.getNodeData().y();
            float w = n.getNodeData().getTextData().getWidth();
            float h = n.getNodeData().getTextData().getHeight();
            float radius = n.getNodeData().getRadius();

            if (w > 0 && h > 0) {
                // Get the rectangle occupied by the node (size + label)
                double nxmin = Math.min(x - w / 2, x - radius);
                double nxmax = Math.max(x + w / 2, x + radius);
                double nymin = Math.min(y - h / 2, y - radius);
                double nymax = Math.max(y + h / 2, y + radius);

                // Update global boundaries
                this.xmin = Math.min(this.xmin, nxmin);
                this.xmax = Math.max(this.xmax, nxmax);
                this.ymin = Math.min(this.ymin, nymin);
                this.ymax = Math.max(this.ymax, nymax);

                correctNodes.add(n);
            }
        }

        if (correctNodes.isEmpty()) {
            return;
        }

        // Secure the bounds
        double xwidth = this.xmax - this.xmin;
        double yheight = this.ymax - this.ymin;
        double xcenter = (this.xmin + this.xmax) / 2;
        double ycenter = (this.ymin + this.ymax) / 2;
        double ratio = 1.1;
        this.xmin = xcenter - ratio * xwidth / 2;
        this.xmax = xcenter + ratio * xwidth / 2;
        this.ymin = ycenter - ratio * yheight / 2;
        this.ymax = ycenter + ratio * yheight / 2;
        //System.out.println("BOUNDS this.xmin="+this.xmin+" this.xmax="+this.xmax+" this.ymin="+this.ymin+" this.ymax="+this.ymax);

        SpatialGrid grid = new SpatialGrid();

        // Put nodes in their boxes
        for (Node n : correctNodes) {
            grid.add(n);
        }

        // Now we have boxes with nodes in it. Nodes that are in the same box, or in adjacent boxes, are tested for repulsion.
        // But they are not repulsed several times, even if they are in several boxes...
        // So we build a relation of proximity between nodes.

        // Build proximities
        for (int row = 0; row < grid.countRows(); row++) {
            for (int col = 0; col < grid.countColumns(); col++) {
                for (Node n : grid.getContent(row, col)) {
                    LabelAdjustLayoutData lald = n.getNodeData().getLayoutData();

                    // For node n in the box "box"...
                    // We search nodes that are in the boxes that are adjacent or the same.
                    for (int row2 = Math.max(0, row - 1); row2 <= Math.min(row + 1, grid.countRows() - 1); row2++) {
                        for (int col2 = Math.max(0, col - 1); col2 <= Math.min(col + 1, grid.countColumns() - 1); col2++) {
                            for (Node n2 : grid.getContent(row2, col2)) {
                                if (n2 != n && !lald.neighbours.contains(n2)) {
                                    lald.neighbours.add(n2);
                                }
                            }
                        }
                    }
                }
            }
        }

        // Proximities are built !

        // Apply repulsion force - along proximities...
        for (Node n1 : correctNodes) {
            LabelAdjustLayoutData lald = n1.getNodeData().getLayoutData();
            for (Node n2 : lald.neighbours) {
                float n1x = n1.getNodeData().x();
                float n1y = n1.getNodeData().y();
                float n2x = n2.getNodeData().x();
                float n2y = n2.getNodeData().y();
                float n1w = n1.getNodeData().getTextData().getWidth();
                float n2w = n2.getNodeData().getTextData().getWidth();
                float n1h = n1.getNodeData().getTextData().getHeight();
                float n2h = n2.getNodeData().getTextData().getHeight();

                // Check sizes (spheric)
                double xDist = Math.abs(n1x - n2x);
                double yDist = Math.abs(n1.getNodeData().y() - n2.getNodeData().y());
                boolean sphereCollision = Math.sqrt(xDist * xDist + yDist * yDist) < RADIUS_SCALE * (n1.getNodeData().getRadius() + n2.getNodeData().getRadius());
                if (sphereCollision) {
                    ForceVectorUtils.fcUniRepulsor(n1.getNodeData(), n2.getNodeData(), 0.1 * n1.getNodeData().getRadius());
                    somethingMoved = true;
                }

                // Check labels, but when no label keep a rectangle equivalent to the sphere
                double n1xmin = n1x - 0.5 * n1w;
                double n2xmin = n2x - 0.5 * n2w;
                double n1ymin = n1y - 0.5 * n1h;
                double n2ymin = n2y - 0.5 * n2h;
                double n1xmax = n1x + 0.5 * n1w;
                double n2xmax = n2x + 0.5 * n2w;
                double n1ymax = n1y + 0.5 * n1h;
                double n2ymax = n2y + 0.5 * n2h;

                double upDifferential = n1ymax - n2ymin;
                double downDifferential = n2ymax - n1ymin;
                double labelCollisionXleft = n2xmax - n1xmin;
                double labelCollisionXright = n1xmax - n2xmin;
                LabelAdjustLayoutData layoutData = n2.getNodeData().getLayoutData();

                if (upDifferential > 0 && downDifferential > 0) { // Potential collision
                    if (labelCollisionXleft > 0 && labelCollisionXright > 0) {// Collision
                        if (upDifferential > downDifferential) {
                            // N1 pushes N2 up
                            layoutData.dy -= 0.01 * n1h * (0.8 + 0.4 * Math.random());
                            somethingMoved = true;
                        } else {
                            // N1 pushes N2 down
                            layoutData.dy += 0.01 * n1h * (0.8 + 0.4 * Math.random());
                            somethingMoved = true;
                        }
                        if (labelCollisionXleft > labelCollisionXright) {
                            // N1 pushes N2 right
                            layoutData.dx += 0.01 * (n1h / 2) * (0.8 + 0.4 * Math.random());
                            somethingMoved = true;
                        } else {
                            // N1 pushes N2 left
                            layoutData.dx -= 0.01 * (n1h / 2) * (0.8 + 0.4 * Math.random());
                            somethingMoved = true;
                        }
                    }
                }
            }
        }

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

        if (!somethingMoved) {
            setConverged(true);
        }
        graph.readUnlock();
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
                    this, Double.class, "speed", LABELADJUST_CATEGORY, "speed", "getSpeed", "setSpeed"));
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

    private class SpatialGrid {

        //Param
        private final int COLUMNS_ROWS = 20;
        //Data
        private Map<Cell, List<Node>> data = new HashMap<Cell, List<Node>>();

        public SpatialGrid() {
            for (int row = 0; row < COLUMNS_ROWS; row++) {
                for (int col = 0; col < COLUMNS_ROWS; col++) {
                    List<Node> localnodes = new ArrayList<Node>();
                    data.put(new Cell(row, col), localnodes);
                }
            }
        }

        public Iterable<Node> getContent(int row, int col) {
            return data.get(new Cell(row, col));
        }

        public int countColumns() {
            return COLUMNS_ROWS;
        }

        public int countRows() {
            return COLUMNS_ROWS;
        }

        public void add(Node node) {
            float x = node.getNodeData().x();
            float y = node.getNodeData().y();
            float w = node.getNodeData().getTextData().getWidth();
            float h = node.getNodeData().getTextData().getHeight();
            float radius = node.getNodeData().getRadius();

            // Get the rectangle occupied by the node (size + label)
            double nxmin = Math.min(x - w / 2, x - radius);
            double nxmax = Math.max(x + w / 2, x + radius);
            double nymin = Math.min(y - h / 2, y - radius);
            double nymax = Math.max(y + h / 2, y + radius);

            // Get the rectangle as boxes
            int minXbox = (int) Math.floor((COLUMNS_ROWS - 1) * (nxmin - xmin) / (xmax - xmin));
            int maxXbox = (int) Math.floor((COLUMNS_ROWS - 1) * (nxmax - xmin) / (xmax - xmin));
            int minYbox = (int) Math.floor((COLUMNS_ROWS - 1) * (nymin - ymin) / (ymax - ymin));
            int maxYbox = (int) Math.floor((COLUMNS_ROWS - 1) * (nymax - ymin) / (ymax - ymin));
            for (int col = minXbox; col <= maxXbox; col++) {
                for (int row = minYbox; row <= maxYbox; row++) {
                    try {
                        data.get(new Cell(row, col)).add(node);
                    } catch (Exception e) {
                        //e.printStackTrace();
                        if (nxmin < xmin || nxmax > xmax) {
                            System.err.println("Xerr0r* - " + node.getId() + " - nxmin=" + nxmin + " this.xmin=" + xmin + " nxmax=" + nxmax + " this.xmax=" + xmax);
                        }
                        if (nymin < ymin || nymax > ymax) {
                            System.err.println("Yerr0r* - " + node.getId() + " - nymin=" + nymin + " this.ymin=" + ymin + " nymax=" + nymax + " this.ymax=" + ymax);
                        }
                    }
                }
            }
        }
    }

    private static class Cell {

        private final int row;
        private final int col;

        public Cell(int row, int col) {
            this.row = row;
            this.col = col;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Cell other = (Cell) obj;
            if (this.row != other.row) {
                return false;
            }
            if (this.col != other.col) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 11 * hash + this.row;
            hash = 11 * hash + this.col;
            return hash;
        }
    }
}
