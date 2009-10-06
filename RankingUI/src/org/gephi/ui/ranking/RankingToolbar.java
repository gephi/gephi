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

import java.awt.Component;
import org.gephi.ui.ranking.TransformerUI;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.UIManager;
import javax.swing.border.Border;
import org.gephi.ranking.RankingUIModel;
import org.gephi.ui.utils.UIUtils;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Mathieu Bastian
 */
public class RankingToolbar extends JToolBar {

    private RankingUIModel model;

    public RankingToolbar(RankingUIModel model) {
        this.model = model;
        initComponents();
        initTransformersUI();
        model.addPropertyChangeListener(new PropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName().equals("nodeTransformer") ||
                        evt.getPropertyName().equals("edgeTransformer") ||
                        evt.getPropertyName().equals("ranking")) {
                    refreshModel();
                }
            }
        });
        refreshModel();
    }

    private void refreshModel() {
        boolean nodeSelected = model.getRanking() == RankingUIModel.NODE_RANKING;
        boolean edgeSelected = !nodeSelected;
        elementGroup.setSelected(nodeSelected ? nodeButton.getModel() : edgeButton.getModel(), true);

        for (Enumeration<AbstractButton> btns = nodeTransformerGroup.getElements(); btns.hasMoreElements();) {
            AbstractButton btn = btns.nextElement();
            btn.setVisible(nodeSelected);
            if (btn.getName().equals(model.getNodeTransformer())) {
                nodeTransformerGroup.setSelected(btn.getModel(), true);
            }
        }
        for (Enumeration<AbstractButton> btns = edgeTransformerGroup.getElements(); btns.hasMoreElements();) {
            AbstractButton btn = btns.nextElement();
            btn.setVisible(edgeSelected);
            if (btn.getName().equals(model.getEdgeTransformer())) {
                edgeTransformerGroup.setSelected(btn.getModel(), true);
            }
        }
    }

    private void initTransformersUI() {
        nodeTransformerGroup = new ButtonGroup();
        edgeTransformerGroup = new ButtonGroup();
        List<TransformerUI> nodeTrans = new ArrayList<TransformerUI>();
        List<TransformerUI> edgeTrans = new ArrayList<TransformerUI>();
        TransformerUI[] allTrans = Lookup.getDefault().lookupAll(TransformerUI.class).toArray(new TransformerUI[0]);
        for (TransformerUI t : allTrans) {
            if (t.isNodeTransformer()) {
                nodeTrans.add(t);
            }
            if (t.isEdgeTransformer()) {
                edgeTrans.add(t);
            }
        }

        for (final TransformerUI t : nodeTrans) {
            JToggleButton btn = new JToggleButton(t.getIcon());
            btn.setToolTipText(t.getName());
            btn.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    model.setNodeTransformer(t.getTransformerClass().getSimpleName());
                }
            });
            btn.setName(t.getTransformerClass().getSimpleName());
            btn.setFocusPainted(false);
            nodeTransformerGroup.add(btn);
            add(btn);
        }

        for (final TransformerUI t : edgeTrans) {
            JToggleButton btn = new JToggleButton(t.getIcon());
            btn.setToolTipText(t.getName());
            btn.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    model.setEdgeTransformer(t.getTransformerClass().getSimpleName());
                }
            });
            btn.setName(t.getTransformerClass().getSimpleName());
            btn.setFocusPainted(false);
            edgeTransformerGroup.add(btn);
            add(btn);
        }

        //Init first
        if (!nodeTrans.isEmpty()) {
            model.setNodeTransformer(nodeTrans.get(0).getTransformerClass().getSimpleName());
        }
        if (!edgeTrans.isEmpty()) {
            model.setEdgeTransformer(edgeTrans.get(0).getTransformerClass().getSimpleName());
        }
    }

    private void initComponents() {
        elementGroup = new javax.swing.ButtonGroup();
        nodeButton = new javax.swing.JToggleButton();
        edgeButton = new javax.swing.JToggleButton();
        nodeButton.setFocusPainted(false);
        edgeButton.setFocusPainted(false);
        box = new javax.swing.JLabel();

        setFloatable(false);
        setRollover(true);
        Border b = (Border) UIManager.get("Nb.Editor.Toolbar.border"); //NOI18N
        setBorder(b);
        if (UIUtils.isAquaLookAndFeel()) {
            setBackground(UIManager.getColor("NbExplorerView.background"));
        }

        elementGroup.add(nodeButton);
        nodeButton.setText(NbBundle.getMessage(RankingToolbar.class, "RankingToolbar.nodes.label"));
        nodeButton.setEnabled(false);
        add(nodeButton);

        elementGroup.add(edgeButton);
        edgeButton.setText(NbBundle.getMessage(RankingToolbar.class, "RankingToolbar.edges.label"));
        edgeButton.setEnabled(false);
        add(edgeButton);
        addSeparator();

        box.setMaximumSize(new java.awt.Dimension(32767, 32767));
        add(box);

        nodeButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                model.setRanking(RankingUIModel.NODE_RANKING);
            }
        });
        edgeButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                model.setRanking(RankingUIModel.EDGE_RANKING);
            }
        });
    }

    @Override
    public void setEnabled(boolean enabled) {
        for (Component c : getComponents()) {
            c.setEnabled(enabled);
        }
    }
    private javax.swing.JLabel box;
    private javax.swing.JToggleButton edgeButton;
    private javax.swing.ButtonGroup elementGroup;
    private javax.swing.ButtonGroup nodeTransformerGroup;
    private javax.swing.ButtonGroup edgeTransformerGroup;
    private javax.swing.JToggleButton nodeButton;
}
