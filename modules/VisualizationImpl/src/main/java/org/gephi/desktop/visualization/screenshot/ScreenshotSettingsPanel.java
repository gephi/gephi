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
import javax.swing.JFileChooser;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.gephi.lib.validation.Multiple4NumberValidator;
import org.gephi.visualization.api.ScreenshotModel;
import org.gephi.visualization.api.ScreenshotController;
import org.netbeans.validation.api.builtin.stringvalidation.StringValidators;
import org.netbeans.validation.api.ui.ValidationGroup;
import org.netbeans.validation.api.ui.swing.ValidationPanel;
import org.openide.util.Lookup;
import org.openide.windows.WindowManager;

/**
 * @author Mathieu Bastian
 */
public class ScreenshotSettingsPanel extends javax.swing.JPanel {

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox antiAliasingCombo;
    private javax.swing.JCheckBox autoSaveCheckBox;
    private javax.swing.JTextField heightTextField;
    private javax.swing.JPanel imagePanel;
    private javax.swing.JLabel labelAntiAliasing;
    private javax.swing.JLabel labelHeight;
    private javax.swing.JLabel labelWidth;
    private javax.swing.JButton selectDirectoryButton;
    private javax.swing.JCheckBox transparentBackgroundCheckbox;
    private javax.swing.JTextField widthTextField;
    // End of variables declaration//GEN-END:variables

    // Controller
    private final ScreenshotController controller;

    /**
     * Creates new form ScreenshotSettingsPanel
     */
    public ScreenshotSettingsPanel() {
        controller = Lookup.getDefault().lookup(ScreenshotController.class);
        initComponents();

        autoSaveCheckBox.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                selectDirectoryButton.setEnabled(autoSaveCheckBox.isSelected());
            }
        });
    }

    public static ValidationPanel createValidationPanel(ScreenshotSettingsPanel innerPanel) {
        ValidationPanel validationPanel = new ValidationPanel();
        if (innerPanel == null) {
            innerPanel = new ScreenshotSettingsPanel();
        }
        validationPanel.setInnerComponent(innerPanel);

        ValidationGroup group = validationPanel.getValidationGroup();

        //Node field
        group.add(innerPanel.widthTextField, StringValidators.REQUIRE_NON_EMPTY_STRING,
            new Multiple4NumberValidator());

        //Edge field
        group.add(innerPanel.heightTextField, StringValidators.REQUIRE_NON_EMPTY_STRING,
            new Multiple4NumberValidator());

        return validationPanel;
    }

    public void setup(final ScreenshotModel model) {
        autoSaveCheckBox.setSelected(model.isAutoSave());
        selectDirectoryButton.setEnabled(autoSaveCheckBox.isSelected());
        widthTextField.setText(String.valueOf(model.getWidth()));
        heightTextField.setText(String.valueOf(model.getHeight()));
        switch (model.getAntiAliasing()) {
            case 0:
                antiAliasingCombo.setSelectedIndex(0);
                break;
            case 2:
                antiAliasingCombo.setSelectedIndex(1);
                break;
            case 4:
                antiAliasingCombo.setSelectedIndex(2);
                break;
            case 8:
                antiAliasingCombo.setSelectedIndex(3);
                break;
            default:
                antiAliasingCombo.setSelectedIndex(4);
                break;
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
    }

    public void unsetup() {
        controller.setAutoSave(autoSaveCheckBox.isSelected());
        controller.setWidth(Integer.parseInt(widthTextField.getText()));
        controller.setHeight(Integer.parseInt(heightTextField.getText()));
        switch (antiAliasingCombo.getSelectedIndex()) {
            case 0:
                controller.setAntiAliasing(0);
                break;
            case 1:
                controller.setAntiAliasing(2);
                break;
            case 2:
                controller.setAntiAliasing(4);
                break;
            case 3:
                controller.setAntiAliasing(8);
                break;
            case 4:
                controller.setAntiAliasing(16);
                break;
            default:
                controller.setAntiAliasing(0);
                break;
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
        labelHeight = new javax.swing.JLabel();
        labelWidth = new javax.swing.JLabel();
        widthTextField = new javax.swing.JTextField();
        labelAntiAliasing = new javax.swing.JLabel();
        antiAliasingCombo = new javax.swing.JComboBox();
        heightTextField = new javax.swing.JTextField();
        transparentBackgroundCheckbox = new javax.swing.JCheckBox();
        autoSaveCheckBox = new javax.swing.JCheckBox();
        selectDirectoryButton = new javax.swing.JButton();

        imagePanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        labelHeight.setText(org.openide.util.NbBundle.getMessage(ScreenshotSettingsPanel.class, "ScreenshotSettingsPanel.labelHeight.text")); // NOI18N

        labelWidth.setText(org.openide.util.NbBundle.getMessage(ScreenshotSettingsPanel.class, "ScreenshotSettingsPanel.labelWidth.text")); // NOI18N

        widthTextField.setText(org.openide.util.NbBundle.getMessage(ScreenshotSettingsPanel.class, "ScreenshotSettingsPanel.widthTextField.text")); // NOI18N

        labelAntiAliasing.setText(org.openide.util.NbBundle.getMessage(ScreenshotSettingsPanel.class, "ScreenshotSettingsPanel.labelAntiAliasing.text")); // NOI18N

        antiAliasingCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "0x", "2x", "4x", "8x", "16x" }));

        heightTextField.setText(org.openide.util.NbBundle.getMessage(ScreenshotSettingsPanel.class, "ScreenshotSettingsPanel.heightTextField.text")); // NOI18N

        transparentBackgroundCheckbox.setText(org.openide.util.NbBundle.getMessage(ScreenshotSettingsPanel.class, "ScreenshotSettingsPanel.transparentBackgroundCheckbox.text")); // NOI18N

        javax.swing.GroupLayout imagePanelLayout = new javax.swing.GroupLayout(imagePanel);
        imagePanel.setLayout(imagePanelLayout);
        imagePanelLayout.setHorizontalGroup(
            imagePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(imagePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(imagePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(imagePanelLayout.createSequentialGroup()
                        .addComponent(labelAntiAliasing)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(antiAliasingCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(transparentBackgroundCheckbox)
                    .addGroup(imagePanelLayout.createSequentialGroup()
                        .addComponent(labelWidth)
                        .addGap(3, 3, 3)
                        .addComponent(widthTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 58, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(labelHeight)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(heightTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 58, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        imagePanelLayout.setVerticalGroup(
            imagePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(imagePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(imagePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelWidth)
                    .addComponent(widthTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(labelHeight)
                    .addComponent(heightTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(imagePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelAntiAliasing)
                    .addComponent(antiAliasingCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(transparentBackgroundCheckbox)
                .addContainerGap(18, Short.MAX_VALUE))
        );

        autoSaveCheckBox.setText(org.openide.util.NbBundle.getMessage(ScreenshotSettingsPanel.class, "ScreenshotSettingsPanel.autoSaveCheckBox.text")); // NOI18N

        selectDirectoryButton.setText(org.openide.util.NbBundle.getMessage(ScreenshotSettingsPanel.class, "ScreenshotSettingsPanel.selectDirectoryButton.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(imagePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(autoSaveCheckBox)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(selectDirectoryButton)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(imagePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(autoSaveCheckBox)
                    .addComponent(selectDirectoryButton, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(23, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
}
