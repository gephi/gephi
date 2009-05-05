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
package org.gephi.data.network.node;

import org.gephi.graph.api.NodeWrap;
import org.gephi.data.network.utils.avl.BackwardEdgeTree;
import org.gephi.data.network.utils.avl.DhnsEdgeTree;
import org.gephi.data.network.utils.avl.ForwardEdgeTree;
import org.gephi.data.network.edge.DhnsEdge;
import org.gephi.data.network.edge.PreEdge;
import org.gephi.data.network.edge.PreEdge.EdgeType;
import org.gephi.data.network.edge.VirtualEdge;
import org.gephi.data.network.mode.EdgeProcessing;
import org.gephi.data.network.node.treelist.PreNodeTreeList;
import org.gephi.data.network.node.treelist.PreNodeTreeList.AVLNode;
import org.gephi.data.network.potato.PotatoImpl;

import org.gephi.datastructure.avl.simple.AVLItem;

/**
 * Node of the tree. Maintained in a global order tree, the node is build on a <b>pre/post/size/level</b> pane.
 * The <b>pre</b> is the global number in the tree, the <b>size</b> the number of node's child and <b>level</b>
 * the level within the hierarchy. The post is equal to <code>pre-level+size</code> and speed up algorithms 
 * when performing skipping.
 * <p> 
 * To support the concept of view on a hierarchical graph the class also contains <code>enabled</code> and
 * <code>space</code>.
 * <p>
 * If the node contains physical edges, they are stored in AVL trees in this class. For edges linked to a
 * node with a higher <code>pre</code> number, they are stored in a {@link ForwardEdgeTree}. For edges linked
 * to a lower <code>pre</code> number they are stored in {@link BackwardEdgeTree}.
 * <p>
 * Virtual edges are set to {@link DhnsEdgeTree} as well and divided in <code>virtualEdgesIN</code> and
 * <code>virtualEdgesIN</code> trees.
 * 
 * @author Mathieu Bastian
 * @see PreNodeTreeList
 * @see EdgeProcessing
 */
public class PreNode implements AVLItem, NodeWrap {

    private static int IDGen = 0;
    private int ID;

    //Tree Structure
    public int pre;
    public int size;
    public PreNode parent;
    public int level;
    public int post;
    private boolean enabled;
    public AVLNode avlNode;

    //Physical Edges
    private ForwardEdgeTree forwardEdges;
    private BackwardEdgeTree backwardEdges;

    //Virtual Edges
    private DhnsEdgeTree dhnsEdgeTreeIN;
    private DhnsEdgeTree dhnsEdgeTreeOUT;

    //Data content
    private NodeImpl node;
    private PotatoImpl potato;

    //Computing utils
    public int preTrace = -1;
    public int preTraceType = 0;
    public VirtualEdge lastEdge;

    public PreNode(int pre, int size, int level, PreNode parent) {
        this.ID = PreNode.IDGen++;
        this.pre = pre;
        this.size = size;
        this.level = level;
        this.parent = parent;
        this.post = pre - level + size;

        forwardEdges = new ForwardEdgeTree();
        backwardEdges = new BackwardEdgeTree();

        dhnsEdgeTreeIN = new DhnsEdgeTree(this);
        dhnsEdgeTreeOUT = new DhnsEdgeTree(this);
    }

    public int getPost() {
        this.post = pre - level + size;
        return post;
    }

    public String toString() {
        return "" + pre;
    }

    public void reinitTrace() {
        preTrace = -1;
        preTraceType = 0;
        lastEdge = null;
    }

    public int getPre() {
        return avlNode.getIndex();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public DhnsEdge getVirtualEdge(PreEdge physicalEdge, int forwardPre) {
        if (physicalEdge.edgeType == EdgeType.IN) {
            return dhnsEdgeTreeIN.getItem(forwardPre);
        } else {
            return dhnsEdgeTreeOUT.getItem(forwardPre);
        }
    }

    public void removeVirtualEdge(VirtualEdge edge) {
        if (edge.getPreNodeFrom() == this) {
            dhnsEdgeTreeIN.remove(edge);
        } else {
            dhnsEdgeTreeOUT.remove(edge);
        }
    }

    public void addForwardEdge(PreEdge edge) {
        forwardEdges.add(edge);
    }

    public void removeForwardEdge(PreEdge edge) {
        forwardEdges.remove(edge);
    }

    public void addBackwardEdge(PreEdge edge) {
        backwardEdges.add(edge);
    }

    public void removeBackwardEdge(PreEdge edge) {
        backwardEdges.remove(edge);
    }

    public boolean isLeaf() {
        return size == 0;
    }

    public ForwardEdgeTree getForwardEdges() {
        return forwardEdges;
    }

    public int countForwardEdges() {
        return forwardEdges.getCount();
    }

    public int countBackwardEdges() {
        return backwardEdges.getCount();
    }

    public void clearDhnsEdges() {
        dhnsEdgeTreeIN.clear();
        dhnsEdgeTreeOUT.clear();
    }

    public void clearDhnsEdgesIN() {
        dhnsEdgeTreeIN.clear();
    }

    public void clearDhnsEdgesOUT() {
        dhnsEdgeTreeOUT.clear();
    }

    public void addDhnsEdgeIN(DhnsEdge edge) {
        dhnsEdgeTreeIN.add(edge);
    }

    public void addDhnsEdgeOUT(DhnsEdge edge) {
        dhnsEdgeTreeOUT.add(edge);
    }

    public void removeDhnsEdgeIN(DhnsEdge edge) {
        dhnsEdgeTreeIN.remove(edge);
    }

    public void removeDhnsEdgeOUT(DhnsEdge edge) {
        dhnsEdgeTreeOUT.remove(edge);
    }

    public int countDhnsEdgeIN() {
        return dhnsEdgeTreeIN.getCount();
    }

    public int countDhnsEdgeOUT() {
        return dhnsEdgeTreeOUT.getCount();
    }

    public DhnsEdgeTree getDhnsEdgesIN() {
        return dhnsEdgeTreeIN;
    }

    public DhnsEdgeTree getDhnsEdgesOUT() {
        return dhnsEdgeTreeOUT;
    }

    @Override
    public int getNumber() {
        return getPre();
    }

    public BackwardEdgeTree getBackwardEdges() {
        return backwardEdges;
    }

    public NodeImpl initNodeInstance() {
        node = new NodeImpl();
        node.setPreNode(this);
        return node;
    }

    public NodeImpl getNode() {
        if (node == null) {
            initNodeInstance();
        }
        return node;
    }

    public void setNode(NodeImpl node) {
        this.node = node;
        node.setPreNode(this);
    }

    public PotatoImpl getPotato() {
        return potato;
    }

    public void setPotato(PotatoImpl potato) {
        this.potato = potato;
    }

    public void touchPotatoes() {
        if (parent.getPotato() != null) {
            parent.potato.updatePotatoHierarchy();
        }
    }

    public int getID() {
        return ID;
    }
}
