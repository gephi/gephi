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
package org.gephi.desktop.context;

import java.awt.Font;
import java.text.NumberFormat;
import javax.swing.SwingUtilities;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphModel;
import org.openide.util.NbBundle;

/**
 *
 * @author Mathieu Bastian
 */
public class ContextPanel extends javax.swing.JPanel {

    private enum GraphType {

        DIRECTED(NbBundle.getMessage(ContextPanel.class, "ContextPanel.graphType.directed")),
        UNDIRECTED(NbBundle.getMessage(ContextPanel.class, "ContextPanel.graphType.undirected")),
        MIXED(NbBundle.getMessage(ContextPanel.class, "ContextPanel.graphType.mixed"));
        protected final String type;

        GraphType(String type) {
            this.type = type;
        }
    }
    private GraphModel model;
    private NumberFormat formatter;
    private ContextRefreshThread consumerThread;

    public ContextPanel() {
        initComponents();
        initDesign();
        refreshModel(null);
    }

    private void initDesign() {
        labelNodes.setFont(labelNodes.getFont().deriveFont(Font.BOLD));
        labelEdges.setFont(labelEdges.getFont().deriveFont(Font.BOLD));
        formatter = NumberFormat.getPercentInstance();
        formatter.setMaximumFractionDigits(2);
    }

    public void refreshModel(GraphModel model) {
        if (consumerThread != null) {
            consumerThread.shutdown();
        }
        this.model = model;
        setEnable(model != null);
        if (this.model != null) {
            consumerThread = new ContextRefreshThread(model, new RefreshRunnable());
        }
    }

    private class RefreshRunnable implements Runnable {

        @Override
        public void run() {
            Graph visibleGraph = model.getGraphVisible();
            Graph fullGraph = model.getGraph();
            final int nodesFull = fullGraph.getNodeCount();
            final int nodesVisible = visibleGraph.getNodeCount();
            final int edgesFull = fullGraph.getEdgeCount();
            final int edgesVisible = visibleGraph.getEdgeCount();
            final GraphType graphType = model.isDirected() ? GraphType.DIRECTED : model.isUndirected() ? GraphType.UNDIRECTED : GraphType.MIXED;
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    String visible = NbBundle.getMessage(ContextPanel.class, "ContextPanel.visible");
                    String nodeText = String.valueOf(nodesVisible);
                    String edgeText = String.valueOf(edgesVisible);
                    if (nodesFull != nodesVisible || edgesFull != edgesVisible) {
                        nodeText += nodesFull > 0 ? " (" + formatter.format(nodesVisible / (double) nodesFull) + " " + visible + ")" : "";
                        edgeText += edgesFull > 0 ? " (" + formatter.format(edgesVisible / (double) edgesFull) + " " + visible + ")" : "";
                    }
                    nodeLabel.setText(nodeText);
                    edgeLabel.setText(edgeText);
                    graphTypeLabel.setText(graphType.type);
                }
            });
        }
    }

    private void setEnable(boolean enable) {
        labelNodes.setEnabled(enable);
        labelEdges.setEnabled(enable);
        nodeLabel.setEnabled(enable);
        edgeLabel.setEnabled(enable);

        if (!enable) {
            nodeLabel.setText("");
            edgeLabel.setText("");
            graphTypeLabel.setText("");
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        commandToolbar = new javax.swing.JToolBar();
        labelNodes = new javax.swing.JLabel();
        nodeLabel = new javax.swing.JLabel();
        labelEdges = new javax.swing.JLabel();
        edgeLabel = new javax.swing.JLabel();
        graphTypeLabel = new javax.swing.JLabel();

        setOpaque(false);
        setLayout(new java.awt.GridBagLayout());

        commandToolbar.setFloatable(false);
        commandToolbar.setOrientation(javax.swing.SwingConstants.VERTICAL);
        commandToolbar.setRollover(true);
        commandToolbar.setOpaque(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.weighty = 1.0;
        add(commandToolbar, gridBagConstraints);

        labelNodes.setText(org.openide.util.NbBundle.getMessage(ContextPanel.class, "ContextPanel.labelNodes.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(7, 7, 0, 5);
        add(labelNodes, gridBagConstraints);

        nodeLabel.setText(org.openide.util.NbBundle.getMessage(ContextPanel.class, "ContextPanel.nodeLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(7, 4, 0, 3);
        add(nodeLabel, gridBagConstraints);

        labelEdges.setText(org.openide.util.NbBundle.getMessage(ContextPanel.class, "ContextPanel.labelEdges.text")); // NOI18N
        labelEdges.setToolTipText("Number of edges, without meta-edges"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 7, 0, 5);
        add(labelEdges, gridBagConstraints);

        edgeLabel.setText(org.openide.util.NbBundle.getMessage(ContextPanel.class, "ContextPanel.edgeLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 4, 0, 3);
        add(edgeLabel, gridBagConstraints);

        graphTypeLabel.setText(org.openide.util.NbBundle.getMessage(ContextPanel.class, "ContextPanel.graphTypeLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 7, 5, 5);
        add(graphTypeLabel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JToolBar commandToolbar;
    private javax.swing.JLabel edgeLabel;
    private javax.swing.JLabel graphTypeLabel;
    private javax.swing.JLabel labelEdges;
    private javax.swing.JLabel labelNodes;
    private javax.swing.JLabel nodeLabel;
    // End of variables declaration//GEN-END:variables
}
