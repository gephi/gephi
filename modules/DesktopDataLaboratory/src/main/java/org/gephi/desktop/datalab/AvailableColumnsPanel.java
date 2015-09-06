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
package org.gephi.desktop.datalab;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JCheckBox;
import net.miginfocom.swing.MigLayout;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Table;
import org.netbeans.validation.api.Problems;
import org.netbeans.validation.api.Severity;
import org.netbeans.validation.api.ui.ValidationGroup;
import org.netbeans.validation.api.ui.ValidationListener;
import org.netbeans.validation.api.ui.ValidationPanel;
import org.openide.util.NbBundle;

/**
 * UI for selecting available columns of a table in Data laboratory
 * @see AvailableColumnsModel
 * @author Eduardo
 */
public class AvailableColumnsPanel extends javax.swing.JPanel {

    private final Table table;
    private final AvailableColumnsModel availableColumnsModel;
    private Column[] columns;
    private JCheckBox[] columnsCheckBoxes;
    private AvailableColumnsValidator validator;

    /** Creates new form AvailableColumnsPanel */
    public AvailableColumnsPanel(Table table, AvailableColumnsModel availableColumnsModel) {
        initComponents();
        this.table = table;
        this.availableColumnsModel = availableColumnsModel;
        columns = table.toArray();
        refreshColumns();
        refreshAvailableColumnsControls();
    }

    public ValidationPanel getValidationPanel() {
        ValidationPanel validationPanel = new ValidationPanel();
        validationPanel.setInnerComponent(this);

        ValidationGroup group = validationPanel.getValidationGroup();
        group.add(validator = new AvailableColumnsValidator());
        refreshAvailableColumnsControls();
        return validationPanel;
    }

    private void refreshColumns() {
        columnsCheckBoxes = new JCheckBox[columns.length];
        contentPanel.removeAll();
        contentPanel.setLayout(new MigLayout("", "[pref!]"));
        for (int i = 0; i < columns.length; i++) {
            columnsCheckBoxes[i] = new JCheckBox(columns[i].getTitle(), availableColumnsModel.isColumnAvailable(columns[i]));
            columnsCheckBoxes[i].addActionListener(new ColumnCheckBoxListener(i));
            contentPanel.add(columnsCheckBoxes[i], "wrap");
        }
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void refreshAvailableColumnsControls() {
        boolean enabled = availableColumnsModel.canAddAvailableColumn();
        for (JCheckBox cb : columnsCheckBoxes) {
            if (!cb.isSelected()) {
                cb.setEnabled(enabled);
            }
        }
        if (validator != null) {
            validator.event();
        }
    }

    class ColumnCheckBoxListener implements ActionListener {

        private int index;

        public ColumnCheckBoxListener(int index) {
            this.index = index;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (columnsCheckBoxes[index].isSelected()) {
                availableColumnsModel.addAvailableColumn(columns[index]);
            } else {
                availableColumnsModel.removeAvailableColumn(columns[index]);
            }
            refreshAvailableColumnsControls();
        }
    }

    class AvailableColumnsValidator extends ValidationListener {

        @Override
        protected boolean validate(Problems prblms) {
            if (!availableColumnsModel.canAddAvailableColumn()) {
                prblms.add(NbBundle.getMessage(AvailableColumnsPanel.class, "AvailableColumnsPanel.maximum-available-columns.info"), Severity.INFO);
                return false;
            }
            return true;
        }

        public void event() {
            validate();
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

        scroll = new javax.swing.JScrollPane();
        contentPanel = new javax.swing.JPanel();
        descriptionLabel = new javax.swing.JLabel();

        contentPanel.setLayout(new java.awt.GridLayout());
        scroll.setViewportView(contentPanel);

        descriptionLabel.setText(org.openide.util.NbBundle.getMessage(AvailableColumnsPanel.class, "AvailableColumnsPanel.descriptionLabel.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(descriptionLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 341, Short.MAX_VALUE)
                    .addComponent(scroll, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 341, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(descriptionLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 29, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(scroll, javax.swing.GroupLayout.PREFERRED_SIZE, 238, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel contentPanel;
    private javax.swing.JLabel descriptionLabel;
    private javax.swing.JScrollPane scroll;
    // End of variables declaration//GEN-END:variables
}
