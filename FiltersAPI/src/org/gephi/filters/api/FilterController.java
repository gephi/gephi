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

import org.gephi.filters.spi.Filter;
import org.gephi.graph.api.GraphView;
import org.gephi.project.api.Workspace;

/**
 * Use filters and queries for filtering the Graph. This controller manages
 * <code>FilterModel</code> instances. A model is defined for each workspace.
 * <p>
 * This controller is a singleton and can therefore be found in Lookup:
 * <pre>FilterController fc = Lookup.getDefault().lookup(FilterController.class);</pre>
 * <p>
 * The controller has two ways to execute filtering, a one-shot one that
 * immediately returns the <code>GraphView</code> and a more complex one suitable
 * for user interface interaction, with live parameter change.
 * <p>
 * The one-shot filtering can be executed like below:
 * <pre>
 * Filter filter = ...
 * Query query = controller.createQuery(filter);
 * GraphView view = controller.filter(query);
 * </pre>
 * The normal mode is to call {@link #filterVisible(org.gephi.filters.api.Query)}
 * which let this controller manage the execution. The benefit of this of this mode
 * is that properties change on filters are listened and filtering is automatically
 * reexecuted if values changes. See how to execute a filter with two different
 * values:
 * <pre>
 * Filter filter = ...
 * filter.getProperties()[0].setValue(1);       //Set value 1, for example a threshold
 * Query query = controller.createQuery(filter);
 * controller.add(query);
 * controller.filterVisible(query);     //A background thread executes the query
 * filter.getProperties[0].setValue(2)      //The background thread reexecute the query
 * </pre>
 * @author Mathieu Bastian
 * @see GraphView
 */
public interface FilterController {

    /**
     * Creates a query from <code>filter</code>. The created query is a root query.
     * @param filter        the filter that is to be wrapped in a new query
     * @return              a query that is wrapping <code>filter</code>
     */
    public Query createQuery(Filter filter);

    /**
     * Adds <code>query</code> as a new query in the system. The query should be
     * a root query.
     * @param query         the query that is to be added
     */
    public void add(Query query);

    /**
     * Removes <code>query</code> from the systemn if exists.
     * @param query         the query that is to be removed
     */
    public void remove(Query query);

    /**
     * Renames <code>query</code> with <code>name</code>.
     * @param query         the query that is to be renamed
     * @param name          the new query's name
     */
    public void rename(Query query, String name);

    /**
     * Sets <code>subQuery</code> as a child of <code>query</code>. If
     * <code>subQuery</code> already has a parent query, it will be removed first.
     * @param query         the query that <code>subQuery</code> is to be added
     * as a new child
     * @param subQuery      the query that is to be added as a child of <code>
     * query</code>
     */
    public void setSubQuery(Query query, Query subQuery);

    /**
     * Removes <code>query</code> from <code>parent</code> query.
     * @param query         the query that is to be removed from <code>parent</code>
     * @param parent        the query that <code>query</code> is to be removed as
     * a child
     */
    public void removeSubQuery(Query query, Query parent);

    /**
     * Filters main graph with <code>query</code> and set result as the new
     * visible graph. Note that the query will be executed in a background thread
     * and results delivered as soon as ready. Then, <code>query</code> is defined
     * as the currently active query and property's value changes are watched.
     * If a query's property is changed the query is automatically reexecuted.
     * @param query         the query that is to be executed
     */
    public void filterVisible(Query query);

    /**
     * Selects <code>query</code> results on the main graph visualization
     * window. Note that the query will be executed in a background thread
     * and results delivered as soon as ready. Then, <code>query</code> is defined
     * as the currently active query and property's value changes are watched.
     * If a query's property is changed the query is automatically reexecuted.
     * @param query         the query that is to be executed
     */
    public void selectVisible(Query query);

    /**
     * Filtering method for API users. The <code>query</code> is executed and
     * the <code>GraphView</code> result is returned.
     * @param query         the query that is to be executed
     * @return              a graph view that represents the query result
     */
    public GraphView filter(Query query);

    /**
     * Exports <code>query</code> result in a new column <code>title</code>.
     * Nodes and edges that pass the <code>query</code> have <b>true</b> value and
     * <b>false</b> for others.
     * @param title         the column's title
     * @param query         the query that is to be executed
     */
    public void exportToColumn(String title, Query query);

    /**
     * Exports <code>query</code> result in a new workspace. Note that query is
     * executed in a separate thread and the workspace may not be ready immediately
     * when this method returns.
     * @param query         the query that is to be executed
     */
    public void exportToNewWorkspace(Query query);

    /**
     * Exports <code>query</code> result to visible/hidden labels. Each node and
     * edge not present in the query result has its label set hidden. Label
     * visibility is controlled from <code>TextData</code> object, accessible from
     * <code>NodeData</code> or <code>EdgeData</code>.
     * @param query         the query that is to be used to hide labels
     */
    public void exportToLabelVisible(Query query);

    public void setAutoRefresh(boolean autoRefresh);

    /**
     * Returns the filter's model.
     * @return              the filter's model
     */
    public FilterModel getModel();

    /**
     * Returns the filter's model for <code>workspace</code>.
     * @return              the filter's model in the given workspace
     */
    public FilterModel getModel(Workspace workspace);
}
