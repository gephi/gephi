/*
Copyright 2008-2017 Gephi
Authors : Eduardo Ramos <eduardo.ramos@gephi.org>
Website : http://www.gephi.org

This file is part of Gephi.

DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

Copyright 2016 Gephi Consortium. All rights reserved.

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
package org.gephi.io.exporter.plugin;

import java.io.Writer;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.gephi.graph.api.AttributeUtils;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Element;
import org.gephi.graph.api.ElementIterable;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Table;
import org.gephi.graph.api.TimeFormat;
import org.gephi.io.exporter.spi.CharacterExporter;
import org.gephi.io.exporter.spi.GraphExporter;
import org.gephi.project.api.Workspace;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.Progress;
import org.gephi.utils.progress.ProgressTicket;
import org.joda.time.DateTimeZone;

/**
 *
 * @author Eduardo Ramos
 */
public class ExporterSpreadsheet implements GraphExporter, CharacterExporter, LongTask {

    /**
     * Formatter for limiting precision to 6 decimals, avoiding precision errors (epsilon).
     */
    private static final DecimalFormat NUMBER_FORMAT = new DecimalFormat("0.######");
    static {
        DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance(Locale.ENGLISH);
        symbols.setInfinity("Infinity");
        NUMBER_FORMAT.setDecimalFormatSymbols(symbols);
    }
    
    public enum ExportTable {
        NODES,
        EDGES;
    }

    //Settings
    private boolean exportVisible;
    private ExportTable tableToExport = null;//If null => edges is the default
    private char fieldDelimiter = ',';
    private LinkedHashSet<String> columnIdsToExport = null;

    //Architecture
    private Workspace workspace;
    private Writer writer;
    private boolean cancel = false;
    private ProgressTicket progressTicket;

    @Override
    public boolean execute() {
        GraphModel graphModel = workspace.getLookup().lookup(GraphModel.class);
        Graph graph = exportVisible ? graphModel.getGraphVisible() : graphModel.getGraph();

        graph.readLock();
        try {
            exportData(graph);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            graph.readUnlock();
            Progress.finish(progressTicket);
        }

        return !cancel;
    }

    private void exportData(Graph graph) throws Exception {
        final CSVFormat format = CSVFormat.DEFAULT
                .withDelimiter(fieldDelimiter);

        try (CSVPrinter csvWriter = new CSVPrinter(writer, format)) {
            boolean isEdgeTable = tableToExport != ExportTable.NODES;
            Table table = isEdgeTable ? graph.getModel().getEdgeTable() : graph.getModel().getNodeTable();

            ElementIterable<? extends Element> rows;

            Object[] edgeLabels = graph.getModel().getEdgeTypeLabels();
            boolean includeEdgeKindColumn = false;
            for (Object edgeLabel : edgeLabels) {
                if (edgeLabel != null && !edgeLabel.toString().isEmpty()) {
                    includeEdgeKindColumn = true;
                }
            }

            TimeFormat timeFormat = graph.getModel().getTimeFormat();
            DateTimeZone timeZone = graph.getModel().getTimeZone();
            List<Column> columns = new ArrayList<>();

            if (columnIdsToExport != null) {
                for (String columnId : columnIdsToExport) {
                    Column column = table.getColumn(columnId);
                    if (column != null) {
                        columns.add(column);
                    }
                }
            } else {
                for (Column column : table) {
                    columns.add(column);
                }
            }

            //Write column headers:
            if (isEdgeTable) {
                csvWriter.print("Source");
                csvWriter.print("Target");
                csvWriter.print("Type");
                if (includeEdgeKindColumn) {
                    csvWriter.print("Kind");
                }
            }

            for (Column column : columns) {
                //Use the title only if it's the same as the id (case insensitive):
                String columnId = column.getId();
                String columnTitle = column.getTitle();
                String columnHeader = columnId.equalsIgnoreCase(columnTitle) ? columnTitle : columnId;
                csvWriter.print(columnHeader);
            }
            csvWriter.println();

            //Write rows:
            if (isEdgeTable) {
                rows = graph.getEdges();
            } else {
                rows = graph.getNodes();
            }

            for (Element row : rows) {
                if (isEdgeTable) {
                    Edge edge = (Edge) row;

                    csvWriter.print(edge.getSource().getId());
                    csvWriter.print(edge.getTarget().getId());
                    csvWriter.print(edge.isDirected() ? "Directed" : "Undirected");
                    if (includeEdgeKindColumn) {
                        csvWriter.print(edge.getTypeLabel().toString());
                    }
                }

                for (Column column : columns) {
                    Object value = row.getAttribute(column);
                    String text;

                    if (value != null) {
                        if (value instanceof Number) {
                            text = NUMBER_FORMAT.format(value);
                        } else {
                            text = AttributeUtils.print(value, timeFormat, timeZone);
                        }
                    } else {
                        text = "";
                    }
                    csvWriter.print(text);
                }

                csvWriter.println();
            }
        }
    }

    @Override
    public boolean cancel() {
        return cancel = true;
    }

    @Override
    public void setProgressTicket(ProgressTicket progressTicket) {
        this.progressTicket = progressTicket;
    }

    @Override
    public boolean isExportVisible() {
        return exportVisible;
    }

    @Override
    public void setExportVisible(boolean exportVisible) {
        this.exportVisible = exportVisible;
    }

    @Override
    public void setWriter(Writer writer) {
        this.writer = writer;
    }

    @Override
    public Workspace getWorkspace() {
        return workspace;
    }

    @Override
    public void setWorkspace(Workspace workspace) {
        this.workspace = workspace;
    }

    public char getFieldDelimiter() {
        return fieldDelimiter;
    }

    public void setFieldDelimiter(char fieldDelimiter) {
        this.fieldDelimiter = fieldDelimiter;
    }

    public LinkedHashSet<String> getColumnIdsToExport() {
        return new LinkedHashSet<>(columnIdsToExport);
    }

    public void setColumnIdsToExport(LinkedHashSet<String> columnIdsToExport) {
        this.columnIdsToExport = columnIdsToExport != null ? new LinkedHashSet<>(columnIdsToExport) : null;
    }

    public ExportTable getTableToExport() {
        return tableToExport;
    }

    public void setTableToExport(ExportTable tableToExport) {
        this.tableToExport = tableToExport;
    }
}
