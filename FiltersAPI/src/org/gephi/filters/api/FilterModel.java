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

import javax.swing.event.ChangeListener;
import org.gephi.filters.spi.FilterBuilder;

/**
 * The Filter Model hosts the queries defined in the system and the currently
 * active query. It also stroe the selection or filtering flag. The filtering mode
 * display the subgraph made from filters, whereas the selection mode highlight
 * elements on the graph.
 *
 * @author Mathieu Bastian
 * @see FilterController
 */
public interface FilterModel {

    /**
     * Returns the <code>FilterLibrary</code>, where {@link FilterBuilder}
     * belongs to.
     * @return          the filter library
     */
    public FilterLibrary getLibrary();

    /**
     * Returns all queries in the model, represented by their root query.
     * @return          all root queries in the model
     */
    public Query[] getQueries();

    /**
     * Returns the query currently active or <code>null</code> if none is active.
     * @return          the current query
     */
    public Query getCurrentQuery();

    /**
     * Returns <code>true</code> if the system is currently in filtering mode.
     * @return          <code>true</code> if the result graph is filtered,
     * <code>false</code> if it's in selection mode
     */
    public boolean isFiltering();

    /**
     * Returns <code>true</code> if the system is currently in selection mode.
     * @return          <code>true</code> if the result is selected on the graph,
     * <code>false</code> if it's filtered
     */
    public boolean isSelecting();

    public boolean isAutoRefresh();

    public void addChangeListener(ChangeListener listener);

    public void removeChangeListener(ChangeListener listener);
}
