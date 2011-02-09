/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
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
package org.gephi.visualization.apiimpl;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import org.gephi.datalab.api.DataLaboratoryHelper;
import org.gephi.datalab.spi.ContextMenuItemManipulator;
import org.gephi.graph.api.HierarchicalGraph;
import org.gephi.graph.api.Node;
import org.gephi.visualization.VizController;
import org.gephi.visualization.bridge.DHNSEventBridge;
import org.gephi.visualization.spi.GraphContextMenuItem;
import org.openide.util.Lookup;

/**
 *
 * @author Mathieu Bastian
 */
public class GraphContextMenu {

    private VizConfig config;
    private DHNSEventBridge eventBridge;

    public GraphContextMenu() {
        config = VizController.getInstance().getVizConfig();
        eventBridge = (DHNSEventBridge) VizController.getInstance().getEventBridge();
    }

    public JPopupMenu getMenu() {
        GraphContextMenuItem[] items = getGraphContextMenuItems();
        final Node[] selectedNodes = eventBridge.getSelectedNodes();
        final HierarchicalGraph graph = eventBridge.getGraph();
        JPopupMenu contextMenu = new JPopupMenu();

        //Add items ordered:
        Integer lastItemType = null;
        for (GraphContextMenuItem item : items) {
            item.setup(graph, selectedNodes);
            if (lastItemType == null) {
                lastItemType = item.getType();
            }
            if (lastItemType != item.getType()) {
                contextMenu.addSeparator();
            }
            lastItemType = item.getType();
            if (item.isAvailable()) {
                contextMenu.add(createMenuItemFromGraphContextMenuItem(item, graph, selectedNodes));
            }
        }

        return contextMenu;
    }

    /**
     * <p>Prepares an array with one new instance of every GraphContextMenuItem and returns it.</p>
     * <p>It also returns the items ordered first by type and then by position.</p>
     * @return Array of all GraphContextMenuItem implementations
     */
    public GraphContextMenuItem[] getGraphContextMenuItems() {
        ArrayList<GraphContextMenuItem> items = new ArrayList<GraphContextMenuItem>();
        items.addAll(Lookup.getDefault().lookupAll(GraphContextMenuItem.class));
        sortItems(items);
        return items.toArray(new GraphContextMenuItem[0]);
    }

    public JMenuItem createMenuItemFromGraphContextMenuItem(final GraphContextMenuItem item, final HierarchicalGraph graph, final Node[] nodes) {
        ContextMenuItemManipulator[] subItems = item.getSubItems();
        if (subItems != null && item.canExecute()) {
            JMenu subMenu = new JMenu();
            subMenu.setText(item.getName());
            if (item.getDescription() != null && !item.getDescription().isEmpty()) {
                subMenu.setToolTipText(item.getDescription());
            }
            subMenu.setIcon(item.getIcon());
            Integer lastItemType = null;
            for (ContextMenuItemManipulator subItem : subItems) {
                ((GraphContextMenuItem)subItem).setup(graph, nodes);
                if (lastItemType == null) {
                    lastItemType = subItem.getType();
                }
                if (lastItemType != subItem.getType()) {
                    subMenu.addSeparator();
                }
                lastItemType = subItem.getType();
                if (subItem.isAvailable()) {
                    subMenu.add(createMenuItemFromGraphContextMenuItem((GraphContextMenuItem)subItem, graph, nodes));
                }
            }
            if(item.getMnemonicKey()!=null){
                subMenu.setMnemonic(item.getMnemonicKey());//Mnemonic for opening a sub menu
            }
            return subMenu;
        } else {
            JMenuItem menuItem = new JMenuItem();
            menuItem.setText(item.getName());
            if (item.getDescription() != null && !item.getDescription().isEmpty()) {
                menuItem.setToolTipText(item.getDescription());
            }
            menuItem.setIcon(item.getIcon());
            if (item.canExecute()) {
                menuItem.addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent e) {
                        new Thread() {

                            @Override
                            public void run() {
                                DataLaboratoryHelper.getDefault().executeManipulator(item);
                            }
                        }.start();
                    }
                });
            } else {
                menuItem.setEnabled(false);
            }
            if(item.getMnemonicKey()!=null){
                menuItem.setMnemonic(item.getMnemonicKey());//Mnemonic for executing the action
                menuItem.setAccelerator(KeyStroke.getKeyStroke(item.getMnemonicKey(),KeyEvent.CTRL_DOWN_MASK));//And the same key mnemonic + ctrl for executing the action (and as a help display for the user!).
            }
            return menuItem;
        }
    }

    private void sortItems(ArrayList<? extends GraphContextMenuItem> m) {
        Collections.sort(m, new Comparator<GraphContextMenuItem>() {

            public int compare(GraphContextMenuItem o1, GraphContextMenuItem o2) {
                //Order by type, position.
                if (o1.getType() == o2.getType()) {
                    return o1.getPosition() - o2.getPosition();
                } else {
                    return o1.getType() - o2.getType();
                }
            }
        });
    }
}
