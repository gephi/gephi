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
package org.gephi.datalab.plugin.manipulators.columns.ui;

import java.util.List;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.gephi.datalab.plugin.manipulators.columns.DuplicateColumn;
import org.gephi.datalab.spi.DialogControls;
import org.gephi.datalab.spi.columns.AttributeColumnsManipulator;
import org.gephi.datalab.spi.columns.AttributeColumnsManipulatorUI;
import org.gephi.datalab.utils.SupportedColumnTypeWrapper;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Table;
import org.gephi.ui.utils.ColumnTitleValidator;
import org.netbeans.validation.api.ui.ValidationGroup;
import org.netbeans.validation.api.ui.ValidationPanel;
import org.openide.util.NbBundle;

/**
 * UI for DuplicateColumn AttributeColumnsManipulator.
 *
 * @author Eduardo Ramos
 */
public class DuplicateColumnUI extends javax.swing.JPanel implements AttributeColumnsManipulatorUI {

    private DuplicateColumn manipulator;
    private Table table;
    private DialogControls dialogControls;

    /**
     * Creates new form DuplicateColumnUI
     */
    public DuplicateColumnUI() {
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
    public void setup(AttributeColumnsManipulator m, GraphModel graphModel, Table table, Column column, DialogControls dialogControls) {
        this.table = table;
        this.dialogControls = dialogControls;
        this.manipulator = (DuplicateColumn) m;

        descriptionLabel.setText(NbBundle.getMessage(DuplicateColumnUI.class, "DuplicateColumnUI.descriptionLabel.text", column.getTitle()));
        titleTextField.setText(NbBundle.getMessage(DuplicateColumnUI.class, "DuplicateColumnUI.new.title", column.getTitle()));
        
        List<SupportedColumnTypeWrapper> supportedTypesWrappers = SupportedColumnTypeWrapper.buildOrderedSupportedTypesList(graphModel);

        for (SupportedColumnTypeWrapper supportedColumnTypeWrapper : supportedTypesWrappers) {
            typeComboBox.addItem(supportedColumnTypeWrapper);
        }

        typeComboBox.setSelectedItem(new SupportedColumnTypeWrapper(column.getTypeClass()));
    }

    @Override
    public void unSetup() {
        manipulator.setColumnType(((SupportedColumnTypeWrapper) typeComboBox.getSelectedItem()).getType());
        manipulator.setTitle(titleTextField.getText());
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

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        titleLabel = new javax.swing.JLabel();
        titleTextField = new javax.swing.JTextField();
        typeLabel = new javax.swing.JLabel();
        typeComboBox = new javax.swing.JComboBox();
        descriptionLabel = new javax.swing.JLabel();

        titleLabel.setText(org.openide.util.NbBundle.getMessage(DuplicateColumnUI.class, "DuplicateColumnUI.titleLabel.text")); // NOI18N

        titleTextField.setText(org.openide.util.NbBundle.getMessage(DuplicateColumnUI.class, "DuplicateColumnUI.titleTextField.text")); // NOI18N

        typeLabel.setText(org.openide.util.NbBundle.getMessage(DuplicateColumnUI.class, "DuplicateColumnUI.typeLabel.text")); // NOI18N

        descriptionLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        descriptionLabel.setText(null);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(descriptionLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 299, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addComponent(titleLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(titleTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 203, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addComponent(typeLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(typeComboBox, 0, 199, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(descriptionLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
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
