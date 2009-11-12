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

import org.openide.util.Lookup;
import org.openide.util.LookupListener;

/**
 *
 * @author Mathieu Bastian
 */
public interface AttributeTable {

    /**
     * Returns the name of the class.
     * @return the name of the class
     */
    public String getName();

    /**
     * Returns the current attributes columns.
     */
    public AttributeColumn[] getColumns();

    /**
     * Returns the number of column.
     */
    public int countColumns();

    /**
     * Creates and add a new column to this class. The default origin is set at <code>DATA</code>.
     * <p>
     * The title of the column is the identifier.
     * @param id The identifier of the column.
     * @param type The type of the column.
     */
    public AttributeColumn addColumn(String id, AttributeType type);

    /**
     * Create and add a new column to this class.
     * <p>
     * The title of the column is the identifier.
     * @param id The identifier of the column.
     * @param type The type of the column.
     * @param origin The origin of the column.
     */
    public AttributeColumn addColumn(String id, AttributeType type, AttributeOrigin origin);

    /**
     * Create and add a new column to this class.
     * @param id The identifier of the column.
     * @param title The title of the column.
     * @param type The type of the column.
     * @param origin The origin of the column.
     * @param defaultValue The default value of the column.
     */
    public AttributeColumn addColumn(String id, String title, AttributeType type, AttributeOrigin origin, Object defaultValue);

    /**
     * If exists, remove the column and all rows values.
     * @param column The column to remove
     */
    public void removeColumn(AttributeColumn column);

    /**
     * Get the column at the current index of <code>null</code> if the index is not valid.
     */
    public AttributeColumn getColumn(int index);

    /**
     * Get the column with the given identifier or <code>null</code> if it is not found.
     */
    public AttributeColumn getColumn(String id);

    /**
     * Get the column which match the given parameters or <code>null</code> if it is not found.
     */
    public AttributeColumn getColumn(String title, AttributeType type);

    /**
     * Return true if the class has a column with the given title.
     */
    public boolean hasColumn(String title);

    /**
     * Return the table's lookup, which contains {@link AttributeColumn} objects. Add a
     * {@link LookupListener} to be notified when columns are added or removed.
     * @return the table's columns <code>Lookup</code>
     */
    public Lookup getLookup();

    /**
     * Merge this table with the <code>table</code> given in parameter. New columns from
     * <code>table</code> are added to this table. Columns are compared according to their
     * <code>identifier</code> and <code>type</code>.
     * @param table the table that is to be merged with this table
     */
    public void mergeTable(AttributeTable table);
}
