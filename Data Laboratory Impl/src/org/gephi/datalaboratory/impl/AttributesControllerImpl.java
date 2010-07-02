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

import java.util.HashMap;
import java.util.Map;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeOrigin;
import org.gephi.data.attributes.api.AttributeRow;
import org.gephi.data.attributes.api.AttributeTable;
import org.gephi.data.attributes.api.AttributeType;
import org.gephi.data.attributes.api.AttributeValue;
import org.gephi.data.properties.PropertiesColumn;
import org.gephi.datalaboratory.api.AttributesController;
import org.gephi.datalaboratory.api.GraphElementsController;
import org.gephi.graph.api.Attributes;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.Node;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 * Implementation of the AttributesController interface
 * declared in the Data Laboratory API.
 * @author Eduardo Ramos <eduramiba@gmail.com>
 * @see AttributesController
 */
@ServiceProvider(service = AttributesController.class)
public class AttributesControllerImpl implements AttributesController {

    public AttributeColumn addAttributeColumn(AttributeTable table, String title, AttributeType type) {
        String columnId = String.valueOf(table.countColumns() + 1);
        return table.addColumn(columnId, title, type, AttributeOrigin.DATA, null);
    }

    public void deleteAttributeColumn(AttributeTable table, AttributeColumn column) {
        table.removeColumn(column);
    }

    public AttributeColumn duplicateColumn(AttributeTable table, AttributeColumn column, String title, AttributeType type) {
        AttributeColumn newColumn = addAttributeColumn(table, title, type);
        int oldColumnIndex = column.getIndex();
        int newColumnIndex = newColumn.getIndex();
        if (type != column.getType()) {
            Object value;
            for (Attributes attributes : getTableAttributeRows(table)) {
                value = attributes.getValue(oldColumnIndex);
                if (value != null) {
                    try {
                        value = type.parse(value.toString());//Try to convert to new type
                    } catch (Exception ex) {
                        value = null;//Could not parse
                    }
                }
                attributes.setValue(newColumnIndex, value);
            }
        } else {
            for (Attributes attributes : getTableAttributeRows(table)) {
                attributes.setValue(newColumnIndex, attributes.getValue(oldColumnIndex));
            }
        }
        return newColumn;
    }

    public void clearColumnData(AttributeTable table, AttributeColumn column) {
        int columnIndex = column.getIndex();
        for (Attributes attributes : getTableAttributeRows(table)) {
            attributes.setValue(columnIndex, null);
        }
    }

    public Map<Object,Integer> calculateColumnValuesFrequencies(AttributeTable table,AttributeColumn column){
        Map<Object,Integer> valuesFrequencies=new HashMap<Object, Integer>();
        Object value;
        for(Attributes attributes:getTableAttributeRows(table)){
            value=attributes.getValue(column.getIndex());
            if(valuesFrequencies.containsKey(value)){
                valuesFrequencies.put(value, new Integer(valuesFrequencies.get(value)+1));
            }else{
                valuesFrequencies.put(value, new Integer(1));
            }
        }

        return valuesFrequencies;
    }

    public void clearNodeData(Node node) {
        GraphElementsController gec = Lookup.getDefault().lookup(GraphElementsController.class);
        if (gec.isNodeInGraph(node)) {
            AttributeRow row = (AttributeRow) node.getNodeData().getAttributes();
            AttributeValue[] values = row.getValues();
            for (int i = 0; i < values.length; i++) {
                //Clear all except id and computed attributes:
                if (isNotComputedOrIDColumn(values[i].getColumn(), true)) {
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
                if (isNotComputedOrIDColumn(values[i].getColumn(), false)) {
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

    /************Private methods : ************/
    /**
     * Used for checking if a table is nodes table or edges table (assumed because data laboratory uses nodes table and edges table only).
     * @return True if the table is nodes table, false otherwise
     */
    private boolean isNodeTable(AttributeTable table) {
        AttributeController ac = Lookup.getDefault().lookup(AttributeController.class);
        return table == ac.getModel().getNodeTable();
    }

    /**
     * Used for iterating through all nodes of the graph
     * @return Array with all graph nodes
     */
    private Node[] getNodesArray() {
        Graph graph = Lookup.getDefault().lookup(GraphController.class).getModel().getGraph();
        return graph.getNodes().toArray();
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
     * Used for iterating through all attribute rows of a table
     * @param table Table to get attribute rows
     * @return Array of attribute rows of the table
     */
    private Attributes[] getTableAttributeRows(AttributeTable table) {
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

    /**
     * Used for checking if a column is not a computed column or id column. Must indicate if the column is from nodes table or not.
     * (Then assumes it is edges table column because data laboratory uses nodes table and edges table only)
     * @return True if the column is not a computed column or id column, false otherwise
     */
    private boolean isNotComputedOrIDColumn(AttributeColumn column, boolean nodeTable) {
        if (nodeTable) {
            return column.getIndex() != PropertiesColumn.NODE_ID.getIndex() && column.getOrigin() != AttributeOrigin.COMPUTED;
        } else {
            return column.getIndex() != PropertiesColumn.EDGE_ID.getIndex() && column.getOrigin() != AttributeOrigin.COMPUTED;
        }
    }
}
