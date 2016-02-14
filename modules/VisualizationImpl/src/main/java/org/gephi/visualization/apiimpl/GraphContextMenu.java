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
package org.gephi.visualization.apiimpl;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import org.gephi.datalab.api.DataLaboratoryHelper;
import org.gephi.datalab.spi.ContextMenuItemManipulator;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;
import org.gephi.visualization.VizController;
import org.gephi.visualization.bridge.DataBridge;
import org.gephi.visualization.model.node.NodeModel;
import org.gephi.visualization.opengl.AbstractEngine;
import org.gephi.visualization.spi.GraphContextMenuItem;
import org.openide.util.Lookup;

/**
 *
 * @author Mathieu Bastian
 */
public class GraphContextMenu {

    private final VizConfig config;
    private final AbstractEngine engine;
    private final DataBridge dataBridge;

    public GraphContextMenu() {
        config = VizController.getInstance().getVizConfig();
        engine = VizController.getInstance().getEngine();
        dataBridge = VizController.getInstance().getDataBridge();
    }

    public JPopupMenu getMenu() {
        GraphContextMenuItem[] items = getGraphContextMenuItems();
        final List<NodeModel> selectedNodeModels = engine.getSelectedNodes();
        Node[] selectedNodes = new Node[selectedNodeModels.size()];
        int i = 0;
        for (NodeModel nm : selectedNodeModels) {
            selectedNodes[i++] = nm.getNode();
        }
        final Graph graph = dataBridge.getGraph();
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
     * <p>
     * Prepares an array with one new instance of every GraphContextMenuItem and
     * returns it.</p>
     * <p>
     * It also returns the items ordered first by type and then by position.</p>
     *
     * @return Array of all GraphContextMenuItem implementations
     */
    public GraphContextMenuItem[] getGraphContextMenuItems() {
        ArrayList<GraphContextMenuItem> items = new ArrayList<>();
        items.addAll(Lookup.getDefault().lookupAll(GraphContextMenuItem.class));
        sortItems(items);
        return items.toArray(new GraphContextMenuItem[0]);
    }

    public JMenuItem createMenuItemFromGraphContextMenuItem(final GraphContextMenuItem item, final Graph graph, final Node[] nodes) {
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
                ((GraphContextMenuItem) subItem).setup(graph, nodes);
                if (lastItemType == null) {
                    lastItemType = subItem.getType();
                }
                if (lastItemType != subItem.getType()) {
                    subMenu.addSeparator();
                }
                lastItemType = subItem.getType();
                if (subItem.isAvailable()) {
                    subMenu.add(createMenuItemFromGraphContextMenuItem((GraphContextMenuItem) subItem, graph, nodes));
                }
            }
            if (item.getMnemonicKey() != null) {
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
            if (item.getMnemonicKey() != null) {
                menuItem.setMnemonic(item.getMnemonicKey());//Mnemonic for executing the action
                menuItem.setAccelerator(KeyStroke.getKeyStroke(item.getMnemonicKey(), KeyEvent.CTRL_DOWN_MASK));//And the same key mnemonic + ctrl for executing the action (and as a help display for the user!).
            }
            return menuItem;
        }
    }

    private void sortItems(ArrayList<? extends GraphContextMenuItem> m) {
        Collections.sort(m, new Comparator<GraphContextMenuItem>() {
            @Override
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
