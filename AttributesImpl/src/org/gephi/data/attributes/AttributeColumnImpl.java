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
import org.gephi.data.attributes.api.AttributeOrigin;
import org.gephi.data.attributes.api.AttributeType;
import org.gephi.data.attributes.spi.AttributeValueDelegateProvider;

/**
 *
 * @author Mathieu Bastian
 * @author Martin Škurla
 */
public class AttributeColumnImpl implements AttributeColumn {

    protected final AttributeTableImpl table;
    protected final int index;
    protected final String id;
    protected final String title;
    protected final AttributeType type;
    protected final AttributeOrigin origin;
    protected final AttributeValueImpl defaultValue;
    protected final AttributeValueDelegateProvider attributeValueDelegateProvider;

    public AttributeColumnImpl(AttributeTableImpl table, int index, String id, String title, AttributeType attributeType, AttributeOrigin origin, Object defaultValue, AttributeValueDelegateProvider attributeValueDelegateProvider) {
        this.table = table;
        this.index = index;
        this.id = id;
        this.type = attributeType;
        this.title = title;
        this.origin = origin;
        this.attributeValueDelegateProvider = attributeValueDelegateProvider;
        this.defaultValue = new AttributeValueImpl(this, defaultValue);
    }

    public AttributeTableImpl getTable() {
        return table;
    }

    public AttributeType getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public int getIndex() {
        return index;
    }

    public AttributeOrigin getOrigin() {
        return origin;
    }

    public String getId() {
        return id;
    }

    public Object getDefaultValue() {
        return defaultValue.getValue();
    }

    public AttributeValueDelegateProvider getProvider() {
        return attributeValueDelegateProvider;
    }

    @Override
    public String toString() {
        return title + " (" + type.toString() + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof AttributeColumn) {
            AttributeColumnImpl o = (AttributeColumnImpl) obj;
            return id.equals(o.id) && o.type == type;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + (this.id != null ? this.id.hashCode() : 0);
        hash = 53 * hash + (this.type != null ? this.type.hashCode() : 0);
        return hash;
    }
}
