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

    public void setCurrentQuery(Query query);

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
