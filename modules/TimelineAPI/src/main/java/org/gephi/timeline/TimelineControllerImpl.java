/*
 Copyright 2008-2011 Gephi
 Authors : Mathieu Bastian
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

package org.gephi.timeline;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import org.gephi.filters.api.FilterController;
import org.gephi.filters.api.FilterModel;
import org.gephi.filters.api.Query;
import org.gephi.filters.api.Range;
import org.gephi.filters.plugin.dynamic.DynamicRangeBuilder;
import org.gephi.filters.plugin.dynamic.DynamicRangeBuilder.DynamicRangeFilter;
import org.gephi.filters.spi.FilterBuilder;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.TimeFormat;
import org.gephi.graph.api.types.IntervalMap;
import org.gephi.graph.api.types.TimestampMap;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.project.api.WorkspaceListener;
import org.gephi.project.spi.Controller;
import org.gephi.timeline.api.TimelineChart;
import org.gephi.timeline.api.TimelineController;
import org.gephi.timeline.api.TimelineModel;
import org.gephi.timeline.api.TimelineModel.PlayMode;
import org.gephi.timeline.api.TimelineModelEvent;
import org.gephi.timeline.api.TimelineModelListener;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;

/**
 * @author Mathieu Bastian
 */
@ServiceProviders({
    @ServiceProvider(service = TimelineController.class),
    @ServiceProvider(service = Controller.class, position = 3000)})
public class TimelineControllerImpl implements TimelineController, Controller<TimelineModelImpl> {

    private final List<TimelineModelListener> listeners;
    private GraphObserverThread observerThread;
    private ScheduledExecutorService playExecutor;

    public TimelineControllerImpl() {
        listeners = new ArrayList<>();

        //Workspace events
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        pc.addWorkspaceListener(new WorkspaceListener() {

            @Override
            public void initialize(Workspace workspace) {
            }

            @Override
            public void select(Workspace workspace) {
                TimelineModelImpl model = workspace.getLookup().lookup(TimelineModelImpl.class);
                fireTimelineModelEvent(new TimelineModelEvent(TimelineModelEvent.EventType.MODEL, model, null));
                observerThread = new GraphObserverThread(TimelineControllerImpl.this, model);
                observerThread.start();
            }

            @Override
            public void unselect(Workspace workspace) {
                stopPlay();
                if (observerThread != null) {
                    observerThread.stopThread();
                    observerThread = null;
                }
            }

            @Override
            public void close(Workspace workspace) {
            }

            @Override
            public void disable() {
                stopPlay();
                if (observerThread != null) {
                    observerThread.stopThread();
                    observerThread = null;
                }
                fireTimelineModelEvent(new TimelineModelEvent(TimelineModelEvent.EventType.MODEL, null, null));
            }
        });
    }

    @Override
    public TimelineModelImpl newModel(Workspace workspace) {
        return new TimelineModelImpl(workspace);
    }

    @Override
    public Class<TimelineModelImpl> getModelClass() {
        return TimelineModelImpl.class;
    }

    @Override
    public TimelineModelImpl getModel(Workspace workspace) {
        return Controller.super.getModel(workspace);
    }

    @Override
    public TimelineModelImpl getModel() {
        return Controller.super.getModel();
    }

    @Override
    public void setTimeFormat(TimeFormat timeFormat) {
        TimelineModelImpl currentModel = getModel();
        if (currentModel != null) {
            currentModel.getGraphModel().setTimeFormat(timeFormat);
        }
    }

    protected boolean setMinMax(double min, double max) {
        TimelineModelImpl currentModel = getModel();
        if (currentModel == null) {
            return false;
        }
        double[] prevCustomBounds = new double[2];
        if (!currentModel.updateMinMax(min, max, prevCustomBounds)) {
            return false;
        }
        if (currentModel.hasValidBounds()) {
            fireTimelineModelEvent(
                new TimelineModelEvent(TimelineModelEvent.EventType.MIN_MAX, currentModel, new double[] {min, max}));

            if (currentModel.getCustomMax() != max || currentModel.getCustomMin() != min) {
                fireTimelineModelEvent(new TimelineModelEvent(TimelineModelEvent.EventType.CUSTOM_BOUNDS, currentModel,
                    new double[] {min, max}));
            }
        }
        if ((Double.isInfinite(prevCustomBounds[1]) || Double.isInfinite(prevCustomBounds[0])) &&
            currentModel.hasValidBounds()) {
            fireTimelineModelEvent(
                new TimelineModelEvent(TimelineModelEvent.EventType.VALID_BOUNDS, currentModel, true));
        } else if (!Double.isInfinite(prevCustomBounds[1]) && !Double.isInfinite(prevCustomBounds[0]) &&
            !currentModel.hasValidBounds()) {
            fireTimelineModelEvent(
                new TimelineModelEvent(TimelineModelEvent.EventType.VALID_BOUNDS, currentModel, false));
        }
        return true;
    }

    @Override
    public void setCustomBounds(double min, double max) {
        TimelineModelImpl currentModel = getModel();
        if (currentModel != null && currentModel.setCustomBounds(min, max)) {
            fireTimelineModelEvent(new TimelineModelEvent(TimelineModelEvent.EventType.CUSTOM_BOUNDS, currentModel,
                new double[] {min, max}));
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        TimelineModelImpl currentModel = getModel();
        if (currentModel == null) {
            return;
        }
        if (currentModel.setEnabled(enabled)) {
            fireTimelineModelEvent(new TimelineModelEvent(TimelineModelEvent.EventType.ENABLED, currentModel, enabled));
        }
        if (!enabled) {
            setInterval(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
        }
    }

    @Override
    public void setInterval(double from, double to) {
        TimelineModelImpl currentModel = getModel();
        if (currentModel == null) {
            return;
        }
        FilterController filterController = Lookup.getDefault().lookup(FilterController.class);
        FilterModel filterModel = filterController.getModel(currentModel.getWorkspace());
        if (filterModel == null || !currentModel.setInterval(from, to)) {
            return;
        }
        applyIntervalFilter(currentModel, filterModel, from, to);
    }

    private void applyIntervalFilter(TimelineModelImpl currentModel, FilterModel filterModel, double from, double to) {
        Query dynamicQuery = null;
        boolean selecting = false;

        if (filterModel.getCurrentQuery() != null) {
            Query query = filterModel.getCurrentQuery();
            Query[] dynamicQueries = query.getQueries(DynamicRangeFilter.class);
            if (dynamicQueries.length > 0) {
                dynamicQuery = query;
                selecting = filterModel.isSelecting();
            }
        } else if (filterModel.getQueries().length == 1) {
            Query query = filterModel.getQueries()[0];
            Query[] dynamicQueries = query.getQueries(DynamicRangeFilter.class);
            if (dynamicQueries.length > 0) {
                dynamicQuery = query;
            }
        }

        FilterController filterController = Lookup.getDefault().lookup(FilterController.class);
        if (Double.isInfinite(from) && Double.isInfinite(to)) {
            if (dynamicQuery != null) {
                filterController.remove(dynamicQuery);
            }
        } else {
            if (dynamicQuery == null) {
                DynamicRangeBuilder rangeBuilder =
                    filterModel.getLibrary().getLookup().lookup(DynamicRangeBuilder.class);
                if (rangeBuilder != null) {
                    FilterBuilder[] fb = rangeBuilder.getBuilders(filterModel.getWorkspace());
                    if (fb.length > 0) {
                        dynamicQuery = filterController.createQuery(fb[0]);
                        filterController.add(dynamicQuery);
                    }
                }
            }
            if (dynamicQuery != null) {
                dynamicQuery.getFilter().getProperties()[0].setValue(new Range(from, to));
                if (selecting) {
                    filterController.selectVisible(dynamicQuery);
                } else {
                    filterController.filterVisible(dynamicQuery);
                }
                fireTimelineModelEvent(
                    new TimelineModelEvent(TimelineModelEvent.EventType.INTERVAL, currentModel,
                        new double[] {from, to}));
            }
        }
    }

    @Override
    public String[] getDynamicGraphColumns() {
        TimelineModelImpl currentModel = getModel();
        if (currentModel != null) {
            GraphModel graphModel = currentModel.getGraphModel();
            List<String> columns = new ArrayList<>();
            for (String k : graphModel.getGraph().getAttributeKeys()) {
                Object a = graphModel.getGraph().getAttribute(k);
                if (a instanceof IntervalMap || a instanceof TimestampMap) {
                    columns.add(k);
                }
            }
            return columns.toArray(new String[0]);
        }
        return new String[0];
    }

    @Override
    public void selectColumn(final String column) {
        final TimelineModelImpl currentModel = getModel();
        if (currentModel == null) {
            return;
        }
        if (!(currentModel.getChart() == null && column == null)
            || (currentModel.getChart() != null && !currentModel.getChart().getColumn().equals(column))) {
            if (column != null && currentModel.getGraphModel().getGraph().getAttribute(column) == null) {
                throw new IllegalArgumentException("Not a graph column");
            }
            Thread thread = new Thread(new Runnable() {

                @Override
                public void run() {
                    Graph graph = currentModel.getGraphModel().getGraphVisible();
                    TimelineChart chart = TimelineChartImpl.of(graph, column);
                    currentModel.setChart(chart);

                    fireTimelineModelEvent(
                        new TimelineModelEvent(TimelineModelEvent.EventType.CHART, currentModel, chart));
                }
            }, "Timeline Chart");
            thread.start();
        }
    }

    protected void fireTimelineModelEvent(TimelineModelEvent event) {
        for (TimelineModelListener listener : listeners.toArray(new TimelineModelListener[0])) {
            listener.timelineModelChanged(event);
        }
    }

    @Override
    public synchronized void addListener(TimelineModelListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    @Override
    public synchronized void removeListener(TimelineModelListener listener) {
        listeners.remove(listener);
    }

    @Override
    public void startPlay() {
        TimelineModelImpl currentModel = getModel();
        if (currentModel == null || currentModel.isPlaying()) {
            return;
        }
        currentModel.setPlaying(true);
        playExecutor = Executors.newScheduledThreadPool(1, new ThreadFactory() {

            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "Timeline animator");
            }
        });
        playExecutor.scheduleAtFixedRate(new Runnable() {

            @Override
            public void run() {
                TimelineModelImpl m = getModel();
                if (m == null) {
                    return;
                }
                double min = m.getCustomMin();
                double max = m.getCustomMax();
                double duration = max - min;
                double step = (duration * m.getPlayStep()) * 0.95;
                double from = m.getIntervalStart();
                double to = m.getIntervalEnd();
                boolean bothBounds = m.getPlayMode().equals(TimelineModel.PlayMode.TWO_BOUNDS);
                boolean someAction = false;
                if (bothBounds) {
                    if (step > 0 && to < max) {
                        from += step;
                        to += step;
                        someAction = true;
                    } else if (step < 0 && from > min) {
                        from += step;
                        to += step;
                        someAction = true;
                    }
                } else if (step > 0 && to < max) {
                    to += step;
                    someAction = true;
                } else if (step < 0 && from > min) {
                    from += step;
                    someAction = true;
                }

                if (someAction) {
                    from = Math.max(from, min);
                    to = Math.min(to, max);
                    setInterval(from, to);
                } else {
                    stopPlay();
                }
            }
        }, currentModel.getPlayDelay(), currentModel.getPlayDelay(), TimeUnit.MILLISECONDS);
        fireTimelineModelEvent(new TimelineModelEvent(TimelineModelEvent.EventType.PLAY_START, currentModel, null));
    }

    @Override
    public void stopPlay() {
        TimelineModelImpl currentModel = getModel();
        if (currentModel != null && currentModel.isPlaying()) {
            currentModel.setPlaying(false);
            fireTimelineModelEvent(new TimelineModelEvent(TimelineModelEvent.EventType.PLAY_STOP, currentModel, null));
        }
        if (playExecutor != null) {
            playExecutor.shutdown();
        }
    }

    @Override
    public void setPlaySpeed(int delay) {
        TimelineModelImpl currentModel = getModel();
        if (currentModel != null) {
            currentModel.setPlayDelay(delay);
        }
    }

    @Override
    public void setPlayStep(double step) {
        TimelineModelImpl currentModel = getModel();
        if (currentModel != null) {
            currentModel.setPlayStep(step);
        }
    }

    @Override
    public void setPlayMode(PlayMode playMode) {
        TimelineModelImpl currentModel = getModel();
        if (currentModel != null) {
            currentModel.setPlayMode(playMode);
        }
    }
}
