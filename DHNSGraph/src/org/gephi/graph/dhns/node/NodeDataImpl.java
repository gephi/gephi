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
import org.gephi.graph.api.LayoutData;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.NodeData;
import org.gephi.graph.api.Model;
import org.gephi.graph.api.TextData;

/**
 * Implementation of the node data interface.
 *
 * @author Mathieu Bastian
 */
public class NodeDataImpl implements NodeData {

    protected Node node;
    protected LayoutData layoutData;
    protected String id = "";
    protected float x;
    protected float y;
    protected float z;
    protected String label = "";
    protected float r = 0f;
    protected float g = 0f;
    protected float b = 0f;
    protected float alpha = 1f;
    protected float size = 1f;
    protected Model model;
    protected boolean fixed;
    protected Attributes attributes;
    protected TextData textData;

    public NodeDataImpl(Node node) {
        this.node = node;
    }

    public Node getNode() {
        return node;
    }

    public LayoutData getLayoutData() {
        return layoutData;
    }

    public void setLayoutData(LayoutData layoutData) {
        this.layoutData = layoutData;
    }

    public Attributes getAttributes() {
        return attributes;
    }

    public void setAttributes(Attributes attributes) {
        this.attributes = attributes;
    }

    public float x() {
        return x;
    }

    public float y() {
        return y;
    }

    public float z() {
        return z;
    }

    public void setX(float x) {
        this.x = x;
        updatePositionFlag();
    }

    public void setY(float y) {
        this.y = y;
        updatePositionFlag();
    }

    public void setZ(float z) {
        this.z = z;
        updatePositionFlag();
    }

    private void updatePositionFlag() {
        if (model != null) {
            model.updatePositionFlag();
        }
    }

    public float getRadius() {
        return size;
    }

    public float getSize() {
        return size;
    }

    public void setSize(float size) {
        this.size = size;
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

    public void setLabel(String label) {
        this.label = label;
    }

    public float alpha() {
        return alpha;
    }

    public void setAlpha(float alpha) {
        this.alpha = alpha;
    }

    public Model getModel() {
        return model;
    }

    public void setModel(Model obj) {
        this.model = obj;
    }

    public String getLabel() {
        return label;
    }

    public String getId() {
        return id;
    }

    public boolean isFixed() {
        return fixed;
    }

    public void setFixed(boolean fixed) {
        this.fixed = fixed;
    }

    public TextData getTextData() {
        return textData;
    }

    public void setTextData(TextData textData) {
        this.textData = textData;
    }
}
