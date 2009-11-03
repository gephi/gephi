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
package org.gephi.ui.partition;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;
import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JToolBar;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.gephi.partition.api.PartitionModel;
import org.gephi.ui.utils.UIUtils;
import org.openide.util.NbBundle;

/**
 *
 * @author Mathieu Bastian
 */
public class PartitionToolbar extends JToolBar implements ChangeListener {

    //Architecture
    private PartitionModel model;

    public PartitionToolbar() {
        initComponents();
        initTransformersUI();
    }

    public void setup(PartitionModel model) {
        model.addChangeListener(this);
    }

    public void unsetup() {
        model.removeChangeListener(this);
    }

    private void refreshModel() {
        boolean nodeSelected = model.getSelectedPartitioning() == PartitionModel.NODE_PARTITIONING;
        boolean edgeSelected = !nodeSelected;
        elementGroup.setSelected(nodeSelected ? nodeButton.getModel() : edgeButton.getModel(), true);

    /*for (Enumeration<AbstractButton> btns = nodeTransformerGroup.getElements(); btns.hasMoreElements();) {
    AbstractButton btn = btns.nextElement();
    btn.setVisible(nodeSelected);
    if (btn.getName().equals(model.getNodeTransformer().getSimpleName())) {
    nodeTransformerGroup.setSelected(btn.getModel(), true);
    }
    }
    for (Enumeration<AbstractButton> btns = edgeTransformerGroup.getElements(); btns.hasMoreElements();) {
    AbstractButton btn = btns.nextElement();
    btn.setVisible(edgeSelected);
    if (btn.getName().equals(model.getEdgeTransformer().getSimpleName())) {
    edgeTransformerGroup.setSelected(btn.getModel(), true);
    }
    }*/
    }

    public void stateChanged(ChangeEvent e) {
        refreshModel();
    }

    private void initTransformersUI() {
        nodeTransformerGroup = new ButtonGroup();
        edgeTransformerGroup = new ButtonGroup();
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
        nodeButton.setText(NbBundle.getMessage(PartitionToolbar.class, "RankingToolbar.nodes.label"));
        nodeButton.setEnabled(false);
        add(nodeButton);

        elementGroup.add(edgeButton);
        edgeButton.setText(NbBundle.getMessage(PartitionToolbar.class, "RankingToolbar.edges.label"));
        edgeButton.setEnabled(false);
        add(edgeButton);
        addSeparator();

        box.setMaximumSize(new java.awt.Dimension(32767, 32767));
        add(box);

        nodeButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
            }
        });
        edgeButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
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
