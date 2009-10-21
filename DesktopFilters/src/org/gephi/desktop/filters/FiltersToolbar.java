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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import org.gephi.filters.api.FilterBuilder;
import org.gephi.ui.components.JDropDownButton;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.MenuView;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Node;
import org.openide.nodes.NodeAcceptor;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Mathieu Bastian
 */
public class FiltersToolbar extends JToolBar implements ExplorerManager.Provider {

    private final ExplorerManager manager = new ExplorerManager();

    //Components
    private JToggleButton filterToggleButton;
    private JToggleButton selectToggleButton;
    private JButton copyToWorkspaceButton;
    private JButton cleanButton;
    private JMenu addFilterMenu;

    public FiltersToolbar() {
        setFloatable(false);
        setRollover(true);

        //Add Filter
        FilterBuilder[] filters = Lookup.getDefault().lookupAll(FilterBuilder.class).toArray(new FilterBuilder[0]);
        manager.setRootContext(new AbstractNode(new FiltersNodeChildren(filters)));

        initComponents();
        initEvents();
    }

    private void initComponents() {
        ButtonGroup resultMethod = new ButtonGroup();
        filterToggleButton = new JToggleButton();
        filterToggleButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/gephi/desktop/filters/resources/filter.png")));
        filterToggleButton.setToolTipText(NbBundle.getMessage(FiltersToolbar.class, "FiltersToolbar.filterButton.tooltiptext"));
        resultMethod.add(filterToggleButton);
        selectToggleButton = new JToggleButton();
        selectToggleButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/gephi/desktop/filters/resources/select.png")));
        selectToggleButton.setToolTipText(NbBundle.getMessage(FiltersToolbar.class, "FiltersToolbar.selectButton.tooltiptext"));
        resultMethod.add(selectToggleButton);

        add(filterToggleButton);
        add(selectToggleButton);
        addSeparator();

        addFilterMenu = new MenuView.Menu(manager.getRootContext(), new NodeAcceptor() {

            public boolean acceptNodes(Node[] nodes) {
                if (nodes.length > 0 && nodes[0].getActions(false).length > 0) {
                    nodes[0].getActions(false)[0].actionPerformed(null);
                }
                return true;
            }
        });
        JDropDownButton addFilterButton = new JDropDownButton(new javax.swing.ImageIcon(getClass().getResource("/org/gephi/desktop/filters/resources/addFilter.png")), addFilterMenu.getPopupMenu());
        add(addFilterButton);

        copyToWorkspaceButton = new JButton();
        copyToWorkspaceButton.setToolTipText(NbBundle.getMessage(FiltersToolbar.class, "FiltersToolbar.copyToWorkspaceButton.tooltiptext"));
        copyToWorkspaceButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/gephi/desktop/filters/resources/copyWorkspace.png")));

        cleanButton = new JButton();
        cleanButton.setToolTipText(NbBundle.getMessage(FiltersToolbar.class, "FiltersToolbar.cleanButton.tooltiptext"));
        cleanButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/gephi/desktop/filters/resources/clean.gif")));

        add(copyToWorkspaceButton);
        add(cleanButton);

        NbBundle.getMessage(FiltersToolbar.class, "FiltersToolbar.addFilterButton.tooltiptext");
    }

    private void initEvents() {
        filterToggleButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
            }
        });
        selectToggleButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
            }
        });
    }

    public ExplorerManager getExplorerManager() {
        return manager;
    }
}
