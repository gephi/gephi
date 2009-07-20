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
import org.gephi.graph.api.Group;
import org.gephi.graph.api.GroupData;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.NodeData;
import org.gephi.graph.dhns.core.DurableTreeList.DurableAVLNode;
import org.gephi.graph.dhns.utils.avl.EdgeOppositeTree;
import org.gephi.graph.dhns.utils.avl.MetaEdgeTree;
import org.gephi.graph.dhns.utils.avl.ViewAVLTree;
import org.gephi.graph.dhns.view.View;

/**
 * Implementation of node with default behaviour.
 *
 * @author Mathieu Bastian
 */
public abstract class AbstractNode implements Node, Group, AVLItem {

    //Structure properties
    protected boolean visible = true;
    //Tree Structure
    public int pre;
    public int size;
    public AbstractNode parent;
    public int level;
    public int post;
    public DurableAVLNode avlNode;

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    //Structure methods
    public int getPost() {
        this.post = pre - level + size;
        return post;
    }

    public int getPre() {
        return avlNode.getIndex();
    }

    //Abstract
    public abstract void addView(View view, boolean enabled);

    public abstract void removeView(View view);

    public abstract boolean isInView(View view);

    public abstract ViewAVLTree getViews();

    public abstract boolean isEnabled(View view);

    public abstract void setEnabled(View view, boolean enabled);

    public abstract MetaEdgeTree getMetaEdgesOutTree(View view);

    public abstract MetaEdgeTree getMetaEdgesInTree(View view);

    public abstract void clearMetaEdges();

    public abstract EdgeOppositeTree getEdgesOutTree();

    public abstract EdgeOppositeTree getEdgesInTree();

    public abstract boolean isValid();

    public abstract boolean isClone();

    public abstract int getId();

    public abstract NodeData getNodeData();

    public abstract GroupData getGroupData();

    public abstract boolean hasAttributes();

    public abstract void setAttributes(Attributes attributes);

    public abstract PreNode getOriginalNode();
}