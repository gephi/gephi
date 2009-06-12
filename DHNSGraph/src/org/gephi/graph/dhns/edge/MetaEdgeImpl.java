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

import org.gephi.graph.api.Edge;
import org.gephi.graph.dhns.node.PreNode;
import org.gephi.graph.api.MetaEdge;
import org.gephi.graph.dhns.node.utils.avl.EdgeTree;

/**
 * Meta edge implementation. Edge between upper activated clusters in hierarchy. Contains lower edges.
 *
 * @author Mathieu Bastian
 */
public class MetaEdgeImpl extends AbstractEdge implements MetaEdge {

    private EdgeTree edges;

    public MetaEdgeImpl(int ID, PreNode source, PreNode target) {
        super(ID, source, target);
        this.edges = new EdgeTree();
    }

    public void addEdge(AbstractEdge edge) {
        if(edges.add(edge)) {
            weight += edge.getWeight();
        }
    }

    public void removeEdge(AbstractEdge edge) {
        if(edges.remove(edge)) {
            weight -= edge.getWeight();
        }
    }

    @Override
    public AbstractEdge getUndirected() {
        if(source==target) {
            return this;
        }
        AbstractEdge mutual = source.getMetaEdgesInTree().getItem(target.getNumber());
        if(mutual!=null && mutual.getId() < ID) {
            return mutual;
        }
        return this;
    }


    public Iterable<AbstractEdge> getEdges()
    {
        return edges;
    }

    public boolean isEmpty() {
        return edges.getCount() == 0;
    }

    @Override
    public boolean isMetaEdge() {
        return true;
    }
}
