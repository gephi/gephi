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
package org.gephi.desktop.importer;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.lang.reflect.InvocationTargetException;
import java.util.Enumeration;
import java.util.List;
import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import org.gephi.io.importer.api.Container;
import org.gephi.io.importer.api.ContainerUnloader;
import org.gephi.io.importer.api.EdgeDefault;
import org.gephi.io.importer.api.Issue;
import org.gephi.io.importer.api.Report;
import org.gephi.io.processor.spi.Processor;
import org.gephi.io.processor.spi.ProcessorUI;
import org.gephi.ui.utils.BusyUtils;
import org.netbeans.swing.outline.DefaultOutlineModel;
import org.netbeans.swing.outline.OutlineModel;
import org.netbeans.swing.outline.RenderDataProvider;
import org.netbeans.swing.outline.RowModel;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbPreferences;

/**
 *
 * @author Mathieu Bastian
 */
public class ReportPanel extends javax.swing.JPanel {

    //Preferences
    private final static String SHOW_ISSUES = "ReportPanel_Show_Issues";
    private final static String SHOW_REPORT = "ReportPanel_Show_Report";
    private ThreadGroup fillingThreads;
    //Icons
    private ImageIcon infoIcon;
    private ImageIcon warningIcon;
    private ImageIcon severeIcon;
    private ImageIcon criticalIcon;
    //Container
    private Container container;
    //UI
    private ButtonGroup processorGroup = new ButtonGroup();

    public ReportPanel() {
        try {
            SwingUtilities.invokeAndWait(new Runnable() {

                public void run() {
                    initComponents();
                    initIcons();
                    initProcessors();
                }
            });
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        } catch (InvocationTargetException ex) {
            Exceptions.printStackTrace(ex);
        }

        fillingThreads = new ThreadGroup("Report Panel Issues");

        graphTypeCombo.addItemListener(new ItemListener() {

            public void itemStateChanged(ItemEvent e) {
                int g = graphTypeCombo.getSelectedIndex();
                switch (g) {
                    case 0:
                        container.getLoader().setEdgeDefault(EdgeDefault.DIRECTED);
                        break;
                    case 1:
                        container.getLoader().setEdgeDefault(EdgeDefault.UNDIRECTED);
                        break;
                    case 2:
                        container.getLoader().setEdgeDefault(EdgeDefault.MIXED);
                        break;
                }
            }
        });

        autoscaleCheckbox.addItemListener(new ItemListener() {

            public void itemStateChanged(ItemEvent e) {
                if (autoscaleCheckbox.isSelected() != container.isAutoScale()) {
                    container.setAutoScale(autoscaleCheckbox.isSelected());
                }
            }
        });

        createMissingNodesCheckbox.addItemListener(new ItemListener() {

            public void itemStateChanged(ItemEvent e) {
                if (createMissingNodesCheckbox.isSelected() != container.getUnloader().allowAutoNode()) {
                    container.setAllowAutoNode(createMissingNodesCheckbox.isSelected());
                }
            }
        });
    }

    public void initIcons() {
        infoIcon = new javax.swing.ImageIcon(getClass().getResource("/org/gephi/desktop/importer/resources/info.png"));
        warningIcon = new javax.swing.ImageIcon(getClass().getResource("/org/gephi/desktop/importer/resources/warning.gif"));
        severeIcon = new javax.swing.ImageIcon(getClass().getResource("/org/gephi/desktop/importer/resources/severe.png"));
        criticalIcon = new javax.swing.ImageIcon(getClass().getResource("/org/gephi/desktop/importer/resources/critical.png"));
    }

    public void setData(Report report, Container container) {
        this.container = container;
        initProcessorsUI();
        fillIssues(report);
        fillReport(report);
        fillStats(container);
        autoscaleCheckbox.setSelected(container.isAutoScale());
        createMissingNodesCheckbox.setSelected(container.getUnloader().allowAutoNode());
    }

    private void fillIssues(Report report) {
        final List<Issue> issues = report.getIssues();
        if (issues.isEmpty()) {
            JLabel label = new JLabel("No issue found during import");
            label.setHorizontalAlignment(SwingConstants.CENTER);
            tab1ScrollPane.setViewportView(label);
        } else {
            //Busy label
            final BusyUtils.BusyLabel busyLabel = BusyUtils.createCenteredBusyLabel(tab1ScrollPane, "Retrieving issues...", issuesOutline);

            //Thread
            Thread thread = new Thread(fillingThreads, new Runnable() {

                public void run() {
                    busyLabel.setBusy(true);
                    final TreeModel treeMdl = new IssueTreeModel(issues);
                    final OutlineModel mdl = DefaultOutlineModel.createOutlineModel(treeMdl, new IssueRowModel(), true);

                    SwingUtilities.invokeLater(new Runnable() {

                        public void run() {
                            issuesOutline.setRootVisible(false);
                            issuesOutline.setRenderDataProvider(new IssueRenderer());
                            issuesOutline.setModel(mdl);
                            busyLabel.setBusy(false);
                        }
                    });
                }
            }, "Report Panel Issues Outline");
            if (NbPreferences.forModule(ReportPanel.class).getBoolean(SHOW_ISSUES, true)) {
                thread.start();
            }
        }
    }

    private void fillReport(final Report report) {
        Thread thread = new Thread(fillingThreads, new Runnable() {

            public void run() {
                final String str = report.getText();
                SwingUtilities.invokeLater(new Runnable() {

                    public void run() {
                        reportEditor.setText(str);
                    }
                });
            }
        }, "Report Panel Issues Report");
        if (NbPreferences.forModule(ReportPanel.class).getBoolean(SHOW_REPORT, true)) {
            thread.start();
        }
    }

    private void fillStats(final Container container) {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                //Source
                sourceLabel.setText(container.getSource());

                //Autoscale
                autoscaleCheckbox.setSelected(container.isAutoScale());

                ContainerUnloader unloader = container.getUnloader();

                //Node & Edge count
                int nodeCount = unloader.getNodes().size();
                int edgeCount = unloader.getEdges().size();
                nodeCountLabel.setText("" + nodeCount);
                edgeCountLabel.setText("" + edgeCount);

                switch (unloader.getEdgeDefault()) {
                    case DIRECTED:
                        graphTypeCombo.setSelectedIndex(0);
                        break;
                    case UNDIRECTED:
                        graphTypeCombo.setSelectedIndex(1);
                        break;
                    case MIXED:
                        graphTypeCombo.setSelectedIndex(2);
                        break;
                }

                //Dynamic & Hierarchical graph
                dynamicLabel.setText(container.isDynamicGraph() ? "yes" : "no");
                hierarchicalLabel.setText(container.isHierarchicalGraph() ? "yes" : "no");
            }
        });
    }
    private static final Object PROCESSOR_KEY = new Object();

    private void initProcessors() {
        int i = 0;
        for (Processor processor : Lookup.getDefault().lookupAll(Processor.class)) {
            JRadioButton radio = new JRadioButton(processor.getDisplayName());
            radio.setSelected(i == 0);
            radio.putClientProperty(PROCESSOR_KEY, processor);
            processorGroup.add(radio);
            GridBagConstraints constraints = new GridBagConstraints(0, i++, 1, 1, 1, 0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0);
            processorPanel.add(radio, constraints);
        }
    }

    private void initProcessorsUI() {
        for (Enumeration<AbstractButton> enumeration = processorGroup.getElements(); enumeration.hasMoreElements();) {
            AbstractButton radioButton = enumeration.nextElement();
            Processor p = (Processor) radioButton.getClientProperty(PROCESSOR_KEY);
            //Enabled
            ProcessorUI pui = getProcessorUI(p);
            if (pui != null) {
                radioButton.setEnabled(pui.isValid(container));
            }
        }
    }

    public void destroy() {
        fillingThreads.interrupt();
    }

    public Processor getProcessor() {
        for (Enumeration<AbstractButton> enumeration = processorGroup.getElements(); enumeration.hasMoreElements();) {
            AbstractButton radioButton = enumeration.nextElement();
            if (radioButton.isSelected()) {
                return (Processor) radioButton.getClientProperty(PROCESSOR_KEY);
            }
        }
        return null;
    }

    private ProcessorUI getProcessorUI(Processor processor) {
        for (ProcessorUI pui : Lookup.getDefault().lookupAll(ProcessorUI.class)) {
            if (pui.isUIFoProcessor(processor)) {
                return pui;
            }
        }
        return null;
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

        processorStrategyRadio = new javax.swing.ButtonGroup();
        labelSrc = new javax.swing.JLabel();
        sourceLabel = new javax.swing.JLabel();
        tabbedPane = new javax.swing.JTabbedPane();
        tab1ScrollPane = new javax.swing.JScrollPane();
        issuesOutline = new org.netbeans.swing.outline.Outline();
        tab2ScrollPane = new javax.swing.JScrollPane();
        reportEditor = new javax.swing.JEditorPane();
        labelGraphType = new javax.swing.JLabel();
        graphTypeCombo = new javax.swing.JComboBox();
        autoscaleCheckbox = new javax.swing.JCheckBox();
        processorPanel = new javax.swing.JPanel();
        createMissingNodesCheckbox = new javax.swing.JCheckBox();
        jPanel1 = new javax.swing.JPanel();
        labelNodeCount = new javax.swing.JLabel();
        labelEdgeCount = new javax.swing.JLabel();
        nodeCountLabel = new javax.swing.JLabel();
        edgeCountLabel = new javax.swing.JLabel();
        dynamicLabel = new javax.swing.JLabel();
        hierarchicalLabel = new javax.swing.JLabel();
        labelHierarchical = new javax.swing.JLabel();
        labelDynamic = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();

        labelSrc.setText(org.openide.util.NbBundle.getMessage(ReportPanel.class, "ReportPanel.labelSrc.text")); // NOI18N

        sourceLabel.setText(org.openide.util.NbBundle.getMessage(ReportPanel.class, "ReportPanel.sourceLabel.text")); // NOI18N

        tab1ScrollPane.setViewportView(issuesOutline);

        tabbedPane.addTab(org.openide.util.NbBundle.getMessage(ReportPanel.class, "ReportPanel.tab1ScrollPane.TabConstraints.tabTitle"), tab1ScrollPane); // NOI18N

        tab2ScrollPane.setViewportView(reportEditor);

        tabbedPane.addTab(org.openide.util.NbBundle.getMessage(ReportPanel.class, "ReportPanel.tab2ScrollPane.TabConstraints.tabTitle"), tab2ScrollPane); // NOI18N

        labelGraphType.setText(org.openide.util.NbBundle.getMessage(ReportPanel.class, "ReportPanel.labelGraphType.text")); // NOI18N

        graphTypeCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Directed", "Undirected", "Mixed" }));

        autoscaleCheckbox.setText(org.openide.util.NbBundle.getMessage(ReportPanel.class, "ReportPanel.autoscaleCheckbox.text")); // NOI18N
        autoscaleCheckbox.setToolTipText(org.openide.util.NbBundle.getMessage(ReportPanel.class, "ReportPanel.autoscaleCheckbox.toolTipText")); // NOI18N

        processorPanel.setLayout(new java.awt.GridBagLayout());

        createMissingNodesCheckbox.setText(org.openide.util.NbBundle.getMessage(ReportPanel.class, "ReportPanel.createMissingNodesCheckbox.text")); // NOI18N

        jPanel1.setLayout(new java.awt.GridBagLayout());

        labelNodeCount.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        labelNodeCount.setText(org.openide.util.NbBundle.getMessage(ReportPanel.class, "ReportPanel.labelNodeCount.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 6, 0);
        jPanel1.add(labelNodeCount, gridBagConstraints);

        labelEdgeCount.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        labelEdgeCount.setText(org.openide.util.NbBundle.getMessage(ReportPanel.class, "ReportPanel.labelEdgeCount.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 0);
        jPanel1.add(labelEdgeCount, gridBagConstraints);

        nodeCountLabel.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        nodeCountLabel.setText(org.openide.util.NbBundle.getMessage(ReportPanel.class, "ReportPanel.nodeCountLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 6, 0);
        jPanel1.add(nodeCountLabel, gridBagConstraints);

        edgeCountLabel.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        edgeCountLabel.setText(org.openide.util.NbBundle.getMessage(ReportPanel.class, "ReportPanel.edgeCountLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 10, 0);
        jPanel1.add(edgeCountLabel, gridBagConstraints);

        dynamicLabel.setText(org.openide.util.NbBundle.getMessage(ReportPanel.class, "ReportPanel.dynamicLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 6, 0);
        jPanel1.add(dynamicLabel, gridBagConstraints);

        hierarchicalLabel.setText(org.openide.util.NbBundle.getMessage(ReportPanel.class, "ReportPanel.hierarchicalLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
        jPanel1.add(hierarchicalLabel, gridBagConstraints);

        labelHierarchical.setText(org.openide.util.NbBundle.getMessage(ReportPanel.class, "ReportPanel.labelHierarchical.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel1.add(labelHierarchical, gridBagConstraints);

        labelDynamic.setText(org.openide.util.NbBundle.getMessage(ReportPanel.class, "ReportPanel.labelDynamic.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        jPanel1.add(labelDynamic, gridBagConstraints);

        jLabel1.setText(org.openide.util.NbBundle.getMessage(ReportPanel.class, "ReportPanel.jLabel1.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 1.0;
        jPanel1.add(jLabel1, gridBagConstraints);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(tabbedPane, javax.swing.GroupLayout.DEFAULT_SIZE, 545, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(labelSrc)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(sourceLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 502, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(labelGraphType)
                                .addGap(49, 49, 49)
                                .addComponent(graphTypeCombo, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 216, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 122, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(createMissingNodesCheckbox, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(autoscaleCheckbox, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 145, Short.MAX_VALUE))
                            .addComponent(processorPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 207, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelSrc)
                    .addComponent(sourceLabel))
                .addGap(18, 18, 18)
                .addComponent(tabbedPane, javax.swing.GroupLayout.DEFAULT_SIZE, 137, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelGraphType)
                    .addComponent(autoscaleCheckbox)
                    .addComponent(graphTypeCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addComponent(createMissingNodesCheckbox)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(processorPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 57, Short.MAX_VALUE)
                        .addContainerGap())
                    .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox autoscaleCheckbox;
    private javax.swing.JCheckBox createMissingNodesCheckbox;
    private javax.swing.JLabel dynamicLabel;
    private javax.swing.JLabel edgeCountLabel;
    private javax.swing.JComboBox graphTypeCombo;
    private javax.swing.JLabel hierarchicalLabel;
    private org.netbeans.swing.outline.Outline issuesOutline;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JLabel labelDynamic;
    private javax.swing.JLabel labelEdgeCount;
    private javax.swing.JLabel labelGraphType;
    private javax.swing.JLabel labelHierarchical;
    private javax.swing.JLabel labelNodeCount;
    private javax.swing.JLabel labelSrc;
    private javax.swing.JLabel nodeCountLabel;
    private javax.swing.JPanel processorPanel;
    private javax.swing.ButtonGroup processorStrategyRadio;
    private javax.swing.JEditorPane reportEditor;
    private javax.swing.JLabel sourceLabel;
    private javax.swing.JScrollPane tab1ScrollPane;
    private javax.swing.JScrollPane tab2ScrollPane;
    private javax.swing.JTabbedPane tabbedPane;
    // End of variables declaration//GEN-END:variables

    private class IssueTreeModel implements TreeModel {

        private List<Issue> issues;

        public IssueTreeModel(List<Issue> issues) {
            this.issues = issues;
        }

        public Object getRoot() {
            return "root";
        }

        public Object getChild(Object parent, int index) {
            return issues.get(index);
        }

        public int getChildCount(Object parent) {
            return issues.size();
        }

        public boolean isLeaf(Object node) {
            if (node instanceof Issue) {
                return true;
            }
            return false;
        }

        public void valueForPathChanged(TreePath path, Object newValue) {
        }

        public int getIndexOfChild(Object parent, Object child) {
            return issues.indexOf(child);
        }

        public void addTreeModelListener(TreeModelListener l) {
        }

        public void removeTreeModelListener(TreeModelListener l) {
        }
    }

    private class IssueRowModel implements RowModel {

        public int getColumnCount() {
            return 1;
        }

        public Object getValueFor(Object node, int column) {
            if (node instanceof Issue) {
                Issue issue = (Issue) node;
                return issue.getLevel().toString();
            }
            return "";
        }

        public Class getColumnClass(int column) {
            return String.class;
        }

        public boolean isCellEditable(Object node, int column) {
            return false;
        }

        public void setValueFor(Object node, int column, Object value) {
        }

        public String getColumnName(int column) {
            return "Issues";
        }
    }

    private class IssueRenderer implements RenderDataProvider {

        public String getDisplayName(Object o) {
            Issue issue = (Issue) o;
            return issue.getMessage();
        }

        public boolean isHtmlDisplayName(Object o) {
            return false;
        }

        public Color getBackground(Object o) {
            return null;
        }

        public Color getForeground(Object o) {
            return null;
        }

        public String getTooltipText(Object o) {
            return "";
        }

        public Icon getIcon(Object o) {
            Issue issue = (Issue) o;
            switch (issue.getLevel()) {
                case INFO:
                    return infoIcon;
                case WARNING:
                    return warningIcon;
                case SEVERE:
                    return severeIcon;
                case CRITICAL:
                    return criticalIcon;
            }
            return null;
        }
    }
}
