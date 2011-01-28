/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
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
package org.gephi.ui.components;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

//Inspired by org.netbeans.swing.etable
public class ColumnSelectionPanel extends JPanel {

    private Map<ColumnSelectionModel, JCheckBox> checkBoxes;

    public ColumnSelectionPanel(ColumnSelectionModel[] columns) {
        checkBoxes = new HashMap<ColumnSelectionModel, JCheckBox>();
        setLayout(new GridBagLayout());
        init(columns);
    }

    public void init(ColumnSelectionModel[] columns) {
        int i = 0;
        int j = 0;
        int width = 1;
        int rows = columns.length / width;

        for (int col = 0; col < columns.length; col++) {
            if (i >= rows) {
                i = 0;
                j++;
            }
            ColumnSelectionModel column = columns[col];
            JCheckBox checkBox = new JCheckBox();
            checkBox.setText(column.getName());
            checkBox.setSelected(column.isSelected());
            checkBox.setEnabled(column.isEnabled());

            GridBagConstraints gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = j;
            gridBagConstraints.gridy = i + i;
            gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 12);
            gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
            gridBagConstraints.weightx = 1;
            add(checkBox, gridBagConstraints);
            checkBoxes.put(column, checkBox);
            i++;
        }
    }

    private void applyDialogChanges() {
        for (Iterator<ColumnSelectionModel> it = checkBoxes.keySet().iterator(); it.hasNext();) {
            ColumnSelectionModel columnModel = it.next();
            JCheckBox checkBox = checkBoxes.get(columnModel);
            columnModel.setSelected(checkBox.isSelected());
        }
    }

    public static void showColumnSelectionPopup(ColumnSelectionModel[] columns, Component c) {
        JPopupMenu popup = new JPopupMenu();

        for (int col = 0; col < columns.length; col++) {
            final ColumnSelectionModel column = columns[col];
            final JCheckBoxMenuItem checkBox = new JCheckBoxMenuItem();
            checkBox.setText(column.getName());
            checkBox.setSelected(column.isSelected());
            checkBox.setEnabled(column.isEnabled());
            checkBox.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent evt) {
                    column.setSelected(checkBox.isSelected());
                }
            });
            popup.add(checkBox);
        }

        popup.show(c, 8, 8);
    }

    public static void showColumnSelectionDialog(ColumnSelectionModel[] columns, String dialogTitle) {
        ColumnSelectionPanel panel = new ColumnSelectionPanel(columns);
        int res = JOptionPane.showConfirmDialog(null, panel, dialogTitle, JOptionPane.OK_CANCEL_OPTION);
        if (res == JOptionPane.OK_OPTION) {
            panel.applyDialogChanges();
        }
    }

    public static interface ColumnSelectionModel {

        public boolean isEnabled();

        public boolean isSelected();

        public void setSelected(boolean selected);

        public String getName();
    }
}
