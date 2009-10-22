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

import com.connectina.swing.fontchooser.JFontChooser;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.DefaultComboBoxModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.gephi.ui.components.JColorButton;
import org.gephi.visualization.VizController;
import org.gephi.visualization.VizModel;
import org.gephi.visualization.opengl.text.ColorMode;
import org.gephi.visualization.opengl.text.SizeMode;
import org.gephi.visualization.opengl.text.TextManager;
import org.gephi.visualization.opengl.text.TextModel;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;

/**
 *
 * @author Mathieu Bastian
 */
public class LabelSettingsPanel extends javax.swing.JPanel {

    /** Creates new form LabelSettingsPanel */
    public LabelSettingsPanel() {
        initComponents();
    }

    public void setup() {
        VizModel vizModel = VizController.getInstance().getVizModel();
        TextModel model = vizModel.getTextModel();
        vizModel.addPropertyChangeListener(new PropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName().equals("init")) {
                    refreshSharedConfig();
                }
            }
        });

        //NodePanel
        showNodeLabelsCheckbox.addItemListener(new ItemListener() {

            public void itemStateChanged(ItemEvent e) {
                boolean value = showNodeLabelsCheckbox.isSelected();
                TextModel model = VizController.getInstance().getVizModel().getTextModel();
                if (value != model.isShowNodeLabels()) {
                    model.setShowNodeLabels(value);
                    setEnable(true);
                }
            }
        });
        nodeFontButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                TextModel model = VizController.getInstance().getVizModel().getTextModel();
                Font font = JFontChooser.showDialog(WindowManager.getDefault().getMainWindow(), model.getNodeFont());
                if (font != null && font != model.getNodeFont()) {
                    model.setNodeFont(font);
                }
            }
        });
        ((JColorButton) nodeColorButton).addPropertyChangeListener(JColorButton.EVENT_COLOR, new PropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent evt) {
                TextModel model = VizController.getInstance().getVizModel().getTextModel();
                model.setNodeColor(((JColorButton) nodeColorButton).getColor());
            }
        });
        nodeSizeSlider.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                TextModel model = VizController.getInstance().getVizModel().getTextModel();
                model.setNodeSizeFactor(nodeSizeSlider.getValue() / 100f);
            }
        });

        //EdgePanel
        showEdgeLabelsCheckbox.addItemListener(new ItemListener() {

            public void itemStateChanged(ItemEvent e) {
                boolean value = showEdgeLabelsCheckbox.isSelected();
                TextModel model = VizController.getInstance().getVizModel().getTextModel();
                if (value != model.isShowEdgeLabels()) {
                    model.setShowEdgeLabels(value);
                    setEnable(true);
                }
            }
        });
        edgeFontButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                TextModel model = VizController.getInstance().getVizModel().getTextModel();
                Font font = JFontChooser.showDialog(WindowManager.getDefault().getMainWindow(), model.getEdgeFont());
                if (font != null && font != model.getEdgeFont()) {
                    model.setEdgeFont(font);
                }
            }
        });
        ((JColorButton) edgeColorButton).addPropertyChangeListener(JColorButton.EVENT_COLOR, new PropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent evt) {
                TextModel model = VizController.getInstance().getVizModel().getTextModel();
                model.setEdgeColor(((JColorButton) edgeColorButton).getColor());
            }
        });
        edgeSizeSlider.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                TextModel model = VizController.getInstance().getVizModel().getTextModel();
                model.setEdgeSizeFactor(edgeSizeSlider.getValue() / 100f);
            }
        });

        //General
        final TextManager textManager = VizController.getInstance().getTextManager();
        final DefaultComboBoxModel sizeModeModel = new DefaultComboBoxModel(textManager.getSizeModes());
        sizeModeCombo.setModel(sizeModeModel);
        final DefaultComboBoxModel colorModeModel = new DefaultComboBoxModel(textManager.getColorModes());
        colorModeCombo.setModel(colorModeModel);
        sizeModeCombo.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                TextModel model = VizController.getInstance().getVizModel().getTextModel();
                if (model.getSizeMode() != sizeModeModel.getSelectedItem()) {
                    model.setSizeMode((SizeMode) sizeModeModel.getSelectedItem());
                }
            }
        });
        colorModeCombo.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                TextModel model = VizController.getInstance().getVizModel().getTextModel();
                if (model.getColorMode() != colorModeModel.getSelectedItem()) {
                    model.setColorMode((ColorMode) colorModeModel.getSelectedItem());
                }
            }
        });
        hideNonSelectedCheckbox.addItemListener(new ItemListener() {

            public void itemStateChanged(ItemEvent e) {
                TextModel model = VizController.getInstance().getVizModel().getTextModel();
                if (model.isSelectedOnly() != hideNonSelectedCheckbox.isSelected()) {
                    model.setSelectedOnly(hideNonSelectedCheckbox.isSelected());
                }
            }
        });

        //Attributes
        configureLabelsButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                TextModel model = VizController.getInstance().getVizModel().getTextModel();
                LabelAttributesPanel panel = new LabelAttributesPanel();
                panel.setup(model);
                DialogDescriptor dd = new DialogDescriptor(panel, NbBundle.getMessage(VizBarController.class, "LabelAttributesPanel.title"), true, NotifyDescriptor.OK_CANCEL_OPTION, null, null);
                if (DialogDisplayer.getDefault().notify(dd).equals(NotifyDescriptor.OK_OPTION)) {
                    panel.unsetup();
                    return;
                }
            }
        });

        //Evt
        model.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                refreshSharedConfig();
            }
        });
        refreshSharedConfig();
    }

    private void refreshSharedConfig() {
        VizModel vizModel = VizController.getInstance().getVizModel();
        setEnable(!vizModel.isDefaultModel());
        if (vizModel.isDefaultModel()) {
            return;
        }
        TextModel model = vizModel.getTextModel();

        //node
        nodeFontButton.setText(model.getNodeFont().getFontName() + ", " + model.getNodeFont().getSize());
        ((JColorButton) nodeColorButton).setColor(model.getNodeColor());
        if (showNodeLabelsCheckbox.isSelected() != model.isShowNodeLabels()) {
            showNodeLabelsCheckbox.setSelected(model.isShowNodeLabels());
        }
        if (nodeSizeSlider.getValue() / 100f != model.getNodeSizeFactor()) {
            nodeSizeSlider.setValue((int) (model.getNodeSizeFactor() * 100f));
        }

        //edge
        edgeFontButton.setText(model.getEdgeFont().getFontName() + ", " + model.getEdgeFont().getSize());
        ((JColorButton) edgeColorButton).setColor(model.getEdgeColor());
        if (showEdgeLabelsCheckbox.isSelected() != model.isShowEdgeLabels()) {
            showEdgeLabelsCheckbox.setSelected(model.isShowEdgeLabels());
        }
        if (edgeSizeSlider.getValue() / 100f != model.getEdgeSizeFactor()) {
            edgeSizeSlider.setValue((int) (model.getEdgeSizeFactor() * 100f));
        }

        //general
        if (hideNonSelectedCheckbox.isSelected() != model.isSelectedOnly()) {
            hideNonSelectedCheckbox.setSelected(model.isSelectedOnly());
        }
        if (sizeModeCombo.getSelectedItem() != model.getSizeMode()) {
            sizeModeCombo.setSelectedItem(model.getSizeMode());
        }
        if (colorModeCombo.getSelectedItem() != model.getColorMode()) {
            colorModeCombo.setSelectedItem(model.getColorMode());
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

        TextModel model = VizController.getInstance().getVizModel().getTextModel();
        boolean edgeValue = model.isShowEdgeLabels();
        edgeFontButton.setEnabled(enable && edgeValue);
        edgeColorButton.setEnabled(enable && edgeValue);
        edgeSizeSlider.setEnabled(enable && edgeValue);
        labelEdgeColor.setEnabled(enable && edgeValue);
        labelEdgeFont.setEnabled(enable && edgeValue);
        labelEdgeSize.setEnabled(enable && edgeValue);
        boolean nodeValue = model.isShowNodeLabels();
        nodeFontButton.setEnabled(enable && nodeValue);
        nodeColorButton.setEnabled(enable && nodeValue);
        nodeSizeSlider.setEnabled(enable && nodeValue);
        labelNodeColor.setEnabled(enable && nodeValue);
        labelNodeFont.setEnabled(enable && nodeValue);
        labelNodeSize.setEnabled(enable && nodeValue);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
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

        showNodeLabelsCheckbox.setText(org.openide.util.NbBundle.getMessage(LabelSettingsPanel.class, "LabelSettingsPanel.showNodeLabelsCheckbox.text")); // NOI18N
        showNodeLabelsCheckbox.setBorder(null);
        showNodeLabelsCheckbox.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        showNodeLabelsCheckbox.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        showNodeLabelsCheckbox.setMargin(new java.awt.Insets(2, 0, 2, 2));

        labelNodeFont.setText(org.openide.util.NbBundle.getMessage(LabelSettingsPanel.class, "LabelSettingsPanel.labelNodeFont.text")); // NOI18N

        labelNodeColor.setText(org.openide.util.NbBundle.getMessage(LabelSettingsPanel.class, "LabelSettingsPanel.labelNodeColor.text")); // NOI18N

        nodeColorButton.setText(org.openide.util.NbBundle.getMessage(LabelSettingsPanel.class, "LabelSettingsPanel.nodeColorButton.text")); // NOI18N
        nodeColorButton.setMargin(new java.awt.Insets(1, 0, 1, 0));

        labelNodeSize.setText(org.openide.util.NbBundle.getMessage(LabelSettingsPanel.class, "LabelSettingsPanel.labelNodeSize.text")); // NOI18N

        nodeFontButton.setText(org.openide.util.NbBundle.getMessage(LabelSettingsPanel.class, "LabelSettingsPanel.nodeFontButton.text")); // NOI18N

        javax.swing.GroupLayout nodePanelLayout = new javax.swing.GroupLayout(nodePanel);
        nodePanel.setLayout(nodePanelLayout);
        nodePanelLayout.setHorizontalGroup(
            nodePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(nodePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(showNodeLabelsCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, 97, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(130, Short.MAX_VALUE))
            .addGroup(nodePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(nodePanelLayout.createSequentialGroup()
                    .addGap(27, 27, 27)
                    .addGroup(nodePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addGroup(nodePanelLayout.createSequentialGroup()
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(labelNodeSize)
                            .addGap(18, 18, 18)
                            .addComponent(nodeSizeSlider, 0, 0, Short.MAX_VALUE))
                        .addGroup(nodePanelLayout.createSequentialGroup()
                            .addComponent(labelNodeFont, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(9, 9, 9)
                            .addComponent(nodeFontButton)
                            .addGap(23, 23, 23)
                            .addComponent(labelNodeColor)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(nodeColorButton, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addContainerGap()))
        );
        nodePanelLayout.setVerticalGroup(
            nodePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(nodePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(showNodeLabelsCheckbox)
                .addContainerGap(80, Short.MAX_VALUE))
            .addGroup(nodePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(nodePanelLayout.createSequentialGroup()
                    .addGap(31, 31, 31)
                    .addGroup(nodePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(labelNodeFont)
                        .addComponent(labelNodeColor)
                        .addComponent(nodeColorButton)
                        .addComponent(nodeFontButton))
                    .addGroup(nodePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(nodePanelLayout.createSequentialGroup()
                            .addGap(15, 15, 15)
                            .addComponent(labelNodeSize))
                        .addGroup(nodePanelLayout.createSequentialGroup()
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(nodeSizeSlider, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addContainerGap(12, Short.MAX_VALUE)))
        );

        jSeparator1.setOrientation(javax.swing.SwingConstants.VERTICAL);

        showEdgeLabelsCheckbox.setText(org.openide.util.NbBundle.getMessage(LabelSettingsPanel.class, "LabelSettingsPanel.showEdgeLabelsCheckbox.text")); // NOI18N
        showEdgeLabelsCheckbox.setBorder(null);
        showEdgeLabelsCheckbox.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        showEdgeLabelsCheckbox.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        showEdgeLabelsCheckbox.setMargin(new java.awt.Insets(2, 0, 2, 2));

        labelEdgeFont.setText(org.openide.util.NbBundle.getMessage(LabelSettingsPanel.class, "LabelSettingsPanel.labelEdgeFont.text")); // NOI18N

        edgeFontButton.setText(org.openide.util.NbBundle.getMessage(LabelSettingsPanel.class, "LabelSettingsPanel.edgeFontButton.text")); // NOI18N

        labelEdgeColor.setText(org.openide.util.NbBundle.getMessage(LabelSettingsPanel.class, "LabelSettingsPanel.labelEdgeColor.text")); // NOI18N

        edgeColorButton.setText(org.openide.util.NbBundle.getMessage(LabelSettingsPanel.class, "LabelSettingsPanel.edgeColorButton.text")); // NOI18N
        edgeColorButton.setMargin(new java.awt.Insets(1, 0, 1, 0));

        labelEdgeSize.setText(org.openide.util.NbBundle.getMessage(LabelSettingsPanel.class, "LabelSettingsPanel.labelEdgeSize.text")); // NOI18N

        javax.swing.GroupLayout edgePanelLayout = new javax.swing.GroupLayout(edgePanel);
        edgePanel.setLayout(edgePanelLayout);
        edgePanelLayout.setHorizontalGroup(
            edgePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(edgePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(edgePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(edgePanelLayout.createSequentialGroup()
                        .addGap(17, 17, 17)
                        .addGroup(edgePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(edgePanelLayout.createSequentialGroup()
                                .addComponent(labelEdgeSize)
                                .addGap(18, 18, 18)
                                .addComponent(edgeSizeSlider, 0, 0, Short.MAX_VALUE))
                            .addGroup(edgePanelLayout.createSequentialGroup()
                                .addComponent(labelEdgeFont, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(9, 9, 9)
                                .addComponent(edgeFontButton)
                                .addGap(23, 23, 23)
                                .addComponent(labelEdgeColor)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(edgeColorButton, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addComponent(showEdgeLabelsCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, 97, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        edgePanelLayout.setVerticalGroup(
            edgePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(edgePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(showEdgeLabelsCheckbox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(edgePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelEdgeFont)
                    .addComponent(labelEdgeColor)
                    .addComponent(edgeColorButton)
                    .addComponent(edgeFontButton))
                .addGroup(edgePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(edgePanelLayout.createSequentialGroup()
                        .addGap(15, 15, 15)
                        .addComponent(labelEdgeSize))
                    .addGroup(edgePanelLayout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(edgeSizeSlider, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jSeparator2.setOrientation(javax.swing.SwingConstants.VERTICAL);

        labelSizeMode.setText(org.openide.util.NbBundle.getMessage(LabelSettingsPanel.class, "LabelSettingsPanel.labelSizeMode.text")); // NOI18N

        sizeModeCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        labelColorMode.setText(org.openide.util.NbBundle.getMessage(LabelSettingsPanel.class, "LabelSettingsPanel.labelColorMode.text")); // NOI18N

        colorModeCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        hideNonSelectedCheckbox.setText(org.openide.util.NbBundle.getMessage(LabelSettingsPanel.class, "LabelSettingsPanel.hideNonSelectedCheckbox.text")); // NOI18N
        hideNonSelectedCheckbox.setBorder(null);
        hideNonSelectedCheckbox.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        hideNonSelectedCheckbox.setMargin(new java.awt.Insets(2, 0, 2, 2));

        configureLabelsButton.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
        configureLabelsButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/gephi/visualization/component/configureLabels.png"))); // NOI18N
        configureLabelsButton.setText(org.openide.util.NbBundle.getMessage(LabelSettingsPanel.class, "LabelSettingsPanel.configureLabelsButton.text")); // NOI18N
        configureLabelsButton.setBorder(null);
        configureLabelsButton.setMargin(new java.awt.Insets(2, 7, 2, 7));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(nodePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(edgePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
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
                        .addComponent(configureLabelsButton, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSeparator1, javax.swing.GroupLayout.DEFAULT_SIZE, 106, Short.MAX_VALUE)
            .addComponent(nodePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(edgePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jSeparator2, javax.swing.GroupLayout.DEFAULT_SIZE, 106, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(sizeModeCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(labelSizeMode))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(colorModeCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(labelColorMode))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(hideNonSelectedCheckbox)
                .addContainerGap(23, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(configureLabelsButton, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(49, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox colorModeCombo;
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
    private javax.swing.JComboBox sizeModeCombo;
    // End of variables declaration//GEN-END:variables
}
