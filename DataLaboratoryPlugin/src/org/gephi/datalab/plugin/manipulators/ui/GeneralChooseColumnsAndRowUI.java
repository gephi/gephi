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
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import net.miginfocom.swing.MigLayout;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.datalab.plugin.manipulators.GeneralColumnsAndRowChooser;
import org.gephi.datalab.spi.DialogControls;
import org.gephi.datalab.spi.Manipulator;
import org.gephi.datalab.spi.ManipulatorUI;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Node;

/**
 * UI for GeneralColumnsChooser (ClearNodesData and ClearEdgesData)
 * @author Eduardo Ramos <eduramiba@gmail.com>
 */
public class GeneralChooseColumnsAndRowUI extends javax.swing.JPanel implements ManipulatorUI {

    private GeneralColumnsAndRowChooser columnsAndRowChooser;
    private ColumnCheckBox[] columnsCheckBoxes;
    private Object[] rows;//Node or edge

    /** Creates new form GeneralChooseColumnsUI */
    public GeneralChooseColumnsAndRowUI(String rowDescription, String columnsDescription) {
        initComponents();
        rowDescriptionLabel.setText(rowDescription);
        columnsDescriptionLabel.setText(columnsDescription);
    }

    public void setup(Manipulator m, DialogControls dialogControls) {
        this.columnsAndRowChooser = (GeneralColumnsAndRowChooser) m;
        refreshColumns();
        refreshRows();
    }

    public void unSetup() {
        columnsAndRowChooser.setColumns(getChosenColumns());
        columnsAndRowChooser.setRow(rows[rowComboBox.getSelectedIndex()]);
    }

    public String getDisplayName() {
        return columnsAndRowChooser.getName();
    }

    public JPanel getSettingsPanel() {
        return this;
    }

    public boolean isModal() {
        return true;
    }

    public AttributeColumn[] getChosenColumns() {
        ArrayList<AttributeColumn> columnsToClearDataList = new ArrayList<AttributeColumn>();
        for (ColumnCheckBox c : columnsCheckBoxes) {
            if (c.isSelected()) {
                columnsToClearDataList.add(c.getColumn());
            }
        }
        return columnsToClearDataList.toArray(new AttributeColumn[0]);
    }

    private void refreshColumns() {
        AttributeColumn[] columns = columnsAndRowChooser.getColumns();
        columnsCheckBoxes = new ColumnCheckBox[columns.length];
        contentPanel.removeAll();
        contentPanel.setLayout(new MigLayout("", "[pref!]"));
        for (int i = 0; i < columns.length; i++) {
            columnsCheckBoxes[i] = new ColumnCheckBox(columns[i], true);
            contentPanel.add(columnsCheckBoxes[i].getCheckBox(), "wrap");
        }
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void refreshRows() {
        rows = columnsAndRowChooser.getRows();
        Object sourceRow = columnsAndRowChooser.getRow();
        Node node;
        Edge edge;
        //Prepare combo box with nodes/edges data:
        for (int i = 0; i < rows.length; i++) {
            if (rows[i] instanceof Node) {
                node = (Node) rows[i];
                rowComboBox.addItem(node.getId() + " - " + node.getNodeData().getLabel());
            } else {
                edge = (Edge) rows[i];
                rowComboBox.addItem(edge.getId() + " - " + edge.getEdgeData().getLabel());
            }
            if (rows[i] == sourceRow) {
                rowComboBox.setSelectedIndex(i);
            }
        }
    }

    private static class ColumnCheckBox {

        private JCheckBox checkBox;
        private AttributeColumn column;

        public ColumnCheckBox(AttributeColumn column, boolean selected) {
            checkBox = new JCheckBox(column.getTitle(), selected);
            this.column = column;
        }

        public void setSelected(boolean selected) {
            checkBox.setSelected(selected);
        }

        public boolean isSelected() {
            return checkBox.isSelected();
        }

        public JCheckBox getCheckBox() {
            return checkBox;
        }

        public AttributeColumn getColumn() {
            return column;
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

        contentScrollPane = new javax.swing.JScrollPane();
        contentPanel = new javax.swing.JPanel();
        columnsDescriptionLabel = new javax.swing.JLabel();
        rowDescriptionLabel = new javax.swing.JLabel();
        rowComboBox = new javax.swing.JComboBox();

        contentPanel.setLayout(new java.awt.GridLayout(1, 0));
        contentScrollPane.setViewportView(contentPanel);

        columnsDescriptionLabel.setText(null);

        rowDescriptionLabel.setText(null);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(columnsDescriptionLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 301, Short.MAX_VALUE)
                    .addComponent(contentScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 301, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(rowDescriptionLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 149, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(rowComboBox, 0, 142, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(rowDescriptionLabel)
                    .addComponent(rowComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(columnsDescriptionLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(contentScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 194, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel columnsDescriptionLabel;
    private javax.swing.JPanel contentPanel;
    private javax.swing.JScrollPane contentScrollPane;
    private javax.swing.JComboBox rowComboBox;
    private javax.swing.JLabel rowDescriptionLabel;
    // End of variables declaration//GEN-END:variables
}
