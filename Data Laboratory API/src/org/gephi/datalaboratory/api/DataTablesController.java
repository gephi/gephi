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
package org.gephi.datalaboratory.api;


/**
 * This interface defines part of the Data Laboratory API.
 * It provides methods to control the Data Table UI that shows a table for nodes and other for edges.
 * This is done by registering the data table ui as a listener of these events that can be requested with this controller.
 * @author Eduardo Ramos <eduramiba@gmail.com>
 */
public interface DataTablesController extends DataTablesEventListener{
    void addDataTablesEventListener(DataTablesEventListener listener);

    void removeDataTablesEventListener(DataTablesEventListener listener);
}
