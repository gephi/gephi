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
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
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
import org.gephi.ui.components.JHTMLEditorPane;
import org.gephi.ui.utils.BusyUtils;
import org.netbeans.swing.outline.DefaultOutlineModel;
import org.netbeans.swing.outline.OutlineModel;
import org.netbeans.swing.outline.RenderDataProvider;
import org.netbeans.swing.outline.RowModel;
import org.openide.util.Exceptions;
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

    public ReportPanel() {
        try {
            SwingUtilities.invokeAndWait(new Runnable() {

                public void run() {
                    initComponents();
                    initIcons();
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
    }

    public void initIcons() {
        infoIcon = new javax.swing.ImageIcon(getClass().getResource("/org/gephi/desktop/importer/resources/info.png"));
        warningIcon = new javax.swing.ImageIcon(getClass().getResource("/org/gephi/desktop/importer/resources/warning.gif"));
        severeIcon = new javax.swing.ImageIcon(getClass().getResource("/org/gephi/desktop/importer/resources/severe.png"));
        criticalIcon = new javax.swing.ImageIcon(getClass().getResource("/org/gephi/desktop/importer/resources/critical.png"));
    }

    public void setData(Report report, Container container) {
        this.container = container;
        fillIssues(report);
        fillReport(report);
        fillStats(container);
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
                    issuesOutline.setRootVisible(false);
                    issuesOutline.setRenderDataProvider(new IssueRenderer());
                    SwingUtilities.invokeLater(new Runnable() {

                        public void run() {
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
                String str = report.getHtml();
                //reportEditor.setContentType("text/html");
                reportEditor.setText(str);
            }
        }, "Report Panel Issues Report");
        if (NbPreferences.forModule(ReportPanel.class).getBoolean(SHOW_REPORT, true)) {
            thread.start();
        }
    }

    private void fillStats(Container container) {
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
        dynamicLabel.setText(String.valueOf(container.isDynamicGraph()));
        hierarchicalLabel.setText(String.valueOf(container.isHierarchicalGraph()));
    }

    public void destroy() {
        fillingThreads.interrupt();
    }

    public ProcessorStrategyEnum getProcessorStrategy() {
        if (processorStrategyRadio.getSelection() == appendGraphRadio.getModel()) {
            return ProcessorStrategyEnum.APPEND;
        } else {
            return ProcessorStrategyEnum.FULL;
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

        processorStrategyRadio = new javax.swing.ButtonGroup();
        labelSrc = new javax.swing.JLabel();
        sourceLabel = new javax.swing.JLabel();
        tabbedPane = new javax.swing.JTabbedPane();
        tab1ScrollPane = new javax.swing.JScrollPane();
        issuesOutline = new org.netbeans.swing.outline.Outline();
        tab2ScrollPane = new javax.swing.JScrollPane();
        reportEditor = new JHTMLEditorPane();
        labelGraphType = new javax.swing.JLabel();
        labelNodeCount = new javax.swing.JLabel();
        labelEdgeCount = new javax.swing.JLabel();
        graphTypeCombo = new javax.swing.JComboBox();
        nodeCountLabel = new javax.swing.JLabel();
        edgeCountLabel = new javax.swing.JLabel();
        fullGraphRadio = new javax.swing.JRadioButton();
        appendGraphRadio = new javax.swing.JRadioButton();
        labelDynamic = new javax.swing.JLabel();
        labelHierarchical = new javax.swing.JLabel();
        dynamicLabel = new javax.swing.JLabel();
        hierarchicalLabel = new javax.swing.JLabel();
        autoscaleCheckbox = new javax.swing.JCheckBox();

        labelSrc.setText(org.openide.util.NbBundle.getMessage(ReportPanel.class, "ReportPanel.labelSrc.text")); // NOI18N

        sourceLabel.setText(org.openide.util.NbBundle.getMessage(ReportPanel.class, "ReportPanel.sourceLabel.text")); // NOI18N

        tab1ScrollPane.setViewportView(issuesOutline);

        tabbedPane.addTab(org.openide.util.NbBundle.getMessage(ReportPanel.class, "ReportPanel.tab1ScrollPane.TabConstraints.tabTitle"), tab1ScrollPane); // NOI18N

        tab2ScrollPane.setViewportView(reportEditor);

        tabbedPane.addTab(org.openide.util.NbBundle.getMessage(ReportPanel.class, "ReportPanel.tab2ScrollPane.TabConstraints.tabTitle"), tab2ScrollPane); // NOI18N

        labelGraphType.setText(org.openide.util.NbBundle.getMessage(ReportPanel.class, "ReportPanel.labelGraphType.text")); // NOI18N

        labelNodeCount.setFont(new java.awt.Font("Tahoma", 1, 11));
        labelNodeCount.setText(org.openide.util.NbBundle.getMessage(ReportPanel.class, "ReportPanel.labelNodeCount.text")); // NOI18N

        labelEdgeCount.setFont(new java.awt.Font("Tahoma", 1, 11));
        labelEdgeCount.setText(org.openide.util.NbBundle.getMessage(ReportPanel.class, "ReportPanel.labelEdgeCount.text")); // NOI18N

        graphTypeCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Directed", "Undirected", "Mixed" }));

        nodeCountLabel.setFont(new java.awt.Font("Tahoma", 1, 11));
        nodeCountLabel.setText(org.openide.util.NbBundle.getMessage(ReportPanel.class, "ReportPanel.nodeCountLabel.text")); // NOI18N

        edgeCountLabel.setFont(new java.awt.Font("Tahoma", 1, 11));
        edgeCountLabel.setText(org.openide.util.NbBundle.getMessage(ReportPanel.class, "ReportPanel.edgeCountLabel.text")); // NOI18N

        processorStrategyRadio.add(fullGraphRadio);
        fullGraphRadio.setSelected(true);
        fullGraphRadio.setText(org.openide.util.NbBundle.getMessage(ReportPanel.class, "ReportPanel.fullGraphRadio.text")); // NOI18N

        processorStrategyRadio.add(appendGraphRadio);
        appendGraphRadio.setText(org.openide.util.NbBundle.getMessage(ReportPanel.class, "ReportPanel.appendGraphRadio.text")); // NOI18N

        labelDynamic.setText(org.openide.util.NbBundle.getMessage(ReportPanel.class, "ReportPanel.labelDynamic.text")); // NOI18N

        labelHierarchical.setText(org.openide.util.NbBundle.getMessage(ReportPanel.class, "ReportPanel.labelHierarchical.text")); // NOI18N

        dynamicLabel.setText(org.openide.util.NbBundle.getMessage(ReportPanel.class, "ReportPanel.dynamicLabel.text")); // NOI18N

        hierarchicalLabel.setText(org.openide.util.NbBundle.getMessage(ReportPanel.class, "ReportPanel.hierarchicalLabel.text")); // NOI18N

        autoscaleCheckbox.setText(org.openide.util.NbBundle.getMessage(ReportPanel.class, "ReportPanel.autoscaleCheckbox.text")); // NOI18N
        autoscaleCheckbox.setToolTipText(org.openide.util.NbBundle.getMessage(ReportPanel.class, "ReportPanel.autoscaleCheckbox.toolTipText")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(tabbedPane, javax.swing.GroupLayout.DEFAULT_SIZE, 548, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(labelSrc)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(sourceLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 505, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(labelGraphType)
                                        .addComponent(labelNodeCount)
                                        .addComponent(labelEdgeCount))
                                    .addGap(43, 43, 43))
                                .addGroup(layout.createSequentialGroup()
                                    .addComponent(labelDynamic)
                                    .addGap(27, 27, 27)))
                            .addComponent(labelHierarchical))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(nodeCountLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 350, Short.MAX_VALUE)
                                    .addComponent(graphTypeCombo, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(autoscaleCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, 92, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(hierarchicalLabel)
                                    .addComponent(dynamicLabel)
                                    .addComponent(edgeCountLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 349, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(fullGraphRadio)
                                    .addComponent(appendGraphRadio))))))
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
                .addComponent(tabbedPane, javax.swing.GroupLayout.PREFERRED_SIZE, 162, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(labelGraphType)
                            .addComponent(graphTypeCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(autoscaleCheckbox))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(labelNodeCount)
                            .addComponent(nodeCountLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(labelEdgeCount)
                            .addComponent(edgeCountLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(labelDynamic)
                            .addComponent(dynamicLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(labelHierarchical)
                            .addComponent(hierarchicalLabel)
                            .addComponent(appendGraphRadio)))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(fullGraphRadio)
                        .addGap(23, 23, 23)))
                .addContainerGap(19, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JRadioButton appendGraphRadio;
    private javax.swing.JCheckBox autoscaleCheckbox;
    private javax.swing.JLabel dynamicLabel;
    private javax.swing.JLabel edgeCountLabel;
    private javax.swing.JRadioButton fullGraphRadio;
    private javax.swing.JComboBox graphTypeCombo;
    private javax.swing.JLabel hierarchicalLabel;
    private org.netbeans.swing.outline.Outline issuesOutline;
    private javax.swing.JLabel labelDynamic;
    private javax.swing.JLabel labelEdgeCount;
    private javax.swing.JLabel labelGraphType;
    private javax.swing.JLabel labelHierarchical;
    private javax.swing.JLabel labelNodeCount;
    private javax.swing.JLabel labelSrc;
    private javax.swing.JLabel nodeCountLabel;
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
