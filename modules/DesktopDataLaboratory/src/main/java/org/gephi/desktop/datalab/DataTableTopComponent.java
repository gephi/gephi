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
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Table;
import org.gephi.graph.api.TableObserver;
import org.gephi.datalab.api.DataLaboratoryHelper;
import org.gephi.datalab.api.datatables.AttributeTableCSVExporter;
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
import org.gephi.desktop.datalab.tables.EdgesDataTable;
import org.gephi.desktop.datalab.tables.NodesDataTable;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Element;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.GraphObserver;
import org.gephi.graph.api.Node;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.ProjectInformation;
import org.gephi.project.api.Workspace;
import org.gephi.project.api.WorkspaceInformation;
import org.gephi.project.api.WorkspaceListener;
import org.gephi.project.api.WorkspaceProvider;
import org.gephi.ui.components.BusyUtils;
import org.gephi.ui.components.WrapLayout;
import org.gephi.ui.utils.DialogFileFilter;
import org.gephi.ui.utils.UIUtils;
import org.gephi.utils.JTableCSVExporter;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.windows.TopComponent;
import org.pushingpixels.flamingo.api.common.CommandButtonDisplayState;
import org.pushingpixels.flamingo.api.common.JCommandButton;
import org.pushingpixels.flamingo.api.common.JCommandButtonPanel;
import org.pushingpixels.flamingo.api.common.JCommandButtonStrip;
import org.pushingpixels.flamingo.api.common.JCommandMenuButton;
import org.pushingpixels.flamingo.api.common.RichTooltip;
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
public class DataTableTopComponent extends TopComponent implements AWTEventListener, DataTablesEventListener {
    private enum DisplayTable {
        NODE, EDGE
    };
    //Settings
    private static final long AUTO_REFRESH_RATE_MILLISECONDS = 100;
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
    private final ProjectController pc;
    private final GraphController gc;
    private volatile GraphModel graphModel;
    private volatile DataTablesModel dataTablesModel;
    private volatile AvailableColumnsModel nodeAvailableColumnsModel;
    private volatile AvailableColumnsModel edgeAvailableColumnsModel;
    
    //Observers for auto-refreshing:
    private boolean autoRefreshEnabled = true;
    private GraphObserver graphObserver;
    private TableObserver nodesTableObserver;
    private TableObserver edgesTableObserver;
    //Timer for the observers:
    private java.util.Timer observersTimer;
    
    //Table
    private NodesDataTable nodeTable;
    private EdgesDataTable edgeTable;
    //General actions buttons
    private ArrayList<JComponent> generalActionsButtons = new ArrayList<JComponent>();
    //States
    private DisplayTable displayTable = DisplayTable.NODE;//Display nodes by default at first.
    private ArrayList previousNodeFilterColumns = new ArrayList();
    private ArrayList previousEdgeFilterColumns = new ArrayList();
    private Map<DisplayTable, String> filterTextByDisplayTable = new EnumMap<DisplayTable, String>(DisplayTable.class);
    private Map<DisplayTable, Integer> filterColumnIndexByDisplayTable = new EnumMap<DisplayTable, Integer>(DisplayTable.class);
    
    //Refresh executor
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


        //Init tables
        nodeTable = new NodesDataTable();
        edgeTable = new EdgesDataTable();

        nodeTable.setDrawSparklines(useSparklines);
        nodeTable.setDrawTimeIntervalGraphics(timeIntervalGraphics);
        edgeTable.setDrawSparklines(useSparklines);
        edgeTable.setDrawTimeIntervalGraphics(timeIntervalGraphics);
        edgeTable.setShowEdgesNodesLabels(showEdgesNodesLabels);

        //Init
        pc = Lookup.getDefault().lookup(ProjectController.class);
        gc = Lookup.getDefault().lookup(GraphController.class);
        Workspace workspace = pc.getCurrentWorkspace();
        if (workspace == null) {
            clearAll();
        } else {
            dataTablesModel = workspace.getLookup().lookup(DataTablesModel.class);
            if (dataTablesModel == null) {
                workspace.add(dataTablesModel = new DataTablesModel(workspace));
            }
            nodeAvailableColumnsModel = dataTablesModel.getNodeAvailableColumnsModel();
            edgeAvailableColumnsModel = dataTablesModel.getEdgeAvailableColumnsModel();
            refreshAllOnce();
        }

        initEvents();
        bannerPanel.setVisible(false);
    }

    private void activateWorkspace(Workspace workspace){
        //Prepare DataTablesEvent listener
        Lookup.getDefault().lookup(DataTablesController.class).setDataTablesEventListener(DataTableTopComponent.this);

        dataTablesModel = workspace.getLookup().lookup(DataTablesModel.class);
        if (dataTablesModel == null) {
            workspace.add(dataTablesModel = new DataTablesModel(workspace));
        }
        nodeAvailableColumnsModel = dataTablesModel.getNodeAvailableColumnsModel();
        edgeAvailableColumnsModel = dataTablesModel.getEdgeAvailableColumnsModel();
        hideTable();
        enableTableControls();

        graphModel = gc.getGraphModel(workspace);
        nodesTableObserver = gc.getGraphModel(workspace).getNodeTable().createTableObserver(false);
        edgesTableObserver = gc.getGraphModel(workspace).getEdgeTable().createTableObserver(false);
        graphObserver = graphModel.createGraphObserver(graphModel.getGraph(), false);

        refreshAllOnce();
    }
    
    private void deactivateAll(){
        graphObserver = null;
        if(nodesTableObserver != null){
            nodesTableObserver.destroy();
            nodesTableObserver = null;
        }
        if(edgesTableObserver != null){
            edgesTableObserver.destroy();
            edgesTableObserver = null;
        }

        graphModel = null;
        dataTablesModel = null;
        nodeAvailableColumnsModel = null;
        edgeAvailableColumnsModel = null;

        clearAll();
    }
    
    private void initEvents() {
        //Workspace Listener
        pc.addWorkspaceListener(new WorkspaceListener() {

            @Override
            public void initialize(Workspace workspace) {
                //Prepare DataTablesEvent listener
                Lookup.getDefault().lookup(DataTablesController.class).setDataTablesEventListener(DataTableTopComponent.this);
                if (workspace.getLookup().lookup(DataTablesModel.class) == null) {
                    workspace.add(new DataTablesModel(workspace));
                }
            }

            @Override
            public void select(Workspace workspace) {
                activateWorkspace(workspace);
            }

            @Override
            public void unselect(Workspace workspace) {
                deactivateAll();
            }

            @Override
            public void close(Workspace workspace) {
            }

            @Override
            public void disable() {
                clearAll();
                //No more workspaces active, disable the DataTablesEvent listener
                Lookup.getDefault().lookup(DataTablesController.class).setDataTablesEventListener(null);
            }
        });
        
        if (pc.getCurrentWorkspace() != null) {
            activateWorkspace(pc.getCurrentWorkspace());
        }
        
        observersTimer = new java.util.Timer("DataLaboratoryGraphObservers");
        observersTimer.scheduleAtFixedRate(new TimerTask() {

            @Override
            public void run() {
                if(!autoRefreshEnabled){
                    return;
                }
                
                boolean hasChanges = 
                        (graphObserver != null && graphObserver.hasGraphChanged())
                        || (nodesTableObserver != null && nodesTableObserver.hasTableChanged())
                        || (edgesTableObserver != null && edgesTableObserver.hasTableChanged());
                
                if(hasChanges){
                    graphChanged();//Execute refresh
                }
            }
        }
        , 0, AUTO_REFRESH_RATE_MILLISECONDS);//Check graph and tables for changes every 100 ms

        //Filter
        if (dynamicFiltering) {
            filterTextField.getDocument().addDocumentListener(new DocumentListener() {

                @Override
                public void insertUpdate(DocumentEvent e) {
                    refreshAppliedFilter();
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    refreshAppliedFilter();
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                }
            });
        } else {
            filterTextField.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    refreshAppliedFilter();
                }
            });
        }
        columnComboBox.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                refreshAppliedFilter();
            }
        });
        initKeyEventContextMenuActionMappings();
    }
    
    private boolean isShowingNodesTable(){
        return displayTable == DisplayTable.NODE;
    }
    
    private boolean isShowingEdgesTable(){
        return displayTable == DisplayTable.EDGE;
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

    @Override
    public void setAutoRefreshEnabled(boolean enabled) {
        this.autoRefreshEnabled = enabled;
    }

    @Override
    public boolean isAutoRefreshEnabled() {
        return autoRefreshEnabled;
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

            @Override
            public void run() {
                clearTableControls();
                clearColumnManipulators();
                clearGeneralActionsButtons();
            }
        });
    }

    private AvailableColumnsModel getTableAvailableColumnsModel(Table table) {
        if (Lookup.getDefault().lookup(GraphController.class).getGraphModel().getNodeTable() == table) {
            return nodeAvailableColumnsModel;
        } else if (Lookup.getDefault().lookup(GraphController.class).getGraphModel().getEdgeTable() == table) {
            return edgeAvailableColumnsModel;
        } else {
            return null;//Graph table or other table, not supported in data laboratory for now.
        }
    }

    /**
     * Start an auto-refresh if necessary.
     */
    public void graphChanged() {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
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
    private void refreshAppliedFilter() {
        int index = columnComboBox.getSelectedIndex();
        if (index < 0) {
            return;
        }
        if (isShowingNodesTable()) {
            if (nodeTable.setPattern(filterTextField.getText(), index)) {
                filterTextField.setBackground(Color.WHITE);
            } else {
                filterTextField.setBackground(invalidFilterColor);
            }
        } else if (isShowingEdgesTable()) {
            if (edgeTable.setPattern(filterTextField.getText(), index)) {
                filterTextField.setBackground(Color.WHITE);
            } else {
                filterTextField.setBackground(invalidFilterColor);
            }
        }
    }
    
    private void refreshAvailableColumnsButton(AvailableColumnsModel availableColumnsModel, Table table){
        if(table.countColumns() > availableColumnsModel.getAvailableColumnsCount()){
            availableColumnsButton.setIcon(ImageUtilities.loadImageIcon("org/gephi/desktop/datalab/resources/light-bulb--plus.png", true));
        }else{
            availableColumnsButton.setIcon(ImageUtilities.loadImageIcon("org/gephi/desktop/datalab/resources/light-bulb.png", true));
        }
    }

    private void initNodesView() {
        Runnable initNodesRunnable = new Runnable() {

            @Override
            public void run() {
                try {
                    if(dataTablesModel == null){
                        return;
                    }
                    
                    String busyMsg = NbBundle.getMessage(DataTableTopComponent.class, "DataTableTopComponent.tableScrollPane.busyMessage");
                    BusyUtils.BusyLabel busylabel = BusyUtils.createCenteredBusyLabel(tableScrollPane, busyMsg, nodeTable.getTable());
                    busylabel.setBusy(true);

                    //Attributes columns
                    nodeAvailableColumnsModel.syncronizeTableColumns();
                    final Column[] cols = nodeAvailableColumnsModel.getAvailableColumns();
                    
                    refreshAvailableColumnsButton(nodeAvailableColumnsModel, Lookup.getDefault().lookup(GraphController.class).getGraphModel().getNodeTable());

                    //Nodes from graph
                    Graph graph;
                    if (visibleOnly) {
                        graph = graphModel.getGraphVisible();
                    } else {
                        graph = graphModel.getGraph();
                    }
                    if (graph == null) {
                        tableScrollPane.setViewportView(null);
                        return;
                    }

                    //Model
                    nodeTable.refreshModel(graph.getNodes().toArray(), cols, graphModel, dataTablesModel);

                    busylabel.setBusy(false);
                    nodeTable.scrollToFirstElementSelected();
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

            @Override
            public void run() {
                try {
                    if(dataTablesModel == null){
                        return;
                    }
                    
                    String busyMsg = NbBundle.getMessage(DataTableTopComponent.class, "DataTableTopComponent.tableScrollPane.busyMessage");
                    BusyUtils.BusyLabel busylabel = BusyUtils.createCenteredBusyLabel(tableScrollPane, busyMsg, edgeTable.getTable());
                    busylabel.setBusy(true);

                    //Attributes columns
                    edgeAvailableColumnsModel.syncronizeTableColumns();
                    final Column[] cols = edgeAvailableColumnsModel.getAvailableColumns();
                    
                    refreshAvailableColumnsButton(edgeAvailableColumnsModel, Lookup.getDefault().lookup(GraphController.class).getGraphModel().getEdgeTable());

                    //Edges from graph
                    Graph graph;
                    if (visibleOnly) {
                        graph = graphModel.getGraphVisible();
                    } else {
                        graph = graphModel.getGraph();
                    }
                    if (graph == null) {
                        tableScrollPane.setViewportView(null);
                        return;
                    }

                    //Model
                    edgeTable.refreshModel(graph.getEdges().toArray(), cols, graphModel, dataTablesModel);

                    busylabel.setBusy(false);
                    edgeTable.scrollToFirstElementSelected();
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

            @Override
            public void run() {
                ArrayList columns = new ArrayList();
                if (isShowingNodesTable()) {
                    DefaultComboBoxModel model = new DefaultComboBoxModel();
                    for (int i = 0; i < nodeTable.getTable().getColumnCount(); i++) {
                        if (nodeTable.getTable().getColumnExt(i).isVisible()) {
                            model.addElement(nodeTable.getTable().getColumnExt(i).getTitle());
                            columns.add(nodeTable.getTable().getColumnExt(i).getTitle());
                        }
                    }

                    columnComboBox.setModel(model);
                    
                    Integer previousNodeColumnsFilterIndex = filterColumnIndexByDisplayTable.get(DisplayTable.NODE);
                    if (columns.equals(previousNodeFilterColumns) 
                            && previousNodeColumnsFilterIndex != null
                            && previousNodeColumnsFilterIndex < columnComboBox.getItemCount()) {//Preserve user selected column when the columns list does not change
                        columnComboBox.setSelectedIndex(previousNodeColumnsFilterIndex);
                    }
                    previousNodeFilterColumns = columns;
                } else if (isShowingEdgesTable()) {
                    DefaultComboBoxModel model = new DefaultComboBoxModel();
                    for (int i = 0; i < edgeTable.getTable().getColumnCount(); i++) {
                        if (edgeTable.getTable().getColumnExt(i).isVisible()) {
                            model.addElement(edgeTable.getTable().getColumnExt(i).getTitle());
                            columns.add(edgeTable.getTable().getColumnExt(i).getTitle());
                        }
                    }
                    columnComboBox.setModel(model);
                    
                    Integer previousEdgeColumnsFilterIndex = filterColumnIndexByDisplayTable.get(DisplayTable.EDGE);
                    if (columns.equals(previousEdgeFilterColumns)
                            && previousEdgeColumnsFilterIndex != null
                            && previousEdgeColumnsFilterIndex < columnComboBox.getItemCount()) {//Preserve user selected column when the columns list does not change
                        columnComboBox.setSelectedIndex(previousEdgeColumnsFilterIndex);
                    }
                    previousEdgeFilterColumns = columns;
                }
                
                
            }
        });
    }

    private void enableTableControls() {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
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

            @Override
            public void run() {
                tableScrollPane.setViewportView(null);
            }
        });
    }

    private void refreshTable() {
        bannerPanel.setVisible(false);
        if (isShowingNodesTable()) {
            nodesButton.setSelected(true);
            initNodesView();
        } else if (isShowingEdgesTable()) {
            edgesButton.setSelected(true);
            initEdgesView();
        }
        
        refreshFilterColumns();
        refreshAppliedFilter();
    }
    
    private void selectDisplayTable(DisplayTable newDisplayTable){
        filterTextByDisplayTable.put(displayTable, filterTextField.getText());
        filterColumnIndexByDisplayTable.put(displayTable, columnComboBox.getSelectedIndex());

        this.displayTable = newDisplayTable;

        filterTextField.setText(filterTextByDisplayTable.get(displayTable));
        refreshAllOnce();
    }

    @Override
    public void selectNodesTable() {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                selectDisplayTable(DisplayTable.NODE);
            }
        });
    }

    @Override
    public void selectEdgesTable() {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                selectDisplayTable(DisplayTable.EDGE);
            }
        });
    }

    @Override
    public void refreshCurrentTable() {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                refreshAllOnce();
            }
        });
    }

    @Override
    public void setNodeTableSelection(final Node[] nodes) {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                nodeTable.setElementsSelection(nodes);
                nodeTable.scrollToFirstElementSelected();
            }
        });
    }

    @Override
    public void setEdgeTableSelection(final Edge[] edges) {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                edgeTable.setElementsSelection(edges);
                edgeTable.scrollToFirstElementSelected();
            }
        });
    }

    @Override
    public Node[] getNodeTableSelection() {
        return nodeTable.getElementsFromSelectedRows().toArray(new Node[0]);
    }

    @Override
    public Edge[] getEdgeTableSelection() {
        return edgeTable.getElementsFromSelectedRows().toArray(new Edge[0]);
    }

    @Override
    public boolean isNodeTableMode() {
        return isShowingNodesTable();
    }

    @Override
    public boolean isEdgeTableMode() {
        return isShowingEdgesTable();
    }

    @Override
    public boolean isShowOnlyVisible() {
        return visibleOnly;
    }

    @Override
    public void setShowOnlyVisible(boolean showOnlyVisible) {
        visibleOnly = showOnlyVisible;
        refreshCurrentTable();
    }

    @Override
    public boolean isUseSparklines() {
        return useSparklines;
    }

    @Override
    public void setUseSparklines(boolean useSparklines) {
        this.useSparklines = useSparklines;
        nodeTable.setDrawSparklines(useSparklines);
        edgeTable.setDrawSparklines(useSparklines);
        refreshCurrentTable();
    }

    @Override
    public boolean isTimeIntervalGraphics() {
        return timeIntervalGraphics;
    }

    @Override
    public void setTimeIntervalGraphics(boolean timeIntervalGraphics) {
        this.timeIntervalGraphics = timeIntervalGraphics;
        nodeTable.setDrawTimeIntervalGraphics(timeIntervalGraphics);
        edgeTable.setDrawTimeIntervalGraphics(timeIntervalGraphics);
        refreshCurrentTable();
    }

    @Override
    public boolean isShowEdgesNodesLabels() {
        return showEdgesNodesLabels;
    }

    @Override
    public void setShowEdgesNodesLabels(boolean showEdgesNodesLabels) {
        this.showEdgesNodesLabels = showEdgesNodesLabels;
        edgeTable.setShowEdgesNodesLabels(showEdgesNodesLabels);
        refreshCurrentTable();
    }

    @Override
    public void exportCurrentTable(ExportMode exportMode) {
        Table table;
        String fileName = prepareTableExportFileName();
        boolean edgesTable;

        if (isShowingNodesTable()) {
            table = Lookup.getDefault().lookup(GraphController.class).getGraphModel().getNodeTable();
            edgesTable = false;
            fileName += " [Nodes]";
        } else {
            table = Lookup.getDefault().lookup(GraphController.class).getGraphModel().getEdgeTable();
            edgesTable = true;
            fileName += " [Edges]";
        }
        fileName += ".csv";

        switch (exportMode) {
            case CSV:
                showCSVExportUI(table, edgesTable, fileName);
                break;
        }
    }

    private String prepareTableExportFileName() {
        String fileName = null;
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

    private void showCSVExportUI(Table table, boolean edgesTable, String fileName) {
        CSVExportUI csvUI = new CSVExportUI(table, edgesTable);
        DialogDescriptor dd = new DialogDescriptor(csvUI, csvUI.getDisplayName());
        if (DialogDisplayer.getDefault().notify(dd).equals(DialogDescriptor.OK_OPTION)) {
            DataTableTopComponent.exportTableAsCSV(this, this.visibleOnly, table, edgesTable, csvUI.getSelectedSeparator(), csvUI.getSelectedCharset(), csvUI.getSelectedColumnsIndexes(), fileName);
        }
        csvUI.unSetup();
    }

    /**
     * ***********Column manipulators related methods:************
     */
    private void refreshColumnManipulators() {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
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
        GraphModel attributeModel = Lookup.getDefault().lookup(GraphController.class).getGraphModel();
        Table table;
        Column[] columns;
        if (isShowingNodesTable()) {
            table = attributeModel.getNodeTable();
        } else {
            table = attributeModel.getEdgeTable();
        }
        columns = getTableAvailableColumnsModel(table).getAvailableColumns();

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
    private JCommandButton prepareJCommandButton(final Table table, final Column[] columns, final AttributeColumnsManipulator acm) {
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

        final ArrayList<Column> availableColumns = new ArrayList<Column>();
        for (final Column column : columns) {
            if (acm.canManipulateColumn(table, column)) {
                availableColumns.add(column);
            }
        }

        if (!availableColumns.isEmpty()) {
            manipulatorButton.setPopupCallback(new PopupPanelCallback() {

                @Override
                public JPopupPanel getPopupPanel(JCommandButton jcb) {
                    JCommandPopupMenu popup = new JCommandPopupMenu();

                    JCommandMenuButton button;
                    for (final Column column : availableColumns) {

                        button = new JCommandMenuButton(column.getTitle(), ImageWrapperResizableIcon.getIcon(ImageUtilities.loadImage("org/gephi/desktop/datalab/resources/column.png"), new Dimension(16, 16)));
                        button.addActionListener(new ActionListener() {

                            @Override
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
        if (isShowingNodesTable()) {
            button.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    showAddColumnUI(AddColumnUI.Mode.NODES_TABLE);
                }
            });
        } else {
            button.addActionListener(new ActionListener() {

                @Override
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
        if (isShowingNodesTable()) {
            button.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    showMergeColumnsUI(MergeColumnsUI.Mode.NODES_TABLE);
                }
            });
        } else {
            button.addActionListener(new ActionListener() {

                @Override
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

            @Override
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

                    @Override
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

                @Override
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

                @Override
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
    @Override
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
                if (isShowingNodesTable()) {
                    final ContextMenuItemManipulator item = nodesActionMappings.get(evt.getKeyCode());
                    if (item != null) {
                        Node[] nodes = nodeTable.getElementsFromSelectedRows().toArray(new Node[0]);
                        if (nodes.length > 0) {
                            ((NodesManipulator) item).setup(nodes, nodes[0]);
                            if (item.isAvailable() && item.canExecute()) {
                                DataLaboratoryHelper.getDefault().executeManipulator(item);
                            }
                        }
                        evt.consume();
                    }
                } else if (isShowingEdgesTable()) {
                    final ContextMenuItemManipulator item = edgesActionMappings.get(evt.getKeyCode());
                    if (item != null) {
                        Edge[] edges = edgeTable.getElementsFromSelectedRows().toArray(new Edge[0]);
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
        DialogDescriptor dd = new DialogDescriptor(new ConfigurationPanel(this, graphModel), NbBundle.getMessage(DataTableTopComponent.class, "ConfigurationPanel.title"));
        dd.setOptions(new Object[]{DialogDescriptor.OK_OPTION});
        DialogDisplayer.getDefault().notify(dd);

        //Save preferences:
        NbPreferences.forModule(DataTableTopComponent.class).putBoolean(DATA_LABORATORY_ONLY_VISIBLE, visibleOnly);
        NbPreferences.forModule(DataTableTopComponent.class).putBoolean(DATA_LABORATORY_SPARKLINES, useSparklines);
        NbPreferences.forModule(DataTableTopComponent.class).putBoolean(DATA_LABORATORY_TIME_INTERVAL_GRAPHICS, timeIntervalGraphics);
        NbPreferences.forModule(DataTableTopComponent.class).putBoolean(DATA_LABORATORY_EDGES_NODES_LABELS, showEdgesNodesLabels);
    }//GEN-LAST:event_configurationButtonActionPerformed

    private void availableColumnsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_availableColumnsButtonActionPerformed
        Table table;
        AvailableColumnsModel availableColumnsModel;
        if (isShowingNodesTable()) {
            table = Lookup.getDefault().lookup(GraphController.class).getGraphModel().getNodeTable();
        } else {
            table = Lookup.getDefault().lookup(GraphController.class).getGraphModel().getEdgeTable();
        }
        availableColumnsModel = getTableAvailableColumnsModel(table);
        DialogDescriptor dd = new DialogDescriptor(new AvailableColumnsPanel(table, availableColumnsModel).getValidationPanel(), NbBundle.getMessage(DataTableTopComponent.class, "AvailableColumnsPanel.title"));
        dd.setOptions(new Object[]{DialogDescriptor.OK_OPTION});
        DialogDisplayer.getDefault().notify(dd);
        refreshAllOnce();
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
    }

    void readProperties(java.util.Properties p) {
        String version = p.getProperty("version");
    }

    /**
     * <p>Exports a AttributeTable to a CSV file showing first a dialog to select the
     * file to write.</p>
     *
     * @param parent Parent window
     * @param visibleOnly Show only visible graph
     * @param table Table to export
     * @param separator Separator to use for separating values of a row in the
     * CSV file. If null ',' will be used.
     * @param charset Charset encoding for the file
     * @param columnsToExport Indicates the indexes of the columns to export.
     * All columns will be exported if null
     */
    public static void exportTableAsCSV(JComponent parent, boolean visibleOnly, Table table, boolean edgesTable, Character separator, Charset charset, Integer[] columnsToExport, String fileName) {
        //Validate that at least 1 column is selected:
        if(columnsToExport.length < 1){
            return;
        }
        
        String lastPath = NbPreferences.forModule(JTableCSVExporter.class).get(LAST_PATH, null);
        final JFileChooser chooser = new JFileChooser(lastPath);
        chooser.setAcceptAllFileFilterUsed(false);
        DialogFileFilter dialogFileFilter = new DialogFileFilter(NbBundle.getMessage(DataTableTopComponent.class, "TableCSVExporter.filechooser.csvDescription"));
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
        NbPreferences.forModule(JTableCSVExporter.class).put(LAST_PATH, defaultDirectory);
        try {
            Element[] rows;
            Graph graph;
            if (visibleOnly) {
                graph = Lookup.getDefault().lookup(GraphController.class).getGraphModel().getGraphVisible();
            } else {
                graph = Lookup.getDefault().lookup(GraphController.class).getGraphModel().getGraph();
            }

            if(edgesTable){
                rows = graph.getEdges().toArray();
            }else{
                rows = graph.getNodes().toArray();
            }
            
            AttributeTableCSVExporter.writeCSVFile(graph, table, file, separator, charset, columnsToExport, rows);
            JOptionPane.showMessageDialog(parent, NbBundle.getMessage(DataTableTopComponent.class, "TableCSVExporter.dialog.success"));
        } catch (Exception ex) {
            Logger.getLogger("").log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(parent, NbBundle.getMessage(DataTableTopComponent.class, "TableCSVExporter.dialog.error"), NbBundle.getMessage(DataTableTopComponent.class, "TableCSVExporter.dialog.error.title"), JOptionPane.ERROR_MESSAGE);
        }
    }
    private static final String LAST_PATH = "TableCSVExporter_Save_Last_Path";
}
