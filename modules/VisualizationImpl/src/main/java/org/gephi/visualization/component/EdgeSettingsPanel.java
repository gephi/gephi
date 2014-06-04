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
package org.gephi.visualization.component;

import java.awt.Color;
import java.awt.color.ColorSpace;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.gephi.ui.components.JColorButton;
import org.gephi.visualization.VizController;
import org.gephi.visualization.VizModel;

/**
 *
 * @author Mathieu Bastian
 */
public class EdgeSettingsPanel extends javax.swing.JPanel {

    /**
     * Creates new form EdgeSettingsPanel
     */
    public EdgeSettingsPanel() {
        initComponents();
    }

    public void setup() {
        VizModel vizModel = VizController.getInstance().getVizModel();
        vizModel.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName().equals("init")) {
                    refreshSharedConfig();
                } else if (evt.getPropertyName().equals("edgeHasUniColor")) {
                    refreshSharedConfig();
                } else if (evt.getPropertyName().equals("showEdges")) {
                    refreshSharedConfig();
                } else if (evt.getPropertyName().equals("edgeUniColor")) {
                    refreshSharedConfig();
                } else if (evt.getPropertyName().equals("edgeSelectionColor")) {
                    refreshSharedConfig();
                } else if (evt.getPropertyName().equals("edgeInSelectionColor")) {
                    refreshSharedConfig();
                } else if (evt.getPropertyName().equals("edgeOutSelectionColor")) {
                    refreshSharedConfig();
                } else if (evt.getPropertyName().equals("edgeBothSelectionColor")) {
                    refreshSharedConfig();
                } else if (evt.getPropertyName().equals("edgeScale")) {
                    refreshSharedConfig();
                }
            }
        });
        refreshSharedConfig();

        showEdgesCheckbox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                VizModel vizModel = VizController.getInstance().getVizModel();
                vizModel.setShowEdges(showEdgesCheckbox.isSelected());
                setEnable(true);
            }
        });
        ((JColorButton) edgeColorButton).addPropertyChangeListener(JColorButton.EVENT_COLOR, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                VizModel vizModel = VizController.getInstance().getVizModel();
                vizModel.setEdgeUniColor(((JColorButton) edgeColorButton).getColorArray());
            }
        });
        sourceNodeColorCheckbox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                VizModel vizModel = VizController.getInstance().getVizModel();
                vizModel.setEdgeHasUniColor(!sourceNodeColorCheckbox.isSelected());
            }
        });
        selectionColorCheckbox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                VizModel vizModel = VizController.getInstance().getVizModel();
                vizModel.setEdgeSelectionColor(selectionColorCheckbox.isSelected());
            }
        });
        edgeInSelectionColorChooser.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                VizModel vizModel = VizController.getInstance().getVizModel();
                vizModel.setEdgeInSelectionColor(edgeInSelectionColorChooser.getColor().getComponents(null));
            }
        });
        edgeBothSelectionColorChooser.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                VizModel vizModel = VizController.getInstance().getVizModel();
                vizModel.setEdgeBothSelectionColor(edgeBothSelectionColorChooser.getColor().getComponents(null));
            }
        });
        edgeOutSelectionColorChooser.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                VizModel vizModel = VizController.getInstance().getVizModel();
                vizModel.setEdgeOutSelectionColor(edgeOutSelectionColorChooser.getColor().getComponents(null));
            }
        });
        scaleSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                VizModel vizModel = VizController.getInstance().getVizModel();
                if (vizModel.getEdgeScale() != (scaleSlider.getValue() / 10f + 0.1f)) {
                    vizModel.setEdgeScale(scaleSlider.getValue() / 10f + 0.1f);
                }
            }
        });
    }

    private void refreshSharedConfig() {
        VizModel vizModel = VizController.getInstance().getVizModel();
        setEnable(!vizModel.isDefaultModel());
        if (vizModel.isDefaultModel()) {
            return;
        }
        if (showEdgesCheckbox.isSelected() != vizModel.isShowEdges()) {
            showEdgesCheckbox.setSelected(vizModel.isShowEdges());
        }
        float[] edgeCol = vizModel.getEdgeUniColor();
        ((JColorButton) edgeColorButton).setColor(new Color(edgeCol[0], edgeCol[1], edgeCol[2], edgeCol[3]));

        if (sourceNodeColorCheckbox.isSelected() != !vizModel.isEdgeHasUniColor()) {
            sourceNodeColorCheckbox.setSelected(!vizModel.isEdgeHasUniColor());
        }
        if (selectionColorCheckbox.isSelected() != vizModel.isEdgeSelectionColor()) {
            selectionColorCheckbox.setSelected(vizModel.isEdgeSelectionColor());
        }
        Color in = new Color(ColorSpace.getInstance(ColorSpace.CS_sRGB), vizModel.getEdgeInSelectionColor(), 1f);
        Color out = new Color(ColorSpace.getInstance(ColorSpace.CS_sRGB), vizModel.getEdgeOutSelectionColor(), 1f);
        Color both = new Color(ColorSpace.getInstance(ColorSpace.CS_sRGB), vizModel.getEdgeBothSelectionColor(), 1f);
        if (!edgeInSelectionColorChooser.getColor().equals(in)) {
            edgeInSelectionColorChooser.setColor(in);
        }
        if (!edgeBothSelectionColorChooser.getColor().equals(both)) {
            edgeBothSelectionColorChooser.setColor(both);
        }
        if (!edgeOutSelectionColorChooser.getColor().equals(out)) {
            edgeOutSelectionColorChooser.setColor(out);
        }
        if (scaleSlider.getValue() / 10f + 0.1f != vizModel.getEdgeScale()) {
            scaleSlider.setValue((int) ((vizModel.getEdgeScale() - 0.1f) * 10));
        }
    }

    private void setEnable(boolean enable) {
        showEdgesCheckbox.setEnabled(enable);
        edgeColorButton.setEnabled(enable && showEdgesCheckbox.isSelected());
        sourceNodeColorCheckbox.setEnabled(enable && showEdgesCheckbox.isSelected());
        labelEdgeColor.setEnabled(enable && showEdgesCheckbox.isSelected());
        scaleSlider.setEnabled(enable && showEdgesCheckbox.isSelected());
        labelScale.setEnabled(enable && showEdgesCheckbox.isSelected());
        selectionColorCheckbox.setEnabled(enable && showEdgesCheckbox.isSelected());
        edgeInSelectionColorChooser.setEnabled(enable && showEdgesCheckbox.isSelected() && selectionColorCheckbox.isSelected());
        edgeBothSelectionColorChooser.setEnabled(enable && showEdgesCheckbox.isSelected() && selectionColorCheckbox.isSelected());
        edgeOutSelectionColorChooser.setEnabled(enable && showEdgesCheckbox.isSelected() && selectionColorCheckbox.isSelected());
        labelIn.setEnabled(enable && showEdgesCheckbox.isSelected() && selectionColorCheckbox.isSelected());
        labelOut.setEnabled(enable && showEdgesCheckbox.isSelected() && selectionColorCheckbox.isSelected());
        labelBoth.setEnabled(enable && showEdgesCheckbox.isSelected() && selectionColorCheckbox.isSelected());
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

        showEdgesCheckbox = new javax.swing.JCheckBox();
        labelEdgeColor = new javax.swing.JLabel();
        edgeColorButton = new JColorButton(Color.BLACK, false, true);
        sourceNodeColorCheckbox = new javax.swing.JCheckBox();
        selectionColorPanel = new javax.swing.JPanel();
        selectionColorCheckbox = new javax.swing.JCheckBox();
        edgeInSelectionColorChooser = new net.java.dev.colorchooser.ColorChooser();
        edgeOutSelectionColorChooser = new net.java.dev.colorchooser.ColorChooser();
        edgeBothSelectionColorChooser = new net.java.dev.colorchooser.ColorChooser();
        labelIn = new javax.swing.JLabel();
        labelOut = new javax.swing.JLabel();
        labelBoth = new javax.swing.JLabel();
        scalePanel = new javax.swing.JPanel();
        labelScale = new javax.swing.JLabel();
        scaleSlider = new javax.swing.JSlider();

        showEdgesCheckbox.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        showEdgesCheckbox.setText(org.openide.util.NbBundle.getMessage(EdgeSettingsPanel.class, "EdgeSettingsPanel.showEdgesCheckbox.text")); // NOI18N

        labelEdgeColor.setText(org.openide.util.NbBundle.getMessage(EdgeSettingsPanel.class, "EdgeSettingsPanel.labelEdgeColor.text")); // NOI18N

        edgeColorButton.setText(org.openide.util.NbBundle.getMessage(EdgeSettingsPanel.class, "EdgeSettingsPanel.edgeColorButton.text")); // NOI18N

        sourceNodeColorCheckbox.setText(org.openide.util.NbBundle.getMessage(EdgeSettingsPanel.class, "EdgeSettingsPanel.sourceNodeColorCheckbox.text")); // NOI18N
        sourceNodeColorCheckbox.setBorder(null);
        sourceNodeColorCheckbox.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        sourceNodeColorCheckbox.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        sourceNodeColorCheckbox.setMargin(new java.awt.Insets(2, 0, 2, 2));

        selectionColorPanel.setOpaque(false);
        selectionColorPanel.setLayout(new java.awt.GridBagLayout());

        selectionColorCheckbox.setText(org.openide.util.NbBundle.getMessage(EdgeSettingsPanel.class, "EdgeSettingsPanel.selectionColorCheckbox.text")); // NOI18N
        selectionColorCheckbox.setToolTipText(org.openide.util.NbBundle.getMessage(EdgeSettingsPanel.class, "EdgeSettingsPanel.selectionColorCheckbox.toolTipText")); // NOI18N
        selectionColorCheckbox.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        selectionColorCheckbox.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        selectionColorCheckbox.setMaximumSize(new java.awt.Dimension(160, 18));
        selectionColorCheckbox.setMinimumSize(new java.awt.Dimension(160, 18));
        selectionColorCheckbox.setPreferredSize(new java.awt.Dimension(160, 18));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        selectionColorPanel.add(selectionColorCheckbox, gridBagConstraints);

        edgeInSelectionColorChooser.setMinimumSize(new java.awt.Dimension(14, 14));
        edgeInSelectionColorChooser.setPreferredSize(new java.awt.Dimension(14, 14));
        edgeInSelectionColorChooser.setToolTipText(org.openide.util.NbBundle.getMessage(EdgeSettingsPanel.class, "EdgeSettingsPanel.edgeInSelectionColorChooser.toolTipText")); // NOI18N

        javax.swing.GroupLayout edgeInSelectionColorChooserLayout = new javax.swing.GroupLayout(edgeInSelectionColorChooser);
        edgeInSelectionColorChooser.setLayout(edgeInSelectionColorChooserLayout);
        edgeInSelectionColorChooserLayout.setHorizontalGroup(
            edgeInSelectionColorChooserLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 12, Short.MAX_VALUE)
        );
        edgeInSelectionColorChooserLayout.setVerticalGroup(
            edgeInSelectionColorChooserLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 12, Short.MAX_VALUE)
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
        selectionColorPanel.add(edgeInSelectionColorChooser, gridBagConstraints);

        edgeOutSelectionColorChooser.setMinimumSize(new java.awt.Dimension(14, 14));
        edgeOutSelectionColorChooser.setPreferredSize(new java.awt.Dimension(14, 14));
        edgeOutSelectionColorChooser.setToolTipText(org.openide.util.NbBundle.getMessage(EdgeSettingsPanel.class, "EdgeSettingsPanel.edgeOutSelectionColorChooser.toolTipText")); // NOI18N

        javax.swing.GroupLayout edgeOutSelectionColorChooserLayout = new javax.swing.GroupLayout(edgeOutSelectionColorChooser);
        edgeOutSelectionColorChooser.setLayout(edgeOutSelectionColorChooserLayout);
        edgeOutSelectionColorChooserLayout.setHorizontalGroup(
            edgeOutSelectionColorChooserLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 12, Short.MAX_VALUE)
        );
        edgeOutSelectionColorChooserLayout.setVerticalGroup(
            edgeOutSelectionColorChooserLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 12, Short.MAX_VALUE)
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
        selectionColorPanel.add(edgeOutSelectionColorChooser, gridBagConstraints);

        edgeBothSelectionColorChooser.setMinimumSize(new java.awt.Dimension(14, 14));
        edgeBothSelectionColorChooser.setPreferredSize(new java.awt.Dimension(14, 14));
        edgeBothSelectionColorChooser.setToolTipText(org.openide.util.NbBundle.getMessage(EdgeSettingsPanel.class, "EdgeSettingsPanel.edgeBothSelectionColorChooser.toolTipText")); // NOI18N

        javax.swing.GroupLayout edgeBothSelectionColorChooserLayout = new javax.swing.GroupLayout(edgeBothSelectionColorChooser);
        edgeBothSelectionColorChooser.setLayout(edgeBothSelectionColorChooserLayout);
        edgeBothSelectionColorChooserLayout.setHorizontalGroup(
            edgeBothSelectionColorChooserLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 12, Short.MAX_VALUE)
        );
        edgeBothSelectionColorChooserLayout.setVerticalGroup(
            edgeBothSelectionColorChooserLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 12, Short.MAX_VALUE)
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
        selectionColorPanel.add(edgeBothSelectionColorChooser, gridBagConstraints);

        labelIn.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
        labelIn.setText(org.openide.util.NbBundle.getMessage(EdgeSettingsPanel.class, "EdgeSettingsPanel.labelIn.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 0, 0);
        selectionColorPanel.add(labelIn, gridBagConstraints);

        labelOut.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
        labelOut.setText(org.openide.util.NbBundle.getMessage(EdgeSettingsPanel.class, "EdgeSettingsPanel.labelOut.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(7, 10, 0, 0);
        selectionColorPanel.add(labelOut, gridBagConstraints);

        labelBoth.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
        labelBoth.setText(org.openide.util.NbBundle.getMessage(EdgeSettingsPanel.class, "EdgeSettingsPanel.labelBoth.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        selectionColorPanel.add(labelBoth, gridBagConstraints);

        scalePanel.setOpaque(false);
        scalePanel.setLayout(new java.awt.GridBagLayout());

        labelScale.setText(org.openide.util.NbBundle.getMessage(EdgeSettingsPanel.class, "EdgeSettingsPanel.labelScale.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 5, 2, 0);
        scalePanel.add(labelScale, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        scalePanel.add(scaleSlider, gridBagConstraints);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(19, 19, 19)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(labelEdgeColor)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(edgeColorButton, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(sourceNodeColorCheckbox))
                        .addGap(28, 28, 28)
                        .addComponent(scalePanel, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(selectionColorPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 176, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(showEdgesCheckbox)))
                .addContainerGap(146, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(showEdgesCheckbox)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(labelEdgeColor))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addGap(32, 32, 32)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(edgeColorButton, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(13, 13, 13)
                                .addComponent(sourceNodeColorCheckbox))
                            .addComponent(scalePanel, javax.swing.GroupLayout.DEFAULT_SIZE, 61, Short.MAX_VALUE)
                            .addComponent(selectionColorPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 61, Short.MAX_VALUE))))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private net.java.dev.colorchooser.ColorChooser edgeBothSelectionColorChooser;
    private javax.swing.JButton edgeColorButton;
    private net.java.dev.colorchooser.ColorChooser edgeInSelectionColorChooser;
    private net.java.dev.colorchooser.ColorChooser edgeOutSelectionColorChooser;
    private javax.swing.JLabel labelBoth;
    private javax.swing.JLabel labelEdgeColor;
    private javax.swing.JLabel labelIn;
    private javax.swing.JLabel labelOut;
    private javax.swing.JLabel labelScale;
    private javax.swing.JPanel scalePanel;
    private javax.swing.JSlider scaleSlider;
    private javax.swing.JCheckBox selectionColorCheckbox;
    private javax.swing.JPanel selectionColorPanel;
    private javax.swing.JCheckBox showEdgesCheckbox;
    private javax.swing.JCheckBox sourceNodeColorCheckbox;
    // End of variables declaration//GEN-END:variables
}
