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

    /**
     * Creates new form GlobalSettingsPanel
     */
    public GlobalSettingsPanel() {
        initComponents();
    }

    public void setup() {
        VizModel vizModel = VizController.getInstance().getVizModel();
        vizModel.addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName().equals("init")) {
                    refreshSharedConfig();
                } else if (evt.getPropertyName().equals("backgroundColor")) {
                    refreshSharedConfig();
                } else if (evt.getPropertyName().equals("autoSelectNeighbor")) {
                    refreshSharedConfig();
                } else if (evt.getPropertyName().equals("lightenNonSelectedAuto")) {
                    refreshSharedConfig();
                } else if (evt.getPropertyName().equals("use3d")) {
                    refreshSharedConfig();
                }
            }
        });
        refreshSharedConfig();
        hightlightCheckBox.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                VizModel vizModel = VizController.getInstance().getVizModel();
                vizModel.setLightenNonSelectedAuto(hightlightCheckBox.isSelected());
            }
        });
        ((JColorButton) backgroundColorButton).addPropertyChangeListener(JColorButton.EVENT_COLOR, new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                VizModel vizModel = VizController.getInstance().getVizModel();
                vizModel.setBackgroundColor(((JColorButton) backgroundColorButton).getColor());
            }
        });
        autoSelectNeigborCheckbox.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                VizModel vizModel = VizController.getInstance().getVizModel();
                vizModel.setAutoSelectNeighbor(autoSelectNeigborCheckbox.isSelected());
            }
        });
        zoomSlider.addChangeListener(new ChangeListener() {

            @Override
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
            zoomSlider.setValue(zoomValue);
        }
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

        zoomPanel.setOpaque(false);
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
                .addComponent(hightlightCheckBox)
                .addGap(32, 32, 32))
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
