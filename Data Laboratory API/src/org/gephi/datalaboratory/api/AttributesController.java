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

import java.util.Map;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeTable;
import org.gephi.data.attributes.api.AttributeType;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Node;

/**
 * This interface defines part of the Data Laboratory API.
 * It contains methods for manipulating the attributes and properties of nodes and edges.
 * @author Eduardo Ramos <eduramiba@gmail.com>
 */
public interface AttributesController {

    /**
     * ADds a new column to the specified table with the given id, title and type of column.
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
     * Deletes a specified column from a table if the table has the column.
     * @param table Table to delete the column
     * @param column Column to delete
     */
    void deleteAttributeColumn(AttributeTable table, AttributeColumn column);

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
     * Clears all node attributes except computed attributes and id, checking first that the node is in the graph.
     * @param node Node to clear data
     */
    void clearNodeData(Node node);

    /**
     * Clears all the nodes attributes except computed attributes and id, checking first that the nodes are in the graph.
     * @param nodes Array of nodes to clear data
     */
    void clearNodesData(Node[] nodes);

    /**
     * Clears all edge attributes except computed attributes and id, checking first that the edge is in the graph.
     * @param node Edge to clear data
     */
    void clearEdgeData(Edge edge);

    /**
     * Clears all the edges attributes except computed attributes and id, checking first that the edges are in the graph.
     * @param nodes Array of edges to clear data
     */
    void clearEdgesData(Edge[] edges);
}
