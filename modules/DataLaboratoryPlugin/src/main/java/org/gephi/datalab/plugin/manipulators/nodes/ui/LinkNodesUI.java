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

package org.gephi.datalab.plugin.manipulators.nodes.ui;

import javax.swing.JPanel;
import org.gephi.datalab.plugin.manipulators.nodes.LinkNodes;
import org.gephi.datalab.spi.DialogControls;
import org.gephi.datalab.spi.Manipulator;
import org.gephi.datalab.spi.ManipulatorUI;
import org.gephi.graph.api.Node;

/**
 * UI for LinkNodes nodes manipulator.
 *
 * @author Eduardo Ramos
 */
public class LinkNodesUI extends javax.swing.JPanel implements ManipulatorUI {

    private LinkNodes manipulator;
    private Node[] nodes;
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel descriptionLabel;
    private javax.swing.JRadioButton directedEdge;
    private javax.swing.ButtonGroup edgeTypeButtonGroup;
    private javax.swing.JLabel edgeTypeLabel;
    private javax.swing.JComboBox sourceNodeComboBox;
    private javax.swing.JLabel sourceNodeLabel;
    private javax.swing.JRadioButton undirectedEdge;
    // End of variables declaration//GEN-END:variables

    /**
     * Creates new form LinkNodesUI
     */
    public LinkNodesUI() {
        initComponents();
    }

    @Override
    public void setup(Manipulator m, DialogControls dialogControls) {
        manipulator = (LinkNodes) m;
        nodes = manipulator.getNodes();
        if (manipulator.isDirected()) {
            directedEdge.setSelected(true);
        } else {
            undirectedEdge.setSelected(true);
        }

        Node sourceNode = manipulator.getSourceNode();
        //Prepare combo box with nodes data:
        for (int i = 0; i < nodes.length; i++) {
            sourceNodeComboBox.addItem(nodes[i].getId() + " - " + nodes[i].getLabel());
            if (nodes[i] == sourceNode) {
                sourceNodeComboBox.setSelectedIndex(i);
            }
        }
    }

    @Override
    public void unSetup() {
        manipulator.setSourceNode(nodes[sourceNodeComboBox.getSelectedIndex()]);
        manipulator.setDirected(directedEdge.isSelected());
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

    /**
     * This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        edgeTypeButtonGroup = new javax.swing.ButtonGroup();
        descriptionLabel = new javax.swing.JLabel();
        directedEdge = new javax.swing.JRadioButton();
        undirectedEdge = new javax.swing.JRadioButton();
        sourceNodeComboBox = new javax.swing.JComboBox();
        sourceNodeLabel = new javax.swing.JLabel();
        edgeTypeLabel = new javax.swing.JLabel();

        descriptionLabel.setText(
            org.openide.util.NbBundle.getMessage(LinkNodesUI.class, "LinkNodesUI.descriptionLabel.text")); // NOI18N

        edgeTypeButtonGroup.add(directedEdge);
        directedEdge.setText(
            org.openide.util.NbBundle.getMessage(LinkNodesUI.class, "LinkNodesUI.directedEdge.text")); // NOI18N

        edgeTypeButtonGroup.add(undirectedEdge);
        undirectedEdge.setText(
            org.openide.util.NbBundle.getMessage(LinkNodesUI.class, "LinkNodesUI.undirectedEdge.text")); // NOI18N

        sourceNodeLabel.setText(
            org.openide.util.NbBundle.getMessage(LinkNodesUI.class, "LinkNodesUI.sourceNodeLabel.text")); // NOI18N

        edgeTypeLabel.setText(
            org.openide.util.NbBundle.getMessage(LinkNodesUI.class, "LinkNodesUI.edgeTypeLabel.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(descriptionLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 242, Short.MAX_VALUE)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(sourceNodeLabel)
                                .addComponent(edgeTypeLabel))
                            .addGap(18, 18, 18)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(layout.createSequentialGroup()
                                    .addComponent(directedEdge)
                                    .addGap(18, 18, 18)
                                    .addComponent(undirectedEdge))
                                .addComponent(sourceNodeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 160,
                                    javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(descriptionLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 51,
                        javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(5, 5, 5)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(sourceNodeLabel)
                        .addComponent(sourceNodeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE,
                            javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGap(18, 18, 18)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(directedEdge, javax.swing.GroupLayout.PREFERRED_SIZE, 23,
                            javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(undirectedEdge)
                        .addComponent(edgeTypeLabel))
                    .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
}
