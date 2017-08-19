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
package org.gephi.desktop.filters;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
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
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.explorer.ExplorerManager;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Mathieu Bastian
 */
public class FiltersPanel extends javax.swing.JPanel implements ExplorerManager.Provider, ChangeListener {

    private final ExplorerManager manager = new ExplorerManager();
    //Models
    private FilterModel filterModel;
    private FilterUIModel uiModel;
    //Components
    private final FilterPanelPanel filterPanelPanel;
    private QueryExplorer queriesExplorer;
    private final QueriesPanel queriesPanel;

    public FiltersPanel() {
        initComponents();
        //Toolbar
        Border b = (Border) UIManager.get("Nb.Editor.Toolbar.border"); //NOI18N
        toolbar.setBorder(b);
        if (UIUtils.isAquaLookAndFeel()) {
            toolbar.setBackground(UIManager.getColor("NbExplorerView.background"));
            setBackground(UIManager.getColor("NbExplorerView.background"));
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

            @Override
            public void actionPerformed(ActionEvent e) {
                FilterController controller = Lookup.getDefault().lookup(FilterController.class);
                for (Query query : filterModel.getQueries()) {
                    controller.remove(query);
                }
//                uiModel.setSelectedQuery(null);
                controller.selectVisible(null);
                controller.filterVisible(null);
                ((FiltersExplorer) libraryTree).setup(manager, filterModel, uiModel);
                stopButton.setVisible(false);
                filterButton.setVisible(true);
            }
        });
        filterButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                //selectButton.setSelected(false);
                if (uiModel.getSelectedQuery() != null) {
                    FilterController controller = Lookup.getDefault().lookup(FilterController.class);
                    controller.filterVisible(uiModel.getSelectedRoot());
                    stopButton.setSelected(false);
                    stopButton.setVisible(true);
                    filterButton.setVisible(false);
                }
            }
        });
        stopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                FilterController controller = Lookup.getDefault().lookup(FilterController.class);
                controller.filterVisible(null);
                controller.selectVisible(null);
                stopButton.setVisible(false);
                filterButton.setSelected(true);
                filterButton.setVisible(true);
            }
        });
        selectButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                FilterController controller = Lookup.getDefault().lookup(FilterController.class);
                if (controller.getModel().isSelecting()) {
                    controller.selectVisible(null);
                } else {
                    if (uiModel.getSelectedQuery() != null) {
                        controller.selectVisible(uiModel.getSelectedRoot());
                    }
                }
            }
        });
        exportColumnButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (uiModel.getSelectedQuery() != null) {
                    FilterController controller = Lookup.getDefault().lookup(FilterController.class);
                    NotifyDescriptor.InputLine question = new NotifyDescriptor.InputLine(
                            NbBundle.getMessage(FiltersPanel.class, "FiltersPanel.exportColumn.input"),
                            NbBundle.getMessage(FiltersPanel.class, "FiltersPanel.exportColumn.input.title"));
                    if (DialogDisplayer.getDefault().notify(question) == NotifyDescriptor.OK_OPTION) {
                        String input = question.getInputText();
                        if (input != null && !input.isEmpty()) {
                            controller.exportToColumn(input, uiModel.getSelectedRoot());
                        }
                    }
                }
            }
        });
        exportWorkspaceButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (uiModel.getSelectedQuery() != null) {
                    FilterController controller = Lookup.getDefault().lookup(FilterController.class);
                    controller.exportToNewWorkspace(uiModel.getSelectedRoot());
                }
            }
        });
        exportLabelVisible.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (uiModel.getSelectedQuery() != null) {
                    FilterController controller = Lookup.getDefault().lookup(FilterController.class);
                    controller.exportToLabelVisible(uiModel.getSelectedRoot());
                }
            }
        });
        
        updateEnabled(false);
    }

    public void refreshModel(FilterModel filterModel, FilterUIModel uiModel) {
        //System.out.println("refresh filter model thread=" + Thread.currentThread().getName());
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
        updateControls();
    }

    private class QueriesPanel extends JPanel implements ExplorerManager.Provider {

        private ExplorerManager manager = new ExplorerManager();

        public QueriesPanel() {
            super(new BorderLayout());
            queriesExplorer = new QueryExplorer();
            add(queriesExplorer, BorderLayout.CENTER);
        }

        @Override
        public ExplorerManager getExplorerManager() {
            return manager;
        }
    }

    private void updateEnabled(final boolean enabled) {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                resetButton.setEnabled(enabled);
                selectButton.setEnabled(enabled && uiModel.getSelectedQuery() != null);
                filterButton.setEnabled(enabled && uiModel.getSelectedQuery() != null);
                /*autoRefreshButton.setEnabled(enabled);*/
                exportColumnButton.setEnabled(enabled && uiModel.getSelectedQuery() != null && filterModel.getCurrentQuery() != null);
                exportWorkspaceButton.setEnabled(enabled && uiModel.getSelectedQuery() != null && filterModel.getCurrentQuery() != null);
                exportLabelVisible.setEnabled(enabled && uiModel.getSelectedQuery() != null && filterModel.getCurrentQuery() != null);
            }
        });
    }

    private void updateControls() {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                if (filterModel != null) {
                    if (filterModel.isFiltering()) {
                        stopButton.setVisible(true);
                        stopButton.setSelected(false);
                        filterButton.setVisible(false);
                    } else {
                        stopButton.setVisible(false);
                        filterButton.setSelected(false);
                        filterButton.setVisible(true);
                    }
                    
                    selectButton.setSelected(filterModel.isSelecting());
                } else {
                    stopButton.setVisible(false);
                    filterButton.setVisible(true);
                    filterButton.setSelected(false);
                    selectButton.setSelected(false);
                }
            }
        });

    }

    @Override
    public void stateChanged(ChangeEvent e) {
        if (e.getSource() instanceof FilterUIModel) {
            if (uiModel.getSelectedQuery() != null && stopButton.isVisible()) {
                FilterController controller = Lookup.getDefault().lookup(FilterController.class);
                controller.filterVisible(uiModel.getSelectedRoot());
            } else if (uiModel.getSelectedQuery() != null && selectButton.isSelected()) {
                FilterController controller = Lookup.getDefault().lookup(FilterController.class);
                controller.selectVisible(uiModel.getSelectedRoot());
            }
        } else if (e.getSource() instanceof FilterModel) {
            if (uiModel.getSelectedQuery() != null && filterModel.getCurrentQuery() == null) {
                //Remove case
                if (!Arrays.asList(filterModel.getQueries()).contains(uiModel.getSelectedRoot())) {
                    uiModel.setSelectedQuery(null);
                }
            } else if (filterModel.getCurrentQuery() != null
                    && filterModel.getCurrentQuery() != uiModel.getSelectedQuery()
                    && filterModel.getCurrentQuery() != uiModel.getSelectedRoot()) {
                uiModel.setSelectedQuery(filterModel.getCurrentQuery());
            }
        }
        updateControls();
        updateEnabled(true);
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

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        toolbar = new javax.swing.JToolBar();
        resetButton = new javax.swing.JButton();
        separator = new javax.swing.JToolBar.Separator();
        exportColumnButton = new javax.swing.JButton();
        exportWorkspaceButton = new javax.swing.JButton();
        exportLabelVisible = new javax.swing.JButton();
        splitPane = new javax.swing.JSplitPane();
        libraryTree = new FiltersExplorer();
        southPanel = new javax.swing.JPanel();
        filtersUIPanel = new javax.swing.JPanel();
        southToolbar = new javax.swing.JToolBar();
        buttonsPanel = new javax.swing.JPanel();
        selectButton = new javax.swing.JToggleButton();
        filterButton = new javax.swing.JToggleButton();
        stopButton = new javax.swing.JToggleButton();

        setLayout(new java.awt.GridBagLayout());

        toolbar.setFloatable(false);
        toolbar.setRollover(true);

        resetButton.setText(org.openide.util.NbBundle.getMessage(FiltersPanel.class, "FiltersPanel.resetButton.text")); // NOI18N
        resetButton.setFocusable(false);
        resetButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        resetButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolbar.add(resetButton);
        toolbar.add(separator);

        exportColumnButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/gephi/desktop/filters/resources/table_export.png"))); // NOI18N
        exportColumnButton.setText(org.openide.util.NbBundle.getMessage(FiltersPanel.class, "FiltersPanel.exportColumnButton.text")); // NOI18N
        exportColumnButton.setToolTipText(org.openide.util.NbBundle.getMessage(FiltersPanel.class, "FiltersPanel.exportColumnButton.toolTipText")); // NOI18N
        exportColumnButton.setFocusable(false);
        exportColumnButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        exportColumnButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolbar.add(exportColumnButton);

        exportWorkspaceButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/gephi/desktop/filters/resources/workspace_export.png"))); // NOI18N
        exportWorkspaceButton.setText(org.openide.util.NbBundle.getMessage(FiltersPanel.class, "FiltersPanel.exportWorkspaceButton.text")); // NOI18N
        exportWorkspaceButton.setToolTipText(org.openide.util.NbBundle.getMessage(FiltersPanel.class, "FiltersPanel.exportWorkspaceButton.toolTipText")); // NOI18N
        exportWorkspaceButton.setFocusable(false);
        exportWorkspaceButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        exportWorkspaceButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolbar.add(exportWorkspaceButton);

        exportLabelVisible.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/gephi/desktop/filters/resources/labelvisible_export.png"))); // NOI18N
        exportLabelVisible.setToolTipText(org.openide.util.NbBundle.getMessage(FiltersPanel.class, "FiltersPanel.exportLabelVisible.toolTipText")); // NOI18N
        exportLabelVisible.setFocusable(false);
        exportLabelVisible.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        exportLabelVisible.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolbar.add(exportLabelVisible);

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
        southToolbar.setOpaque(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 5, 0, 0);
        add(southToolbar, gridBagConstraints);

        buttonsPanel.setOpaque(false);
        buttonsPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 4, 4));

        selectButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/gephi/desktop/filters/resources/select.png"))); // NOI18N
        selectButton.setText(org.openide.util.NbBundle.getMessage(FiltersPanel.class, "FiltersPanel.selectButton.text")); // NOI18N
        selectButton.setMargin(new java.awt.Insets(2, 7, 2, 14));
        buttonsPanel.add(selectButton);

        filterButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/gephi/desktop/filters/resources/filter.png"))); // NOI18N
        filterButton.setText(org.openide.util.NbBundle.getMessage(FiltersPanel.class, "FiltersPanel.filterButton.text")); // NOI18N
        filterButton.setFocusable(false);
        filterButton.setMargin(new java.awt.Insets(2, 7, 2, 14));
        buttonsPanel.add(filterButton);

        stopButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/gephi/desktop/filters/resources/stop.png"))); // NOI18N
        stopButton.setText(org.openide.util.NbBundle.getMessage(FiltersPanel.class, "FiltersPanel.stopButton.text")); // NOI18N
        stopButton.setMargin(new java.awt.Insets(2, 7, 2, 14));
        buttonsPanel.add(stopButton);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        add(buttonsPanel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel buttonsPanel;
    private javax.swing.JButton exportColumnButton;
    private javax.swing.JButton exportLabelVisible;
    private javax.swing.JButton exportWorkspaceButton;
    private javax.swing.JToggleButton filterButton;
    private javax.swing.JPanel filtersUIPanel;
    private javax.swing.JScrollPane libraryTree;
    private javax.swing.JButton resetButton;
    private javax.swing.JToggleButton selectButton;
    private javax.swing.JToolBar.Separator separator;
    private javax.swing.JPanel southPanel;
    private javax.swing.JToolBar southToolbar;
    private javax.swing.JSplitPane splitPane;
    private javax.swing.JToggleButton stopButton;
    private javax.swing.JToolBar toolbar;
    // End of variables declaration//GEN-END:variables

    @Override
    public ExplorerManager getExplorerManager() {
        return manager;
    }
}
