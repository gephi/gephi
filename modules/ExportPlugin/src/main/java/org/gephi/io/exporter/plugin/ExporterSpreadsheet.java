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
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.gephi.graph.api.AttributeUtils;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Element;
import org.gephi.graph.api.ElementIterable;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
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
 * @author Eduardo Ramos
 */
public class ExporterSpreadsheet implements GraphExporter, CharacterExporter, LongTask {

    //Settings
    private boolean exportVisible;
    private ExportTable tableToExport = ExportTable.EDGES;
    private char fieldDelimiter = ',';
    private Set<String> excludedColumns = new HashSet<>();
    //Architecture
    private Workspace workspace;
    private Writer writer;
    private boolean cancel = false;
    private ProgressTicket progressTicket;

    //Settings
    private DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols(Locale.ENGLISH);
    /**
     * Formatter for limiting precision to 6 decimals, avoiding precision errors (epsilon).
     */
    private DecimalFormat numberFormat = new DecimalFormat("0.######");
    private boolean normalize = false;
    private boolean exportColors = false;
    private boolean exportAttributes = true;
    private boolean exportPosition = false;
    private boolean exportSize = false;
    private boolean exportDynamic = true;

    @Override
    public boolean execute() {
        GraphModel graphModel = workspace.getLookup().lookup(GraphModel.class);
        Graph graph = exportVisible ? graphModel.getGraphVisible() : graphModel.getGraph();

        Progress.start(progressTicket);
        graph.readLock();

        try {
            exportData(graph);
        } catch (Exception e) {
            Logger.getLogger(ExporterSpreadsheet.class.getName()).log(Level.SEVERE, null, e);
        } finally {
            graph.readUnlock();
            Progress.finish(progressTicket);
        }

        return !cancel;
    }

    private void exportData(Graph graph) throws Exception {
        decimalFormatSymbols.setInfinity("Infinity");
        numberFormat.setDecimalFormatSymbols(decimalFormatSymbols);

        NormalizationHelper normalization = NormalizationHelper.build(normalize, graph);

        final CSVFormat format = CSVFormat.Builder.create(CSVFormat.DEFAULT)
            .setDelimiter(fieldDelimiter).build();

        try (CSVPrinter csvWriter = new CSVPrinter(writer, format)) {
            boolean isEdgeTable = tableToExport != ExportTable.NODES;
            Table table = isEdgeTable ? graph.getModel().getEdgeTable() : graph.getModel().getNodeTable();

            ElementIterable<? extends Element> rows;

            Object[] edgeLabels = graph.getModel().getEdgeTypeLabels(false);
            boolean includeEdgeKindColumn = edgeLabels.length > 1;


            TimeFormat timeFormat = graph.getModel().getTimeFormat();
            DateTimeZone timeZone = graph.getModel().getTimeZone();

            //Columns to export
            Collection<Column> columns = getExportableColumns(graph.getModel(), table).stream()
                .filter(c -> !excludedColumns.contains(c.getId()))
                .collect(Collectors.toCollection(ArrayList::new));

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

            if (!isEdgeTable && exportPosition) {
                csvWriter.print("X");
                csvWriter.print("Y");
                if (normalization.minZ != 0 || normalization.maxZ != 0) {
                    csvWriter.print("Z");
                }
            }
            if (!isEdgeTable && exportSize) {
                csvWriter.print("Size");
            }
            if (exportColors) {
                csvWriter.print("Color");
            }

            csvWriter.println();

            //Write rows:
            if (isEdgeTable) {
                rows = graph.getEdges();
                Progress.switchToDeterminate(progressTicket, graph.getEdgeCount());
            } else {
                rows = graph.getNodes();
                Progress.switchToDeterminate(progressTicket, graph.getNodeCount());
            }

            for (Element row : rows) {
                if (isEdgeTable) {
                    Edge edge = (Edge) row;

                    csvWriter.print(edge.getSource().getId());
                    csvWriter.print(edge.getTarget().getId());
                    csvWriter.print(edge.isDirected() ? "Directed" : "Undirected");
                    if (includeEdgeKindColumn) {
                        Object edgeTypeLabel = edge.getTypeLabel();
                        if (edgeTypeLabel != null) {
                            csvWriter.print(edgeTypeLabel.toString());
                        } else {
                            csvWriter.print("");
                        }
                    }
                }

                for (Column column : columns) {
                    Object value = exportDynamic ? row.getAttribute(column) : row.getAttribute(column, graph.getView());

                    String text;

                    if (value != null) {
                        if (value instanceof Number) {
                            text = numberFormat.format(value);
                        } else {
                            text = AttributeUtils.print(value, timeFormat, timeZone);
                        }
                    } else {
                        text = "";
                    }
                    csvWriter.print(text);
                }

                if (!isEdgeTable) {
                    Node node = (Node) row;
                    if (exportPosition) {
                        float x = normalization.normalizeX(node.x());
                        float y = normalization.normalizeY(node.y());
                        float z = normalization.normalizeZ(node.z());

                        csvWriter.print(numberFormat.format(x));
                        csvWriter.print(numberFormat.format(y));
                        if (normalization.minZ != 0 || normalization.maxZ != 0) {
                            csvWriter.print(numberFormat.format(z));
                        }
                    }

                    if (exportSize) {
                        float size = normalization.normalizeSize(node.size());
                        csvWriter.print(numberFormat.format(size));
                    }
                }

                if (exportColors) {
                    csvWriter.print(String.format("#%06x", row.getColor().getRGB() & 0x00FFFFFF));
                }

                csvWriter.println();

                Progress.progress(progressTicket);

                if (cancel) {
                    rows.doBreak();
                    break;
                }
            }
        }
    }

    public Collection<Column> getExportableColumns(GraphModel graphModel, Table table) {
        boolean includeTimeSet = graphModel.isDynamic();
        List<Column> columns = new ArrayList<>();
        for (Column column : table) {
            if (!(column.getId().equals("timeset") && !includeTimeSet) &&
                (exportAttributes || column.isProperty())) {
                columns.add(column);
            }
        }
        return columns;
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

    public Set<String> getExcludedColumns() {
        return excludedColumns;
    }

    public void setExcludedColumns(Set<String> excludedColumns) {
        this.excludedColumns = excludedColumns;
    }

    public ExportTable getTableToExport() {
        return tableToExport;
    }

    public void setTableToExport(ExportTable tableToExport) {
        if (tableToExport == null) {
            throw new NullPointerException("tableToExport must not be null");
        }
        this.tableToExport = tableToExport;
    }

    public void setNumberFormat(DecimalFormat numberFormat) {
        this.numberFormat = numberFormat;
    }

    public DecimalFormat getNumberFormat() {
        return numberFormat;
    }

    public void setDecimalFormatSymbols(DecimalFormatSymbols decimalFormatSymbols) {
        this.decimalFormatSymbols = decimalFormatSymbols;
    }

    public DecimalFormatSymbols getDecimalFormatSymbols() {
        return decimalFormatSymbols;
    }

    public void setExportColors(boolean exportColors) {
        this.exportColors = exportColors;
    }

    public boolean isExportColors() {
        return exportColors;
    }

    public void setExportPosition(boolean exportPosition) {
        this.exportPosition = exportPosition;
    }

    public boolean isExportPosition() {
        return exportPosition;
    }

    public void setExportSize(boolean exportSize) {
        this.exportSize = exportSize;
    }

    public void setNormalize(boolean normalize) {
        this.normalize = normalize;
    }

    public boolean isNormalize() {
        return normalize;
    }

    public boolean isExportSize() {
        return exportSize;
    }

    public void setExportDynamic(boolean exportDynamic) {
        this.exportDynamic = exportDynamic;
    }

    public boolean isExportDynamic() {
        return exportDynamic;
    }

    public void setExportAttributes(boolean exportAttributes) {
        this.exportAttributes = exportAttributes;
    }

    public boolean isExportAttributes() {
        return exportAttributes;
    }

    public enum ExportTable {
        NODES,
        EDGES
    }
}
