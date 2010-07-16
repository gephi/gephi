/*
Copyright 2008-2010 Gephi
Authors : Eduardo Ramos <eduramiba@gmail.com>
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gephi.datalaboratory.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeOrigin;
import org.gephi.data.attributes.api.AttributeRow;
import org.gephi.data.attributes.api.AttributeTable;
import org.gephi.data.attributes.api.AttributeType;
import org.gephi.data.attributes.api.AttributeUtils;
import org.gephi.data.attributes.api.AttributeValue;
import org.gephi.data.attributes.type.BooleanList;
import org.gephi.data.attributes.type.StringList;
import org.gephi.data.properties.PropertiesColumn;
import org.gephi.datalaboratory.api.AttributeColumnsController;
import org.gephi.datalaboratory.api.GraphElementsController;
import org.gephi.graph.api.Attributes;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.Node;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 * Implementation of the AttributeColumnsController interface
 * declared in the Data Laboratory API.
 * @author Eduardo Ramos <eduramiba@gmail.com>
 * @see AttributeColumnsController
 */
@ServiceProvider(service = AttributeColumnsController.class)
public class AttributeColumnsControllerImpl implements AttributeColumnsController {

    public AttributeColumn addAttributeColumn(AttributeTable table, String title, AttributeType type) {
        String columnId = String.valueOf(table.countColumns() + 1);
        title = title != null ? title : "";
        return table.addColumn(columnId, title, type, AttributeOrigin.DATA, null);
    }

    public void deleteAttributeColumn(AttributeTable table, AttributeColumn column) {
        if (canDeleteColumn(column)) {
            table.removeColumn(column);
        }
    }

    public AttributeColumn duplicateColumn(AttributeTable table, AttributeColumn column, String title, AttributeType type) {
        AttributeColumn newColumn = addAttributeColumn(table, title, type);
        int oldColumnIndex = column.getIndex();
        int newColumnIndex = newColumn.getIndex();
        if (type != column.getType()) {
            Object value;
            for (Attributes row : getTableAttributeRows(table)) {
                value = row.getValue(oldColumnIndex);
                if (value != null) {
                    try {
                        value = type.parse(value.toString());//Try to convert to new type
                    } catch (Exception ex) {
                        value = null;//Could not parse
                    }
                }
                row.setValue(newColumnIndex, value);
            }
        } else {
            for (Attributes attributes : getTableAttributeRows(table)) {
                attributes.setValue(newColumnIndex, attributes.getValue(oldColumnIndex));
            }
        }
        return newColumn;
    }

    public void clearColumnData(AttributeTable table, AttributeColumn column) {
        if (canChangeColumnData(column)) {
            int columnIndex = column.getIndex();
            for (Attributes attributes : getTableAttributeRows(table)) {
                attributes.setValue(columnIndex, null);
            }
        }
    }

    public Map<Object, Integer> calculateColumnValuesFrequencies(AttributeTable table, AttributeColumn column) {
        Map<Object, Integer> valuesFrequencies = new HashMap<Object, Integer>();
        Object value;
        for (Attributes row : getTableAttributeRows(table)) {
            value = row.getValue(column.getIndex());
            if (valuesFrequencies.containsKey(value)) {
                valuesFrequencies.put(value, new Integer(valuesFrequencies.get(value) + 1));
            } else {
                valuesFrequencies.put(value, new Integer(1));
            }
        }

        return valuesFrequencies;
    }

    public void createBooleanMatchesColumn(AttributeTable table, AttributeColumn column, String newColumnTitle, Pattern pattern) {
        if (pattern != null) {
            AttributeColumn newColumn = addAttributeColumn(table, newColumnTitle, AttributeType.BOOLEAN);
            Matcher matcher;
            Object value;
            for (Attributes row : getTableAttributeRows(table)) {
                value = row.getValue(column.getIndex());
                if (value != null) {
                    matcher = pattern.matcher(value.toString());
                } else {
                    matcher = pattern.matcher("");
                }
                row.setValue(newColumn.getIndex(), new Boolean(matcher.matches()));
            }
        }
    }

    public void NegateBooleanColumn(AttributeTable table, AttributeColumn column) {
        if (isColumnOfType(column, AttributeType.BOOLEAN)) {
            negateColumnBooleanType(table, column);
        } else if (isColumnOfType(column, AttributeType.LIST_BOOLEAN)) {
            negateColumnListBooleanType(table, column);
        } else {
            throw new IllegalArgumentException();
        }
    }

    public void createFoundGroupsListColumn(AttributeTable table, AttributeColumn column, String newColumnTitle, Pattern pattern) {
        if (pattern != null) {
            AttributeColumn newColumn = addAttributeColumn(table, newColumnTitle, AttributeType.LIST_STRING);
            Matcher matcher;
            Object value;
            ArrayList<String> foundGroups = new ArrayList<String>();
            for (Attributes attributes : getTableAttributeRows(table)) {
                value = attributes.getValue(column.getIndex());
                if (value != null) {
                    matcher = pattern.matcher(value.toString());
                } else {
                    matcher = pattern.matcher("");
                }
                while (matcher.find()) {
                    foundGroups.add(matcher.group());
                }
                if (foundGroups.size() > 0) {
                    attributes.setValue(newColumn.getIndex(), new StringList(foundGroups.toArray(new String[0])));
                    foundGroups.clear();
                } else {
                    attributes.setValue(newColumn.getIndex(), null);
                }
            }
        }
    }

    public void clearNodeData(Node node) {
        GraphElementsController gec = Lookup.getDefault().lookup(GraphElementsController.class);
        if (gec.isNodeInGraph(node)) {
            AttributeRow row = (AttributeRow) node.getNodeData().getAttributes();
            AttributeValue[] values = row.getValues();
            for (int i = 0; i < values.length; i++) {
                //Clear all except id and computed attributes:
                if (canChangeColumnData(values[i].getColumn())) {
                    row.setValue(i, null);
                }
            }
        }
    }

    public void clearNodesData(Node[] nodes) {
        for (Node n : nodes) {
            clearNodeData(n);
        }
    }

    public void clearEdgeData(Edge edge) {
        GraphElementsController gec = Lookup.getDefault().lookup(GraphElementsController.class);
        if (gec.isEdgeInGraph(edge)) {
            AttributeRow row = (AttributeRow) edge.getEdgeData().getAttributes();
            AttributeValue[] values = row.getValues();
            for (int i = 0; i < values.length; i++) {
                //Clear all except id and computed attributes:
                if (canChangeColumnData(values[i].getColumn())) {
                    row.setValue(i, null);
                }
            }
        }
    }

    public void clearEdgesData(Edge[] edges) {
        for (Edge e : edges) {
            clearEdgeData(e);
        }
    }

    public Attributes[] getTableAttributeRows(AttributeTable table) {
        Attributes[] attributes;
        if (isNodeTable(table)) {
            Node[] nodes = getNodesArray();
            attributes = new Attributes[nodes.length];
            for (int i = 0; i < nodes.length; i++) {
                attributes[i] = nodes[i].getNodeData().getAttributes();
            }
        } else {
            Edge[] edges = getEdgesArray();
            attributes = new Attributes[edges.length];
            for (int i = 0; i < edges.length; i++) {
                attributes[i] = edges[i].getEdgeData().getAttributes();
            }
        }
        return attributes;
    }

    public int getTableRowsCount(AttributeTable table) {
        if (isNodeTable(table)) {
            return Lookup.getDefault().lookup(GraphElementsController.class).getNodesCount();
        } else {
            return Lookup.getDefault().lookup(GraphElementsController.class).getEdgesCount();
        }
    }

    public boolean isNodeTable(AttributeTable table) {
        AttributeController ac = Lookup.getDefault().lookup(AttributeController.class);
        return table == ac.getModel().getNodeTable();
    }

    public boolean isEdgeTable(AttributeTable table) {
        AttributeController ac = Lookup.getDefault().lookup(AttributeController.class);
        return table == ac.getModel().getEdgeTable();
    }

    public boolean canDeleteColumn(AttributeColumn column) {
        return column.getOrigin() != AttributeOrigin.PROPERTY;
    }

    public boolean canChangeColumnData(AttributeColumn column) {
        AttributeUtils au = Lookup.getDefault().lookup(AttributeUtils.class);
        if (au.isNodeColumn(column)) {
            //Can change values of columns with DATA origin and label of nodes:
            return canChangeGenericColumnData(column) || column.getIndex() == PropertiesColumn.NODE_LABEL.getIndex();
        } else if (au.isEdgeColumn(column)) {
            return canChangeGenericColumnData(column) || column.getIndex() == PropertiesColumn.EDGE_LABEL.getIndex();
        } else {
            return canChangeGenericColumnData(column);
        }
    }

    public boolean isColumnOfType(AttributeColumn column, AttributeType type) {
        return column.getType() == type;
    }

    public boolean areColumnsOfType(AttributeColumn[] columns, AttributeType type) {
        for (AttributeColumn column : columns) {
            if (!isColumnOfType(column, type)) {
                return false;
            }
        }
        return true;
    }

    /************Private methods : ************/
    /**
     * Used for iterating through all nodes of the graph
     * @return Array with all graph nodes
     */
    private Node[] getNodesArray() {
        return Lookup.getDefault().lookup(GraphController.class).getModel().getHierarchicalGraph().getNodesTree().toArray();
    }

    /**
     * Used for iterating through all edges of the graph
     * @return Array with all graph edges
     */
    private Edge[] getEdgesArray() {
        Graph graph = Lookup.getDefault().lookup(GraphController.class).getModel().getGraph();
        return graph.getEdges().toArray();
    }

    /**
     * Only checks that a column is of type DATA and has a changeable AttributeType. (Does not check if it is the label of nodes/edges table)
     * Used in various methods to not repeat code.
     * @param column Column to check
     * @return True if the column data can be changed, false otherwise
     */
    private boolean canChangeGenericColumnData(AttributeColumn column) {
        return column.getOrigin() == AttributeOrigin.DATA && !column.getType().isDynamicType();
    }

    private void negateColumnBooleanType(AttributeTable table, AttributeColumn column) {
        final int columnIndex = column.getIndex();
        Object value;
        Boolean newValue;
        for (Attributes row : getTableAttributeRows(table)) {
            value = row.getValue(columnIndex);
            if (value != null) {
                newValue = !((Boolean) value);
                row.setValue(columnIndex, newValue);
            }
        }
    }

    private void negateColumnListBooleanType(AttributeTable table, AttributeColumn column) {
        final int columnIndex = column.getIndex();
        Object value;
        BooleanList list;
        Boolean[] newValues;
        for (Attributes row : getTableAttributeRows(table)) {
            value = row.getValue(columnIndex);
            if (value != null) {
                list = (BooleanList) value;
                newValues=new Boolean[list.size()];
                for (int i = 0; i < list.size(); i++) {
                    newValues[i]=!list.getItem(i);
                }
                row.setValue(columnIndex, new BooleanList(newValues));
            }
        }
    }
}
