/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.ui.spigot.plugin;

import javax.swing.JPanel;
import org.gephi.io.importer.spi.Importer;
import org.gephi.io.importer.spi.ImporterUI;
import org.gephi.io.spigot.plugin.SampleSpigot;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Mathieu Bastian
 */
@ServiceProvider(service = ImporterUI.class)
public class SampleSpigotUI implements ImporterUI {

    private SampleSpigotPanel panel;

    public void setup(Importer importer) {
    }

    public JPanel getPanel() {
        panel = new SampleSpigotPanel();
        return panel;
    }

    public void unsetup() {
        panel = null;
    }

    public String getDisplayName() {
        return "Sample";
    }

    public boolean isUIForImporter(Importer importer) {
        return importer instanceof SampleSpigot;
    }
}
