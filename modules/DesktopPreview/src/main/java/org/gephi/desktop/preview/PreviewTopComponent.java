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
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Field;
import javax.swing.BorderFactory;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import org.gephi.desktop.preview.api.PreviewUIController;
import org.gephi.desktop.preview.api.PreviewUIModel;
import org.gephi.preview.api.G2DTarget;
import org.gephi.preview.api.PreviewController;
import org.gephi.preview.api.PreviewModel;
import org.gephi.preview.api.PreviewProperty;
import org.gephi.preview.api.RenderTarget;
import org.gephi.ui.components.JColorButton;
import org.gephi.ui.utils.UIUtils;
import org.jdesktop.swingx.JXBusyLabel;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

/**
 *
 * @author Jérémy Subtil, Mathieu Bastian
 */
@ConvertAsProperties(dtd = "-//org.gephi.desktop.preview//Preview//EN",
        autostore = false)
@TopComponent.Description(preferredID = "PreviewTopComponent",
        iconBase = "org/gephi/desktop/preview/resources/preview.png",
        persistenceType = TopComponent.PERSISTENCE_ALWAYS)
@TopComponent.Registration(mode = "editor", openAtStartup = true, roles = {"preview"})
@ActionID(category = "Window", id = "org.gephi.desktop.preview.PreviewTopComponent")
@ActionReference(path = "Menu/Window", position = 900)
@TopComponent.OpenActionRegistration(displayName = "#CTL_PreviewTopComponent",
        preferredID = "PreviewTopComponent")
public final class PreviewTopComponent extends TopComponent implements PropertyChangeListener {

    //Data
    private transient PreviewUIModel model;
    private transient G2DTarget target;
    private transient PreviewSketch sketch;

    public PreviewTopComponent() {
        initComponents();
        setName(NbBundle.getMessage(PreviewTopComponent.class, "CTL_PreviewTopComponent"));

        if (UIUtils.isAquaLookAndFeel()) {
            previewPanel.setBackground(UIManager.getColor("NbExplorerView.background"));
        }
        if (UIUtils.isAquaLookAndFeel()) {
            southToolbar.setBackground(UIManager.getColor("NbExplorerView.background"));
        }

        bannerPanel.setVisible(false);

        //background color
        ((JColorButton) backgroundButton).addPropertyChangeListener(JColorButton.EVENT_COLOR, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                PreviewController previewController = Lookup.getDefault().lookup(PreviewController.class);
                previewController.getModel().getProperties().putValue(PreviewProperty.BACKGROUND_COLOR, (Color) evt.getNewValue());
                PreviewUIController previewUIController = Lookup.getDefault().lookup(PreviewUIController.class);
                previewUIController.refreshPreview();
            }
        });
        southBusyLabel.setVisible(false);
        resetZoomButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sketch.resetZoom();
            }
        });
        plusButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sketch.zoomPlus();
            }
        });
        minusButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sketch.zoomMinus();
            }
        });

        PreviewUIController controller = Lookup.getDefault().lookup(PreviewUIController.class);
        controller.addPropertyChangeListener(this);

        PreviewUIModel m = controller.getModel();
        if (m != null) {
            this.model = m;
            initTarget(model);
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(PreviewUIController.SELECT)) {
            this.model = (PreviewUIModel) evt.getNewValue();
            initTarget(model);
        } else if (evt.getPropertyName().equals(PreviewUIController.REFRESHED)) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    target.refresh();
                }
            });
        } else if (evt.getPropertyName().equals(PreviewUIController.REFRESHING)) {
            setRefresh((Boolean) evt.getNewValue());
        } else if (evt.getPropertyName().equals(PreviewUIController.GRAPH_CHANGED)) {
            if ((Boolean) evt.getNewValue()) {
                showBannerPanel();
            } else {
                hideBannerPanel();
            }
        }
    }

    public void setRefresh(final boolean refresh) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                CardLayout cl = (CardLayout) previewPanel.getLayout();
                cl.show(previewPanel, refresh ? "refreshCard" : "previewCard");
                ((JXBusyLabel) busyLabel).setBusy(refresh);
            }
        });
    }

    protected Dimension getSketchDimensions() {
        int width = sketchPanel.getWidth();
        int height = sketchPanel.getHeight();
        if (width > 1 && height > 1) {
            if (isRetina()) {
                width = (int) (width * 2.0);
                height = (int) (height * 2.0);
            }
            return new Dimension(width, height);
        }
        return new Dimension(1, 1);
    }

    public void initTarget(PreviewUIModel previewUIModel) {
        // inits the preview applet
        if (previewUIModel != null && target == null) {
            PreviewController previewController = Lookup.getDefault().lookup(PreviewController.class);
            PreviewModel previewModel = previewUIModel.getPreviewModel();

            Color background = previewModel.getProperties().getColorValue(PreviewProperty.BACKGROUND_COLOR);
            if (background != null) {
                setBackgroundColor(background);
            }

            Dimension dimensions = getSketchDimensions();
            previewModel.getProperties().putValue("width", (int) dimensions.getWidth());
            previewModel.getProperties().putValue("height", (int) dimensions.getHeight());

            target = (G2DTarget) previewController.getRenderTarget(RenderTarget.G2D_TARGET);
            if (target != null) {
                sketch = new PreviewSketch(target);
                sketchPanel.add(sketch, BorderLayout.CENTER);
            }
        } else if (previewUIModel == null) {
            sketchPanel.remove(sketch);
            target = null;
        }
    }

    /**
     * Shows the banner panel.
     */
    public void showBannerPanel() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                bannerPanel.setVisible(true);
            }
        });
    }

    public void setBackgroundColor(Color color) {
        ((JColorButton) backgroundButton).setColor(color);
    }

    /**
     * Hides the banner panel.
     */
    public void hideBannerPanel() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                bannerPanel.setVisible(false);
            }
        });
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        southBusyLabel = new JXBusyLabel(new Dimension(14,14));
        bannerPanel = new javax.swing.JPanel();
        bannerLabel = new javax.swing.JLabel();
        refreshButton = new javax.swing.JButton();
        previewPanel = new javax.swing.JPanel();
        sketchPanel = new javax.swing.JPanel();
        refreshPanel = new javax.swing.JPanel();
        busyLabel = new JXBusyLabel(new Dimension(20,20));
        southToolbar = new javax.swing.JToolBar();
        backgroundButton = new JColorButton(Color.WHITE);
        resetZoomButton = new javax.swing.JButton();
        minusButton = new javax.swing.JButton();
        plusButton = new javax.swing.JButton();

        setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        add(southBusyLabel, gridBagConstraints);

        bannerPanel.setBackground(new java.awt.Color(178, 223, 240));
        bannerPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.BLACK));
        bannerPanel.setLayout(new java.awt.GridBagLayout());

        bannerLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/gephi/desktop/preview/resources/info.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(bannerLabel, org.openide.util.NbBundle.getMessage(PreviewTopComponent.class, "PreviewTopComponent.bannerLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 5, 2, 5);
        bannerPanel.add(bannerLabel, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(refreshButton, org.openide.util.NbBundle.getMessage(PreviewTopComponent.class, "PreviewTopComponent.refreshButton.text")); // NOI18N
        refreshButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(1, 0, 1, 1);
        bannerPanel.add(refreshButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        add(bannerPanel, gridBagConstraints);

        previewPanel.setLayout(new java.awt.CardLayout());

        sketchPanel.setBackground(new java.awt.Color(255, 255, 255));
        sketchPanel.setPreferredSize(new java.awt.Dimension(500, 500));
        sketchPanel.setLayout(new java.awt.BorderLayout());
        previewPanel.add(sketchPanel, "previewCard");

        refreshPanel.setOpaque(false);
        refreshPanel.setLayout(new java.awt.GridBagLayout());

        busyLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        org.openide.awt.Mnemonics.setLocalizedText(busyLabel, org.openide.util.NbBundle.getMessage(PreviewTopComponent.class, "PreviewTopComponent.busyLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        refreshPanel.add(busyLabel, gridBagConstraints);

        previewPanel.add(refreshPanel, "refreshCard");

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(previewPanel, gridBagConstraints);

        southToolbar.setFloatable(false);
        southToolbar.setRollover(true);

        org.openide.awt.Mnemonics.setLocalizedText(backgroundButton, org.openide.util.NbBundle.getMessage(PreviewTopComponent.class, "PreviewTopComponent.backgroundButton.text")); // NOI18N
        backgroundButton.setFocusable(false);
        southToolbar.add(backgroundButton);

        org.openide.awt.Mnemonics.setLocalizedText(resetZoomButton, org.openide.util.NbBundle.getMessage(PreviewTopComponent.class, "PreviewTopComponent.resetZoomButton.text")); // NOI18N
        resetZoomButton.setFocusable(false);
        resetZoomButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        resetZoomButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        southToolbar.add(resetZoomButton);

        org.openide.awt.Mnemonics.setLocalizedText(minusButton, "-"); // NOI18N
        minusButton.setToolTipText(org.openide.util.NbBundle.getMessage(PreviewTopComponent.class, "PreviewTopComponent.minusButton.toolTipText")); // NOI18N
        minusButton.setFocusable(false);
        minusButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        minusButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        southToolbar.add(minusButton);

        org.openide.awt.Mnemonics.setLocalizedText(plusButton, "+"); // NOI18N
        plusButton.setToolTipText(org.openide.util.NbBundle.getMessage(PreviewTopComponent.class, "PreviewTopComponent.plusButton.toolTipText")); // NOI18N
        plusButton.setFocusable(false);
        plusButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        plusButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        southToolbar.add(plusButton);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        add(southToolbar, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void refreshButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshButtonActionPerformed
        Lookup.getDefault().lookup(PreviewUIController.class).refreshPreview();
    }//GEN-LAST:event_refreshButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton backgroundButton;
    private javax.swing.JLabel bannerLabel;
    private javax.swing.JPanel bannerPanel;
    private javax.swing.JLabel busyLabel;
    private javax.swing.JButton minusButton;
    private javax.swing.JButton plusButton;
    private javax.swing.JPanel previewPanel;
    private javax.swing.JButton refreshButton;
    private javax.swing.JPanel refreshPanel;
    private javax.swing.JButton resetZoomButton;
    private javax.swing.JPanel sketchPanel;
    private javax.swing.JLabel southBusyLabel;
    private javax.swing.JToolBar southToolbar;
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

    /**
     * Returns true if the default screen is in retina display (high dpi).
     *
     * @return true if retina, false otherwise
     */
    protected static boolean isRetina() {

        boolean isRetina = false;
        try {
            GraphicsDevice graphicsDevice = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
            Field field = graphicsDevice.getClass().getDeclaredField("scale");
            if (field != null) {
                field.setAccessible(true);
                Object scale = field.get(graphicsDevice);
                if (scale instanceof Integer && ((Integer) scale).intValue() == 2) {
                    isRetina = true;
                }
            }
        } catch (Exception e) {
            //Ignore
        }
        return isRetina;
    }
}
