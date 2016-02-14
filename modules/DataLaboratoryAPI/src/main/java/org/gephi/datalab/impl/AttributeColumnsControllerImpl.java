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
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.gephi.datalab.api.AttributeColumnsController;
import org.gephi.datalab.api.GraphElementsController;
import org.gephi.datalab.spi.rows.merge.AttributeRowsMergeStrategy;
import org.gephi.graph.api.AttributeUtils;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Element;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Interval;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.Origin;
import org.gephi.graph.api.Table;
import org.gephi.graph.api.TimeFormat;
import org.gephi.graph.api.TimeRepresentation;
import org.gephi.graph.api.types.IntervalMap;
import org.gephi.graph.api.types.TimestampMap;
import org.gephi.utils.StatisticsUtils;
import org.joda.time.DateTimeZone;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 * Implementation of the AttributeColumnsController interface declared in the Data Laboratory API.
 *
 * @author Eduardo Ramos
 * @see AttributeColumnsController
 */
@ServiceProvider(service = AttributeColumnsController.class)
public class AttributeColumnsControllerImpl implements AttributeColumnsController {

    @Override
    public boolean setAttributeValue(Object value, Element row, Column column) {
        if (!canChangeColumnData(column)) {
            return false;
        }

        Class targetType = column.getTypeClass();
        if (value != null && !value.getClass().equals(targetType)) {
            try {
                GraphModel graphModel = column.getTable().getGraph().getModel();
                
                String stringValue = AttributeUtils.print(value, graphModel.getTimeFormat(), graphModel.getTimeZone());
                value = AttributeUtils.parse(stringValue, targetType);//Try to convert to target type from string representation
            } catch (Exception ex) {
                return false;//Could not parse
            }
        }

        if (value == null && !canClearColumnData(column)) {
            return false;//Do not set a null value when the column can't have a null value
        } else {
            try {
                if (value == null) {
                    row.removeAttribute(column);
                } else {
                    row.setAttribute(column, value);
                }

                return true;
            } catch (Exception e) {
                Logger.getLogger("").log(Level.SEVERE, null, e);
                return false;
            }
        }
    }

    @Override
    public Column addAttributeColumn(Table table, String title, Class type) {
        if (title == null || title.isEmpty()) {
            return null;
        }
        if (table.hasColumn(title)) {
            return null;
        }
        return table.addColumn(title, type, Origin.DATA);
    }

    @Override
    public void deleteAttributeColumn(Table table, Column column) {
        if (canDeleteColumn(column)) {
            table.removeColumn(column);
        }
    }

    @Override
    public Column convertAttributeColumnToDynamic(Table table, Column column, double low, double high) {
        return convertColumnToDynamic(table, column, low, high, null);
    }

    @Override
    public Column convertAttributeColumnToNewDynamicColumn(Table table, Column column, double low, double high, String newColumnTitle) {
        return convertColumnToDynamic(table, column, low, high, newColumnTitle);
    }

    private Column convertColumnToDynamic(Table table, Column column, double low, double high, String newColumnTitle) {
        Class oldType = column.getTypeClass();

        TimeRepresentation timeRepresentation = Lookup.getDefault().lookup(GraphController.class).getGraphModel().getConfiguration().getTimeRepresentation();
        Class<?> newType;
        if (timeRepresentation == TimeRepresentation.TIMESTAMP) {
            newType = AttributeUtils.getTimestampMapType(oldType);
        } else {
            newType = AttributeUtils.getIntervalMapType(oldType);
        }

        if (newColumnTitle != null) {
            if (newColumnTitle.equals(column.getTitle())) {
                throw new IllegalArgumentException("Column titles can't be equal");
            }
        }

        Element rows[] = getTableAttributeRows(table);

        Object[] oldValues = new Object[rows.length];
        for (int i = 0; i < rows.length; i++) {
            oldValues[i] = rows[i].getAttribute(column);
        }

        Column newColumn;
        if (newColumnTitle == null) {
            table.removeColumn(column);
            newColumn = table.addColumn(column.getTitle(), newType, column.getOrigin());
        } else {
            newColumn = table.addColumn(newColumnTitle, newType, column.getOrigin());
        }

        if (timeRepresentation == TimeRepresentation.TIMESTAMP) {
            for (int i = 0; i < rows.length; i++) {
                if (oldValues[i] != null) {
                    rows[i].setAttribute(newColumn, oldValues[i], low);
                }
            }
        } else {
            Interval interval = new Interval(low, high);
            for (int i = 0; i < rows.length; i++) {
                if (oldValues[i] != null) {
                    rows[i].setAttribute(newColumn, oldValues[i], interval);
                }
            }
        }

        return newColumn;
    }

    @Override
    public Column duplicateColumn(Table table, Column column, String title, Class type) {
        Column newColumn = addAttributeColumn(table, title, type);
        if (newColumn == null) {
            return null;
        }
        copyColumnDataToOtherColumn(table, column, newColumn);
        return newColumn;
    }

    @Override
    public void copyColumnDataToOtherColumn(Table table, Column sourceColumn, Column targetColumn) {
        if (sourceColumn == targetColumn) {
            throw new IllegalArgumentException("Source and target columns can't be equal");
        }

        Class targetType = targetColumn.getTypeClass();
        Object value;
        if (!targetType.equals(sourceColumn.getTypeClass())) {
            for (Element row : getTableAttributeRows(table)) {
                value = row.getAttribute(sourceColumn);
                setAttributeValue(value, row, targetColumn);
            }
        } else {
            for (Element row : getTableAttributeRows(table)) {
                value = row.getAttribute(sourceColumn);
                if (value == null) {
                    row.removeAttribute(targetColumn);
                } else {
                    row.setAttribute(targetColumn, value);
                }
            }
        }
    }

    @Override
    public void fillColumnWithValue(Table table, Column column, String value) {
        if (canChangeColumnData(column)) {
            for (Element row : getTableAttributeRows(table)) {
                setAttributeValue(value, row, column);
            }
        }
    }

    @Override
    public void fillNodesColumnWithValue(Node[] nodes, Column column, String value) {
        if (canChangeColumnData(column)) {
            for (Node node : nodes) {
                setAttributeValue(value, node, column);
            }
        }
    }

    @Override
    public void fillEdgesColumnWithValue(Edge[] edges, Column column, String value) {
        if (canChangeColumnData(column)) {
            for (Edge edge : edges) {
                setAttributeValue(value, edge, column);
            }
        }
    }

    @Override
    public void clearColumnData(Table table, Column column) {
        if (canClearColumnData(column)) {
            for (Element row : getTableAttributeRows(table)) {
                row.removeAttribute(column);
            }
        }
    }

    @Override
    public Map<Object, Integer> calculateColumnValuesFrequencies(Table table, Column column) {
        Map<Object, Integer> valuesFrequencies = new HashMap<>();
        Object value;
        for (Element row : getTableAttributeRows(table)) {
            value = row.getAttribute(column);
            if (valuesFrequencies.containsKey(value)) {
                valuesFrequencies.put(value, valuesFrequencies.get(value) + 1);
            } else {
                valuesFrequencies.put(value, 1);
            }
        }

        return valuesFrequencies;
    }

    @Override
    public Column createBooleanMatchesColumn(Table table, Column column, String newColumnTitle, Pattern pattern) {
        if (pattern != null) {
            Column newColumn = addAttributeColumn(table, newColumnTitle, Boolean.class);
            if (newColumn == null) {
                return null;
            }
            Matcher matcher;
            Object value;

            TimeFormat timeFormat = table.getGraph().getModel().getTimeFormat();
            DateTimeZone timeZone = table.getGraph().getModel().getTimeZone();
            for (Element row : getTableAttributeRows(table)) {
                value = row.getAttribute(column);
                if (value != null) {
                    matcher = pattern.matcher(AttributeUtils.print(value, timeFormat, timeZone));
                } else {
                    matcher = pattern.matcher("");
                }
                row.setAttribute(newColumn, matcher.matches());
            }
            return newColumn;
        } else {
            return null;
        }
    }

    @Override
    public void negateBooleanColumn(Table table, Column column) {
        if (column.getTypeClass().equals(Boolean.class)) {
            negateColumnBooleanType(table, column);
        } else if (column.getTypeClass().equals(Boolean[].class)) {
            negateColumnListBooleanType(table, column);
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public Column createFoundGroupsListColumn(Table table, Column column, String newColumnTitle, Pattern pattern) {
        if (pattern != null) {
            Column newColumn = addAttributeColumn(table, newColumnTitle, String[].class);
            if (newColumn == null) {
                return null;
            }
            Matcher matcher;
            Object value;
            ArrayList<String> foundGroups = new ArrayList<>();

            TimeFormat timeFormat = table.getGraph().getModel().getTimeFormat();
            DateTimeZone timeZone = table.getGraph().getModel().getTimeZone();

            for (Element row : getTableAttributeRows(table)) {
                value = row.getAttribute(column);
                if (value != null) {
                    matcher = pattern.matcher(AttributeUtils.print(value, timeFormat, timeZone));
                } else {
                    matcher = pattern.matcher("");
                }
                while (matcher.find()) {
                    foundGroups.add(matcher.group());
                }
                if (foundGroups.size() > 0) {
                    row.setAttribute(newColumn, foundGroups.toArray(new String[0]));
                    foundGroups.clear();
                } else {
                    row.setAttribute(newColumn, null);
                }
            }
            return newColumn;
        } else {
            return null;
        }
    }

    @Override
    public void clearNodeData(Node node, Column[] columnsToClear) {
        clearRowData(node, columnsToClear);
    }

    @Override
    public void clearNodesData(Node[] nodes, Column[] columnsToClear) {
        for (Node n : nodes) {
            clearNodeData(n, columnsToClear);
        }
    }

    @Override
    public void clearEdgeData(Edge edge, Column[] columnsToClear) {
        clearRowData(edge, columnsToClear);
    }

    @Override
    public void clearEdgesData(Edge[] edges, Column[] columnsToClear) {
        for (Edge e : edges) {
            clearEdgeData(e, columnsToClear);
        }
    }

    @Override
    public void clearRowData(Element row, Column[] columnsToClear) {
        if (columnsToClear != null) {
            for (Column column : columnsToClear) {
                //Clear all except id and computed attributes:
                if (canClearColumnData(column)) {
                    row.removeAttribute(column);
                }
            }
        } else {
            Table table;
            if (row instanceof Node) {
                table = Lookup.getDefault().lookup(GraphController.class).getGraphModel().getNodeTable();
            } else {
                table = Lookup.getDefault().lookup(GraphController.class).getGraphModel().getNodeTable();
            }

            for (Column column : table) {
                if (canClearColumnData(column)) {
                    row.removeAttribute(column);
                }
            }
        }
    }

    @Override
    public void copyNodeDataToOtherNodes(Node node, Node[] otherNodes, Column[] columnsToCopy) {
        copyRowDataToOtherRows(node, otherNodes, columnsToCopy);
    }

    @Override
    public void copyEdgeDataToOtherEdges(Edge edge, Edge[] otherEdges, Column[] columnsToCopy) {
        copyRowDataToOtherRows(edge, otherEdges, columnsToCopy);
    }

    @Override
    public void copyRowDataToOtherRows(Element row, Element[] otherRows, Column[] columnsToCopy) {
        if (columnsToCopy != null) {
            for (Column column : columnsToCopy) {
                //Copy all except id and computed attributes:
                if (canChangeColumnData(column)) {
                    for (Element otherRow : otherRows) {
                        Object value = row.getAttribute(column);
                        setAttributeValue(value, otherRow, column);
                    }
                }
            }
        } else {
            Table table;
            if (row instanceof Node) {
                table = Lookup.getDefault().lookup(GraphController.class).getGraphModel().getNodeTable();
            } else {
                table = Lookup.getDefault().lookup(GraphController.class).getGraphModel().getNodeTable();
            }

            for (Column column : table) {
                if (canChangeColumnData(column)) {
                    for (Element otherRow : otherRows) {
                        otherRow.removeAttribute(column);
                    }
                }
            }
        }
    }

    @Override
    public Element[] getTableAttributeRows(Table table) {
        if (isNodeTable(table)) {
            return getNodesArray();
        } else {
            return getEdgesArray();
        }
    }

    @Override
    public int getTableRowsCount(Table table) {
        if (isNodeTable(table)) {
            return Lookup.getDefault().lookup(GraphElementsController.class).getNodesCount();
        } else {
            return Lookup.getDefault().lookup(GraphElementsController.class).getEdgesCount();
        }
    }

    @Override
    public boolean isNodeTable(Table table) {
        return Node.class.equals(table.getElementClass());
    }

    @Override
    public boolean isEdgeTable(Table table) {
        return Edge.class.equals(table.getElementClass());
    }

    @Override
    public boolean canDeleteColumn(Column column) {
        return !column.isReadOnly() && column.getOrigin() != Origin.PROPERTY;
    }

    @Override
    public boolean isTableColumn(Table table, Column column) {
        return column.getTable() == table;
    }

    @Override
    public boolean isNodeColumn(Column column) {
        return isNodeTable(column.getTable());
    }

    @Override
    public boolean isEdgeColumn(Column column) {
        return isEdgeTable(column.getTable());
    }

    @Override
    public boolean canChangeColumnData(Column column) {
        return !column.isReadOnly();
    }

    @Override
    public boolean canClearColumnData(Column column) {
        if (isEdgeColumn(column) && column.getId().equalsIgnoreCase("weight")) {
            return false;//Should not remove weight value but grapshtore currently allows it
        }

        return !column.isReadOnly();
    }

    @Override
    public boolean canConvertColumnToDynamic(Column column) {
        if (column.isReadOnly() || AttributeUtils.isDynamicType(column.getTypeClass())) {
            return false;
        }

        try {
            //Make sure the simple type can actually be part of a dynamic type of intervals/timestamps
            //For example array types cannot be converted to dynamic
            AttributeUtils.getIntervalMapType(column.getTypeClass());
            AttributeUtils.getTimestampMapType(column.getTypeClass());
        } catch (Exception e) {
            return false;
        }

        if (isNodeColumn(column) || isEdgeColumn(column)) {
            return !column.getTitle().equalsIgnoreCase("Label");
        } else {
            return true;
        }
    }

    @Override
    public BigDecimal[] getNumberOrNumberListColumnStatistics(Table table, Column column) {
        return StatisticsUtils.getAllStatistics(getColumnNumbers(table, column));
    }

    @Override
    public Number[] getColumnNumbers(Table table, Column column) {
        return getRowsColumnNumbers(getTableAttributeRows(table), column);
    }

    @Override
    public Number[] getRowsColumnNumbers(Element[] rows, Column column) {
        Class type = column.getTypeClass();
        if (!AttributeUtils.isNumberType(type)) {
            throw new IllegalArgumentException("The column has to be a number column");
        }

        boolean isDynamic = AttributeUtils.isDynamicType(type);
        boolean isArray = type.isArray();

        ArrayList<Number> numbers = new ArrayList<>();
        Number number;
        for (Element row : rows) {
            Object value = row.getAttribute(column);
            if (value != null) {
                if (!isDynamic) {
                    if (isArray) {
                        numbers.addAll(getArrayNumbers(value));
                    } else {
                        //Single number column:
                        number = (Number) row.getAttribute(column);
                        if (number != null) {
                            numbers.add(number);
                        }
                    }
                } else {
                    numbers.addAll(getDynamicNumberColumnNumbers(row, column));
                }
            }
        }

        return numbers.toArray(new Number[0]);
    }

    @Override
    public Number[] getRowNumbers(Element row, Column[] columns) {
        ArrayList<Number> numbers = new ArrayList<>();
        Number number;
        for (Column column : columns) {
            Class type = column.getTypeClass();
            if (!AttributeUtils.isNumberType(type)) {
                throw new IllegalArgumentException("The column has to be a number column");
            }
            Object value = row.getAttribute(column);

            if (value != null) {
                if (!AttributeUtils.isDynamicType(type)) {
                    if (type.isArray()) {
                        numbers.addAll(getArrayNumbers(value));
                    } else {
                        //Single number column:
                        number = (Number) value;
                        if (number != null) {
                            numbers.add(number);
                        }
                    }
                } else {
                    numbers.addAll(getDynamicNumberColumnNumbers(row, column));
                }
            }

        }

        return numbers.toArray(new Number[0]);
    }

    @Override
    public void importCSVToNodesTable(Graph graph, File file, Character separator, Charset charset, String[] columnNames, Class[] columnTypes, boolean assignNewNodeIds) {
        if (columnNames == null || columnNames.length == 0) {
            return;
        }

        if (columnTypes == null || columnNames.length != columnTypes.length) {
            throw new IllegalArgumentException("Column names length must be the same as column types length");
        }

        CsvReader reader = null;
        try {
            //Prepare attribute columns for the column names, creating the not already existing columns:
            Table nodesTable = graph.getModel().getNodeTable();
            String idColumn = null;
            HashMap<Column, String> columnHeaders = new HashMap<>();//Necessary because of column name case insensitivity, to map columns to its corresponding csv header.
            for (int i = 0; i < columnNames.length; i++) {
                //Separate first id column found from the list to use as id. If more are found later, the will not be in the list and be ignored.
                if (columnNames[i].equalsIgnoreCase("id")) {
                    if (idColumn == null) {
                        idColumn = columnNames[i];
                    }
                } else if (nodesTable.hasColumn(columnNames[i])) {
                    Column column = nodesTable.getColumn(columnNames[i]);
                    columnHeaders.put(column, columnNames[i]);
                } else {
                    Column column = addAttributeColumn(nodesTable, columnNames[i], columnTypes[i]);
                    if (column != null) {
                        columnHeaders.put(column, columnNames[i]);
                    }
                }
            }

            Set<Column> columnList = columnHeaders.keySet();

            //Create nodes:
            GraphElementsController gec = Lookup.getDefault().lookup(GraphElementsController.class);
            String id;
            Node node;
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
                    node = gec.createNode(null);
                }
                //Assign attributes to the current node:
                for (Column column : columnList) {
                    setAttributeValue(reader.get(columnHeaders.get(column)), node, column);
                }
            }
        } catch (FileNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }

    @Override
    public void importCSVToEdgesTable(Graph graph, File file, Character separator, Charset charset, String[] columnNames, Class[] columnTypes, boolean createNewNodes) {
        if (columnNames == null || columnNames.length == 0) {
            return;
        }

        if (columnTypes == null || columnNames.length != columnTypes.length) {
            throw new IllegalArgumentException("Column names length must be the same as column types length");
        }

        CsvReader reader = null;
        try {
            //Prepare attribute columns for the column names, creating the not already existing columns:
            Table edgesTable = graph.getModel().getEdgeTable();
            Column weightColumn = edgesTable.getColumn("Weight");
            boolean isDynamicWeight = weightColumn.isDynamic();

            String idColumnHeader = null;
            String sourceColumnHeader = null;
            String targetColumnHeader = null;
            String typeColumnHeader = null;
            String weightColumnHeader = null;
            HashMap<Column, String> columnHeaders = new HashMap<>();//Necessary because of column name case insensitivity, to map columns to its corresponding csv header.
            for (int i = 0; i < columnNames.length; i++) {
                //Separate first id column found from the list to use as id. If more are found later, the will not be in the list and be ignored.
                if (columnNames[i].equalsIgnoreCase("id")) {
                    if (idColumnHeader == null) {
                        idColumnHeader = columnNames[i];
                    }
                } else if (columnNames[i].equalsIgnoreCase("source") && sourceColumnHeader == null) {//Separate first source column found from the list to use as source node id
                    sourceColumnHeader = columnNames[i];
                } else if (columnNames[i].equalsIgnoreCase("target") && targetColumnHeader == null) {//Separate first target column found from the list to use as target node id
                    targetColumnHeader = columnNames[i];
                } else if (columnNames[i].equalsIgnoreCase("type") && typeColumnHeader == null) {//Separate first type column found from the list to use as edge type (directed/undirected)
                    typeColumnHeader = columnNames[i];
                } else if (edgesTable.hasColumn(columnNames[i])) {
                    //Any other existing column:
                    Column column = edgesTable.getColumn(columnNames[i]);
                    columnHeaders.put(column, columnNames[i]);
                    if (column.equals(weightColumn)) {
                        weightColumnHeader = columnNames[i];
                    }
                } else {
                    //New column:
                    Column column = addAttributeColumn(edgesTable, columnNames[i], columnTypes[i]);
                    if (column != null) {
                        columnHeaders.put(column, columnNames[i]);
                    }
                }
            }

            Set<Column> columnList = columnHeaders.keySet();

            //Create edges:
            GraphElementsController gec = Lookup.getDefault().lookup(GraphElementsController.class);
            reader = new CsvReader(new FileInputStream(file), separator, charset);
            reader.setTrimWhitespace(false);
            reader.readHeaders();

            int recordNumber = 0;
            while (reader.readRecord()) {
                String id;
                Edge edge = null;
                String sourceId, targetId;
                Node source, target;
                String type;
                boolean directed;

                recordNumber++;
                sourceId = reader.get(sourceColumnHeader);
                targetId = reader.get(targetColumnHeader);

                if (sourceId == null || sourceId.trim().isEmpty() || targetId == null || targetId.trim().isEmpty()) {
                    Logger.getLogger("").log(Level.WARNING, "Ignoring record {0} due to empty source and/or target node ids", recordNumber);
                    continue;//No correct source and target ids were provided, ignore row
                }

                graph.readLock();
                source = graph.getNode(sourceId);
                graph.readUnlock();

                if (source == null) {
                    if (createNewNodes) {//Create new nodes when they don't exist already and option is enabled
                        if (source == null) {
                            source = gec.createNode(null, sourceId, graph);
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
                            target = gec.createNode(null, targetId, graph);
                        }
                    } else {
                        continue;//Ignore this edge row, since no new nodes should be created.
                    }
                }

                if (typeColumnHeader != null) {
                    type = reader.get(typeColumnHeader);
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
                if (idColumnHeader != null) {
                    id = reader.get(idColumnHeader);
                    if (id == null || id.isEmpty()) {
                        edge = gec.createEdge(source, target, directed);//id null or empty, assign one
                    } else {
                        Edge edgeById = graph.getEdge(id);

                        if (edgeById == null) {
                            edge = gec.createEdge(id, source, target, directed);
                        }
                        if (edge == null) {//Edge with that id already in graph
                            edge = gec.createEdge(source, target, directed);
                        }
                    }
                } else {
                    edge = gec.createEdge(source, target, directed);
                }

                if (edge != null) {//Edge could be created because it does not already exist:
                    //Assign all attributes to the new edge:
                    for (Column column : columnList) {
                        setAttributeValue(reader.get(columnHeaders.get(column)), edge, column);
                    }
                } else {
                    edge = graph.getEdge(source, target);
                    if (edge == null) {
                        //Not from source to target but undirected and reverse?
                        edge = graph.getEdge(target, source);
                        if (edge != null && edge.isDirected()) {
                            edge = null;//Cannot use it since it's actually directed
                        }
                    }
                    if (edge != null) {
                        //Increase non dynamic edge weight with specified weight (if specified), else increase by 1:
                        if (!isDynamicWeight) {
                            if (weightColumnHeader != null) {
                                String weight = reader.get(weightColumnHeader);
                                try {
                                    Float weightFloat = Float.parseFloat(weight);
                                    edge.setWeight(edge.getWeight() + weightFloat);
                                } catch (NumberFormatException numberFormatException) {
                                    //Not valid weight, add 1
                                    edge.setWeight(edge.getWeight() + 1);
                                }
                            } else {
                                //Add 1 (weight not specified)
                                edge.setWeight(edge.getWeight() + 1);
                            }
                        }
                    }
                }
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger("").log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger("").log(Level.SEVERE, null, ex);
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }

    @Override
    public void mergeRowsValues(Column[] columns, AttributeRowsMergeStrategy[] mergeStrategies, Element[] rows, Element selectedRow, Element resultRow) {
        if (columns.length != mergeStrategies.length) {
            throw new IllegalArgumentException("The number of columns must be equal to the number of merge strategies provided");
        }
        if (selectedRow == null) {
            selectedRow = rows[0];
        }

        AttributeRowsMergeStrategy mergeStrategy;
        Object value;

        int i = 0;
        for (Column column : columns) {
            mergeStrategy = mergeStrategies[i];
            if (mergeStrategy != null) {
                mergeStrategy.setup(rows, selectedRow, column);
                if (mergeStrategy.canExecute()) {
                    mergeStrategy.execute();
                    value = mergeStrategy.getReducedValue();
                } else {
                    value = selectedRow.getAttribute(column);
                }
            } else {
                value = selectedRow.getAttribute(column);
            }
            setAttributeValue(value, resultRow, column);

            i++;
        }
    }

    @Override
    public List<List<Node>> detectNodeDuplicatesByColumn(Column column, boolean caseSensitive) {
        final HashMap<String, List<Node>> valuesMap = new HashMap<>();

        Graph graph = Lookup.getDefault().lookup(GraphController.class).getGraphModel().getGraph();
        Object value;
        String strValue;
        
        TimeFormat timeFormat = graph.getModel().getTimeFormat();
        DateTimeZone timeZone = graph.getModel().getTimeZone();
        
        for (Node node : graph.getNodes().toArray()) {
            value = node.getAttribute(column);
            if (value != null) {
                strValue = AttributeUtils.print(value, timeFormat, timeZone);
                if (!caseSensitive) {
                    strValue = strValue.toLowerCase();
                }
                if (valuesMap.containsKey(strValue)) {
                    valuesMap.get(strValue).add(node);
                } else {
                    ArrayList<Node> newGroup = new ArrayList<>();
                    newGroup.add(node);
                    valuesMap.put(strValue, newGroup);
                }
            }
        }

        final List<List<Node>> groupsList = new ArrayList<>();
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
        return Lookup.getDefault().lookup(GraphController.class).getGraphModel().getGraph().getNodes().toArray();
    }

    /**
     * Used for iterating through all edges of the graph
     *
     * @return Array with all graph edges
     */
    private Edge[] getEdgesArray() {
        return Lookup.getDefault().lookup(GraphController.class).getGraphModel().getGraph().getEdges().toArray();
    }

    /**
     * Used to negate the values of a single boolean column.
     */
    private void negateColumnBooleanType(Table table, Column column) {
        Object value;
        Boolean newValue;
        for (Element row : getTableAttributeRows(table)) {
            value = row.getAttribute(column);
            if (value != null) {
                newValue = !((Boolean) value);
                row.setAttribute(column, newValue);
            }
        }
    }

    /**
     * Used to negate all values of a list of boolean values column.
     */
    private void negateColumnListBooleanType(Table table, Column column) {
        Object value;
        Boolean[] newValues;
        for (Element row : getTableAttributeRows(table)) {
            value = row.getAttribute(column);
            if (value != null) {
                Boolean[] list = (Boolean[]) value;
                newValues = new Boolean[list.length];
                for (int i = 0; i < list.length; i++) {
                    newValues[i] = !list[i];
                }
                row.setAttribute(column, newValues);
            }
        }
    }

    /**
     * Used for obtaining a list of the numbers of row of a dynamic number column.
     *
     * @param row Row
     * @param column Column with dynamic type
     * @return list of numbers
     */
    private List<Number> getDynamicNumberColumnNumbers(Element row, Column column) {
        Class type = column.getTypeClass();
        if (!(AttributeUtils.isNumberType(type) && AttributeUtils.isDynamicType(type))) {
            throw new IllegalArgumentException("Column must be a dynamic number column");
        }

        if (TimestampMap.class.isAssignableFrom(type)) {//Timestamp type:
            TimestampMap timestampMap = (TimestampMap) row.getAttribute(column);
            if (timestampMap == null) {
                return new ArrayList<>();
            }
            Number[] dynamicNumbers = (Number[]) timestampMap.toValuesArray();
            return Arrays.asList(dynamicNumbers);
        } else if (IntervalMap.class.isAssignableFrom(type)) {//Interval type:
            IntervalMap intervalMap = (IntervalMap) row.getAttribute(column);
            if (intervalMap == null) {
                return new ArrayList<>();
            }
            Number[] dynamicNumbers = (Number[]) intervalMap.toValuesArray();
            return Arrays.asList(dynamicNumbers);
        } else {
            throw new IllegalArgumentException("Unsupported dynamic type class " + type.getCanonicalName());
        }
    }

    /**
     * Works for arrays of primitive and non primitive numbers.
     *
     * @param arr Array of Number assignable type
     * @return numbers
     */
    private List<Number> getArrayNumbers(Object arr) {
        int length = Array.getLength(arr);
        List<Number> result = new ArrayList<>();

        for (int i = 0; i < length; i++) {
            result.add((Number) Array.get(arr, i));
        }
        return result;
    }
}
