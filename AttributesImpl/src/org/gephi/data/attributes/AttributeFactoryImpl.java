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

    private AbstractAttributeManager manager;

    public AttributeFactoryImpl(AbstractAttributeManager manager) {
        this.manager = manager;
    }

    public AttributeValue newValue(AttributeColumn column, Object value) {
        if (value.getClass() != column.getAttributeType().getType() && value.getClass() == String.class) {
            value = column.getAttributeType().parse((String) value);
        }
        Object managedValue = manager.getManagedValue(value, column.getAttributeType());
        return new AttributeValueImpl((AttributeColumnImpl) column, managedValue);
    }

    public AttributeRowImpl newNodeRow() {
        return new AttributeRowImpl(manager.getNodeClass());
    }

    public AttributeRowImpl newEdgeRow() {
        return new AttributeRowImpl(manager.getEdgeClass());
    }

    public AttributeRowImpl newRowForClass(String className) {
        AbstractAttributeClass attClass = manager.getClass(className);
        if (attClass != null) {
            return new AttributeRowImpl(attClass);
        }
        return null;
    }

    public void setManager(AbstractAttributeManager manager) {
        this.manager = manager;
    }
}
