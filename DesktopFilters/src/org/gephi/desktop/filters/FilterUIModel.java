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
package org.gephi.desktop.filters;

import java.util.ArrayList;
import java.util.List;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.gephi.filters.api.Query;
import org.gephi.filters.spi.Category;
import org.gephi.filters.spi.Filter;

/**
 *
 * @author Mathieu Bastian
 */
public final class FilterUIModel {

    private Query selectedQuery;
    private List<Query> expandedQueryNodes;
    private List<Query> expandedParametersNodes;
    private List<Category> expandedCategoryNodes;
    private List<ChangeListener> listeners;

    public FilterUIModel() {
        listeners = new ArrayList<ChangeListener>();
        expandedQueryNodes = new ArrayList<Query>();
        expandedCategoryNodes = new ArrayList<Category>();
        expandedParametersNodes = new ArrayList<Query>();
    }

    public Query getSelectedQuery() {
        return selectedQuery;
    }

    public Query getSelectedRoot() {
        if (selectedQuery != null) {
            Query root = selectedQuery;
            while (root.getParent() != null) {
                root = root.getParent();
            }
            return root;
        }
        return null;
    }

    public void setSelectedQuery(Query query) {
        selectedQuery = query;
        fireChangeEvent();
    }

    public void setExpand(Query query, boolean expanded, boolean parametersExpanded) {
        if (expanded && !expandedQueryNodes.contains(query)) {
            expandedQueryNodes.add(query);
        } else if (!expanded) {
            expandedQueryNodes.remove(query);
        }

        if (parametersExpanded && !expandedParametersNodes.contains(query)) {
            expandedParametersNodes.add(query);
        } else if (!parametersExpanded) {
            expandedParametersNodes.remove(query);
        }
    }

    public void setExpand(Category category, boolean expanded) {
        if (expanded && !expandedCategoryNodes.contains(category)) {
            expandedCategoryNodes.add(category);
        } else if (!expanded) {
            expandedCategoryNodes.remove(category);
        }
    }

    public boolean isExpanded(Query query) {
        return expandedQueryNodes.contains(query);
    }

    public boolean isParametersExpanded(Query query) {
        return expandedParametersNodes.contains(query);
    }

    public boolean isExpanded(Category category) {
        return expandedCategoryNodes.contains(category);
    }

    public void addChangeListener(ChangeListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeChangeListener(ChangeListener listener) {
        listeners.remove(listener);
    }

    private void fireChangeEvent() {
        ChangeEvent evt = new ChangeEvent(this);
        for (ChangeListener listener : listeners) {
            listener.stateChanged(evt);
        }
    }
}
