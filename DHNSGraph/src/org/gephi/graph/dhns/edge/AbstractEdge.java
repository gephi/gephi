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
package org.gephi.graph.dhns.edge;

import org.gephi.utils.collection.avl.AVLItem;
import org.gephi.graph.api.Attributes;
import org.gephi.graph.api.Edge;
import org.gephi.graph.dhns.node.AbstractNode;

/**
 * Abstract edge with one source and one target.
 *
 * @author Mathieu Bastian
 */
public abstract class AbstractEdge implements Edge, AVLItem {

    protected final int ID;
    protected final AbstractNode source;
    protected final AbstractNode target;
    protected float weight = 1f;
    protected EdgeDataImpl edgeData;

    public AbstractEdge(int ID, AbstractNode source, AbstractNode target) {
        this.source = source;
        this.target = target;
        this.ID = ID;
        this.edgeData = new EdgeDataImpl(this);
    }

    public AbstractEdge(AbstractEdge edge, AbstractNode source, AbstractNode target) {
        this.source = source;
        this.target = target;
        this.ID = edge.ID;
        this.edgeData = edge.edgeData;
        this.weight = edge.weight;
    }

    public AbstractNode getSource() {
        return source;
    }

    public AbstractNode getTarget() {
        return target;
    }

    public AbstractNode getSource(int viewId) {
        return source.getInView(viewId);
    }

    public AbstractNode getTarget(int viewId) {
        return target.getInView(viewId);
    }

    public float getWeight() {
        return weight;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }

    public int getNumber() {
        return ID;
    }

    public EdgeDataImpl getEdgeData() {
        return edgeData;
    }

    public AbstractEdge getUndirected(int viewId) {
        if (source == target) {
            return this;
        }
        AbstractEdge mutual = getSource(viewId).getEdgesInTree().getItem(target.getNumber());
        if (mutual != null && mutual.getId() < ID) {
            return mutual;
        }
        return this;
    }

    public boolean isDirected() {
        return true;
    }

    public boolean isSelfLoop() {
        return source == target;
    }

    public boolean isValid(int viewId) {
        return source.isValid(viewId) && target.isValid(viewId);
    }

    public boolean isValid() {
        return source.avlNode != null && target.avlNode != null;
    }

    public boolean isMetaEdge() {
        return false;
    }

    public boolean isMixed() {
        return false;
    }

    public boolean hasAttributes() {
        return edgeData.getAttributes() != null;
    }

    public void setAttributes(Attributes attributes) {
        if (attributes != null) {
            edgeData.setAttributes(attributes);
        }
    }

    public int getId() {
        return ID;
    }

    @Override
    public String toString() {
        return source.getId() + "-" + target.getId();
    }
}
