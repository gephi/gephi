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
import java.util.regex.PatternSyntaxException;
import javax.swing.RowFilter;
import org.gephi.graph.api.Column;
import org.gephi.datalab.api.AttributeColumnsController;
import org.gephi.desktop.datalab.tables.celleditors.AttributeTypesSupportCellEditor;
import org.gephi.desktop.datalab.utils.SparkLinesRenderer;
import org.gephi.desktop.datalab.utils.TimeIntervalsRenderer;
import org.gephi.graph.api.AttributeUtils;
import org.gephi.graph.api.Element;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Interval;
import org.gephi.graph.api.TimeFormat;
import org.gephi.graph.api.types.IntervalSet;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.openide.util.Lookup;

/**
 *
 * @author Mathieu Bastian
 * @author Eduardo Ramos
 */
public abstract class AbstractElementsDataTable<T extends Element> {

    protected final JXTable table;
    protected RowFilter rowFilter;
    protected List<T> selectedElements;
    protected final AttributeColumnsController attributeColumnsController;
    protected boolean refreshingTable = false;
    protected Column[] showingColumns = null;
    protected ElementsDataTableModel<T> model;
    private final TimeIntervalsRenderer timeIntervalsRenderer;
    private final List<AttributeTypesSupportCellEditor> cellEditors = new ArrayList<AttributeTypesSupportCellEditor>();
    private final SparkLinesRenderer sparkLinesRenderer;

    public AbstractElementsDataTable() {
        attributeColumnsController = Lookup.getDefault().lookup(AttributeColumnsController.class);
        table = new JXTable();
        table.setHighlighters(HighlighterFactory.createAlternateStriping());
        table.setColumnControlVisible(false);
        table.setSortable(true);
        table.setAutoCreateRowSorter(true);
        table.setRowFilter(rowFilter);
        sparkLinesRenderer = new SparkLinesRenderer();
        timeIntervalsRenderer = new TimeIntervalsRenderer();
        
        prepareCellEditors();
        prepareRenderers();
    }

    public abstract List<? extends ElementDataColumn<T>> getFakeDataColumns();

    private void prepareCellEditors() {
        for (Class<?> typeClass : AttributeUtils.getSupportedTypes()) {
            if (AttributeUtils.isDynamicType(typeClass)) {
                AttributeTypesSupportCellEditor cellEditor = new AttributeTypesSupportCellEditor(typeClass);
                cellEditors.add(cellEditor);
                table.setDefaultEditor(typeClass, cellEditor);
            }
        }
    }

    private void prepareRenderers() {
        for (Class<?> typeClass : AttributeUtils.getSupportedTypes()) {
            boolean isDynamicNumber = AttributeUtils.isDynamicType(typeClass) && AttributeUtils.isNumberType(typeClass);
            boolean isArrayNumber = Number[].class.isAssignableFrom(typeClass);
            
            if (isDynamicNumber || isArrayNumber) {
                table.setDefaultRenderer(typeClass, sparkLinesRenderer);
            }
        }

        table.setDefaultRenderer(IntervalSet.class, timeIntervalsRenderer);
    }

    public JXTable getTable() {
        return table;
    }

    public boolean setPattern(String regularExpr, int column) {
        try {
            if (!regularExpr.startsWith("(?i)")) {   //CASE_INSENSITIVE
                regularExpr = "(?i)" + regularExpr;
            }
            rowFilter = RowFilter.regexFilter(regularExpr, column);
            table.setRowFilter(rowFilter);
        } catch (PatternSyntaxException e) {
            return false;
        }
        return true;
    }

    public void refreshModel(T[] elements, Column[] cols, GraphModel graphModel, DataTablesModel dataTablesModel) {
        showingColumns = cols;
        Interval timeBounds = null;
        try {
            timeBounds = graphModel.getTimeBounds();
        } catch (Exception e) {
            //Current graphstore does not implement this for intervals... (TODO)
        }
        double min = timeBounds != null ? timeBounds.getLow() : Double.NEGATIVE_INFINITY;
        double max = timeBounds != null ? timeBounds.getHigh() : Double.POSITIVE_INFINITY;
        TimeFormat currentTimeFormat = graphModel.getTimeFormat();
        
        sparkLinesRenderer.setTimeFormat(currentTimeFormat);
        
        timeIntervalsRenderer.setTimeFormat(currentTimeFormat);
        timeIntervalsRenderer.setMinMax(min, max);
        
        for (AttributeTypesSupportCellEditor cellEditor : cellEditors) {
            cellEditor.setTimeFormat(currentTimeFormat);
        }
        
        refreshingTable = true;
        if (selectedElements == null) {
            selectedElements = getElementsFromSelectedRows();
        }
        ArrayList<ElementDataColumn<T>> columns = new ArrayList<ElementDataColumn<T>>();
        columns.addAll(getFakeDataColumns());

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
     * @return
     */
    public Column getColumnAtIndex(int columnIndex) {
        int realColumnIndex = table.convertColumnIndexToModel(columnIndex) - getFakeDataColumns().size();//Get real attribute column index not counting fake columns.
        if (realColumnIndex >= 0 && realColumnIndex < showingColumns.length) {
            return showingColumns[realColumnIndex];
        } else {
            return null;
        }
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
        return sparkLinesRenderer.isDrawGraphics();
    }

    public void setDrawSparklines(boolean drawSparklines) {
        sparkLinesRenderer.setDrawGraphics(drawSparklines);
    }

    public boolean isDrawTimeIntervalGraphics() {
        return timeIntervalsRenderer.isDrawGraphics();
    }

    public void setDrawTimeIntervalGraphics(boolean drawTimeIntervalGraphics) {
        timeIntervalsRenderer.setDrawGraphics(drawTimeIntervalGraphics);
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
