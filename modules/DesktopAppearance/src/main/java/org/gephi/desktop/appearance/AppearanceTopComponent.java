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
package org.gephi.desktop.appearance;

import java.beans.PropertyChangeEvent;
import javax.swing.Box;
import javax.swing.JToggleButton;
import javax.swing.UIManager;
import org.gephi.ui.utils.UIUtils;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

@ConvertAsProperties(dtd = "-//org.gephi.desktop.appearance//Appearance//EN",
        autostore = false)
@TopComponent.Description(preferredID = "AppearanceTopComponent",
        iconBase = "org/gephi/desktop/appearance/resources/small.png",
        persistenceType = TopComponent.PERSISTENCE_ALWAYS)
@TopComponent.Registration(mode = "rankingmode", openAtStartup = true, roles = {"overview"})
@ActionID(category = "Window", id = "org.gephi.desktop.appearance.AppearanceTopComponent")
@ActionReference(path = "Menu/Window", position = 1100)
@TopComponent.OpenActionRegistration(displayName = "#CTL_AppearanceAction",
        preferredID = "AppearanceTopComponent")
public class AppearanceTopComponent extends TopComponent implements Lookup.Provider, AppearanceUIModelListener {

    //UI
    private transient final AppearanceToolbar toolbar;
    private transient JToggleButton listButton;
    private transient JToggleButton localScaleButton;
    //Model
    private transient final AppearanceUIController controller;
    private transient AppearanceUIModel model;

    public AppearanceTopComponent() {
        setName(NbBundle.getMessage(AppearanceTopComponent.class, "CTL_AppearanceTopComponent"));

        controller = Lookup.getDefault().lookup(AppearanceUIController.class);
        model = controller.getModel();
        controller.addPropertyChangeListener(this);

        toolbar = new AppearanceToolbar(controller);

        initComponents();
        initSouth();
        if (UIUtils.isAquaLookAndFeel()) {
            mainPanel.setBackground(UIManager.getColor("NbExplorerView.background"));
        }

        refreshModel(model);
    }

    public void refreshModel(AppearanceUIModel model) {
        this.model = model;
        refreshEnable();

        //South visible
        /*
         * if (barChartPanel.isVisible() != model.isBarChartVisible()) {
         * barChartPanel.setVisible(model.isBarChartVisible()); revalidate();
         * repaint(); }
         */
//        ((ResultListPanel) listResultPanel).unselect();
//        if (model != null) {
//            ((ResultListPanel) listResultPanel).select(model);
//            if (listResultPanel.isVisible() != model.isListVisible()) {
//                listResultPanel.setVisible(model.isListVisible());
//                revalidate();
//                repaint();
//            }
//
//            //barChartButton.setSelected(model.isBarChartVisible());
//            listButton.setSelected(model.isListVisible());
//            localScaleButton.setSelected(model.isLocalScale());
//        } else {
//            listResultPanel.setVisible(false);
//            listButton.setSelected(false);
//            localScaleButton.setSelected(false);
//        }


        //Chooser
//        ((RankingChooser) centerPanel).refreshModel(model);

        //Toolbar
//        ((RankingToolbar) categoryToolbar).refreshModel(model);
    }

    @Override
    public void propertyChange(PropertyChangeEvent pce) {
        AppearanceUIModel source = (AppearanceUIModel) pce.getSource();
        if (pce.getPropertyName().equals(AppearanceUIModelEvent.MODEL)) {
            refreshModel(source);
        }
        //        if (pce.getPropertyName().equals(RankingUIModel.LIST_VISIBLE)) {
        //            listButton.setSelected((Boolean) pce.getNewValue());
        //            if (listResultPanel.isVisible() != model.isListVisible()) {
        //                listResultPanel.setVisible(model.isListVisible());
        //                revalidate();
        //                repaint();
        //            }
        //        } else if (pce.getPropertyName().equals(RankingUIModel.BARCHART_VISIBLE)) {
        //            //barChartButton.setSelected((Boolean)pce.getNewValue());
        //        } else if (pce.getPropertyName().equals(RankingUIModel.LOCAL_SCALE)) {
        //            localScaleButton.setSelected((Boolean) pce.getNewValue());
        //        } else if (pce.getPropertyName().equals(RankingUIModel.LOCAL_SCALE_ENABLED)) {
        //            localScaleButton.setEnabled((Boolean) pce.getNewValue());
        //        }
        {
        }
    }

    private void initSouth() {
//        listButton = new JToggleButton();
//        listButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/gephi/desktop/ranking/resources/list.png"))); // NOI18N
//        listButton.setToolTipText(NbBundle.getMessage(RankingTopComponent.class, "RankingTopComponent.listButton.text"));
//        listButton.setEnabled(false);
//        listButton.setFocusable(false);
//        southToolbar.add(listButton);
        /*
         * barChartButton = new JToggleButton(); barChartButton.setIcon(new
         * javax.swing.ImageIcon(getClass().getResource("/org/gephi/desktop/ranking/resources/barchart.png")));
         * // NOI18N NbBundle.getMessage(RankingTopComponent.class,
         * "RankingTopComponent.barchartButton.text");
         * barChartButton.setEnabled(false); barChartButton.setFocusable(false);
         * southToolbar.add(barChartButton);
         */
//        localScaleButton = new JToggleButton();
//        localScaleButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/gephi/desktop/ranking/resources/funnel.png"))); // NOI18N
//        localScaleButton.setToolTipText(NbBundle.getMessage(RankingTopComponent.class, "RankingTopComponent.localScaleButton.text"));
//        localScaleButton.setEnabled(false);
//        localScaleButton.setFocusable(false);
//        southToolbar.add(localScaleButton);
//
//        //Local scale enabled
//        localScaleButton.setEnabled(model != null ? model.isLocalScaleEnabled() : false);
//
//        //BarChartPanel & ListPanel
//        listResultPanel.setVisible(false);
//
//        listButton.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                model.setListVisible(listButton.isSelected());
//            }
//        });
//
//        localScaleButton.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                model.setLocalScale(localScaleButton.isSelected());
//            }
//        });

        /*
         * barChartButton.addActionListener(new ActionListener() {
         *
         * public void actionPerformed(ActionEvent e) {
         * model.setBarChartVisible(barChartButton.isSelected()); } });
         */
    }

    private void refreshEnable() {
        boolean modelEnabled = isModelEnabled();

        //barChartButton.setEnabled(modelEnabled);
//        listButton.setEnabled(modelEnabled);
//        localScaleButton.setEnabled(modelEnabled && model.isLocalScaleEnabled());
//        rankingChooser.setEnabled(modelEnabled);
//        rankingToolbar.setEnabled(modelEnabled);
//        listResultPanel.setEnabled(modelEnabled);
    }

    private boolean isModelEnabled() {
        return model != null;
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
        categoryToolbar = toolbar.getCategoryToolbar();
        tranformerToolbar = new javax.swing.JToolBar();
        centerPanel = new javax.swing.JPanel();
        southToolbar = new javax.swing.JToolBar();
        controlPanel = new javax.swing.JPanel();
        applyButton = new javax.swing.JButton();
        splineButton = new org.jdesktop.swingx.JXHyperlink();
        autoApplyButton = new javax.swing.JToggleButton();
        autoApplyToolbar = new javax.swing.JToolBar();
        enableAutoButton = new javax.swing.JToggleButton();

        setOpaque(true);
        setLayout(new java.awt.BorderLayout());

        mainPanel.setLayout(new java.awt.GridBagLayout());

        categoryToolbar.setFloatable(false);
        categoryToolbar.setRollover(true);
        categoryToolbar.setOpaque(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 1.0;
        mainPanel.add(categoryToolbar, gridBagConstraints);

        tranformerToolbar.setFloatable(false);
        tranformerToolbar.setRollover(true);
        tranformerToolbar.setOpaque(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 1.0;
        mainPanel.add(tranformerToolbar, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        mainPanel.add(centerPanel, gridBagConstraints);

        southToolbar.setFloatable(false);
        southToolbar.setRollover(true);
        southToolbar.setOpaque(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LAST_LINE_END;
        gridBagConstraints.weightx = 1.0;
        mainPanel.add(southToolbar, gridBagConstraints);

        controlPanel.setOpaque(false);
        controlPanel.setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(applyButton, org.openide.util.NbBundle.getMessage(AppearanceTopComponent.class, "AppearanceTopComponent.applyButton.text")); // NOI18N
        applyButton.setToolTipText(org.openide.util.NbBundle.getMessage(AppearanceTopComponent.class, "AppearanceTopComponent.applyButton.toolTipText")); // NOI18N
        applyButton.setMargin(new java.awt.Insets(0, 14, 0, 14));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHEAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 18, 3, 5);
        controlPanel.add(applyButton, gridBagConstraints);

        splineButton.setClickedColor(new java.awt.Color(0, 51, 255));
        org.openide.awt.Mnemonics.setLocalizedText(splineButton, org.openide.util.NbBundle.getMessage(AppearanceTopComponent.class, "AppearanceTopComponent.splineButton.text")); // NOI18N
        splineButton.setToolTipText(org.openide.util.NbBundle.getMessage(AppearanceTopComponent.class, "AppearanceTopComponent.splineButton.toolTipText")); // NOI18N
        splineButton.setFocusPainted(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        controlPanel.add(splineButton, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(autoApplyButton, org.openide.util.NbBundle.getMessage(AppearanceTopComponent.class, "AppearanceTopComponent.autoApplyButton.text")); // NOI18N
        autoApplyButton.setToolTipText(org.openide.util.NbBundle.getMessage(AppearanceTopComponent.class, "AppearanceTopComponent.autoApplyButton.toolTipText")); // NOI18N
        autoApplyButton.setFocusable(false);
        autoApplyButton.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        autoApplyButton.setMargin(new java.awt.Insets(0, 7, 0, 7));
        autoApplyButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHEAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 3, 3, 5);
        controlPanel.add(autoApplyButton, gridBagConstraints);

        autoApplyToolbar.setFloatable(false);
        autoApplyToolbar.setRollover(true);
        autoApplyToolbar.setOpaque(false);

        enableAutoButton.setToolTipText(org.openide.util.NbBundle.getMessage(AppearanceTopComponent.class, "AppearanceTopComponent.enableAutoButton.toolTipText")); // NOI18N
        enableAutoButton.setFocusable(false);
        enableAutoButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        enableAutoButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        autoApplyToolbar.add(Box.createHorizontalGlue());
        autoApplyToolbar.add(enableAutoButton);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        controlPanel.add(autoApplyToolbar, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        mainPanel.add(controlPanel, gridBagConstraints);

        add(mainPanel, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton applyButton;
    private javax.swing.JToggleButton autoApplyButton;
    private javax.swing.JToolBar autoApplyToolbar;
    private javax.swing.JToolBar categoryToolbar;
    private javax.swing.JPanel centerPanel;
    private javax.swing.JPanel controlPanel;
    private javax.swing.JToggleButton enableAutoButton;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JToolBar southToolbar;
    private org.jdesktop.swingx.JXHyperlink splineButton;
    private javax.swing.JToolBar tranformerToolbar;
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
