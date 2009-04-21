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
package org.gephi.data.network.potato;

import java.util.ArrayList;
import java.util.List;
import org.gephi.data.network.node.NodeImpl;
import org.gephi.data.network.node.PreNode;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.Object3d;
import org.gephi.graph.api.Potato;

/**
 *
 * @author Mathieu Bastian
 */
public class PotatoImpl implements Potato {

    //Manager
    private PotatoManager manager;
    private PreNode node;
    private List<NodeImpl> content;

    //Renderable
    private Object3d object3d;

    //Display
    private List<float[]> triangles;

    public PotatoImpl(PotatoManager potatoManager) {
        content = new ArrayList<NodeImpl>();
        this.manager = potatoManager;
    }

    public void setNode(PreNode node) {
        this.node = node;
        node.setPotato(this);
    }

    public void addContent(PreNode content) {
        this.content.add(content.getNode());
    }

    public PreNode getNode() {
        return node;
    }

    public void updatePotato() {
        manager.renderPotato(this);
    }

    public Iterable<? extends Node> getContent() {
        return content;
    }

    public int countContent() {
        return content.size();
    }

    public List<float[]> getTriangles() {
        return triangles;
    }

    public void setTriangles(List<float[]> triangles) {
        this.triangles = triangles;
    }

    //Renderable
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
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setSize(float size) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public float r() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public float g() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public float b() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setR(float r) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setG(float g) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setB(float b) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public float alpha() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setAlpha(float alpha) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Object3d getObject3d() {
        return object3d;
    }

    public void setObject3d(Object3d obj) {
        this.object3d = obj;
    }
}
