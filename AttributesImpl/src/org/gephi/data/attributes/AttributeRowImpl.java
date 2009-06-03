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

import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeRow;
import org.gephi.data.attributes.api.AttributeValue;

/**
 *
 * @author Mathieu Bastian
 */
public class AttributeRowImpl implements AttributeRow {

    protected AbstractAttributeClass attributeClass;
    protected AttributeValueImpl[] values;
    protected int rowVersion = -1;

    public AttributeRowImpl(AbstractAttributeClass attributeClass) {
        this.attributeClass = attributeClass;
        reset();
    }

    public void reset() {
        rowVersion = attributeClass.getVersion();
        int attSize = attributeClass.countAttributeColumns();
        AttributeValueImpl[] newValues = new AttributeValueImpl[attSize];
        for (int i = 0; i < attSize; i++) {
            newValues[i] = attributeClass.getAttributeColumn(i).defaultValue;
        }
        this.values = newValues;
    }

    public void setValues(AttributeRow attributeRow) {
        AttributeValue[] attValues = attributeRow.getValues();
        for (int i = 0; i < attValues.length; i++) {
            setValue(attValues[i]);
        }
    }

    public void setValue(int index, Object value) {
        AttributeColumn column = attributeClass.getAttributeColumn(index);
        if (column != null) {
            setValue(column, value);
        }
    }

    public void setValue(String column, Object value) {
        AttributeColumn attributeColumn = attributeClass.getAttributeColumn(column);
        if (attributeColumn != null) {
            setValue(attributeColumn, value);
        }
    }

    public void setValue(AttributeColumn column, Object value) {
        setValue(new AttributeValueImpl((AttributeColumnImpl) column, value));
    }

    public void setValue(AttributeValue value) {
        AttributeColumn column = value.getColumn();
        setValue(column.getIndex(), (AttributeValueImpl) value);
    }

    private void setValue(int index, AttributeValueImpl value) {
        updateColumns();
        this.values[index] = value;
    }

    public Object getValue(AttributeColumn column) {
        updateColumns();
        int index = column.getIndex();
        if (checkIndexRange(index)) {
            AttributeValue val = values[index];
            if (val.getColumn() == column) {
                return val.getValue();
            }
        }
        return null;
    }

    public Object getValue(String column) {
        AttributeColumn attributeColumn = attributeClass.getAttributeColumn(column);
        if (attributeColumn != null) {
            return getValue(attributeColumn);
        }
        return null;
    }

    public AttributeValue[] getValues() {
        return values;
    }

    private void updateColumns() {

        int classVersion = attributeClass.getVersion();
        if (rowVersion < classVersion) {

            //Need to update
            AttributeColumnImpl[] classColumns = attributeClass.getAttributeColumns();
            AttributeValueImpl[] newValues = new AttributeValueImpl[classColumns.length];

            int j = 0;
            for (int i = 0; i < classColumns.length; i++) {
                AttributeColumnImpl classCol = classColumns[i];
                newValues[i] = classCol.defaultValue;
                while (j < values.length) {
                    AttributeValueImpl val = values[j++];
                    if (val.getColumn() == classCol) {
                        newValues[i] = val;
                        break;
                    }
                }
            }
            values = newValues;

            //Upd version
            rowVersion = classVersion;
        }
    }

    private boolean checkIndexRange(int index) {
        return index < values.length;
    }
}
