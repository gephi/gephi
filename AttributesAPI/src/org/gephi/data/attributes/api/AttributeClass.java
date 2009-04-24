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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.data.attributes.api;

/**
 *
 * @author Mathieu Bastian
 */
public interface AttributeClass {

    public String getName();

    /**
     * Returns the current attributes columns.
     */
    public AttributeColumn[] getAttributeColumns();

    /**
     * Returns the number of column.
     */
    public int countAttributeColumns();

    /**
     * Create and add a new column to this class. The default origin is set at <code>DATA</code>.
     * <p>
     * The title of the column is the identifier.
     * @param id The identifier of the column.
     * @param type The type of the column.
     */
    public void addAttributeColumn(String id, AttributeType type);

    /**
     * Create and add a new column to this class.
     * <p>
     * The title of the column is the identifier.
     * @param id The identifier of the column.
     * @param type The type of the column.
     * @param origin The origin of the column.
     */
    public void addAttributeColumn(String id, AttributeType type, AttributeOrigin origin);

    /**
     * Create and add a new column to this class.
     * @param id The identifier of the column.
     * @param title The title of the column.
     * @param type The type of the column.
     * @param origin The origin of the column.
     * @param defaultValue The default value of the column.
     */
    public void addAttributeColumn(String id, String title, AttributeType type, AttributeOrigin origin, Object defaultValue);

    /**
     * Get the column at the current index of <code>null</code> if the index is not valid.
     */
    public AttributeColumn getAttributeColumn(int index);

    /**
     * Get the column with the given identifier or <code>null</code> if it is not found.
     */
    public AttributeColumn getAttributeColumn(String id);

     /**
     * Get the column which match the given parameters or <code>null</code> if it is not found.
     */
    public AttributeColumn getAttributeColumn(String title, AttributeType type);

    /**
     * Return true if the class has a column with the given title.
     */
    public boolean hasAttributeColumn(String title);
}
