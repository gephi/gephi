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

import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.gephi.datalab.plugin.manipulators.columns.ConvertColumnToDynamic;
import org.gephi.datalab.spi.DialogControls;
import org.gephi.datalab.spi.columns.AttributeColumnsManipulator;
import org.gephi.datalab.spi.columns.AttributeColumnsManipulatorUI;
import org.gephi.graph.api.AttributeUtils;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Table;
import org.gephi.ui.utils.ColumnTitleValidator;
import org.gephi.ui.utils.IntervalBoundValidator;
import org.netbeans.validation.api.ui.ValidationGroup;
import org.netbeans.validation.api.ui.ValidationPanel;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 * UI for ConvertColumnToDynamic AttributeColumnsManipulator and for <b>Timestamps</b>
 *
 * @author Eduardo Ramos
 */
public class ConvertColumnToDynamicTimestampsUI extends javax.swing.JPanel implements AttributeColumnsManipulatorUI {
    
    private static final String TIMESTAMP_PREFERENCE = "ConvertColumnToDynamicTimestampsUI.intervalStart";
    private static final String REPLACE_COLUMN_PREFERENCE = "ConvertColumnToDynamicTimestampsUI.replaceColumn";
    
    private static final String DEFAULT_TIMESTAMP = "0";

    private ConvertColumnToDynamic manipulator;
    private Table table;
    private DialogControls dialogControls;
    private ValidationPanel validationPanel;

    /**
     * Creates new form DuplicateColumnUI
     */
    public ConvertColumnToDynamicTimestampsUI() {
        initComponents();

        timestampText.setText(NbPreferences.forModule(ConvertColumnToDynamicTimestampsUI.class).get(TIMESTAMP_PREFERENCE, DEFAULT_TIMESTAMP));
        replaceColumnCheckbox.setSelected(NbPreferences.forModule(ConvertColumnToDynamicTimestampsUI.class).getBoolean(REPLACE_COLUMN_PREFERENCE, false));
    }

    private void buildValidationPanel() {
        validationPanel = new ValidationPanel();
        validationPanel.setInnerComponent(this);

        ValidationGroup group = validationPanel.getValidationGroup();

        group.add(titleTextField, new ColumnTitleValidator(table));
        group.add(timestampText, new IntervalBoundValidator());

        validationPanel.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                dialogControls.setOkButtonEnabled(!validationPanel.isProblem());
            }
        });
    }

    @Override
    public void setup(AttributeColumnsManipulator m, GraphModel graphModel, Table table, Column column, DialogControls dialogControls) {
        this.table = table;
        this.dialogControls = dialogControls;
        this.manipulator = (ConvertColumnToDynamic) m;

        buildValidationPanel();

        descriptionLabel.setText(NbBundle.getMessage(ConvertColumnToDynamicTimestampsUI.class, "ConvertColumnToDynamicUI.descriptionLabel.text", column.getTitle()));
        titleTextField.setText(NbBundle.getMessage(ConvertColumnToDynamicTimestampsUI.class, "ConvertColumnToDynamicUI.new.title", column.getTitle()));
        
        refreshTitleEnabledState();
    }

    @Override
    public void unSetup() {
        String timestampString = timestampText.getText();
        boolean replaceColumn = replaceColumnCheckbox.isSelected();
        
        NbPreferences.forModule(ConvertColumnToDynamicTimestampsUI.class).put(TIMESTAMP_PREFERENCE, timestampString);
        NbPreferences.forModule(ConvertColumnToDynamicTimestampsUI.class).putBoolean(REPLACE_COLUMN_PREFERENCE, replaceColumn);
        
        if (!validationPanel.isProblem()) {
            manipulator.setTitle(titleTextField.getText());
            manipulator.setReplaceColumn(replaceColumn);
            
            double timestamp = AttributeUtils.parseDateTimeOrTimestamp(timestampString);
            
            manipulator.setLow(timestamp);
            manipulator.setHigh(timestamp);
        }
    }

    @Override
    public String getDisplayName() {
        return manipulator.getName();
    }

    @Override
    public JPanel getSettingsPanel() {
        return validationPanel;
    }

    @Override
    public boolean isModal() {
        return true;
    }

    private void refreshTitleEnabledState() {
        boolean enabled = !replaceColumnCheckbox.isSelected();
        titleTextField.setEnabled(enabled);
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        titleLabel = new javax.swing.JLabel();
        titleTextField = new javax.swing.JTextField();
        descriptionLabel = new javax.swing.JLabel();
        replaceColumnCheckbox = new javax.swing.JCheckBox();
        timestampLabel = new javax.swing.JLabel();
        timestampText = new javax.swing.JTextField();

        titleLabel.setText(org.openide.util.NbBundle.getMessage(ConvertColumnToDynamicTimestampsUI.class, "ConvertColumnToDynamicTimestampsUI.titleLabel.text")); // NOI18N

        titleTextField.setText(org.openide.util.NbBundle.getMessage(ConvertColumnToDynamicTimestampsUI.class, "ConvertColumnToDynamicTimestampsUI.titleTextField.text")); // NOI18N

        descriptionLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        descriptionLabel.setText(null);

        replaceColumnCheckbox.setText(org.openide.util.NbBundle.getMessage(ConvertColumnToDynamicTimestampsUI.class, "ConvertColumnToDynamicTimestampsUI.replaceColumnCheckbox.text")); // NOI18N
        replaceColumnCheckbox.setToolTipText(org.openide.util.NbBundle.getMessage(ConvertColumnToDynamicTimestampsUI.class, "ConvertColumnToDynamicTimestampsUI.replaceColumnCheckbox.toolTipText")); // NOI18N
        replaceColumnCheckbox.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);
        replaceColumnCheckbox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                replaceColumnCheckboxActionPerformed(evt);
            }
        });

        timestampLabel.setText(org.openide.util.NbBundle.getMessage(ConvertColumnToDynamicTimestampsUI.class, "ConvertColumnToDynamicTimestampsUI.timestampLabel.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(descriptionLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(replaceColumnCheckbox)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(titleLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(titleTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 177, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(timestampLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 108, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(timestampText)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(descriptionLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(timestampLabel)
                    .addComponent(timestampText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(titleLabel)
                    .addComponent(titleTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(replaceColumnCheckbox))
                .addGap(24, 24, 24))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void replaceColumnCheckboxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_replaceColumnCheckboxActionPerformed
        refreshTitleEnabledState();
    }//GEN-LAST:event_replaceColumnCheckboxActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel descriptionLabel;
    private javax.swing.JCheckBox replaceColumnCheckbox;
    private javax.swing.JLabel timestampLabel;
    private javax.swing.JTextField timestampText;
    private javax.swing.JLabel titleLabel;
    private javax.swing.JTextField titleTextField;
    // End of variables declaration//GEN-END:variables
}
