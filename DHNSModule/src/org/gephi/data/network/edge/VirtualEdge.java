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
package org.gephi.data.network.edge;

import org.gephi.data.network.node.PreNode;
import org.gephi.data.network.sight.SightImpl;
import org.gephi.datastructure.avl.simple.SimpleAVLTree;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.EdgeLayoutInterface;
import org.gephi.graph.api.Sight;

/**
 *
 * @author Mathieu Bastian
 */
public class VirtualEdge implements DhnsEdge {

    protected PreNode preNodeFrom;
    protected PreNode preNodeTo;
    protected float cardinal = 0f;
    protected SimpleAVLTree physicalEdges;
    protected EdgeImpl edge;
    protected SightImpl sight;

    public VirtualEdge(PreNode preNodeFrom, PreNode preNodeTo, SightImpl sight) {
        this.preNodeFrom = preNodeFrom;
        this.preNodeTo = preNodeTo;
        this.physicalEdges = new SimpleAVLTree();
        this.sight = sight;
    }

    public void addPhysicalEdge(PreEdge preEdge) {
        physicalEdges.add(preEdge);
        cardinal += preEdge.cardinal;
    }

    public void removePhysicalEdge(PreEdge preEdge) {
        physicalEdges.remove(preEdge);
        cardinal -= preEdge.cardinal;
    }

    public boolean isEmpty() {
        return physicalEdges.getCount() == 0;
    }

    @Override
    public PreNode getPreNodeFrom() {
        return preNodeFrom;
    }

    @Override
    public void setPreNodeFrom(PreNode preNodeFrom) {
        this.preNodeFrom = preNodeFrom;
    }

    @Override
    public PreNode getPreNodeTo() {
        return preNodeTo;
    }

    @Override
    public void setPreNodeTo(PreNode preNodeTo) {
        this.preNodeTo = preNodeTo;
    }

    public float getCardinal() {
        return cardinal;
    }

    public void setCardinal(float cardinal) {
        this.cardinal = cardinal;
    }

    public Edge initEdgeInstance() {
        edge = new EdgeImpl(this);
        return edge;
    }

    public EdgeImpl getEdge() {
        if(edge==null)
            initEdgeInstance();
        return edge;
    }

    public void setEdge(Edge edge) {
        this.edge = (EdgeImpl)edge;
    }

    public SightImpl getSight() {
        return sight;
    }
}
