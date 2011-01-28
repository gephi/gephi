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
package org.gephi.filters.spi;

import org.gephi.filters.api.Query;

/**
 * Filters are pruning the graph by keeping only nodes and edges that satisify
 * filters conditions. Filters are predicates or functions that reduce the graph
 * and therefore create sub-graphs.
 * <p>
 * Filters are the basic building blocks that are wrapped in queries and assembled to
 * make simple or complex conditions on nodes and edges.
 * <p>
 * Filters objects are built in {@link FilterBuilder}. Implementors should define
 * their own <code>FilterBuilder</code> class to propose new filter to users.
 *
 * @author Mathieu Bastian
 * @see Query
 */
public interface Filter {

    /**
     * Returns the filter's display name.
     * @return  the filter's dispaly name
     */
    public String getName();

    /**
     * Returns the filter properties. Property values can be get and set from
     * <code>FilterProperty</code> objects.
     * @return  the filter's properties
     */
    public FilterProperty[] getProperties();
}
