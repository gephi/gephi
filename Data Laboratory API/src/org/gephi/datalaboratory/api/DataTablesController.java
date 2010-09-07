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
package org.gephi.datalaboratory.api;

import org.gephi.data.attributes.api.AttributeTable;

/**
 * <p>This interface defines part of the Data Laboratory API.</p>
 * <p>It provides methods to control the Data Table UI that shows a table for nodes and edges.</p>
 * <p>This is done by registering the data table ui as a listener of these events that can be requested with this controller.
 * <b>Note that data table ui will not be registered to listen to the events of this controller until it is instanced opening Data Laboratory Group</b></p>
 * @author Eduardo Ramos <eduramiba@gmail.com>
 */
public interface DataTablesController extends DataTablesEventListener{

    /**
     * Request the tables implementation to show the given table (nodes or edges table)
     * @param table Table to show
     */
    void selectTable(AttributeTable table);

    /**
     * Register a listener for these requests.
     * @param listener Instance of DataTablesEventListener
     */
    void setDataTablesEventListener(DataTablesEventListener listener);

    /**
     * Returns the current registered DataTablesEventListener.
     * It can be null if it is still not activated or there is no active workspace.
     * @return Current listener or null
     */
    DataTablesEventListener getDataTablesEventListener();

    /**
     * Indicates if Data Table UI is registered as a listener of the events created by this controller.
     * @return True if Data Table UI is prepared, false otherwise
     */
    boolean isDataTablesReady();
}
