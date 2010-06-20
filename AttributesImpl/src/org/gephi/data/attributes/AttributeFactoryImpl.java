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
import org.gephi.data.attributes.api.AttributeRowFactory;
import org.gephi.data.attributes.api.AttributeValueFactory;
import org.gephi.data.attributes.api.AttributeValue;

/**
 *
 * @author Mathieu Bastian
 */
public class AttributeFactoryImpl implements AttributeValueFactory, AttributeRowFactory {

    private AbstractAttributeModel model;

    public AttributeFactoryImpl(AbstractAttributeModel model) {
        this.model = model;
    }

    public AttributeValue newValue(AttributeColumn column, Object value) {
        if (value == null) {
            return new AttributeValueImpl((AttributeColumnImpl) column, null);
        }
//        System.out.println("column: " + column.getId());//TODO remove
//        System.out.println("value: " + value + ": " + value.getClass());
//        System.out.println();
//
//        value.getClass();
//        column.getType();
//        column.getType().getType();
//        value.getClass();

        if (value.getClass() != column.getType().getType() && value.getClass() == String.class) {
            value = column.getType().parse((String) value);
        }
        Object managedValue = model.getManagedValue(value, column.getType());
        return new AttributeValueImpl((AttributeColumnImpl) column, managedValue);
    }

    public AttributeRowImpl newNodeRow() {
        return new AttributeRowImpl(model.getNodeTable());
    }

    public AttributeRowImpl newEdgeRow() {
        return new AttributeRowImpl(model.getEdgeTable());
    }

    public AttributeRowImpl newRowForTable(String tableName) {
        AttributeTableImpl attTable = model.getTable(tableName);
        if (attTable != null) {
            return new AttributeRowImpl(attTable);
        }
        return null;
    }

    public void setModel(AbstractAttributeModel model) {
        this.model = model;
    }
}
