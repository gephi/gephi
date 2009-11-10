/*
Copyright 2008 WebAtlas
Authors : Mathieu Bastian, Mathieu Jacomy, Julian Bilcke, Patrick J. McSweeney (pjmcswee@syr.edu)
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
package org.gephi.desktop.statistics;

import java.util.ArrayList;
import org.gephi.statistics.api.*;
import java.util.List;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.project.api.ProjectController;
import org.gephi.statistics.ui.api.StatisticsUI;
import org.gephi.utils.longtask.LongTask;
import org.gephi.utils.longtask.LongTaskExecutor;
import org.gephi.utils.longtask.LongTaskListener;
import org.gephi.workspace.api.Workspace;
import org.gephi.workspace.api.WorkspaceListener;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Mathieu Bastian, Patrick J. McSweeney
 */
@ServiceProvider(service = StatisticsController.class)
public class StatisticsControllerImpl implements StatisticsController {

    private StatisticsBuilder[] statisticsBuilders;
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
        }
    }

    /**
     *
     * @param statistics
     */
    public void execute(final Statistics pStatistics, LongTaskListener listener) {
        final GraphController graphController = Lookup.getDefault().lookup(GraphController.class);
        final GraphModel graphModel = graphController.getModel();
        StatisticsBuilder builder = getBuilder(pStatistics.getClass());
        final StatisticsUI[] uis = getUI(pStatistics);
        for (StatisticsUI s : uis) {
            s.setup(pStatistics);
            model.setRunning(s, true);
        }

        if (pStatistics instanceof LongTask) {
            LongTaskExecutor executor = new LongTaskExecutor(true, builder.getName(), 10);
//            executor.addLongTaskListener(this);
            if (listener != null) {
                executor.setLongTaskListener(listener);
            }
            executor.execute((LongTask) pStatistics, new Runnable() {

                public void run() {
                    pStatistics.execute(graphModel);
                    for (StatisticsUI s : uis) {
                        model.setRunning(s, false);
                    }
                    model.addStatistics(pStatistics);
                }
            }, builder.getName(), null);
        } else {
            pStatistics.execute(graphModel);
            if (listener != null) {
                listener.taskFinished(null);
            }
            for (StatisticsUI s : uis) {
                model.setRunning(s, false);
            }
            model.addStatistics(pStatistics);
        }
    }

    /**
     * 
     * @return
     */
    public List<StatisticsBuilder> getStatistics() {
        return null;
    }

    public StatisticsUI[] getUI(Statistics statistics) {
        ArrayList<StatisticsUI> list = new ArrayList<StatisticsUI>();
        for (StatisticsUI sui : Lookup.getDefault().lookupAll(StatisticsUI.class)) {
            if (sui.getStatisticsClass().equals(statistics.getClass())) {
                list.add(sui);
            }
        }
        return list.toArray(new StatisticsUI[0]);
    }

    public StatisticsBuilder getBuilder(Class<? extends Statistics> statisticsClass) {
        for (StatisticsBuilder b : statisticsBuilders) {
            if (b.getStatisticsClass().equals(statisticsClass)) {
                return b;
            }
        }
        return null;
    }

    public void setStatisticsUIVisible(StatisticsUI ui, boolean visible) {
        model.setVisible(ui, visible);
    }
}
