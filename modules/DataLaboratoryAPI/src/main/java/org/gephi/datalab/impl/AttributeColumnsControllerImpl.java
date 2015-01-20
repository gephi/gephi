/*
 Copyright 2008-2010 Gephi
 Authors : Eduardo Ramos <eduramiba@gmail.com>
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
package org.gephi.datalab.impl;

import com.csvreader.CsvReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
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
import org.gephi.data.attributes.type.DynamicType;
import org.gephi.data.attributes.type.Interval;
import org.gephi.data.attributes.type.NumberList;
import org.gephi.data.attributes.type.StringList;
import org.gephi.data.attributes.type.TypeConvertor;
import org.gephi.data.properties.PropertiesColumn;
import org.gephi.datalab.api.AttributeColumnsController;
import org.gephi.datalab.api.GraphElementsController;
import org.gephi.datalab.spi.rows.merge.AttributeRowsMergeStrategy;
import org.gephi.dynamic.api.DynamicModel;
import org.gephi.graph.api.Attributes;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.Node;
import org.gephi.utils.StatisticsUtils;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 * Implementation of the AttributeColumnsController interface declared in the Data Laboratory API.
 *
 * @author Eduardo Ramos <eduramiba@gmail.com>
 * @see AttributeColumnsController
 */
@ServiceProvider(service = AttributeColumnsController.class)
public class AttributeColumnsControllerImpl implements AttributeColumnsController {

    public boolean setAttributeValue(Object value, Attributes row, AttributeColumn column) {
        AttributeType targetType = column.getType();
        if (value != null && !value.getClass().equals(targetType.getType())) {
            try {
                value = targetType.parse(value.toString());//Try to convert to target type
            } catch (Exception ex) {
                value = null;//Could not parse
            }
        }

        if (value == null && !canClearColumnData(column)) {
            return false;//Do not set a null value when the column can't have a null value.
        } else {
            row.setValue(column.getIndex(), value);
            return true;
        }
    }

    public AttributeColumn addAttributeColumn(AttributeTable table, String title, AttributeType type) {
        if (title == null || title.isEmpty()) {
            return null;
        }
        if (table.hasColumn(title)) {
            return null;
        }
        if (type == AttributeType.TIME_INTERVAL && table.getColumn(DynamicModel.TIMEINTERVAL_COLUMN) == null) {
            return table.addColumn(DynamicModel.TIMEINTERVAL_COLUMN, title, type, AttributeOrigin.PROPERTY, null);
        }
        return table.addColumn(title, title, type, AttributeOrigin.DATA, null);
    }

    public void deleteAttributeColumn(AttributeTable table, AttributeColumn column) {
        if (canDeleteColumn(column)) {
            table.removeColumn(column);
        }
    }

    @Override
    public AttributeColumn convertAttributeColumnToDynamic(AttributeTable table, AttributeColumn column, double low, double high, boolean lopen, boolean ropen) {
        return convertColumnToDynamic(table, column, low, high, lopen, ropen, null);
    }

    @Override
    public AttributeColumn convertAttributeColumnToNewDynamicColumn(AttributeTable table, AttributeColumn column, double low, double high, boolean lopen, boolean ropen, String newColumnTitle) {
        return convertColumnToDynamic(table, column, low, high, lopen, ropen, newColumnTitle);
    }

    private AttributeColumn convertColumnToDynamic(AttributeTable table, AttributeColumn column, double low, double high, boolean lopen, boolean ropen, String newColumnTitle) {
        AttributeType oldType = column.getType();
        AttributeType newType = TypeConvertor.getDynamicType(oldType);

        if (newColumnTitle != null) {
            if (newColumnTitle.equals(column.getTitle())) {
                throw new IllegalArgumentException("Column titles can't be equal");
            }
        }

        int oldColumnIndex = column.getIndex();

        Attributes rows[] = getTableAttributeRows(table);

        Object[] oldValues = new Object[rows.length];
        for (int i = 0; i < rows.length; i++) {
            oldValues[i] = rows[i].getValue(oldColumnIndex);
        }

        AttributeColumn newColumn;
        if (newColumnTitle == null) {
            newColumn = table.replaceColumn(column, column.getId(), column.getTitle(), newType, column.getOrigin(), null);
        } else {
            newColumn = table.addColumn(newColumnTitle, newColumnTitle, newType, column.getOrigin(), null);
        }
        int newColumnIndex = newColumn.getIndex();
        
        Object value;
        for (int i = 0; i < rows.length; i++) {
            if (oldValues[i] != null) {
                Interval interval = new Interval(low, high, lopen, ropen, oldValues[i]);
                value = newType.createDynamicObject(Arrays.asList(new Interval[]{interval}));
            } else {
                value = null;
            }
            
            rows[i].setValue(newColumnIndex, value);
        }

        return newColumn;
    }

    public AttributeColumn duplicateColumn(AttributeTable table, AttributeColumn column, String title, AttributeType type) {
        AttributeColumn newColumn = addAttributeColumn(table, title, type);
        if (newColumn == null) {
            return null;
        }
        copyColumnDataToOtherColumn(table, column, newColumn);
        return newColumn;
    }

    public void copyColumnDataToOtherColumn(AttributeTable table, AttributeColumn sourceColumn, AttributeColumn targetColumn) {
        if (sourceColumn == targetColumn) {
            throw new IllegalArgumentException("Source and target columns can't be equal");
        }

        final int sourceColumnIndex = sourceColumn.getIndex();
        final int targetColumnIndex = targetColumn.getIndex();
        AttributeType targetType = targetColumn.getType();
        if (targetType != sourceColumn.getType()) {
            Object value;
            for (Attributes row : getTableAttributeRows(table)) {
                value = row.getValue(sourceColumnIndex);
                setAttributeValue(value, row, targetColumn);
            }
        } else {
            for (Attributes row : getTableAttributeRows(table)) {
                row.setValue(targetColumnIndex, row.getValue(sourceColumnIndex));
            }
        }
    }

    public void fillColumnWithValue(AttributeTable table, AttributeColumn column, String value) {
        if (canChangeColumnData(column)) {
            for (Attributes row : getTableAttributeRows(table)) {
                setAttributeValue(value, row, column);
            }
        }
    }

    public void fillNodesColumnWithValue(Node[] nodes, AttributeColumn column, String value) {
        if (canChangeColumnData(column)) {
            for (Node node : nodes) {
                setAttributeValue(value, node.getNodeData().getAttributes(), column);
            }
        }
    }

    public void fillEdgesColumnWithValue(Edge[] edges, AttributeColumn column, String value) {
        if (canChangeColumnData(column)) {
            for (Edge edge : edges) {
                setAttributeValue(value, edge.getEdgeData().getAttributes(), column);
            }
        }
    }

    public void clearColumnData(AttributeTable table, AttributeColumn column) {
        if (canClearColumnData(column)) {
            final int columnIndex = column.getIndex();
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

    public AttributeColumn createBooleanMatchesColumn(AttributeTable table, AttributeColumn column, String newColumnTitle, Pattern pattern) {
        if (pattern != null) {
            AttributeColumn newColumn = addAttributeColumn(table, newColumnTitle, AttributeType.BOOLEAN);
            if (newColumn == null) {
                return null;
            }
            Matcher matcher;
            Object value;
            for (Attributes row : getTableAttributeRows(table)) {
                value = row.getValue(column.getIndex());
                if (value != null) {
                    matcher = pattern.matcher(value.toString());
                } else {
                    matcher = pattern.matcher("");
                }
                row.setValue(newColumn.getIndex(), matcher.matches());
            }
            return newColumn;
        } else {
            return null;
        }
    }

    public void negateBooleanColumn(AttributeTable table, AttributeColumn column) {
        AttributeUtils attributeUtils = AttributeUtils.getDefault();
        if (attributeUtils.isColumnOfType(column, AttributeType.BOOLEAN)) {
            negateColumnBooleanType(table, column);
        } else if (attributeUtils.isColumnOfType(column, AttributeType.LIST_BOOLEAN)) {
            negateColumnListBooleanType(table, column);
        } else {
            throw new IllegalArgumentException();
        }
    }

    public AttributeColumn createFoundGroupsListColumn(AttributeTable table, AttributeColumn column, String newColumnTitle, Pattern pattern) {
        if (pattern != null) {
            AttributeColumn newColumn = addAttributeColumn(table, newColumnTitle, AttributeType.LIST_STRING);
            if (newColumn == null) {
                return null;
            }
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
            return newColumn;
        } else {
            return null;
        }
    }

    public void clearNodeData(Node node, AttributeColumn[] columnsToClear) {
        clearRowData((AttributeRow) node.getNodeData().getAttributes(), columnsToClear);
    }

    public void clearNodesData(Node[] nodes, AttributeColumn[] columnsToClear) {
        for (Node n : nodes) {
            clearNodeData(n, columnsToClear);
        }
    }

    public void clearEdgeData(Edge edge, AttributeColumn[] columnsToClear) {
        clearRowData((AttributeRow) edge.getEdgeData().getAttributes(), columnsToClear);
    }

    public void clearEdgesData(Edge[] edges, AttributeColumn[] columnsToClear) {
        for (Edge e : edges) {
            clearEdgeData(e, columnsToClear);
        }
    }

    public void clearRowData(Attributes row, AttributeColumn[] columnsToClear) {
        AttributeRow attributeRow = (AttributeRow) row;
        if (columnsToClear != null) {
            for (AttributeColumn column : columnsToClear) {
                //Clear all except id and computed attributes:
                if (canClearColumnData(column)) {
                    row.setValue(column.getIndex(), null);
                }
            }
        } else {
            AttributeValue[] values = attributeRow.getValues();
            for (int i = 0; i < values.length; i++) {
                //Clear all except id and computed attributes:
                if (canClearColumnData(values[i].getColumn())) {
                    row.setValue(i, null);
                }
            }
        }
    }

    public void copyNodeDataToOtherNodes(Node node, Node[] otherNodes, AttributeColumn[] columnsToCopy) {
        Attributes row = node.getNodeData().getAttributes();
        Attributes[] otherRows = new Attributes[otherNodes.length];
        for (int i = 0; i < otherNodes.length; i++) {
            otherRows[i] = otherNodes[i].getNodeData().getAttributes();
        }

        copyRowDataToOtherRows(row, otherRows, columnsToCopy);
    }

    public void copyEdgeDataToOtherEdges(Edge edge, Edge[] otherEdges, AttributeColumn[] columnsToCopy) {
        Attributes row = edge.getEdgeData().getAttributes();
        Attributes[] otherRows = new Attributes[otherEdges.length];
        for (int i = 0; i < otherEdges.length; i++) {
            otherRows[i] = otherEdges[i].getEdgeData().getAttributes();
        }

        copyRowDataToOtherRows(row, otherRows, columnsToCopy);
    }

    public void copyRowDataToOtherRows(Attributes row, Attributes[] otherRows, AttributeColumn[] columnsToCopy) {
        AttributeRow attributeRow = (AttributeRow) row;
        if (columnsToCopy != null) {
            for (AttributeColumn column : columnsToCopy) {
                //Copy all except id and computed attributes:
                if (canChangeColumnData(column)) {
                    for (Attributes otherRow : otherRows) {
                        otherRow.setValue(column.getIndex(), row.getValue(column.getIndex()));
                    }
                }
            }
        } else {
            AttributeColumn column;
            AttributeValue[] values = attributeRow.getValues();
            for (int i = 0; i < values.length; i++) {
                column = values[i].getColumn();
                //Copy all except id and computed attributes:
                if (canChangeColumnData(column)) {
                    for (Attributes otherRow : otherRows) {
                        otherRow.setValue(column.getIndex(), row.getValue(column.getIndex()));
                    }
                }
            }
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
            return canChangeGenericColumnData(column) && column.getIndex() != PropertiesColumn.NODE_ID.getIndex();
        } else if (au.isEdgeColumn(column)) {
            return canChangeGenericColumnData(column) && column.getIndex() != PropertiesColumn.EDGE_ID.getIndex();
        } else {
            return canChangeGenericColumnData(column);
        }
    }

    public boolean canClearColumnData(AttributeColumn column) {
        AttributeUtils au = Lookup.getDefault().lookup(AttributeUtils.class);
        if (au.isNodeColumn(column)) {
            return canChangeGenericColumnData(column) && column.getIndex() != PropertiesColumn.NODE_ID.getIndex();
        } else if (au.isEdgeColumn(column)) {
            return canChangeGenericColumnData(column) && column.getIndex() != PropertiesColumn.EDGE_ID.getIndex() && column.getIndex() != PropertiesColumn.EDGE_WEIGHT.getIndex();
        } else {
            return canChangeGenericColumnData(column);
        }
    }

    public boolean canConvertColumnToDynamic(AttributeColumn column) {
        if(column.getType().isDynamicType()){
            return false;
        }
        
        AttributeUtils au = Lookup.getDefault().lookup(AttributeUtils.class);
        if (au.isNodeColumn(column)) {
            return canChangeGenericColumnData(column) && column.getIndex() != PropertiesColumn.NODE_ID.getIndex() && column.getIndex() != PropertiesColumn.NODE_LABEL.getIndex();
        } else if (au.isEdgeColumn(column)) {
            return canChangeGenericColumnData(column) && column.getIndex() != PropertiesColumn.EDGE_ID.getIndex() && column.getIndex() != PropertiesColumn.EDGE_LABEL.getIndex();
        } else {
            return true;
        }
    }

    public BigDecimal[] getNumberOrNumberListColumnStatistics(AttributeTable table, AttributeColumn column) {
        return StatisticsUtils.getAllStatistics(getColumnNumbers(table, column));
    }

    public Number[] getColumnNumbers(AttributeTable table, AttributeColumn column) {
        return getRowsColumnNumbers(getTableAttributeRows(table), column);
    }

    public Number[] getRowsColumnNumbers(Attributes[] rows, AttributeColumn column) {
        AttributeUtils attributeUtils = AttributeUtils.getDefault();
        if (!attributeUtils.isNumberOrNumberListColumn(column)) {
            throw new IllegalArgumentException("The column has to be a number or number list column");
        }

        ArrayList<Number> numbers = new ArrayList<Number>();
        final int columnIndex = column.getIndex();
        Number number;
        if (attributeUtils.isNumberColumn(column)) {//Number column
            for (Attributes row : rows) {
                number = (Number) row.getValue(columnIndex);
                if (number != null) {
                    numbers.add(number);
                }
            }
        } else {//Number list column
            for (Attributes row : rows) {
                numbers.addAll(getNumberListColumnNumbers(row, column));
            }
        }

        return numbers.toArray(new Number[0]);
    }

    public Number[] getRowNumbers(Attributes row, AttributeColumn[] columns) {
        AttributeUtils attributeUtils = AttributeUtils.getDefault();
        checkColumnsAreNumberOrNumberList(columns);

        ArrayList<Number> numbers = new ArrayList<Number>();
        Number number;
        for (AttributeColumn column : columns) {
            if (attributeUtils.isNumberColumn(column)) {//Single number column:
                number = (Number) row.getValue(column.getIndex());
                if (number != null) {
                    numbers.add(number);
                }
            } else if (attributeUtils.isNumberListColumn(column)) {//Number list column:
                numbers.addAll(getNumberListColumnNumbers(row, column));
            } else if (attributeUtils.isDynamicNumberColumn(column)) {//Dynamic number column
                numbers.addAll(getDynamicNumberColumnNumbers(row, column));
            }
        }

        return numbers.toArray(new Number[0]);
    }

    public void importCSVToNodesTable(File file, Character separator, Charset charset, String[] columnNames, AttributeType[] columnTypes, boolean assignNewNodeIds) {
        if (columnNames == null || columnNames.length == 0) {
            return;
        }

        if (columnTypes == null || columnNames.length != columnTypes.length) {
            throw new IllegalArgumentException("Column names length must be the same as column types lenght");
        }

        CsvReader reader = null;
        try {
            //Prepare attribute columns for the column names, creating the not already existing columns:
            AttributeTable nodesTable = Lookup.getDefault().lookup(AttributeController.class).getModel().getNodeTable();
            String idColumn = null;
            ArrayList<AttributeColumn> columnsList = new ArrayList<AttributeColumn>();
            HashMap<AttributeColumn, String> columnHeaders = new HashMap<AttributeColumn, String>();//Necessary because of column name case insensitivity, to map columns to its corresponding csv header.
            for (int i = 0; i < columnNames.length; i++) {
                //Separate first id column found from the list to use as id. If more are found later, the will not be in the list and be ignored.
                if (columnNames[i].equalsIgnoreCase("id")) {
                    if (idColumn == null) {
                        idColumn = columnNames[i];
                    }
                } else if (nodesTable.hasColumn(columnNames[i])) {
                    AttributeColumn column = nodesTable.getColumn(columnNames[i]);
                    columnsList.add(column);
                    columnHeaders.put(column, columnNames[i]);
                } else {
                    AttributeColumn column = addAttributeColumn(nodesTable, columnNames[i], columnTypes[i]);
                    if (column != null) {
                        columnsList.add(column);
                        columnHeaders.put(column, columnNames[i]);
                    }
                }
            }

            //Create nodes:
            GraphElementsController gec = Lookup.getDefault().lookup(GraphElementsController.class);
            Graph graph = Lookup.getDefault().lookup(GraphController.class).getModel().getGraph();
            String id = null;
            Node node;
            Attributes nodeAttributes;
            reader = new CsvReader(new FileInputStream(file), separator, charset);
            reader.setTrimWhitespace(false);
            reader.readHeaders();
            while (reader.readRecord()) {
                //Prepare the correct node to assign the attributes:
                if (idColumn != null) {
                    id = reader.get(idColumn);
                    if (id == null || id.isEmpty()) {
                        node = gec.createNode(null, graph);//id null or empty, assign one
                    } else {
                        graph.readLock();
                        node = graph.getNode(id);
                        graph.readUnlock();
                        if (node != null) {//Node with that id already in graph
                            if (assignNewNodeIds) {
                                node = gec.createNode(null, graph);
                            }
                        } else {
                            node = gec.createNode(null, id, graph);//New id in the graph
                        }
                    }
                } else {
                    node = gec.createNode(null, graph);
                }
                //Assign attributes to the current node:
                nodeAttributes = node.getNodeData().getAttributes();
                for (AttributeColumn column : columnsList) {
                    setAttributeValue(reader.get(columnHeaders.get(column)), nodeAttributes, column);
                }
            }
        } catch (FileNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } finally {
            reader.close();
        }
    }

    public void importCSVToEdgesTable(File file, Character separator, Charset charset, String[] columnNames, AttributeType[] columnTypes, boolean createNewNodes) {
        if (columnNames == null || columnNames.length == 0) {
            return;
        }

        if (columnTypes == null || columnNames.length != columnTypes.length) {
            throw new IllegalArgumentException("Column names length must be the same as column types lenght");
        }

        CsvReader reader = null;
        try {
            //Prepare attribute columns for the column names, creating the not already existing columns:
            AttributeTable edgesTable = Lookup.getDefault().lookup(AttributeController.class).getModel().getEdgeTable();
            String idColumn = null;
            String sourceColumn = null;
            String targetColumn = null;
            String typeColumn = null;
            ArrayList<AttributeColumn> columnsList = new ArrayList<AttributeColumn>();
            HashMap<AttributeColumn, String> columnHeaders = new HashMap<AttributeColumn, String>();//Necessary because of column name case insensitivity, to map columns to its corresponding csv header.
            for (int i = 0; i < columnNames.length; i++) {
                //Separate first id column found from the list to use as id. If more are found later, the will not be in the list and be ignored.
                if (columnNames[i].equalsIgnoreCase("id")) {
                    if (idColumn == null) {
                        idColumn = columnNames[i];
                    }
                } else if (columnNames[i].equalsIgnoreCase("source") && sourceColumn == null) {//Separate first source column found from the list to use as source node id
                    sourceColumn = columnNames[i];
                } else if (columnNames[i].equalsIgnoreCase("target") && targetColumn == null) {//Separate first target column found from the list to use as target node id
                    targetColumn = columnNames[i];
                } else if (columnNames[i].equalsIgnoreCase("type") && typeColumn == null) {//Separate first type column found from the list to use as edge type (directed/undirected)
                    typeColumn = columnNames[i];
                } else if (edgesTable.hasColumn(columnNames[i])) {
                    AttributeColumn column = edgesTable.getColumn(columnNames[i]);
                    columnsList.add(column);
                    columnHeaders.put(column, columnNames[i]);
                } else {
                    AttributeColumn column = addAttributeColumn(edgesTable, columnNames[i], columnTypes[i]);
                    if (column != null) {
                        columnsList.add(column);
                        columnHeaders.put(column, columnNames[i]);
                    }
                }
            }

            //Create edges:
            GraphElementsController gec = Lookup.getDefault().lookup(GraphElementsController.class);
            Graph graph = Lookup.getDefault().lookup(GraphController.class).getModel().getGraph();
            String id = null;
            Edge edge;
            String sourceId, targetId;
            Node source, target;
            String type;
            boolean directed;
            Attributes edgeAttributes;
            reader = new CsvReader(new FileInputStream(file), separator, charset);
            reader.setTrimWhitespace(false);
            reader.readHeaders();
            while (reader.readRecord()) {
                sourceId = reader.get(sourceColumn);
                targetId = reader.get(targetColumn);

                if (sourceId == null || sourceId.isEmpty() || targetId == null || targetId.isEmpty()) {
                    continue;//No correct source and target ids were provided, ignore row
                }

                graph.readLock();
                source = graph.getNode(sourceId);
                graph.readUnlock();

                if (source == null) {
                    if (createNewNodes) {//Create new nodes when they don't exist already and option is enabled
                        if (source == null) {
                            source = gec.createNode(null, sourceId);
                        }
                    } else {
                        continue;//Ignore this edge row, since no new nodes should be created.
                    }
                }

                graph.readLock();
                target = graph.getNode(targetId);
                graph.readUnlock();

                if (target == null) {
                    if (createNewNodes) {//Create new nodes when they don't exist already and option is enabled
                        if (target == null) {
                            target = gec.createNode(null, targetId);
                        }
                    } else {
                        continue;//Ignore this edge row, since no new nodes should be created.
                    }
                }

                if (typeColumn != null) {
                    type = reader.get(typeColumn);
                    //Undirected if indicated correctly, otherwise always directed:
                    if (type != null) {
                        directed = !type.equalsIgnoreCase("undirected");
                    } else {
                        directed = true;
                    }
                } else {
                    directed = true;//Directed by default when not indicated
                }

                //Prepare the correct edge to assign the attributes:
                if (idColumn != null) {
                    id = reader.get(idColumn);
                    if (id == null || id.isEmpty()) {
                        edge = gec.createEdge(source, target, directed);//id null or empty, assign one
                    } else {
                        edge = gec.createEdge(id, source, target, directed);
                        if (edge == null) {//Edge with that id already in graph
                            edge = gec.createEdge(source, target, directed);
                        }
                    }
                } else {
                    edge = gec.createEdge(source, target, directed);
                }

                if (edge != null) {//Edge could be created because it does not already exist:
                    //Assign attributes to the current edge:
                    edgeAttributes = edge.getEdgeData().getAttributes();
                    for (AttributeColumn column : columnsList) {
                        setAttributeValue(reader.get(columnHeaders.get(column)), edgeAttributes, column);
                    }
                } else {
                    //Do not ignore repeated edge, instead increase edge weight
                    edge = graph.getEdge(source, target);
                    if (edge == null) {
                        //Not from source to target but undirected and reverse?
                        edge = graph.getEdge(target, source);
                        if (edge != null && edge.isDirected()) {
                            edge = null;
                        }
                    }
                    if (edge != null) {
                        //Increase edge weight with specified weight (if specified), else increase by 1:
                        String weight = reader.get(columnHeaders.get(edgesTable.getColumn(PropertiesColumn.EDGE_WEIGHT.getIndex())));
                        if (weight != null) {
                            try {
                                Float weightFloat = Float.parseFloat(weight);
                                edge.getEdgeData().getAttributes().setValue(PropertiesColumn.EDGE_WEIGHT.getIndex(), edge.getWeight() + weightFloat);
                            } catch (NumberFormatException numberFormatException) {
                                //Not valid weight, add 1
                                edge.getEdgeData().getAttributes().setValue(PropertiesColumn.EDGE_WEIGHT.getIndex(), edge.getWeight() + 1);
                            }
                        } else {
                            //Add 1 (weight not specified)
                            edge.getEdgeData().getAttributes().setValue(PropertiesColumn.EDGE_WEIGHT.getIndex(), edge.getWeight() + 1);
                        }
                    }
                }
            }
        } catch (FileNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } finally {
            reader.close();
        }
    }

    public void mergeRowsValues(AttributeTable table, AttributeRowsMergeStrategy[] mergeStrategies, Attributes[] rows, Attributes selectedRow, Attributes resultRow) {
        AttributeColumn[] columns = table.getColumns();
        if (columns.length != mergeStrategies.length) {
            throw new IllegalArgumentException("The number of columns must be equal to the number of merge strategies provided");
        }
        if (selectedRow == null) {
            selectedRow = rows[0];
        }

        AttributeRowsMergeStrategy mergeStrategy;
        Object value;
        for (int i = 0; i < columns.length; i++) {
            mergeStrategy = mergeStrategies[i];
            if (mergeStrategy != null) {
                mergeStrategy.setup(rows, selectedRow, columns[i]);
                if (mergeStrategy.canExecute()) {
                    mergeStrategy.execute();
                    value = mergeStrategy.getReducedValue();
                } else {
                    value = selectedRow.getValue(columns[i].getIndex());
                }
            } else {
                value = selectedRow.getValue(columns[i].getIndex());
            }
            setAttributeValue(value, resultRow, columns[i]);
        }
    }

    public List<List<Node>> detectNodeDuplicatesByColumn(AttributeColumn column, boolean caseSensitive) {
        final HashMap<String, List<Node>> valuesMap = new HashMap<String, List<Node>>();
        final int columnIndex = column.getIndex();

        Graph graph = Lookup.getDefault().lookup(GraphController.class).getModel().getGraph();
        Object value;
        String strValue;
        for (Node node : graph.getNodes().toArray()) {
            value = node.getNodeData().getAttributes().getValue(columnIndex);
            if (value != null) {
                strValue = value.toString();
                if (!caseSensitive) {
                    strValue = strValue.toLowerCase();
                }
                if (valuesMap.containsKey(strValue)) {
                    valuesMap.get(strValue).add(node);
                } else {
                    ArrayList<Node> newGroup = new ArrayList<Node>();
                    newGroup.add(node);
                    valuesMap.put(strValue, newGroup);
                }
            }
        }

        final List<List<Node>> groupsList = new ArrayList<List<Node>>();
        for (List<Node> group : valuesMap.values()) {
            if (group.size() > 1) {
                groupsList.add(group);
            }
        }
        return groupsList;
    }

    /**
     * **********Private methods : ***********
     */
    /**
     * Used for iterating through all nodes of the graph
     *
     * @return Array with all graph nodes
     */
    private Node[] getNodesArray() {
        return Lookup.getDefault().lookup(GraphController.class).getModel().getHierarchicalGraph().getNodesTree().toArray();
    }

    /**
     * Used for iterating through all edges of the graph
     *
     * @return Array with all graph edges
     */
    private Edge[] getEdgesArray() {
        return Lookup.getDefault().lookup(GraphController.class).getModel().getHierarchicalGraph().getEdges().toArray();
    }

    /**
     * Only checks that a column is not
     * <code>COMPUTED</code> or
     * <code>DELEGATE</code>
     */
    private boolean canChangeGenericColumnData(AttributeColumn column) {
        return column.getOrigin() != AttributeOrigin.COMPUTED && column.getOrigin() != AttributeOrigin.DELEGATE;
    }

    /**
     * Used to negate the values of a single boolean column.
     */
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

    /**
     * Used to negate all values of a list of boolean values column.
     */
    private void negateColumnListBooleanType(AttributeTable table, AttributeColumn column) {
        final int columnIndex = column.getIndex();
        Object value;
        BooleanList list;
        Boolean[] newValues;
        for (Attributes row : getTableAttributeRows(table)) {
            value = row.getValue(columnIndex);
            if (value != null) {
                list = (BooleanList) value;
                newValues = new Boolean[list.size()];
                for (int i = 0; i < list.size(); i++) {
                    newValues[i] = !list.getItem(i);
                }
                row.setValue(columnIndex, new BooleanList(newValues));
            }
        }
    }

    /**
     * Used for obtaining a list of the numbers of row of a number list column.
     */
    private ArrayList<Number> getNumberListColumnNumbers(Attributes row, AttributeColumn column) {
        if (!AttributeUtils.getDefault().isNumberListColumn(column)) {
            throw new IllegalArgumentException("Column must be a number list column");
        }

        ArrayList<Number> numbers = new ArrayList<Number>();
        NumberList list = (NumberList) row.getValue(column.getIndex());
        if (list == null) {
            return numbers;
        }
        Number n;
        for (int i = 0; i < list.size(); i++) {
            n = (Number) list.getItem(i);
            if (n != null) {
                numbers.add((Number) n);
            }
        }
        return numbers;
    }

    /**
     * Used for obtaining a list of the numbers of row of a dynamic number column.
     */
    private ArrayList<Number> getDynamicNumberColumnNumbers(Attributes row, AttributeColumn column) {
        if (!AttributeUtils.getDefault().isDynamicNumberColumn(column)) {
            throw new IllegalArgumentException("Column must be a dynamic number column");
        }
        ArrayList<Number> numbers = new ArrayList<Number>();
        DynamicType dynamicList = (DynamicType) row.getValue(column.getIndex());
        if (dynamicList == null) {
            return numbers;
        }
        Number[] dynamicNumbers;
        dynamicNumbers = (Number[]) dynamicList.getValues().toArray(new Number[0]);
        Number n;
        for (int i = 0; i < dynamicNumbers.length; i++) {
            n = (Number) dynamicNumbers[i];
            if (n != null) {
                numbers.add((Number) n);
            }
        }
        return numbers;
    }

    private void checkColumnsAreNumberOrNumberList(AttributeColumn[] columns) {
        if (columns == null || (!AttributeUtils.getDefault().areAllNumberOrNumberListColumns(columns) && !AttributeUtils.getDefault().areAllDynamicNumberColumns(columns))) {
            throw new IllegalArgumentException("All columns have to be number or number list columns and can't be null");
        }
    }
}
