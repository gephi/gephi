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
