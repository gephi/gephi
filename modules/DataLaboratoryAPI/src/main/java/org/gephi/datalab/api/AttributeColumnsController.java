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
package org.gephi.datalab.api;

import java.io.File;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.gephi.datalab.spi.rows.merge.AttributeRowsMergeStrategy;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Element;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.Table;
import org.gephi.graph.api.TimeRepresentation;

/**
 * <p>This interface defines part of the Data Laboratory API basic actions.</p>
 * <p>It contains methods for manipulating the attributes and properties of nodes and edges.</p>
 * @author Eduardo Ramos
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
    boolean setAttributeValue(Object value, Element row, Column column);

    /**
     * <p>Adds a new column to the specified table with the given title and type of column.</p>
     * <p>The title for the new column can't be repeated in the table, null or an empty string.</p>.
     * <p>The id of the column will be set to the same as the title,
     * but if the first TimeInterval column of the table is created
     * it will be given the default dynamic time interval id to be able to use dynamic filters.</p>
     * <p>The <code>AttributeOrigin</code> of the column will be set to <code>DATA</code>.</p>
     * <p>Default column value will be set to null.</p>
     * @param table Table to add the column
     * @param title Title for the new column, can't be repeated in the table, null or empty string
     * @param type Type for the new column
     * @return The created column or null if the column could not be created
     */
    Column addAttributeColumn(Table table, String title, Class type);

    /**
     * <p>Duplicates a given column of a table and copies al row values.</p>
     * <p>If the <code>Class</code> for the new column is different from the old column type, it will try to parse each value. If it is not possible, the value will be set to null.</p>
     * <p>The title for the new column can't be repeated in the table, null or an empty string.</p>.
     * <p>The id of the column will be set to the title.</p>
     * <p>The <code>AttributeOrigin</code> of the column will be set to <code>DATA</code>.</p>
     * <p>Default column value will be set to null.</p>
     * @param table Table of the column to duplicate
     * @param column Column to duplicate
     * @param title Title for the new column
     * @param type Class for the new column
     * @return The created column or null if the column could not be created
     */
    Column duplicateColumn(Table table, Column column, String title, Class type);

    /**
     * <p>Copies all row values of a column to another column.</p>
     * <p>If the <code>Class</code> for the target is different from the source column type, it will try to parse each value. If it is not possible, the value will be set to null.</p>
     * <p>Source and target columns must be different.</p>
     * @param table Table of the columns
     * @param sourceColumn Source column
     * @param targetColumn Target column
     */
    void copyColumnDataToOtherColumn(Table table, Column sourceColumn, Column targetColumn);

    /**
     * <p>Deletes the specified column from a table if the table has the column and data laboratory behaviour allows to delete it (see <code>canDeleteColumn</code> method).</p>
     * @param table Table to delete the column
     * @param column Column to delete
     */
    void deleteAttributeColumn(Table table, Column column);
    
    /**
     * <p>Converts and replaces a table column with a dynamic column preserving original column values.</p>
     * <p>This should be used only in columns where the <code>canConvertColumnToDynamic</code> returns true</p>
     * <p>For graphs with {@code INTERVAL} {@link TimeRepresentation}, the new values have a default interval that uses the {@code low} and {@code high} parameters.</p>
     * <p>For graphs with {@code TIMESTAMP} {@link TimeRepresentation}, the new values have a default timestamp that uses the {@code low} parameter, {@code high} parameter is ignored.</p>
     * @param table Table of the column
     * @param column Column to convert and replace
     * @param low Low bound for default interval or default timestamp
     * @param high High bound for default interval or ignored for timestamps
     * @return The new column
     */
    Column convertAttributeColumnToDynamic(Table table, Column column, double low, double high);
    
    /**
     * <p>Converts a table column into a new dynamic column preserving original column values. The original column is kept intact</p>
     * <p>For graphs with {@code INTERVAL} {@link TimeRepresentation}, the new values have a default interval that uses the {@code low} and {@code high} parameters.</p>
     * <p>For graphs with {@code TIMESTAMP} {@link TimeRepresentation}, the new values have a default timestamp that uses the {@code low} parameter, {@code high} parameter is ignored.</p>
     * @param table Table of the column
     * @param column Column to convert to dynamic
     * @param low Low bound for default interval or default timestamp
     * @param high High bound for default interval or ignored for timestamps
     * @param newColumnTitle Title for the new dynamic column
     * @return The new column
     */
    Column convertAttributeColumnToNewDynamicColumn(Table table, Column column, double low, double high, String newColumnTitle);

    /**
     * <p>Fills the data values of a given column of a table with a value as a String,
     * parsing it for the <code>Class</code> of the column. If it is not possible to parse,
     * the value will be set to null.</p>
     * @param table Table of the column
     * @param column Column to fill
     * @param value String representation of the value for each row of the column
     */
    void fillColumnWithValue(Table table, Column column, String value);

    /**
     * <p>Fills the data values of a given column of the indicated nodes with a value as a String,
     * parsing it for the <code>Class</code> of the column. If it is not possible to parse,
     * the value will be set to null.</p>
     * @param nodes Nodes to fill
     * @param column Column to fill
     * @param value String representation of the value for the column for each node
     */
    void fillNodesColumnWithValue(Node[] nodes, Column column, String value);

    /**
     * <p>Fills the data values of a given column of the indicated edges with a value as a String,
     * parsing it for the <code>Class</code> of the column. If it is not possible to parse,
     * the value will be set to null.</p>
     * @param edges Edges to fill
     * @param column Column to fill
     * @param value String representation of the value for the column for each edge
     */
    void fillEdgesColumnWithValue(Edge[] edges, Column column, String value);

    /**
     * <p>Clears all rows data for a given column of a table (nodes table or edges table)</p>
     * @param table Table to clear column data
     * @param column Column to clear data
     */
    void clearColumnData(Table table, Column column);

    /**
     * <p>Calculates the absolute frequency of appearance of each value of the given column and returns a Map containing each different value mapped to its frequency of appearance.</p>
     * @param table Table of the column
     * @param column Column to calculate values frequencies
     * @return Map containing each different value mapped to its frequency of appearance
     */
    Map<Object, Integer> calculateColumnValuesFrequencies(Table table, Column column);

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
    Column createBooleanMatchesColumn(Table table, Column column, String newColumnTitle, Pattern pattern);

    /**
     * <p>Negates not null values of a given <code>BOOLEAN</code> or <code>LIST_BOOLEAN</code>column.</p>
     * <p>Throws IllegalArgumentException if the column does not have <code>BOOLEAN</code> or <code>LIST_BOOLEAN</code> <code>Class</code>.</p>
     * @param table Table of the column to negate
     * @param column Boolean column to negate
     */
    void negateBooleanColumn(Table table, Column column);

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
    Column createFoundGroupsListColumn(Table table, Column column, String newColumnTitle, Pattern pattern);

    /**
     * <p>Clears all node attributes except computed attributes and id, checking first that the node is in the graph.</p>
     * <p>Columns to clear can be specified, but id and computed columns will not be cleared.</p>
     * @param node Node to clear data
     * @param columnsToClear Columns of the node to clear. All columns will be cleared if it is null
     */
    void clearNodeData(Node node, Column[] columnsToClear);

    /**
     * <p>Clears all the nodes attributes except computed attributes and id.</p>
     * <p>Columns to clear can be specified, but id and computed columns will not be cleared.</p>
     * @param nodes Array of nodes to clear data
     * @param columnsToClear Columns of the nodes to clear. All columns will be cleared if it is null
     */
    void clearNodesData(Node[] nodes, Column[] columnsToClear);

    /**
     * <p>Clears all edge attributes except computed attributes and id.</p>
     * <p>Columns to clear can be specified, but id and computed columns will not be cleared.</p>
     * @param edge Edge to clear data
     * @param columnsToClear Columns of the edge to clear. All columns will be cleared if it is null
     */
    void clearEdgeData(Edge edge, Column[] columnsToClear);

    /**
     * <p>Clears all the edges attributes except computed attributes and id, checking first that the edges are in the graph.</p>
     * <p>Columns to clear can be specified, but id and computed columns will not be cleared.</p>
     * @param edges Array of edges to clear data
     * @param columnsToClear Columns of the edges to clear. All columns will be cleared if it is null
     */
    void clearEdgesData(Edge[] edges, Column[] columnsToClear);

    /**
     * <p>Clears row attributes except computed attributes and id if node/edge row.</p>
     * <p>Columns to clear can be specified, but id of node/edge and computed columns will not be cleared.</p>
     * @param row Array of rows to clear data
     * @param columnsToClear Columns of the row to clear. All columns will be cleared if it is null
     */
    void clearRowData(Element row, Column[] columnsToClear);

    /**
     * <p>Copies attributes data of the given node to the other rows except computed attributes and id.</p>
     * <p>Columns to copy can be specified, but id node and computed columns will not be copied.</p>
     * @param node Node to copy data from
     * @param otherNodes Nodes to copy data to
     * @param columnsToCopy Columns of the node to copy. All columns will be copied if it is null
     */
    void copyNodeDataToOtherNodes(Node node, Node[] otherNodes, Column[] columnsToCopy);

    /**
     * <p>Copies attributes data of the given edge to the other rows except computed attributes and id.</p>
     * <p>Columns to copy can be specified, but id edge and computed columns will not be copied.</p>
     * @param edge Edge to copy data from
     * @param otherEdges Edges to copy data to
     * @param columnsToCopy Columns of the edge to copy. All columns will be copied if it is null
     */
    void copyEdgeDataToOtherEdges(Edge edge, Edge[] otherEdges, Column[] columnsToCopy);

    /**
     * <p>Copies attributes data of the given row to the other rows except computed attributes and id if node/edge.</p>
     * <p>Columns to copy can be specified, but id of node/edge and computed columns will not be copied.</p>
     * @param row Row to copy data from
     * @param otherRows Rows to copy data to
     * @param columnsToCopy Columns of the row to copy. All columns will be copied if it is null
     */
    void copyRowDataToOtherRows(Element row, Element[] otherRows, Column[] columnsToCopy);

    /**
     * <p>Returns all rows of a given table (node or edges table).</p>
     * <p>Used for iterating through all attribute rows of a table</p>
     * @param table Table to get attribute rows
     * @return Array of attribute rows of the table
     */
    Element[] getTableAttributeRows(Table table);

    /**
     * <p>Counts the number of rows of a table (nodes or edges table) and returns the result.</p>
     * <p>Uses <code>GraphElementsController</code> <code>getNodesCount</code> and <code>getEdgesCount</code> to calculate the result.</p>
     * @param table
     * @return the number of rows in <code>table</code>
     */
    int getTableRowsCount(Table table);

    /**
     * <p>Checks if the given table is nodes table.</p>
     * @param table Table to check
     * @return True if the table is nodes table, false otherwise
     */
    boolean isNodeTable(Table table);

    /**
     * <p>Checks if the given table is edges table.</p>
     * @param table Table to check
     * @return True if the table is edges table, false otherwise
     */
    boolean isEdgeTable(Table table);
    
    boolean isTableColumn(Table table, Column column);
    
    boolean isNodeColumn(Column column);
    
    boolean isEdgeColumn(Column column);

    /**
     * <p>Indicates if the Data Laboratory API behaviour allows to delete the given column of a table.</p>
     * <p>The behaviour is: Any column that does not have a <code>AttributeOrigin</code> of type <code>PROPERTY</code> can be deleted.</p>
     * @param column Column to check if it can be deleted
     * @return True if it can be deleted, false otherwise
     */
    boolean canDeleteColumn(Column column);

    /**
     * <p>Indicates if the Data Laboratory API behaviour allows to change a value of the given column of a table.</p>
     * <p>The behaviour is: Only values of columns with <code>AttributeOrigin</code> of type <code>DATA</code> or a node/edge label and weight column can be changed. (but weight can't be null. see <code>canClearColumnData</code> method).</p>
     * @param column Column to check if values can be changed
     * @return True if the column values can be changed, false otherwise
     */
    boolean canChangeColumnData(Column column);

    /**
     * <p>Indicates if the Data Laboratory API behaviour allows to set as null a value of the given column of a table.</p>
     * <p>The behaviour is: Only values of columns with <code>AttributeOrigin</code> of type <code>DATA</code> or a node/edge label column can be set to null. Edge weight can't be null</p>
     * @param column Column to check if values can be changed
     * @return True if the column values can be changed, false otherwise
     */
    boolean canClearColumnData(Column column);
    
    /**
     * <p>Indicates if the Data Laboratory API behaviour allows to convert an existing column into its dynamic equivalent.</p>
     * <p>The behaviour is: Only values of columns with <code>AttributeOrigin</code> of type <code>DATA</code> and <b>edge weight</b> can be converted.</p>
     * @param column Column to check if can be converted
     * @return True if the column can be converted to dynamic, false otherwise
     */
    boolean canConvertColumnToDynamic(Column column);

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
    BigDecimal[] getNumberOrNumberListColumnStatistics(Table table, Column column);

    /**
     * <p>Prepares an array with all not null numbers of all the rows of a given column.</p>
     * <p>The column can only be a number/number list column.</p>
     * <p>Otherwise, a IllegalArgumentException will be thrown.</p>
     * @param table Table of the column to get numbers
     * @param column Column to get numbers
     * @return Array with all numbers.
     */
    Number[] getColumnNumbers(Table table, Column column);
    
    /**
     * <p>Prepares an array <b>only</b> with all not null numbers the indicated rows of a given column.</p>
     * <p>The column can only be a number/number list column.</p>
     * <p>Otherwise, a IllegalArgumentException will be thrown.</p>
     * @param rows Rows to get numbers
     * @param column Column to get numbers
     * @return Array with all numbers.
     */
    Number[] getRowsColumnNumbers(Element[] rows, Column column);

    /**
     * <p>Prepares an array with all not null numbers of a row using only the given columns.</p>
     * <p>The columns can only be number/dynamic number/number list columns (in any combination).</p>
     * <p><b>All</b> numbers intervals of a dynamic number column will be used.</p>
     * <p>Otherwise, a IllegalArgumentException will be thrown.</p>
     * @param row Row to get numbers
     * @param columns Columns of the row to use
     * @return Array with all numbers
     */
    Number[] getRowNumbers(Element row, Column[] columns);

    /**
     * <p>Merges the given rows values to the given result row using one merge strategy for each column of the table.</p>
     * <p>The number of columns must be equal to the number of merge strategies provided</p>
     * <p>No parameters can be null except selectedRow (first row will be used in case selectedRow is null)</p>
     * <p>If any strategy is null, the value of the selectedRow will be used</p>
     * @param columns Columns to apply a merge strategy in each row
     * @param mergeStrategies Strategies for each column in {@code columns}
     * @param rows Rows to merge (at least 1)
     * @param selectedRow Main selected row or null (first row will be used in case selectedRow is null)
     * @param resultRow Already existing row to put the values on
     */
    void mergeRowsValues(Column[] columns, AttributeRowsMergeStrategy[] mergeStrategies, Element[] rows, Element selectedRow, Element resultRow);
    
    /**
     * <p>Finds and returns nodes duplicates based on the values of a given column of nodes table</p>
     * <p>A node is a duplicate of other if they have the same value (String representation of the values is used) in the given column.</p>
     * <p>This is useful to be used to automatically merge duplicated nodes</p>
     * @param column Column to use values to detect duplicates
     * @param caseSensitive Case insensitivity when comparing the column values
     * @return List of node duplicates groups (at least 2 nodes in each group)
     */
    List<List<Node>> detectNodeDuplicatesByColumn(Column column, boolean caseSensitive);
}
