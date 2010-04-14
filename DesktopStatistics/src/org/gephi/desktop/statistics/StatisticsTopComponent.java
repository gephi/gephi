/*
Copyright 2008 WebAtlas
Authors :  Patrick J. McSweeney (pjmcswee@syr.edu)
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
package org.gephi.desktop.statistics;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Logger;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.project.api.WorkspaceListener;
import org.gephi.ui.utils.UIUtils;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;

@ConvertAsProperties(dtd = "-//org.gephi.desktop.statistics//Statistics//EN",
autostore = false)
public final class StatisticsTopComponent extends TopComponent implements ChangeListener {

    private static StatisticsTopComponent instance;
    static final String ICON_PATH = "org/gephi/desktop/statistics/resources/small.png";
    private static final String PREFERRED_ID = "StatisticsTopComponent";
    //Model
    private StatisticsModelImpl model;

    public StatisticsTopComponent() {
        initComponents();
        initDesign();
        setName(NbBundle.getMessage(StatisticsTopComponent.class, "CTL_StatisticsTopComponent"));
//        setToolTipText(NbBundle.getMessage(StatisticsTopComponent.class, "HINT_StatisticsTopComponent"));
        setIcon(ImageUtilities.loadImage(ICON_PATH));
        putClientProperty(TopComponent.PROP_MAXIMIZATION_DISABLED, Boolean.TRUE);

        //Workspace events
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        pc.addWorkspaceListener(new WorkspaceListener() {

            public void initialize(Workspace workspace) {
                workspace.add(new StatisticsModelImpl());
            }

            public void select(Workspace workspace) {
                StatisticsModelImpl m = workspace.getLookup().lookup(StatisticsModelImpl.class);
                if (m == null) {
                    m = new StatisticsModelImpl();
                    workspace.add(m);
                }
                refreshModel(m);
            }

            public void unselect(Workspace workspace) {
            }

            public void close(Workspace workspace) {
            }

            public void disable() {
                refreshModel(null);
            }
        });

        if (pc.getCurrentWorkspace() != null) {
            StatisticsModelImpl m = pc.getCurrentWorkspace().getLookup().lookup(StatisticsModelImpl.class);
            if (m == null) {
                m = new StatisticsModelImpl();
                pc.getCurrentWorkspace().add(m);
            }
            refreshModel(m);
        } else {
            refreshModel(null);
        }

        //Settings
        settingsButton.addActionListener(new ActionListener() {

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

    private void refreshModel(StatisticsModelImpl model) {
        if (model != null && model != this.model) {
            if (this.model != null) {
                this.model.removeChangeListener(this);
            }
            model.addChangeListener(this);
        }
        this.model = model;
        refreshEnable(model != null);
        ((StatisticsPanel) statisticsPanel).refreshModel(model);
    }

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

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
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

    /**
     * Gets default instance. Do not use directly: reserved for *.settings files only,
     * i.e. deserialization routines; otherwise you could get a non-deserialized instance.
     * To obtain the singleton instance, use {@link #findInstance}.
     */
    public static synchronized StatisticsTopComponent getDefault() {
        if (instance == null) {
            instance = new StatisticsTopComponent();
        }
        return instance;
    }

    /**
     * Obtain the StatisticsTopComponent instance. Never call {@link #getDefault} directly!
     */
    public static synchronized StatisticsTopComponent findInstance() {
        TopComponent win = WindowManager.getDefault().findTopComponent(PREFERRED_ID);
        if (win == null) {
            Logger.getLogger(StatisticsTopComponent.class.getName()).warning(
                    "Cannot find " + PREFERRED_ID + " component. It will not be located properly in the window system.");
            return getDefault();
        }
        if (win instanceof StatisticsTopComponent) {
            return (StatisticsTopComponent) win;
        }
        Logger.getLogger(StatisticsTopComponent.class.getName()).warning(
                "There seem to be multiple components with the '" + PREFERRED_ID
                + "' ID. That is a potential source of errors and unexpected behavior.");
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

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // TODO store your settings
    }

    Object readProperties(java.util.Properties p) {
        if (instance == null) {
            instance = this;
        }
        instance.readPropertiesImpl(p);
        return instance;
    }

    private void readPropertiesImpl(java.util.Properties p) {
        String version = p.getProperty("version");
        // TODO read your settings according to their version
    }

    @Override
    protected String preferredID() {
        return PREFERRED_ID;
    }
}
