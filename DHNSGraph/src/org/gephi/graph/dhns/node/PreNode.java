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
package org.gephi.graph.dhns.node;

import org.gephi.datastructure.avl.simple.AVLItem;
import org.gephi.graph.api.Attributes;
import org.gephi.graph.api.GroupData;
import org.gephi.graph.api.NodeData;
import org.gephi.graph.dhns.core.DurableTreeList.DurableAVLNode;
import org.gephi.graph.dhns.utils.avl.EdgeOppositeTree;
import org.gephi.graph.dhns.utils.avl.MetaEdgeTree;
import org.gephi.graph.dhns.utils.avl.MetaEdgesAccessor;
import org.gephi.graph.dhns.utils.avl.ViewAVLTree;
import org.gephi.graph.dhns.view.View;

/**
 * Node of the tree. Maintained in a global order tree, the node is build on a <b>pre/post/size/level</b> pane.
 * The <b>pre</b> is the global number in the tree, the <b>size</b> the number of node's descendants and <b>level</b>
 * the level within the hierarchy (greater when deeper). The post is equal to <code>pre-level+size</code>
 * and speed up algorithms when performing skipping. It also directly contains a reference on
 * his {@link DurableAVLNode}.
 * <p> 
 * To support the concept of view on a hierarchical graph the class also contains <code>enabled</code>.
 * <p>
 * If the node contains edges, they are stored in AVL trees in this class. Meta edges are stored and
 * maintained in trees as well.
 * 
 * @author Mathieu Bastian
 */
public class PreNode extends AbstractNode implements AVLItem {

    //Properties
    protected final int ID;
    protected NodeDataImpl nodeData;
    protected ViewAVLTree viewTree;
    //Edges
    private EdgeOppositeTree edgesOutTree;
    private EdgeOppositeTree edgesInTree;
    private MetaEdgesAccessor metaEdgeAccessor;

    //Clone
    private CloneNode clones;

    public PreNode(int ID, int pre, int size, int level, AbstractNode parent) {
        this.pre = pre;
        this.size = size;
        this.level = level;
        this.parent = parent;
        this.post = pre - level + size;

        edgesOutTree = new EdgeOppositeTree(this);
        edgesInTree = new EdgeOppositeTree(this);
        metaEdgeAccessor = new MetaEdgesAccessor();
        viewTree = new ViewAVLTree();

        this.ID = ID;
        nodeData = new NodeDataImpl(this);
    }

    @Override
    public String toString() {
        return "" + pre;
    }

    public MetaEdgeTree getMetaEdgesOutTree(View view) {
        return metaEdgeAccessor.getMetaEdgeOutTree(view);
    }

    public MetaEdgeTree getMetaEdgesInTree(View view) {
        return metaEdgeAccessor.getMetaEdgeInTree(view);
    }

    @Override
    public void clearMetaEdges() {
        metaEdgeAccessor = new MetaEdgesAccessor();
    }

    public EdgeOppositeTree getEdgesOutTree() {
        return edgesOutTree;
    }

    public EdgeOppositeTree getEdgesInTree() {
        return edgesInTree;
    }

    public boolean isValid() {
        return avlNode != null;
    }

    @Override
    public void addView(View view, boolean enabled) {
        if (viewTree.add(view, enabled)) {
            metaEdgeAccessor.createMetaEdgeTree(this, view);
        }
    }

    @Override
    public void removeView(View view) {
        if (viewTree.remove(view)) {
            metaEdgeAccessor.removeMetaEdgeTree(view);
        }
    }

    @Override
    public boolean isInView(View view) {
        return viewTree.contains(view);
    }

    @Override
    public ViewAVLTree getViews() {
        return viewTree;
    }

    public void setEdgesInTree(EdgeOppositeTree edgesInTree) {
        this.edgesInTree = edgesInTree;
    }

    public void setEdgesOutTree(EdgeOppositeTree edgesOutTree) {
        this.edgesOutTree = edgesOutTree;
    }

    @Override
    public int getNumber() {
        return ID;
    }

    @Override
    public int getId() {
        return ID;
    }

    @Override
    public boolean isClone() {
        return false;
    }

    @Override
    public NodeData getNodeData() {
        return nodeData;
    }

    @Override
    public GroupData getGroupData() {
        return nodeData;
    }

    @Override
    public boolean hasAttributes() {
        return nodeData.getAttributes() != null;
    }

    @Override
    public void setAttributes(Attributes attributes) {
        if (attributes != null) {
            nodeData.setAttributes(attributes);
        }
    }

    @Override
    public boolean isEnabled(View view) {
        return viewTree.isEnabled(view);
    }

    @Override
    public void setEnabled(View view, boolean enabled) {
        viewTree.setEnabled(view, enabled);
    }

    //CLone operations
    public void addClone(CloneNode clone) {
        clone.next = this.clones;
        this.clones = clone;
    }

    public void removeClone(CloneNode clone) {
        CloneNode c = this.clones;
        if (c == clone) {
            this.clones = c.next;
        } else if (c != null) {
            while (c.next != null) {
                if (c.next == clone) {
                    c.next = clone.next;
                    break;
                }
                c = c.next;
            }
        }
    }

    public CloneNode getClones() {
        return clones;
    }

    public int countClones() {
        int res = 0;
        CloneNode c = this.clones;
        while (c != null) {
            c = c.next;
            res++;
        }
        return res;
    }

    @Override
    public PreNode getOriginalNode() {
        return this;
    }

    public void transferToClones() {
        if (this.clones != null) {
            avlNode = clones.avlNode;
            pre = clones.pre;
            post = clones.post;
            size = clones.size;
            level = clones.level;
            parent = clones.parent;
            clones = clones.next;
        }
    }
}
