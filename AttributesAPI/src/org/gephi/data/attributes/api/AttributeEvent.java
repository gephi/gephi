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
 *
 * @author Mathieu Bastian
 */
public interface AttributeEvent {

    /**
     * <ul>
     * <li><b>ADD_COLUMN:</b> A column has been created. Source is the
     * <code>AttributeTable</code> and data is the column.</li>
     * <li><b>REMOVE_COLUMN:</b> A column has been removed. Source is the
     * <code>AttributeTable</code> and data is the column.</li>
     * </ul>
     */
    public enum EventType {

        ADD_COLUMN, REMOVE_COLUMN,
    };

    public EventType getEventType();

    public Object getSource();

    public Object getData();
}
