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
package org.gephi.desktop.partition;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Enumeration;
import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.gephi.partition.api.PartitionController;
import org.gephi.partition.api.PartitionModel;
import org.gephi.partition.api.TransformerBuilder;
import org.gephi.partition.api.TransformerUI;
import org.gephi.ui.utils.UIUtils;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Mathieu Bastian
 */
public class PartitionToolbar extends JToolBar implements PropertyChangeListener {

    //Architecture
    private PartitionModel model;

    public PartitionToolbar() {
        initComponents();
        initTransformersUI();
        refreshModel();
    }

    public void setup(PartitionModel model) {
        if (model != null) {
            this.model = model;
            model.addPropertyChangeListener(this);
            refreshModel();
        }
    }

    public void unsetup() {
        if (model != null) {
            model.removePropertyChangeListener(this);
            model = null;
        }
        refreshModel();
    }

    private void refreshModel() {
        if (model == null) {
            setEnabled(false);
            return;
        } else {
            setEnabled(true);
        }
        boolean nodeSelected = model.getSelectedPartitioning() == PartitionModel.NODE_PARTITIONING;
        boolean edgeSelected = !nodeSelected;
        elementGroup.setSelected(nodeSelected ? nodeButton.getModel() : edgeButton.getModel(), true);

        nodeTransformerGroup.clearSelection();
        edgeTransformerGroup.clearSelection();

        for (Enumeration<AbstractButton> btns = nodeTransformerGroup.getElements(); btns.hasMoreElements();) {
            AbstractButton btn = btns.nextElement();
            btn.setVisible(nodeSelected);
            if (model.getNodeTransformerBuilder() != null && btn.getName().equals(model.getNodeTransformerBuilder().getClass().getName())) {
                nodeTransformerGroup.setSelected(btn.getModel(), true);
            }
        }
        for (Enumeration<AbstractButton> btns = edgeTransformerGroup.getElements(); btns.hasMoreElements();) {
            AbstractButton btn = btns.nextElement();
            btn.setVisible(edgeSelected);
            if (model.getEdgeTransformerBuilder() != null && btn.getName().equals(model.getEdgeTransformerBuilder().getClass().getName())) {
                edgeTransformerGroup.setSelected(btn.getModel(), true);
            }
        }
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(PartitionModel.SELECTED_PARTIONING)) {
            refreshModel();
        } else if (evt.getPropertyName().equals(PartitionModel.NODE_TRANSFORMER)) {
            refreshModel();
        } else if (evt.getPropertyName().equals(PartitionModel.EDGE_TRANSFORMER)) {
            refreshModel();
        }
    }

    private void initTransformersUI() {
        nodeTransformerGroup = new ButtonGroup();
        edgeTransformerGroup = new ButtonGroup();

        TransformerBuilder[] builders = Lookup.getDefault().lookupAll(TransformerBuilder.class).toArray(new TransformerBuilder[0]);

        for (final TransformerBuilder t : builders) {
            TransformerUI transformerUI = t.getUI();
            JToggleButton btn = new JToggleButton(transformerUI.getIcon());
            btn.setToolTipText(transformerUI.getName());
            btn.setVisible(false);
            btn.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    PartitionController pc = Lookup.getDefault().lookup(PartitionController.class);
                    pc.setSelectedTransformerBuilder(t);
                }
            });
            btn.setName(t.getClass().getName());
            btn.setFocusPainted(false);
            if (t instanceof TransformerBuilder.Node) {
                nodeTransformerGroup.add(btn);
            } else {
                edgeTransformerGroup.add(btn);
            }
            add(btn);
        }

        //Init first
        /*if (!nodeTrans.isEmpty()) {
        model.setNodeTransformer(nodeTrans.get(0).getTransformerClass());
        }
        if (!edgeTrans.isEmpty()) {
        model.setEdgeTransformer(edgeTrans.get(0).getTransformerClass());
        }*/
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
        nodeButton.setText(NbBundle.getMessage(PartitionToolbar.class, "PartitionToolbar.nodes.label"));
        nodeButton.setEnabled(false);
        add(nodeButton);

        elementGroup.add(edgeButton);
        edgeButton.setText(NbBundle.getMessage(PartitionToolbar.class, "PartitionToolbar.edges.label"));
        edgeButton.setEnabled(false);
        add(edgeButton);
        addSeparator();

        box.setMaximumSize(new java.awt.Dimension(32767, 32767));
        add(box);

        nodeButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                PartitionController pc = Lookup.getDefault().lookup(PartitionController.class);
                pc.setSelectedPartitioning(PartitionModel.NODE_PARTITIONING);
            }
        });
        edgeButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                PartitionController pc = Lookup.getDefault().lookup(PartitionController.class);
                pc.setSelectedPartitioning(PartitionModel.EDGE_PARTITIONING);
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
