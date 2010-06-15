/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.ui.importer.plugin;

import javax.swing.JPanel;
import org.gephi.io.database.drivers.SQLDriver;
import org.gephi.io.importer.api.Database;
import org.gephi.io.importer.plugin.database.ImporterBuilderEdgeList;
import org.gephi.io.importer.plugin.database.ImporterEdgeList;
import org.gephi.io.importer.spi.DatabaseImporter;
import org.gephi.io.importer.spi.Importer;
import org.gephi.io.importer.spi.ImporterUI;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Mathieu Bastian
 */
@ServiceProvider(service = ImporterUI.class)
public class EdgeListDatabaseImporterUI implements ImporterUI {

    private EdgeListPanel panel;
    private DatabaseImporter importer;

    public void setup(Importer importer) {
        this.importer = (DatabaseImporter) importer;
        if (panel == null) {
            panel = new EdgeListPanel();
        }
        panel.setup();

        //Driver Combo
        SQLDriver[] driverArray = new SQLDriver[0];
        driverArray = Lookup.getDefault().lookupAll(SQLDriver.class).toArray(driverArray);
        panel.setSQLDrivers(driverArray);
    }

    public JPanel getPanel() {
        if (panel == null) {
            panel = new EdgeListPanel();
        }
        return EdgeListPanel.createValidationPanel(panel);
    }

    public void unsetup(boolean update) {
        if (update) {
            Database database = panel.getSelectedDatabase();
            importer.setDatabase(database);
        }
        panel = null;
        importer = null;
    }

    public String getDisplayName() {
        return NbBundle.getMessage(getClass(), "EdgeListBuilder.displayName");
    }

    public String getIdentifier() {
        return ImporterBuilderEdgeList.IDENTIFER;
    }

    public boolean isUIForImporter(Importer importer) {
        return importer instanceof ImporterEdgeList;
    }
}
