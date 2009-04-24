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
package org.gephi.data.attributes;

import java.util.Arrays;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributesRow;

/**
 *
 * @author Mathieu Bastian
 */
public class AttributeRowImpl implements AttributesRow {

    protected AbstractAttributeClass attributeClass;
    protected Object[] values;

    public AttributeRowImpl(AbstractAttributeClass attributeClass) {
        reset();
    }

    public void reset() {

        int attSize = attributeClass.countAttributeColumns();
        Object[] newValues = new Object[attSize];
        for (int i = 0; i < attSize; i++) {
            newValues[i] = attributeClass.getAttributeColumn(i).getDefaultValue();
        }
        this.values = newValues;
    }

    public void setValues(Object[] values) {
        for (int i = 0; i < values.length; i++) {
            setValue(i, values[i]);
        }
    }

    public void setValues(AttributesRow attributeRow) {
        setValues(((AttributeRowImpl) attributeRow).values);
    }

    public void setValue(int index, Object value) {
        if (checkIndexRange(index)) {
            AttributeColumn column = attributeClass.getAttributeColumn(index);
            this.values[index] = attributeClass.getManagedValue(value, column.getAttributeType());
        }
    }

    public void setValue(AttributeColumn column, Object value) {
        setValue(column.getIndex(), value);
    }

    public Object getValue(AttributeColumn column) {
        return getValue(column.getIndex());
    }

    public Object getValue(String column) {
        AttributeColumn attributeColumn = attributeClass.getAttributeColumn(column);
        if (attributeColumn != null) {
            return getValue(attributeColumn.getIndex());
        }
        return null;
    }

    public Object getValue(int index) {
        if (checkIndexRange(index)) {
            return values[index];
        }
        return null;
    }

    private boolean checkIndexRange(int index) {
        return index < values.length;
    }

    public void addColumn(Object defaultValue)
    {
        Object[] newValues = Arrays.copyOf(values, values.length+1);
        newValues[newValues.length-1] = defaultValue;
    }

    public void removeColumn(int index)
    {
        Object[] newValues = Arrays.copyOf(values, values.length-1);
        for(int i=index;i<newValues.length;i++)
        {
            newValues[i] = values[i+1];
        }
    }
}
