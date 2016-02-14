/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
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
        checkBoxes = new HashMap<>();
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

                @Override
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
