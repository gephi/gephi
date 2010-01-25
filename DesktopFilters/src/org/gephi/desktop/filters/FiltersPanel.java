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
package org.gephi.desktop.filters;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.gephi.desktop.filters.library.FiltersExplorer;
import org.gephi.desktop.filters.query.QueryExplorer;
import org.gephi.filters.api.FilterController;
import org.gephi.filters.api.FilterModel;
import org.gephi.filters.api.Query;
import org.gephi.ui.utils.UIUtils;
import org.openide.explorer.ExplorerManager;
import org.openide.util.Lookup;

/**
 *
 * @author Mathieu Bastian
 */
public class FiltersPanel extends javax.swing.JPanel implements ExplorerManager.Provider, ChangeListener {

    private ExplorerManager manager = new ExplorerManager();
    //Models
    private FilterModel filterModel;
    private FilterUIModel uiModel;
    //Components
    private FilterPanelPanel filterPanelPanel;
    private QueryExplorer queriesExplorer;
    private QueriesPanel queriesPanel;

    public FiltersPanel() {
        initComponents();
        //Toolbar
        Border b = (Border) UIManager.get("Nb.Editor.Toolbar.border"); //NOI18N
        toolbar.setBorder(b);
        if (UIUtils.isAquaLookAndFeel()) {
            toolbar.setBackground(UIManager.getColor("NbExplorerView.background"));
        }

        //Components
        queriesPanel = new QueriesPanel();
        southPanel.add(queriesPanel, BorderLayout.CENTER);
        filterPanelPanel = new FilterPanelPanel();
        filtersUIPanel.add(filterPanelPanel);

        initEvents();
    }

    private void initEvents() {
        resetButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                FilterController controller = Lookup.getDefault().lookup(FilterController.class);
                for (Query query : filterModel.getQueries()) {
                    controller.remove(query);
                }
                uiModel.setSelectedQuery(null);
                controller.select(null);
                controller.filter(null);
            }
        });
        filterButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                //selectButton.setSelected(false);
                if (uiModel.getSelectedQuery() != null && filterButton.isSelected()) {
                    FilterController controller = Lookup.getDefault().lookup(FilterController.class);
                    controller.filter(uiModel.getSelectedRoot());
                } else {
                    FilterController controller = Lookup.getDefault().lookup(FilterController.class);
                    controller.filter(null);
                }
            }
        });
        selectButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                //filterButton.setSelected(false);
                if (uiModel.getSelectedQuery() != null && selectButton.isSelected()) {
                    FilterController controller = Lookup.getDefault().lookup(FilterController.class);
                    controller.select(uiModel.getSelectedRoot());
                } else {
                    FilterController controller = Lookup.getDefault().lookup(FilterController.class);
                    controller.select(null);
                }
            }
        });
        updateEnabled(false);
    }

    public void refreshModel(FilterModel filterModel, FilterUIModel uiModel) {
        this.filterModel = filterModel;
        this.uiModel = uiModel;
        //Unsetup
        unsetup();
        filterPanelPanel.unsetup();
        queriesExplorer.unsetup();
        //Setup
        setup();
        ((FiltersExplorer) libraryTree).setup(manager, filterModel, uiModel);
        queriesExplorer.setup(queriesPanel.manager, filterModel, uiModel);
        filterPanelPanel.setup(uiModel);
        updateEnabled(filterModel != null);
    }

    private class QueriesPanel extends JPanel implements ExplorerManager.Provider {

        private ExplorerManager manager = new ExplorerManager();

        public QueriesPanel() {
            super(new BorderLayout());
            queriesExplorer = new QueryExplorer();
            add(queriesExplorer, BorderLayout.CENTER);
        }

        public ExplorerManager getExplorerManager() {
            return manager;
        }
    }

    private void updateEnabled(boolean enabled) {
        resetButton.setEnabled(enabled);
        selectButton.setEnabled(enabled);
        filterButton.setEnabled(enabled);
    }

    public void stateChanged(ChangeEvent e) {
        if (e.getSource() instanceof FilterUIModel) {
            if (uiModel.getSelectedQuery() != null && filterButton.isSelected()) {
                FilterController controller = Lookup.getDefault().lookup(FilterController.class);
                controller.filter(uiModel.getSelectedRoot());
            } else if (uiModel.getSelectedQuery() != null && selectButton.isSelected()) {
                FilterController controller = Lookup.getDefault().lookup(FilterController.class);
                controller.select(uiModel.getSelectedRoot());
            }
        }
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                filterButton.setSelected(filterModel.isFiltering());
                selectButton.setSelected(filterModel.isSelecting());
            }
        });
    }

    private void unsetup() {
        if (filterModel != null) {
            filterModel.removeChangeListener(this);
        }
        if (uiModel != null) {
            uiModel.removeChangeListener(this);
        }
    }

    private void setup() {
        if (filterModel != null) {
            filterModel.addChangeListener(this);
        }
        if (uiModel != null) {
            uiModel.addChangeListener(this);
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
        java.awt.GridBagConstraints gridBagConstraints;

        toolbar = new javax.swing.JToolBar();
        resetButton = new javax.swing.JButton();
        splitPane = new javax.swing.JSplitPane();
        libraryTree = new FiltersExplorer();
        southPanel = new javax.swing.JPanel();
        filtersUIPanel = new javax.swing.JPanel();
        southToolbar = new javax.swing.JToolBar();
        buttonsPanel = new javax.swing.JPanel();
        selectButton = new javax.swing.JToggleButton();
        filterButton = new javax.swing.JToggleButton();

        setLayout(new java.awt.GridBagLayout());

        toolbar.setFloatable(false);
        toolbar.setRollover(true);

        resetButton.setText(org.openide.util.NbBundle.getMessage(FiltersPanel.class, "FiltersPanel.resetButton.text")); // NOI18N
        resetButton.setFocusable(false);
        resetButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        resetButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolbar.add(resetButton);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 1.0;
        add(toolbar, gridBagConstraints);

        splitPane.setBorder(null);
        splitPane.setDividerLocation(260);
        splitPane.setDividerSize(3);
        splitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        libraryTree.setBorder(null);
        splitPane.setLeftComponent(libraryTree);

        southPanel.setLayout(new java.awt.BorderLayout());
        splitPane.setRightComponent(southPanel);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(splitPane, gridBagConstraints);

        filtersUIPanel.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 1, 5);
        add(filtersUIPanel, gridBagConstraints);

        southToolbar.setFloatable(false);
        southToolbar.setRollover(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 5, 0, 0);
        add(southToolbar, gridBagConstraints);

        buttonsPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 4, 4));

        selectButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/gephi/desktop/filters/resources/select.png"))); // NOI18N
        selectButton.setText(org.openide.util.NbBundle.getMessage(FiltersPanel.class, "FiltersPanel.selectButton.text")); // NOI18N
        selectButton.setMargin(new java.awt.Insets(2, 7, 2, 14));
        buttonsPanel.add(selectButton);

        filterButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/gephi/desktop/filters/resources/filter.png"))); // NOI18N
        filterButton.setText(org.openide.util.NbBundle.getMessage(FiltersPanel.class, "FiltersPanel.filterButton.text")); // NOI18N
        filterButton.setMargin(new java.awt.Insets(2, 7, 2, 14));
        buttonsPanel.add(filterButton);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        add(buttonsPanel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel buttonsPanel;
    private javax.swing.JToggleButton filterButton;
    private javax.swing.JPanel filtersUIPanel;
    private javax.swing.JScrollPane libraryTree;
    private javax.swing.JButton resetButton;
    private javax.swing.JToggleButton selectButton;
    private javax.swing.JPanel southPanel;
    private javax.swing.JToolBar southToolbar;
    private javax.swing.JSplitPane splitPane;
    private javax.swing.JToolBar toolbar;
    // End of variables declaration//GEN-END:variables

    public ExplorerManager getExplorerManager() {
        return manager;
    }
}
