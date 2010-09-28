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
package org.gephi.desktop.datalab;

import java.util.ArrayList;
import org.gephi.data.attributes.api.AttributeColumn;

/**
 * Class to keep available state (in data laboratory) of the columns of a table of a workspace.
 * Useful, but also necessary to limit the maximum number of available columns when there are a lot of columns.
 * @author Eduardo
 */
public class AvailableColumnsModel {

    private static final int MAX_AVAILABLE_COLUMNS = 35;
    private ArrayList<AttributeColumn> availableColumns = new ArrayList<AttributeColumn>();

    public boolean isColumnAvailable(AttributeColumn column) {
        return availableColumns.contains(column);
    }

    /**
     * Add a column as available if it can be added.
     * @param column Column to add
     * @return True if the column was successfully added, false otherwise (no more columns can be available)
     */
    public boolean addAvailableColumn(AttributeColumn column) {
        if (canAddAvailableColumn()) {
            if (!availableColumns.contains(column)) {
                availableColumns.add(column);
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * Remove an available column from the model if possible.
     * @param column Column to make not available
     * @return True if the column could be removed
     */
    public boolean removeAvailableColumn(AttributeColumn column) {
        return availableColumns.remove(column);
    }

    /**
     * Indicates if more columns can be made available a the moment
     * @return
     */
    public boolean canAddAvailableColumn() {
        return availableColumns.size() < MAX_AVAILABLE_COLUMNS;
    }

    public AttributeColumn[] getAvailableColumns() {
        return availableColumns.toArray(new AttributeColumn[0]);
    }

    public int getAvailableColumnsCount() {
        return availableColumns.size();
    }
}
