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
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.gephi.ui.components.JColorButton;
import org.gephi.visualization.VizController;
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
import org.openide.windows.WindowManager;

/**
 * @author Mathieu Bastian
 */
public class LabelSettingsPanel extends javax.swing.JPanel implements VisualizationPropertyChangeListener {

    private final VisualizationController vizController;
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox<LabelColorMode> colorModeCombo;
    private javax.swing.JButton configureLabelsButton;
    private javax.swing.JButton edgeColorButton;
    private javax.swing.JButton edgeFontButton;
    private javax.swing.JPanel edgePanel;
    private javax.swing.JSlider edgeSizeSlider;
    private javax.swing.JCheckBox hideNonSelectedCheckbox;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JLabel labelColorMode;
    private javax.swing.JLabel labelEdgeColor;
    private javax.swing.JLabel labelEdgeFont;
    private javax.swing.JLabel labelEdgeSize;
    private javax.swing.JLabel labelNodeColor;
    private javax.swing.JLabel labelNodeFont;
    private javax.swing.JLabel labelNodeSize;
    private javax.swing.JLabel labelSizeMode;
    private javax.swing.JButton nodeColorButton;
    private javax.swing.JButton nodeFontButton;
    private javax.swing.JPanel nodePanel;
    private javax.swing.JSlider nodeSizeSlider;
    private javax.swing.JCheckBox showEdgeLabelsCheckbox;
    private javax.swing.JCheckBox showNodeLabelsCheckbox;
    private javax.swing.JComboBox<LabelSizeMode> sizeModeCombo;
    // End of variables declaration//GEN-END:variables



    /**
     * Creates new form LabelSettingsPanel
     */
    public LabelSettingsPanel() {
        vizController = Lookup.getDefault().lookup(VizController.class);

        initComponents();

        showNodeLabelsCheckbox.addItemListener(e -> {
            vizController.setShowNodeLabels(showNodeLabelsCheckbox.isSelected());
            setEnable(true);
        });
        nodeFontButton.addActionListener(e -> {
            VisualisationModel model = vizController.getModel();
            Font font = JFontChooser.showDialog(WindowManager.getDefault().getMainWindow(), model.getNodeLabelFont());
            if (font != null && font != model.getNodeLabelFont()) {
                vizController.setNodeLabelFont(font);
            }
        });
        ((JColorButton) nodeColorButton)
            .addPropertyChangeListener(JColorButton.EVENT_COLOR,
                evt -> vizController.setNodeLabelColor(((JColorButton) nodeColorButton).getColor()));
        nodeSizeSlider.addChangeListener(e -> vizController.setNodeLabelSize(nodeSizeSlider.getValue() / 100f));

        //EdgePanel
        showEdgeLabelsCheckbox.addItemListener(e -> {
            vizController.setShowEdgeLabels(showEdgeLabelsCheckbox.isSelected());
            setEnable(true);
        });
        edgeFontButton.addActionListener(e -> {
            VisualisationModel model = vizController.getModel();
            Font font = JFontChooser.showDialog(WindowManager.getDefault().getMainWindow(), model.getEdgeLabelFont());
            if (font != null && font != model.getNodeLabelFont()) {
                vizController.setEdgeLabelFont(font);
            }
        });
        ((JColorButton) edgeColorButton)
            .addPropertyChangeListener(JColorButton.EVENT_COLOR,
                evt -> vizController.setEdgeLabelColor(((JColorButton) edgeColorButton).getColor()));
        edgeSizeSlider.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                vizController.setEdgeLabelSize(edgeSizeSlider.getValue() / 100f);
            }
        });

        //General
        final DefaultComboBoxModel<LabelSizeMode> sizeModeModel = new DefaultComboBoxModel<>(LabelSizeMode.values());
        sizeModeCombo.setModel(sizeModeModel);
        final DefaultComboBoxModel<LabelColorMode> colorModeModel = new DefaultComboBoxModel<>(LabelColorMode.values());
        colorModeCombo.setModel(colorModeModel);
        sizeModeCombo.addActionListener(
            e -> vizController.setNodeLabelSizeMode((LabelSizeMode) sizeModeModel.getSelectedItem()));
        colorModeCombo.addActionListener(
            e -> vizController.setNodeLabelColorMode((LabelColorMode) colorModeModel.getSelectedItem()));
        hideNonSelectedCheckbox.addItemListener(
            e -> vizController.setHideNonSelectedLabels(hideNonSelectedCheckbox.isSelected()));

        // Attributes
        configureLabelsButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                VisualisationModel model = vizController.getModel();
                LabelAttributesPanel panel = new LabelAttributesPanel(model);
                panel.setup();
                DialogDescriptor dd = new DialogDescriptor(panel,
                    NbBundle.getMessage(LabelSettingsPanel.class, "LabelAttributesPanel.title"), true,
                    NotifyDescriptor.OK_CANCEL_OPTION, null, null);
                if (DialogDisplayer.getDefault().notify(dd).equals(NotifyDescriptor.OK_OPTION)) {
                    panel.unsetup();
                }
            }
        });

        // Renderers
        sizeModeCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
                                                          boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

                if (value instanceof LabelSizeMode) {
                    if (value == LabelSizeMode.FIXED) {
                        setText(org.openide.util.NbBundle.getMessage(LabelSettingsPanel.class, "FixedSizeMode.name"));
                    } else if (value == LabelSizeMode.SCALED) {
                        setText(org.openide.util.NbBundle.getMessage(LabelSettingsPanel.class, "ScaledSizeMode.name"));
                    } else if (value == LabelSizeMode.PROPORTIONAL) {
                        setText(org.openide.util.NbBundle.getMessage(LabelSettingsPanel.class,
                            "ProportionalSizeMode.name"));
                    }
                }
                return this;
            }
        });
        colorModeCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
                                                          boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

                if (value instanceof LabelColorMode) {
                    if (value == LabelColorMode.UNIQUE) {
                        setText(org.openide.util.NbBundle.getMessage(LabelSettingsPanel.class, "UniqueColorMode.name"));
                    } else if (value == LabelColorMode.TEXT) {
                        setText(org.openide.util.NbBundle.getMessage(LabelSettingsPanel.class, "TextColorMode.name"));
                    } else if (value == LabelColorMode.OBJECT) {
                        setText(org.openide.util.NbBundle.getMessage(LabelSettingsPanel.class,
                            "ObjectColorMode.name"));
                    }
                }
                return this;
            }
        });

    }

    public void propertyChange(VisualisationModel model, PropertyChangeEvent evt) {
        if(evt.getPropertyName().equals("showNodeLabels")) {
            refreshSharedConfig(model);
        } else if (evt.getPropertyName().equals("showEdgeLabels")) {
            refreshSharedConfig(model);
        } else if (evt.getPropertyName().equals("nodeLabelFont")) {
            refreshSharedConfig(model);
        } else if (evt.getPropertyName().equals("nodeLabelColor")) {
            refreshSharedConfig(model);
        } else if (evt.getPropertyName().equals("nodeLabelSize")) {
            refreshSharedConfig(model);
        } else if (evt.getPropertyName().equals("edgeLabelFont")) {
            refreshSharedConfig(model);
        } else if (evt.getPropertyName().equals("edgeLabelColor")) {
            refreshSharedConfig(model);
        } else if (evt.getPropertyName().equals("edgeLabelSize")) {
            refreshSharedConfig(model);
        } else if (evt.getPropertyName().equals("hideNonSelectedLabels")) {
            refreshSharedConfig(model);
        } else if (evt.getPropertyName().equals("nodeLabelSizeMode")) {
            refreshSharedConfig(model);
        } else if (evt.getPropertyName().equals("nodeLabelColorMode")) {
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

    private void refreshSharedConfig(VisualisationModel model) {
        // Node
        nodeFontButton.setText(model.getNodeLabelFont().getFontName() + ", " + model.getNodeLabelFont().getSize());
        ((JColorButton) nodeColorButton).setColor(model.getNodeLabelColor());
        if (showNodeLabelsCheckbox.isSelected() != model.isShowNodeLabels()) {
            showNodeLabelsCheckbox.setSelected(model.isShowNodeLabels());
        }
        if (nodeSizeSlider.getValue() / 100f != model.getNodeLabelSize()) {
            nodeSizeSlider.setValue((int) (model.getNodeLabelSize() * 100f));
        }

        // Edge
        edgeFontButton.setText(model.getEdgeLabelFont().getFontName() + ", " + model.getEdgeLabelFont().getSize());
        ((JColorButton) edgeColorButton).setColor(model.getEdgeLabelColor());
        if (showEdgeLabelsCheckbox.isSelected() != model.isShowEdgeLabels()) {
            showEdgeLabelsCheckbox.setSelected(model.isShowEdgeLabels());
        }
        if (edgeSizeSlider.getValue() / 100f != model.getEdgeLabelSize()) {
            edgeSizeSlider.setValue((int) (model.getEdgeLabelSize() * 100f));
        }

        // General
        if (hideNonSelectedCheckbox.isSelected() != model.isHideNonSelectedLabels()) {
            hideNonSelectedCheckbox.setSelected(model.isHideNonSelectedLabels());
        }
        if (sizeModeCombo.getSelectedItem() != model.getNodeLabelSizeMode()) {
            sizeModeCombo.setSelectedItem(model.getNodeLabelSizeMode());
        }
        if (colorModeCombo.getSelectedItem() != model.getNodeLabelColorMode()) {
            colorModeCombo.setSelectedItem(model.getNodeLabelColorMode());
        }
    }

    public void setEnable(boolean enable) {
        showEdgeLabelsCheckbox.setEnabled(enable);
        showNodeLabelsCheckbox.setEnabled(enable);
        sizeModeCombo.setEnabled(enable);
        colorModeCombo.setEnabled(enable);
        hideNonSelectedCheckbox.setEnabled(enable);
        labelColorMode.setEnabled(enable);
        labelSizeMode.setEnabled(enable);
        configureLabelsButton.setEnabled(enable);

        boolean edgeValue =showEdgeLabelsCheckbox.isSelected();
        edgeFontButton.setEnabled(enable && edgeValue);
        edgeColorButton.setEnabled(enable && edgeValue);
        edgeSizeSlider.setEnabled(enable && edgeValue);
        labelEdgeColor.setEnabled(enable && edgeValue);
        labelEdgeFont.setEnabled(enable && edgeValue);
        labelEdgeSize.setEnabled(enable && edgeValue);
        boolean nodeValue = showNodeLabelsCheckbox.isSelected();
        nodeFontButton.setEnabled(enable && nodeValue);
        nodeColorButton.setEnabled(enable && nodeValue);
        nodeSizeSlider.setEnabled(enable && nodeValue);
        labelNodeColor.setEnabled(enable && nodeValue);
        labelNodeFont.setEnabled(enable && nodeValue);
        labelNodeSize.setEnabled(enable && nodeValue);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        nodePanel = new javax.swing.JPanel();
        showNodeLabelsCheckbox = new javax.swing.JCheckBox();
        labelNodeFont = new javax.swing.JLabel();
        nodeSizeSlider = new javax.swing.JSlider();
        labelNodeColor = new javax.swing.JLabel();
        nodeColorButton = new JColorButton(Color.BLACK);
        labelNodeSize = new javax.swing.JLabel();
        nodeFontButton = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        edgePanel = new javax.swing.JPanel();
        showEdgeLabelsCheckbox = new javax.swing.JCheckBox();
        labelEdgeFont = new javax.swing.JLabel();
        edgeFontButton = new javax.swing.JButton();
        labelEdgeColor = new javax.swing.JLabel();
        edgeColorButton = new JColorButton(Color.BLACK);
        edgeSizeSlider = new javax.swing.JSlider();
        labelEdgeSize = new javax.swing.JLabel();
        jSeparator2 = new javax.swing.JSeparator();
        labelSizeMode = new javax.swing.JLabel();
        sizeModeCombo = new javax.swing.JComboBox();
        labelColorMode = new javax.swing.JLabel();
        colorModeCombo = new javax.swing.JComboBox();
        hideNonSelectedCheckbox = new javax.swing.JCheckBox();
        configureLabelsButton = new javax.swing.JButton();

        nodePanel.setOpaque(true);

        showNodeLabelsCheckbox.setText(org.openide.util.NbBundle
            .getMessage(LabelSettingsPanel.class, "LabelSettingsPanel.showNodeLabelsCheckbox.text")); // NOI18N
        showNodeLabelsCheckbox.setBorder(null);
        showNodeLabelsCheckbox.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        showNodeLabelsCheckbox.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        showNodeLabelsCheckbox.setMargin(new java.awt.Insets(2, 0, 2, 2));

        labelNodeFont.setText(org.openide.util.NbBundle
            .getMessage(LabelSettingsPanel.class, "LabelSettingsPanel.labelNodeFont.text")); // NOI18N
        labelNodeFont.setMaximumSize(new java.awt.Dimension(60, 15));

        labelNodeColor.setText(org.openide.util.NbBundle
            .getMessage(LabelSettingsPanel.class, "LabelSettingsPanel.labelNodeColor.text")); // NOI18N

        nodeColorButton.setText(org.openide.util.NbBundle
            .getMessage(LabelSettingsPanel.class, "LabelSettingsPanel.nodeColorButton.text")); // NOI18N
        nodeColorButton.setMargin(new java.awt.Insets(1, 0, 1, 0));

        labelNodeSize.setText(org.openide.util.NbBundle
            .getMessage(LabelSettingsPanel.class, "LabelSettingsPanel.labelNodeSize.text")); // NOI18N

        nodeFontButton.setText(org.openide.util.NbBundle
            .getMessage(LabelSettingsPanel.class, "LabelSettingsPanel.nodeFontButton.text")); // NOI18N

        javax.swing.GroupLayout nodePanelLayout = new javax.swing.GroupLayout(nodePanel);
        nodePanel.setLayout(nodePanelLayout);
        nodePanelLayout.setHorizontalGroup(
            nodePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(nodePanelLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(showNodeLabelsCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, 97,
                        javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(163, Short.MAX_VALUE))
                .addGroup(nodePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(nodePanelLayout.createSequentialGroup()
                        .addGap(27, 27, 27)
                        .addGroup(nodePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(nodePanelLayout.createSequentialGroup()
                                .addComponent(labelNodeSize)
                                .addGap(18, 18, 18)
                                .addComponent(nodeSizeSlider, 0, 0, Short.MAX_VALUE))
                            .addGroup(nodePanelLayout.createSequentialGroup()
                                .addComponent(labelNodeFont, javax.swing.GroupLayout.PREFERRED_SIZE, 32,
                                    javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(nodeFontButton)
                                .addGap(23, 23, 23)
                                .addComponent(labelNodeColor)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(nodeColorButton, javax.swing.GroupLayout.PREFERRED_SIZE, 26,
                                    javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addContainerGap()))
        );
        nodePanelLayout.setVerticalGroup(
            nodePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(nodePanelLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(showNodeLabelsCheckbox)
                    .addContainerGap(112, Short.MAX_VALUE))
                .addGroup(nodePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(nodePanelLayout.createSequentialGroup()
                        .addGap(31, 31, 31)
                        .addGroup(nodePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(labelNodeFont, javax.swing.GroupLayout.PREFERRED_SIZE,
                                javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(labelNodeColor)
                            .addComponent(nodeColorButton)
                            .addComponent(nodeFontButton))
                        .addGroup(nodePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(nodePanelLayout.createSequentialGroup()
                                .addGap(15, 15, 15)
                                .addComponent(labelNodeSize))
                            .addGroup(nodePanelLayout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(nodeSizeSlider, javax.swing.GroupLayout.PREFERRED_SIZE, 34,
                                    javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addContainerGap(50, Short.MAX_VALUE)))
        );

        jSeparator1.setOrientation(javax.swing.SwingConstants.VERTICAL);

        edgePanel.setOpaque(true);

        showEdgeLabelsCheckbox.setText(org.openide.util.NbBundle
            .getMessage(LabelSettingsPanel.class, "LabelSettingsPanel.showEdgeLabelsCheckbox.text")); // NOI18N
        showEdgeLabelsCheckbox.setBorder(null);
        showEdgeLabelsCheckbox.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        showEdgeLabelsCheckbox.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        showEdgeLabelsCheckbox.setMargin(new java.awt.Insets(2, 0, 2, 2));

        labelEdgeFont.setText(org.openide.util.NbBundle
            .getMessage(LabelSettingsPanel.class, "LabelSettingsPanel.labelEdgeFont.text")); // NOI18N
        labelEdgeFont.setMaximumSize(new java.awt.Dimension(60, 15));

        edgeFontButton.setText(org.openide.util.NbBundle
            .getMessage(LabelSettingsPanel.class, "LabelSettingsPanel.edgeFontButton.text")); // NOI18N

        labelEdgeColor.setText(org.openide.util.NbBundle
            .getMessage(LabelSettingsPanel.class, "LabelSettingsPanel.labelEdgeColor.text")); // NOI18N

        edgeColorButton.setText(org.openide.util.NbBundle
            .getMessage(LabelSettingsPanel.class, "LabelSettingsPanel.edgeColorButton.text")); // NOI18N
        edgeColorButton.setMargin(new java.awt.Insets(1, 0, 1, 0));

        labelEdgeSize.setText(org.openide.util.NbBundle
            .getMessage(LabelSettingsPanel.class, "LabelSettingsPanel.labelEdgeSize.text")); // NOI18N

        javax.swing.GroupLayout edgePanelLayout = new javax.swing.GroupLayout(edgePanel);
        edgePanel.setLayout(edgePanelLayout);
        edgePanelLayout.setHorizontalGroup(
            edgePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(edgePanelLayout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(edgePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(edgePanelLayout.createSequentialGroup()
                            .addGap(17, 17, 17)
                            .addGroup(
                                edgePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addGroup(edgePanelLayout.createSequentialGroup()
                                        .addComponent(labelEdgeSize)
                                        .addGap(18, 18, 18)
                                        .addComponent(edgeSizeSlider, 0, 0, Short.MAX_VALUE))
                                    .addGroup(edgePanelLayout.createSequentialGroup()
                                        .addComponent(labelEdgeFont, javax.swing.GroupLayout.PREFERRED_SIZE, 32,
                                            javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(edgeFontButton)
                                        .addGap(23, 23, 23)
                                        .addComponent(labelEdgeColor)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(edgeColorButton, javax.swing.GroupLayout.PREFERRED_SIZE, 26,
                                            javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addComponent(showEdgeLabelsCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, 97,
                            javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        edgePanelLayout.setVerticalGroup(
            edgePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(edgePanelLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(showEdgeLabelsCheckbox)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(edgePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(labelEdgeFont, javax.swing.GroupLayout.PREFERRED_SIZE,
                            javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(labelEdgeColor)
                        .addComponent(edgeColorButton)
                        .addComponent(edgeFontButton))
                    .addGroup(edgePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(edgePanelLayout.createSequentialGroup()
                            .addGap(15, 15, 15)
                            .addComponent(labelEdgeSize))
                        .addGroup(edgePanelLayout.createSequentialGroup()
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(edgeSizeSlider, javax.swing.GroupLayout.PREFERRED_SIZE, 34,
                                javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addContainerGap(51, Short.MAX_VALUE))
        );

        jSeparator2.setOrientation(javax.swing.SwingConstants.VERTICAL);

        labelSizeMode.setText(org.openide.util.NbBundle
            .getMessage(LabelSettingsPanel.class, "LabelSettingsPanel.labelSizeMode.text")); // NOI18N

        sizeModeCombo
            .setModel(new javax.swing.DefaultComboBoxModel(new String[] {"Item 1", "Item 2", "Item 3", "Item 4"}));

        labelColorMode.setText(org.openide.util.NbBundle
            .getMessage(LabelSettingsPanel.class, "LabelSettingsPanel.labelColorMode.text")); // NOI18N

        colorModeCombo
            .setModel(new javax.swing.DefaultComboBoxModel(new String[] {"Item 1", "Item 2", "Item 3", "Item 4"}));

        hideNonSelectedCheckbox.setText(org.openide.util.NbBundle
            .getMessage(LabelSettingsPanel.class, "LabelSettingsPanel.hideNonSelectedCheckbox.text")); // NOI18N
        hideNonSelectedCheckbox.setBorder(null);
        hideNonSelectedCheckbox.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        hideNonSelectedCheckbox.setMargin(new java.awt.Insets(2, 0, 2, 2));

        configureLabelsButton.setFont(new java.awt.Font("Tahoma", 0, 10));
        configureLabelsButton.setIcon(
            ImageUtilities.loadImageIcon("VisualizationImpl/configureLabels.svg", false)); // NOI18N
        configureLabelsButton.setText(org.openide.util.NbBundle
            .getMessage(LabelSettingsPanel.class, "LabelSettingsPanel.configureLabelsButton.text")); // NOI18N
        configureLabelsButton.setBorder(null);
        configureLabelsButton.setMargin(new java.awt.Insets(2, 7, 2, 7));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addComponent(nodePanel, javax.swing.GroupLayout.PREFERRED_SIZE,
                        javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE,
                        javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(edgePanel, javax.swing.GroupLayout.PREFERRED_SIZE,
                        javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE,
                        javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(18, 18, 18)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(hideNonSelectedCheckbox)
                        .addGroup(layout.createSequentialGroup()
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(labelColorMode)
                                .addComponent(labelSizeMode))
                            .addGap(10, 10, 10)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(colorModeCombo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(sizeModeCombo, 0, 91, Short.MAX_VALUE))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 31, Short.MAX_VALUE)
                            .addComponent(configureLabelsButton, javax.swing.GroupLayout.PREFERRED_SIZE, 93,
                                javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jSeparator1, javax.swing.GroupLayout.DEFAULT_SIZE, 136, Short.MAX_VALUE)
                .addComponent(nodePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE,
                    Short.MAX_VALUE)
                .addComponent(edgePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE,
                    Short.MAX_VALUE)
                .addComponent(jSeparator2, javax.swing.GroupLayout.DEFAULT_SIZE, 136, Short.MAX_VALUE)
                .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(sizeModeCombo, javax.swing.GroupLayout.PREFERRED_SIZE,
                            javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(labelSizeMode))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(colorModeCombo, javax.swing.GroupLayout.PREFERRED_SIZE,
                            javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(labelColorMode))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                    .addComponent(hideNonSelectedCheckbox)
                    .addContainerGap(44, Short.MAX_VALUE))
                .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(configureLabelsButton, javax.swing.GroupLayout.PREFERRED_SIZE, 46,
                        javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(84, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
}
