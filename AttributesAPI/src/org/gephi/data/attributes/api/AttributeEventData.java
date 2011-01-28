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
 * Data associated with an attribute event.
 * 
 * @author Mathieu Bastian
 * @see AttributeEvent
 */
public interface AttributeEventData {

    /**
     * Returns columns that have been added. Look at {@link AttributeEvent#getSource() }
     * to know to which <code>AttributeTable</code>.
     * @return the added columns
     */
    public AttributeColumn[] getAddedColumns();

    /**
     * Returns columns that have been removed. Look at {@link AttributeEvent#getSource() }
     * to know from which <code>AttributeTable</code>.
     * @return the removed columns
     */
    public AttributeColumn[] getRemovedColumns();

    /**
     * Returns objects where attribute values have been modified. Objects are
     * either <code>NodeData</code> or <code>EdgeData</code>. The index of the
     * returned array is matching with values from <code>getTouchedValues()</code>.
     * @return the objects modified with the <code>SET_VALUE</code>
     *         event
     */
    public Object[] getTouchedObjects();

    /**
     * Returns values with the <code>SET_VALUE</code> event. The
     * <code>AttributeValue</code> object contains the new value that has been set.
     * The index of the array is matching with values from <code>getTouchedObjects()</code>.
     * @return the new values set with the <code>SET_VALUE</code> event.
     */
    public AttributeValue[] getTouchedValues();
}
