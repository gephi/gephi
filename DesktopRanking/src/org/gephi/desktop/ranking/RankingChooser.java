/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
*/
package org.gephi.desktop.ranking;

import org.gephi.ranking.api.TransformerUI;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.gephi.ranking.api.Ranking;
import org.gephi.ranking.api.RankingController;
import org.gephi.ranking.api.RankingModel;
import org.gephi.ranking.api.RankingUIModel;
import org.gephi.ranking.api.Transformer;
import org.gephi.ui.components.SplineEditor.SplineEditor;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Mathieu Bastian
 */
public class RankingChooser extends javax.swing.JPanel {

    private final String NO_SELECTION;
    private RankingUIModel modelUI;
    private RankingModel model;
    private JPanel centerPanel;
    private Ranking selectedRanking;
    private TransformerUI[] transformerUIs;
    //Spline
    private SplineEditor splineEditor;
    private org.jdesktop.animation.timing.interpolation.Interpolator interpolator;

    public RankingChooser(RankingUIModel modelUI, RankingModel rankingModel) {
        this.modelUI = modelUI;
        this.model = rankingModel;
        NO_SELECTION = NbBundle.getMessage(RankingChooser.class, "RankingChooser.choose.text");
        initComponents();
        initRanking();
        initApply();
    }

    private void initRanking() {
        DefaultComboBoxModel comboBoxModel = new DefaultComboBoxModel();
        comboBoxModel.addElement(NO_SELECTION);
        comboBoxModel.setSelectedItem(NO_SELECTION);
        rankingComboBox.setModel(comboBoxModel);
        rankingComboBox.addItemListener(new ItemListener() {

            public void itemStateChanged(ItemEvent e) {
                if (!rankingComboBox.getSelectedItem().equals(getSelectedRanking())) {
                    resetTransformers();
                    if (!rankingComboBox.getSelectedItem().equals(NO_SELECTION)) {
                        setSelectedRanking((String) rankingComboBox.getSelectedItem());
                    } else {
                        setSelectedRanking(null);
                    }

                    //refreshModel();
                }
            }
        });
        model.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                refreshModel();
            }
        });
        modelUI.addPropertyChangeListener(new PropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName().equals("ranking")
                        || evt.getPropertyName().equals("nodeTransformer")
                        || evt.getPropertyName().equals("edgeTransformer")
                        || evt.getPropertyName().equals("selectedNodeRanking")
                        || evt.getPropertyName().equals("selectedEdgeRanking")) {
                    refreshModel();
                }
            }
        });
        transformerUIs = Lookup.getDefault().lookupAll(TransformerUI.class).toArray(new TransformerUI[0]);
    }

    private void initApply() {
        applyButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                Transformer transformer = getSelectedTransformer();
                if (transformer != null) {
                    RankingController rankingController = Lookup.getDefault().lookup(RankingController.class);
                    if (interpolator != null) {
                        transformer.setInterpolator(new org.gephi.ranking.api.Interpolator() {

                            public float interpolate(float x) {
                                return interpolator.interpolate(x);
                            }
                        });
                    }
                    rankingController.transform(transformer);
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
            }
        });
    }

    public synchronized void refreshModel() {
        refreshSelectedRankings();
        Ranking[] rankings = new Ranking[0];
        if (modelUI.getRanking() == RankingUIModel.NODE_RANKING) {
            rankings = model.getNodeRanking();
        } else {
            rankings = model.getEdgeRanking();
        }
        //Ranking list
        DefaultComboBoxModel comboBoxModel = new DefaultComboBoxModel();
        comboBoxModel.addElement(NO_SELECTION);
        comboBoxModel.setSelectedItem(NO_SELECTION);
        for (Ranking r : rankings) {
            String elem = r.toString();
            comboBoxModel.addElement(elem);
            if (selectedRanking != null && selectedRanking.toString().equals(r.toString())) {
                comboBoxModel.setSelectedItem(elem);
            }
        }
        rankingComboBox.setModel(comboBoxModel);

        //CenterPanel
        if (centerPanel != null) {
            remove(centerPanel);
        }
        applyButton.setVisible(false);
        splineButton.setVisible(false);

        if (selectedRanking != null) {
            Transformer transformer = getSelectedTransformer();
            TransformerUI transformerUI;
            if (transformer != null) {
                //Saved Transformer in the model
                transformerUI = getUIForTransformer(transformer);
            } else {
                transformerUI = getUIForTransformer();
                if (transformerUI != null) {
                    transformer = transformerUI.buildTransformer(selectedRanking);     //Create transformer
                    addTransformer(transformer);
                }
            }
            centerPanel = transformerUI.getPanel(transformer, selectedRanking);
            centerPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(5, 5, 0, 5), BorderFactory.createEtchedBorder()));
            centerPanel.setOpaque(false);
            add(centerPanel, BorderLayout.CENTER);
            applyButton.setVisible(true);
            splineButton.setVisible(true);
        }

        revalidate();
        repaint();
    }

    private void refreshSelectedRankings() {
        selectedRanking = null;
        if (modelUI.getRanking() == RankingUIModel.NODE_RANKING) {
            if (modelUI.getSelectedNodeRanking() != null) {
                for (Ranking r : model.getNodeRanking()) {
                    String elem = r.toString();
                    if (elem.equals(modelUI.getSelectedNodeRanking())) {
                        selectedRanking = r;
                        break;
                    }
                }
            }
            if (selectedRanking != null) {
                modelUI.setSelectedNodeRanking(selectedRanking.toString());
            } else {
                modelUI.setSelectedNodeRanking(null);
            }
        } else {
            if (modelUI.getSelectedEdgeRanking() != null) {
                for (Ranking r : model.getEdgeRanking()) {
                    String elem = r.toString();
                    if (elem.equals(modelUI.getSelectedEdgeRanking())) {
                        selectedRanking = r;
                        break;
                    }
                }
            }
            if (selectedRanking != null) {
                modelUI.setSelectedEdgeRanking(selectedRanking.toString());
            } else {
                modelUI.setSelectedEdgeRanking(null);
            }
        }
    }

    private String getSelectedRanking() {
        if (modelUI.getRanking() == RankingUIModel.NODE_RANKING) {
            return modelUI.getSelectedNodeRanking();
        } else {
            return modelUI.getSelectedEdgeRanking();
        }
    }

    private void setSelectedRanking(String selectedRanking) {
        if (modelUI.getRanking() == RankingUIModel.NODE_RANKING) {
            modelUI.setSelectedNodeRanking(selectedRanking);
        } else {
            modelUI.setSelectedEdgeRanking(selectedRanking);
        }
    }

    private void resetTransformers() {
        if (modelUI.getRanking() == RankingUIModel.NODE_RANKING) {
            modelUI.resetNodeTransformers();
        } else {
            modelUI.resetEdgeTransformers();
        }
    }

    private Transformer getSelectedTransformer() {
        if (modelUI.getRanking() == RankingUIModel.NODE_RANKING) {
            return modelUI.getSelectedNodeTransformer();
        } else {
            return modelUI.getSelectedEdgeTransformer();
        }
    }

    private TransformerUI getUIForTransformer(Transformer transformer) {
        if (transformer != null) {
            for (TransformerUI u : transformerUIs) {
                if (u.getTransformerClass().isAssignableFrom(transformer.getClass())) {
                    return u;
                }
            }
        }
        return null;
    }

    private TransformerUI getUIForTransformer() {
        Class classTransformer;
        if (modelUI.getRanking() == RankingUIModel.NODE_RANKING) {
            classTransformer = modelUI.getNodeTransformer();
        } else {
            classTransformer = modelUI.getEdgeTransformer();
        }
        for (TransformerUI u : transformerUIs) {
            if (u.getTransformerClass().equals(classTransformer)) {
                return u;
            }
        }
        return null;
    }

    private void addTransformer(Transformer transformer) {
        if (modelUI.getRanking() == RankingUIModel.NODE_RANKING) {
            modelUI.addNodeTransformer(transformer);
        } else {
            modelUI.addEdgeTransformer(transformer);
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        applyButton.setEnabled(enabled);
        rankingComboBox.setEnabled(enabled);
        splineButton.setEnabled(enabled);
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
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHEAST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 3, 5);
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

        add(controlPanel, java.awt.BorderLayout.PAGE_END);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton applyButton;
    private javax.swing.JPanel chooserPanel;
    private javax.swing.JPanel controlPanel;
    private javax.swing.JComboBox rankingComboBox;
    private org.jdesktop.swingx.JXHyperlink splineButton;
    // End of variables declaration//GEN-END:variables
}
