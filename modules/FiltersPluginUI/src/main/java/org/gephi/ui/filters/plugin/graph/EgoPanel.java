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

package org.gephi.ui.filters.plugin.graph;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import org.gephi.filters.plugin.graph.EgoBuilder.EgoFilter;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * @author flomzey
 */
public class EgoPanel extends javax.swing.JPanel {

    private EgoFilter egoFilter;
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox considerNonDirectEdgesCheckBox;
    private javax.swing.JTextField depthTextField;
    private javax.swing.JLabel labelDepth;
    private javax.swing.JLabel labelMode;
    private javax.swing.JLabel labelNodeId;
    private javax.swing.JComboBox<String> modeComboBox;
    private javax.swing.JTextField nodeIdTextField;
    private javax.swing.JCheckBox withSelfCheckbox;
    // End of variables declaration//GEN-END:variables

    /**
     * Creates new form EgoPanel
     */
    public EgoPanel() {
        initComponents();

        nodeIdTextField.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                updateEgoNode();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateEgoNode();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateEgoNode();
            }

            private void updateEgoNode() {
                egoFilter.getProperties()[0].setValue(nodeIdTextField.getText());
            }

        });

        depthTextField.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                updateDepth();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateDepth();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateDepth();
            }

            private void updateDepth() {
                egoFilter.getProperties()[1].setValue(depthTextField.getText());
            }
        });

        modeComboBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                int i = modeComboBox.getSelectedIndex();
                egoFilter.getProperties()[4].setValue(EgoFilter.Mode.values()[i]);
            }
        });

        withSelfCheckbox.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                if (!egoFilter.isSelf() == withSelfCheckbox.isSelected()) {
                    egoFilter.getProperties()[2].setValue(withSelfCheckbox.isSelected());
                }
            }
        });

        considerNonDirectEdgesCheckBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (!egoFilter.isConsiderNonDirected() == considerNonDirectEdgesCheckBox.isSelected()) {
                    egoFilter.getProperties()[3].setValue(considerNonDirectEdgesCheckBox.isSelected());
                }
            }
        });
    }

    public void setup(EgoFilter egoFilter) {
        this.egoFilter = egoFilter;
        nodeIdTextField.setText(egoFilter.getPattern());
        withSelfCheckbox.setSelected(egoFilter.isSelf());
        modeComboBox.setSelectedIndex(egoFilter.getMode().ordinal());
        considerNonDirectEdgesCheckBox.setSelected(egoFilter.isConsiderNonDirected());
    }

    /**
     * This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        labelNodeId = new javax.swing.JLabel();
        nodeIdTextField = new javax.swing.JTextField();
        labelDepth = new javax.swing.JLabel();
        withSelfCheckbox = new javax.swing.JCheckBox();
        depthTextField = new javax.swing.JTextField();
        modeComboBox = new javax.swing.JComboBox<>();
        considerNonDirectEdgesCheckBox = new javax.swing.JCheckBox();
        labelMode = new javax.swing.JLabel();

        labelNodeId.setText(
                org.openide.util.NbBundle.getMessage(EgoPanel.class, "EgoPanel.labelNodeId.text")); // NOI18N

        nodeIdTextField.setText(
                org.openide.util.NbBundle.getMessage(EgoPanel.class, "EgoPanel.nodeIdTextField.text")); // NOI18N
        nodeIdTextField.setToolTipText(
                org.openide.util.NbBundle.getMessage(EgoPanel.class, "EgoPanel.nodeIdTextField.toolTipText")); // NOI18N

        labelDepth.setText(org.openide.util.NbBundle.getMessage(EgoPanel.class, "EgoPanel.labelDepth.text")); // NOI18N

        labelMode.setText(org.openide.util.NbBundle.getMessage(EgoPanel.class, "EgoPanel.labelMode.text")); // NOI18N

        withSelfCheckbox.setText(
                org.openide.util.NbBundle.getMessage(EgoPanel.class, "EgoPanel.withSelfCheckbox.text")); // NOI18N

        considerNonDirectEdgesCheckBox.setText(
                org.openide.util.NbBundle.getMessage(EgoPanel.class, "EgoPanel.considerNonDirectEdges.text")); // NOI18N

        depthTextField.setText(
                org.openide.util.NbBundle.getMessage(EgoPanel.class, "EgoPanel.depthTextField.text")); // NOI18N
        depthTextField.setToolTipText(
                org.openide.util.NbBundle.getMessage(EgoPanel.class, "EgoPanel.depthTextField.toolTipText")); // NOI18N

        modeComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] {
                org.openide.util.NbBundle.getMessage(EgoPanel.class, "EgoPanel.modeComboBoxOption0.text"),
                org.openide.util.NbBundle.getMessage(EgoPanel.class, "EgoPanel.modeComboBoxOption1.text"),
                org.openide.util.NbBundle.getMessage(EgoPanel.class, "EgoPanel.modeComboBoxOption2.text")}));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(layout.createSequentialGroup()
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                                .addComponent(labelNodeId, javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                        javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                                .addComponent(labelDepth, javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                        javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                                        .addComponent(labelMode, javax.swing.GroupLayout.Alignment.TRAILING,
                                                                javax.swing.GroupLayout.DEFAULT_SIZE, 190, Short.MAX_VALUE))
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addComponent(modeComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                        .addComponent(nodeIdTextField, javax.swing.GroupLayout.Alignment.TRAILING,
                                                                javax.swing.GroupLayout.DEFAULT_SIZE, 365, Short.MAX_VALUE)
                                                        .addComponent(depthTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 1,
                                                                Short.MAX_VALUE)))
                                        .addGroup(layout.createSequentialGroup()
                                                .addComponent(withSelfCheckbox)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(considerNonDirectEdgesCheckBox, javax.swing.GroupLayout.DEFAULT_SIZE,
                                                        javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addGap(120, 120, 120))
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addGap(6, 6, 6)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(labelNodeId)
                                        .addComponent(nodeIdTextField, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(6, 6, 6)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(depthTextField, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(labelDepth))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(modeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(labelMode))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(withSelfCheckbox)
                                        .addComponent(considerNonDirectEdgesCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
}
