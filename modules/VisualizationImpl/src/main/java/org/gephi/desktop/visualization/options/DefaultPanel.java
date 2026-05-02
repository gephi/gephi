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

package org.gephi.desktop.visualization.options;

import com.connectina.swing.fontchooser.JFontChooser;
import java.awt.Component;
import java.awt.Font;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import org.gephi.desktop.visualization.collapse.EdgeSettingsPanel;
import org.gephi.ui.utils.ColorUtils;
import org.gephi.ui.utils.FontUtils;
import org.gephi.visualization.VizConfig;
import org.gephi.visualization.api.EdgeColorMode;
import org.gephi.visualization.api.LabelColorMode;
import org.gephi.visualization.api.LabelSizeMode;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.windows.WindowManager;

final class DefaultPanel extends javax.swing.JPanel {

    private final DefaultOptionsPanelController controller;
    private Font nodeFont;
    private Font edgeFont;
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox autoSelectNeighborCheckbox;
    private javax.swing.JCheckBox avoidNodeLabelOverlapCheckbox;
    private net.java.dev.colorchooser.ColorChooser backgroundColor;
    private net.java.dev.colorchooser.ColorChooser edgeBothSelectionColorChooser;
    private javax.swing.JComboBox<EdgeColorMode> edgeColorCombo;
    private javax.swing.JButton edgeFontButton;
    private net.java.dev.colorchooser.ColorChooser edgeInSelectionColorChooser;
    private javax.swing.JComboBox<LabelColorMode> edgeLabelColorCombo;
    private javax.swing.JPanel edgeLabelPanel;
    private javax.swing.JSlider edgeLabelScaleSlider;
    private javax.swing.JComboBox<LabelSizeMode> edgeLabelSizeCombo;
    private net.java.dev.colorchooser.ColorChooser edgeOutSelectionColorChooser;
    private javax.swing.JPanel edgePanel;
    private javax.swing.JSlider edgeScaleSlider;
    private javax.swing.JPanel edgeSelectionColorsPanel;
    private javax.swing.JCheckBox fitToNodeSizeCheckbox;
    private javax.swing.JPanel globalPanel;
    private javax.swing.JCheckBox hideNonSelectedEdgeLabelsCheckbox;
    private javax.swing.JCheckBox hideNonSelectedEdgesCheckbox;
    private javax.swing.JCheckBox hideNonSelectedNodeLabelsCheckbox;
    private javax.swing.JCheckBox highlightCheckbox;
    private javax.swing.JLabel labelBackground;
    private javax.swing.JLabel labelEdgeBothColor;
    private javax.swing.JLabel labelEdgeColor;
    private javax.swing.JLabel labelEdgeFont;
    private javax.swing.JLabel labelEdgeInColor;
    private javax.swing.JLabel labelEdgeLabelColor;
    private javax.swing.JLabel labelEdgeLabelScale;
    private javax.swing.JLabel labelEdgeLabelSize;
    private javax.swing.JLabel labelEdgeOutColor;
    private javax.swing.JLabel labelEdgeScale;
    private javax.swing.JLabel labelNodeFont;
    private javax.swing.JLabel labelNodeLabelColor;
    private javax.swing.JLabel labelNodeLabelScale;
    private javax.swing.JLabel labelNodeLabelSize;
    private javax.swing.JLabel labelNodeScale;
    private javax.swing.JButton nodeFontButton;
    private javax.swing.JComboBox<LabelColorMode> nodeLabelColorCombo;
    private javax.swing.JPanel nodeLabelPanel;
    private javax.swing.JSlider nodeLabelScaleSlider;
    private javax.swing.JComboBox<LabelSizeMode> nodeLabelSizeCombo;
    private javax.swing.JPanel nodePanel;
    private javax.swing.JSlider nodeScaleSlider;
    private javax.swing.JCheckBox rescaleEdgeWeightCheckbox;
    private javax.swing.JButton resetButton;
    private javax.swing.JPanel resetPanel;
    private javax.swing.JCheckBox selectionColorCheckbox;
    private javax.swing.JCheckBox showEdgesCheckbox;
    private org.jdesktop.swingx.JXTitledSeparator titleEdgeLabelSettings;
    private org.jdesktop.swingx.JXTitledSeparator titleEdgeSettings;
    private org.jdesktop.swingx.JXTitledSeparator titleGlobalSettings;
    private org.jdesktop.swingx.JXTitledSeparator titleNodeLabelSettings;
    private org.jdesktop.swingx.JXTitledSeparator titleNodeSettings;
    private javax.swing.JCheckBox useEdgeWeightCheckbox;
    // End of variables declaration//GEN-END:variables

    DefaultPanel(DefaultOptionsPanelController panelController) {
        this.controller = panelController;
        initComponents();

        nodeFontButton.addActionListener(e -> {
            Font font = JFontChooser.showDialog(WindowManager.getDefault().getMainWindow(), nodeFont);
            if (font != null) {
                nodeFont = font;
                nodeFontButton.setText(nodeFont.getFontName() + ", " + nodeFont.getSize());
                controller.changed();
            }
        });
        edgeFontButton.addActionListener(e -> {
            Font font = JFontChooser.showDialog(WindowManager.getDefault().getMainWindow(), edgeFont);
            if (font != null) {
                edgeFont = font;
                edgeFontButton.setText(edgeFont.getFontName() + ", " + edgeFont.getSize());
                controller.changed();
            }
        });

        nodeLabelColorCombo.setModel(new DefaultComboBoxModel<>(LabelColorMode.values()));
        nodeLabelColorCombo.setRenderer(labelColorModeRenderer(false));
        nodeLabelColorCombo.addActionListener(e -> controller.changed());

        edgeLabelColorCombo.setModel(new DefaultComboBoxModel<>(LabelColorMode.values()));
        edgeLabelColorCombo.setRenderer(labelColorModeRenderer(true));
        edgeLabelColorCombo.addActionListener(e -> controller.changed());

        nodeLabelSizeCombo.setModel(new DefaultComboBoxModel<>(LabelSizeMode.values()));
        nodeLabelSizeCombo.setRenderer(labelSizeModeRenderer());
        nodeLabelSizeCombo.addActionListener(e -> controller.changed());

        edgeLabelSizeCombo.setModel(new DefaultComboBoxModel<>(LabelSizeMode.values()));
        edgeLabelSizeCombo.setRenderer(labelSizeModeRenderer());
        edgeLabelSizeCombo.addActionListener(e -> controller.changed());

        edgeColorCombo.setModel(new DefaultComboBoxModel<>(EdgeColorMode.values()));
        edgeColorCombo.setRenderer(edgeColorModeRenderer());
        edgeColorCombo.addActionListener(e -> controller.changed());

        autoSelectNeighborCheckbox.addActionListener(e -> controller.changed());
        highlightCheckbox.addActionListener(e -> controller.changed());
        backgroundColor.addPropertyChangeListener("color", e -> controller.changed());
        nodeScaleSlider.addChangeListener(e -> controller.changed());
        nodeLabelScaleSlider.addChangeListener(e -> controller.changed());
        hideNonSelectedNodeLabelsCheckbox.addActionListener(e -> controller.changed());
        fitToNodeSizeCheckbox.addActionListener(e -> controller.changed());
        avoidNodeLabelOverlapCheckbox.addActionListener(e -> controller.changed());
        edgeLabelScaleSlider.addChangeListener(e -> controller.changed());
        hideNonSelectedEdgeLabelsCheckbox.addActionListener(e -> controller.changed());

        edgeScaleSlider.addChangeListener(e -> controller.changed());
        showEdgesCheckbox.addActionListener(e -> controller.changed());
        hideNonSelectedEdgesCheckbox.addActionListener(e -> controller.changed());
        useEdgeWeightCheckbox.addActionListener(e -> controller.changed());
        rescaleEdgeWeightCheckbox.addActionListener(e -> controller.changed());
        selectionColorCheckbox.addActionListener(e -> controller.changed());
        edgeInSelectionColorChooser.addPropertyChangeListener("color", e -> controller.changed());
        edgeOutSelectionColorChooser.addPropertyChangeListener("color", e -> controller.changed());
        edgeBothSelectionColorChooser.addPropertyChangeListener("color", e -> controller.changed());
    }

    private DefaultListCellRenderer labelColorModeRenderer(boolean forEdges) {
        return new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
                                                          boolean cellHasFocus) {
                JLabel label =
                    (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof LabelColorMode mode) {
                    String prefix = forEdges ? "EdgeLabelColorMode." : "NodeLabelColorMode.";
                    label.setText(NbBundle.getMessage(EdgeSettingsPanel.class,
                        prefix + mode.name().toLowerCase() + ".name"));
                    label.setIcon(
                        ImageUtilities.loadIcon("VisualizationImpl/LabelColorMode_" + mode.name() + ".svg"));
                }
                return label;
            }
        };
    }

    private DefaultListCellRenderer labelSizeModeRenderer() {
        return new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
                                                          boolean cellHasFocus) {
                JLabel label =
                    (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof LabelSizeMode mode) {
                    label.setText(NbBundle.getMessage(EdgeSettingsPanel.class,
                        "LabelSizeMode." + mode.name().toLowerCase() + ".name"));
                    label.setIcon(
                        ImageUtilities.loadIcon("VisualizationImpl/LabelSizeMode_" + mode.name() + ".svg"));
                }
                return label;
            }
        };
    }

    private DefaultListCellRenderer edgeColorModeRenderer() {
        return new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
                                                          boolean cellHasFocus) {
                JLabel label =
                    (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof EdgeColorMode mode) {
                    label.setText(NbBundle.getMessage(EdgeSettingsPanel.class,
                        "EdgeColorMode." + mode.name().toLowerCase() + ".name"));
                    label.setIcon(
                        ImageUtilities.loadIcon("VisualizationImpl/EdgeColorMode_" + mode.name() + ".svg"));
                }
                return label;
            }
        };
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        titleGlobalSettings = new org.jdesktop.swingx.JXTitledSeparator();
        globalPanel = new javax.swing.JPanel();
        labelBackground = new javax.swing.JLabel();
        backgroundColor = new net.java.dev.colorchooser.ColorChooser();
        highlightCheckbox = new javax.swing.JCheckBox();
        autoSelectNeighborCheckbox = new javax.swing.JCheckBox();
        titleNodeSettings = new org.jdesktop.swingx.JXTitledSeparator();
        nodePanel = new javax.swing.JPanel();
        labelNodeScale = new javax.swing.JLabel();
        nodeScaleSlider = new javax.swing.JSlider();
        titleEdgeSettings = new org.jdesktop.swingx.JXTitledSeparator();
        edgePanel = new javax.swing.JPanel();
        labelEdgeColor = new javax.swing.JLabel();
        edgeColorCombo = new javax.swing.JComboBox<>();
        labelEdgeScale = new javax.swing.JLabel();
        edgeScaleSlider = new javax.swing.JSlider();
        showEdgesCheckbox = new javax.swing.JCheckBox();
        hideNonSelectedEdgesCheckbox = new javax.swing.JCheckBox();
        useEdgeWeightCheckbox = new javax.swing.JCheckBox();
        rescaleEdgeWeightCheckbox = new javax.swing.JCheckBox();
        selectionColorCheckbox = new javax.swing.JCheckBox();
        edgeSelectionColorsPanel = new javax.swing.JPanel();
        labelEdgeInColor = new javax.swing.JLabel();
        edgeInSelectionColorChooser = new net.java.dev.colorchooser.ColorChooser();
        labelEdgeOutColor = new javax.swing.JLabel();
        edgeOutSelectionColorChooser = new net.java.dev.colorchooser.ColorChooser();
        labelEdgeBothColor = new javax.swing.JLabel();
        edgeBothSelectionColorChooser = new net.java.dev.colorchooser.ColorChooser();
        titleNodeLabelSettings = new org.jdesktop.swingx.JXTitledSeparator();
        nodeLabelPanel = new javax.swing.JPanel();
        labelNodeFont = new javax.swing.JLabel();
        nodeFontButton = new javax.swing.JButton();
        labelNodeLabelColor = new javax.swing.JLabel();
        nodeLabelColorCombo = new javax.swing.JComboBox<>();
        labelNodeLabelSize = new javax.swing.JLabel();
        nodeLabelSizeCombo = new javax.swing.JComboBox<>();
        labelNodeLabelScale = new javax.swing.JLabel();
        nodeLabelScaleSlider = new javax.swing.JSlider();
        hideNonSelectedNodeLabelsCheckbox = new javax.swing.JCheckBox();
        fitToNodeSizeCheckbox = new javax.swing.JCheckBox();
        avoidNodeLabelOverlapCheckbox = new javax.swing.JCheckBox();
        titleEdgeLabelSettings = new org.jdesktop.swingx.JXTitledSeparator();
        edgeLabelPanel = new javax.swing.JPanel();
        labelEdgeFont = new javax.swing.JLabel();
        edgeFontButton = new javax.swing.JButton();
        labelEdgeLabelColor = new javax.swing.JLabel();
        edgeLabelColorCombo = new javax.swing.JComboBox<>();
        labelEdgeLabelSize = new javax.swing.JLabel();
        edgeLabelSizeCombo = new javax.swing.JComboBox<>();
        labelEdgeLabelScale = new javax.swing.JLabel();
        edgeLabelScaleSlider = new javax.swing.JSlider();
        hideNonSelectedEdgeLabelsCheckbox = new javax.swing.JCheckBox();
        resetPanel = new javax.swing.JPanel();
        resetButton = new javax.swing.JButton();

        titleGlobalSettings.setFont(
            titleGlobalSettings.getFont().deriveFont(titleGlobalSettings.getFont().getStyle() | java.awt.Font.BOLD));
        titleGlobalSettings.setTitle(org.openide.util.NbBundle.getMessage(DefaultPanel.class,
            "DefaultPanel.titleGlobalSettings.title")); // NOI18N

        globalPanel.setOpaque(false);
        globalPanel.setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(labelBackground,
            org.openide.util.NbBundle.getMessage(DefaultPanel.class, "DefaultPanel.labelBackground.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 8, 3, 8);
        globalPanel.add(labelBackground, gridBagConstraints);

        backgroundColor.setMaximumSize(new java.awt.Dimension(20, 20));
        backgroundColor.setPreferredSize(new java.awt.Dimension(20, 20));

        javax.swing.GroupLayout backgroundColorLayout = new javax.swing.GroupLayout(backgroundColor);
        backgroundColor.setLayout(backgroundColorLayout);
        backgroundColorLayout.setHorizontalGroup(
            backgroundColorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGap(0, 0, Short.MAX_VALUE)
        );
        backgroundColorLayout.setVerticalGroup(
            backgroundColorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGap(0, 0, Short.MAX_VALUE)
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        globalPanel.add(backgroundColor, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(highlightCheckbox,
            org.openide.util.NbBundle.getMessage(DefaultPanel.class, "DefaultPanel.highlightCheckbox.text")); // NOI18N
        highlightCheckbox.setMargin(new java.awt.Insets(2, 0, 2, 2));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 20, 3, 8);
        globalPanel.add(highlightCheckbox, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(autoSelectNeighborCheckbox,
            org.openide.util.NbBundle.getMessage(DefaultPanel.class,
                "DefaultPanel.autoSelectNeighborCheckbox.text")); // NOI18N
        autoSelectNeighborCheckbox.setMargin(new java.awt.Insets(2, 0, 2, 2));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 8, 3, 8);
        globalPanel.add(autoSelectNeighborCheckbox, gridBagConstraints);

        titleNodeSettings.setBorder(javax.swing.BorderFactory.createEmptyBorder(7, 0, 0, 0));
        titleNodeSettings.setFont(
            titleNodeSettings.getFont().deriveFont(titleNodeSettings.getFont().getStyle() | java.awt.Font.BOLD));
        titleNodeSettings.setTitle(
            org.openide.util.NbBundle.getMessage(DefaultPanel.class, "DefaultPanel.titleNodeSettings.title")); // NOI18N

        nodePanel.setOpaque(false);
        nodePanel.setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(labelNodeScale,
            org.openide.util.NbBundle.getMessage(DefaultPanel.class, "DefaultPanel.labelNodeScale.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 8, 3, 8);
        nodePanel.add(labelNodeScale, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 8, 3, 8);
        nodePanel.add(nodeScaleSlider, gridBagConstraints);

        titleEdgeSettings.setBorder(javax.swing.BorderFactory.createEmptyBorder(7, 0, 0, 0));
        titleEdgeSettings.setFont(
            titleEdgeSettings.getFont().deriveFont(titleEdgeSettings.getFont().getStyle() | java.awt.Font.BOLD));
        titleEdgeSettings.setTitle(
            org.openide.util.NbBundle.getMessage(DefaultPanel.class, "DefaultPanel.titleEdgeSettings.title")); // NOI18N

        edgePanel.setOpaque(false);
        edgePanel.setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(labelEdgeColor,
            org.openide.util.NbBundle.getMessage(DefaultPanel.class, "DefaultPanel.labelEdgeColor.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 8, 3, 8);
        edgePanel.add(labelEdgeColor, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 8, 3, 8);
        edgePanel.add(edgeColorCombo, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(labelEdgeScale,
            org.openide.util.NbBundle.getMessage(DefaultPanel.class, "DefaultPanel.labelEdgeScale.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 8, 3, 8);
        edgePanel.add(labelEdgeScale, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 8, 3, 8);
        edgePanel.add(edgeScaleSlider, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(showEdgesCheckbox,
            org.openide.util.NbBundle.getMessage(DefaultPanel.class, "DefaultPanel.showEdgesCheckbox.text")); // NOI18N
        showEdgesCheckbox.setMargin(new java.awt.Insets(2, 0, 2, 2));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 8, 3, 8);
        edgePanel.add(showEdgesCheckbox, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(hideNonSelectedEdgesCheckbox,
            org.openide.util.NbBundle.getMessage(DefaultPanel.class,
                "DefaultPanel.hideNonSelectedEdgesCheckbox.text")); // NOI18N
        hideNonSelectedEdgesCheckbox.setMargin(new java.awt.Insets(2, 0, 2, 2));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 8, 3, 8);
        edgePanel.add(hideNonSelectedEdgesCheckbox, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(useEdgeWeightCheckbox,
            org.openide.util.NbBundle.getMessage(DefaultPanel.class,
                "DefaultPanel.useEdgeWeightCheckbox.text")); // NOI18N
        useEdgeWeightCheckbox.setMargin(new java.awt.Insets(2, 0, 2, 2));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 8, 3, 8);
        edgePanel.add(useEdgeWeightCheckbox, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(rescaleEdgeWeightCheckbox,
            org.openide.util.NbBundle.getMessage(DefaultPanel.class,
                "DefaultPanel.rescaleEdgeWeightCheckbox.text")); // NOI18N
        rescaleEdgeWeightCheckbox.setMargin(new java.awt.Insets(2, 0, 2, 2));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 8, 3, 8);
        edgePanel.add(rescaleEdgeWeightCheckbox, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(selectionColorCheckbox,
            org.openide.util.NbBundle.getMessage(DefaultPanel.class,
                "DefaultPanel.selectionColorCheckbox.text")); // NOI18N
        selectionColorCheckbox.setMargin(new java.awt.Insets(2, 0, 2, 2));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 8, 3, 8);
        edgePanel.add(selectionColorCheckbox, gridBagConstraints);

        edgeSelectionColorsPanel.setOpaque(false);

        org.openide.awt.Mnemonics.setLocalizedText(labelEdgeInColor,
            org.openide.util.NbBundle.getMessage(DefaultPanel.class, "DefaultPanel.labelEdgeInColor.text")); // NOI18N
        edgeSelectionColorsPanel.add(labelEdgeInColor);

        edgeInSelectionColorChooser.setPreferredSize(new java.awt.Dimension(16, 16));
        edgeInSelectionColorChooser.setMaximumSize(new java.awt.Dimension(16, 16));

        javax.swing.GroupLayout edgeInSelectionColorChooserLayout =
            new javax.swing.GroupLayout(edgeInSelectionColorChooser);
        edgeInSelectionColorChooser.setLayout(edgeInSelectionColorChooserLayout);
        edgeInSelectionColorChooserLayout.setHorizontalGroup(
            edgeInSelectionColorChooserLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGap(0, 14, Short.MAX_VALUE)
        );
        edgeInSelectionColorChooserLayout.setVerticalGroup(
            edgeInSelectionColorChooserLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGap(0, 14, Short.MAX_VALUE)
        );

        edgeSelectionColorsPanel.add(edgeInSelectionColorChooser);

        org.openide.awt.Mnemonics.setLocalizedText(labelEdgeOutColor,
            org.openide.util.NbBundle.getMessage(DefaultPanel.class, "DefaultPanel.labelEdgeOutColor.text")); // NOI18N
        edgeSelectionColorsPanel.add(labelEdgeOutColor);

        edgeOutSelectionColorChooser.setPreferredSize(new java.awt.Dimension(16, 16));
        edgeOutSelectionColorChooser.setMaximumSize(new java.awt.Dimension(16, 16));

        javax.swing.GroupLayout edgeOutSelectionColorChooserLayout =
            new javax.swing.GroupLayout(edgeOutSelectionColorChooser);
        edgeOutSelectionColorChooser.setLayout(edgeOutSelectionColorChooserLayout);
        edgeOutSelectionColorChooserLayout.setHorizontalGroup(
            edgeOutSelectionColorChooserLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGap(0, 14, Short.MAX_VALUE)
        );
        edgeOutSelectionColorChooserLayout.setVerticalGroup(
            edgeOutSelectionColorChooserLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGap(0, 14, Short.MAX_VALUE)
        );

        edgeSelectionColorsPanel.add(edgeOutSelectionColorChooser);

        org.openide.awt.Mnemonics.setLocalizedText(labelEdgeBothColor,
            org.openide.util.NbBundle.getMessage(DefaultPanel.class, "DefaultPanel.labelEdgeBothColor.text")); // NOI18N
        edgeSelectionColorsPanel.add(labelEdgeBothColor);

        edgeBothSelectionColorChooser.setMaximumSize(new java.awt.Dimension(16, 16));
        edgeBothSelectionColorChooser.setPreferredSize(new java.awt.Dimension(16, 16));

        javax.swing.GroupLayout edgeBothSelectionColorChooserLayout =
            new javax.swing.GroupLayout(edgeBothSelectionColorChooser);
        edgeBothSelectionColorChooser.setLayout(edgeBothSelectionColorChooserLayout);
        edgeBothSelectionColorChooserLayout.setHorizontalGroup(
            edgeBothSelectionColorChooserLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGap(0, 14, Short.MAX_VALUE)
        );
        edgeBothSelectionColorChooserLayout.setVerticalGroup(
            edgeBothSelectionColorChooserLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGap(0, 14, Short.MAX_VALUE)
        );

        edgeSelectionColorsPanel.add(edgeBothSelectionColorChooser);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 3, 8);
        edgePanel.add(edgeSelectionColorsPanel, gridBagConstraints);

        titleNodeLabelSettings.setFont(titleNodeLabelSettings.getFont()
            .deriveFont(titleNodeLabelSettings.getFont().getStyle() | java.awt.Font.BOLD));
        titleNodeLabelSettings.setTitle(org.openide.util.NbBundle.getMessage(DefaultPanel.class,
            "DefaultPanel.titleNodeLabelSettings.title")); // NOI18N

        nodeLabelPanel.setOpaque(false);
        nodeLabelPanel.setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(labelNodeFont,
            org.openide.util.NbBundle.getMessage(DefaultPanel.class, "DefaultPanel.labelNodeFont.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 8, 3, 8);
        nodeLabelPanel.add(labelNodeFont, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(nodeFontButton,
            org.openide.util.NbBundle.getMessage(DefaultPanel.class, "DefaultPanel.nodeFontButton.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 8, 3, 8);
        nodeLabelPanel.add(nodeFontButton, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(labelNodeLabelColor,
            org.openide.util.NbBundle.getMessage(DefaultPanel.class,
                "DefaultPanel.labelNodeLabelColor.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 8, 3, 8);
        nodeLabelPanel.add(labelNodeLabelColor, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 8, 3, 8);
        nodeLabelPanel.add(nodeLabelColorCombo, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(labelNodeLabelSize,
            org.openide.util.NbBundle.getMessage(DefaultPanel.class, "DefaultPanel.labelNodeLabelSize.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 8, 3, 8);
        nodeLabelPanel.add(labelNodeLabelSize, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 8, 3, 8);
        nodeLabelPanel.add(nodeLabelSizeCombo, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(labelNodeLabelScale,
            org.openide.util.NbBundle.getMessage(DefaultPanel.class,
                "DefaultPanel.labelNodeLabelScale.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 8, 3, 8);
        nodeLabelPanel.add(labelNodeLabelScale, gridBagConstraints);

        nodeLabelScaleSlider.setMinimum(1);
        nodeLabelScaleSlider.setValue(1);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 8, 3, 8);
        nodeLabelPanel.add(nodeLabelScaleSlider, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(hideNonSelectedNodeLabelsCheckbox,
            org.openide.util.NbBundle.getMessage(DefaultPanel.class,
                "DefaultPanel.hideNonSelectedNodeLabelsCheckbox.text")); // NOI18N
        hideNonSelectedNodeLabelsCheckbox.setMargin(new java.awt.Insets(2, 0, 2, 2));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 8, 3, 8);
        nodeLabelPanel.add(hideNonSelectedNodeLabelsCheckbox, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(fitToNodeSizeCheckbox,
            org.openide.util.NbBundle.getMessage(DefaultPanel.class,
                "DefaultPanel.fitToNodeSizeCheckbox.text")); // NOI18N
        fitToNodeSizeCheckbox.setMargin(new java.awt.Insets(2, 0, 2, 2));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 8, 3, 8);
        nodeLabelPanel.add(fitToNodeSizeCheckbox, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(avoidNodeLabelOverlapCheckbox,
            org.openide.util.NbBundle.getMessage(DefaultPanel.class,
                "DefaultPanel.avoidNodeLabelOverlapCheckbox.text")); // NOI18N
        avoidNodeLabelOverlapCheckbox.setMargin(new java.awt.Insets(2, 0, 2, 2));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 8, 3, 8);
        nodeLabelPanel.add(avoidNodeLabelOverlapCheckbox, gridBagConstraints);

        titleEdgeLabelSettings.setBorder(javax.swing.BorderFactory.createEmptyBorder(7, 0, 0, 0));
        titleEdgeLabelSettings.setFont(titleEdgeLabelSettings.getFont()
            .deriveFont(titleEdgeLabelSettings.getFont().getStyle() | java.awt.Font.BOLD));
        titleEdgeLabelSettings.setTitle(org.openide.util.NbBundle.getMessage(DefaultPanel.class,
            "DefaultPanel.titleEdgeLabelSettings.title")); // NOI18N

        edgeLabelPanel.setOpaque(false);
        edgeLabelPanel.setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(labelEdgeFont,
            org.openide.util.NbBundle.getMessage(DefaultPanel.class, "DefaultPanel.labelEdgeFont.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 8, 3, 8);
        edgeLabelPanel.add(labelEdgeFont, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(edgeFontButton,
            org.openide.util.NbBundle.getMessage(DefaultPanel.class, "DefaultPanel.edgeFontButton.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 8, 3, 8);
        edgeLabelPanel.add(edgeFontButton, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(labelEdgeLabelColor,
            org.openide.util.NbBundle.getMessage(DefaultPanel.class,
                "DefaultPanel.labelEdgeLabelColor.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 8, 3, 8);
        edgeLabelPanel.add(labelEdgeLabelColor, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 8, 3, 8);
        edgeLabelPanel.add(edgeLabelColorCombo, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(labelEdgeLabelSize,
            org.openide.util.NbBundle.getMessage(DefaultPanel.class, "DefaultPanel.labelEdgeLabelSize.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 8, 3, 8);
        edgeLabelPanel.add(labelEdgeLabelSize, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 8, 3, 8);
        edgeLabelPanel.add(edgeLabelSizeCombo, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(labelEdgeLabelScale,
            org.openide.util.NbBundle.getMessage(DefaultPanel.class,
                "DefaultPanel.labelEdgeLabelScale.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 8, 3, 8);
        edgeLabelPanel.add(labelEdgeLabelScale, gridBagConstraints);

        edgeLabelScaleSlider.setMinimum(1);
        edgeLabelScaleSlider.setValue(1);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 8, 3, 8);
        edgeLabelPanel.add(edgeLabelScaleSlider, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(hideNonSelectedEdgeLabelsCheckbox,
            org.openide.util.NbBundle.getMessage(DefaultPanel.class,
                "DefaultPanel.hideNonSelectedEdgeLabelsCheckbox.text")); // NOI18N
        hideNonSelectedEdgeLabelsCheckbox.setMargin(new java.awt.Insets(2, 0, 2, 2));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 8, 3, 8);
        edgeLabelPanel.add(hideNonSelectedEdgeLabelsCheckbox, gridBagConstraints);

        resetPanel.setOpaque(false);

        org.openide.awt.Mnemonics.setLocalizedText(resetButton,
            org.openide.util.NbBundle.getMessage(DefaultPanel.class, "DefaultPanel.resetButton.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(resetButton)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(resetPanel, javax.swing.GroupLayout.DEFAULT_SIZE,
                                javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addComponent(edgePanel, javax.swing.GroupLayout.DEFAULT_SIZE,
                            javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(nodePanel, javax.swing.GroupLayout.Alignment.TRAILING,
                            javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(globalPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 344,
                                javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(0, 0, Short.MAX_VALUE))
                        .addComponent(nodeLabelPanel, javax.swing.GroupLayout.DEFAULT_SIZE,
                            javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(layout.createSequentialGroup()
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(titleEdgeLabelSettings, javax.swing.GroupLayout.DEFAULT_SIZE,
                                    javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(titleNodeLabelSettings, javax.swing.GroupLayout.DEFAULT_SIZE,
                                    javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(titleEdgeSettings, javax.swing.GroupLayout.DEFAULT_SIZE,
                                    javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(titleGlobalSettings, javax.swing.GroupLayout.DEFAULT_SIZE,
                                    javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(titleNodeSettings, javax.swing.GroupLayout.DEFAULT_SIZE,
                                    javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addContainerGap())
                        .addComponent(edgeLabelPanel, javax.swing.GroupLayout.DEFAULT_SIZE,
                            javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(titleGlobalSettings, javax.swing.GroupLayout.PREFERRED_SIZE,
                        javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(globalPanel, javax.swing.GroupLayout.PREFERRED_SIZE,
                        javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(titleNodeSettings, javax.swing.GroupLayout.PREFERRED_SIZE,
                        javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 0, 0)
                    .addComponent(nodePanel, javax.swing.GroupLayout.PREFERRED_SIZE,
                        javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(titleEdgeSettings, javax.swing.GroupLayout.PREFERRED_SIZE,
                        javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(edgePanel, javax.swing.GroupLayout.PREFERRED_SIZE,
                        javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(titleNodeLabelSettings, javax.swing.GroupLayout.PREFERRED_SIZE,
                        javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(nodeLabelPanel, javax.swing.GroupLayout.PREFERRED_SIZE,
                        javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(titleEdgeLabelSettings, javax.swing.GroupLayout.PREFERRED_SIZE,
                        javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(edgeLabelPanel, javax.swing.GroupLayout.PREFERRED_SIZE,
                        javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 19, Short.MAX_VALUE)
                            .addComponent(resetButton)
                            .addContainerGap())
                        .addGroup(layout.createSequentialGroup()
                            .addGap(13, 13, 13)
                            .addComponent(resetPanel, javax.swing.GroupLayout.PREFERRED_SIZE,
                                javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void resetButtonActionPerformed(
        java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetButtonActionPerformed

        java.util.prefs.Preferences prefs = NbPreferences.forModule(VizConfig.class);
        prefs.remove(VizConfig.HIGHLIGHT);
        prefs.remove(VizConfig.NEIGHBOUR_SELECT);
        prefs.remove(VizConfig.BACKGROUND_COLOR);
        prefs.remove(VizConfig.NODE_SCALE);
        prefs.remove(VizConfig.NODE_LABEL_FONT);
        prefs.remove(VizConfig.NODE_LABEL_COLOR_MODE);
        prefs.remove(VizConfig.NODE_LABEL_SIZE_MODE);
        prefs.remove(VizConfig.NODE_LABEL_SCALE);
        prefs.remove(VizConfig.HIDE_NONSELECTED_NODE_LABELS);
        prefs.remove(VizConfig.FIT_NODE_LABELS_TO_NODE_SIZE);
        prefs.remove(VizConfig.AVOID_NODE_LABEL_OVERLAP);
        prefs.remove(VizConfig.EDGE_COLOR_MODE);
        prefs.remove(VizConfig.EDGE_SCALE);
        prefs.remove(VizConfig.SHOW_EDGES);
        prefs.remove(VizConfig.HIDE_NONSELECTED_EDGES);
        prefs.remove(VizConfig.EDGE_WEIGHTED);
        prefs.remove(VizConfig.EDGE_RESCALE_WEIGHT);
        prefs.remove(VizConfig.SELECTEDEDGE_HAS_COLOR);
        prefs.remove(VizConfig.SELECTEDEDGE_IN_COLOR);
        prefs.remove(VizConfig.SELECTEDEDGE_OUT_COLOR);
        prefs.remove(VizConfig.SELECTEDEDGE_BOTH_COLOR);
        prefs.remove(VizConfig.EDGE_LABEL_FONT);
        prefs.remove(VizConfig.EDGE_LABEL_COLOR_MODE);
        prefs.remove(VizConfig.EDGE_LABEL_SIZE_MODE);
        prefs.remove(VizConfig.EDGE_LABEL_SCALE);
        prefs.remove(VizConfig.HIDE_NONSELECTED_EDGE_LABELS);
        load();
    }//GEN-LAST:event_resetButtonActionPerformed

    void load() {
        java.util.prefs.Preferences prefs = NbPreferences.forModule(VizConfig.class);

        // Global settings
        highlightCheckbox.setSelected(
            prefs.getBoolean(VizConfig.HIGHLIGHT, VizConfig.DEFAULT_HIGHLIGHT));
        autoSelectNeighborCheckbox.setSelected(
            prefs.getBoolean(VizConfig.NEIGHBOUR_SELECT, VizConfig.DEFAULT_NEIGHBOUR_SELECT));
        backgroundColor.setColor(ColorUtils.decode(
            prefs.get(VizConfig.BACKGROUND_COLOR, ColorUtils.encode(VizConfig.DEFAULT_BACKGROUND_COLOR))));

        // Node settings
        float nodeScale = prefs.getFloat(VizConfig.NODE_SCALE, VizConfig.DEFAULT_NODE_SCALE);
        int nodeScaleVal = (int) Math.round(
            Math.log((double) nodeScale / VizConfig.NODE_SCALE_MIN)
                / Math.log((double) VizConfig.NODE_SCALE_MAX / VizConfig.NODE_SCALE_MIN) * 100);
        nodeScaleSlider.setValue(Math.max(0, Math.min(100, nodeScaleVal)));

        // Node label settings
        nodeFont = Font.decode(
            prefs.get(VizConfig.NODE_LABEL_FONT, FontUtils.encode(VizConfig.DEFAULT_NODE_LABEL_FONT)));
        nodeFontButton.setText(nodeFont.getFontName() + ", " + nodeFont.getSize());
        nodeLabelColorCombo.setSelectedItem(LabelColorMode.valueOf(
            prefs.get(VizConfig.NODE_LABEL_COLOR_MODE, VizConfig.DEFAULT_NODE_LABEL_COLOR_MODE)));
        nodeLabelSizeCombo.setSelectedItem(LabelSizeMode.valueOf(
            prefs.get(VizConfig.NODE_LABEL_SIZE_MODE, VizConfig.DEFAULT_NODE_LABEL_SIZE_MODE)));
        float nodeLabelScale = prefs.getFloat(VizConfig.NODE_LABEL_SCALE, VizConfig.DEFAULT_NODE_LABEL_SCALE);
        int nodeLabelScaleVal = 1 + (int) (
            (nodeLabelScale - VizConfig.NODE_LABEL_SCALE_MIN)
                / (VizConfig.NODE_LABEL_SCALE_MAX - VizConfig.NODE_LABEL_SCALE_MIN) * 99);
        nodeLabelScaleSlider.setValue(Math.max(1, Math.min(100, nodeLabelScaleVal)));
        hideNonSelectedNodeLabelsCheckbox.setSelected(
            prefs.getBoolean(VizConfig.HIDE_NONSELECTED_NODE_LABELS, VizConfig.DEFAULT_HIDE_NONSELECTED_NODE_LABELS));
        fitToNodeSizeCheckbox.setSelected(
            prefs.getBoolean(VizConfig.FIT_NODE_LABELS_TO_NODE_SIZE, VizConfig.DEFAULT_FIT_NODE_LABELS_TO_NODE_SIZE));
        avoidNodeLabelOverlapCheckbox.setSelected(
            prefs.getBoolean(VizConfig.AVOID_NODE_LABEL_OVERLAP, VizConfig.DEFAULT_AVOID_NODE_LABEL_OVERLAP));

        // Edge settings
        edgeColorCombo.setSelectedItem(EdgeColorMode.valueOf(
            prefs.get(VizConfig.EDGE_COLOR_MODE, VizConfig.DEFAULT_EDGE_COLOR_MODE.name())));
        float edgeScale = prefs.getFloat(VizConfig.EDGE_SCALE, VizConfig.DEFAULT_EDGE_SCALE);
        int edgeScaleVal = (int) Math.round(
            Math.log((double) edgeScale / VizConfig.EDGE_SCALE_MIN)
                / Math.log((double) VizConfig.EDGE_SCALE_MAX / VizConfig.EDGE_SCALE_MIN) * 100);
        edgeScaleSlider.setValue(Math.max(0, Math.min(100, edgeScaleVal)));
        showEdgesCheckbox.setSelected(
            prefs.getBoolean(VizConfig.SHOW_EDGES, VizConfig.DEFAULT_SHOW_EDGES));
        hideNonSelectedEdgesCheckbox.setSelected(
            prefs.getBoolean(VizConfig.HIDE_NONSELECTED_EDGES, VizConfig.DEFAULT_HIDE_NONSELECTED_EDGES));
        useEdgeWeightCheckbox.setSelected(
            prefs.getBoolean(VizConfig.EDGE_WEIGHTED, VizConfig.DEFAULT_EDGE_WEIGHTED));
        rescaleEdgeWeightCheckbox.setSelected(
            prefs.getBoolean(VizConfig.EDGE_RESCALE_WEIGHT, VizConfig.DEFAULT_EDGE_RESCALE_WEIGHTED));
        selectionColorCheckbox.setSelected(
            prefs.getBoolean(VizConfig.SELECTEDEDGE_HAS_COLOR, VizConfig.DEFAULT_SELECTEDEDGE_HAS_COLOR));
        edgeInSelectionColorChooser.setColor(ColorUtils.decode(
            prefs.get(VizConfig.SELECTEDEDGE_IN_COLOR, ColorUtils.encode(VizConfig.DEFAULT_SELECTEDEDGE_IN_COLOR))));
        edgeOutSelectionColorChooser.setColor(ColorUtils.decode(
            prefs.get(VizConfig.SELECTEDEDGE_OUT_COLOR, ColorUtils.encode(VizConfig.DEFAULT_SELECTEDEDGE_OUT_COLOR))));
        edgeBothSelectionColorChooser.setColor(ColorUtils.decode(
            prefs.get(VizConfig.SELECTEDEDGE_BOTH_COLOR,
                ColorUtils.encode(VizConfig.DEFAULT_SELECTEDEDGE_BOTH_COLOR))));

        // Edge label settings
        edgeFont = Font.decode(
            prefs.get(VizConfig.EDGE_LABEL_FONT, FontUtils.encode(VizConfig.DEFAULT_EDGE_LABEL_FONT)));
        edgeFontButton.setText(edgeFont.getFontName() + ", " + edgeFont.getSize());
        edgeLabelColorCombo.setSelectedItem(LabelColorMode.valueOf(
            prefs.get(VizConfig.EDGE_LABEL_COLOR_MODE, VizConfig.DEFAULT_EDGE_LABEL_COLOR_MODE)));
        edgeLabelSizeCombo.setSelectedItem(LabelSizeMode.valueOf(
            prefs.get(VizConfig.EDGE_LABEL_SIZE_MODE, VizConfig.DEFAULT_EDGE_LABEL_SIZE_MODE)));
        float edgeLabelScale = prefs.getFloat(VizConfig.EDGE_LABEL_SCALE, VizConfig.DEFAULT_EDGE_LABEL_SCALE);
        int edgeLabelScaleVal = 1 + (int) (
            (edgeLabelScale - VizConfig.EDGE_LABEL_SCALE_MIN)
                / (VizConfig.EDGE_LABEL_SCALE_MAX - VizConfig.EDGE_LABEL_SCALE_MIN) * 99);
        edgeLabelScaleSlider.setValue(Math.max(1, Math.min(100, edgeLabelScaleVal)));
        hideNonSelectedEdgeLabelsCheckbox.setSelected(
            prefs.getBoolean(VizConfig.HIDE_NONSELECTED_EDGE_LABELS, VizConfig.DEFAULT_HIDE_NONSELECTED_EDGE_LABELS));
    }

    void store() {
        java.util.prefs.Preferences prefs = NbPreferences.forModule(VizConfig.class);

        // Global settings
        prefs.putBoolean(VizConfig.HIGHLIGHT, highlightCheckbox.isSelected());
        prefs.putBoolean(VizConfig.NEIGHBOUR_SELECT, autoSelectNeighborCheckbox.isSelected());
        prefs.put(VizConfig.BACKGROUND_COLOR, ColorUtils.encode(backgroundColor.getColor()));

        // Node settings
        float nodeScale = VizConfig.NODE_SCALE_MIN * (float) Math.pow(
            (double) VizConfig.NODE_SCALE_MAX / VizConfig.NODE_SCALE_MIN, nodeScaleSlider.getValue() / 100.0);
        prefs.putFloat(VizConfig.NODE_SCALE, nodeScale);

        // Node label settings
        prefs.put(VizConfig.NODE_LABEL_FONT, FontUtils.encode(nodeFont));
        LabelColorMode nodeLabelColorMode = (LabelColorMode) nodeLabelColorCombo.getSelectedItem();
        if (nodeLabelColorMode != null) {
            prefs.put(VizConfig.NODE_LABEL_COLOR_MODE, nodeLabelColorMode.name());
        }
        LabelSizeMode nodeLabelSizeMode = (LabelSizeMode) nodeLabelSizeCombo.getSelectedItem();
        if (nodeLabelSizeMode != null) {
            prefs.put(VizConfig.NODE_LABEL_SIZE_MODE, nodeLabelSizeMode.name());
        }
        float nodeLabelScale = VizConfig.NODE_LABEL_SCALE_MIN
            + (VizConfig.NODE_LABEL_SCALE_MAX - VizConfig.NODE_LABEL_SCALE_MIN)
            * (nodeLabelScaleSlider.getValue() - 1) / 99f;
        prefs.putFloat(VizConfig.NODE_LABEL_SCALE, nodeLabelScale);
        prefs.putBoolean(VizConfig.HIDE_NONSELECTED_NODE_LABELS, hideNonSelectedNodeLabelsCheckbox.isSelected());
        prefs.putBoolean(VizConfig.FIT_NODE_LABELS_TO_NODE_SIZE, fitToNodeSizeCheckbox.isSelected());
        prefs.putBoolean(VizConfig.AVOID_NODE_LABEL_OVERLAP, avoidNodeLabelOverlapCheckbox.isSelected());

        // Edge settings
        EdgeColorMode edgeColorMode = (EdgeColorMode) edgeColorCombo.getSelectedItem();
        if (edgeColorMode != null) {
            prefs.put(VizConfig.EDGE_COLOR_MODE, edgeColorMode.name());
        }
        float edgeScale = VizConfig.EDGE_SCALE_MIN * (float) Math.pow(
            (double) VizConfig.EDGE_SCALE_MAX / VizConfig.EDGE_SCALE_MIN, edgeScaleSlider.getValue() / 100.0);
        prefs.putFloat(VizConfig.EDGE_SCALE, edgeScale);
        prefs.putBoolean(VizConfig.SHOW_EDGES, showEdgesCheckbox.isSelected());
        prefs.putBoolean(VizConfig.HIDE_NONSELECTED_EDGES, hideNonSelectedEdgesCheckbox.isSelected());
        prefs.putBoolean(VizConfig.EDGE_WEIGHTED, useEdgeWeightCheckbox.isSelected());
        prefs.putBoolean(VizConfig.EDGE_RESCALE_WEIGHT, rescaleEdgeWeightCheckbox.isSelected());
        prefs.putBoolean(VizConfig.SELECTEDEDGE_HAS_COLOR, selectionColorCheckbox.isSelected());
        prefs.put(VizConfig.SELECTEDEDGE_IN_COLOR, ColorUtils.encode(edgeInSelectionColorChooser.getColor()));
        prefs.put(VizConfig.SELECTEDEDGE_OUT_COLOR, ColorUtils.encode(edgeOutSelectionColorChooser.getColor()));
        prefs.put(VizConfig.SELECTEDEDGE_BOTH_COLOR, ColorUtils.encode(edgeBothSelectionColorChooser.getColor()));

        // Edge label settings
        prefs.put(VizConfig.EDGE_LABEL_FONT, FontUtils.encode(edgeFont));
        LabelColorMode edgeLabelColorMode = (LabelColorMode) edgeLabelColorCombo.getSelectedItem();
        if (edgeLabelColorMode != null) {
            prefs.put(VizConfig.EDGE_LABEL_COLOR_MODE, edgeLabelColorMode.name());
        }
        LabelSizeMode edgeLabelSizeMode = (LabelSizeMode) edgeLabelSizeCombo.getSelectedItem();
        if (edgeLabelSizeMode != null) {
            prefs.put(VizConfig.EDGE_LABEL_SIZE_MODE, edgeLabelSizeMode.name());
        }
        float edgeLabelScale = VizConfig.EDGE_LABEL_SCALE_MIN
            + (VizConfig.EDGE_LABEL_SCALE_MAX - VizConfig.EDGE_LABEL_SCALE_MIN)
            * (edgeLabelScaleSlider.getValue() - 1) / 99f;
        prefs.putFloat(VizConfig.EDGE_LABEL_SCALE, edgeLabelScale);
        prefs.putBoolean(VizConfig.HIDE_NONSELECTED_EDGE_LABELS, hideNonSelectedEdgeLabelsCheckbox.isSelected());
    }

    boolean valid() {
        return true;
    }
}
