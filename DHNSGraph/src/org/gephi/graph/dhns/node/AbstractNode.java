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
import org.gephi.graph.api.Node;
import org.gephi.graph.api.NodeData;
import org.gephi.graph.dhns.core.DurableTreeList.DurableAVLNode;
import org.gephi.graph.dhns.utils.avl.EdgeOppositeTree;
import org.gephi.graph.dhns.utils.avl.MetaEdgeTree;

/**
 * Implementation of node with default behaviour.
 *
 * @author Mathieu Bastian
 */
public abstract class AbstractNode implements Node, AVLItem {

    protected final int ID;
    protected NodeDataImpl nodeData;
    protected boolean visible = true;

    //Structure properties
    //Tree Structure
    public int pre;
    public int size;
    public AbstractNode parent;
    public int level;
    public int post;
    protected boolean enabled = true;
    public DurableAVLNode avlNode;

    public AbstractNode(int ID) {
        this.ID = ID;
        nodeData = new NodeDataImpl(this);
    }

    public int getNumber() {
        return ID;
    }

    public NodeData getNodeData() {
        return nodeData;
    }

    public boolean hasAttributes() {
        return nodeData.getAttributes() != null;
    }

    public void setAttributes(Attributes attributes) {
        if (attributes != null) {
            nodeData.setAttributes(attributes);
        }
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public int getId() {
        return ID;
    }

    //Structure methods
    public int getPost() {
        this.post = pre - level + size;
        return post;
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

    public abstract MetaEdgeTree getMetaEdgesOutTree();

    public abstract MetaEdgeTree getMetaEdgesInTree();

    public abstract EdgeOppositeTree getEdgesOutTree();

    public abstract EdgeOppositeTree getEdgesInTree();

    public abstract boolean isValid();

    public abstract boolean isClone();
}