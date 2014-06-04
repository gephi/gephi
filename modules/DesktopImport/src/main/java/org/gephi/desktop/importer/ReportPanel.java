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
package org.gephi.desktop.importer;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.lang.reflect.InvocationTargetException;
import java.util.Enumeration;
import java.util.List;
import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JRootPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import org.gephi.io.importer.api.Container;
import org.gephi.io.importer.api.ContainerUnloader;
import org.gephi.io.importer.api.EdgeDirectionDefault;
import org.gephi.io.importer.api.EdgeWeightMergeStrategy;
import org.gephi.io.importer.api.Issue;
import org.gephi.io.importer.api.Report;
import org.gephi.io.processor.spi.Processor;
import org.gephi.io.processor.spi.ProcessorUI;
import org.gephi.ui.components.BusyUtils;
import org.netbeans.swing.outline.DefaultOutlineModel;
import org.netbeans.swing.outline.OutlineModel;
import org.netbeans.swing.outline.RenderDataProvider;
import org.netbeans.swing.outline.RowModel;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 *
 * @author Mathieu Bastian
 */
public class ReportPanel extends javax.swing.JPanel {

    //Preferences
    private final static String SHOW_ISSUES = "ReportPanel_Show_Issues";
    private final static String SHOW_REPORT = "ReportPanel_Show_Report";
    private final static int ISSUES_LIMIT = 5000;
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
                @Override
                public void run() {
                    initComponents();
                    initIcons();
                    initProcessors();
                    initProcessorsUI();
                    initMoreOptionsPanel();
                    initMergeStrategyCombo();
                }
            });
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        } catch (InvocationTargetException ex) {
            Exceptions.printStackTrace(ex);
        }

        fillingThreads = new ThreadGroup("Report Panel Issues");

        autoscaleCheckbox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (autoscaleCheckbox.isSelected() != container.getUnloader().isAutoScale()) {
                    container.getLoader().setAutoScale(autoscaleCheckbox.isSelected());
                }
            }
        });

        createMissingNodesCheckbox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (createMissingNodesCheckbox.isSelected() != container.getUnloader().allowAutoNode()) {
                    container.getLoader().setAllowAutoNode(createMissingNodesCheckbox.isSelected());
                }
            }
        });

        moreOptionsLink.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                moreOptionsPanel.setVisible(!moreOptionsPanel.isVisible());
                JRootPane rootPane = SwingUtilities.getRootPane(ReportPanel.this);
                ((JDialog) rootPane.getParent()).pack();
            }
        });

        edgesMergeStrategyCombo.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                int g = edgesMergeStrategyCombo.getSelectedIndex();
                switch (g) {
                    case 0:
                        container.getLoader().setEdgesMergeStrategy(EdgeWeightMergeStrategy.SUM);
                        break;
                    case 1:
                        container.getLoader().setEdgesMergeStrategy(EdgeWeightMergeStrategy.AVG);
                        break;
                    case 2:
                        container.getLoader().setEdgesMergeStrategy(EdgeWeightMergeStrategy.MIN);
                        break;
                    case 3:
                        container.getLoader().setEdgesMergeStrategy(EdgeWeightMergeStrategy.MAX);
                        break;
                }
            }
        });

        selfLoopCheckBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (selfLoopCheckBox.isSelected() != container.getUnloader().allowSelfLoop()) {
                    container.getLoader().setAllowSelfLoop(selfLoopCheckBox.isSelected());
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
        initGraphTypeCombo(container);

        report.pruneReport(ISSUES_LIMIT);
        fillIssues(report);
        fillReport(report);

        fillStats(container);
        fillParameters(container);
    }

    private void removeTabbedPane() {
        tabbedPane.setVisible(false);
    }

    private void initMergeStrategyCombo() {
        DefaultComboBoxModel mergeStrategryModel = new DefaultComboBoxModel(new String[]{
            NbBundle.getMessage(ReportPanel.class, "ReportPanel.mergeStrategy.sum"),
            NbBundle.getMessage(ReportPanel.class, "ReportPanel.mergeStrategy.avg"),
            NbBundle.getMessage(ReportPanel.class, "ReportPanel.mergeStrategy.min"),
            NbBundle.getMessage(ReportPanel.class, "ReportPanel.mergeStrategy.max")});
        edgesMergeStrategyCombo.setModel(mergeStrategryModel);
    }

    private void initMoreOptionsPanel() {
        moreOptionsPanel.setVisible(false);
    }

    private void initGraphTypeCombo(final Container container) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                String directedStr = NbBundle.getMessage(ReportPanel.class, "ReportPanel.graphType.directed");
                String undirectedStr = NbBundle.getMessage(ReportPanel.class, "ReportPanel.graphType.undirected");
                String mixedStr = NbBundle.getMessage(ReportPanel.class, "ReportPanel.graphType.mixed");

                DefaultComboBoxModel comboModel = new DefaultComboBoxModel();

                EdgeDirectionDefault dir = container.getUnloader().getEdgeDefault();
                switch (dir) {
                    case DIRECTED:
                        comboModel.addElement(directedStr);
                        comboModel.addElement(undirectedStr);
                        comboModel.addElement(mixedStr);
                        break;
                    case UNDIRECTED:
                        comboModel.addElement(undirectedStr);
                        comboModel.addElement(mixedStr);
                        break;
                    case MIXED:
                        comboModel.addElement(directedStr);
                        comboModel.addElement(undirectedStr);
                        comboModel.addElement(mixedStr);
                        break;
                }

                graphTypeCombo.setModel(comboModel);
            }
        });
        graphTypeCombo.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                int g = graphTypeCombo.getSelectedIndex();
                EdgeDirectionDefault dir = container.getUnloader().getEdgeDefault();
                if (dir.equals(EdgeDirectionDefault.UNDIRECTED)) {
                    switch (g) {
                        case 0:
                            container.getLoader().setEdgeDefault(EdgeDirectionDefault.UNDIRECTED);
                            break;
                        case 1:
                            container.getLoader().setEdgeDefault(EdgeDirectionDefault.MIXED);
                            break;
                    }
                } else {
                    switch (g) {
                        case 0:
                            container.getLoader().setEdgeDefault(EdgeDirectionDefault.DIRECTED);
                            break;
                        case 1:
                            container.getLoader().setEdgeDefault(EdgeDirectionDefault.UNDIRECTED);
                            break;
                        case 2:
                            container.getLoader().setEdgeDefault(EdgeDirectionDefault.MIXED);
                            break;
                    }
                }

            }
        });
    }

    private void fillIssues(Report report) {
        final List<Issue> issues = report.getIssues();
        if (issues.isEmpty()) {
            JLabel label = new JLabel(NbBundle.getMessage(getClass(), "ReportPanel.noIssues"));
            label.setHorizontalAlignment(SwingConstants.CENTER);
            tab1ScrollPane.setViewportView(label);
        } else {
            //Busy label
            final BusyUtils.BusyLabel busyLabel = BusyUtils.createCenteredBusyLabel(tab1ScrollPane, "Retrieving issues...", issuesOutline);

            //Thread
            Thread thread = new Thread(fillingThreads, new Runnable() {
                @Override
                public void run() {
                    busyLabel.setBusy(true);
                    final TreeModel treeMdl = new IssueTreeModel(issues);
                    final OutlineModel mdl = DefaultOutlineModel.createOutlineModel(treeMdl, new IssueRowModel(), true);

                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
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
            @Override
            public void run() {
                final String str = report.getText();
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
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

    private void fillParameters(final Container container) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                //Autoscale
                autoscaleCheckbox.setSelected(container.getUnloader().isAutoScale());
                selfLoopCheckBox.setSelected(container.getUnloader().allowSelfLoop());
                createMissingNodesCheckbox.setSelected(container.getUnloader().allowAutoNode());

                switch (container.getUnloader().getEdgeDefault()) {
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

                switch (container.getUnloader().getEdgesMergeStrategy()) {
                    case SUM:
                        edgesMergeStrategyCombo.setSelectedIndex(0);
                        break;
                    case AVG:
                        edgesMergeStrategyCombo.setSelectedIndex(1);
                        break;
                    case MIN:
                        edgesMergeStrategyCombo.setSelectedIndex(2);
                        break;
                    case MAX:
                        edgesMergeStrategyCombo.setSelectedIndex(3);
                        break;
                }
            }
        });
    }

    private void fillStats(final Container container) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                //Source
                String source = container.getSource();
                String[] label = source.split("\\.");
                if (label.length > 2 && label[label.length - 2].matches("\\d+")) { //case of temp file
                    source = source.replaceFirst("." + label[label.length - 2], "");
                }

                sourceLabel.setText(source);

                ContainerUnloader unloader = container.getUnloader();

                //Node & Edge count
                int nodeCount = unloader.getNodeCount();
                int edgeCount = unloader.getEdgeCount();
                nodeCountLabel.setText("" + nodeCount);
                edgeCountLabel.setText("" + edgeCount);

                //Dynamic & Hierarchical graph
                String yes = NbBundle.getMessage(getClass(), "ReportPanel.yes");
                String no = NbBundle.getMessage(getClass(), "ReportPanel.no");
                dynamicLabel.setText(container.isDynamicGraph() ? yes : no);
                dynamicAttsLabel.setText(container.hasDynamicAttributes() ? yes : no);
                multigraphLabel.setText(container.isMultiGraph() ? yes : no);
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
            GridBagConstraints constraints = new GridBagConstraints(0, i++, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0);
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

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
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
        processorPanel = new javax.swing.JPanel();
        statsPanel = new javax.swing.JPanel();
        labelNodeCount = new javax.swing.JLabel();
        labelEdgeCount = new javax.swing.JLabel();
        nodeCountLabel = new javax.swing.JLabel();
        edgeCountLabel = new javax.swing.JLabel();
        dynamicLabel = new javax.swing.JLabel();
        labelDynamic = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        labelMultiGraph = new javax.swing.JLabel();
        multigraphLabel = new javax.swing.JLabel();
        labelDynamicAtts = new javax.swing.JLabel();
        dynamicAttsLabel = new javax.swing.JLabel();
        moreOptionsLink = new org.jdesktop.swingx.JXHyperlink();
        moreOptionsPanel = new javax.swing.JPanel();
        autoscaleCheckbox = new javax.swing.JCheckBox();
        createMissingNodesCheckbox = new javax.swing.JCheckBox();
        selfLoopCheckBox = new javax.swing.JCheckBox();
        labelParallelEdgesMergeStrategy = new javax.swing.JLabel();
        edgesMergeStrategyCombo = new javax.swing.JComboBox();

        labelSrc.setText(org.openide.util.NbBundle.getMessage(ReportPanel.class, "ReportPanel.labelSrc.text")); // NOI18N

        sourceLabel.setText(org.openide.util.NbBundle.getMessage(ReportPanel.class, "ReportPanel.sourceLabel.text")); // NOI18N

        tab1ScrollPane.setViewportView(issuesOutline);

        tabbedPane.addTab(org.openide.util.NbBundle.getMessage(ReportPanel.class, "ReportPanel.tab1ScrollPane.TabConstraints.tabTitle"), tab1ScrollPane); // NOI18N

        tab2ScrollPane.setViewportView(reportEditor);

        tabbedPane.addTab(org.openide.util.NbBundle.getMessage(ReportPanel.class, "ReportPanel.tab2ScrollPane.TabConstraints.tabTitle"), tab2ScrollPane); // NOI18N

        labelGraphType.setText(org.openide.util.NbBundle.getMessage(ReportPanel.class, "ReportPanel.labelGraphType.text")); // NOI18N

        processorPanel.setLayout(new java.awt.GridBagLayout());

        statsPanel.setLayout(new java.awt.GridBagLayout());

        labelNodeCount.setFont(labelNodeCount.getFont().deriveFont(labelNodeCount.getFont().getStyle() | java.awt.Font.BOLD));
        labelNodeCount.setText(org.openide.util.NbBundle.getMessage(ReportPanel.class, "ReportPanel.labelNodeCount.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 6, 0);
        statsPanel.add(labelNodeCount, gridBagConstraints);

        labelEdgeCount.setFont(labelEdgeCount.getFont().deriveFont(labelEdgeCount.getFont().getStyle() | java.awt.Font.BOLD));
        labelEdgeCount.setText(org.openide.util.NbBundle.getMessage(ReportPanel.class, "ReportPanel.labelEdgeCount.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 0);
        statsPanel.add(labelEdgeCount, gridBagConstraints);

        nodeCountLabel.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        nodeCountLabel.setText(org.openide.util.NbBundle.getMessage(ReportPanel.class, "ReportPanel.nodeCountLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 6, 0);
        statsPanel.add(nodeCountLabel, gridBagConstraints);

        edgeCountLabel.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        edgeCountLabel.setText(org.openide.util.NbBundle.getMessage(ReportPanel.class, "ReportPanel.edgeCountLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 10, 0);
        statsPanel.add(edgeCountLabel, gridBagConstraints);

        dynamicLabel.setText(org.openide.util.NbBundle.getMessage(ReportPanel.class, "ReportPanel.dynamicLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 6, 0);
        statsPanel.add(dynamicLabel, gridBagConstraints);

        labelDynamic.setText(org.openide.util.NbBundle.getMessage(ReportPanel.class, "ReportPanel.labelDynamic.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        statsPanel.add(labelDynamic, gridBagConstraints);

        jLabel1.setText(org.openide.util.NbBundle.getMessage(ReportPanel.class, "ReportPanel.jLabel1.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 1.0;
        statsPanel.add(jLabel1, gridBagConstraints);

        labelMultiGraph.setText(org.openide.util.NbBundle.getMessage(ReportPanel.class, "ReportPanel.labelMultiGraph.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        statsPanel.add(labelMultiGraph, gridBagConstraints);

        multigraphLabel.setText(org.openide.util.NbBundle.getMessage(ReportPanel.class, "ReportPanel.multigraphLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 6, 0);
        statsPanel.add(multigraphLabel, gridBagConstraints);

        labelDynamicAtts.setText(org.openide.util.NbBundle.getMessage(ReportPanel.class, "ReportPanel.labelDynamicAtts.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        statsPanel.add(labelDynamicAtts, gridBagConstraints);

        dynamicAttsLabel.setText(org.openide.util.NbBundle.getMessage(ReportPanel.class, "ReportPanel.dynamicAttsLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 6, 0);
        statsPanel.add(dynamicAttsLabel, gridBagConstraints);

        moreOptionsLink.setText(org.openide.util.NbBundle.getMessage(ReportPanel.class, "ReportPanel.moreOptionsLink.text")); // NOI18N
        moreOptionsLink.setClickedColor(new java.awt.Color(0, 51, 255));

        moreOptionsPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        autoscaleCheckbox.setText(org.openide.util.NbBundle.getMessage(ReportPanel.class, "ReportPanel.autoscaleCheckbox.text")); // NOI18N
        autoscaleCheckbox.setToolTipText(org.openide.util.NbBundle.getMessage(ReportPanel.class, "ReportPanel.autoscaleCheckbox.toolTipText")); // NOI18N

        createMissingNodesCheckbox.setText(org.openide.util.NbBundle.getMessage(ReportPanel.class, "ReportPanel.createMissingNodesCheckbox.text")); // NOI18N
        createMissingNodesCheckbox.setToolTipText(org.openide.util.NbBundle.getMessage(ReportPanel.class, "ReportPanel.createMissingNodesCheckbox.toolTipText")); // NOI18N

        selfLoopCheckBox.setText(org.openide.util.NbBundle.getMessage(ReportPanel.class, "ReportPanel.selfLoopCheckBox.text")); // NOI18N
        selfLoopCheckBox.setToolTipText(org.openide.util.NbBundle.getMessage(ReportPanel.class, "ReportPanel.selfLoopCheckBox.toolTipText")); // NOI18N
        selfLoopCheckBox.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);

        labelParallelEdgesMergeStrategy.setText(org.openide.util.NbBundle.getMessage(ReportPanel.class, "ReportPanel.labelParallelEdgesMergeStrategy.text")); // NOI18N

        javax.swing.GroupLayout moreOptionsPanelLayout = new javax.swing.GroupLayout(moreOptionsPanel);
        moreOptionsPanel.setLayout(moreOptionsPanelLayout);
        moreOptionsPanelLayout.setHorizontalGroup(
            moreOptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(moreOptionsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(moreOptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(moreOptionsPanelLayout.createSequentialGroup()
                        .addComponent(autoscaleCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, 168, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(labelParallelEdgesMergeStrategy)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(edgesMergeStrategyCombo, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(moreOptionsPanelLayout.createSequentialGroup()
                        .addGroup(moreOptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(createMissingNodesCheckbox)
                            .addComponent(selfLoopCheckBox))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        moreOptionsPanelLayout.setVerticalGroup(
            moreOptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(moreOptionsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(moreOptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelParallelEdgesMergeStrategy)
                    .addComponent(edgesMergeStrategyCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(autoscaleCheckbox))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(createMissingNodesCheckbox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(selfLoopCheckBox)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(tabbedPane, javax.swing.GroupLayout.DEFAULT_SIZE, 600, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(labelSrc)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(sourceLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(labelGraphType)
                                .addGap(18, 18, 18)
                                .addComponent(graphTypeCombo, javax.swing.GroupLayout.PREFERRED_SIZE, 152, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addComponent(statsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(moreOptionsLink, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(8, 8, 8))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(18, 18, 18)
                                .addComponent(processorPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 173, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addComponent(moreOptionsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelSrc)
                    .addComponent(sourceLabel))
                .addGap(18, 18, 18)
                .addComponent(tabbedPane, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelGraphType)
                    .addComponent(graphTypeCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(moreOptionsLink, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(moreOptionsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(statsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 122, Short.MAX_VALUE)
                    .addComponent(processorPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox autoscaleCheckbox;
    private javax.swing.JCheckBox createMissingNodesCheckbox;
    private javax.swing.JLabel dynamicAttsLabel;
    private javax.swing.JLabel dynamicLabel;
    private javax.swing.JLabel edgeCountLabel;
    private javax.swing.JComboBox edgesMergeStrategyCombo;
    private javax.swing.JComboBox graphTypeCombo;
    private org.netbeans.swing.outline.Outline issuesOutline;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel labelDynamic;
    private javax.swing.JLabel labelDynamicAtts;
    private javax.swing.JLabel labelEdgeCount;
    private javax.swing.JLabel labelGraphType;
    private javax.swing.JLabel labelMultiGraph;
    private javax.swing.JLabel labelNodeCount;
    private javax.swing.JLabel labelParallelEdgesMergeStrategy;
    private javax.swing.JLabel labelSrc;
    private org.jdesktop.swingx.JXHyperlink moreOptionsLink;
    private javax.swing.JPanel moreOptionsPanel;
    private javax.swing.JLabel multigraphLabel;
    private javax.swing.JLabel nodeCountLabel;
    private javax.swing.JPanel processorPanel;
    private javax.swing.ButtonGroup processorStrategyRadio;
    private javax.swing.JEditorPane reportEditor;
    private javax.swing.JCheckBox selfLoopCheckBox;
    private javax.swing.JLabel sourceLabel;
    private javax.swing.JPanel statsPanel;
    private javax.swing.JScrollPane tab1ScrollPane;
    private javax.swing.JScrollPane tab2ScrollPane;
    private javax.swing.JTabbedPane tabbedPane;
    // End of variables declaration//GEN-END:variables

    private class IssueTreeModel implements TreeModel {

        private List<Issue> issues;

        public IssueTreeModel(List<Issue> issues) {
            this.issues = issues;
        }

        @Override
        public Object getRoot() {
            return "root";
        }

        @Override
        public Object getChild(Object parent, int index) {
            return issues.get(index);
        }

        @Override
        public int getChildCount(Object parent) {
            return issues.size();
        }

        @Override
        public boolean isLeaf(Object node) {
            if (node instanceof Issue) {
                return true;
            }
            return false;
        }

        @Override
        public void valueForPathChanged(TreePath path, Object newValue) {
        }

        @Override
        public int getIndexOfChild(Object parent, Object child) {
            return issues.indexOf(child);
        }

        @Override
        public void addTreeModelListener(TreeModelListener l) {
        }

        @Override
        public void removeTreeModelListener(TreeModelListener l) {
        }
    }

    private class IssueRowModel implements RowModel {

        @Override
        public int getColumnCount() {
            return 1;
        }

        @Override
        public Object getValueFor(Object node, int column) {
            if (node instanceof Issue) {
                Issue issue = (Issue) node;
                return issue.getLevel().toString();
            }
            return "";
        }

        @Override
        public Class getColumnClass(int column) {
            return String.class;
        }

        @Override
        public boolean isCellEditable(Object node, int column) {
            return false;
        }

        @Override
        public void setValueFor(Object node, int column, Object value) {
        }

        @Override
        public String getColumnName(int column) {
            return NbBundle.getMessage(ReportPanel.class, "ReportPanel.issueTable.issues");
        }
    }

    private class IssueRenderer implements RenderDataProvider {

        @Override
        public String getDisplayName(Object o) {
            Issue issue = (Issue) o;
            return issue.getMessage();
        }

        @Override
        public boolean isHtmlDisplayName(Object o) {
            return false;
        }

        @Override
        public Color getBackground(Object o) {
            return null;
        }

        @Override
        public Color getForeground(Object o) {
            return null;
        }

        @Override
        public String getTooltipText(Object o) {
            return "";
        }

        @Override
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
