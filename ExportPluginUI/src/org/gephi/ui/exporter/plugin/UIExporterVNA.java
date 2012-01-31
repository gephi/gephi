/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.ui.exporter.plugin;

import javax.swing.JPanel;
import org.gephi.io.exporter.plugin.ExporterVNA;
import org.gephi.io.exporter.spi.Exporter;
import org.gephi.io.exporter.spi.ExporterUI;
import org.gephi.io.exporter.spi.GraphExporter;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author megaterik
 */
@ServiceProvider(service = ExporterUI.class)
public class UIExporterVNA implements ExporterUI {

    UIExporterVNAPanel panel;
    ExporterVNA exporter;
    ExporterVNASettings settings = new ExporterVNASettings();

    public JPanel getPanel() {
        panel = new UIExporterVNAPanel();
        return panel;
    }

    public void setup(Exporter exporter) {
        this.exporter = (ExporterVNA)exporter;
        settings.load(this.exporter);
        panel.setup((ExporterVNA)exporter);
    }

    public void unsetup(boolean update) {
        if (update)
        {
            panel.unsetup(exporter);
            settings.save(exporter);
        }
        panel = null;
        exporter = null;
    }

    public boolean isUIForExporter(Exporter exporter) {
        return exporter instanceof ExporterVNA;
    }

    public String getDisplayName() {
        return "Settings";
    }

    private static class ExporterVNASettings {
        private boolean exportEdgeWeight = true;
        private boolean exportCoords = true;
        private boolean exportSize = true;
        private boolean exportShortLabel = true;
        private boolean exportColor = true;
        private boolean normalize = false;
        private boolean exportAttributes = true;
        
        private void load(ExporterVNA exporter)
        {
            exporter.setExportColor(exportColor);
            exporter.setExportCoords(exportCoords);
            exporter.setExportEdgeWeight(exportEdgeWeight);
            exporter.setExportShortLabel(exportShortLabel);
            exporter.setExportSize(exportSize);
            exporter.setExportAttributes(exportAttributes);
            exporter.setNormalize(normalize);
        }
        
        private void save(ExporterVNA exporter)
        {
            exportColor = exporter.isExportColor();
            exportCoords = exporter.isExportCoords();
            exportEdgeWeight = exporter.isExportEdgeWeight();
            exportShortLabel = exporter.isExportShortLabel();
            exportSize = exporter.isExportSize();
            exportAttributes = exporter.isExportAttributes();
            normalize = exporter.isNormalize();
        }
    }
}
