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
package org.gephi.visualization.component;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;
import org.gephi.visualization.VizController;
import org.gephi.visualization.VizModel;
import org.gephi.visualization.api.initializer.Modeler;
import org.gephi.visualization.api.initializer.NodeModeler;
import org.gephi.visualization.api.objects.ModelClass;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;

/**
 *
 * @author Mathieu Bastian
 */
public class NodeSettingsPanel extends javax.swing.JPanel {

    /** Creates new form NodeSettingsPanel */
    public NodeSettingsPanel() {
        initComponents();
    }

    public void setup() {
        VizModel vizModel = VizController.getInstance().getVizModel();
        adjustTextCheckbox.setSelected(vizModel.isAdjustByText());
        adjustTextCheckbox.addItemListener(new ItemListener() {

            public void itemStateChanged(ItemEvent e) {
                VizModel vizModel = VizController.getInstance().getVizModel();
                vizModel.setAdjustByText(adjustTextCheckbox.isSelected());
            }
        });

        final DefaultComboBoxModel comboModel = new DefaultComboBoxModel();
        final ModelClass nodeClass = VizController.getInstance().getModelClassLibrary().getNodeClass();
        for (Modeler modeler : nodeClass.getModelers()) {
            comboModel.addElement(modeler);
        }
        comboModel.setSelectedItem(nodeClass.getCurrentModeler());
        shapeCombo.setModel(comboModel);
        shapeCombo.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (nodeClass.getCurrentModeler() == comboModel.getSelectedItem()) {
                    return;
                }
                VizModel vizModel = VizController.getInstance().getVizModel();
                NodeModeler modeler = (NodeModeler) comboModel.getSelectedItem();
                if (modeler.is3d() && !vizModel.isUse3d()) {
//                    String msg = NbBundle.getMessage(NodeSettingsPanel.class, "NodeSettingsPanel.defaultShape.message3d");
//                    if (JOptionPane.showConfirmDialog(WindowManager.getDefault().getMainWindow(), msg, NbBundle.getMessage(NodeSettingsPanel.class, "NodeSettingsPanel.defaultShape.message.title"), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                        //enable 3d
                        vizModel.setUse3d(true);
                        nodeClass.setCurrentModeler(modeler);
//                    }

                } else if (!modeler.is3d() && vizModel.isUse3d()) {
//                    String msg = NbBundle.getMessage(NodeSettingsPanel.class, "NodeSettingsPanel.defaultShape.message2d");
//                    if (JOptionPane.showConfirmDialog(WindowManager.getDefault().getMainWindow(), msg, NbBundle.getMessage(NodeSettingsPanel.class, "NodeSettingsPanel.defaultShape.message.title"), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                        //disable 3d
                        vizModel.setUse3d(false);
                        nodeClass.setCurrentModeler(modeler);
//                    }
                } else {
                    nodeClass.setCurrentModeler(modeler);
                }
            }
        });

        showHullsCheckbox.setSelected(vizModel.isShowHulls());
        showHullsCheckbox.addItemListener(new ItemListener() {

            public void itemStateChanged(ItemEvent e) {
                VizModel vizModel = VizController.getInstance().getVizModel();
                vizModel.setShowHulls(showHullsCheckbox.isSelected());
            }
        });

        vizModel.addPropertyChangeListener(new PropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName().equals("nodeModeler")) {
                    refreshSharedConfig();
                } else if (evt.getPropertyName().equals("init")) {
                    refreshSharedConfig();
                } else if (evt.getPropertyName().equals("adjustByText")) {
                    refreshSharedConfig();
                } else if (evt.getPropertyName().equals("showHulls")) {
                    refreshSharedConfig();
                }
            }
        });
        refreshSharedConfig();
    }

    private void refreshSharedConfig() {
        VizModel vizModel = VizController.getInstance().getVizModel();
        setEnable(!vizModel.isDefaultModel());
        if (vizModel.isDefaultModel()) {
            return;
        }
        final ModelClass nodeClass = VizController.getInstance().getModelClassLibrary().getNodeClass();
        if (shapeCombo.getSelectedItem() != nodeClass.getCurrentModeler()) {
            shapeCombo.setSelectedItem(nodeClass.getCurrentModeler());
        }
        if (adjustTextCheckbox.isSelected() != vizModel.isAdjustByText()) {
            adjustTextCheckbox.setSelected(vizModel.isAdjustByText());
        }
        if (showHullsCheckbox.isSelected() != vizModel.isShowHulls()) {
            showHullsCheckbox.setSelected(vizModel.isShowHulls());
        }
    }

    public void setEnable(boolean enable) {
        labelShape.setEnabled(enable);
        adjustTextCheckbox.setEnabled(enable);
        shapeCombo.setEnabled(enable);
        showHullsCheckbox.setEnabled(enable);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        labelShape = new javax.swing.JLabel();
        adjustTextCheckbox = new javax.swing.JCheckBox();
        shapeCombo = new javax.swing.JComboBox();
        showHullsCheckbox = new javax.swing.JCheckBox();

        labelShape.setText(org.openide.util.NbBundle.getMessage(NodeSettingsPanel.class, "NodeSettingsPanel.labelShape.text")); // NOI18N

        adjustTextCheckbox.setText(org.openide.util.NbBundle.getMessage(NodeSettingsPanel.class, "NodeSettingsPanel.adjustTextCheckbox.text")); // NOI18N

        shapeCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        showHullsCheckbox.setText(org.openide.util.NbBundle.getMessage(NodeSettingsPanel.class, "NodeSettingsPanel.showHullsCheckbox.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(labelShape)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(shapeCombo, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(102, 102, 102)
                        .addComponent(showHullsCheckbox))
                    .addComponent(adjustTextCheckbox))
                .addContainerGap(156, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelShape)
                    .addComponent(shapeCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(showHullsCheckbox))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(adjustTextCheckbox)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox adjustTextCheckbox;
    private javax.swing.JLabel labelShape;
    private javax.swing.JComboBox shapeCombo;
    private javax.swing.JCheckBox showHullsCheckbox;
    // End of variables declaration//GEN-END:variables
}
