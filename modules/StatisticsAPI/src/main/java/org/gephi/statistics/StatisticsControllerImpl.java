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

import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.GraphView;
import org.gephi.graph.api.Interval;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.Subgraph;
import org.gephi.graph.api.TimeIndex;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.project.api.WorkspaceListener;
import org.gephi.statistics.api.*;
import org.gephi.statistics.spi.DynamicStatistics;
import org.gephi.statistics.spi.Statistics;
import org.gephi.statistics.spi.StatisticsBuilder;
import org.gephi.utils.longtask.api.LongTaskExecutor;
import org.gephi.utils.longtask.api.LongTaskListener;
import org.gephi.utils.longtask.spi.LongTask;
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

            @Override
            public void initialize(Workspace workspace) {
                if (workspace.getLookup().lookup(StatisticsModelImpl.class) == null) {
                    workspace.add(new StatisticsModelImpl());
                }
            }

            @Override
            public void select(Workspace workspace) {
                model = workspace.getLookup().lookup(StatisticsModelImpl.class);
                if (model == null) {
                    model = new StatisticsModelImpl();
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

        if (pc.getCurrentWorkspace() != null) {
            model = pc.getCurrentWorkspace().getLookup().lookup(StatisticsModelImpl.class);
            if (model == null) {
                model = new StatisticsModelImpl();
                pc.getCurrentWorkspace().add(model);
            }
        }
    }

    @Override
    public void execute(final Statistics statistics, LongTaskListener listener) {
        StatisticsBuilder builder = getBuilder(statistics.getClass());
        LongTaskExecutor executor = new LongTaskExecutor(true, "Statistics " + builder.getName(), 10);
        if (listener != null) {
            executor.setLongTaskListener(listener);
        }

        if (statistics instanceof DynamicStatistics) {
            final DynamicLongTask dynamicLongTask = new DynamicLongTask((DynamicStatistics) statistics);
            executor.execute(dynamicLongTask, new Runnable() {

                @Override
                public void run() {
                    executeDynamic((DynamicStatistics) statistics, dynamicLongTask);
                }
            }, builder.getName(), null);
        } else {
            LongTask task = statistics instanceof LongTask ? (LongTask) statistics : null;
            executor.execute(task, new Runnable() {

                @Override
                public void run() {
                    execute(statistics);
                }
            }, builder.getName(), null);
        }
    }

    @Override
    public void execute(Statistics statistics) {
        if (statistics instanceof DynamicStatistics) {
            executeDynamic((DynamicStatistics) statistics, null);
        } else {
            GraphController graphController = Lookup.getDefault().lookup(GraphController.class);
            GraphModel graphModel = graphController.getGraphModel();
            statistics.execute(graphModel);
            model.addReport(statistics);
        }
    }

    private void executeDynamic(DynamicStatistics statistics, DynamicLongTask dynamicLongTask) {
        GraphController graphController = Lookup.getDefault().lookup(GraphController.class);
        GraphModel graphModel = graphController.getGraphModel();

        double window = statistics.getWindow();
        double tick = statistics.getTick();

        GraphView currentView = graphModel.getVisibleView();
        Interval bounds = statistics.getBounds();
        if (bounds == null) {
            if (currentView.isMainView()) {
                bounds = graphModel.getTimeBounds();
            } else {
                bounds = currentView.getTimeInterval();
            }
            statistics.setBounds(bounds);
        }

        if (dynamicLongTask != null) {
            //Count
            int c = (int) ((bounds.getHigh() - window - bounds.getLow()) / tick);
            dynamicLongTask.start(c);
        }

        //Init
        statistics.execute(graphModel);

        //Loop
        for (double low = bounds.getLow(); low <= bounds.getHigh() - window; low += tick) {
            double high = low + window;

            Graph graph = graphModel.getGraphVisible();

            graph.writeLock();

            GraphView view = graphModel.createView();
            Subgraph g = graphModel.getGraph(view);

            TimeIndex<Node> nodeIndex = graphModel.getNodeTimeIndex(currentView);
            if (Double.isInfinite(nodeIndex.getMinTimestamp()) && Double.isInfinite(nodeIndex.getMaxTimestamp())) {
                for (Node node : graph.getNodes()) {
                    g.addNode(node);
                }
            } else {
                for (Node node : nodeIndex.get(new Interval(low, high))) {
                    g.addNode(node);
                }
            }

            TimeIndex<Edge> edgeIndex = graphModel.getEdgeTimeIndex(currentView);
            if (Double.isInfinite(edgeIndex.getMinTimestamp()) && Double.isInfinite(edgeIndex.getMaxTimestamp())) {
                for (Edge edge : graph.getEdges()) {
                    if (g.contains(edge.getSource()) && g.contains(edge.getTarget())) {
                        g.addEdge(edge);
                    }
                }
            } else {
                for (Edge edge : edgeIndex.get(new Interval(low, high))) {
                    if (g.contains(edge.getSource()) && g.contains(edge.getTarget())) {
                        g.addEdge(edge);
                    }
                }
            }

            graph.writeUnlock();

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

    @Override
    public StatisticsBuilder getBuilder(Class<? extends Statistics> statisticsClass) {
        for (StatisticsBuilder b : statisticsBuilders) {
            if (b.getStatisticsClass().equals(statisticsClass)) {
                return b;
            }
        }
        return null;
    }

    @Override
    public StatisticsModelImpl getModel() {
        return model;
    }

    @Override
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

        @Override
        public boolean cancel() {
            cancel = true;
            if (longTask != null) {
                this.longTask.cancel();
            }
            return true;
        }

        @Override
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
