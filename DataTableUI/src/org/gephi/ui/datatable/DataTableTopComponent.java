/*
Copyright 2008 WebAtlas
Authors : Mathieu Bastian, Mathieu Jacomy, Julian Bilcke, Eduardo Ramos
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
package org.gephi.ui.datatable;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.Serializable;
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
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeEvent;
import org.gephi.data.attributes.api.AttributeListener;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.data.attributes.api.AttributeTable;
import org.gephi.datalaboratory.api.DataLaboratoryHelper;
import org.gephi.datalaboratory.api.DataTablesController;
import org.gephi.datalaboratory.api.DataTablesEventListener;
import org.gephi.datalaboratory.spi.attributecolumns.AttributeColumnsManipulator;
import org.gephi.datalaboratory.spi.generalactions.GeneralActionsManipulator;
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
import org.gephi.ui.general.actions.AddColumnPanel;
import org.gephi.ui.utils.UIUtils;
import org.jvnet.flamingo.common.CommandButtonDisplayState;
import org.jvnet.flamingo.common.JCommandButton;
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
import org.openide.NotifyDescriptor;
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
final class DataTableTopComponent extends TopComponent implements DataTablesEventListener, AttributeListener, GraphListener {

    private enum ClassDisplayed {

        NONE, NODE, EDGE
    };
    private static DataTableTopComponent instance;
    static final String ICON_PATH = "org/gephi/ui/datatable/resources/small.png";
    private static final String PREFERRED_ID = "DataTableTopComponent";
    //Settings
    private static final String DATA_LABORATORY_DYNAMIC_FILTERING = "DataLaboratory_Dynamic_Filtering";
    private static final Color invalidFilterColor = new Color(254, 242, 242);
    private final boolean dynamicFiltering;
    //Data
    private GraphModel graphModel;
    private DataTablesModel dataTablesModel;
    private boolean visibleOnly = false;
    //Table
    private NodeDataTable nodeTable;
    private EdgeDataTable edgeTable;
    //General actions buttons
    private ArrayList<JButton> generalActionsButtons = new ArrayList<JButton>();
    //States
    ClassDisplayed classDisplayed = ClassDisplayed.NODE;//Display nodes by default at first.
    //Executor
    ExecutorService taskExecutor;
    RefreshOnceHelperThread refreshOnceHelperThread;

    private DataTableTopComponent() {

        //Settings
        dynamicFiltering = NbPreferences.forModule(DataTableTopComponent.class).getBoolean(DATA_LABORATORY_DYNAMIC_FILTERING, true);

        taskExecutor = new ThreadPoolExecutor(0, 1, 10L, TimeUnit.SECONDS, new LinkedBlockingDeque<Runnable>(20), new ThreadFactory() {

            public Thread newThread(Runnable r) {
                return new Thread(r, "Data Laboratory fetching");
            }
        });

        initComponents();
        setName(NbBundle.getMessage(DataTableTopComponent.class, "CTL_DataTableTopComponent"));
//        setToolTipText(NbBundle.getMessage(DataTableTopComponent.class, "HINT_DataTableTopComponent"));
        setIcon(ImageUtilities.loadImage(ICON_PATH));

        //toolbar
        Border b = (Border) UIManager.get("Nb.Editor.Toolbar.border"); //NOI18N
        controlToolbar.setBorder(b);
        if (UIUtils.isAquaLookAndFeel()) {
            controlToolbar.setBackground(UIManager.getColor("NbExplorerView.background"));
        }

        initEvents();

        //Init table
        nodeTable = new NodeDataTable();
        edgeTable = new EdgeDataTable();

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
        //DataTablesEvent listener
        Lookup.getDefault().lookup(DataTablesController.class).addDataTablesEventListener(this);
        //Workspace Listener
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        final GraphController gc = Lookup.getDefault().lookup(GraphController.class);
        pc.addWorkspaceListener(new WorkspaceListener() {

            public void initialize(Workspace workspace) {
                workspace.add(new DataTablesModel());
            }

            public void select(Workspace workspace) {
                hideTable();
                enableTableControls();
                bannerPanel.setVisible(false);

                AttributeModel attributeModel = Lookup.getDefault().lookup(AttributeController.class).getModel();
                attributeModel.getNodeTable().addAttributeListener(DataTableTopComponent.this);
                attributeModel.getEdgeTable().addAttributeListener(DataTableTopComponent.this);

                graphModel = gc.getModel();
                graphModel.addGraphListener(DataTableTopComponent.this);
                dataTablesModel = workspace.getLookup().lookup(DataTablesModel.class);

                refreshAll();
            }

            public void unselect(Workspace workspace) {
                graphModel.removeGraphListener(DataTableTopComponent.this);
                AttributeModel attributeModel = workspace.getLookup().lookup(AttributeModel.class);
                attributeModel.getNodeTable().removeAttributeListener(DataTableTopComponent.this);
                attributeModel.getEdgeTable().removeAttributeListener(DataTableTopComponent.this);
                graphModel = null;
                dataTablesModel = null;
                clearAll();
            }

            public void close(Workspace workspace) {
                clearAll();
            }

            public void disable() {
                clearAll();
            }
        });
        if (pc.getCurrentWorkspace() != null) {
            dataTablesModel = pc.getCurrentWorkspace().getLookup().lookup(DataTablesModel.class);
            graphModel = gc.getModel();
            graphModel.addGraphListener(DataTableTopComponent.this);

            AttributeModel attributeModel = pc.getCurrentWorkspace().getLookup().lookup(AttributeModel.class);
            attributeModel.getNodeTable().addAttributeListener(DataTableTopComponent.this);
            attributeModel.getEdgeTable().addAttributeListener(DataTableTopComponent.this);
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

        visibleGraphCheckbox.setSelected(visibleOnly);
        visibleGraphCheckbox.addItemListener(new ItemListener() {

            public void itemStateChanged(ItemEvent e) {
                visibleOnly = visibleGraphCheckbox.isSelected();
                if (classDisplayed.equals(ClassDisplayed.NODE)) {
                    initNodesView();
                } else if (classDisplayed.equals(ClassDisplayed.EDGE)) {
                    initEdgesView();
                }
            }
        });
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
        if (refreshOnceHelperThread == null || !refreshOnceHelperThread.isAlive()) {
            refreshOnceHelperThread = new RefreshOnceHelperThread();
            refreshOnceHelperThread.start();
        } else {
            refreshOnceHelperThread.eventAttended();
        }
//        if (!bannerPanel.isVisible() && classDisplayed != ClassDisplayed.NONE) {
//            SwingUtilities.invokeLater(new Runnable() {
//
//                public void run() {
//                    bannerPanel.setVisible(true);
//                }
//            });
//        }
//        refreshColumnManipulators();
//        refreshGeneralActionsButtons();
    }

    public void graphChanged(GraphEvent event) {
        if (refreshOnceHelperThread == null || !refreshOnceHelperThread.isAlive()) {
            refreshOnceHelperThread = new RefreshOnceHelperThread();
            refreshOnceHelperThread.start();
        } else {
            refreshOnceHelperThread.eventAttended();
        }
//        if (!bannerPanel.isVisible() && classDisplayed != ClassDisplayed.NONE) {
//            SwingUtilities.invokeLater(new Runnable() {
//
//                public void run() {
//                    bannerPanel.setVisible(true);
//                }
//            });
//        }
//        refreshColumnManipulators();
//        refreshGeneralActionsButtons();
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

    private void enableTableControls() {
        nodesButton.setEnabled(true);
        edgesButton.setEnabled(true);
        filterTextField.setEnabled(true);
        labelFilter.setEnabled(true);
        visibleGraphCheckbox.setEnabled(true);
    }

    private void clearTableControls() {
        elementGroup.clearSelection();
        nodesButton.setEnabled(false);
        edgesButton.setEnabled(false);
        filterTextField.setEnabled(false);
        labelFilter.setEnabled(false);
        bannerPanel.setVisible(false);
        visibleGraphCheckbox.setEnabled(false);
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
            }
        });
    }

    public void setEdgeTableSelection(final Edge[] edges) {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                edgeTable.setEdgesSelection(edges);
            }
        });
    }

    /*************Column manipulators related methods:*************/
    private void refreshColumnManipulators() {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                clearColumnManipulators();
                prepareAddColumnButton();
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

        DataLaboratoryHelper dlh = Lookup.getDefault().lookup(DataLaboratoryHelper.class);
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

                        button = new JCommandMenuButton(column.getTitle(), ImageWrapperResizableIcon.getIcon(ImageUtilities.loadImage("org/gephi/ui/datatable/resources/column.png"), new Dimension(16, 16)));
                        button.addActionListener(new ActionListener() {

                            public void actionPerformed(ActionEvent e) {
                                Lookup.getDefault().lookup(DataLaboratoryHelper.class).executeAttributeColumnsManipulator(acm, table, column);
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
     * Create the special Add new column buttons for nodes and edges table.
     */
    private void prepareAddColumnButton() {
        JCommandButtonStrip strip = new JCommandButtonStrip(JCommandButtonStrip.StripOrientation.HORIZONTAL);
        strip.setDisplayState(CommandButtonDisplayState.BIG);
        JCommandButton button = new JCommandButton(NbBundle.getMessage(DataTableTopComponent.class, "DataTableTopComponent.addColumnButton.text"), ImageWrapperResizableIcon.getIcon(ImageUtilities.loadImage("/org/gephi/ui/datatable/resources/table-insert-column.png", true), new Dimension(16, 16)));
        button.setCommandButtonKind(JCommandButton.CommandButtonKind.ACTION_ONLY);
        button.setDisplayState(CommandButtonDisplayState.BIG);
        if (classDisplayed == ClassDisplayed.NODE) {
            button.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    showAddColumnUI(AddColumnPanel.Mode.NODES_TABLE);
                }
            });
        } else {
            button.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    showAddColumnUI(AddColumnPanel.Mode.EDGES_TABLE);
                }
            });
        }
        strip.add(button);
        columnManipulatorsPanel.add(strip);
    }

    private void showAddColumnUI(AddColumnPanel.Mode mode) {
        AddColumnPanel panel = new AddColumnPanel();
        panel.setup(mode);
        DialogDescriptor dd = new DialogDescriptor(panel, panel.getDisplayName());
        if (DialogDisplayer.getDefault().notify(dd).equals(NotifyDescriptor.OK_OPTION)) {
            panel.execute();
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
        for (JButton b : generalActionsButtons) {
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

        final DataLaboratoryHelper dlh = Lookup.getDefault().lookup(DataLaboratoryHelper.class);
        GeneralActionsManipulator[] manipulators = dlh.getGeneralActionsManipulators();
        JButton button;
        for (final GeneralActionsManipulator m : manipulators) {
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
        controlToolbar.updateUI();
    }

    /**
     * This thread is used for processing graphChanged and attributesChanged events.
     * It takes care to only refresh the UI once (the last one) when a lot of events come in a short period of time.
     */
    class RefreshOnceHelperThread extends Thread {
        private static final int CHECK_TIME_INTERVAL=50;//50 ms.
        private boolean moreEvents = false;

        @Override
        public void run() {
            try {
                do {
                    moreEvents = false;
                    Thread.sleep(CHECK_TIME_INTERVAL);
                } while (moreEvents);
                DataTableTopComponent.this.refreshAll();
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        public void eventAttended() {
            this.moreEvents = true;
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
        visibleGraphCheckbox = new javax.swing.JCheckBox();
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

        org.openide.awt.Mnemonics.setLocalizedText(visibleGraphCheckbox, org.openide.util.NbBundle.getMessage(DataTableTopComponent.class, "DataTableTopComponent.visibleGraphCheckbox.text")); // NOI18N
        visibleGraphCheckbox.setFocusable(false);
        visibleGraphCheckbox.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        controlToolbar.add(visibleGraphCheckbox);
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

        labelBanner.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/gephi/ui/datatable/resources/info.png"))); // NOI18N
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

        attributeColumnsScrollPane.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(DataTableTopComponent.class, "DataTableTopComponent.attributeColumnsPanel.title"))); // NOI18N
        attributeColumnsScrollPane.setMinimumSize(new java.awt.Dimension(200, 130));
        attributeColumnsScrollPane.setPreferredSize(new java.awt.Dimension(200, 130));

        columnManipulatorsPanel.setMinimumSize(new java.awt.Dimension(200, 100));
        columnManipulatorsPanel.setPreferredSize(new java.awt.Dimension(200, 100));
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
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane attributeColumnsScrollPane;
    private javax.swing.JPanel bannerPanel;
    private javax.swing.JLabel boxGlue;
    private javax.swing.JComboBox columnComboBox;
    private javax.swing.JPanel columnManipulatorsPanel;
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
    private javax.swing.JCheckBox visibleGraphCheckbox;
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
}
