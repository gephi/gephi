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
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.gephi.ui.components.JColorButton;
import org.gephi.visualization.VizController;
import org.gephi.visualization.VizModel;
import org.gephi.visualization.apiimpl.GraphIO;

/**
 *
 * @author Mathieu Bastian
 */
public class GlobalSettingsPanel extends javax.swing.JPanel {

    /** Creates new form GlobalSettingsPanel */
    public GlobalSettingsPanel() {
        initComponents();
    }

    public void setup() {
        VizModel vizModel = VizController.getInstance().getVizModel();
        vizModel.addPropertyChangeListener(new PropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName().equals("init")) {
                    refreshSharedConfig();
                    refreshZoom();
                } else if (evt.getPropertyName().equals("backgroundColor")) {
                    refreshSharedConfig();
                } else if (evt.getPropertyName().equals("autoSelectNeighbor")) {
                    refreshSharedConfig();
                } else if (evt.getPropertyName().equals("lightenNonSelectedAuto")) {
                    refreshSharedConfig();
                } else if (evt.getPropertyName().equals("use3d")) {
                    refreshSharedConfig();
                } else if (evt.getPropertyName().equals("cameraDistance")) {
                    refreshZoom();
                }
            }
        });
        refreshSharedConfig();
        hightlightCheckBox.addItemListener(new ItemListener() {

            public void itemStateChanged(ItemEvent e) {
                VizModel vizModel = VizController.getInstance().getVizModel();
                vizModel.setLightenNonSelectedAuto(hightlightCheckBox.isSelected());
            }
        });
        ((JColorButton) backgroundColorButton).addPropertyChangeListener(JColorButton.EVENT_COLOR, new PropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent evt) {
                VizModel vizModel = VizController.getInstance().getVizModel();
                vizModel.setBackgroundColor(((JColorButton) backgroundColorButton).getColor());
            }
        });
        autoSelectNeigborCheckbox.addItemListener(new ItemListener() {

            public void itemStateChanged(ItemEvent e) {
                VizModel vizModel = VizController.getInstance().getVizModel();
                vizModel.setAutoSelectNeighbor(autoSelectNeigborCheckbox.isSelected());
            }
        });
        zoomSlider.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                int cam = (int) VizController.getInstance().getVizModel().getCameraDistance();
                if (zoomSlider.getValue() != cam && cam < zoomSlider.getMaximum()) {
                    GraphIO io = VizController.getInstance().getGraphIO();
                    io.setCameraDistance(zoomSlider.getValue());
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
        if (autoSelectNeigborCheckbox.isSelected() != vizModel.isAutoSelectNeighbor()) {
            autoSelectNeigborCheckbox.setSelected(vizModel.isAutoSelectNeighbor());
        }
        ((JColorButton) backgroundColorButton).setColor(vizModel.getBackgroundColor());
        if (hightlightCheckBox.isSelected() != vizModel.isLightenNonSelectedAuto()) {
            hightlightCheckBox.setSelected(vizModel.isLightenNonSelectedAuto());
        }
    }

    private void setEnable(boolean enable) {
        autoSelectNeigborCheckbox.setEnabled(enable);
        backgroundColorButton.setEnabled(enable);
        hightlightCheckBox.setEnabled(enable);
        labelBackgroundColor.setEnabled(enable);
        labelZoom.setEnabled(enable);
        zoomSlider.setEnabled(enable);
    }

    private void refreshZoom() {
        int zoomValue = (int) VizController.getInstance().getVizModel().getCameraDistance();
        if (zoomSlider.getValue() != zoomValue) {
            System.out.println("refresh zoom " + zoomValue);
            zoomSlider.setValue(zoomValue);
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
        java.awt.GridBagConstraints gridBagConstraints;

        labelBackgroundColor = new javax.swing.JLabel();
        backgroundColorButton = new JColorButton(Color.BLACK);
        hightlightCheckBox = new javax.swing.JCheckBox();
        autoSelectNeigborCheckbox = new javax.swing.JCheckBox();
        zoomPanel = new javax.swing.JPanel();
        labelZoom = new javax.swing.JLabel();
        zoomSlider = new javax.swing.JSlider();

        labelBackgroundColor.setText(org.openide.util.NbBundle.getMessage(GlobalSettingsPanel.class, "GlobalSettingsPanel.labelBackgroundColor.text")); // NOI18N

        backgroundColorButton.setText(org.openide.util.NbBundle.getMessage(GlobalSettingsPanel.class, "GlobalSettingsPanel.backgroundColorButton.text")); // NOI18N

        hightlightCheckBox.setText(org.openide.util.NbBundle.getMessage(GlobalSettingsPanel.class, "GlobalSettingsPanel.hightlightCheckBox.text")); // NOI18N
        hightlightCheckBox.setBorder(null);
        hightlightCheckBox.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        hightlightCheckBox.setMargin(new java.awt.Insets(2, 0, 2, 2));

        autoSelectNeigborCheckbox.setText(org.openide.util.NbBundle.getMessage(GlobalSettingsPanel.class, "GlobalSettingsPanel.autoSelectNeigborCheckbox.text")); // NOI18N
        autoSelectNeigborCheckbox.setBorder(null);
        autoSelectNeigborCheckbox.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        autoSelectNeigborCheckbox.setMargin(new java.awt.Insets(2, 0, 2, 2));

        zoomPanel.setLayout(new java.awt.GridBagLayout());

        labelZoom.setText(org.openide.util.NbBundle.getMessage(GlobalSettingsPanel.class, "GlobalSettingsPanel.labelZoom.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 2, 0);
        zoomPanel.add(labelZoom, gridBagConstraints);

        zoomSlider.setMaximum(10000);
        zoomSlider.setValue(5000);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        zoomPanel.add(zoomSlider, gridBagConstraints);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(labelBackgroundColor)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(backgroundColorButton, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(autoSelectNeigborCheckbox))
                .addGap(27, 27, 27)
                .addComponent(zoomPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 147, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(28, 28, 28)
                .addComponent(hightlightCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(50, 50, 50))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(hightlightCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(zoomPanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addComponent(labelBackgroundColor, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(backgroundColorButton, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(autoSelectNeigborCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox autoSelectNeigborCheckbox;
    private javax.swing.JButton backgroundColorButton;
    private javax.swing.JCheckBox hightlightCheckBox;
    private javax.swing.JLabel labelBackgroundColor;
    private javax.swing.JLabel labelZoom;
    private javax.swing.JPanel zoomPanel;
    private javax.swing.JSlider zoomSlider;
    // End of variables declaration//GEN-END:variables
}
