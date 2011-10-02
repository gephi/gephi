/*
 * Copyright 2008-2010 Gephi
 * Authors : Cezary Bartosiak
 *           Mathieu Bastian <mathieu.bastian@gephi.org>
 * Website : http://www.gephi.org
 * 
 * This file is part of Gephi.
 *
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
package org.gephi.dynamic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import org.gephi.data.attributes.api.Estimator;
import org.gephi.data.attributes.type.TimeInterval;
import org.gephi.dynamic.api.DynamicController;
import org.gephi.dynamic.api.DynamicModel;
import org.gephi.dynamic.api.DynamicModel.TimeFormat;
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
 * @author Mathieu Bastian
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
    public synchronized DynamicModel getModel() {
        if (model == null) {
            ProjectController projectController = Lookup.getDefault().lookup(ProjectController.class);
            if (projectController.getCurrentWorkspace() != null) {
                Workspace workspace = projectController.getCurrentWorkspace();
                return workspace.getLookup().lookup(DynamicModel.class);
            }
        }
        return model;
    }

    @Override
    public synchronized  DynamicModel getModel(Workspace workspace) {
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
            //System.out.println("set visible interval "+interval);
            model.setVisibleTimeInterval(interval);
        }
    }

    @Override
    public void setVisibleInterval(double low, double high) {
        setVisibleInterval(new TimeInterval(low, high));
    }

    @Override
    public void setTimeFormat(TimeFormat timeFormat) {
        if (model != null) {
            model.setTimeFormat(timeFormat);
        }
    }

    @Override
    public void setEstimator(Estimator estimator) {
        if (model != null) {
            model.setEstimator(estimator);
        }
    }

    @Override
    public void setNumberEstimator(Estimator numberEstimator) {
        if (model != null) {
            model.setNumberEstimator(numberEstimator);
        }
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
