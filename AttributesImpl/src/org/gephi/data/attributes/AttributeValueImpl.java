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

import org.gephi.data.attributes.api.AttributeOrigin;
import org.gephi.data.attributes.api.AttributeValue;
import org.gephi.data.attributes.spi.AttributeValueDelegateProvider;

/**
 *
 * @author Mathieu Bastian
 * @author Martin Škurla
 */
public final class AttributeValueImpl implements AttributeValue {

    private final AttributeColumnImpl column;
    private final Object value;

    public AttributeValueImpl(AttributeColumnImpl column, Object value) {
        this.column = column;
        this.value = value;
    }

    public AttributeColumnImpl getColumn() {
        return column;
    }

    public Object getValue() {
        if (column.getOrigin() != AttributeOrigin.DELEGATE) {
            return value;
        }
        else {
            if (value == null)
                return null;

            AttributeValueDelegateProvider attributeValueDelegateProvider = column.getProvider();

            if (AttributeUtilsImpl.getDefault().isEdgeColumn(column))
                return attributeValueDelegateProvider.getEdgeValue(column, value);
            else if (AttributeUtilsImpl.getDefault().isNodeColumn(column))
                return attributeValueDelegateProvider.getNodeValue(column, value);
            else
                throw new AssertionError();
        }
    }
}
