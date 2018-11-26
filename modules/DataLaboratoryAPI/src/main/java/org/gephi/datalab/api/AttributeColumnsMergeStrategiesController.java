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

import java.text.SimpleDateFormat;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Table;

/**
 * This interface defines part of the Data Laboratory API basic actions.
 * It contains methods for applying different basic attribute columns merge strategies.
 * @author Eduardo Ramos
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
    Column joinWithSeparatorMerge(Table table, Column[] columnsToMerge, Class newColumnType, String newColumnTitle, String separator);

    /**
     * <p>Merge 1 or 2 columns creating a time interval for each row. Values of the columns will be expected as numbers</p>
     * <p>Only one of the 2 column could be null, and its corresponding start/end default will be used.</p>
     * <p>Columns can be of any type. If not numeric, their values will be parsed.</p>
     * <p>Default start and end values will be used when the columns don't have a value or it can't be parsed to a double.</p>
     * When start &gt; end for any reason:
     * <ul>
     * <li>If both columns were provided: A infinite time interval will be set</li>
     * <li>If only one column was provided: The value for the provided column will be kept and the other will be infinite</li>
     * </ul>
     * @param table Table of the columns, can't be null or wrong
     * @param startColumn Column to use as start value
     * @param endColumn Column to use as end value
     * @param defaultStart Default start value
     * @param defaultEnd Default end value
     * @return Time interval column
     */
    Column mergeNumericColumnsToTimeInterval(Table table, Column startColumn, Column endColumn, double defaultStart, double defaultEnd);

    /**
     * <p>Merge 1 or 2 columns creating a time interval for each row. Values of the columns will be expected as dates in the given date format</p>
     * <p>Only one of the 2 column could be null, and its corresponding start/end default will be used.</p>
     * <p>Columns can be of any type.</p>
     * <p>Default start and end values will be used when the columns don't have a value or it can't be parsed to a date.
     * If a default value can't be parsed to a date, infinity will be used as default instead.</p>
     * When start &gt; end for any reason:
     * <ul>
     * <li>If both columns were provided: A infinite time interval will be set</li>
     * <li>If only one column was provided: The value for the provided column will be kept and the other will be infinite</li>
     * </ul>
     * @param table Table of the columns, can't be null or wrong
     * @param startColumn Column to use as start value
     * @param endColumn Column to use as end value
     * @param dateFormat Format for the dates, can't be null
     * @param defaultStartDate Default date to use as start if it can be parsed
     * @param defaultEndDate Default date to use as end if it can be parsed
     * @return Time interval column
     */
    Column mergeDateColumnsToTimeInterval(Table table, Column startColumn, Column endColumn, SimpleDateFormat dateFormat, String defaultStartDate, String defaultEndDate);

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
    Column booleanLogicOperationsMerge(Table table, Column[] columnsToMerge, BooleanOperations[] booleanOperations, String newColumnTitle);

    /**
     * <p>Merges any combination of number or number list columns, calculating the average of all not null values
     * and puts the result of each row in a new column of <code>BIGDECIMAL</code> <code>AttributeType</code>.</p>
     * @param table Table of the columns to merge
     * @param columnsToMerge Number or number list columns
     * @param newColumnTitle Title for the new column
     * @return The new created column
     */
    Column averageNumberMerge(Table table, Column[] columnsToMerge, String newColumnTitle);

    /**
     * <p>Merges any combination of number or number list columns, calculating the first quartile (Q1) of all not null values
     * and puts the result of each row in a new column of <code>BIGDECIMAL</code> <code>AttributeType</code>.</p>
     * @param table Table of the columns to merge
     * @param columnsToMerge Number or number list columns
     * @param newColumnTitle Title for the new column
     * @return The new created column
     */
    Column firstQuartileNumberMerge(Table table, Column[] columnsToMerge, String newColumnTitle);

    /**
     * <p>Merges any combination of number or number list columns, calculating the median of all not null values
     * and puts the result of each row in a new column of <code>BIGDECIMAL</code> <code>AttributeType</code>.</p>
     * @param table Table of the columns to merge
     * @param columnsToMerge Number or number list columns
     * @param newColumnTitle Title for the new column
     * @return The new created column
     */
    Column medianNumberMerge(Table table, Column[] columnsToMerge, String newColumnTitle);

    /**
     * <p>Merges any combination of number or number list columns, calculating the third quartile (Q3) of all not null values
     * and puts the result of each row in a new column of <code>BIGDECIMAL</code> <code>AttributeType</code>.</p>
     * @param table Table of the columns to merge
     * @param columnsToMerge Number or number list columns
     * @param newColumnTitle Title for the new column
     * @return The new created column
     */
    Column thirdQuartileNumberMerge(Table table, Column[] columnsToMerge, String newColumnTitle);

    /**
     * <p>Merges any combination of number or number list columns, calculating the interquartile range (IQR) of all not null values
     * and puts the result of each row in a new column of <code>BIGDECIMAL</code> <code>AttributeType</code>.</p>
     * @param table Table of the columns to merge
     * @param columnsToMerge Number or number list columns
     * @param newColumnTitle Title for the new column
     * @return The new created column
     */
    Column interQuartileRangeNumberMerge(Table table, Column[] columnsToMerge, String newColumnTitle);

    /**
     * <p>Merges any combination of number or number list columns, calculating the sum of all not null values
     * and puts the result of each row in a new column of <code>BIGDECIMAL</code> <code>AttributeType</code>.</p>
     * @param table Table of the columns to merge
     * @param columnsToMerge Number or number list columns
     * @param newColumnTitle Title for the new column
     * @return The new created column
     */
    Column sumNumbersMerge(Table table, Column[] columnsToMerge, String newColumnTitle);

    /**
     * Merges any combination of number or number list columns, calculating the minimum value of all not null values
     * and puts the result of each row in a new column of <code>BIGDECIMAL</code> <code>AttributeType</code>.
     * @param table Table of the columns to merge
     * @param columnsToMerge Number or number list columns
     * @param newColumnTitle Title for the new column
     * @return The new created column
     */
    Column minValueNumbersMerge(Table table, Column[] columnsToMerge, String newColumnTitle);

    /**
     * <p>Merges any combination of number or number list columns, calculating the maximum value of all not null values
     * and puts the result of each row in a new column of <code>BIGDECIMAL</code> <code>AttributeType</code>.</p>
     * @param table Table of the columns to merge
     * @param columnsToMerge Number or number list columns
     * @param newColumnTitle Title for the new column
     * @return The new created column
     */
    Column maxValueNumbersMerge(Table table, Column[] columnsToMerge, String newColumnTitle);
}
