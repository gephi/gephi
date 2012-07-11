/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>, Cezary Bartosiak
Website : http://www.gephi.org

This file is part of Gephi.

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
package org.gephi.data.attributes;

import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeEvent.EventType;
import org.gephi.data.attributes.api.AttributeOrigin;
import org.gephi.data.attributes.api.AttributeRow;
import org.gephi.data.attributes.api.AttributeType;
import org.gephi.data.attributes.api.AttributeValue;
import org.gephi.data.attributes.event.ValueEvent;

/**
 *
 * @author Mathieu Bastian
 * @author Cezary Bartosiak
 */
public class AttributeRowImpl implements AttributeRow {
    
    protected final Object object;
    protected final AttributeTableImpl attributeTable;
    protected AttributeValueImpl[] values;
    protected int rowVersion = -1;
    
    public AttributeRowImpl(AttributeTableImpl attributeTable, Object object) {
        this.attributeTable = attributeTable;
        this.object = object;
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
        } else {
            //add column
            AttributeType type = AttributeType.parse(value);
            //System.out.println("parsed value type: " + value.getClass());
            if (type != null) {
                attributeColumn = attributeTable.addColumn(column, type);
                setValue(attributeColumn, value);
            }
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
                throw new IllegalArgumentException("The "+attributeTable.getName()+" value column "+value.getColumn().getId()+" with index "+value.getColumn().getIndex()+" doesn't exist");
            }
            value = attributeTable.getFactory().newValue(column, value.getValue());
        }
        
        setValue(column.getIndex(), (AttributeValueImpl) value);
    }
    
    private void setValue(int index, AttributeValueImpl value) {
        updateColumns();
        
        AttributeValueImpl oldValue = this.values[index];
        
        this.values[index] = value;
        
        if (!((oldValue == null && value == null) || (oldValue != null && oldValue.equals(value)))
                && index > 0 && !value.getColumn().getOrigin().equals(AttributeOrigin.COMPUTED)) {    //0 is the index of node id and edge id cols, not useful to send these events
            attributeTable.model.fireAttributeEvent(new ValueEvent(EventType.SET_VALUE, attributeTable, object, value));
        }
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
    
    public AttributeValue getAttributeValueAt(int index) {
        if (checkIndexRange(index)) {
            return values[index];
        }
        return null;
    }
    
    public int countValues() {
        updateColumns();
        return values.length;
    }
    
    public AttributeColumn getColumnAt(int index) {
        updateColumns();
        return attributeTable.getColumn(index);
    }
    
    public Object getObject() {
        return object;
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
        return index < values.length && index >= 0;
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
