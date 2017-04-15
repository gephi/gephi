/*
Copyright 2008-2017 Gephi
Authors : Eduardo Ramos <eduardo.ramos@gephi.org>
Website : http://www.gephi.org

This file is part of Gephi.

DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

Copyright 2016 Gephi Consortium. All rights reserved.

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

Portions Copyrighted 2017 Gephi Consortium.
 */
package org.gephi.ui.exporter.plugin;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import javax.swing.JCheckBox;
import net.miginfocom.swing.MigLayout;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Table;
import org.gephi.io.exporter.plugin.ExporterSpreadsheet;
import org.gephi.project.api.ProjectController;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 * UI for selecting CSV export options of a JTable.
 *
 * @author Eduardo Ramos
 */
public class UIExporterSpreadsheetPanel extends javax.swing.JPanel {

    private static final String SEPARATOR_SAVED_PREFERENCES = "UIExporterSpreadsheetPanel_Separator";
    private static final String TABLE_SAVED_PREFERENCES = "UIExporterSpreadsheetPanel_Table";
    private ColumnCheckboxWrapper[] columnsCheckBoxes;
    private GraphModel graphModel;

    /**
     * Creates new form UIExporterSpreadsheet
     */
    public UIExporterSpreadsheetPanel() {
        initComponents();
        separatorComboBox.addItem(new SeparatorWrapper((','), getMessage("UIExporterSpreadsheetPanel.comma")));
        separatorComboBox.addItem(new SeparatorWrapper((';'), getMessage("UIExporterSpreadsheetPanel.semicolon")));
        separatorComboBox.addItem(new SeparatorWrapper(('\t'), getMessage("UIExporterSpreadsheetPanel.tab")));
        separatorComboBox.addItem(new SeparatorWrapper((' '), getMessage("UIExporterSpreadsheetPanel.space")));

        separatorComboBox.setSelectedIndex(NbPreferences.forModule(UIExporterSpreadsheetPanel.class).getInt(SEPARATOR_SAVED_PREFERENCES, 0));//Use saved separator or comma if not saved

        tableComboBox.addItem(getMessage("UIExporterSpreadsheetPanel.table.nodes"));
        tableComboBox.addItem(getMessage("UIExporterSpreadsheetPanel.table.edges"));
        
        separatorComboBox.setSelectedIndex(NbPreferences.forModule(UIExporterSpreadsheetPanel.class).getInt(SEPARATOR_SAVED_PREFERENCES, 0));//Use saved separator or comma if not saved
        tableComboBox.setSelectedIndex(NbPreferences.forModule(UIExporterSpreadsheetPanel.class).getInt(TABLE_SAVED_PREFERENCES, 1));//Use saved separator or edges by default if not saved
    }

    public void unSetup() {
        NbPreferences.forModule(UIExporterSpreadsheetPanel.class).putInt(SEPARATOR_SAVED_PREFERENCES, separatorComboBox.getSelectedIndex());
        NbPreferences.forModule(UIExporterSpreadsheetPanel.class).putInt(TABLE_SAVED_PREFERENCES, tableComboBox.getSelectedIndex());
    }

    private void refreshColumns() {
        if (graphModel == null) {
            return;
        }

        columnsPanel.removeAll();
        columnsPanel.setLayout(new MigLayout("", "[pref!]"));

        ArrayList<ColumnCheckboxWrapper> columnCheckboxesList = new ArrayList<>();

        //Show rest of columns:
        Table table = getSelectedTable() == ExporterSpreadsheet.ExportTable.NODES ? graphModel.getNodeTable() : graphModel.getEdgeTable();
        for (Column column : table) {
            columnCheckboxesList.add(new ColumnCheckboxWrapper(column.getId(), column.getTitle()));
        }

        columnsCheckBoxes = columnCheckboxesList.toArray(new ColumnCheckboxWrapper[0]);
        for (ColumnCheckboxWrapper columnCheckboxWrapper : columnsCheckBoxes) {
            columnsPanel.add(columnCheckboxWrapper, "wrap");
        }

        columnsPanel.revalidate();
        columnsPanel.repaint();
    }

    public Character getSelectedSeparator() {
        Object item = separatorComboBox.getSelectedItem();
        if (item instanceof SeparatorWrapper) {
            return ((SeparatorWrapper) item).separator;
        } else {
            return item.toString().charAt(0);
        }
    }

    public LinkedHashSet<String> getSelectedColumnsIds() {
        LinkedHashSet<String> columnsIds = new LinkedHashSet<>();
        for (ColumnCheckboxWrapper columnsCheckBox : columnsCheckBoxes) {
            if (columnsCheckBox.isSelected()) {
                columnsIds.add(columnsCheckBox.id);
            }
        }

        return columnsIds;
    }

    public ExporterSpreadsheet.ExportTable getSelectedTable() {
        return tableComboBox.getSelectedIndex() == 0 ? ExporterSpreadsheet.ExportTable.NODES : ExporterSpreadsheet.ExportTable.EDGES;
    }

    public void setup(ExporterSpreadsheet exporter) {
        ExporterSpreadsheet.ExportTable tableToExport = exporter.getTableToExport();
        if (tableToExport != null) {
            tableComboBox.setSelectedIndex(tableToExport == ExporterSpreadsheet.ExportTable.NODES ? 0 : 1);
        }

        ProjectController projectController = Lookup.getDefault().lookup(ProjectController.class);
        graphModel = projectController.getCurrentWorkspace().getLookup().lookup(GraphModel.class);
        refreshColumns();
    }

    private class SeparatorWrapper {

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
        return NbBundle.getMessage(UIExporterSpreadsheetPanel.class, resName);
    }

    private class ColumnCheckboxWrapper extends JCheckBox {

        private final String id;

        public ColumnCheckboxWrapper(String id, String title) {
            super(title, true);
            this.id = id;
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
        tableLabel = new javax.swing.JLabel();
        tableComboBox = new javax.swing.JComboBox();

        separatorLabel.setText(org.openide.util.NbBundle.getMessage(UIExporterSpreadsheetPanel.class, "UIExporterSpreadsheetPanel.separatorLabel.text")); // NOI18N

        columnsPanel.setLayout(new java.awt.GridLayout(1, 0));
        scroll.setViewportView(columnsPanel);

        columnsLabel.setText(org.openide.util.NbBundle.getMessage(UIExporterSpreadsheetPanel.class, "UIExporterSpreadsheetPanel.columnsLabel.text")); // NOI18N

        tableLabel.setText(org.openide.util.NbBundle.getMessage(UIExporterSpreadsheetPanel.class, "UIExporterSpreadsheetPanel.tableLabel.text")); // NOI18N

        tableComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tableComboBoxActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(scroll, javax.swing.GroupLayout.DEFAULT_SIZE, 235, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(columnsLabel)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(tableLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 1, Short.MAX_VALUE)
                            .addComponent(separatorLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(separatorComboBox, 0, 151, Short.MAX_VALUE)
                            .addComponent(tableComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tableLabel)
                    .addComponent(tableComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(separatorLabel)
                    .addComponent(separatorComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(columnsLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(scroll, javax.swing.GroupLayout.DEFAULT_SIZE, 225, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void tableComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tableComboBoxActionPerformed
        refreshColumns();
    }//GEN-LAST:event_tableComboBoxActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel columnsLabel;
    private javax.swing.JPanel columnsPanel;
    private javax.swing.JScrollPane scroll;
    private javax.swing.JComboBox separatorComboBox;
    private javax.swing.JLabel separatorLabel;
    private javax.swing.JComboBox tableComboBox;
    private javax.swing.JLabel tableLabel;
    // End of variables declaration//GEN-END:variables
}
