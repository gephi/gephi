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

import org.gephi.data.network.node.NodeImpl;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.EdgeLayoutInterface;
import org.gephi.graph.api.Object3d;

/**
 *
 * @author Mathieu Bastian
 */
public class EdgeImpl implements Edge {

    private NodeImpl source;
    private NodeImpl target;
    protected float r = 0f;
    protected float g = 0f;
    protected float b = 0f;
    protected float alpha = 1f;
    protected float cardinal = 1f;
    private Object3d obj;

    //Impl
    private DhnsEdge dhnsEdge;
    private EdgeLayoutInterface edgeLayout;

    public EdgeImpl(NodeImpl source, NodeImpl target) {
        this.source = source;
        this.target = target;
    }

    public EdgeImpl(DhnsEdge edge) {
        this.source = edge.getPreNodeFrom().getNode();
        this.target = edge.getPreNodeTo().getNode();
        this.dhnsEdge = edge;
    }

    public NodeImpl getSource() {
        return source;
    }

    public NodeImpl getTarget() {
        return target;
    }

    public float x() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public float y() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public float z() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setX(float x) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setY(float y) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setZ(float z) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public float getRadius() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public float getSize() {
        return cardinal;
    }

    public void setSize(float size) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public float r() {
        return r;
    }

    public float g() {
        return g;
    }

    public float b() {
        return b;
    }

    public void setR(float r) {
        this.r = r;
    }

    public void setG(float g) {
        this.g = g;
    }

    public void setB(float b) {
        this.b = b;
    }

    public float alpha() {
        return alpha;
    }

    public void setAlpha(float alpha) {
        this.alpha = alpha;
    }

    public Object3d getObject3d() {
        return obj;
    }

    public void setObject3d(Object3d obj) {
        this.obj = obj;
    }

    public void setCardinal(float cardinal) {
        this.cardinal = cardinal;
    }

    public DhnsEdge getDhnsEdge() {
        return dhnsEdge;
    }

    public EdgeLayoutInterface getEdgeLayout() {
        return edgeLayout;
    }

    public void setEdgeLayout(EdgeLayoutInterface edgeLayout) {
        this.edgeLayout = edgeLayout;
    }
}
