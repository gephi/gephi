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

import java.awt.Color;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Table;
import org.gephi.datalab.plugin.manipulators.columns.GeneralCreateColumnFromRegex;
import org.gephi.datalab.spi.DialogControls;
import org.gephi.datalab.spi.columns.AttributeColumnsManipulator;
import org.gephi.datalab.spi.columns.AttributeColumnsManipulatorUI;
import org.gephi.ui.utils.ColumnTitleValidator;
import org.netbeans.validation.api.ui.ValidationGroup;
import org.netbeans.validation.api.ui.ValidationPanel;
import org.openide.util.NbBundle;

/**
 * UI for CreateBooleanMatchesColumn AttributeColumnsManipulator
 * @author Eduardo Ramos
 */
public class GeneralCreateColumnFromRegexUI extends javax.swing.JPanel implements AttributeColumnsManipulatorUI {

    private DialogControls dialogControls;
    private Table table;

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

            @Override
            public void insertUpdate(DocumentEvent e) {
                refreshPattern();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                refreshPattern();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                refreshPattern();
            }
        });
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
        });
    }

    @Override
    public void setup(AttributeColumnsManipulator m, Table table, Column column, DialogControls dialogControls) {
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

    @Override
    public void unSetup() {
        manipulator.setTitle(titleTextField.getText());
        manipulator.setPattern(pattern);
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
