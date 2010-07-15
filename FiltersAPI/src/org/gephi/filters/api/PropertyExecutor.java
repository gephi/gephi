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
package org.gephi.filters.api;

import org.gephi.filters.spi.FilterProperty;

/**
 * PropertyExecutor's role is to synchronize property edition with filter execution.
 * When a filter is executed it usually uses properties users can edit. Editing
 * properties values while a filter is executing in another thread could make
 * uncertain behaviour. This executor is responsible to postpone value edition
 * until filter's execution is finished.
 *
 * @author Mathieu Bastian
 * @see FilterProperty
 */
public interface PropertyExecutor {

    /**
     * Set <code>value</code> on <code>property</code> in a safe way by using
     * <code>callback</code>.
     * @param property  the filter property that value is to be set
     * @param value     the value that is to be set
     * @param callback  the callback function to be notified when setting has to
     * be done
     */
    public void setValue(FilterProperty property, Object value, Callback callback);

    /**
     * Callback interface for setting value. When called, setting value is done
     * in a safe window between filter execution.
     */
    public interface Callback {

        public void setValue(Object value);
    }
}
