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
package org.gephi.datalab.api.datatables;

import org.gephi.graph.api.Table;

/**
 * <p>This interface defines part of the Data Laboratory API.</p>
 * <p>It provides methods to control the Data Table UI that shows a table for nodes and edges.</p>
 * <p>This is done by registering the data table ui as a listener of these events that can be requested with this controller.
 * <b>Note that data table ui will not be registered to listen to the events of this controller until it is instanced opening Data Laboratory Group</b></p>
 * @author Eduardo Ramos
 */
public interface DataTablesController extends DataTablesCommonInterface {

    /**
     * Request the tables implementation to show the given table (nodes or edges table)
     * @param table Table to show
     */
    void selectTable(Table table);

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

    /**
     * Looks for an available <code>DataTablesEventListenerBuilder</code> and sets its <code>DataTablesEventListener</code>.
     * @return True if listener found, false otherwise
     */
    boolean prepareDataTables();
}
