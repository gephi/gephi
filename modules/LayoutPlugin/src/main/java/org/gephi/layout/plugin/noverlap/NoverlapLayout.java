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

package org.gephi.layout.plugin.noverlap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.NodeIterable;
import org.gephi.layout.plugin.AbstractLayout;
import org.gephi.layout.spi.Layout;
import org.gephi.layout.spi.LayoutBuilder;
import org.gephi.layout.spi.LayoutProperty;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.ProgressTicket;
import org.openide.util.Exceptions;

/**
 * @author Mathieu Jacomy
 */
public class NoverlapLayout extends AbstractLayout implements Layout, LongTask {

    protected boolean cancel;
    protected Graph graph;
    private double speed;
    private double ratio;
    private double margin;
    private double xmin;
    private double xmax;
    private double ymin;
    private double ymax;

    public NoverlapLayout(LayoutBuilder layoutBuilder) {
        super(layoutBuilder);
    }

    @Override
    public void initAlgo() {
        this.graph = graphModel.getGraphVisible();
        setConverged(false);
        cancel = false;
    }

    @Override
    public void goAlgo() {
        setConverged(true);
        this.graph = graphModel.getGraphVisible();
        graph.readLock();
        try {
            //Reset Layout Data
            for (Node n : graph.getNodes()) {
                if (n.getLayoutData() == null || !(n.getLayoutData() instanceof NoverlapLayoutData)) {
                    n.setLayoutData(new NoverlapLayoutData());
                }
                NoverlapLayoutData layoutData = n.getLayoutData();
                layoutData.neighbours.clear();
                layoutData.dx = 0;
                layoutData.dy = 0;
            }

            // Get xmin, xmax, ymin, ymax
            this.xmin = Double.MAX_VALUE;
            this.xmax = Double.MIN_VALUE;
            this.ymin = Double.MAX_VALUE;
            this.ymax = Double.MIN_VALUE;

            for (Node n : graph.getNodes()) {
                float x = n.x();
                float y = n.y();
                float radius = n.size();

                // Get the rectangle occupied by the node
                double nxmin = x - (radius * ratio + margin);
                double nxmax = x + (radius * ratio + margin);
                double nymin = y - (radius * ratio + margin);
                double nymax = y + (radius * ratio + margin);

                // Update global boundaries
                this.xmin = Math.min(this.xmin, nxmin);
                this.xmax = Math.max(this.xmax, nxmax);
                this.ymin = Math.min(this.ymin, nymin);
                this.ymax = Math.max(this.ymax, nymax);
            }

            // Secure the bounds
            double xwidth = this.xmax - this.xmin;
            double yheight = this.ymax - this.ymin;
            double xcenter = (this.xmin + this.xmax) / 2;
            double ycenter = (this.ymin + this.ymax) / 2;
            double securityRatio = 1.05;
            this.xmin = xcenter - securityRatio * xwidth / 2;
            this.xmax = xcenter + securityRatio * xwidth / 2;
            this.ymin = ycenter - securityRatio * yheight / 2;
            this.ymax = ycenter + securityRatio * yheight / 2;

            // Estimate necessary number of columns
            double area = xwidth*yheight;
            double areaPerNode = area / graph.getNodeCount();
            // Explanation for the weird line below:
            // If nodes were equally distributed in space, we would like to have grid cells that contain
            // a small number of nodes, maybe about a dozen or something. We would like to balance the number of
            // nodes per cell with the number of cells.
            // But we basically know that in practice, the nodes are very concentrated. Because of this, the practical
            // density is probably much higher than the average density. So we should lower the grid size consequently.
            // In the end, we can eyeball this as targeting the same order of magnitude, hence the formula below.
            // I keep it as such to make explicit that there is a ratio, even if we keep it to 1.
            double targetAreaPerNode = 1. * areaPerNode;
            double targetGridSize = Math.sqrt(targetAreaPerNode);
            int columns_count = (int) Math.ceil(Math.min(xwidth, yheight) / targetGridSize);
            System.out.println("Columns count: "+columns_count);
            
            // Create the grid where neighborhood (potential overlap) is computed
            SpatialGrid grid = new SpatialGrid(columns_count);

            // Put nodes in their boxes
            for (Node n : graph.getNodes()) {
                grid.add(n);
            }

            // Now we have cells with nodes in it. Nodes that are in the same cell are tested for repulsion.
            // But they are not repulsed several times, even if they are in several cells...
            // So we build a relation of proximity between nodes.
            // Build proximity pairs
            Map<String, Node[]> proximities = new HashMap<>();
            for (int row = 0; row < grid.countRows() && !cancel; row++) {
                for (int col = 0; col < grid.countColumns() && !cancel; col++) {
                    for (Node n : grid.getContent(row, col)) {
                        int nid = n.getStoreId();
                        for (Node n2 : grid.getContent(row, col)) {
                            int n2id = n2.getStoreId();
                            if (n2id < nid) {
                                Node[] nodePair = new Node[2];
                                nodePair[0] = n;
                                nodePair[1] = n2;

                                proximities.put(nid+"|"+n2id, nodePair);
                            }
                        }
                    }
                }
            }

            // Move colliding nodes as registered in proximity
            int collisions = 0;
            for (Map.Entry<String, Node[]> entry : proximities.entrySet()) {
                Node[] nodePair = entry.getValue();
                Node n1 = nodePair[0];
                Node n2 = nodePair[1];

                float n1x = n1.x();
                float n1y = n1.y();
                float n2x = n2.x();
                float n2y = n2.y();
                float n1radius = (float) (n1.size() * ratio + margin);
                float n2radius = (float) (n2.size() * ratio + margin);
                float n1Weight = n1radius * n1radius;
                float n2Weight = n2radius * n2radius;
                float n1Ratio = n2Weight / (n1Weight + n2Weight);
                float n2Ratio = 1-n1Ratio;

                // Check sizes
                double xDist = n2x - n1x;
                double yDist = n2y - n1y;
                double angle = Math.atan2(yDist, xDist);
                double dist = Math.sqrt(xDist * xDist + yDist * yDist);
                double overlap = n1radius + n2radius - dist;
                boolean collision = overlap > 0;
                if (collision) {
                    collisions += 1;
                    double xOverlap = 1.1 * overlap * Math.cos(angle);
                    double yOverlap = 1.1 * overlap * Math.sin(angle);

                    // n1 and n2 move each other of half the overlap
                    NoverlapLayoutData n1ldata = n1.getLayoutData();
                    n1ldata.dx -= n1Ratio * xOverlap;
                    n1ldata.dy -= n1Ratio * yOverlap;

                    NoverlapLayoutData n2ldata = n2.getLayoutData();
                    n2ldata.dx += n2Ratio * xOverlap;
                    n2ldata.dy += n2Ratio * yOverlap;
                }

                if (cancel) {
                    break;
                }
            }

            System.out.println(collisions + " collisions detected.");
            if (collisions > 0) {
                setConverged(false);
            }

            for (Node n : graph.getNodes()) {
                NoverlapLayoutData layoutData = n.getLayoutData();
                if (!n.isFixed()) {
                    float x = n.x() + layoutData.dx;
                    float y = n.y() + layoutData.dy;
                    n.setX(x);
                    n.setY(y);
                }
            }
        } finally {
            graph.readUnlockAll();
        }
    }

    @Override
    public void endAlgo() {
        graph.readLock();
        try {
            for (Node n : graph.getNodes()) {
                n.setLayoutData(null);
            }
        } finally {
            graph.readUnlockAll();
        }
    }

    @Override
    public LayoutProperty[] getProperties() {
        List<LayoutProperty> properties = new ArrayList<>();
        final String NOVERLAP_CATEGORY = "Noverlap";
        try {
            properties.add(LayoutProperty.createProperty(
                this, Double.class, "speed", NOVERLAP_CATEGORY, "speed", "getSpeed", "setSpeed"));
        } catch (Exception e) {
            Exceptions.printStackTrace(e);
        }
        try {
            properties.add(LayoutProperty.createProperty(
                this, Double.class, "ratio", NOVERLAP_CATEGORY, "ratio", "getRatio", "setRatio"));
        } catch (Exception e) {
            Exceptions.printStackTrace(e);
        }
        try {
            properties.add(LayoutProperty.createProperty(
                this, Double.class, "margin", NOVERLAP_CATEGORY, "margin", "getMargin", "setMargin"));
        } catch (Exception e) {
            Exceptions.printStackTrace(e);
        }
        return properties.toArray(new LayoutProperty[0]);
    }

    @Override
    public void resetPropertiesValues() {
        setSpeed(3.);
        setRatio(1.2);
        setMargin(5.);
    }

    public Double getSpeed() {
        return speed;
    }

    public void setSpeed(Double speed) {
        this.speed = speed;
    }

    public Double getRatio() {
        return ratio;
    }

    public void setRatio(Double ratio) {
        this.ratio = ratio;
    }

    public Double getMargin() {
        return margin;
    }

    public void setMargin(Double margin) {
        this.margin = margin;
    }

    @Override
    public boolean cancel() {
        cancel = true;
        return cancel;
    }

    @Override
    public void setProgressTicket(ProgressTicket progressTicket) {
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
            return this.col == other.col;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 11 * hash + this.row;
            hash = 11 * hash + this.col;
            return hash;
        }
    }

    private class SpatialGrid {

        //Param
        private int COLUMNS_ROWS = 20;
        //Data
        private final Map<Cell, List<Node>> data = new HashMap<>();

        public SpatialGrid() {
            for (int row = 0; row < COLUMNS_ROWS; row++) {
                for (int col = 0; col < COLUMNS_ROWS; col++) {
                    List<Node> localnodes = new ArrayList<>();
                    data.put(new Cell(row, col), localnodes);
                }
            }
        }

        public SpatialGrid(int columns_rows) {
            COLUMNS_ROWS = columns_rows;
            for (int row = 0; row < COLUMNS_ROWS; row++) {
                for (int col = 0; col < COLUMNS_ROWS; col++) {
                    List<Node> localnodes = new ArrayList<>();
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
            float x = node.x();
            float y = node.y();
            float radius = (float) (node.size() * ratio + margin);

            // Get the rectangle occupied by the node
            double nxmin = x - (radius * ratio + margin);
            double nxmax = x + (radius * ratio + margin);
            double nymin = y - (radius * ratio + margin);
            double nymax = y + (radius * ratio + margin);

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
                        //Exceptions.printStackTrace(e);
                        if (nxmin < xmin || nxmax > xmax) {
                        }
                        if (nymin < ymin || nymax > ymax) {
                        }
                    }
                }
            }
        }
    }
}
