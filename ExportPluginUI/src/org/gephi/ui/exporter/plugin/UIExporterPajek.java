/*
Copyright 2008-2011 Gephi
Authors : Daniel Bernardes <daniel.bernardes@polytechnique.edu>
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
import org.gephi.io.exporter.plugin.ExporterPajek;
import org.gephi.io.exporter.spi.Exporter;
import org.gephi.io.exporter.spi.ExporterUI;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Daniel Bernardes
 */
@ServiceProvider(service = ExporterUI.class)
public class UIExporterPajek implements ExporterUI {

    private UIExporterPajekPanel panel;
    private ExporterPajek exporterPajek;
    private ExporterPajekSettings settings =  new ExporterPajekSettings();

    public void setup(Exporter exporter) {
        exporterPajek = (ExporterPajek) exporter;
        settings.load(exporterPajek);
        panel.setup(exporterPajek);
    }

    public void unsetup(boolean update) {
        if (update) {
            panel.unsetup(exporterPajek);
            settings.save(exporterPajek);
        }
        panel = null;
        exporterPajek = null;
    }

    public JPanel getPanel() {
        panel = new UIExporterPajekPanel();
        return panel;
    }

    public boolean isUIForExporter(Exporter exporter) {
        return exporter instanceof ExporterPajek;
    }

    public String getDisplayName() {
        return NbBundle.getMessage(UIExporterPajek.class, "UIExporterPajek.name");
    }

    private static class ExporterPajekSettings {

        private boolean exportPosition = true;
        private boolean exportEdgeWeight = true;

        private void save(ExporterPajek exporterPajek) {
            this.exportPosition = exporterPajek.isExportPosition();
            this.exportEdgeWeight = exporterPajek.isExportEdgeWeight();
        }

        private void load(ExporterPajek exporterPajek) {
            exporterPajek.setExportPosition(exportPosition);
            exporterPajek.setExportEdgeWeight(exportEdgeWeight);
        }
    }
}
