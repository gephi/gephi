/*
Copyright 2008-2011 Gephi
Authors : Taras Klaskovsky <megaterik@gmail.com>
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

package org.gephi.ui.exporter.plugin;

import org.gephi.io.exporter.plugin.ExporterDL;

public class UIExporterDLPanel extends javax.swing.JPanel {


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JRadioButton listRadioButton;
    private javax.swing.JRadioButton matrixRadioButton;
    private javax.swing.JCheckBox symmetricCheckBox;
    // End of variables declaration//GEN-END:variables

    /**
     * Creates new customizer UIExporterDLPanel
     */
    public UIExporterDLPanel() {
        initComponents();
    }

    void setup(ExporterDL exporter) {
        // normalizeCheckBox.setSelected(exporter.isNormalize());
        matrixRadioButton.setSelected(exporter.isUseMatrixFormat());
        listRadioButton.setSelected(exporter.isUseListFormat());
        symmetricCheckBox.setSelected(exporter.isMakeSymmetricMatrix());
        symmetricCheckBox.setEnabled(matrixRadioButton.isSelected());
    }

    void unsetup(ExporterDL exporter) {
        // exporter.setNormalize(normalizeCheckBox.isSelected());
        exporter.setUseMatrixFormat(matrixRadioButton.isSelected());
        exporter.setUseListFormat(listRadioButton.isSelected());
        exporter.setMakeSymmetricMatrix(symmetricCheckBox.isSelected());
    }

    /**
     * This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the FormEditor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        matrixRadioButton = new javax.swing.JRadioButton();
        listRadioButton = new javax.swing.JRadioButton();
        symmetricCheckBox = new javax.swing.JCheckBox();

        matrixRadioButton.setText("Matrix");
        matrixRadioButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                matrixRadioButtonActionPerformed(evt);
            }
        });

        listRadioButton.setText("List");
        listRadioButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                listRadioButtonActionPerformed(evt);
            }
        });

        symmetricCheckBox.setLabel("Symmetric");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(listRadioButton, javax.swing.GroupLayout.PREFERRED_SIZE, 86,
                        javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(symmetricCheckBox)
                        .addComponent(matrixRadioButton))
                    .addContainerGap(99, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(listRadioButton)
                        .addComponent(matrixRadioButton))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(symmetricCheckBox)
                    .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        symmetricCheckBox.getAccessibleContext().setAccessibleName("symmetricCheckBox");
    }// </editor-fold>//GEN-END:initComponents

    private void matrixRadioButtonActionPerformed(
        java.awt.event.ActionEvent evt) {//GEN-FIRST:event_matrixRadioButtonActionPerformed
        listRadioButton.setSelected(!matrixRadioButton.isSelected());
        symmetricCheckBox.setEnabled(matrixRadioButton.isSelected());
    }//GEN-LAST:event_matrixRadioButtonActionPerformed

    private void listRadioButtonActionPerformed(
        java.awt.event.ActionEvent evt) {//GEN-FIRST:event_listRadioButtonActionPerformed
        matrixRadioButton.setSelected(!listRadioButton.isSelected());
        symmetricCheckBox.setEnabled(matrixRadioButton.isSelected());
    }//GEN-LAST:event_listRadioButtonActionPerformed
}
