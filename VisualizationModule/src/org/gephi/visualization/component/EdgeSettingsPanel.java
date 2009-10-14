/*
Copyright 2008 WebAtlas
Authors : Mathieu Bastian, Mathieu Jacomy, Julian Bilcke
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
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
        edgeOutSelectionColorChooser.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                VizModel vizModel = VizController.getInstance().getVizModel();
                vizModel.setEdgeOutSelectionColor(edgeOutSelectionColorChooser.getColor().getComponents(null));
            }
        });
        edgeBothSelectionColorChooser.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                VizModel vizModel = VizController.getInstance().getVizModel();
                vizModel.setEdgeBothSelectionColor(edgeBothSelectionColorChooser.getColor().getComponents(null));
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
        if (!edgeOutSelectionColorChooser.getColor().equals(out)) {
            edgeOutSelectionColorChooser.setColor(out);
        }
        if (!edgeBothSelectionColorChooser.getColor().equals(both)) {
            edgeBothSelectionColorChooser.setColor(both);
        }
    }

    private void setEnable(boolean enable) {
        showEdgesCheckbox.setEnabled(enable);
        edgeColorButton.setEnabled(enable && showEdgesCheckbox.isSelected());
        sourceNodeColorCheckbox.setEnabled(enable && showEdgesCheckbox.isSelected());
        labelEdgeColor.setEnabled(enable && showEdgesCheckbox.isSelected());
        selectionColorCheckbox.setEnabled(enable && showEdgesCheckbox.isSelected());
        edgeInSelectionColorChooser.setEnabled(enable && showEdgesCheckbox.isSelected() && selectionColorCheckbox.isSelected());
        edgeOutSelectionColorChooser.setEnabled(enable && showEdgesCheckbox.isSelected() && selectionColorCheckbox.isSelected());
        edgeBothSelectionColorChooser.setEnabled(enable && showEdgesCheckbox.isSelected() && selectionColorCheckbox.isSelected());
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        showEdgesCheckbox = new javax.swing.JCheckBox();
        labelEdgeColor = new javax.swing.JLabel();
        edgeColorButton = new JColorButton(Color.BLACK);
        sourceNodeColorCheckbox = new javax.swing.JCheckBox();
        selectionColorCheckbox = new javax.swing.JCheckBox();
        edgeInSelectionColorChooser = new net.java.dev.colorchooser.ColorChooser();
        edgeBothSelectionColorChooser = new net.java.dev.colorchooser.ColorChooser();
        edgeOutSelectionColorChooser = new net.java.dev.colorchooser.ColorChooser();

        showEdgesCheckbox.setFont(new java.awt.Font("Tahoma", 1, 11));
        showEdgesCheckbox.setText(org.openide.util.NbBundle.getMessage(EdgeSettingsPanel.class, "EdgeSettingsPanel.showEdgesCheckbox.text")); // NOI18N

        labelEdgeColor.setText(org.openide.util.NbBundle.getMessage(EdgeSettingsPanel.class, "EdgeSettingsPanel.labelEdgeColor.text")); // NOI18N

        edgeColorButton.setText(org.openide.util.NbBundle.getMessage(EdgeSettingsPanel.class, "EdgeSettingsPanel.edgeColorButton.text")); // NOI18N

        sourceNodeColorCheckbox.setText(org.openide.util.NbBundle.getMessage(EdgeSettingsPanel.class, "EdgeSettingsPanel.sourceNodeColorCheckbox.text")); // NOI18N
        sourceNodeColorCheckbox.setBorder(null);
        sourceNodeColorCheckbox.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        sourceNodeColorCheckbox.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        sourceNodeColorCheckbox.setMargin(new java.awt.Insets(2, 0, 2, 2));

        selectionColorCheckbox.setText(org.openide.util.NbBundle.getMessage(EdgeSettingsPanel.class, "EdgeSettingsPanel.selectionColorCheckbox.text")); // NOI18N
        selectionColorCheckbox.setToolTipText(org.openide.util.NbBundle.getMessage(EdgeSettingsPanel.class, "EdgeSettingsPanel.selectionColorCheckbox.toolTipText")); // NOI18N
        selectionColorCheckbox.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        selectionColorCheckbox.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);

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

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(showEdgesCheckbox))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(19, 19, 19)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(labelEdgeColor)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(edgeColorButton, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(sourceNodeColorCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, 166, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(23, 23, 23)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(21, 21, 21)
                                .addComponent(edgeInSelectionColorChooser, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(edgeOutSelectionColorChooser, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(edgeBothSelectionColorChooser, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(selectionColorCheckbox))))
                .addContainerGap(327, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(showEdgesCheckbox)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(labelEdgeColor))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(32, 32, 32)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(selectionColorCheckbox, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(edgeColorButton, javax.swing.GroupLayout.DEFAULT_SIZE, 24, Short.MAX_VALUE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(sourceNodeColorCheckbox)
                    .addComponent(edgeInSelectionColorChooser, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(edgeOutSelectionColorChooser, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(edgeBothSelectionColorChooser, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(12, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private net.java.dev.colorchooser.ColorChooser edgeBothSelectionColorChooser;
    private javax.swing.JButton edgeColorButton;
    private net.java.dev.colorchooser.ColorChooser edgeInSelectionColorChooser;
    private net.java.dev.colorchooser.ColorChooser edgeOutSelectionColorChooser;
    private javax.swing.JLabel labelEdgeColor;
    private javax.swing.JCheckBox selectionColorCheckbox;
    private javax.swing.JCheckBox showEdgesCheckbox;
    private javax.swing.JCheckBox sourceNodeColorCheckbox;
    // End of variables declaration//GEN-END:variables
}
