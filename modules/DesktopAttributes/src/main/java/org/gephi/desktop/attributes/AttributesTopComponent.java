package org.gephi.desktop.attributes;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JToolBar;
import org.gephi.desktop.attributes.edit.EditPanel;
import org.gephi.desktop.attributes.selection.SelectionPanel;
import org.gephi.graph.api.Column;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

@ConvertAsProperties(dtd = "-//org.gephi.desktop.attributes//Attributes//EN",
    autostore = false)
@TopComponent.Description(preferredID = "AttributesTopComponent",
    iconBase = "DesktopAttributes/edit.svg",
    persistenceType = TopComponent.PERSISTENCE_ALWAYS)
@TopComponent.Registration(mode = "rankingmode", openAtStartup = false, roles = {"overview", "datalab"}, position = 20)
@ActionID(category = "Window", id = "org.gephi.desktop.attributes.AttributesTopComponent")
@ActionReference(path = "Menu/Window", position = 1500)
@TopComponent.OpenActionRegistration(displayName = "#CTL_AttributesTopComponent",
    preferredID = "AttributesTopComponent")
public final class AttributesTopComponent extends TopComponent implements AttributesUIModelListener {

    private static final String SELECTION_CARD = "selection";
    private static final String EDIT_CARD = "edit";

    private final CardLayout cardLayout;
    private final JPanel cardPanel;
    private final EditPanel editPanel;
    private final SelectionPanel selectionPanel;
    private final JButton columnsButton;

    private final AttributesUIControllerImpl controller;

    // Model
    private AttributesUIModelImpl model;

    public AttributesTopComponent() {
        // Register
        controller = Lookup.getDefault().lookup(AttributesUIControllerImpl.class);
        controller.addPropertyChangeListener(this);

        setName(NbBundle.getMessage(AttributesTopComponent.class, "CTL_AttributesTopComponent"));

        putClientProperty(TopComponent.PROP_MAXIMIZATION_DISABLED, Boolean.TRUE);

        setLayout(new BorderLayout());

        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);

        selectionPanel = new SelectionPanel();
        editPanel = new EditPanel();

        cardPanel.add(selectionPanel, SELECTION_CARD);
        cardPanel.add(editPanel, EDIT_CARD);

        cardLayout.show(cardPanel, SELECTION_CARD);

        add(cardPanel, BorderLayout.CENTER);

        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        toolbar.setRollover(true);

        columnsButton = new JButton(
            ImageUtilities.loadImageIcon("DesktopAttributes/column.svg", false));
        columnsButton.setText(
            NbBundle.getMessage(AttributesTopComponent.class, "AttributesTopComponent.columnsButton.text"));
        columnsButton.setToolTipText(
            NbBundle.getMessage(AttributesTopComponent.class, "AttributesTopComponent.columnsButton.tooltip"));
        columnsButton.setFocusable(false);
        columnsButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                showColumnsPopup(e);
            }
        });
        toolbar.add(columnsButton);

        add(toolbar, BorderLayout.SOUTH);

        // Init if needed
        AttributesUIModelImpl model = controller.getModel();
        if (model != null) {
            setup(model);
        } else {
            unsetup();
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(AttributesUIModelEvent.SELECTED_ELEMENTS) ||
            evt.getPropertyName().equals(AttributesUIModelEvent.HIDDEN_COLUMN_IDS) ||
            evt.getPropertyName().equals(AttributesUIModelEvent.SHOW_NULL_COLUMNS) ||
            evt.getPropertyName().equals(AttributesUIModelEvent.INCLUDE_PROPERTIES)) {
            refreshSelection();
        } else if (evt.getPropertyName().equals(AttributesUIModelEvent.MODEL)) {
            if (evt.getNewValue() == null) {
                unsetup();
            } else {
                setup((AttributesUIModelImpl) evt.getNewValue());
            }
        } else if (evt.getPropertyName().equals(AttributesUIModelEvent.EDIT_MODE)) {
            setup(this.model);
        }
    }

    private void refreshSelection() {
        if (model == null) {
            return;
        }
        if (model.isEditMode()) {
            editPanel.refreshSelected(model);
        } else {
            selectionPanel.refreshSelectedNodes(model);
        }
    }

    private void setup(AttributesUIModelImpl model) {
        this.model = model;

        if (model.isEditMode()) {
            cardLayout.show(cardPanel, EDIT_CARD);
        } else {
            cardLayout.show(cardPanel, SELECTION_CARD);
        }
        columnsButton.setEnabled(true);
    }

    private void unsetup() {
        this.model = null;

        cardLayout.show(cardPanel, SELECTION_CARD);
        columnsButton.setEnabled(false);
    }

    private void showColumnsPopup(MouseEvent e) {
        List<Column> columns = model.getEligibleColumns();

        JPopupMenu popup = new JPopupMenu();

        if (model.isEditMode()) {
            JCheckBoxMenuItem includeProperties = new JCheckBoxMenuItem(
                NbBundle.getMessage(AttributesTopComponent.class,
                    "AttributesTopComponent.includeProperties"));
            includeProperties.setSelected(model.isIncludeProperties());
            includeProperties.addActionListener(evt -> {
                model.setIncludeProperties(includeProperties.isSelected());
                controller.firePropertyChangeEvent(
                    AttributesUIModelEvent.INCLUDE_PROPERTIES,
                    !includeProperties.isSelected(),
                    includeProperties.isSelected());
            });
            popup.add(includeProperties);
        } else {
            JCheckBoxMenuItem showNullItem = new JCheckBoxMenuItem(
                NbBundle.getMessage(AttributesTopComponent.class,
                    "AttributesTopComponent.showNullButton"));
            showNullItem.setSelected(model.isShowNullColumns());
            showNullItem.addActionListener(evt -> {
                model.setShowNullColumns(showNullItem.isSelected());
                controller.firePropertyChangeEvent(
                    AttributesUIModelEvent.SHOW_NULL_COLUMNS,
                    !showNullItem.isSelected(),
                    showNullItem.isSelected());
            });
            popup.add(showNullItem);
        }

        popup.addSeparator();

        if (columns.isEmpty()) {
            JMenuItem emptyLabel = new JMenuItem(
                NbBundle.getMessage(AttributesTopComponent.class, "AttributesTopComponent.noColumns"));
            emptyLabel.setEnabled(false);
            popup.add(emptyLabel);
        } else {
            boolean allVisible = columns.stream().allMatch(model::isColumnVisible);
            boolean noneVisible = columns.stream().noneMatch(model::isColumnVisible);

            columns.stream()
                .map(column -> {
                    JCheckBoxMenuItem item = new JCheckBoxMenuItem(column.getTitle());
                    item.setSelected(model.isColumnVisible(column));
                    item.addActionListener(evt -> {
                        model.setColumnHidden(column, !item.isSelected());
                        controller.firePropertyChangeEvent(
                            AttributesUIModelEvent.HIDDEN_COLUMN_IDS, null, null);
                    });
                    return item;
                })
                .forEach(popup::add);

            popup.addSeparator();

            JMenuItem selectAll = new JMenuItem(
                NbBundle.getMessage(AttributesTopComponent.class, "AttributesTopComponent.selectAll"));
            selectAll.setEnabled(!allVisible);
            selectAll.addActionListener(evt -> {
                columns.forEach(column -> model.setColumnHidden(column, false));
                controller.firePropertyChangeEvent(
                    AttributesUIModelEvent.HIDDEN_COLUMN_IDS, null, null);
            });
            popup.add(selectAll);

            JMenuItem unselectAll = new JMenuItem(
                NbBundle.getMessage(AttributesTopComponent.class, "AttributesTopComponent.unselectAll"));
            unselectAll.setEnabled(!noneVisible);
            unselectAll.addActionListener(evt -> {
                columns.forEach(column -> model.setColumnHidden(column, true));
                controller.firePropertyChangeEvent(
                    AttributesUIModelEvent.HIDDEN_COLUMN_IDS, null, null);
            });
            popup.add(unselectAll);
        }

        popup.show(columnsButton, 0, -popup.getPreferredSize().height);
    }

    public EditPanel getEditPanel() {
        return editPanel;
    }

    public SelectionPanel getSelectionPanel() {
        return selectionPanel;
    }

    @Override
    public void componentOpened() {
    }

    @Override
    public void componentClosed() {
    }

    void writeProperties(java.util.Properties p) {
        p.setProperty("version", "1.0");
    }

    void readProperties(java.util.Properties p) {
        String version = p.getProperty("version");
    }

}
