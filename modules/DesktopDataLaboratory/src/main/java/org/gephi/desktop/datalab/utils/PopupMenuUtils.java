/*
Copyright 2008-2010 Gephi
Authors : Eduardo Ramos <eduramiba@gmail.com>
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
package org.gephi.desktop.datalab.utils;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import org.gephi.graph.api.Column;
import org.gephi.datalab.api.DataLaboratoryHelper;
import org.gephi.datalab.spi.ContextMenuItemManipulator;
import org.gephi.datalab.spi.Manipulator;
import org.gephi.datalab.spi.edges.EdgesManipulator;
import org.gephi.datalab.spi.nodes.NodesManipulator;
import org.gephi.datalab.spi.values.AttributeValueManipulator;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Element;
import org.gephi.graph.api.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 * Utils for building popup menus at right click on nodes/edges rows.
 * @author Eduardo Ramos
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

                    @Override
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

                    @Override
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

                @Override
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

    public static JMenu createSubMenuFromRowColumn(Element row, Column column) {
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
