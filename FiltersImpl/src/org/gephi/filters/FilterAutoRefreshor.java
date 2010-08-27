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
package org.gephi.filters;

import java.util.concurrent.atomic.AtomicBoolean;
import org.gephi.filters.api.FilterModel;
import org.gephi.graph.api.GraphEvent;
import org.gephi.graph.api.GraphListener;
import org.gephi.graph.api.GraphModel;
import org.openide.util.Exceptions;

/**
 *
 * @author Mathieu Bastian
 */
public class FilterAutoRefreshor extends Thread implements GraphListener {

    private static final int TIMER = 1000;
    private final GraphModel graphModel;
    private final FilterModelImpl filterModel;
    private boolean running = true;
    private AtomicBoolean refresh = new AtomicBoolean(false);

    public FilterAutoRefreshor(FilterModelImpl filterModel, GraphModel graphModel) {
        super("Filter Auto-Refresh");
        setDaemon(true);
        this.graphModel = graphModel;
        this.filterModel = filterModel;
    }

    @Override
    public void run() {
        while (running) {
            try {
                if (refresh.compareAndSet(true, false)) {
                    if (filterModel.getFilterThread() != null && filterModel.getCurrentQuery() != null) {
                        filterModel.getFilterThread().setRootQuery((AbstractQueryImpl) filterModel.getCurrentQuery());
                        System.out.println("Refresh");
                    }
                }
                Thread.sleep(TIMER);
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    public void setEnable(boolean enable) {
        System.out.println("setEnable"+enable);
        if (enable) {
            graphModel.addGraphListener(this);
        } else {
            graphModel.removeGraphListener(this);
            refresh.set(false);
        }
        if (!isAlive()) {
            start();
        }
    }

    public void graphChanged(GraphEvent event) {
        if (event.getSource().isMainView() && event.is(GraphEvent.EventType.ADD_EDGES,
                GraphEvent.EventType.ADD_NODES,
                GraphEvent.EventType.CLEAR_EDGES,
                GraphEvent.EventType.CLEAR_NODES,
                GraphEvent.EventType.MOVE_NODES,
                GraphEvent.EventType.REMOVE_EDGES,
                GraphEvent.EventType.REMOVE_NODES)) {
            refresh.set(true);
            System.out.println("set refresh true");
        }
    }

    public void setRunning(boolean running) {
        this.running = running;
        if (!running) {
            graphModel.removeGraphListener(this);
            refresh.set(false);
        }
    }
}
