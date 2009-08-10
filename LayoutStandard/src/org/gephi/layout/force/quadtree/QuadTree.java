/*
Copyright 2008-2009 Gephi
Authors : Helder Suzuki <heldersuzuki@gephi.org>
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
package org.gephi.layout.force.quadtree;

import java.util.ArrayList;
import java.util.List;
import org.gephi.graph.api.ClusteredGraph;
import org.gephi.graph.api.ClusteredUndirectedGraph;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.Spatial;

/**
 * @author Helder Suzuki <heldersuzuki@gephi.org>
 */
public class QuadTree implements Spatial {

    private float posX;
    private float posY;
    private float size;
    private float centerMassX;  // X and Y position of the center of mass
    private float centerMassY;
    private int mass;  // Mass of this tree (the number of nodes it contains)
    private int maxLevel;
    private AddBehaviour add;
    private List<QuadTree> children;
    private boolean isLeaf;
    public static final float eps = (float) 1e-6;

    public static QuadTree buildTree(ClusteredGraph graph, int maxLevel) {
        float minX = Float.POSITIVE_INFINITY;
        float maxX = Float.NEGATIVE_INFINITY;
        float minY = Float.POSITIVE_INFINITY;
        float maxY = Float.NEGATIVE_INFINITY;

        for (Node node : graph.getTopNodes()) {
            minX = Math.min(minX, node.getNodeData().x());
            maxX = Math.max(maxX, node.getNodeData().x());
            minY = Math.min(minY, node.getNodeData().y());
            maxY = Math.max(maxY, node.getNodeData().y());
        }

        float size = Math.max(maxY - minY, maxX - minX);
        QuadTree tree = new QuadTree(minX, minY, size, maxLevel);
        for (Node node : graph.getTopNodes()) {
            tree.addNode(node.getNodeData());
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

    public float size() {
        return size;
    }

    private void divideTree() {
        float childSize = size / 2;

        children = new ArrayList<QuadTree>();
        children.add(new QuadTree(posX + childSize, posY + childSize,
                                  childSize, maxLevel - 1));
        children.add(new QuadTree(posX, posY + childSize,
                                  childSize, maxLevel - 1));
        children.add(new QuadTree(posX, posY, childSize, maxLevel - 1));
        children.add(new QuadTree(posX + childSize, posY,
                                  childSize, maxLevel - 1));

        isLeaf = false;
    }

    private boolean addToChildren(Spatial node) {
        for (QuadTree q : children) {
            if (q.addNode(node)) {
                return true;
            }
        }
        return false;
    }

    private void assimilateNode(Spatial node) {
        centerMassX = (mass * centerMassX + node.x()) / (mass + 1);
        centerMassY = (mass * centerMassY + node.y()) / (mass + 1);
        mass++;
    }

    public Iterable<QuadTree> getChildren() {
        return children;
    }

    public float x() {
        return centerMassX;
    }

    public float y() {
        return centerMassY;
    }

    public int mass() {
        return mass;
    }

    public float z() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean addNode(Spatial node) {
        if (posX <= node.x() && node.x() <= posX + size &&
            posY <= node.y() && node.y() <= posY + size) {
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

    class FirstAdd implements AddBehaviour {

        public boolean addNode(Spatial node) {
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

        public boolean addNode(Spatial node) {
            divideTree();
            add = new RootAdd();
            /* This QuadTree represents one node, add it to a child accordingly
             */
            addToChildren(QuadTree.this);
            return add.addNode(node);
        }
    }

    class LeafAdd implements AddBehaviour {

        public boolean addNode(Spatial node) {
            assimilateNode(node);
            return true;
        }
    }

    class RootAdd implements AddBehaviour {

        public boolean addNode(Spatial node) {
            assimilateNode(node);
            return addToChildren(node);
        }
    }
}

interface AddBehaviour {

    public boolean addNode(Spatial node);
}