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
package org.gephi.desktop.clustering;

import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.gephi.clustering.api.Cluster;
import org.gephi.clustering.api.ClusteringController;
import org.gephi.clustering.spi.Clusterer;
import org.gephi.clustering.spi.ClustererBuilder;
import org.gephi.clustering.spi.ClustererUI;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.project.api.WorkspaceListener;
import org.gephi.ui.utils.UIUtils;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
//import org.openide.util.ImageUtilities;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.util.Lookup;

@ConvertAsProperties(dtd = "-//org.gephi.desktop.clustering//Clustering//EN",
autostore = false)
@TopComponent.Description(preferredID = "ClusteringTopComponent",
iconBase = "org/gephi/desktop/clustering/resources/small.png",
persistenceType = TopComponent.PERSISTENCE_ALWAYS)
@TopComponent.Registration(mode = "rankingmode", openAtStartup = false, roles = {"overview"})
@ActionID(category = "Window", id = "org.gephi.desktop.clustering.ClusteringTopComponent")
@ActionReference(path = "Menu/Window", position = 110)
@TopComponent.OpenActionRegistration(displayName = "#CTL_ClusteringTopComponent",
preferredID = "ClusteringTopComponent")
public final class ClusteringTopComponent extends TopComponent implements ChangeListener {

    //Const
    private final String NO_SELECTION;
    //Architecture
    private ClusteringModelImpl model;
    private ClustererBuilder[] builders;

    public ClusteringTopComponent() {
        NO_SELECTION = NbBundle.getMessage(ClusteringTopComponent.class, "ClusteringTopComponent.chooser.text");
        initComponents();
        setName(NbBundle.getMessage(ClusteringTopComponent.class, "CTL_ClusteringTopComponent"));

        putClientProperty(TopComponent.PROP_MAXIMIZATION_DISABLED, Boolean.TRUE);
        if (UIUtils.isAquaLookAndFeel()) {
            mainPanel.setBackground(UIManager.getColor("NbExplorerView.background"));
        }

        ClusteringController cc = Lookup.getDefault().lookup(ClusteringController.class);
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        pc.addWorkspaceListener(new WorkspaceListener() {

            public void initialize(Workspace workspace) {
                workspace.add(new ClusteringModelImpl());
            }

            public void select(Workspace workspace) {
                model = workspace.getLookup().lookup(ClusteringModelImpl.class);
                if (model == null) {
                    model = new ClusteringModelImpl();
                    workspace.add(model);
                }
                model.addChangeListener(ClusteringTopComponent.this);
                refreshModel();
            }

            public void unselect(Workspace workspace) {
                if (model != null) {
                    model.removeChangeListener(ClusteringTopComponent.this);
                }
                model = null;
            }

            public void close(Workspace workspace) {
            }

            public void disable() {
                refreshModel();
            }
        });
        if (pc.getCurrentWorkspace() != null) {
            model = pc.getCurrentWorkspace().getLookup().lookup(ClusteringModelImpl.class);
            if (model == null) {
                model = new ClusteringModelImpl();
                pc.getCurrentWorkspace().add(model);
            }
        }

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
            resetLink.setEnabled(false);
            descriptionLabel.setText("");
            runButton.setText(NbBundle.getMessage(ClusteringTopComponent.class, "ClusteringTopComponent.runButton.text"));
            runButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/gephi/desktop/clustering/resources/run.gif"))); // NOI18N
            refreshResults();
        } else {
            algorithmComboBox.setEnabled(true);
            if (model.getSelectedClusterer() == null) {
                settingsButton.setEnabled(false);
                runButton.setEnabled(false);
                resetLink.setEnabled(false);
                descriptionLabel.setText("");
                algorithmComboBox.setSelectedItem(NO_SELECTION);
                runButton.setText(NbBundle.getMessage(ClusteringTopComponent.class, "ClusteringTopComponent.runButton.text"));
                runButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/gephi/desktop/clustering/resources/run.gif"))); // NOI18N
                refreshResults();
            } else {
                ClustererBuilder selectedBuilder = getBuilder(model.getSelectedClusterer());
                if (selectedBuilder != algorithmComboBox.getSelectedItem()) {
                    algorithmComboBox.setSelectedItem(selectedBuilder);
                }

                descriptionLabel.setText(selectedBuilder.getDescription());

                if (model.isRunning()) {
                    algorithmComboBox.setEnabled(false);
                    settingsButton.setEnabled(false);
                    runButton.setEnabled(true);
                    resetLink.setEnabled(false);
                    runButton.setText(NbBundle.getMessage(ClusteringTopComponent.class, "ClusteringTopComponent.runButton.stop"));
                    runButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/gephi/desktop/clustering/resources/stop.png"))); // NOI18N
                } else {
                    settingsButton.setEnabled(true);
                    runButton.setEnabled(true);
                    resetLink.setEnabled(true);
                    runButton.setText(NbBundle.getMessage(ClusteringTopComponent.class, "ClusteringTopComponent.runButton.text"));
                    runButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/gephi/desktop/clustering/resources/run.gif"))); // NOI18N
                    refreshResults();
                }
            }
        }
    }

    private void run() {
        if (!model.isRunning()) {
            Clusterer clusterer = model.getSelectedClusterer();
            ClusteringController controller = Lookup.getDefault().lookup(ClusteringController.class);
            controller.clusterize(clusterer);
        } else {
            //stop
            Clusterer clusterer = model.getSelectedClusterer();
            ClusteringController controller = Lookup.getDefault().lookup(ClusteringController.class);
            controller.cancelClusterize(clusterer);
        }

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
        ClusterExplorer clusterExplorer = (ClusterExplorer) resultPanel;
        if (model == null || model.getSelectedClusterer() == null) {
            clusterExplorer.resetExplorer();
            return;
        }
        Clusterer clusterer = model.getSelectedClusterer();
        Cluster[] clusters = clusterer.getClusters();
        clusterExplorer.initExplorer(clusters);
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

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        mainPanel = new javax.swing.JPanel();
        controlToolbar = new javax.swing.JToolBar();
        settingsButton = new javax.swing.JButton();
        algorithmComboBox = new javax.swing.JComboBox();
        descriptionLabel = new org.jdesktop.swingx.JXLabel();
        resultPanel = new ClusterExplorer();
        runButton = new javax.swing.JButton();
        resetLink = new org.jdesktop.swingx.JXHyperlink();

        setLayout(new java.awt.BorderLayout());

        mainPanel.setLayout(new java.awt.GridBagLayout());

        controlToolbar.setBorder(null);
        controlToolbar.setFloatable(false);
        controlToolbar.setRollover(true);
        controlToolbar.setOpaque(false);

        org.openide.awt.Mnemonics.setLocalizedText(settingsButton, org.openide.util.NbBundle.getMessage(ClusteringTopComponent.class, "ClusteringTopComponent.settingsButton.text")); // NOI18N
        settingsButton.setFocusable(false);
        settingsButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        settingsButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        settingsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                settingsButtonActionPerformed(evt);
            }
        });
        controlToolbar.add(settingsButton);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 5);
        mainPanel.add(controlToolbar, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipady = 10;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 3, 5);
        mainPanel.add(algorithmComboBox, gridBagConstraints);

        descriptionLabel.setLineWrap(true);
        org.openide.awt.Mnemonics.setLocalizedText(descriptionLabel, org.openide.util.NbBundle.getMessage(ClusteringTopComponent.class, "ClusteringTopComponent.descriptionLabel.text")); // NOI18N
        descriptionLabel.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 5, 0, 5);
        mainPanel.add(descriptionLabel, gridBagConstraints);

        resultPanel.setOpaque(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        mainPanel.add(resultPanel, gridBagConstraints);

        runButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/gephi/desktop/clustering/resources/run.gif"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(runButton, org.openide.util.NbBundle.getMessage(ClusteringTopComponent.class, "ClusteringTopComponent.runButton.text")); // NOI18N
        runButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                runButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 5);
        mainPanel.add(runButton, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(resetLink, org.openide.util.NbBundle.getMessage(ClusteringTopComponent.class, "ClusteringTopComponent.resetLink.text")); // NOI18N
        resetLink.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resetLinkActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
        mainPanel.add(resetLink, gridBagConstraints);

        add(mainPanel, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void resetLinkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetLinkActionPerformed
        reset();
    }//GEN-LAST:event_resetLinkActionPerformed

    private void runButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_runButtonActionPerformed
        run();
    }//GEN-LAST:event_runButtonActionPerformed

    private void settingsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_settingsButtonActionPerformed
        settings();
    }//GEN-LAST:event_settingsButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox algorithmComboBox;
    private javax.swing.JToolBar controlToolbar;
    private org.jdesktop.swingx.JXLabel descriptionLabel;
    private javax.swing.JPanel mainPanel;
    private org.jdesktop.swingx.JXHyperlink resetLink;
    private javax.swing.JPanel resultPanel;
    private javax.swing.JButton runButton;
    private javax.swing.JButton settingsButton;
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
