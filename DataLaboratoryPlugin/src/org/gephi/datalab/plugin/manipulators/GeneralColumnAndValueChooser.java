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

import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeTable;
import org.gephi.datalab.plugin.manipulators.nodes.TagNodes;

/**
 * Interface in common for choosing a column to manipulate from a list and a String value.
 * Used to be able to mass-tag a column of nodes/edges.
 * @author Eduardo Ramos <eduramiba@gmail.com>
 * @see FillNodesColumnWithValue
 */
public interface GeneralColumnAndValueChooser{

    /**
     * Provide columns to show in the UI to select one.
     * Normally provide all table columns that can be manipulated.
     * @return Columns to show in the GeneralColumnAndValueChooserUI
     */
    AttributeColumn[] getColumns();

    /**
     * Provide table for auto-completion of column values
     * @return
     */
    AttributeTable getTable();

    /**
     * The GeneralColumnAndValueChooserUI will use this method to set the column to finally manipulate, after the GeneralColumnAndValueChooserUI is closed.
     * @param columnsToClearData Column to manipulate
     */
    void setColumn(AttributeColumn column);

    /**
     * The GeneralColumnAndValueChooserUI will use this method to set the String value to finally use, after the GeneralColumnAndValueChooserUI is closed.
     * @param columnsToClearData Column to manipulate
     */
    void setValue(String value);

    /**
     * Provide title for the GeneralColumnAndValueChooserUI.
     * @return Title name
     */
    String getName();
}
