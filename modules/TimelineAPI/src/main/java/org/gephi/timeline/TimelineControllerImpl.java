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
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.TimeFormat;
import org.gephi.graph.api.types.IntervalMap;
import org.gephi.graph.api.types.TimestampMap;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.project.api.WorkspaceListener;
import org.gephi.timeline.api.*;
import org.gephi.timeline.api.TimelineModel.PlayMode;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Mathieu Bastian
 */
@ServiceProvider(service = TimelineController.class)
public class TimelineControllerImpl implements TimelineController {

    private final List<TimelineModelListener> listeners;
    private TimelineModelImpl model;
    private GraphObserverThread observerThread;
    private GraphModel graphModel;
    private ScheduledExecutorService playExecutor;
    private FilterModel filterModel;
    private FilterController filterController;

    public TimelineControllerImpl() {
        listeners = new ArrayList<>();

        //Workspace events
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        filterController = Lookup.getDefault().lookup(FilterController.class);

        pc.addWorkspaceListener(new WorkspaceListener() {

            @Override
            public void initialize(Workspace workspace) {
            }

            @Override
            public void select(Workspace workspace) {
                model = workspace.getLookup().lookup(TimelineModelImpl.class);
                graphModel = Lookup.getDefault().lookup(GraphController.class).getGraphModel(workspace);
                if (model == null) {
                    model = new TimelineModelImpl(graphModel);
                    workspace.add(model);
                }
                observerThread = new GraphObserverThread(TimelineControllerImpl.this, model);
                setup();
                observerThread.start();
                filterModel = filterController.getModel(workspace);
            }

            @Override
            public void unselect(Workspace workspace) {
                unsetup();
                if (observerThread != null) {
                    observerThread.stopThread();
                }
                filterModel = null;
            }

            @Override
            public void close(Workspace workspace) {
            }

            @Override
            public void disable() {
                model = null;
                graphModel = null;
                filterModel = null;
                if (observerThread != null) {
                    observerThread.stopThread();
                }
                fireTimelineModelEvent(new TimelineModelEvent(TimelineModelEvent.EventType.MODEL, null, null));
            }
        });

        if (pc.getCurrentWorkspace() != null) {
            model = pc.getCurrentWorkspace().getLookup().lookup(TimelineModelImpl.class);
            graphModel = Lookup.getDefault().lookup(GraphController.class).getGraphModel(pc.getCurrentWorkspace());
            if (model == null) {
                model = new TimelineModelImpl(graphModel);
                pc.getCurrentWorkspace().add(model);
            }
            setup();
        }
    }

    @Override
    public synchronized TimelineModel getModel(Workspace workspace) {
        return workspace.getLookup().lookup(TimelineModel.class);
    }

    @Override
    public synchronized TimelineModel getModel() {
        return model;
    }

    private void setup() {
        fireTimelineModelEvent(new TimelineModelEvent(TimelineModelEvent.EventType.MODEL, model, null));
    }

    private void unsetup() {
    }

    @Override
    public void setTimeFormat(TimeFormat timeFormat) {
        graphModel.setTimeFormat(timeFormat);
    }

//
//    @Override
//    public void dynamicModelChanged(DynamicModelEvent event) {
//        if (event.getEventType().equals(DynamicModelEvent.EventType.MIN_CHANGED)
//                || event.getEventType().equals(DynamicModelEvent.EventType.MAX_CHANGED)) {
//            double newMax = event.getSource().getMax();
//            double newMin = event.getSource().getMin();
//            setMinMax(newMin, newMax);
//        } else if (event.getEventType().equals(DynamicModelEvent.EventType.VISIBLE_INTERVAL)) {
//            TimeInterval timeInterval = (TimeInterval) event.getData();
//            double min = timeInterval.getLow();
//            double max = timeInterval.getHigh();
//            fireTimelineModelEvent(new TimelineModelEvent(TimelineModelEvent.EventType.INTERVAL, model, new double[]{min, max}));
//        } else if (event.getEventType().equals(DynamicModelEvent.EventType.TIME_FORMAT)) {
//            fireTimelineModelEvent(new TimelineModelEvent(TimelineModelEvent.EventType.MODEL, model, null)); //refresh display
//        }
//    }
    protected boolean setMinMax(double min, double max) {
        if (model != null) {
            if (min > max) {
                throw new IllegalArgumentException("min should be less than max");
            } else if (min == max) {
                //Avoid setting values at this point
                return false;
            }
            double previousBoundsMin = model.getCustomMin();
            double previousBoundsMax = model.getCustomMax();

            //Custom bounds
            if (model.getCustomMin() == model.getPreviousMin()) {
                model.setCustomMin(min);
            } else if (model.getCustomMin() < min) {
                model.setCustomMin(min);
            }
            if (model.getCustomMax() == model.getPreviousMax()) {
                model.setCustomMax(max);
            } else if (model.getCustomMax() > max) {
                model.setCustomMax(max);
            }

            model.setPreviousMin(min);
            model.setPreviousMax(max);

            if (model.hasValidBounds()) {
                fireTimelineModelEvent(new TimelineModelEvent(TimelineModelEvent.EventType.MIN_MAX, model, new double[]{min, max}));

                if (model.getCustomMax() != max || model.getCustomMin() != min) {
                    fireTimelineModelEvent(new TimelineModelEvent(TimelineModelEvent.EventType.CUSTOM_BOUNDS, model, new double[]{min, max}));
                }
            }

            if ((Double.isInfinite(previousBoundsMax) || Double.isInfinite(previousBoundsMin)) && model.hasValidBounds()) {
                fireTimelineModelEvent(new TimelineModelEvent(TimelineModelEvent.EventType.VALID_BOUNDS, model, true));
            } else if (!Double.isInfinite(previousBoundsMax) && !Double.isInfinite(previousBoundsMin) && !model.hasValidBounds()) {
                fireTimelineModelEvent(new TimelineModelEvent(TimelineModelEvent.EventType.VALID_BOUNDS, model, false));
            }

            return true;
        }

        return false;
    }

    @Override
    public void setCustomBounds(double min, double max) {
        if (model != null) {
            if (model.getCustomMin() != min || model.getCustomMax() != max) {
                if (min >= max) {
                    throw new IllegalArgumentException("min should be less than max");
                }
                if (min < model.getMin() || max > model.getMax()) {
                    throw new IllegalArgumentException("Min and max should be in the bounds");
                }

                //Interval
                if (model.getIntervalStart() < min || model.getIntervalEnd() > max) {
//                    dynamicController.setVisibleInterval(min, max);
                }

                //Custom bounds
                double[] val = new double[]{min, max};
                model.setCustomMin(min);
                model.setCustomMax(max);
                fireTimelineModelEvent(new TimelineModelEvent(TimelineModelEvent.EventType.CUSTOM_BOUNDS, model, val));
            }
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        if (model != null) {
            if (enabled != model.isEnabled() && model.hasValidBounds()) {
                model.setEnabled(enabled);
                fireTimelineModelEvent(new TimelineModelEvent(TimelineModelEvent.EventType.ENABLED, model, enabled));
            }
            if (!enabled) {
                //Disable filtering
                setInterval(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
            }
        }
    }

    @Override
    public void setInterval(double from, double to) {
        if (model != null) {
            if (model.getIntervalStart() != from || model.getIntervalEnd() != to) {
                if (from >= to) {
                    throw new IllegalArgumentException("from should be less than to");
                }
                if (!(Double.isInfinite(from) && Double.isInfinite(to))) {
                    if (from < model.getCustomMin() || to > model.getCustomMax()) {
                        throw new IllegalArgumentException("From and to should be in the bounds");
                    }
                }
                model.setInterval(from, to);

                //Filter magic
                Query dynamicQuery = null;
                boolean selecting = false;

                //Get or create Dynamic Query
                if (filterModel.getCurrentQuery() != null) {
                    //Look if current query is dynamic - filtering must be active
                    Query query = filterModel.getCurrentQuery();
                    Query[] dynamicQueries = query.getQueries(DynamicRangeFilter.class);
                    if (dynamicQueries.length > 0) {
                        dynamicQuery = query;
                        selecting = filterModel.isSelecting();
                    }
                } else if (filterModel.getQueries().length == 1) {
                    //Look if a dynamic query alone exists
                    Query query = filterModel.getQueries()[0];
                    Query[] dynamicQueries = query.getQueries(DynamicRangeFilter.class);
                    if (dynamicQueries.length > 0) {
                        dynamicQuery = query;
                    }
                }

                if (Double.isInfinite(from) && Double.isInfinite(to)) {
                    if (dynamicQuery != null) {
                        filterController.remove(dynamicQuery);
                    }
                } else {
                    if (dynamicQuery == null) {
                        //Create dynamic filter
                        DynamicRangeBuilder rangeBuilder = filterModel.getLibrary().getLookup().lookup(DynamicRangeBuilder.class);
                        FilterBuilder[] fb = rangeBuilder.getBuilders(filterModel.getWorkspace());
                        if (fb.length > 0) {
                            dynamicQuery = filterController.createQuery(fb[0]);
                            filterController.add(dynamicQuery);
                        }
                    }
                    if (dynamicQuery != null) {
                        dynamicQuery.getFilter().getProperties()[0].setValue(new Range(from, to));
                        if (selecting) {
                            filterController.selectVisible(dynamicQuery);
                        } else {
                            filterController.filterVisible(dynamicQuery);
                        }
                        fireTimelineModelEvent(new TimelineModelEvent(TimelineModelEvent.EventType.INTERVAL, model, new double[]{from, to}));
                    }
                }
            }
        }
    }

    @Override
    public String[] getDynamicGraphColumns() {
        if (graphModel != null) {
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
        if (model != null) {
            if (!(model.getChart() == null && column == null)
                    || (model.getChart() != null && !model.getChart().getColumn().equals(column))) {
                if (column != null && graphModel.getGraph().getAttribute(column) != null) {
                    throw new IllegalArgumentException("Not a graph column");
                }
                Thread thread = new Thread(new Runnable() {

                    @Override
                    public void run() {
                        TimelineChart chart = null;
                        Graph graph = Lookup.getDefault().lookup(GraphController.class).getGraphModel().getGraphVisible();
                        if (column != null) {
//                            DynamicType type = (DynamicType) graph.getAttributes().getValue(column.getIndex());
//                            if (type != null) {
//                                List<Interval> intervals = type.getIntervals(model.getCustomMin(), model.getCustomMax());
//                                Number[] xs = new Number[intervals.size() * 2];
//                                Number[] ys = new Number[intervals.size() * 2];
//                                int i = 0;
//                                Interval interval;
//                                for (int j = 0; j < intervals.size(); j++) {
//                                    interval = intervals.get(j);
//                                    Number x = (Double) interval.getLow();
//                                    Number y = (Number) interval.getValue();
//                                    xs[i] = x;
//                                    ys[i] = y;
//                                    i++;
//                                    if (j != intervals.size() - 1 && intervals.get(j + 1).getLow() < interval.getHigh()) {
//                                        xs[i] = (Double) intervals.get(j + 1).getLow();
//                                    } else {
//                                        xs[i] = (Double) interval.getHigh();
//                                    }
//                                    ys[i] = y;
//                                    i++;
//                                }
//                                if (xs.length > 0) {
//                                    chart = new TimelineChartImpl(column, xs, ys);
//                                }
//                            }
                        }
                        model.setChart(chart);

                        fireTimelineModelEvent(new TimelineModelEvent(TimelineModelEvent.EventType.CHART, model, chart));
                    }
                }, "Timeline Chart");
                thread.start();
            }
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
        if (model != null && !model.isPlaying()) {
            model.setPlaying(true);
            playExecutor = Executors.newScheduledThreadPool(1, new ThreadFactory() {

                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r, "Timeline animator");
                }
            });
            playExecutor.scheduleAtFixedRate(new Runnable() {

                @Override
                public void run() {
                    double min = model.getCustomMin();
                    double max = model.getCustomMax();
                    double duration = max - min;
                    double step = (duration * model.getPlayStep()) * 0.95;
                    double from = model.getIntervalStart();
                    double to = model.getIntervalEnd();
                    boolean bothBounds = model.getPlayMode().equals(TimelineModel.PlayMode.TWO_BOUNDS);
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
            }, model.getPlayDelay(), model.getPlayDelay(), TimeUnit.MILLISECONDS);
            fireTimelineModelEvent(new TimelineModelEvent(TimelineModelEvent.EventType.PLAY_START, model, null));
        }
    }

    @Override
    public void stopPlay() {
        if (model != null && model.isPlaying()) {
            model.setPlaying(false);
            fireTimelineModelEvent(new TimelineModelEvent(TimelineModelEvent.EventType.PLAY_STOP, model, null));
        }
        if (playExecutor != null) {
            playExecutor.shutdown();
        }
    }

    @Override
    public void setPlaySpeed(int delay) {
        if (model != null) {
            model.setPlayDelay(delay);
        }
    }

    @Override
    public void setPlayStep(double step) {
        if (model != null) {
            model.setPlayStep(step);
        }
    }

    @Override
    public void setPlayMode(PlayMode playMode) {
        if (model != null) {
            model.setPlayMode(playMode);
        }
    }
}
