/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.ui.exporter.plugin;

import javax.swing.JPanel;
import org.gephi.io.exporter.plugin.ExporterCSV;
import org.gephi.io.exporter.spi.Exporter;
import org.gephi.io.exporter.spi.ExporterUI;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Mathieu Bastian
 */
@ServiceProvider(service = ExporterUI.class)
public class UIExporterCSV implements ExporterUI {

    private UIExporterCSVPanel panel;
    private ExporterCSV exporterCSV;

    public void setup(Exporter exporter) {
        exporterCSV = (ExporterCSV) exporter;
        panel.setup(exporterCSV);
    }

    public void unsetup(boolean update) {
        if (update) {
            panel.unsetup(exporterCSV);
        }
        panel = null;
        exporterCSV = null;
    }

    public JPanel getPanel() {
        panel = new UIExporterCSVPanel();
        return panel;
    }

    public boolean isMatchingExporter(Exporter exporter) {
        return exporter instanceof ExporterCSV;
    }
}
