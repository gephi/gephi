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

/**
 * Factory which is building exclusively {@link AttributeValue}. It can be get
 * from the {@link AttributeModel#valueFactory()}.
 *
 * @author Mathieu Bastian
 */
public interface AttributeValueFactory {

    /**
     * Returns a new cell value for the given <code>column</code> and
     * <code>value</code>. The <code>value</code> can be <code>null</code>.
     * <p>
     * The <code>value</code> type should be compatible with the column type.
     *
     * @param column    the column where the cell belongs
     * @param value     a compatible value, or <code>null</code>
     * @return          the new value for the given column
     */
    public AttributeValue newValue(AttributeColumn column, Object value);
}
