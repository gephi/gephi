/*
Copyright 2008-2010 Gephi
Authors : Eduardo Ramos <eduramiba@gmail.com>
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
package org.gephi.datalaboratory.impl.manipulators;

import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.datalaboratory.impl.manipulators.nodes.ClearNodesData;

/**
 * Interface in common for clearing a row data (node or edge).
 * Used to be able to get/set the columns to clear in the GeneralClearRowDataUI.
 * @author Eduardo Ramos <eduramiba@gmail.com>
 * @see ClearNodesData
 */
public interface GeneralClearRowData{

    /**
     * Provide columns to show in the UI to be selected or not to clear.
     * Normally provide al table columns that can be cleared
     * @return Columns to show in the GeneralClearRowDataUI
     */
    AttributeColumn[] getColumnsToClearData();

    /**
     * The GeneralClearRowDataUI will use this method to set the columns to finally clear.
     * @param columnsToClearData Columns to clear
     */
    void setColumnsToClearData(AttributeColumn[] columnsToClearData);
}
