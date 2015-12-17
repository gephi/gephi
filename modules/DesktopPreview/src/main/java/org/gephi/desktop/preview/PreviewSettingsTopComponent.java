/*
 Copyright 2008-2010 Gephi
 Authors : Jérémy Subtil <jeremy.subtil@gephi.org>, Mathieu Bastian
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
package org.gephi.desktop.preview;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.NumberFormat;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.gephi.desktop.io.export.api.VectorialFileExporterUI;
import org.gephi.desktop.preview.api.PreviewUIController;
import org.gephi.desktop.preview.api.PreviewUIModel;
import org.gephi.preview.api.PreviewController;
import org.gephi.preview.api.PreviewModel;
import org.gephi.preview.api.PreviewPreset;
import org.gephi.preview.spi.PreviewUI;
import org.gephi.ui.utils.UIUtils;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.StatusDisplayer;
import org.openide.explorer.propertysheet.PropertySheet;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

/**
 *
 * @author Jérémy Subtil, Mathieu Bastian
 */
@ConvertAsProperties(dtd = "-//org.gephi.desktop.preview//PreviewSettings//EN",
        autostore = false)
@TopComponent.Description(preferredID = "PreviewSettingsTopComponent",
        iconBase = "org/gephi/desktop/preview/resources/settings.png",
        persistenceType = TopComponent.PERSISTENCE_ALWAYS)
@TopComponent.Registration(mode = "layoutmode", openAtStartup = true, roles = {"preview"})
@ActionID(category = "Window", id = "org.gephi.desktop.preview.PreviewSettingsTopComponent")
@ActionReference(path = "Menu/Window", position = 1000)
@TopComponent.OpenActionRegistration(displayName = "#CTL_PreviewSettingsTopComponent",
        preferredID = "PreviewSettingsTopComponent")
public final class PreviewSettingsTopComponent extends TopComponent implements PropertyChangeListener {

    private final String NO_SELECTION = "---";
    //Component
    private transient PropertySheet propertySheet;
    private transient RendererManager rendererManager;
    private transient JTabbedPane tabbedPane;
    //State
    private int defaultPresetLimit;

    public PreviewSettingsTopComponent() {
        initComponents();
        setName(NbBundle.getMessage(PreviewSettingsTopComponent.class, "CTL_PreviewSettingsTopComponent"));

        if (UIUtils.isAquaLookAndFeel()) {
            mainPanel.setBackground(UIManager.getColor("NbExplorerView.background"));
        }

        // property sheet
        propertySheet = new PropertySheet();
        propertySheet.setNodes(new Node[]{new PreviewNode(propertySheet)});
        propertySheet.setDescriptionAreaVisible(false);

        rendererManager = new RendererManager();
        //Tabs for property sheet, manager and preview UI
        tabbedPane = new JTabbedPane();
        tabbedPane.addTab(NbBundle.getMessage(PreviewSettingsTopComponent.class, "PreviewSettingsTopComponent.propertySheetTab"), propertySheet);
        tabbedPane.addTab(NbBundle.getMessage(PreviewSettingsTopComponent.class, "PreviewSettingsTopComponent.rendererManagerTab"), rendererManager);
        propertiesPanel.add(tabbedPane, BorderLayout.CENTER);

        tabbedPane.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                if (tabbedPane.getSelectedComponent() == propertySheet) {
                    propertySheet.setNodes(new Node[]{new PreviewNode(propertySheet)});
                }
            }
        });

        //Ratio
        ratioSlider.addChangeListener(new ChangeListener() {

            NumberFormat formatter = NumberFormat.getPercentInstance();

            @Override
            public void stateChanged(ChangeEvent e) {
                float val = ratioSlider.getValue() / 100f;
                if (val == 0f) {
                    ratioLabel.setText(NbBundle.getMessage(PreviewSettingsTopComponent.class, "PreviewSettingsTopComponent.ratio.minimum"));
                } else {
                    ratioLabel.setText(formatter.format(val));
                }
                PreviewUIController puic = Lookup.getDefault().lookup(PreviewUIController.class);
                puic.setVisibilityRatio(getVisibilityRatio());
            }
        });

        //Presets
        presetComboBox.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                PreviewUIController pc = Lookup.getDefault().lookup(PreviewUIController.class);
                PreviewUIModel previewModel = pc.getModel();
                if (previewModel != null && presetComboBox.getSelectedItem() instanceof PreviewPreset) {
                    if (previewModel.getCurrentPreset() != presetComboBox.getSelectedItem()) {
                        pc.setCurrentPreset((PreviewPreset) presetComboBox.getSelectedItem());
                        propertySheet.setNodes(new Node[]{new PreviewNode(propertySheet)});
                    }
                }
            }
        });

        //Export
        svgExportButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                VectorialFileExporterUI ui = Lookup.getDefault().lookup(VectorialFileExporterUI.class);
                ui.action();
            }
        });
        setup(null);

        PreviewUIController controller = Lookup.getDefault().lookup(PreviewUIController.class);
        controller.addPropertyChangeListener(this);

        PreviewUIModel m = controller.getModel();
        if (m != null) {
            setup(m);
            enableRefreshButton();
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(PreviewUIController.SELECT)) {
            PreviewUIModel model = (PreviewUIModel) evt.getNewValue();
            setup(model);
            if (model != null) {
                enableRefreshButton();
            } else {
                disableRefreshButton();
            }
        } else if (evt.getPropertyName().equals(PreviewUIController.REFRESHED)) {
        } else if (evt.getPropertyName().equals(PreviewUIController.REFRESHING)) {
            boolean refrehsing = (Boolean) evt.getNewValue();
            if (refrehsing) {
                disableRefreshButton();
            } else {
                enableRefreshButton();
            }
        }
    }

    public void setup(PreviewUIModel previewModel) {
        propertySheet.setNodes(new Node[]{new PreviewNode(propertySheet)});
        PreviewUIController previewUIController = Lookup.getDefault().lookup(PreviewUIController.class);
        if (previewModel != null) {
            ratioSlider.setValue((int) (previewModel.getVisibilityRatio() * 100));
        }

        //Presets
        if (previewModel == null) {
            saveButton.setEnabled(false);
            labelPreset.setEnabled(false);
            presetComboBox.setEnabled(false);
            presetComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[]{"---"}));
        } else {
            saveButton.setEnabled(true);
            labelPreset.setEnabled(true);
            presetComboBox.setEnabled(true);
            DefaultComboBoxModel comboBoxModel = new DefaultComboBoxModel();
            defaultPresetLimit = 0;
            for (PreviewPreset preset : previewUIController.getDefaultPresets()) {
                comboBoxModel.addElement(preset);
                defaultPresetLimit++;
            }
            PreviewPreset[] userPresets = previewUIController.getUserPresets();
            if (userPresets.length > 0) {
                comboBoxModel.addElement(NO_SELECTION);
                for (PreviewPreset preset : userPresets) {
                    comboBoxModel.addElement(preset);
                }
            }
            presetComboBox.setSelectedItem(previewModel.getCurrentPreset());
            presetComboBox.setModel(comboBoxModel);
        }

        //Refresh tabs
        int tabCount = tabbedPane.getTabCount();
        for (int i = 2; i < tabCount; i++) {//Start at 2, not removing settings and renderer manager tabs
            tabbedPane.removeTabAt(i);
        }
        for (PreviewUI pui : Lookup.getDefault().lookupAll(PreviewUI.class)) {
            pui.unsetup();
        }
        if (previewModel != null) {
            PreviewController previewController = Lookup.getDefault().lookup(PreviewController.class);
            PreviewModel pModel = previewController.getModel();
            //Add new tabs
            for (PreviewUI pui : Lookup.getDefault().lookupAll(PreviewUI.class)) {
                pui.setup(pModel);
                JPanel pluginPanel = pui.getPanel();
                if (UIUtils.isAquaLookAndFeel()) {
                    pluginPanel.setBackground(UIManager.getColor("NbExplorerView.background"));
                }
                if (pui.getIcon() != null) {
                    tabbedPane.addTab(pui.getPanelTitle(), pui.getIcon(), pluginPanel);
                } else {
                    tabbedPane.addTab(pui.getPanelTitle(), pluginPanel);
                }
            }
        }
    }

    public void unsetup() {
    }

    /**
     * Returns the graph visibility ratio set in the visibilityRatioSpinner component.
     *
     * @return the graph visibility ratio
     */
    public float getVisibilityRatio() {
        float value = (Integer) ratioSlider.getValue();

        if (value < 0) {
            value = 0;
        } else if (value > 100) {
            value = 100;
        }

        return value / 100;
    }

    /**
     * Enables the refresh button.
     *
     * @see PreviewUIController#enableRefresh()
     */
    public void enableRefreshButton() {
        refreshButton.setEnabled(true);
        labelRatio.setEnabled(true);
        ratioLabel.setEnabled(true);
        ratioSlider.setEnabled(true);
        labelExport.setEnabled(true);
        svgExportButton.setEnabled(true);
    }

    /**
     * Disables the refresh button.
     *
     * @see PreviewUIController#disableRefresh()
     */
    public void disableRefreshButton() {
        refreshButton.setEnabled(false);
        labelRatio.setEnabled(false);
        ratioLabel.setEnabled(false);
        ratioSlider.setEnabled(false);
        labelExport.setEnabled(false);
        svgExportButton.setEnabled(false);
    }

    private boolean isDefaultPreset(PreviewPreset preset) {
        int i;
        for (i = 0; i < presetComboBox.getItemCount(); i++) {
            if (presetComboBox.getModel().getElementAt(i).equals(preset)) {
                break;
            }
        }
        return i < defaultPresetLimit;
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        mainPanel = new javax.swing.JPanel();
        presetPanel = new javax.swing.JPanel();
        presetComboBox = new javax.swing.JComboBox();
        presetToolbar = new javax.swing.JToolBar();
        box = new javax.swing.JLabel();
        saveButton = new javax.swing.JButton();
        labelPreset = new javax.swing.JLabel();
        refreshButton = new javax.swing.JButton();
        propertiesPanel = new javax.swing.JPanel();
        labelRatio = new javax.swing.JLabel();
        ratioLabel = new javax.swing.JLabel();
        ratioSlider = new javax.swing.JSlider();
        southToolbar = new javax.swing.JToolBar();
        labelExport = new javax.swing.JLabel();
        svgExportButton = new javax.swing.JButton();

        setLayout(new java.awt.BorderLayout());

        mainPanel.setLayout(new java.awt.GridBagLayout());

        presetPanel.setOpaque(false);
        presetPanel.setLayout(new java.awt.GridBagLayout());

        presetComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "---" }));
        presetComboBox.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipady = 5;
        gridBagConstraints.weightx = 2.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 10, 5);
        presetPanel.add(presetComboBox, gridBagConstraints);

        presetToolbar.setBorder(null);
        presetToolbar.setFloatable(false);
        presetToolbar.setRollover(true);
        presetToolbar.setOpaque(false);

        org.openide.awt.Mnemonics.setLocalizedText(box, org.openide.util.NbBundle.getMessage(PreviewSettingsTopComponent.class, "PreviewSettingsTopComponent.box.text")); // NOI18N
        box.setMaximumSize(new java.awt.Dimension(32767, 32767));
        presetToolbar.add(box);

        saveButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/gephi/desktop/preview/resources/save.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(saveButton, org.openide.util.NbBundle.getMessage(PreviewSettingsTopComponent.class, "PreviewSettingsTopComponent.saveButton.text")); // NOI18N
        saveButton.setToolTipText(org.openide.util.NbBundle.getMessage(PreviewSettingsTopComponent.class, "PreviewSettingsTopComponent.saveButton.toolTipText")); // NOI18N
        saveButton.setEnabled(false);
        saveButton.setFocusable(false);
        saveButton.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        saveButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveButtonActionPerformed(evt);
            }
        });
        presetToolbar.add(saveButton);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(3, 5, 0, 5);
        presetPanel.add(presetToolbar, gridBagConstraints);

        labelPreset.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/gephi/desktop/preview/resources/preset.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(labelPreset, org.openide.util.NbBundle.getMessage(PreviewSettingsTopComponent.class, "PreviewSettingsTopComponent.labelPreset.text")); // NOI18N
        labelPreset.setEnabled(false);
        labelPreset.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(3, 5, 0, 0);
        presetPanel.add(labelPreset, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        mainPanel.add(presetPanel, gridBagConstraints);

        refreshButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/gephi/desktop/preview/resources/refresh.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(refreshButton, org.openide.util.NbBundle.getMessage(PreviewSettingsTopComponent.class, "PreviewSettingsTopComponent.refreshButton.text")); // NOI18N
        refreshButton.setEnabled(false);
        refreshButton.setMargin(new java.awt.Insets(10, 14, 10, 14));
        refreshButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridheight = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(7, 5, 0, 10);
        mainPanel.add(refreshButton, gridBagConstraints);

        propertiesPanel.setOpaque(false);
        propertiesPanel.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        mainPanel.add(propertiesPanel, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(labelRatio, org.openide.util.NbBundle.getMessage(PreviewSettingsTopComponent.class, "PreviewSettingsTopComponent.labelRatio.text")); // NOI18N
        labelRatio.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 7, 3, 5);
        mainPanel.add(labelRatio, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(ratioLabel, org.openide.util.NbBundle.getMessage(PreviewSettingsTopComponent.class, "PreviewSettingsTopComponent.ratioLabel.text")); // NOI18N
        ratioLabel.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 3, 0);
        mainPanel.add(ratioLabel, gridBagConstraints);

        ratioSlider.setEnabled(false);
        ratioSlider.setPreferredSize(new java.awt.Dimension(120, 23));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 3, 5, 20);
        mainPanel.add(ratioSlider, gridBagConstraints);

        southToolbar.setFloatable(false);
        southToolbar.setRollover(true);
        southToolbar.setOpaque(false);

        labelExport.setFont(new java.awt.Font("Tahoma", 0, 10));
        org.openide.awt.Mnemonics.setLocalizedText(labelExport, org.openide.util.NbBundle.getMessage(PreviewSettingsTopComponent.class, "PreviewSettingsTopComponent.labelExport.text")); // NOI18N
        labelExport.setEnabled(false);
        southToolbar.add(labelExport);

        org.openide.awt.Mnemonics.setLocalizedText(svgExportButton, org.openide.util.NbBundle.getMessage(PreviewSettingsTopComponent.class, "PreviewSettingsTopComponent.svgExportButton.text")); // NOI18N
        svgExportButton.setToolTipText(org.openide.util.NbBundle.getMessage(PreviewSettingsTopComponent.class, "PreviewSettingsTopComponent.svgExportButton.toolTipText")); // NOI18N
        svgExportButton.setEnabled(false);
        svgExportButton.setFocusable(false);
        svgExportButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        svgExportButton.setMargin(new java.awt.Insets(2, 8, 2, 8));
        svgExportButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        southToolbar.add(svgExportButton);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
        mainPanel.add(southToolbar, gridBagConstraints);

        add(mainPanel, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void refreshButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshButtonActionPerformed
        Lookup.getDefault().lookup(PreviewUIController.class).refreshPreview();
}//GEN-LAST:event_refreshButtonActionPerformed

    private void saveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveButtonActionPerformed
        PreviewUIController previewController = Lookup.getDefault().lookup(PreviewUIController.class);
        PreviewPreset preset = previewController.getModel().getCurrentPreset();
        boolean saved = false;
        if (isDefaultPreset(preset)) {
            NotifyDescriptor.InputLine question = new NotifyDescriptor.InputLine(
                    NbBundle.getMessage(PreviewSettingsTopComponent.class, "PreviewSettingsTopComponent.savePreset.input"),
                    NbBundle.getMessage(PreviewSettingsTopComponent.class, "PreviewSettingsTopComponent.savePreset.input.title"));
            if (DialogDisplayer.getDefault().notify(question) == NotifyDescriptor.OK_OPTION) {
                String input = question.getInputText();
                if (input != null && !input.isEmpty()) {
                    previewController.savePreset(input);
                    saved = true;
                    StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(PreviewSettingsTopComponent.class, "PreviewSettingsTopComponent.savePreset.status", input));
                }
            }
        } else {
            previewController.savePreset(preset.getName());
            saved = true;
            StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(PreviewSettingsTopComponent.class, "PreviewSettingsTopComponent.savePreset.status", preset.getName()));
        }

        if (saved) {
            //refresh combo
            DefaultComboBoxModel comboBoxModel = new DefaultComboBoxModel();
            defaultPresetLimit = 0;
            for (PreviewPreset p : previewController.getDefaultPresets()) {
                comboBoxModel.addElement(p);
                defaultPresetLimit++;
            }
            PreviewPreset[] userPresets = previewController.getUserPresets();
            if (userPresets.length > 0) {
                comboBoxModel.addElement(NO_SELECTION);
                for (PreviewPreset p : userPresets) {
                    comboBoxModel.addElement(p);
                }
            }
            comboBoxModel.setSelectedItem(previewController.getModel().getCurrentPreset());
            presetComboBox.setModel(comboBoxModel);
        }
    }//GEN-LAST:event_saveButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel box;
    private javax.swing.JLabel labelExport;
    private javax.swing.JLabel labelPreset;
    private javax.swing.JLabel labelRatio;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JComboBox presetComboBox;
    private javax.swing.JPanel presetPanel;
    private javax.swing.JToolBar presetToolbar;
    private javax.swing.JPanel propertiesPanel;
    private javax.swing.JLabel ratioLabel;
    private javax.swing.JSlider ratioSlider;
    private javax.swing.JButton refreshButton;
    private javax.swing.JButton saveButton;
    private javax.swing.JToolBar southToolbar;
    private javax.swing.JButton svgExportButton;
    // End of variables declaration//GEN-END:variables

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // TODO store your settings
    }

    void readProperties(java.util.Properties p) {
        String version = p.getProperty("version");
        // TODO read your settings according to their version
    }
}
