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
package org.gephi.io.processor.plugin;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeOrigin;
import org.gephi.data.attributes.api.AttributeRow;
import org.gephi.data.attributes.api.AttributeTable;
import org.gephi.data.attributes.api.AttributeType;
import org.gephi.data.attributes.api.AttributeValue;
import org.gephi.data.attributes.type.DynamicType;
import org.gephi.data.attributes.type.Interval;
import org.gephi.data.attributes.type.TimeInterval;
import org.gephi.data.attributes.type.TypeConvertor;
import org.gephi.data.properties.PropertiesColumn;
import org.gephi.dynamic.DynamicUtilities;
import org.gephi.dynamic.api.DynamicController;
import org.gephi.dynamic.api.DynamicModel;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphFactory;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.HierarchicalGraph;
import org.gephi.graph.api.Node;
import org.gephi.io.importer.api.EdgeDraft.EdgeType;
import org.gephi.io.importer.api.EdgeDraftGetter;
import org.gephi.io.importer.api.NodeDraftGetter;
import org.gephi.io.processor.spi.Processor;
import org.gephi.project.api.ProjectController;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Mathieu Bastian
 */
@ServiceProvider(service = Processor.class)
public class DynamicProcessor extends AbstractProcessor implements Processor {

    //Settings
    private boolean dateMode = true;
    private String date = "";
    //Variable
    private double point;

    public void process() {
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        //Workspace
        if (workspace == null) {
            workspace = pc.getCurrentWorkspace();
            if (workspace == null) {
                //Append mode but no workspace
                workspace = pc.newWorkspace(pc.getCurrentProject());
                pc.openWorkspace(workspace);
            }
        }
        if (container.getSource() != null) {
            pc.setSource(workspace, container.getSource());
        }

        //Architecture
        GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getModel();

        HierarchicalGraph graph = null;
        switch (container.getEdgeDefault()) {
            case DIRECTED:
                graph = graphModel.getHierarchicalDirectedGraph();
                break;
            case UNDIRECTED:
                graph = graphModel.getHierarchicalUndirectedGraph();
                break;
            case MIXED:
                graph = graphModel.getHierarchicalMixedGraph();
                break;
            default:
                graph = graphModel.getHierarchicalMixedGraph();
                break;
        }
        GraphFactory factory = graphModel.factory();

        //Attributes - Manually merge models with new dynamic cols
        attributeModel = Lookup.getDefault().lookup(AttributeController.class).getModel();
        AttributeTable nodeTable = container.getAttributeModel().getNodeTable();
        AttributeTable edgeTable = container.getAttributeModel().getEdgeTable();
        for (AttributeColumn column : nodeTable.getColumns()) {
            AttributeColumn existingCol = attributeModel.getNodeTable().getColumn(column.getTitle());
            if (existingCol == null) {
                if (!column.getOrigin().equals(AttributeOrigin.PROPERTY)) {
                    AttributeType dynamicType = TypeConvertor.getDynamicType(column.getType());
                    if (dynamicType != null && !column.getType().isDynamicType()) {
                        attributeModel.getNodeTable().addColumn(column.getId(), column.getTitle(), dynamicType, column.getOrigin(), null);
                    } else {
                        attributeModel.getNodeTable().addColumn(column.getId(), column.getTitle(), column.getType(), column.getOrigin(), column.getDefaultValue());
                    }
                }
            }

        }
        for (AttributeColumn column : edgeTable.getColumns()) {
            AttributeColumn existingCol = attributeModel.getEdgeTable().getColumn(column.getTitle());
            if (existingCol == null) {
                if (!column.getOrigin().equals(AttributeOrigin.PROPERTY)) {
                    AttributeType dynamicType = TypeConvertor.getDynamicType(column.getType());
                    if (dynamicType != null && !column.getType().isDynamicType()) {
                        attributeModel.getEdgeTable().addColumn(column.getId(), column.getTitle(), dynamicType, column.getOrigin(), null);
                    } else {
                        attributeModel.getEdgeTable().addColumn(column.getId(), column.getTitle(), column.getType(), column.getOrigin(), column.getDefaultValue());
                    }
                }
            } else if (PropertiesColumn.EDGE_WEIGHT.getId().equals(column.getId())) {
                attributeModel.getEdgeTable().replaceColumn(attributeModel.getEdgeTable().getColumn(PropertiesColumn.EDGE_WEIGHT.getIndex()), PropertiesColumn.EDGE_WEIGHT.getId(), PropertiesColumn.EDGE_WEIGHT.getTitle(), AttributeType.DYNAMIC_FLOAT, AttributeOrigin.PROPERTY, null);
            }
        }

        //Get Time Interval Column
        AttributeColumn nodeDynamicColumn = attributeModel.getNodeTable().getColumn(DynamicModel.TIMEINTERVAL_COLUMN);
        AttributeColumn edgeDynamicColumn = attributeModel.getEdgeTable().getColumn(DynamicModel.TIMEINTERVAL_COLUMN);
        if (nodeDynamicColumn == null) {
            nodeDynamicColumn = attributeModel.getNodeTable().addColumn(DynamicModel.TIMEINTERVAL_COLUMN, "Time Interval", AttributeType.TIME_INTERVAL, AttributeOrigin.PROPERTY, null);
        }
        if (edgeDynamicColumn == null) {
            edgeDynamicColumn = attributeModel.getEdgeTable().addColumn(DynamicModel.TIMEINTERVAL_COLUMN, "Time Interval", AttributeType.TIME_INTERVAL, AttributeOrigin.PROPERTY, null);
        }

        //Get Time stamp
        if (dateMode) {
            try {
                point = DynamicUtilities.getDoubleFromXMLDateString(date);
            } catch (Exception e) {
                throw new RuntimeException("The entered date can't be parsed");
            }
        } else {
            point = Double.parseDouble(date);
        }
        DynamicController dynamicController = Lookup.getDefault().lookup(DynamicController.class);
        dynamicController.setTimeFormat(dateMode ? DynamicModel.TimeFormat.DATE : DynamicModel.TimeFormat.DOUBLE);

        //Index existing graph
        Map<String, Node> map = new HashMap<String, Node>();
        for (Node n : graph.getNodes()) {
            String id = n.getNodeData().getId();
            if (id != null && !id.equalsIgnoreCase(String.valueOf(n.getId()))) {
                map.put(id, n);
            }
            if (n.getNodeData().getLabel() != null && !n.getNodeData().getLabel().isEmpty()) {
                map.put(n.getNodeData().getLabel(), n);
            }
        }

        //Create all nodes
        Set<Node> nodesInDraft = new HashSet<Node>();
        int newNodeCount = 0;
        for (NodeDraftGetter draftNode : container.getNodes()) {
            Node node = null;
            String id = draftNode.getId();
            String label = draftNode.getLabel();
            if (!draftNode.isAutoId() && id != null && map.get(id) != null) {
                node = map.get(id);
            } else if (label != null && map.get(label) != null) {
                node = map.get(label);
            }

            TimeInterval timeInterval = null;
            if (node == null) {
                //Node is new
                node = factory.newNode(draftNode.isAutoId() ? null : draftNode.getId());
                flushToNode(draftNode, node);
                draftNode.setNode(node);
                newNodeCount++;
            } else {
                timeInterval = (TimeInterval) node.getNodeData().getAttributes().getValue(nodeDynamicColumn.getIndex());
                flushToNodeAttributes(draftNode, node);
                draftNode.setNode(node);
            }
            nodesInDraft.add(node);

            //Add Point
            node.getNodeData().getAttributes().setValue(nodeDynamicColumn.getIndex(), addPoint(timeInterval, point));
        }

        //Push nodes in data structure
        for (NodeDraftGetter draftNode : container.getNodes()) {
            Node n = draftNode.getNode();
            NodeDraftGetter[] parents = draftNode.getParents();
            if (parents != null) {
                for (int i = 0; i < parents.length; i++) {
                    Node parent = parents[i].getNode();
                    graph.addNode(n, parent);
                }
            } else {
                graph.addNode(n);
            }
        }

        //Remove point from all nodes not in draft
        for (Node node : graph.getNodes()) {
            if (!nodesInDraft.contains(node)) {
                TimeInterval timeInterval = (TimeInterval) node.getNodeData().getAttributes().getValue(nodeDynamicColumn.getIndex());
                node.getNodeData().getAttributes().setValue(nodeDynamicColumn.getIndex(), removePoint(timeInterval, point));
            }
        }

        //Create all edges and push to data structure
        Set<Edge> edgesInDraft = new HashSet<Edge>();
        int newEdgeCount = 0;
        for (EdgeDraftGetter draftEdge : container.getEdges()) {
            Node source = draftEdge.getSource().getNode();
            Node target = draftEdge.getTarget().getNode();
            Edge edge = graph.getEdge(source, target);
            TimeInterval timeInterval = null;
            if (edge == null) {
                //Edge is new
                switch (container.getEdgeDefault()) {
                    case DIRECTED:
                        edge = factory.newEdge(draftEdge.isAutoId() ? null : draftEdge.getId(), source, target, draftEdge.getWeight(), true);
                        break;
                    case UNDIRECTED:
                        edge = factory.newEdge(draftEdge.isAutoId() ? null : draftEdge.getId(), source, target, draftEdge.getWeight(), false);
                        break;
                    case MIXED:
                        edge = factory.newEdge(draftEdge.isAutoId() ? null : draftEdge.getId(), source, target, draftEdge.getWeight(), draftEdge.getType().equals(EdgeType.UNDIRECTED) ? false : true);
                        break;
                }
                newEdgeCount++;
                graph.addEdge(edge);
                flushToEdge(draftEdge, edge);
            } else {
                timeInterval = (TimeInterval) edge.getEdgeData().getAttributes().getValue(edgeDynamicColumn.getIndex());
                flushToEdgeAttributes(draftEdge, edge);
            }
            edgesInDraft.add(edge);

            //Add Point
            edge.getEdgeData().getAttributes().setValue(edgeDynamicColumn.getIndex(), addPoint(timeInterval, point));
        }

        //Remove point from all edges not in draft
        for (Edge edge : graph.getEdges()) {
            if (!edgesInDraft.contains(edge)) {
                TimeInterval timeInterval = (TimeInterval) edge.getEdgeData().getAttributes().getValue(edgeDynamicColumn.getIndex());
                edge.getEdgeData().getAttributes().setValue(edgeDynamicColumn.getIndex(), removePoint(timeInterval, point));
            }
        }

        System.out.println("# New Nodes loaded: " + newNodeCount + "\n# New Edges loaded: " + newEdgeCount);
        workspace = null;
    }

    @Override
    protected void flushToNodeAttributes(NodeDraftGetter nodeDraft, Node node) {
        if (node.getNodeData().getAttributes() != null) {
            AttributeRow row = (AttributeRow) node.getNodeData().getAttributes();
            for (int i = 0; i < row.countValues(); i++) {
                Object val = row.getValue(i);
                AttributeColumn col = row.getColumnAt(i);
                Object draftValue = nodeDraft.getAttributeRow().getValue(col.getId());
                if (col.getType().isDynamicType()) {
                    if (draftValue == null && val != null) {
                        removePoint(col.getType(), (DynamicType) val, point);
                    } else if (draftValue != null) {
                        DynamicType dynamicValue = addPoint(col.getType(), (DynamicType) val, draftValue, point);
                        row.setValue(col.getIndex(), dynamicValue);
                    }
                } else if (draftValue != null && !col.getOrigin().equals(AttributeOrigin.PROPERTY)) {
                    row.setValue(col.getIndex(), draftValue);
                }
            }
        }
    }

    @Override
    protected void flushToEdgeAttributes(EdgeDraftGetter edgeDraft, Edge edge) {
        if (edge.getEdgeData().getAttributes() != null) {
            AttributeRow row = (AttributeRow) edge.getEdgeData().getAttributes();
            for (int i = 0; i < row.countValues(); i++) {
                Object val = row.getValue(i);
                AttributeColumn col = row.getColumnAt(i);
                Object draftValue = edgeDraft.getAttributeRow().getValue(col);
                if (col.getId().equals(PropertiesColumn.EDGE_WEIGHT.getId())) {
                    draftValue = new Float(edgeDraft.getWeight());
                }
                if (col.getType().isDynamicType()) {
                    if (draftValue == null && val != null) {
                        removePoint(col.getType(), (DynamicType) val, point);
                    } else if (draftValue != null) {
                        DynamicType dynamicValue = addPoint(col.getType(), (DynamicType) val, draftValue, point);
                        row.setValue(col.getIndex(), dynamicValue);
                    }
                } else if (draftValue != null && !col.getOrigin().equals(AttributeOrigin.PROPERTY)) {
                    row.setValue(col.getIndex(), draftValue);
                }
            }
        }
    }

    private TimeInterval addPoint(TimeInterval source, double point) {
        if (source == null) {
            return new TimeInterval(point, Double.POSITIVE_INFINITY);
        }
        List<Interval<Double[]>> intervals = source.getIntervals(point, point);
        if (intervals.isEmpty()) {
            return new TimeInterval(source, point, Double.POSITIVE_INFINITY);
        }
        return source;
    }

    private DynamicType addPoint(AttributeType type, DynamicType source, Object value, double point) {
        if (source == null) {
            return DynamicUtilities.createDynamicObject(type, new Interval(point, Double.POSITIVE_INFINITY, value));
        }
        List<Interval<?>> intervals = source.getIntervals(point, point);
        if (intervals.isEmpty()) {
            return DynamicUtilities.createDynamicObject(type, source, new Interval(point, Double.POSITIVE_INFINITY, value));
        } else if (intervals.size() > 1) {
            throw new RuntimeException("DynamicProcessor doesn't support overlapping intervals.");
        } else {
            Interval<?> toRemove = intervals.get(0);
            if (!toRemove.getValue().equals(value)) {
                Interval toAdd = new Interval(toRemove.getLow(), point, toRemove.isLowExcluded(), true, toRemove.getValue());
                DynamicType updated = DynamicUtilities.createDynamicObject(type, source, toAdd, toRemove);
                toAdd = new Interval(point, Double.POSITIVE_INFINITY, value);
                updated = DynamicUtilities.createDynamicObject(type, updated, toAdd);
                return updated;
            }
        }
        return source;
    }

    private TimeInterval removePoint(TimeInterval source, double point) {
        if (source == null) {
            return null;
        }
        List<Interval<Double[]>> intervals = source.getIntervals(point, point);
        if (intervals.size() > 1) {
            throw new RuntimeException("DynamicProcessor doesn't support overlapping intervals.");
        } else if (!intervals.isEmpty()) {
            Interval<Double[]> toRemove = intervals.get(0);
            if (toRemove.getLow() >= point) {
                return source;
            }

            Double[] toAdd = new Double[]{toRemove.getLow(), point};

            return new TimeInterval(source, toAdd[0], toAdd[1], toRemove.isLowExcluded(), true, toRemove.getLow(), toRemove.getHigh(), toRemove.isLowExcluded(), toRemove.isHighExcluded());
        }
        return source;
    }

    private DynamicType removePoint(AttributeType type, DynamicType source, double point) {
        if (source == null) {
            return null;
        }
        List<Interval<?>> intervals = source.getIntervals(point, point);
        if (intervals.size() > 1) {
            throw new RuntimeException("DynamicProcessor doesn't support overlapping intervals.");
        } else if (!intervals.isEmpty()) {
            Interval<?> toRemove = intervals.get(0);
            if (toRemove.getLow() >= point) {
                return source;
            }

            Interval toAdd = new Interval(toRemove.getLow(), point, toRemove.isLowExcluded(), true, toRemove.getValue());
            return DynamicUtilities.createDynamicObject(type, source, toAdd, toRemove);
        }
        return source;
    }

    public String getDisplayName() {
        return NbBundle.getMessage(DynamicProcessor.class, "DynamicProcessor.displayName");
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public boolean isDateMode() {
        return dateMode;
    }

    public void setDateMode(boolean dateMode) {
        this.dateMode = dateMode;
    }
}
