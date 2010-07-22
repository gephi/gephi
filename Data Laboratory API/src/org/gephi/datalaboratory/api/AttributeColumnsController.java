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
package org.gephi.datalaboratory.api;

import java.math.BigDecimal;
import java.util.Map;
import java.util.regex.Pattern;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeTable;
import org.gephi.data.attributes.api.AttributeType;
import org.gephi.graph.api.Attributes;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Node;

/**
 * This interface defines part of the Data Laboratory API.
 * It contains methods for manipulating the attributes and properties of nodes and edges.
 * @author Eduardo Ramos <eduramiba@gmail.com>
 */
public interface AttributeColumnsController {

    /**
     * Sets a value to the given row,column using the toString representation of the object.
     * Takes care to avoid parsing exceptions of the target column type.
     * Also does not set a null value to a column that can't have null values if the given object is null or the parse fails.
     * @param value Value to set
     * @param row Row
     * @param column Column
     * @return True if the value was set, false otherwise
     */
    boolean setAttributeValue(Object value, Attributes row, AttributeColumn column);

    /**
     * Adds a new column to the specified table with the given id, title and type of column.
     * The id of the column will be set to the number of columns.
     * The AttributeOrigin of the column will be set to DATA.
     * Default column value will be set to null.
     * @param table Table to add the column.
     * @param title Title for the new column
     * @param type Type for the new column
     * @return The created column
     */
    AttributeColumn addAttributeColumn(AttributeTable table, String title, AttributeType type);

    /**
     * Duplicates a given column of a table and copies al row values.
     * If the AttributeType for the new column is different from the old column, it will try to parse each value. If it is not possible, the value will be set to null.
     * @param table Table of the column to duplicate
     * @param column Column to duplicate
     * @param title Title for the new column
     * @param type AttributeType for the new column
     * @return The created column
     */
    AttributeColumn duplicateColumn(AttributeTable table, AttributeColumn column, String title, AttributeType type);
    
    /**
     * Copies all row values of a column to another column.
     * If the AttributeType for the target is different from the source column, it will try to parse each value. If it is not possible, the value will be set to null.
     * Source and target columns must be different.
     * @param table Table of the columns
     * @param sourceColumn Source column
     * @param targetColumn Target column
     */
    void copyColumnDataToOtherColumn(AttributeTable table, AttributeColumn sourceColumn, AttributeColumn targetColumn);

    /**
     * Deletes a specified column from a table if the table has the column and data laboratory behavious allows to delete it (see <code>canDeleteColumn</code>)
     * @param table Table to delete the column
     * @param column Column to delete
     */
    void deleteAttributeColumn(AttributeTable table, AttributeColumn column);

    /**
     * Fills the data values of a given column of a table with a value as a String,
     * parsing it for the AttributeType of the column.
     * @param table Table of the column
     * @param column Column to fill
     * @param value String representation of the value for each row of the column
     */
    void fillColumnWithValue(AttributeTable table, AttributeColumn column, String value);

    /**
     * Clears all rows data for a given column of a table (nodes table or edges table)
     * @param table Table to clear column data
     * @param column Column to clear data
     */
    void clearColumnData(AttributeTable table, AttributeColumn column);

    /**
     * Calculates the frequency of appearance of each value of the given column.
     * @param table Table of the column
     * @param column Column to calculate values frequencies
     * @return Map containing each different value mapped to its frequency of appearance
     */
    Map<Object,Integer> calculateColumnValuesFrequencies(AttributeTable table,AttributeColumn column);

    /**
     * Creates a new boolean column from the given column and regular expression with boolean values that indicate if
     * each of the old column values match the regular expression.
     * @param table Table of the column to match
     * @param column Column to match
     * @param newColumnTitle Title for the new boolean column
     * @param regex Regular expression to match
     */
    void createBooleanMatchesColumn(AttributeTable table, AttributeColumn column, String newColumnTitle,Pattern pattern);

    /**
     * Negates not null values of a given <code>BOOLEAN</code> or <code>LIST_BOOLEAN</code>column.
     * Throws IllegalArgumentException if the column does not have <code>BOOLEAN</code> or <code>LIST_BOOLEAN</code> AttributeType.
     * @param table Table of the column to negate
     * @param column Boolean column to negate
     */
    void NegateBooleanColumn(AttributeTable table, AttributeColumn column);

    /**
     * Creates a new string list column from the given column and regular expression with values that are
     * the list of matching groups of the given regular expression.
     * @param table Table of the column to match
     * @param column Column to match
     * @param newColumnTitle Title for the new boolean column
     * @param regex Regular expression to match
     */
    void createFoundGroupsListColumn(AttributeTable table, AttributeColumn column, String newColumnTitle, Pattern pattern);

    /**
     * Clears all node attributes except computed attributes and id, checking first that the node is in the graph.
     * Columns to clear can be specified, but id and computed columns will not be cleared.
     * @param node Node to clear data
     * @param columnsToClear Columns of the node to clear. All columns will be cleared if it is null
     */
    void clearNodeData(Node node,AttributeColumn[] columnsToClear);

    /**
     * Clears all the nodes attributes except computed attributes and id, checking first that the nodes are in the graph.
     * Columns to clear can be specified, but id and computed columns will not be cleared.
     * @param nodes Array of nodes to clear data
     * @param columnsToClear Columns of the nodes to clear. All columns will be cleared if it is null
     */
    void clearNodesData(Node[] nodes,AttributeColumn[] columnsToClear);

    /**
     * Clears all edge attributes except computed attributes and id, checking first that the edge is in the graph.
     * Columns to clear can be specified, but id and computed columns will not be cleared.
     * @param node Edge to clear data
     * @param columnsToClear Columns of the edge to clear. All columns will be cleared if it is null
     */
    void clearEdgeData(Edge edge,AttributeColumn[] columnsToClear);

    /**
     * Clears all the edges attributes except computed attributes and id, checking first that the edges are in the graph.
     * Columns to clear can be specified, but id and computed columns will not be cleared.
     * @param nodes Array of edges to clear data
     * @param columnsToClear Columns of the edges to clear. All columns will be cleared if it is null
     */
    void clearEdgesData(Edge[] edges,AttributeColumn[] columnsToClear);

    /**
     * Returns all rows of a given table.
     * Used for iterating through all attribute rows of a table
     * @param table Table to get attribute rows
     * @return Array of attribute rows of the table
     */
    Attributes[] getTableAttributeRows(AttributeTable table);

    /**
     * Counts the number of rows of a table (nodes or edges table) and returns the result.
     * Uses <code>GraphElementsController</code> <code>getNodesCount</code> and <code>getEdgesCount</code> to calculate the result.
     * @param table
     * @return
     */
    int getTableRowsCount(AttributeTable table);

    /**
     * Checks if the given table is nodes table.
     * @return True if the table is nodes table, false otherwise
     */
    boolean isNodeTable(AttributeTable table);

    /**
     * Checks if the given table is edges table.
     * @return True if the table is edges table, false otherwise
     */
    boolean isEdgeTable(AttributeTable table);

    /**
     * Indicates if the Data Laboratory API behavious allows to delete the given column of a table.
     * The behaviour is: Any column that does not have a AttributeOrigin of type PROPERTY can be deleted.
     * @param column Column to check if it can be deleted
     * @return True if it can be deleted, false otherwise
     */
    boolean canDeleteColumn(AttributeColumn column);

    /**
     * Indicates if the Data Laboratory API behavious allows to change a value of the given column of a table.
     * The behaviour is: Only values of columns with AttributeOrigin of type DATA or a node/edge label and label weight column can be changed. (but weight can't be null. see <code>canClearColumnData</code> method).
     * Also, columns with a <code>DYNAMIC</code> or <code>TIME_INTERVAL</code> AttributeType are not allowed to be changed since they are only used for dynamic attributes purposes.
     * If table is not nodes or edges table, it will not be checked if it is the label column.
     * @param column Column to theck its values can be changed
     * @return True if the column values can be changed, false otherwise
     */
    boolean canChangeColumnData(AttributeColumn column);

    /**
     * Indicates if the Data Laboratory API behavious allows to set as null a value of the given column of a table.
     * The behaviour is: Only values of columns with AttributeOrigin of type DATA or a node/edge label column can be set to null.
     * Also, columns with a <code>DYNAMIC</code> or <code>TIME_INTERVAL</code> AttributeType are not allowed to be changed since they are only used for dynamic attributes purposes.
     * If table is not nodes or edges table, it will not be checked if it is the label column.
     * @param column Column to theck its values can be changed
     * @return True if the column values can be changed, false otherwise
     */
    boolean canClearColumnData(AttributeColumn column);

    /**
     * Calculates all at once the average, median, sum, minimum and maximum values from a number/number list column using MathUtils class.
     * Return an array with length=5 of BigDecimal numbers with the results in the following order: average, median, sum, minimumValue and maximumValue.
     * The column can only be a number/number list column.
     * Otherwise, a IllegalArgumentException will be thrown.
     * @param table Table of the column
     * @param column Column to get statistics
     * @return Array with statistics
     */
    BigDecimal[] getNumberOrNumberListColumnStatistics(AttributeTable table, AttributeColumn column);

    /**
     * Prepares an array with all not null numbers of all the rows of a given column.
     * The column can only be a number/number list column.
     * Otherwise, a IllegalArgumentException will be thrown.
     * @param table Table of the column to get numbers
     * @param column Column to get numbers
     * @return Array with all numbers.
     */
    public Number[] getColumnNumbers(AttributeTable table, AttributeColumn column);

    /**
     * Prepares an array with all not null numbers of a row using only the given columns.
     * The columns can only be a number/number list columns (in any combination).
     * Otherwise, a IllegalArgumentException will be thrown.
     * @param row Row to get numbers
     * @param columns Columns of the row to use
     * @return Array with all numbers
     */
    public Number[] getRowNumbers(Attributes row, AttributeColumn[] columns);
}
