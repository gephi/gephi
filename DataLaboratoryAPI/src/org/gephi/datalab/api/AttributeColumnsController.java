/*
Copyright 2008-2010 Gephi
Authors : Eduardo Ramos <eduramiba@gmail.com>
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
package org.gephi.datalab.api;

import java.io.File;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.regex.Pattern;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeTable;
import org.gephi.data.attributes.api.AttributeType;
import org.gephi.graph.api.Attributes;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Node;

/**
 * <p>This interface defines part of the Data Laboratory API basic actions.</p>
 * <p>It contains methods for manipulating the attributes and properties of nodes and edges.</p>
 * @author Eduardo Ramos <eduramiba@gmail.com>
 */
public interface AttributeColumnsController {

    /**
     * <p>Sets a value to the given row,column pair (cell). If the class of the value is not the column type class,
     * it will try to parse the <code>toString</code> representation of the value.</p>
     * <p>Takes care to avoid parsing exceptions of the target column type.</p>
     * <p>Also, this will not set a null value to a column that can't have null values (see <code>canClearColumnData</code> method) if the given object is null or the parsing fails.</p>
     * @param value Value to set, can be null
     * @param row Row
     * @param column Column
     * @return True if the value was set, false otherwise
     */
    boolean setAttributeValue(Object value, Attributes row, AttributeColumn column);

    /**
     * <p>Adds a new column to the specified table with the given title and type of column.</p>
     * <p>The title for the new column can't be repeated in the table, null or an empty string.</p>.
     * <p>The id of the column will be set to the title.</p>
     * <p>The <code>AttributeOrigin</code> of the column will be set to <code>DATA</code>.</p>
     * <p>Default column value will be set to null.</p>
     * @param table Table to add the column
     * @param title Title for the new column, can't be repeated in the table, null or empty string
     * @param type Type for the new column
     * @return The created column or null if title is not correct
     */
    AttributeColumn addAttributeColumn(AttributeTable table, String title, AttributeType type);

    /**
     * <p>Duplicates a given column of a table and copies al row values.</p>
     * <p>If the <code>AttributeType</code> for the new column is different from the old column type, it will try to parse each value. If it is not possible, the value will be set to null.</p>
     * <p>The title for the new column can't be repeated in the table, null or an empty string.</p>.
     * <p>The id of the column will be set to the title.</p>
     * <p>The <code>AttributeOrigin</code> of the column will be set to <code>DATA</code>.</p>
     * <p>Default column value will be set to null.</p>
     * @param table Table of the column to duplicate
     * @param column Column to duplicate
     * @param title Title for the new column
     * @param type AttributeType for the new column
     * @return The created column or null if title is not correct
     */
    AttributeColumn duplicateColumn(AttributeTable table, AttributeColumn column, String title, AttributeType type);

    /**
     * <p>Copies all row values of a column to another column.</p>
     * <p>If the <code>AttributeType</code> for the target is different from the source column type, it will try to parse each value. If it is not possible, the value will be set to null.</p>
     * <p>Source and target columns must be different.</p>
     * @param table Table of the columns
     * @param sourceColumn Source column
     * @param targetColumn Target column
     */
    void copyColumnDataToOtherColumn(AttributeTable table, AttributeColumn sourceColumn, AttributeColumn targetColumn);

    /**
     * <p>Deletes the specified column from a table if the table has the column and data laboratory behaviour allows to delete it (see <code>canDeleteColumn</code> method).</p>
     * @param table Table to delete the column
     * @param column Column to delete
     */
    void deleteAttributeColumn(AttributeTable table, AttributeColumn column);

    /**
     * <p>Fills the data values of a given column of a table with a value as a String,
     * parsing it for the <code>AttributeType</code> of the column. If it is not possible to parse,
     * the value will be set to null.</p>
     * @param table Table of the column
     * @param column Column to fill
     * @param value String representation of the value for each row of the column
     */
    void fillColumnWithValue(AttributeTable table, AttributeColumn column, String value);

    /**
     * <p>Clears all rows data for a given column of a table (nodes table or edges table)</p>
     * @param table Table to clear column data
     * @param column Column to clear data
     */
    void clearColumnData(AttributeTable table, AttributeColumn column);

    /**
     * <p>Calculates the absolute frequency of appearance of each value of the given column and returns a Map containing each different value mapped to its frequency of appearance.</p>
     * @param table Table of the column
     * @param column Column to calculate values frequencies
     * @return Map containing each different value mapped to its frequency of appearance
     */
    Map<Object, Integer> calculateColumnValuesFrequencies(AttributeTable table, AttributeColumn column);

    /**
     * <p>Creates a new <code>BOOLEAN</code> column from the given column and regular expression
     * filling it with boolean values that indicate if each of the old column values match the regular expression.</p>
     * <p>Title for the new column can't be repeated in the table, null or empty.</p>
     * @param table Table of the column to match
     * @param column Column to match
     * @param newColumnTitle Title for the new boolean column
     * @param pattern Regular expression to match
     * @return New created column or null if title is not correct
     */
    AttributeColumn createBooleanMatchesColumn(AttributeTable table, AttributeColumn column, String newColumnTitle, Pattern pattern);

    /**
     * <p>Negates not null values of a given <code>BOOLEAN</code> or <code>LIST_BOOLEAN</code>column.</p>
     * <p>Throws IllegalArgumentException if the column does not have <code>BOOLEAN</code> or <code>LIST_BOOLEAN</code> <code>AttributeType</code>.</p>
     * @param table Table of the column to negate
     * @param column Boolean column to negate
     */
    void negateBooleanColumn(AttributeTable table, AttributeColumn column);

    /**
     * <p>Creates a new <code>LIST_STRING</code> column from the given column and regular expression with values that are
     * the list of matching groups for the given regular expression for each row.</p>
     * <p>The title for the new column can't be repeated in the table, null or an empty string.</p>.
     * @param table Table of the column to match
     * @param column Column to match
     * @param newColumnTitle Title for the new boolean column
     * @param pattern Regular expression to match
     * @return New created column or null if title is not correct
     */
    AttributeColumn createFoundGroupsListColumn(AttributeTable table, AttributeColumn column, String newColumnTitle, Pattern pattern);

    /**
     * <p>Clears all node attributes except computed attributes and id, checking first that the node is in the graph.</p>
     * <p>Columns to clear can be specified, but id and computed columns will not be cleared.</p>
     * @param node Node to clear data
     * @param columnsToClear Columns of the node to clear. All columns will be cleared if it is null
     */
    void clearNodeData(Node node, AttributeColumn[] columnsToClear);

    /**
     * <p>Clears all the nodes attributes except computed attributes and id.</p>
     * <p>Columns to clear can be specified, but id and computed columns will not be cleared.</p>
     * @param nodes Array of nodes to clear data
     * @param columnsToClear Columns of the nodes to clear. All columns will be cleared if it is null
     */
    void clearNodesData(Node[] nodes, AttributeColumn[] columnsToClear);

    /**
     * <p>Clears all edge attributes except computed attributes and id.</p>
     * <p>Columns to clear can be specified, but id and computed columns will not be cleared.</p>
     * @param edge Edge to clear data
     * @param columnsToClear Columns of the edge to clear. All columns will be cleared if it is null
     */
    void clearEdgeData(Edge edge, AttributeColumn[] columnsToClear);

    /**
     * <p>Clears all the edges attributes except computed attributes and id, checking first that the edges are in the graph.</p>
     * <p>Columns to clear can be specified, but id and computed columns will not be cleared.</p>
     * @param edges Array of edges to clear data
     * @param columnsToClear Columns of the edges to clear. All columns will be cleared if it is null
     */
    void clearEdgesData(Edge[] edges, AttributeColumn[] columnsToClear);

    /**
     * <p>Clears row attributes except computed attributes and id if node/edge row.</p>
     * <p>Columns to clear can be specified, but id of node/edge and computed columns will not be cleared.</p>
     * @param row Array of rows to clear data
     * @param columnsToClear Columns of the row to clear. All columns will be cleared if it is null
     */
    void clearRowData(Attributes row, AttributeColumn[] columnsToClear);

    /**
     * <p>Copies attributes data of the given node to the other rows except computed attributes and id.</p>
     * <p>Columns to copy can be specified, but id node and computed columns will not be copied.</p>
     * @param node Node to copy data from
     * @param otherNodes Nodes to copy data to
     * @param columnsToCopy Columns of the node to copy. All columns will be copied if it is null
     */
    void copyNodeDataToOtherNodes(Node node, Node[] otherNodes, AttributeColumn[] columnsToCopy);

    /**
     * <p>Copies attributes data of the given edge to the other rows except computed attributes and id.</p>
     * <p>Columns to copy can be specified, but id edge and computed columns will not be copied.</p>
     * @param edge Edge to copy data from
     * @param otherEdges Edges to copy data to
     * @param columnsToCopy Columns of the edge to copy. All columns will be copied if it is null
     */
    void copyEdgeDataToOtherEdges(Edge edge, Edge[] otherEdges, AttributeColumn[] columnsToCopy);

    /**
     * <p>Copies attributes data of the given row to the other rows except computed attributes and id if node/edge.</p>
     * <p>Columns to copy can be specified, but id of node/edge and computed columns will not be copied.</p>
     * @param row Row to copy data from
     * @param otherRows Rows to copy data to
     * @param columnsToCopy Columns of the row to copy. All columns will be copied if it is null
     */
    void copyRowDataToOtherRows(Attributes row, Attributes[] otherRows, AttributeColumn[] columnsToCopy);

    /**
     * <p>Returns all rows of a given table (node or edges table).</p>
     * <p>Used for iterating through all attribute rows of a table</p>
     * @param table Table to get attribute rows
     * @return Array of attribute rows of the table
     */
    Attributes[] getTableAttributeRows(AttributeTable table);

    /**
     * <p>Counts the number of rows of a table (nodes or edges table) and returns the result.</p>
     * <p>Uses <code>GraphElementsController</code> <code>getNodesCount</code> and <code>getEdgesCount</code> to calculate the result.</p>
     * @param table
     * @return the number of rows in <code>table</code>
     */
    int getTableRowsCount(AttributeTable table);

    /**
     * <p>Checks if the given table is nodes table.</p>
     * @return True if the table is nodes table, false otherwise
     */
    boolean isNodeTable(AttributeTable table);

    /**
     * <p>Checks if the given table is edges table.</p>
     * @return True if the table is edges table, false otherwise
     */
    boolean isEdgeTable(AttributeTable table);

    /**
     * <p>Indicates if the Data Laboratory API behaviour allows to delete the given column of a table.</p>
     * <p>The behaviour is: Any column that does not have a <code>AttributeOrigin</code> of type <code>PROPERTY</code> can be deleted.</p>
     * @param column Column to check if it can be deleted
     * @return True if it can be deleted, false otherwise
     */
    boolean canDeleteColumn(AttributeColumn column);

    /**
     * <p>Indicates if the Data Laboratory API behaviour allows to change a value of the given column of a table.</p>
     * <p>The behaviour is: Only values of columns with <code>AttributeOrigin</code> of type <code>DATA</code> or a node/edge label and weight column can be changed. (but weight can't be null. see <code>canClearColumnData</code> method).</p>
     * <p>Also, columns with a <code>DYNAMIC</code> or <code>TIME_INTERVAL</code> <code>AttributeType</code> are not allowed to be changed since they are only used for dynamic attributes purposes.</p>
     * @param column Column to theck its values can be changed
     * @return True if the column values can be changed, false otherwise
     */
    boolean canChangeColumnData(AttributeColumn column);

    /**
     * <p>Indicates if the Data Laboratory API behaviour allows to set as null a value of the given column of a table.</p>
     * <p>The behaviour is: Only values of columns with <code>AttributeOrigin</code> of type <code>DATA</code> or a node/edge label column can be set to null. Edge weight can't be null</p>
     * <p>Also, columns with a <code>DYNAMIC</code> or <code>TIME_INTERVAL</code> AttributeType are not allowed to be cleared since they are only used for dynamic attributes purposes.</p>
     * @param column Column to theck its values can be changed
     * @return True if the column values can be changed, false otherwise
     */
    boolean canClearColumnData(AttributeColumn column);

    /**
     * <p>Calculates all statistics at once from a number/number list column using <code>MathUtils</code> class.</p>
     * <p>Returns an array of <b>length=8</b> of <code>BigDecimal</code> numbers with the results in the following order: 
     * <ol>
     * <li>average</li>
     * <li>first quartile (Q1)</li>
     * <li>median</li>
     * <li>third quartile (Q3)</li>
     * <li>interquartile range (IQR)</li>
     * <li>sum</li>
     * <li>minimumValue</li>
     * <li>maximumValue</li>
     * </ol>
     * </p>
     * <p>The column can only be a number/number list column.</p>
     * <p>Otherwise, a IllegalArgumentException will be thrown.</p>
     * @param table Table of the column
     * @param column Column to get statistics
     * @return Array with statistics
     */
    BigDecimal[] getNumberOrNumberListColumnStatistics(AttributeTable table, AttributeColumn column);

    /**
     * <p>Prepares an array with all not null numbers of all the rows of a given column.</p>
     * <p>The column can only be a number/number list column.</p>
     * <p>Otherwise, a IllegalArgumentException will be thrown.</p>
     * @param table Table of the column to get numbers
     * @param column Column to get numbers
     * @return Array with all numbers.
     */
    Number[] getColumnNumbers(AttributeTable table, AttributeColumn column);

    /**
     * <p>Prepares an array with all not null numbers of a row using only the given columns.</p>
     * <p>The columns can only be number/dynamic number/number list columns (in any combination).</p>
     * <p><b>All</b> numbers intervals of a dynamic number column will be used.</p>
     * <p>Otherwise, a IllegalArgumentException will be thrown.</p>
     * @param row Row to get numbers
     * @param columns Columns of the row to use
     * @return Array with all numbers
     */
    Number[] getRowNumbers(Attributes row, AttributeColumn[] columns);

    /**
     * <p>Method for importing CSV file data to nodes table.</p>
     * <p>Only special case is treating columns is id columns: first column found named 'id' (case insensitive) will be used as node id, others will be ignored.</p>
     * <p>No special column must be provided.</p>
     * <p>If a column name is not already in nodes table, it will be created with the corresponding columnType index.</p>
     * <p>If a node id already exists, depending on assignNewNodeIds, a new id will be assigned to it or instead, the already existing node attributes will be updated with the CSV data</p>
     * @param file CSV file
     * @param separator Separator of values of the CSV file
     * @param charset Charset of the CSV file
     * @param columnNames Names of the columns in the CSV file to use
     * @param columnTypes Types of the columns in the CSV file to use when creating columns
     * @param assignNewNodeIds Indicates if nodes should be assigned new ids when the ids are already in nodes table or not provided.
     */
    void importCSVToNodesTable(File file, Character separator, Charset charset, String[] columnNames, AttributeType[] columnTypes, boolean assignNewNodeIds);

    /**
     * <p>Method for importing csv data to edges table.</p>
     * <p>Column named 'Source' and 'Target' (case insensitive) should be provided. Any row that does not provide a source and target nodes ids will be ignored.</p>
     * <p>If no 'Type' (case insensitive) column is provided, all edges will be directed.</p>
     * <p>If an edge already exists or cannot be created, it will be ignored, and no data will be updated.</p>
     *
     * <p>Special cases are id, source, target and type columns:
     * <ul>
     * <li>First column found named 'id' (case insensitive) will be used as node id, others will be ignored.</li>
     * <li>First column named 'Source' (case insensitive) will be used as source node id. The next ones will be used as normal columns, and created if not already existing.</li>
     * <li>First column named 'Target' (case insensitive) will be used as target node id. The next ones will be used as normal columns, and created if not already existing.</li>
     * <li>First column named 'Type' (case insensitive) will be used as edge type, matching 'Directed' or 'Undirected' strings (case insensitive). The next ones will be used as normal columns, and created if not already existing.</li>
     * </ul>
     * </p>
     * @param file CSV file
     * @param separator Separator of values of the CSV file
     * @param charset Charset of the CSV file
     * @param columnNames Names of the columns in the CSV file to use
     * @param columnTypes Types of the columns in the CSV file to use when creating columns
     * @param createNewNodes Indicates if missing nodes should be created when an edge declares a source or target id not already existing
     */
    void importCSVToEdgesTable(File file, Character separator, Charset charset, String[] columnNames, AttributeType[] columnTypes, boolean createNewNodes);
}
