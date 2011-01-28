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
package org.gephi.data.attributes.api;

import org.gephi.data.attributes.spi.AttributeValueDelegateProvider;

/**
 * Column is the header of a data column. It belongs to an <code>AttributeTable</code>
 * and is the key to access data within <code>AttributeRow</code>.
 * <p>
 * It contains its index that may be used to get the appropriate value in the
 * <code>AttributeRow</code> values array.
 * <p>
 * For Gephi internal implementation purposes, names of columns are restricted. They can have any name
 * except these defined in {@link org.gephi.data.properties.PropertiesColumn PropertiesColumn} enum.
 * <h2>Iterate rows values</h2>
 * <pre>
 * Attribute row = ...;
 * for(AttributeColumn column : table.getColumns()) {
 *      Object value = row.getValue(column);
 * }
 * </pre>
 * 
 * @author Mathieu Bastian
 * @author Martin Škurla
 * @see AttributeRow
 * @see AttributeTable
 */
public interface AttributeColumn {

    /**
     * Returns the type of this column content.
     *
     * @return  the type of this column
     */
    public AttributeType getType();

    /**
     * Returns the title of this column. The title is a human-readable text that
     * describes the column data. When no title exists, returns the <code>Id</code>
     * of this column.
     *
     * @return  the title of this column, if exists, or the <code>Id</code> otherwise
     */
    public String getTitle();

    /**
     * Returns the index of this column. The index is the fastest way to access a
     * column from its <code>AttributeTable</code> or manipulate
     * <code>AttributeRow</code>.
     * 
     * @return  the index of this column
     * @see     AttributeTable#getColumn(int)
     * @see     AttributeRow#getValue(int)
     */
    public int getIndex();

    /**
     * Returns the origin of this column content, meta-data that describes where
     * the column comes from. Default value is <code>AttributeOrigin.DATA</code>.
     *
     * @return  the origin of this column content
     */
    public AttributeOrigin getOrigin();

    /**
     * Returns the id of this column. The id is the unique identifier that describes
     * the column data.
     *
     * @return  the id of this column
     */
    public String getId();

    /**
     * Returns the default value for this column. May be <code>null</code>.
     * <p>
     * The returned <code>Object</code> class type is equal to the class obtained
     * with <code>AttributeType.getType()</code>.
     *
     * @return  the default value, or <code>null</code>
     */
    public Object getDefaultValue();

    /**
     * Returns the attribute value delegate provider. The Provider is always set if the origin of the
     * current attribute column is AttributeOrigin.DELEGATE.
     *
     * @return attribute value delegate provider
     */
    public AttributeValueDelegateProvider getProvider();
}
