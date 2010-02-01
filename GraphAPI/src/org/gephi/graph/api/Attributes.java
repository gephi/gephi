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


package org.gephi.graph.api;

/**
 * Get and set any object to this attributes class. See <b>AttributesAPI</b>
 * module to look at columns definition. This interface is extended by
 * <code>AttributeRow</code> that one can find in the AttributesAPI. Cast this
 * class to <code>AttributeRow</code> to profit from all features.
 * <p>
 * In few words, attribute values can be get with the column index or id.
 * 
 * @author Mathieu Bastian
 */
public interface Attributes {

    /**
     * Returns the number of values this row has.
     * @return          the number of values
     */
    public int countValues();

    /**
     * Returns the value located at the specified column.
     * @param column    the column <b>id</b> or <b>title</b>
     * @return          the value for the specified column position, or
     *                  <code>null</code> if not found
     */
    public Object getValue(String column);

    /**
     * Returns the value located at the specified column position.
     * @param column    the column index
     * @return          the value for the specified column position, or
     *                  <code>null</code> if index out of range
     */
    public Object getValue(int index);

    /**
     * Sets the value for a specified column. Accepts <code>null</code> values.
     * @param column    the column <b>id</b> or <b>title</b>
     * @param value     the value that is to be set at the specified column position
     */
    public void setValue(String column, Object value);

    /**
     * Sets the value for a specified column position. Accepts <code>null</code> values.
     * @param index     the column index
     * @param value     the value that is to be set at the specified column position
     */
    public void setValue(int index, Object value);

    /**
     * Resets the content of the row.
     */
    public void reset();
}
