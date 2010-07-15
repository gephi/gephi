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

import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeTable;

/**
 * This interface defines part of the Data Laboratory API.
 * It contains methods for applying different basic attribute columns merge strategies.
 * @author Eduardo Ramos <eduramiba@gmail.com>
 */
public interface AttributeColumnsMergeStrategiesController {

    public enum BooleanOperations {
        AND,
        OR,
        XOR,
        NAND,
        NOR
    }

    /**
     * Joins various columns of any type into a new column using the given separator string (or null).
     * The new created column will have <code>STRING</code> type.
     * @param table Table of the columns to merge
     * @param columnsToMerge Columns to merge
     * @param newColumnTitle Title for the new column
     * @param separator Separator to put between each value
     * @return The new created column
     */
    AttributeColumn joinWithSeparatorMerge(AttributeTable table, AttributeColumn[] columnsToMerge, String newColumnTitle, String separator);

    /**
     * AttributeColumnsMergeStrategy for only all boolean columns that merges various columns into a new boolean column
     * allowing to define each operation to apply between each pair of columns to merge.
     * The length of the operations array must be the length of the columns array-1, or IllegalArgumentException will be thrown.
     * @param table Table of the columns to merge
     * @param columnsToMerge Boolean columns to merge
     * @param booleanOperations Boolean operations to apply
     * @param newColumnTitle Title for the new column
     * @return The new created column
     */
    AttributeColumn booleanLogicOperationsMerge(AttributeTable table, AttributeColumn[] columnsToMerge, BooleanOperations[] booleanOperations, String newColumnTitle);
}
