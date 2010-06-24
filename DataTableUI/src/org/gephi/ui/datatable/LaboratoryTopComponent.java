/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.ui.datatable;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeEvent;
import org.gephi.data.attributes.api.AttributeListener;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.data.attributes.api.AttributeTable;
import org.gephi.datalaboratory.api.DataLaboratoryHelper;
import org.gephi.datalaboratory.spi.attributecolumns.AttributeColumnsManipulator;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.project.api.WorkspaceListener;
import org.jvnet.flamingo.common.CommandButtonDisplayState;
import org.jvnet.flamingo.common.JCommandButton;
import org.jvnet.flamingo.common.JCommandMenuButton;
import org.jvnet.flamingo.common.RichTooltip;
import org.jvnet.flamingo.common.icon.ImageWrapperResizableIcon;
import org.jvnet.flamingo.common.popup.JCommandPopupMenu;
import org.jvnet.flamingo.common.popup.JPopupPanel;
import org.jvnet.flamingo.common.popup.PopupPanelCallback;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;

/**
 * 
 * @author Eduardo Ramos <eduramiba@gmail.com>
 */
public final class LaboratoryTopComponent extends TopComponent implements AttributeListener {

    private static LaboratoryTopComponent instance;
    /** path to the icon used by the component and its open action */
    static final String ICON_PATH = "org/gephi/ui/datatable/resources/diamond.png";
    private static final String PREFERRED_ID = "LaboratoryTopComponent";

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
        pc.addWorkspaceListener(new WorkspaceListener() {

            public void initialize(Workspace workspace) {
            }

            public void select(Workspace workspace) {
                AttributeModel attributeModel = workspace.getLookup().lookup(AttributeModel.class);
                attributeModel.getNodeTable().addAttributeListener(LaboratoryTopComponent.this);
                attributeModel.getEdgeTable().addAttributeListener(LaboratoryTopComponent.this);
                refresh();
            }

            public void unselect(Workspace workspace) {
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
            AttributeModel attributeModel = pc.getCurrentWorkspace().getLookup().lookup(AttributeModel.class);
            attributeModel.getNodeTable().addAttributeListener(LaboratoryTopComponent.this);
            attributeModel.getEdgeTable().addAttributeListener(LaboratoryTopComponent.this);
        }
    }

    public void attributesChanged(AttributeEvent event) {
        refresh();
    }

    /**
     * Creates the buttons that call the AttributeColumnManipulators for nodes and edges table.
     */
    private void prepareNodeAndEdgeColumnButtons() {
        AttributeModel attributeModel=Lookup.getDefault().lookup(ProjectController.class).getCurrentWorkspace().getLookup().lookup(AttributeModel.class);
        AttributeTable nodesTable = attributeModel.getNodeTable();
        AttributeTable edgesTable = attributeModel.getEdgeTable();

        DataLaboratoryHelper dlh = Lookup.getDefault().lookup(DataLaboratoryHelper.class);
        AttributeColumnsManipulator[] manipulators = dlh.getAttributeColumnsManipulators();

        for (AttributeColumnsManipulator acm : manipulators) {
            nodesAttributeColumnsPanel.add(prepareJCommandButton(nodesTable, acm));
            edgesAttributeColumnsPanel.add(prepareJCommandButton(edgesTable, acm));
        }
    }

    /**
     * Creates a JCommandButton for the specified table and AttributeColumnsManipulator
     * @param table Table
     * @param acm AttributeColumnsManipulator
     * @return Prepared JCommandButton
     */
    private JCommandButton prepareJCommandButton(final AttributeTable table, final AttributeColumnsManipulator acm) {
        final AttributeColumn[] columns =table.getColumns();
        JCommandButton manipulatorButton;
        if (acm.getIcon() != null) {
            manipulatorButton = new JCommandButton(acm.getName(),ImageWrapperResizableIcon.getIcon(acm.getIcon(),new Dimension(16, 16)));
        }else{
            manipulatorButton = new JCommandButton(acm.getName());
        }
        manipulatorButton.setCommandButtonKind(JCommandButton.CommandButtonKind.POPUP_ONLY);
        manipulatorButton.setDisplayState(CommandButtonDisplayState.MEDIUM);
        if (acm.getDescription() != null && !acm.getDescription().isEmpty()) {
            manipulatorButton.setPopupRichTooltip(new RichTooltip(NbBundle.getMessage(LaboratoryTopComponent.class, "LaboratoryTopComponent.RichToolTip.title.text"), acm.getDescription()));
        }

        manipulatorButton.setPopupCallback(new PopupPanelCallback() {

            public JPopupPanel getPopupPanel(JCommandButton jcb) {
                JCommandPopupMenu popup = new JCommandPopupMenu();

                JCommandMenuButton button;
                for (final AttributeColumn column : columns) {
                    if (acm.canManipulateColumn(column)) {
                        button = new JCommandMenuButton(column.getTitle(), ImageWrapperResizableIcon.getIcon(ImageUtilities.loadImage("org/gephi/ui/datatable/resources/column.png"), new Dimension(16, 16)));
                        button.addActionListener(new ActionListener() {

                            public void actionPerformed(ActionEvent e) {
                                acm.execute(table, column);
                                //TODO: execute with Data Laboratory API.
                            }
                        });
                        popup.addMenuButton(button);
                    }
                }
                return popup;
            }
        });

        return manipulatorButton;
    }

    private void refresh() {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                clear();
                nodeEdgeTabbedPane.setEnabled(true);
                prepareNodeAndEdgeColumnButtons();
            }
        });

    }

    private void clear() {
        nodesAttributeColumnsPanel.removeAll();
        edgesAttributeColumnsPanel.removeAll();
        nodeEdgeTabbedPane.setEnabled(false);
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
        nodesAttributeColumnsPanel = new javax.swing.JPanel();
        edgesAttributeColumnsPanel = new javax.swing.JPanel();

        attributeColumnsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(LaboratoryTopComponent.class, "attributeColumnsPanel.title"))); // NOI18N

        nodesAttributeColumnsPanel.setName("a"); // NOI18N
        nodeEdgeTabbedPane.addTab(org.openide.util.NbBundle.getMessage(LaboratoryTopComponent.class, "LaboratoryTopComponent.a.TabConstraints.tabTitle"), nodesAttributeColumnsPanel); // NOI18N
        nodeEdgeTabbedPane.addTab(org.openide.util.NbBundle.getMessage(LaboratoryTopComponent.class, "LaboratoryTopComponent.edgesAttributeColumnsPanel.TabConstraints.tabTitle"), edgesAttributeColumnsPanel); // NOI18N

        javax.swing.GroupLayout attributeColumnsPanelLayout = new javax.swing.GroupLayout(attributeColumnsPanel);
        attributeColumnsPanel.setLayout(attributeColumnsPanelLayout);
        attributeColumnsPanelLayout.setHorizontalGroup(
            attributeColumnsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(attributeColumnsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(nodeEdgeTabbedPane, javax.swing.GroupLayout.DEFAULT_SIZE, 694, Short.MAX_VALUE)
                .addContainerGap())
        );
        attributeColumnsPanelLayout.setVerticalGroup(
            attributeColumnsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, attributeColumnsPanelLayout.createSequentialGroup()
                .addComponent(nodeEdgeTabbedPane, javax.swing.GroupLayout.DEFAULT_SIZE, 125, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(attributeColumnsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(attributeColumnsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel attributeColumnsPanel;
    private javax.swing.JPanel edgesAttributeColumnsPanel;
    private javax.swing.JTabbedPane nodeEdgeTabbedPane;
    private javax.swing.JPanel nodesAttributeColumnsPanel;
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

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // TODO store your settings
    }

    Object readProperties(java.util.Properties p) {
        if (instance == null) {
            instance = this;
        }
        instance.readPropertiesImpl(p);
        return instance;
    }

    private void readPropertiesImpl(java.util.Properties p) {
        String version = p.getProperty("version");
        // TODO read your settings according to their version
    }

    @Override
    protected String preferredID() {
        return PREFERRED_ID;
    }
}
