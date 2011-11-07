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
package org.gephi.desktop.ranking;

import java.awt.Component;
import java.beans.PropertyChangeEvent;
import javax.swing.JList;
import org.gephi.ranking.spi.TransformerUI;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.Comparator;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.gephi.ranking.api.Interpolator;
import org.gephi.ranking.api.Ranking;
import org.gephi.ranking.api.RankingController;
import org.gephi.ranking.api.Transformer;
import org.gephi.ui.components.SplineEditor.SplineEditor;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Mathieu Bastian
 */
public class RankingChooser extends javax.swing.JPanel implements PropertyChangeListener {

    private final String NO_SELECTION;
    private final ItemListener rankingItemListener;
    private final RankingUIController controller;
    private RankingUIModel model;
    private JPanel centerPanel;
    //Spline
    private SplineEditor splineEditor;
    private Interpolator interpolator;

    public RankingChooser(RankingUIController controller) {
        NO_SELECTION = NbBundle.getMessage(RankingChooser.class, "RankingChooser.choose.text");
        this.controller = controller;
        initComponents();
        initControls();

        rankingItemListener = new ItemListener() {

            public void itemStateChanged(ItemEvent e) {
                if (model != null) {
                    if (!rankingComboBox.getSelectedItem().equals(NO_SELECTION)) {
                        model.setCurrentRanking((Ranking) rankingComboBox.getSelectedItem());
                    } else {
                        model.setCurrentRanking(null);
                    }
                }
            }
        };
        rankingComboBox.setRenderer(new RankingListCellRenderer());
    }

    public void refreshModel(RankingUIModel model) {
        if (this.model != null) {
            this.model.removePropertyChangeListener(this);
        }
        this.model = model;
        if (model != null) {
            model.addPropertyChangeListener(this);
        }

        refreshModel();
    }

    private void refreshModel() {
        //CenterPanel
        if (centerPanel != null) {
            remove(centerPanel);
        }
        applyButton.setVisible(false);
        autoApplyButton.setVisible(false);
        enableAutoButton.setVisible(false);
        splineButton.setVisible(false);

        if (model != null) {

            //Ranking
            Ranking selectedRanking = refreshCombo();

            if (selectedRanking != null) {
                refreshTransformerPanel(selectedRanking);
            }
        }

        revalidate();
        repaint();
    }

    public void propertyChange(PropertyChangeEvent pce) {
        if (pce.getPropertyName().equals(RankingUIModel.CURRENT_ELEMENT_TYPE)) {
            refreshModel();
        } else if (pce.getPropertyName().equals(RankingUIModel.CURRENT_RANKING)
                || pce.getPropertyName().equals(RankingUIModel.CURRENT_TRANSFORMER)) {

            final Ranking selectedRanking = model.getCurrentRanking();
            //CenterPanel
            if (centerPanel != null) {
                remove(centerPanel);
            }
            applyButton.setVisible(false);
            autoApplyButton.setVisible(false);
            enableAutoButton.setVisible(false);
            splineButton.setVisible(false);

            if (selectedRanking != null) {
                refreshTransformerPanel(selectedRanking);
                if (rankingComboBox.getSelectedItem() != selectedRanking) {
                    refreshCombo();
                }
            }

            revalidate();
            repaint();
        } else if (pce.getPropertyName().equals(RankingUIModel.RANKINGS)) {
            refreshCombo();
        }
    }

    private void refreshTransformerPanel(Ranking selectedRanking) {
        Transformer transformer = model.getCurrentTransformer();
        boolean autoTransformer = model.isAutoTransformer(transformer);
        TransformerUI transformerUI = controller.getUI(transformer);
        if (!Double.isNaN(selectedRanking.getMinimumValue().doubleValue())
                && !Double.isNaN(selectedRanking.getMaximumValue().doubleValue())
                && selectedRanking.getMinimumValue() != selectedRanking.getMaximumValue()) {
            applyButton.setEnabled(true);
        } else {
            applyButton.setEnabled(false);
        }
        centerPanel = transformerUI.getPanel(transformer, selectedRanking);
        centerPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(5, 5, 0, 5), BorderFactory.createEtchedBorder()));
        centerPanel.setOpaque(false);
        add(centerPanel, BorderLayout.CENTER);
        splineButton.setVisible(true);
        if (autoTransformer) {
            autoApplyButton.setVisible(true);
            enableAutoButton.setSelected(true);
            setAutoApplySelected(true);
            autoApplyButton.setSelected(true);
        } else {
            applyButton.setVisible(true);
            enableAutoButton.setSelected(false);
        }
        enableAutoButton.setVisible(true);
    }

    private Ranking refreshCombo() {
        //Ranking
        Ranking selectedRanking = model.getCurrentRanking();
        rankingComboBox.removeItemListener(rankingItemListener);
        final DefaultComboBoxModel comboBoxModel = new DefaultComboBoxModel();
        comboBoxModel.addElement(NO_SELECTION);
        comboBoxModel.setSelectedItem(NO_SELECTION);
        Ranking[] rankings = model.getRankings();
        Arrays.sort(rankings, new Comparator() {

            public int compare(Object o1, Object o2) {
                return ((Ranking) o1).getDisplayName().compareTo(((Ranking) o2).getDisplayName());
            }
        });
        for (Ranking r : rankings) {
            comboBoxModel.addElement(r);
            if (selectedRanking != null && selectedRanking.getName().equals(r.getName())) {
                comboBoxModel.setSelectedItem(r);
            }
        }
        selectedRanking = model.getCurrentRanking();    //May have been refresh by the model
        rankingComboBox.addItemListener(rankingItemListener);
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                rankingComboBox.setModel(comboBoxModel);
            }
        });
        return selectedRanking;
    }

    private void initControls() {
        applyButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                Transformer transformer = model.getCurrentTransformer();
                if (transformer != null) {
                    RankingController rankingController = Lookup.getDefault().lookup(RankingController.class);
                    if (interpolator != null) {
                        rankingController.setInterpolator(new org.gephi.ranking.api.Interpolator() {

                            public float interpolate(float x) {
                                return interpolator.interpolate(x);
                            }
                        });
                    }
                    rankingController.transform(model.getCurrentRanking(), transformer);
                }
            }
        });

        splineButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (splineEditor == null) {
                    splineEditor = new SplineEditor(NbBundle.getMessage(RankingChooser.class, "RankingChooser.splineEditor.title"));
                }
                splineEditor.setVisible(true);
                interpolator = splineEditor.getCurrentInterpolator();
                RankingController rankingController = Lookup.getDefault().lookup(RankingController.class);
                rankingController.setInterpolator(new org.gephi.ranking.api.Interpolator() {

                    public float interpolate(float x) {
                        return interpolator.interpolate(x);
                    }
                });
            }
        });
        autoApplyButton.setVisible(false);
        enableAutoButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                if (enableAutoButton.isSelected()) {
                    autoApplyButton.setVisible(true);
                    setAutoApplySelected(false);
                    autoApplyButton.setSelected(false);
                    applyButton.setVisible(false);
                } else {
                    autoApplyButton.setVisible(false);
                    applyButton.setVisible(true);
                    model.setAutoTransformer(model.getCurrentTransformer(), false);
                }

            }
        });
        autoApplyButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                if (interpolator != null) {
                    RankingController rankingController = Lookup.getDefault().lookup(RankingController.class);
                    rankingController.setInterpolator(new org.gephi.ranking.api.Interpolator() {

                        public float interpolate(float x) {
                            return interpolator.interpolate(x);
                        }
                    });
                }
                model.setAutoTransformer(model.getCurrentTransformer(), autoApplyButton.isSelected());
                setAutoApplySelected(autoApplyButton.isSelected());
            }
        });
    }

    @Override
    public void setEnabled(boolean enabled) {
        applyButton.setEnabled(enabled);
        rankingComboBox.setEnabled(enabled);
        splineButton.setEnabled(enabled);
        autoApplyButton.setEnabled(enabled);
        autoApplyToolbar.setEnabled(enabled);
    }

    private void setAutoApplySelected(boolean selected) {
        if (!selected) {
            autoApplyButton.setIcon(ImageUtilities.loadImageIcon("org/gephi/desktop/ranking/resources/apply.gif", false));
            autoApplyButton.setToolTipText(NbBundle.getMessage(RankingChooser.class, "RankingChooser.autoApplyButton.toolTipText"));
        } else {
            autoApplyButton.setIcon(ImageUtilities.loadImageIcon("org/gephi/desktop/layout/resources/stop.png", false));
            autoApplyButton.setToolTipText(NbBundle.getMessage(RankingChooser.class, "RankingChooser.autoApplyButton.stop.toolTipText"));
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        chooserPanel = new javax.swing.JPanel();
        rankingComboBox = new javax.swing.JComboBox();
        controlPanel = new javax.swing.JPanel();
        applyButton = new javax.swing.JButton();
        splineButton = new org.jdesktop.swingx.JXHyperlink();
        autoApplyButton = new javax.swing.JToggleButton();
        autoApplyToolbar = new javax.swing.JToolBar();
        enableAutoButton = new javax.swing.JToggleButton();

        setOpaque(false);
        setLayout(new java.awt.BorderLayout());

        chooserPanel.setOpaque(false);
        chooserPanel.setLayout(new java.awt.GridBagLayout());

        rankingComboBox.setToolTipText(org.openide.util.NbBundle.getMessage(RankingChooser.class, "RankingChooser.rankingComboBox.toolTipText")); // NOI18N
        rankingComboBox.setPreferredSize(new java.awt.Dimension(56, 25));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        chooserPanel.add(rankingComboBox, gridBagConstraints);

        add(chooserPanel, java.awt.BorderLayout.PAGE_START);

        controlPanel.setOpaque(false);
        controlPanel.setLayout(new java.awt.GridBagLayout());

        applyButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/gephi/desktop/ranking/resources/apply.gif"))); // NOI18N
        applyButton.setText(org.openide.util.NbBundle.getMessage(RankingChooser.class, "RankingChooser.applyButton.text")); // NOI18N
        applyButton.setToolTipText(org.openide.util.NbBundle.getMessage(RankingChooser.class, "RankingChooser.applyButton.toolTipText")); // NOI18N
        applyButton.setMargin(new java.awt.Insets(0, 14, 0, 14));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHEAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 18, 3, 5);
        controlPanel.add(applyButton, gridBagConstraints);

        splineButton.setClickedColor(new java.awt.Color(0, 51, 255));
        splineButton.setText(org.openide.util.NbBundle.getMessage(RankingChooser.class, "RankingChooser.splineButton.text")); // NOI18N
        splineButton.setToolTipText(org.openide.util.NbBundle.getMessage(RankingChooser.class, "RankingChooser.splineButton.toolTipText")); // NOI18N
        splineButton.setFocusPainted(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        controlPanel.add(splineButton, gridBagConstraints);

        autoApplyButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/gephi/desktop/ranking/resources/apply.gif"))); // NOI18N
        autoApplyButton.setText(org.openide.util.NbBundle.getMessage(RankingChooser.class, "RankingChooser.autoApplyButton.text")); // NOI18N
        autoApplyButton.setToolTipText(org.openide.util.NbBundle.getMessage(RankingChooser.class, "RankingChooser.autoApplyButton.toolTipText")); // NOI18N
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

        enableAutoButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/gephi/desktop/ranking/resources/chain.png"))); // NOI18N
        enableAutoButton.setToolTipText(org.openide.util.NbBundle.getMessage(RankingChooser.class, "RankingChooser.enableAutoButton.toolTipText")); // NOI18N
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

        add(controlPanel, java.awt.BorderLayout.PAGE_END);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton applyButton;
    private javax.swing.JToggleButton autoApplyButton;
    private javax.swing.JToolBar autoApplyToolbar;
    private javax.swing.JPanel chooserPanel;
    private javax.swing.JPanel controlPanel;
    private javax.swing.JToggleButton enableAutoButton;
    private javax.swing.JComboBox rankingComboBox;
    private org.jdesktop.swingx.JXHyperlink splineButton;
    // End of variables declaration//GEN-END:variables

    private class RankingListCellRenderer extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(JList jlist, Object o, int i, boolean bln, boolean bln1) {
            if (o instanceof Ranking) {
                return super.getListCellRendererComponent(jlist, ((Ranking) o).getDisplayName(), i, bln, bln1);
            } else {
                return super.getListCellRendererComponent(jlist, o, i, bln, bln1);
            }
        }
    }
}
