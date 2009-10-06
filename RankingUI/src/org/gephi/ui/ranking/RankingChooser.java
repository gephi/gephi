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
package org.gephi.ui.ranking;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JPanel;
import org.gephi.ranking.Ranking;
import org.gephi.ranking.RankingUIModel;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;

/**
 *
 * @author Mathieu Bastian
 */
public class RankingChooser extends javax.swing.JPanel {

    private final String NO_SELECTION;
    private RankingUIModel model;
    private Lookup nodeRankingLookup;
    private Lookup edgeRankingLookup;
    private JPanel centerPanel;
    private Ranking selectedRanking;

    public RankingChooser(RankingUIModel model, Lookup nodeRankingLookup, Lookup edgeRankingLookup) {
        this.model = model;
        this.nodeRankingLookup = nodeRankingLookup;
        this.edgeRankingLookup = edgeRankingLookup;
        NO_SELECTION = "----------------";
        initComponents();
        initRanking();
    }

    private void initRanking() {
        DefaultComboBoxModel comboBoxModel = new DefaultComboBoxModel();
        comboBoxModel.addElement(NO_SELECTION);
        comboBoxModel.setSelectedItem(NO_SELECTION);
        rankingComboBox.setModel(comboBoxModel);
        rankingComboBox.addItemListener(new ItemListener() {

            public void itemStateChanged(ItemEvent e) {
                if (!rankingComboBox.getSelectedItem().equals(getSelectedRanking())) {
                    if (!rankingComboBox.getSelectedItem().equals(NO_SELECTION)) {
                        setSelectedRanking((String) rankingComboBox.getSelectedItem());
                    } else {
                        setSelectedRanking(null);
                    }
                    resetTransformers();
                    refreshModel();
                }
            }
        });
        nodeRankingLookup.lookupResult(Ranking.class).addLookupListener(new LookupListener() {

            public void resultChanged(LookupEvent ev) {
                refreshModel();
            }
        });
        edgeRankingLookup.lookupResult(Ranking.class).addLookupListener(new LookupListener() {

            public void resultChanged(LookupEvent ev) {
                refreshModel();
            }
        });
    }

    private synchronized void refreshModel() {
        refreshSelectedRankings();
        Ranking[] rankings = new Ranking[0];
        if (model.getRanking() == RankingUIModel.NODE_RANKING) {
            rankings = nodeRankingLookup.lookupAll(Ranking.class).toArray(new Ranking[0]);
        } else {
            rankings = edgeRankingLookup.lookupAll(Ranking.class).toArray(new Ranking[0]);
        }
        //Ranking list
        DefaultComboBoxModel comboBoxModel = new DefaultComboBoxModel();
        comboBoxModel.addElement(NO_SELECTION);
        comboBoxModel.setSelectedItem(NO_SELECTION);
        for (Ranking r : rankings) {
            String elem = r.toString();
            comboBoxModel.addElement(elem);
            if (selectedRanking == r) {
                comboBoxModel.setSelectedItem(elem);
            }
        }
        rankingComboBox.setModel(comboBoxModel);

        //CenterPanel
        if (centerPanel != null) {
            remove(centerPanel);
        }

        System.out.println(getSelectedRanking() + " " + model.getNodeTransformer());
    }

    private void refreshSelectedRankings() {
        selectedRanking = null;
        if (model.getRanking() == RankingUIModel.NODE_RANKING) {
            if (model.getSelectedNodeRanking() != null) {
                for (Ranking r : nodeRankingLookup.lookupAll(Ranking.class)) {
                    String elem = r.toString();
                    if (elem.equals(model.getSelectedNodeRanking())) {
                        selectedRanking = r;
                    }
                }
            }
            model.setSelectedNodeRanking(selectedRanking.toString());
        } else {
            if (model.getSelectedEdgeRanking() != null) {
                for (Ranking r : edgeRankingLookup.lookupAll(Ranking.class)) {
                    String elem = r.toString();
                    if (elem.equals(model.getSelectedEdgeRanking())) {
                        selectedRanking = r;
                    }
                }
            }
            model.setSelectedEdgeRanking(selectedRanking.toString());
        }
    }

    private String getSelectedRanking() {
        if (model.getRanking() == RankingUIModel.NODE_RANKING) {
            return model.getSelectedNodeRanking();
        } else {
            return model.getSelectedEdgeRanking();
        }
    }

    private void setSelectedRanking(String selectedRanking) {
        if (model.getRanking() == RankingUIModel.NODE_RANKING) {
            model.setSelectedNodeRanking(selectedRanking);
        } else {
            model.setSelectedEdgeRanking(selectedRanking);
        }
    }

    private void resetTransformers() {
        if (model.getRanking() == RankingUIModel.NODE_RANKING) {
            model.resetNodeTransformers();
        } else {
            model.resetEdgeTransformers();
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

        setLayout(new java.awt.BorderLayout());

        chooserPanel.setLayout(new java.awt.GridBagLayout());

        rankingComboBox.setPreferredSize(new java.awt.Dimension(56, 25));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        chooserPanel.add(rankingComboBox, gridBagConstraints);

        add(chooserPanel, java.awt.BorderLayout.PAGE_START);

        controlPanel.setLayout(new java.awt.GridBagLayout());

        applyButton.setText(org.openide.util.NbBundle.getMessage(RankingChooser.class, "RankingChooser.applyButton.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        controlPanel.add(applyButton, gridBagConstraints);

        add(controlPanel, java.awt.BorderLayout.PAGE_END);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton applyButton;
    private javax.swing.JPanel chooserPanel;
    private javax.swing.JPanel controlPanel;
    private javax.swing.JComboBox rankingComboBox;
    // End of variables declaration//GEN-END:variables
}
