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

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.swing.Box;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import org.gephi.appearance.api.Function;
import org.gephi.appearance.api.Interpolator;
import org.gephi.appearance.api.RankingFunction;
import org.gephi.appearance.spi.TransformerUI;
import org.gephi.ui.components.splineeditor.SplineEditor;
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

    //Const
    private final String NO_SELECTION = NbBundle.getMessage(AppearanceTopComponent.class, "AppearanceTopComponent.choose.text");
    //UI
    private transient JPanel transformerPanel;
    private transient final AppearanceToolbar toolbar;
    private transient JToggleButton listButton;
    private transient ItemListener attributeListener;
    private transient SplineEditor splineEditor;
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
        initControls();
        if (UIUtils.isAquaLookAndFeel()) {
            mainPanel.setBackground(UIManager.getColor("NbExplorerView.background"));
            centerPanel.setBackground(UIManager.getColor("NbExplorerView.background"));
        }

        //Hide for now
        localScaleButton.setVisible(false);

        refreshModel(model);
    }

    @Override
    public void propertyChange(PropertyChangeEvent pce) {
        if (pce.getPropertyName().equals(AppearanceUIModelEvent.MODEL)) {
            refreshModel((AppearanceUIModel) pce.getNewValue());
        } else if (pce.getPropertyName().equals(AppearanceUIModelEvent.SELECTED_CATEGORY)
                || pce.getPropertyName().equals(AppearanceUIModelEvent.SELECTED_ELEMENT_CLASS)
                || pce.getPropertyName().equals(AppearanceUIModelEvent.SELECTED_TRANSFORMER_UI)) {
            refreshCenterPanel();
            refreshCombo();
            refreshControls();
        } else if (pce.getPropertyName().equals(AppearanceUIModelEvent.SELECTED_FUNCTION)) {
            refreshCenterPanel();
            refreshCombo();
            refreshControls();
        } else if (pce.getPropertyName().equals(AppearanceUIModelEvent.SET_AUTO_APPLY)) {
            refreshControls();
        } else if (pce.getPropertyName().equals(AppearanceUIModelEvent.START_STOP_AUTO_APPLY)) {
            refreshControls();
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
    }

    public void refreshModel(AppearanceUIModel model) {
        this.model = model;
        refreshEnable();
        refreshCenterPanel();
        refreshCombo();
        refreshControls();

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

    private void refreshCenterPanel() {

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                if (transformerPanel != null) {
                    centerPanel.remove(transformerPanel);
                    transformerPanel = null;
                }
                if (model != null) {
                    TransformerUI ui = model.getSelectedTransformerUI();
                    if (ui != null) {
                        boolean attribute = model.isAttributeTransformerUI(ui);

                        attributePanel.setVisible(attribute);
                        if (attribute) {
                            Function function = model.getSelectedFunction();
                            if (function != null) {
                                ui = function.getUI();
                                transformerPanel = ui.getPanel(function);
                            }
                        } else {
                            Function function = model.getSelectedFunction();
                            transformerPanel = ui.getPanel(function);
                        }

                        if (transformerPanel != null) {
                            transformerPanel.setOpaque(false);
                            centerPanel.add(transformerPanel, BorderLayout.CENTER);
                        }

                        centerPanel.repaint();

                        //setCenterPanel
                        return;
                    }
                }
                attributePanel.setVisible(false);
            }
        });
    }

    private void refreshCombo() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                final DefaultComboBoxModel comboBoxModel = new DefaultComboBoxModel();
                if (model != null) {
                    TransformerUI ui = model.getSelectedTransformerUI();
                    if (ui != null && model.isAttributeTransformerUI(ui)) {

                        //Ranking
                        Function selectedColumn = model.getSelectedFunction();
                        attibuteBox.removeItemListener(attributeListener);

                        comboBoxModel.addElement(NO_SELECTION);
                        comboBoxModel.setSelectedItem(NO_SELECTION);

                        List<Function> rows = new ArrayList<>();
                        rows.addAll(model.getFunctions());

                        Collections.sort(rows, new Comparator<Function>() {
                            @Override
                            public int compare(Function o1, Function o2) {
                                if(o1.isAttribute() && !o2.isAttribute()) {
                                    return 1;
                                } else if(!o1.isAttribute() && o2.isAttribute()) {
                                    return -1;
                                }
                                return o1.toString().compareTo(o2.toString());
                            }
                        });
                        for (Function r : rows) {
                            comboBoxModel.addElement(r);
                            if (selectedColumn != null && selectedColumn.equals(r)) {
                                comboBoxModel.setSelectedItem(r);
                            }
                        }
                        attributeListener = new ItemListener() {
                            @Override
                            public void itemStateChanged(ItemEvent e) {
                                if (model != null) {
                                    if (!attibuteBox.getSelectedItem().equals(NO_SELECTION)) {
                                        Function selectedItem = (Function) attibuteBox.getSelectedItem();
                                        Function selectedFunction = model.getSelectedFunction();
                                        if (selectedFunction != selectedItem) {
                                            controller.setSelectedFunction(selectedItem);
                                        }
                                    } else {
                                        controller.setSelectedFunction(null);
                                    }
                                }
                            }
                        };
                        attibuteBox.addItemListener(attributeListener);
                    }
                }
                attibuteBox.setModel(comboBoxModel);
            }
        });
    }

    private void refreshControls() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                if (model != null && model.getSelectedFunction() != null) {
                    enableAutoButton.setEnabled(true);
                    if (model.getAutoAppyTransformer() != null) {
                        applyButton.setVisible(false);
                        enableAutoButton.setSelected(true);
                        AutoAppyTransformer aat = model.getAutoAppyTransformer();
                        if (aat.isRunning()) {
                            autoApplyButton.setVisible(false);
                            stopAutoApplyButton.setVisible(true);
                            stopAutoApplyButton.setSelected(true);
                        } else {
                            autoApplyButton.setVisible(true);
                            autoApplyButton.setSelected(false);
                            stopAutoApplyButton.setVisible(false);
                        }
                    } else {
                        autoApplyButton.setVisible(false);
                        stopAutoApplyButton.setVisible(false);
                        enableAutoButton.setSelected(false);
                        applyButton.setVisible(true);
                        applyButton.setEnabled(true);
                    }
                    localScaleButton.setSelected(model.isLocalScale());
                    return;
                }
                //Disable
                stopAutoApplyButton.setVisible(false);
                autoApplyButton.setVisible(false);
                applyButton.setVisible(true);
                applyButton.setEnabled(false);
                enableAutoButton.setEnabled(false);
            }
        });
    }

    private void initControls() {
        //Add ranking controls
//        toolbar.addRankingControl(localScaleButton);
        toolbar.addRankingControl(splineButton);

        //Add partition controls
//        toolbar.addPartitionControl(localScaleButton);
        //Actions
        localScaleButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.getAppearanceController().setUseLocalScale(localScaleButton.isSelected());
            }
        });
        splineButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                RankingFunction function = (RankingFunction) model.getSelectedFunction();
                if (splineEditor == null) {
                    splineEditor = new SplineEditor(NbBundle.getMessage(AppearanceTopComponent.class, "AppearanceTopComponent.splineEditor.title"));
                }
                Interpolator interpolator = function.getInterpolator();
                if (interpolator instanceof Interpolator.BezierInterpolator) {
                    Interpolator.BezierInterpolator bezierInterpolator = (Interpolator.BezierInterpolator) interpolator;
                    splineEditor.setControl1(bezierInterpolator.getControl1());
                    splineEditor.setControl2(bezierInterpolator.getControl2());
                } else {
                    splineEditor.setControl1(new Point2D.Float(0, 0));
                    splineEditor.setControl2(new Point2D.Float(1, 1));
                }
                splineEditor.setVisible(true);
                function.setInterpolator(
                        new Interpolator.BezierInterpolator(
                                (float) splineEditor.getControl1().getX(), (float) splineEditor.getControl1().getY(),
                                (float) splineEditor.getControl2().getX(), (float) splineEditor.getControl2().getY()));
            }
        });
        applyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.appearanceController.transform(model.getSelectedFunction());
            }
        });
        autoApplyButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                controller.startAutoApply();
            }
        });
        stopAutoApplyButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                controller.stopAutoApply();
            }

        });
        enableAutoButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                controller.setAutoApply(model.getAutoAppyTransformer() == null);
            }
        });
        stopAutoApplyButton.setVisible(false);
        autoApplyButton.setVisible(false);

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
        tranformerToolbar = toolbar.getTransformerToolbar();
        attributePanel = new javax.swing.JPanel();
        attibuteBox = new javax.swing.JComboBox();
        centerPanel = new javax.swing.JPanel();
        controlToolbar = toolbar.getControlToolbar();
        localScaleButton = new javax.swing.JToggleButton();
        splineButton = new org.jdesktop.swingx.JXHyperlink();
        controlPanel = new javax.swing.JPanel();
        applyButton = new javax.swing.JButton();
        stopAutoApplyButton = new javax.swing.JToggleButton();
        autoApplyToolbar = new javax.swing.JToolBar();
        enableAutoButton = new javax.swing.JToggleButton();
        autoApplyButton = new javax.swing.JToggleButton();

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

        attributePanel.setOpaque(false);
        attributePanel.setLayout(new java.awt.GridBagLayout());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        attributePanel.add(attibuteBox, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 1.0;
        mainPanel.add(attributePanel, gridBagConstraints);

        centerPanel.setOpaque(false);
        centerPanel.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        mainPanel.add(centerPanel, gridBagConstraints);

        controlToolbar.setFloatable(false);
        controlToolbar.setRollover(true);
        controlToolbar.setMargin(new java.awt.Insets(0, 4, 0, 0));
        controlToolbar.setOpaque(false);

        localScaleButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/gephi/desktop/appearance/resources/funnel.png"))); // NOI18N
        localScaleButton.setToolTipText(org.openide.util.NbBundle.getMessage(AppearanceTopComponent.class, "AppearanceTopComponent.localScaleButton.toolTipText")); // NOI18N
        localScaleButton.setFocusable(false);
        controlToolbar.add(localScaleButton);

        org.openide.awt.Mnemonics.setLocalizedText(splineButton, org.openide.util.NbBundle.getMessage(AppearanceTopComponent.class, "AppearanceTopComponent.splineButton.text")); // NOI18N
        splineButton.setToolTipText(org.openide.util.NbBundle.getMessage(AppearanceTopComponent.class, "AppearanceTopComponent.splineButton.toolTipText")); // NOI18N
        splineButton.setClickedColor(new java.awt.Color(0, 51, 255));
        splineButton.setFocusPainted(false);
        splineButton.setFocusable(false);
        splineButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        splineButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        controlToolbar.add(splineButton);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LAST_LINE_END;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 3);
        mainPanel.add(controlToolbar, gridBagConstraints);

        controlPanel.setOpaque(false);
        controlPanel.setLayout(new java.awt.GridBagLayout());

        applyButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/gephi/desktop/appearance/resources/apply.gif"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(applyButton, org.openide.util.NbBundle.getMessage(AppearanceTopComponent.class, "AppearanceTopComponent.applyButton.text")); // NOI18N
        applyButton.setToolTipText(org.openide.util.NbBundle.getMessage(AppearanceTopComponent.class, "AppearanceTopComponent.applyButton.toolTipText")); // NOI18N
        applyButton.setMargin(new java.awt.Insets(0, 14, 0, 14));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHEAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 18, 3, 5);
        controlPanel.add(applyButton, gridBagConstraints);

        stopAutoApplyButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/gephi/desktop/appearance/resources/stop.png"))); // NOI18N
        stopAutoApplyButton.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(stopAutoApplyButton, org.openide.util.NbBundle.getMessage(AppearanceTopComponent.class, "AppearanceTopComponent.stopAutoApplyButton.text")); // NOI18N
        stopAutoApplyButton.setToolTipText(org.openide.util.NbBundle.getMessage(AppearanceTopComponent.class, "AppearanceTopComponent.stopAutoApplyButton.toolTipText")); // NOI18N
        stopAutoApplyButton.setFocusable(false);
        stopAutoApplyButton.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        stopAutoApplyButton.setMargin(new java.awt.Insets(0, 7, 0, 7));
        stopAutoApplyButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHEAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 3, 3, 5);
        controlPanel.add(stopAutoApplyButton, gridBagConstraints);

        autoApplyToolbar.setFloatable(false);
        autoApplyToolbar.setRollover(true);
        autoApplyToolbar.setOpaque(false);

        enableAutoButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/gephi/desktop/appearance/resources/chain.png"))); // NOI18N
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

        autoApplyButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/gephi/desktop/appearance/resources/apply.gif"))); // NOI18N
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

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        mainPanel.add(controlPanel, gridBagConstraints);

        add(mainPanel, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton applyButton;
    private javax.swing.JComboBox attibuteBox;
    private javax.swing.JPanel attributePanel;
    private javax.swing.JToggleButton autoApplyButton;
    private javax.swing.JToolBar autoApplyToolbar;
    private javax.swing.JToolBar categoryToolbar;
    private javax.swing.JPanel centerPanel;
    private javax.swing.JPanel controlPanel;
    private javax.swing.JToolBar controlToolbar;
    private javax.swing.JToggleButton enableAutoButton;
    private javax.swing.JToggleButton localScaleButton;
    private javax.swing.JPanel mainPanel;
    private org.jdesktop.swingx.JXHyperlink splineButton;
    private javax.swing.JToggleButton stopAutoApplyButton;
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
