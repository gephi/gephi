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

import java.util.ArrayList;
import java.util.List;
import org.gephi.graph.api.HierarchicalGraph;
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

    public static QuadTree buildTree(HierarchicalGraph graph, int maxLevel) {
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