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
package org.gephi.ui.exporter.preview;

import javax.swing.JPanel;
import org.gephi.io.exporter.preview.SVGExporter;
import org.gephi.io.exporter.spi.Exporter;
import org.gephi.io.exporter.spi.ExporterUI;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Mathieu Bastian
 */
@ServiceProvider(service = ExporterUI.class)
public class UIExporterSVG implements ExporterUI {

    private UIExporterSVGPanel panel;
    private SVGExporter exporterSVG;

    public void setup(Exporter exporter) {
        exporterSVG = (SVGExporter) exporter;
        loadPreferences();
        panel.setup(exporterSVG);
    }

    public void unsetup(boolean update) {
        if (update) {
            panel.unsetup(exporterSVG);
            savePreferences();
        }
        panel = null;
        exporterSVG = null;
    }

    public JPanel getPanel() {
        panel = new UIExporterSVGPanel();
        return panel;
    }

    public boolean isUIForExporter(Exporter exporter) {
        return exporter instanceof SVGExporter;
    }

    public String getDisplayName() {
        return NbBundle.getMessage(UIExporterPDF.class, "UIExporterSVG.name");
    }

    private void loadPreferences() {
        boolean strokeScale = NbPreferences.forModule(UIExporterSVG.class).getBoolean("ScaleStrokeWidth", false);
        exporterSVG.setScaleStrokes(strokeScale);
    }

    private void savePreferences() {
        NbPreferences.forModule(UIExporterSVG.class).putBoolean("ScaleStrokeWidth", exporterSVG.isScaleStrokes());
    }
}
