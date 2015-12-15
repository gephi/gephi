/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian, Mathieu Jacomy, Julian Bilcke, Eduardo Ramos
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
package org.gephi.datalab.plugin.manipulators.general.ui;

import com.csvreader.CsvReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import org.gephi.ui.utils.DialogFileFilter;
import org.netbeans.validation.api.Problems;
import org.netbeans.validation.api.Validator;
import org.netbeans.validation.api.ui.ValidationGroup;
import org.netbeans.validation.api.ui.ValidationPanel;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 * 
 * @author Eduardo Ramos
 */
public class ImportCSVUIVisualPanel1 extends javax.swing.JPanel {

    private static final String CHARSET_SAVED_PREFERENCES = "ImportCSVUIVisualPanel1_Charset";
    private static final String SEPARATOR_SAVED_PREFERENCES = "ImportCSVUIVisualPanel1_Separator";
    private static final String TABLE_SAVED_PREFERENCES = "ImportCSVUIVisualPanel1_Table";

    private static final int MAX_ROWS_PREVIEW = 25;
    private File selectedFile = null;
    private ImportCSVUIWizardPanel1 wizard1;
    private int columnCount = 0;
    private boolean hasSourceNodeColumn = false;
    private boolean hasTargetNodeColumn = false;
    private boolean hasColumnNamesRepeated = false;
    private boolean hasRowsMissingSourcesOrTargets = false;
    private ValidationPanel validationPanel;

    /** Creates new form ImportCSVUIVisualPanel1 */
    public ImportCSVUIVisualPanel1(ImportCSVUIWizardPanel1 wizard1) {
        initComponents();
        this.wizard1 = wizard1;
        separatorComboBox.addItem(new SeparatorWrapper((','), getMessage("ImportCSVUIVisualPanel1.comma")));
        separatorComboBox.addItem(new SeparatorWrapper((';'), getMessage("ImportCSVUIVisualPanel1.semicolon")));
        separatorComboBox.addItem(new SeparatorWrapper(('\t'), getMessage("ImportCSVUIVisualPanel1.tab")));
        separatorComboBox.addItem(new SeparatorWrapper((' '), getMessage("ImportCSVUIVisualPanel1.space")));

        separatorComboBox.setSelectedIndex(NbPreferences.forModule(ImportCSVUIVisualPanel1.class).getInt(SEPARATOR_SAVED_PREFERENCES, 0));//Use saved separator or comma if not saved yet

        tableComboBox.addItem(getMessage("ImportCSVUIVisualPanel1.nodes-table"));
        tableComboBox.addItem(getMessage("ImportCSVUIVisualPanel1.edges-table"));

        tableComboBox.setSelectedIndex(NbPreferences.forModule(ImportCSVUIVisualPanel1.class).getInt(TABLE_SAVED_PREFERENCES, 0));//Use saved table or nodes table if not saved yet

        for (String charset : Charset.availableCharsets().keySet()) {
            charsetComboBox.addItem(charset);
        }
        String savedCharset = NbPreferences.forModule(ImportCSVUIVisualPanel1.class).get(CHARSET_SAVED_PREFERENCES, null);
        if (savedCharset != null) {
            charsetComboBox.setSelectedItem(savedCharset);
        }else{
            charsetComboBox.setSelectedItem(Charset.forName("UTF-8").name());//UTF-8 by default, not system default charset
        }
    }

    public void unSetup(){
        NbPreferences.forModule(ImportCSVUIVisualPanel1.class).put(CHARSET_SAVED_PREFERENCES, charsetComboBox.getSelectedItem().toString());
        NbPreferences.forModule(ImportCSVUIVisualPanel1.class).putInt(SEPARATOR_SAVED_PREFERENCES, separatorComboBox.getSelectedIndex());
        NbPreferences.forModule(ImportCSVUIVisualPanel1.class).putInt(TABLE_SAVED_PREFERENCES, tableComboBox.getSelectedIndex());
    }

    public ValidationPanel getValidationPanel() {
        if (validationPanel != null) {
            return validationPanel;
        }
        try {
            SwingUtilities.invokeAndWait(new Runnable() {

                @Override
                public void run() {
                    validationPanel = new ValidationPanel();
                    validationPanel.setInnerComponent(ImportCSVUIVisualPanel1.this);

                    ValidationGroup validationGroup = validationPanel.getValidationGroup();

                    validationGroup.add(pathTextField, new Validator<String>() {

                        @Override
                        public boolean validate(Problems prblms, String string, String t) {
                            if (!isValidFile()) {
                                prblms.add(getMessage("ImportCSVUIVisualPanel1.validation.invalid-file"));
                                return false;
                            }
                            if (!hasColumns()) {
                                prblms.add(getMessage("ImportCSVUIVisualPanel1.validation.no-columns"));
                                return false;
                            }
                            if (hasColumnNamesRepeated()) {
                                prblms.add(getMessage("ImportCSVUIVisualPanel1.validation.repeated-columns"));
                                return false;
                            }
                            if (!areValidColumnsForTable()) {
                                prblms.add(getMessage("ImportCSVUIVisualPanel1.validation.edges.no-source-target-columns"));
                                return false;
                            }
                            if (hasRowsMissingSourcesOrTargets()) {
                                prblms.add(NbBundle.getMessage(ImportCSVUIVisualPanel1.class, 
                                    "ImportCSVUIVisualPanel1.validation.edges.empty-sources-or-targets"
                                ));
                                return false;
                            }
                            return true;
                        }
                    });
                }
            });
            validationPanel.setName(getName());
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        } catch (InvocationTargetException ex) {
            Exceptions.printStackTrace(ex);
        }

        return validationPanel;
    }

    public void refreshPreviewTable() {
        if (selectedFile != null && selectedFile.exists()) {
            try {
                CsvReader reader = new CsvReader(new FileInputStream(selectedFile), getSelectedSeparator(), getSelectedCharset());
                reader.setTrimWhitespace(false);
                String[] headers;
                try {
                    reader.readHeaders();
                    headers = reader.getHeaders();
                } catch (Exception ex) {
                    headers = new String[0];//Some charsets can be problematic with unreal columns lenght. Don't show table when there are problems
                }
                columnCount = headers.length;

                //Check for repeated column names:
                Set<String> columnNamesSet = new HashSet<String>();
                hasColumnNamesRepeated = false;
                hasSourceNodeColumn = false;
                hasTargetNodeColumn = false;
                int sourceColumnIndex = 0, 
                    targetColumnIndex = 0,
                    currentColumn = 0;
                for (String header : headers) {
                    if (header.equalsIgnoreCase("source")) {
                        hasSourceNodeColumn = true;
                        sourceColumnIndex = currentColumn;
                    }
                    if (header.equalsIgnoreCase("target")) {
                        hasTargetNodeColumn = true;
                        targetColumnIndex = currentColumn;
                    }
                    if (columnNamesSet.contains(header)) {
                        hasColumnNamesRepeated = true;
                        break;
                    }
                    columnNamesSet.add(header);
                    currentColumn++;
                }

                ArrayList<String[]> records = new ArrayList<String[]>();
                hasRowsMissingSourcesOrTargets = false;
                ImportCSVUIWizardAction.Mode mode = getMode();
                if (columnCount > 0) {
                    String[] currentRecord;
                    
                    while (reader.readRecord()) {
                        int recordColumnCount = reader.getColumnCount();
                        currentRecord = new String[recordColumnCount];
                        for (int i = 0; i < currentRecord.length; i++) {
                            currentRecord[i] = reader.get(i);
                        }
                        
                        // Search for missing source or target columns for edges table
                        if(mode == ImportCSVUIWizardAction.Mode.EDGES_TABLE){
                            if (recordColumnCount < sourceColumnIndex
                                    || currentRecord[sourceColumnIndex].trim().isEmpty()
                                    || recordColumnCount < targetColumnIndex
                                    || currentRecord[targetColumnIndex].trim().isEmpty()) {
                                hasRowsMissingSourcesOrTargets = true;
                            }
                        }
                        
                        if (records.size() < MAX_ROWS_PREVIEW) {
                            records.add(currentRecord);
                        }
                    }
                }
                reader.close();
                final String[] columnNames = headers;
                final String[][] values = records.toArray(new String[0][]);
                previewTable.setModel(new TableModel() {

                    @Override
                    public int getRowCount() {
                        return values.length;
                    }

                    @Override
                    public int getColumnCount() {
                        return columnNames.length;
                    }

                    @Override
                    public String getColumnName(int columnIndex) {
                        return columnNames[columnIndex];
                    }

                    @Override
                    public Class<?> getColumnClass(int columnIndex) {
                        return String.class;
                    }

                    @Override
                    public boolean isCellEditable(int rowIndex, int columnIndex) {
                        return false;
                    }

                    @Override
                    public Object getValueAt(int rowIndex, int columnIndex) {
                        if (values[rowIndex].length > columnIndex) {
                            return values[rowIndex][columnIndex];
                        } else {
                            return null;
                        }
                    }

                    @Override
                    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
                    }

                    @Override
                    public void addTableModelListener(TableModelListener l) {
                    }

                    @Override
                    public void removeTableModelListener(TableModelListener l) {
                    }
                });
            } catch (FileNotFoundException ex) {
                Exceptions.printStackTrace(ex);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, getMessage("ImportCSVUIVisualPanel1.validation.error"), getMessage("ImportCSVUIVisualPanel1.validation.file-permissions-error"), JOptionPane.ERROR_MESSAGE);
            } 
        }
        wizard1.fireChangeEvent();
        pathTextField.setText(pathTextField.getText());//To fire validation panel messages.
    }

    @Override
    public String getName() {
        return getMessage("ImportCSVUIVisualPanel1.name");
    }

    public Character getSelectedSeparator() {
        Object item = separatorComboBox.getSelectedItem();
        if (item instanceof SeparatorWrapper) {
            return ((SeparatorWrapper) item).separator;
        } else {
            return item.toString().charAt(0);
        }
    }

    public File getSelectedFile() {
        return selectedFile;
    }

    public ImportCSVUIWizardAction.Mode getMode() {
        switch (tableComboBox.getSelectedIndex()) {
            case 0:
                return ImportCSVUIWizardAction.Mode.NODES_TABLE;
            case 1:
                return ImportCSVUIWizardAction.Mode.EDGES_TABLE;
            default:
                return ImportCSVUIWizardAction.Mode.NODES_TABLE;//Not going to happen.
        }
    }

    public Charset getSelectedCharset() {
        return Charset.forName(charsetComboBox.getSelectedItem().toString());
    }

    public int getColumnCount() {
        return columnCount;
    }

    public boolean hasColumnNamesRepeated() {
        return hasColumnNamesRepeated;
    }

    public boolean isValidFile() {
        return selectedFile != null && selectedFile.exists();
    }

    public boolean hasColumns() {
        return columnCount > 0;
    }

    public boolean areValidColumnsForTable() {
        switch (getMode()) {
            case NODES_TABLE:
                return true;
            case EDGES_TABLE:
                return hasSourceNodeColumn && hasTargetNodeColumn;
            default:
                return false;
        }
    }

    public boolean isCSVValid() {
        return isValidFile() && hasColumns() && !hasColumnNamesRepeated && areValidColumnsForTable() &&!hasRowsMissingSourcesOrTargets();
    }

    public boolean hasRowsMissingSourcesOrTargets() {
        return hasRowsMissingSourcesOrTargets;
    }

    class SeparatorWrapper {

        private Character separator;
        private String displayText;

        public SeparatorWrapper(Character separator) {
            this.separator = separator;
        }

        public SeparatorWrapper(Character separator, String displayText) {
            this.separator = separator;
            this.displayText = displayText;
        }

        @Override
        public String toString() {
            if (displayText != null) {
                return displayText;
            } else {
                return String.valueOf(separator);
            }
        }
    }

    private String getMessage(String resName) {
        return NbBundle.getMessage(ImportCSVUIVisualPanel1.class, resName);
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
        pathTextField = new javax.swing.JTextField();
        fileButton = new javax.swing.JButton();
        separatorLabel = new javax.swing.JLabel();
        separatorComboBox = new javax.swing.JComboBox();
        tableLabel = new javax.swing.JLabel();
        tableComboBox = new javax.swing.JComboBox();
        previewLabel = new javax.swing.JLabel();
        scroll = new javax.swing.JScrollPane();
        previewTable = new javax.swing.JTable();
        charsetLabel = new javax.swing.JLabel();
        charsetComboBox = new javax.swing.JComboBox();

        descriptionLabel.setText(org.openide.util.NbBundle.getMessage(ImportCSVUIVisualPanel1.class, "ImportCSVUIVisualPanel1.descriptionLabel.text")); // NOI18N

        pathTextField.setEditable(false);
        pathTextField.setText(org.openide.util.NbBundle.getMessage(ImportCSVUIVisualPanel1.class, "ImportCSVUIVisualPanel1.pathTextField.text")); // NOI18N

        fileButton.setText(org.openide.util.NbBundle.getMessage(ImportCSVUIVisualPanel1.class, "ImportCSVUIVisualPanel1.fileButton.text")); // NOI18N
        fileButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fileButtonActionPerformed(evt);
            }
        });

        separatorLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        separatorLabel.setText(org.openide.util.NbBundle.getMessage(ImportCSVUIVisualPanel1.class, "ImportCSVUIVisualPanel1.separatorLabel.text")); // NOI18N

        separatorComboBox.addItemListener(new java.awt.event.ItemListener() {
            @Override
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                separatorComboBoxItemStateChanged(evt);
            }
        });

        tableLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        tableLabel.setText(org.openide.util.NbBundle.getMessage(ImportCSVUIVisualPanel1.class, "ImportCSVUIVisualPanel1.tableLabel.text")); // NOI18N

        tableComboBox.addItemListener(new java.awt.event.ItemListener() {
            @Override
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                tableComboBoxItemStateChanged(evt);
            }
        });

        previewLabel.setText(org.openide.util.NbBundle.getMessage(ImportCSVUIVisualPanel1.class, "ImportCSVUIVisualPanel1.previewLabel.text")); // NOI18N

        scroll.setViewportView(previewTable);

        charsetLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        charsetLabel.setText(org.openide.util.NbBundle.getMessage(ImportCSVUIVisualPanel1.class, "ImportCSVUIVisualPanel1.charsetLabel.text")); // NOI18N

        charsetComboBox.addItemListener(new java.awt.event.ItemListener() {
            @Override
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                charsetComboBoxItemStateChanged(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(scroll, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 325, Short.MAX_VALUE)
                    .addComponent(descriptionLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 325, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(pathTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 274, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(fileButton))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(separatorLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(separatorComboBox, 0, 90, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(tableLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(tableComboBox, 0, 123, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(charsetLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 96, Short.MAX_VALUE)
                            .addComponent(charsetComboBox, 0, 96, Short.MAX_VALUE)))
                    .addComponent(previewLabel, javax.swing.GroupLayout.Alignment.LEADING))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(descriptionLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(pathTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(fileButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(separatorLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(separatorComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(tableLabel)
                            .addComponent(charsetLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(tableComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(charsetComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(18, 18, 18)
                .addComponent(previewLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(scroll, javax.swing.GroupLayout.DEFAULT_SIZE, 147, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void fileButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fileButtonActionPerformed
        String lastPath = NbPreferences.forModule(ImportCSVUIVisualPanel1.class).get(LAST_PATH, null);
        final JFileChooser chooser = new JFileChooser(lastPath);
        chooser.setAcceptAllFileFilterUsed(true);
        DialogFileFilter dialogFileFilter = new DialogFileFilter(NbBundle.getMessage(ImportCSVUIVisualPanel1.class, "ImportCSVUIVisualPanel1.filechooser.csvDescription"));
        dialogFileFilter.addExtension("csv");
        chooser.addChoosableFileFilter(dialogFileFilter);
        chooser.setSelectedFile(selectedFile);
        int returnFile = chooser.showOpenDialog(null);
        if (returnFile != JFileChooser.APPROVE_OPTION) {
            return;
        }

        selectedFile = chooser.getSelectedFile();
        String path = selectedFile.getAbsolutePath();

        pathTextField.setText(path);

        //Save last path
        String defaultDirectory = selectedFile.getParentFile().getAbsolutePath();
        NbPreferences.forModule(ImportCSVUIVisualPanel1.class).put(LAST_PATH, defaultDirectory);
        refreshPreviewTable();
    }//GEN-LAST:event_fileButtonActionPerformed

    private void separatorComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_separatorComboBoxItemStateChanged
        refreshPreviewTable();
    }//GEN-LAST:event_separatorComboBoxItemStateChanged

    private void charsetComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_charsetComboBoxItemStateChanged
        refreshPreviewTable();
    }//GEN-LAST:event_charsetComboBoxItemStateChanged

    private void tableComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_tableComboBoxItemStateChanged
        refreshPreviewTable();
    }//GEN-LAST:event_tableComboBoxItemStateChanged
    private static final String LAST_PATH = "ImportCSVUIVisualPanel1_Save_Last_Path";
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox charsetComboBox;
    private javax.swing.JLabel charsetLabel;
    private javax.swing.JLabel descriptionLabel;
    private javax.swing.JButton fileButton;
    private javax.swing.JTextField pathTextField;
    private javax.swing.JLabel previewLabel;
    private javax.swing.JTable previewTable;
    private javax.swing.JScrollPane scroll;
    private javax.swing.JComboBox separatorComboBox;
    private javax.swing.JLabel separatorLabel;
    private javax.swing.JComboBox tableComboBox;
    private javax.swing.JLabel tableLabel;
    // End of variables declaration//GEN-END:variables
}
