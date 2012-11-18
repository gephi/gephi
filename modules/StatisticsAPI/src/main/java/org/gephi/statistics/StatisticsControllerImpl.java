/*
Copyright 2008-2010 Gephi
Authors : Patick J. McSweeney <pjmcswee@syr.edu>,
Mathieu Bastian <mathieu.bastian@gephi.org>
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
package org.gephi.statistics;

import org.gephi.statistics.spi.StatisticsBuilder;
import org.gephi.statistics.spi.Statistics;
import org.gephi.statistics.api.*;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.data.attributes.type.Interval;
import org.gephi.data.attributes.type.TimeInterval;
import org.gephi.dynamic.api.DynamicController;
import org.gephi.dynamic.api.DynamicGraph;
import org.gephi.dynamic.api.DynamicModel;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.HierarchicalGraph;
import org.gephi.project.api.ProjectController;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.longtask.api.LongTaskExecutor;
import org.gephi.utils.longtask.api.LongTaskListener;
import org.gephi.project.api.Workspace;
import org.gephi.project.api.WorkspaceListener;
import org.gephi.statistics.spi.DynamicStatistics;
import org.gephi.utils.progress.Progress;
import org.gephi.utils.progress.ProgressTicket;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Mathieu Bastian
 * @author Patrick J. McSweeney
 */
@ServiceProvider(service = StatisticsController.class)
public class StatisticsControllerImpl implements StatisticsController {

    private final StatisticsBuilder[] statisticsBuilders;
    private StatisticsModelImpl model;

    public StatisticsControllerImpl() {
        statisticsBuilders = Lookup.getDefault().lookupAll(StatisticsBuilder.class).toArray(new StatisticsBuilder[0]);

        //Workspace events
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        pc.addWorkspaceListener(new WorkspaceListener() {

            public void initialize(Workspace workspace) {
                workspace.add(new StatisticsModelImpl());
            }

            public void select(Workspace workspace) {
                model = workspace.getLookup().lookup(StatisticsModelImpl.class);
                if (model == null) {
                    model = new StatisticsModelImpl();
                    workspace.add(model);
                }
            }

            public void unselect(Workspace workspace) {
            }

            public void close(Workspace workspace) {
            }

            public void disable() {
                model = null;
            }
        });

        if (pc.getCurrentWorkspace() != null) {
            model = pc.getCurrentWorkspace().getLookup().lookup(StatisticsModelImpl.class);
            if (model == null) {
                model = new StatisticsModelImpl();
                pc.getCurrentWorkspace().add(model);
            }
        }
    }

    public void execute(final Statistics statistics, LongTaskListener listener) {
        StatisticsBuilder builder = getBuilder(statistics.getClass());
        LongTaskExecutor executor = new LongTaskExecutor(true, "Statistics " + builder.getName(), 10);
        if (listener != null) {
            executor.setLongTaskListener(listener);
        }

        if (statistics instanceof DynamicStatistics) {
            final DynamicLongTask dynamicLongTask = new DynamicLongTask((DynamicStatistics) statistics);
            executor.execute(dynamicLongTask, new Runnable() {

                public void run() {
                    executeDynamic((DynamicStatistics) statistics, dynamicLongTask);
                }
            }, builder.getName(), null);
        } else {
            LongTask task = statistics instanceof LongTask ? (LongTask) statistics : null;
            executor.execute(task, new Runnable() {

                public void run() {
                    execute(statistics);
                }
            }, builder.getName(), null);
        }
    }

    public void execute(Statistics statistics) {
        if (statistics instanceof DynamicStatistics) {
            executeDynamic((DynamicStatistics) statistics, null);
        } else {
            GraphController graphController = Lookup.getDefault().lookup(GraphController.class);
            GraphModel graphModel = graphController.getModel();
            AttributeModel attributeModel = Lookup.getDefault().lookup(AttributeController.class).getModel();
            statistics.execute(graphModel, attributeModel);
            model.addReport(statistics);
        }
    }

    private void executeDynamic(DynamicStatistics statistics, DynamicLongTask dynamicLongTask) {
        GraphController graphController = Lookup.getDefault().lookup(GraphController.class);
        GraphModel graphModel = graphController.getModel();
        AttributeController attributeController = Lookup.getDefault().lookup(AttributeController.class);
        DynamicController dynamicController = Lookup.getDefault().lookup(DynamicController.class);
        DynamicModel dynamicModel = dynamicController.getModel();
        AttributeModel attributeModel = attributeController.getModel();
        if (!dynamicModel.isDynamicGraph()) {
            throw new IllegalArgumentException("The current graph must be a dynamic graph");
        }

        double window = statistics.getWindow();
        double tick = statistics.getTick();
        Interval bounds = statistics.getBounds();
        if (bounds == null) {
            TimeInterval visibleInterval = dynamicModel.getVisibleInterval();
            double low = visibleInterval.getLow();
            if (Double.isInfinite(low)) {
                low = dynamicModel.getMin();
            }
            double high = visibleInterval.getHigh();
            if (Double.isInfinite(high)) {
                high = dynamicModel.getMax();
            }
            bounds = new Interval(low, high);
            statistics.setBounds(bounds);
        }


        if (dynamicLongTask != null) {
            //Count
            int c = (int) ((bounds.getHigh() - window - bounds.getLow()) / tick);
            dynamicLongTask.start(c);
        }

        HierarchicalGraph graph = graphModel.getHierarchicalGraphVisible();
        DynamicGraph dynamicGraph = dynamicModel.createDynamicGraph(graph, bounds);

        //Init
        statistics.execute(graphModel, attributeModel);

        //Loop
        for (double low = bounds.getLow(); low <= bounds.getHigh() - window; low += tick) {
            double high = low + window;

            Graph g = dynamicGraph.getSnapshotGraph(low, high);

            statistics.loop(g.getView(), new Interval(low, high));

            //Cancelled?
            if (dynamicLongTask != null && dynamicLongTask.isCancelled()) {
                return;
            } else if (dynamicLongTask != null) {
                dynamicLongTask.progress();
            }
        }
        statistics.end();
        model.addReport(statistics);
    }

    public StatisticsBuilder getBuilder(Class<? extends Statistics> statisticsClass) {
        for (StatisticsBuilder b : statisticsBuilders) {
            if (b.getStatisticsClass().equals(statisticsClass)) {
                return b;
            }
        }
        return null;
    }

    public StatisticsModelImpl getModel() {
        return model;
    }

    public StatisticsModel getModel(Workspace workspace) {
        StatisticsModel statModel = workspace.getLookup().lookup(StatisticsModelImpl.class);
        if (statModel == null) {
            statModel = new StatisticsModelImpl();
            workspace.add(statModel);
        }
        return statModel;
    }

    private static class DynamicLongTask implements LongTask {

        private ProgressTicket progressTicket;
        private boolean cancel = false;
        private final LongTask longTask;

        public DynamicLongTask(DynamicStatistics statistics) {
            if (statistics instanceof LongTask) {
                this.longTask = (LongTask) statistics;
            } else {
                this.longTask = null;
            }
        }

        public boolean cancel() {
            cancel = true;
            if (longTask != null) {
                this.longTask.cancel();
            }
            return true;
        }

        public void setProgressTicket(ProgressTicket progressTicket) {
            this.progressTicket = progressTicket;
        }

        public void start(int iterations) {
            Progress.start(progressTicket, iterations);
        }

        public void progress() {
            Progress.progress(progressTicket);
        }

        public boolean isCancelled() {
            return cancel;
        }
    }
}
