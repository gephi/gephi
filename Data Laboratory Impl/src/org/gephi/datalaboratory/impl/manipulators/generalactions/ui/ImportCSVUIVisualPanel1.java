/*
Copyright 2008 WebAtlas
Authors : Mathieu Bastian, Mathieu Jacomy, Julian Bilcke, Eduardo Ramos
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gephi.datalaboratory.impl.manipulators.generalactions.ui;

import com.csvreader.CsvReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import javax.swing.JFileChooser;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import org.gephi.ui.utils.DialogFileFilter;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 * 
 * @author Eduardo Ramos <eduramiba@gmail.com>
 */
public class ImportCSVUIVisualPanel1 extends javax.swing.JPanel {

    private static final int MAX_ROWS_PREVIEW = 20;
    private File selectedFile = null;
    private ImportCSVUIWizardPanel1 wizardDescriptor;

    /** Creates new form ImportCSVUIVisualPanel1 */
    public ImportCSVUIVisualPanel1(ImportCSVUIWizardPanel1 wizardDescriptor) {
        initComponents();
        this.wizardDescriptor = wizardDescriptor;
        separatorComboBox.addItem(new SeparatorWrapper((','), getMessage("ImportCSVUIVisualPanel1.comma")));
        separatorComboBox.addItem(new SeparatorWrapper((';'), getMessage("ImportCSVUIVisualPanel1.semicolon")));
        separatorComboBox.addItem(new SeparatorWrapper(('\t'), getMessage("ImportCSVUIVisualPanel1.tab")));
        separatorComboBox.addItem(new SeparatorWrapper((' '), getMessage("ImportCSVUIVisualPanel1.space")));

        tableComboBox.addItem(getMessage("ImportCSVUIVisualPanel1.nodes-table"));
        tableComboBox.addItem(getMessage("ImportCSVUIVisualPanel1.edges-table"));
    }

    private void refreshTable() {
        if (selectedFile != null && selectedFile.exists()) {
            try {
                CsvReader reader = new CsvReader(new FileReader(selectedFile), getSelectedSeparator());
                reader.readHeaders();
                reader.setTrimWhitespace(false);
                final String[] headers = reader.getHeaders();

                ArrayList<String[]> records = new ArrayList<String[]>();
                if (headers.length > 0) {
                    String[] currentRecord;
                    while (reader.readRecord() && records.size() < MAX_ROWS_PREVIEW) {
                        currentRecord = new String[reader.getColumnCount()];
                        for (int i = 0; i < currentRecord.length; i++) {
                            currentRecord[i] = reader.get(i);
                        }
                        records.add(currentRecord);
                    }
                }
                final String[][] values = records.toArray(new String[0][]);
                table.setModel(new TableModel() {

                    public int getRowCount() {
                        return values.length;
                    }

                    public int getColumnCount() {
                        return headers.length;
                    }

                    public String getColumnName(int columnIndex) {
                        return headers[columnIndex];
                    }

                    public Class<?> getColumnClass(int columnIndex) {
                        return String.class;
                    }

                    public boolean isCellEditable(int rowIndex, int columnIndex) {
                        return false;
                    }

                    public Object getValueAt(int rowIndex, int columnIndex) {
                        if (values[rowIndex].length > columnIndex) {
                            return values[rowIndex][columnIndex];
                        } else {
                            return null;
                        }
                    }

                    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
                    }

                    public void addTableModelListener(TableModelListener l) {
                    }

                    public void removeTableModelListener(TableModelListener l) {
                    }
                });
            } catch (FileNotFoundException ex) {
                Exceptions.printStackTrace(ex);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
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
        table = new javax.swing.JTable();

        descriptionLabel.setText(org.openide.util.NbBundle.getMessage(ImportCSVUIVisualPanel1.class, "ImportCSVUIVisualPanel1.descriptionLabel.text")); // NOI18N

        pathTextField.setEditable(false);
        pathTextField.setText(org.openide.util.NbBundle.getMessage(ImportCSVUIVisualPanel1.class, "ImportCSVUIVisualPanel1.pathTextField.text")); // NOI18N

        fileButton.setText(org.openide.util.NbBundle.getMessage(ImportCSVUIVisualPanel1.class, "ImportCSVUIVisualPanel1.fileButton.text")); // NOI18N
        fileButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fileButtonActionPerformed(evt);
            }
        });

        separatorLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        separatorLabel.setText(org.openide.util.NbBundle.getMessage(ImportCSVUIVisualPanel1.class, "ImportCSVUIVisualPanel1.separatorLabel.text")); // NOI18N

        separatorComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                separatorComboBoxItemStateChanged(evt);
            }
        });

        tableLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        tableLabel.setText(org.openide.util.NbBundle.getMessage(ImportCSVUIVisualPanel1.class, "ImportCSVUIVisualPanel1.tableLabel.text")); // NOI18N

        previewLabel.setText(org.openide.util.NbBundle.getMessage(ImportCSVUIVisualPanel1.class, "ImportCSVUIVisualPanel1.previewLabel.text")); // NOI18N

        scroll.setViewportView(table);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(scroll, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 303, Short.MAX_VALUE)
                    .addComponent(descriptionLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 303, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(pathTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 252, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(fileButton))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(separatorLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(separatorComboBox, 0, 90, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 28, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(tableLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(tableComboBox, 0, 185, Short.MAX_VALUE)))
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
                    .addComponent(tableLabel)
                    .addComponent(separatorLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(separatorComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(previewLabel))
                    .addComponent(tableComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(scroll, javax.swing.GroupLayout.DEFAULT_SIZE, 147, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void fileButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fileButtonActionPerformed
        String lastPath = NbPreferences.forModule(ImportCSVUIVisualPanel1.class).get(LAST_PATH, null);
        final JFileChooser chooser = new JFileChooser(lastPath);
        chooser.setAcceptAllFileFilterUsed(false);
        DialogFileFilter dialogFileFilter = new DialogFileFilter(NbBundle.getMessage(ImportCSVUIVisualPanel1.class, "ImportCSVUIVisualPanel1.filechooser.csvDescription"));
        dialogFileFilter.addExtension("csv");
        chooser.addChoosableFileFilter(dialogFileFilter);
        chooser.setSelectedFile(selectedFile);
        int returnFile = chooser.showOpenDialog(null);
        if (returnFile != JFileChooser.APPROVE_OPTION) {
            return;
        }
        selectedFile = chooser.getSelectedFile();
        pathTextField.setText(selectedFile.getAbsolutePath());

        //Save last path
        String defaultDirectory = selectedFile.getParentFile().getAbsolutePath();
        NbPreferences.forModule(ImportCSVUIVisualPanel1.class).put(LAST_PATH, defaultDirectory);
        wizardDescriptor.fireChangeEvent();
        refreshTable();
    }//GEN-LAST:event_fileButtonActionPerformed

    private void separatorComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_separatorComboBoxItemStateChanged
        refreshTable();
    }//GEN-LAST:event_separatorComboBoxItemStateChanged
    private static final String LAST_PATH = "ImportCSVUIVisualPanel1_Save_Last_Path";
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel descriptionLabel;
    private javax.swing.JButton fileButton;
    private javax.swing.JTextField pathTextField;
    private javax.swing.JLabel previewLabel;
    private javax.swing.JScrollPane scroll;
    private javax.swing.JComboBox separatorComboBox;
    private javax.swing.JLabel separatorLabel;
    private javax.swing.JTable table;
    private javax.swing.JComboBox tableComboBox;
    private javax.swing.JLabel tableLabel;
    // End of variables declaration//GEN-END:variables
}
