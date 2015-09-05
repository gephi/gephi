/*
 Copyright 2008-2010 Gephi
 Authors : Eduardo Ramos
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

import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JToolBar;
import javax.swing.UIManager;
import net.miginfocom.swing.MigLayout;
import org.gephi.desktop.preview.api.PreviewUIController;
import org.gephi.preview.api.ManagedRenderer;
import org.gephi.preview.api.PreviewController;
import org.gephi.preview.api.PreviewModel;
import org.gephi.preview.spi.Renderer;
import org.gephi.ui.components.richtooltip.RichTooltip;
import org.gephi.ui.utils.UIUtils;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * UI for managing preview renderers enabled state and execution order.
 *
 * @author Eduardo Ramos
 */
public class RendererManager extends javax.swing.JPanel implements PropertyChangeListener {

    private ArrayList<RendererCheckBox> renderersList = new ArrayList<RendererCheckBox>();
    private PreviewController previewController;

    /**
     * Creates new form RendererManagerPanel
     */
    public RendererManager() {
        initComponents();
        buildTooltip();

        if (UIUtils.isAquaLookAndFeel()) {
            panel.setBackground(UIManager.getColor("NbExplorerView.background"));
        }
        if (UIUtils.isAquaLookAndFeel()) {
            toolBar.setBackground(UIManager.getColor("NbExplorerView.background"));
        }

        previewController = Lookup.getDefault().lookup(PreviewController.class);
        Lookup.getDefault().lookup(PreviewUIController.class).addPropertyChangeListener(this);
        panel.setLayout(new MigLayout("insets 3", "[pref!]"));
        setup();
    }

    private void buildTooltip() {
        final RichTooltip richTooltip = new RichTooltip();
        richTooltip.setTitle(NbBundle.getMessage(RendererManager.class, "PreviewSettingsTopComponent.rendererManagerTab"));
        richTooltip.addDescriptionSection(NbBundle.getMessage(RendererManager.class, "RendererManager.description1"));
        richTooltip.addDescriptionSection(NbBundle.getMessage(RendererManager.class, "RendererManager.description2"));
        infoLabel.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseEntered(MouseEvent e) {
                richTooltip.showTooltip(RendererManager.this, e.getLocationOnScreen());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                richTooltip.hideTooltip();
            }
        });
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(PreviewUIController.SELECT) || evt.getPropertyName().equals(PreviewUIController.UNSELECT)) {
            setup();
        }
    }

    private void setup() {
        PreviewModel model = previewController.getModel();
        setControlsEnabled(model != null);
        refresh();
    }

    /**
     * Restores the original order of the renderers list, preserving their enabled state.
     */
    private void restoreRenderersList() {
        PreviewModel model = previewController.getModel();
        Set<Renderer> enabledRenderers = null;
        if (model != null && model.getManagedRenderers() != null) {
            enabledRenderers = new HashSet<Renderer>();
            enabledRenderers.addAll(Arrays.asList(model.getManagedEnabledRenderers()));
        }
        renderersList.clear();
        for (Renderer r : previewController.getRegisteredRenderers()) {
            renderersList.add(new RendererCheckBox(r, enabledRenderers == null || enabledRenderers.contains(r)));
        }
        updateModelManagedRenderers();
    }

    private void refresh() {
        panel.removeAll();
        loadModelManagedRenderers();
        //Show renderers in inverse execution order to make it intuitive for users (last executed renderers remain on top of the image)
        for (int i = renderersList.size()-1; i >=0; i--) {
            JToolBar bar = new JToolBar();
            bar.setFloatable(false);
            if (UIUtils.isAquaLookAndFeel()) {
                bar.setBackground(UIManager.getColor("NbExplorerView.background"));
            }
            bar.add(new MoveRendererButton(i, true));
            bar.add(new MoveRendererButton(i, false));
            bar.add(renderersList.get(i));
            panel.add(bar, "wrap");
        }
        panel.updateUI();
    }

    /**
     * Obtain renderers enabled state and order from the preview model.
     */
    private void loadModelManagedRenderers() {
        renderersList.clear();
        PreviewModel model = previewController.getModel();
        if (model != null) {
            if (model.getManagedRenderers() != null) {
                for (ManagedRenderer mr : model.getManagedRenderers()) {
                    renderersList.add(new RendererCheckBox(mr.getRenderer(), mr.isEnabled()));
                }
            } else {
                restoreRenderersList();
            }
        }
    }

    /**
     * Sets current renderers enabled state and order to the preview model.
     */
    private void updateModelManagedRenderers() {
        PreviewModel model = previewController.getModel();
        if (model != null) {
            ArrayList<ManagedRenderer> managedRenderers = new ArrayList<ManagedRenderer>();
            for (RendererCheckBox rendererCheckBox : renderersList) {
                managedRenderers.add(new ManagedRenderer(rendererCheckBox.renderer, rendererCheckBox.isSelected()));
            }
            model.setManagedRenderers(managedRenderers.toArray(new ManagedRenderer[0]));
        }
    }

    private void setAllSelected(boolean selected) {
        for (RendererCheckBox rendererWrapper : renderersList) {
            rendererWrapper.setSelected(selected);
        }
        updateModelManagedRenderers();
    }

    private void setControlsEnabled(boolean enabled) {
        selectAllButton.setEnabled(enabled);
        unselectAllButon.setEnabled(enabled);
        restoreOrderButton.setEnabled(enabled);
    }

    class RendererCheckBox extends JCheckBox implements ActionListener {

        private Renderer renderer;

        public RendererCheckBox(Renderer renderer, boolean selected) {
            this.renderer = renderer;
            setSelected(selected);
            prepareName();
            addActionListener(this);
        }

        private void prepareName() {
            setText(renderer.getDisplayName());
            setToolTipText(renderer.getClass().getName());
        }

        public Renderer getRenderer() {
            return renderer;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            updateModelManagedRenderers();
        }
    }

    class MoveRendererButton extends JButton implements ActionListener {

        private int index;//Original index in renderers list
        private boolean up;//Move up or move down

        public MoveRendererButton(int index, boolean up) {
            super(ImageUtilities.loadImageIcon("org/gephi/desktop/preview/resources/" + (up ? "up" : "down") + ".png", false));
            setMargin(new Insets(1, 1, 1, 1));//Small margin for icon-only buttons
            this.index = index;
            this.up = up;

            if (up) {
                setEnabled(index < renderersList.size() - 1);
            } else {
                setEnabled(index > 0);
            }
            addActionListener(this);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            int newIndex = up ? index + 1 : index - 1;
            RendererCheckBox oldItem = renderersList.get(newIndex);
            RendererCheckBox item = renderersList.get(index);

            //Move and update UI
            renderersList.set(newIndex, item);
            renderersList.set(index, oldItem);
            updateModelManagedRenderers();
            refresh();
        }
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        toolBar = new javax.swing.JToolBar();
        restoreOrderButton = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JToolBar.Separator();
        selectAllButton = new javax.swing.JButton();
        unselectAllButon = new javax.swing.JButton();
        glue = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 0));
        infoLabel = new javax.swing.JLabel();
        fill = new javax.swing.Box.Filler(new java.awt.Dimension(5, 0), new java.awt.Dimension(5, 0), new java.awt.Dimension(5, 32767));
        scroll = new javax.swing.JScrollPane();
        panel = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());

        toolBar.setFloatable(false);
        toolBar.setRollover(true);

        restoreOrderButton.setText(org.openide.util.NbBundle.getMessage(RendererManager.class, "RendererManager.restoreOrderButton.text")); // NOI18N
        restoreOrderButton.setFocusable(false);
        restoreOrderButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        restoreOrderButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        restoreOrderButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                restoreOrderButtonActionPerformed(evt);
            }
        });
        toolBar.add(restoreOrderButton);
        toolBar.add(jSeparator1);

        selectAllButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/gephi/desktop/preview/resources/ui-check-box.png"))); // NOI18N
        selectAllButton.setText(org.openide.util.NbBundle.getMessage(RendererManager.class, "RendererManager.selectAllButton.text")); // NOI18N
        selectAllButton.setFocusable(false);
        selectAllButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        selectAllButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectAllButtonActionPerformed(evt);
            }
        });
        toolBar.add(selectAllButton);

        unselectAllButon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/gephi/desktop/preview/resources/ui-check-box-uncheck.png"))); // NOI18N
        unselectAllButon.setText(org.openide.util.NbBundle.getMessage(RendererManager.class, "RendererManager.unselectAllButon.text")); // NOI18N
        unselectAllButon.setFocusable(false);
        unselectAllButon.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        unselectAllButon.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                unselectAllButonActionPerformed(evt);
            }
        });
        toolBar.add(unselectAllButon);
        toolBar.add(glue);

        infoLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/gephi/desktop/preview/resources/info.png"))); // NOI18N
        infoLabel.setText(org.openide.util.NbBundle.getMessage(RendererManager.class, "RendererManager.infoLabel.text")); // NOI18N
        toolBar.add(infoLabel);
        toolBar.add(fill);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        add(toolBar, gridBagConstraints);

        scroll.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        javax.swing.GroupLayout panelLayout = new javax.swing.GroupLayout(panel);
        panel.setLayout(panelLayout);
        panelLayout.setHorizontalGroup(
            panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 398, Short.MAX_VALUE)
        );
        panelLayout.setVerticalGroup(
            panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 274, Short.MAX_VALUE)
        );

        scroll.setViewportView(panel);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(scroll, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void selectAllButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectAllButtonActionPerformed
        setAllSelected(true);
    }//GEN-LAST:event_selectAllButtonActionPerformed

    private void unselectAllButonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_unselectAllButonActionPerformed
        setAllSelected(false);
    }//GEN-LAST:event_unselectAllButonActionPerformed

    private void restoreOrderButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_restoreOrderButtonActionPerformed
        restoreRenderersList();
        refresh();
    }//GEN-LAST:event_restoreOrderButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.Box.Filler fill;
    private javax.swing.Box.Filler glue;
    private javax.swing.JLabel infoLabel;
    private javax.swing.JToolBar.Separator jSeparator1;
    private javax.swing.JPanel panel;
    private javax.swing.JButton restoreOrderButton;
    private javax.swing.JScrollPane scroll;
    private javax.swing.JButton selectAllButton;
    private javax.swing.JToolBar toolBar;
    private javax.swing.JButton unselectAllButon;
    // End of variables declaration//GEN-END:variables
}
