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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.gephi.datalab.plugin.manipulators.columns.merge.CreateTimeInterval;
import org.gephi.datalab.spi.DialogControls;
import org.gephi.datalab.spi.Manipulator;
import org.gephi.datalab.spi.ManipulatorUI;
import org.gephi.graph.api.Column;
import org.netbeans.validation.api.Problems;
import org.netbeans.validation.api.Validator;
import org.netbeans.validation.api.ui.ValidationGroup;
import org.netbeans.validation.api.ui.ValidationPanel;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 * UI for CreateTimeInterval
 *
 * @author Eduardo Ramos
 */
public class CreateTimeIntervalUI extends javax.swing.JPanel implements ManipulatorUI {

    private static final String PARSE_NUMBERS_SAVED_PARAMETER = "CreateTimeIntervalUI_parseNumbers";
    private static final String START_NUMBER_SAVED_PARAMETER = "CreateTimeIntervalUI_startNumber";
    private static final String END_NUMBER_SAVED_PARAMETER = "CreateTimeIntervalUI_endNumber";
    private static final String DATE_FORMAT_SAVED_PARAMETER = "CreateTimeIntervalUI_dateFormat";//Deprecated
    private static final String DATE_FORMAT_SAVED_PARAMETER_STRING = "CreateTimeIntervalUI_dateFormat_string";
    private static final String START_DATE_SAVED_PARAMETER = "CreateTimeIntervalUI_startDate";
    private static final String END_DATE_SAVED_PARAMETER = "CreateTimeIntervalUI_endDate";
    private CreateTimeInterval manipulator;
    private DialogControls dialogControls;
    private ValidationPanel validationPanel;
    private ColumnWrapper column1, column2;

    /**
     * Creates new form CreateTimeIntervalUI
     */
    public CreateTimeIntervalUI() {
        initComponents();
        
        defaultStartDatePicker.setFormats("yyyy-MM-dd");
        defaultEndDatePicker.setFormats("yyyy-MM-dd");

        //Add some common date formats to choose:
        dateFormatComboBox.addItem("yyyy-MM-dd");
        dateFormatComboBox.addItem("yyyy/MM/dd");
        dateFormatComboBox.addItem("dd-MM-yyyy");
        dateFormatComboBox.addItem("dd/MM/yyyy");
        dateFormatComboBox.addItem("yyyy-MM-dd HH:mm:ss");
        dateFormatComboBox.addItem("yyyy/MM/dd HH:mm:ss");
        dateFormatComboBox.addItem("dd-MM-yyyy HH:mm:ss");
        dateFormatComboBox.addItem("dd/MM/yyyy HH:mm:ss");
        dateFormatComboBox.setSelectedIndex(0);
    }

    private Column getComboBoxColumn(JComboBox comboBox) {
        return ((ColumnWrapper) comboBox.getSelectedItem()).column;
    }

    private void readSavedParameters() {
        parseNumbersRadioButton.setSelected(NbPreferences.forModule(CreateTimeIntervalUI.class).getBoolean(PARSE_NUMBERS_SAVED_PARAMETER, true));
        defaultStartNumberText.setText(NbPreferences.forModule(CreateTimeIntervalUI.class).get(START_NUMBER_SAVED_PARAMETER, ""));
        defaultEndNumberText.setText(NbPreferences.forModule(CreateTimeIntervalUI.class).get(END_NUMBER_SAVED_PARAMETER, ""));
        dateFormatComboBox.setSelectedItem(NbPreferences.forModule(CreateTimeIntervalUI.class).get(DATE_FORMAT_SAVED_PARAMETER_STRING, "yyyy-MM-dd"));
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String date = NbPreferences.forModule(CreateTimeIntervalUI.class).get(START_DATE_SAVED_PARAMETER, "");
            if (!date.isEmpty()) {
                defaultStartDatePicker.setDate(sdf.parse(date));
            }
            date = NbPreferences.forModule(CreateTimeIntervalUI.class).get(END_DATE_SAVED_PARAMETER, "");
            if (!date.isEmpty()) {
                defaultEndDatePicker.setDate(sdf.parse(date));
            }
        } catch (ParseException ex) {
        }
    }

    private void storeSavedParameters() {
        NbPreferences.forModule(CreateTimeIntervalUI.class).putBoolean(PARSE_NUMBERS_SAVED_PARAMETER, parseNumbersRadioButton.isSelected());
        NbPreferences.forModule(CreateTimeIntervalUI.class).put(START_NUMBER_SAVED_PARAMETER, defaultStartNumberText.getText());
        NbPreferences.forModule(CreateTimeIntervalUI.class).put(END_NUMBER_SAVED_PARAMETER, defaultEndNumberText.getText());
        NbPreferences.forModule(CreateTimeIntervalUI.class).put(DATE_FORMAT_SAVED_PARAMETER_STRING, dateFormatComboBox.getSelectedItem().toString());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date date;
        date = defaultStartDatePicker.getDate();
        if (date != null) {
            NbPreferences.forModule(CreateTimeIntervalUI.class).put(START_DATE_SAVED_PARAMETER, sdf.format(date));
        }
        date = defaultEndDatePicker.getDate();
        if (date != null) {
            NbPreferences.forModule(CreateTimeIntervalUI.class).put(END_DATE_SAVED_PARAMETER, sdf.format(date));
        }
    }

    @Override
    public void setup(Manipulator m, DialogControls dialogControls) {
        this.manipulator = (CreateTimeInterval) m;
        this.dialogControls = dialogControls;
        Column[] columns = manipulator.getColumns();
        column1 = new ColumnWrapper(columns[0]);
        if (columns.length == 2) {//2 columns were chosen to merge
            column2 = new ColumnWrapper(columns[1]);
        } else {//Only 1 column was chosen
            column2 = new ColumnWrapper(null);
        }
        startColumnComboBox.addItem(column1);
        startColumnComboBox.addItem(column2);
        endColumnComboBox.addItem(column1);
        endColumnComboBox.addItem(column2);
        if (columns.length == 2) {//Make possible to choose null column even when 2 columns were chosen to merge 
            startColumnComboBox.addItem(new ColumnWrapper(null));
            endColumnComboBox.addItem(new ColumnWrapper(null));
        }

        buildValidationPanel();

        readSavedParameters();
        refreshTimeParseMode();
        refreshOkButton();
    }

    @Override
    public void unSetup() {
        if (dialogControls.isOkButtonEnabled()) {
            boolean parseNumbers = parseNumbersRadioButton.isSelected();
            manipulator.setParseNumbers(parseNumbers);
            manipulator.setStartColumn(getComboBoxColumn(startColumnComboBox));
            manipulator.setEndColumn(getComboBoxColumn(endColumnComboBox));
            if (parseNumbers) {
                if (defaultStartNumberText.getText().trim().isEmpty()) {
                    manipulator.setStartNumber(Double.NEGATIVE_INFINITY);
                } else {
                    manipulator.setStartNumber(Double.parseDouble(defaultStartNumberText.getText().trim()));
                }
                if (defaultEndNumberText.getText().trim().isEmpty()) {
                    manipulator.setEndNumber(Double.POSITIVE_INFINITY);
                } else {
                    manipulator.setEndNumber(Double.parseDouble(defaultEndNumberText.getText().trim()));
                }
            } else {
                SimpleDateFormat dateFormat = new SimpleDateFormat(dateFormatComboBox.getSelectedItem().toString());
                manipulator.setDateFormat(dateFormat);
                manipulator.setStartDate(defaultStartDatePicker.getDate() != null ? dateFormat.format(defaultStartDatePicker.getDate()) : null);
                manipulator.setEndDate(defaultEndDatePicker.getDate() != null ? dateFormat.format(defaultEndDatePicker.getDate()) : null);
            }
        }
        storeSavedParameters();
    }

    @Override
    public String getDisplayName() {
        return manipulator.getName();
    }

    private void buildValidationPanel() {
        validationPanel = new ValidationPanel();
        validationPanel.setInnerComponent(this);

        ValidationGroup group = validationPanel.getValidationGroup();

        group.add(dateFormatComboBox, new Validator<String>() {
            @Override
            public boolean validate(Problems prblms, String string, String t) {
                boolean valid = validateDateFormat(t);
                if(!valid){
                    prblms.add(NbBundle.getMessage(CreateTimeIntervalUI.class, "CreateTimeIntervalUI.invalid.dateformat"));
                }
                return valid;
            }
        });

        Validator<String> emptyOrNumberValidator = new Validator<String>() {
            @Override
            public boolean validate(Problems prblms, String string, String t) {
                boolean valid = validateNumberOrEmpty(t);
                if(!valid){
                    prblms.add(NbBundle.getMessage(CreateTimeIntervalUI.class, "CreateTimeIntervalUI.invalid.number"));
                }
                return valid;
            }
        };
        group.add(defaultStartNumberText, emptyOrNumberValidator);
        group.add(defaultEndNumberText, emptyOrNumberValidator);

        validationPanel.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                refreshOkButton();
            }
        });
    }

    @Override
    public JPanel getSettingsPanel() {

        return validationPanel;
    }

    @Override
    public boolean isModal() {
        return true;
    }

    private boolean validateNumberOrEmpty(String text) {
        text = text.trim();
        if (text.isEmpty()) {
            return true;
        } else {
            try {
                Double.parseDouble(text);
                return true;
            } catch (Exception ex) {
                return false;
            }
        }
    }

    private boolean validateDateFormat(String dateFormat) {
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat);

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void refreshOkButton() {
        boolean enabled = getComboBoxColumn(startColumnComboBox) != null || getComboBoxColumn(endColumnComboBox) != null;//At least 1 column not null
        enabled &= validationPanel != null && !validationPanel.isProblem();
        dialogControls.setOkButtonEnabled(enabled);
    }

    private void refreshTimeParseMode() {
        boolean parseNumbers = parseNumbersRadioButton.isSelected();
        defaultStartNumberLabel.setEnabled(parseNumbers);
        defaultEndNumberLabel.setEnabled(parseNumbers);
        defaultStartNumberText.setEnabled(parseNumbers);
        defaultEndNumberText.setEnabled(parseNumbers);
        dateFormatLabel.setEnabled(!parseNumbers);
        dateFormatComboBox.setEnabled(!parseNumbers);
        dateDefaultStartLabel.setEnabled(!parseNumbers);
        dateDefaultEndLabel.setEnabled(!parseNumbers);
        defaultStartDatePicker.setEnabled(!parseNumbers);
        defaultEndDatePicker.setEnabled(!parseNumbers);
        refreshOkButton();
    }

    private class ColumnWrapper {

        private Column column;

        public ColumnWrapper(Column column) {
            this.column = column;
        }

        @Override
        public String toString() {
            return column != null ? column.getTitle() : "";
        }
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup = new javax.swing.ButtonGroup();
        startColumnLabel = new javax.swing.JLabel();
        startColumnComboBox = new javax.swing.JComboBox();
        endColumnComboBox = new javax.swing.JComboBox();
        endColumnLabel = new javax.swing.JLabel();
        parseNumbersRadioButton = new javax.swing.JRadioButton();
        header = new org.jdesktop.swingx.JXHeader();
        parseDatesRadioButton = new javax.swing.JRadioButton();
        defaultStartDatePicker = new org.jdesktop.swingx.JXDatePicker();
        dateDefaultStartLabel = new javax.swing.JLabel();
        defaultEndDatePicker = new org.jdesktop.swingx.JXDatePicker();
        dateDefaultEndLabel = new javax.swing.JLabel();
        dateFormatLabel = new javax.swing.JLabel();
        dateFormatComboBox = new javax.swing.JComboBox();
        defaultStartNumberLabel = new javax.swing.JLabel();
        defaultEndNumberLabel = new javax.swing.JLabel();
        defaultStartNumberText = new javax.swing.JTextField();
        defaultEndNumberText = new javax.swing.JTextField();

        startColumnLabel.setText(org.openide.util.NbBundle.getMessage(CreateTimeIntervalUI.class, "CreateTimeIntervalUI.startColumnLabel.text")); // NOI18N

        startColumnComboBox.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startColumnComboBoxActionPerformed(evt);
            }
        });

        endColumnComboBox.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                endColumnComboBoxActionPerformed(evt);
            }
        });

        endColumnLabel.setText(org.openide.util.NbBundle.getMessage(CreateTimeIntervalUI.class, "CreateTimeIntervalUI.endColumnLabel.text")); // NOI18N

        buttonGroup.add(parseNumbersRadioButton);
        parseNumbersRadioButton.setSelected(true);
        parseNumbersRadioButton.setText(org.openide.util.NbBundle.getMessage(CreateTimeIntervalUI.class, "CreateTimeIntervalUI.parseNumbersRadioButton.text")); // NOI18N
        parseNumbersRadioButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                parseNumbersRadioButtonActionPerformed(evt);
            }
        });

        header.setDescription(org.openide.util.NbBundle.getMessage(CreateTimeIntervalUI.class, "CreateTimeIntervalUI.header.description")); // NOI18N
        header.setTitle(org.openide.util.NbBundle.getMessage(CreateTimeIntervalUI.class, "CreateTimeIntervalUI.header.title")); // NOI18N

        buttonGroup.add(parseDatesRadioButton);
        parseDatesRadioButton.setText(org.openide.util.NbBundle.getMessage(CreateTimeIntervalUI.class, "CreateTimeIntervalUI.parseDatesRadioButton.text")); // NOI18N
        parseDatesRadioButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                parseDatesRadioButtonActionPerformed(evt);
            }
        });

        defaultStartDatePicker.setEnabled(false);

        dateDefaultStartLabel.setText(org.openide.util.NbBundle.getMessage(CreateTimeIntervalUI.class, "CreateTimeIntervalUI.dateDefaultStartLabel.text")); // NOI18N
        dateDefaultStartLabel.setEnabled(false);

        defaultEndDatePicker.setEnabled(false);

        dateDefaultEndLabel.setText(org.openide.util.NbBundle.getMessage(CreateTimeIntervalUI.class, "CreateTimeIntervalUI.dateDefaultEndLabel.text")); // NOI18N
        dateDefaultEndLabel.setEnabled(false);

        dateFormatLabel.setText(org.openide.util.NbBundle.getMessage(CreateTimeIntervalUI.class, "CreateTimeIntervalUI.dateFormatLabel.text")); // NOI18N
        dateFormatLabel.setEnabled(false);

        dateFormatComboBox.setEditable(true);
        dateFormatComboBox.setEnabled(false);

        defaultStartNumberLabel.setText(org.openide.util.NbBundle.getMessage(CreateTimeIntervalUI.class, "CreateTimeIntervalUI.defaultStartNumberLabel.text")); // NOI18N

        defaultEndNumberLabel.setText(org.openide.util.NbBundle.getMessage(CreateTimeIntervalUI.class, "CreateTimeIntervalUI.defaultEndNumberLabel.text")); // NOI18N

        defaultStartNumberText.setText(org.openide.util.NbBundle.getMessage(CreateTimeIntervalUI.class, "CreateTimeIntervalUI.defaultStartNumberText.text")); // NOI18N

        defaultEndNumberText.setText(org.openide.util.NbBundle.getMessage(CreateTimeIntervalUI.class, "CreateTimeIntervalUI.defaultEndNumberText.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(header, javax.swing.GroupLayout.DEFAULT_SIZE, 664, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                .addGap(15, 15, 15)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(dateDefaultStartLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(dateDefaultEndLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(dateFormatLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addGap(6, 6, 6))))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(13, 13, 13)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(defaultStartNumberLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(defaultEndNumberLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(defaultStartDatePicker, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(defaultEndDatePicker, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(dateFormatComboBox, javax.swing.GroupLayout.Alignment.TRAILING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(defaultStartNumberText)
                            .addComponent(defaultEndNumberText)))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(endColumnLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(startColumnLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(startColumnComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(endColumnComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addComponent(parseDatesRadioButton)
                    .addComponent(parseNumbersRadioButton))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(header, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(startColumnLabel)
                    .addComponent(startColumnComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(endColumnComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(endColumnLabel))
                .addGap(18, 18, 18)
                .addComponent(parseNumbersRadioButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(defaultStartNumberLabel)
                    .addComponent(defaultStartNumberText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(defaultEndNumberLabel)
                    .addComponent(defaultEndNumberText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(33, 33, 33)
                .addComponent(parseDatesRadioButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(dateFormatComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(dateFormatLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(defaultStartDatePicker, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(dateDefaultStartLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(dateDefaultEndLabel)
                    .addComponent(defaultEndDatePicker, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void parseNumbersRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_parseNumbersRadioButtonActionPerformed
        refreshTimeParseMode();
    }//GEN-LAST:event_parseNumbersRadioButtonActionPerformed

    private void parseDatesRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_parseDatesRadioButtonActionPerformed
        refreshTimeParseMode();
    }//GEN-LAST:event_parseDatesRadioButtonActionPerformed

    private void startColumnComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startColumnComboBoxActionPerformed
        refreshOkButton();
    }//GEN-LAST:event_startColumnComboBoxActionPerformed

    private void endColumnComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_endColumnComboBoxActionPerformed
        refreshOkButton();
    }//GEN-LAST:event_endColumnComboBoxActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup;
    private javax.swing.JLabel dateDefaultEndLabel;
    private javax.swing.JLabel dateDefaultStartLabel;
    private javax.swing.JComboBox dateFormatComboBox;
    private javax.swing.JLabel dateFormatLabel;
    private org.jdesktop.swingx.JXDatePicker defaultEndDatePicker;
    private javax.swing.JLabel defaultEndNumberLabel;
    private javax.swing.JTextField defaultEndNumberText;
    private org.jdesktop.swingx.JXDatePicker defaultStartDatePicker;
    private javax.swing.JLabel defaultStartNumberLabel;
    private javax.swing.JTextField defaultStartNumberText;
    private javax.swing.JComboBox endColumnComboBox;
    private javax.swing.JLabel endColumnLabel;
    private org.jdesktop.swingx.JXHeader header;
    private javax.swing.JRadioButton parseDatesRadioButton;
    private javax.swing.JRadioButton parseNumbersRadioButton;
    private javax.swing.JComboBox startColumnComboBox;
    private javax.swing.JLabel startColumnLabel;
    // End of variables declaration//GEN-END:variables
}
