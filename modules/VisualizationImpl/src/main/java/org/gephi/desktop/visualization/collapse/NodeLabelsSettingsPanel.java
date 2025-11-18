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
public class NodeLabelsSettingsPanel extends javax.swing.JPanel implements VisualizationPropertyChangeListener {

    private final VisualizationController vizController;
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton attributesButton;
    private javax.swing.JCheckBox avoidOverlap;
    private javax.swing.JToggleButton fitToNodeSizeToggleButton;
    private javax.swing.JCheckBox hideNonSelectedCheckbox;
    private javax.swing.JLabel labelNodeColor;
    private javax.swing.JLabel labelNodeFont;
    private javax.swing.JLabel labelNodeScale;
    private javax.swing.JLabel labelNodeSize;
    private javax.swing.JComboBox<LabelColorMode> nodeColorCombo;
    private javax.swing.JButton nodeFontButton;
    private javax.swing.JComboBox<LabelSizeMode> nodeSizeCombo;
    private javax.swing.JSlider nodeSizeSlider;
    private org.jdesktop.swingx.JXHyperlink selfColorLink;
    private javax.swing.JCheckBox showLabelsCheckbox;
    // End of variables declaration//GEN-END:variables

    /**
     * Creates new form NodeLabelsSettingsPanel
     */
    public NodeLabelsSettingsPanel() {
        initComponents();

        vizController = Lookup.getDefault().lookup(VisualizationController.class);

        // Color node
        final DefaultComboBoxModel<LabelColorMode> colorModeModel = new DefaultComboBoxModel<>(LabelColorMode.values());
        nodeColorCombo.setModel(colorModeModel);
        nodeColorCombo.addActionListener(e -> {
            LabelColorMode selected = (LabelColorMode) nodeColorCombo.getSelectedItem();
            if (selected == null) {
                return;
            }
            vizController.setNodeLabelColorMode(selected);
            selfColorLink.setVisible(selected.equals(LabelColorMode.SELF));
        });
        nodeColorCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
                                                          boolean cellHasFocus) {
                JLabel label =
                    (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof LabelColorMode) {
                    label.setText(org.openide.util.NbBundle.getMessage(NodeLabelsSettingsPanel.class,
                        "NodeLabelColorMode." + ((LabelColorMode) value).name().toLowerCase() + ".name"));
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
            appearanceUIController.setSelectedElementClass("nodes");
            appearanceUIController.setSelectedCategory(category);
            UniqueLabelColorTransformerUI transformerUI =
                Lookup.getDefault().lookup(UniqueLabelColorTransformerUI.class);
            appearanceUIController.setSelectedTransformerUI(transformerUI);
        });

        // Size node
        final DefaultComboBoxModel<LabelSizeMode> sizeModeModel = new DefaultComboBoxModel<>(LabelSizeMode.values());
        nodeSizeCombo.setModel(sizeModeModel);
        nodeSizeCombo.addActionListener(
            e -> vizController.setNodeLabelSizeMode((LabelSizeMode) nodeSizeCombo.getSelectedItem()));
        nodeSizeCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
                                                          boolean cellHasFocus) {
                JLabel label =
                    (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof LabelSizeMode) {
                    label.setText(org.openide.util.NbBundle.getMessage(NodeLabelsSettingsPanel.class,
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
            vizController.setShowNodeLabels(showLabelsCheckbox.isSelected());
            setEnable(true);
        });

        // Font
        nodeFontButton.addActionListener(e -> {
            VisualisationModel model = vizController.getModel();
            Font font = JFontChooser.showDialog(WindowManager.getDefault().getMainWindow(), model.getNodeLabelFont());
            if (font != null && font != model.getNodeLabelFont()) {
                vizController.setNodeLabelFont(font);
            }
        });
        nodeSizeSlider.addChangeListener(e -> vizController.setNodeLabelScale(nodeSizeSlider.getValue() / 100f));

        // Attributes
        attributesButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                VisualisationModel model = vizController.getModel();
                LabelAttributesPanel panel = new LabelAttributesPanel(model, false);
                panel.setup();
                DialogDescriptor dd = new DialogDescriptor(panel,
                    NbBundle.getMessage(NodeLabelsSettingsPanel.class, "LabelAttributesPanel.title"), true,
                    NotifyDescriptor.OK_CANCEL_OPTION, null, null);
                if (DialogDisplayer.getDefault().notify(dd).equals(NotifyDescriptor.OK_OPTION)) {
                    panel.unsetup();
                }
            }
        });
        attributesButton.setIcon(ImageUtilities.loadImageIcon("VisualizationImpl/configureLabels.svg", false));

        // Hide non selected
        hideNonSelectedCheckbox.addActionListener(
            e -> vizController.setHideNonSelectedNodeLabels(hideNonSelectedCheckbox.isSelected()));

        // Fit to node size
        fitToNodeSizeToggleButton.addActionListener(
            e -> vizController.setNodeLabelFitToNodeSize(fitToNodeSizeToggleButton.isSelected()));
        fitToNodeSizeToggleButton.setIcon(ImageUtilities.loadImageIcon("VisualizationImpl/fitToNodeSize.svg", false));
        fitToNodeSizeToggleButton.setToolTipText(NbBundle.getMessage(NodeLabelsSettingsPanel.class,
            "NodeLabelsSettingsPanel.fitToNodeSizeToggleButton.toolTipText"));

        // Avoid overlap
        avoidOverlap.addActionListener(e -> vizController.setAvoidNodeLabelOverlap(avoidOverlap.isSelected()));
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
        if (evt.getPropertyName().equals("showNodeLabels")) {
            refreshSharedConfig(model);
        } else if (evt.getPropertyName().equals("nodeLabelFont")) {
            refreshSharedConfig(model);
        } else if (evt.getPropertyName().equals("nodeLabelColor")) {
            refreshSharedConfig(model);
        } else if (evt.getPropertyName().equals("nodeLabelScale")) {
            refreshSharedConfig(model);
        } else if (evt.getPropertyName().equals("hideNonSelectedNodeLabels")) {
            refreshSharedConfig(model);
        } else if (evt.getPropertyName().equals("nodeLabelSizeMode")) {
            refreshSharedConfig(model);
        } else if (evt.getPropertyName().equals("nodeLabelColorMode")) {
            refreshSharedConfig(model);
        } else if (evt.getPropertyName().equals("nodeLabelFitToNodeSize")) {
            refreshSharedConfig(model);
        } else if (evt.getPropertyName().equals("avoidNodeLabelOverlap")) {
            refreshSharedConfig(model);
        }
    }

    private void refreshSharedConfig(VisualisationModel vizModel) {
        if (showLabelsCheckbox.isSelected() != vizModel.isShowNodeLabels()) {
            showLabelsCheckbox.setSelected(vizModel.isShowNodeLabels());
        }
        if (nodeColorCombo.getSelectedItem() != vizModel.getNodeLabelColorMode()) {
            nodeColorCombo.setSelectedItem(vizModel.getNodeLabelColorMode());
        }
        if (nodeSizeCombo.getSelectedItem() != vizModel.getNodeLabelSizeMode()) {
            nodeSizeCombo.setSelectedItem(vizModel.getNodeLabelSizeMode());
        }
        nodeFontButton.setText(
            vizModel.getNodeLabelFont().getFontName() + ", " + vizModel.getNodeLabelFont().getSize());
        if (nodeSizeSlider.getValue() / 100f != vizModel.getNodeLabelScale()) {
            nodeSizeSlider.setValue((int) (vizModel.getNodeLabelScale() * 100f));
        }
        if (hideNonSelectedCheckbox.isSelected() != vizModel.isHideNonSelectedNodeLabels()) {
            hideNonSelectedCheckbox.setSelected(vizModel.isHideNonSelectedNodeLabels());
        }
        if (fitToNodeSizeToggleButton.isSelected() != vizModel.isNodeLabelFitToNodeSize()) {
            fitToNodeSizeToggleButton.setSelected(vizModel.isNodeLabelFitToNodeSize());
        }
        if (avoidOverlap.isSelected() != vizModel.isAvoidNodeLabelOverlap()) {
            avoidOverlap.setSelected(vizModel.isAvoidNodeLabelOverlap());
        }
        selfColorLink.setVisible(nodeColorCombo.getSelectedItem() == LabelColorMode.SELF);
    }

    private void setEnable(boolean enable) {
        showLabelsCheckbox.setEnabled(enable);
        nodeColorCombo.setEnabled(enable && showLabelsCheckbox.isSelected());
        labelNodeColor.setEnabled(enable && showLabelsCheckbox.isSelected());
        nodeFontButton.setEnabled(enable && showLabelsCheckbox.isSelected());
        labelNodeFont.setEnabled(enable && showLabelsCheckbox.isSelected());
        nodeSizeSlider.setEnabled(enable && showLabelsCheckbox.isSelected());
        labelNodeScale.setEnabled(enable && showLabelsCheckbox.isSelected());
        nodeSizeCombo.setEnabled(enable && showLabelsCheckbox.isSelected());
        labelNodeSize.setEnabled(enable && showLabelsCheckbox.isSelected());
        fitToNodeSizeToggleButton.setEnabled(enable && showLabelsCheckbox.isSelected());
        hideNonSelectedCheckbox.setEnabled(enable && showLabelsCheckbox.isSelected());
        avoidOverlap.setEnabled(enable && showLabelsCheckbox.isSelected());
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
        labelNodeColor = new javax.swing.JLabel();
        nodeColorCombo = new javax.swing.JComboBox<>();
        labelNodeFont = new javax.swing.JLabel();
        nodeFontButton = new javax.swing.JButton();
        labelNodeScale = new javax.swing.JLabel();
        nodeSizeSlider = new javax.swing.JSlider();
        labelNodeSize = new javax.swing.JLabel();
        nodeSizeCombo = new javax.swing.JComboBox<>();
        fitToNodeSizeToggleButton = new javax.swing.JToggleButton();
        hideNonSelectedCheckbox = new javax.swing.JCheckBox();
        attributesButton = new javax.swing.JButton();
        selfColorLink = new org.jdesktop.swingx.JXHyperlink();
        avoidOverlap = new javax.swing.JCheckBox();

        showLabelsCheckbox.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        showLabelsCheckbox.setText(org.openide.util.NbBundle.getMessage(NodeLabelsSettingsPanel.class,
            "NodeLabelsSettingsPanel.showLabelsCheckbox.text")); // NOI18N

        labelNodeColor.setText(org.openide.util.NbBundle.getMessage(NodeLabelsSettingsPanel.class,
            "NodeLabelsSettingsPanel.labelNodeColor.text")); // NOI18N

        labelNodeFont.setText(org.openide.util.NbBundle.getMessage(NodeLabelsSettingsPanel.class,
            "NodeLabelsSettingsPanel.labelNodeFont.text")); // NOI18N
        labelNodeFont.setMaximumSize(new java.awt.Dimension(60, 15));

        nodeFontButton.setText(org.openide.util.NbBundle.getMessage(NodeLabelsSettingsPanel.class,
            "NodeLabelsSettingsPanel.nodeFontButton.text")); // NOI18N

        labelNodeScale.setText(org.openide.util.NbBundle.getMessage(NodeLabelsSettingsPanel.class,
            "NodeLabelsSettingsPanel.labelNodeScale.text")); // NOI18N

        nodeSizeSlider.setMinimum(1);

        labelNodeSize.setText(org.openide.util.NbBundle.getMessage(NodeLabelsSettingsPanel.class,
            "NodeLabelsSettingsPanel.labelNodeSize.text")); // NOI18N

        fitToNodeSizeToggleButton.setText(org.openide.util.NbBundle.getMessage(NodeLabelsSettingsPanel.class,
            "NodeLabelsSettingsPanel.fitToNodeSizeToggleButton.text")); // NOI18N

        hideNonSelectedCheckbox.setText(org.openide.util.NbBundle.getMessage(NodeLabelsSettingsPanel.class,
            "NodeLabelsSettingsPanel.hideNonSelectedCheckbox.text")); // NOI18N
        hideNonSelectedCheckbox.setBorder(null);
        hideNonSelectedCheckbox.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        hideNonSelectedCheckbox.setMargin(new java.awt.Insets(2, 0, 2, 2));

        attributesButton.setText(org.openide.util.NbBundle.getMessage(NodeLabelsSettingsPanel.class,
            "NodeLabelsSettingsPanel.attributesButton.text")); // NOI18N

        selfColorLink.setText(org.openide.util.NbBundle.getMessage(NodeLabelsSettingsPanel.class,
            "NodeLabelsSettingsPanel.selfColorLink.text")); // NOI18N

        avoidOverlap.setText(org.openide.util.NbBundle.getMessage(NodeLabelsSettingsPanel.class,
            "NodeLabelsSettingsPanel.avoidOverlap.text")); // NOI18N
        avoidOverlap.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        avoidOverlap.setMargin(new java.awt.Insets(2, 0, 2, 2));

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
                                    .addComponent(labelNodeColor)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(nodeColorCombo, javax.swing.GroupLayout.PREFERRED_SIZE, 145,
                                        javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(layout.createSequentialGroup()
                                    .addComponent(labelNodeSize)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                    .addComponent(nodeSizeCombo, javax.swing.GroupLayout.PREFERRED_SIZE, 145,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(layout.createSequentialGroup()
                                    .addGap(13, 13, 13)
                                    .addComponent(fitToNodeSizeToggleButton))
                                .addGroup(layout.createSequentialGroup()
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(selfColorLink, javax.swing.GroupLayout.PREFERRED_SIZE, 31,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGap(18, 18, 18)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addGroup(layout.createSequentialGroup()
                                    .addComponent(labelNodeFont, javax.swing.GroupLayout.PREFERRED_SIZE,
                                        javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(nodeFontButton, javax.swing.GroupLayout.DEFAULT_SIZE,
                                        javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addComponent(hideNonSelectedCheckbox))
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(layout.createSequentialGroup()
                                    .addGap(18, 18, 18)
                                    .addComponent(labelNodeScale)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(nodeSizeSlider, javax.swing.GroupLayout.PREFERRED_SIZE,
                                        javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(attributesButton, javax.swing.GroupLayout.PREFERRED_SIZE, 125,
                                        javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(layout.createSequentialGroup()
                                    .addGap(18, 18, 18)
                                    .addComponent(avoidOverlap))))
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
                        .addComponent(labelNodeColor)
                        .addComponent(nodeColorCombo, javax.swing.GroupLayout.PREFERRED_SIZE,
                            javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(labelNodeFont, javax.swing.GroupLayout.PREFERRED_SIZE,
                            javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(nodeFontButton)
                        .addComponent(labelNodeScale)
                        .addComponent(nodeSizeSlider, javax.swing.GroupLayout.PREFERRED_SIZE,
                            javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(selfColorLink, javax.swing.GroupLayout.PREFERRED_SIZE,
                            javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(attributesButton))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(labelNodeSize)
                        .addComponent(nodeSizeCombo, javax.swing.GroupLayout.PREFERRED_SIZE,
                            javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(fitToNodeSizeToggleButton)
                        .addComponent(hideNonSelectedCheckbox)
                        .addComponent(avoidOverlap))
                    .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
}


