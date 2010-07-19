/*
Copyright 2008-2010 Gephi
Authors : Eduardo Ramos <eduramiba@gmail.com>
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
package org.gephi.ui.general.actions;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeTable;
import org.gephi.datalaboratory.api.DataLaboratoryHelper;
import org.gephi.datalaboratory.spi.attributecolumns.mergestrategies.AttributeColumnsMergeStrategy;
import org.gephi.ui.components.richtooltip.RichTooltip;
import org.netbeans.validation.api.Problems;
import org.netbeans.validation.api.Validator;
import org.netbeans.validation.api.ui.ValidationGroup;
import org.netbeans.validation.api.ui.ValidationPanel;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * UI for choosing columns to merge and a merge strategy.
 * @author Eduardo Ramos <eduramiba@gmail.com>
 */
public class MergeColumnsUI extends javax.swing.JPanel {

    private JButton okButton;

    public enum Mode {

        NODES_TABLE,
        EDGES_TABLE
    }
    private Mode mode = Mode.NODES_TABLE;
    private AttributeTable table;
    private DefaultListModel availableColumnsModel;
    private DefaultListModel columnsToMergeModel;
    private AttributeColumnsMergeStrategy[] availableMergeStrategies;

    /** Creates new form MergeColumnsUI */
    public MergeColumnsUI() {
        initComponents();
        infoLabel.addMouseListener(new MouseAdapter() {

            RichTooltip richTooltip;

            @Override
            public void mouseEntered(MouseEvent e) {
                int index = availableStrategiesComboBox.getSelectedIndex();
                if (infoLabel.isEnabled() && index != -1) {
                    richTooltip = buildTooltip(availableMergeStrategies[index]);
                }

                if (richTooltip != null) {
                    richTooltip.showTooltip(infoLabel);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (richTooltip != null) {
                    richTooltip.hideTooltip();
                    richTooltip = null;
                }
            }

            private RichTooltip buildTooltip(AttributeColumnsMergeStrategy strategy) {
                if (strategy.getDescription() != null && !strategy.getDescription().isEmpty()) {
                    RichTooltip tooltip = new RichTooltip(strategy.getName(), strategy.getDescription());
                    if (strategy.getIcon() != null) {
                        tooltip.setMainImage(ImageUtilities.icon2Image(strategy.getIcon()));
                    }
                    return tooltip;
                } else {
                    return null;
                }
            }
        });
        availableColumnsModel = new DefaultListModel();
        columnsToMergeModel = new DefaultListModel();

        columnsToMergeModel.addListDataListener(new ListDataListener() {

            public void intervalAdded(ListDataEvent e) {
                refreshAvailableMergeStrategies();
            }

            public void intervalRemoved(ListDataEvent e) {
                refreshAvailableMergeStrategies();
            }

            public void contentsChanged(ListDataEvent e) {
                refreshAvailableMergeStrategies();
            }
        });
    }

    private void loadColumns() {
        availableColumnsModel.clear();
        columnsToMergeModel.clear();

        AttributeController ac = Lookup.getDefault().lookup(AttributeController.class);
        AttributeColumn[] columns;
        if (mode == Mode.NODES_TABLE) {
            table = ac.getModel().getNodeTable();
            columns = table.getColumns();
        } else {
            table = ac.getModel().getEdgeTable();
            columns = table.getColumns();
        }

        for (int i = 0; i < columns.length; i++) {
            availableColumnsModel.addElement(new ColumnWrapper(columns[i]));
        }

        availableColumnsList.setModel(availableColumnsModel);
        columnsToMergeList.setModel(columnsToMergeModel);
    }

    private void refreshAvailableMergeStrategies() {
        //Save currently selected strategy index:
        int selectedStrategyIndex = availableStrategiesComboBox.getSelectedIndex();

        availableStrategiesComboBox.removeAllItems();

        AttributeColumn[] columnsToMerge = getColumnsToMerge();

        if (columnsToMerge.length < 1) {
            return;
        }
        AttributeColumnsMergeStrategy[] strategies = Lookup.getDefault().lookup(DataLaboratoryHelper.class).getAttributeColumnsMergeStrategies();
        ArrayList<AttributeColumnsMergeStrategy> availableStrategiesList = new ArrayList<AttributeColumnsMergeStrategy>();
        for (AttributeColumnsMergeStrategy strategy : strategies) {
            strategy.setup(table, columnsToMerge);
            availableStrategiesList.add(strategy);//Add all but disallow executing the strategies that cannot be executed with given column
        }

        availableMergeStrategies = availableStrategiesList.toArray(new AttributeColumnsMergeStrategy[0]);
        for (AttributeColumnsMergeStrategy s : availableMergeStrategies) {
            availableStrategiesComboBox.addItem(s.getName());
        }

        if (selectedStrategyIndex >= 0 && selectedStrategyIndex < availableStrategiesComboBox.getItemCount()) {
            availableStrategiesComboBox.setSelectedIndex(selectedStrategyIndex);
        }
    }

    private void refreshOkButton() {
        if (okButton != null) {
            okButton.setEnabled(canExecuteSelectedStrategy());
        }
    }

    public boolean canExecuteSelectedStrategy() {
        int index = availableStrategiesComboBox.getSelectedIndex();
        boolean result;
        if (index != -1) {
            result = availableMergeStrategies[index].canExecute();
        } else {
            result = false;
        }
        return result;
    }

    private AttributeColumn[] getColumnsToMerge() {
        Object[] elements = columnsToMergeModel.toArray();

        AttributeColumn[] columns = new AttributeColumn[elements.length];
        for (int i = 0; i < elements.length; i++) {
            columns[i] = ((ColumnWrapper) elements[i]).getColumn();
        }
        return columns;
    }

    public void setup(Mode mode) {
        this.mode = mode;
        loadColumns();
    }

    public void execute() {
        int index = availableStrategiesComboBox.getSelectedIndex();
        if (index != -1) {
            Lookup.getDefault().lookup(DataLaboratoryHelper.class).executeManipulator(availableMergeStrategies[index]);
        }
    }

    public String getDisplayName() {
        return NbBundle.getMessage(MergeColumnsUI.class, "MergeColumnsUI.title");
    }

    public void setOkButton(JButton okButton) {
        this.okButton = okButton;
        refreshOkButton();
    }

    /**
     * Class to contain a column and return its name + type with toString method.
     */
    class ColumnWrapper {

        private AttributeColumn column;

        public ColumnWrapper(AttributeColumn column) {
            this.column = column;
        }

        public AttributeColumn getColumn() {
            return column;
        }

        public void setColumn(AttributeColumn column) {
            this.column = column;
        }

        @Override
        public String toString() {
            return column.getTitle() + " -- " + column.getType().getTypeString();
        }
    }

    private void moveElementsFromListToOtherList(JList sourceList, JList targetList) {
        DefaultListModel sourceModel, targetModel;
        sourceModel = (DefaultListModel) sourceList.getModel();
        targetModel = (DefaultListModel) targetList.getModel();
        Object[] selection = sourceList.getSelectedValues();
        for (Object element : selection) {
            sourceModel.removeElement(element);
            targetModel.addElement(element);
        }
    }

    public static ValidationPanel createValidationPanel(MergeColumnsUI innerPanel) {
        ValidationPanel validationPanel = new ValidationPanel();
        if (innerPanel == null) {
            innerPanel = new MergeColumnsUI();
        }
        validationPanel.setInnerComponent(innerPanel);

        ValidationGroup group = validationPanel.getValidationGroup();

        group.add(innerPanel.availableStrategiesComboBox, new MergeStrategyValidator(innerPanel));

        return validationPanel;
    }

    private static class MergeStrategyValidator implements Validator<ComboBoxModel> {

        private MergeColumnsUI ui;

        public MergeStrategyValidator(MergeColumnsUI ui) {
            this.ui = ui;
        }

        public boolean validate(Problems problems, String string, ComboBoxModel t) {
            if (t.getSelectedItem() != null) {
                if (ui.canExecuteSelectedStrategy()) {
                    return true;
                } else {
                    problems.add(NbBundle.getMessage(MergeColumnsUI.class, "MergeColumnsUI.problems.not_executable_strategy"));
                    return false;
                }
            } else {
                problems.add(NbBundle.getMessage(MergeColumnsUI.class, "MergeColumnsUI.problems.less_than_2_columns_selected"));
                return false;
            }
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

        jScrollPane1 = new javax.swing.JScrollPane();
        columnsToMergeList = new javax.swing.JList();
        description = new javax.swing.JLabel();
        addColumnButton = new javax.swing.JButton();
        removeColumnButton = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        availableColumnsList = new javax.swing.JList();
        availableColumnsLabel = new javax.swing.JLabel();
        columnsToMergeLabel = new javax.swing.JLabel();
        availableStrategiesLabel = new javax.swing.JLabel();
        availableStrategiesComboBox = new javax.swing.JComboBox();
        infoLabel = new javax.swing.JLabel();

        jScrollPane1.setViewportView(columnsToMergeList);

        description.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        description.setText(org.openide.util.NbBundle.getMessage(MergeColumnsUI.class, "MergeColumnsUI.description.text")); // NOI18N

        addColumnButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/gephi/ui/datatable/resources/arrow.png"))); // NOI18N
        addColumnButton.setText(org.openide.util.NbBundle.getMessage(MergeColumnsUI.class, "MergeColumnsUI.addColumnButton.text")); // NOI18N
        addColumnButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addColumnButtonActionPerformed(evt);
            }
        });

        removeColumnButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/gephi/ui/datatable/resources/arrow-180.png"))); // NOI18N
        removeColumnButton.setText(org.openide.util.NbBundle.getMessage(MergeColumnsUI.class, "MergeColumnsUI.removeColumnButton.text")); // NOI18N
        removeColumnButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeColumnButtonActionPerformed(evt);
            }
        });

        jScrollPane2.setViewportView(availableColumnsList);

        availableColumnsLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        availableColumnsLabel.setText(org.openide.util.NbBundle.getMessage(MergeColumnsUI.class, "MergeColumnsUI.availableColumnsLabel.text")); // NOI18N

        columnsToMergeLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        columnsToMergeLabel.setText(org.openide.util.NbBundle.getMessage(MergeColumnsUI.class, "MergeColumnsUI.columnsToMergeLabel.text")); // NOI18N

        availableStrategiesLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        availableStrategiesLabel.setText(org.openide.util.NbBundle.getMessage(MergeColumnsUI.class, "MergeColumnsUI.availableStrategiesLabel.text")); // NOI18N

        availableStrategiesComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                availableStrategiesComboBoxItemStateChanged(evt);
            }
        });

        infoLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        infoLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/gephi/ui/datatable/resources/info.png"))); // NOI18N
        infoLabel.setText(org.openide.util.NbBundle.getMessage(MergeColumnsUI.class, "MergeColumnsUI.infoLabel.text")); // NOI18N
        infoLabel.setEnabled(false);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(description, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 404, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(availableColumnsLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jScrollPane2, 0, 0, Short.MAX_VALUE)
                            .addComponent(availableStrategiesLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 152, Short.MAX_VALUE))
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(10, 10, 10)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addComponent(addColumnButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(removeColumnButton, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGap(14, 14, 14)
                                        .addComponent(columnsToMergeLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 149, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 153, javax.swing.GroupLayout.PREFERRED_SIZE))))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(availableStrategiesComboBox, 0, 218, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(infoLabel)
                                .addContainerGap())))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(description, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(availableColumnsLabel)
                            .addComponent(columnsToMergeLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 204, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 204, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(12, 12, 12))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(addColumnButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(removeColumnButton)
                        .addGap(94, 94, 94)))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(availableStrategiesComboBox, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(infoLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 20, Short.MAX_VALUE)
                    .addComponent(availableStrategiesLabel))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void addColumnButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addColumnButtonActionPerformed
        moveElementsFromListToOtherList(availableColumnsList, columnsToMergeList);
    }//GEN-LAST:event_addColumnButtonActionPerformed

    private void removeColumnButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeColumnButtonActionPerformed
        moveElementsFromListToOtherList(columnsToMergeList, availableColumnsList);
    }//GEN-LAST:event_removeColumnButtonActionPerformed

    private void availableStrategiesComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_availableStrategiesComboBoxItemStateChanged
        refreshOkButton();
        infoLabel.setEnabled(availableStrategiesComboBox.getSelectedIndex() != -1);
    }//GEN-LAST:event_availableStrategiesComboBoxItemStateChanged
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addColumnButton;
    private javax.swing.JLabel availableColumnsLabel;
    private javax.swing.JList availableColumnsList;
    private javax.swing.JComboBox availableStrategiesComboBox;
    private javax.swing.JLabel availableStrategiesLabel;
    private javax.swing.JLabel columnsToMergeLabel;
    private javax.swing.JList columnsToMergeList;
    private javax.swing.JLabel description;
    private javax.swing.JLabel infoLabel;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JButton removeColumnButton;
    // End of variables declaration//GEN-END:variables
}
