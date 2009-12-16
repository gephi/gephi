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

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import javax.swing.DefaultComboBoxModel;
import org.gephi.layout.api.Layout;
import org.gephi.layout.api.LayoutBuilder;
import org.gephi.layout.api.LayoutController;
import org.gephi.layout.api.LayoutModel;
import org.openide.explorer.propertysheet.PropertySheet;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
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
        } else if (model.isRunning()) {
            runButton.setText(NbBundle.getMessage(LayoutPanel.class, "LayoutPanel.stopButton.text"));
        }

        boolean enabled = model != null && model.getSelectedLayout() != null;
        runButton.setEnabled(enabled);
        resetButton.setEnabled(enabled);
        infoLabel.setEnabled(enabled);
        propertySheet.setEnabled(enabled);
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
            propertySheet.setNodes(new Node[]{new LayoutNode(model.getSelectedLayout())});
        }
    }

    private void refreshEnable() {
        boolean enabled = model != null;
        layoutCombobox.setEnabled(enabled);
        runButton.setEnabled(enabled);
        propertySheet.setEnabled(enabled);
        resetButton.setEnabled(enabled);
    }

    private void setSelectedLayout(LayoutBuilder builder) {
        controller.setLayout(model.getLayout(builder));
    }

    private void reset() {
        if (model.getSelectedLayout() != null) {
            model.getSelectedLayout().resetPropertiesValues();
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

        layoutCombobox = new javax.swing.JComboBox();
        infoLabel = new javax.swing.JLabel();
        runButton = new javax.swing.JButton();
        propertiesScrollPane = new javax.swing.JScrollPane();
        resetButton = new javax.swing.JButton();

        infoLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        infoLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/gephi/ui/layout/resources/layoutInfo.png"))); // NOI18N
        infoLabel.setText(org.openide.util.NbBundle.getMessage(LayoutPanel.class, "LayoutPanel.infoLabel.text")); // NOI18N

        runButton.setText(org.openide.util.NbBundle.getMessage(LayoutPanel.class, "LayoutPanel.runButton.text")); // NOI18N
        runButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                runButtonActionPerformed(evt);
            }
        });

        propertiesScrollPane.setBorder(null);

        resetButton.setText(org.openide.util.NbBundle.getMessage(LayoutPanel.class, "LayoutPanel.resetButton.text")); // NOI18N
        resetButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resetButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(propertiesScrollPane, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 246, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(infoLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 106, Short.MAX_VALUE)
                        .addComponent(resetButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(runButton))
                    .addComponent(layoutCombobox, javax.swing.GroupLayout.Alignment.TRAILING, 0, 246, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(layoutCombobox, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(11, 11, 11)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(infoLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(runButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(resetButton)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(propertiesScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 179, Short.MAX_VALUE)
                .addContainerGap())
        );
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
    private javax.swing.JScrollPane propertiesScrollPane;
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

    private static class LayoutChildren extends Children.Keys<Layout> {

        private ArrayList<Layout> layouts;

        public LayoutChildren() {
            layouts = new ArrayList<Layout>();
        }

        @Override
        protected void addNotify() {
            setKeys(layouts);
        }

        @Override
        protected void removeNotify() {
            setKeys(Collections.EMPTY_SET);
        }

        public void addLayout(Layout layout) {
            layouts.add(layout);
            setKeys(layouts);
        }

        public void removeLayout(Layout layout) {
            layouts.remove(layout);
            setKeys(layouts);
        }

        @Override
        protected Node[] createNodes(Layout layout) {
            return new Node[]{new LayoutNode(layout)};
        }
    }
}
