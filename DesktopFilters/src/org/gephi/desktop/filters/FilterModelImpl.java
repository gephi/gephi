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
package org.gephi.desktop.filters;

import java.util.ArrayList;
import java.util.List;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.gephi.filters.api.Filter;
import org.gephi.filters.api.FilterModel;
import org.gephi.graph.api.GraphController;
import org.openide.util.Lookup;

/**
 *
 * @author Mathieu Bastian
 */
public class FilterModelImpl implements FilterModel {

    List<Filter> filters = new ArrayList<Filter>();
    List<ChangeListener> listeners = new ArrayList<ChangeListener>();

    public FilterModelImpl() {
    }

    public Filter[] getFilters() {
        return filters.toArray(new Filter[0]);
    }

    public void addFilter(Filter filter) {
        filters.add(filter);
        GraphController gc = Lookup.getDefault().lookup(GraphController.class);
        gc.getModel().getGraphVisible().getView().addPredicate(filter.getPredicate());
        fireChangeEvent();
    }

    public void removeFilter(Filter filter) {
        filters.remove(filter);
    }

    public void addChangeListener(ChangeListener changeListener) {
        if (!listeners.contains(changeListener)) {
            listeners.add(changeListener);
        }
    }

    public void removeChangeListener(ChangeListener changeListener) {
        listeners.remove(changeListener);
    }

    private void fireChangeEvent() {
        ChangeEvent changeEvent = new ChangeEvent(this);
        for (ChangeListener changeListener : listeners) {
            changeListener.stateChanged(changeEvent);
        }
    }
}
