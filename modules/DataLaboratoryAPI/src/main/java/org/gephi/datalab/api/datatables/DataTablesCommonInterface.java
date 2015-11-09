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

import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Node;

/**
 * Common interface for <code>DataTablesEventListener</code> and <code>DataTablesController</code>
 * @author Eduardo
 */
interface DataTablesCommonInterface {

    /**
     * Requests the tables implementation to show nodes table.
     */
    void selectNodesTable();

    /**
     * Requests the tables implementation to show edges table.
     */
    void selectEdgesTable();

    /**
     * Requests the tables implementation to refresh the data of the table being shown.
     */
    void refreshCurrentTable();
    
    /**
     * Sets auto-refresh suspended state. True by default.
     * @param enabled 
     */
    void setAutoRefreshEnabled(boolean enabled);
    
    /**
     * Gets auto-refresh suspended state. True by default.
     * @return Current auto-refresh state
     */
    boolean isAutoRefreshEnabled();

    /**
     * Requests the tables implementation to adapt the nodes table row selection to the specified nodes.
     * @param nodes Nodes to select
     */
    void setNodeTableSelection(Node[] nodes);

    /**
     * Requests the tables implementation to adapt the edges table row selection to the specified edges.
     * @param edges Edges to select
     */
    void setEdgeTableSelection(Edge[] edges);

    /**
     * Request the tables implementation to provide the selected nodes in nodes table.
     * @return Array of selected nodes
     */
    Node[] getNodeTableSelection();

    /**
     * Request the tables implementation to provide the selected edges in edges table.
     * @return Array of selected edges
     */
    Edge[] getEdgeTableSelection();

    /**
     * Checks if the data tables implementation is showing nodes table
     * @return True if nodes table is being shown, false otherwise
     */
    boolean isNodeTableMode();

    /**
     * Checks if the data tables implementation is showing edges table
     * @return True if edges table is being shown, false otherwise
     */
    boolean isEdgeTableMode();

    /**
     * Checks if the data tables implementation is showing only visible elements (nodes or edges)
     * in the graph at the moment.
     * @return True if only visible elements are being shown, false otherwise
     */
    boolean isShowOnlyVisible();

    /**
     * Requests the tables implementation to show only visible elements or not.
     * @param showOnlyVisible Indicates if only visible elements have to be shown in table
     */
    void setShowOnlyVisible(boolean showOnlyVisible);

    /**
     * Checks if the data tables implementation is showing number lists and dynamic numbers as sparklines at the moment.
     * @return True if sparklines are on, false otherwise
     */
    boolean isUseSparklines();

    /**
     * Requests the tables implementation to show number lists and dynamic numbers as sparklines.
     * @param useSparklines Indicates if sparklines should be used
     */
    void setUseSparklines(boolean useSparklines);

    /**
     * Checks if the data tables implementation is showing time intervals as graphics at the moment.
     * @return True if sparklines are on, false otherwise
     */
    boolean isTimeIntervalGraphics();

    /**
     * Requests the tables implementation to show time intervals as graphics.
     * @param timeIntervalGraphics Indicates if time interval graphics should be used
     */
    void setTimeIntervalGraphics(boolean timeIntervalGraphics);

    /**
     * Checks if the data tables implementation is showing edges nodes (source and target) labels at the moment.
     * @return True if edges nodes lables are shown, false otherwise
     */
    boolean isShowEdgesNodesLabels();

    /**
     * Requests the tables implementation to show edges nodes (source and target).
     * @param showEdgesNodesLabels Indicates if edges nodes labels should be shown
     */
    void setShowEdgesNodesLabels(boolean showEdgesNodesLabels);

    public enum ExportMode {

        CSV
    }

    /**
     * Requests to exports current table being shown as a file.
     * @param exportMode <code>ExportMode</code> - CSV only for now
     */
    void exportCurrentTable(ExportMode exportMode);
}
