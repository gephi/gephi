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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import net.miginfocom.swing.MigLayout;
import org.gephi.desktop.preview.api.PreviewUIController;
import org.gephi.preview.api.ManagedRenderer;
import org.gephi.preview.api.PreviewController;
import org.gephi.preview.api.PreviewModel;
import org.gephi.preview.spi.Renderer;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;

/**
 * UI for managing preview renderers availability and execution order.
 *
 * @author Eduardo Ramos<eduramiba@gmail.com>
 */
public class RendererManager extends javax.swing.JPanel implements PropertyChangeListener {

    private ArrayList<RendererCheckBox> renderersList = new ArrayList<RendererCheckBox>();
    private PreviewController previewController;

    /**
     * Creates new form RendererManagerPanel
     */
    public RendererManager() {
        initComponents();
        previewController = Lookup.getDefault().lookup(PreviewController.class);
        Lookup.getDefault().lookup(PreviewUIController.class).addPropertyChangeListener(this);
        panel.setLayout(new MigLayout("", "[pref!]"));
        setup();
    }

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

    private void restoreRenderersList() {
        PreviewModel model = previewController.getModel();
        Set<Renderer> enabledRenderers = null;
        if (model != null && model.getManagedRenderers() != null) {
            enabledRenderers = new HashSet<Renderer>();
            enabledRenderers.addAll(Arrays.asList(model.getManagedEnabledRenderers()));
        }
        renderersList.clear();
        for (Renderer r : Lookup.getDefault().lookupAll(Renderer.class)) {
            renderersList.add(new RendererCheckBox(r, enabledRenderers == null || enabledRenderers.contains(r)));
        }
        updateModelManagedRenderers();
    }

    private void refresh() {
        panel.removeAll();
        loadModelManagedRenderers();
        for (int i = 0; i < renderersList.size(); i++) {
            panel.add(new MoveRendererWrapperButton(i, true));
            panel.add(new MoveRendererWrapperButton(i, false));
            panel.add(renderersList.get(i), "wrap");
        }
        panel.updateUI();
    }

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
            if (renderer instanceof Renderer.NamedRenderer) {
                setText("<html><font color=blue>" + ((Renderer.NamedRenderer) renderer).getName() + "</font> - " + renderer.getClass().getName() + "</html>");
            } else {
                setText(renderer.getClass().getName());
            }
        }

        public Renderer getRenderer() {
            return renderer;
        }

        public void actionPerformed(ActionEvent e) {
            updateModelManagedRenderers();
        }
    }

    class MoveRendererWrapperButton extends JButton implements ActionListener {

        private int index;
        private boolean up;

        public MoveRendererWrapperButton(int index, boolean up) {
            super(ImageUtilities.loadImageIcon("org/gephi/desktop/preview/resources/" + (up ? "up" : "down") + ".png", false));
            setMargin(new Insets(1, 1, 1, 1));//Small margin for icon-only buttons
            this.index = index;
            this.up = up;

            if (up) {
                setEnabled(index > 0);
            } else {
                setEnabled(index < renderersList.size() - 1);
            }
            addActionListener(this);
        }

        public void actionPerformed(ActionEvent e) {
            int newIndex = up ? index - 1 : index + 1;
            RendererCheckBox oldItem = renderersList.get(newIndex);
            RendererCheckBox item = renderersList.get(index);

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

        toolBar = new javax.swing.JToolBar();
        restoreOrderButton = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JToolBar.Separator();
        selectAllButton = new javax.swing.JButton();
        unselectAllButon = new javax.swing.JButton();
        scroll = new javax.swing.JScrollPane();
        panel = new javax.swing.JPanel();

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

        selectAllButton.setText(org.openide.util.NbBundle.getMessage(RendererManager.class, "RendererManager.selectAllButton.text")); // NOI18N
        selectAllButton.setFocusable(false);
        selectAllButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        selectAllButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        selectAllButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectAllButtonActionPerformed(evt);
            }
        });
        toolBar.add(selectAllButton);

        unselectAllButon.setText(org.openide.util.NbBundle.getMessage(RendererManager.class, "RendererManager.unselectAllButon.text")); // NOI18N
        unselectAllButon.setFocusable(false);
        unselectAllButon.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        unselectAllButon.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        unselectAllButon.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                unselectAllButonActionPerformed(evt);
            }
        });
        toolBar.add(unselectAllButon);

        javax.swing.GroupLayout panelLayout = new javax.swing.GroupLayout(panel);
        panel.setLayout(panelLayout);
        panelLayout.setHorizontalGroup(
            panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 398, Short.MAX_VALUE)
        );
        panelLayout.setVerticalGroup(
            panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 267, Short.MAX_VALUE)
        );

        scroll.setViewportView(panel);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(toolBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(scroll)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(toolBar, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(scroll))
        );
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
    private javax.swing.JToolBar.Separator jSeparator1;
    private javax.swing.JPanel panel;
    private javax.swing.JButton restoreOrderButton;
    private javax.swing.JScrollPane scroll;
    private javax.swing.JButton selectAllButton;
    private javax.swing.JToolBar toolBar;
    private javax.swing.JButton unselectAllButon;
    // End of variables declaration//GEN-END:variables
}
