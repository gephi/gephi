/*
Copyright 2008 WebAtlas
Authors : Mathieu Bastian, Mathieu Jacomy, Julian Bilcke
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gephi.ui.database.standard;

import javax.swing.JPanel;
import org.gephi.io.database.Database;
import org.gephi.io.database.DatabaseType;
import org.gephi.io.database.EdgeListDatabase;
import org.gephi.io.database.drivers.SQLDriver;
import org.gephi.ui.database.standard.EdgeListDatabaseUI;
import org.openide.util.Lookup;

/**
 *
 * @author Mathieu Bastian
 */
public class EdgeListDatabaseUIImpl implements EdgeListDatabaseUI {

    private EdgeListPanel panel;
    private Database database;

    public EdgeListDatabaseUIImpl() {
    }

    public JPanel getPanel() {
        if (panel == null) {
            panel = new EdgeListPanel();
        }
        return EdgeListPanel.createValidationPanel(panel);
    }

    public void setup(DatabaseType type) {
        if (panel == null) {
            panel = new EdgeListPanel();
        }
        panel.setDatabaseType(type);

        //Driver Combo
        SQLDriver[] driverArray = new SQLDriver[0];
        driverArray = Lookup.getDefault().lookupAll(SQLDriver.class).toArray(driverArray);
        panel.setSQLDrivers(driverArray);
    }

    public void unsetup() {
        EdgeListDatabase selectedDB = (EdgeListDatabase) panel.getSelectedDatabase();
        selectedDB.setDBName(panel.dbTextField.getText());
        selectedDB.setHost(panel.hostTextField.getText());
        selectedDB.setPasswd(new String(panel.pwdTextField.getPassword()));
        selectedDB.setPort(Integer.parseInt(panel.portTextField.getText()));
        selectedDB.setUsername(panel.userTextField.getText());
        selectedDB.setSQLDriver(panel.getSelectedSQLDriver());
        selectedDB.setNodeQuery(panel.nodeQueryTextField.getText());
        selectedDB.setEdgeQuery(panel.edgeQueryTextField.getText());
        selectedDB.setNodeAttributesQuery(panel.nodeAttQueryTextField.getText());
        selectedDB.setEdgeAttributesQuery(panel.edgeAttQueryTextField.getText());
        this.database = selectedDB;
        panel = null;
    }

    public Database getDatabase() {
        return database;
    }
}
