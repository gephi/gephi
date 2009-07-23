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

import org.gephi.graph.api.Attributes;
import org.gephi.graph.api.DynamicData;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.EdgeData;
import org.gephi.graph.api.LayoutData;
import org.gephi.graph.api.NodeData;
import org.gephi.graph.api.Model;
import org.gephi.graph.api.TextData;

/**
 * Implementation of the edge data interface.
 *
 * @author Mathieu Bastian
 */
public class EdgeDataImpl implements EdgeData, DynamicData {

    protected Edge edge;
    protected LayoutData layoutData;
    protected String label = "";
    protected float r = 0f;
    protected float g = 0f;
    protected float b = 0f;
    protected float alpha = 1f;
    protected float cardinal = 1f;
    protected boolean labelVisible = true;
    private Model model;
    protected Attributes attributes;
    protected TextData textData;
    protected float dynamicRangeFrom = -1;
    protected float dynamicRangeTo = -1;

    public EdgeDataImpl(Edge edge) {
        this.edge = edge;
    }

    public Edge getEdge() {
        return edge;
    }

    public NodeData getSource() {
        return edge.getSource().getNodeData();
    }

    public NodeData getTarget() {
        return edge.getTarget().getNodeData();
    }

    public String getLabel() {
        return label;
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
        return (getSource().x() + getTarget().x()) / 2f;
    }

    public float y() {
        return (getSource().y() + getTarget().y()) / 2f;
    }

    public float z() {
        return (getSource().z() + getTarget().z()) / 2f;
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

    public Model getModel() {
        return model;
    }

    public void setModel(Model obj) {
        this.model = obj;
    }

    public TextData getTextData() {
        return textData;
    }

    public void setTextData(TextData textData) {
        this.textData = textData;
    }

    public boolean isLabelVisible() {
        return labelVisible;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setLabelVisible(boolean value) {
        this.labelVisible = value;
    }

    public DynamicData getDynamicData() {
        return this;
    }

    public float getRangeFrom() {
        return dynamicRangeFrom;
    }

    public float getRangeTo() {
        return dynamicRangeTo;
    }

    public void setRange(float from, float to) {
        this.dynamicRangeFrom = from;
        this.dynamicRangeTo = to;
    }
}
