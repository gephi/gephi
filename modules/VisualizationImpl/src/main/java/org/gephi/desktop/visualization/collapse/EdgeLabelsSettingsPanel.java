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

import com.connectina.swing.fontchooser.JFontChooser;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import org.gephi.appearance.spi.TransformerCategory;
import org.gephi.desktop.appearance.AppearanceUIController;
import org.gephi.ui.appearance.plugin.UniqueLabelColorTransformerUI;
import org.gephi.ui.appearance.plugin.category.DefaultCategory;
import org.gephi.visualization.api.LabelColorMode;
import org.gephi.visualization.api.LabelSizeMode;
import org.gephi.visualization.api.VisualisationModel;
import org.gephi.visualization.api.VisualizationController;
import org.gephi.visualization.api.VisualizationPropertyChangeListener;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * @author Mathieu Bastian
 */
public class EdgeLabelsSettingsPanel extends javax.swing.JPanel implements VisualizationPropertyChangeListener {

    private final VisualizationController vizController;
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton attributesButton;
    private javax.swing.JComboBox<LabelColorMode> edgeColorCombo;
    private javax.swing.JButton edgeFontButton;
    private javax.swing.JComboBox<LabelSizeMode> edgeSizeCombo;
    private javax.swing.JSlider edgeSizeSlider;
    private javax.swing.JCheckBox hideNonSelectedCheckbox;
    private javax.swing.JLabel labelEdgeColor;
    private javax.swing.JLabel labelEdgeFont;
    private javax.swing.JLabel labelEdgeScale;
    private javax.swing.JLabel labelEdgeSize;
    private org.jdesktop.swingx.JXHyperlink selfColorLink;
    private javax.swing.JCheckBox showLabelsCheckbox;
    // End of variables declaration//GEN-END:variables

    /**
     * Creates new form NodeLabelsSettingsPanel
     */
    public EdgeLabelsSettingsPanel() {
        initComponents();

        vizController = Lookup.getDefault().lookup(VisualizationController.class);

        // Color node
        final DefaultComboBoxModel<LabelColorMode> colorModeModel = new DefaultComboBoxModel<>(LabelColorMode.values());
        edgeColorCombo.setModel(colorModeModel);
        edgeColorCombo.addActionListener(e -> {
            LabelColorMode selected = (LabelColorMode) edgeColorCombo.getSelectedItem();
            if (selected == null) {
                return;
            }
            vizController.setEdgeLabelColorMode(selected);
            selfColorLink.setVisible(selected.equals(LabelColorMode.SELF));
        });
        edgeColorCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
                                                          boolean cellHasFocus) {
                JLabel label =
                    (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof LabelColorMode) {
                    label.setText(org.openide.util.NbBundle.getMessage(EdgeLabelsSettingsPanel.class,
                        "EdgeLabelColorMode." + ((LabelColorMode) value).name().toLowerCase() + ".name"));
                    label.setIcon(ImageUtilities.loadIcon(
                        "VisualizationImpl/LabelColorMode_" + ((LabelColorMode) value).name() + ".svg"));
                } else {
                    throw new IllegalArgumentException("Expected LabelColorMode");
                }
                return this;
            }
        });

        selfColorLink.addActionListener(e -> {
            TopComponent topComponent = WindowManager.getDefault().findTopComponent("AppearanceTopComponent");
            topComponent.open();
            topComponent.requestActive();

            AppearanceUIController appearanceUIController = Lookup.getDefault().lookup(AppearanceUIController.class);
            TransformerCategory category = DefaultCategory.LABEL_COLOR;
            appearanceUIController.setSelectedElementClass("edges");
            appearanceUIController.setSelectedCategory(category);
            UniqueLabelColorTransformerUI transformerUI =
                Lookup.getDefault().lookup(UniqueLabelColorTransformerUI.class);
            appearanceUIController.setSelectedTransformerUI(transformerUI);
        });

        // Size mode
        final DefaultComboBoxModel<LabelSizeMode> sizeModeModel = new DefaultComboBoxModel<>(LabelSizeMode.values());
        edgeSizeCombo.setModel(sizeModeModel);
        edgeSizeCombo.addActionListener(
            e -> vizController.setEdgeLabelSizeMode((LabelSizeMode) edgeSizeCombo.getSelectedItem()));
        edgeSizeCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
                                                          boolean cellHasFocus) {
                JLabel label =
                    (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof LabelSizeMode) {
                    label.setText(org.openide.util.NbBundle.getMessage(EdgeLabelsSettingsPanel.class,
                        "LabelSizeMode." + ((LabelSizeMode) value).name().toLowerCase() + ".name"));
                    label.setIcon(ImageUtilities.loadIcon(
                        "VisualizationImpl/LabelSizeMode_" + ((LabelSizeMode) value).name() + ".svg"));
                } else {
                    throw new IllegalArgumentException("Expected NodeLabelSizeMode");
                }
                return this;
            }
        });

        // Show
        showLabelsCheckbox.addItemListener(e -> {
            vizController.setShowEdgeLabels(showLabelsCheckbox.isSelected());
            setEnable(true);
        });

        // Font
        edgeFontButton.addActionListener(e -> {
            VisualisationModel model = vizController.getModel();
            Font font = JFontChooser.showDialog(WindowManager.getDefault().getMainWindow(), model.getEdgeLabelFont());
            if (font != null && font != model.getEdgeLabelFont()) {
                vizController.setEdgeLabelFont(font);
            }
        });
        edgeSizeSlider.addChangeListener(e -> vizController.setEdgeLabelScale(edgeSizeSlider.getValue() / 100f));

        // Attributes
        attributesButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                VisualisationModel model = vizController.getModel();
                LabelAttributesPanel panel = new LabelAttributesPanel(model, true);
                panel.setup();
                DialogDescriptor dd = new DialogDescriptor(panel,
                    NbBundle.getMessage(EdgeLabelsSettingsPanel.class, "LabelAttributesPanel.title"), true,
                    NotifyDescriptor.OK_CANCEL_OPTION, null, null);
                if (DialogDisplayer.getDefault().notify(dd).equals(NotifyDescriptor.OK_OPTION)) {
                    panel.unsetup();
                }
            }
        });
        attributesButton.setIcon(ImageUtilities.loadImageIcon("VisualizationImpl/configureLabels.svg", false));

        // Hide non selected
        hideNonSelectedCheckbox.addActionListener(
            e -> vizController.setHideNonSelectedEdgeLabels(hideNonSelectedCheckbox.isSelected()));
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
        if (evt.getPropertyName().equals("showEdgeLabels")) {
            refreshSharedConfig(model);
        } else if (evt.getPropertyName().equals("edgeLabelFont")) {
            refreshSharedConfig(model);
        } else if (evt.getPropertyName().equals("edgeLabelScale")) {
            refreshSharedConfig(model);
        } else if (evt.getPropertyName().equals("hideNonSelectedEdgeLabels")) {
            refreshSharedConfig(model);
        } else if (evt.getPropertyName().equals("edgeLabelSizeMode")) {
            refreshSharedConfig(model);
        } else if (evt.getPropertyName().equals("edgeLabelColorMode")) {
            refreshSharedConfig(model);
        }
    }

    private void refreshSharedConfig(VisualisationModel vizModel) {
        if (showLabelsCheckbox.isSelected() != vizModel.isShowEdgeLabels()) {
            showLabelsCheckbox.setSelected(vizModel.isShowEdgeLabels());
        }
        if (edgeColorCombo.getSelectedItem() != vizModel.getEdgeLabelColorMode()) {
            edgeColorCombo.setSelectedItem(vizModel.getEdgeLabelColorMode());
        }
        if (edgeSizeCombo.getSelectedItem() != vizModel.getEdgeLabelSizeMode()) {
            edgeSizeCombo.setSelectedItem(vizModel.getEdgeLabelSizeMode());
        }
        edgeFontButton.setText(
            vizModel.getEdgeLabelFont().getFontName() + ", " + vizModel.getEdgeLabelFont().getSize());
        if (edgeSizeSlider.getValue() / 100f != vizModel.getEdgeLabelScale()) {
            edgeSizeSlider.setValue((int) (vizModel.getEdgeLabelScale() * 100f));
        }
        if (hideNonSelectedCheckbox.isSelected() != vizModel.isHideNonSelectedEdgeLabels()) {
            hideNonSelectedCheckbox.setSelected(vizModel.isHideNonSelectedEdgeLabels());
        }
        selfColorLink.setVisible(edgeColorCombo.getSelectedItem() == LabelColorMode.SELF);
    }

    private void setEnable(boolean enable) {
        showLabelsCheckbox.setEnabled(enable);
        edgeColorCombo.setEnabled(enable && showLabelsCheckbox.isSelected());
        labelEdgeColor.setEnabled(enable && showLabelsCheckbox.isSelected());
        edgeFontButton.setEnabled(enable && showLabelsCheckbox.isSelected());
        labelEdgeFont.setEnabled(enable && showLabelsCheckbox.isSelected());
        edgeSizeSlider.setEnabled(enable && showLabelsCheckbox.isSelected());
        labelEdgeScale.setEnabled(enable && showLabelsCheckbox.isSelected());
        edgeSizeCombo.setEnabled(enable && showLabelsCheckbox.isSelected());
        labelEdgeSize.setEnabled(enable && showLabelsCheckbox.isSelected());
        hideNonSelectedCheckbox.setEnabled(enable && showLabelsCheckbox.isSelected());
        attributesButton.setEnabled(enable && showLabelsCheckbox.isSelected());
        selfColorLink.setEnabled(enable && showLabelsCheckbox.isSelected());
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        showLabelsCheckbox = new javax.swing.JCheckBox();
        labelEdgeColor = new javax.swing.JLabel();
        edgeColorCombo = new javax.swing.JComboBox<>();
        labelEdgeFont = new javax.swing.JLabel();
        edgeFontButton = new javax.swing.JButton();
        labelEdgeScale = new javax.swing.JLabel();
        edgeSizeSlider = new javax.swing.JSlider();
        labelEdgeSize = new javax.swing.JLabel();
        edgeSizeCombo = new javax.swing.JComboBox<>();
        hideNonSelectedCheckbox = new javax.swing.JCheckBox();
        attributesButton = new javax.swing.JButton();
        selfColorLink = new org.jdesktop.swingx.JXHyperlink();

        showLabelsCheckbox.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        showLabelsCheckbox.setText(org.openide.util.NbBundle.getMessage(EdgeLabelsSettingsPanel.class,
            "EdgeLabelsSettingsPanel.showLabelsCheckbox.text")); // NOI18N

        labelEdgeColor.setText(org.openide.util.NbBundle.getMessage(EdgeLabelsSettingsPanel.class,
            "EdgeLabelsSettingsPanel.labelEdgeColor.text")); // NOI18N

        labelEdgeFont.setText(org.openide.util.NbBundle.getMessage(EdgeLabelsSettingsPanel.class,
            "EdgeLabelsSettingsPanel.labelEdgeFont.text")); // NOI18N
        labelEdgeFont.setMaximumSize(new java.awt.Dimension(60, 15));

        edgeFontButton.setText(org.openide.util.NbBundle.getMessage(EdgeLabelsSettingsPanel.class,
            "EdgeLabelsSettingsPanel.edgeFontButton.text")); // NOI18N

        labelEdgeScale.setText(org.openide.util.NbBundle.getMessage(EdgeLabelsSettingsPanel.class,
            "EdgeLabelsSettingsPanel.labelEdgeScale.text")); // NOI18N

        edgeSizeSlider.setMinimum(1);

        labelEdgeSize.setText(org.openide.util.NbBundle.getMessage(EdgeLabelsSettingsPanel.class,
            "EdgeLabelsSettingsPanel.labelEdgeSize.text")); // NOI18N

        hideNonSelectedCheckbox.setText(org.openide.util.NbBundle.getMessage(EdgeLabelsSettingsPanel.class,
            "EdgeLabelsSettingsPanel.hideNonSelectedCheckbox.text")); // NOI18N
        hideNonSelectedCheckbox.setBorder(null);
        hideNonSelectedCheckbox.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        hideNonSelectedCheckbox.setMargin(new java.awt.Insets(2, 0, 2, 2));

        attributesButton.setText(org.openide.util.NbBundle.getMessage(EdgeLabelsSettingsPanel.class,
            "EdgeLabelsSettingsPanel.attributesButton.text")); // NOI18N

        selfColorLink.setText(org.openide.util.NbBundle.getMessage(EdgeLabelsSettingsPanel.class,
            "EdgeLabelsSettingsPanel.selfColorLink.text")); // NOI18N

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
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(edgeColorCombo, javax.swing.GroupLayout.PREFERRED_SIZE, 145,
                                        javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(layout.createSequentialGroup()
                                    .addComponent(labelEdgeSize)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                    .addComponent(edgeSizeCombo, javax.swing.GroupLayout.PREFERRED_SIZE, 145,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(selfColorLink, javax.swing.GroupLayout.PREFERRED_SIZE, 31,
                                javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(24, 24, 24)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addGroup(layout.createSequentialGroup()
                                    .addComponent(labelEdgeFont, javax.swing.GroupLayout.PREFERRED_SIZE,
                                        javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(edgeFontButton, javax.swing.GroupLayout.DEFAULT_SIZE,
                                        javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addComponent(hideNonSelectedCheckbox))
                            .addGap(18, 18, 18)
                            .addComponent(labelEdgeScale)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(edgeSizeSlider, javax.swing.GroupLayout.PREFERRED_SIZE,
                                javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(attributesButton, javax.swing.GroupLayout.PREFERRED_SIZE, 125,
                                javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(layout.createSequentialGroup()
                            .addContainerGap()
                            .addComponent(showLabelsCheckbox)))
                    .addContainerGap(99, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(showLabelsCheckbox)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(labelEdgeColor)
                        .addComponent(edgeColorCombo, javax.swing.GroupLayout.PREFERRED_SIZE,
                            javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(labelEdgeFont, javax.swing.GroupLayout.PREFERRED_SIZE,
                            javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(edgeFontButton)
                        .addComponent(labelEdgeScale)
                        .addComponent(edgeSizeSlider, javax.swing.GroupLayout.PREFERRED_SIZE,
                            javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(selfColorLink, javax.swing.GroupLayout.PREFERRED_SIZE,
                            javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(attributesButton))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(labelEdgeSize)
                        .addComponent(edgeSizeCombo, javax.swing.GroupLayout.PREFERRED_SIZE,
                            javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(hideNonSelectedCheckbox))
                    .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
}


