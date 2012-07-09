/*
 Copyright 2008-2010 Gephi
 Authors : Mathieu Bastian <mathieu.bastian@gephi.org>, Mathieu Jacomy, Julian Bilcke, Eduardo Ramos
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
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.gephi.data.attributes.api.AttributeEvent.EventType;
import org.gephi.data.attributes.api.*;
import org.gephi.datalab.api.DataLaboratoryHelper;
import org.gephi.datalab.api.datatables.DataTablesController;
import org.gephi.datalab.api.datatables.DataTablesEventListener;
import org.gephi.datalab.spi.ContextMenuItemManipulator;
import org.gephi.datalab.spi.columns.AttributeColumnsManipulator;
import org.gephi.datalab.spi.edges.EdgesManipulator;
import org.gephi.datalab.spi.general.GeneralActionsManipulator;
import org.gephi.datalab.spi.general.PluginGeneralActionsManipulator;
import org.gephi.datalab.spi.nodes.NodesManipulator;
import org.gephi.desktop.datalab.general.actions.AddColumnUI;
import org.gephi.desktop.datalab.general.actions.CSVExportUI;
import org.gephi.desktop.datalab.general.actions.MergeColumnsUI;
import org.gephi.graph.api.*;
import org.gephi.project.api.*;
import org.gephi.ui.components.WrapLayout;
import org.gephi.ui.utils.BusyUtils;
import org.gephi.ui.utils.DialogFileFilter;
import org.gephi.ui.utils.UIUtils;
import org.gephi.utils.TableCSVExporter;
import org.netbeans.api.settings.ConvertAsProperties;
import org.netbeans.swing.etable.ETableColumnModel;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.util.*;
import org.openide.windows.TopComponent;
import org.pushingpixels.flamingo.api.common.*;
import org.pushingpixels.flamingo.api.common.icon.ImageWrapperResizableIcon;
import org.pushingpixels.flamingo.api.common.popup.JCommandPopupMenu;
import org.pushingpixels.flamingo.api.common.popup.JPopupPanel;
import org.pushingpixels.flamingo.api.common.popup.PopupPanelCallback;

/**
 *
 * @author Mathieu Bastian
 */
@ConvertAsProperties(dtd = "-//org.gephi.desktop.datalab//DataTable//EN",
autostore = false)
@TopComponent.Description(preferredID = "DataTableTopComponent",
iconBase = "org/gephi/desktop/datalab/resources/small.png",
persistenceType = TopComponent.PERSISTENCE_ALWAYS)
@TopComponent.Registration(mode = "editor", openAtStartup = true, roles = {"datalab"})
@ActionID(category = "Window", id = "org.gephi.desktop.datalab.DataTableTopComponent")
@ActionReference(path = "Menu/Window", position = 300)
@TopComponent.OpenActionRegistration(displayName = "#CTL_DataTableTopComponent",
preferredID = "DataTableTopComponent")
public class DataTableTopComponent extends TopComponent implements AWTEventListener, DataTablesEventListener, AttributeListener, GraphListener {

    private enum ClassDisplayed {

        NONE, NODE, EDGE
    };
    //Settings
    private static final String DATA_LABORATORY_DYNAMIC_FILTERING = "DataLaboratory_Dynamic_Filtering";
    private static final String DATA_LABORATORY_ONLY_VISIBLE = "DataLaboratory_visibleOnly";
    private static final String DATA_LABORATORY_SPARKLINES = "DataLaboratory_useSparklines";
    private static final String DATA_LABORATORY_TIME_INTERVAL_GRAPHICS = "DataLaboratory_timeIntervalGraphics";
    private static final String DATA_LABORATORY_EDGES_NODES_LABELS = "DataLaboratory_showEdgesNodesLabels";
    private static final Color invalidFilterColor = new Color(254, 150, 150);
    private final boolean dynamicFiltering;
    private boolean visibleOnly = true;
    private boolean useSparklines = false;
    private boolean timeIntervalGraphics = false;
    private boolean showEdgesNodesLabels = false;
    private Map<Integer, ContextMenuItemManipulator> nodesActionMappings = new HashMap<Integer, ContextMenuItemManipulator>();//For key bindings
    private Map<Integer, ContextMenuItemManipulator> edgesActionMappings = new HashMap<Integer, ContextMenuItemManipulator>();//For key bindings
    //Data
    private GraphModel graphModel;
    private DataTablesModel dataTablesModel;
    private AvailableColumnsModel nodeAvailableColumnsModel;
    private AvailableColumnsModel edgeAvailableColumnsModel;
    //Table
    private NodeDataTable nodeTable;
    private EdgeDataTable edgeTable;
    //General actions buttons
    private ArrayList<JComponent> generalActionsButtons = new ArrayList<JComponent>();
    //States
    private ClassDisplayed classDisplayed = ClassDisplayed.NODE;//Display nodes by default at first.
    private ArrayList previousNodeFilterColumns = new ArrayList();
    private int previousNodeColumnsFilterIndex = 0;
    private ArrayList previousEdgeFilterColumns = new ArrayList();
    private int previousEdgeColumnsFilterIndex = 0;
    //Executor
    private RefreshOnceHelperThread refreshOnceHelperThread;

    public DataTableTopComponent() {

        //Get saved preferences if existing:
        dynamicFiltering = NbPreferences.forModule(DataTableTopComponent.class).getBoolean(DATA_LABORATORY_DYNAMIC_FILTERING, true);
        visibleOnly = NbPreferences.forModule(DataTableTopComponent.class).getBoolean(DATA_LABORATORY_ONLY_VISIBLE, true);
        useSparklines = NbPreferences.forModule(DataTableTopComponent.class).getBoolean(DATA_LABORATORY_SPARKLINES, false);
        timeIntervalGraphics = NbPreferences.forModule(DataTableTopComponent.class).getBoolean(DATA_LABORATORY_TIME_INTERVAL_GRAPHICS, false);
        showEdgesNodesLabels = NbPreferences.forModule(DataTableTopComponent.class).getBoolean(DATA_LABORATORY_EDGES_NODES_LABELS, false);

        initComponents();
        if (UIUtils.isAquaLookAndFeel()) {
            columnManipulatorsPanel.setBackground(UIManager.getColor("NbExplorerView.background"));
        }

        columnManipulatorsPanel.setLayout(new WrapLayout(WrapLayout.CENTER, 25, 20));
        setName(NbBundle.getMessage(DataTableTopComponent.class, "CTL_DataTableTopComponent"));

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
        Workspace workspace = pc.getCurrentWorkspace();
        if (workspace == null) {
            clearAll();
        } else {
            AttributeModel attributeModel = Lookup.getDefault().lookup(AttributeController.class).getModel();

            dataTablesModel = workspace.getLookup().lookup(DataTablesModel.class);
            if (dataTablesModel == null) {
                workspace.add(dataTablesModel = new DataTablesModel(attributeModel.getNodeTable(), attributeModel.getEdgeTable()));
            }
            nodeAvailableColumnsModel = dataTablesModel.getNodeAvailableColumnsModel();
            edgeAvailableColumnsModel = dataTablesModel.getEdgeAvailableColumnsModel();
            refreshAllOnce();
        }
        bannerPanel.setVisible(false);
    }

    private void initEvents() {

        //Workspace Listener
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        final GraphController gc = Lookup.getDefault().lookup(GraphController.class);
        pc.addWorkspaceListener(new WorkspaceListener() {

            public void initialize(Workspace workspace) {
                //Prepare DataTablesEvent listener
                Lookup.getDefault().lookup(DataTablesController.class).setDataTablesEventListener(DataTableTopComponent.this);
            }

            public void select(Workspace workspace) {
                //Prepare DataTablesEvent listener
                Lookup.getDefault().lookup(DataTablesController.class).setDataTablesEventListener(DataTableTopComponent.this);
                AttributeModel attributeModel = Lookup.getDefault().lookup(AttributeController.class).getModel();

                dataTablesModel = workspace.getLookup().lookup(DataTablesModel.class);
                if (dataTablesModel == null) {
                    workspace.add(dataTablesModel = new DataTablesModel(attributeModel.getNodeTable(), attributeModel.getEdgeTable()));
                }
                nodeAvailableColumnsModel = dataTablesModel.getNodeAvailableColumnsModel();
                edgeAvailableColumnsModel = dataTablesModel.getEdgeAvailableColumnsModel();
                hideTable();
                enableTableControls();

                attributeModel.addAttributeListener(DataTableTopComponent.this);

                graphModel = gc.getModel();
                graphModel.addGraphListener(DataTableTopComponent.this);

                refreshAllOnce();
            }

            public void unselect(Workspace workspace) {
                graphModel.removeGraphListener(DataTableTopComponent.this);

                AttributeModel attributeModel = workspace.getLookup().lookup(AttributeModel.class);
                attributeModel.removeAttributeListener(DataTableTopComponent.this);
                graphModel = null;
                dataTablesModel = null;
                nodeAvailableColumnsModel = null;
                edgeAvailableColumnsModel = null;
                clearAll();
            }

            public void close(Workspace workspace) {
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
        columnComboBox.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                refreshFilter();
            }
        });
        initKeyEventContextMenuActionMappings();
    }

    private void initKeyEventContextMenuActionMappings() {
        mapItems(DataLaboratoryHelper.getDefault().getNodesManipulators(), nodesActionMappings);
        mapItems(DataLaboratoryHelper.getDefault().getEdgesManipulators(), edgesActionMappings);
    }

    private void mapItems(ContextMenuItemManipulator[] items, Map<Integer, ContextMenuItemManipulator> map) {
        Integer key;
        ContextMenuItemManipulator[] subItems;
        for (ContextMenuItemManipulator item : items) {
            key = item.getMnemonicKey();
            if (key != null) {
                if (!map.containsKey(key)) {
                    map.put(key, item);
                }
            }
            subItems = item.getSubItems();
            if (subItems != null) {
                mapItems(subItems, map);
            }
        }
    }

    private synchronized void refreshAll() {
        if (Lookup.getDefault().lookup(ProjectController.class).getCurrentWorkspace() != null) {//Some workspace is selected
            refreshTable();
            refreshColumnManipulators();
            refreshGeneralActionsButtons();
        }
    }

    private void clearAll() {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                clearTableControls();
                clearColumnManipulators();
                clearGeneralActionsButtons();
            }
        });
    }

    private AvailableColumnsModel getTableAvailableColumnsModel(AttributeTable table) {
        if (Lookup.getDefault().lookup(AttributeController.class).getModel().getNodeTable() == table) {
            return nodeAvailableColumnsModel;
        } else if (Lookup.getDefault().lookup(AttributeController.class).getModel().getEdgeTable() == table) {
            return edgeAvailableColumnsModel;
        } else {
            return null;//Graph table or other table, not supported in data laboratory for now.
        }
    }

    public void attributesChanged(final AttributeEvent event) {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                AttributeTable table = event.getSource();
                AvailableColumnsModel tableAvailableColumnsModel = getTableAvailableColumnsModel(table);
                if (tableAvailableColumnsModel != null) {
                    switch (event.getEventType()) {
                        case ADD_COLUMN:
                            for (AttributeColumn c : event.getData().getAddedColumns()) {
                                if (!tableAvailableColumnsModel.addAvailableColumn(c)) {//Add as available by default. Will only be added if the max number of available columns is not surpassed
                                    availableColumnsButton.setIcon(ImageUtilities.loadImageIcon("org/gephi/desktop/datalab/resources/light-bulb--plus.png", true));
                                    break;
                                }
                            }
                            break;
                        case REMOVE_COLUMN:
                            for (AttributeColumn c : event.getData().getAddedColumns()) {
                                tableAvailableColumnsModel.removeAvailableColumn(c);
                            }
                            break;
                        case REPLACE_COLUMN:
                            for (AttributeColumn c : event.getData().getRemovedColumns()) {
                                tableAvailableColumnsModel.removeAvailableColumn(c);
                                tableAvailableColumnsModel.addAvailableColumn(table.getColumn(c.getId()));
                            }
                            break;
                    }
                }
                if (isOpened()) {
                    refreshOnce(event.is(EventType.SET_VALUE));
                }
            }
        });
    }

    public void graphChanged(GraphEvent event) {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                if (isOpened()) {
                    refreshOnce(false);
                }
            }
        });
    }

    /**
     * This method ensures that the refreshing of all Data laboratory or table
     * only happens once in a short time period.
     *
     * @param refreshTableOnly True to refresh only table values, false to
     * refresh all UI including manipulators
     */
    private void refreshOnce(boolean refreshTableOnly) {
        if (refreshOnceHelperThread == null || !refreshOnceHelperThread.isAlive() || (refreshOnceHelperThread.refreshTableOnly && !refreshTableOnly)) {
            refreshOnceHelperThread = new RefreshOnceHelperThread(refreshTableOnly);
            refreshOnceHelperThread.start();
        } else {
            refreshOnceHelperThread.eventAttended();
        }
    }

    private void refreshAllOnce() {
        refreshOnce(false);
    }

    /**
     * **************Table related methods:****************
     */
    private void refreshFilter() {
        int index = columnComboBox.getSelectedIndex();
        if (index < 0) {
            return;
        }
        if (classDisplayed.equals(ClassDisplayed.NODE)) {
            if (nodeTable.setFilter(filterTextField.getText(), index)) {
                filterTextField.setBackground(Color.WHITE);
            } else {
                filterTextField.setBackground(invalidFilterColor);
            }
            previousNodeColumnsFilterIndex = index;
        } else if (classDisplayed.equals(ClassDisplayed.EDGE)) {
            if (edgeTable.setPattern(filterTextField.getText(), index)) {
                filterTextField.setBackground(Color.WHITE);
            } else {
                filterTextField.setBackground(invalidFilterColor);
            }
            previousEdgeColumnsFilterIndex = index;
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
                    final AttributeColumn[] cols = nodeAvailableColumnsModel.getAvailableColumns();

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
                    Exceptions.printStackTrace(e);
                    JLabel errorLabel = new JLabel(NbBundle.getMessage(DataTableTopComponent.class, "DataTableTopComponent.tableScrollPane.error"), SwingConstants.CENTER);
                    tableScrollPane.setViewportView(errorLabel);
                }
            }
        };
        SwingUtilities.invokeLater(initNodesRunnable);
    }

    private void initEdgesView() {
        Runnable initEdgesRunnable = new Runnable() {

            public void run() {
                try {
                    String busyMsg = NbBundle.getMessage(DataTableTopComponent.class, "DataTableTopComponent.tableScrollPane.busyMessage");
                    BusyUtils.BusyLabel busylabel = BusyUtils.createCenteredBusyLabel(tableScrollPane, busyMsg, edgeTable.getTable());
                    busylabel.setBusy(true);

                    //Attributes columns
                    final AttributeColumn[] cols = edgeAvailableColumnsModel.getAvailableColumns();

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
                    Exceptions.printStackTrace(e);
                    JLabel errorLabel = new JLabel(NbBundle.getMessage(DataTableTopComponent.class, "DataTableTopComponent.tableScrollPane.error"), SwingConstants.CENTER);
                    tableScrollPane.setViewportView(errorLabel);
                }
            }
        };
        SwingUtilities.invokeLater(initEdgesRunnable);
    }

    private void refreshFilterColumns() {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                ArrayList columns = new ArrayList();
                if (classDisplayed.equals(ClassDisplayed.NODE)) {
                    ETableColumnModel columnModel = (ETableColumnModel) nodeTable.getOutlineTable().getColumnModel();
                    DefaultComboBoxModel model = new DefaultComboBoxModel();
                    for (int i = 0; i < columnModel.getColumnCount(); i++) {
                        if (!columnModel.isColumnHidden(columnModel.getColumn(i))) {
                            model.addElement(columnModel.getColumn(i).getHeaderValue());
                            columns.add(columnModel.getColumn(i).getHeaderValue());
                        }
                    }

                    columnComboBox.setModel(model);
                    if (columns.equals(previousNodeFilterColumns) && previousNodeColumnsFilterIndex < columnComboBox.getItemCount()) {//Preserve user selected column when the columns list does not change
                        columnComboBox.setSelectedIndex(previousNodeColumnsFilterIndex);
                    } else {
                        previousNodeColumnsFilterIndex = 0;
                    }
                    previousNodeFilterColumns = columns;
                } else if (classDisplayed.equals(ClassDisplayed.EDGE)) {
                    DefaultComboBoxModel model = new DefaultComboBoxModel();
                    for (int i = 0; i < edgeTable.getTable().getColumnCount(); i++) {
                        if (edgeTable.getTable().getColumnExt(i).isVisible()) {
                            model.addElement(edgeTable.getTable().getColumnExt(i).getTitle());
                            columns.add(edgeTable.getTable().getColumnExt(i).getTitle());
                        }
                    }
                    columnComboBox.setModel(model);
                    if (columns.equals(previousEdgeFilterColumns) && previousEdgeColumnsFilterIndex < columnComboBox.getItemCount()) {//Preserve user selected column when the columns list does not change
                        columnComboBox.setSelectedIndex(previousEdgeColumnsFilterIndex);
                    } else {
                        previousEdgeColumnsFilterIndex = 0;
                    }
                    previousEdgeFilterColumns = columns;
                }
            }
        });
    }

    private void enableTableControls() {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                nodesButton.setEnabled(true);
                edgesButton.setEnabled(true);
                configurationButton.setEnabled(true);
                availableColumnsButton.setEnabled(true);
                filterTextField.setEnabled(true);
                columnComboBox.setEnabled(true);
                labelFilter.setEnabled(true);
            }
        });
    }

    private void clearTableControls() {
        elementGroup.clearSelection();
        nodesButton.setEnabled(false);
        edgesButton.setEnabled(false);
        configurationButton.setEnabled(false);
        filterTextField.setEnabled(false);
        filterTextField.setText("");
        columnComboBox.setEnabled(false);
        columnComboBox.removeAllItems();
        previousNodeFilterColumns.clear();
        previousEdgeFilterColumns.clear();
        availableColumnsButton.setEnabled(false);
        availableColumnsButton.setIcon(ImageUtilities.loadImageIcon("org/gephi/desktop/datalab/resources/light-bulb.png", true));
        labelFilter.setEnabled(false);
        bannerPanel.setVisible(false);
        hideTable();
    }

    private void hideTable() {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                tableScrollPane.setViewportView(null);
            }
        });
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
                classDisplayed = ClassDisplayed.NODE;
                refreshAllOnce();
            }
        });
    }

    public void selectEdgesTable() {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                classDisplayed = ClassDisplayed.EDGE;
                refreshAllOnce();
            }
        });
    }

    public void refreshCurrentTable() {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                refreshOnce(true);
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
        String fileName = prepareTableExportFileName();

        if (classDisplayed == classDisplayed.NODE) {
            table = nodeTable.getOutlineTable();
            fileName += " [Nodes]";
        } else {
            table = edgeTable.getTable();
            fileName += " [Edges]";
        }
        fileName += ".csv";

        switch (exportMode) {
            case CSV:
                showCSVExportUI(table, fileName);
                break;
        }
    }

    private String prepareTableExportFileName() {
        String fileName = null;
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        ProjectInformation projectInfo = pc.getCurrentProject().getLookup().lookup(ProjectInformation.class);
        if (projectInfo.hasFile()) {
            fileName = removeFileNameExtension(projectInfo.getFileName());
        }
        WorkspaceProvider wp = pc.getCurrentProject().getLookup().lookup(WorkspaceProvider.class);
        if (wp.getWorkspaces().length > 1 || fileName == null) {
            if (fileName != null) {
                fileName += " - ";
            } else {
                fileName = "";
            }

            WorkspaceInformation workspaceInfo = pc.getCurrentWorkspace().getLookup().lookup(WorkspaceInformation.class);
            if (workspaceInfo.hasSource()) {
                fileName += removeFileNameExtension(workspaceInfo.getSource());
            } else {
                fileName += workspaceInfo.getName();
            }
        }

        return fileName;
    }

    private String removeFileNameExtension(String fileName) {
        int extensionIndex = fileName.lastIndexOf(".");
        if (extensionIndex != -1) {
            fileName = fileName.substring(0, extensionIndex);
        }
        return fileName;
    }

    private void showCSVExportUI(JTable table, String fileName) {
        CSVExportUI csvUI = new CSVExportUI(table);
        DialogDescriptor dd = new DialogDescriptor(csvUI, csvUI.getDisplayName());
        if (DialogDisplayer.getDefault().notify(dd).equals(DialogDescriptor.OK_OPTION)) {
            DataTableTopComponent.exportTableAsCSV(this, table, csvUI.getSelectedSeparator(), csvUI.getSelectedCharset(), csvUI.getSelectedColumnsIndexes(), fileName);
        }
        csvUI.unSetup();
    }

    /**
     * ***********Column manipulators related methods:************
     */
    private void refreshColumnManipulators() {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                clearColumnManipulators();
                prepareAddColumnButton();
                prepareMergeColumnsButton();
                prepareColumnManipulatorsButtons();
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
        AttributeColumn[] columns;
        if (classDisplayed == ClassDisplayed.NODE) {
            table = attributeModel.getNodeTable();
            columns = nodeAvailableColumnsModel.getAvailableColumns();
        } else {
            table = attributeModel.getEdgeTable();
            columns = edgeAvailableColumnsModel.getAvailableColumns();
        }

        DataLaboratoryHelper dlh = DataLaboratoryHelper.getDefault();
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
            currentButtonGroup.add(prepareJCommandButton(table, columns, acm));
        }
        columnManipulatorsPanel.add(currentButtonGroup);
    }

    /**
     * Creates a JCommandButton for the specified columns of a table and
     * AttributeColumnsManipulator
     *
     * @param table table
     * @param columns Columns
     * @param acm AttributeColumnsManipulator
     * @return Prepared JCommandButton
     */
    private JCommandButton prepareJCommandButton(final AttributeTable table, final AttributeColumn[] columns, final AttributeColumnsManipulator acm) {
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
                                DataLaboratoryHelper.getDefault().executeAttributeColumnsManipulator(acm, table, column);
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

    /**
     * ************General actions manipulators related methods:**************
     */
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

        final DataLaboratoryHelper dlh = DataLaboratoryHelper.getDefault();
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
                    JCommandPopupMenu popup = new JCommandPopupMenu(pluginsPanel, 4, 20);
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
     *
     * @param m PluginGeneralActionsManipulator for the button
     * @return JCommandButton for the manipulator
     */
    private JCommandButton preparePluginGeneralActionsButton(final PluginGeneralActionsManipulator m) {
        JCommandButton button = new JCommandButton(m.getName(), m.getIcon() != null ? ImageWrapperResizableIcon.getIcon(ImageUtilities.icon2Image(m.getIcon()), new Dimension(16, 16)) : null);//Convert icon to Image if it is not null
        button.setDisplayState(CommandButtonDisplayState.BIG);
        button.setCommandButtonKind(JCommandButton.CommandButtonKind.ACTION_ONLY);
        if (m.getDescription() != null && !m.getDescription().isEmpty()) {
            button.setPopupRichTooltip(new RichTooltip(NbBundle.getMessage(DataTableTopComponent.class, "DataTableTopComponent.RichToolTip.title.text"), m.getDescription()));
        }
        if (m.canExecute()) {
            button.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    DataLaboratoryHelper.getDefault().executeManipulator(m);
                }
            });
        } else {
            button.setEnabled(false);
        }
        return button;
    }

    /**
     * This thread is used for processing graphChanged and attributesChanged
     * events. It takes care to only refresh the UI once (the last one) when a
     * lot of events come in a short period of time.
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
     * To react to Ctrl+F keys combination calling Search/Replace general action
     * (and nodes/edges context menu mappings)
     *
     * @param event
     */
    public void eventDispatched(AWTEvent event) {
        KeyEvent evt = (KeyEvent) event;

        if (evt.getID() == KeyEvent.KEY_RELEASED && (evt.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) != 0) {
            DataLaboratoryHelper dlh = DataLaboratoryHelper.getDefault();
            if (evt.getKeyCode() == KeyEvent.VK_F) {//Call Search replace with 'F' without general actions key mappings support:
                GeneralActionsManipulator gam = dlh.getGeneralActionsManipulatorByName("SearchReplace");
                if (gam != null) {
                    dlh.executeManipulator(gam);
                }
                evt.consume();
            } else {//Nodes/edges mappings:
                if (classDisplayed == ClassDisplayed.NODE) {
                    final ContextMenuItemManipulator item = nodesActionMappings.get(evt.getKeyCode());
                    if (item != null) {
                        Node[] nodes = nodeTable.getNodesFromSelectedRows();
                        if (nodes.length > 0) {
                            ((NodesManipulator) item).setup(nodes, nodes[0]);
                            if (item.isAvailable() && item.canExecute()) {
                                DataLaboratoryHelper.getDefault().executeManipulator(item);
                            }
                        }
                        evt.consume();
                    }
                } else if (classDisplayed == ClassDisplayed.EDGE) {
                    final ContextMenuItemManipulator item = edgesActionMappings.get(evt.getKeyCode());
                    if (item != null) {
                        Edge[] edges = edgeTable.getEdgesFromSelectedRows();
                        if (edges.length > 0) {
                            ((EdgesManipulator) item).setup(edges, edges[0]);
                            if (item.isAvailable() && item.canExecute()) {
                                DataLaboratoryHelper.getDefault().executeManipulator(item);
                            }
                        }
                        evt.consume();
                    }
                }
            }
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
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
        availableColumnsButton = new javax.swing.JButton();
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

        configurationButton.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        configurationButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/gephi/desktop/datalab/resources/gear-small.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(configurationButton, org.openide.util.NbBundle.getMessage(DataTableTopComponent.class, "DataTableTopComponent.configurationButton.text")); // NOI18N
        configurationButton.setFocusable(false);
        configurationButton.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
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

        availableColumnsButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/gephi/desktop/datalab/resources/light-bulb.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(availableColumnsButton, org.openide.util.NbBundle.getMessage(DataTableTopComponent.class, "DataTableTopComponent.availableColumnsButton.text")); // NOI18N
        availableColumnsButton.setToolTipText(org.openide.util.NbBundle.getMessage(DataTableTopComponent.class, "DataTableTopComponent.availableColumnsButton.toolTipText")); // NOI18N
        availableColumnsButton.setFocusable(false);
        availableColumnsButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        availableColumnsButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        availableColumnsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                availableColumnsButtonActionPerformed(evt);
            }
        });
        controlToolbar.add(availableColumnsButton);

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

        attributeColumnsScrollPane.setMinimumSize(new java.awt.Dimension(200, 100));
        attributeColumnsScrollPane.setPreferredSize(new java.awt.Dimension(200, 100));

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

    private void availableColumnsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_availableColumnsButtonActionPerformed
        AttributeTable table;
        AvailableColumnsModel availableColumnsModel;
        if (classDisplayed == classDisplayed.NODE) {
            table = Lookup.getDefault().lookup(AttributeController.class).getModel().getNodeTable();
        } else {
            table = Lookup.getDefault().lookup(AttributeController.class).getModel().getEdgeTable();
        }
        availableColumnsModel = getTableAvailableColumnsModel(table);
        DialogDescriptor dd = new DialogDescriptor(new AvailableColumnsPanel(table, availableColumnsModel).getValidationPanel(), NbBundle.getMessage(DataTableTopComponent.class, "AvailableColumnsPanel.title"));
        dd.setOptions(new Object[]{DialogDescriptor.OK_OPTION});
        DialogDisplayer.getDefault().notify(dd);
        refreshAllOnce();
        availableColumnsButton.setIcon(ImageUtilities.loadImageIcon("org/gephi/desktop/datalab/resources/light-bulb.png", true));
    }//GEN-LAST:event_availableColumnsButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane attributeColumnsScrollPane;
    private javax.swing.JButton availableColumnsButton;
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

    @Override
    public void componentOpened() {
        refreshAllOnce();
    }

    @Override
    public void componentClosed() {
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

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // TODO store your settings
    }

    void readProperties(java.util.Properties p) {
        String version = p.getProperty("version");
        // TODO read your settings according to their version
    }

    /**
     * <p>Exports a JTable to a CSV file showing first a dialog to select the
     * file to write.</p>
     *
     * @param parent Parent window
     * @param table Table to export
     * @param separator Separator to use for separating values of a row in the
     * CSV file. If null ',' will be used.
     * @param charset Charset encoding for the file
     * @param columnsToExport Indicates the indexes of the columns to export.
     * All columns will be exported if null
     */
    public static void exportTableAsCSV(JComponent parent, JTable table, Character separator, Charset charset, Integer[] columnsToExport, String fileName) {
        String lastPath = NbPreferences.forModule(TableCSVExporter.class).get(LAST_PATH, null);
        final JFileChooser chooser = new JFileChooser(lastPath);
        chooser.setAcceptAllFileFilterUsed(false);
        DialogFileFilter dialogFileFilter = new DialogFileFilter(NbBundle.getMessage(TableCSVExporter.class, "TableCSVExporter.filechooser.csvDescription"));
        dialogFileFilter.addExtension("csv");
        chooser.addChoosableFileFilter(dialogFileFilter);
        File selectedFile = new File(chooser.getCurrentDirectory(), fileName);
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
