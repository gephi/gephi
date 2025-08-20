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
import java.beans.PropertyChangeEvent;
import org.gephi.ui.components.JColorButton;
import org.gephi.visualization.VizController;
import org.gephi.visualization.api.VisualisationModel;
import org.gephi.visualization.api.VisualizationController;
import org.gephi.visualization.api.VisualizationPropertyChangeListener;
import org.openide.util.Lookup;

/**
 * @author Mathieu Bastian
 */
public class GlobalSettingsPanel extends javax.swing.JPanel implements VisualizationPropertyChangeListener {

    private final VisualizationController vizController;
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox autoSelectNeigborCheckbox;
    private javax.swing.JButton backgroundColorButton;
    private javax.swing.JCheckBox hightlightCheckBox;
    private javax.swing.JLabel labelBackgroundColor;
    // End of variables declaration//GEN-END:variables

    /**
     * Creates new form GlobalSettingsPanel
     */
    public GlobalSettingsPanel() {
        initComponents();

        vizController = Lookup.getDefault().lookup(VizController.class);

        hightlightCheckBox.addItemListener(e -> {
            vizController.setLightenNonSelectedAuto(hightlightCheckBox.isSelected());
        });

        backgroundColorButton
            .addPropertyChangeListener(JColorButton.EVENT_COLOR, evt -> {
                vizController.setBackgroundColor(((JColorButton) backgroundColorButton).getColor());
            });

        autoSelectNeigborCheckbox.addItemListener(e -> {
            vizController.setAutoSelectNeighbors(autoSelectNeigborCheckbox.isSelected());
        });
    }

    @Override
    public void propertyChange(VisualisationModel model, PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals("backgroundColor")) {
            refreshSharedConfig(model);
        } else if (evt.getPropertyName().equals("autoSelectNeighbor")) {
            refreshSharedConfig(model);
        } else if (evt.getPropertyName().equals("lightenNonSelectedAuto")) {
            refreshSharedConfig(model);
        }
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

    private void refreshSharedConfig(VisualisationModel vizModel) {
        if (autoSelectNeigborCheckbox.isSelected() != vizModel.isAutoSelectNeighbors()) {
            autoSelectNeigborCheckbox.setSelected(vizModel.isAutoSelectNeighbors());
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

        labelBackgroundColor.setText(org.openide.util.NbBundle.getMessage(GlobalSettingsPanel.class,
            "GlobalSettingsPanel.labelBackgroundColor.text")); // NOI18N

        backgroundColorButton.setText(org.openide.util.NbBundle.getMessage(GlobalSettingsPanel.class,
            "GlobalSettingsPanel.backgroundColorButton.text")); // NOI18N

        hightlightCheckBox.setText(org.openide.util.NbBundle.getMessage(GlobalSettingsPanel.class,
            "GlobalSettingsPanel.hightlightCheckBox.text")); // NOI18N
        hightlightCheckBox.setBorder(null);
        hightlightCheckBox.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        hightlightCheckBox.setMargin(new java.awt.Insets(2, 0, 2, 2));

        autoSelectNeigborCheckbox.setText(org.openide.util.NbBundle.getMessage(GlobalSettingsPanel.class,
            "GlobalSettingsPanel.autoSelectNeigborCheckbox.text")); // NOI18N
        autoSelectNeigborCheckbox.setBorder(null);
        autoSelectNeigborCheckbox.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        autoSelectNeigborCheckbox.setMargin(new java.awt.Insets(2, 0, 2, 2));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(autoSelectNeigborCheckbox)
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(labelBackgroundColor)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(backgroundColorButton, javax.swing.GroupLayout.PREFERRED_SIZE, 24,
                                javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(18, 18, 18)
                            .addComponent(hightlightCheckBox)))
                    .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(labelBackgroundColor, javax.swing.GroupLayout.Alignment.LEADING,
                            javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING,
                            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(backgroundColorButton, javax.swing.GroupLayout.PREFERRED_SIZE, 24,
                                    javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(hightlightCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, 25,
                                    javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(autoSelectNeigborCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, 26,
                        javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
}
