/*
 * Copyright 2008-2010 Gephi
 * Authors : Cezary Bartosiak
 * Website : http://www.gephi.org
 * 
 * This file is part of Gephi.
 *
 * Gephi is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Gephi is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gephi.dynamic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import org.gephi.data.attributes.type.TimeInterval;
import org.gephi.dynamic.api.DynamicController;
import org.gephi.dynamic.api.DynamicModel;
import org.gephi.dynamic.api.DynamicModelEvent;
import org.gephi.dynamic.api.DynamicModelListener;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.project.api.WorkspaceListener;
import org.gephi.project.api.WorkspaceProvider;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 * It is the default implementation of the {@code DynamicController} class.
 *
 * @author Cezary Bartosiak
 */
@ServiceProvider(service = DynamicController.class)
public final class DynamicControllerImpl implements DynamicController {

    private DynamicModelImpl model;
    private List<DynamicModelListener> listeners;
    private DynamicModelEventDispatchThread eventThread;

    /**
     * The default constructor.
     */
    public DynamicControllerImpl() {
        listeners = Collections.synchronizedList(new ArrayList<DynamicModelListener>());
        eventThread = new DynamicModelEventDispatchThread();
        eventThread.start();

        ProjectController projectController = Lookup.getDefault().lookup(ProjectController.class);
        projectController.addWorkspaceListener(new WorkspaceListener() {

            @Override
            public void initialize(Workspace workspace) {
                workspace.add(new DynamicModelImpl(DynamicControllerImpl.this, workspace));
            }

            @Override
            public void select(Workspace workspace) {
                model = workspace.getLookup().lookup(DynamicModelImpl.class);
                if (model == null) {
                    model = new DynamicModelImpl(DynamicControllerImpl.this, workspace);
                    workspace.add(model);
                }
            }

            @Override
            public void unselect(Workspace workspace) {
            }

            @Override
            public void close(Workspace workspace) {
            }

            @Override
            public void disable() {
                model = null;
            }
        });
        if (projectController.getCurrentProject() != null) {
            Workspace[] workspaces = projectController.getCurrentProject().getLookup().
                    lookup(WorkspaceProvider.class).getWorkspaces();
            for (Workspace workspace : workspaces) {
                DynamicModelImpl m = (DynamicModelImpl) workspace.getLookup().lookup(DynamicModelImpl.class);
                if (m == null) {
                    m = new DynamicModelImpl(this, workspace);
                    workspace.add(m);
                }
                if (workspace == projectController.getCurrentWorkspace()) {
                    model = m;
                }
            }
        }
    }

    @Override
    public DynamicModel getModel() {
        return model;
    }

    @Override
    public DynamicModel getModel(Workspace workspace) {
        if (workspace != null) {
            DynamicModel m = workspace.getLookup().lookup(DynamicModel.class);
            if (m != null) {
                return m;
            }
            m = new DynamicModelImpl(this, workspace);
            workspace.add(m);
            return m;
        }
        return null;
    }

    @Override
    public void setVisibleInterval(TimeInterval interval) {
        if (model != null) {
            model.setVisibleTimeInterval(interval);
        }
    }

    @Override
    public void setVisibleInterval(double low, double high) {
        setVisibleInterval(new TimeInterval(low, high));
    }

    @Override
    public void addModelListener(DynamicModelListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    @Override
    public void removeModelListener(DynamicModelListener listener) {
        listeners.remove(listener);
    }

    public void fireModelEvent(DynamicModelEvent event) {
        eventThread.fireEvent(event);
    }

    protected class DynamicModelEventDispatchThread extends Thread {

        private boolean stop;
        private final LinkedBlockingQueue<DynamicModelEvent> eventQueue;
        private final Object lock = new Object();

        public DynamicModelEventDispatchThread() {
            super("Dynamic Model EventDispatchThread");
            setDaemon(true);
            this.eventQueue = new LinkedBlockingQueue<DynamicModelEvent>();
        }

        @Override
        public void run() {
            while (!stop) {
                DynamicModelEvent evt;
                while ((evt = eventQueue.poll()) != null) {
                    for (DynamicModelListener l : listeners.toArray(new DynamicModelListener[0])) {
                        l.dynamicModelChanged(evt);
                    }
                }

                while (eventQueue.isEmpty()) {
                    try {
                        synchronized (lock) {
                            lock.wait();
                        }
                    } catch (InterruptedException e) {
                    }
                }
            }
        }

        public void stop(boolean stop) {
            this.stop = stop;
        }

        public void fireEvent(DynamicModelEvent event) {
            eventQueue.add(event);
            synchronized (lock) {
                lock.notifyAll();
            }
        }
    }
}
