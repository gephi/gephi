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
package org.gephi.layout;

import org.gephi.graph.api.GraphController;
import org.gephi.layout.spi.Layout;
import org.gephi.layout.api.LayoutController;
import org.gephi.layout.api.LayoutModel;
import org.gephi.project.api.ProjectController;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.ProgressTicket;
import org.gephi.project.api.Workspace;
import org.gephi.project.api.WorkspaceListener;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Mathieu Bastian
 */
@ServiceProvider(service = LayoutController.class)
public class LayoutControllerImpl implements LayoutController {

    private LayoutModelImpl model;
    private LayoutRun layoutRun;

    public LayoutControllerImpl() {
        Lookup.getDefault().lookup(ProjectController.class).addWorkspaceListener(new WorkspaceListener() {

            public void initialize(Workspace workspace) {
                workspace.add(new LayoutModelImpl());
            }

            public void select(Workspace workspace) {
                model = workspace.getLookup().lookup(LayoutModelImpl.class);
                if (model == null) {
                    model = new LayoutModelImpl();
                }
                workspace.add(model);
            }

            public void unselect(Workspace workspace) {
                if (model.getSelectedLayout() != null) {
                    model.saveProperties(model.getSelectedLayout());
                }
            }

            public void close(Workspace workspace) {
            }

            public void disable() {
                model = null;
            }
        });

        ProjectController projectController = Lookup.getDefault().lookup(ProjectController.class);
        if (projectController.getCurrentWorkspace() != null) {
            model = projectController.getCurrentWorkspace().getLookup().lookup(LayoutModelImpl.class);
            if (model == null) {
                model = new LayoutModelImpl();
            }
            projectController.getCurrentWorkspace().add(model);
        }
    }

    public LayoutModel getModel() {
        return model;
    }

    public void setLayout(Layout layout) {
        model.setSelectedLayout(layout);
        injectGraph();
    }

    public void executeLayout() {
        if (model.getSelectedLayout() != null) {
            layoutRun = new LayoutRun(model.getSelectedLayout());
            model.getExecutor().execute(layoutRun, layoutRun);
            model.setRunning(true);
        }
    }

    public void injectGraph() {
        GraphController graphController = Lookup.getDefault().lookup(GraphController.class);
        if (model.getSelectedLayout() != null && graphController.getModel() != null) {
            model.getSelectedLayout().setGraphModel(graphController.getModel());
        }
    }

    public boolean canExecute() {
        return model.getSelectedLayout() != null && !model.isRunning();
    }

    public boolean canStop() {
        return model.isRunning();
    }

    public void stopLayout() {
        model.getExecutor().cancel();
    }

    private static class LayoutRun implements LongTask, Runnable {

        private Layout layout;
        private boolean stopRun = false;

        public LayoutRun(Layout layout) {
            this.layout = layout;
        }

        public void run() {
            layout.initAlgo();
            while (layout.canAlgo() && !stopRun) {
                layout.goAlgo();
            }
            layout.endAlgo();
        }

        public boolean cancel() {
            stopRun = true;
            return true;
        }

        public void setProgressTicket(ProgressTicket progressTicket) {
        }
    }
}
