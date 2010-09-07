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

import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeTable;
import org.gephi.data.attributes.api.AttributeType;

/**
 * This interface defines part of the Data Laboratory API basic actions.
 * It contains methods for applying different basic attribute columns merge strategies.
 * @author Eduardo Ramos <eduramiba@gmail.com>
 */
public interface AttributeColumnsMergeStrategiesController {

    /**
     * Enumeration that defines the supported logic operations for a merge with <code>booleanLogicOperationsMerge</code> strategy.
     */
    public enum BooleanOperations {
        AND,
        OR,
        XOR,
        NAND,
        NOR
    }

    /**
     * <p>Joins various columns of any type into a new column using the given separator string (or null).</p>
     * <p>If the specified column type is null, the new created column will have <code>STRING</code> <code>AttributeType</code> by default.</p>
     * @param table Table of the columns to merge
     * @param columnsToMerge Columns to merge
     * @param newColumnType Type for the new column. If null, <code>STRING</code> will be used by default
     * @param newColumnTitle Title for the new column
     * @param separator Separator to put between each value
     * @return The new created column
     */
    AttributeColumn joinWithSeparatorMerge(AttributeTable table, AttributeColumn[] columnsToMerge, AttributeType newColumnType, String newColumnTitle, String separator);

    /**
     * <p>Strategy to apply only to all boolean columns. Merges various columns into a new boolean column
     * allowing to define each operation to apply between each pair of columns to merge.</p>
     * <p>The length of the operations array must be the length of the columns array-1, or IllegalArgumentException will be thrown.</p>
     * @param table Table of the columns to merge
     * @param columnsToMerge Boolean columns to merge
     * @param booleanOperations Boolean operations to apply
     * @param newColumnTitle Title for the new column
     * @return The new created column
     */
    AttributeColumn booleanLogicOperationsMerge(AttributeTable table, AttributeColumn[] columnsToMerge, BooleanOperations[] booleanOperations, String newColumnTitle);

    /**
     * <p>Merges any combination of number or number list columns, calculating the average of all not null values
     * and puts the result of each row in a new column of <code>BIGDECIMAL</code> <code>AttributeType</code>.</p>
     * @param table Table of the columns to merge
     * @param columnsToMerge Number or number list columns
     * @param newColumnTitle Title for the new column
     * @return The new created column
     */
    AttributeColumn averageNumberMerge(AttributeTable table, AttributeColumn[] columnsToMerge, String newColumnTitle);

    /**
     * <p>Merges any combination of number or number list columns, calculating the first quartile (Q1) of all not null values
     * and puts the result of each row in a new column of <code>BIGDECIMAL</code> <code>AttributeType</code>.</p>
     * @param table Table of the columns to merge
     * @param columnsToMerge Number or number list columns
     * @param newColumnTitle Title for the new column
     * @return The new created column
     */
    AttributeColumn firstQuartileNumberMerge(AttributeTable table, AttributeColumn[] columnsToMerge, String newColumnTitle);

    /**
     * <p>Merges any combination of number or number list columns, calculating the median of all not null values
     * and puts the result of each row in a new column of <code>BIGDECIMAL</code> <code>AttributeType</code>.</p>
     * @param table Table of the columns to merge
     * @param columnsToMerge Number or number list columns
     * @param newColumnTitle Title for the new column
     * @return The new created column
     */
    AttributeColumn medianNumberMerge(AttributeTable table, AttributeColumn[] columnsToMerge, String newColumnTitle);

    /**
     * <p>Merges any combination of number or number list columns, calculating the third quartile (Q3) of all not null values
     * and puts the result of each row in a new column of <code>BIGDECIMAL</code> <code>AttributeType</code>.</p>
     * @param table Table of the columns to merge
     * @param columnsToMerge Number or number list columns
     * @param newColumnTitle Title for the new column
     * @return The new created column
     */
    AttributeColumn thirdQuartileNumberMerge(AttributeTable table, AttributeColumn[] columnsToMerge, String newColumnTitle);

    /**
     * <p>Merges any combination of number or number list columns, calculating the interquartile range (IQR) of all not null values
     * and puts the result of each row in a new column of <code>BIGDECIMAL</code> <code>AttributeType</code>.</p>
     * @param table Table of the columns to merge
     * @param columnsToMerge Number or number list columns
     * @param newColumnTitle Title for the new column
     * @return The new created column
     */
    AttributeColumn interQuartileRangeNumberMerge(AttributeTable table, AttributeColumn[] columnsToMerge, String newColumnTitle);

    /**
     * <p>Merges any combination of number or number list columns, calculating the sum of all not null values
     * and puts the result of each row in a new column of <code>BIGDECIMAL</code> <code>AttributeType</code>.</p>
     * @param table Table of the columns to merge
     * @param columnsToMerge Number or number list columns
     * @param newColumnTitle Title for the new column
     * @return The new created column
     */
    AttributeColumn sumNumbersMerge(AttributeTable table, AttributeColumn[] columnsToMerge, String newColumnTitle);

    /**
     * Merges any combination of number or number list columns, calculating the minimum value of all not null values
     * and puts the result of each row in a new column of <code>BIGDECIMAL</code> <code>AttributeType</code>.
     * @param table Table of the columns to merge
     * @param columnsToMerge Number or number list columns
     * @param newColumnTitle Title for the new column
     * @return The new created column
     */
    AttributeColumn minValueNumbersMerge(AttributeTable table, AttributeColumn[] columnsToMerge, String newColumnTitle);

    /**
     * <p>Merges any combination of number or number list columns, calculating the maximum value of all not null values
     * and puts the result of each row in a new column of <code>BIGDECIMAL</code> <code>AttributeType</code>.</p>
     * @param table Table of the columns to merge
     * @param columnsToMerge Number or number list columns
     * @param newColumnTitle Title for the new column
     * @return The new created column
     */
    AttributeColumn maxValueNumbersMerge(AttributeTable table, AttributeColumn[] columnsToMerge, String newColumnTitle);
}
