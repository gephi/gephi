/*
Copyright 2008-2010 Gephi
Authors : Eduardo Ramos <eduramiba@gmail.com>
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
(at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gephi.desktop.datalab.general.actions;

import javax.swing.JButton;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeTable;
import org.gephi.data.attributes.api.AttributeType;
import org.gephi.datalab.api.AttributeColumnsController;
import org.gephi.ui.utils.ColumnTitleValidator;
import org.netbeans.validation.api.ui.ValidationGroup;
import org.netbeans.validation.api.ui.ValidationPanel;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 *
 * @author Eduardo Ramos <eduramiba@gmail.com>
 */
public class AddColumnUI extends javax.swing.JPanel {

    private static final String COLUMN_TYPE_SAVED_PREFERENCES = "AddColumnUI_type";
    private AttributeTable table;
    private JButton okButton;

    public enum Mode {

        NODES_TABLE, EDGES_TABLE;
    }

    /** Creates new form AddColumnUI */
    public AddColumnUI() {
        initComponents();
        titleTextField.getDocument().addDocumentListener(new DocumentListener() {

            public void insertUpdate(DocumentEvent e) {
                refreshOkButton();
            }

            public void removeUpdate(DocumentEvent e) {
                refreshOkButton();
            }

            public void changedUpdate(DocumentEvent e) {
                refreshOkButton();
            }
        });
    }

    public void unSetup(){
        NbPreferences.forModule(AddColumnUI.class).putInt(COLUMN_TYPE_SAVED_PREFERENCES, typeComboBox.getSelectedIndex());
    }

    public String getDisplayName() {
        return NbBundle.getMessage(AddColumnUI.class, "AddColumnUI.title");
    }

    /**
     * Setup the mode of column creation: nodes table or edges table.
     * @param mode Mode
     */
    public void setup(Mode mode) {
        AttributeController ac = Lookup.getDefault().lookup(AttributeController.class);
        //Set description text for the mode of column creation:
        switch (mode) {
            case NODES_TABLE:
                descriptionLabel.setText(NbBundle.getMessage(AddColumnUI.class, "AddColumnUI.descriptionLabel.text.nodes"));
                table = ac.getModel().getNodeTable();
                break;
            case EDGES_TABLE:
                descriptionLabel.setText(NbBundle.getMessage(AddColumnUI.class, "AddColumnUI.descriptionLabel.text.edges"));
                table = ac.getModel().getEdgeTable();
                break;
        }

        for (AttributeType type : AttributeType.values()) {
            typeComboBox.addItem(type);
        }

        int savedType=NbPreferences.forModule(AddColumnUI.class).getInt(COLUMN_TYPE_SAVED_PREFERENCES, -1);
        //Set last saved type or String by default:
        if (savedType != -1) {
            typeComboBox.setSelectedIndex(savedType);
        } else {
            typeComboBox.setSelectedItem(AttributeType.STRING);
        }
    }

    /**
     * Execute the creation of the column, with the given parameters in setup and with the interface itself.
     */
    public void execute() {
        Lookup.getDefault().lookup(AttributeColumnsController.class).addAttributeColumn(table, titleTextField.getText(), (AttributeType) typeComboBox.getSelectedItem());
    }

    public void setOkButton(JButton okButton) {
        this.okButton = okButton;
        refreshOkButton();
    }

    private void refreshOkButton() {
        String title = titleTextField.getText();
        if (okButton != null) {
            okButton.setEnabled(title != null && !title.isEmpty() && !table.hasColumn(title));
        }
    }

    public static ValidationPanel createValidationPanel(AddColumnUI innerPanel) {
        ValidationPanel validationPanel = new ValidationPanel();
        if (innerPanel == null) {
            innerPanel = new AddColumnUI();
        }
        validationPanel.setInnerComponent(innerPanel);

        ValidationGroup group = validationPanel.getValidationGroup();

        group.add(innerPanel.titleTextField, new ColumnTitleValidator(innerPanel.table));

        return validationPanel;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        descriptionLabel = new javax.swing.JLabel();
        titleLabel = new javax.swing.JLabel();
        titleTextField = new javax.swing.JTextField();
        typeLabel = new javax.swing.JLabel();
        typeComboBox = new javax.swing.JComboBox();

        descriptionLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        descriptionLabel.setText(null);

        titleLabel.setText(org.openide.util.NbBundle.getMessage(AddColumnUI.class, "AddColumnUI.titleLabel.text")); // NOI18N

        titleTextField.setText(org.openide.util.NbBundle.getMessage(AddColumnUI.class, "AddColumnUI.titleTextField.text")); // NOI18N

        typeLabel.setText(org.openide.util.NbBundle.getMessage(AddColumnUI.class, "AddColumnUI.typeLabel.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(descriptionLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 299, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(titleLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(titleTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 203, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(typeLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(typeComboBox, 0, 199, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(descriptionLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(titleLabel)
                    .addComponent(titleTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(typeLabel)
                    .addComponent(typeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel descriptionLabel;
    private javax.swing.JLabel titleLabel;
    private javax.swing.JTextField titleTextField;
    private javax.swing.JComboBox typeComboBox;
    private javax.swing.JLabel typeLabel;
    // End of variables declaration//GEN-END:variables
}
