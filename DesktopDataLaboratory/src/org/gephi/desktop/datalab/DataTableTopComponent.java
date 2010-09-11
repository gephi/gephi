/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>, Mathieu Jacomy, Julian Bilcke, Eduardo Ramos
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
package org.gephi.desktop.datalab;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.AWTEventListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeEvent;
import org.gephi.data.attributes.api.AttributeEvent.EventType;
import org.gephi.data.attributes.api.AttributeListener;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.data.attributes.api.AttributeTable;
import org.gephi.datalab.api.DataTablesController;
import org.gephi.datalab.api.DataTablesEventListener;
import org.gephi.datalab.spi.columns.AttributeColumnsManipulator;
import org.gephi.datalab.spi.general.GeneralActionsManipulator;
import org.gephi.datalab.spi.general.PluginGeneralActionsManipulator;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphEvent;
import org.gephi.graph.api.GraphListener;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.HierarchicalGraph;
import org.gephi.graph.api.Node;
import org.gephi.project.api.ProjectController;
import org.gephi.ui.utils.BusyUtils;
import org.gephi.project.api.Workspace;
import org.gephi.project.api.WorkspaceListener;
import org.gephi.ui.components.WrapLayout;
import org.gephi.desktop.datalab.general.actions.AddColumnUI;
import org.gephi.desktop.datalab.general.actions.CSVExportUI;
import org.gephi.desktop.datalab.general.actions.MergeColumnsUI;
import org.gephi.desktop.datalab.utils.DataLaboratoryHelper;
import org.gephi.ui.utils.DialogFileFilter;
import org.gephi.ui.utils.UIUtils;
import org.gephi.utils.TableCSVExporter;
import org.jvnet.flamingo.common.CommandButtonDisplayState;
import org.jvnet.flamingo.common.JCommandButton;
import org.jvnet.flamingo.common.JCommandButtonPanel;
import org.jvnet.flamingo.common.JCommandButtonStrip;
import org.jvnet.flamingo.common.JCommandMenuButton;
import org.jvnet.flamingo.common.RichTooltip;
import org.jvnet.flamingo.common.icon.ImageWrapperResizableIcon;
import org.jvnet.flamingo.common.popup.JCommandPopupMenu;
import org.jvnet.flamingo.common.popup.JPopupPanel;
import org.jvnet.flamingo.common.popup.PopupPanelCallback;
import org.netbeans.swing.etable.ETableColumnModel;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 *
 * @author Mathieu Bastian
 */
final class DataTableTopComponent extends TopComponent implements AWTEventListener, DataTablesEventListener, AttributeListener, GraphListener {

    private enum ClassDisplayed {

        NONE, NODE, EDGE
    };
    private static DataTableTopComponent instance;
    static final String ICON_PATH = "org/gephi/desktop/datalab/resources/small.png";
    private static final String PREFERRED_ID = "DataTableTopComponent";
    //Settings
    private static final String DATA_LABORATORY_DYNAMIC_FILTERING = "DataLaboratory_Dynamic_Filtering";
    private static final String DATA_LABORATORY_ONLY_VISIBLE = "DataLaboratory_visibleOnly";
    private static final String DATA_LABORATORY_SPARKLINES = "DataLaboratory_useSparklines";
    private static final String DATA_LABORATORY_TIME_INTERVAL_GRAPHICS = "DataLaboratory_timeIntervalGraphics";
    private static final String DATA_LABORATORY_EDGES_NODES_LABELS = "DataLaboratory_showEdgesNodesLabels";
    private static final Color invalidFilterColor = new Color(254, 150, 150);
    private final boolean dynamicFiltering;
    //Data
    private GraphModel graphModel;
    private DataTablesModel dataTablesModel;
    private boolean visibleOnly = false;
    private boolean useSparklines = false;
    private boolean timeIntervalGraphics = false;
    private boolean showEdgesNodesLabels = false;
    //Table
    private NodeDataTable nodeTable;
    private EdgeDataTable edgeTable;
    //General actions buttons
    private ArrayList<JComponent> generalActionsButtons = new ArrayList<JComponent>();
    //States
    private ClassDisplayed classDisplayed = ClassDisplayed.NODE;//Display nodes by default at first.
    //Executor
    private ExecutorService taskExecutor;
    private RefreshOnceHelperThread refreshOnceHelperThread;

    private DataTableTopComponent() {

        //Get saved preferences if existing:
        dynamicFiltering = NbPreferences.forModule(DataTableTopComponent.class).getBoolean(DATA_LABORATORY_DYNAMIC_FILTERING, true);
        visibleOnly = NbPreferences.forModule(DataTableTopComponent.class).getBoolean(DATA_LABORATORY_ONLY_VISIBLE, false);
        useSparklines = NbPreferences.forModule(DataTableTopComponent.class).getBoolean(DATA_LABORATORY_SPARKLINES, false);
        timeIntervalGraphics = NbPreferences.forModule(DataTableTopComponent.class).getBoolean(DATA_LABORATORY_TIME_INTERVAL_GRAPHICS, false);
        showEdgesNodesLabels = NbPreferences.forModule(DataTableTopComponent.class).getBoolean(DATA_LABORATORY_EDGES_NODES_LABELS, false);

        taskExecutor = new ThreadPoolExecutor(0, 1, 10L, TimeUnit.SECONDS, new LinkedBlockingDeque<Runnable>(20), new ThreadFactory() {

            public Thread newThread(Runnable r) {
                return new Thread(r, "Data Laboratory fetching");
            }
        });

        initComponents();

        columnManipulatorsPanel.setLayout(new WrapLayout(WrapLayout.CENTER, 25, 20));
        setName(NbBundle.getMessage(DataTableTopComponent.class, "CTL_DataTableTopComponent"));
        setIcon(ImageUtilities.loadImage(ICON_PATH));

        //toolbar
        Border b = (Border) UIManager.get("Nb.Editor.Toolbar.border"); //NOI18N
        controlToolbar.setBorder(b);
        if (UIUtils.isAquaLookAndFeel()) {
            controlToolbar.setBackground(UIManager.getColor("NbExplorerView.background"));
        }

        initEvents();

        //Init tables
        nodeTable = new NodeDataTable();
        edgeTable = new EdgeDataTable();

        nodeTable.setUseSparklines(useSparklines);
        nodeTable.setTimeIntervalGraphics(timeIntervalGraphics);
        edgeTable.setUseSparklines(useSparklines);
        edgeTable.setTimeIntervalGraphics(timeIntervalGraphics);
        edgeTable.setShowEdgesNodesLabels(showEdgesNodesLabels);


        //Init
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        if (pc.getCurrentWorkspace() == null) {
            clearAll();
        } else {
            refreshAll();
        }
        bannerPanel.setVisible(false);
    }

    private void initEvents() {

        //Workspace Listener
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        final GraphController gc = Lookup.getDefault().lookup(GraphController.class);
        pc.addWorkspaceListener(new WorkspaceListener() {

            public void initialize(Workspace workspace) {
                workspace.add(new DataTablesModel());
                //Prepare DataTablesEvent listener
                Lookup.getDefault().lookup(DataTablesController.class).setDataTablesEventListener(DataTableTopComponent.this);
            }

            public void select(Workspace workspace) {
                //Prepare DataTablesEvent listener
                Lookup.getDefault().lookup(DataTablesController.class).setDataTablesEventListener(DataTableTopComponent.this);

                hideTable();
                enableTableControls();
                bannerPanel.setVisible(false);

                AttributeModel attributeModel = Lookup.getDefault().lookup(AttributeController.class).getModel();
                attributeModel.addAttributeListener(DataTableTopComponent.this);

                graphModel = gc.getModel();
                graphModel.addGraphListener(DataTableTopComponent.this);
                dataTablesModel = workspace.getLookup().lookup(DataTablesModel.class);
                refreshAll();
            }

            public void unselect(Workspace workspace) {
                graphModel.removeGraphListener(DataTableTopComponent.this);

                AttributeModel attributeModel = workspace.getLookup().lookup(AttributeModel.class);
                attributeModel.removeAttributeListener(DataTableTopComponent.this);
                graphModel = null;
                dataTablesModel = null;
                clearAll();
            }

            public void close(Workspace workspace) {
                clearAll();
            }

            public void disable() {
                clearAll();
                //No more workspaces active, disable the DataTablesEvent listener
                Lookup.getDefault().lookup(DataTablesController.class).setDataTablesEventListener(null);
            }
        });
        if (pc.getCurrentWorkspace() != null) {
            //Prepare DataTablesEvent listener
            Lookup.getDefault().lookup(DataTablesController.class).setDataTablesEventListener(DataTableTopComponent.this);
            dataTablesModel = pc.getCurrentWorkspace().getLookup().lookup(DataTablesModel.class);
            graphModel = gc.getModel();
            graphModel.addGraphListener(DataTableTopComponent.this);

            AttributeModel attributeModel = pc.getCurrentWorkspace().getLookup().lookup(AttributeModel.class);
            attributeModel.addAttributeListener(DataTableTopComponent.this);
        }

        //Filter
        if (dynamicFiltering) {
            filterTextField.getDocument().addDocumentListener(new DocumentListener() {

                public void insertUpdate(DocumentEvent e) {
                    refreshFilter();
                }

                public void removeUpdate(DocumentEvent e) {
                    refreshFilter();
                }

                public void changedUpdate(DocumentEvent e) {
                }
            });
        } else {
            filterTextField.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    refreshFilter();
                }
            });
        }
    }

    private void refreshAll() {
        refreshTable();
        refreshColumnManipulators();
        refreshGeneralActionsButtons();
    }

    private void clearAll() {
        clearTableControls();
        clearColumnManipulators();
        clearGeneralActionsButtons();
    }

    public void attributesChanged(AttributeEvent event) {
        refreshOnce(event.is(EventType.SET_VALUE));
    }

    public void graphChanged(GraphEvent event) {
        refreshOnce(false);
    }

    private void refreshOnce(boolean refreshTableOnly) {
        if (refreshOnceHelperThread == null || !refreshOnceHelperThread.isAlive() || (refreshOnceHelperThread.refreshTableOnly && !refreshTableOnly)) {
            refreshOnceHelperThread = new RefreshOnceHelperThread(refreshTableOnly);
            refreshOnceHelperThread.start();
        } else {
            refreshOnceHelperThread.eventAttended();
        }
    }

    /****************Table related methods:*****************/
    private void refreshFilter() {
        if (classDisplayed.equals(ClassDisplayed.NODE)) {
            if (nodeTable.setFilter(filterTextField.getText(), columnComboBox.getSelectedIndex())) {
                filterTextField.setBackground(Color.WHITE);
            } else {
                filterTextField.setBackground(invalidFilterColor);
            }
        } else if (classDisplayed.equals(ClassDisplayed.EDGE)) {
            if (edgeTable.setPattern(filterTextField.getText(), columnComboBox.getSelectedIndex())) {
                filterTextField.setBackground(Color.WHITE);
            } else {
                filterTextField.setBackground(invalidFilterColor);
            }
        }
    }

    private void initNodesView() {
        Runnable initNodesRunnable = new Runnable() {

            public void run() {
                try {
                    String busyMsg = NbBundle.getMessage(DataTableTopComponent.class, "DataTableTopComponent.tableScrollPane.busyMessage");
                    BusyUtils.BusyLabel busylabel = BusyUtils.createCenteredBusyLabel(tableScrollPane, busyMsg, nodeTable.getOutlineTable());
                    busylabel.setBusy(true);

                    //Attributes columns
                    AttributeModel attModel = Lookup.getDefault().lookup(AttributeController.class).getModel();
                    final AttributeColumn[] cols = attModel.getNodeTable().getColumns();

                    //Nodes from DHNS
                    HierarchicalGraph graph;
                    if (visibleOnly) {
                        graph = graphModel.getHierarchicalGraphVisible();
                    } else {
                        graph = graphModel.getHierarchicalGraph();
                    }
                    if (graph == null) {
                        tableScrollPane.setViewportView(null);
                        return;
                    }

                    //Model
                    nodeTable.refreshModel(graph, cols, dataTablesModel);
                    refreshFilterColumns();

                    busylabel.setBusy(false);
                    nodeTable.scrollToFirstNodeSelected();
                } catch (Exception e) {
                    e.printStackTrace();
                    JLabel errorLabel = new JLabel(NbBundle.getMessage(DataTableTopComponent.class, "DataTableTopComponent.tableScrollPane.error"), SwingConstants.CENTER);
                    tableScrollPane.setViewportView(errorLabel);
                }
            }
        };
        Future future = taskExecutor.submit(initNodesRunnable);
    }

    private void initEdgesView() {
        Runnable initEdgesRunnable = new Runnable() {

            public void run() {
                try {
                    String busyMsg = NbBundle.getMessage(DataTableTopComponent.class, "DataTableTopComponent.tableScrollPane.busyMessage");
                    BusyUtils.BusyLabel busylabel = BusyUtils.createCenteredBusyLabel(tableScrollPane, busyMsg, edgeTable.getTable());
                    busylabel.setBusy(true);

                    //Attributes columns
                    AttributeModel attModel = Lookup.getDefault().lookup(AttributeController.class).getModel();
                    final AttributeColumn[] cols = attModel.getEdgeTable().getColumns();

                    //Edges from DHNS
                    HierarchicalGraph graph;
                    if (visibleOnly) {
                        graph = graphModel.getHierarchicalGraphVisible();
                    } else {
                        graph = graphModel.getHierarchicalGraph();
                    }
                    if (graph == null) {
                        tableScrollPane.setViewportView(null);
                        return;
                    }

                    //Model
                    edgeTable.refreshModel(graph, cols, dataTablesModel);
                    refreshFilterColumns();

                    busylabel.setBusy(false);
                    edgeTable.scrollToFirstEdgeSelected();
                } catch (Exception e) {
                    e.printStackTrace();
                    JLabel errorLabel = new JLabel(NbBundle.getMessage(DataTableTopComponent.class, "DataTableTopComponent.tableScrollPane.error"), SwingConstants.CENTER);
                    tableScrollPane.setViewportView(errorLabel);
                }
            }
        };
        Future future = taskExecutor.submit(initEdgesRunnable);
    }

    private void refreshFilterColumns() {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                if (classDisplayed.equals(ClassDisplayed.NODE)) {
                    ETableColumnModel columnModel = (ETableColumnModel) nodeTable.getOutlineTable().getColumnModel();
                    DefaultComboBoxModel model = new DefaultComboBoxModel();
                    for (int i = 0; i < columnModel.getColumnCount(); i++) {
                        if (!columnModel.isColumnHidden(columnModel.getColumn(i))) {
                            model.addElement(columnModel.getColumn(i).getHeaderValue());
                        }
                    }
                    columnComboBox.setModel(model);
                } else if (classDisplayed.equals(ClassDisplayed.EDGE)) {
                    DefaultComboBoxModel model = new DefaultComboBoxModel();
                    for (int i = 0; i < edgeTable.getTable().getColumnCount(); i++) {
                        if (edgeTable.getTable().getColumnExt(i).isVisible()) {
                            model.addElement(edgeTable.getTable().getColumnExt(i).getTitle());
                        }
                    }
                    columnComboBox.setModel(model);
                }
            }
        });
    }

    private void enableTableControls() {
        nodesButton.setEnabled(true);
        edgesButton.setEnabled(true);
        configurationButton.setEnabled(true);
        filterTextField.setEnabled(true);
        labelFilter.setEnabled(true);
    }

    private void clearTableControls() {
        elementGroup.clearSelection();
        nodesButton.setEnabled(false);
        edgesButton.setEnabled(false);
        configurationButton.setEnabled(false);
        filterTextField.setEnabled(false);
        labelFilter.setEnabled(false);
        bannerPanel.setVisible(false);
        hideTable();
    }

    private void hideTable() {
        tableScrollPane.setViewportView(null);
    }

    private void refreshTable() {
        bannerPanel.setVisible(false);
        if (classDisplayed.equals(ClassDisplayed.NODE)) {
            nodesButton.setSelected(true);
            initNodesView();
        } else if (classDisplayed.equals(ClassDisplayed.EDGE)) {
            edgesButton.setSelected(true);
            initEdgesView();
        }
    }

    public void selectNodesTable() {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                classDisplayed = classDisplayed.NODE;
                nodesButton.setSelected(true);
                tableScrollPane.setViewportView(nodeTable.getOutlineTable());
                refreshFilterColumns();
                refreshAll();
            }
        });
    }

    public void selectEdgesTable() {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                classDisplayed = classDisplayed.EDGE;
                edgesButton.setSelected(true);
                tableScrollPane.setViewportView(edgeTable.getTable());
                refreshFilterColumns();
                refreshAll();
            }
        });
    }

    public void refreshCurrentTable() {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                refreshTable();
            }
        });
    }

    public void setNodeTableSelection(final Node[] nodes) {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                nodeTable.setNodesSelection(nodes);
                nodeTable.scrollToFirstNodeSelected();
            }
        });
    }

    public void setEdgeTableSelection(final Edge[] edges) {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                edgeTable.setEdgesSelection(edges);
                edgeTable.scrollToFirstEdgeSelected();
            }
        });
    }

    public Node[] getNodeTableSelection() {
        return nodeTable.getNodesFromSelectedRows();
    }

    public Edge[] getEdgeTableSelection() {
        return edgeTable.getEdgesFromSelectedRows();
    }

    public boolean isNodeTableMode() {
        return classDisplayed == ClassDisplayed.NODE;
    }

    public boolean isEdgeTableMode() {
        return classDisplayed == ClassDisplayed.EDGE;
    }

    public boolean isShowOnlyVisible() {
        return visibleOnly;
    }

    public void setShowOnlyVisible(boolean showOnlyVisible) {
        visibleOnly = showOnlyVisible;
        refreshCurrentTable();
    }

    public boolean isUseSparklines() {
        return useSparklines;
    }

    public void setUseSparklines(boolean useSparklines) {
        this.useSparklines = useSparklines;
        nodeTable.setUseSparklines(useSparklines);
        edgeTable.setUseSparklines(useSparklines);
        refreshCurrentTable();
    }

    public boolean isTimeIntervalGraphics() {
        return timeIntervalGraphics;
    }

    public void setTimeIntervalGraphics(boolean timeIntervalGraphics) {
        this.timeIntervalGraphics = timeIntervalGraphics;
        nodeTable.setTimeIntervalGraphics(timeIntervalGraphics);
        edgeTable.setTimeIntervalGraphics(timeIntervalGraphics);
        refreshCurrentTable();
    }

    public boolean isShowEdgesNodesLabels() {
        return showEdgesNodesLabels;
    }

    public void setShowEdgesNodesLabels(boolean showEdgesNodesLabels) {
        this.showEdgesNodesLabels = showEdgesNodesLabels;
        edgeTable.setShowEdgesNodesLabels(showEdgesNodesLabels);
        refreshCurrentTable();
    }

    public void exportCurrentTable(ExportMode exportMode) {
        JTable table;
        if (classDisplayed == classDisplayed.NODE) {
            table = nodeTable.getOutlineTable();
        } else {
            table = edgeTable.getTable();
        }

        switch (exportMode) {
            case CSV:
                showCSVExportUI(table);
                break;
        }
    }

    private void showCSVExportUI(JTable table) {
        CSVExportUI csvUI = new CSVExportUI(table);
        DialogDescriptor dd = new DialogDescriptor(csvUI, csvUI.getDisplayName());
        if (DialogDisplayer.getDefault().notify(dd).equals(DialogDescriptor.OK_OPTION)) {
            DataTableTopComponent.exportTableAsCSV(this, table, csvUI.getSelectedSeparator(), csvUI.getSelectedCharset(), csvUI.getSelectedColumnsIndexes());
        }
        csvUI.unSetup();
    }

    /*************Column manipulators related methods:*************/
    private void refreshColumnManipulators() {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                clearColumnManipulators();
                prepareAddColumnButton();
                prepareMergeColumnsButton();
                prepareColumnManipulatorsButtons();
                columnManipulatorsPanel.updateUI();
            }
        });
    }

    private void clearColumnManipulators() {
        columnManipulatorsPanel.removeAll();
        columnManipulatorsPanel.updateUI();
    }

    /**
     * Creates the buttons that call the AttributeColumnManipulators.
     */
    private void prepareColumnManipulatorsButtons() {
        AttributeModel attributeModel = Lookup.getDefault().lookup(ProjectController.class).getCurrentWorkspace().getLookup().lookup(AttributeModel.class);
        AttributeTable table;
        if (classDisplayed == ClassDisplayed.NODE) {
            table = attributeModel.getNodeTable();
        } else {
            table = attributeModel.getEdgeTable();
        }

        DataLaboratoryHelper dlh = new DataLaboratoryHelper();
        AttributeColumnsManipulator[] manipulators = dlh.getAttributeColumnsManipulators();

        JCommandButtonStrip currentButtonGroup = new JCommandButtonStrip(JCommandButtonStrip.StripOrientation.HORIZONTAL);
        currentButtonGroup.setDisplayState(CommandButtonDisplayState.BIG);
        Integer lastManipulatorType = null;
        for (AttributeColumnsManipulator acm : manipulators) {
            if (lastManipulatorType == null) {
                lastManipulatorType = acm.getType();
            }
            if (lastManipulatorType != acm.getType()) {
                columnManipulatorsPanel.add(currentButtonGroup);
                currentButtonGroup = new JCommandButtonStrip(JCommandButtonStrip.StripOrientation.HORIZONTAL);
                currentButtonGroup.setDisplayState(CommandButtonDisplayState.BIG);
            }
            lastManipulatorType = acm.getType();
            currentButtonGroup.add(prepareJCommandButton(table, acm));
        }
        columnManipulatorsPanel.add(currentButtonGroup);
    }

    /**
     * Creates a JCommandButton for the specified table and AttributeColumnsManipulator
     * @param table Table
     * @param acm AttributeColumnsManipulator
     * @return Prepared JCommandButton
     */
    private JCommandButton prepareJCommandButton(final AttributeTable table, final AttributeColumnsManipulator acm) {
        final AttributeColumn[] columns = table.getColumns();
        JCommandButton manipulatorButton;
        if (acm.getIcon() != null) {
            manipulatorButton = new JCommandButton(acm.getName(), ImageWrapperResizableIcon.getIcon(acm.getIcon(), new Dimension(16, 16)));
        } else {
            manipulatorButton = new JCommandButton(acm.getName());
        }
        manipulatorButton.setCommandButtonKind(JCommandButton.CommandButtonKind.POPUP_ONLY);
        manipulatorButton.setDisplayState(CommandButtonDisplayState.MEDIUM);
        if (acm.getDescription() != null && !acm.getDescription().isEmpty()) {
            manipulatorButton.setPopupRichTooltip(new RichTooltip(NbBundle.getMessage(DataTableTopComponent.class, "DataTableTopComponent.RichToolTip.title.text"), acm.getDescription()));
        }

        final ArrayList<AttributeColumn> availableColumns = new ArrayList<AttributeColumn>();
        for (final AttributeColumn column : columns) {
            if (acm.canManipulateColumn(table, column)) {
                availableColumns.add(column);
            }
        }

        if (!availableColumns.isEmpty()) {
            manipulatorButton.setPopupCallback(new PopupPanelCallback() {

                public JPopupPanel getPopupPanel(JCommandButton jcb) {
                    JCommandPopupMenu popup = new JCommandPopupMenu();

                    JCommandMenuButton button;
                    for (final AttributeColumn column : availableColumns) {

                        button = new JCommandMenuButton(column.getTitle(), ImageWrapperResizableIcon.getIcon(ImageUtilities.loadImage("org/gephi/desktop/datalab/resources/column.png"), new Dimension(16, 16)));
                        button.addActionListener(new ActionListener() {

                            public void actionPerformed(ActionEvent e) {
                                new DataLaboratoryHelper().executeAttributeColumnsManipulator(acm, table, column);
                            }
                        });
                        popup.addMenuButton(button);
                    }
                    return popup;
                }
            });
        } else {
            manipulatorButton.setEnabled(false);
        }

        return manipulatorButton;
    }

    /**
     * Create the special Add new column button.
     */
    private void prepareAddColumnButton() {
        JCommandButtonStrip strip = new JCommandButtonStrip(JCommandButtonStrip.StripOrientation.HORIZONTAL);
        strip.setDisplayState(CommandButtonDisplayState.BIG);
        JCommandButton button = new JCommandButton(NbBundle.getMessage(DataTableTopComponent.class, "DataTableTopComponent.addColumnButton.text"), ImageWrapperResizableIcon.getIcon(ImageUtilities.loadImage("org/gephi/desktop/datalab/resources/table-insert-column.png", true), new Dimension(16, 16)));
        button.setCommandButtonKind(JCommandButton.CommandButtonKind.ACTION_ONLY);
        button.setDisplayState(CommandButtonDisplayState.BIG);
        if (classDisplayed == ClassDisplayed.NODE) {
            button.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    showAddColumnUI(AddColumnUI.Mode.NODES_TABLE);
                }
            });
        } else {
            button.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    showAddColumnUI(AddColumnUI.Mode.EDGES_TABLE);
                }
            });
        }
        strip.add(button);
        columnManipulatorsPanel.add(strip);
    }

    /**
     * Create the special merge columns button.
     */
    private void prepareMergeColumnsButton() {
        JCommandButtonStrip strip = new JCommandButtonStrip(JCommandButtonStrip.StripOrientation.HORIZONTAL);
        strip.setDisplayState(CommandButtonDisplayState.BIG);
        JCommandButton button = new JCommandButton(NbBundle.getMessage(DataTableTopComponent.class, "DataTableTopComponent.mergeColumnsButton.text"), ImageWrapperResizableIcon.getIcon(ImageUtilities.loadImage("org/gephi/desktop/datalab/resources/merge.png", true), new Dimension(16, 16)));
        button.setCommandButtonKind(JCommandButton.CommandButtonKind.ACTION_ONLY);
        button.setDisplayState(CommandButtonDisplayState.BIG);
        if (classDisplayed == ClassDisplayed.NODE) {
            button.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    showMergeColumnsUI(MergeColumnsUI.Mode.NODES_TABLE);
                }
            });
        } else {
            button.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    showMergeColumnsUI(MergeColumnsUI.Mode.EDGES_TABLE);
                }
            });
        }
        strip.add(button);
        columnManipulatorsPanel.add(strip);
    }

    private void showAddColumnUI(AddColumnUI.Mode mode) {
        JButton okButton = new JButton(NbBundle.getMessage(DataTableTopComponent.class, "DataTableTopComponent.dialogs.okButton.text"));
        AddColumnUI addColumnUI = new AddColumnUI();
        addColumnUI.setup(mode);
        addColumnUI.setOkButton(okButton);
        DialogDescriptor dd = new DialogDescriptor(AddColumnUI.createValidationPanel(addColumnUI), addColumnUI.getDisplayName());
        dd.setOptions(new Object[]{okButton, DialogDescriptor.CANCEL_OPTION});
        if (DialogDisplayer.getDefault().notify(dd).equals(okButton)) {
            addColumnUI.execute();
        }
        addColumnUI.unSetup();
    }

    private void showMergeColumnsUI(MergeColumnsUI.Mode mode) {
        JButton okButton = new JButton(NbBundle.getMessage(DataTableTopComponent.class, "DataTableTopComponent.dialogs.okButton.text"));
        MergeColumnsUI mergeColumnsUI = new MergeColumnsUI();
        mergeColumnsUI.setup(mode);
        mergeColumnsUI.setOkButton(okButton);
        DialogDescriptor dd = new DialogDescriptor(MergeColumnsUI.createValidationPanel(mergeColumnsUI), mergeColumnsUI.getDisplayName());
        dd.setOptions(new Object[]{okButton, DialogDescriptor.CANCEL_OPTION});
        if (DialogDisplayer.getDefault().notify(dd).equals(okButton)) {
            mergeColumnsUI.execute();
        }
    }

    /**************General actions manipulators related methods:***************/
    private void refreshGeneralActionsButtons() {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                clearGeneralActionsButtons();
                prepareGeneralActionsButtons();
            }
        });
    }

    private void clearGeneralActionsButtons() {
        for (JComponent b : generalActionsButtons) {
            controlToolbar.remove(b);
        }
        generalActionsButtons.clear();
        controlToolbar.updateUI();
    }

    /**
     * Adds the buttons for the GeneralActionsManipulators.
     */
    public void prepareGeneralActionsButtons() {
        //Figure out the index to place the buttons, in order to put them between separator 2 and the boxGlue.
        int index = controlToolbar.getComponentIndex(boxGlue);

        final DataLaboratoryHelper dlh = new DataLaboratoryHelper();
        JButton button;
        for (final GeneralActionsManipulator m : dlh.getGeneralActionsManipulators()) {
            button = new JButton(m.getName(), m.getIcon());
            if (m.getDescription() != null && !m.getDescription().isEmpty()) {
                button.setToolTipText(m.getDescription());
            }
            if (m.canExecute()) {
                button.addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent e) {
                        dlh.executeManipulator(m);
                    }
                });
            } else {
                button.setEnabled(false);
            }
            controlToolbar.add(button, index);
            index++;
            generalActionsButtons.add(button);
        }

        //Add plugin general actions as a drop down list:
        final PluginGeneralActionsManipulator[] plugins = dlh.getPluginGeneralActionsManipulators();
        if (plugins != null && plugins.length > 0) {
            JCommandButton pluginsButton = new JCommandButton(NbBundle.getMessage(DataTableTopComponent.class, "DataTableTopComponent.general.actions.plugins.button.text"), ImageWrapperResizableIcon.getIcon(ImageUtilities.loadImage("org/gephi/desktop/datalab/resources/puzzle--arrow.png", true), new Dimension(16, 16)));
            pluginsButton.setDisplayState(CommandButtonDisplayState.MEDIUM);
            pluginsButton.setCommandButtonKind(JCommandButton.CommandButtonKind.POPUP_ONLY);
            pluginsButton.setPopupCallback(new PopupPanelCallback() {

                public JPopupPanel getPopupPanel(JCommandButton jcb) {
                    JCommandButtonPanel pluginsPanel = new JCommandButtonPanel(CommandButtonDisplayState.BIG);
                    Integer lastManipulatorType = null;
                    int group = 1;
                    pluginsPanel.addButtonGroup(NbBundle.getMessage(DataTableTopComponent.class, "DataTableTopComponent.general.actions.plugins.group.name", group));
                    for (final PluginGeneralActionsManipulator m : plugins) {
                        if (lastManipulatorType == null) {
                            lastManipulatorType = m.getType();
                        }
                        if (lastManipulatorType != m.getType()) {
                            group++;
                            pluginsPanel.addButtonGroup(NbBundle.getMessage(DataTableTopComponent.class, "DataTableTopComponent.general.actions.plugins.group.name", group));
                        }
                        lastManipulatorType = m.getType();
                        pluginsPanel.addButtonToLastGroup(preparePluginGeneralActionsButton(m));
                    }
                    JCommandPopupMenu popup = new JCommandPopupMenu(pluginsPanel, 8, 8);
                    return popup;
                }
            });
            controlToolbar.add(pluginsButton, index);
            generalActionsButtons.add(pluginsButton);
        }
        controlToolbar.updateUI();
    }

    /**
     * Prepare a button for the popup panel for plugin general actions.
     * @param m PluginGeneralActionsManipulator for the button
     * @return JCommandButton for the manipulator
     */
    private JCommandButton preparePluginGeneralActionsButton(final PluginGeneralActionsManipulator m) {
        JCommandButton button = new JCommandButton(m.getName(), m.getIcon() != null ? ImageWrapperResizableIcon.getIcon(ImageUtilities.icon2Image(m.getIcon()), new Dimension(16, 16)) : null);//Convert icon to Image if it is not null
        button.setDisplayState(CommandButtonDisplayState.BIG);
        button.setCommandButtonKind(JCommandButton.CommandButtonKind.ACTION_ONLY);
        if (m.getDescription() != null && !m.getDescription().isEmpty()) {
            button.setToolTipText(m.getDescription());
        }
        if (m.canExecute()) {
            button.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    new DataLaboratoryHelper().executeManipulator(m);
                }
            });
        } else {
            button.setEnabled(false);
        }
        return button;
    }

    /**
     * This thread is used for processing graphChanged and attributesChanged events.
     * It takes care to only refresh the UI once (the last one) when a lot of events come in a short period of time.
     */
    class RefreshOnceHelperThread extends Thread {

        private static final int CHECK_TIME_INTERVAL = 100;//100 ms.
        private volatile boolean moreEvents = false;
        private boolean refreshTableOnly;

        public RefreshOnceHelperThread() {
            refreshTableOnly = false;
        }

        public RefreshOnceHelperThread(boolean refreshTableOnly) {
            this.refreshTableOnly = refreshTableOnly;
        }

        @Override
        public void run() {
            try {
                do {
                    moreEvents = false;
                    Thread.sleep(CHECK_TIME_INTERVAL);
                } while (moreEvents);
                if (refreshTableOnly) {
                    DataTableTopComponent.this.refreshTable();
                } else {
                    DataTableTopComponent.this.refreshAll();
                }
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        public void eventAttended() {
            this.moreEvents = true;
        }
    }

    /**
     * To react to Ctrl+F keys combination calling Search/Replace general action:
     * @param event
     */
    public void eventDispatched(AWTEvent event) {
        KeyEvent evt = (KeyEvent) event;

        if (evt.getID() == KeyEvent.KEY_RELEASED && (evt.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) != 0 && evt.getKeyCode() == KeyEvent.VK_F) {
            DataLaboratoryHelper dlh = new DataLaboratoryHelper();
            dlh.executeManipulator(dlh.getSearchReplaceManipulator());
            evt.consume();
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        elementGroup = new javax.swing.ButtonGroup();
        controlToolbar = new javax.swing.JToolBar();
        nodesButton = new javax.swing.JToggleButton();
        edgesButton = new javax.swing.JToggleButton();
        separator = new javax.swing.JToolBar.Separator();
        configurationButton = new javax.swing.JButton();
        separator2 = new javax.swing.JToolBar.Separator();
        boxGlue = new javax.swing.JLabel();
        labelFilter = new org.jdesktop.swingx.JXLabel();
        filterTextField = new javax.swing.JTextField();
        columnComboBox = new javax.swing.JComboBox();
        tableScrollPane = new javax.swing.JScrollPane();
        bannerPanel = new javax.swing.JPanel();
        labelBanner = new javax.swing.JLabel();
        refreshButton = new javax.swing.JButton();
        attributeColumnsScrollPane = new javax.swing.JScrollPane();
        columnManipulatorsPanel = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());

        controlToolbar.setFloatable(false);
        controlToolbar.setRollover(true);

        elementGroup.add(nodesButton);
        org.openide.awt.Mnemonics.setLocalizedText(nodesButton, org.openide.util.NbBundle.getMessage(DataTableTopComponent.class, "DataTableTopComponent.nodesButton.text")); // NOI18N
        nodesButton.setFocusable(false);
        nodesButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        nodesButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        nodesButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nodesButtonActionPerformed(evt);
            }
        });
        controlToolbar.add(nodesButton);

        elementGroup.add(edgesButton);
        org.openide.awt.Mnemonics.setLocalizedText(edgesButton, org.openide.util.NbBundle.getMessage(DataTableTopComponent.class, "DataTableTopComponent.edgesButton.text")); // NOI18N
        edgesButton.setFocusable(false);
        edgesButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        edgesButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        edgesButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                edgesButtonActionPerformed(evt);
            }
        });
        controlToolbar.add(edgesButton);
        controlToolbar.add(separator);

        org.openide.awt.Mnemonics.setLocalizedText(configurationButton, org.openide.util.NbBundle.getMessage(DataTableTopComponent.class, "DataTableTopComponent.configurationButton.text")); // NOI18N
        configurationButton.setFocusable(false);
        configurationButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        configurationButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        configurationButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                configurationButtonActionPerformed(evt);
            }
        });
        controlToolbar.add(configurationButton);
        controlToolbar.add(separator2);

        org.openide.awt.Mnemonics.setLocalizedText(boxGlue, org.openide.util.NbBundle.getMessage(DataTableTopComponent.class, "DataTableTopComponent.boxGlue.text")); // NOI18N
        boxGlue.setMaximumSize(new java.awt.Dimension(32767, 32767));
        controlToolbar.add(boxGlue);

        org.openide.awt.Mnemonics.setLocalizedText(labelFilter, org.openide.util.NbBundle.getMessage(DataTableTopComponent.class, "DataTableTopComponent.labelFilter.text")); // NOI18N
        controlToolbar.add(labelFilter);

        filterTextField.setText(org.openide.util.NbBundle.getMessage(DataTableTopComponent.class, "DataTableTopComponent.filterTextField.text")); // NOI18N
        filterTextField.setToolTipText(org.openide.util.NbBundle.getMessage(DataTableTopComponent.class, "DataTableTopComponent.filterTextField.toolTipText")); // NOI18N
        filterTextField.setMaximumSize(new java.awt.Dimension(1000, 30));
        filterTextField.setPreferredSize(new java.awt.Dimension(150, 20));
        controlToolbar.add(filterTextField);

        columnComboBox.setMaximumSize(new java.awt.Dimension(2000, 20));
        columnComboBox.setPreferredSize(new java.awt.Dimension(120, 20));
        controlToolbar.add(columnComboBox);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(controlToolbar, gridBagConstraints);

        tableScrollPane.setMinimumSize(new java.awt.Dimension(100, 100));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(tableScrollPane, gridBagConstraints);

        bannerPanel.setBackground(new java.awt.Color(178, 223, 240));
        bannerPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.BLACK));
        bannerPanel.setLayout(new java.awt.GridBagLayout());

        labelBanner.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/gephi/desktop/datalab/resources/info.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(labelBanner, org.openide.util.NbBundle.getMessage(DataTableTopComponent.class, "DataTableTopComponent.labelBanner.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 5, 2, 0);
        bannerPanel.add(labelBanner, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(refreshButton, org.openide.util.NbBundle.getMessage(DataTableTopComponent.class, "DataTableTopComponent.refreshButton.text")); // NOI18N
        refreshButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(1, 0, 1, 1);
        bannerPanel.add(refreshButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 1.0;
        add(bannerPanel, gridBagConstraints);

        attributeColumnsScrollPane.setMinimumSize(new java.awt.Dimension(200, 130));
        attributeColumnsScrollPane.setPreferredSize(new java.awt.Dimension(200, 130));

        columnManipulatorsPanel.setMinimumSize(new java.awt.Dimension(200, 100));
        columnManipulatorsPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 25, 20));
        attributeColumnsScrollPane.setViewportView(columnManipulatorsPanel);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        add(attributeColumnsScrollPane, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void refreshButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshButtonActionPerformed
        refreshCurrentTable();
}//GEN-LAST:event_refreshButtonActionPerformed

    private void edgesButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_edgesButtonActionPerformed
        selectEdgesTable();
}//GEN-LAST:event_edgesButtonActionPerformed

    private void nodesButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nodesButtonActionPerformed
        selectNodesTable();
}//GEN-LAST:event_nodesButtonActionPerformed

    private void configurationButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_configurationButtonActionPerformed
        DialogDescriptor dd = new DialogDescriptor(new ConfigurationPanel(this), NbBundle.getMessage(DataTableTopComponent.class, "ConfigurationPanel.title"));
        dd.setOptions(new Object[]{DialogDescriptor.OK_OPTION});
        DialogDisplayer.getDefault().notify(dd);

        //Save preferences:
        NbPreferences.forModule(DataTableTopComponent.class).putBoolean(DATA_LABORATORY_ONLY_VISIBLE, visibleOnly);
        NbPreferences.forModule(DataTableTopComponent.class).putBoolean(DATA_LABORATORY_SPARKLINES, useSparklines);
        NbPreferences.forModule(DataTableTopComponent.class).putBoolean(DATA_LABORATORY_TIME_INTERVAL_GRAPHICS, timeIntervalGraphics);
        NbPreferences.forModule(DataTableTopComponent.class).putBoolean(DATA_LABORATORY_EDGES_NODES_LABELS, showEdgesNodesLabels);
    }//GEN-LAST:event_configurationButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane attributeColumnsScrollPane;
    private javax.swing.JPanel bannerPanel;
    private javax.swing.JLabel boxGlue;
    private javax.swing.JComboBox columnComboBox;
    private javax.swing.JPanel columnManipulatorsPanel;
    private javax.swing.JButton configurationButton;
    private javax.swing.JToolBar controlToolbar;
    private javax.swing.JToggleButton edgesButton;
    private javax.swing.ButtonGroup elementGroup;
    private javax.swing.JTextField filterTextField;
    private javax.swing.JLabel labelBanner;
    private org.jdesktop.swingx.JXLabel labelFilter;
    private javax.swing.JToggleButton nodesButton;
    private javax.swing.JButton refreshButton;
    private javax.swing.JToolBar.Separator separator;
    private javax.swing.JToolBar.Separator separator2;
    private javax.swing.JScrollPane tableScrollPane;
    // End of variables declaration//GEN-END:variables

    /**
     * Gets default instance. Do not use directly: reserved for *.settings files only,
     * i.e. deserialization routines; otherwise you could get a non-deserialized instance.
     * To obtain the singleton instance, use {@link #findInstance}.
     */
    public static synchronized DataTableTopComponent getDefault() {
        if (instance == null) {
            instance = new DataTableTopComponent();
        }
        return instance;
    }

    /**
     * Obtain the DataTableTopComponent instance. Never call {@link #getDefault} directly!
     */
    public static synchronized DataTableTopComponent findInstance() {
        TopComponent win = WindowManager.getDefault().findTopComponent(PREFERRED_ID);
        if (win == null) {
            Logger.getLogger(DataTableTopComponent.class.getName()).warning(
                    "Cannot find " + PREFERRED_ID + " component. It will not be located properly in the window system.");
            return getDefault();
        }
        if (win instanceof DataTableTopComponent) {
            return (DataTableTopComponent) win;
        }
        Logger.getLogger(DataTableTopComponent.class.getName()).warning(
                "There seem to be multiple components with the '" + PREFERRED_ID
                + "' ID. That is a potential source of errors and unexpected behavior.");
        return getDefault();
    }

    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_ALWAYS;
    }

    @Override
    public void componentOpened() {
        // TODO add custom code on component opening
    }

    @Override
    public void componentClosed() {
        // TODO add custom code on component closing
    }

    @Override
    protected void componentActivated() {
        super.componentActivated();
        java.awt.Toolkit.getDefaultToolkit().addAWTEventListener(this, AWTEvent.KEY_EVENT_MASK);
    }

    @Override
    protected void componentDeactivated() {
        super.componentDeactivated();
        java.awt.Toolkit.getDefaultToolkit().removeAWTEventListener(this);
    }

    /** replaces this in object stream */
    @Override
    public Object writeReplace() {
        return new ResolvableHelper();
    }

    @Override
    protected String preferredID() {
        return PREFERRED_ID;
    }

    final static class ResolvableHelper implements Serializable {

        private static final long serialVersionUID = 1L;

        public Object readResolve() {
            return DataTableTopComponent.getDefault();
        }
    }

    /**
     * <p>Exports a JTable to a CSV file showing first a dialog to select the file to write.</p>
     * @param parent Parent window
     * @param table Table to export
     * @param separator Separator to use for separating values of a row in the CSV file. If null ',' will be used.
     * @param charset Charset encoding for the file
     * @param columnsToExport Indicates the indexes of the columns to export. All columns will be exported if null
     */
    public static void exportTableAsCSV(JComponent parent, JTable table, Character separator, Charset charset, Integer[] columnsToExport) {
        String lastPath = NbPreferences.forModule(TableCSVExporter.class).get(LAST_PATH, null);
        final JFileChooser chooser = new JFileChooser(lastPath);
        chooser.setAcceptAllFileFilterUsed(false);
        DialogFileFilter dialogFileFilter = new DialogFileFilter(NbBundle.getMessage(TableCSVExporter.class, "TableCSVExporter.filechooser.csvDescription"));
        dialogFileFilter.addExtension("csv");
        chooser.addChoosableFileFilter(dialogFileFilter);
        File selectedFile = new File(chooser.getCurrentDirectory(), "table.csv");
        chooser.setSelectedFile(selectedFile);
        int returnFile = chooser.showSaveDialog(null);
        if (returnFile != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File file = chooser.getSelectedFile();

        if (!file.getPath().endsWith(".csv")) {
            file = new File(file.getPath() + ".csv");
        }

        //Save last path
        String defaultDirectory = file.getParentFile().getAbsolutePath();
        NbPreferences.forModule(TableCSVExporter.class).put(LAST_PATH, defaultDirectory);
        try {
            TableCSVExporter.writeCSVFile(table, file, separator, charset, columnsToExport);
            JOptionPane.showMessageDialog(parent, NbBundle.getMessage(TableCSVExporter.class, "TableCSVExporter.dialog.success"));
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(parent, NbBundle.getMessage(TableCSVExporter.class, "TableCSVExporter.dialog.error"), NbBundle.getMessage(TableCSVExporter.class, "TableCSVExporter.dialog.error.title"), JOptionPane.ERROR_MESSAGE);
        }
    }
    private static final String LAST_PATH = "TableCSVExporter_Save_Last_Path";
}
