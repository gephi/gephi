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
package org.gephi.ranking.impl;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.gephi.data.attributes.api.AttributeEvent;
import org.gephi.ranking.api.RankingModel;
import org.gephi.ranking.api.NodeRanking;
import org.gephi.ranking.api.EdgeRanking;
import org.gephi.ranking.api.Ranking;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeListener;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.data.attributes.api.Estimator;
import org.gephi.data.attributes.type.TimeInterval;
import org.gephi.dynamic.api.DynamicController;
import org.gephi.dynamic.api.DynamicModel;
import org.gephi.graph.api.DirectedGraph;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.HierarchicalGraph;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.project.api.WorkspaceListener;
import org.openide.util.Lookup;

/**
 *
 * @author Mathieu Bastian
 */
public class RankingModelImpl implements RankingModel, AttributeListener {

    private final List<ChangeListener> listeners = new ArrayList<ChangeListener>();
    private DynamicModel dynamicModel;
    private Estimator defaultEstimator;

    public RankingModelImpl() {
        final ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        final AttributeController ac = Lookup.getDefault().lookup(AttributeController.class);
        pc.addWorkspaceListener(new WorkspaceListener() {

            public void initialize(Workspace workspace) {
            }

            public void select(Workspace workspace) {
                AttributeModel attributeModel = workspace.getLookup().lookup(AttributeModel.class);
                attributeModel.addAttributeListener(RankingModelImpl.this);
            }

            public void unselect(Workspace workspace) {
                AttributeModel attributeModel = workspace.getLookup().lookup(AttributeModel.class);
                attributeModel.removeAttributeListener(RankingModelImpl.this);
                dynamicModel = null;
            }

            public void close(Workspace workspace) {
            }

            public void disable() {
                dynamicModel = null;
            }
        });
        if (pc.getCurrentWorkspace() != null) {
            AttributeModel attributeModel = pc.getCurrentWorkspace().getLookup().lookup(AttributeModel.class);
            attributeModel.addAttributeListener(RankingModelImpl.this);
        }

        defaultEstimator = Estimator.AVERAGE;
    }
    private Timer refreshTimer; //hack

    public void attributesChanged(AttributeEvent event) {
        if (event.getEventType().equals(AttributeEvent.EventType.ADD_COLUMN) || event.getEventType().equals(AttributeEvent.EventType.REMOVE_COLUMN)) {
            if (refreshTimer != null) {
                refreshTimer.restart();
            } else {
                refreshTimer = new Timer(1000, new ActionListener() {

                    public void actionPerformed(ActionEvent e) {
                        fireChangeEvent();
                    }
                });
                refreshTimer.setRepeats(false);
                refreshTimer.start();
            }
        }
    }

    public NodeRanking getDegreeRanking() {
        GraphModel model = Lookup.getDefault().lookup(GraphController.class).getModel();
        Graph graph = model.getGraphVisible();
        NodeRanking degreeRanking = RankingFactory.getNodeDegreeRanking(graph);
        if (degreeRanking.getMinimumValue() != null && degreeRanking.getMaximumValue() != null && !degreeRanking.getMinimumValue().equals(degreeRanking.getMaximumValue())) {
            return degreeRanking;
        }
        return null;
    }

    public NodeRanking getInDegreeRanking() {
        GraphModel model = Lookup.getDefault().lookup(GraphController.class).getModel();
        DirectedGraph graph = model.getDirectedGraphVisible();
        NodeRanking degreeRanking = RankingFactory.getNodeInDegreeRanking(graph);
        if (degreeRanking.getMinimumValue() != null && degreeRanking.getMaximumValue() != null && !degreeRanking.getMinimumValue().equals(degreeRanking.getMaximumValue())) {
            return degreeRanking;
        }
        return null;
    }

    public NodeRanking getOutDegreeRanking() {
        GraphModel model = Lookup.getDefault().lookup(GraphController.class).getModel();
        DirectedGraph graph = model.getDirectedGraphVisible();
        NodeRanking degreeRanking = RankingFactory.getNodeOutDegreeRanking(graph);
        if (degreeRanking.getMinimumValue() != null && degreeRanking.getMaximumValue() != null && !degreeRanking.getMinimumValue().equals(degreeRanking.getMaximumValue())) {
            return degreeRanking;
        }
        return null;
    }

    public NodeRanking getNodeAttributeRanking(AttributeColumn column) {
        GraphModel model = Lookup.getDefault().lookup(GraphController.class).getModel();
        Graph graph = model.getGraphVisible();
        if (RankingFactory.isNumberColumn(column)) {
            NodeRanking r = RankingFactory.getNodeAttributeRanking(column, graph);
            if (r.getMinimumValue() != null && r.getMaximumValue() != null && !r.getMinimumValue().equals(r.getMaximumValue())) {
                return r;
            }
        } else if (RankingFactory.isDynamicNumberColumn(column) && getDynamicModel() != null) {
            TimeInterval visibleInterval = dynamicModel.getVisibleInterval();
            NodeRanking r = RankingFactory.getNodeDynamicAttributeRanking(column, graph, visibleInterval, defaultEstimator);
            if (r.getMinimumValue() != null && r.getMaximumValue() != null && !r.getMinimumValue().equals(r.getMaximumValue())) {
                return r;
            }
        }
        return null;
    }

    public EdgeRanking getEdgeAttributeRanking(AttributeColumn column) {
        GraphModel model = Lookup.getDefault().lookup(GraphController.class).getModel();
        Graph graph = model.getGraphVisible();
        if (RankingFactory.isNumberColumn(column)) {
            EdgeRanking r = RankingFactory.getEdgeAttributeRanking(column, graph);
            if (r.getMinimumValue() != null && r.getMaximumValue() != null && !r.getMinimumValue().equals(r.getMaximumValue())) {
                return r;
            }
        } else if (RankingFactory.isDynamicNumberColumn(column) && getDynamicModel() != null) {
            TimeInterval visibleInterval = dynamicModel.getVisibleInterval();
            EdgeRanking r = RankingFactory.getEdgeDynamicAttributeRanking(column, graph, visibleInterval, defaultEstimator);
            if (r.getMinimumValue() != null && r.getMaximumValue() != null && !r.getMinimumValue().equals(r.getMaximumValue())) {
                return r;
            }
        }
        return null;
    }

    public NodeRanking[] getNodeRanking() {
        AttributeController attributeController = Lookup.getDefault().lookup(AttributeController.class);
        List<Ranking> rankingList = new ArrayList<Ranking>();
        GraphModel model = Lookup.getDefault().lookup(GraphController.class).getModel();
        Graph graph = model.getGraphVisible();

        //Topology
        NodeRanking degreeRanking = RankingFactory.getNodeDegreeRanking(graph);
        if (degreeRanking.getMinimumValue() != null && degreeRanking.getMaximumValue() != null && !degreeRanking.getMinimumValue().equals(degreeRanking.getMaximumValue())) {
            rankingList.add(degreeRanking);
        }

        if (model.isDirected()) {
            DirectedGraph directedGraph = model.getDirectedGraphVisible();
            NodeRanking inDegreeRanking = RankingFactory.getNodeInDegreeRanking(directedGraph);
            if (inDegreeRanking.getMinimumValue() != null && inDegreeRanking.getMaximumValue() != null && !inDegreeRanking.getMinimumValue().equals(inDegreeRanking.getMaximumValue())) {
                rankingList.add(inDegreeRanking);
            }
            NodeRanking outDegreeRanking = RankingFactory.getNodeOutDegreeRanking(directedGraph);
            if (outDegreeRanking.getMinimumValue() != null && outDegreeRanking.getMaximumValue() != null && !outDegreeRanking.getMinimumValue().equals(outDegreeRanking.getMaximumValue())) {
                rankingList.add(outDegreeRanking);
            }
        }

        if (model.isHierarchical()) {
            HierarchicalGraph hierarchicalGraph = model.getHierarchicalGraphVisible();
            NodeRanking childrenRanking = RankingFactory.getNodeChildrenCountRanking(hierarchicalGraph);
            if (childrenRanking.getMinimumValue() != null && childrenRanking.getMaximumValue() != null && !childrenRanking.getMinimumValue().equals(childrenRanking.getMaximumValue())) {
                rankingList.add(childrenRanking);
            }
        }

        //Attributes
        int nativeCount = rankingList.size();
        for (AttributeColumn column : attributeController.getModel().getNodeTable().getColumns()) {
            if (RankingFactory.isNumberColumn(column)) {
                NodeRanking r = RankingFactory.getNodeAttributeRanking(column, graph);
                if (r.getMinimumValue() != null && r.getMaximumValue() != null && !r.getMinimumValue().equals(r.getMaximumValue())) {
                    rankingList.add(r);
                }
            } else if (RankingFactory.isDynamicNumberColumn(column) && getDynamicModel() != null) {
                TimeInterval visibleInterval = dynamicModel.getVisibleInterval();
                NodeRanking r = RankingFactory.getNodeDynamicAttributeRanking(column, graph, visibleInterval, defaultEstimator);
                if (r.getMinimumValue() != null && r.getMaximumValue() != null && !r.getMinimumValue().equals(r.getMaximumValue())) {
                    rankingList.add(r);
                }
            }
        }

        NodeRanking[] rankingArray = rankingList.toArray(new NodeRanking[0]);
        Arrays.sort(rankingArray, nativeCount, rankingArray.length, new Comparator<NodeRanking>() {

            public int compare(NodeRanking a, NodeRanking b) {
                return (a.toString().compareTo(b.toString()));
            }
        });

        return rankingArray;
    }

    public EdgeRanking[] getEdgeRanking() {
        AttributeController attributeController = Lookup.getDefault().lookup(AttributeController.class);
        Graph graph = Lookup.getDefault().lookup(GraphController.class).getModel().getGraphVisible();
        List<Ranking> rankingList = new ArrayList<Ranking>();
        for (AttributeColumn column : attributeController.getModel().getEdgeTable().getColumns()) {
            if (RankingFactory.isNumberColumn(column)) {
                EdgeRanking r = RankingFactory.getEdgeAttributeRanking(column, graph);
                if (r.getMinimumValue() != null && r.getMaximumValue() != null && !r.getMinimumValue().equals(r.getMaximumValue())) {
                    rankingList.add(r);
                }
            } else if (RankingFactory.isDynamicNumberColumn(column) && getDynamicModel() != null) {
                TimeInterval visibleInterval = dynamicModel.getVisibleInterval();
                EdgeRanking r = RankingFactory.getEdgeDynamicAttributeRanking(column, graph, visibleInterval, defaultEstimator);
                if (r.getMinimumValue() != null && r.getMaximumValue() != null && !r.getMinimumValue().equals(r.getMaximumValue())) {
                    rankingList.add(r);
                }
            }
        }
        return rankingList.toArray(new EdgeRanking[0]);
    }

    public DynamicModel getDynamicModel() {
        if (dynamicModel == null) {
            DynamicController dynamicController = Lookup.getDefault().lookup(DynamicController.class);
            if (dynamicController != null) {
                dynamicModel = dynamicController.getModel();
            }
        }
        return dynamicModel;
    }

    public void setDefaultEstimator(Estimator estimator) {
        this.defaultEstimator = estimator;
    }

    public void addChangeListener(ChangeListener changeListener) {
        if (!listeners.contains(changeListener)) {
            listeners.add(changeListener);
        }
    }

    public void removeChangeListener(ChangeListener changeListener) {
        listeners.remove(changeListener);
    }

    public void fireChangeEvent() {
        ChangeEvent changeEvent = new ChangeEvent(this);
        for (ChangeListener changeListener : listeners) {
            changeListener.stateChanged(changeEvent);
        }
    }
}
