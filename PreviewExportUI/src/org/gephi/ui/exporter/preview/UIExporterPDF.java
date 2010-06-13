/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
