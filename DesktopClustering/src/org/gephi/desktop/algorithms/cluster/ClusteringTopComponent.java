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
package org.gephi.desktop.algorithms.cluster;

import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.Serializable;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.gephi.algorithms.cluster.api.Clusterer;
import org.gephi.algorithms.cluster.api.ClustererBuilder;
import org.gephi.algorithms.cluster.api.ClustererUI;
import org.gephi.algorithms.cluster.api.ClusteringController;
import org.gephi.algorithms.cluster.api.ClusteringModel;
import org.gephi.project.api.ProjectController;
import org.gephi.workspace.api.Workspace;
import org.gephi.workspace.api.WorkspaceDataKey;
import org.gephi.workspace.api.WorkspaceListener;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
//import org.openide.util.Utilities;

final class ClusteringTopComponent extends TopComponent implements ChangeListener {

    private static ClusteringTopComponent instance;
    /** path to the icon used by the component and its open action */
//    static final String ICON_PATH = "SET/PATH/TO/ICON/HERE";
    private static final String PREFERRED_ID = "ClusteringTopComponent";

    //Const
    private final String NO_SELECTION;

    //Architecture
    private ClusteringModelImpl model;
    private ClustererBuilder[] builders;

    private ClusteringTopComponent() {
        NO_SELECTION = NbBundle.getMessage(ClusteringTopComponent.class, "ClusteringTopComponent.chooser.text");
        initComponents();
        setName(NbBundle.getMessage(ClusteringTopComponent.class, "CTL_ClusteringTopComponent"));
        setToolTipText(NbBundle.getMessage(ClusteringTopComponent.class, "HINT_ClusteringTopComponent"));
//        setIcon(Utilities.loadImage(ICON_PATH, true));

        final WorkspaceDataKey<ClusteringModel> key = Lookup.getDefault().lookup(ClusteringModelWorkspaceDataProvider.class).getWorkspaceDataKey();
        Lookup.getDefault().lookup(ProjectController.class).addWorkspaceListener(new WorkspaceListener() {

            public void initialize(Workspace workspace) {
            }

            public void select(Workspace workspace) {
                model = (ClusteringModelImpl) workspace.getWorkspaceData().getData(key);
                model.addChangeListener(ClusteringTopComponent.this);
                refreshModel();
            }

            public void unselect(Workspace workspace) {
                model.removeChangeListener(ClusteringTopComponent.this);
                model = null;
            }

            public void close(Workspace workspace) {
            }

            public void disable() {
                refreshModel();
            }
        });

        initChooser();
        refreshModel();
    }

    private void initChooser() {
        builders = Lookup.getDefault().lookupAll(ClustererBuilder.class).toArray(new ClustererBuilder[0]);
        DefaultComboBoxModel comboboxModel = new DefaultComboBoxModel();
        comboboxModel.addElement(NO_SELECTION);
        for (ClustererBuilder b : builders) {
            comboboxModel.addElement(b);
        }
        algorithmComboBox.setModel(comboboxModel);
        algorithmComboBox.setRenderer(new AlgorithmListCellRenderer());
        algorithmComboBox.addItemListener(new ItemListener() {

            public void itemStateChanged(ItemEvent e) {
                if (algorithmComboBox.getSelectedItem() == NO_SELECTION) {
                    if (model.getSelectedClusterer() != null) {
                        model.setSelectedClusterer(null);
                    }
                } else {
                    ClustererBuilder selectedBuilder = (ClustererBuilder) algorithmComboBox.getSelectedItem();
                    Clusterer savedData = getSavedClusterer(selectedBuilder);
                    if (savedData != null) {
                        model.setSelectedClusterer(savedData);
                    } else {
                        Clusterer newClusterer = selectedBuilder.getClusterer();
                        model.addClusterer(newClusterer);
                        model.setSelectedClusterer(newClusterer);
                    }
                }
            }
        });
    }

    private void refreshModel() {
        if (model == null) {
            algorithmComboBox.setEnabled(false);
            settingsButton.setEnabled(false);
            runButton.setEnabled(false);
            resetButton.setEnabled(false);
        } else {
            algorithmComboBox.setEnabled(true);
            if (model.getSelectedClusterer() == null) {
                settingsButton.setEnabled(false);
                runButton.setEnabled(false);
                resetButton.setEnabled(false);
                algorithmComboBox.setSelectedItem(NO_SELECTION);
            } else {
                ClustererBuilder selectedBuilder = getBuilder(model.getSelectedClusterer());
                if (selectedBuilder != algorithmComboBox.getSelectedItem()) {
                    algorithmComboBox.setSelectedItem(selectedBuilder);
                }

                if (model.isRunning()) {
                    algorithmComboBox.setEnabled(false);
                    settingsButton.setEnabled(false);
                    runButton.setEnabled(false);
                    resetButton.setEnabled(false);
                } else {
                    settingsButton.setEnabled(true);
                    runButton.setEnabled(true);
                    resetButton.setEnabled(true);
                    refreshResults();
                }
            }
        }
    }

    private void run() {
        Clusterer clusterer = model.getSelectedClusterer();
        ClusteringController controller = Lookup.getDefault().lookup(ClusteringController.class);
        controller.clusterize(clusterer);
    }

    private void settings() {
        ClustererBuilder builder = (ClustererBuilder) algorithmComboBox.getSelectedItem();
        ClustererUI clustererUI = builder.getUI();
        JPanel panel = clustererUI.getPanel();
        clustererUI.setup(model.getSelectedClusterer());
        DialogDescriptor dd = new DialogDescriptor(panel, NbBundle.getMessage(ClusteringTopComponent.class, "ClusteringTopComponent.settings.title", builder.getName()));
        if (DialogDisplayer.getDefault().notify(dd).equals(NotifyDescriptor.OK_OPTION)) {
            clustererUI.unsetup();
        }
    }

    private void reset() {
        model.removeClusterer(model.getSelectedClusterer());
        ClustererBuilder selectedBuilder = (ClustererBuilder) algorithmComboBox.getSelectedItem();
        Clusterer newClusterer = selectedBuilder.getClusterer();
        model.addClusterer(newClusterer);
        model.setSelectedClusterer(newClusterer);
    }

    private void refreshResults() {
        Clusterer clusterer = model.getSelectedClusterer();
        ClusterExplorer clusterExplorer = (ClusterExplorer) resultPanel;
        clusterExplorer.initExplorer(clusterer.getClusters());
    }

    public void stateChanged(ChangeEvent e) {
        refreshModel();
    }

    private Clusterer getSavedClusterer(ClustererBuilder builder) {
        for (Clusterer c : model.getClusterers()) {
            if (c.getClass().equals(builder.getClustererClass())) {
                return c;
            }
        }
        return null;
    }

    public ClustererBuilder getBuilder(Clusterer clusterer) {
        for (ClustererBuilder b : builders) {
            if (b.getClustererClass().equals(clusterer.getClass())) {
                return b;
            }
        }
        return null;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        algorithmComboBox = new javax.swing.JComboBox();
        settingsButton = new javax.swing.JButton();
        resultPanel = new ClusterExplorer();
        runButton = new javax.swing.JButton();
        resetButton = new javax.swing.JButton();

        algorithmComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        org.openide.awt.Mnemonics.setLocalizedText(settingsButton, org.openide.util.NbBundle.getMessage(ClusteringTopComponent.class, "ClusteringTopComponent.settingsButton.text")); // NOI18N
        settingsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                settingsButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(runButton, org.openide.util.NbBundle.getMessage(ClusteringTopComponent.class, "ClusteringTopComponent.runButton.text")); // NOI18N
        runButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                runButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(resetButton, org.openide.util.NbBundle.getMessage(ClusteringTopComponent.class, "ClusteringTopComponent.resetButton.text")); // NOI18N
        resetButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resetButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(algorithmComboBox, 0, 189, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(settingsButton))
            .addComponent(resultPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 276, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(146, 146, 146)
                .addComponent(resetButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(runButton))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(algorithmComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(settingsButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(resultPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 172, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(runButton)
                    .addComponent(resetButton)))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void settingsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_settingsButtonActionPerformed
        settings();
    }//GEN-LAST:event_settingsButtonActionPerformed

    private void runButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_runButtonActionPerformed
        run();
    }//GEN-LAST:event_runButtonActionPerformed

    private void resetButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetButtonActionPerformed
        reset();
    }//GEN-LAST:event_resetButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox algorithmComboBox;
    private javax.swing.JButton resetButton;
    private javax.swing.JPanel resultPanel;
    private javax.swing.JButton runButton;
    private javax.swing.JButton settingsButton;
    // End of variables declaration//GEN-END:variables

    /**
     * Gets default instance. Do not use directly: reserved for *.settings files only,
     * i.e. deserialization routines; otherwise you could get a non-deserialized instance.
     * To obtain the singleton instance, use {@link #findInstance}.
     */
    public static synchronized ClusteringTopComponent getDefault() {
        if (instance == null) {
            instance = new ClusteringTopComponent();
        }
        return instance;
    }

    /**
     * Obtain the ClusteringTopComponent instance. Never call {@link #getDefault} directly!
     */
    public static synchronized ClusteringTopComponent findInstance() {
        TopComponent win = WindowManager.getDefault().findTopComponent(PREFERRED_ID);
        if (win == null) {
            Logger.getLogger(ClusteringTopComponent.class.getName()).warning(
                    "Cannot find " + PREFERRED_ID + " component. It will not be located properly in the window system.");
            return getDefault();
        }
        if (win instanceof ClusteringTopComponent) {
            return (ClusteringTopComponent) win;
        }
        Logger.getLogger(ClusteringTopComponent.class.getName()).warning(
                "There seem to be multiple components with the '" + PREFERRED_ID +
                "' ID. That is a potential source of errors and unexpected behavior.");
        return getDefault();
    }

    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_ALWAYS;
    }

    @Override
    public void componentOpened() {
        // TODO add custom code on component opening
    }

    @Override
    public void componentClosed() {
        // TODO add custom code on component closing
    }

    /** replaces this in object stream */
    @Override
    public Object writeReplace() {
        return new ResolvableHelper();
    }

    @Override
    protected String preferredID() {
        return PREFERRED_ID;
    }

    final static class ResolvableHelper implements Serializable {

        private static final long serialVersionUID = 1L;

        public Object readResolve() {
            return ClusteringTopComponent.getDefault();
        }
    }

    private static class AlgorithmListCellRenderer extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof ClustererBuilder) {
                ClustererBuilder builder = (ClustererBuilder) value;
                setText(builder.getName());
            } else {
                setText((String) value);
            }
            return this;
        }
    }
}
