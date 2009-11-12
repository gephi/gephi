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

import org.gephi.graph.api.Attributes;

/**
 *
 * @author Mathieu Bastian
 */
public interface AttributeRow extends Attributes {

    public void reset();

    public int countValues();

    public void setValues(AttributeRow attributeRow);

    public void setValue(AttributeValue value);

    public void setValue(AttributeColumn column, Object value);

    public void setValue(String column, Object value);

    public Object getValue(AttributeColumn column);

    public Object getValue(String column);

    public Object getValue(int index);

    public AttributeValue[] getValues();
}
