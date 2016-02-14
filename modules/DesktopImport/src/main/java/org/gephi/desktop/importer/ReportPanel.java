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
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JRootPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import org.gephi.io.importer.api.Container;
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
    private final ThreadGroup fillingThreads;
    //Icons
    private ImageIcon infoIcon;
    private ImageIcon warningIcon;
    private ImageIcon severeIcon;
    private ImageIcon criticalIcon;
    //Container
    private Container[] containers;
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
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    boolean s = autoscaleCheckbox.isSelected();
                    for (Container container : containers) {
                        container.getLoader().setAutoScale(s);
                    }
                }
            }
        });

        createMissingNodesCheckbox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    boolean s = createMissingNodesCheckbox.isSelected();
                    for (Container container : containers) {
                        container.getLoader().setAllowAutoNode(s);
                    }
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
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    switch (g) {
                        case 0:
                            for (Container container : containers) {
                                container.getLoader().setEdgesMergeStrategy(EdgeWeightMergeStrategy.SUM);
                            }
                            break;
                        case 1:
                            for (Container container : containers) {
                                container.getLoader().setEdgesMergeStrategy(EdgeWeightMergeStrategy.AVG);
                            }
                            break;
                        case 2:
                            for (Container container : containers) {
                                container.getLoader().setEdgesMergeStrategy(EdgeWeightMergeStrategy.MIN);
                            }
                            break;
                        case 3:
                            for (Container container : containers) {
                                container.getLoader().setEdgesMergeStrategy(EdgeWeightMergeStrategy.MAX);
                            }
                            break;
                    }
                }
            }
        });

        selfLoopCheckBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    boolean s = selfLoopCheckBox.isSelected();
                    for (Container container : containers) {
                        container.getLoader().setAllowSelfLoop(s);
                    }
                }
            }
        });

        reportEditor.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    JPopupMenu contextMenu = new JPopupMenu();
                    JMenuItem menuItem = new JMenuItem();
                    menuItem.setText(NbBundle.getMessage(ReportPanel.class, "ReportPanel.reportCopy.text"));
                    menuItem.setToolTipText(NbBundle.getMessage(ReportPanel.class, "ReportPanel.reportCopy.description"));
                    menuItem.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
                            clpbrd.setContents(new StringSelection(reportEditor.getText()), null);
                        }
                    });
                    contextMenu.add(menuItem);
                    contextMenu.show(reportEditor, e.getX(), e.getY());
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

    public void setData(Report report, Container[] containers) {
        this.containers = containers;
        initGraphTypeCombo(containers);

        initProcessorsUI();

        fillIssues(report);
        fillReport(report);

        fillStats(containers);
        fillParameters(containers);
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

    private void initGraphTypeCombo(final Container[] containers) {
        final String directedStr = NbBundle.getMessage(ReportPanel.class, "ReportPanel.graphType.directed");
        final String undirectedStr = NbBundle.getMessage(ReportPanel.class, "ReportPanel.graphType.undirected");
        final String mixedStr = NbBundle.getMessage(ReportPanel.class, "ReportPanel.graphType.mixed");

        EdgeDirectionDefault edd = null;
        for (Container container : containers) {
            EdgeDirectionDefault d = container.getUnloader().getEdgeDefault();
            if (edd == null) {
                edd = d;
            } else if (d.equals(EdgeDirectionDefault.UNDIRECTED) && !edd.equals(EdgeDirectionDefault.UNDIRECTED)) {
                edd = EdgeDirectionDefault.MIXED;
            } else if (d.equals(EdgeDirectionDefault.DIRECTED) && !edd.equals(EdgeDirectionDefault.DIRECTED)) {
                edd = EdgeDirectionDefault.MIXED;
            }
        }
        final EdgeDirectionDefault dir = edd;

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {

                DefaultComboBoxModel comboModel = new DefaultComboBoxModel();

                switch (dir) {
                    case DIRECTED:
                        comboModel.addElement(directedStr);
                        comboModel.addElement(undirectedStr);
                        comboModel.addElement(mixedStr);
                        comboModel.setSelectedItem(directedStr);
                        break;
                    case UNDIRECTED:
                        comboModel.addElement(undirectedStr);
                        comboModel.addElement(mixedStr);
                        comboModel.setSelectedItem(undirectedStr);
                        break;
                    case MIXED:
                        comboModel.addElement(directedStr);
                        comboModel.addElement(undirectedStr);
                        comboModel.addElement(mixedStr);
                        comboModel.setSelectedItem(mixedStr);
                        break;
                }

                graphTypeCombo.setModel(comboModel);
            }
        });
        graphTypeCombo.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    Object g = e.getItem();
                    if (g.equals(directedStr)) {
                        for (Container container : containers) {
                            container.getLoader().setEdgeDefault(EdgeDirectionDefault.DIRECTED);
                        }
                    } else if (g.equals(undirectedStr)) {
                        for (Container container : containers) {
                            container.getLoader().setEdgeDefault(EdgeDirectionDefault.UNDIRECTED);
                        }
                    } else if (g.equals(mixedStr)) {
                        for (Container container : containers) {
                            container.getLoader().setEdgeDefault(EdgeDirectionDefault.MIXED);
                        }
                    }
                }
            }
        });
    }

    private void fillIssues(Report report) {
        final List<Issue> issues = new ArrayList<>();
        Iterator<Issue> itr = report.getIssues(ISSUES_LIMIT);
        while (itr.hasNext()) {
            issues.add(itr.next());
        }
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

    private void fillParameters(final Container[] containers) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                //Autoscale
                autoscaleCheckbox.setSelected(containers[0].getUnloader().isAutoScale());
                selfLoopCheckBox.setSelected(containers[0].getUnloader().allowSelfLoop());
                createMissingNodesCheckbox.setSelected(containers[0].getUnloader().allowAutoNode());

                switch (containers[0].getUnloader().getEdgesMergeStrategy()) {
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

    private void fillStats(final Container[] containers) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                //Source
                String source;
                if (containers.length == 1) {
                    source = containers[0].getSource();
                    String[] label = source.split("\\.");
                    if (label.length > 2 && label[label.length - 2].matches("\\d+")) { //case of temp file
                        source = source.replaceFirst("." + label[label.length - 2], "");
                    }
                } else {
                    source = NbBundle.getMessage(ReportPanel.class, "ReportPanel.multiSourceLabel.text");
                }
                sourceLabel.setText(source);

                //Node & Edge count
                int nodeCount = 0, edgeCount = 0;
                boolean dynamic = false, dynamicAtts = false, multiGraph = false;
                for (Container container : containers) {
                    nodeCount += container.getUnloader().getNodeCount();
                    edgeCount += container.getUnloader().getEdgeCount();
                    dynamic |= container.isDynamicGraph();
                    dynamicAtts |= container.hasDynamicAttributes();
                    multiGraph |= container.isMultiGraph();
                }
                graphCountLabel.setText("" + containers.length);
                nodeCountLabel.setText("" + nodeCount);
                edgeCountLabel.setText("" + edgeCount);

                //Dynamic graph
                String yes = NbBundle.getMessage(getClass(), "ReportPanel.yes");
                String no = NbBundle.getMessage(getClass(), "ReportPanel.no");
                dynamicLabel.setText(dynamic ? yes : no);
                dynamicAttsLabel.setText(dynamicAtts ? yes : no);
                multigraphLabel.setText(multiGraph ? yes : no);

                //Multi sources
                if (containers.length == 1) {
                    graphCountLabel.setVisible(false);
                    labelGraphCount.setVisible(false);
                }
            }
        });
    }
    private static final Object PROCESSOR_KEY = new Object();

    private void initProcessors() {
        int i = 0;
        for (Processor processor : Lookup.getDefault().lookupAll(Processor.class)) {
            JRadioButton radio = new JRadioButton(processor.getDisplayName());
            radio.putClientProperty(PROCESSOR_KEY, processor);
            processorGroup.add(radio);
        }
    }

    private void initProcessorsUI() {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {

                List<AbstractButton> validButtons = new ArrayList<>();
                for (Enumeration<AbstractButton> enumeration = processorGroup.getElements(); enumeration.hasMoreElements();) {
                    AbstractButton radioButton = enumeration.nextElement();
                    Processor p = (Processor) radioButton.getClientProperty(PROCESSOR_KEY);
                    //Enabled
                    ProcessorUI pui = getProcessorUI(p);
                    if (pui != null) {
                        boolean isValid = pui.isValid(containers);
                        if (isValid) {
                            validButtons.add(radioButton);
                        }
                    }
                }

                int i = 0;
                for (AbstractButton radio : validButtons) {
                    radio.setSelected(i == 0);
                    GridBagConstraints constraints = new GridBagConstraints(0, i++, 1, 1, 0, (i == validButtons.size() ? 1.0 : 0.0), GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0);
                    processorPanel.add(radio, constraints);
                }
            }
        });
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
        processorPanel = new javax.swing.JPanel();
        statsPanel = new javax.swing.JPanel();
        labelNodeCount = new javax.swing.JLabel();
        labelEdgeCount = new javax.swing.JLabel();
        nodeCountLabel = new javax.swing.JLabel();
        edgeCountLabel = new javax.swing.JLabel();
        dynamicLabel = new javax.swing.JLabel();
        labelDynamic = new javax.swing.JLabel();
        labelMultiGraph = new javax.swing.JLabel();
        multigraphLabel = new javax.swing.JLabel();
        labelDynamicAtts = new javax.swing.JLabel();
        dynamicAttsLabel = new javax.swing.JLabel();
        labelGraphCount = new javax.swing.JLabel();
        graphCountLabel = new javax.swing.JLabel();
        moreOptionsPanel = new javax.swing.JPanel();
        moreOptionsLeftPanel = new javax.swing.JPanel();
        autoscaleCheckbox = new javax.swing.JCheckBox();
        createMissingNodesCheckbox = new javax.swing.JCheckBox();
        selfLoopCheckBox = new javax.swing.JCheckBox();
        labelParallelEdgesMergeStrategy = new javax.swing.JLabel();
        edgesMergeStrategyCombo = new javax.swing.JComboBox();
        graphTypeCombo = new javax.swing.JComboBox();
        moreOptionsLink = new org.jdesktop.swingx.JXHyperlink();

        labelSrc.setText(org.openide.util.NbBundle.getMessage(ReportPanel.class, "ReportPanel.labelSrc.text")); // NOI18N

        sourceLabel.setText(org.openide.util.NbBundle.getMessage(ReportPanel.class, "ReportPanel.sourceLabel.text")); // NOI18N

        tab1ScrollPane.setViewportView(issuesOutline);

        tabbedPane.addTab(org.openide.util.NbBundle.getMessage(ReportPanel.class, "ReportPanel.tab1ScrollPane.TabConstraints.tabTitle"), tab1ScrollPane); // NOI18N

        reportEditor.setEditable(false);
        reportEditor.setFocusable(false);
        tab2ScrollPane.setViewportView(reportEditor);

        tabbedPane.addTab(org.openide.util.NbBundle.getMessage(ReportPanel.class, "ReportPanel.tab2ScrollPane.TabConstraints.tabTitle"), tab2ScrollPane); // NOI18N

        labelGraphType.setText(org.openide.util.NbBundle.getMessage(ReportPanel.class, "ReportPanel.labelGraphType.text")); // NOI18N

        processorPanel.setLayout(new java.awt.GridBagLayout());

        statsPanel.setLayout(new java.awt.GridBagLayout());

        labelNodeCount.setFont(labelNodeCount.getFont().deriveFont(labelNodeCount.getFont().getStyle() | java.awt.Font.BOLD));
        labelNodeCount.setText(org.openide.util.NbBundle.getMessage(ReportPanel.class, "ReportPanel.labelNodeCount.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 2, 6, 0);
        statsPanel.add(labelNodeCount, gridBagConstraints);

        labelEdgeCount.setFont(labelEdgeCount.getFont().deriveFont(labelEdgeCount.getFont().getStyle() | java.awt.Font.BOLD));
        labelEdgeCount.setText(org.openide.util.NbBundle.getMessage(ReportPanel.class, "ReportPanel.labelEdgeCount.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 2, 10, 0);
        statsPanel.add(labelEdgeCount, gridBagConstraints);

        nodeCountLabel.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        nodeCountLabel.setText(org.openide.util.NbBundle.getMessage(ReportPanel.class, "ReportPanel.nodeCountLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 6, 0);
        statsPanel.add(nodeCountLabel, gridBagConstraints);

        edgeCountLabel.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        edgeCountLabel.setText(org.openide.util.NbBundle.getMessage(ReportPanel.class, "ReportPanel.edgeCountLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 10, 0);
        statsPanel.add(edgeCountLabel, gridBagConstraints);

        dynamicLabel.setText(org.openide.util.NbBundle.getMessage(ReportPanel.class, "ReportPanel.dynamicLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 6, 0);
        statsPanel.add(dynamicLabel, gridBagConstraints);

        labelDynamic.setText(org.openide.util.NbBundle.getMessage(ReportPanel.class, "ReportPanel.labelDynamic.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 2, 6, 0);
        statsPanel.add(labelDynamic, gridBagConstraints);

        labelMultiGraph.setText(org.openide.util.NbBundle.getMessage(ReportPanel.class, "ReportPanel.labelMultiGraph.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 2, 6, 0);
        statsPanel.add(labelMultiGraph, gridBagConstraints);

        multigraphLabel.setText(org.openide.util.NbBundle.getMessage(ReportPanel.class, "ReportPanel.multigraphLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 6, 0);
        statsPanel.add(multigraphLabel, gridBagConstraints);

        labelDynamicAtts.setText(org.openide.util.NbBundle.getMessage(ReportPanel.class, "ReportPanel.labelDynamicAtts.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 2, 6, 0);
        statsPanel.add(labelDynamicAtts, gridBagConstraints);

        dynamicAttsLabel.setText(org.openide.util.NbBundle.getMessage(ReportPanel.class, "ReportPanel.dynamicAttsLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 6, 0);
        statsPanel.add(dynamicAttsLabel, gridBagConstraints);

        labelGraphCount.setFont(labelGraphCount.getFont().deriveFont(labelGraphCount.getFont().getStyle() | java.awt.Font.BOLD));
        labelGraphCount.setText(org.openide.util.NbBundle.getMessage(ReportPanel.class, "ReportPanel.labelGraphCount.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 2, 6, 0);
        statsPanel.add(labelGraphCount, gridBagConstraints);

        graphCountLabel.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        graphCountLabel.setText(org.openide.util.NbBundle.getMessage(ReportPanel.class, "ReportPanel.graphCountLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 10, 6, 0);
        statsPanel.add(graphCountLabel, gridBagConstraints);

        moreOptionsPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        moreOptionsPanel.setLayout(new java.awt.GridBagLayout());

        moreOptionsLeftPanel.setLayout(new java.awt.GridBagLayout());

        autoscaleCheckbox.setText(org.openide.util.NbBundle.getMessage(ReportPanel.class, "ReportPanel.autoscaleCheckbox.text")); // NOI18N
        autoscaleCheckbox.setToolTipText(org.openide.util.NbBundle.getMessage(ReportPanel.class, "ReportPanel.autoscaleCheckbox.toolTipText")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
        moreOptionsLeftPanel.add(autoscaleCheckbox, gridBagConstraints);

        createMissingNodesCheckbox.setText(org.openide.util.NbBundle.getMessage(ReportPanel.class, "ReportPanel.createMissingNodesCheckbox.text")); // NOI18N
        createMissingNodesCheckbox.setToolTipText(org.openide.util.NbBundle.getMessage(ReportPanel.class, "ReportPanel.createMissingNodesCheckbox.toolTipText")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        moreOptionsLeftPanel.add(createMissingNodesCheckbox, gridBagConstraints);

        selfLoopCheckBox.setText(org.openide.util.NbBundle.getMessage(ReportPanel.class, "ReportPanel.selfLoopCheckBox.text")); // NOI18N
        selfLoopCheckBox.setToolTipText(org.openide.util.NbBundle.getMessage(ReportPanel.class, "ReportPanel.selfLoopCheckBox.toolTipText")); // NOI18N
        selfLoopCheckBox.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        moreOptionsLeftPanel.add(selfLoopCheckBox, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        moreOptionsPanel.add(moreOptionsLeftPanel, gridBagConstraints);

        labelParallelEdgesMergeStrategy.setText(org.openide.util.NbBundle.getMessage(ReportPanel.class, "ReportPanel.labelParallelEdgesMergeStrategy.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        moreOptionsPanel.add(labelParallelEdgesMergeStrategy, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.insets = new java.awt.Insets(2, 0, 0, 0);
        moreOptionsPanel.add(edgesMergeStrategyCombo, gridBagConstraints);

        moreOptionsLink.setText(org.openide.util.NbBundle.getMessage(ReportPanel.class, "ReportPanel.moreOptionsLink.text")); // NOI18N
        moreOptionsLink.setClickedColor(new java.awt.Color(0, 51, 255));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(tabbedPane, javax.swing.GroupLayout.DEFAULT_SIZE, 625, Short.MAX_VALUE)
                    .addComponent(moreOptionsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(labelSrc)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(sourceLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(statsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(18, 18, 18)
                        .addComponent(processorPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 173, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(labelGraphType)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(graphTypeCombo, javax.swing.GroupLayout.PREFERRED_SIZE, 152, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(moreOptionsLink, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelSrc)
                    .addComponent(sourceLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tabbedPane, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelGraphType)
                    .addComponent(graphTypeCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(moreOptionsLink, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(moreOptionsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(processorPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(statsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox autoscaleCheckbox;
    private javax.swing.JCheckBox createMissingNodesCheckbox;
    private javax.swing.JLabel dynamicAttsLabel;
    private javax.swing.JLabel dynamicLabel;
    private javax.swing.JLabel edgeCountLabel;
    private javax.swing.JComboBox edgesMergeStrategyCombo;
    private javax.swing.JLabel graphCountLabel;
    private javax.swing.JComboBox graphTypeCombo;
    private org.netbeans.swing.outline.Outline issuesOutline;
    private javax.swing.JLabel labelDynamic;
    private javax.swing.JLabel labelDynamicAtts;
    private javax.swing.JLabel labelEdgeCount;
    private javax.swing.JLabel labelGraphCount;
    private javax.swing.JLabel labelGraphType;
    private javax.swing.JLabel labelMultiGraph;
    private javax.swing.JLabel labelNodeCount;
    private javax.swing.JLabel labelParallelEdgesMergeStrategy;
    private javax.swing.JLabel labelSrc;
    private javax.swing.JPanel moreOptionsLeftPanel;
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

        private final List<Issue> issues;

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
            return node instanceof Issue;
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
