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
package org.gephi.desktop.datalab.general.actions;

import java.nio.charset.Charset;
import java.util.ArrayList;
import javax.swing.JCheckBox;
import net.miginfocom.swing.MigLayout;
import org.gephi.datalab.api.datatables.AttributeTableCSVExporter;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Table;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 * UI for selecting CSV export options of a JTable.
 *
 * @author Eduardo Ramos
 */
public class CSVExportUI extends javax.swing.JPanel {

    private static final String CHARSET_SAVED_PREFERENCES = "CSVExportUI_Charset";
    private static final String SEPARATOR_SAVED_PREFERENCES = "CSVExportUI_Separator";
    private final Column[] columns;
    private ColumnCheckboxWrapper[] columnsCheckBoxes;
    private final boolean edgesTable;

    /**
     * Creates new form CSVExportUI
     */
    public CSVExportUI(Table table, boolean edgesTable) {
        initComponents();
        this.columns = table.toArray();
        this.edgesTable = edgesTable;
        separatorComboBox.addItem(new SeparatorWrapper((','), getMessage("CSVExportUI.comma")));
        separatorComboBox.addItem(new SeparatorWrapper((';'), getMessage("CSVExportUI.semicolon")));
        separatorComboBox.addItem(new SeparatorWrapper(('\t'), getMessage("CSVExportUI.tab")));
        separatorComboBox.addItem(new SeparatorWrapper((' '), getMessage("CSVExportUI.space")));

        separatorComboBox.setSelectedIndex(NbPreferences.forModule(CSVExportUI.class).getInt(SEPARATOR_SAVED_PREFERENCES, 0));//Use saved separator or comma if not saved

        for (String charset : Charset.availableCharsets().keySet()) {
            charsetComboBox.addItem(charset);
        }
        String savedCharset = NbPreferences.forModule(CSVExportUI.class).get(CHARSET_SAVED_PREFERENCES, null);
        if (savedCharset != null) {
            charsetComboBox.setSelectedItem(savedCharset);
        } else {
            charsetComboBox.setSelectedItem(Charset.forName("UTF-8").name());//UTF-8 by default, not system default charset
        }
        refreshColumns();
    }

    public void unSetup() {
        NbPreferences.forModule(CSVExportUI.class).put(CHARSET_SAVED_PREFERENCES, charsetComboBox.getSelectedItem().toString());
        NbPreferences.forModule(CSVExportUI.class).putInt(SEPARATOR_SAVED_PREFERENCES, separatorComboBox.getSelectedIndex());
    }

    private void refreshColumns() {
        columnsPanel.removeAll();
        columnsPanel.setLayout(new MigLayout("", "[pref!]"));
        
        ArrayList<ColumnCheckboxWrapper> columnCheckboxesList = new ArrayList<>();
        
        //In case of edges table, we need to include fake source, target and type columns:
        if(edgesTable){
            columnCheckboxesList.add(new ColumnCheckboxWrapper(AttributeTableCSVExporter.FAKE_COLUMN_EDGE_SOURCE, "Source", true));
            columnCheckboxesList.add(new ColumnCheckboxWrapper(AttributeTableCSVExporter.FAKE_COLUMN_EDGE_TARGET, "Target", true));
            columnCheckboxesList.add(new ColumnCheckboxWrapper(AttributeTableCSVExporter.FAKE_COLUMN_EDGE_TYPE, "Type", true));
        }
        
        //Show rest of columns:
        for (Column column : columns) {
            columnCheckboxesList.add(new ColumnCheckboxWrapper(column.getIndex(), column.getTitle(), true));
        }

        columnsCheckBoxes = columnCheckboxesList.toArray(new ColumnCheckboxWrapper[0]);
        for (ColumnCheckboxWrapper columnCheckboxWrapper : columnsCheckBoxes) {
            columnsPanel.add(columnCheckboxWrapper, "wrap");
        }
        
        columnsPanel.revalidate();
        columnsPanel.repaint();
    }

    public String getDisplayName() {
        return getMessage("CSVExportUI.title");
    }

    public Character getSelectedSeparator() {
        Object item = separatorComboBox.getSelectedItem();
        if (item instanceof SeparatorWrapper) {
            return ((SeparatorWrapper) item).separator;
        } else {
            return item.toString().charAt(0);
        }
    }

    public Integer[] getSelectedColumnsIndexes() {
        ArrayList<Integer> columnsIndexes = new ArrayList<>();
        for (int i = 0; i < columnsCheckBoxes.length; i++) {
            if (columnsCheckBoxes[i].isSelected()) {
                columnsIndexes.add(columnsCheckBoxes[i].index);
            }
        }
        return columnsIndexes.toArray(new Integer[0]);
    }

    public Charset getSelectedCharset() {
        return Charset.forName(charsetComboBox.getSelectedItem().toString());
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
        return NbBundle.getMessage(CSVExportUI.class, resName);
    }
    
    class ColumnCheckboxWrapper extends JCheckBox{
        int index;

        public ColumnCheckboxWrapper(int index, String text, boolean selected) {
            super(text, selected);
            this.index = index;
        }
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        separatorLabel = new javax.swing.JLabel();
        separatorComboBox = new javax.swing.JComboBox();
        scroll = new javax.swing.JScrollPane();
        columnsPanel = new javax.swing.JPanel();
        columnsLabel = new javax.swing.JLabel();
        charsetLabel = new javax.swing.JLabel();
        charsetComboBox = new javax.swing.JComboBox();

        separatorLabel.setText(org.openide.util.NbBundle.getMessage(CSVExportUI.class, "CSVExportUI.separatorLabel.text")); // NOI18N

        columnsPanel.setLayout(new java.awt.GridLayout(1, 0));
        scroll.setViewportView(columnsPanel);

        columnsLabel.setText(org.openide.util.NbBundle.getMessage(CSVExportUI.class, "CSVExportUI.columnsLabel.text")); // NOI18N

        charsetLabel.setText(org.openide.util.NbBundle.getMessage(CSVExportUI.class, "CSVExportUI.charsetLabel.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(scroll, javax.swing.GroupLayout.DEFAULT_SIZE, 220, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(separatorLabel)
                        .addGap(18, 18, 18)
                        .addComponent(separatorComboBox, 0, 150, Short.MAX_VALUE))
                    .addComponent(columnsLabel)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(charsetLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(charsetComboBox, 0, 174, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(separatorLabel)
                    .addComponent(separatorComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(charsetLabel)
                    .addComponent(charsetComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(columnsLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(scroll, javax.swing.GroupLayout.DEFAULT_SIZE, 157, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox charsetComboBox;
    private javax.swing.JLabel charsetLabel;
    private javax.swing.JLabel columnsLabel;
    private javax.swing.JPanel columnsPanel;
    private javax.swing.JScrollPane scroll;
    private javax.swing.JComboBox separatorComboBox;
    private javax.swing.JLabel separatorLabel;
    // End of variables declaration//GEN-END:variables
}
