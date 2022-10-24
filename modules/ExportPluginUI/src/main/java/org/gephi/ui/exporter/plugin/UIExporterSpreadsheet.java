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

package org.gephi.ui.exporter.plugin;

import java.text.DecimalFormatSymbols;
import java.util.Arrays;
import java.util.stream.Collectors;
import javax.swing.JPanel;
import org.gephi.io.exporter.plugin.ExporterSpreadsheet;
import org.gephi.io.exporter.spi.Exporter;
import org.gephi.io.exporter.spi.ExporterUI;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 * @author Eduardo Ramos
 */
@ServiceProvider(service = ExporterUI.class)
public class UIExporterSpreadsheet implements ExporterUI {

    private final ExporterSpreadsheetSettings settings = new ExporterSpreadsheetSettings();
    private UIExporterSpreadsheetPanel panel;
    private ExporterSpreadsheet exporterSpreadsheet;

    @Override
    public void setup(Exporter exporter) {
        exporterSpreadsheet = (ExporterSpreadsheet) exporter;
        settings.load(exporterSpreadsheet);
        if (panel != null) {
            panel.setup(exporterSpreadsheet);
        }
    }

    @Override
    public void unsetup(boolean update) {
        if (update) {
            panel.unsetup(exporterSpreadsheet);
            settings.save(exporterSpreadsheet);
        }
        panel = null;
        exporterSpreadsheet = null;
    }

    @Override
    public JPanel getPanel() {
        return panel = new UIExporterSpreadsheetPanel();
    }

    @Override
    public boolean isUIForExporter(Exporter exporter) {
        return exporter instanceof ExporterSpreadsheet;
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(UIExporterSpreadsheet.class, "UIExporterSpreadsheet.name");
    }

    private static class ExporterSpreadsheetSettings extends AbstractExporterSettings {

        // Preference names
        private final static String NORMALIZE = "Spreadsheet_normalize";
        private final static String EXPORT_COLORS = "Spreadsheet_exportColors";
        private final static String EXPORT_POSITION = "Spreadsheet_exportPosition";
        private final static String EXPORT_ATTRIBUTES = "Spreadsheet_exportAttributes";
        private final static String EXPORT_SIZE = "Spreadsheet_exportSize";
        private final static String EXPORT_DYNAMICS = "Spreadsheet_exportDynamics";
        private final static String SEPARATOR = "Spreadsheet_separator";
        private final static String DECIMAL_SEPARATOR = "Spreadsheet_decimalSeparator";
        private final static String TABLE = "Spreadsheet_table";
        private final static String EXCLUDED_NODE_COLUMNS = "Spreadsheet_excludedNodeColumns";
        private final static String EXCLUDED_EDGE_COLUMNS = "Spreadsheet_excludedEdgeColumns";
        // Default
        private final static ExporterSpreadsheet DEFAULT = new ExporterSpreadsheet();

        private void save(ExporterSpreadsheet exporterSpreadsheet) {
            put(NORMALIZE, exporterSpreadsheet.isNormalize());
            put(EXPORT_COLORS, exporterSpreadsheet.isExportColors());
            put(EXPORT_POSITION, exporterSpreadsheet.isExportPosition());
            put(EXPORT_SIZE, exporterSpreadsheet.isExportSize());
            put(EXPORT_ATTRIBUTES, exporterSpreadsheet.isExportAttributes());
            put(EXPORT_DYNAMICS, exporterSpreadsheet.isExportDynamic());
            put(SEPARATOR, exporterSpreadsheet.getFieldDelimiter());
            put(TABLE, exporterSpreadsheet.getTableToExport().name());
            put(DECIMAL_SEPARATOR, exporterSpreadsheet.getDecimalFormatSymbols().getDecimalSeparator());
            if (exporterSpreadsheet.getTableToExport().equals(ExporterSpreadsheet.ExportTable.NODES)) {
                put(EXCLUDED_NODE_COLUMNS, exporterSpreadsheet.getExcludedColumns().toArray(new String[0]));
            } else {
                put(EXCLUDED_EDGE_COLUMNS, exporterSpreadsheet.getExcludedColumns().toArray(new String[0]));
            }
        }

        private void load(ExporterSpreadsheet exporterSpreadsheet) {
            exporterSpreadsheet.setNormalize(get(NORMALIZE, DEFAULT.isNormalize()));
            exporterSpreadsheet.setExportColors(get(EXPORT_COLORS, DEFAULT.isExportColors()));
            exporterSpreadsheet.setExportAttributes(get(EXPORT_ATTRIBUTES, DEFAULT.isExportAttributes()));
            exporterSpreadsheet.setExportPosition(get(EXPORT_POSITION, DEFAULT.isExportPosition()));
            exporterSpreadsheet.setExportSize(get(EXPORT_SIZE, DEFAULT.isExportSize()));
            exporterSpreadsheet.setExportDynamic(get(EXPORT_DYNAMICS, DEFAULT.isExportDynamic()));
            exporterSpreadsheet.setFieldDelimiter(get(SEPARATOR, DEFAULT.getFieldDelimiter()));
            exporterSpreadsheet.setTableToExport(
                ExporterSpreadsheet.ExportTable.valueOf(get(TABLE, DEFAULT.getTableToExport().name())));

            DecimalFormatSymbols dfs = exporterSpreadsheet.getDecimalFormatSymbols();
            dfs.setDecimalSeparator(get(DECIMAL_SEPARATOR, DEFAULT.getDecimalFormatSymbols().getDecimalSeparator()));

            if (exporterSpreadsheet.getTableToExport().equals(ExporterSpreadsheet.ExportTable.NODES)) {
                exporterSpreadsheet.setExcludedColumns(
                    Arrays.stream(get(EXCLUDED_NODE_COLUMNS, DEFAULT.getExcludedColumns().toArray(new String[0])))
                        .collect(
                            Collectors.toSet()));
            } else {
                exporterSpreadsheet.setExcludedColumns(
                    Arrays.stream(get(EXCLUDED_EDGE_COLUMNS, DEFAULT.getExcludedColumns().toArray(new String[0])))
                        .collect(
                            Collectors.toSet()));
            }
        }
    }
}
