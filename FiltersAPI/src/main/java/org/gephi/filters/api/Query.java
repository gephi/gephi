/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
Website : http://www.gephi.org

This file is part of Gephi.

DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

Copyright 2011 Gephi Consortium. All rights reserved.

The contents of this file are subject to the terms of either the GNU
General Public License Version 3 only ("GPL") or the Common
Development and Distribution License("CDDL") (collectively, the
"License"). You may not use this file except in compliance with the
License. You can obtain a copy of the License at
http://gephi.org/about/legal/license-notice/
or /cddl-1.0.txt and /gpl-3.0.txt. See the License for the
specific language governing permissions and limitations under the
License.  When distributing the software, include this License Header
Notice in each file and include the License files at
/cddl-1.0.txt and /gpl-3.0.txt. If applicable, add the following below the
License Header, with the fields enclosed by brackets [] replaced by
your own identifying information:
"Portions Copyrighted [year] [name of copyright owner]"

If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 3, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 3] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 3 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 3 code and therefore, elected the GPL
Version 3 license, then the option applies only if the new code is
made subject to such option by the copyright holder.

Contributor(s):

Portions Copyrighted 2011 Gephi Consortium.
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
     * Utility method that returns all descendant queries plus this query.
     * @return      all descendant queries and self
     */
    public Query[] getDescendantsAndSelf();

    /**
     * Returns the filter this query is wrapping.
     * @return      the filter
     */
    public Filter getFilter();
}
