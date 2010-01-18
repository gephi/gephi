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
 * Represents the data model, like a standard database would do. As a database,
 * contains a list of tables, where columns are defined. By default, a model
 * owns a <b>node</b> and <b>edge</b> table, but more could exist, depending
 * of the model implementation.
 * <p>
 * The model also provides factories that are linked to this model. Use row
 * factory to build new rows and value factory to push new values to these
 * rows. Columns are manipulated from the <code>AttributeTable</code> class.
 *
 * @author Mathieu Bastian
 * @see AttributeController
 */
public interface AttributeModel {

    /**
     * Returns the <b>node</b> table. Contains all the columns associated to
     * node elements.
     * <p>
     * An <code>AttributeModel</code> has always <b>node</b> and <b>edge</b>
     * tables by default.
     *
     * @return      the node table, contains node columns
     */
    public AttributeTable getNodeTable();

    /**
     * Returns the <b>edge</b> table. Contains all the columns associated to
     * edge elements.
     * <p>
     * An <code>AttributeModel</code> has always <b>node</b> and <b>edge</b>
     * tables by default.
     *
     * @return      the edge table, contains edge columns
     */
    public AttributeTable getEdgeTable();

    /**
     * Returns the <code>AttributeTable</code> which has the given <code>name</code>
     * or <code>null</code> if this table doesn't exist.
     *
     * @param name  the table's name
     * @return      the table that has been found, or <code>null</code>
     */
    public AttributeTable getTable(String name);

    /**
     * Returns all tables this model contains. By default, only contains
     * <b>node</b> and <b>edge</b> tables.
     *
     * @return      all the tables of this model
     */
    public AttributeTable[] getTables();

    /**
     * Return the value factory.
     * 
     * @return      the value factory
     */
    public AttributeValueFactory valueFactory();

    /**
     * Returns the row factory.
     *
     * @return      the row factory
     */
    public AttributeRowFactory rowFactory();

    /**
     * Merge <code>model</code> in this model. Makes the union of tables and
     * columns of both models. Copy tables this model don't
     * have and merge existing ones. For existing tables, call
     * {@link AttributeTable#mergeTable(AttributeTable)}
     * to merge columns.
     * <p>
     * Columns are compared according to their <code>id</code> and <code>type</code>.
     * Columns found in <code>model</code> are appended only if they no column
     * exist with the same <code>id</code> and <code>type</code>.
     *
     * @param model the model that is to be merged in this model
     */
    public void mergeModel(AttributeModel model);
}
