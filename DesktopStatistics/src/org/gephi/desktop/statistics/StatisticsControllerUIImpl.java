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
package org.gephi.desktop.statistics;

import java.util.ArrayList;
import org.gephi.desktop.statistics.api.StatisticsControllerUI;
import org.gephi.dynamic.api.DynamicController;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.project.api.WorkspaceListener;
import org.gephi.statistics.api.StatisticsController;
import org.gephi.statistics.spi.Statistics;
import org.gephi.statistics.spi.StatisticsUI;
import org.gephi.utils.longtask.api.LongTaskListener;
import org.gephi.utils.longtask.spi.LongTask;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Mathieu Bastian
 */
@ServiceProvider(service = StatisticsControllerUI.class)
public class StatisticsControllerUIImpl implements StatisticsControllerUI {

    private StatisticsModelUIImpl model;

    public void setup(StatisticsModelUIImpl model) {
        this.model = model;
        unsetup();

        if (model != null) {
            DynamicController dynamicController = Lookup.getDefault().lookup(DynamicController.class);
            boolean isDynamic = dynamicController.getModel(model.getWorkspace()).isDynamicGraph();
            if (!isDynamic) {
                for (StatisticsUI ui : Lookup.getDefault().lookupAll(StatisticsUI.class)) {
                    if (ui.getCategory().equals(StatisticsUI.CATEGORY_DYNAMIC)) {
                        setStatisticsUIVisible(ui, false);
                    }
                }
            }
        }
    }

    public void unsetup() {
        if (model != null) {
            DynamicController dynamicController = Lookup.getDefault().lookup(DynamicController.class);
        }
    }

    public void execute(final Statistics statistics) {
        StatisticsController controller = Lookup.getDefault().lookup(StatisticsController.class);
        final StatisticsUI[] uis = getUI(statistics);

        for (StatisticsUI s : uis) {
            s.setup(statistics);
        }
        model.setRunning(statistics, true);

        controller.execute(statistics, new LongTaskListener() {

            public void taskFinished(LongTask task) {
                model.setRunning(statistics, false);
                for (StatisticsUI s : uis) {
                    model.addResult(s);
                    s.unsetup();
                }
            }
        });
    }

    public void execute(final Statistics statistics, final LongTaskListener listener) {
        StatisticsController controller = Lookup.getDefault().lookup(StatisticsController.class);
        final StatisticsUI[] uis = getUI(statistics);

        for (StatisticsUI s : uis) {
            s.setup(statistics);
        }
        model.setRunning(statistics, true);

        controller.execute(statistics, new LongTaskListener() {

            public void taskFinished(LongTask task) {
                model.setRunning(statistics, false);
                for (StatisticsUI s : uis) {
                    model.addResult(s);
                    s.unsetup();
                }
                if (listener != null) {
                    listener.taskFinished(statistics instanceof LongTask ? (LongTask) statistics : null);
                }
            }
        });
    }

    public StatisticsUI[] getUI(Statistics statistics) {
        boolean dynamic = false;
        ArrayList<StatisticsUI> list = new ArrayList<StatisticsUI>();
        for (StatisticsUI sui : Lookup.getDefault().lookupAll(StatisticsUI.class)) {
            if (sui.getStatisticsClass().equals(statistics.getClass())) {
                list.add(sui);
            }
        }
        return list.toArray(new StatisticsUI[0]);
    }

    public void setStatisticsUIVisible(StatisticsUI ui, boolean visible) {
        if (model != null) {
            model.setVisible(ui, visible);
        }
    }
}
