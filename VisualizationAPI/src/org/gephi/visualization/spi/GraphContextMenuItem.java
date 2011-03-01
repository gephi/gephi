
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
package org.gephi.visualization.spi;

import org.gephi.datalab.spi.ContextMenuItemManipulator;
import org.gephi.graph.api.HierarchicalGraph;
import org.gephi.graph.api.Node;

/**
 * <p><b>Please note that the methods offered in this service are the same as Data Laboratory nodes manipulators.
 * It is possible to reuse actions implementations by adding both <code>ServiceProvider</code> annotations.</b></p>
 * <p>Interface from providing graph context menu items as services.</p>
 * <p>All context menu items are able to:</p>
 * <ul>
 *  <li>Execute an action</li>
 *  <li>Provide a name, type and order of appearance (position in group of its type)</li>
 * <li>Indicate wether they have to be available (appear in the context menu) or not</li>
 *  <li>Indicate wether they have to be executable (enabled in the context menu) or not</li>
 *  <li>Provide and icon or not</li>
 * </ul>
 * <p>Used for different manipulators such as NodesManipulator, EdgesManipulator and GeneralActionsManipulator.</p>
 * <p>The only methods that are called before setting up an item with the data are <b>getSubItems, getType and getPosition.</b>
 * This way, the other methods behaviour can depend on the data that has been setup before</p>
 * <p><b>getSubItems will be called before and after setup. Take care when the nodes are null!</b></p>
 *
 * To provide a context menu item, a class has to implement this interface and have a <code>@ServiceProvider</code> annotation
 * @author Eduardo Ramos <eduramiba@gmail.com>
 */
public interface GraphContextMenuItem extends ContextMenuItemManipulator{

    /**
     * Prepare nodes for this item. Note that nodes could contain 0 nodes.
     * @param graph Hierarchical graph
     * @param nodes All selected nodes
     */
    void setup(HierarchicalGraph graph, Node[] nodes);
}
