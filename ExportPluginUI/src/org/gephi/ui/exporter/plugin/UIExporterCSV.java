/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
