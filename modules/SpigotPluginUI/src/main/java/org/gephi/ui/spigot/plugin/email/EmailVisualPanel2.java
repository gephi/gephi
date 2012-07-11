/*
Copyright 2008-2010 Gephi
Authors : Yi Du <duyi001@gmail.com>
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
package org.gephi.ui.spigot.plugin.email;

import com.csvreader.CsvReader;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.event.CellEditorListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import org.gephi.io.importer.spi.SpigotImporter;
import org.gephi.io.spigot.plugin.EmailImporter;
import org.gephi.io.spigot.plugin.email.EmailDataType;
import org.gephi.ui.utils.DialogFileFilter;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

public final class EmailVisualPanel2 extends JPanel {

    private static final String CSV_LAST_PATH = "EmailVisualPanel2_csv_lastpath";

    /** Creates new form EmailVisualPanel2 */
    public EmailVisualPanel2() {
        configFilterTableModel();
        initComponents();
        setEnableFilters();

        importCSVButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                javax.swing.JFileChooser fileChooser = new javax.swing.JFileChooser();
                fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                DialogFileFilter fileFilter = new DialogFileFilter(NbBundle.getMessage(EmailVisualPanel2.class, "fileType_CSV_Name"));
                fileFilter.addExtension("csv");
                fileChooser.setFileFilter(fileFilter);
                String lastPath = NbPreferences.forModule(EmailVisualPanel2.class).get(CSV_LAST_PATH, "");
                fileChooser.setCurrentDirectory(new File(lastPath));
                int returnValue = fileChooser.showOpenDialog(EmailVisualPanel2.this);
                if (returnValue == javax.swing.JFileChooser.APPROVE_OPTION) {
                    NbPreferences.forModule(EmailVisualPanel2.class).put(CSV_LAST_PATH, fileChooser.getCurrentDirectory().getAbsolutePath());
                    File csvFile = fileChooser.getSelectedFile();
                    try {
                        CsvReader csvReader = new CsvReader(new FileInputStream(csvFile), Charset.forName("UTF-8"));
                        csvReader.setSkipEmptyRecords(true);
                        List<String[]> rows = new ArrayList<String[]>();
                        while (csvReader.readRecord()) {
                            String[] values = csvReader.getValues();
                            if (values.length > 0) {
                                String email = values[0];
                                String type = values.length > 1 ? values[1] : "All";
                                if (!email.isEmpty() && (type.equals("From")
                                        || type.equals("To")
                                        || type.equals("Cc")
                                        || type.equals("Bcc")
                                        || type.equals("All"))) {
                                    rows.add(new String[]{email, type});
                                }
                            }
                        }
                        csvReader.close();

                        //Clean Rows
                        filterTableModel.setRowCount(0);

                        //Add
                        for (String[] row : rows) {
                            filterTableModel.addRow(row);
                        }
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }

                }

            }
        });
    }

    @Override
    public String getName() {
        return "Do Filtering";
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        buttonGroup2 = new javax.swing.ButtonGroup();
        buttonGroup3 = new javax.swing.ButtonGroup();
        jLabel1 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jCheckBoxEmailAddrFilter = new javax.swing.JCheckBox();
        jScrollPane1 = new javax.swing.JScrollPane();
        importCSVButton = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jCheckBoxDayAfter = new javax.swing.JCheckBox();
        jCheckBoxDayBefore = new javax.swing.JCheckBox();
        jPanelDayAfter = new javax.swing.JPanel();
        jPanelDayBefore = new javax.swing.JPanel();
        jCheckBoxMessageInclude1 = new javax.swing.JCheckBox();
        jTextFieldMessageInclude1 = new javax.swing.JTextField();
        jRadioButtonHasNoAtta = new javax.swing.JRadioButton();
        jRadioButtonHasNoCc = new javax.swing.JRadioButton();
        jRadioButtonHasNoBcc = new javax.swing.JRadioButton();
        jRadioButtonHasBcc = new javax.swing.JRadioButton();
        jRadioButtonHasCc = new javax.swing.JRadioButton();
        jTextFieldSubjectInclude = new javax.swing.JTextField();
        jCheckBoxSubjectInclude = new javax.swing.JCheckBox();
        jCheckBoxAttachement = new javax.swing.JCheckBox();
        jCheckBoxCc = new javax.swing.JCheckBox();
        jCheckBoxBcc = new javax.swing.JCheckBox();
        jRadioButtonHasAtta = new javax.swing.JRadioButton();

        setMaximumSize(new java.awt.Dimension(570, 360));
        setMinimumSize(new java.awt.Dimension(570, 360));
        setPreferredSize(new java.awt.Dimension(570, 360));

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(EmailVisualPanel2.class, "EmailVisualPanel2.jLabel1.text")); // NOI18N

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        org.openide.awt.Mnemonics.setLocalizedText(jCheckBoxEmailAddrFilter, org.openide.util.NbBundle.getMessage(EmailVisualPanel2.class, "EmailVisualPanel2.jCheckBoxEmailAddrFilter.text")); // NOI18N
        jCheckBoxEmailAddrFilter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxEmailAddrFilterActionPerformed(evt);
            }
        });

        jTableFilter.setModel(filterTableModel);
        javax.swing.JComboBox comboBox = new javax.swing.JComboBox();
        comboBox.addItem("All");
        comboBox.addItem("From");
        comboBox.addItem("To");
        comboBox.addItem("Cc");
        comboBox.addItem("Bcc");
        javax.swing.table.TableColumn tableColumn = jTableFilter.getColumn(org.openide.util.NbBundle.getMessage(
            EmailVisualPanel2.class, "EmailVisualPanel2.jTableFilter.column2.text"));
    tableColumn.setCellEditor(new javax.swing.DefaultCellEditor(comboBox));
    comboBox.addItemListener(new java.awt.event.ItemListener(){
        public void itemStateChanged(java.awt.event.ItemEvent e) {
            if(!e.getItem().equals("")){
                ((javax.swing.table.DefaultTableModel)jTableFilter.getModel()).addRow(new java.util.Vector(2));
            }
        }
    });
    jScrollPane1.setViewportView(jTableFilter);

    org.openide.awt.Mnemonics.setLocalizedText(importCSVButton, org.openide.util.NbBundle.getMessage(EmailVisualPanel2.class, "EmailVisualPanel2.importCSVButton.text")); // NOI18N
    importCSVButton.setToolTipText(org.openide.util.NbBundle.getMessage(EmailVisualPanel2.class, "EmailVisualPanel2.importCSVButton.toolTipText")); // NOI18N
    importCSVButton.setEnabled(false);

    javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
    jPanel1.setLayout(jPanel1Layout);
    jPanel1Layout.setHorizontalGroup(
        jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addComponent(jCheckBoxEmailAddrFilter, javax.swing.GroupLayout.DEFAULT_SIZE, 657, Short.MAX_VALUE)
        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
            .addContainerGap()
            .addComponent(importCSVButton))
        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 657, Short.MAX_VALUE)
    );
    jPanel1Layout.setVerticalGroup(
        jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(jPanel1Layout.createSequentialGroup()
            .addComponent(jCheckBoxEmailAddrFilter)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 56, Short.MAX_VALUE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(importCSVButton))
    );

    jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(EmailVisualPanel2.class, "EmailVisualPanel2.jPanel3.border.title"))); // NOI18N

    org.openide.awt.Mnemonics.setLocalizedText(jCheckBoxDayAfter, org.openide.util.NbBundle.getMessage(EmailVisualPanel2.class, "EmailVisualPanel2.jCheckBoxDayAfter.text")); // NOI18N
    jCheckBoxDayAfter.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            jCheckBoxDayAfterActionPerformed(evt);
        }
    });

    org.openide.awt.Mnemonics.setLocalizedText(jCheckBoxDayBefore, org.openide.util.NbBundle.getMessage(EmailVisualPanel2.class, "EmailVisualPanel2.jCheckBoxDayBefore.text")); // NOI18N
    jCheckBoxDayBefore.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            jCheckBoxDayBeforeActionPerformed(evt);
        }
    });

    jDateChooserAfter = new com.toedter.calendar.JDateChooser();
    jDateChooserAfter.setSize(130, 20);
    jPanelDayAfter.add(jDateChooserAfter,java.awt.BorderLayout.CENTER);
    jDateChooserAfter.setDateFormatString(EmailImporter.DATEFORMAT);
    jPanelDayAfter.setPreferredSize(new java.awt.Dimension(132, 20));

    javax.swing.GroupLayout jPanelDayAfterLayout = new javax.swing.GroupLayout(jPanelDayAfter);
    jPanelDayAfter.setLayout(jPanelDayAfterLayout);
    jPanelDayAfterLayout.setHorizontalGroup(
        jPanelDayAfterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGap(0, 132, Short.MAX_VALUE)
    );
    jPanelDayAfterLayout.setVerticalGroup(
        jPanelDayAfterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGap(0, 19, Short.MAX_VALUE)
    );

    jDateChooserBefore = new com.toedter.calendar.JDateChooser();
    jDateChooserBefore.setSize(130, 20);
    jPanelDayBefore.add(jDateChooserBefore,java.awt.BorderLayout.CENTER);
    jDateChooserBefore.setDateFormatString(EmailImporter.DATEFORMAT);

    javax.swing.GroupLayout jPanelDayBeforeLayout = new javax.swing.GroupLayout(jPanelDayBefore);
    jPanelDayBefore.setLayout(jPanelDayBeforeLayout);
    jPanelDayBeforeLayout.setHorizontalGroup(
        jPanelDayBeforeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGap(0, 132, Short.MAX_VALUE)
    );
    jPanelDayBeforeLayout.setVerticalGroup(
        jPanelDayBeforeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGap(0, 19, Short.MAX_VALUE)
    );

    javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
    jPanel3.setLayout(jPanel3Layout);
    jPanel3Layout.setHorizontalGroup(
        jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(jPanel3Layout.createSequentialGroup()
            .addContainerGap()
            .addComponent(jCheckBoxDayAfter)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
            .addComponent(jPanelDayAfter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
            .addComponent(jCheckBoxDayBefore)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
            .addComponent(jPanelDayBefore, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addContainerGap(81, Short.MAX_VALUE))
    );
    jPanel3Layout.setVerticalGroup(
        jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(jPanel3Layout.createSequentialGroup()
            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                .addComponent(jPanelDayBefore, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jCheckBoxDayAfter, javax.swing.GroupLayout.PREFERRED_SIZE, 19, Short.MAX_VALUE)
                .addComponent(jPanelDayAfter, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 19, Short.MAX_VALUE)
                .addComponent(jCheckBoxDayBefore, javax.swing.GroupLayout.PREFERRED_SIZE, 19, Short.MAX_VALUE))
            .addGap(20, 20, 20))
    );

    org.openide.awt.Mnemonics.setLocalizedText(jCheckBoxMessageInclude1, org.openide.util.NbBundle.getMessage(EmailVisualPanel2.class, "EmailVisualPanel2.jCheckBoxMessageInclude1.text")); // NOI18N
    jCheckBoxMessageInclude1.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            jCheckBoxMessageInclude1ActionPerformed(evt);
        }
    });

    jTextFieldMessageInclude1.setText(org.openide.util.NbBundle.getMessage(EmailVisualPanel2.class, "EmailVisualPanel2.jTextFieldMessageInclude1.text")); // NOI18N

    buttonGroup1.add(jRadioButtonHasNoAtta);
    org.openide.awt.Mnemonics.setLocalizedText(jRadioButtonHasNoAtta, org.openide.util.NbBundle.getMessage(EmailVisualPanel2.class, "EmailVisualPanel2.jRadioButtonHasNoAtta.text")); // NOI18N

    buttonGroup2.add(jRadioButtonHasNoCc);
    org.openide.awt.Mnemonics.setLocalizedText(jRadioButtonHasNoCc, org.openide.util.NbBundle.getMessage(EmailVisualPanel2.class, "EmailVisualPanel2.jRadioButtonHasNoCc.text")); // NOI18N

    buttonGroup3.add(jRadioButtonHasNoBcc);
    org.openide.awt.Mnemonics.setLocalizedText(jRadioButtonHasNoBcc, org.openide.util.NbBundle.getMessage(EmailVisualPanel2.class, "EmailVisualPanel2.jRadioButtonHasNoBcc.text")); // NOI18N

    buttonGroup3.add(jRadioButtonHasBcc);
    jRadioButtonHasBcc.setSelected(true);
    org.openide.awt.Mnemonics.setLocalizedText(jRadioButtonHasBcc, org.openide.util.NbBundle.getMessage(EmailVisualPanel2.class, "EmailVisualPanel2.jRadioButtonHasBcc.text")); // NOI18N

    buttonGroup2.add(jRadioButtonHasCc);
    jRadioButtonHasCc.setSelected(true);
    org.openide.awt.Mnemonics.setLocalizedText(jRadioButtonHasCc, org.openide.util.NbBundle.getMessage(EmailVisualPanel2.class, "EmailVisualPanel2.jRadioButtonHasCc.text")); // NOI18N

    jTextFieldSubjectInclude.setText(org.openide.util.NbBundle.getMessage(EmailVisualPanel2.class, "EmailVisualPanel2.jTextFieldSubjectInclude.text")); // NOI18N

    org.openide.awt.Mnemonics.setLocalizedText(jCheckBoxSubjectInclude, org.openide.util.NbBundle.getMessage(EmailVisualPanel2.class, "EmailVisualPanel2.jCheckBoxSubjectInclude.text")); // NOI18N
    jCheckBoxSubjectInclude.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            jCheckBoxSubjectIncludeActionPerformed(evt);
        }
    });

    org.openide.awt.Mnemonics.setLocalizedText(jCheckBoxAttachement, org.openide.util.NbBundle.getMessage(EmailVisualPanel2.class, "EmailVisualPanel2.jCheckBoxAttachement.text")); // NOI18N
    jCheckBoxAttachement.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            jCheckBoxAttachementActionPerformed(evt);
        }
    });

    org.openide.awt.Mnemonics.setLocalizedText(jCheckBoxCc, org.openide.util.NbBundle.getMessage(EmailVisualPanel2.class, "EmailVisualPanel2.jCheckBoxCc.text")); // NOI18N
    jCheckBoxCc.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            jCheckBoxCcActionPerformed(evt);
        }
    });

    org.openide.awt.Mnemonics.setLocalizedText(jCheckBoxBcc, org.openide.util.NbBundle.getMessage(EmailVisualPanel2.class, "EmailVisualPanel2.jCheckBoxBcc.text")); // NOI18N
    jCheckBoxBcc.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            jCheckBoxBccActionPerformed(evt);
        }
    });

    buttonGroup1.add(jRadioButtonHasAtta);
    jRadioButtonHasAtta.setSelected(true);
    org.openide.awt.Mnemonics.setLocalizedText(jRadioButtonHasAtta, org.openide.util.NbBundle.getMessage(EmailVisualPanel2.class, "EmailVisualPanel2.jRadioButtonHasAtta.text")); // NOI18N
    jRadioButtonHasAtta.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            jRadioButtonHasAttaActionPerformed(evt);
        }
    });

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
    this.setLayout(layout);
    layout.setHorizontalGroup(
        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(layout.createSequentialGroup()
            .addContainerGap()
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createSequentialGroup()
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jCheckBoxSubjectInclude)
                        .addComponent(jCheckBoxAttachement)
                        .addComponent(jCheckBoxCc)
                        .addComponent(jCheckBoxBcc))
                    .addGap(7, 7, 7)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jRadioButtonHasAtta)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jTextFieldSubjectInclude)
                            .addComponent(jRadioButtonHasCc)
                            .addComponent(jRadioButtonHasBcc)))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                            .addGap(10, 10, 10)
                            .addComponent(jCheckBoxMessageInclude1)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jTextFieldMessageInclude1, javax.swing.GroupLayout.DEFAULT_SIZE, 163, Short.MAX_VALUE))
                        .addComponent(jRadioButtonHasNoAtta)
                        .addComponent(jRadioButtonHasNoBcc)
                        .addComponent(jRadioButtonHasNoCc)))
                .addComponent(jLabel1)
                .addComponent(jPanel3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addContainerGap())
    );
    layout.setVerticalGroup(
        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
            .addContainerGap()
            .addComponent(jLabel1)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jCheckBoxSubjectInclude)
                .addComponent(jTextFieldSubjectInclude, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jCheckBoxMessageInclude1)
                .addComponent(jTextFieldMessageInclude1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGap(18, 18, 18)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jCheckBoxAttachement, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jRadioButtonHasNoAtta, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jRadioButtonHasAtta, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE)))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                .addComponent(jCheckBoxCc)
                .addComponent(jRadioButtonHasCc, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jRadioButtonHasNoCc, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGap(7, 7, 7)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                .addComponent(jCheckBoxBcc)
                .addComponent(jRadioButtonHasBcc, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jRadioButtonHasNoBcc, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addContainerGap())
    );
    }// </editor-fold>//GEN-END:initComponents

    private void jCheckBoxEmailAddrFilterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxEmailAddrFilterActionPerformed
        if (jCheckBoxEmailAddrFilter.isSelected()) {
            jTableFilter.setEnabled(true);
            importCSVButton.setEnabled(true);
        } else {
            jTableFilter.setEnabled(false);
            importCSVButton.setEnabled(false);
        }
}//GEN-LAST:event_jCheckBoxEmailAddrFilterActionPerformed

    private void jCheckBoxDayAfterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxDayAfterActionPerformed
        if (jCheckBoxDayAfter.isSelected()) {
            jDateChooserAfter.setEnabled(true);
        } else {
            jDateChooserAfter.setEnabled(false);
        }
}//GEN-LAST:event_jCheckBoxDayAfterActionPerformed

    private void jCheckBoxDayBeforeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxDayBeforeActionPerformed
        if (jCheckBoxDayBefore.isSelected()) {
            jDateChooserBefore.setEnabled(true);
        } else {
            jDateChooserBefore.setEnabled(false);
        }
}//GEN-LAST:event_jCheckBoxDayBeforeActionPerformed

    private void jCheckBoxAttachementActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxAttachementActionPerformed
        if (jCheckBoxAttachement.isSelected()) {
            jRadioButtonHasAtta.setEnabled(true);
            jRadioButtonHasNoAtta.setEnabled(true);
        } else {
            jRadioButtonHasAtta.setEnabled(false);
            jRadioButtonHasNoAtta.setEnabled(false);
        }
}//GEN-LAST:event_jCheckBoxAttachementActionPerformed

    private void jRadioButtonHasAttaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonHasAttaActionPerformed
        // TODO add your handling code here:
}//GEN-LAST:event_jRadioButtonHasAttaActionPerformed

    private void jCheckBoxCcActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxCcActionPerformed
        if (jCheckBoxCc.isSelected()) {
            jRadioButtonHasCc.setEnabled(true);
            jRadioButtonHasNoCc.setEnabled(true);
        } else {
            jRadioButtonHasCc.setEnabled(false);
            jRadioButtonHasNoCc.setEnabled(false);
        }
}//GEN-LAST:event_jCheckBoxCcActionPerformed

    private void jCheckBoxBccActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxBccActionPerformed
        if (jCheckBoxBcc.isSelected()) {
            jRadioButtonHasBcc.setEnabled(true);
            jRadioButtonHasNoBcc.setEnabled(true);
        } else {
            jRadioButtonHasBcc.setEnabled(false);
            jRadioButtonHasNoBcc.setEnabled(false);
        }
}//GEN-LAST:event_jCheckBoxBccActionPerformed

    private void jCheckBoxSubjectIncludeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxSubjectIncludeActionPerformed
        if (jCheckBoxSubjectInclude.isSelected()) {
            jTextFieldSubjectInclude.setEnabled(true);
        } else {
            jTextFieldSubjectInclude.setEnabled(false);
        }
}//GEN-LAST:event_jCheckBoxSubjectIncludeActionPerformed

    private void jCheckBoxMessageInclude1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxMessageInclude1ActionPerformed
        if (jCheckBoxMessageInclude1.isSelected()) {
            jTextFieldMessageInclude1.setEnabled(true);
        } else {
            jTextFieldMessageInclude1.setEnabled(false);
        }
}//GEN-LAST:event_jCheckBoxMessageInclude1ActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.ButtonGroup buttonGroup2;
    private javax.swing.ButtonGroup buttonGroup3;
    private javax.swing.JButton importCSVButton;
    private javax.swing.JCheckBox jCheckBoxAttachement;
    private javax.swing.JCheckBox jCheckBoxBcc;
    private javax.swing.JCheckBox jCheckBoxCc;
    private javax.swing.JCheckBox jCheckBoxDayAfter;
    private javax.swing.JCheckBox jCheckBoxDayBefore;
    private javax.swing.JCheckBox jCheckBoxEmailAddrFilter;
    private javax.swing.JCheckBox jCheckBoxMessageInclude1;
    private javax.swing.JCheckBox jCheckBoxSubjectInclude;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanelDayAfter;
    private javax.swing.JPanel jPanelDayBefore;
    private javax.swing.JRadioButton jRadioButtonHasAtta;
    private javax.swing.JRadioButton jRadioButtonHasBcc;
    private javax.swing.JRadioButton jRadioButtonHasCc;
    private javax.swing.JRadioButton jRadioButtonHasNoAtta;
    private javax.swing.JRadioButton jRadioButtonHasNoBcc;
    private javax.swing.JRadioButton jRadioButtonHasNoCc;
    private javax.swing.JScrollPane jScrollPane1;
    private final javax.swing.JTable jTableFilter = new javax.swing.JTable();
    private javax.swing.JTextField jTextFieldMessageInclude1;
    private javax.swing.JTextField jTextFieldSubjectInclude;
    // End of variables declaration//GEN-END:variables
    private com.toedter.calendar.JDateChooser jDateChooserBefore;
    private com.toedter.calendar.JDateChooser jDateChooserAfter;
    private DefaultTableModel filterTableModel;

    private void configFilterTableModel() {

        filterTableModel = new DefaultTableModel();
        filterTableModel.setColumnCount(2);
        Object[] col = {org.openide.util.NbBundle.getMessage(EmailVisualPanel2.class, "EmailVisualPanel2.jTableFilter.column1.text"),
            org.openide.util.NbBundle.getMessage(EmailVisualPanel2.class, "EmailVisualPanel2.jTableFilter.column2.text")};
        filterTableModel.setColumnIdentifiers(col);
        filterTableModel.setRowCount(1);
        filterTableModel.setValueAt("mail", 0, 0);
        filterTableModel.setValueAt("All", 0, 1);
    }

    public void unsetup(SpigotImporter importer) {
        EmailImporter currentImporter = (EmailImporter) importer;

        boolean hasFilter = true;
        currentImporter.setFilter(hasFilter);
        currentImporter.getFilter().clear();
        if (hasFilter) {
            //set email address filter to datastructure
            if (jCheckBoxEmailAddrFilter.isSelected()) {
                DefaultTableModel model = (DefaultTableModel) jTableFilter.getModel();
                for (int i = 0; i < model.getRowCount(); i++) {
                    Object filter = model.getValueAt(i, 0);
                    String type = (String) model.getValueAt(i, 1);
                    if (type != null && filter != null && !type.equals("") && !filter.equals("")) {
                        if (type.equalsIgnoreCase("From") || type.equalsIgnoreCase("All")) {
                            String str = currentImporter.getFilter().get(EmailImporter.FILTER_EMAIL_ADDRESS_FROM);
                            str = str == null ? filter.toString() : str + EmailDataType.SPLIT_CHAR + filter.toString();
                            currentImporter.setFilterProperty(EmailImporter.FILTER_EMAIL_ADDRESS_FROM, str);
                        }
                        if (type.equalsIgnoreCase("To") || type.equalsIgnoreCase("All")) {
                            String str = currentImporter.getFilter().get(EmailImporter.FILTER_EMAIL_ADDRESS_TO);
                            str = str == null ? filter.toString() : str + EmailDataType.SPLIT_CHAR + filter.toString();
                            currentImporter.setFilterProperty(EmailImporter.FILTER_EMAIL_ADDRESS_TO, str);
                        }
                        if (type.equalsIgnoreCase("Cc") || type.equalsIgnoreCase("All")) {
                            String str = currentImporter.getFilter().get(EmailImporter.FILTER_EMAIL_ADDRESS_CC);
                            str = str == null ? filter.toString() : str + EmailDataType.SPLIT_CHAR + filter.toString();
                            currentImporter.setFilterProperty(EmailImporter.FILTER_EMAIL_ADDRESS_CC, str);
                        }
                        if (type.equalsIgnoreCase("Bcc") || type.equalsIgnoreCase("All")) {
                            String str = currentImporter.getFilter().get(EmailImporter.FILTER_EMAIL_ADDRESS_BCC);
                            str = str == null ? filter.toString() : str + EmailDataType.SPLIT_CHAR + filter.toString();
                            currentImporter.setFilterProperty(EmailImporter.FILTER_EMAIL_ADDRESS_BCC, str);
                        }
                    }
                }
            }
            //set date range
            if (jCheckBoxDayAfter.isSelected()) {
                String formatedDate = "";
                for (java.awt.Component cc : jDateChooserAfter.getComponents()) {
                    if (cc instanceof javax.swing.JTextField) {
                        formatedDate = ((javax.swing.JTextField) cc).getText();
                    }
                }
                currentImporter.setFilterProperty(EmailImporter.FILTER_DATERANGE_AFTER, formatedDate);
            }
            if (jCheckBoxDayBefore.isSelected()) {
                String formatedDate = "";
                for (java.awt.Component cc : jCheckBoxDayBefore.getComponents()) {
                    if (cc instanceof javax.swing.JTextField) {
                        formatedDate = ((javax.swing.JTextField) cc).getText();
                    }
                }
                currentImporter.setFilterProperty(EmailImporter.FILTER_DATERANGE_BEFORE, formatedDate);
            }
            //set attachment
            if (jCheckBoxAttachement.isSelected()) {
                currentImporter.setFilterProperty(EmailImporter.FILTER_ATTACHMENT, Boolean.toString(jRadioButtonHasAtta.isSelected()));
            }
            //set cc
            if (jCheckBoxCc.isSelected()) {
                currentImporter.setFilterProperty(EmailImporter.FILTER_CC, Boolean.toString(jRadioButtonHasCc.isSelected()));
            }
            //set bcc
            if (jCheckBoxBcc.isSelected()) {
                currentImporter.setFilterProperty(EmailImporter.FILTER_BCC, Boolean.toString(jRadioButtonHasBcc.isSelected()));
            }
            //set message include text
            if (jCheckBoxMessageInclude1.isSelected()) {
                currentImporter.setFilterProperty(EmailImporter.FILTER_message, jTextFieldMessageInclude1.getText().trim());
            }
            //set subject include text
            if (jCheckBoxSubjectInclude.isSelected()) {
                currentImporter.setFilterProperty(EmailImporter.FILTER_SUBJECT, jTextFieldSubjectInclude.getText().trim());
            }

        }

    }

    /**
     * set all the filters enable,but checkbox disable
     * @param component
     */
    public void setEnableFilters() {
        setEnableFilterCheckBox();
        if (jCheckBoxEmailAddrFilter.isSelected()) {
            jTableFilter.setEnabled(true);
        } else {
            jTableFilter.setEnabled(false);
        }
        if (jCheckBoxDayAfter.isSelected()) {
            jDateChooserAfter.setEnabled(true);
        } else {
            jDateChooserAfter.setEnabled(false);
        }
        if (jCheckBoxDayBefore.isSelected()) {
            jDateChooserBefore.setEnabled(true);
        } else {
            jDateChooserBefore.setEnabled(false);
        }
        if (jCheckBoxAttachement.isSelected()) {
            jRadioButtonHasAtta.setEnabled(true);
            jRadioButtonHasNoAtta.setEnabled(true);
        } else {
            jRadioButtonHasAtta.setEnabled(false);
            jRadioButtonHasNoAtta.setEnabled(false);
        }
        if (jCheckBoxBcc.isSelected()) {
            jRadioButtonHasBcc.setEnabled(true);
            jRadioButtonHasNoBcc.setEnabled(true);
        } else {
            jRadioButtonHasBcc.setEnabled(false);
            jRadioButtonHasNoBcc.setEnabled(false);
        }
        if (jCheckBoxCc.isSelected()) {
            jRadioButtonHasCc.setEnabled(true);
            jRadioButtonHasNoCc.setEnabled(true);
        } else {
            jRadioButtonHasCc.setEnabled(false);
            jRadioButtonHasNoCc.setEnabled(false);
        }
        if (jCheckBoxMessageInclude1.isSelected()) {
            jTextFieldMessageInclude1.setEnabled(true);
        } else {
            jTextFieldMessageInclude1.setEnabled(false);
        }
        if (jCheckBoxSubjectInclude.isSelected()) {
            jTextFieldSubjectInclude.setEnabled(true);
        } else {
            jTextFieldSubjectInclude.setEnabled(false);
        }
    }

    /**
     * set the filter checkbox enable
     */
    private void setEnableFilterCheckBox() {
        jCheckBoxEmailAddrFilter.setEnabled(true);
        jCheckBoxAttachement.setEnabled(true);
        jCheckBoxDayAfter.setEnabled(true);
        jCheckBoxDayBefore.setEnabled(true);
        jCheckBoxBcc.setEnabled(true);
        jCheckBoxCc.setEnabled(true);
        jCheckBoxMessageInclude1.setEnabled(true);
        jCheckBoxSubjectInclude.setEnabled(true);
    }

    public void setup(SpigotImporter importer) {
        EmailImporter current = (EmailImporter) importer;
        if (current == null) {
            return;
        }

        setEnableFilters();
        //TODO load email address filter, not load email address
//            if(current.getFilterProperty(EmailDataType.FILTER_EMAIL_ADDRESS_TO) == null)

        //set date range
        String date = current.getFilterProperty(EmailDataType.FILTER_DATERANGE_AFTER);
        if (date != null) {
            jDateChooserAfter.setEnabled(true);
            jCheckBoxDayAfter.setSelected(true);
            for (java.awt.Component cc : jDateChooserAfter.getComponents()) {
                if (cc instanceof javax.swing.JTextField) {
                    ((javax.swing.JTextField) cc).setText(date);
                }
            }
        }
        date = current.getFilterProperty(EmailDataType.FILTER_DATERANGE_BEFORE);
        if (date != null) {
            jDateChooserBefore.setEnabled(true);
            jCheckBoxDayBefore.setSelected(true);
            for (java.awt.Component cc : jDateChooserBefore.getComponents()) {
                if (cc instanceof javax.swing.JTextField) {
                    ((javax.swing.JTextField) cc).setText(date);
                }
            }
        }
        //setattachment
        String att = current.getFilterProperty(EmailDataType.FILTER_ATTACHMENT);
        if (att != null) {
            jRadioButtonHasAtta.setEnabled(true);
            jRadioButtonHasNoAtta.setEnabled(true);
            jCheckBoxAttachement.setSelected(true);
            if (att.equals(Boolean.toString(true))) {
                jRadioButtonHasAtta.setSelected(true);
            } else {
                jRadioButtonHasNoAtta.setSelected(true);
            }
        }
        //cc
        att = current.getFilterProperty(EmailDataType.FILTER_CC);
        if (att != null) {
            jRadioButtonHasCc.setEnabled(true);
            jRadioButtonHasNoCc.setEnabled(true);
            jCheckBoxCc.setSelected(true);
            if (att.equals(Boolean.toString(true))) {
                jRadioButtonHasCc.setSelected(true);
            } else {
                jRadioButtonHasNoCc.setSelected(true);
            }
        }
        //bcc
        att = current.getFilterProperty(EmailDataType.FILTER_BCC);
        if (att != null) {
            jRadioButtonHasBcc.setEnabled(true);
            jRadioButtonHasNoBcc.setEnabled(true);
            jCheckBoxBcc.setSelected(true);
            if (att.equals(Boolean.toString(true))) {
                jRadioButtonHasBcc.setSelected(true);
            } else {
                jRadioButtonHasNoBcc.setSelected(true);
            }
        }
        //message
        att = current.getFilterProperty(EmailDataType.FILTER_message);
        if (att != null) {
            jTextFieldMessageInclude1.setEnabled(true);
            jCheckBoxMessageInclude1.setSelected(true);
            jTextFieldMessageInclude1.setText(att);
        }
        //subject
        att = current.getFilterProperty(EmailDataType.FILTER_SUBJECT);
        if (att != null) {
            jTextFieldSubjectInclude.setEnabled(true);
            jCheckBoxSubjectInclude.setSelected(true);
            jTextFieldSubjectInclude.setText(att);
        }


    }

    /**
     * set all the component enable
     * @param component
     */
    public void setEnable(javax.swing.JComponent component) {
        for (java.awt.Component c : component.getComponents()) {
            if (c instanceof javax.swing.JComponent) {
                c.setEnabled(true);
                setEnable((javax.swing.JComponent) c);
            } else {
                c.getParent().setEnabled(true);
            }
        }
    }

    class MailFilterCellEditor implements TableCellEditor {

        public MailFilterCellEditor() {
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Object getCellEditorValue() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean isCellEditable(EventObject anEvent) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean shouldSelectCell(EventObject anEvent) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean stopCellEditing() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void cancelCellEditing() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void addCellEditorListener(CellEditorListener l) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void removeCellEditorListener(CellEditorListener l) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
}
