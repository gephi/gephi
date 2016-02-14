/*
 Copyright 2008-2010 Gephi
 Authors : Eduardo Ramos <eduramiba@gmail.com>
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
package org.gephi.datalab.plugin.manipulators.columns.merge.ui;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.gephi.datalab.api.AttributeColumnsMergeStrategiesController.BooleanOperations;
import org.gephi.datalab.plugin.manipulators.columns.merge.BooleanLogicOperations;
import org.gephi.datalab.spi.DialogControls;
import org.gephi.datalab.spi.Manipulator;
import org.gephi.datalab.spi.ManipulatorUI;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Table;
import org.gephi.ui.utils.ColumnTitleValidator;
import org.netbeans.validation.api.ui.ValidationGroup;
import org.netbeans.validation.api.ui.ValidationPanel;

/**
 * UI for BooleanLogicOperations AttributeColumnsMergeStrategy
 *
 * @author Eduardo Ramos
 */
public class BooleanLogicOperationsUI extends javax.swing.JPanel implements ManipulatorUI {

    private BooleanLogicOperations manipulator;
    private JComboBox[] operationSelectors;
    private Table table;
    private DialogControls dialogControls;

    /**
     * Creates new form BooleanLogicOperationsUI
     */
    public BooleanLogicOperationsUI() {
        initComponents();
        titleTextField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                refreshOkButton();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                refreshOkButton();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                refreshOkButton();
            }

            private void refreshOkButton() {
                String text = titleTextField.getText();
                dialogControls.setOkButtonEnabled(text != null && !text.isEmpty() && !table.hasColumn(text));//Title not empty and not repeated.
            }
        });
    }

    @Override
    public void setup(Manipulator m, DialogControls dialogControls) {
        manipulator = (BooleanLogicOperations) m;
        this.dialogControls = dialogControls;
        this.table = manipulator.getTable();
        prepareColumnsAndOperations();
    }

    @Override
    public void unSetup() {
        BooleanOperations[] booleanOperations = new BooleanOperations[operationSelectors.length];
        for (int i = 0; i < booleanOperations.length; i++) {
            booleanOperations[i] = (BooleanOperations) operationSelectors[i].getSelectedItem();
        }

        manipulator.setBooleanOperations(booleanOperations);
        manipulator.setNewColumnTitle(titleTextField.getText());
    }

    @Override
    public String getDisplayName() {
        return manipulator.getName();
    }

    @Override
    public JPanel getSettingsPanel() {
        ValidationPanel validationPanel = new ValidationPanel();
        validationPanel.setInnerComponent(this);

        ValidationGroup group = validationPanel.getValidationGroup();

        group.add(titleTextField, new ColumnTitleValidator(table));

        return validationPanel;
    }

    @Override
    public boolean isModal() {
        return true;
    }

    private void prepareColumnsAndOperations() {
        Column[] columns = manipulator.getColumns();
        operationSelectors = new JComboBox[columns.length - 1];

        JLabel columnLabel;
        for (int i = 0; i < columns.length; i++) {
            columnLabel = new JLabel(columns[i].getTitle());
            columnLabel.setHorizontalAlignment(JLabel.CENTER);
            panel.add(columnLabel);
            if (i < columns.length - 1) {
                operationSelectors[i] = prepareOperationSelector();
                panel.add(operationSelectors[i]);
            }
        }
    }

    private JComboBox prepareOperationSelector() {
        JComboBox selector = new JComboBox(BooleanOperations.values());
        return selector;
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        descriptionLabel = new javax.swing.JLabel();
        scroll = new javax.swing.JScrollPane();
        panel = new javax.swing.JPanel();
        titleLabel = new javax.swing.JLabel();
        titleTextField = new javax.swing.JTextField();

        descriptionLabel.setText(org.openide.util.NbBundle.getMessage(BooleanLogicOperationsUI.class, "BooleanLogicOperationsUI.descriptionLabel.text")); // NOI18N

        panel.setLayout(new java.awt.GridLayout(0, 1, 0, 20));
        scroll.setViewportView(panel);

        titleLabel.setText(org.openide.util.NbBundle.getMessage(BooleanLogicOperationsUI.class, "BooleanLogicOperationsUI.titleLabel.text")); // NOI18N

        titleTextField.setText(org.openide.util.NbBundle.getMessage(BooleanLogicOperationsUI.class, "BooleanLogicOperationsUI.titleTextField.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(descriptionLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 254, Short.MAX_VALUE)
                    .addComponent(scroll, javax.swing.GroupLayout.DEFAULT_SIZE, 254, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(titleLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(titleTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 135, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(titleLabel)
                    .addComponent(titleTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(descriptionLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(scroll, javax.swing.GroupLayout.DEFAULT_SIZE, 105, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel descriptionLabel;
    private javax.swing.JPanel panel;
    private javax.swing.JScrollPane scroll;
    private javax.swing.JLabel titleLabel;
    private javax.swing.JTextField titleTextField;
    // End of variables declaration//GEN-END:variables
}
