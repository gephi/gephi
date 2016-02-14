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
package org.gephi.desktop.datalab.general.actions;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import org.gephi.datalab.api.DataLaboratoryHelper;
import org.gephi.datalab.spi.columns.merge.AttributeColumnsMergeStrategy;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Table;
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
 * @author Eduardo Ramos
 */
public class MergeColumnsUI extends javax.swing.JPanel {

    private JButton okButton;

    public enum Mode {

        NODES_TABLE,
        EDGES_TABLE
    }
    private Mode mode = Mode.NODES_TABLE;
    private Table table;
    private final DefaultListModel availableColumnsModel;
    private final DefaultListModel columnsToMergeModel;
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

            @Override
            public void intervalAdded(ListDataEvent e) {
                refreshAvailableMergeStrategies();
            }

            @Override
            public void intervalRemoved(ListDataEvent e) {
                refreshAvailableMergeStrategies();
            }

            @Override
            public void contentsChanged(ListDataEvent e) {
                refreshAvailableMergeStrategies();
            }
        });
    }

    private void loadColumns() {
        availableColumnsModel.clear();
        columnsToMergeModel.clear();

        GraphModel am = Lookup.getDefault().lookup(GraphController.class).getGraphModel();
        Column[] columns;
        if (mode == Mode.NODES_TABLE) {
            table = am.getNodeTable();
            columns = table.toArray();
        } else {
            table = am.getEdgeTable();
            columns = table.toArray();
        }

        for (Column column : columns) {
            availableColumnsModel.addElement(new ColumnWrapper(column));
        }

        availableColumnsList.setModel(availableColumnsModel);
        columnsToMergeList.setModel(columnsToMergeModel);
    }

    private void refreshAvailableMergeStrategies() {
        //Save currently selected strategy index:
        int selectedStrategyIndex = availableStrategiesComboBox.getSelectedIndex();

        availableStrategiesComboBox.removeAllItems();

        Column[] columnsToMerge = getColumnsToMerge();

        if (columnsToMerge.length < 1) {
            return;
        }
        AttributeColumnsMergeStrategy[] strategies = DataLaboratoryHelper.getDefault().getAttributeColumnsMergeStrategies();
        ArrayList<AttributeColumnsMergeStrategy> availableStrategiesList = new ArrayList<>();
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

    private Column[] getColumnsToMerge() {
        Object[] elements = columnsToMergeModel.toArray();

        Column[] columns = new Column[elements.length];
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
            DataLaboratoryHelper.getDefault().executeManipulator(availableMergeStrategies[index]);
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

        private Column column;

        public ColumnWrapper(Column column) {
            this.column = column;
        }

        public Column getColumn() {
            return column;
        }

        public void setColumn(Column column) {
            this.column = column;
        }

        @Override
        public String toString() {
            return column.getTitle() + " -- " + column.getTypeClass().getSimpleName();
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

        @Override
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

        columnsToMergeList.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                columnsToMergeListMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(columnsToMergeList);

        description.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        description.setText(org.openide.util.NbBundle.getMessage(MergeColumnsUI.class, "MergeColumnsUI.description.text")); // NOI18N

        addColumnButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/gephi/desktop/datalab/resources/arrow.png"))); // NOI18N
        addColumnButton.setText(org.openide.util.NbBundle.getMessage(MergeColumnsUI.class, "MergeColumnsUI.addColumnButton.text")); // NOI18N
        addColumnButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addColumnButtonActionPerformed(evt);
            }
        });

        removeColumnButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/gephi/desktop/datalab/resources/arrow-180.png"))); // NOI18N
        removeColumnButton.setText(org.openide.util.NbBundle.getMessage(MergeColumnsUI.class, "MergeColumnsUI.removeColumnButton.text")); // NOI18N
        removeColumnButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeColumnButtonActionPerformed(evt);
            }
        });

        availableColumnsList.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                availableColumnsListMouseClicked(evt);
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
            @Override
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                availableStrategiesComboBoxItemStateChanged(evt);
            }
        });

        infoLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        infoLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/gephi/desktop/datalab/resources/info.png"))); // NOI18N
        infoLabel.setText(org.openide.util.NbBundle.getMessage(MergeColumnsUI.class, "MergeColumnsUI.infoLabel.text")); // NOI18N
        infoLabel.setEnabled(false);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(description, javax.swing.GroupLayout.DEFAULT_SIZE, 394, Short.MAX_VALUE)
                        .addContainerGap())
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(availableColumnsLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                            .addComponent(availableStrategiesLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 152, Short.MAX_VALUE))
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(10, 10, 10)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(addColumnButton)
                                    .addComponent(removeColumnButton))
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGap(14, 14, 14)
                                        .addComponent(columnsToMergeLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 149, Short.MAX_VALUE))
                                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 153, Short.MAX_VALUE)))
                                .addGap(30, 30, 30))
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
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
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
                    .addComponent(infoLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
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

    private void availableColumnsListMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_availableColumnsListMouseClicked
        if (evt.getClickCount() == 2) {
            int index = availableColumnsList.locationToIndex(evt.getPoint());
            availableColumnsList.setSelectedIndex(index);
            moveElementsFromListToOtherList(availableColumnsList, columnsToMergeList);
        }
    }//GEN-LAST:event_availableColumnsListMouseClicked

    private void columnsToMergeListMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_columnsToMergeListMouseClicked
        if (evt.getClickCount() == 2) {
            int index = columnsToMergeList.locationToIndex(evt.getPoint());
            columnsToMergeList.setSelectedIndex(index);
            moveElementsFromListToOtherList(columnsToMergeList, availableColumnsList);
        }
    }//GEN-LAST:event_columnsToMergeListMouseClicked

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
