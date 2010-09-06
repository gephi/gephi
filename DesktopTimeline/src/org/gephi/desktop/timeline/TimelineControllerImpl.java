/*
Copyright 2008-2010 Gephi
Authors : Julian Bilcke <julian.bilcke@gephi.org>
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
package org.gephi.desktop.timeline;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.SwingUtilities;
import org.gephi.dynamic.api.DynamicController;
import org.gephi.dynamic.api.DynamicModel;
import org.gephi.project.api.ProjectController;
import org.gephi.timeline.api.TimelineController;
import org.gephi.timeline.api.TimelineModel;
import org.gephi.project.api.Workspace;
import org.gephi.project.api.WorkspaceListener;
import org.gephi.timeline.api.TimelineModelEvent;
import org.gephi.timeline.api.TimelineModelListener;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 *
 * @author Julian Bilcke
 */
@ServiceProvider(service = TimelineController.class)
public class TimelineControllerImpl implements TimelineController {

    private TimelineModel model;
    private final List<TimelineModelListener> listeners;

    public TimelineControllerImpl() {
        listeners = Collections.synchronizedList(new ArrayList<TimelineModelListener>());

        //Workspace events
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        final DynamicController dynamicController = Lookup.getDefault().lookup(DynamicController.class);

        pc.addWorkspaceListener(new WorkspaceListener() {

            public void initialize(Workspace workspace) {
            }

            public void select(Workspace workspace) {
                model = workspace.getLookup().lookup(TimelineModelImpl.class);
                if (model == null) {
                    model = new TimelineModelImpl(TimelineControllerImpl.this);
                    workspace.add(model);
                }

                DynamicModel dynamicModel = dynamicController.getModel(workspace);
                model.setup(dynamicModel);
            }

            public void unselect(Workspace workspace) {
                model.unsetup();
            }

            public void close(Workspace workspace) {
            }

            public void disable() {
                model = null;
                setTimeLineVisible(false);
            }
        });

        if (pc.getCurrentWorkspace() != null) {
            model = pc.getCurrentWorkspace().getLookup().lookup(TimelineModelImpl.class);
            if (model == null) {
                model = new TimelineModelImpl(TimelineControllerImpl.this);
                pc.getCurrentWorkspace().add(model);
            }
            DynamicModel dynamicModel = dynamicController.getModel(pc.getCurrentWorkspace());
            model.setup(dynamicModel);
        }

        //TODO remove this force
        setTimeLineVisible(true);
    }

    public TimelineModel getModel() {
        return model;
    }

    public TimelineModel getModel(Workspace workspace) {
        return workspace.getLookup().lookup(TimelineModel.class);
    }

    public void setMin(double min) {
        if (model != null) {
            model.setMinValue(min);
        }
    }

    public void setMax(double max) {
        if (model != null) {
            model.setMaxValue(max);
        }
    }

    private void setTimeLineVisible(final boolean visible) {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                final TopComponent topComponent = WindowManager.getDefault().findTopComponent("TimelineTopComponent");
                if (visible && !topComponent.isOpened()) {
                    topComponent.open();
                    topComponent.requestActive();
                } else if (!visible && topComponent.isOpened()) {
                    topComponent.close();
                }
            }
        });
    }

    protected void fireTimelineModelEvent(TimelineModelEvent event) {
        for (TimelineModelListener listener : listeners.toArray(new TimelineModelListener[0])) {
            listener.timelineModelChanged(event);
        }
    }

    public void addListener(TimelineModelListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeListener(TimelineModelListener listener) {
        listeners.remove(listener);
    }
}
