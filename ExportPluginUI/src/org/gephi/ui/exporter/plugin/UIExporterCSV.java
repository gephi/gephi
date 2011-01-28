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
import org.gephi.io.exporter.plugin.ExporterCSV;
import org.gephi.io.exporter.spi.Exporter;
import org.gephi.io.exporter.spi.ExporterUI;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Mathieu Bastian
 */
@ServiceProvider(service = ExporterUI.class)
public class UIExporterCSV implements ExporterUI {

    private UIExporterCSVPanel panel;
    private ExporterCSV exporterCSV;
    private ExporterCSVSettings settings = new ExporterCSVSettings();

    public void setup(Exporter exporter) {
        exporterCSV = (ExporterCSV) exporter;
        settings.load(exporterCSV);
        panel.setup(exporterCSV);
    }

    public void unsetup(boolean update) {
        if (update) {
            panel.unsetup(exporterCSV);
            settings.save(exporterCSV);
        }
        panel = null;
        exporterCSV = null;
    }

    public JPanel getPanel() {
        panel = new UIExporterCSVPanel();
        return panel;
    }

    public boolean isUIForExporter(Exporter exporter) {
        return exporter instanceof ExporterCSV;
    }

    public String getDisplayName() {
        return NbBundle.getMessage(UIExporterCSV.class, "UIExporterCSV.name");
    }

    private static class ExporterCSVSettings {

        private boolean edgeWeight = true;
        private boolean writeZero = true;
        private boolean header = true;
        private boolean list = false;

        private void save(ExporterCSV exporterCSV) {
            this.edgeWeight = exporterCSV.isEdgeWeight();
            this.writeZero = exporterCSV.isWriteZero();
            this.header = exporterCSV.isHeader();
            this.list = exporterCSV.isList();
        }

        private void load(ExporterCSV exporterCSV) {
            exporterCSV.setEdgeWeight(edgeWeight);
            exporterCSV.setWriteZero(writeZero);
            exporterCSV.setHeader(header);
            exporterCSV.setList(list);
        }
    }
}
