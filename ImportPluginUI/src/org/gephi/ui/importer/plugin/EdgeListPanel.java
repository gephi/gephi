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
package org.gephi.ui.importer.plugin;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;

import javax.swing.DefaultComboBoxModel;

import org.gephi.io.database.drivers.SQLDriver;
import org.gephi.io.database.drivers.SQLUtils;
import org.gephi.io.importer.api.Database;
import org.gephi.io.importer.plugin.database.EdgeListDatabaseImpl;
import org.netbeans.validation.api.builtin.Validators;
import org.netbeans.validation.api.ui.ValidationGroup;
import org.netbeans.validation.api.ui.ValidationPanel;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

/**
 *
 * @author Mathieu Bastian
 */
public class EdgeListPanel extends javax.swing.JPanel {

    private EdgeListDatabaseManager databaseManager;
    private static String NEW_CONFIGURATION_NAME =
            NbBundle.getMessage(EdgeListPanel.class,
            "EdgeListPanel.template.name");

    /** Creates new form EdgeListPanel */
    public EdgeListPanel() {
        databaseManager = new EdgeListDatabaseManager();
        initComponents();
    }

    public static ValidationPanel createValidationPanel(EdgeListPanel innerPanel) {
        ValidationPanel validationPanel = new ValidationPanel();
        if (innerPanel == null) {
            throw new NullPointerException();
        }
        validationPanel.setInnerComponent(innerPanel);

        ValidationGroup group = validationPanel.getValidationGroup();

        //Validators
        group.add(innerPanel.configNameTextField, Validators.REQUIRE_NON_EMPTY_STRING);
        group.add(innerPanel.hostTextField, Validators.HOST_NAME_OR_IP_ADDRESS);
        group.add(innerPanel.dbTextField, Validators.REQUIRE_NON_EMPTY_STRING);
        group.add(innerPanel.portTextField, Validators.REQUIRE_NON_EMPTY_STRING,
                Validators.REQUIRE_VALID_INTEGER,
                Validators.numberRange(1, 65535));
        group.add(innerPanel.userTextField, Validators.REQUIRE_NON_EMPTY_STRING);

        return validationPanel;
    }

    public Database getSelectedDatabase() {
        ConfigurationComboModel model =
                (ConfigurationComboModel) configurationCombo.getModel();
        ConfigurationComboItem item = (ConfigurationComboItem) model.getSelectedItem();

        populateEdgeListDatabase(item.db);

        // add configuration if user changed the template configuration
        if (item.equals(model.templateConfiguration)) {
            databaseManager.addDatabase(item.db);
        }

        databaseManager.persist();

        return item.db;
    }

    public SQLDriver getSelectedSQLDriver() {
        return (SQLDriver) driverComboBox.getSelectedItem();
    }

    public void setSQLDrivers(SQLDriver[] drivers) {
        DefaultComboBoxModel driverModel = new DefaultComboBoxModel(drivers);
        driverComboBox.setModel(driverModel);
    }

    public void setup() {
        configurationCombo.setModel(new EdgeListPanel.ConfigurationComboModel());
        ConfigurationComboModel model =
                (ConfigurationComboModel) configurationCombo.getModel();
        if (model.getSelectedItem().equals(model.templateConfiguration)) {
            this.removeConfigurationButton.setEnabled(false);
        } else {
            this.removeConfigurationButton.setEnabled(true);
        }
    }

    private void populateForm(EdgeListDatabaseImpl db) {
        configNameTextField.setText(db.getName());
        dbTextField.setText(db.getDBName());
        hostTextField.setText(db.getHost());
        portTextField.setText(db.getPort() == 0 ? "" : "" + db.getPort());
        userTextField.setText(db.getUsername());
        pwdTextField.setText(db.getPasswd());
        driverComboBox.getModel().setSelectedItem(db.getSQLDriver());
        nodeQueryTextField.setText(db.getNodeQuery());
        edgeQueryTextField.setText(db.getEdgeQuery());
        nodeAttQueryTextField.setText(db.getNodeAttributesQuery());
        edgeAttQueryTextField.setText(db.getEdgeAttributesQuery());
    }

    private void populateEdgeListDatabase(EdgeListDatabaseImpl db) {
        db.setName(this.configNameTextField.getText());
        db.setDBName(this.dbTextField.getText());
        db.setHost(this.hostTextField.getText());
        db.setPasswd(new String(this.pwdTextField.getPassword()));
        db.setPort(portTextField.getText() != null
                && !"".equals(portTextField.getText())
                ? Integer.parseInt(portTextField.getText()) : 0);
        db.setUsername(this.userTextField.getText());
        db.setSQLDriver(this.getSelectedSQLDriver());
        db.setNodeQuery(this.nodeQueryTextField.getText());
        db.setEdgeQuery(this.edgeQueryTextField.getText());
        db.setNodeAttributesQuery(this.nodeAttQueryTextField.getText());
        db.setEdgeAttributesQuery(this.edgeAttQueryTextField.getText());
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        configurationCombo = new javax.swing.JComboBox();
        configurationLabel = new javax.swing.JLabel();
        hostLabel = new javax.swing.JLabel();
        portLabel = new javax.swing.JLabel();
        hostTextField = new javax.swing.JTextField();
        portTextField = new javax.swing.JTextField();
        userLabel = new javax.swing.JLabel();
        dbLabel = new javax.swing.JLabel();
        pwdLabel = new javax.swing.JLabel();
        dbTextField = new javax.swing.JTextField();
        userTextField = new javax.swing.JTextField();
        driverLabel = new javax.swing.JLabel();
        driverComboBox = new javax.swing.JComboBox();
        nodeQueryLabel = new javax.swing.JLabel();
        nodeQueryTextField = new javax.swing.JTextField();
        edgeQueryLabel = new javax.swing.JLabel();
        nodeAttQueyLabel = new javax.swing.JLabel();
        edgeAttQueryLabel = new javax.swing.JLabel();
        edgeQueryTextField = new javax.swing.JTextField();
        nodeAttQueryTextField = new javax.swing.JTextField();
        edgeAttQueryTextField = new javax.swing.JTextField();
        testConnection = new javax.swing.JButton();
        pwdTextField = new javax.swing.JPasswordField();
        configNameTextField = new javax.swing.JTextField();
        configNameLabel = new javax.swing.JLabel();
        removeConfigurationButton = new javax.swing.JButton();
        jXHeader1 = new org.jdesktop.swingx.JXHeader();

        configurationCombo.setModel(new EdgeListPanel.ConfigurationComboModel());
        configurationCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                configurationComboActionPerformed(evt);
            }
        });

        configurationLabel.setText(org.openide.util.NbBundle.getMessage(EdgeListPanel.class, "EdgeListPanel.configurationLabel.text")); // NOI18N

        hostLabel.setText(org.openide.util.NbBundle.getMessage(EdgeListPanel.class, "EdgeListPanel.hostLabel.text")); // NOI18N

        portLabel.setText(org.openide.util.NbBundle.getMessage(EdgeListPanel.class, "EdgeListPanel.portLabel.text")); // NOI18N

        hostTextField.setName("host"); // NOI18N

        portTextField.setText(org.openide.util.NbBundle.getMessage(EdgeListPanel.class, "EdgeListPanel.port.text")); // NOI18N
        portTextField.setName("port"); // NOI18N

        userLabel.setText(org.openide.util.NbBundle.getMessage(EdgeListPanel.class, "EdgeListPanel.userLabel.text")); // NOI18N

        dbLabel.setText(org.openide.util.NbBundle.getMessage(EdgeListPanel.class, "EdgeListPanel.dbLabel.text")); // NOI18N

        pwdLabel.setText(org.openide.util.NbBundle.getMessage(EdgeListPanel.class, "EdgeListPanel.pwdLabel.text")); // NOI18N

        dbTextField.setText(org.openide.util.NbBundle.getMessage(EdgeListPanel.class, "EdgeListPanel.database.text")); // NOI18N
        dbTextField.setName("database"); // NOI18N

        userTextField.setText(org.openide.util.NbBundle.getMessage(EdgeListPanel.class, "EdgeListPanel.user name.text")); // NOI18N
        userTextField.setName("user name"); // NOI18N

        driverLabel.setText(org.openide.util.NbBundle.getMessage(EdgeListPanel.class, "EdgeListPanel.driverLabel.text")); // NOI18N

        nodeQueryLabel.setText(org.openide.util.NbBundle.getMessage(EdgeListPanel.class, "EdgeListPanel.nodeQueryLabel.text")); // NOI18N

        nodeQueryTextField.setText(org.openide.util.NbBundle.getMessage(EdgeListPanel.class, "EdgeListPanel.nodeQueryTextField.text")); // NOI18N

        edgeQueryLabel.setText(org.openide.util.NbBundle.getMessage(EdgeListPanel.class, "EdgeListPanel.edgeQueryLabel.text")); // NOI18N

        nodeAttQueyLabel.setText(org.openide.util.NbBundle.getMessage(EdgeListPanel.class, "EdgeListPanel.nodeAttQueyLabel.text")); // NOI18N

        edgeAttQueryLabel.setText(org.openide.util.NbBundle.getMessage(EdgeListPanel.class, "EdgeListPanel.edgeAttQueryLabel.text")); // NOI18N

        edgeQueryTextField.setText(org.openide.util.NbBundle.getMessage(EdgeListPanel.class, "EdgeListPanel.edgeQueryTextField.text")); // NOI18N

        nodeAttQueryTextField.setText(org.openide.util.NbBundle.getMessage(EdgeListPanel.class, "EdgeListPanel.nodeAttQueryTextField.text")); // NOI18N

        edgeAttQueryTextField.setText(org.openide.util.NbBundle.getMessage(EdgeListPanel.class, "EdgeListPanel.edgeAttQueryTextField.text")); // NOI18N

        testConnection.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/gephi/ui/importer/plugin/resources/test_connection.png"))); // NOI18N
        testConnection.setText(org.openide.util.NbBundle.getMessage(EdgeListPanel.class, "EdgeListPanel.testConnection.text")); // NOI18N
        testConnection.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                testConnectionActionPerformed(evt);
            }
        });

        pwdTextField.setText(org.openide.util.NbBundle.getMessage(EdgeListPanel.class, "EdgeListPanel.password.text")); // NOI18N
        pwdTextField.setName("password"); // NOI18N

        configNameTextField.setText(org.openide.util.NbBundle.getMessage(EdgeListPanel.class, "EdgeListPanel.configName.text")); // NOI18N
        configNameTextField.setName("configName"); // NOI18N

        configNameLabel.setText(org.openide.util.NbBundle.getMessage(EdgeListPanel.class, "EdgeListPanel.configNameLabel.text")); // NOI18N

        removeConfigurationButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/gephi/ui/importer/plugin/resources/remove_config.png"))); // NOI18N
        removeConfigurationButton.setToolTipText(org.openide.util.NbBundle.getMessage(EdgeListPanel.class, "EdgeListPanel.removeConfigurationButton.toolTipText")); // NOI18N
        removeConfigurationButton.setPreferredSize(new java.awt.Dimension(65, 29));
        removeConfigurationButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeConfigurationButtonActionPerformed(evt);
            }
        });

        jXHeader1.setDescription(org.openide.util.NbBundle.getMessage(EdgeListPanel.class, "EdgeListPanel.header")); // NOI18N
        jXHeader1.setTitle(org.openide.util.NbBundle.getMessage(EdgeListPanel.class, "EdgeListPanel.jXHeader1.title")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(userLabel)
                            .addComponent(pwdLabel)
                            .addComponent(driverLabel)
                            .addComponent(hostLabel)
                            .addComponent(portLabel)
                            .addComponent(dbLabel)
                            .addComponent(nodeQueryLabel)
                            .addComponent(edgeQueryLabel)
                            .addComponent(nodeAttQueyLabel)
                            .addComponent(edgeAttQueryLabel)
                            .addComponent(configNameLabel)
                            .addComponent(configurationLabel))
                        .addGap(22, 22, 22)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(configurationCombo, 0, 421, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(removeConfigurationButton, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(configNameTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 448, Short.MAX_VALUE)
                            .addComponent(edgeAttQueryTextField, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 448, Short.MAX_VALUE)
                            .addComponent(nodeAttQueryTextField, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 448, Short.MAX_VALUE)
                            .addComponent(edgeQueryTextField, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 448, Short.MAX_VALUE)
                            .addComponent(nodeQueryTextField, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 448, Short.MAX_VALUE)
                            .addComponent(portTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 448, Short.MAX_VALUE)
                            .addComponent(hostTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 448, Short.MAX_VALUE)
                            .addComponent(dbTextField, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 448, Short.MAX_VALUE)
                            .addComponent(userTextField, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 448, Short.MAX_VALUE)
                            .addComponent(driverComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(pwdTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 448, Short.MAX_VALUE)))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(testConnection)
                        .addGap(5, 5, 5)))
                .addContainerGap())
            .addComponent(jXHeader1, javax.swing.GroupLayout.DEFAULT_SIZE, 603, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jXHeader1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(19, 19, 19)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(removeConfigurationButton, 0, 0, Short.MAX_VALUE)
                    .addComponent(configurationCombo, javax.swing.GroupLayout.DEFAULT_SIZE, 24, Short.MAX_VALUE)
                    .addComponent(configurationLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(19, 19, 19)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(configNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(configNameLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(driverLabel)
                    .addComponent(driverComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(9, 9, 9)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(hostLabel)
                    .addComponent(hostTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(portTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(portLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(dbLabel)
                    .addComponent(dbTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(userLabel)
                    .addComponent(userTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(pwdLabel)
                    .addComponent(pwdTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(nodeQueryLabel)
                    .addComponent(nodeQueryTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(edgeQueryLabel)
                    .addComponent(edgeQueryTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(nodeAttQueyLabel)
                    .addComponent(nodeAttQueryTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(edgeAttQueryLabel)
                    .addComponent(edgeAttQueryTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(testConnection)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void testConnectionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_testConnectionActionPerformed
        try {
            Integer.parseInt(portTextField.getText());
        } catch (Exception e) {
            return;
        }
        Connection conn = null;
        try {
            conn = getSelectedSQLDriver().getConnection(SQLUtils.getUrl(getSelectedSQLDriver(), hostTextField.getText(), Integer.parseInt(portTextField.getText()), dbTextField.getText()), userTextField.getText(), new String(pwdTextField.getPassword()));
            String message = NbBundle.getMessage(EdgeListPanel.class, "EdgeListPanel.alert.connection_successful");
            NotifyDescriptor.Message e = new NotifyDescriptor.Message(message, NotifyDescriptor.INFORMATION_MESSAGE);
            DialogDisplayer.getDefault().notifyLater(e);
        } catch (SQLException ex) {
            NotifyDescriptor.Exception e = new NotifyDescriptor.Exception(ex);
            DialogDisplayer.getDefault().notifyLater(e);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                    System.out.println("Database connection terminated");
                } catch (Exception e) { /* ignore close errors */ }
            }
        }
    }//GEN-LAST:event_testConnectionActionPerformed

    private void removeConfigurationButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeConfigurationButtonActionPerformed
        ConfigurationComboModel model =
                (ConfigurationComboModel) configurationCombo.getModel();
        ConfigurationComboItem item = (ConfigurationComboItem) model.getSelectedItem();

        if (databaseManager.removeDatabase(item.db)) {

            model.removeElement(item);
            databaseManager.persist();
            String message = NbBundle.getMessage(EdgeListPanel.class,
                    "EdgeListPanel.alert.configuration_removed", item.toString());
            NotifyDescriptor.Message e = new NotifyDescriptor.Message(
                    message, NotifyDescriptor.INFORMATION_MESSAGE);
            DialogDisplayer.getDefault().notifyLater(e);
            model.setSelectedItem(model.getElementAt(0));

        } else {
            String message = NbBundle.getMessage(EdgeListPanel.class,
                    "EdgeListPanel.alert.configuration_unsaved");
            NotifyDescriptor.Message e = new NotifyDescriptor.Message(
                    message, NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notifyLater(e);
        }

    }//GEN-LAST:event_removeConfigurationButtonActionPerformed

    private void configurationComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_configurationComboActionPerformed
        ConfigurationComboModel model =
                (ConfigurationComboModel) configurationCombo.getModel();
        ConfigurationComboItem item = (ConfigurationComboItem) model.getSelectedItem();
        if (item.equals(model.templateConfiguration)) {
            this.removeConfigurationButton.setEnabled(false);
        } else {
            this.removeConfigurationButton.setEnabled(true);
        }
    }//GEN-LAST:event_configurationComboActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel configNameLabel;
    private javax.swing.JTextField configNameTextField;
    private javax.swing.JComboBox configurationCombo;
    private javax.swing.JLabel configurationLabel;
    private javax.swing.JLabel dbLabel;
    protected javax.swing.JTextField dbTextField;
    private javax.swing.JComboBox driverComboBox;
    private javax.swing.JLabel driverLabel;
    private javax.swing.JLabel edgeAttQueryLabel;
    protected javax.swing.JTextField edgeAttQueryTextField;
    private javax.swing.JLabel edgeQueryLabel;
    protected javax.swing.JTextField edgeQueryTextField;
    private javax.swing.JLabel hostLabel;
    protected javax.swing.JTextField hostTextField;
    private org.jdesktop.swingx.JXHeader jXHeader1;
    protected javax.swing.JTextField nodeAttQueryTextField;
    private javax.swing.JLabel nodeAttQueyLabel;
    private javax.swing.JLabel nodeQueryLabel;
    protected javax.swing.JTextField nodeQueryTextField;
    private javax.swing.JLabel portLabel;
    protected javax.swing.JTextField portTextField;
    private javax.swing.JLabel pwdLabel;
    protected javax.swing.JPasswordField pwdTextField;
    private javax.swing.JButton removeConfigurationButton;
    private javax.swing.JButton testConnection;
    private javax.swing.JLabel userLabel;
    protected javax.swing.JTextField userTextField;
    // End of variables declaration//GEN-END:variables

    public void initEvents() {
    }

    private class ConfigurationComboModel extends DefaultComboBoxModel {

        /**
         * The template configuration (will appear as "New Configuration")
         */
        ConfigurationComboItem templateConfiguration;

        public ConfigurationComboModel() {
            super();
            Collection<Database> configs = databaseManager.getEdgeListDatabases();
            for (Database db : configs) {
                EdgeListDatabaseImpl dbe = (EdgeListDatabaseImpl) db;
                ConfigurationComboItem item = new ConfigurationComboItem(dbe);
                this.insertElementAt(item, this.getSize());
            }

            // add template configuration option at end
            EdgeListDatabaseImpl db = new EdgeListDatabaseImpl();
            populateEdgeListDatabase(db);
            templateConfiguration = new ConfigurationComboItem(db);
            templateConfiguration.setConfigurationName(NEW_CONFIGURATION_NAME);
            this.insertElementAt(templateConfiguration, this.getSize());

            this.setSelectedItem(this.getElementAt(0));
        }

        @Override
        public void setSelectedItem(Object anItem) {
            ConfigurationComboItem item = (ConfigurationComboItem) anItem;
            populateForm(item.db);
            super.setSelectedItem(anItem);
        }
    }

    private class ConfigurationComboItem {

        private final EdgeListDatabaseImpl db;
        private String configurationName;

        public ConfigurationComboItem(EdgeListDatabaseImpl db) {
            this.db = db;
            this.configurationName = db.getName();
        }

        public EdgeListDatabaseImpl getDb() {
            return db;
        }

        public void setConfigurationName(String configurationName) {
            this.configurationName = configurationName;
        }

        @Override
        public String toString() {
            String name = configurationName;
            if (name == null || name.isEmpty()) {
                name = SQLUtils.getUrl(db.getSQLDriver(), db.getHost(), db.getPort(), db.getDBName());
            }
            return name;
        }
    }
}
