/*
Copyright 2008-2017 Gephi
Authors : Eduardo Ramos <eduardo.ramos@gephi.org>
Website : http://www.gephi.org

This file is part of Gephi.

DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

Copyright 2017 Gephi Consortium. All rights reserved.

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

Portions Copyrighted 2017 Gephi Consortium.
 */
package org.gephi.ui.importer.plugin.spreadsheet.wizard;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import org.gephi.io.importer.plugin.file.spreadsheet.AbstractImporterSpreadsheet;
import org.gephi.io.importer.plugin.file.spreadsheet.process.SpreadsheetGeneralConfiguration;
import org.gephi.io.importer.plugin.file.spreadsheet.sheet.SheetParser;
import org.gephi.io.importer.plugin.file.spreadsheet.sheet.SheetRow;

/**
 *
 * @author Eduardo Ramos
 */
public abstract class AbstractWizardVisualPanel1 extends javax.swing.JPanel {

    protected static final int MAX_ROWS_PREVIEW = 25;

    private final AbstractImporterSpreadsheet importer;
    protected int columnCount = 0;
    protected boolean hasSourceNodeColumn = false;
    protected boolean hasTargetNodeColumn = false;
    protected boolean hasRowsMissingSourcesOrTargets = false;

    public AbstractWizardVisualPanel1(AbstractImporterSpreadsheet importer) {
        this.importer = importer;
    }

    public void refreshPreviewTable() {
        try (SheetParser parser = importer.createParser()) {
            Map<String, Integer> headerMap = parser.getHeaderMap();
            final String[] headers = headerMap.keySet().toArray(new String[0]);

            columnCount = headers.length;

            hasSourceNodeColumn = false;
            hasTargetNodeColumn = false;
            int sourceColumnIndex = 0;
            int targetColumnIndex = 0;

            for (String header : headers) {
                if (header.equalsIgnoreCase("source")) {
                    hasSourceNodeColumn = true;
                    sourceColumnIndex = headerMap.get(header);
                }
                if (header.equalsIgnoreCase("target")) {
                    hasTargetNodeColumn = true;
                    targetColumnIndex = headerMap.get(header);
                }
            }

            ArrayList<String[]> records = new ArrayList<>();
            hasRowsMissingSourcesOrTargets = false;
            final SpreadsheetGeneralConfiguration.Mode mode = getSelectedMode();
            int maxRowSize = 0;
            String[] currentRecord;

            Iterator<SheetRow> iterator = parser.iterator();

            int count = 0;
            while (iterator.hasNext() && count < MAX_ROWS_PREVIEW) {
                count++;

                final SheetRow row = iterator.next();
                final int rowSize = row.size();

                maxRowSize = Math.max(maxRowSize, row.size());

                currentRecord = new String[rowSize];
                for (int i = 0; i < rowSize; i++) {
                    currentRecord[i] = row.get(i);
                }

                // Search for missing source or target columns for edges table
                if (mode == SpreadsheetGeneralConfiguration.Mode.EDGES_TABLE) {
                    if (rowSize <= sourceColumnIndex || rowSize <= targetColumnIndex || currentRecord[sourceColumnIndex] == null || currentRecord[targetColumnIndex] == null) {
                        hasRowsMissingSourcesOrTargets = true;
                    }
                }

                records.add(currentRecord);
            }

            final String[] columnNames = headers;
            final String[][] values = records.toArray(new String[0][]);
            final int rowSize = maxRowSize;

            final JTable table = getPreviewTable();
            table.setModel(new TableModel() {

                @Override
                public int getRowCount() {
                    return values.length;
                }

                @Override
                public int getColumnCount() {
                    return rowSize;
                }

                @Override
                public String getColumnName(int columnIndex) {
                    if (columnIndex > columnNames.length - 1) {
                        return null;
                    }
                    return columnNames[columnIndex];
                }

                @Override
                public Class<?> getColumnClass(int columnIndex) {
                    return String.class;
                }

                @Override
                public boolean isCellEditable(int rowIndex, int columnIndex) {
                    return false;
                }

                @Override
                public Object getValueAt(int rowIndex, int columnIndex) {
                    if (values[rowIndex].length > columnIndex) {
                        return values[rowIndex][columnIndex];
                    } else {
                        return null;
                    }
                }

                @Override
                public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
                }

                @Override
                public void addTableModelListener(TableModelListener l) {
                }

                @Override
                public void removeTableModelListener(TableModelListener l) {
                }
            });

            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    boolean needsHeader = headers.length > 0;
                    getPreviewTableScrollPane().setColumnHeaderView(needsHeader ? table.getTableHeader() : null);
                }
            });
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    protected abstract JTable getPreviewTable();

    protected abstract JScrollPane getPreviewTableScrollPane();

    protected abstract SpreadsheetGeneralConfiguration.Mode getSelectedMode();

}
