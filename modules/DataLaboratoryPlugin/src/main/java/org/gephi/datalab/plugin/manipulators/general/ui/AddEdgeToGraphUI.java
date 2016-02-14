/*
 Copyright 2008-2010 Gephi
 Authors : Eduardo Ramos <eduramiba@gmail.com>
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
package org.gephi.datalab.plugin.manipulators.general.ui;

import javax.swing.JPanel;
import org.gephi.datalab.plugin.manipulators.general.AddEdgeToGraph;
import org.gephi.datalab.spi.DialogControls;
import org.gephi.datalab.spi.Manipulator;
import org.gephi.datalab.spi.ManipulatorUI;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.Node;
import org.openide.util.Lookup;

/**
 * UI for AddEdgeToGraph GeneralActionsManipulator
 *
 * @author Eduardo Ramos
 */
public class AddEdgeToGraphUI extends javax.swing.JPanel implements ManipulatorUI {

    private AddEdgeToGraph manipulator;
    private Node[] nodes;
    private Graph graph;
    private DialogControls dialogControls;

    /**
     * Creates new form AddEdgeToGraphUI
     */
    public AddEdgeToGraphUI() {
        initComponents();
    }

    @Override
    public void setup(Manipulator m, DialogControls dialogControls) {
        this.manipulator = (AddEdgeToGraph) m;
        this.dialogControls = dialogControls;
        if (manipulator.isDirected()) {
            directedRadioButton.setSelected(true);
        } else {
            undirectedRadioButton.setSelected(true);
        }

        graph = Lookup.getDefault().lookup(GraphController.class).getGraphModel().getGraph();
        nodes = graph.getNodes().toArray();

        for (Node n : nodes) {
            sourceNodesComboBox.addItem(n.getId() + " - " + n.getLabel());
            targetNodesComboBox.addItem(n.getId() + " - " + n.getLabel());
        }

        Node selectedSource = manipulator.getSource();
        if (selectedSource != null) {
            for (int i = 0; i < nodes.length; i++) {
                if (nodes[i] == selectedSource) {
                    sourceNodesComboBox.setSelectedIndex(i);
                }
            }
        }

        refreshAvailableEdgeTypes();
        
        dialogControls.setOkButtonEnabled(nodes.length > 0);
    }

    @Override
    public void unSetup() {
        manipulator.setDirected(directedRadioButton.isSelected());
        if (targetNodesComboBox.getSelectedIndex() != -1) {
            manipulator.setSource(nodes[sourceNodesComboBox.getSelectedIndex()]);
            manipulator.setTarget(nodes[targetNodesComboBox.getSelectedIndex()]);
            String edgeType = getSelectedEdgeType();
            manipulator.setEdgeTypeLabel(edgeType);
        }
    }

    @Override
    public String getDisplayName() {
        return manipulator.getName();
    }

    @Override
    public JPanel getSettingsPanel() {
        return this;
    }

    @Override
    public boolean isModal() {
        return true;
    }

    private String getSelectedEdgeType() {
        String edgeType = edgeTypeComboBox.getSelectedItem() != null ? edgeTypeComboBox.getSelectedItem().toString() : null;
        if (edgeType != null && edgeType.trim().isEmpty()) {
            edgeType = null;
        }

        return edgeType;
    }

    private void refreshAvailableEdgeTypes() {
        for (Object edgeType : graph.getModel().getEdgeTypeLabels()) {
            edgeTypeComboBox.addItem(edgeType);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        directedUndirectedRadioButtonGroup = new javax.swing.ButtonGroup();
        directedRadioButton = new javax.swing.JRadioButton();
        undirectedRadioButton = new javax.swing.JRadioButton();
        descriptionLabel = new javax.swing.JLabel();
        sourceNodesComboBox = new javax.swing.JComboBox();
        sourceNodeLabel = new javax.swing.JLabel();
        targetNodeLabel = new javax.swing.JLabel();
        targetNodesComboBox = new javax.swing.JComboBox();
        edgeTypeLabel = new javax.swing.JLabel();
        edgeTypeComboBox = new javax.swing.JComboBox();

        directedUndirectedRadioButtonGroup.add(directedRadioButton);
        directedRadioButton.setText(org.openide.util.NbBundle.getMessage(AddEdgeToGraphUI.class, "AddEdgeToGraphUI.directedRadioButton.text")); // NOI18N

        directedUndirectedRadioButtonGroup.add(undirectedRadioButton);
        undirectedRadioButton.setText(org.openide.util.NbBundle.getMessage(AddEdgeToGraphUI.class, "AddEdgeToGraphUI.undirectedRadioButton.text")); // NOI18N

        descriptionLabel.setText(org.openide.util.NbBundle.getMessage(AddEdgeToGraphUI.class, "AddEdgeToGraphUI.descriptionLabel.text")); // NOI18N

        sourceNodeLabel.setText(org.openide.util.NbBundle.getMessage(AddEdgeToGraphUI.class, "AddEdgeToGraphUI.sourceNodeLabel.text")); // NOI18N

        targetNodeLabel.setText(org.openide.util.NbBundle.getMessage(AddEdgeToGraphUI.class, "AddEdgeToGraphUI.targetNodeLabel.text")); // NOI18N

        edgeTypeLabel.setText(org.openide.util.NbBundle.getMessage(AddEdgeToGraphUI.class, "AddEdgeToGraphUI.edgeTypeLabel.text")); // NOI18N

        edgeTypeComboBox.setEditable(true);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(descriptionLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(directedRadioButton)
                            .addComponent(sourceNodeLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(sourceNodesComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(undirectedRadioButton)))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(targetNodeLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(targetNodesComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(edgeTypeLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(edgeTypeComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(descriptionLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(directedRadioButton)
                    .addComponent(undirectedRadioButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(sourceNodesComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(sourceNodeLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(targetNodesComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(targetNodeLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(edgeTypeLabel)
                    .addComponent(edgeTypeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel descriptionLabel;
    private javax.swing.JRadioButton directedRadioButton;
    private javax.swing.ButtonGroup directedUndirectedRadioButtonGroup;
    private javax.swing.JComboBox edgeTypeComboBox;
    private javax.swing.JLabel edgeTypeLabel;
    private javax.swing.JLabel sourceNodeLabel;
    private javax.swing.JComboBox sourceNodesComboBox;
    private javax.swing.JLabel targetNodeLabel;
    private javax.swing.JComboBox targetNodesComboBox;
    private javax.swing.JRadioButton undirectedRadioButton;
    // End of variables declaration//GEN-END:variables
}
