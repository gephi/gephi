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
package org.gephi.ui.layout;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.DefaultComboBoxModel;
import org.gephi.layout.api.LayoutBuilder;
import org.gephi.layout.api.LayoutController;
import org.gephi.layout.api.LayoutModel;
import org.gephi.layout.api.LayoutUI;
import org.gephi.ui.components.JPopupButton;
import org.gephi.ui.components.richtooltip.RichTooltip;
import org.openide.explorer.propertysheet.PropertySheet;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

public class LayoutPanel extends javax.swing.JPanel implements PropertyChangeListener {

    private final String NO_SELECTION;
    private LayoutModel model;
    private PropertySheet propertySheet;
    private LayoutController controller;

    public LayoutPanel() {
        NO_SELECTION = NbBundle.getMessage(LayoutPanel.class, "LayoutPanel.choose.text");
        controller = Lookup.getDefault().lookup(LayoutController.class);
        initComponents();
        propertySheet = new PropertySheet();
        propertiesScrollPane.setViewportView(propertySheet);
        initEvents();
    }

    private void initEvents() {
        layoutCombobox.addItemListener(new ItemListener() {

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
                    richTooltip.showTooltip(infoLabel);
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

        ((JPopupButton)loadPresetButton).addItem("test", null);
    }

    public void refreshModel(LayoutModel layoutModel) {
        this.model = layoutModel;
        if (model != null) {
            model.addPropertyChangeListener(this);
        }

        refreshEnable();
        refreshModel();
    }

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
            runButton.setIcon(ImageUtilities.loadImageIcon("org/gephi/ui/layout/resources/run.gif", false));
        } else if (model.isRunning()) {
            runButton.setText(NbBundle.getMessage(LayoutPanel.class, "LayoutPanel.stopButton.text"));
            runButton.setIcon(ImageUtilities.loadImageIcon("org/gephi/ui/layout/resources/stop.png", false));
        }

        boolean enabled = model != null && model.getSelectedLayout() != null;
        runButton.setEnabled(enabled);
        resetButton.setEnabled(enabled);
        infoLabel.setEnabled(enabled);
        propertySheet.setEnabled(enabled);
        loadPresetButton.setEnabled(enabled);
        savePresetButton.setEnabled(enabled);
    }

    private void refreshChooser() {
        DefaultComboBoxModel comboBoxModel = new DefaultComboBoxModel();
        comboBoxModel.addElement(NO_SELECTION);
        comboBoxModel.setSelectedItem(NO_SELECTION);
        if (model != null) {
            for (LayoutBuilder builder : Lookup.getDefault().lookupAll(LayoutBuilder.class)) {
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
            propertySheet.setNodes(new Node[0]);
        } else {
            LayoutNode layoutNode = new LayoutNode(model.getSelectedLayout());
            propertySheet.setNodes(new Node[]{layoutNode});
        }
    }

    private void refreshEnable() {
        boolean enabled = model != null;
        layoutCombobox.setEnabled(enabled);
        runButton.setEnabled(enabled);
        propertySheet.setEnabled(enabled);
        resetButton.setEnabled(enabled);
        loadPresetButton.setEnabled(enabled);
        savePresetButton.setEnabled(enabled);
    }

    private void setSelectedLayout(LayoutBuilder builder) {
        controller.setLayout(model.getLayout(builder));
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

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        layoutCombobox = new javax.swing.JComboBox();
        infoLabel = new javax.swing.JLabel();
        runButton = new javax.swing.JButton();
        propertiesScrollPane = new javax.swing.JScrollPane();
        layoutToolbar = new javax.swing.JToolBar();
        resetButton = new javax.swing.JButton();
        savePresetButton = new javax.swing.JButton();
        loadPresetButton = new JPopupButton();

        setLayout(new java.awt.GridBagLayout());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipady = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(layoutCombobox, gridBagConstraints);

        infoLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        infoLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/gephi/ui/layout/resources/layoutInfo.png"))); // NOI18N
        infoLabel.setText(org.openide.util.NbBundle.getMessage(LayoutPanel.class, "LayoutPanel.infoLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 7, 0, 0);
        add(infoLabel, gridBagConstraints);

        runButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/gephi/ui/layout/resources/run.gif"))); // NOI18N
        runButton.setText(org.openide.util.NbBundle.getMessage(LayoutPanel.class, "LayoutPanel.runButton.text")); // NOI18N
        runButton.setIconTextGap(5);
        runButton.setMargin(new java.awt.Insets(2, 7, 2, 14));
        runButton.addActionListener(new java.awt.event.ActionListener() {
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

        propertiesScrollPane.setBorder(null);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(propertiesScrollPane, gridBagConstraints);

        layoutToolbar.setFloatable(false);
        layoutToolbar.setRollover(true);

        resetButton.setText(org.openide.util.NbBundle.getMessage(LayoutPanel.class, "LayoutPanel.resetButton.text")); // NOI18N
        resetButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resetButtonActionPerformed(evt);
            }
        });
        layoutToolbar.add(resetButton);

        savePresetButton.setText(org.openide.util.NbBundle.getMessage(LayoutPanel.class, "LayoutPanel.savePresetButton.text")); // NOI18N
        savePresetButton.setFocusable(false);
        savePresetButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        savePresetButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        layoutToolbar.add(savePresetButton);

        loadPresetButton.setText(org.openide.util.NbBundle.getMessage(LayoutPanel.class, "LayoutPanel.loadPresetButton.text")); // NOI18N
        loadPresetButton.setFocusable(false);
        loadPresetButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        loadPresetButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        layoutToolbar.add(loadPresetButton);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        add(layoutToolbar, gridBagConstraints);
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
    private javax.swing.JToolBar layoutToolbar;
    private javax.swing.JButton loadPresetButton;
    private javax.swing.JScrollPane propertiesScrollPane;
    private javax.swing.JButton resetButton;
    private javax.swing.JButton runButton;
    private javax.swing.JButton savePresetButton;
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
            description = layoutUI.getDescription();
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
            greenIcon = ImageUtilities.loadImage("org/gephi/ui/layout/resources/yellow.png");
            grayIcon = ImageUtilities.loadImage("org/gephi/ui/layout/resources/grey.png");
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
