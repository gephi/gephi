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
package org.gephi.io.container.standard;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeValue;
import org.gephi.graph.api.Node;
import org.gephi.io.container.NodeDraft;
import org.gephi.io.processor.NodeDraftGetter;



/**
 *
 * @author Mathieu Bastian
 */
public class NodeDraftImpl implements NodeDraft, NodeDraftGetter {

    //Architecture
    private ImportContainerImpl container;

    //Basic
    private String id;
    private String label;
    private List<NodeDraftImpl> children = new ArrayList();
    public boolean hasParent = false;

    //Viz attributes
    private Color color;
    private float size;
    private float x;
    private float y;
    private float z;
    private boolean labelVisible;
    private boolean visible;
    private boolean fixed;

    //Attributes
    private List<AttributeValue> attributeValues = new ArrayList<AttributeValue>();

    //Result
    private Node node;

    public NodeDraftImpl(ImportContainerImpl container, String id) {
        this.container = container;
        this.id = id;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public void setColor(String r, String g, String b) {
        setColor(Integer.parseInt(r), Integer.parseInt(g), Integer.parseInt(b));
    }

    public void setColor(float r, float g, float b) {
        r = Math.max(Math.min(r, 1f), 0f);
        g = Math.max(Math.min(g, 1f), 0f);
        b = Math.max(Math.min(b, 1f), 0f);
        this.color = new Color(r, g, b);
    }

    public void setColor(int r, int g, int b) {
        setColor(r / 255f, g / 255f, b / 255f);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public float getSize() {
        return size;
    }

    public void setSize(float size) {
        this.size = size;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getZ() {
        return z;
    }

    public void setZ(float z) {
        this.z = z;
    }

    public boolean isFixed() {
        return fixed;
    }

    public void setFixed(boolean fixed) {
        this.fixed = fixed;
    }

    public boolean isLabelVisible() {
        return labelVisible;
    }

    public void setLabelVisible(boolean labelVisible) {
        this.labelVisible = labelVisible;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public void addAttributeValue(AttributeColumn column, Object value) {
        AttributeValue attValue = container.getFactory().newValue(column, value);
        attributeValues.add(attValue);
    }

    public List<AttributeValue> getAttributeValues() {
        return attributeValues;
    }

    public void addChild(NodeDraft child) {
        children.add((NodeDraftImpl) child);
    }

    public List<? extends NodeDraft> getChildren() {
        return children;
    }

    public void setNode(Node node) {
        this.node = node;
    }

    public Node getNode() {
        return node;
    }
}
