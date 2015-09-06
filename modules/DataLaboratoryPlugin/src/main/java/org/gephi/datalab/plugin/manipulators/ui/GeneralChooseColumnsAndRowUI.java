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
package org.gephi.datalab.plugin.manipulators.ui;

import java.util.ArrayList;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import net.miginfocom.swing.MigLayout;
import org.gephi.graph.api.Column;
import org.gephi.datalab.plugin.manipulators.GeneralColumnsAndRowChooser;
import org.gephi.datalab.spi.DialogControls;
import org.gephi.datalab.spi.Manipulator;
import org.gephi.datalab.spi.ManipulatorUI;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Element;
import org.gephi.graph.api.Node;

/**
 * UI for GeneralColumnsChooser (ClearNodesData and ClearEdgesData)
 * @author Eduardo Ramos
 */
public class GeneralChooseColumnsAndRowUI extends javax.swing.JPanel implements ManipulatorUI {

    private GeneralColumnsAndRowChooser columnsAndRowChooser;
    private ColumnCheckBox[] columnsCheckBoxes;
    private Element[] rows;//Node or edge

    /** Creates new form GeneralChooseColumnsUI */
    public GeneralChooseColumnsAndRowUI(String rowDescription, String columnsDescription) {
        initComponents();
        rowDescriptionLabel.setText(rowDescription);
        columnsDescriptionLabel.setText(columnsDescription);
    }

    @Override
    public void setup(Manipulator m, DialogControls dialogControls) {
        this.columnsAndRowChooser = (GeneralColumnsAndRowChooser) m;
        refreshColumns();
        refreshRows();
    }

    @Override
    public void unSetup() {
        columnsAndRowChooser.setColumns(getChosenColumns());
        columnsAndRowChooser.setRow(rows[rowComboBox.getSelectedIndex()]);
    }

    @Override
    public String getDisplayName() {
        return columnsAndRowChooser.getName();
    }

    @Override
    public JPanel getSettingsPanel() {
        return this;
    }

    @Override
    public boolean isModal() {
        return true;
    }

    public Column[] getChosenColumns() {
        ArrayList<Column> columnsToClearDataList = new ArrayList<Column>();
        for (ColumnCheckBox c : columnsCheckBoxes) {
            if (c.isSelected()) {
                columnsToClearDataList.add(c.getColumn());
            }
        }
        return columnsToClearDataList.toArray(new Column[0]);
    }

    private void refreshColumns() {
        Column[] columns = columnsAndRowChooser.getColumns();
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
                rowComboBox.addItem(node.getId() + " - " + node.getLabel());
            } else {
                edge = (Edge) rows[i];
                rowComboBox.addItem(edge.getId() + " - " + edge.getLabel());
            }
            if (rows[i] == sourceRow) {
                rowComboBox.setSelectedIndex(i);
            }
        }
    }

    private static class ColumnCheckBox {

        private JCheckBox checkBox;
        private Column column;

        public ColumnCheckBox(Column column, boolean selected) {
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

        public Column getColumn() {
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
