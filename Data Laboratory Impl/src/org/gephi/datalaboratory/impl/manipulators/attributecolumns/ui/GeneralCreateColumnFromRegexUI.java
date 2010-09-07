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
package org.gephi.datalaboratory.impl.manipulators.attributecolumns.ui;

import java.awt.Color;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeTable;
import org.gephi.datalaboratory.impl.manipulators.attributecolumns.GeneralCreateColumnFromRegex;
import org.gephi.datalaboratory.spi.DialogControls;
import org.gephi.datalaboratory.spi.attributecolumns.AttributeColumnsManipulator;
import org.gephi.datalaboratory.spi.attributecolumns.AttributeColumnsManipulatorUI;
import org.gephi.ui.utils.ColumnTitleValidator;
import org.netbeans.validation.api.ui.ValidationGroup;
import org.netbeans.validation.api.ui.ValidationPanel;
import org.openide.util.NbBundle;

/**
 * UI for CreateBooleanMatchesColumn AttributeColumnsManipulator
 * @author Eduardo Ramos <eduramiba@gmail.com>
 */
public class GeneralCreateColumnFromRegexUI extends javax.swing.JPanel implements AttributeColumnsManipulatorUI {

    private DialogControls dialogControls;
    private AttributeTable table;

    public enum Mode {

        BOOLEAN,
        MATCHING_GROUPS
    }
    private Mode mode = Mode.BOOLEAN;
    private static final Color invalidRegexColor = new Color(254, 150, 150);
    private GeneralCreateColumnFromRegex manipulator;
    private Pattern pattern;

    /** Creates new form CreateBooleanMatchesColumnUI */
    public GeneralCreateColumnFromRegexUI() {
        initComponents();
        regexTextField.getDocument().addDocumentListener(new DocumentListener() {

            public void insertUpdate(DocumentEvent e) {
                refreshPattern();
            }

            public void removeUpdate(DocumentEvent e) {
                refreshPattern();
            }

            public void changedUpdate(DocumentEvent e) {
                refreshPattern();
            }
        });
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

    public void setup(AttributeColumnsManipulator m, AttributeTable table, AttributeColumn column, DialogControls dialogControls) {
        this.manipulator = (GeneralCreateColumnFromRegex) m;
        this.table = table;
        this.dialogControls = dialogControls;
        switch (mode) {
            case BOOLEAN:
                descriptionLabel.setText(NbBundle.getMessage(GeneralCreateColumnFromRegexUI.class, "GeneralCreateColumnFromRegexUI.descriptionLabel.text.boolean", column.getTitle()));
                break;
            case MATCHING_GROUPS:
                descriptionLabel.setText(NbBundle.getMessage(GeneralCreateColumnFromRegexUI.class, "GeneralCreateColumnFromRegexUI.descriptionLabel.text.matching_groups", column.getTitle()));
                break;
        }
        refreshPattern();
    }

    public void unSetup() {
        manipulator.setTitle(titleTextField.getText());
        manipulator.setPattern(pattern);
    }

    public String getDisplayName() {
        return manipulator.getName();
    }

    public JPanel getSettingsPanel() {
        ValidationPanel validationPanel = new ValidationPanel();
        validationPanel.setInnerComponent(this);

        ValidationGroup group = validationPanel.getValidationGroup();

        group.add(titleTextField, new ColumnTitleValidator(table));

        return validationPanel;
    }

    public boolean isModal() {
        return true;
    }

    private void refreshOkButton() {
        String text = titleTextField.getText();
        dialogControls.setOkButtonEnabled(pattern != null && text != null && !text.isEmpty() && !table.hasColumn(text));//Valid regex and title not empty and not repeated.
    }

    private void refreshPattern() {
        //Try to validate the regex and help the user:
        try {
            pattern = Pattern.compile(regexTextField.getText());
            regexTextField.setBackground(Color.WHITE);
        } catch (PatternSyntaxException ex) {
            regexTextField.setBackground(invalidRegexColor);
            pattern = null;
        }
        refreshOkButton();
    }

    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        titleLabel = new javax.swing.JLabel();
        titleTextField = new javax.swing.JTextField();
        regexLabel = new javax.swing.JLabel();
        regexTextField = new javax.swing.JTextField();
        descriptionLabel = new javax.swing.JLabel();

        titleLabel.setText(org.openide.util.NbBundle.getMessage(GeneralCreateColumnFromRegexUI.class, "GeneralCreateColumnFromRegexUI.titleLabel.text")); // NOI18N

        titleTextField.setText(org.openide.util.NbBundle.getMessage(GeneralCreateColumnFromRegexUI.class, "GeneralCreateColumnFromRegexUI.titleTextField.text")); // NOI18N

        regexLabel.setText(org.openide.util.NbBundle.getMessage(GeneralCreateColumnFromRegexUI.class, "GeneralCreateColumnFromRegexUI.regexLabel.text")); // NOI18N

        regexTextField.setText(org.openide.util.NbBundle.getMessage(GeneralCreateColumnFromRegexUI.class, "GeneralCreateColumnFromRegexUI.regexTextField.text")); // NOI18N

        descriptionLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        descriptionLabel.setText(null);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(descriptionLabel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 280, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(regexLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(regexTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 180, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(titleLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(titleTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 184, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(descriptionLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(titleTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(titleLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(regexLabel)
                    .addComponent(regexTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel descriptionLabel;
    private javax.swing.JLabel regexLabel;
    private javax.swing.JTextField regexTextField;
    private javax.swing.JLabel titleLabel;
    private javax.swing.JTextField titleTextField;
    // End of variables declaration//GEN-END:variables
}
