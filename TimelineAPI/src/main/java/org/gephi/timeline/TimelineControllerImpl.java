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
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.data.attributes.api.AttributeUtils;
import org.gephi.data.attributes.type.DynamicType;
import org.gephi.data.attributes.type.Interval;
import org.gephi.data.attributes.type.TimeInterval;
import org.gephi.dynamic.api.DynamicController;
import org.gephi.dynamic.api.DynamicModelEvent;
import org.gephi.dynamic.api.DynamicModelListener;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.project.api.WorkspaceListener;
import org.gephi.timeline.api.TimelineModel.PlayMode;
import org.gephi.timeline.api.*;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Mathieu Bastian
 */
@ServiceProvider(service = TimelineController.class)
public class TimelineControllerImpl implements TimelineController, DynamicModelListener {

    private final List<TimelineModelListener> listeners;
    private TimelineModelImpl model;
    private final DynamicController dynamicController;
    private AttributeModel attributeModel;
    private ScheduledExecutorService playExecutor;

    public TimelineControllerImpl() {
        listeners = new ArrayList<TimelineModelListener>();

        //Workspace events
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        dynamicController = Lookup.getDefault().lookup(DynamicController.class);

        pc.addWorkspaceListener(new WorkspaceListener() {

            @Override
            public void initialize(Workspace workspace) {
            }

            @Override
            public void select(Workspace workspace) {
                model = workspace.getLookup().lookup(TimelineModelImpl.class);
                if (model == null) {
                    model = new TimelineModelImpl(dynamicController.getModel(workspace));
                    workspace.add(model);
                }
                attributeModel = Lookup.getDefault().lookup(AttributeController.class).getModel(workspace);
                setup();
            }

            @Override
            public void unselect(Workspace workspace) {
                unsetup();
            }

            @Override
            public void close(Workspace workspace) {
            }

            @Override
            public void disable() {
                model = null;
                attributeModel = null;
                fireTimelineModelEvent(new TimelineModelEvent(TimelineModelEvent.EventType.MODEL, null, null));
            }
        });

        if (pc.getCurrentWorkspace() != null) {
            model = pc.getCurrentWorkspace().getLookup().lookup(TimelineModelImpl.class);
            if (model == null) {
                model = new TimelineModelImpl(dynamicController.getModel(pc.getCurrentWorkspace()));
                pc.getCurrentWorkspace().add(model);
            }
            attributeModel = Lookup.getDefault().lookup(AttributeController.class).getModel(pc.getCurrentWorkspace());
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

        dynamicController.addModelListener(this);
    }

    private void unsetup() {
        dynamicController.removeModelListener(this);
    }

    @Override
    public void dynamicModelChanged(DynamicModelEvent event) {
        if (event.getEventType().equals(DynamicModelEvent.EventType.MIN_CHANGED)
                || event.getEventType().equals(DynamicModelEvent.EventType.MAX_CHANGED)) {
            double newMax = event.getSource().getMax();
            double newMin = event.getSource().getMin();
            setMinMax(newMin, newMax);
        } else if (event.getEventType().equals(DynamicModelEvent.EventType.VISIBLE_INTERVAL)) {
            TimeInterval timeInterval = (TimeInterval) event.getData();
            double min = timeInterval.getLow();
            double max = timeInterval.getHigh();
            fireTimelineModelEvent(new TimelineModelEvent(TimelineModelEvent.EventType.INTERVAL, model, new double[]{min, max}));
        } else if (event.getEventType().equals(DynamicModelEvent.EventType.TIME_FORMAT)) {
            fireTimelineModelEvent(new TimelineModelEvent(TimelineModelEvent.EventType.MODEL, model, null)); //refresh display
        }
    }

    private boolean setMinMax(double min, double max) {
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
                    dynamicController.setVisibleInterval(min, max);
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
                dynamicController.setVisibleInterval(new TimeInterval());
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
                if (from < model.getCustomMin() || to > model.getCustomMax()) {
                    throw new IllegalArgumentException("From and to should be in the bounds");
                }
                dynamicController.setVisibleInterval(from, to);
            }
        }
    }

    @Override
    public AttributeColumn[] getDynamicGraphColumns() {
        if (attributeModel != null) {
            List<AttributeColumn> columns = new ArrayList<AttributeColumn>();
            AttributeUtils utils = AttributeUtils.getDefault();
            for (AttributeColumn col : attributeModel.getGraphTable().getColumns()) {
                if (utils.isDynamicNumberColumn(col)) {
                    columns.add(col);
                }
            }
            return columns.toArray(new AttributeColumn[0]);
        }
        return new AttributeColumn[0];
    }

    @Override
    public void selectColumn(final AttributeColumn column) {
        if (model != null) {
            if (!(model.getChart() == null && column == null)
                    || (model.getChart() != null && !model.getChart().getColumn().equals(column))) {
                if (column != null && !attributeModel.getGraphTable().hasColumn(column.getId())) {
                    throw new IllegalArgumentException("Not a graph column");
                }
                Thread thread = new Thread(new Runnable() {

                    @Override
                    public void run() {
                        TimelineChart chart = null;
                        Graph graph = Lookup.getDefault().lookup(GraphController.class).getModel().getGraphVisible();
                        if (column != null) {
                            DynamicType type = (DynamicType) graph.getAttributes().getValue(column.getIndex());
                            if (type != null) {
                                List<Interval> intervals = type.getIntervals(model.getCustomMin(), model.getCustomMax());
                                Number[] xs = new Number[intervals.size() * 2];
                                Number[] ys = new Number[intervals.size() * 2];
                                int i = 0;
                                Interval interval;
                                for (int j = 0; j < intervals.size(); j++) {
                                    interval = intervals.get(j);
                                    Number x = (Double) interval.getLow();
                                    Number y = (Number) interval.getValue();
                                    xs[i] = x;
                                    ys[i] = y;
                                    i++;
                                    if (j != intervals.size() - 1 && intervals.get(j + 1).getLow() < interval.getHigh()) {
                                        xs[i] = (Double) intervals.get(j + 1).getLow();
                                    } else {
                                        xs[i] = (Double) interval.getHigh();
                                    }
                                    ys[i] = y;
                                    i++;
                                }
                                if (xs.length > 0) {
                                    chart = new TimelineChartImpl(column, xs, ys);
                                }
                            }
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
                    } else {
                        if (step > 0 && to < max) {
                            to += step;
                            someAction = true;
                        } else if (step < 0 && from > min) {
                            from += step;
                            someAction = true;
                        }
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
