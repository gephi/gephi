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
package org.gephi.neo4j.attributes;

import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeRow;
import org.gephi.data.attributes.api.AttributeType;
import org.gephi.data.attributes.api.AttributeValue;
import org.neo4j.graphdb.GraphDatabaseService;
import org.openide.util.NbBundle;

/**
 *
 * @author Mathieu Bastian
 */
public class AttributeRowImpl implements AttributeRow {
    //public final String RESERVED_NEO4J_COLUMN_NAME = "neoid";
    private GraphDatabaseService graphDB;//inject value
    private int neo4jId;

    protected AttributeTableImpl attributeTable;
    //protected AttributeValueImpl[] values;
    protected int rowVersion = -1;

    public AttributeRowImpl(AttributeTableImpl attributeClass) {
        this.attributeTable = attributeClass;
        reset();
    }

    public void setNeo4jId(int neo4jId) {
        this.neo4jId = neo4jId;
    }

    public void reset() {
        rowVersion = attributeTable.getVersion();

        if (attributeTable.getName().equals(NbBundle.getMessage(AttributeTableImpl.class, "NodeAttributeTable.name"))) {
            for (String propertyKey : graphDB.getNodeById(neo4jId).getPropertyKeys()) {
                graphDB.getNodeById(neo4jId).setProperty(propertyKey, attributeTable.getColumn(propertyKey).defaultValue);
            }
        }
        else if (attributeTable.getName().equals(NbBundle.getMessage(AttributeTableImpl.class, "EdgeAttributeTable.name"))) {
            for (String propertyKey : graphDB.getRelationshipById(neo4jId).getPropertyKeys()) {
                graphDB.getRelationshipById(neo4jId).setProperty(propertyKey, attributeTable.getColumn(propertyKey).defaultValue);
            }
        }

//        int attSize = attributeTable.countColumns();
//        AttributeValueImpl[] newValues = new AttributeValueImpl[attSize];
//        for (int i = 0; i < attSize; i++) {
//            newValues[i] = attributeTable.getColumn(i).defaultValue;
//        }
        //this.values = newValues;
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
                throw new IllegalArgumentException("The value column doesn't exist");
            }
            value = attributeTable.getFactory().newValue(column, value.getValue());
        }
        setValue(column.getIndex(), (AttributeValueImpl) value);
    }

    private void setValue(int index, AttributeValueImpl value) {
//        updateColumns();
//        this.values[index] = value;
        if (attributeTable.getName().equals(NbBundle.getMessage(AttributeTableImpl.class, "NodeAttributeTable.name"))) {
            graphDB.getNodeById(neo4jId).setProperty(value.getColumn().getId(), value.getValue());
        }
        else if (attributeTable.getName().equals(NbBundle.getMessage(AttributeTableImpl.class, "EdgeAttributeTable.name"))) {
            graphDB.getRelationshipById(neo4jId).setProperty(value.getColumn().getId(), value.getValue());
        }
    }

    public Object getValue(AttributeColumn column) {
        if (column == null) {
            throw new NullPointerException();
        }
//        updateColumns();

        if (attributeTable.getName().equals(NbBundle.getMessage(AttributeTableImpl.class, "NodeAttributeTable.name"))) {
            return graphDB.getNodeById(neo4jId).getProperty(column.getId());
        }
        else if (attributeTable.getName().equals(NbBundle.getMessage(AttributeTableImpl.class, "EdgeAttributeTable.name"))) {
            return  graphDB.getRelationshipById(neo4jId).getProperty(column.getId());
        }
        return null;
//        int index = column.getIndex();
//        if (checkIndexRange(index)) {
//            AttributeValue val = values[index];
//            if (val.getColumn() == column) {
//                return val.getValue();
//            }
//        }
//        return null;
    }

    public Object getValue(int index) {
//        updateColumns();
        if (checkIndexRange(index)) {
            AttributeColumn attributeColumn = attributeTable.getColumn(index);
            return getValue(attributeColumn);
        }
        return null;
    }

    public Object getValue(String column) {
//        updateColumns();
        AttributeColumn attributeColumn = attributeTable.getColumn(column);
        if (attributeColumn != null) {
            return getValue(attributeColumn);
        }
        return null;
    }

    public AttributeValue[] getValues() {
        AttributeValueImpl[] attributeValues = new AttributeValueImpl [countValues()];

        if (attributeTable.getName().equals(NbBundle.getMessage(AttributeTableImpl.class, "NodeAttributeTable.name"))) {
            int index = 0;

            for (String propertyKey : graphDB.getNodeById(neo4jId).getPropertyKeys()) {
                AttributeColumnImpl attributeColumnImpl =
                        attributeTable.getColumn(propertyKey);
                Object attributeValue = graphDB.getNodeById(neo4jId).getProperty(propertyKey);
                attributeValues[index] = new AttributeValueImpl(attributeColumnImpl, attributeValue);
                index++;
            }
            return attributeValues;
        }
        else if (attributeTable.getName().equals(NbBundle.getMessage(AttributeTableImpl.class, "EdgeAttributeTable.name"))) {
            int index = 0;

            for (String propertyKey : graphDB.getRelationshipById(neo4jId).getPropertyKeys()) {
                AttributeColumnImpl attributeColumnImpl =
                        attributeTable.getColumn(propertyKey);
                Object attributeValue = graphDB.getRelationshipById(neo4jId).getProperty(propertyKey);
                attributeValues[index] = new AttributeValueImpl(attributeColumnImpl, attributeValue);
                index++;
            }
            return attributeValues;
        }

        return null;
    }

    public int countValues() {
//        updateColumns();
        //return values.length;

        if (attributeTable.getName().equals(NbBundle.getMessage(AttributeTableImpl.class, "NodeAttributeTable.name"))) {
            int count = 0;

            for (String propertyKey : graphDB.getNodeById(neo4jId).getPropertyKeys()) {
                count++;
            }
            return count;
        }
        else if (attributeTable.getName().equals(NbBundle.getMessage(AttributeTableImpl.class, "EdgeAttributeTable.name"))) {
            int count = 0;

            for (String propertyKey : graphDB.getRelationshipById(neo4jId).getPropertyKeys()) {
                count++;
            }
            return count;
        }
        else
            return  -1;
    }

//    private void updateColumns() {
//
//        int tableVersion = attributeTable.getVersion();
//        if (rowVersion < tableVersion) {
//
//            //Need to update
//            AttributeColumnImpl[] columns = attributeTable.getColumns();
//            AttributeValueImpl[] newValues = new AttributeValueImpl[columns.length];
//
//            int j = 0;
//            for (int i = 0; i < columns.length; i++) {
//                AttributeColumnImpl tableCol = columns[i];
//                newValues[i] = tableCol.defaultValue;
//                while (j < values.length) {
//                    AttributeValueImpl val = values[j++];
//                    if (val.getColumn() == tableCol) {
//                        newValues[i] = val;
//                        break;
//                    }
//                }
//            }
//            values = newValues;
//
//            //Upd version
//            rowVersion = tableVersion;
//        }
//    }

    private boolean checkIndexRange(int index) {
        return index >= 0 && index < countValues();
    }

    public int getRowVersion() {
        return rowVersion;
    }

    public void setRowVersion(int rowVersion) {
        this.rowVersion = rowVersion;
    }

//    public void setValues(AttributeValueImpl[] values) {
//        this.values = values;
//    }
}
