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
package org.gephi.io.importer.impl;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeRow;
import org.gephi.data.attributes.api.AttributeType;
import org.gephi.data.attributes.type.DynamicType;
import org.gephi.data.attributes.type.Interval;
import org.gephi.data.attributes.type.TimeInterval;
import org.gephi.data.attributes.type.TypeConvertor;
import org.gephi.dynamic.DynamicUtilities;
import org.gephi.io.importer.api.EdgeDraft;
import org.gephi.io.importer.api.NodeDraft;
import org.gephi.io.importer.api.EdgeDraftGetter;
import org.openide.util.NbBundle;

/**
 *
 * @author Mathieu Bastian
 */
public class EdgeDraftImpl implements EdgeDraft, EdgeDraftGetter {

    //Architecture
    private final ImportContainerImpl container;
    //Basic
    private String id;
    private String label;
    //Flag
    private boolean autoId;
    //Topology
    private NodeDraftImpl source;
    private NodeDraftImpl target;
    private float weight = 1f;
    private EdgeType edgeType;
    //Viz
    private Color color;
    private boolean visible = true;
    //Text
    private Color labelColor;
    private float labelSize = -1f;
    private boolean labelVisible = true;
    //Attributes
    private final AttributeRow attributeRow;
    //Dynamic
    private TimeInterval timeInterval;

    public EdgeDraftImpl(ImportContainerImpl container, String id) {
        this.container = container;
        this.id = id;
        this.autoId = true;
        this.attributeRow = container.getAttributeModel().rowFactory().newEdgeRow();
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

    public void setLabel(String label) {
        this.label = label;
    }

    public void setLabelSize(float size) {
        this.labelSize = size;
    }

    public void setLabelVisible(boolean labelVisible) {
        this.labelVisible = labelVisible;
    }

    public void setLabelColor(Color color) {
        this.labelColor = color;
    }

    public void setLabelColor(String r, String g, String b) {
        setColor(Integer.parseInt(r), Integer.parseInt(g), Integer.parseInt(b));
    }

    public void setLabelColor(float r, float g, float b) {
        r = Math.max(Math.min(r, 1f), 0f);
        g = Math.max(Math.min(g, 1f), 0f);
        b = Math.max(Math.min(b, 1f), 0f);
        setColor(new Color(r, g, b));
    }

    public void setLabelColor(int r, int g, int b) {
        setColor(r / 255f, g / 255f, b / 255f);
    }

    public void setLabelColor(String color) {
        setColor(Color.getColor(color));
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
        if (column.getType().isDynamicType() && !(value instanceof DynamicType)) {
            if (value instanceof String && !column.getType().equals(AttributeType.DYNAMIC_STRING)) {
                //Value needs to be parsed
                value = TypeConvertor.getStaticType(column.getType()).parse(value);
            }
            //Wrap value in a dynamic type
            value = DynamicUtilities.createDynamicObject(column.getType(), new Interval(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, value));
        }
        attributeRow.setValue(column, value);
    }

    public void addAttributeValue(AttributeColumn column, Object value, String dateFrom, String dateTo) throws IllegalArgumentException {
        if (!column.getType().isDynamicType()) {
            throw new IllegalArgumentException("The column must be dynamic");
        }
        Double start = null;
        Double end = null;
        if (dateFrom != null && !dateFrom.isEmpty()) {
            try {
                start = DynamicUtilities.getDoubleFromXMLDateString(dateFrom);
            } catch (Exception ex) {
                try {
                    start = Double.parseDouble(dateFrom);
                } catch (Exception ex2) {
                    throw new IllegalArgumentException(NbBundle.getMessage(EdgeDraftImpl.class, "ImportContainerException_TimeInterval_ParseError", dateFrom));
                }
            }
        }
        if (dateTo != null && !dateTo.isEmpty()) {
            try {
                end = DynamicUtilities.getDoubleFromXMLDateString(dateTo);
            } catch (Exception ex) {
                try {
                    end = Double.parseDouble(dateTo);
                } catch (Exception ex2) {
                    throw new IllegalArgumentException(NbBundle.getMessage(EdgeDraftImpl.class, "ImportContainerException_TimeInterval_ParseError", dateTo));
                }
            }
        }
        if (start == null && end == null) {
            throw new IllegalArgumentException(NbBundle.getMessage(EdgeDraftImpl.class, "ImportContainerException_TimeInterval_Empty"));
        }
        if (value instanceof String && !column.getType().equals(AttributeType.DYNAMIC_STRING)) {
            //Value needs to be parsed
            value = TypeConvertor.getStaticType(column.getType()).parse(value);
        }
        Object sourceVal = attributeRow.getValue(column);
        if (sourceVal != null && sourceVal instanceof DynamicType) {
            value = DynamicUtilities.createDynamicObject(column.getType(), (DynamicType) sourceVal, new Interval(start, end, value));
        } else if (sourceVal != null && !(sourceVal instanceof DynamicType)) {
            List<Interval> intervals = new ArrayList<Interval>(2);
            intervals.add(new Interval(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, sourceVal));
            intervals.add(new Interval(start, end, value));
            value = DynamicUtilities.createDynamicObject(column.getType(), intervals);
        } else {
            value = DynamicUtilities.createDynamicObject(column.getType(), new Interval(start, end, value));
        }
        attributeRow.setValue(column, value);
    }

    public void addTimeInterval(String dateFrom, String dateTo) throws IllegalArgumentException {
        if (timeInterval == null) {
            timeInterval = new TimeInterval();
        }
        Double start = null;
        Double end = null;
        if (dateFrom != null && !dateFrom.isEmpty()) {
            try {
                start = DynamicUtilities.getDoubleFromXMLDateString(dateFrom);
            } catch (Exception ex) {
                try {
                    start = Double.parseDouble(dateFrom);
                } catch (Exception ex2) {
                    throw new IllegalArgumentException(NbBundle.getMessage(EdgeDraftImpl.class, "ImportContainerException_TimeInterval_ParseError", dateFrom));
                }
            }
        }
        if (dateTo != null && !dateTo.isEmpty()) {
            try {
                end = DynamicUtilities.getDoubleFromXMLDateString(dateTo);
            } catch (Exception ex) {
                try {
                    end = Double.parseDouble(dateTo);
                } catch (Exception ex2) {
                    throw new IllegalArgumentException(NbBundle.getMessage(EdgeDraftImpl.class, "ImportContainerException_TimeInterval_ParseError", dateTo));
                }
            }
        }
        if (start == null && end == null) {
            throw new IllegalArgumentException(NbBundle.getMessage(EdgeDraftImpl.class, "ImportContainerException_TimeInterval_Empty"));
        }
        timeInterval = new TimeInterval(timeInterval, start, end);
    }

    public void setTimeInterval(TimeInterval timeInterval) {
        this.timeInterval = timeInterval;
    }

    //GETTERS
    public AttributeRow getAttributeRow() {
        return attributeRow;
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

    public float getLabelSize() {
        return labelSize;
    }

    public boolean isLabelVisible() {
        return labelVisible;
    }

    public Color getLabelColor() {
        return labelColor;
    }

    public float getWeight() {
        return weight;
    }

    public EdgeType getType() {
        return edgeType;
    }

    public boolean isVisible() {
        return visible;
    }

    public TimeInterval getTimeInterval() {
        return timeInterval;
    }

    @Override
    public String toString() {
        String res = "edge";
        if (!autoId) {
            res += " id=" + id;
        } else if (label != null) {
            res += " label=" + label;
        } else {
            res += id;
        }
        return res;
    }
}
