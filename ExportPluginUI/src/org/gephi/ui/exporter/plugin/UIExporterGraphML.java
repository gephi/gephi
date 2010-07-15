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
import org.gephi.io.exporter.plugin.ExporterGraphML;
import org.gephi.io.exporter.spi.Exporter;
import org.gephi.io.exporter.spi.ExporterUI;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Mathieu Bastian
 */
@ServiceProvider(service = ExporterUI.class)
public class UIExporterGraphML implements ExporterUI {

    private UIExporterGraphMLPanel panel;
    private ExporterGraphML exporterGraphML;
    private ExporterGraphMLSettings settings =  new ExporterGraphMLSettings();

    public void setup(Exporter exporter) {
        exporterGraphML = (ExporterGraphML) exporter;
        settings.load(exporterGraphML);
        panel.setup(exporterGraphML);
    }

    public void unsetup(boolean update) {
        if (update) {
            panel.unsetup(exporterGraphML);
            settings.save(exporterGraphML);
        }
        panel = null;
        exporterGraphML = null;
    }

    public JPanel getPanel() {
        panel = new UIExporterGraphMLPanel();
        return panel;
    }

    public boolean isUIForExporter(Exporter exporter) {
        return exporter instanceof ExporterGraphML;
    }

    public String getDisplayName() {
        return NbBundle.getMessage(UIExporterGraphML.class, "UIExporterGraphML.name");
    }

    private static class ExporterGraphMLSettings {

        private boolean normalize = false;
        private boolean exportColors = true;
        private boolean exportPosition = true;
        private boolean exportSize = true;
        private boolean exportAttributes = true;

        private void save(ExporterGraphML exporterGraphML) {
            this.normalize = exporterGraphML.isNormalize();
            this.exportColors = exporterGraphML.isExportColors();
            this.exportPosition = exporterGraphML.isExportPosition();
            this.exportSize = exporterGraphML.isExportSize();
            this.exportAttributes = exporterGraphML.isExportAttributes();
        }

        private void load(ExporterGraphML exporterGraphML) {
            exporterGraphML.setNormalize(normalize);
            exporterGraphML.setExportColors(exportColors);
            exporterGraphML.setExportAttributes(exportAttributes);
            exporterGraphML.setExportPosition(exportPosition);
            exporterGraphML.setExportSize(exportSize);
        }
    }
}
