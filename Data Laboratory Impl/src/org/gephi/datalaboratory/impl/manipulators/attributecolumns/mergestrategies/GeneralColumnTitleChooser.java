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
package org.gephi.datalaboratory.impl.manipulators.attributecolumns.mergestrategies;

import org.gephi.data.attributes.api.AttributeTable;

/**
 * Interface that general merge strategies that only need to choose a title for the column to create
 * should implement in order to be able to use GeneralColumnTitleChooserUI.
 * @author Eduardo Ramos <eduramiba@gmail.com>
 */
public interface GeneralColumnTitleChooser {

    /**
     * Provide a initial title for the UI if needed.
     * @return Title
     */
    String getColumnTitle();

    /**
     * Called from the UI to set the final title to use.
     * @param columnTitle Title
     */
    void setColumnTitle(String columnTitle);

    /**
     * Manipulators must provide the table to use in the UI to validate the column title with this method.
     * @return Table for the new column
     */
    AttributeTable getTable();
}
