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
package org.gephi.datalab.plugin.manipulators;

import org.gephi.datalab.plugin.manipulators.nodes.CopyNodeDataToOtherNodes;

/**
 * Interface in common for choosing columns to manipulate.
 * Used to be able to get/set the columns to copy and row (node or edge) to user in the GeneralChooseColumnsAndRowUI.
 * @author Eduardo Ramos <eduramiba@gmail.com>
 * @see CopyNodeDataToOtherNodes
 */
public interface GeneralColumnsAndRowChooser extends GeneralColumnsChooser{

    /**
     * Provide rows (nodes or edges) to show in the GeneralChooseColumnsAndRowUI to be selected or not.
     * @return Nodes or edges set to select one
     */
    Object[] getRows();

    /**
     * Provide initially selected node or edge in the GeneralChooseColumnsAndRowUI
     * @return Initially selected node or edge
     */
    Object getRow();

    /**
     * The GeneralChooseColumnsAndRowUI will use this method to set the row to finally manipulate, after the GeneralChooseColumnsAndRowUI is closed.
     * @param row Selected node or edge depending on the manipulator
     */
    void setRow(Object row);
}
