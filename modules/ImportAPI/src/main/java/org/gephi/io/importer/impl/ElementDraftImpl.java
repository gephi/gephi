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

import it.unimi.dsi.fastutil.doubles.Double2ObjectMap;
import it.unimi.dsi.fastutil.doubles.Double2ObjectOpenHashMap;
import java.awt.Color;
import org.gephi.attribute.api.AttributeUtils;
import org.gephi.io.importer.api.ColumnDraft;
import org.gephi.io.importer.api.ElementDraft;

/**
 *
 * @author mbastian
 */
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
    protected double[] timeStamps;
    //Dynamic values
    protected Double2ObjectMap[] dynamicAttributes;

    public ElementDraftImpl(ImportContainerImpl container, String id) {
        this.container = container;
        this.id = id;
        this.attributes = new Object[0];
        this.timeStamps = new double[0];
        this.dynamicAttributes = new Double2ObjectMap[0];
    }

    abstract ColumnDraft getColumn(String key);

    abstract ColumnDraft getColumn(String key, Class type);

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
        setColor(Color.getColor(color));
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
        setLabelColor(Color.getColor(color));
    }

    @Override
    public void setValue(String key, Object value) {
        ColumnDraft column = getColumn(key, value.getClass());
        setAttributeValue(((ColumnDraftImpl) column).getIndex(), value);
    }

    @Override
    public void setValue(String key, Object value, String dateTime) {
        setValue(key, value, AttributeUtils.parseDateTime(dateTime));
    }

    @Override
    public void setValue(String key, Object value, double timestamp) {
        ColumnDraft column = getColumn(key, value.getClass());
        setAttributeValue(((ColumnDraftImpl) column).getIndex(), value, timestamp);
    }

    @Override
    public void parseAndSetValue(String key, String value) {
        ColumnDraft column = getColumn(key);
        Object val = AttributeUtils.parse(value, column.getTypeClass());
        setAttributeValue(((ColumnDraftImpl) column).getIndex(), val);
    }

    @Override
    public void parseAndSetValue(String key, String value, String dateTime) {
        parseAndSetValue(key, value, AttributeUtils.parseDateTime(dateTime));
    }

    @Override
    public void parseAndSetValue(String key, String value, double timestamp) {
        ColumnDraft column = getColumn(key);
        Object val = AttributeUtils.parse(value, column.getTypeClass());
        setAttributeValue(((ColumnDraftImpl) column).getIndex(), val, timestamp);
    }

    @Override
    public void addTimestamp(String dateTime) {
        addTimestamp(AttributeUtils.parseDateTime(dateTime));
    }

    @Override
    public void addTimestamp(double timestamp) {
        int index = timeStamps.length;
        ensureTimestampArraySize(index);
        timeStamps[index] = timestamp;
    }

    @Override
    public double[] getTimestamps() {
        return timeStamps;
    }

    @Override
    public double[] getTimestamps(String key) {
        ColumnDraft col = getColumn(key);
        if (col != null) {
            Double2ObjectMap m = getDynamicAttributeValue(((ColumnDraftImpl) col).getIndex());
            if (m != null) {
                return m.keySet().toDoubleArray();
            }
        }
        return null;
    }

    @Override
    public Object getValue(String key, double timestamp) {
        ColumnDraft col = getColumn(key);
        if (col != null) {
            Double2ObjectMap m = getDynamicAttributeValue(((ColumnDraftImpl) col).getIndex());
            if (m != null) {
                return m.get(timestamp);
            }
        }
        return null;
    }

    public boolean isDynamic() {
        return timeStamps.length > 0;
    }

    public boolean hasDynamicAttributes() {
        return dynamicAttributes.length > 0;
    }

    //UTILITY
    protected void setAttributeValue(int index, Object value) {
        if (index >= attributes.length) {
            Object[] newArray = new Object[index + 1];
            System.arraycopy(attributes, 0, newArray, 0, attributes.length);
            attributes = newArray;
        }
        attributes[index] = value;
    }

    protected void setAttributeValue(int index, Object value, double timestamp) {
        if (index >= dynamicAttributes.length) {
            Double2ObjectMap[] newArray = new Double2ObjectMap[index + 1];
            System.arraycopy(dynamicAttributes, 0, newArray, 0, dynamicAttributes.length);
            dynamicAttributes = newArray;
        }
        Double2ObjectMap m = dynamicAttributes[index];
        if (m == null) {
            m = new Double2ObjectOpenHashMap();
            dynamicAttributes[index] = m;
        }
        m.put(timestamp, value);
    }

    protected Object getAttributeValue(int index) {
        if (index < attributes.length) {
            return attributes[index];
        }
        return null;
    }

    protected Double2ObjectMap getDynamicAttributeValue(int index) {
        if (index < dynamicAttributes.length) {
            return dynamicAttributes[index];
        }
        return null;
    }

    protected void ensureTimestampArraySize(int index) {
        if (index >= timeStamps.length) {
            double[] newArray = new double[index + 1];
            System.arraycopy(timeStamps, 0, newArray, 0, timeStamps.length);
            timeStamps = newArray;
        }
    }
}
