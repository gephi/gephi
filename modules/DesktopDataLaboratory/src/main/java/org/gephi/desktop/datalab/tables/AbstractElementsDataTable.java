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

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.regex.PatternSyntaxException;
import javax.swing.RowFilter;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import org.gephi.datalab.api.AttributeColumnsController;
import org.gephi.desktop.datalab.*;
import org.gephi.desktop.datalab.tables.celleditors.AttributeTypesSupportCellEditor;
import org.gephi.desktop.datalab.tables.columns.AttributeDataColumn;
import org.gephi.desktop.datalab.tables.columns.ElementDataColumn;
import org.gephi.desktop.datalab.utils.GraphModelProvider;
import org.gephi.desktop.datalab.utils.componentproviders.ArraySparklinesGraphicsComponentProvider;
import org.gephi.desktop.datalab.utils.componentproviders.IntervalMapSparklinesGraphicsComponentProvider;
import org.gephi.desktop.datalab.utils.componentproviders.IntervalSetGraphicsComponentProvider;
import org.gephi.desktop.datalab.utils.componentproviders.TimestampMapSparklinesGraphicsComponentProvider;
import org.gephi.desktop.datalab.utils.componentproviders.TimestampSetGraphicsComponentProvider;
import org.gephi.desktop.datalab.utils.stringconverters.ArrayStringConverter;
import org.gephi.desktop.datalab.utils.stringconverters.DefaultStringRepresentationConverter;
import org.gephi.desktop.datalab.utils.stringconverters.DoubleStringConverter;
import org.gephi.desktop.datalab.utils.stringconverters.TimeMapStringConverter;
import org.gephi.desktop.datalab.utils.stringconverters.TimeSetStringConverter;
import org.gephi.graph.api.AttributeUtils;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Element;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Interval;
import org.gephi.graph.api.types.IntervalMap;
import org.gephi.graph.api.types.IntervalSet;
import org.gephi.graph.api.types.TimestampMap;
import org.gephi.graph.api.types.TimestampSet;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Mathieu Bastian
 * @author Eduardo Ramos
 */
public abstract class AbstractElementsDataTable<T extends Element> implements GraphModelProvider {

    protected final JXTable table;
    protected String filterPattern;
    protected List<T> selectedElements;
    protected final AttributeColumnsController attributeColumnsController;
    protected boolean refreshingTable = false;
    protected Column[] showingColumns = null;
    protected ElementsDataTableModel<T> model;
    protected GraphModel graphModel;

    //Renderers:
    private boolean drawTimeIntervalGraphics = false;
    private boolean drawSparklines = false;
    private final DefaultTableRenderer arrayRenderer = new DefaultTableRenderer(new ArrayStringConverter());
    private final DefaultTableRenderer defaultStringRepresentationRenderer = new DefaultTableRenderer(new DefaultStringRepresentationConverter());
    private final DefaultTableRenderer timeSetRenderer;
    private final DefaultTableRenderer timeMapRenderer;
    private final DefaultTableRenderer doubleRenderer = new DefaultTableRenderer(new DoubleStringConverter());

    //Graphics renderers:
    private final IntervalSetGraphicsComponentProvider intervalSetGraphicsComponentProvider;
    private final TimestampSetGraphicsComponentProvider timestampSetGraphicsComponentProvider;
    private final DefaultTableRenderer intervalSetGraphicsRenderer;
    private final DefaultTableRenderer timestampSetGraphicsRenderer;
    private final DefaultTableRenderer intervalMapSparklinesGraphicsRenderer;
    private final DefaultTableRenderer timestampMapSparklinesGraphicsRenderer;
    private final DefaultTableRenderer arraySparklinesGraphicsRenderer;

    public AbstractElementsDataTable() {
        attributeColumnsController = Lookup.getDefault().lookup(AttributeColumnsController.class);
        table = new JXTable();
        table.setHighlighters(HighlighterFactory.createAlternateStriping());
        table.setColumnControlVisible(false);
        table.setSortable(true);
        table.setAutoCreateRowSorter(true);

        intervalSetGraphicsComponentProvider = new IntervalSetGraphicsComponentProvider(this, table);
        timestampSetGraphicsComponentProvider = new TimestampSetGraphicsComponentProvider(this, table);

        intervalSetGraphicsRenderer = new DefaultTableRenderer(intervalSetGraphicsComponentProvider);
        timestampSetGraphicsRenderer = new DefaultTableRenderer(timestampSetGraphicsComponentProvider);

        timeSetRenderer = new DefaultTableRenderer(new TimeSetStringConverter(this));
        timeMapRenderer = new DefaultTableRenderer(new TimeMapStringConverter(this));

        intervalMapSparklinesGraphicsRenderer = new DefaultTableRenderer(new IntervalMapSparklinesGraphicsComponentProvider(this, table));
        timestampMapSparklinesGraphicsRenderer = new DefaultTableRenderer(new TimestampMapSparklinesGraphicsComponentProvider(this, table));
        arraySparklinesGraphicsRenderer = new DefaultTableRenderer(new ArraySparklinesGraphicsComponentProvider(this, table));

        prepareCellEditors();
        prepareRenderers();
    }

    public abstract List<? extends ElementDataColumn<T>> getFakeDataColumns(GraphModel graphModel, DataTablesModel dataTablesModel);

    private void prepareCellEditors() {
        for (Class<?> typeClass : AttributeUtils.getSupportedTypes()) {
            //For booleans we want the default cell editor that uses a checkbox
            //For any other type, use our own cell editor that supports all attribute types parsing.
            if (!typeClass.equals(Boolean.class) && !typeClass.equals(boolean.class)) {
                table.setDefaultEditor(typeClass, new AttributeTypesSupportCellEditor(this, typeClass));
            }
        }
    }

    private void prepareRenderers() {
        for (Class<?> typeClass : AttributeUtils.getSupportedTypes()) {
            TableCellRenderer typeRenderer = null;

            boolean isNumberType = AttributeUtils.isNumberType(typeClass);

            boolean isDynamic = AttributeUtils.isDynamicType(typeClass);
            boolean isArray = typeClass.isArray();

            if (typeClass.equals(IntervalSet.class)) {
                typeRenderer = drawTimeIntervalGraphics ? intervalSetGraphicsRenderer : timeSetRenderer;
            } else if (typeClass.equals(TimestampSet.class)) {
                typeRenderer = drawTimeIntervalGraphics ? timestampSetGraphicsRenderer : timeSetRenderer;
            } else if (drawSparklines && isNumberType && (isArray || isDynamic)) {
                if (isArray) {
                    typeRenderer = arraySparklinesGraphicsRenderer;
                } else if (IntervalMap.class.isAssignableFrom(typeClass)) {
                    typeRenderer = intervalMapSparklinesGraphicsRenderer;
                } else if (TimestampMap.class.isAssignableFrom(typeClass)) {
                    typeRenderer = timestampMapSparklinesGraphicsRenderer;
                }
            }

            if (typeRenderer == null) {
                if (isArray) {
                    typeRenderer = arrayRenderer;
                } else if (isDynamic) {
                    typeRenderer = timeMapRenderer;
                } else if (isNumberType) {
                    boolean isDecimalType = typeClass.equals(Double.class)
                            || typeClass.equals(double.class)
                            || typeClass.equals(Float.class)
                            || typeClass.equals(float.class);

                    if (isDecimalType) {
                        typeRenderer = doubleRenderer;
                    }
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
        this.graphModel = graphModel;

        showingColumns = cols;
        Interval timeBounds = graphModel.getTimeBounds();
        double min = timeBounds != null ? timeBounds.getLow() : 0;
        double max = timeBounds != null ? timeBounds.getHigh() : 0;

        refreshCellRenderersConfiguration(graphModel, min, max);

        refreshingTable = true;
        if (selectedElements == null) {
            selectedElements = getElementsFromSelectedRows();
        }
        ArrayList<ElementDataColumn<T>> columns = new ArrayList<>();
        columns.addAll(getFakeDataColumns(graphModel, dataTablesModel));

        for (Column c : cols) {
            columns.add(new AttributeDataColumn<T>(attributeColumnsController, c));
        }

        if (model == null) {
            model = new ElementsDataTableModel<>(elements, columns.toArray(new ElementDataColumn[0]));
            table.setModel(model);
        } else {
            model.configure(elements, columns.toArray(new ElementDataColumn[0]));
        }

        TableHeaderWithTooltip headerWithTooltips = new TableHeaderWithTooltip(table.getColumnModel(), columns);
        table.setTableHeader(headerWithTooltips);

        setElementsSelection(selectedElements);//Keep row selection before refreshing.
        selectedElements = null;
        refreshingTable = false;
    }

    private void refreshCellRenderersConfiguration(GraphModel graphModel, double min, double max) {
        intervalSetGraphicsComponentProvider.setMinMax(min, max);
        timestampSetGraphicsComponentProvider.setMinMax(min, max);
    }

    @Override
    public GraphModel getGraphModel() {
        return graphModel;
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
        HashSet<T> elementsSet = new HashSet<>();
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

    public void setDrawSparklines(boolean drawSparklines) {
        this.drawSparklines = drawSparklines;
        prepareRenderers();
    }

    public void setDrawTimeIntervalGraphics(boolean drawTimeIntervalGraphics) {
        this.drawTimeIntervalGraphics = drawTimeIntervalGraphics;
        prepareRenderers();
    }

    public boolean isDrawTimeIntervalGraphics() {
        return drawTimeIntervalGraphics;
    }

    public T getElementFromRow(int row) {
        return ((ElementsDataTableModel<T>) table.getModel()).getElementAtRow(table.convertRowIndexToModel(row));
    }

    public List<T> getElementsFromSelectedRows() {
        int[] selectedRows = table.getSelectedRows();
        List<T> elements = new ArrayList<>();

        for (int i = 0; i < selectedRows.length; i++) {
            elements.add(getElementFromRow(selectedRows[i]));
        }

        return elements;
    }

    private class TableHeaderWithTooltip extends JTableHeader {

        private final List<ElementDataColumn<T>> columns;

        public TableHeaderWithTooltip(TableColumnModel columnModel, List<ElementDataColumn<T>> columns) {
            super(columnModel);
            this.columns = columns;
        }

        @Override
        public String getToolTipText(MouseEvent e) {
            Point p = e.getPoint();
            int index = columnModel.getColumnIndexAtX(p.x);
            int realIndex = columnModel.getColumn(index).getModelIndex();

            if (realIndex < columns.size() && columns.get(realIndex).getColumn() != null) {
                String id = columns.get(realIndex).getColumn().getId();

                return NbBundle.getMessage(AbstractElementsDataTable.class, "AbstractElementsDataTable.column.tooltip", id);
            } else {
                return null;
            }
        }
    }
}
