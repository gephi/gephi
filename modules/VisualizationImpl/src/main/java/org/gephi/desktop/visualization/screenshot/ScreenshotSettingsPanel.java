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

package org.gephi.desktop.visualization.screenshot;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JFileChooser;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.gephi.visualization.api.ScreenshotModel;
import org.gephi.visualization.screenshot.ScreenshotControllerImpl;
import org.gephi.viz.engine.jogl.util.ScreenshotTaker;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;

/**
 * @author Mathieu Bastian
 */
public class ScreenshotSettingsPanel extends javax.swing.JPanel {

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox autoSaveCheckBox;
    private javax.swing.JSpinner customScaleFactorSpinner;
    private javax.swing.JLabel heightLabel;
    private javax.swing.JPanel imagePanel;
    private javax.swing.JLabel labelCustomScaleFactor;
    private javax.swing.JLabel labelHeight;
    private javax.swing.JLabel labelScaleFactor;
    private javax.swing.JLabel labelWidth;
    private javax.swing.JComboBox<String> scaleFactorCombo;
    private javax.swing.JButton selectDirectoryButton;
    private javax.swing.JCheckBox transparentBackgroundCheckbox;
    private javax.swing.JLabel widthLabel;
    // End of variables declaration//GEN-END:variables

    // Controller
    private final ScreenshotControllerImpl controller;
    //
    private int surfaceWidth;
    private int surfaceHeight;

    /**
     * Creates new form ScreenshotSettingsPanel
     */
    public ScreenshotSettingsPanel(ScreenshotControllerImpl screenshotController) {
        controller = screenshotController;
        initComponents();

        autoSaveCheckBox.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                selectDirectoryButton.setEnabled(autoSaveCheckBox.isSelected());
            }
        });

        // Setup combo listener for custom scale factor toggle
        scaleFactorCombo.addItemListener(e -> {
            String customElement =
                NbBundle.getMessage(ScreenshotSettingsPanel.class, "ScreenshotSettingsPanel.scaleFactorCombo.customItem");
            if (scaleFactorCombo.getSelectedItem() != null && scaleFactorCombo.getSelectedItem().equals(customElement)) {
                customScaleFactorSpinner.setVisible(true);
                labelCustomScaleFactor.setVisible(true);
                refreshWidthAndHeightLabels((Integer) customScaleFactorSpinner.getModel().getValue());
            } else if (scaleFactorCombo.getSelectedItem() != null) {
                customScaleFactorSpinner.setVisible(false);
                labelCustomScaleFactor.setVisible(false);
                customScaleFactorSpinner.getModel()
                    .setValue(Integer.parseInt(scaleFactorCombo.getSelectedItem().toString().replace("x", "")));
            }
        });
        customScaleFactorSpinner.addChangeListener(
            e -> refreshWidthAndHeightLabels((Integer) customScaleFactorSpinner.getModel().getValue()));
    }

    private void refreshWidthAndHeightLabels(int scaleFactor) {
        if (this.surfaceHeight == 0 || this.surfaceWidth == 0) {
            widthLabel.setText("0");
            heightLabel.setText("0");
            return;
        }
        int width = this.surfaceWidth * scaleFactor;
        int height = this.surfaceHeight * scaleFactor;
        widthLabel.setText(width + "px");
        heightLabel.setText(height + "px");
    }

    public void setup(final ScreenshotModel model) {
        this.surfaceHeight = controller.getSurfaceHeight();
        this.surfaceWidth = controller.getSurfaceWidth();
        
        // Calculate maximum scale factor using worst case (transparent = 4 bytes/pixel)
        int maxScaleFactor = ScreenshotTaker.getMaxScaleFactor(surfaceWidth, surfaceHeight, true);
        
        // Build combo model with scale factors up to the maximum
        String customElement =
            NbBundle.getMessage(ScreenshotSettingsPanel.class, "ScreenshotSettingsPanel.scaleFactorCombo.customItem");
        DefaultComboBoxModel<String> comboModel = new DefaultComboBoxModel<>();
        int[] standardScaleFactors = {1, 2, 4, 8, 16, 32, 64};
        for (int scaleFactor : standardScaleFactors) {
            if (scaleFactor <= maxScaleFactor) {
                comboModel.addElement(scaleFactor + "x");
            }
        }
        comboModel.addElement(customElement);
        scaleFactorCombo.setModel(comboModel);
        
        // Update spinner model with maximum
        SpinnerNumberModel spinnerModel = new SpinnerNumberModel(
            Math.min(model.getScaleFactor(), maxScaleFactor), // current value
            1, // minimum
            maxScaleFactor, // maximum
            1  // step
        );
        customScaleFactorSpinner.setModel(spinnerModel);
        
        autoSaveCheckBox.setSelected(model.isAutoSave());
        selectDirectoryButton.setEnabled(autoSaveCheckBox.isSelected());
        customScaleFactorSpinner.setVisible(false);
        labelCustomScaleFactor.setVisible(false);
        
        // Select appropriate combo item based on model's scale factor
        int modelScaleFactor = Math.min(model.getScaleFactor(), maxScaleFactor);
        boolean foundInCombo = false;
        for (int i = 0; i < comboModel.getSize() - 1; i++) { // -1 to exclude custom element
            String item = comboModel.getElementAt(i);
            int itemScaleFactor = Integer.parseInt(item.replace("x", ""));
            if (itemScaleFactor == modelScaleFactor) {
                scaleFactorCombo.setSelectedIndex(i);
                foundInCombo = true;
                break;
            }
        }
        
        if (!foundInCombo) {
            // Select custom and show spinner
            scaleFactorCombo.setSelectedIndex(comboModel.getSize() - 1);
            customScaleFactorSpinner.setValue(modelScaleFactor);
            customScaleFactorSpinner.setVisible(true);
            labelCustomScaleFactor.setVisible(true);
        }
        
        transparentBackgroundCheckbox.setSelected(model.isTransparentBackground());
        
        selectDirectoryButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser(model.getDefaultDirectory());
                fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                int result = fileChooser.showOpenDialog(WindowManager.getDefault().getMainWindow());
                if (result == JFileChooser.APPROVE_OPTION) {
                    controller.setDefaultDirectory(fileChooser.getSelectedFile());
                }
            }
        });
        refreshWidthAndHeightLabels((Integer) customScaleFactorSpinner.getModel().getValue());
    }

    public void unsetup() {
        controller.setAutoSave(autoSaveCheckBox.isSelected());
        
        // Get scale factor from either combo or custom spinner
        String customElement =
            NbBundle.getMessage(ScreenshotSettingsPanel.class, "ScreenshotSettingsPanel.scaleFactorCombo.customItem");
        Object selectedItem = scaleFactorCombo.getSelectedItem();
        
        if (selectedItem != null && selectedItem.equals(customElement)) {
            controller.setScaleFactor((Integer) customScaleFactorSpinner.getModel().getValue());
        } else if (selectedItem != null) {
            controller.setScaleFactor(Integer.parseInt(selectedItem.toString().replace("x", "")));
        }
        
        controller.setTransparentBackground(transparentBackgroundCheckbox.isSelected());
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        imagePanel = new javax.swing.JPanel();
        labelScaleFactor = new javax.swing.JLabel();
        scaleFactorCombo = new javax.swing.JComboBox<>();
        transparentBackgroundCheckbox = new javax.swing.JCheckBox();
        labelCustomScaleFactor = new javax.swing.JLabel();
        customScaleFactorSpinner = new javax.swing.JSpinner();
        labelWidth = new javax.swing.JLabel();
        widthLabel = new javax.swing.JLabel();
        labelHeight = new javax.swing.JLabel();
        heightLabel = new javax.swing.JLabel();
        autoSaveCheckBox = new javax.swing.JCheckBox();
        selectDirectoryButton = new javax.swing.JButton();

        imagePanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        labelScaleFactor.setText(org.openide.util.NbBundle.getMessage(ScreenshotSettingsPanel.class,
            "ScreenshotSettingsPanel.labelScaleFactor.text")); // NOI18N

        transparentBackgroundCheckbox.setText(org.openide.util.NbBundle.getMessage(ScreenshotSettingsPanel.class,
            "ScreenshotSettingsPanel.transparentBackgroundCheckbox.text")); // NOI18N

        labelCustomScaleFactor.setText(org.openide.util.NbBundle.getMessage(ScreenshotSettingsPanel.class,
            "ScreenshotSettingsPanel.labelCustomScaleFactor.text")); // NOI18N

        customScaleFactorSpinner.setModel(new javax.swing.SpinnerNumberModel(1, 1, null, 1));

        labelWidth.setText(org.openide.util.NbBundle.getMessage(ScreenshotSettingsPanel.class,
            "ScreenshotSettingsPanel.labelWidth.text")); // NOI18N

        widthLabel.setText(org.openide.util.NbBundle.getMessage(ScreenshotSettingsPanel.class,
            "ScreenshotSettingsPanel.widthLabel.text")); // NOI18N

        labelHeight.setText(org.openide.util.NbBundle.getMessage(ScreenshotSettingsPanel.class,
            "ScreenshotSettingsPanel.labelHeight.text")); // NOI18N

        heightLabel.setText(org.openide.util.NbBundle.getMessage(ScreenshotSettingsPanel.class,
            "ScreenshotSettingsPanel.heightLabel.text")); // NOI18N

        javax.swing.GroupLayout imagePanelLayout = new javax.swing.GroupLayout(imagePanel);
        imagePanel.setLayout(imagePanelLayout);
        imagePanelLayout.setHorizontalGroup(
            imagePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(imagePanelLayout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(imagePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(transparentBackgroundCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, 0,
                            Short.MAX_VALUE)
                        .addGroup(imagePanelLayout.createSequentialGroup()
                            .addGroup(imagePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(imagePanelLayout.createSequentialGroup()
                                    .addComponent(labelScaleFactor)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(scaleFactorCombo, javax.swing.GroupLayout.PREFERRED_SIZE,
                                        javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(imagePanelLayout.createSequentialGroup()
                                    .addComponent(labelCustomScaleFactor)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(customScaleFactorSpinner, javax.swing.GroupLayout.PREFERRED_SIZE,
                                        javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(imagePanelLayout.createSequentialGroup()
                                    .addComponent(labelWidth)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(widthLabel)
                                    .addGap(32, 32, 32)
                                    .addComponent(labelHeight)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(heightLabel)))
                            .addGap(0, 0, Short.MAX_VALUE)))
                    .addContainerGap())
        );
        imagePanelLayout.setVerticalGroup(
            imagePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(imagePanelLayout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(imagePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(labelScaleFactor)
                        .addComponent(scaleFactorCombo, javax.swing.GroupLayout.PREFERRED_SIZE,
                            javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(imagePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(labelCustomScaleFactor)
                        .addComponent(customScaleFactorSpinner, javax.swing.GroupLayout.PREFERRED_SIZE,
                            javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(imagePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(labelWidth)
                        .addComponent(heightLabel)
                        .addComponent(widthLabel)
                        .addComponent(labelHeight))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 14, Short.MAX_VALUE)
                    .addComponent(transparentBackgroundCheckbox)
                    .addContainerGap())
        );

        autoSaveCheckBox.setText(org.openide.util.NbBundle.getMessage(ScreenshotSettingsPanel.class,
            "ScreenshotSettingsPanel.autoSaveCheckBox.text")); // NOI18N

        selectDirectoryButton.setText(org.openide.util.NbBundle.getMessage(ScreenshotSettingsPanel.class,
            "ScreenshotSettingsPanel.selectDirectoryButton.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(imagePanel, javax.swing.GroupLayout.DEFAULT_SIZE,
                            javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(autoSaveCheckBox)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(selectDirectoryButton, javax.swing.GroupLayout.PREFERRED_SIZE, 1,
                                Short.MAX_VALUE)))
                    .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(imagePanel, javax.swing.GroupLayout.PREFERRED_SIZE,
                        javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 36, Short.MAX_VALUE)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(autoSaveCheckBox)
                        .addComponent(selectDirectoryButton, javax.swing.GroupLayout.PREFERRED_SIZE, 23,
                            javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
}
