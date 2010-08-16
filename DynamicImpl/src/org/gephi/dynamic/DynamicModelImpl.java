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
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeEvent;
import org.gephi.data.attributes.api.AttributeListener;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.data.attributes.type.TimeInterval;
import org.gephi.dynamic.api.DynamicGraph;
import org.gephi.dynamic.api.DynamicModel;
import org.gephi.dynamic.api.DynamicModelEvent;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphEvent;
import org.gephi.graph.api.GraphListener;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.project.api.Workspace;

/**
 * The default implementation of {@code DynamicModel}.
 *
 * @author Cezary Bartosiak
 */
public final class DynamicModelImpl implements DynamicModel {

    private DynamicControllerImpl controller;
    private GraphModel graphModel;
    private TimeInterval visibleTimeInterval;
    private TimeIntervalIndex timeIntervalIndex;
    private AttributeColumn nodeColumn;
    private AttributeColumn edgeColumn;

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

        this.controller = controller;
        graphModel = workspace.getLookup().lookup(GraphModel.class);
        if (graphModel == null || graphModel.getGraph() == null) {
            throw new NullPointerException("The graph model and its underlying graph cannot be nulls.");
        }

        //Index intervals
        timeIntervalIndex = new TimeIntervalIndex();
        AttributeModel attModel = workspace.getLookup().lookup(AttributeModel.class);
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

        //Listen columns
        attModel.getNodeTable().addAttributeListener(new AttributeListener() {

            @Override
            public void attributesChanged(AttributeEvent event) {
                switch (event.getEventType()) {
                    case ADD_COLUMN:
                        if (nodeColumn == null) {
                            AttributeColumn col = (AttributeColumn) event.getData();
                            if (col.getId().equals(TIMEINTERVAL_COLUMN)) {
                                nodeColumn = col;
                            }
                        }
                        break;
                    case REMOVE_COLUMN:
                        if (nodeColumn != null && nodeColumn == event.getData()) {
                            nodeColumn = null;
                        }
                        break;
                    default:
                        break;
                }
            }
        });
        attModel.getEdgeTable().addAttributeListener(new AttributeListener() {

            @Override
            public void attributesChanged(AttributeEvent event) {
                switch (event.getEventType()) {
                    case ADD_COLUMN:
                        if (edgeColumn == null) {
                            AttributeColumn col = (AttributeColumn) event.getData();
                            if (col.getId().equals(TIMEINTERVAL_COLUMN)) {
                                edgeColumn = col;
                            }
                        }
                        break;
                    case REMOVE_COLUMN:
                        if (edgeColumn != null && edgeColumn == event.getData()) {
                            edgeColumn = null;
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
                        case ADD_EDGES:
                            if (edgeColumn != null) {
                                for (Edge e : event.getData().addedEdges()) {
                                    TimeInterval ti = (TimeInterval) e.getEdgeData().getAttributes().getValue(edgeColumn.getIndex());
                                    if (ti != null) {
                                        timeIntervalIndex.add(ti);
                                    }
                                }
                            }
                            break;
                        case ADD_NODES:
                            if (nodeColumn != null) {
                                for (Node n : event.getData().addedNodes()) {
                                    TimeInterval ti = (TimeInterval) n.getNodeData().getAttributes().getValue(nodeColumn.getIndex());
                                    if (ti != null) {
                                        timeIntervalIndex.add(ti);
                                    }
                                }
                            }
                            break;
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
        this.visibleTimeInterval = visibleTimeInterval;
        // Trigger Event
        controller.fireModelEvent(new DynamicModelEvent(DynamicModelEvent.EventType.VISIBLE_INTERVAL_CHANGED, this, visibleTimeInterval));
    }

    @Override
    public double getMin() {
        return timeIntervalIndex.getMin();
    }

    @Override
    public double getMax() {
        return timeIntervalIndex.getMax();
    }

    private static class TimeIntervalIndex {

        private SortedMap<Double, Integer> lowMap = new TreeMap<Double, Integer>();
        private SortedMap<Double, Integer> highMap = new TreeMap<Double, Integer>();

        public void add(TimeInterval interval) {
            Double low = interval.getLow();
            Double high = interval.getHigh();
            if (low != Double.NEGATIVE_INFINITY) {
                Integer c = lowMap.get((Double) interval.getLow());
                if (c == null) {
                    lowMap.put(low, 1);
                } else {
                    lowMap.put(low, c + 1);
                }
            }
            if (high != Double.POSITIVE_INFINITY) {
                Integer c = highMap.get((Double) interval.getHigh());
                if (c == null) {
                    highMap.put(low, 1);
                } else {
                    highMap.put(low, c + 1);
                }
            }
        }

        public void remove(TimeInterval interval) {
            Double low = interval.getLow();
            Double high = interval.getHigh();
            if (low != Double.NEGATIVE_INFINITY) {
                Integer c = lowMap.get((Double) interval.getLow());
                if (c != null) {
                    if (c - 1 == 0) {
                        lowMap.remove(low);
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
                    } else {
                        highMap.put(high, c - 1);
                    }
                } else {
                    System.err.println("Problem, the interval is not there");
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
    }
}
