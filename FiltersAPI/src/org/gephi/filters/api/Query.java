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
package org.gephi.filters.api;

import org.gephi.filters.spi.Filter;

/**
 * Queries are wrapping filters and are assembled in a query tree. Each query is
 * built from a single filter instance and it's role is to basically to execute
 * the filter. The graph that is passed to the filter depends on the fact the query
 * belongs to a complex query tree or if the tree is a single leaf.
 * <p>
 * The system works like this. Leaves of the query tree receives the complete
 * graph and the subgraphs they return are passed to the parent query. Thus the
 * root query is the last query to get the subgraphs and returns the final result.
 * This querying system make possible to create query chains and complex scenario
 * with various operators (AND, OR, ...).
 * <p>
 * Queries are built by the <code>FilterController</code> from filter instances.
 *
 * @author Mathieu Bastian
 * @see FilterController
 */
public interface Query {

    /**
     * Returns query's full name.
     * @return      query's name
     */
    public String getName();

    /**
     * Returns queries that are children of this query.
     * @return      query's children
     */
    public Query[] getChildren();

    /**
     * Returns the limit number of children this query can have. Return 1 for a
     * standard query.
     * @return      the number of allowed children query
     */
    public int getChildrenSlotsCount();

    /**
     * Returns the parent query or <code>null</code> if this query is root.
     * @return      the query's parent query, or <code>null</code>
     */
    public Query getParent();

    /**
     * Returns the number of properties this query has.
     * @return      the query's number of properties
     */
    public int getPropertiesCount();

    /**
     * Returns the name of the property at the specified <code>index</code>.
     * @param index the index of the property
     * @return      the query's property name
     * @throws ArrayIndexOutOfBoundsException if <code>index</code> is out of
     * bounds
     */
    public String getPropertyName(int index);

    /**
     * Returns the value of the property at the specified <code>index</code>.
     * @param index the index of the property
     * @return      the query's property value
     * @throws ArrayIndexOutOfBoundsException if <code>index</code> is out of
     * bounds
     */
    public Object getPropertyValue(int index);

    /**
     * Utility method that returns all queries in this query hierarchy that are
     * <code>filterClass</code> instance.
     * @param filterClass the filter class that is to be queried
     * @return      all queries, including self that are <code>filterClass</code>
     * instance
     */
    public Query[] getQueries(Class<? extends Filter> filterClass);

    /**
     * Returns the filter this query is wrapping.
     * @return      the filter
     */
    public Filter getFilter();
}
