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

    public void setup(Exporter exporter) {
        exporterGraphML = (ExporterGraphML) exporter;
        panel.setup(exporterGraphML);
    }

    public void unsetup(boolean update) {
        if (update) {
            panel.unsetup(exporterGraphML);
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
}
