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
package org.gephi.desktop.ranking;

import java.awt.BorderLayout;
import java.io.Serializable;
import java.util.logging.Logger;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import org.gephi.project.api.ProjectController;
import org.gephi.ranking.RankingUIModel;
import org.gephi.ui.ranking.BarChartPanel;
import org.gephi.ui.ranking.RankingChooser;
import org.gephi.ui.ranking.RankingToolbar;
import org.gephi.ui.ranking.ResultListPanel;
import org.gephi.workspace.api.Workspace;
import org.gephi.workspace.api.WorkspaceDataKey;
import org.gephi.workspace.api.WorkspaceListener;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

final class RankingTopComponent extends TopComponent implements Lookup.Provider {

    private static RankingTopComponent instance;
    /** path to the icon used by the component and its open action */
//    static final String ICON_PATH = "SET/PATH/TO/ICON/HERE";
    private static final String PREFERRED_ID = "RankingTopComponent";

    //UI
    private JToggleButton barChartButton;
    private JToggleButton listButton;
    private JPanel contentPanel;
    private JPanel barChartPanel;
    private JScrollPane listPanel;
    private RankingChooser rankingChooser;
    private RankingToolbar rankingToolbar;

    //Model
    private RankingUIModel model;
    private AbstractLookup rankingLookup;

    private RankingTopComponent() {
        initComponents();
        setName(NbBundle.getMessage(RankingTopComponent.class, "CTL_RankingTopComponent"));
        setToolTipText(NbBundle.getMessage(RankingTopComponent.class, "HINT_RankingTopComponent"));
//        setIcon(Utilities.loadImage(ICON_PATH, true));

        initEvents();
        rankingToolbar = new RankingToolbar(model);
        add(rankingToolbar, BorderLayout.NORTH);
        rankingChooser = new RankingChooser(model, rankingLookup);
        add(rankingChooser, BorderLayout.CENTER);
        initSouth();
    }

    private void initSouth() {
        //SouthToolbar
        JToolBar southToolbar = new JToolBar();
        southToolbar.setFloatable(false);
        southToolbar.setRollover(true);
        listButton = new JToggleButton();
        listButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/gephi/desktop/ranking/resources/list.png"))); // NOI18N
        NbBundle.getMessage(RankingTopComponent.class, "RankingTopComponent.listButton.text");
        listButton.setEnabled(false);
        listButton.setFocusable(false);
        southToolbar.add(listButton);
        barChartButton = new JToggleButton();
        barChartButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/gephi/desktop/ranking/resources/barchart.png"))); // NOI18N
        NbBundle.getMessage(RankingTopComponent.class, "RankingTopComponent.barchartButton.text");
        barChartButton.setEnabled(false);
        barChartButton.setFocusable(false);
        southToolbar.add(barChartButton);

        //BarChartPanel & ListPanel
        JPanel southPanel = new JPanel(new BorderLayout());
        barChartPanel = new BarChartPanel();
        listPanel = new ResultListPanel();
        southPanel.add(barChartPanel, BorderLayout.NORTH);
        southPanel.add(listPanel, BorderLayout.CENTER);
        southPanel.add(southToolbar, BorderLayout.SOUTH);

        listPanel.setVisible(false);
        barChartPanel.setVisible(false);
        add(southPanel, BorderLayout.SOUTH);
    }

    private void initEvents() {
        //Lookup
        rankingLookup = new AbstractLookup(new InstanceContent());

        model = new RankingUIModel();

        final WorkspaceDataKey<RankingUIModel> key = Lookup.getDefault().lookup(RankingUIWorkspaceDataProvider.class).getWorkspaceDataKey();
        final ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        pc.addWorkspaceListener(new WorkspaceListener() {

            public void initialize(Workspace workspace) {
            }

            public void select(Workspace workspace) {
                //Enable
                barChartButton.setEnabled(true);
                listButton.setEnabled(true);
                rankingChooser.setEnabled(true);
                rankingToolbar.setEnabled(true);
                RankingUIModel newModel = workspace.getWorkspaceData().getData(key);
                if (newModel != null) {
                    model.loadModel(newModel);
                }
            }

            public void unselect(Workspace workspace) {
                workspace.getWorkspaceData().setData(key, model.saveModel());
            }

            public void close(Workspace workspace) {
            }

            public void disable() {
                barChartButton.setEnabled(false);
                listButton.setEnabled(false);
                rankingChooser.setEnabled(false);
                rankingToolbar.setEnabled(false);
            }
        });
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setLayout(new java.awt.BorderLayout());
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
    /**
     * Gets default instance. Do not use directly: reserved for *.settings files only,
     * i.e. deserialization routines; otherwise you could get a non-deserialized instance.
     * To obtain the singleton instance, use {@link #findInstance}.
     */
    public static synchronized RankingTopComponent getDefault() {
        if (instance == null) {
            instance = new RankingTopComponent();
        }
        return instance;
    }

    /**
     * Obtain the RankingTopComponent instance. Never call {@link #getDefault} directly!
     */
    public static synchronized RankingTopComponent findInstance() {
        TopComponent win = WindowManager.getDefault().findTopComponent(PREFERRED_ID);
        if (win == null) {
            Logger.getLogger(RankingTopComponent.class.getName()).warning(
                    "Cannot find " + PREFERRED_ID + " component. It will not be located properly in the window system.");
            return getDefault();
        }
        if (win instanceof RankingTopComponent) {
            return (RankingTopComponent) win;
        }
        Logger.getLogger(RankingTopComponent.class.getName()).warning(
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
            return RankingTopComponent.getDefault();
        }
    }
}
