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
package org.gephi.desktop.context;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.swing.SwingUtilities;
import org.gephi.graph.api.DirectedGraph;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphEvent;
import org.gephi.graph.api.GraphListener;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.UndirectedGraph;

/**
 *
 * @author Mathieu Bastian
 */
public class ContextPanel extends javax.swing.JPanel implements GraphListener {

    private enum GraphType {

        DIRECTED("Directed Graph"),
        UNDIRECTED("Undirected Graph"),
        MIXED("Mixed Graph");
        protected final String type;

        GraphType(String type) {
            this.type = type;
        }
    }
    private GraphModel model;
    private ContextPieChart pieChart;
    private NumberFormat formatter;
    private boolean showPie = true;
    private ThreadPoolExecutor consumerThread;

    public ContextPanel() {
        initComponents();
        initDesign();
        refreshModel(null);

        pieButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                pieChart.setChartVisible(pieButton.isSelected());
            }
        });
        pieChart.setChartVisible(pieButton.isSelected());

        //Event manager
        consumerThread = new ThreadPoolExecutor(0, 1, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(5), new ThreadFactory() {

            public Thread newThread(Runnable r) {
                Thread t = new Thread(r, "Context Panel consumer thread");
                t.setDaemon(true);
                return t;
            }
        }, new ThreadPoolExecutor.DiscardOldestPolicy());
    }

    private void initDesign() {
        pieChart = new ContextPieChart();
        piePanel.add(pieChart.getChartPanel(), BorderLayout.CENTER);
        labelNodes.setFont(labelNodes.getFont().deriveFont(Font.BOLD));
        labelEdges.setFont(labelEdges.getFont().deriveFont(Font.BOLD));
        formatter = NumberFormat.getPercentInstance();
        formatter.setMaximumFractionDigits(2);
    }

    public void refreshModel(GraphModel model) {
        if (this.model != null) {
            this.model.removeGraphListener(this);
        }
        this.model = model;
        setEnable(model != null);
        if (this.model != null) {
            model.addGraphListener(this);
            refreshModelData();
        }
    }

    private void refreshModelData() {
        if (consumerThread.getQueue().remainingCapacity() > 0) {
            consumerThread.execute(new RefreshRunnable());
        }
    }

    private class RefreshRunnable implements Runnable {

        public void run() {
            Graph visibleGraph = model.getGraphVisible();
            Graph fullGraph = model.getGraph();
            final int nodesFull = fullGraph.getNodeCount();
            final int nodesVisible = visibleGraph.getNodeCount();
            final int edgesFull = fullGraph.getEdgeCount();
            final int edgesVisible = visibleGraph.getEdgeCount();
            final GraphType graphType = visibleGraph instanceof DirectedGraph ? GraphType.DIRECTED : visibleGraph instanceof UndirectedGraph ? GraphType.UNDIRECTED : GraphType.MIXED;
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    String nodeText = String.valueOf(nodesVisible);
                    String edgeText = String.valueOf(edgesVisible);
                    if (nodesFull != nodesVisible || edgesFull != edgesVisible) {
                        nodeText += nodesFull > 0 ? " (" + formatter.format(nodesVisible / (double) nodesFull) + " visible)" : "";
                        edgeText += edgesFull > 0 ? " (" + formatter.format(edgesVisible / (double) edgesFull) + " visible)" : "";
                    }
                    nodeLabel.setText(nodeText);
                    edgeLabel.setText(edgeText);
                    graphTypeLabel.setText(graphType.type);
                    double percentage = 0.5 * nodesVisible / (double) nodesFull + 0.5 * edgesVisible / (double) edgesFull;
                    pieChart.refreshChart(percentage);
                }
            });
        }
    }

    public void graphChanged(GraphEvent event) {
        refreshModelData();
    }

    private void setEnable(boolean enable) {
        labelNodes.setEnabled(enable);
        labelEdges.setEnabled(enable);
        nodeLabel.setEnabled(enable);
        edgeLabel.setEnabled(enable);

        if (!enable) {
            nodeLabel.setText("NaN");
            edgeLabel.setText("NaN");
            graphTypeLabel.setText("");
        }
        pieButton.setEnabled(enable);
        piePanel.setVisible(showPie);
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

        commandToolbar = new javax.swing.JToolBar();
        pieButton = new javax.swing.JToggleButton();
        piePanel = new javax.swing.JPanel();
        labelNodes = new javax.swing.JLabel();
        nodeLabel = new javax.swing.JLabel();
        labelEdges = new javax.swing.JLabel();
        edgeLabel = new javax.swing.JLabel();
        graphTypeLabel = new javax.swing.JLabel();

        setOpaque(false);
        setLayout(new java.awt.GridBagLayout());

        commandToolbar.setFloatable(false);
        commandToolbar.setOrientation(1);
        commandToolbar.setRollover(true);
        commandToolbar.setOpaque(false);

        pieButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/gephi/desktop/context/resources/pie.png"))); // NOI18N
        pieButton.setFocusable(false);
        pieButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        pieButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        commandToolbar.add(pieButton);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.weighty = 1.0;
        add(commandToolbar, gridBagConstraints);

        piePanel.setOpaque(false);
        piePanel.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(piePanel, gridBagConstraints);

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
        labelEdges.setToolTipText("null"); // NOI18N
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
    private javax.swing.JToggleButton pieButton;
    private javax.swing.JPanel piePanel;
    // End of variables declaration//GEN-END:variables
}
