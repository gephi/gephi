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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.SortedMap;
import java.util.SortedSet;
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
import org.gephi.data.attributes.type.DynamicType;
import org.gephi.data.attributes.type.Interval;
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
import org.gephi.graph.api.Attributes;
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

    protected final DynamicControllerImpl controller;
    private final FilterController filterController;
    private final DynamicIndex timeIntervalIndex;
    private final GraphModel graphModel;
    private final AttributeModel attributeModel;
    private final FilterModel filterModel;
    private final List<AttributeColumn> nodeDynamicColumns;
    private final List<AttributeColumn> edgeDynamicColumns;
    //Variables
    private TimeInterval visibleTimeInterval;
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
        timeIntervalIndex = new DynamicIndex(this);
        attributeModel = Lookup.getDefault().lookup(AttributeController.class).getModel(workspace);
        nodeDynamicColumns = Collections.synchronizedList(new ArrayList<AttributeColumn>());
        edgeDynamicColumns = Collections.synchronizedList(new ArrayList<AttributeColumn>());

        refresh();

        //Visible interval
        visibleTimeInterval = new TimeInterval(timeIntervalIndex.getMin(), timeIntervalIndex.getMax());

        //AttUtils
        final AttributeUtils attUtils = AttributeUtils.getDefault();

        //Listen columns
        AttributeListener attributeListener = new AttributeListener() {

            @Override
            public void attributesChanged(AttributeEvent event) {
                switch (event.getEventType()) {
                    case ADD_COLUMN:
                        AttributeColumn[] addedColumns = event.getData().getAddedColumns();
                        for (int i = 0; i < addedColumns.length; i++) {
                            AttributeColumn col = addedColumns[i];
                            if (col.getType().isDynamicType() && attUtils.isNodeColumn(col)) {
                                nodeDynamicColumns.add(col);
                            } else if (col.getType().isDynamicType() && attUtils.isEdgeColumn(col)) {
                                edgeDynamicColumns.add(col);
                            }
                        }
                        break;
                    case REMOVE_COLUMN:
                        AttributeColumn[] removedColumns = event.getData().getRemovedColumns();
                        for (int i = 0; i < removedColumns.length; i++) {
                            AttributeColumn col = removedColumns[i];
                            if (col.getType().isDynamicType() && attUtils.isNodeColumn(col)) {
                                nodeDynamicColumns.remove(col);
                            } else if (col.getType().isDynamicType() && attUtils.isEdgeColumn(col)) {
                                edgeDynamicColumns.remove(col);
                            }
                        }
                        break;
                    case SET_VALUE:
                        AttributeValue[] values = event.getData().getTouchedValues();
                        for (int i = 0; i < values.length; i++) {
                            AttributeValue val = values[i];
                            if (val.getValue() != null) {
                                AttributeColumn col = values[i].getColumn();
                                if (col.getType().isDynamicType()) {
                                    DynamicType<?> dynamicType = (DynamicType) val.getValue();
                                    for (Interval interval : dynamicType.getIntervals(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY)) {
                                        timeIntervalIndex.add(interval);
                                    }
                                }
                            }
                        }
                        break;
                    default:
                        break;
                }
            }
        };
        attributeModel.addAttributeListener(attributeListener);

        GraphListener graphListener = new GraphListener() {

            @Override
            public void graphChanged(GraphEvent event) {
                if (event.getSource().isMainView()) {
                    switch (event.getEventType()) {
                        case REMOVE_EDGES:
                            if (!edgeDynamicColumns.isEmpty()) {
                                AttributeColumn[] dynamicCols = edgeDynamicColumns.toArray(new AttributeColumn[0]);
                                for (Edge e : event.getData().removedEdges()) {
                                    Attributes attributeRow = e.getEdgeData().getAttributes();
                                    for (int i = 0; i < dynamicCols.length; i++) {
                                        DynamicType<?> ti = (DynamicType) attributeRow.getValue(dynamicCols[i].getIndex());
                                        if (ti != null) {
                                            for (Interval interval : ti.getIntervals(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY)) {
                                                timeIntervalIndex.remove(interval);
                                            }
                                        }
                                    }
                                }
                            }
                            break;
                        case REMOVE_NODES:
                            if (!nodeDynamicColumns.isEmpty()) {
                               AttributeColumn[] dynamicCols = edgeDynamicColumns.toArray(new AttributeColumn[0]);
                                for (Node n : event.getData().removedNodes()) {
                                    Attributes attributeRow = n.getNodeData().getAttributes();
                                    for (int i = 0; i < dynamicCols.length; i++) {
                                        DynamicType<?> ti = (DynamicType) attributeRow.getValue(dynamicCols[i].getIndex());
                                        if (ti != null) {
                                            for (Interval interval : ti.getIntervals(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY)) {
                                                timeIntervalIndex.remove(interval);
                                            }
                                        }
                                    }
                                }
                            }
                            break;
                        case CLEAR_NODES:
                            timeIntervalIndex.clear();
                            break;
                        default:
                            break;
                    }
                }
            }
        };
        graphModel.addGraphListener(graphListener);
    }

    private void refresh() {
        timeIntervalIndex.clear();
        for (AttributeColumn col : attributeModel.getNodeTable().getColumns()) {
            if (col.getType().isDynamicType()) {
                nodeDynamicColumns.add(col);
            }
        }
        AttributeColumn[] dynamicCols = nodeDynamicColumns.toArray(new AttributeColumn[0]);
        if (dynamicCols.length>0) {
            Graph graph = graphModel.getGraph();
            for (Node n : graph.getNodes()) {
                Attributes attributeRow = n.getNodeData().getAttributes();
                for (int i = 0; i < dynamicCols.length; i++) {
                    DynamicType<?> ti = (DynamicType) attributeRow.getValue(dynamicCols[i].getIndex());
                    if (ti != null) {
                        for (Interval interval : ti.getIntervals(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY)) {
                            timeIntervalIndex.add(interval);
                        }
                    }
                }
            }
        }
        for (AttributeColumn col : attributeModel.getNodeTable().getColumns()) {
            if (col.getType().isDynamicType()) {
                edgeDynamicColumns.add(col);
            }
        }
        dynamicCols = nodeDynamicColumns.toArray(new AttributeColumn[0]);
        if (dynamicCols.length>0) {
            Graph graph = graphModel.getGraph();
            for (Edge e : graph.getEdges()) {
                Attributes attributeRow = e.getEdgeData().getAttributes();
                for (int i = 0; i < dynamicCols.length; i++) {
                    DynamicType<?> ti = (DynamicType) attributeRow.getValue(dynamicCols[i].getIndex());
                    if (ti != null) {
                        for (Interval interval : ti.getIntervals(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY)) {
                            timeIntervalIndex.add(interval);
                        }
                    }
                }
            }
        }
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

            //Get or create Dynamic Query
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
}
