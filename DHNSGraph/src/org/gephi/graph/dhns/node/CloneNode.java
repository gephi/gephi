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

import org.gephi.graph.api.Attributes;
import org.gephi.graph.api.GroupData;
import org.gephi.graph.api.NodeData;
import org.gephi.graph.dhns.utils.avl.EdgeOppositeTree;
import org.gephi.graph.dhns.utils.avl.MetaEdgeTree;
import org.gephi.graph.dhns.utils.avl.ViewAVLTree;
import org.gephi.graph.dhns.view.View;

/**
 * Virtual clone of a PreNode to represents a node in two different groups.
 *
 * @author Mathieu Bastian
 */
public class CloneNode extends AbstractNode {

    private PreNode preNode;
    protected CloneNode next;

    public CloneNode(AbstractNode absNode) {
        if (absNode.isClone()) {
            CloneNode cn = (CloneNode) absNode;
            this.preNode = cn.getPreNode();
        } else {
            this.preNode = (PreNode) absNode;
        }
        this.preNode.addClone(this);
    }

    @Override
    public boolean isEnabled(View view) {
        return preNode.isEnabled(view);
    }

    @Override
    public void setEnabled(View view, boolean enabled) {
        preNode.setEnabled(view, enabled);
    }

    @Override
    public void addView(View view, boolean enabled) {
        preNode.addView(view, enabled);
    }

    @Override
    public void removeView(View view) {
        preNode.removeView(view);
    }

    @Override
    public boolean isInView(View view) {
        return preNode.isInView(view);
    }

    @Override
    public ViewAVLTree getViews() {
        return preNode.getViews();
    }

    @Override
    public MetaEdgeTree getMetaEdgesOutTree(View view) {
        return preNode.getMetaEdgesOutTree(view);
    }

    @Override
    public MetaEdgeTree getMetaEdgesInTree(View view) {
        return preNode.getMetaEdgesInTree(view);
    }

    @Override
    public void clearMetaEdges() {
        preNode.clearMetaEdges();
    }

    @Override
    public EdgeOppositeTree getEdgesOutTree() {
        return preNode.getEdgesOutTree();
    }

    @Override
    public EdgeOppositeTree getEdgesInTree() {
        return preNode.getEdgesInTree();
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public boolean isClone() {
        return true;
    }

    @Override
    public int getId() {
        return preNode.ID;
    }

    @Override
    public NodeData getNodeData() {
        return preNode.nodeData;
    }

    @Override
    public GroupData getGroupData() {
        return preNode.nodeData;
    }

    @Override
    public boolean hasAttributes() {
        return preNode.hasAttributes();
    }

    @Override
    public void setAttributes(Attributes attributes) {
        preNode.setAttributes(attributes);
    }

    public int getNumber() {
        return preNode.ID;
    }

    public PreNode getPreNode() {
        return preNode;
    }

    public CloneNode getNext() {
        return next;
    }

    @Override
    public PreNode getOriginalNode() {
        return preNode;
    }

    @Override
    public String toString() {
        return "" + getPre();
    }
}
