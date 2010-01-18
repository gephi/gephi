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

/**
 * Cell that contains the value for a particular {@link AttributeColumn} and
 * {@link AttributeRow}.
 * <p>
 * Cells are build from a {@link AttributeValueFactory}, that can be get from the
 * {@link AttributeModel}.
 *
 * @author Mathieu Bastian
 */
public interface AttributeValue {

    /**
     * Returns the column this value belongs.
     * 
     * @return  the column this value belongs
     */
    public AttributeColumn getColumn();

    /**
     * Returns the value. May be <code>null</code> or equal to the column's
     * default value.
     *
     * @return  the value or <code>null</code>
     * @see     AttributeColumn#getDefaultValue()
     */
    public Object getValue();
}
