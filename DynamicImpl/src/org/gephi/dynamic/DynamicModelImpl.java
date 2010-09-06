/*
 * Copyright 2008-2010 Gephi
 * Authors : Cezary Bartosiak
 * Website : http://www.gephi.org
 * 
 * This file is part of Gephi.
 *
 * Gephi is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Gephi is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gephi.dynamic;

import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeEvent;
import org.gephi.data.attributes.api.AttributeListener;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.data.attributes.api.AttributeType;
import org.gephi.data.attributes.api.AttributeUtils;
import org.gephi.data.attributes.api.AttributeValue;
import org.gephi.data.attributes.type.TimeInterval;
import org.gephi.dynamic.api.DynamicGraph;
import org.gephi.dynamic.api.DynamicModel;
import org.gephi.dynamic.api.DynamicModelEvent;
import org.gephi.filters.api.FilterController;
import org.gephi.filters.api.FilterModel;
import org.gephi.filters.api.Query;
import org.gephi.filters.plugin.dynamic.DynamicRangeBuilder;
import org.gephi.filters.plugin.dynamic.DynamicRangeBuilder.DynamicRangeFilter;
import org.gephi.filters.spi.FilterBuilder;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphEvent;
import org.gephi.graph.api.GraphListener;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.project.api.Workspace;
import org.openide.util.Lookup;

/**
 * The default implementation of {@code DynamicModel}.
 *
 * @author Cezary Bartosiak
 */
public final class DynamicModelImpl implements DynamicModel {

    private DynamicControllerImpl controller;
    private GraphModel graphModel;
    private FilterController filterController;
    private FilterModel filterModel;
    private TimeInterval visibleTimeInterval;
    private TimeIntervalIndex timeIntervalIndex;
    private AttributeColumn nodeColumn;
    private AttributeColumn edgeColumn;
    private TimeFormat timeFormat;

    /**
     * The default constructor.
     *
     * @param workspace  workspace related to this model
     *
     * @throws NullPointerException if {@code workspace} is null.
     */
    public DynamicModelImpl(DynamicControllerImpl controller, Workspace workspace) {
        this(controller, workspace, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
    }

    /**
     * Constructs a new {@code DynamicModel} for the {@code workspace}.
     *
     * @param workspace  workspace related to this model
     * @param low        the left endpoint of the visible time interval
     * @param high       the right endpoint of the visible time interval
     *
     * @throws NullPointerException if {@code workspace} is null or the graph model
     *                              and/or its underlying graph are nulls.
     */
    public DynamicModelImpl(DynamicControllerImpl controller, Workspace workspace, double low, double high) {
        if (workspace == null) {
            throw new NullPointerException("The workspace cannot be null.");
        }

        this.timeFormat = TimeFormat.DOUBLE;
        this.controller = controller;
        graphModel = Lookup.getDefault().lookup(GraphController.class).getModel(workspace);
        if (graphModel == null || graphModel.getGraph() == null) {
            throw new NullPointerException("The graph model and its underlying graph cannot be nulls.");
        }

        filterController = Lookup.getDefault().lookup(FilterController.class);
        filterModel = filterController.getModel(workspace);
        if (filterModel == null) {
            throw new NullPointerException("The filter model.");
        }

        //Index intervals
        timeIntervalIndex = new TimeIntervalIndex();
        AttributeModel attModel = Lookup.getDefault().lookup(AttributeController.class).getModel(workspace);
        nodeColumn = attModel.getNodeTable().getColumn(DynamicModel.TIMEINTERVAL_COLUMN);
        edgeColumn = attModel.getEdgeTable().getColumn(DynamicModel.TIMEINTERVAL_COLUMN);
        Graph graph = graphModel.getGraph();
        if (nodeColumn != null) {
            for (Node n : graph.getNodes()) {
                TimeInterval ti = (TimeInterval) n.getNodeData().getAttributes().getValue(nodeColumn.getIndex());
                if (ti != null) {
                    timeIntervalIndex.add(ti);
                }
            }
        }
        if (edgeColumn != null) {
            for (Edge e : graph.getEdges()) {
                TimeInterval ti = (TimeInterval) e.getEdgeData().getAttributes().getValue(edgeColumn.getIndex());
                if (ti != null) {
                    timeIntervalIndex.add(ti);
                }
            }
        }


        //Visible interval
        visibleTimeInterval = new TimeInterval(timeIntervalIndex.getMin(), timeIntervalIndex.getMax());

        //AttUtils
        final AttributeUtils attUtils = AttributeUtils.getDefault();

        //Listen columns
        attModel.addAttributeListener(new AttributeListener() {

            @Override
            public void attributesChanged(AttributeEvent event) {
                switch (event.getEventType()) {
                    case ADD_COLUMN:
                        if (nodeColumn == null) {
                            AttributeColumn[] addedColumns = event.getData().getAddedColumns();
                            for (int i = 0; i < addedColumns.length; i++) {
                                AttributeColumn col = addedColumns[i];
                                if (col.getId().equals(TIMEINTERVAL_COLUMN)) {
                                    if (attUtils.isNodeColumn(col)) {
                                        nodeColumn = col;
                                    } else {
                                        edgeColumn = col;
                                    }
                                }
                            }
                        }
                        break;
                    case REMOVE_COLUMN:
                        AttributeColumn[] addedColumns = event.getData().getAddedColumns();
                        for (int i = 0; i < addedColumns.length; i++) {
                            AttributeColumn col = addedColumns[i];
                            if (nodeColumn != null && nodeColumn == col) {
                                nodeColumn = null;
                            } else if (edgeColumn != null && edgeColumn == col) {
                                edgeColumn = null;
                            }
                        }
                        break;
                    case SET_VALUE:
                        AttributeValue[] values = event.getData().getTouchedValues();
                        for (int i = 0; i < values.length; i++) {
                            AttributeValue val = values[i];
                            if (val.getValue() != null) {
                                AttributeColumn col = values[i].getColumn();
                                if (col.getType().equals(AttributeType.TIME_INTERVAL)) {
                                    if (nodeColumn == null && attUtils.isNodeColumn(col) && col.getId().equals(TIMEINTERVAL_COLUMN)) {
                                        nodeColumn = col;
                                    } else if (edgeColumn == null && attUtils.isEdgeColumn(col) && col.getId().equals(TIMEINTERVAL_COLUMN)) {
                                        edgeColumn = col;
                                    }
                                    timeIntervalIndex.add((TimeInterval) val.getValue());
                                }
                            }
                        }
                        break;
                    default:
                        break;
                }
            }
        });

        graphModel.addGraphListener(new GraphListener() {

            @Override
            public void graphChanged(GraphEvent event) {
                if (event.getSource().isMainView()) {
                    switch (event.getEventType()) {
                        case REMOVE_EDGES:
                            if (edgeColumn != null) {
                                for (Edge e : event.getData().removedEdges()) {
                                    TimeInterval ti = (TimeInterval) e.getEdgeData().getAttributes().getValue(edgeColumn.getIndex());
                                    if (ti != null) {
                                        timeIntervalIndex.remove(ti);
                                    }
                                }
                            }
                            break;
                        case REMOVE_NODES:
                            if (nodeColumn != null) {
                                for (Node n : event.getData().removedNodes()) {
                                    TimeInterval ti = (TimeInterval) n.getNodeData().getAttributes().getValue(nodeColumn.getIndex());
                                    if (ti != null) {
                                        timeIntervalIndex.remove(ti);
                                    }
                                }
                            }
                        case CLEAR_NODES:
                            timeIntervalIndex.clear();
                            break;
                        default:
                            break;
                    }
                }
            }
        });
    }

    @Override
    public DynamicGraph createDynamicGraph(Graph graph) {
        return new DynamicGraphImpl(graph);
    }

    @Override
    public DynamicGraph createDynamicGraph(Graph graph, TimeInterval interval) {
        return new DynamicGraphImpl(graph, interval.getLow(), interval.getHigh());
    }

    @Override
    public TimeInterval getVisibleInterval() {
        return visibleTimeInterval;
    }

    public void setVisibleTimeInterval(TimeInterval visibleTimeInterval) {
        if (!Double.isNaN(visibleTimeInterval.getLow()) && !Double.isNaN(visibleTimeInterval.getHigh()) && !this.visibleTimeInterval.equals(visibleTimeInterval)) {
            this.visibleTimeInterval = visibleTimeInterval;

            //Filters
            Query dynamicQuery = null;
            boolean selecting = false;
            if (filterModel.getCurrentQuery() != null) {
                //Look if current query is dynamic - filtering must be active
                Query query = filterModel.getCurrentQuery();
                Query[] dynamicQueries = query.getQueries(DynamicRangeFilter.class);
                if (dynamicQueries.length > 0) {
                    dynamicQuery = dynamicQueries[0];
                    selecting = filterModel.isSelecting();
                }
            } else if (filterModel.getQueries().length == 1) {
                //Look if a dynamic query alone exists
                Query query = filterModel.getQueries()[0];
                if (query.getChildren().length == 0 && query.getQueries(DynamicRangeFilter.class).length == 1) {
                    dynamicQuery = query;
                }
            }
            if (Double.isInfinite(visibleTimeInterval.getLow()) && Double.isInfinite(visibleTimeInterval.getHigh())) {
                if (dynamicQuery != null) {
                    filterController.remove(dynamicQuery);
                }
            } else {
                if (dynamicQuery == null) {
                    //Create dynamic filter
                    DynamicRangeBuilder rangeBuilder = filterModel.getLibrary().getLookup().lookup(DynamicRangeBuilder.class);
                    FilterBuilder[] fb = rangeBuilder.getBuilders();
                    if (fb.length > 0) {
                        DynamicRangeFilter filter = (DynamicRangeFilter) fb[0].getFilter();
                        dynamicQuery = filterController.createQuery(filter);
                        filterController.add(dynamicQuery);
                    }
                }
                if (dynamicQuery != null) {
                    if (selecting) {
                        filterController.selectVisible(dynamicQuery);
                    } else {
                        filterController.filterVisible(dynamicQuery);
                    }
                }
            }


            // Trigger Event
            controller.fireModelEvent(new DynamicModelEvent(DynamicModelEvent.EventType.VISIBLE_INTERVAL, this, visibleTimeInterval));
        }
    }

    @Override
    public boolean isDynamicGraph() {
        return !Double.isInfinite(timeIntervalIndex.getMax()) || !Double.isInfinite(timeIntervalIndex.getMin());
    }

    @Override
    public TimeFormat getTimeFormat() {
        return timeFormat;
    }

    public void setTimeFormat(TimeFormat timeFormat) {
        this.timeFormat = timeFormat;
    }

    @Override
    public double getMin() {
        return timeIntervalIndex.getMin();
    }

    @Override
    public double getMax() {
        return timeIntervalIndex.getMax();
    }

    public double[] getPoints() {
        Double[] points = timeIntervalIndex.getPoints();
        double[] d = new double[points.length];
        for (int i = 0; i < d.length; i++) {
            d[i] = (double) points[i];
        }
        return d;
    }

    private class TimeIntervalIndex {

        private SortedMap<Double, Integer> lowMap = new TreeMap<Double, Integer>();
        private SortedMap<Double, Integer> highMap = new TreeMap<Double, Integer>();
        private TreeSet<Double> pointsSet = new TreeSet<Double>();

        public void add(TimeInterval interval) {
            boolean newDynamic = false;
            double min = getMin();
            double max = getMax();
            if (lowMap.isEmpty() && highMap.isEmpty()) {
                newDynamic = true;
            }
            Double low = interval.getLow();
            Double high = interval.getHigh();
            if (low != Double.NEGATIVE_INFINITY) {
                Integer c = lowMap.get((Double) interval.getLow());
                if (c == null) {
                    lowMap.put(low, 1);
                    pointsSet.add(low);
                } else {
                    lowMap.put(low, c + 1);
                }
            }
            if (high != Double.POSITIVE_INFINITY) {
                Integer c = highMap.get((Double) interval.getHigh());
                if (c == null) {
                    highMap.put(high, 1);
                    pointsSet.add(high);
                } else {
                    highMap.put(high, c + 1);
                }
            }
            if (newDynamic) {
                setDynamic(true);
            } else {
                double newMin = getMin();
                double newMax = getMax();
                if (newMin != min) {
                    setNewMin(min);
                }
                if (newMax != max) {
                    setNewMax(max);
                }
            }
        }

        public void remove(TimeInterval interval) {
            double min = getMin();
            double max = getMax();
            Double low = interval.getLow();
            Double high = interval.getHigh();
            if (low != Double.NEGATIVE_INFINITY) {
                Integer c = lowMap.get((Double) interval.getLow());
                if (c != null) {
                    if (c - 1 == 0) {
                        lowMap.remove(low);
                        pointsSet.remove(low);
                    } else {
                        lowMap.put(low, c - 1);
                    }
                } else {
                    System.err.println("Problem, the interval is not there");
                }
            }
            if (high != Double.POSITIVE_INFINITY) {
                Integer c = highMap.get((Double) interval.getHigh());
                if (c != null) {
                    if (c - 1 == 0) {
                        highMap.remove(high);
                        pointsSet.remove(high);
                    } else {
                        highMap.put(high, c - 1);
                    }
                } else {
                    System.err.println("Problem, the interval is not there");
                }
            }

            if (lowMap.isEmpty() && highMap.isEmpty()) {
                setDynamic(false);
            } else {
                double newMin = getMin();
                double newMax = getMax();
                if (newMin != min) {
                    setNewMin(min);
                }
                if (newMax != max) {
                    setNewMax(max);
                }
            }
        }

        public void clear() {
            lowMap.clear();
            highMap.clear();
        }

        public double getMin() {
            if (lowMap.isEmpty() && highMap.isEmpty()) {
                return Double.NEGATIVE_INFINITY;
            } else if (lowMap.isEmpty()) {
                return highMap.firstKey();
            } else {
                return lowMap.firstKey();
            }
        }

        public double getMax() {
            if (lowMap.isEmpty() && highMap.isEmpty()) {
                return Double.POSITIVE_INFINITY;
            } else if (highMap.isEmpty()) {
                return lowMap.lastKey();
            } else {
                return highMap.lastKey();
            }
        }

        public Double[] getPoints() {
            return pointsSet.toArray(new Double[0]);
        }

        private void setDynamic(boolean dynamic) {
            controller.fireModelEvent(new DynamicModelEvent(DynamicModelEvent.EventType.IS_DYNAMIC, DynamicModelImpl.this, dynamic));
        }

        private void setNewMin(double min) {
            controller.fireModelEvent(new DynamicModelEvent(DynamicModelEvent.EventType.MIN_CHANGED, DynamicModelImpl.this, min));
        }

        private void setNewMax(double max) {
            controller.fireModelEvent(new DynamicModelEvent(DynamicModelEvent.EventType.MAX_CHANGED, DynamicModelImpl.this, max));
        }
    }
}
