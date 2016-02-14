/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
Website : http://www.gephi.org

This file is part of Gephi.

DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

Copyright 2011 Gephi Consortium. All rights reserved.

The contents of this file are subject to the terms of either the GNU
General Public License Version 3 only ("GPL") or the Common
Development and Distribution License("CDDL") (collectively, the
"License"). You may not use this file except in compliance with the
License. You can obtain a copy of the License at
http://gephi.org/about/legal/license-notice/
or /cddl-1.0.txt and /gpl-3.0.txt. See the License for the
specific language governing permissions and limitations under the
License.  When distributing the software, include this License Header
Notice in each file and include the License files at
/cddl-1.0.txt and /gpl-3.0.txt. If applicable, add the following below the
License Header, with the fields enclosed by brackets [] replaced by
your own identifying information:
"Portions Copyrighted [year] [name of copyright owner]"

If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 3, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 3] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 3 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 3 code and therefore, elected the GPL
Version 3 license, then the option applies only if the new code is
made subject to such option by the copyright holder.

Contributor(s):

Portions Copyrighted 2011 Gephi Consortium.
 */
package org.gephi.ui.importer.plugin;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import org.gephi.io.database.drivers.SQLDriver;
import org.gephi.io.database.drivers.SQLUtils;
import org.gephi.io.importer.api.Database;
import org.gephi.io.importer.plugin.database.EdgeListDatabaseImpl;
import org.gephi.ui.utils.DialogFileFilter;
import org.netbeans.validation.api.Problems;
import org.netbeans.validation.api.Validator;
import org.netbeans.validation.api.builtin.Validators;
import org.netbeans.validation.api.ui.ValidationGroup;
import org.netbeans.validation.api.ui.ValidationPanel;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 *
 * @author Mathieu Bastian
 */
public class EdgeListPanel extends javax.swing.JPanel {

    private EdgeListDatabaseManager databaseManager;
    private final String LAST_PATH = "EdgeListPanel_Sqlite_Last_Path";
    private static String NEW_CONFIGURATION_NAME
            = NbBundle.getMessage(EdgeListPanel.class,
                    "EdgeListPanel.template.name");
    private boolean inited = false;

    /**
     * Creates new form EdgeListPanel
     */
    public EdgeListPanel() {
        databaseManager = new EdgeListDatabaseManager();
        initComponents();

        driverComboBox.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    initDriverType((SQLDriver) e.getItem());
                }
            }
        });

        browseButton.setVisible(false);
        browseButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                String lastPath = NbPreferences.forModule(EdgeListPanel.class).get(LAST_PATH, "");
                final JFileChooser chooser = new JFileChooser(lastPath);
                chooser.setAcceptAllFileFilterUsed(false);
                chooser.setDialogTitle(NbBundle.getMessage(EdgeListPanel.class, "EdgeListPanel.sqliteFileChooser.title"));
                DialogFileFilter dialogFileFilter = new DialogFileFilter(NbBundle.getMessage(EdgeListPanel.class, "EdgeListPanel.sqliteFileChooser.filefilter"));
                dialogFileFilter.addExtension("sqlite");
                dialogFileFilter.addExtension("db");
                chooser.addChoosableFileFilter(dialogFileFilter);

                int returnFile = chooser.showSaveDialog(null);
                if (returnFile != JFileChooser.APPROVE_OPTION) {
                    return;
                }
                File file = chooser.getSelectedFile();
                hostTextField.setText(file.getAbsolutePath());

                //Save last path
                NbPreferences.forModule(EdgeListPanel.class).put(LAST_PATH, file.getParentFile().getAbsolutePath());
            }
        });
    }
    static ValidationGroup group;

    public static ValidationPanel createValidationPanel(EdgeListPanel innerPanel) {
        ValidationPanel validationPanel = new ValidationPanel();
        if (innerPanel == null) {
            throw new NullPointerException();
        }
        validationPanel.setInnerComponent(innerPanel);

        group = validationPanel.getValidationGroup();

        //Validators
        group.add(innerPanel.configNameTextField, Validators.REQUIRE_NON_EMPTY_STRING);
        group.add(innerPanel.hostTextField, new HostOrFileValidator(innerPanel));
        group.add(innerPanel.dbTextField, new NotEmptyValidator(innerPanel));
        group.add(innerPanel.portTextField, new PortValidator(innerPanel));
        group.add(innerPanel.userTextField, new NotEmptyValidator(innerPanel));

        return validationPanel;
    }

    private void initDriverType(final SQLDriver driver) {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                if (driver != null && driver.getPrefix().equals("sqlite")) {
                    hostLabel.setText(NbBundle.getMessage(EdgeListPanel.class, "EdgeListPanel.fileLabel.text"));
                    portTextField.setEnabled(false);
                    portLabel.setEnabled(false);
                    dbLabel.setEnabled(false);
                    dbTextField.setEnabled(false);
                    userLabel.setEnabled(false);
                    userTextField.setEnabled(false);
                    pwdLabel.setEnabled(false);
                    pwdTextField.setEnabled(false);
                    pwdTextField.setText("");
                    userTextField.setText("");
                    dbTextField.setText("");
                    portTextField.setText("");
                    browseButton.setVisible(true);
                } else {
                    hostLabel.setText(NbBundle.getMessage(EdgeListPanel.class, "EdgeListPanel.hostLabel.text"));
                    portTextField.setEnabled(true);
                    portLabel.setEnabled(true);
                    dbLabel.setEnabled(true);
                    dbTextField.setEnabled(true);
                    userLabel.setEnabled(true);
                    userTextField.setEnabled(true);
                    pwdLabel.setEnabled(true);
                    pwdTextField.setEnabled(true);
                    browseButton.setVisible(false);
                }
                group.validateAll();
            }
        });
    }

    public Database getSelectedDatabase() {
        ConfigurationComboModel model
                = (ConfigurationComboModel) configurationCombo.getModel();
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
        ConfigurationComboModel model
                = (ConfigurationComboModel) configurationCombo.getModel();
        if (model.getSelectedItem().equals(model.templateConfiguration)) {
            this.removeConfigurationButton.setEnabled(false);
        } else {
            this.removeConfigurationButton.setEnabled(true);
        }
        inited = true;
        group.validateAll();
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

        initDriverType(db.getSQLDriver());
    }

    private void populateEdgeListDatabase(EdgeListDatabaseImpl db) {
        db.setName(this.configNameTextField.getText());
        db.setDBName(this.dbTextField.getText());
        db.setHost(this.hostTextField.getText());
        db.setPasswd(new String(this.pwdTextField.getPassword()));
        db.setPort(!portTextField.getText().isEmpty()
                ? Integer.parseInt(portTextField.getText()) : 0);
        db.setUsername(this.userTextField.getText());
        db.setSQLDriver(this.getSelectedSQLDriver());
        db.setNodeQuery(this.nodeQueryTextField.getText());
        db.setEdgeQuery(this.edgeQueryTextField.getText());
        db.setNodeAttributesQuery("");
        db.setEdgeAttributesQuery("");
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
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
        edgeQueryTextField = new javax.swing.JTextField();
        testConnection = new javax.swing.JButton();
        pwdTextField = new javax.swing.JPasswordField();
        configNameTextField = new javax.swing.JTextField();
        configNameLabel = new javax.swing.JLabel();
        removeConfigurationButton = new javax.swing.JButton();
        jXHeader1 = new org.jdesktop.swingx.JXHeader();
        browseButton = new javax.swing.JButton();

        configurationCombo.setModel(new EdgeListPanel.ConfigurationComboModel());
        configurationCombo.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                configurationComboActionPerformed(evt);
            }
        });

        configurationLabel.setText(org.openide.util.NbBundle.getMessage(EdgeListPanel.class, "EdgeListPanel.configurationLabel.text")); // NOI18N

        hostLabel.setText(org.openide.util.NbBundle.getMessage(EdgeListPanel.class, "EdgeListPanel.hostLabel.text")); // NOI18N

        portLabel.setText(org.openide.util.NbBundle.getMessage(EdgeListPanel.class, "EdgeListPanel.portLabel.text")); // NOI18N

        hostTextField.setName("host"); // NOI18N

        portTextField.setName("port"); // NOI18N

        userLabel.setText(org.openide.util.NbBundle.getMessage(EdgeListPanel.class, "EdgeListPanel.userLabel.text")); // NOI18N

        dbLabel.setText(org.openide.util.NbBundle.getMessage(EdgeListPanel.class, "EdgeListPanel.dbLabel.text")); // NOI18N

        pwdLabel.setText(org.openide.util.NbBundle.getMessage(EdgeListPanel.class, "EdgeListPanel.pwdLabel.text")); // NOI18N

        dbTextField.setName("database"); // NOI18N

        userTextField.setName("user name"); // NOI18N

        driverLabel.setText(org.openide.util.NbBundle.getMessage(EdgeListPanel.class, "EdgeListPanel.driverLabel.text")); // NOI18N

        nodeQueryLabel.setText(org.openide.util.NbBundle.getMessage(EdgeListPanel.class, "EdgeListPanel.nodeQueryLabel.text")); // NOI18N

        nodeQueryTextField.setText(org.openide.util.NbBundle.getMessage(EdgeListPanel.class, "EdgeListPanel.nodeQueryTextField.text")); // NOI18N

        edgeQueryLabel.setText(org.openide.util.NbBundle.getMessage(EdgeListPanel.class, "EdgeListPanel.edgeQueryLabel.text")); // NOI18N

        edgeQueryTextField.setText(org.openide.util.NbBundle.getMessage(EdgeListPanel.class, "EdgeListPanel.edgeQueryTextField.text")); // NOI18N

        testConnection.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/gephi/ui/importer/plugin/resources/test_connection.png"))); // NOI18N
        testConnection.setText(org.openide.util.NbBundle.getMessage(EdgeListPanel.class, "EdgeListPanel.testConnection.text")); // NOI18N
        testConnection.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                testConnectionActionPerformed(evt);
            }
        });

        pwdTextField.setName("password"); // NOI18N

        configNameTextField.setName("configName"); // NOI18N

        configNameLabel.setText(org.openide.util.NbBundle.getMessage(EdgeListPanel.class, "EdgeListPanel.configNameLabel.text")); // NOI18N

        removeConfigurationButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/gephi/ui/importer/plugin/resources/remove_config.png"))); // NOI18N
        removeConfigurationButton.setToolTipText(org.openide.util.NbBundle.getMessage(EdgeListPanel.class, "EdgeListPanel.removeConfigurationButton.toolTipText")); // NOI18N
        removeConfigurationButton.setMargin(new java.awt.Insets(0, 4, 0, 2));
        removeConfigurationButton.setPreferredSize(new java.awt.Dimension(65, 29));
        removeConfigurationButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeConfigurationButtonActionPerformed(evt);
            }
        });

        jXHeader1.setDescription(org.openide.util.NbBundle.getMessage(EdgeListPanel.class, "EdgeListPanel.header")); // NOI18N
        jXHeader1.setTitle(org.openide.util.NbBundle.getMessage(EdgeListPanel.class, "EdgeListPanel.jXHeader1.title")); // NOI18N

        browseButton.setText(org.openide.util.NbBundle.getMessage(EdgeListPanel.class, "EdgeListPanel.browseButton.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jXHeader1, javax.swing.GroupLayout.DEFAULT_SIZE, 655, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(testConnection)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(userLabel)
                            .addComponent(pwdLabel)
                            .addComponent(driverLabel)
                            .addComponent(hostLabel)
                            .addComponent(portLabel)
                            .addComponent(dbLabel)
                            .addComponent(nodeQueryLabel)
                            .addComponent(edgeQueryLabel)
                            .addComponent(configNameLabel)
                            .addComponent(configurationLabel))
                        .addGap(36, 36, 36)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(configurationCombo, 0, 423, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(removeConfigurationButton, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(configNameTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 448, Short.MAX_VALUE)
                            .addComponent(edgeQueryTextField, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 448, Short.MAX_VALUE)
                            .addComponent(nodeQueryTextField, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 448, Short.MAX_VALUE)
                            .addComponent(portTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 448, Short.MAX_VALUE)
                            .addComponent(dbTextField, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 448, Short.MAX_VALUE)
                            .addComponent(userTextField, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 448, Short.MAX_VALUE)
                            .addComponent(driverComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(pwdTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 448, Short.MAX_VALUE)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(hostTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 351, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(browseButton)))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jXHeader1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(19, 19, 19)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(removeConfigurationButton, 0, 0, Short.MAX_VALUE)
                    .addComponent(configurationCombo, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
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
                    .addComponent(hostTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(browseButton))
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
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(testConnection)
                .addContainerGap(53, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void testConnectionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_testConnectionActionPerformed
        if (!portTextField.getText().isEmpty()) {
            try {
                Integer.parseInt(portTextField.getText());
            } catch (Exception e) {
                return;
            }
        }
        Connection conn = null;
        try {
            conn = getSelectedSQLDriver().getConnection(SQLUtils.getUrl(getSelectedSQLDriver(), hostTextField.getText(), (portTextField.getText().isEmpty() ? 0 : Integer.parseInt(portTextField.getText())), dbTextField.getText()), userTextField.getText(), new String(pwdTextField.getPassword()));
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
                    Logger.getLogger("").info("Database connection terminated");
                } catch (Exception e) {
                    /* ignore close errors */ }
            }
        }
    }//GEN-LAST:event_testConnectionActionPerformed

    private void removeConfigurationButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeConfigurationButtonActionPerformed
        ConfigurationComboModel model
                = (ConfigurationComboModel) configurationCombo.getModel();
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
        ConfigurationComboModel model
                = (ConfigurationComboModel) configurationCombo.getModel();
        ConfigurationComboItem item = (ConfigurationComboItem) model.getSelectedItem();
        if (item.equals(model.templateConfiguration)) {
            this.removeConfigurationButton.setEnabled(false);
        } else {
            this.removeConfigurationButton.setEnabled(true);
        }
    }//GEN-LAST:event_configurationComboActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton browseButton;
    private javax.swing.JLabel configNameLabel;
    private javax.swing.JTextField configNameTextField;
    private javax.swing.JComboBox configurationCombo;
    private javax.swing.JLabel configurationLabel;
    private javax.swing.JLabel dbLabel;
    protected javax.swing.JTextField dbTextField;
    private javax.swing.JComboBox driverComboBox;
    private javax.swing.JLabel driverLabel;
    private javax.swing.JLabel edgeQueryLabel;
    protected javax.swing.JTextField edgeQueryTextField;
    private javax.swing.JLabel hostLabel;
    protected javax.swing.JTextField hostTextField;
    private org.jdesktop.swingx.JXHeader jXHeader1;
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

            ConfigurationComboItem selected = (ConfigurationComboItem) this.getElementAt(0);
            this.setSelectedItem(selected);

            driverComboBox.setSelectedItem(selected.db.getSQLDriver());
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

    private static class HostOrFileValidator implements Validator<String> {

        private EdgeListPanel panel;

        public HostOrFileValidator(EdgeListPanel panel) {
            this.panel = panel;
        }

        @Override
        public boolean validate(Problems problems, String compName, String model) {
            if (!panel.inited) {
                return true;
            }
            if (isSqlite(panel)) {
                return Validators.FILE_MUST_BE_FILE.validate(problems, compName, model);
            } else {
                return Validators.REQUIRE_NON_EMPTY_STRING.validate(problems, compName, model);
            }
        }
    }

    private static boolean isSqlite(EdgeListPanel panel) {
        return panel.getSelectedSQLDriver().getPrefix().equals("sqlite");
    }

    private static class NotEmptyValidator implements Validator<String> {

        private EdgeListPanel panel;

        public NotEmptyValidator(EdgeListPanel panel) {
            this.panel = panel;
        }

        @Override
        public boolean validate(Problems problems, String compName, String model) {
            if (!panel.inited) {
                return true;
            }
            if (isSqlite(panel)) {
                return true;
            } else {
                return Validators.REQUIRE_NON_EMPTY_STRING.validate(problems, compName, model);
            }
        }
    }

    private static class PortValidator implements Validator<String> {

        private EdgeListPanel panel;

        public PortValidator(EdgeListPanel panel) {
            this.panel = panel;
        }

        @Override
        public boolean validate(Problems problems, String compName, String model) {
            if (!panel.inited) {
                return true;
            }
            if (isSqlite(panel)) {
                return true;
            } else {
                return Validators.REQUIRE_NON_EMPTY_STRING.validate(problems, compName, model)
                        && Validators.REQUIRE_VALID_INTEGER.validate(problems, compName, model)
                        && Validators.numberRange(1, 65535).validate(problems, compName, model);
            }
        }
    }
}
