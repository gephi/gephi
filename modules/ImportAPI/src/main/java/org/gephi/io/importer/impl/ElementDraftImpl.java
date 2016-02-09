/*
 * Copyright 2008-2010 Gephi
 * Authors : Cezary Bartosiak
 *           Mathieu Bastian <mathieu.bastian@gephi.org>
 * Website : http://www.gephi.org
 *
 * This file is part of Gephi.
 *
 DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

 Copyright 2011 Gephi Consortium. All rights reserved.

 The contents of this file are subject to the terms of either the GNU
 General Public License Version 3 only ("GPL") or the Common
 Development and Distribution License("CDDL") (collectively, the
 "License"). You may not use this file except in compliance with the
 License. You can obtain a copy of the License at
 http://gephi.org/about/legal/license-notice/
 or /cddl-1.0.txt and /gpl-3.0.txt. See the License for the
 specific language governing permissions and limitations under the
 License.  When distributing the software, include this License Header
 Notice in each file and include the License files at
 /cddl-1.0.txt and /gpl-3.0.txt. If applicable, add the following below the
 License Header, with the fields enclosed by brackets [] replaced by
 your own identifying information:
 "Portions Copyrighted [year] [name of copyright owner]"

 If you wish your version of this file to be governed by only the CDDL
 or only the GPL Version 3, indicate your decision by adding
 "[Contributor] elects to include this software in this distribution
 under the [CDDL or GPL Version 3] license." If you do not indicate a
 single choice of license, a recipient has the option to distribute
 your version of this file under either the CDDL, the GPL Version 3 or
 to extend the choice of license to its licensees as provided above.
 However, if you add GPL Version 3 code and therefore, elected the GPL
 Version 3 license, then the option applies only if the new code is
 made subject to such option by the copyright holder.

 Contributor(s):

 Portions Copyrighted 2011 Gephi Consortium.
 */
package org.gephi.io.importer.impl;

import java.awt.Color;
import org.gephi.graph.api.AttributeUtils;
import org.gephi.graph.api.Interval;
import org.gephi.graph.api.TimeFormat;
import org.gephi.graph.api.TimeRepresentation;
import org.gephi.graph.api.types.IntervalMap;
import org.gephi.graph.api.types.IntervalSet;
import org.gephi.graph.api.types.TimeMap;
import org.gephi.graph.api.types.TimeSet;
import org.gephi.graph.api.types.TimestampMap;
import org.gephi.graph.api.types.TimestampSet;
import org.gephi.io.importer.api.ColumnDraft;
import org.gephi.io.importer.api.ElementDraft;
import org.gephi.io.importer.api.ImportUtils;
import org.gephi.io.importer.api.Issue;
import org.openide.util.NbBundle;

public abstract class ElementDraftImpl implements ElementDraft {

    protected final ImportContainerImpl container;
    //Properties
    protected final String id;
    protected String label;
    //Viz
    protected Color color;
    //Text
    protected Color labelColor;
    protected float labelSize = -1f;
    protected boolean labelVisible = true;
    //Attributes
    protected Object[] attributes;
    //Timestamps
    protected TimeSet timeSet;

    public ElementDraftImpl(ImportContainerImpl container, String id) {
        this.container = container;
        this.id = id;
        this.attributes = new Object[0];
    }

    abstract ColumnDraft getColumn(String key);

    abstract ColumnDraft getColumn(String key, Class type);

    @Override
    public Double getGraphTimestamp() {
        return container.getTimestamp();
    }

    @Override
    public Interval getGraphInterval() {
        return container.getInterval();
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public Color getColor() {
        return color;
    }

    @Override
    public boolean isLabelVisible() {
        return labelVisible;
    }

    @Override
    public float getLabelSize() {
        return labelSize;
    }

    @Override
    public Color getLabelColor() {
        return labelColor;
    }

    @Override
    public void setLabel(String label) {
        this.label = label;
    }

    @Override
    public void setColor(Color color) {
        this.color = color;
    }

    @Override
    public Object getValue(String key) {
        ColumnDraft column = getColumn(key);
        if (column != null) {
            return getAttributeValue(((ColumnDraftImpl) column).getIndex());
        }
        return null;
    }

    @Override
    public void setColor(String r, String g, String b) {
        setColor(Integer.parseInt(r), Integer.parseInt(g), Integer.parseInt(b));
    }

    @Override
    public void setColor(float r, float g, float b) {
        r = Math.max(Math.min(r, 1f), 0f);
        g = Math.max(Math.min(g, 1f), 0f);
        b = Math.max(Math.min(b, 1f), 0f);
        setColor(new Color(r, g, b));
    }

    @Override
    public void setColor(int r, int g, int b) {
        setColor(r / 255f, g / 255f, b / 255f);
    }

    @Override
    public void setColor(String color) {
        Color cl = ImportUtils.parseColor(color);
        if (cl != null) {
            setColor(cl);
        } else {
            String message = NbBundle.getMessage(ElementDraftImpl.class, "ElementDraftException_ColorParse", color, id);
            container.getReport().logIssue(new Issue(message, Issue.Level.WARNING));
        }
    }

    @Override
    public void setLabelVisible(boolean labelVisible) {
        this.labelVisible = labelVisible;
    }

    @Override
    public void setLabelSize(float size) {
        this.labelSize = size;
    }

    @Override
    public void setLabelColor(Color color) {
        this.labelColor = color;
    }

    @Override
    public void setLabelColor(String r, String g, String b) {
        setLabelColor(Integer.parseInt(r), Integer.parseInt(g), Integer.parseInt(b));
    }

    @Override
    public void setLabelColor(float r, float g, float b) {
        r = Math.max(Math.min(r, 1f), 0f);
        g = Math.max(Math.min(g, 1f), 0f);
        b = Math.max(Math.min(b, 1f), 0f);
        setLabelColor(new Color(r, g, b));
    }

    @Override
    public void setLabelColor(int r, int g, int b) {
        setLabelColor(r / 255f, g / 255f, b / 255f);
    }

    @Override
    public void setLabelColor(String color) {
        Color cl = ImportUtils.parseColor(color);
        if (cl != null) {
            setLabelColor(cl);
        } else {
            String message = NbBundle.getMessage(ElementDraftImpl.class, "ElementDraftException_LabelColorParse", color, id);
            container.getReport().logIssue(new Issue(message, Issue.Level.WARNING));
        }
    }

    @Override
    public void setValue(String key, Object value) {
        Class type = value.getClass();
        if (AttributeUtils.isDynamicType(type)) {
            type = AttributeUtils.getStaticType(type);
        }
        ColumnDraft column = getColumn(key, type);
        try {
            setAttributeValue(column, value);
        } catch (Exception ex) {
            String message = NbBundle.getMessage(ElementDraftImpl.class, "ElementDraftException_SetValueError", value.toString(), id, ex.getMessage());
            container.getReport().logIssue(new Issue(message, Issue.Level.SEVERE));
        }
    }

    @Override
    public void setValue(String key, Object value, String dateTime) {
        setValue(key, value, container.getTimeFormat().equals(TimeFormat.DOUBLE) ? Double.parseDouble(dateTime) : AttributeUtils.parseDateTime(dateTime));
    }

    @Override
    public void setValue(String key, Object value, double timestamp) {
        ColumnDraft column = getColumn(key, value.getClass());
        try {
            setAttributeValue(column, value, timestamp);
        } catch (Exception ex) {
            String message = NbBundle.getMessage(ElementDraftImpl.class, "ElementDraftException_SetValueTimestampError", value.toString(), id, timestamp, ex.getMessage());
            container.getReport().logIssue(new Issue(message, Issue.Level.SEVERE));
        }
    }

    @Override
    public void setValue(String key, Object value, String startDateTime, String endDateTime) {
        double start, end;
        if (startDateTime == null || startDateTime.isEmpty() || "-inf".equalsIgnoreCase(startDateTime) || "-infinity".equalsIgnoreCase(startDateTime)) {
            start = Double.NEGATIVE_INFINITY;
        } else {
            start = container.getTimeFormat().equals(TimeFormat.DOUBLE) ? Double.parseDouble(startDateTime) : AttributeUtils.parseDateTime(startDateTime);
        }
        if (endDateTime == null || endDateTime.isEmpty() || "inf".equalsIgnoreCase(endDateTime) || "infinity".equalsIgnoreCase(endDateTime)) {
            end = Double.POSITIVE_INFINITY;
        } else {
            end = container.getTimeFormat().equals(TimeFormat.DOUBLE) ? Double.parseDouble(endDateTime) : AttributeUtils.parseDateTime(endDateTime);
        }
        setValue(key, value, start, end);
    }

    @Override
    public void setValue(String key, Object value, double start, double end) {
        ColumnDraft column = getColumn(key, value.getClass());
        try {
            setAttributeValue(column, value, start, end);
        } catch (Exception ex) {
            String interval = "[" + start + "," + end + "]";
            String message = NbBundle.getMessage(ElementDraftImpl.class, "ElementDraftException_SetValueIntervalError", value.toString(), id, interval, ex.getMessage());
            container.getReport().logIssue(new Issue(message, Issue.Level.SEVERE));
        }
    }

    @Override
    public void parseAndSetValue(String key, String value) {
        ColumnDraft column = getColumn(key);
        Class typeClass;
        if (column.isDynamic()) {
            if (container.getTimeRepresentation().equals(TimeRepresentation.INTERVAL)) {
                if (container.getInterval() != null) {
                    parseAndSetValue(key, value, container.getInterval().getLow(), container.getInterval().getHigh());
                    return;
                }
                typeClass = AttributeUtils.getIntervalMapType(column.getTypeClass());
            } else {
                if (container.getTimestamp() != null) {
                    parseAndSetValue(key, value, container.getTimestamp());
                    return;
                }
                typeClass = AttributeUtils.getTimestampMapType(column.getTypeClass());
            }
        } else {
            typeClass = column.getTypeClass();
        }
        Object val = AttributeUtils.parse(value, typeClass);
        setValue(key, val);
    }

    @Override
    public void parseAndSetValue(String key, String value, String dateTime) {
        parseAndSetValue(key, value, container.getTimeFormat().equals(TimeFormat.DOUBLE) ? Double.parseDouble(dateTime) : AttributeUtils.parseDateTime(dateTime));
    }

    @Override
    public void parseAndSetValue(String key, String value, double timestamp) {
        ColumnDraft column = getColumn(key);
        Object val = AttributeUtils.parse(value, column.getTypeClass());
        setValue(key, val, timestamp);
    }

    @Override
    public void parseAndSetValue(String key, String value, String startDateTime, String endDateTime) {
        double start, end;
        if (startDateTime == null || startDateTime.isEmpty() || "-inf".equalsIgnoreCase(startDateTime) || "-infinity".equalsIgnoreCase(startDateTime)) {
            start = Double.NEGATIVE_INFINITY;
        } else {
            start = container.getTimeFormat().equals(TimeFormat.DOUBLE) ? Double.parseDouble(startDateTime) : AttributeUtils.parseDateTime(startDateTime);
        }
        if (endDateTime == null || endDateTime.isEmpty() || "inf".equalsIgnoreCase(endDateTime) || "infinity".equalsIgnoreCase(endDateTime)) {
            end = Double.POSITIVE_INFINITY;
        } else {
            end = container.getTimeFormat().equals(TimeFormat.DOUBLE) ? Double.parseDouble(endDateTime) : AttributeUtils.parseDateTime(endDateTime);
        }
        parseAndSetValue(key, value, start, end);
    }

    @Override
    public void parseAndSetValue(String key, String value, double start, double end) {
        ColumnDraft column = getColumn(key);
        Object val = AttributeUtils.parse(value, column.getTypeClass());
        setValue(key, val, start, end);
    }

    @Override
    public void addTimestamp(String dateTime) {
        addTimestamp(container.getTimeFormat().equals(TimeFormat.DOUBLE) ? Double.parseDouble(dateTime) : AttributeUtils.parseDateTime(dateTime));
    }

    @Override
    public void addTimestamp(double timestamp) {
        if (!container.getTimeRepresentation().equals(TimeRepresentation.TIMESTAMP)) {
            String message = NbBundle.getMessage(ElementDraftImpl.class, "ElementDraftException_NotTimestampRepresentation", id);
            container.getReport().logIssue(new Issue(message, Issue.Level.SEVERE));
            return;
        }
        if (timeSet == null) {
            timeSet = new TimestampSet();
        }
        timeSet.add(timestamp);
    }

    @Override
    public void addTimestamps(String timestamps) {
        if (!container.getTimeRepresentation().equals(TimeRepresentation.TIMESTAMP)) {
            String message = NbBundle.getMessage(ElementDraftImpl.class, "ElementDraftException_NotTimestampRepresentation", id);
            container.getReport().logIssue(new Issue(message, Issue.Level.SEVERE));
            return;
        }
        TimestampSet t = (TimestampSet) AttributeUtils.parse(timestamps, TimestampSet.class);
        if (timeSet == null) {
            timeSet = t;
        } else {
            for (Double d : t.toArray()) {
                timeSet.add(d);
            }
        }
    }

    @Override
    public TimeSet getTimeSet() {
        return timeSet;
    }

    @Override
    public void addInterval(String intervalStartDateTime, String intervalEndDateTime) {
        double start, end;
        if (intervalStartDateTime == null || intervalStartDateTime.isEmpty() || "-inf".equalsIgnoreCase(intervalStartDateTime) || "-infinity".equalsIgnoreCase(intervalStartDateTime)) {
            start = Double.NEGATIVE_INFINITY;
        } else {
            start = container.getTimeFormat().equals(TimeFormat.DOUBLE) ? Double.parseDouble(intervalStartDateTime) : AttributeUtils.parseDateTime(intervalStartDateTime);
        }
        if (intervalEndDateTime == null || intervalEndDateTime.isEmpty() || "inf".equalsIgnoreCase(intervalEndDateTime) || "infinity".equalsIgnoreCase(intervalEndDateTime)) {
            end = Double.POSITIVE_INFINITY;
        } else {
            end = container.getTimeFormat().equals(TimeFormat.DOUBLE) ? Double.parseDouble(intervalEndDateTime) : AttributeUtils.parseDateTime(intervalEndDateTime);
        }
        addInterval(start, end);
    }

    @Override
    public void addIntervals(String intervals) {
        if (!container.getTimeRepresentation().equals(TimeRepresentation.INTERVAL)) {
            String message = NbBundle.getMessage(ElementDraftImpl.class, "ElementDraftException_NotIntervalRepresentation", id);
            container.getReport().logIssue(new Issue(message, Issue.Level.SEVERE));
            return;
        }
        IntervalSet s = (IntervalSet) AttributeUtils.parse(intervals, IntervalSet.class);
        if (timeSet == null) {
            timeSet = s;
        } else {
            for (Interval i : s.toArray()) {
                timeSet.add(i);
            }
        }
    }

    @Override
    public void addInterval(double intervalStart, double intervalEnd) {
        if (!container.getTimeRepresentation().equals(TimeRepresentation.INTERVAL)) {
            String message = NbBundle.getMessage(ElementDraftImpl.class, "ElementDraftException_NotIntervalRepresentation", id);
            container.getReport().logIssue(new Issue(message, Issue.Level.SEVERE));
            return;
        }
        try {
            Interval interval = new Interval(intervalStart, intervalEnd);
            if (timeSet == null) {
                timeSet = new IntervalSet();
            }
            timeSet.add(interval);
        } catch (Exception e) {
            String interval = "[" + intervalStart + "," + intervalEnd + "]";
            String message = NbBundle.getMessage(ElementDraftImpl.class, "ElementDraftException_IntervalSetError", interval, id, e.getMessage());
            container.getReport().logIssue(new Issue(message, Issue.Level.SEVERE));
        }
    }

    public boolean isDynamic() {
        return timeSet != null && !timeSet.isEmpty();
    }

    public boolean hasDynamicAttributes() {
        for (Object att : attributes) {
            if (att != null && att instanceof TimeMap) {
                if (!((TimeMap) att).isEmpty()) {
                    return true;
                }
            }
        }
        return false;
    }

    //UTILITY
    protected void setAttributeValue(ColumnDraft column, Object value) throws Exception {
        int index = ((ColumnDraftImpl) column).getIndex();
        value = AttributeUtils.standardizeValue(value);
        Class typeClass = column.getTypeClass();

        if (column.isDynamic()) {
            if (container.getTimeRepresentation() == TimeRepresentation.INTERVAL) {
                typeClass = AttributeUtils.getIntervalMapType(typeClass);
            } else {
                typeClass = AttributeUtils.getTimestampMapType(typeClass);
            }
        }

        if (!value.getClass().equals(typeClass)) {
            throw new RuntimeException("The expected value class was " + typeClass.getSimpleName() + " and " + value.getClass().getSimpleName() + " was found");
        }
        if (index >= attributes.length) {
            Object[] newArray = new Object[index + 1];
            System.arraycopy(attributes, 0, newArray, 0, attributes.length);
            attributes = newArray;
        }
        attributes[index] = value;
    }

    protected void setAttributeValue(ColumnDraft column, Object value, double timestamp) throws Exception {
        int index = ((ColumnDraftImpl) column).getIndex();
        Class typeClass = column.getTypeClass();
        value = AttributeUtils.standardizeValue(value);
        if (!value.getClass().equals(typeClass)) {
            throw new RuntimeException("The expected value class was " + typeClass.getSimpleName() + " and " + value.getClass().getSimpleName() + " was found");
        }
        if (!column.isDynamic()) {
            throw new RuntimeException("Can't set a dynamic value to a static column");
        }
        if (index >= attributes.length) {
            Object[] newArray = new Object[index + 1];
            System.arraycopy(attributes, 0, newArray, 0, attributes.length);
            attributes = newArray;
        }
        TimestampMap m = (TimestampMap) attributes[index];
        if (m == null) {
            m = AttributeUtils.getTimestampMapType(column.getTypeClass()).newInstance();
            attributes[index] = m;
        }
        m.put(timestamp, value);
    }

    protected void setAttributeValue(ColumnDraft column, Object value, double start, double end) throws Exception {
        int index = ((ColumnDraftImpl) column).getIndex();
        value = AttributeUtils.standardizeValue(value);
        Class typeClass = column.getTypeClass();
        if (!value.getClass().equals(typeClass)) {
            throw new RuntimeException("The expected value class was " + typeClass.getSimpleName() + " and " + value.getClass().getSimpleName() + " was found");
        }
        if (!column.isDynamic()) {
            throw new RuntimeException("Can't set a dynamic value to a static column");
        }
        Interval interval = new Interval(start, end);
        if (index >= attributes.length) {
            Object[] newArray = new Object[index + 1];
            System.arraycopy(attributes, 0, newArray, 0, attributes.length);
            attributes = newArray;
        }
        IntervalMap m = (IntervalMap) attributes[index];
        if (m == null) {
            m = AttributeUtils.getIntervalMapType(column.getTypeClass()).newInstance();
            attributes[index] = m;
        }
        m.put(interval, value);
    }

    protected Object getAttributeValue(int index) {
        if (index < attributes.length) {
            return attributes[index];
        }
        return null;
    }
}
