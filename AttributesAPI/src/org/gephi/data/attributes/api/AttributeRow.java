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
 * Rows contains <code>AttributeValue</code>, one for each column. Rows are
 * not stored in columns and nor in tables, they are stored in the object that
 * possess the row, for instance <code>Nodes</code> or <code>Edges</code>.
 * <p>
 * But colums are fixed, stored in <code>AttributeTable</code>. Rows always
 * contains the values in the same order as columns are described in the table.
 * <p>
 * For instance, if an table contains a single column <b>label</b>, the column
 * index is equal to <b>0</b> and the value can be retrieved in the following ways:
 * <ul>
 * <li><code>row.getValue(column)</code></li>
 * <li><code>row.getValue("label")</code></li>
 * <li><code>row.getValue(0)</code></li>
 * </ul>
 * Rows are build from a {@link AttributeRowFactory}, that can be get from the
 * {@link AttributeModel}.
 *
 * @author Mathieu Bastian
 * @see AttributeColumn
 * @see AttributeTable
 * @see AttributeValue
 */
public interface AttributeRow extends Attributes {

    /**
     * Resets all data in the row.
     */
    public void reset();

    /**
     * Returns the number of values this rows contains. Equal to the number of
     * columns of the <code>AttributeTable</code> this row belongs.
     * 
     * @return          the size of the values array
     */
    public int countValues();

    /**
     * Sets values from another row. Values must have existing column in the
     * current table.
     *
     * @param row       an existing row that may refer to the same columns
     */
    public void setValues(AttributeRow row);

    /**
     * Sets a value for this row. If the <code>column</code> retrieved from
     * <code>value</code> cannot be found at the same index, the column
     * <code>Id</code> is used to find the column.
     *
     * @param value     a value that refers to an existing column for this row
     */
    public void setValue(AttributeValue value);

    /**
     * Sets a value at the specified column index.
     *
     * @param column    a column that exists for this row
     * @param value     the value that is to be set a the specified column index
     */
    public void setValue(AttributeColumn column, Object value);

    /**
     * Sets a value at the specified column index, if column is found. The
     * column is found if <code>column</code> refers to an existing column
     * <code>id</code> or <code>title</code>.
     *
     * @param column    a column <code>id</code> or <code>title</code>
     * @param value     the value that is to be set if <code>column</code> is found
     */
    public void setValue(String column, Object value);

    /**
     * Sets a value at the specified column index, if <code>index</code> is in
     * range. This is equivalent as
     * <code>setValue(AttributeColumn.getIndex(), Object)</code>.
     *
     * @param index     a valid column index
     * @param value     the value that is to be set if <code>index</code> is valide
     */
    public void setValue(int index, Object value);

    /**
     * Returns the value found at the specified column index. May return
     * <code>null</code> if the value is <code>null</code> or if the column
     * doesn't exist.
     * 
     * @param column    a column that exists for this row
     * @return          the value found at the specified column index or
     *                  <code>null</code> otherwise
     */
    public Object getValue(AttributeColumn column);

    /**
     * Returns the value at the specified column, if found. The
     * column is found if <code>column</code> refers to an existing column
     * <code>id</code> or <code>title</code>.
     *
     * @param column    a column <code>id</code> or <code>title</code>
     * @return          the value found at the specified column or
     *                  <code>null</code> otherwise
     */
    public Object getValue(String column);

    /**
     * Returns the value at the specified index, if <code>index</code> is in range.
     * This is equivalent as <code>getValue(AttributeColumn.getIndex())</code>.
     *
     * @param index     a valid column index
     * @return          the value found at the specified column or
     *                  <code>null</code> otherwise
     * @see             AttributeColumn#getIndex()
     */
    public Object getValue(int index);

    /**
     * Returns the value array. Each <code>AttributeValue</code> is a pair between
     * a data and the column it belongs.
     *
     * @return          the value array of this row
     */
    public AttributeValue[] getValues();
}
