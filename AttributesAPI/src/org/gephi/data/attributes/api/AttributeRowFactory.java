/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>, Cezary Bartosiak
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

/**
 * Factory which is building exclusively {@link AttributeRow}. It can be get
 * from the {@link AttributeModel#rowFactory()}.
 *
 * @author Mathieu Bastian
 */
public interface AttributeRowFactory {

    /**
     * Returns a new row for the <b>node</b> table.
     *
     * @return  a newly created row for the node table
     * @see     AttributeModel#getNodeTable()
     */
    public AttributeRow newNodeRow();

    /**
     * Returns a new row for the <b>edge</b> table.
     *
     * @return  a newly created row for the edge table
     * @see     AttributeModel#getEdgeTable()
     */
    public AttributeRow newEdgeRow();

    /**
     * Returns a new row for the given <code>tableName</code>, or <code>null</code>
     * if no table with this name exists.
     *
     * @return  a newly created row for the given table, or <code>null</code>
     *          otherwise
     * @see AttributeModel#getTable(java.lang.String) 
     */
    public AttributeRow newRowForTable(String tableName);
}
