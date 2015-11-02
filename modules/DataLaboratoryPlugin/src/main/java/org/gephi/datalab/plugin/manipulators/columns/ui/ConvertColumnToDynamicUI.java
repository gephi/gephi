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

import java.text.ParseException;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Table;
import org.gephi.datalab.plugin.manipulators.columns.ConvertColumnToDynamic;
import org.gephi.datalab.spi.DialogControls;
import org.gephi.datalab.spi.columns.AttributeColumnsManipulator;
import org.gephi.datalab.spi.columns.AttributeColumnsManipulatorUI;
import org.gephi.graph.api.AttributeUtils;
import org.gephi.ui.utils.ColumnTitleValidator;
import org.gephi.ui.utils.IntervalBoundValidator;
import org.netbeans.validation.api.ui.ValidationGroup;
import org.netbeans.validation.api.ui.ValidationPanel;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 * UI for DuplicateColumn AttributeColumnsManipulator.
 *
 * @author Eduardo Ramos
 */
public class ConvertColumnToDynamicUI extends javax.swing.JPanel implements AttributeColumnsManipulatorUI {
    
    private static final String INTERVAL_START_PREFERENCE = "ConvertColumnToDynamicUI.intervalStart";
    private static final String INTERVAL_END_PREFERENCE = "ConvertColumnToDynamicUI.intervalEnd";
    private static final String REPLACE_COLUMN_PREFERENCE = "ConvertColumnToDynamicUI.replaceColumn";
    
    private static final String DEFAULT_INTERVAL_START = "0";
    private static final String DEFAULT_INTERVAL_END = "1.0";

    private ConvertColumnToDynamic manipulator;
    private Table table;
    private DialogControls dialogControls;
    private ValidationPanel validationPanel;

    /**
     * Creates new form DuplicateColumnUI
     */
    public ConvertColumnToDynamicUI() {
        initComponents();

        intervalStartText.setText(NbPreferences.forModule(ConvertColumnToDynamicUI.class).get(INTERVAL_START_PREFERENCE, DEFAULT_INTERVAL_START));
        intervalEndText.setText(NbPreferences.forModule(ConvertColumnToDynamicUI.class).get(INTERVAL_END_PREFERENCE, DEFAULT_INTERVAL_END));
        replaceColumnCheckbox.setSelected(NbPreferences.forModule(ConvertColumnToDynamicUI.class).getBoolean(REPLACE_COLUMN_PREFERENCE, false));
    }

    private void buildValidationPanel() {
        validationPanel = new ValidationPanel();
        validationPanel.setInnerComponent(this);

        ValidationGroup group = validationPanel.getValidationGroup();

        group.add(titleTextField, new ColumnTitleValidator(table));
        group.add(intervalStartText, new IntervalBoundValidator());
        group.add(intervalEndText, new IntervalBoundValidator(intervalStartText));

        validationPanel.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                dialogControls.setOkButtonEnabled(!validationPanel.isProblem());
            }
        });
    }

    @Override
    public void setup(AttributeColumnsManipulator m, Table table, Column column, DialogControls dialogControls) {
        this.table = table;
        this.dialogControls = dialogControls;
        this.manipulator = (ConvertColumnToDynamic) m;

        buildValidationPanel();

        descriptionLabel.setText(NbBundle.getMessage(ConvertColumnToDynamicUI.class, "ConvertColumnToDynamicUI.descriptionLabel.text", column.getTitle()));
        titleTextField.setText(NbBundle.getMessage(ConvertColumnToDynamicUI.class, "ConvertColumnToDynamicUI.new.title", column.getTitle()));
        
        refreshTitleEnabledState();
    }

    @Override
    public void unSetup() {
        String intervalStart = intervalStartText.getText();
        String intervalEnd = intervalEndText.getText();
        boolean replaceColumn = replaceColumnCheckbox.isSelected();
        
        NbPreferences.forModule(ConvertColumnToDynamicUI.class).put(INTERVAL_START_PREFERENCE, intervalStart);
        NbPreferences.forModule(ConvertColumnToDynamicUI.class).put(INTERVAL_END_PREFERENCE, intervalEnd);
        NbPreferences.forModule(ConvertColumnToDynamicUI.class).putBoolean(REPLACE_COLUMN_PREFERENCE, replaceColumn);
        
        if (!validationPanel.isProblem()) {
            manipulator.setTitle(titleTextField.getText());
            manipulator.setReplaceColumn(replaceColumn);
            manipulator.setLow(AttributeUtils.parseDateTime(intervalStart));
            manipulator.setHigh(AttributeUtils.parseDateTime(intervalEnd));
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
        filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 0));
        intervalStartLabel = new javax.swing.JLabel();
        intervalStartText = new javax.swing.JTextField();
        intervalEndLabel = new javax.swing.JLabel();
        intervalEndText = new javax.swing.JTextField();

        titleLabel.setText(org.openide.util.NbBundle.getMessage(ConvertColumnToDynamicUI.class, "ConvertColumnToDynamicUI.titleLabel.text")); // NOI18N

        titleTextField.setText(org.openide.util.NbBundle.getMessage(ConvertColumnToDynamicUI.class, "ConvertColumnToDynamicUI.titleTextField.text")); // NOI18N

        descriptionLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        descriptionLabel.setText(null);

        replaceColumnCheckbox.setText(org.openide.util.NbBundle.getMessage(ConvertColumnToDynamicUI.class, "ConvertColumnToDynamicUI.replaceColumnCheckbox.text")); // NOI18N
        replaceColumnCheckbox.setToolTipText(org.openide.util.NbBundle.getMessage(ConvertColumnToDynamicUI.class, "ConvertColumnToDynamicUI.replaceColumnCheckbox.toolTipText")); // NOI18N
        replaceColumnCheckbox.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);
        replaceColumnCheckbox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                replaceColumnCheckboxActionPerformed(evt);
            }
        });

        intervalStartLabel.setText(org.openide.util.NbBundle.getMessage(ConvertColumnToDynamicUI.class, "ConvertColumnToDynamicUI.intervalStartLabel.text")); // NOI18N

        intervalStartText.setText(org.openide.util.NbBundle.getMessage(ConvertColumnToDynamicUI.class, "ConvertColumnToDynamicUI.intervalStartText.text")); // NOI18N

        intervalEndLabel.setText(org.openide.util.NbBundle.getMessage(ConvertColumnToDynamicUI.class, "ConvertColumnToDynamicUI.intervalEndLabel.text")); // NOI18N

        intervalEndText.setText(org.openide.util.NbBundle.getMessage(ConvertColumnToDynamicUI.class, "ConvertColumnToDynamicUI.intervalEndText.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(intervalEndLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(intervalStartLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 92, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(intervalStartText)
                            .addComponent(intervalEndText)))
                    .addComponent(descriptionLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(replaceColumnCheckbox)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(filler1, javax.swing.GroupLayout.PREFERRED_SIZE, 4, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(titleLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(titleTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 245, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(descriptionLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(intervalStartLabel)
                    .addComponent(intervalStartText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(intervalEndLabel)
                    .addComponent(intervalEndText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 73, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(filler1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(titleLabel)
                        .addComponent(titleTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(replaceColumnCheckbox)))
                .addGap(24, 24, 24))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void replaceColumnCheckboxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_replaceColumnCheckboxActionPerformed
        refreshTitleEnabledState();
    }//GEN-LAST:event_replaceColumnCheckboxActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel descriptionLabel;
    private javax.swing.Box.Filler filler1;
    private javax.swing.JLabel intervalEndLabel;
    private javax.swing.JTextField intervalEndText;
    private javax.swing.JLabel intervalStartLabel;
    private javax.swing.JTextField intervalStartText;
    private javax.swing.JCheckBox replaceColumnCheckbox;
    private javax.swing.JLabel titleLabel;
    private javax.swing.JTextField titleTextField;
    // End of variables declaration//GEN-END:variables
}
