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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.util.logging.Logger;
import javax.swing.JPanel;
import org.gephi.project.api.ProjectController;
import org.gephi.ranking.RankingUIModel;
import org.gephi.ui.ranking.EdgeRankingPanel;
import org.gephi.ui.ranking.NodeRankingPanel;
import org.gephi.workspace.api.Workspace;
import org.gephi.workspace.api.WorkspaceListener;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
//import org.openide.util.Utilities;

/**
 * Top component which displays something.
 */
final class RankingTopComponent extends TopComponent {

    private static RankingTopComponent instance;
    /** path to the icon used by the component and its open action */
//    static final String ICON_PATH = "SET/PATH/TO/ICON/HERE";
    private static final String PREFERRED_ID = "RankingTopComponent";
    private JPanel contentPanel;

    private RankingTopComponent() {
        initComponents();
        setName(NbBundle.getMessage(RankingTopComponent.class, "CTL_RankingTopComponent"));
        setToolTipText(NbBundle.getMessage(RankingTopComponent.class, "HINT_RankingTopComponent"));
//        setIcon(Utilities.loadImage(ICON_PATH, true));

        initEvents();
    }

    private void initEvents() {
        final RankingUIWorkspaceDataProvider dataProvider = Lookup.getDefault().lookup(RankingUIWorkspaceDataProvider.class);
        final ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        pc.addWorkspaceListener(new WorkspaceListener() {

            public void initialize(Workspace workspace) {
            }

            public void select(Workspace workspace) {
                //Enable
                nodeButton.setEnabled(true);
                edgeButton.setEnabled(true);
                barChartButton.setEnabled(true);
                listButton.setEnabled(true);

                //Get model
                RankingUIModel model = workspace.getWorkspaceData().getData(dataProvider.getWorkspaceDataKey());
                barChartButton.setSelected(model.isBarChartVisible());
                listButton.setSelected(model.isListVisible());
                transformerGroup.setSelected(nodeButton.getModel(), model.getTransformer() == RankingUIModel.NODE_TRANSFORMER);
                transformerGroup.setSelected(edgeButton.getModel(), model.getTransformer() == RankingUIModel.EDGE_TRANSFORMER);
                refreshContentPanel(true);
            }

            public void unselect(Workspace workspace) {
            }

            public void close(Workspace workspace) {
            }

            public void disable() {
                nodeButton.setEnabled(false);
                edgeButton.setEnabled(false);
                barChartButton.setEnabled(false);
                listButton.setEnabled(false);
                refreshContentPanel(false);
            }
        });

        barChartButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent arg0) {
                RankingUIModel model = pc.getCurrentWorkspace().getWorkspaceData().getData(dataProvider.getWorkspaceDataKey());
                model.setBarChartVisible(barChartButton.isSelected());
            }
        });

        listButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent arg0) {
                RankingUIModel model = pc.getCurrentWorkspace().getWorkspaceData().getData(dataProvider.getWorkspaceDataKey());
                model.setListVisible(listButton.isSelected());
            }
        });

        nodeButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent arg0) {
                RankingUIModel model = pc.getCurrentWorkspace().getWorkspaceData().getData(dataProvider.getWorkspaceDataKey());
                model.setTransformer(transformerGroup.getSelection() == nodeButton.getModel() ? RankingUIModel.NODE_TRANSFORMER : RankingUIModel.EDGE_TRANSFORMER);
                refreshContentPanel(true);
            }
        });

        edgeButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent arg0) {
                RankingUIModel model = pc.getCurrentWorkspace().getWorkspaceData().getData(dataProvider.getWorkspaceDataKey());
                model.setTransformer(transformerGroup.getSelection() == nodeButton.getModel() ? RankingUIModel.NODE_TRANSFORMER : RankingUIModel.EDGE_TRANSFORMER);
                refreshContentPanel(true);
            }
        });
    }

    private void refreshContentPanel(boolean enable) {
        if (!enable && contentPanel != null) {
            remove(contentPanel);
            contentPanel = null;
        }
        if (transformerGroup.getSelection() == nodeButton.getModel()) {
            //Node
            contentPanel = new NodeRankingPanel();
            add(contentPanel, BorderLayout.CENTER);
        } else {
            //Edge
            contentPanel = new EdgeRankingPanel();
            add(contentPanel, BorderLayout.CENTER);
        }
        revalidate();
        repaint();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        transformerGroup = new javax.swing.ButtonGroup();
        rankingToolbar = new javax.swing.JToolBar();
        nodeButton = new javax.swing.JToggleButton();
        edgeButton = new javax.swing.JToggleButton();
        box = new javax.swing.JLabel();
        barChartButton = new javax.swing.JToggleButton();
        listButton = new javax.swing.JToggleButton();

        setLayout(new java.awt.BorderLayout());

        rankingToolbar.setFloatable(false);
        rankingToolbar.setRollover(true);

        transformerGroup.add(nodeButton);
        org.openide.awt.Mnemonics.setLocalizedText(nodeButton, org.openide.util.NbBundle.getMessage(RankingTopComponent.class, "RankingTopComponent.nodeButton.text")); // NOI18N
        nodeButton.setEnabled(false);
        nodeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nodeButtonActionPerformed(evt);
            }
        });
        rankingToolbar.add(nodeButton);

        transformerGroup.add(edgeButton);
        org.openide.awt.Mnemonics.setLocalizedText(edgeButton, org.openide.util.NbBundle.getMessage(RankingTopComponent.class, "RankingTopComponent.edgeButton.text")); // NOI18N
        edgeButton.setEnabled(false);
        edgeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                edgeButtonActionPerformed(evt);
            }
        });
        rankingToolbar.add(edgeButton);

        org.openide.awt.Mnemonics.setLocalizedText(box, org.openide.util.NbBundle.getMessage(RankingTopComponent.class, "RankingTopComponent.box.text")); // NOI18N
        box.setMaximumSize(new java.awt.Dimension(32767, 32767));
        rankingToolbar.add(box);

        barChartButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/gephi/desktop/ranking/resources/barchart.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(barChartButton, org.openide.util.NbBundle.getMessage(RankingTopComponent.class, "RankingTopComponent.barChartButton.text")); // NOI18N
        barChartButton.setEnabled(false);
        barChartButton.setFocusable(false);
        barChartButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        barChartButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        rankingToolbar.add(barChartButton);

        listButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/gephi/desktop/ranking/resources/list.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(listButton, org.openide.util.NbBundle.getMessage(RankingTopComponent.class, "RankingTopComponent.listButton.text")); // NOI18N
        listButton.setEnabled(false);
        listButton.setFocusable(false);
        listButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        listButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        rankingToolbar.add(listButton);

        add(rankingToolbar, java.awt.BorderLayout.PAGE_START);
    }// </editor-fold>//GEN-END:initComponents

    private void nodeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nodeButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_nodeButtonActionPerformed

    private void edgeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_edgeButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_edgeButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JToggleButton barChartButton;
    private javax.swing.JLabel box;
    private javax.swing.JToggleButton edgeButton;
    private javax.swing.JToggleButton listButton;
    private javax.swing.JToggleButton nodeButton;
    private javax.swing.JToolBar rankingToolbar;
    private javax.swing.ButtonGroup transformerGroup;
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
