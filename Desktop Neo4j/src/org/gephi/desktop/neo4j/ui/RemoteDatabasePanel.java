package org.gephi.desktop.neo4j.ui;

import org.netbeans.validation.api.builtin.Validators;
import org.netbeans.validation.api.ui.ValidationGroup;
import org.netbeans.validation.api.ui.ValidationPanel;

/**
 *
 * @author Martin Škurla
 */
public class RemoteDatabasePanel extends javax.swing.JPanel {

    public RemoteDatabasePanel() {
        initComponents();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        contentPanel = new javax.swing.JPanel();
        remoteDatabaseUrlLabel = new javax.swing.JLabel();
        remoteDatabaseUrlTextField = new javax.swing.JTextField();
        loginLabel = new javax.swing.JLabel();
        loginTextField = new javax.swing.JTextField();
        passwordLabel = new javax.swing.JLabel();
        passwordTextField = new javax.swing.JTextField();

        contentPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(RemoteDatabasePanel.class, "RemoteDatabasePanel.contentPanel.border.title"))); // NOI18N

        remoteDatabaseUrlLabel.setText(org.openide.util.NbBundle.getMessage(RemoteDatabasePanel.class, "RemoteDatabasePanel.remoteDatabaseUrlLabel.text")); // NOI18N

        remoteDatabaseUrlTextField.setText(org.openide.util.NbBundle.getMessage(RemoteDatabasePanel.class, "RemoteDatabasePanel.remote database URL.text")); // NOI18N
        remoteDatabaseUrlTextField.setToolTipText(org.openide.util.NbBundle.getMessage(RemoteDatabasePanel.class, "RemoteDatabasePanel.remote database URL.toolTipText")); // NOI18N
        remoteDatabaseUrlTextField.setName("remote database URL"); // NOI18N

        loginLabel.setText(org.openide.util.NbBundle.getMessage(RemoteDatabasePanel.class, "RemoteDatabasePanel.loginLabel.text")); // NOI18N

        loginTextField.setText(org.openide.util.NbBundle.getMessage(RemoteDatabasePanel.class, "RemoteDatabasePanel.login.text")); // NOI18N
        loginTextField.setToolTipText(org.openide.util.NbBundle.getMessage(RemoteDatabasePanel.class, "RemoteDatabasePanel.login.toolTipText")); // NOI18N
        loginTextField.setName("login"); // NOI18N

        passwordLabel.setText(org.openide.util.NbBundle.getMessage(RemoteDatabasePanel.class, "RemoteDatabasePanel.passwordLabel.text")); // NOI18N

        passwordTextField.setText(org.openide.util.NbBundle.getMessage(RemoteDatabasePanel.class, "RemoteDatabasePanel.password.text")); // NOI18N
        passwordTextField.setToolTipText(org.openide.util.NbBundle.getMessage(RemoteDatabasePanel.class, "RemoteDatabasePanel.password.toolTipText")); // NOI18N
        passwordTextField.setName("password"); // NOI18N

        javax.swing.GroupLayout contentPanelLayout = new javax.swing.GroupLayout(contentPanel);
        contentPanel.setLayout(contentPanelLayout);
        contentPanelLayout.setHorizontalGroup(
            contentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(contentPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(contentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(passwordLabel)
                    .addComponent(loginLabel)
                    .addComponent(remoteDatabaseUrlLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(contentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(loginTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 219, Short.MAX_VALUE)
                    .addComponent(remoteDatabaseUrlTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 219, Short.MAX_VALUE)
                    .addComponent(passwordTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 219, Short.MAX_VALUE))
                .addContainerGap())
        );
        contentPanelLayout.setVerticalGroup(
            contentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(contentPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(contentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(remoteDatabaseUrlLabel)
                    .addComponent(remoteDatabaseUrlTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(contentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(loginLabel)
                    .addComponent(loginTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(contentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(passwordLabel)
                    .addComponent(passwordTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(contentPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(contentPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(50, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel contentPanel;
    private javax.swing.JLabel loginLabel;
    private javax.swing.JTextField loginTextField;
    private javax.swing.JLabel passwordLabel;
    private javax.swing.JTextField passwordTextField;
    private javax.swing.JLabel remoteDatabaseUrlLabel;
    private javax.swing.JTextField remoteDatabaseUrlTextField;
    // End of variables declaration//GEN-END:variables

    public static ValidationPanel createValidationPanel(RemoteDatabasePanel innerPanel) {
        ValidationPanel validationPanel = new ValidationPanel();
        validationPanel.setInnerComponent(innerPanel);
        ValidationGroup group = validationPanel.getValidationGroup();

        //Validators
        group.add(innerPanel.remoteDatabaseUrlTextField, Validators.URL_MUST_BE_VALID);
        group.add(innerPanel.loginTextField, Validators.REQUIRE_NON_EMPTY_STRING);
        group.add(innerPanel.passwordTextField, Validators.REQUIRE_NON_EMPTY_STRING);

        return validationPanel;
    }

    public String getRemoteUrl() {
        return remoteDatabaseUrlTextField.getText();
    }

    public String getLogin() {
        return loginTextField.getText();
    }

    public String getPassword() {
        return passwordTextField.getText();
    }
}
