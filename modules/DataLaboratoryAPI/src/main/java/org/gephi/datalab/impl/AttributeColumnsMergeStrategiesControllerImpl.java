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

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import org.gephi.graph.api.AttributeUtils;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Origin;
import org.gephi.graph.api.Table;
import org.gephi.datalab.api.AttributeColumnsController;
import org.gephi.datalab.api.AttributeColumnsMergeStrategiesController;
import org.gephi.graph.api.Element;
import org.gephi.graph.api.types.TimestampMap;
import org.gephi.graph.api.types.TimestampSet;
import org.gephi.graph.impl.GraphStoreConfiguration;
import org.gephi.utils.StatisticsUtils;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 * Implementation of the AttributeColumnsMergeStrategiesController interface
 * declared in the Data Laboratory API.
 * @author Eduardo Ramos
 * @see AttributeColumnsMergeStrategiesController
 */
@ServiceProvider(service = AttributeColumnsMergeStrategiesController.class)
public class AttributeColumnsMergeStrategiesControllerImpl implements AttributeColumnsMergeStrategiesController {

    @Override
    public Column joinWithSeparatorMerge(Table table, Column[] columnsToMerge, Class newColumnType, String newColumnTitle, String separator) {
        if (table == null || columnsToMerge == null) {
            throw new IllegalArgumentException("Table or columns can't be null");
        }

        AttributeColumnsController ac = Lookup.getDefault().lookup(AttributeColumnsController.class);
        Column newColumn;
        newColumn = ac.addAttributeColumn(table, newColumnTitle, newColumnType != null ? newColumnType : String.class);//Create as STRING column by default. Then it can be duplicated to other type.
        if (newColumn == null) {
            return null;
        }

        if (separator == null) {
            separator = "";
        }

        Object value;
        StringBuilder sb;
        final int columnsCount = columnsToMerge.length;

        for (Element row : ac.getTableAttributeRows(table)) {
            sb = new StringBuilder();
            for (int i = 0; i < columnsCount; i++) {
                value = row.getAttribute(columnsToMerge[i]);
                if (value != null) {
                    sb.append(value.toString());
                    if (i < columnsCount - 1) {
                        sb.append(separator);
                    }
                }
            }
            row.setAttribute(newColumn, sb.toString());
        }

        return newColumn;
    }

    @Override
    public Column booleanLogicOperationsMerge(Table table, Column[] columnsToMerge, BooleanOperations[] booleanOperations, String newColumnTitle) {
        AttributeColumnsController ac = Lookup.getDefault().lookup(AttributeColumnsController.class);
        if (table == null || columnsToMerge == null || booleanOperations == null || booleanOperations.length != columnsToMerge.length - 1) {
            throw new IllegalArgumentException("table, columns or operations can't be null and operations length must be columns length -1");
        }
        
        for (Column column : columnsToMerge) {
            if(!column.getTypeClass().equals(Boolean.class)){
                throw new IllegalArgumentException("All columns have to be boolean columns");
            }
        }

        Column newColumn;
        newColumn = ac.addAttributeColumn(table, newColumnTitle, Boolean.class);
        if (newColumn == null) {
            return null;
        }

        Boolean value;
        Boolean secondValue;

        for (Element row : ac.getTableAttributeRows(table)) {
            value = (Boolean) row.getAttribute(columnsToMerge[0]);
            value = value != null ? value : false;//Use false if null
            for (int i = 0; i < booleanOperations.length; i++) {
                secondValue = (Boolean) row.getAttribute(columnsToMerge[i + 1]);
                secondValue = secondValue != null ? secondValue : false;//Use false if null
                switch (booleanOperations[i]) {
                    case AND:
                        value = value && secondValue;
                        break;
                    case OR:
                        value = value || secondValue;
                        break;
                    case XOR:
                        value = value ^ secondValue;
                        break;
                    case NAND:
                        value = !(value && secondValue);
                        break;
                    case NOR:
                        value = !(value || secondValue);
                        break;
                }
            }
            row.setAttribute(newColumn, value);
        }

        return newColumn;
    }

    @Override
    public Column mergeNumericColumnsToTimeInterval(Table table, Column startColumn, Column endColumn, double defaultStart, double defaultEnd) {
        checkTableAndOneColumn(table, startColumn, endColumn);

        Column timeIntervalColumn = getTimeIntervalColumn(table);
        final int startColumnIndex = startColumn != null ? startColumn.getIndex() : -1;
        final int endColumnIndex = endColumn != null ? endColumn.getIndex() : -1;
        final boolean isStartColumnNumeric = startColumn != null ? (!AttributeUtils.isDynamicType(startColumn.getTypeClass()) && AttributeUtils.isNumberType(startColumn.getTypeClass())) : false;
        final boolean isEndColumnNumeric = endColumn != null ? (!AttributeUtils.isDynamicType(endColumn.getTypeClass()) && AttributeUtils.isNumberType(endColumn.getTypeClass())) : false;

        AttributeColumnsController ac = Lookup.getDefault().lookup(AttributeColumnsController.class);
        Object value;
        double start, end;
        TimestampMap timeInterval;
        for (Element row : ac.getTableAttributeRows(table)) {
            if (startColumnIndex != -1) {
                value = row.getAttribute(startColumn);
                if (value != null) {
                    if (isStartColumnNumeric) {
                        start = ((Number) value).doubleValue();
                    } else {
                        start = parseDouble(value.toString(), defaultStart);
                    }
                } else {
                    start = defaultStart;
                }
            } else {
                start = defaultStart;
            }
            if (endColumnIndex != -1) {
                value = row.getAttribute(endColumn);
                if (value != null) {
                    if (isEndColumnNumeric) {
                        end = ((Number) value).doubleValue();
                    } else {
                        end = parseDouble(value.toString(), defaultEnd);
                    }
                } else {
                    end = defaultEnd;
                }
            } else {
                end = defaultEnd;
            }
            if (!Double.isInfinite(start) && !Double.isInfinite(end) && start > end) {
                //When start>end, check what column was provided and keep its value. If both columns were provided, set an infinite interval:
                if (startColumnIndex == -1) {
                    start = Double.NEGATIVE_INFINITY;
                } else if (endColumnIndex == -1) {
                    end = Double.POSITIVE_INFINITY;
                } else {
                    start = Double.NEGATIVE_INFINITY;
                    end = Double.POSITIVE_INFINITY;
                }
            }
            
            row.addTimestamp(start);
            row.addTimestamp(end);
        }
        return timeIntervalColumn;
    }

    @Override
    public Column mergeDateColumnsToTimeInterval(Table table, Column startColumn, Column endColumn, SimpleDateFormat dateFormat, String defaultStartDate, String defaultEndDate) {
        checkTableAndOneColumn(table, startColumn, endColumn);
        if (dateFormat == null) {
            throw new IllegalArgumentException("Date format can't be null can't be null");
        }

        Column timeIntervalColumn = getTimeIntervalColumn(table);
        final int startColumnIndex = startColumn != null ? startColumn.getIndex() : -1;
        final int endColumnIndex = endColumn != null ? endColumn.getIndex() : -1;
        double defaultStart = parseDateToDouble(dateFormat, defaultStartDate, Double.NEGATIVE_INFINITY);
        double defaultEnd = parseDateToDouble(dateFormat, defaultEndDate, Double.POSITIVE_INFINITY);

        AttributeColumnsController ac = Lookup.getDefault().lookup(AttributeColumnsController.class);
        Object value;
        double start, end;
        TimestampMap timeInterval;
        for (Element row : ac.getTableAttributeRows(table)) {
            if (startColumnIndex != -1) {
                value = row.getAttribute(startColumn);
                start = parseDateToDouble(dateFormat, value != null ? value.toString() : null, defaultStart);
            } else {
                start = defaultStart;
            }
            if (endColumnIndex != -1) {
                value = row.getAttribute(endColumn);
                end = parseDateToDouble(dateFormat, value != null ? value.toString() : null, defaultEnd);
            } else {
                end = defaultEnd;
            }
            if (!Double.isInfinite(start) && !Double.isInfinite(end) && start > end) {
                //When start>end, check what column was provided and keep its value. If both columns were provided, set an infinite interval:
                if (startColumnIndex == -1) {
                    start = Double.NEGATIVE_INFINITY;
                } else if (endColumnIndex == -1) {
                    end = Double.POSITIVE_INFINITY;
                } else {
                    start = Double.NEGATIVE_INFINITY;
                    end = Double.POSITIVE_INFINITY;
                }
            }
            
            row.addTimestamp(start);
            row.addTimestamp(end);
        }
        return timeIntervalColumn;
    }

    @Override
    public Column averageNumberMerge(Table table, Column[] columnsToMerge, String newColumnTitle) {
        checkTableAndColumnsAreNumberOrNumberList(table, columnsToMerge);

        AttributeColumnsController ac = Lookup.getDefault().lookup(AttributeColumnsController.class);
        Column newColumn;
        newColumn = ac.addAttributeColumn(table, newColumnTitle, BigDecimal.class);//Create as BIGDECIMAL column by default. Then it can be duplicated to other type.
        if (newColumn == null) {
            return null;
        }

        BigDecimal average;
        for (Element row : ac.getTableAttributeRows(table)) {
            average = StatisticsUtils.average(ac.getRowNumbers(row, columnsToMerge));
            row.setAttribute(newColumn, average);
        }

        return newColumn;
    }

    @Override
    public Column firstQuartileNumberMerge(Table table, Column[] columnsToMerge, String newColumnTitle) {
        checkTableAndColumnsAreNumberOrNumberList(table, columnsToMerge);

        AttributeColumnsController ac = Lookup.getDefault().lookup(AttributeColumnsController.class);
        Column newColumn;
        newColumn = ac.addAttributeColumn(table, newColumnTitle, BigDecimal.class);//Create as BIGDECIMAL column by default. Then it can be duplicated to other type.
        if (newColumn == null) {
            return null;
        }

        BigDecimal Q1;
        for (Element row : ac.getTableAttributeRows(table)) {
            Q1 = StatisticsUtils.quartile1(ac.getRowNumbers(row, columnsToMerge));
            row.setAttribute(newColumn, Q1);
        }

        return newColumn;
    }

    @Override
    public Column medianNumberMerge(Table table, Column[] columnsToMerge, String newColumnTitle) {
        checkTableAndColumnsAreNumberOrNumberList(table, columnsToMerge);

        AttributeColumnsController ac = Lookup.getDefault().lookup(AttributeColumnsController.class);
        Column newColumn;
        newColumn = ac.addAttributeColumn(table, newColumnTitle, BigDecimal.class);//Create as BIGDECIMAL column by default. Then it can be duplicated to other type.
        if (newColumn == null) {
            return null;
        }

        final int newColumnIndex = newColumn.getIndex();

        BigDecimal median;
        for (Element row : ac.getTableAttributeRows(table)) {
            median = StatisticsUtils.median(ac.getRowNumbers(row, columnsToMerge));
            row.setAttribute(newColumn, median);
        }

        return newColumn;
    }

    @Override
    public Column thirdQuartileNumberMerge(Table table, Column[] columnsToMerge, String newColumnTitle) {
        checkTableAndColumnsAreNumberOrNumberList(table, columnsToMerge);

        AttributeColumnsController ac = Lookup.getDefault().lookup(AttributeColumnsController.class);
        Column newColumn;
        newColumn = ac.addAttributeColumn(table, newColumnTitle, BigDecimal.class);//Create as BIGDECIMAL column by default. Then it can be duplicated to other type.
        if (newColumn == null) {
            return null;
        }

        BigDecimal Q3;
        for (Element row : ac.getTableAttributeRows(table)) {
            Q3 = StatisticsUtils.quartile3(ac.getRowNumbers(row, columnsToMerge));
            row.setAttribute(newColumn, Q3);
        }

        return newColumn;
    }

    @Override
    public Column interQuartileRangeNumberMerge(Table table, Column[] columnsToMerge, String newColumnTitle) {
        checkTableAndColumnsAreNumberOrNumberList(table, columnsToMerge);

        AttributeColumnsController ac = Lookup.getDefault().lookup(AttributeColumnsController.class);
        Column newColumn;
        newColumn = ac.addAttributeColumn(table, newColumnTitle, BigDecimal.class);//Create as BIGDECIMAL column by default. Then it can be duplicated to other type.
        if (newColumn == null) {
            return null;
        }

        BigDecimal IQR, Q1, Q3;
        Number[] rowNumbers;
        for (Element row : ac.getTableAttributeRows(table)) {
            rowNumbers = ac.getRowNumbers(row, columnsToMerge);
            Q3 = StatisticsUtils.quartile3(rowNumbers);
            Q1 = StatisticsUtils.quartile1(rowNumbers);
            if (Q3 != null && Q1 != null) {
                IQR = Q3.subtract(Q1);
            } else {
                IQR = null;
            }
            row.setAttribute(newColumn, IQR);
        }

        return newColumn;
    }

    @Override
    public Column sumNumbersMerge(Table table, Column[] columnsToMerge, String newColumnTitle) {
        checkTableAndColumnsAreNumberOrNumberList(table, columnsToMerge);

        AttributeColumnsController ac = Lookup.getDefault().lookup(AttributeColumnsController.class);
        Column newColumn;
        newColumn = ac.addAttributeColumn(table, newColumnTitle, BigDecimal.class);//Create as BIGDECIMAL column by default. Then it can be duplicated to other type.
        if (newColumn == null) {
            return null;
        }

        BigDecimal sum;
        for (Element row : ac.getTableAttributeRows(table)) {
            sum = StatisticsUtils.sum(ac.getRowNumbers(row, columnsToMerge));
            row.setAttribute(newColumn, sum);
        }

        return newColumn;
    }

    @Override
    public Column minValueNumbersMerge(Table table, Column[] columnsToMerge, String newColumnTitle) {
        checkTableAndColumnsAreNumberOrNumberList(table, columnsToMerge);

        AttributeColumnsController ac = Lookup.getDefault().lookup(AttributeColumnsController.class);
        Column newColumn;
        newColumn = ac.addAttributeColumn(table, newColumnTitle, BigDecimal.class);//Create as BIGDECIMAL column by default. Then it can be duplicated to other type.
        if (newColumn == null) {
            return null;
        }

        BigDecimal min;
        for (Element row : ac.getTableAttributeRows(table)) {
            min = StatisticsUtils.minValue(ac.getRowNumbers(row, columnsToMerge));
            row.setAttribute(newColumn, min);
        }

        return newColumn;
    }

    @Override
    public Column maxValueNumbersMerge(Table table, Column[] columnsToMerge, String newColumnTitle) {
        checkTableAndColumnsAreNumberOrNumberList(table, columnsToMerge);

        AttributeColumnsController ac = Lookup.getDefault().lookup(AttributeColumnsController.class);
        Column newColumn;
        newColumn = ac.addAttributeColumn(table, newColumnTitle, BigDecimal.class);//Create as BIGDECIMAL column by default. Then it can be duplicated to other type.
        if (newColumn == null) {
            return null;
        }

        BigDecimal max;
        for (Element row : ac.getTableAttributeRows(table)) {
            max = StatisticsUtils.maxValue(ac.getRowNumbers(row, columnsToMerge));
            row.setAttribute(newColumn, max);
        }

        return newColumn;
    }

    /*************Private methods:*************/
    private Column getTimeIntervalColumn(Table table) {
        Column column = table.getColumn(GraphStoreConfiguration.ELEMENT_TIMESET_INDEX);
        if (column == null) {
            column = table.addColumn("timestamp", TimestampSet.class, Origin.PROPERTY);
        }
        return column;
    }

    private double parseDouble(String number, double defaultValue) {
        if (number == null) {
            return defaultValue;
        }
        try {
            return Double.parseDouble(number);
        } catch (Exception ex) {
            return defaultValue;
        }
    }

    private double parseDateToDouble(SimpleDateFormat dateFormat, String date, double defaultValue) {
        if (date == null) {
            return defaultValue;
        }
        try {
            Date d = dateFormat.parse(date);
            Calendar cal = Calendar.getInstance();
            cal.setTime(d);
            return cal.getTimeInMillis();
        } catch (Exception ex) {
            return defaultValue;
        }
    }

    private void checkTableAndOneColumn(Table table, Column startColumn, Column endColumn) {
        if (table == null) {
            throw new IllegalArgumentException("Table can't be null");
        }
        if (startColumn == null && endColumn == null) {
            throw new IllegalArgumentException("Only one column could be null");
        }
    }

    private void checkTableAndColumnsAreNumberOrNumberList(Table table, Column[] columns) {
        if (table == null) {
            throw new IllegalArgumentException("Table can't be null");
        }
        checkColumnsAreNumberOrNumberList(columns);
    }

    private void checkColumnsAreNumberOrNumberList(Column[] columns) {
        if (columns == null) {
            throw new IllegalArgumentException("All columns have to be number or number list columns and can't be null");
        }
        for (Column column : columns) {
            if(!AttributeUtils.isNumberType(column.getTypeClass())){
                throw new IllegalArgumentException("All columns have to be number or number list columns and can't be null");
            }
        }
    }
}
