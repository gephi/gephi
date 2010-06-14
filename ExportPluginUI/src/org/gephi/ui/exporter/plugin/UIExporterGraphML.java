/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
    private ExporterGraphMLSettings settings;

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
