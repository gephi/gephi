/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gephi.data.attributes.event;

import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeEventData;
import org.gephi.data.attributes.api.AttributeValue;

/**
 *
 * @author Mathieu Bastian
 */
public class AttributeEventDataImpl implements AttributeEventData {

    private AttributeColumn[] columns;
    private AttributeValue[] values;
    private Object[] objects;

    public AttributeColumn[] getAddedColumns() {
        return columns;
    }

    public AttributeColumn[] getRemovedColumns() {
        return columns;
    }

    public AttributeValue[] getTouchedValues() {
        return values;
    }

    public Object[] getTouchedObjects() {
        return objects;
    }

    public void setColumns(AttributeColumn[] columns) {
        this.columns = columns;
    }

    public void setValues(AttributeValue[] values) {
        this.values = values;
    }

    public void setObjects(Object[] objects) {
        this.objects = objects;
    }
}
