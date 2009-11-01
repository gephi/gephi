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
package org.gephi.desktop.algorithms.cluster;

import org.gephi.algorithms.cluster.api.Clusterer;
import org.gephi.algorithms.cluster.api.ClusteringController;
import org.gephi.algorithms.cluster.api.ClusteringModel;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.project.api.ProjectController;
import org.gephi.utils.longtask.LongTask;
import org.gephi.utils.longtask.LongTaskErrorHandler;
import org.gephi.utils.longtask.LongTaskExecutor;
import org.gephi.workspace.api.WorkspaceDataKey;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Lookup;

/**
 *
 * @author Mathieu Bastian
 */
public class ClusteringControllerImpl implements ClusteringController {

    private LongTaskExecutor executor;
    private LongTaskErrorHandler errorHandler;

    public ClusteringControllerImpl() {
        executor = new LongTaskExecutor(true, "Clusterer", 10);
        errorHandler = new LongTaskErrorHandler() {

            public void fatalError(Throwable t) {
                NotifyDescriptor.Message e = new NotifyDescriptor.Message(t.getMessage(), NotifyDescriptor.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notifyLater(e);
                t.printStackTrace();
            }
        };
        executor.setDefaultErrorHandler(errorHandler);
    }

    public void clusterize(final Clusterer clusterer) {
        //Get Graph
        GraphController gc = Lookup.getDefault().lookup(GraphController.class);
        final Graph graph = gc.getModel().getGraph();

        //Model
        WorkspaceDataKey<ClusteringModel> key = Lookup.getDefault().lookup(ClusteringModelWorkspaceDataProvider.class).getWorkspaceDataKey();
        final ClusteringModel model = Lookup.getDefault().lookup(ProjectController.class).getCurrentWorkspace().getWorkspaceData().getData(key);

        //LongTask
        LongTask task = null;
        if (clusterer instanceof LongTask) {
            task = (LongTask) clusterer;
        }
        executor.execute(task, new Runnable() {

            public void run() {
                model.setRunning(true);
                clusterer.execute(graph);
                model.setRunning(false);
            }
        });
    }
}
