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
package org.gephi.desktop.layout;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import org.gephi.desktop.layout.LayoutPresetPersistence.Preset;
import org.gephi.layout.api.LayoutController;
import org.gephi.layout.api.LayoutModel;
import org.gephi.layout.spi.LayoutBuilder;
import org.gephi.layout.spi.LayoutUI;
import org.gephi.ui.components.richtooltip.RichTooltip;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.StatusDisplayer;
import org.openide.explorer.propertysheet.PropertySheet;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

public class LayoutPanel extends javax.swing.JPanel implements PropertyChangeListener {

    private final String NO_SELECTION;
    private LayoutModel model;
    private LayoutController controller;
    private LayoutPresetPersistence layoutPresetPersistence;

    public LayoutPanel() {
        NO_SELECTION = NbBundle.getMessage(LayoutPanel.class, "LayoutPanel.choose.text");
        controller = Lookup.getDefault().lookup(LayoutController.class);
        initComponents();
        layoutPresetPersistence = new LayoutPresetPersistence();
        initEvents();
    }

    private void initEvents() {
        layoutCombobox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (layoutCombobox.getSelectedItem().equals(NO_SELECTION) && model.getSelectedLayout() != null) {
                    setSelectedLayout(null);
                } else if (layoutCombobox.getSelectedItem() instanceof LayoutBuilderWrapper) {
                    LayoutBuilder builder = ((LayoutBuilderWrapper) layoutCombobox.getSelectedItem()).getLayoutBuilder();
                    if (model.getSelectedLayout() == null || model.getSelectedBuilder() != builder) {
                        setSelectedLayout(builder);
                    }
                }
            }
        });

        infoLabel.addMouseListener(new MouseAdapter() {
            RichTooltip richTooltip;

            @Override
            public void mouseEntered(MouseEvent e) {
                if (infoLabel.isEnabled() && model != null && model.getSelectedLayout() != null) {
                    richTooltip = buildTooltip(model.getSelectedBuilder());
                    richTooltip.showTooltip(infoLabel, e.getLocationOnScreen());
                }

            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (richTooltip != null) {
                    richTooltip.hideTooltip();
                    richTooltip = null;
                }
            }
        });



        presetsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JPopupMenu menu = new JPopupMenu();
                List<Preset> presets = layoutPresetPersistence.getPresets(model.getSelectedLayout());
                if (presets != null && !presets.isEmpty()) {
                    for (final Preset p : presets) {
                        JMenuItem item = new JMenuItem(p.toString());
                        item.addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                layoutPresetPersistence.loadPreset(p, model.getSelectedLayout());
                                refreshProperties();
                                StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(LayoutPanel.class, "LayoutPanel.status.loadPreset", model.getSelectedBuilder().getName(), p.toString()));
                            }
                        });
                        menu.add(item);
                    }
                } else {
                    menu.add("<html><i>" + NbBundle.getMessage(LayoutPanel.class, "LayoutPanel.presetsButton.nopreset") + "</i></html>");
                }

                JMenuItem saveItem = new JMenuItem(NbBundle.getMessage(LayoutPanel.class, "LayoutPanel.presetsButton.savePreset"));
                saveItem.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        String lastPresetName = NbPreferences.forModule(LayoutPanel.class).get("LayoutPanel.lastPresetName", "");
                        NotifyDescriptor.InputLine question = new NotifyDescriptor.InputLine(
                                NbBundle.getMessage(LayoutPanel.class, "LayoutPanel.presetsButton.savePreset.input"),
                                NbBundle.getMessage(LayoutPanel.class, "LayoutPanel.presetsButton.savePreset.input.name"));
                        question.setInputText(lastPresetName);
                        if (DialogDisplayer.getDefault().notify(question) == NotifyDescriptor.OK_OPTION) {
                            String input = question.getInputText();
                            if (input != null && !input.isEmpty()) {
                                layoutPresetPersistence.savePreset(input, model.getSelectedLayout());
                                StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(LayoutPanel.class, "LayoutPanel.status.savePreset", model.getSelectedBuilder().getName(), input));
                                NbPreferences.forModule(LayoutPanel.class).put("LayoutPanel.lastPresetName", input);
                            }
                        }
                    }
                });
                menu.add(new JSeparator());
                menu.add(saveItem);
                menu.show(layoutToolbar, 0, -menu.getPreferredSize().height);
            }
        });
    }

    public void refreshModel(LayoutModel layoutModel) {
        this.model = layoutModel;
        if (model != null) {
            model.addPropertyChangeListener(this);
        }

        refreshEnable();
        refreshModel();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(LayoutModel.SELECTED_LAYOUT)) {
            refreshModel();
        } else if (evt.getPropertyName().equals(LayoutModel.RUNNING)) {
            refreshModel();
        }
    }

    private void refreshModel() {
        refreshChooser();
        refreshProperties();

        if (model == null || !model.isRunning()) {
            runButton.setText(NbBundle.getMessage(LayoutPanel.class, "LayoutPanel.runButton.text"));
            runButton.setIcon(ImageUtilities.loadImageIcon("org/gephi/desktop/layout/resources/run.gif", false));
            runButton.setToolTipText(NbBundle.getMessage(LayoutPanel.class, "LayoutPanel.runButton.tooltip"));
        } else if (model.isRunning()) {
            runButton.setText(NbBundle.getMessage(LayoutPanel.class, "LayoutPanel.stopButton.text"));
            runButton.setIcon(ImageUtilities.loadImageIcon("org/gephi/desktop/layout/resources/stop.png", false));
            runButton.setToolTipText(NbBundle.getMessage(LayoutPanel.class, "LayoutPanel.stopButton.tooltip"));
        }

        boolean enabled = model != null && model.getSelectedLayout() != null;
        runButton.setEnabled(enabled);
        resetButton.setEnabled(enabled);
        infoLabel.setEnabled(enabled);
        propertySheet.setEnabled(enabled);
        presetsButton.setEnabled(enabled);
    }

    private void refreshChooser() {
        DefaultComboBoxModel comboBoxModel = new DefaultComboBoxModel();
        comboBoxModel.addElement(NO_SELECTION);
        comboBoxModel.setSelectedItem(NO_SELECTION);
        if (model != null) {
            List<LayoutBuilder> builders = new ArrayList<>(Lookup.getDefault().lookupAll(LayoutBuilder.class));
            Collections.sort(builders, new Comparator() {
                @Override
                public int compare(Object o1, Object o2) {
                    return ((LayoutBuilder) o1).getName().compareTo(((LayoutBuilder) o2).getName());
                }
            });
            for (LayoutBuilder builder : builders) {
                LayoutBuilderWrapper item = new LayoutBuilderWrapper(builder);
                comboBoxModel.addElement(item);
                if (model.getSelectedLayout() != null && builder == model.getSelectedBuilder()) {
                    comboBoxModel.setSelectedItem(item);
                }
            }
        }
        layoutCombobox.setModel(comboBoxModel);

        if (model != null) {
            layoutCombobox.setEnabled(!model.isRunning());
        }
    }

    private void refreshProperties() {
        if (model == null || model.getSelectedLayout() == null) {
            ((PropertySheet) propertySheet).setNodes(new Node[0]);
            layoutProvidedPanel.setVisible(false);
            propertySheet.setVisible(true);
            layoutProvidedPanel.removeAll();
        } else {
            LayoutNode layoutNode = new LayoutNode(model.getSelectedLayout());

            JPanel simplePanel = null;
            try {
                simplePanel = layoutNode.getLayout().getBuilder().getUI().getSimplePanel(layoutNode.getLayout());
            } catch (Exception ex) {
                // Tried and failed to getSimplePanel
            }

            if (null != simplePanel) {
                propertySheet.setVisible(false);
                layoutProvidedPanel.setVisible(true);
                layoutProvidedPanel.removeAll();
                layoutProvidedPanel.add(simplePanel);
            } else {
                layoutProvidedPanel.setVisible(false);
                propertySheet.setVisible(true);
                layoutProvidedPanel.removeAll();
            }

            ((PropertySheet) propertySheet).setNodes(new Node[]{layoutNode});
        }
    }

    private void refreshEnable() {
        boolean enabled = model != null;
        layoutCombobox.setEnabled(enabled);
        runButton.setEnabled(enabled);
        propertySheet.setEnabled(enabled);
        resetButton.setEnabled(enabled);
        presetsButton.setEnabled(enabled);
    }

    private void setSelectedLayout(LayoutBuilder builder) {
        controller.setLayout(builder != null ? model.getLayout(builder) : null);
    }

    private void reset() {
        if (model.getSelectedLayout() != null) {
            model.getSelectedLayout().resetPropertiesValues();
            refreshProperties();
        }
    }

    private void run() {
        controller.executeLayout();
    }

    private void stop() {
        controller.stopLayout();
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

        layoutCombobox = new javax.swing.JComboBox();
        infoLabel = new javax.swing.JLabel();
        runButton = new javax.swing.JButton();
        layoutToolbar = new javax.swing.JToolBar();
        presetsButton = new javax.swing.JButton();
        resetButton = new javax.swing.JButton();
        layoutProvidedPanel = new javax.swing.JPanel();
        propertySheet = new PropertySheet();

        setLayout(new java.awt.GridBagLayout());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipady = 10;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(layoutCombobox, gridBagConstraints);

        infoLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        infoLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/gephi/desktop/layout/resources/layoutInfo.png"))); // NOI18N
        infoLabel.setText(org.openide.util.NbBundle.getMessage(LayoutPanel.class, "LayoutPanel.infoLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 7, 0, 0);
        add(infoLabel, gridBagConstraints);

        runButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/gephi/desktop/layout/resources/run.gif"))); // NOI18N
        runButton.setText(org.openide.util.NbBundle.getMessage(LayoutPanel.class, "LayoutPanel.runButton.text")); // NOI18N
        runButton.setIconTextGap(5);
        runButton.setMargin(new java.awt.Insets(2, 7, 2, 14));
        runButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                runButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 5);
        add(runButton, gridBagConstraints);

        layoutToolbar.setFloatable(false);
        layoutToolbar.setRollover(true);
        layoutToolbar.setOpaque(false);

        presetsButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/gephi/desktop/layout/resources/preset.png"))); // NOI18N
        presetsButton.setText(org.openide.util.NbBundle.getMessage(LayoutPanel.class, "LayoutPanel.presetsButton.text")); // NOI18N
        presetsButton.setFocusable(false);
        presetsButton.setIconTextGap(0);
        layoutToolbar.add(presetsButton);

        resetButton.setText(org.openide.util.NbBundle.getMessage(LayoutPanel.class, "LayoutPanel.resetButton.text")); // NOI18N
        resetButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resetButtonActionPerformed(evt);
            }
        });
        layoutToolbar.add(resetButton);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        add(layoutToolbar, gridBagConstraints);

        layoutProvidedPanel.setOpaque(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(layoutProvidedPanel, gridBagConstraints);

        propertySheet.setOpaque(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(propertySheet, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void resetButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetButtonActionPerformed
        reset();
    }//GEN-LAST:event_resetButtonActionPerformed

    private void runButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_runButtonActionPerformed
        if (model.isRunning()) {
            stop();
        } else {
            run();
        }
    }//GEN-LAST:event_runButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel infoLabel;
    private javax.swing.JComboBox layoutCombobox;
    private javax.swing.JPanel layoutProvidedPanel;
    private javax.swing.JToolBar layoutToolbar;
    private javax.swing.JButton presetsButton;
    private javax.swing.JPanel propertySheet;
    private javax.swing.JButton resetButton;
    private javax.swing.JButton runButton;
    // End of variables declaration//GEN-END:variables

    private static class LayoutBuilderWrapper {

        private LayoutBuilder layoutBuilder;

        public LayoutBuilderWrapper(LayoutBuilder layoutBuilder) {
            this.layoutBuilder = layoutBuilder;
        }

        public LayoutBuilder getLayoutBuilder() {
            return layoutBuilder;
        }

        @Override
        public String toString() {
            return layoutBuilder.getName();
        }
    }

    private RichTooltip buildTooltip(LayoutBuilder builder) {
        String description = "";
        LayoutUI layoutUI = null;
        try {
            layoutUI = builder.getUI();
            if (layoutUI.getDescription() != null) {
                description = layoutUI.getDescription();
            }
            if (layoutUI.getQualityRank() < 0 || layoutUI.getSpeedRank() < 0) {
                layoutUI = null;
            }
        } catch (Exception e) {
            layoutUI = null;
        }

        RichTooltip richTooltip = new RichTooltip(builder.getName(), description);
        if (layoutUI != null) {
            LayoutDescriptionImage layoutDescriptionImage = new LayoutDescriptionImage(layoutUI);
            richTooltip.setMainImage(layoutDescriptionImage.getImage());
        }
        return richTooltip;
    }

    private static class LayoutDescriptionImage {

        private static final int STAR_WIDTH = 16;
        private static final int STAR_HEIGHT = 16;
        private static final int STAR_MAX = 5;
        private static final int TEXT_GAP = 5;
        private static final int LINE_GAP = 4;
        private static final int Y_BEGIN = 10;
        private static final int IMAGE_RIGHT_MARIN = 10;
        private Image greenIcon;
        private Image grayIcon;
        private Graphics g;
        private String qualityStr;
        private String speedStr;
        private int textMaxSize;
        private LayoutUI layoutUI;

        public LayoutDescriptionImage(LayoutUI layoutUI) {
            this.layoutUI = layoutUI;
            greenIcon = ImageUtilities.loadImage("org/gephi/desktop/layout/resources/yellow.png");
            grayIcon = ImageUtilities.loadImage("org/gephi/desktop/layout/resources/grey.png");
            qualityStr = NbBundle.getMessage(LayoutPanel.class, "LayoutPanel.tooltip.quality");
            speedStr = NbBundle.getMessage(LayoutPanel.class, "LayoutPanel.tooltip.speed");
        }

        public void paint(Graphics g) {
            g.setColor(Color.BLACK);
            g.drawString(qualityStr, 0, STAR_HEIGHT + Y_BEGIN - 2);
            paintStarPanel(g, textMaxSize + TEXT_GAP, Y_BEGIN, STAR_MAX, layoutUI.getQualityRank());
            g.drawString(speedStr, 0, STAR_HEIGHT * 2 + LINE_GAP + Y_BEGIN - 2);
            paintStarPanel(g, textMaxSize + TEXT_GAP, STAR_HEIGHT + LINE_GAP + Y_BEGIN, STAR_MAX, layoutUI.getSpeedRank());
        }

        public Image getImage() {
            //Image size
            BufferedImage im = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
            textMaxSize = 0;
            textMaxSize = Math.max(im.getGraphics().getFontMetrics().stringWidth(qualityStr), textMaxSize);
            textMaxSize = Math.max(im.getGraphics().getFontMetrics().stringWidth(speedStr), textMaxSize);
            int imageWidth = STAR_MAX * STAR_WIDTH + TEXT_GAP + textMaxSize + IMAGE_RIGHT_MARIN;

            //Paint
            BufferedImage img = new BufferedImage(imageWidth, 100, BufferedImage.TYPE_INT_ARGB);
            this.g = img.getGraphics();
            paint(g);
            return img;
        }

        public void paintStarPanel(Graphics g, int x, int y, int max, int value) {
            for (int i = 0; i < max; i++) {
                if (i < value) {
                    g.drawImage(greenIcon, x + i * 16, y, null);
                } else {
                    g.drawImage(grayIcon, x + i * 16, y, null);
                }
            }
        }
    }
}
