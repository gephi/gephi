/*
Copyright 2008-2010 Gephi
Authors : Eduardo Ramos <eduramiba@gmail.com>
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
package org.gephi.desktop.datalab.utils;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeRow;
import org.gephi.datalab.api.DataLaboratoryHelper;
import org.gephi.datalab.spi.ContextMenuItemManipulator;
import org.gephi.datalab.spi.Manipulator;
import org.gephi.datalab.spi.edges.EdgesManipulator;
import org.gephi.datalab.spi.nodes.NodesManipulator;
import org.gephi.datalab.spi.values.AttributeValueManipulator;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 * Utils for building popup menus at right click on nodes/edges rows.
 * @author Eduardo Ramos <eduramiba@gmail.com>
 */
public class PopupMenuUtils {

    public static JMenuItem createMenuItemFromNodesManipulator(final NodesManipulator item, final Node clickedNode,final Node[] nodes) {
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
                ((NodesManipulator)subItem).setup(nodes,clickedNode);
                if (lastItemType == null) {
                    lastItemType = subItem.getType();
                }
                if (lastItemType != subItem.getType()) {
                    subMenu.addSeparator();
                }
                lastItemType = subItem.getType();
                if (subItem.isAvailable()) {
                    subMenu.add(createMenuItemFromNodesManipulator((NodesManipulator) subItem,clickedNode,nodes));
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

    public static JMenuItem createMenuItemFromEdgesManipulator(final EdgesManipulator item, final Edge clickedEdge,final Edge[] edges) {
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
                ((EdgesManipulator)subItem).setup(edges,clickedEdge);
                if (lastItemType == null) {
                    lastItemType = subItem.getType();
                }
                if (lastItemType != subItem.getType()) {
                    subMenu.addSeparator();
                }
                lastItemType = subItem.getType();
                if (subItem.isAvailable()) {
                    subMenu.add(createMenuItemFromEdgesManipulator((EdgesManipulator) subItem,clickedEdge,edges));
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

    public static JMenuItem createMenuItemFromManipulator(final Manipulator nm) {
        JMenuItem menuItem = new JMenuItem();
        menuItem.setText(nm.getName());
        if (nm.getDescription() != null && !nm.getDescription().isEmpty()) {
            menuItem.setToolTipText(nm.getDescription());
        }
        menuItem.setIcon(nm.getIcon());
        if (nm.canExecute()) {
            menuItem.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    DataLaboratoryHelper dlh = DataLaboratoryHelper.getDefault();
                    dlh.executeManipulator(nm);
                }
            });
        } else {
            menuItem.setEnabled(false);
        }
        return menuItem;
    }

    public static JMenu createSubMenuFromRowColumn(AttributeRow row, AttributeColumn column) {
        DataLaboratoryHelper dlh = DataLaboratoryHelper.getDefault();
        JMenu subMenu = new JMenu(NbBundle.getMessage(PopupMenuUtils.class, "Cell.Popup.subMenu.text"));
        subMenu.setIcon(ImageUtilities.loadImageIcon("org/gephi/desktop/datalab/resources/table-select.png", true));

        Integer lastManipulatorType = null;
        for (AttributeValueManipulator am : dlh.getAttributeValueManipulators()) {
            am.setup(row, column);
            if (lastManipulatorType == null) {
                lastManipulatorType = am.getType();
            }
            if (lastManipulatorType != am.getType()) {
                subMenu.addSeparator();
            }
            lastManipulatorType = am.getType();
            subMenu.add(PopupMenuUtils.createMenuItemFromManipulator(am));
        }
        if(subMenu.getMenuComponentCount()==0){
            subMenu.setEnabled(false);
        }
        return subMenu;
    }
}
