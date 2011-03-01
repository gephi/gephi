
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
package org.gephi.datalab.spi;

import org.gephi.datalab.spi.nodes.NodesManipulator;


/**
 * <p>This interface defines a common extension for the manipulators that appear as context menu items
 * such as NodesManipulator, EdgesManipulator and GraphContextMenuItem (from Visualization API)</p>
 * @author Eduardo Ramos <eduramiba@gmail.com>
 * @see NodesManipulator
 */
public interface ContextMenuItemManipulator extends Manipulator {

    /**
     * <p>This is optional. Return sub items for this menu item if desired.</p>
     * <p>If this item should contain more items, return a new instance of each sub item.
     * If not return null and implement execute for this item.</p>
     * <p>In order to declare mnemonic keys for subitem(s), the implementation of this item
     * must return the subitem(s) with the mnemonic even when it has not been setup.
     * If you don't need a mnemonic, return null if the item is not setup.</p>
     * <p>Returned items have to be of the same type as the subinterface (NodesManipulator for example)</p>
     * @return
     */
    ContextMenuItemManipulator[] getSubItems();

    /**
     * Indicates if this item has to appear in the context menu at all
     * @return True to show, false otherwise
     */
    boolean isAvailable();

    /**
     * Optional. Allows to declare a mnemonic key for this item in the menu.
     * There should not be 2 items with the same mnemonic at the same time.
     * @return Integer from <code>KeyEvent</code> values or null
     */
    Integer getMnemonicKey();
}
