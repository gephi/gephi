/*
 Copyright 2008-2010 Gephi
 Authors : Mathieu Bastian <mathieu.bastian@gephi.org>, 
 Patick J. McSweeney <pjmcswee@syr.edu>
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
package org.gephi.desktop.statistics;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.gephi.desktop.statistics.api.StatisticsControllerUI;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.project.api.WorkspaceListener;
import org.gephi.ui.utils.UIUtils;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

/**
 *
 * @author Mathieu Bastian
 * @author Patick J. McSweeney
 */
@ConvertAsProperties(dtd = "-//org.gephi.desktop.statistics//Statistics//EN",
        autostore = false)
@TopComponent.Description(preferredID = "StatisticsTopComponent",
        iconBase = "org/gephi/desktop/statistics/resources/small.png",
        persistenceType = TopComponent.PERSISTENCE_ALWAYS)
@TopComponent.Registration(mode = "filtersmode", openAtStartup = true, roles = {"overview"})
@ActionID(category = "Window", id = "org.gephi.desktop.statistics.StatisticsTopComponent")
@ActionReference(path = "Menu/Window", position = 1200)
@TopComponent.OpenActionRegistration(displayName = "#CTL_StatisticsTopComponent",
        preferredID = "StatisticsTopComponent")
public final class StatisticsTopComponent extends TopComponent implements ChangeListener {

    //Model
    private transient StatisticsModelUIImpl model;

    public StatisticsTopComponent() {
        initComponents();
        initDesign();
        setName(NbBundle.getMessage(StatisticsTopComponent.class, "CTL_StatisticsTopComponent"));

        putClientProperty(TopComponent.PROP_MAXIMIZATION_DISABLED, Boolean.TRUE);

        //Workspace events
        final StatisticsControllerUI sc = Lookup.getDefault().lookup(StatisticsControllerUI.class);
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        pc.addWorkspaceListener(new WorkspaceListener() {

            @Override
            public void initialize(Workspace workspace) {
            }

            @Override
            public void select(Workspace workspace) {
                StatisticsModelUIImpl model = workspace.getLookup().lookup(StatisticsModelUIImpl.class);
                if (model == null) {
                    model = new StatisticsModelUIImpl(workspace);
                    workspace.add(model);
                }
                refreshModel(model);
            }

            @Override
            public void unselect(Workspace workspace) {
            }

            @Override
            public void close(Workspace workspace) {
            }

            @Override
            public void disable() {
                refreshModel(null);
            }
        });

        if (pc.getCurrentWorkspace() != null) {
            StatisticsModelUIImpl model = pc.getCurrentWorkspace().getLookup().lookup(StatisticsModelUIImpl.class);
            if (model == null) {
                model = new StatisticsModelUIImpl(pc.getCurrentWorkspace());
                pc.getCurrentWorkspace().add(model);
            }
            refreshModel(model);
        } else {
            refreshModel(null);
        }

        //Settings
        settingsButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                AvailableStatisticsChooser chooser = new AvailableStatisticsChooser();
                chooser.setup(model, ((StatisticsPanel) statisticsPanel).getCategories());
                DialogDescriptor dd = new DialogDescriptor(chooser, NbBundle.getMessage(StatisticsTopComponent.class, "AvailableStatisticsChooser.title"));
                if (DialogDisplayer.getDefault().notify(dd).equals(NotifyDescriptor.OK_OPTION)) {
                    chooser.unsetup();
                }
            }
        });
    }

    private void refreshModel(StatisticsModelUIImpl model) {
        if (model != null && model != this.model) {
            if (this.model != null) {
                this.model.removeChangeListener(this);
            }
            model.addChangeListener(this);
        }
        this.model = model;
        Lookup.getDefault().lookup(StatisticsControllerUIImpl.class).setup(model);
        refreshEnable(model != null);
        ((StatisticsPanel) statisticsPanel).refreshModel(model);
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        refreshModel(model);
    }

    private void refreshEnable(boolean enable) {
        statisticsPanel.setEnabled(enable);
        toolbar.setEnabled(enable);
        settingsButton.setEnabled(enable);
    }

    private void initDesign() {
        Border b = (Border) UIManager.get("Nb.Editor.Toolbar.border"); //NOI18N
        toolbar.setBorder(b);
        if (UIUtils.isAquaLookAndFeel()) {
            toolbar.setBackground(UIManager.getColor("NbExplorerView.background"));
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        toolbar = new javax.swing.JToolBar();
        settingsButton = new javax.swing.JButton();
        statisticsPanel = new StatisticsPanel();

        setLayout(new java.awt.GridBagLayout());

        toolbar.setFloatable(false);
        toolbar.setRollover(true);

        org.openide.awt.Mnemonics.setLocalizedText(settingsButton, org.openide.util.NbBundle.getMessage(StatisticsTopComponent.class, "StatisticsTopComponent.settingsButton.text")); // NOI18N
        settingsButton.setFocusable(false);
        settingsButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        settingsButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolbar.add(settingsButton);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 1.0;
        add(toolbar, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(statisticsPanel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton settingsButton;
    private javax.swing.JPanel statisticsPanel;
    private javax.swing.JToolBar toolbar;
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
}
