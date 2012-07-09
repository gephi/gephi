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
package org.gephi.datalab.impl;

import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeTable;
import org.gephi.datalab.api.datatables.DataTablesController;
import org.gephi.datalab.api.datatables.DataTablesEventListener;
import org.gephi.datalab.api.datatables.DataTablesEventListenerBuilder;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Node;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 * Implementation of the DataTablesController interface
 * declared in the Data Laboratory API.
 * @author Eduardo Ramos <eduramiba@gmail.com>
 * @see DataTablesController
 */
@ServiceProvider(service = DataTablesController.class)
public class DataTablesControllerImpl implements DataTablesController {

    DataTablesEventListener listener;

    public void setDataTablesEventListener(DataTablesEventListener listener) {
        this.listener = listener;
    }

    public DataTablesEventListener getDataTablesEventListener() {
        return listener;
    }

    public boolean isDataTablesReady() {
        return listener!=null;
    }

    public void selectNodesTable() {
        if (listener != null) {
            listener.selectNodesTable();
        }
    }

    public void selectEdgesTable() {
        if (listener != null) {
            listener.selectEdgesTable();
        }
    }

    public void selectTable(AttributeTable table) {
        if (listener != null) {
            AttributeController ac = Lookup.getDefault().lookup(AttributeController.class);
            if (ac.getModel().getNodeTable() == table) {
                selectNodesTable();
            } else {
                selectEdgesTable();
            }
        }
    }

    public void refreshCurrentTable() {
        if (listener != null) {
            listener.refreshCurrentTable();
        }
    }

    public void setNodeTableSelection(Node[] nodes) {
        if (listener != null) {
            listener.setNodeTableSelection(nodes);
        }
    }

    public void setEdgeTableSelection(Edge[] edges) {
        if (listener != null) {
            listener.setEdgeTableSelection(edges);
        }
    }

    public Node[] getNodeTableSelection() {
        if (listener != null) {
            return listener.getNodeTableSelection();
        } else {
            return null;
        }
    }

    public Edge[] getEdgeTableSelection() {
        if (listener != null) {
            return listener.getEdgeTableSelection();
        } else {
            return null;
        }
    }

    public boolean isNodeTableMode() {
        if (listener != null) {
            return listener.isNodeTableMode();
        } else {
            return false;
        }
    }

    public boolean isEdgeTableMode() {
        if (listener != null) {
            return listener.isEdgeTableMode();
        } else {
            return false;
        }
    }

    public boolean isShowOnlyVisible() {
        if (listener != null) {
            return listener.isShowOnlyVisible();
        } else {
            return false;
        }
    }

    public void setShowOnlyVisible(boolean showOnlyVisible){
        if (listener != null) {
            listener.setShowOnlyVisible(showOnlyVisible);
        }
    }

    public void exportCurrentTable(ExportMode exportMode) {
        if (listener != null) {
            listener.exportCurrentTable(exportMode);
        }
    }

    public boolean isUseSparklines() {
        if (listener != null) {
            return listener.isUseSparklines();
        } else {
            return false;
        }
    }

    public void setUseSparklines(boolean useSparklines) {
        if (listener != null) {
            listener.setUseSparklines(useSparklines);
        }
    }

    public boolean isTimeIntervalGraphics() {
        if (listener != null) {
            return listener.isTimeIntervalGraphics();
        } else {
            return false;
        }
    }

    public void setTimeIntervalGraphics(boolean timeIntervalGraphics) {
        if (listener != null) {
            listener.setTimeIntervalGraphics(timeIntervalGraphics);
        }
    }

    public boolean isShowEdgesNodesLabels() {
        if (listener != null) {
            return listener.isShowEdgesNodesLabels();
        } else {
            return false;
        }
    }

    public void setShowEdgesNodesLabels(boolean showEdgesNodesLabels) {
        if (listener != null) {
            listener.setShowEdgesNodesLabels(showEdgesNodesLabels);
        }
    }

    public boolean prepareDataTables() {
        DataTablesEventListenerBuilder builder=Lookup.getDefault().lookup(DataTablesEventListenerBuilder.class);
        if(builder!=null){
            listener=builder.getDataTablesEventListener();
        }
        return isDataTablesReady();
    }
}
