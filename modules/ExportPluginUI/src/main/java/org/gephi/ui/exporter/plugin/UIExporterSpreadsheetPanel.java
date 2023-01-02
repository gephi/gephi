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

import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.swing.JCheckBox;
import net.miginfocom.swing.MigLayout;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Table;
import org.gephi.io.exporter.plugin.ExporterSpreadsheet;
import org.openide.util.NbBundle;

/**
 * UI for selecting CSV export options of a JTable.
 *
 * @author Eduardo Ramos
 */
public class UIExporterSpreadsheetPanel extends javax.swing.JPanel {

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox attributesExportCheckbox;
    private javax.swing.JCheckBox colorsExportCheckbox;
    private javax.swing.JLabel columnsLabel;
    private javax.swing.JPanel columnsPanel;
    private javax.swing.JComboBox decimalSeparatorComboBox;
    private javax.swing.JLabel decimalSeparatorLabel;
    private javax.swing.JCheckBox dynamicExportCheckbox;
    private javax.swing.JLabel labelExport;
    private javax.swing.JLabel labelNormalize;
    private javax.swing.JCheckBox normalizeCheckbox;
    private javax.swing.JCheckBox positionExportCheckbox;
    private javax.swing.JScrollPane scroll;
    private javax.swing.JComboBox separatorComboBox;
    private javax.swing.JLabel separatorLabel;
    private javax.swing.JCheckBox sizeExportCheckbox;
    private javax.swing.JComboBox tableComboBox;
    private javax.swing.JLabel tableLabel;
    // End of variables declaration//GEN-END:variables

    private final Set<SeparatorWrapper> separators = new HashSet<>();
    private final Set<SeparatorWrapper> decimalSeparators = new HashSet<>();

    private List<ColumnCheckboxWrapper> columnsCheckBoxes;
    private GraphModel graphModel;
    private ExporterSpreadsheet exporterSpreadsheet;

    /**
     * Creates new form UIExporterSpreadsheet
     */
    public UIExporterSpreadsheetPanel() {
        initComponents();

        separators.add(new SeparatorWrapper((','), getMessage("UIExporterSpreadsheetPanel.comma")));
        separators.add(new SeparatorWrapper((';'), getMessage("UIExporterSpreadsheetPanel.semicolon")));
        separators.add(new SeparatorWrapper(('\t'), getMessage("UIExporterSpreadsheetPanel.tab")));
        separators.add(new SeparatorWrapper((' '), getMessage("UIExporterSpreadsheetPanel.space")));
        separators.forEach(s -> separatorComboBox.addItem(s));

        decimalSeparators.add(new SeparatorWrapper(('.'), getMessage("UIExporterSpreadsheetPanel.dot")));
        decimalSeparators.add(new SeparatorWrapper((','), getMessage("UIExporterSpreadsheetPanel.comma")));
        decimalSeparators.forEach(s -> decimalSeparatorComboBox.addItem(s));

        tableComboBox.addItem(getMessage("UIExporterSpreadsheetPanel.table.nodes"));
        tableComboBox.addItem(getMessage("UIExporterSpreadsheetPanel.table.edges"));
    }

    public void setup(ExporterSpreadsheet exporter) {
        tableComboBox.setSelectedIndex(
            exporter.getTableToExport().equals(ExporterSpreadsheet.ExportTable.NODES) ? 0 : 1);
        separatorComboBox.setSelectedItem(new SeparatorWrapper(exporter.getFieldDelimiter()));
        decimalSeparatorComboBox.setSelectedItem(
            new SeparatorWrapper(exporter.getDecimalFormatSymbols().getDecimalSeparator()));

        positionExportCheckbox.setSelected(exporter.isExportPosition());
        colorsExportCheckbox.setSelected(exporter.isExportColors());
        sizeExportCheckbox.setSelected(exporter.isExportSize());
        attributesExportCheckbox.setSelected(exporter.isExportAttributes());
        dynamicExportCheckbox.setSelected(exporter.isExportDynamic());
        normalizeCheckbox.setSelected(exporter.isNormalize());

        graphModel = exporter.getWorkspace().getLookup().lookup(GraphModel.class);
        exporterSpreadsheet = exporter;

        refreshColumns();
    }

    public void unsetup(ExporterSpreadsheet exporter) {
        exporter.setTableToExport(tableComboBox.getSelectedIndex() == 0 ? ExporterSpreadsheet.ExportTable.NODES :
            ExporterSpreadsheet.ExportTable.EDGES);
        exporter.setFieldDelimiter(((SeparatorWrapper) separatorComboBox.getSelectedItem()).separator);

        DecimalFormatSymbols dfs = exporter.getDecimalFormatSymbols();
        dfs.setDecimalSeparator(((SeparatorWrapper) decimalSeparatorComboBox.getSelectedItem()).separator);
        exporter.setDecimalFormatSymbols(dfs);

        exporter.setExportPosition(positionExportCheckbox.isSelected());
        exporter.setExportColors(colorsExportCheckbox.isSelected());
        exporter.setExportSize(sizeExportCheckbox.isSelected());
        exporter.setExportAttributes(attributesExportCheckbox.isSelected());
        exporter.setExportDynamic(dynamicExportCheckbox.isSelected());
        exporter.setNormalize(normalizeCheckbox.isSelected());

        exporter.setExcludedColumns(columnsCheckBoxes.stream().filter(c -> !c.isSelected()).map(c -> c.id).collect(
            Collectors.toSet()));

        graphModel = null;
        exporterSpreadsheet = null;
        columnsCheckBoxes = null;
    }

    private void refreshColumns() {
        if (graphModel == null) {
            return;
        }

        columnsPanel.removeAll();
        columnsPanel.setLayout(new MigLayout("", "[pref!]"));

        columnsCheckBoxes = new ArrayList<>();

        //Show rest of columns:
        Table table = tableComboBox.getSelectedIndex() == 0 ? graphModel.getNodeTable() :
            graphModel.getEdgeTable();
        for (Column column : exporterSpreadsheet.getExportableColumns(graphModel, table)) {
            ColumnCheckboxWrapper checkBox = new ColumnCheckboxWrapper(column.getId(), column.getTitle());
            columnsCheckBoxes.add(checkBox);

            if (exporterSpreadsheet.getExcludedColumns().contains(column.getId())) {
                checkBox.setSelected(false);
            }

            columnsPanel.add(checkBox, "wrap");
        }

        columnsPanel.revalidate();
        columnsPanel.repaint();
    }

    private String getMessage(String resName) {
        return NbBundle.getMessage(UIExporterSpreadsheetPanel.class, resName);
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
        decimalSeparatorLabel = new javax.swing.JLabel();
        decimalSeparatorComboBox = new javax.swing.JComboBox();
        labelNormalize = new javax.swing.JLabel();
        normalizeCheckbox = new javax.swing.JCheckBox();
        dynamicExportCheckbox = new javax.swing.JCheckBox();
        attributesExportCheckbox = new javax.swing.JCheckBox();
        sizeExportCheckbox = new javax.swing.JCheckBox();
        colorsExportCheckbox = new javax.swing.JCheckBox();
        positionExportCheckbox = new javax.swing.JCheckBox();
        labelExport = new javax.swing.JLabel();

        separatorLabel.setText(org.openide.util.NbBundle.getMessage(UIExporterSpreadsheetPanel.class,
            "UIExporterSpreadsheetPanel.separatorLabel.text")); // NOI18N

        columnsPanel.setLayout(new java.awt.GridLayout(1, 0));
        scroll.setViewportView(columnsPanel);

        columnsLabel.setText(org.openide.util.NbBundle.getMessage(UIExporterSpreadsheetPanel.class,
            "UIExporterSpreadsheetPanel.columnsLabel.text")); // NOI18N

        tableLabel.setText(org.openide.util.NbBundle.getMessage(UIExporterSpreadsheetPanel.class,
            "UIExporterSpreadsheetPanel.tableLabel.text")); // NOI18N

        tableComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tableComboBoxActionPerformed(evt);
            }
        });

        decimalSeparatorLabel.setText(org.openide.util.NbBundle.getMessage(UIExporterSpreadsheetPanel.class,
            "UIExporterSpreadsheetPanel.decimalSeparatorLabel.text")); // NOI18N

        labelNormalize.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
        labelNormalize.setForeground(new java.awt.Color(102, 102, 102));
        labelNormalize.setText(org.openide.util.NbBundle.getMessage(UIExporterSpreadsheetPanel.class,
            "UIExporterSpreadsheetPanel.labelNormalize.text")); // NOI18N

        normalizeCheckbox.setText(org.openide.util.NbBundle.getMessage(UIExporterSpreadsheetPanel.class,
            "UIExporterSpreadsheetPanel.normalizeCheckbox.text")); // NOI18N

        dynamicExportCheckbox.setText(org.openide.util.NbBundle.getMessage(UIExporterSpreadsheetPanel.class,
            "UIExporterSpreadsheetPanel.dynamicExportCheckbox.text")); // NOI18N

        attributesExportCheckbox.setText(org.openide.util.NbBundle.getMessage(UIExporterSpreadsheetPanel.class,
            "UIExporterSpreadsheetPanel.attributesExportCheckbox.text")); // NOI18N
        attributesExportCheckbox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                attributesExportCheckboxActionPerformed(evt);
            }
        });

        sizeExportCheckbox.setText(org.openide.util.NbBundle.getMessage(UIExporterSpreadsheetPanel.class,
            "UIExporterSpreadsheetPanel.sizeExportCheckbox.text")); // NOI18N

        colorsExportCheckbox.setText(org.openide.util.NbBundle.getMessage(UIExporterSpreadsheetPanel.class,
            "UIExporterSpreadsheetPanel.colorsExportCheckbox.text")); // NOI18N

        positionExportCheckbox.setText(org.openide.util.NbBundle.getMessage(UIExporterSpreadsheetPanel.class,
            "UIExporterSpreadsheetPanel.positionExportCheckbox.text")); // NOI18N

        labelExport.setText(org.openide.util.NbBundle.getMessage(UIExporterSpreadsheetPanel.class,
            "UIExporterSpreadsheetPanel.labelExport.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(scroll)
                        .addGroup(layout.createSequentialGroup()
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(layout.createSequentialGroup()
                                    .addGroup(
                                        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                            .addComponent(tableLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 1,
                                                Short.MAX_VALUE)
                                            .addComponent(separatorLabel, javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                    .addGap(18, 18, Short.MAX_VALUE))
                                .addGroup(layout.createSequentialGroup()
                                    .addComponent(decimalSeparatorLabel, javax.swing.GroupLayout.DEFAULT_SIZE,
                                        javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addGap(16, 16, 16)))
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(decimalSeparatorComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE,
                                    Short.MAX_VALUE)
                                .addComponent(separatorComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE,
                                    Short.MAX_VALUE)
                                .addComponent(tableComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(normalizeCheckbox)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(labelNormalize, javax.swing.GroupLayout.DEFAULT_SIZE,
                                javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGroup(layout.createSequentialGroup()
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(columnsLabel)
                                .addGroup(layout.createSequentialGroup()
                                    .addComponent(labelExport)
                                    .addGap(18, 18, 18)
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(colorsExportCheckbox)
                                        .addComponent(sizeExportCheckbox)
                                        .addGroup(layout.createSequentialGroup()
                                            .addComponent(attributesExportCheckbox)
                                            .addGap(18, 18, 18)
                                            .addComponent(dynamicExportCheckbox))
                                        .addComponent(positionExportCheckbox))))
                            .addGap(0, 0, Short.MAX_VALUE)))
                    .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(tableLabel)
                        .addComponent(tableComboBox, javax.swing.GroupLayout.PREFERRED_SIZE,
                            javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(separatorLabel)
                        .addComponent(separatorComboBox, javax.swing.GroupLayout.PREFERRED_SIZE,
                            javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(decimalSeparatorComboBox, javax.swing.GroupLayout.PREFERRED_SIZE,
                            javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(decimalSeparatorLabel))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(positionExportCheckbox)
                        .addComponent(labelExport))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED,
                        javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(colorsExportCheckbox)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(sizeExportCheckbox)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(attributesExportCheckbox)
                        .addComponent(dynamicExportCheckbox))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(normalizeCheckbox)
                        .addComponent(labelNormalize))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                    .addComponent(columnsLabel)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(scroll, javax.swing.GroupLayout.PREFERRED_SIZE, 102,
                        javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void tableComboBoxActionPerformed(
        java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tableComboBoxActionPerformed
        refreshColumns();
    }//GEN-LAST:event_tableComboBoxActionPerformed

    private void attributesExportCheckboxActionPerformed(
        java.awt.event.ActionEvent evt) {//GEN-FIRST:event_attributesExportCheckboxActionPerformed
        exporterSpreadsheet.setExportAttributes(attributesExportCheckbox.isSelected());
        refreshColumns();
    }//GEN-LAST:event_attributesExportCheckboxActionPerformed

    private static class SeparatorWrapper {

        private final Character separator;
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

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 37 * hash + Objects.hashCode(this.separator);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final SeparatorWrapper other = (SeparatorWrapper) obj;
            return Objects.equals(this.separator, other.separator);
        }


    }

    private static class ColumnCheckboxWrapper extends JCheckBox {

        private final String id;

        public ColumnCheckboxWrapper(String id, String title) {
            super(title, true);
            this.id = id;
        }
    }
}
