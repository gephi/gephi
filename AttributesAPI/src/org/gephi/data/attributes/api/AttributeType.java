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
package org.gephi.data.attributes.api;

import org.gephi.data.attributes.type.StringList;

/**
 * The different type an {@link AttributeColumn} can have.
 *
 * @author Mathieu Bastian
 */
public enum AttributeType {

    FLOAT(Float.class),
    DOUBLE(Double.class),
    INT(Integer.class),
    LONG(Long.class),
    BOOLEAN(Boolean.class),
    STRING(String.class),
    LIST_STRING(StringList.class);
    private final Class type;

    AttributeType(Class type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return type.getSimpleName();
    }

    public String getTypeString() {
        return super.toString();
    }

    public Class getType() {
        return type;
    }

    public Object parse(String str) {
        switch (this) {
            case FLOAT:
                return new Float(str);
            case DOUBLE:
                return new Double(str);
            case INT:
                return new Integer(str);
            case LONG:
                return new Long(str);
            case BOOLEAN:
                return new Boolean(str);
            case LIST_STRING:
                return new StringList(str);
        }
        return str;
    }
}
