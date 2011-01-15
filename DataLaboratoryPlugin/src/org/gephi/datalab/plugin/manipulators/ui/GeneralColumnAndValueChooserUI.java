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
package org.gephi.datalab.plugin.manipulators.ui;

import java.util.ArrayList;
import javax.swing.JPanel;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeTable;
import org.gephi.datalab.api.AttributeColumnsController;
import org.gephi.datalab.plugin.manipulators.GeneralColumnAndValueChooser;
import org.gephi.datalab.spi.DialogControls;
import org.gephi.datalab.spi.Manipulator;
import org.gephi.datalab.spi.ManipulatorUI;
import org.gephi.graph.api.Attributes;
import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;
import org.openide.util.Lookup;
import org.openide.util.NbPreferences;

/**
 * UI for GeneralColumnAndValueChooser
 * @author Eduardo Ramos <eduramiba@gmail.com>
 */
public class GeneralColumnAndValueChooserUI extends javax.swing.JPanel implements ManipulatorUI {

    private static final String AUTO_COMPLETE_SAVED_PREFERENCES = "GeneralColumnAndValueChooserUI_autoComplete";
    private GeneralColumnAndValueChooser manipulator;
    private AttributeColumn[] columns;
    private AttributeTable table;
    private String values[];
    private static String lastChosenColumn=null;//To try to preserve last chosen column only while this program execution.

    /** Creates new form GeneralColumnAndValueChooserUI */
    public GeneralColumnAndValueChooserUI() {
        initComponents();
    }

    public void setup(Manipulator m, DialogControls dialogControls) {
        this.manipulator = (GeneralColumnAndValueChooser) m;
        this.table = manipulator.getTable();
        refreshColumns();
        autoComplete.setSelected(NbPreferences.forModule(GeneralChooseColumnsAndRowUI.class).getBoolean(AUTO_COMPLETE_SAVED_PREFERENCES, false));
        refreshAutoComplete();
    }

    public void unSetup() {
        manipulator.setColumn(getChosenColumn());
        manipulator.setValue(valueComboBox.getSelectedItem() != null ? valueComboBox.getSelectedItem().toString() : null);
        NbPreferences.forModule(GeneralChooseColumnsAndRowUI.class).putBoolean(AUTO_COMPLETE_SAVED_PREFERENCES, autoComplete.isSelected());
        lastChosenColumn=getChosenColumn().getTitle();
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

    public AttributeColumn getChosenColumn() {
        if (columnComboBox.getSelectedIndex() != -1) {
            return columns[columnComboBox.getSelectedIndex()];
        } else {
            return null;
        }
    }

    private void refreshColumns() {
        columns = manipulator.getColumns();
        for (int i = 0; i < columns.length; i++) {
            columnComboBox.addItem(columns[i].getTitle());
        }
        if(lastChosenColumn!=null&&!lastChosenColumn.isEmpty()){
            columnComboBox.setSelectedItem(lastChosenColumn);
        }
    }
    private int lastFetchedColumn = -1;

    private void refreshAutoComplete() {
        Object currentValue = valueComboBox.getSelectedItem();
        if (autoComplete.isSelected()) {
            AttributeColumn column = getChosenColumn();
            if (column != null) {
                if ((lastFetchedColumn != columnComboBox.getSelectedIndex()) || values == null) {
                    ArrayList<String> valuesList = new ArrayList<String>();
                    Object value;
                    String str;
                    for (Attributes row : Lookup.getDefault().lookup(AttributeColumnsController.class).getTableAttributeRows(table)) {
                        value = row.getValue(column.getId());
                        if (value != null) {
                            str = value.toString();
                            if (!valuesList.contains(str)) {
                                valuesList.add(str);
                            }
                        }
                    }
                    
                    values = valuesList.toArray(new String[0]);
                    lastFetchedColumn = columnComboBox.getSelectedIndex();
                }
                valueComboBox.removeAllItems();
                for (String item : values) {
                    valueComboBox.addItem(item);
                }
                AutoCompleteDecorator.decorate(valueComboBox);
            }
        } else {
            valueComboBox.removeAllItems();
        }
        valueComboBox.setSelectedItem(currentValue);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        columnComboBox = new javax.swing.JComboBox();
        columnLabel = new javax.swing.JLabel();
        valueLabel = new javax.swing.JLabel();
        valueComboBox = new javax.swing.JComboBox();
        autoComplete = new javax.swing.JCheckBox();

        columnComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                columnComboBoxItemStateChanged(evt);
            }
        });

        columnLabel.setText(org.openide.util.NbBundle.getMessage(GeneralColumnAndValueChooserUI.class, "GeneralColumnAndValueChooserUI.columnLabel.text")); // NOI18N

        valueLabel.setText(org.openide.util.NbBundle.getMessage(GeneralColumnAndValueChooserUI.class, "GeneralColumnAndValueChooserUI.valueLabel.text")); // NOI18N

        valueComboBox.setEditable(true);

        autoComplete.setSelected(true);
        autoComplete.setText(org.openide.util.NbBundle.getMessage(GeneralColumnAndValueChooserUI.class, "GeneralColumnAndValueChooserUI.autoComplete.text")); // NOI18N
        autoComplete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                autoCompleteActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(valueLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(columnLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 63, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(valueComboBox, 0, 145, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(autoComplete))
                    .addComponent(columnComboBox, 0, 246, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(columnLabel)
                    .addComponent(columnComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(valueLabel)
                    .addComponent(valueComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(autoComplete))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void autoCompleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_autoCompleteActionPerformed
        refreshAutoComplete();
    }//GEN-LAST:event_autoCompleteActionPerformed

    private void columnComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_columnComboBoxItemStateChanged
        refreshAutoComplete();
    }//GEN-LAST:event_columnComboBoxItemStateChanged
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox autoComplete;
    private javax.swing.JComboBox columnComboBox;
    private javax.swing.JLabel columnLabel;
    private javax.swing.JComboBox valueComboBox;
    private javax.swing.JLabel valueLabel;
    // End of variables declaration//GEN-END:variables
}
