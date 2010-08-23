/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
*/
package org.gephi.ui.exporter.plugin;

import javax.swing.JPanel;
import org.gephi.io.exporter.spi.Exporter;
import org.gephi.io.exporter.plugin.ExporterGDF;
import org.gephi.io.exporter.spi.ExporterUI;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Mathieu Bastian
 */
@ServiceProvider(service = ExporterUI.class)
public class UIExporterGDF implements ExporterUI {

    private UIExporterGDFPanel panel;
    private ExporterGDF exporterGDF;
    private ExporterGDFSettings settings = new ExporterGDFSettings();

    public void setup(Exporter exporter) {
        exporterGDF = (ExporterGDF) exporter;
        settings.load(exporterGDF);
        panel.setup(exporterGDF);
    }

    public void unsetup(boolean update) {
        if (update) {
            panel.unsetup(exporterGDF);
            settings.save(exporterGDF);
        }
        panel = null;
        exporterGDF = null;
    }

    public JPanel getPanel() {
        panel = new UIExporterGDFPanel();
        return panel;
    }

    public boolean isUIForExporter(Exporter exporter) {
        return exporter instanceof ExporterGDF;
    }

    public String getDisplayName() {
        return NbBundle.getMessage(UIExporterGDF.class, "UIExporterGDF.name");
    }

    private static class ExporterGDFSettings {

        private boolean normalize = false;
        private boolean simpleQuotes = false;
        private boolean useQuotes = true;
        private boolean exportColors = true;
        private boolean exportPosition = true;
        private boolean exportAttributes = true;
        private boolean exportVisibility = false;

        private void save(ExporterGDF exporterGDF) {
            this.normalize = exporterGDF.isNormalize();
            this.simpleQuotes = exporterGDF.isSimpleQuotes();
            this.useQuotes = exporterGDF.isUseQuotes();
            this.exportColors = exporterGDF.isExportColors();
            this.exportPosition = exporterGDF.isExportPosition();
            this.exportAttributes = exporterGDF.isExportAttributes();
            this.exportVisibility = exporterGDF.isExportVisibility();
        }

        private void load(ExporterGDF exporterGDF) {
            exporterGDF.setNormalize(normalize);
            exporterGDF.setSimpleQuotes(simpleQuotes);
            exporterGDF.setUseQuotes(useQuotes);
            exporterGDF.setExportColors(exportColors);
            exporterGDF.setExportAttributes(exportAttributes);
            exporterGDF.setExportPosition(exportPosition);
            exporterGDF.setExportVisibility(exportVisibility);
        }
    }
}
