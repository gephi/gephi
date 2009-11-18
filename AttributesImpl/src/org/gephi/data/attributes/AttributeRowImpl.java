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

    protected AttributeTableImpl attributeTable;
    protected AttributeValueImpl[] values;
    protected int rowVersion = -1;

    public AttributeRowImpl(AttributeTableImpl attributeClass) {
        this.attributeTable = attributeClass;
        reset();
    }

    public void reset() {
        rowVersion = attributeTable.getVersion();
        int attSize = attributeTable.countColumns();
        AttributeValueImpl[] newValues = new AttributeValueImpl[attSize];
        for (int i = 0; i < attSize; i++) {
            newValues[i] = attributeTable.getColumn(i).defaultValue;
        }
        this.values = newValues;
    }

    public void setValues(AttributeRow attributeRow) {
        if (attributeRow == null) {
            throw new NullPointerException();
        }
        AttributeValue[] attValues = attributeRow.getValues();
        for (int i = 0; i < attValues.length; i++) {
            setValue(attValues[i]);
        }
    }

    public void setValue(int index, Object value) {
        AttributeColumn column = attributeTable.getColumn(index);
        if (column != null) {
            setValue(column, value);
        } else {
            throw new IllegalArgumentException("The column doesn't exist");
        }
    }

    public void setValue(String column, Object value) {
        if (column == null) {
            throw new NullPointerException("Column is null");
        }
        AttributeColumn attributeColumn = attributeTable.getColumn(column);
        if (attributeColumn != null) {
            setValue(attributeColumn, value);
        }
    }

    public void setValue(AttributeColumn column, Object value) {
        if (column == null) {
            throw new NullPointerException("Column is null");
        }
        AttributeValue attValue = attributeTable.getFactory().newValue(column, value);
        setValue(attValue);
    }

    public void setValue(AttributeValue value) {
        AttributeColumn column = value.getColumn();
        if (attributeTable.getColumn(column.getIndex()) != column) {
            column = attributeTable.getColumn(column);
            if (column == null) {
                throw new IllegalArgumentException("The value column doesn't exist");
            }
            value = attributeTable.getFactory().newValue(column, value.getValue());
        }
        setValue(column.getIndex(), (AttributeValueImpl) value);
    }

    private void setValue(int index, AttributeValueImpl value) {
        updateColumns();
        this.values[index] = value;
    }

    public Object getValue(AttributeColumn column) {
        if (column == null) {
            throw new NullPointerException();
        }
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

    public Object getValue(int index) {
        updateColumns();
        if (checkIndexRange(index)) {
            AttributeColumn attributeColumn = attributeTable.getColumn(index);
            return getValue(attributeColumn);
        }
        return null;
    }

    public Object getValue(String column) {
        updateColumns();
        AttributeColumn attributeColumn = attributeTable.getColumn(column);
        if (attributeColumn != null) {
            return getValue(attributeColumn);
        }
        return null;
    }

    public AttributeValue[] getValues() {
        return values;
    }

    public int countValues() {
        updateColumns();
        return values.length;
    }

    private void updateColumns() {

        int tableVersion = attributeTable.getVersion();
        if (rowVersion < tableVersion) {

            //Need to update
            AttributeColumnImpl[] columns = attributeTable.getColumns();
            AttributeValueImpl[] newValues = new AttributeValueImpl[columns.length];

            int j = 0;
            for (int i = 0; i < columns.length; i++) {
                AttributeColumnImpl tableCol = columns[i];
                newValues[i] = tableCol.defaultValue;
                while (j < values.length) {
                    AttributeValueImpl val = values[j++];
                    if (val.getColumn() == tableCol) {
                        newValues[i] = val;
                        break;
                    }
                }
            }
            values = newValues;

            //Upd version
            rowVersion = tableVersion;
        }
    }

    private boolean checkIndexRange(int index) {
        return index < values.length;
    }

    public int getRowVersion() {
        return rowVersion;
    }

    public void setRowVersion(int rowVersion) {
        this.rowVersion = rowVersion;
    }

    public void setValues(AttributeValueImpl[] values) {
        this.values = values;
    }
}
