/*
Copyright 2008 WebAtlas
Authors : Mathieu Bastian, Mathieu Jacomy, Julian Bilcke
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gephi.ui.exporter.plugin;

import javax.swing.JPanel;
import org.gephi.io.exporter.spi.Exporter;
import org.gephi.io.exporter.plugin.ExporterGEXF;
import org.gephi.io.exporter.spi.ExporterUI;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Mathieu Bastian
 */
@ServiceProvider(service = ExporterUI.class)
public class UIExporterGEXF implements ExporterUI {

    private UIExporterGEXFPanel panel;
    private ExporterGEXF exporterGEXF;
    private ExporterGEXFSettings settings =  new ExporterGEXFSettings();

    public void setup(Exporter exporter) {
        exporterGEXF = (ExporterGEXF) exporter;
        settings.load(exporterGEXF);
        panel.setup(exporterGEXF);
    }

    public void unsetup(boolean update) {
        if (update) {
            panel.unsetup(exporterGEXF);
            settings.save(exporterGEXF);
        }
        panel = null;
        exporterGEXF = null;
    }

    public JPanel getPanel() {
        panel = new UIExporterGEXFPanel();
        return panel;
    }

    public boolean isUIForExporter(Exporter exporter) {
        return exporter instanceof ExporterGEXF;
    }

    public String getDisplayName() {
        return NbBundle.getMessage(UIExporterGEXF.class, "UIExporterGEXF.name");
    }

    private static class ExporterGEXFSettings {

        private boolean normalize = false;
        private boolean exportColors = true;
        private boolean exportPosition = true;
        private boolean exportSize = true;
        private boolean exportAttributes = true;

        private void save(ExporterGEXF exporterGEXF) {
            this.normalize = exporterGEXF.isNormalize();
            this.exportColors = exporterGEXF.isExportColors();
            this.exportPosition = exporterGEXF.isExportPosition();
            this.exportSize = exporterGEXF.isExportSize();
            this.exportAttributes = exporterGEXF.isExportAttributes();
        }

        private void load(ExporterGEXF exporterGEXF) {
            exporterGEXF.setNormalize(normalize);
            exporterGEXF.setExportColors(exportColors);
            exporterGEXF.setExportAttributes(exportAttributes);
            exporterGEXF.setExportPosition(exportPosition);
            exporterGEXF.setExportSize(exportSize);
        }
    }
}
