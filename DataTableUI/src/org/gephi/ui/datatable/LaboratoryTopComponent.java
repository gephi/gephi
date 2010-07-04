/*
Copyright 2008-2010 Gephi
Authors : Eduardo Ramos <eduramiba@gmail.com>
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

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.SwingUtilities;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeEvent;
import org.gephi.data.attributes.api.AttributeListener;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.data.attributes.api.AttributeTable;
import org.gephi.datalaboratory.api.DataLaboratoryHelper;
import org.gephi.datalaboratory.spi.attributecolumns.AttributeColumnsManipulator;
import org.gephi.datalaboratory.spi.generalactions.GeneralActionsManipulator;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphEvent;
import org.gephi.graph.api.GraphListener;
import org.gephi.graph.api.GraphModel;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.project.api.WorkspaceListener;
import org.gephi.ui.general.actions.AddColumnPanel;
import org.jvnet.flamingo.common.CommandButtonDisplayState;
import org.jvnet.flamingo.common.JCommandButton;
import org.jvnet.flamingo.common.JCommandButtonStrip;
import org.jvnet.flamingo.common.JCommandMenuButton;
import org.jvnet.flamingo.common.RichTooltip;
import org.jvnet.flamingo.common.icon.ImageWrapperResizableIcon;
import org.jvnet.flamingo.common.popup.JCommandPopupMenu;
import org.jvnet.flamingo.common.popup.JPopupPanel;
import org.jvnet.flamingo.common.popup.PopupPanelCallback;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;

/**
 * 
 * @author Eduardo Ramos <eduramiba@gmail.com>
 */
public final class LaboratoryTopComponent extends TopComponent implements AttributeListener, GraphListener {

    private static LaboratoryTopComponent instance;
    /** path to the icon used by the component and its open action */
    static final String ICON_PATH = "org/gephi/ui/datatable/resources/diamond.png";
    private static final String PREFERRED_ID = "LaboratoryTopComponent";
    private GraphModel graphModel;

    public LaboratoryTopComponent() {
        initComponents();
        setName(NbBundle.getMessage(LaboratoryTopComponent.class, "CTL_LaboratoryTopComponent"));
        setToolTipText(NbBundle.getMessage(LaboratoryTopComponent.class, "HINT_LaboratoryTopComponent"));
        setIcon(ImageUtilities.loadImage(ICON_PATH, true));

        initEvents();


        //Init controls:
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        if (pc.getCurrentWorkspace() == null) {
            clear();
        } else {
            refresh();
        }
    }

    private void initEvents() {
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        final GraphController gc = Lookup.getDefault().lookup(GraphController.class);
        pc.addWorkspaceListener(new WorkspaceListener() {

            public void initialize(Workspace workspace) {
            }

            public void select(Workspace workspace) {
                AttributeModel attributeModel = Lookup.getDefault().lookup(AttributeController.class).getModel();
                attributeModel.getNodeTable().addAttributeListener(LaboratoryTopComponent.this);
                attributeModel.getEdgeTable().addAttributeListener(LaboratoryTopComponent.this);

                graphModel = gc.getModel();
                graphModel.addGraphListener(LaboratoryTopComponent.this);
                
                refresh();
            }

            public void unselect(Workspace workspace) {
                graphModel.removeGraphListener(LaboratoryTopComponent.this);
                graphModel = null;

                AttributeModel attributeModel = workspace.getLookup().lookup(AttributeModel.class);
                attributeModel.getNodeTable().addAttributeListener(LaboratoryTopComponent.this);
                attributeModel.getEdgeTable().addAttributeListener(LaboratoryTopComponent.this);
                clear();
            }

            public void close(Workspace workspace) {
                clear();
            }

            public void disable() {
                clear();
            }
        });
        if (pc.getCurrentWorkspace() != null) {
            graphModel = gc.getModel();
            graphModel.addGraphListener(LaboratoryTopComponent.this);

            AttributeModel attributeModel = pc.getCurrentWorkspace().getLookup().lookup(AttributeModel.class);
            attributeModel.getNodeTable().addAttributeListener(LaboratoryTopComponent.this);
            attributeModel.getEdgeTable().addAttributeListener(LaboratoryTopComponent.this);
        }
    }

    public void attributesChanged(AttributeEvent event) {
        refresh();
    }

    public void graphChanged(GraphEvent event) {
        refresh();
    }

    /**
     * Creates the buttons that call the AttributeColumnManipulators for nodes and edges table.
     */
    private void prepareNodeAndEdgeColumnButtons() {
        AttributeModel attributeModel = Lookup.getDefault().lookup(ProjectController.class).getCurrentWorkspace().getLookup().lookup(AttributeModel.class);
        AttributeTable nodesTable = attributeModel.getNodeTable();
        AttributeTable edgesTable = attributeModel.getEdgeTable();

        DataLaboratoryHelper dlh = Lookup.getDefault().lookup(DataLaboratoryHelper.class);
        AttributeColumnsManipulator[] manipulators = dlh.getAttributeColumnsManipulators();

        JCommandButtonStrip currentNodeButtonGroup = new JCommandButtonStrip(JCommandButtonStrip.StripOrientation.HORIZONTAL);
        currentNodeButtonGroup.setDisplayState(CommandButtonDisplayState.MEDIUM);
        JCommandButtonStrip currentEdgeButtonGroup = new JCommandButtonStrip(JCommandButtonStrip.StripOrientation.HORIZONTAL);
        currentEdgeButtonGroup.setDisplayState(CommandButtonDisplayState.MEDIUM);
        Integer lastManipulatorType = null;
        for (AttributeColumnsManipulator acm : manipulators) {
            if (lastManipulatorType == null) {
                lastManipulatorType = acm.getType();
            }
            if (lastManipulatorType != acm.getType()) {
                nodesAttributeColumnsPanel.add(currentNodeButtonGroup);
                edgesAttributeColumnsPanel.add(currentEdgeButtonGroup);
                currentNodeButtonGroup = new JCommandButtonStrip(JCommandButtonStrip.StripOrientation.HORIZONTAL);
                currentNodeButtonGroup.setDisplayState(CommandButtonDisplayState.MEDIUM);
                currentEdgeButtonGroup = new JCommandButtonStrip(JCommandButtonStrip.StripOrientation.HORIZONTAL);
                currentEdgeButtonGroup.setDisplayState(CommandButtonDisplayState.MEDIUM);
            }
            lastManipulatorType = acm.getType();
            currentNodeButtonGroup.add(prepareJCommandButton(nodesTable, acm));
            currentEdgeButtonGroup.add(prepareJCommandButton(edgesTable, acm));
        }
        nodesAttributeColumnsPanel.add(currentNodeButtonGroup);
        edgesAttributeColumnsPanel.add(currentEdgeButtonGroup);
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
            manipulatorButton.setPopupRichTooltip(new RichTooltip(NbBundle.getMessage(LaboratoryTopComponent.class, "LaboratoryTopComponent.RichToolTip.title.text"), acm.getDescription()));
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
    private void prepareAddColumnButtons() {
        JButton button;
        button = new JButton(NbBundle.getMessage(LaboratoryTopComponent.class, "LaboratoryTopComponent.addNodeColumnButton.text"), ImageUtilities.loadImageIcon("/org/gephi/ui/datatable/resources/table-insert-column.png", true));
        button.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                showAddColumnUI(AddColumnPanel.Mode.NODES_TABLE);
            }
        });
        nodesAttributeColumnsPanel.add(button);
        button = new JButton(NbBundle.getMessage(LaboratoryTopComponent.class, "LaboratoryTopComponent.addEdgeColumnButton.text"), ImageUtilities.loadImageIcon("/org/gephi/ui/datatable/resources/table-insert-column.png", true));
        button.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                showAddColumnUI(AddColumnPanel.Mode.EDGES_TABLE);
            }
        });
        edgesAttributeColumnsPanel.add(button);
    }

    /**
     * Adds the buttons for the GeneralActionsManipulators.
     */
    public void prepareGeneralActionButtons() {
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
            generalManipulatorsPanel.add(button);
        }

    }

    private void refresh() {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                clear();
                nodeEdgeTabbedPane.setEnabled(true);
                prepareGeneralActionButtons();
                prepareAddColumnButtons();
                prepareNodeAndEdgeColumnButtons();
                generalManipulatorsPanel.updateUI();
                nodesAttributeColumnsPanel.updateUI();
                edgesAttributeColumnsPanel.updateUI();
            }
        });
    }

    private void clear() {
        generalManipulatorsPanel.removeAll();
        nodesAttributeColumnsPanel.removeAll();
        edgesAttributeColumnsPanel.removeAll();
        nodeEdgeTabbedPane.setEnabled(false);
        generalManipulatorsPanel.updateUI();
        nodesAttributeColumnsPanel.updateUI();
        edgesAttributeColumnsPanel.updateUI();
    }

    private void showAddColumnUI(AddColumnPanel.Mode mode) {
        AddColumnPanel panel = new AddColumnPanel();
        panel.setup(mode);
        DialogDescriptor dd = new DialogDescriptor(panel, panel.getDisplayName());
        if (DialogDisplayer.getDefault().notify(dd).equals(NotifyDescriptor.OK_OPTION)) {
            panel.execute();
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        attributeColumnsPanel = new javax.swing.JPanel();
        nodeEdgeTabbedPane = new javax.swing.JTabbedPane();
        nodesScroll = new javax.swing.JScrollPane();
        nodesAttributeColumnsPanel = new javax.swing.JPanel();
        edgesScroll = new javax.swing.JScrollPane();
        edgesAttributeColumnsPanel = new javax.swing.JPanel();
        generalManipulatorsScroll = new javax.swing.JScrollPane();
        generalManipulatorsPanel = new javax.swing.JPanel();

        attributeColumnsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(LaboratoryTopComponent.class, "LaboratoryTopComponent.attributeColumnsPanel.title"))); // NOI18N

        nodesScroll.setBorder(null);

        nodesAttributeColumnsPanel.setName("a"); // NOI18N
        nodesAttributeColumnsPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 25, 20));
        nodesScroll.setViewportView(nodesAttributeColumnsPanel);

        nodeEdgeTabbedPane.addTab(org.openide.util.NbBundle.getMessage(LaboratoryTopComponent.class, "LaboratoryTopComponent.nodesScroll.TabConstraints.tabTitle"), nodesScroll); // NOI18N

        edgesScroll.setBorder(null);

        edgesAttributeColumnsPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 25, 20));
        edgesScroll.setViewportView(edgesAttributeColumnsPanel);

        nodeEdgeTabbedPane.addTab(org.openide.util.NbBundle.getMessage(LaboratoryTopComponent.class, "LaboratoryTopComponent.edgesScroll.TabConstraints.tabTitle"), edgesScroll); // NOI18N

        javax.swing.GroupLayout attributeColumnsPanelLayout = new javax.swing.GroupLayout(attributeColumnsPanel);
        attributeColumnsPanel.setLayout(attributeColumnsPanelLayout);
        attributeColumnsPanelLayout.setHorizontalGroup(
            attributeColumnsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(attributeColumnsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(nodeEdgeTabbedPane, javax.swing.GroupLayout.DEFAULT_SIZE, 493, Short.MAX_VALUE)
                .addContainerGap())
        );
        attributeColumnsPanelLayout.setVerticalGroup(
            attributeColumnsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, attributeColumnsPanelLayout.createSequentialGroup()
                .addComponent(nodeEdgeTabbedPane, javax.swing.GroupLayout.DEFAULT_SIZE, 99, Short.MAX_VALUE)
                .addContainerGap())
        );

        generalManipulatorsPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 25, 20));
        generalManipulatorsScroll.setViewportView(generalManipulatorsPanel);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(generalManipulatorsScroll, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 525, Short.MAX_VALUE)
                    .addComponent(attributeColumnsPanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(attributeColumnsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(generalManipulatorsScroll, javax.swing.GroupLayout.DEFAULT_SIZE, 69, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel attributeColumnsPanel;
    private javax.swing.JPanel edgesAttributeColumnsPanel;
    private javax.swing.JScrollPane edgesScroll;
    private javax.swing.JPanel generalManipulatorsPanel;
    private javax.swing.JScrollPane generalManipulatorsScroll;
    private javax.swing.JTabbedPane nodeEdgeTabbedPane;
    private javax.swing.JPanel nodesAttributeColumnsPanel;
    private javax.swing.JScrollPane nodesScroll;
    // End of variables declaration//GEN-END:variables

    /**
     * Gets default instance. Do not use directly: reserved for *.settings files only,
     * i.e. deserialization routines; otherwise you could get a non-deserialized instance.
     * To obtain the singleton instance, use {@link #findInstance}.
     */
    public static synchronized LaboratoryTopComponent getDefault() {
        if (instance == null) {
            instance = new LaboratoryTopComponent();
        }
        return instance;
    }

    /**
     * Obtain the LaboratoryTopComponent instance. Never call {@link #getDefault} directly!
     */
    public static synchronized LaboratoryTopComponent findInstance() {
        TopComponent win = WindowManager.getDefault().findTopComponent(PREFERRED_ID);
        if (win == null) {
            Logger.getLogger(LaboratoryTopComponent.class.getName()).warning(
                    "Cannot find " + PREFERRED_ID + " component. It will not be located properly in the window system.");
            return getDefault();
        }
        if (win instanceof LaboratoryTopComponent) {
            return (LaboratoryTopComponent) win;
        }
        Logger.getLogger(LaboratoryTopComponent.class.getName()).warning(
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
    protected String preferredID() {
        return PREFERRED_ID;
    }
}
