/*
 * Copyright 2008-2010 Gephi
 * Authors : Cezary Bartosiak
 *           Mathieu Bastian <mathieu.bastian@gephi.org>
 * Website : http://www.gephi.org
 * 
 * This file is part of Gephi.
 *
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
package org.gephi.dynamic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeEvent;
import org.gephi.data.attributes.api.AttributeListener;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.data.attributes.api.AttributeUtils;
import org.gephi.data.attributes.api.AttributeValue;
import org.gephi.data.attributes.api.Estimator;
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
 * @author Mathieu Bastian
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
    private Estimator estimator = Estimator.FIRST;
    private Estimator numberEstimator = Estimator.AVERAGE;

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
                        case REMOVE_NODES_AND_EDGES:
                            if (!edgeDynamicColumns.isEmpty() && event.getData().removedEdges() != null) {
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
                            if (!nodeDynamicColumns.isEmpty() && event.getData().removedNodes() != null) {
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
        if (dynamicCols.length > 0) {
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
        if (dynamicCols.length > 0) {
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
    public DynamicGraph createDynamicGraph(Graph graph, Interval interval) {
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
        boolean res = !Double.isInfinite(timeIntervalIndex.getMax()) || !Double.isInfinite(timeIntervalIndex.getMin());
        return res || !nodeDynamicColumns.isEmpty() || !edgeDynamicColumns.isEmpty();
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

    @Override
    public Estimator getEstimator() {
        return estimator;
    }

    @Override
    public Estimator getNumberEstimator() {
        return numberEstimator;
    }

    public void setEstimator(Estimator estimator) {
        this.estimator = estimator;
    }

    public void setNumberEstimator(Estimator numberEstimator) {
        this.numberEstimator = numberEstimator;
    }

    @Override
    public boolean hasDynamicEdges() {
        return attributeModel.getEdgeTable().hasColumn(TIMEINTERVAL_COLUMN);
    }

    @Override
    public boolean hasDynamicNodes() {
        return attributeModel.getNodeTable().hasColumn(TIMEINTERVAL_COLUMN);
    }
}
