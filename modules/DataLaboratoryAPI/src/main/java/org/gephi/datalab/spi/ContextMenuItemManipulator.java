
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
package org.gephi.datalab.spi;

import org.gephi.datalab.spi.nodes.NodesManipulator;


/**
 * <p>This interface defines a common extension for the manipulators that appear as context menu items
 * such as NodesManipulator, EdgesManipulator and GraphContextMenuItem (from Visualization API)</p>
 * @author Eduardo Ramos
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
