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

import org.gephi.graph.api.MetaEdge;
import org.gephi.graph.dhns.node.AbstractNode;

/**
 * Meta edge implementation. Edge between upper activated clusters in hierarchy. Contains lower edges.
 *
 * @author Mathieu Bastian
 */
public class MetaEdgeImpl extends AbstractEdge implements MetaEdge {

    private int count = 0;
    private int directedCount = 0;

    public MetaEdgeImpl(int ID, AbstractNode source, AbstractNode target) {
        super(ID, source, target);
        this.weight = 0f;
    }

    public boolean addEdge(AbstractEdge edge) {
        if (edge.isDirected()) {
            directedCount++;
        }
        count++;
        return true;
    }

    public boolean removeEdge(AbstractEdge edge) {
        if (edge.isDirected()) {
            directedCount--;
        }
        count--;
        return true;
    }

    public boolean isEmpty() {
        return count == 0;
    }

    public int getCount() {
        return count;
    }

    @Override
    public boolean isDirected() {
        return directedCount > 0;
    }

    @Override
    public boolean isMetaEdge() {
        return true;
    }

    public AbstractEdge getUndirected() {
        if (source == target) {
            return this;
        }
        AbstractEdge mutual = source.getMetaEdgesInTree().getItem(target.getNumber());
        if (mutual != null && mutual.getId() < ID) {
            return mutual;
        }
        return this;
    }
}
