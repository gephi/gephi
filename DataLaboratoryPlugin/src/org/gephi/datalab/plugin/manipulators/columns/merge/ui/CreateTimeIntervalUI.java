/*
Copyright 2008-2010 Gephi
Authors : Eduardo Ramos <eduramiba@gmail.com>
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gephi.datalab.plugin.manipulators.columns.merge.ui;

import java.awt.Color;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.datalab.plugin.manipulators.columns.merge.CreateTimeInterval;
import org.gephi.datalab.spi.DialogControls;
import org.gephi.datalab.spi.Manipulator;
import org.gephi.datalab.spi.ManipulatorUI;
import org.openide.util.NbPreferences;

/**
 * UI for CreateTimeInterval
 * @author Eduardo Ramos <eduramiba@gmail.com>
 */
public class CreateTimeIntervalUI extends javax.swing.JPanel implements ManipulatorUI {

    private static final Color INVALID_NUMBER_COLOR = new Color(254, 150, 150);
    private static final String PARSE_NUMBERS_SAVED_PARAMETER = "CreateTimeIntervalUI_parseNumbers";
    private static final String START_NUMBER_SAVED_PARAMETER = "CreateTimeIntervalUI_startNumber";
    private static final String END_NUMBER_SAVED_PARAMETER = "CreateTimeIntervalUI_endNumber";
    private static final String DATE_FORMAT_SAVED_PARAMETER = "CreateTimeIntervalUI_dateFormat";
    private static final String START_DATE_SAVED_PARAMETER = "CreateTimeIntervalUI_startDate";
    private static final String END_DATE_SAVED_PARAMETER = "CreateTimeIntervalUI_endDate";
    private CreateTimeInterval manipulator;
    private DialogControls dialogControls;
    private ColumnWrapper column1, column2;

    /** Creates new form CreateTimeIntervalUI */
    public CreateTimeIntervalUI() {
        initComponents();
        defaultStartNumberText.getDocument().addDocumentListener(new DocumentListener() {

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
        defaultEndNumberText.getDocument().addDocumentListener(new DocumentListener() {

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

        //Add some common date formats to choose:
        dateFormatComboBox.addItem("yyyy-MM-dd");
        dateFormatComboBox.addItem("yyyy/MM/dd");
        dateFormatComboBox.addItem("dd-MM-yyyy");
        dateFormatComboBox.addItem("dd/MM/yyyy");
        dateFormatComboBox.addItem("MM-dd-yyyy");
        dateFormatComboBox.addItem("MM/dd/yyyy");
        dateFormatComboBox.setSelectedIndex(0);
    }

    private void readSavedParameters() {
        parseNumbersRadioButton.setSelected(NbPreferences.forModule(CreateTimeIntervalUI.class).getBoolean(PARSE_NUMBERS_SAVED_PARAMETER, true));
        defaultStartNumberText.setText(NbPreferences.forModule(CreateTimeIntervalUI.class).get(START_NUMBER_SAVED_PARAMETER, ""));
        defaultEndNumberText.setText(NbPreferences.forModule(CreateTimeIntervalUI.class).get(END_NUMBER_SAVED_PARAMETER, ""));
        dateFormatComboBox.setSelectedIndex(NbPreferences.forModule(CreateTimeIntervalUI.class).getInt(DATE_FORMAT_SAVED_PARAMETER, 0));
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
        NbPreferences.forModule(CreateTimeIntervalUI.class).putInt(DATE_FORMAT_SAVED_PARAMETER, dateFormatComboBox.getSelectedIndex());
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

    public void setup(Manipulator m, DialogControls dialogControls) {
        this.manipulator = (CreateTimeInterval) m;
        this.dialogControls = dialogControls;
        AttributeColumn[] columns = manipulator.getColumns();
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
        readSavedParameters();
        refreshTimeParseMode();
        refreshOkButton();
    }

    public void unSetup() {
        if (dialogControls.isOkButtonEnabled()) {
            boolean parseNumbers = parseNumbersRadioButton.isSelected();
            manipulator.setParseNumbers(parseNumbers);
            manipulator.setStartColumn(((ColumnWrapper) startColumnComboBox.getSelectedItem()).column);
            manipulator.setEndColumn(((ColumnWrapper) endColumnComboBox.getSelectedItem()).column);
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
                SimpleDateFormat dateFormat = new SimpleDateFormat((String) dateFormatComboBox.getSelectedItem());
                manipulator.setDateFormat(dateFormat);
                manipulator.setStartDate(defaultStartDatePicker.getDate() != null ? dateFormat.format(defaultStartDatePicker.getDate()) : null);
                manipulator.setEndDate(defaultEndDatePicker.getDate() != null ? dateFormat.format(defaultEndDatePicker.getDate()) : null);
            }
        }
        storeSavedParameters();
    }

    public String getDisplayName() {
        return manipulator.getName();
    }

    public JPanel getSettingsPanel() {
        return this;
    }

    public boolean isModal() {
        return true;
    }

    private boolean validateNumberOrEmpty(JTextField textField) {
        if (parseNumbersRadioButton.isSelected()) {
            String text = textField.getText().trim();
            if (text.isEmpty()) {
                textField.setBackground(Color.WHITE);
                return true;
            } else {
                try {
                    Double.parseDouble(text);
                    textField.setBackground(Color.WHITE);
                    return true;
                } catch (Exception ex) {
                    textField.setBackground(INVALID_NUMBER_COLOR);
                    return false;
                }
            }
        } else {
            textField.setBackground(Color.WHITE);
            return true;
        }
    }

    private void refreshOkButton() {
        boolean enabled=validateNumberOrEmpty(defaultStartNumberText);
        enabled=validateNumberOrEmpty(defaultEndNumberText)&&enabled;
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

        private AttributeColumn column;

        public ColumnWrapper(AttributeColumn column) {
            this.column = column;
        }

        @Override
        public String toString() {
            return column != null ? column.getTitle() : "";
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
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
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startColumnComboBoxActionPerformed(evt);
            }
        });

        endColumnComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                endColumnComboBoxActionPerformed(evt);
            }
        });

        endColumnLabel.setText(org.openide.util.NbBundle.getMessage(CreateTimeIntervalUI.class, "CreateTimeIntervalUI.endColumnLabel.text")); // NOI18N

        buttonGroup.add(parseNumbersRadioButton);
        parseNumbersRadioButton.setSelected(true);
        parseNumbersRadioButton.setText(org.openide.util.NbBundle.getMessage(CreateTimeIntervalUI.class, "CreateTimeIntervalUI.parseNumbersRadioButton.text")); // NOI18N
        parseNumbersRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                parseNumbersRadioButtonActionPerformed(evt);
            }
        });

        header.setDescription(org.openide.util.NbBundle.getMessage(CreateTimeIntervalUI.class, "CreateTimeIntervalUI.header.description")); // NOI18N
        header.setTitle(org.openide.util.NbBundle.getMessage(CreateTimeIntervalUI.class, "CreateTimeIntervalUI.header.title")); // NOI18N

        buttonGroup.add(parseDatesRadioButton);
        parseDatesRadioButton.setText(org.openide.util.NbBundle.getMessage(CreateTimeIntervalUI.class, "CreateTimeIntervalUI.parseDatesRadioButton.text")); // NOI18N
        parseDatesRadioButton.addActionListener(new java.awt.event.ActionListener() {
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

        dateFormatComboBox.setEnabled(false);

        defaultStartNumberLabel.setText(org.openide.util.NbBundle.getMessage(CreateTimeIntervalUI.class, "CreateTimeIntervalUI.defaultStartNumberLabel.text")); // NOI18N

        defaultEndNumberLabel.setText(org.openide.util.NbBundle.getMessage(CreateTimeIntervalUI.class, "CreateTimeIntervalUI.defaultEndNumberLabel.text")); // NOI18N

        defaultStartNumberText.setText(org.openide.util.NbBundle.getMessage(CreateTimeIntervalUI.class, "CreateTimeIntervalUI.defaultStartNumberText.text")); // NOI18N

        defaultEndNumberText.setText(org.openide.util.NbBundle.getMessage(CreateTimeIntervalUI.class, "CreateTimeIntervalUI.defaultEndNumberText.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(endColumnLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(startColumnLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 101, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(startColumnComboBox, 0, 180, Short.MAX_VALUE)
                    .addComponent(endColumnComboBox, 0, 180, Short.MAX_VALUE))
                .addContainerGap())
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(parseDatesRadioButton)
                .addContainerGap(216, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(parseNumbersRadioButton)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(defaultStartNumberLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 102, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                .addGap(15, 15, 15)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(dateDefaultStartLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(dateDefaultEndLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(dateFormatLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addComponent(defaultEndNumberLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 102, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(defaultStartDatePicker, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 170, Short.MAX_VALUE)
                            .addComponent(defaultEndDatePicker, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 170, Short.MAX_VALUE)
                            .addComponent(dateFormatComboBox, javax.swing.GroupLayout.Alignment.TRAILING, 0, 170, Short.MAX_VALUE)
                            .addComponent(defaultStartNumberText, javax.swing.GroupLayout.DEFAULT_SIZE, 170, Short.MAX_VALUE)
                            .addComponent(defaultEndNumberText, javax.swing.GroupLayout.DEFAULT_SIZE, 170, Short.MAX_VALUE))))
                .addContainerGap())
            .addComponent(header, javax.swing.GroupLayout.DEFAULT_SIZE, 305, Short.MAX_VALUE)
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
        if (startColumnComboBox.getSelectedItem() == column1) {
            endColumnComboBox.setSelectedItem(column2);
        } else {
            endColumnComboBox.setSelectedItem(column1);
        }
    }//GEN-LAST:event_startColumnComboBoxActionPerformed

    private void endColumnComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_endColumnComboBoxActionPerformed
        if (endColumnComboBox.getSelectedItem() == column1) {
            startColumnComboBox.setSelectedItem(column2);
        } else {
            startColumnComboBox.setSelectedItem(column1);
        }
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
