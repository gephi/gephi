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
import org.gephi.io.container.EdgeDraft;
import org.gephi.io.container.NodeDraft;
import org.gephi.io.processor.EdgeDraftGetter;

/**
 *
 * @author Mathieu Bastian
 */
public class EdgeDraftImpl implements EdgeDraft, EdgeDraftGetter {

    //Architecture
    private ImportContainerImpl container;

    //Basic
    private String id;
    private String label;

     //Flag
    private boolean autoId;

    //Topology
    private NodeDraftImpl source;
    private NodeDraftImpl target;
    private float weight;
    private EdgeType edgeType;

    //Viz
    private Color color;
    private boolean labelVisible;
    private boolean visible;
    private float labelSize;

    //Attributes
    private List<AttributeValue> attributeValues = new ArrayList<AttributeValue>();

    public EdgeDraftImpl(ImportContainerImpl container, String id) {
        this.container = container;
        this.id = id;
        this.autoId = true;
    }

    //SETTERS
    public void setWeight(float weight) {
        this.weight = weight;
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
        setColor(new Color(r, g, b));
    }

    public void setColor(int r, int g, int b) {
        setColor(r / 255f, g / 255f, b / 255f);
    }

    public void setColor(String color) {
        setColor(Color.getColor(color));
    }

    public void setLabelVisible(boolean labelVisible) {
        this.labelVisible = labelVisible;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setLabelSize(float size) {
        this.labelSize = size;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public void setType(EdgeType edgeType) {
        this.edgeType = edgeType;
    }

    public void setId(String id) {
        this.id = id;
        this.autoId = false;
    }

    public void setSource(NodeDraft nodeSource) {
        this.source = (NodeDraftImpl) nodeSource;
    }

    public void setTarget(NodeDraft nodeTarget) {
        this.target = (NodeDraftImpl) nodeTarget;
    }

    public void addAttributeValue(AttributeColumn column, Object value) {
        AttributeValue attValue = container.getFactory().newValue(column, value);
        attributeValues.add(attValue);
    }

    //GETTERS
    public EdgeType getEdgeType() {
        return edgeType;
    }

    public float getLabelSize() {
        return labelSize;
    }

    public NodeDraftImpl getSource() {
        return source;
    }

    public NodeDraftImpl getTarget() {
        return target;
    }

    public Color getColor() {
        return color;
    }

    public String getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public float getWeight() {
        return weight;
    }

    public EdgeType getType() {
        return edgeType;
    }

    public boolean isLabelVisible() {
        return labelVisible;
    }

    public boolean isVisible() {
        return visible;
    }

    @Override
    public String toString() {
        String res = "edge";
        if(!autoId) {
            res+=" id="+id;
        } else if(label!=null) {
            res+=" label="+label;
        } else {
            res+=id;
        }
        return res;
    }
}
