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

import org.gephi.utils.collection.avl.AVLItem;
import org.gephi.graph.api.Group;
import org.gephi.graph.api.GroupData;
import org.gephi.graph.api.Node;
import org.gephi.graph.dhns.core.DurableTreeList.DurableAVLNode;
import org.gephi.graph.dhns.utils.avl.EdgeOppositeTree;
import org.gephi.graph.dhns.utils.avl.MetaEdgeTree;

/**
 * Implementation of node with default behaviour.
 *
 * @author Mathieu Bastian
 */
public class AbstractNode implements Node, Group, AVLItem {

    //For all views
    protected final NodeDataImpl nodeData;
    //Particular
    protected final int viewId;
    public AbstractNode parent;
    public int pre;
    public int size;
    public int level;
    public int post;
    public DurableAVLNode avlNode;
    protected boolean enabled;
    private EdgeOppositeTree edgesOutTree;
    private EdgeOppositeTree edgesInTree;
    private MetaEdgeTree metaEdgesOutTree;
    private MetaEdgeTree metaEdgesInTree;
    //Counting
    private int enabledInDegree;
    private int enabledOutDegree;
    private int enabledMutualDegree;

    public AbstractNode(int ID, int viewId) {
        this(viewId, new NodeDataImpl(ID, null), 0, 0, 0, null);
        nodeData.getNodes().add(this);
    }

    public AbstractNode(NodeDataImpl nodeData, int viewId) {
        this(viewId, nodeData, 0, 0, 0, null);
        nodeData.getNodes().add(this);
    }

    public AbstractNode(int ID, int viewId, int pre, int size, int level, AbstractNode parent) {
        this(viewId, new NodeDataImpl(ID, null), pre, size, level, parent);
        nodeData.getNodes().add(this);
    }

    public AbstractNode(NodeDataImpl nodeData, int viewId, int pre, int size, int level, AbstractNode parent) {
        this(viewId, nodeData, pre, size, level, parent);
        nodeData.getNodes().add(this);
    }

    private AbstractNode(int viewId, NodeDataImpl nodeData, int pre, int size, int level, AbstractNode parent) {
        this.viewId = viewId;
        this.parent = parent;
        this.pre = pre;
        this.size = size;
        this.level = level;
        this.post = pre - level + size;
        this.nodeData = nodeData;
        edgesOutTree = new EdgeOppositeTree(this);
        edgesInTree = new EdgeOppositeTree(this);
        metaEdgesOutTree = new MetaEdgeTree(this);
        metaEdgesInTree = new MetaEdgeTree(this);
    }

    public int getViewId() {
        return viewId;
    }

    public int getPre() {
        return avlNode.getIndex();
    }

    public int getPost() {
        this.post = pre - level + size;
        return post;
    }

    @Override
    public int getId() {
        return nodeData.ID;
    }

    @Override
    public int getNumber() {
        return nodeData.ID;
    }

    @Override
    public NodeDataImpl getNodeData() {
        return nodeData;
    }

    @Override
    public GroupData getGroupData() {
        return nodeData;
    }

    public EdgeOppositeTree getEdgesInTree() {
        return edgesInTree;
    }

    public void setEdgesInTree(EdgeOppositeTree edgesInTree) {
        this.edgesInTree = edgesInTree;
    }

    public EdgeOppositeTree getEdgesOutTree() {
        return edgesOutTree;
    }

    public void setEdgesOutTree(EdgeOppositeTree edgesOutTree) {
        this.edgesOutTree = edgesOutTree;
    }

    public MetaEdgeTree getMetaEdgesInTree() {
        return metaEdgesInTree;
    }

    public void setMetaEdgesInTree(MetaEdgeTree metaEdgesInTree) {
        this.metaEdgesInTree = metaEdgesInTree;
    }

    public MetaEdgeTree getMetaEdgesOutTree() {
        return metaEdgesOutTree;
    }

    public void setMetaEdgesOutTree(MetaEdgeTree metaEdgesOutTree) {
        this.metaEdgesOutTree = metaEdgesOutTree;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void clearMetaEdges() {
        metaEdgesOutTree = new MetaEdgeTree(this);
        metaEdgesInTree = new MetaEdgeTree(this);
    }

    public boolean isValid(int viewId) {
        return avlNode != null && this.viewId == viewId;
    }

    public AbstractNode getInView(int viewId) {
        if (avlNode == null) {
            return null;
        }
        if (this.viewId == viewId) {
            return this;
        }
        return nodeData.getNodes().get(viewId);
    }

    public void removeFromView(int viewId) {
        nodeData.getNodes().remove(viewId);
    }

    public int countInViews() {
        return nodeData.getNodes().getCount();
    }

    public void incEnabledInDegree() {
        enabledInDegree++;
    }

    public void incEnabledOutDegree() {
        enabledOutDegree++;
    }

    public void incEnabledMutualDegree() {
        enabledMutualDegree++;
    }

    public void decEnabledInDegree() {
        enabledInDegree--;
    }

    public void decEnabledOutDegree() {
        enabledOutDegree--;
    }

    public void decEnabledMutualDegree() {
        enabledMutualDegree--;
    }

    public int getEnabledInDegree() {
        return enabledInDegree;
    }

    public void setEnabledInDegree(int enabledInDegree) {
        this.enabledInDegree = enabledInDegree;
    }

    public int getEnabledMutualDegree() {
        return enabledMutualDegree;
    }

    public void setEnabledMutualDegree(int enabledMutualDegree) {
        this.enabledMutualDegree = enabledMutualDegree;
    }

    public int getEnabledOutDegree() {
        return enabledOutDegree;
    }

    public void setEnabledOutDegree(int enabledOutDegree) {
        this.enabledOutDegree = enabledOutDegree;
    }

    @Override
    public String toString() {
        return nodeData.getId();
    }
}
