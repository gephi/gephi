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
import org.gephi.io.exporter.preview.PDFExporter;
import org.gephi.io.exporter.spi.Exporter;
import org.gephi.io.exporter.spi.ExporterUI;
import org.netbeans.validation.api.ui.ValidationPanel;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Mathieu Bastian
 */
@ServiceProvider(service = ExporterUI.class)
public class UIExporterPDF implements ExporterUI {

    private UIExporterPDFPanel panel;
    private ValidationPanel validationPanel;
    private PDFExporter exporterPDF;

    public void setup(Exporter exporter) {
        exporterPDF = (PDFExporter) exporter;
        panel.setup(exporterPDF);
    }

    public void unsetup(boolean update) {
        if (update) {
            panel.unsetup(exporterPDF);
        }
        panel = null;
        exporterPDF = null;
    }

    public JPanel getPanel() {
        panel = new UIExporterPDFPanel();
        validationPanel = UIExporterPDFPanel.createValidationPanel(panel);
        return validationPanel;
    }

    public boolean isUIForExporter(Exporter exporter) {
        return exporter instanceof PDFExporter;
    }

    public String getDisplayName() {
        return NbBundle.getMessage(UIExporterPDF.class, "UIExporterPDF.name");
    }
}
