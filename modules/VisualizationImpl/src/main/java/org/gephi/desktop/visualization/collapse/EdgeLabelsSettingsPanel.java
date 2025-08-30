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

package org.gephi.desktop.visualization.collapse;

import java.awt.Color;
import java.awt.Component;
import java.awt.color.ColorSpace;
import java.beans.PropertyChangeEvent;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import org.gephi.ui.components.JColorButton;
import org.gephi.visualization.api.EdgeColorMode;
import org.gephi.visualization.api.LabelColorMode;
import org.gephi.visualization.api.LabelSizeMode;
import org.gephi.visualization.api.VisualisationModel;
import org.gephi.visualization.api.VisualizationController;
import org.gephi.visualization.api.VisualizationPropertyChangeListener;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;

/**
 * @author Mathieu Bastian
 */
public class EdgeLabelsSettingsPanel extends javax.swing.JPanel implements VisualizationPropertyChangeListener {

    private final VisualizationController vizController;
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox<EdgeColorMode> edgeColorCombo;
    private javax.swing.JButton edgeFontButton;
    private javax.swing.JSlider edgeSizeSlider;
    private javax.swing.JLabel labelEdgeColor;
    private javax.swing.JLabel labelEdgeFont;
    private javax.swing.JLabel labelEdgeScale;
    private javax.swing.JLabel labelEdgeSize;
    private javax.swing.JCheckBox showLabelsCheckbox;
    // End of variables declaration//GEN-END:variables

    /**
     * Creates new form EdgeSettingsPanel
     */
    public EdgeLabelsSettingsPanel() {
        initComponents();

        vizController = Lookup.getDefault().lookup(VisualizationController.class);

        final DefaultComboBoxModel<EdgeColorMode> colorModeModel = new DefaultComboBoxModel<>(EdgeColorMode.values());
        edgeColorCombo.setModel(colorModeModel);
        edgeColorCombo.addActionListener(
            e -> vizController.setEdgeColorMode((EdgeColorMode) edgeColorCombo.getSelectedItem()));
        edgeColorCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
                                                          boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof EdgeColorMode) {
                    label.setText(org.openide.util.NbBundle.getMessage(EdgeLabelsSettingsPanel.class, "EdgeColorMode."+((EdgeColorMode)value).name().toLowerCase()+".name"));
                    label.setIcon(ImageUtilities.loadIcon("VisualizationImpl/EdgeColorMode_" + ((EdgeColorMode) value).name() + ".svg"));
                } else {
                    throw new IllegalArgumentException("Expected EdgeColorMode");
                }
                return this;
            }
        });

        showLabelsCheckbox.addItemListener(e -> {
            vizController.setShowEdges(showLabelsCheckbox.isSelected());
            setEnable(true);
        });
    }

    public void setup(VisualisationModel model) {
        if (model == null) {
            setEnable(false);
            return;
        }
        refreshSharedConfig(model);
        setEnable(true);
        vizController.addPropertyChangeListener(this);
    }

    public void unsetup(VisualisationModel model) {
        vizController.removePropertyChangeListener(this);
    }

    @Override
    public void propertyChange(VisualisationModel model, PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals("showEdges")) {
            refreshSharedConfig(model);
        } else if (evt.getPropertyName().equals("edgeSelectionColor")) {
            refreshSharedConfig(model);
        } else if (evt.getPropertyName().equals("edgeInSelectionColor")) {
            refreshSharedConfig(model);
        } else if (evt.getPropertyName().equals("edgeOutSelectionColor")) {
            refreshSharedConfig(model);
        } else if (evt.getPropertyName().equals("edgeBothSelectionColor")) {
            refreshSharedConfig(model);
        } else if (evt.getPropertyName().equals("edgeScale")) {
            refreshSharedConfig(model);
        } else if (evt.getPropertyName().equals("edgeColorMode")) {
            refreshSharedConfig(model);
        }
    }

    private void refreshSharedConfig(VisualisationModel vizModel) {
        if (showLabelsCheckbox.isSelected() != vizModel.isShowEdges()) {
            showLabelsCheckbox.setSelected(vizModel.isShowEdges());
        }
        if (edgeColorCombo.getSelectedItem() != vizModel.getEdgeColorMode()) {
            edgeColorCombo.setSelectedItem(vizModel.getEdgeColorMode());
        }
    }

    private void setEnable(boolean enable) {
        showLabelsCheckbox.setEnabled(enable);
        edgeColorCombo.setEnabled(enable && showLabelsCheckbox.isSelected());
        labelEdgeColor.setEnabled(enable && showLabelsCheckbox.isSelected());
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        showLabelsCheckbox = new javax.swing.JCheckBox();
        labelEdgeColor = new javax.swing.JLabel();
        edgeColorCombo = new javax.swing.JComboBox<>();
        labelEdgeFont = new javax.swing.JLabel();
        edgeFontButton = new javax.swing.JButton();
        labelEdgeScale = new javax.swing.JLabel();
        edgeSizeSlider = new javax.swing.JSlider();
        labelEdgeSize = new javax.swing.JLabel();

        showLabelsCheckbox.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        showLabelsCheckbox.setText(org.openide.util.NbBundle.getMessage(EdgeLabelsSettingsPanel.class, "EdgeLabelsSettingsPanel.showLabelsCheckbox.text")); // NOI18N
        showLabelsCheckbox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showLabelsCheckboxActionPerformed(evt);
            }
        });

        labelEdgeColor.setText(org.openide.util.NbBundle.getMessage(EdgeLabelsSettingsPanel.class, "EdgeLabelsSettingsPanel.labelEdgeColor.text")); // NOI18N

        labelEdgeFont.setText(org.openide.util.NbBundle.getMessage(EdgeLabelsSettingsPanel.class, "EdgeLabelsSettingsPanel.labelEdgeFont.text")); // NOI18N
        labelEdgeFont.setMaximumSize(new java.awt.Dimension(60, 15));

        edgeFontButton.setText(org.openide.util.NbBundle.getMessage(EdgeLabelsSettingsPanel.class, "EdgeLabelsSettingsPanel.edgeFontButton.text")); // NOI18N

        labelEdgeScale.setText(org.openide.util.NbBundle.getMessage(EdgeLabelsSettingsPanel.class, "EdgeLabelsSettingsPanel.labelEdgeScale.text")); // NOI18N

        labelEdgeSize.setText(org.openide.util.NbBundle.getMessage(EdgeLabelsSettingsPanel.class, "EdgeLabelsSettingsPanel.labelEdgeSize.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(397, 397, 397)
                        .addComponent(labelEdgeScale)
                        .addGap(18, 18, 18)
                        .addComponent(edgeSizeSlider, 0, 194, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(showLabelsCheckbox))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(19, 19, 19)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(labelEdgeSize)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(labelEdgeColor)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(edgeColorCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(labelEdgeFont, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(edgeFontButton)))))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                            .addGap(9, 9, 9)
                            .addComponent(labelEdgeScale))
                        .addComponent(edgeSizeSlider, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(showLabelsCheckbox)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(labelEdgeColor)
                            .addComponent(edgeColorCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(labelEdgeFont, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(edgeFontButton))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(labelEdgeSize)
                        .addGap(6, 6, 6)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void showLabelsCheckboxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showLabelsCheckboxActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_showLabelsCheckboxActionPerformed
}
