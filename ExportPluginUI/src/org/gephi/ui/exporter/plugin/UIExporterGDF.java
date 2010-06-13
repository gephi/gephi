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

    public void setup(Exporter exporter) {
        exporterGDF = (ExporterGDF) exporter;
        panel.setup(exporterGDF);
    }

    public void unsetup(boolean update) {
        if (update) {
            panel.unsetup(exporterGDF);
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
}
