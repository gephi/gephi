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

    /** Creates new form EdgeSettingsPanel */
    public EdgeSettingsPanel() {
        initComponents();
    }

    public void setup() {
        VizModel vizModel = VizController.getInstance().getVizModel();
        vizModel.addPropertyChangeListener(new PropertyChangeListener() {

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
                } else if (evt.getPropertyName().equals("metaEdgeScale")) {
                    refreshSharedConfig();
                }
            }
        });
        refreshSharedConfig();

        showEdgesCheckbox.addItemListener(new ItemListener() {

            public void itemStateChanged(ItemEvent e) {
                VizModel vizModel = VizController.getInstance().getVizModel();
                vizModel.setShowEdges(showEdgesCheckbox.isSelected());
                setEnable(true);
            }
        });
        ((JColorButton) edgeColorButton).addPropertyChangeListener(JColorButton.EVENT_COLOR, new PropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent evt) {
                VizModel vizModel = VizController.getInstance().getVizModel();
                vizModel.setEdgeUniColor(((JColorButton) edgeColorButton).getColorArray());
            }
        });
        sourceNodeColorCheckbox.addItemListener(new ItemListener() {

            public void itemStateChanged(ItemEvent e) {
                VizModel vizModel = VizController.getInstance().getVizModel();
                vizModel.setEdgeHasUniColor(!sourceNodeColorCheckbox.isSelected());
            }
        });
        selectionColorCheckbox.addItemListener(new ItemListener() {

            public void itemStateChanged(ItemEvent e) {
                VizModel vizModel = VizController.getInstance().getVizModel();
                vizModel.setEdgeSelectionColor(selectionColorCheckbox.isSelected());
            }
        });
        edgeInSelectionColorChooser.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                VizModel vizModel = VizController.getInstance().getVizModel();
                vizModel.setEdgeInSelectionColor(edgeInSelectionColorChooser.getColor().getComponents(null));
            }
        });
        edgeBothSelectionColorChooser.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                VizModel vizModel = VizController.getInstance().getVizModel();
                vizModel.setEdgeBothSelectionColor(edgeBothSelectionColorChooser.getColor().getComponents(null));
            }
        });
        edgeOutSelectionColorChooser.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                VizModel vizModel = VizController.getInstance().getVizModel();
                vizModel.setEdgeOutSelectionColor(edgeOutSelectionColorChooser.getColor().getComponents(null));
            }
        });
        scaleSlider.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                VizModel vizModel = VizController.getInstance().getVizModel();
                if (vizModel.getEdgeScale() != (scaleSlider.getValue() / 10f + 0.1f)) {
                    vizModel.setEdgeScale(scaleSlider.getValue() / 10f + 0.1f);
                }
            }
        });
        metaScaleSlider.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                VizModel vizModel = VizController.getInstance().getVizModel();
                if (vizModel.getMetaEdgeScale() != (metaScaleSlider.getValue() / 10f + 0.1f)) {
                    vizModel.setMetaEdgeScale(metaScaleSlider.getValue() / 10f + 0.1f);
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
        if (!edgeBothSelectionColorChooser.getColor().equals(out)) {
            edgeBothSelectionColorChooser.setColor(out);
        }
        if (!edgeOutSelectionColorChooser.getColor().equals(both)) {
            edgeOutSelectionColorChooser.setColor(both);
        }
        if (scaleSlider.getValue() / 10f + 0.1f != vizModel.getEdgeScale()) {
            scaleSlider.setValue((int) ((vizModel.getEdgeScale() - 0.1f) * 10));
        }
        if (metaScaleSlider.getValue() / 10f + 0.1f != vizModel.getMetaEdgeScale()) {
            metaScaleSlider.setValue((int) ((vizModel.getMetaEdgeScale() - 0.1f) * 10));
        }
    }

    private void setEnable(boolean enable) {
        showEdgesCheckbox.setEnabled(enable);
        edgeColorButton.setEnabled(enable && showEdgesCheckbox.isSelected());
        sourceNodeColorCheckbox.setEnabled(enable && showEdgesCheckbox.isSelected());
        labelEdgeColor.setEnabled(enable && showEdgesCheckbox.isSelected());
        scaleSlider.setEnabled(enable && showEdgesCheckbox.isSelected());
        metaScaleSlider.setEnabled(enable && showEdgesCheckbox.isSelected());
        labelScale.setEnabled(enable && showEdgesCheckbox.isSelected());
        labelMetaScale.setEnabled(enable && showEdgesCheckbox.isSelected());
        selectionColorCheckbox.setEnabled(enable && showEdgesCheckbox.isSelected());
        edgeInSelectionColorChooser.setEnabled(enable && showEdgesCheckbox.isSelected() && selectionColorCheckbox.isSelected());
        edgeBothSelectionColorChooser.setEnabled(enable && showEdgesCheckbox.isSelected() && selectionColorCheckbox.isSelected());
        edgeOutSelectionColorChooser.setEnabled(enable && showEdgesCheckbox.isSelected() && selectionColorCheckbox.isSelected());
        labelIn.setEnabled(enable && showEdgesCheckbox.isSelected() && selectionColorCheckbox.isSelected());
        labelOut.setEnabled(enable && showEdgesCheckbox.isSelected() && selectionColorCheckbox.isSelected());
        labelBoth.setEnabled(enable && showEdgesCheckbox.isSelected() && selectionColorCheckbox.isSelected());
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        showEdgesCheckbox = new javax.swing.JCheckBox();
        labelEdgeColor = new javax.swing.JLabel();
        edgeColorButton = new JColorButton(Color.BLACK);
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
        metaScalePanel = new javax.swing.JPanel();
        labelMetaScale = new javax.swing.JLabel();
        metaScaleSlider = new javax.swing.JSlider();

        showEdgesCheckbox.setFont(new java.awt.Font("Tahoma", 1, 11));
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
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        selectionColorPanel.add(selectionColorCheckbox, gridBagConstraints);

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

        metaScalePanel.setOpaque(false);
        metaScalePanel.setLayout(new java.awt.GridBagLayout());

        labelMetaScale.setText(org.openide.util.NbBundle.getMessage(EdgeSettingsPanel.class, "EdgeSettingsPanel.labelMetaScale.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 5, 2, 0);
        metaScalePanel.add(labelMetaScale, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        metaScalePanel.add(metaScaleSlider, gridBagConstraints);

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
                        .addComponent(scalePanel, javax.swing.GroupLayout.PREFERRED_SIZE, 139, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(selectionColorPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(metaScalePanel, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(showEdgesCheckbox)))
                .addContainerGap(79, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(showEdgesCheckbox)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                    .addComponent(labelEdgeColor)
                    .addContainerGap(53, Short.MAX_VALUE))
                .addGroup(layout.createSequentialGroup()
                    .addGap(32, 32, 32)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(edgeColorButton, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(13, 13, 13)
                            .addComponent(sourceNodeColorCheckbox))
                        .addComponent(scalePanel, javax.swing.GroupLayout.DEFAULT_SIZE, 61, Short.MAX_VALUE)
                        .addComponent(selectionColorPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(metaScalePanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 61, Short.MAX_VALUE))
                    .addContainerGap()))
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
    private javax.swing.JLabel labelMetaScale;
    private javax.swing.JLabel labelOut;
    private javax.swing.JLabel labelScale;
    private javax.swing.JPanel metaScalePanel;
    private javax.swing.JSlider metaScaleSlider;
    private javax.swing.JPanel scalePanel;
    private javax.swing.JSlider scaleSlider;
    private javax.swing.JCheckBox selectionColorCheckbox;
    private javax.swing.JPanel selectionColorPanel;
    private javax.swing.JCheckBox showEdgesCheckbox;
    private javax.swing.JCheckBox sourceNodeColorCheckbox;
    // End of variables declaration//GEN-END:variables
}
