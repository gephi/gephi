/*
 Copyright 2008-2010 Gephi
 Authors : Mathieu Bastian <mathieu.bastian@gephi.org>, Mathieu Jacomy, Julian Bilcke, Eduardo Ramos
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
package org.gephi.desktop.datalab.tables;

import org.gephi.desktop.datalab.tables.columns.ElementDataColumn;
import org.gephi.desktop.datalab.tables.columns.AttributeDataColumn;
import org.gephi.desktop.datalab.*;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.regex.PatternSyntaxException;
import javax.swing.RowFilter;
import javax.swing.table.TableCellRenderer;
import org.gephi.graph.api.Column;
import org.gephi.datalab.api.AttributeColumnsController;
import org.gephi.desktop.datalab.tables.celleditors.AttributeTypesSupportCellEditor;
import org.gephi.desktop.datalab.utils.ArrayRenderer;
import org.gephi.desktop.datalab.utils.DefaultStringRepresentationRenderer;
import org.gephi.desktop.datalab.utils.DoubleRenderer;
import org.gephi.desktop.datalab.utils.IntervalMapRenderer;
import org.gephi.desktop.datalab.utils.TimestampMapRenderer;
import org.gephi.desktop.datalab.utils.SparkLinesRenderer;
import org.gephi.desktop.datalab.utils.IntervalSetRenderer;
import org.gephi.graph.api.AttributeUtils;
import org.gephi.graph.api.Element;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Interval;
import org.gephi.graph.api.TimeFormat;
import org.gephi.graph.api.types.IntervalMap;
import org.gephi.graph.api.types.IntervalSet;
import org.gephi.graph.api.types.TimestampMap;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.joda.time.DateTimeZone;
import org.openide.util.Lookup;

/**
 *
 * @author Mathieu Bastian
 * @author Eduardo Ramos
 */
public abstract class AbstractElementsDataTable<T extends Element> {

    protected final JXTable table;
    protected String filterPattern;
    protected List<T> selectedElements;
    protected final AttributeColumnsController attributeColumnsController;
    protected boolean refreshingTable = false;
    protected Column[] showingColumns = null;
    protected ElementsDataTableModel<T> model;
    private final IntervalSetRenderer intervalSetRenderer;
    private final IntervalMapRenderer intervalMapRenderer;
    private final TimestampMapRenderer timestampMapRenderer;
    private final List<AttributeTypesSupportCellEditor> cellEditors = new ArrayList<AttributeTypesSupportCellEditor>();
    private final List<SparkLinesRenderer> sparkLinesRenderers;
    private final ArrayRenderer arrayRenderer = new ArrayRenderer();
    private final DefaultStringRepresentationRenderer defaultStringRepresentationRenderer = new DefaultStringRepresentationRenderer();
    private final DoubleRenderer doubleRenderer = new DoubleRenderer();

    public AbstractElementsDataTable() {
        attributeColumnsController = Lookup.getDefault().lookup(AttributeColumnsController.class);
        table = new JXTable();
        table.setHighlighters(HighlighterFactory.createAlternateStriping());
        table.setColumnControlVisible(false);
        table.setSortable(true);
        table.setAutoCreateRowSorter(true);
        sparkLinesRenderers = new ArrayList<SparkLinesRenderer>();
        intervalSetRenderer = new IntervalSetRenderer();
        intervalMapRenderer = new IntervalMapRenderer();
        timestampMapRenderer = new TimestampMapRenderer();

        prepareCellEditors();
        prepareRenderers();
    }

    public abstract List<? extends ElementDataColumn<T>> getFakeDataColumns(GraphModel graphModel, DataTablesModel dataTablesModel);

    private void prepareCellEditors() {
        for (Class<?> typeClass : AttributeUtils.getSupportedTypes()) {
            //For booleans we want the default cell editor that uses a checkbox
            //For any other type, use our own cell editor that supports all attribute types parsing.
            if (!typeClass.equals(Boolean.class) && !typeClass.equals(boolean.class)) {
                AttributeTypesSupportCellEditor cellEditor = new AttributeTypesSupportCellEditor(typeClass);
                cellEditors.add(cellEditor);
                table.setDefaultEditor(typeClass, cellEditor);
            }
        }
    }

    private void prepareRenderers() {
        for (Class<?> typeClass : AttributeUtils.getSupportedTypes()) {
            TableCellRenderer typeRenderer = null;
            if (typeClass.equals(IntervalSet.class)) {
                typeRenderer = intervalSetRenderer;
            }

            boolean isNumberType = AttributeUtils.isNumberType(typeClass);

            boolean isDynamic = AttributeUtils.isDynamicType(typeClass);
            boolean isArray = typeClass.isArray();
            if (isNumberType) {
                if (isDynamic || isArray) {
                    SparkLinesRenderer renderer = new SparkLinesRenderer(typeClass);
                    sparkLinesRenderers.add(renderer);

                    typeRenderer = renderer;
                } else {
                    boolean isDecimalType = typeClass.equals(Double.class)
                            || typeClass.equals(double.class)
                            || typeClass.equals(Float.class)
                            || typeClass.equals(float.class);

                    if (isDecimalType) {
                        typeRenderer = doubleRenderer;
                    }
                }
            } else if (isArray) {
                typeRenderer = arrayRenderer;
            } else if (isDynamic) {
                boolean isTimestampMapType = TimestampMap.class.isAssignableFrom(typeClass);
                boolean isIntervalMapType = IntervalMap.class.isAssignableFrom(typeClass);

                if (isTimestampMapType) {
                    typeRenderer = timestampMapRenderer;
                } else if (isIntervalMapType) {
                    typeRenderer = intervalMapRenderer;
                }
            }

            if (typeRenderer == null) {
                typeRenderer = defaultStringRepresentationRenderer;
            }

            //For booleans we want the default cell renderer that uses a checkbox
            //For any other type, use our own cell renderer that shows the values with standard, not locale specific toString methods
            if (!typeClass.equals(Boolean.class) && !typeClass.equals(boolean.class)) {
                table.setDefaultRenderer(typeClass, typeRenderer);
            }
        }
    }

    public JXTable getTable() {
        return table;
    }

    public boolean setFilterPattern(String regularExpr, int column) {
        try {
            if (Objects.equals(filterPattern, regularExpr)) {
                return true;
            }
            filterPattern = regularExpr;

            if (regularExpr == null || regularExpr.trim().isEmpty()) {
                table.setRowFilter(null);
            } else {
                if (!regularExpr.startsWith("(?i)")) {   //CASE_INSENSITIVE
                    regularExpr = "(?i)" + regularExpr;
                }
                RowFilter rowFilter = RowFilter.regexFilter(regularExpr, column);
                table.setRowFilter(rowFilter);
            }
        } catch (PatternSyntaxException e) {
            return false;
        }

        return true;
    }

    public String getPattern() {
        return filterPattern;
    }

    public void refreshModel(T[] elements, Column[] cols, GraphModel graphModel, DataTablesModel dataTablesModel) {
        showingColumns = cols;
        Interval timeBounds = graphModel.getTimeBounds();
        double min = timeBounds != null ? timeBounds.getLow() : Double.NEGATIVE_INFINITY;
        double max = timeBounds != null ? timeBounds.getHigh() : Double.POSITIVE_INFINITY;
        TimeFormat currentTimeFormat = graphModel.getTimeFormat();
        DateTimeZone currentTimeZone = graphModel.getTimeZone();

        for (SparkLinesRenderer sparkLinesRenderer : sparkLinesRenderers) {
            sparkLinesRenderer.setTimeFormat(currentTimeFormat);
            sparkLinesRenderer.setTimeZone(currentTimeZone);
        }

        intervalSetRenderer.setTimeFormat(currentTimeFormat);
        intervalSetRenderer.setTimeZone(currentTimeZone);
        intervalSetRenderer.setMinMax(min, max);
        intervalMapRenderer.setTimeFormat(currentTimeFormat);
        intervalMapRenderer.setTimeZone(currentTimeZone);
        timestampMapRenderer.setTimeZone(currentTimeZone);

        for (AttributeTypesSupportCellEditor cellEditor : cellEditors) {
            cellEditor.setTimeFormat(currentTimeFormat);
            cellEditor.setTimeZone(currentTimeZone);
        }

        refreshingTable = true;
        if (selectedElements == null) {
            selectedElements = getElementsFromSelectedRows();
        }
        ArrayList<ElementDataColumn<T>> columns = new ArrayList<ElementDataColumn<T>>();
        columns.addAll(getFakeDataColumns(graphModel, dataTablesModel));

        for (Column c : cols) {
            columns.add(new AttributeDataColumn<T>(attributeColumnsController, c));
        }

        if (model == null) {
            model = new ElementsDataTableModel<T>(elements, columns.toArray(new ElementDataColumn[0]));
            table.setModel(model);
        } else {
            model.configure(elements, columns.toArray(new ElementDataColumn[0]));
        }

        setElementsSelection(selectedElements);//Keep row selection before refreshing.
        selectedElements = null;
        refreshingTable = false;
    }

    public boolean isRefreshingTable() {
        return refreshingTable;
    }

    /**
     *
     * @param columnIndex View index, not model index
     * @return Column or null if it's a fake column
     */
    public Column getColumnAtIndex(int columnIndex) {
        int realColumnIndex = table.convertColumnIndexToModel(columnIndex);
        return model.getColumnAtIndex(realColumnIndex);
    }

    public void setElementsSelection(List<T> elements) {
        this.selectedElements = elements;//Keep this selection request to be able to do it if the table is first refreshed later.
        HashSet<T> elementsSet = new HashSet<T>();
        elementsSet.addAll(elements);
        table.clearSelection();
        for (int i = 0; i < table.getRowCount(); i++) {
            if (elementsSet.contains(getElementFromRow(i))) {
                table.addRowSelectionInterval(i, i);
            }
        }
    }

    public void setElementsSelection(T[] elements) {
        setElementsSelection(Arrays.asList(elements));
    }

    public void scrollToFirstElementSelected() {
        int row = table.getSelectedRow();
        if (row != -1) {
            Rectangle rect = table.getCellRect(row, 0, true);
            table.scrollRectToVisible(rect);
        }
    }

    public boolean hasData() {
        return table.getRowCount() > 0;
    }

    public boolean isDrawSparklines() {
        return sparkLinesRenderers.get(0).isDrawGraphics();
    }

    public void setDrawSparklines(boolean drawSparklines) {
        for (SparkLinesRenderer sparkLinesRenderer : sparkLinesRenderers) {
            sparkLinesRenderer.setDrawGraphics(drawSparklines);
        }
    }

    public boolean isDrawTimeIntervalGraphics() {
        return intervalSetRenderer.isDrawGraphics();
    }

    public void setDrawTimeIntervalGraphics(boolean drawTimeIntervalGraphics) {
        intervalSetRenderer.setDrawGraphics(drawTimeIntervalGraphics);
    }

    public T getElementFromRow(int row) {
        return ((ElementsDataTableModel<T>) table.getModel()).getElementAtRow(table.convertRowIndexToModel(row));
    }

    public List<T> getElementsFromSelectedRows() {
        int[] selectedRows = table.getSelectedRows();
        List<T> elements = new ArrayList<T>();

        for (int i = 0; i < selectedRows.length; i++) {
            elements.add(getElementFromRow(selectedRows[i]));
        }

        return elements;
    }
}
