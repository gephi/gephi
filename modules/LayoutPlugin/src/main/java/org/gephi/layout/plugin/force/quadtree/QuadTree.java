/*
 Copyright 2008-2010 Gephi
 Authors : Helder Suzuki <heldersuzuki@gephi.org>
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
package org.gephi.layout.plugin.force.quadtree;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.ColumnIterable;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphView;
import org.gephi.graph.api.Interval;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.NodeProperties;
import org.gephi.graph.api.Table;
import org.gephi.graph.api.TextProperties;
import org.gephi.graph.spi.LayoutData;

/**
 * @author Helder Suzuki <heldersuzuki@gephi.org>
 */
public class QuadTree implements Node {

    private final float posX;
    private final float posY;
    private final float size;
    private float centerMassX;  // X and Y position of the center of mass
    private float centerMassY;
    private int mass;  // Mass of this tree (the number of nodes it contains)
    private final int maxLevel;
    private AddBehaviour add;
    private List<QuadTree> children;
    private boolean isLeaf;
    public static final float eps = (float) 1e-6;

    public static QuadTree buildTree(Graph graph, int maxLevel) {
        float minX = Float.POSITIVE_INFINITY;
        float maxX = Float.NEGATIVE_INFINITY;
        float minY = Float.POSITIVE_INFINITY;
        float maxY = Float.NEGATIVE_INFINITY;

        for (Node node : graph.getNodes()) {
            minX = Math.min(minX, node.x());
            maxX = Math.max(maxX, node.x());
            minY = Math.min(minY, node.y());
            maxY = Math.max(maxY, node.y());
        }

        float size = Math.max(maxY - minY, maxX - minX);
        QuadTree tree = new QuadTree(minX, minY, size, maxLevel);
        for (Node node : graph.getNodes()) {
            tree.addNode(node);
        }

        return tree;
    }

    public QuadTree(float posX, float posY, float size, int maxLevel) {
        this.posX = posX;
        this.posY = posY;
        this.size = size;
        this.maxLevel = maxLevel;
        this.isLeaf = true;
        mass = 0;
        add = new FirstAdd();
    }

    @Override
    public float size() {
        return size;
    }

    private void divideTree() {
        float childSize = size / 2;

        children = new ArrayList<>();
        children.add(new QuadTree(posX + childSize, posY + childSize,
                childSize, maxLevel - 1));
        children.add(new QuadTree(posX, posY + childSize,
                childSize, maxLevel - 1));
        children.add(new QuadTree(posX, posY, childSize, maxLevel - 1));
        children.add(new QuadTree(posX + childSize, posY,
                childSize, maxLevel - 1));

        isLeaf = false;
    }

    private boolean addToChildren(NodeProperties node) {
        for (QuadTree q : children) {
            if (q.addNode(node)) {
                return true;
            }
        }
        return false;
    }

    private void assimilateNode(NodeProperties node) {
        centerMassX = (mass * centerMassX + node.x()) / (mass + 1);
        centerMassY = (mass * centerMassY + node.y()) / (mass + 1);
        mass++;
    }

    public Iterable<QuadTree> getChildren() {
        return children;
    }

    @Override
    public float x() {
        return centerMassX;
    }

    @Override
    public float y() {
        return centerMassY;
    }

    public int mass() {
        return mass;
    }

    @Override
    public float z() {
        throw new UnsupportedOperationException("Not supported.");
    }

    public boolean addNode(NodeProperties node) {
        if (posX <= node.x() && node.x() <= posX + size
                && posY <= node.y() && node.y() <= posY + size) {
            return add.addNode(node);
        } else {
            return false;
        }
    }

    /**
     * @return the isLeaf
     */
    public boolean isIsLeaf() {
        return isLeaf;
    }

    @Override
    public float r() {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public float g() {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public float b() {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public int getRGBA() {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public Color getColor() {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public float alpha() {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public boolean isFixed() {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public <T extends LayoutData> T getLayoutData() {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public TextProperties getTextProperties() {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public int getStoreId() {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void setX(float x) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void setY(float y) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void setZ(float z) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void setPosition(float x, float y) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void setPosition(float x, float y, float z) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void setR(float r) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void setG(float g) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void setB(float b) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void setAlpha(float a) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void setColor(Color color) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void setSize(float size) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void setFixed(boolean fixed) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void setLayoutData(LayoutData layoutData) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public Object getId() {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public String getLabel() {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public Object getAttribute(String key) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public Object getAttribute(Column column) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public Object[] getAttributes() {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public Set<String> getAttributeKeys() {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public ColumnIterable getAttributeColumns() {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public Object removeAttribute(String key) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public Object removeAttribute(Column column) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void setLabel(String label) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void setAttribute(String key, Object value) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void setAttribute(Column column, Object value) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void setAttribute(String key, Object value, double timestamp) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void setAttribute(Column column, Object value, double timestamp) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public boolean addTimestamp(double timestamp) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public boolean removeTimestamp(double timestamp) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public double[] getTimestamps() {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void clearAttributes() {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public Object getAttribute(String key, double timestamp) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public Object getAttribute(Column column, double timestamp) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public Object getAttribute(String key, GraphView view) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public Object getAttribute(Column column, GraphView view) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public boolean hasTimestamp(double timestamp) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public Object getAttribute(String key, Interval interval) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public Object getAttribute(Column column, Interval interval) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public Iterable<Map.Entry> getAttributes(Column column) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public Object removeAttribute(String key, double timestamp) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public Object removeAttribute(Column column, double timestamp) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public Object removeAttribute(String key, Interval interval) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Object removeAttribute(Column column, Interval interval) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void setAttribute(String key, Object value, Interval interval) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void setAttribute(Column column, Object value, Interval interval) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public boolean addInterval(Interval interval) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public boolean removeInterval(Interval interval) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public boolean hasInterval(Interval interval) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public Interval[] getIntervals() {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public Table getTable() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    class FirstAdd implements AddBehaviour {

        @Override
        public boolean addNode(NodeProperties node) {
            mass = 1;
            centerMassX = node.x();
            centerMassY = node.y();

            if (maxLevel == 0) {
                add = new LeafAdd();
            } else {
                add = new SecondAdd();
            }

            return true;
        }
    }

    class SecondAdd implements AddBehaviour {

        @Override
        public boolean addNode(NodeProperties node) {
            divideTree();
            add = new RootAdd();
            /* This QuadTree represents one node, add it to a child accordingly
             */
            addToChildren(QuadTree.this);
            return add.addNode(node);
        }
    }

    class LeafAdd implements AddBehaviour {

        @Override
        public boolean addNode(NodeProperties node) {
            assimilateNode(node);
            return true;
        }
    }

    class RootAdd implements AddBehaviour {

        @Override
        public boolean addNode(NodeProperties node) {
            assimilateNode(node);
            return addToChildren(node);
        }
    }
}

interface AddBehaviour {

    public boolean addNode(NodeProperties node);
}
